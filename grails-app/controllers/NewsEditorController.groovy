import com.tds.asset.AssetComment
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventNews
import net.transitionmanager.domain.Project
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.UserPreferenceService
import org.springframework.jdbc.core.JdbcTemplate

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class NewsEditorController implements ControllerMethods {

	ControllerService controllerService
	JdbcTemplate jdbcTemplate
	UserPreferenceService userPreferenceService

	static defaultAction = 'newsEditorList'

	/**
	 * Union of assets issues and move event news
	 */
	@HasPermission(Permission.NewsView)
	def newsEditorList() {
		Project project = controllerService.getProjectForPage(this, 'to view News')
		if (!project) return

		def moveEventId = params.moveEvent
		def moveEvent
		if (moveEventId) {
			//userPreferenceService.setUserCurrentMoveEventId(moveEventId.toString())
			userPreferenceService.setPreference(securityService.loadCurrentUserLogin(), PREF.MOVE_EVENT, moveEventId.toString())
			moveEvent = MoveEvent.get(moveEventId)
		}
		else {
			def defaultEvent = userPreferenceService.getPreference(PREF.MOVE_EVENT)
			if (defaultEvent) {
				moveEvent = MoveEvent.get(defaultEvent)
				if (moveEvent?.projectId != project.id) {
					moveEvent = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[project])
				}
			} else {
				moveEvent = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[project])
			}
		}

		List<MoveBundle> moveBundles
		if (moveEvent) {
			moveBundles = MoveBundle.executeQuery(
					'from MoveBundle where moveEvent=:moveEvent order by name asc',
					[moveEvent: moveEvent])
		} else {
			moveBundles = MoveBundle.executeQuery(
					'from MoveBundle where project=:project order by name asc',
					[project: project]
			)
		}

		[moveEventId: moveEvent == null ? null : moveEvent.id, viewFilter: params.viewFilter, bundleId: params.moveBundle,
		 moveBundlesList: moveBundles, moveEventsList: MoveEvent.findAllByProject(project)]
	}

	@HasPermission(Permission.NewsView)
	def listEventNewsJson() {

		Project project = securityService.userCurrentProject
		String viewFilter = params.viewFilter
		MoveEvent moveEvent = MoveEvent.read(params.moveEvent)
		MoveBundle moveBundle = params.moveBundle ? MoveBundle.get(params.moveBundle) : null

		String sortIndex = params.sidx ?: 'comment'
		String sortOrder  = params.sord ?: 'asc'
		int maxRows = params.int('rows')
		int currentPage = params.int('page', 1)

		def assetCommentsQuery = new StringBuilder('''\
			select ac.asset_comment_id as id, ac.date_created as createdAt, display_option as displayOption, comment,
			       CONCAT_WS(' ', p1.first_name, p1.last_name) as createdBy, ac.comment_type as commentType,
			       CONCAT_WS(' ', p2.first_name, p2.last_name) as resolvedBy, ac.date_resolved as resolvedAt,
			       resolution, ae.asset_entity_id as assetEntity
			from asset_comment ac
			     left join asset_entity ae on (ae.asset_entity_id = ac.asset_entity_id)
			     left join move_bundle mb on (mb.move_bundle_id = ae.move_bundle_id)
			     left join project p on (p.project_id = ae.project_id) left join person p1 on (p1.person_id = ac.created_by)
			     left join person p2 on (p2.person_id = ac.resolved_by)
			where ac.comment_type = 'issue'
			  and ''')

		def moveEventNewsQuery = new StringBuilder('''\
			select mn.move_event_news_id as id, mn.date_created as createdAt, 'U' as displayOption,
			       CONCAT_WS(' ', p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ', p2.first_name, p2.last_name) as resolvedBy,
			       'news' as commentType, message as comment, resolution, date_archived as resolvedAt, null as assetEntity
			from move_event_news mn
			     left join move_event me on (me.move_event_id = mn.move_event_id)
			     left join project p on (p.project_id = me.project_id)
			     left join person p1 on (p1.person_id = mn.created_by)
			     left join person p2 on (p2.person_id = mn.archived_by)
			where ''')

		if (moveBundle) {
			assetCommentsQuery.append(" mb.move_bundle_id = $moveBundle.id  ")
		} else {
			assetCommentsQuery.append(" mb.move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = ${moveEvent?.id})")
		}

		if (moveEvent) {
			moveEventNewsQuery.append(" mn.move_event_id = ${moveEvent.id}  and p.project_id = $project.id ")
		} else {
			moveEventNewsQuery.append(" p.project_id = $project.id ")
		}
		if (viewFilter == "active") {
			assetCommentsQuery.append(" and ac.date_resolved is null ")
			moveEventNewsQuery.append(" and mn.is_archived = 0 ")
		} else if (viewFilter == "archived") {
			assetCommentsQuery.append(" and ac.date_resolved is not null ")
			moveEventNewsQuery.append(" and mn.is_archived = 1 ")
		}
		if (params.comment) {
			assetCommentsQuery.append("and ac.comment like '%$params.comment%'")
			moveEventNewsQuery.append("and mn.message like '%$params.comment%'")
		}
		if (params.createdAt) {
			assetCommentsQuery.append("and ac.date_created like '%$params.createdAt%'")
			moveEventNewsQuery.append("and mn.date_created like '%$params.createdAt%'")
		}
		if (params.resolvedAt) {
			assetCommentsQuery.append("and ac.date_resolved like '%$params.resolvedAt%'")
			moveEventNewsQuery.append("and mn.date_archived like '%$params.resolvedAt%'")
		}
		if (params.createdBy) {
			assetCommentsQuery.append("and p1.first_name like '%$params.createdBy%'")
			moveEventNewsQuery.append("and p1.first_name like '%$params.createdBy%'")
		}
		if (params.resolvedBy) {
			assetCommentsQuery.append("and p2.first_name like '%$params.resolvedBy%'")
			moveEventNewsQuery.append("and p2.first_name like '%$params.resolvedBy%'")
		}
		if (params.commentType) {
			assetCommentsQuery.append("and ac.comment_type like '%$params.commentType%'")
			moveEventNewsQuery.append("and news like '%$params.commentType%'")
		}
		if (params.resolution) {
			assetCommentsQuery.append("and ac.resolution like '%$params.resolution%'")
			moveEventNewsQuery.append("and mn.resolution like '%$params.resolution%'")
		}

		assetCommentsQuery.append(" and ac.comment_type = 'news' ")

		def queryForCommentsList = new StringBuilder()
		queryForCommentsList << assetCommentsQuery << " union all " << moveEventNewsQuery
		queryForCommentsList << 'order by ' << sortIndex << ' ' << sortOrder
		def totalComments = jdbcTemplate.queryForList(queryForCommentsList.toString())

		int totalRows = totalComments.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)

		def results = totalComments.collect {
			[cell: [TimeUtil.formatDate(it.createdAt),
			        it.createdBy, it.commentType, it.comment, it.resolution,
			        TimeUtil.formatDate(it.resolvedAt),
			        it.resolvedBy],
			 id: it.id]
		}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}

	@HasPermission(Permission.NewsView)
	def getEventNewsList() {

		Project project = securityService.userCurrentProject
		String viewFilter = params.viewFilter
		MoveEvent moveEvent = MoveEvent.read(params.moveEvent)
		def bundleId = params.moveBundle
		MoveBundle moveBundle = bundleId ? MoveBundle.get(bundleId) : null

		def assetCommentsQuery = new StringBuilder("""select ac.asset_comment_id as id, ac.date_created as createdAt, display_option as displayOption,
									CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy,
									ac.comment_type as commentType, comment , resolution, date_resolved as resolvedAt, ae.asset_entity_id as assetEntity
									from asset_comment ac
									left join asset_entity ae on (ae.asset_entity_id = ac.asset_entity_id)
									left join move_bundle mb on (mb.move_bundle_id = ae.move_bundle_id)
									left join project p on (p.project_id = ae.project_id) left join person p1 on (p1.person_id = ac.created_by)
									left join person p2 on (p2.person_id = ac.resolved_by) where ac.comment_type = 'issue' and """)

		def moveEventNewsQuery = new StringBuilder("""select mn.move_event_news_id as id, mn.date_created as createdAt, 'U' as displayOption,
											CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy,
											'news' as commentType, message as comment ,	resolution, date_archived as resolvedAt, null as assetEntity
											from move_event_news mn
											left join move_event me on (me.move_event_id = mn.move_event_id)
											left join project p on (p.project_id = me.project_id) left join person p1 on (p1.person_id = mn.created_by)
											left join person p2 on (p2.person_id = mn.archived_by) where """)


		if (moveBundle) {
			assetCommentsQuery.append(" mb.move_bundle_id = $moveBundle.id  ")
		} else {
			assetCommentsQuery.append(" mb.move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = ${moveEvent?.id})")
		}

		if (moveEvent) {
			moveEventNewsQuery.append(" mn.move_event_id = ${moveEvent.id}  and p.project_id = $project.id ")
		} else {
			moveEventNewsQuery.append(" p.project_id = $project.id ")
		}

		if (viewFilter == "active") {
			assetCommentsQuery.append(" and ac.date_resolved is null ")
			moveEventNewsQuery.append(" and mn.is_archived = 0 ")
		}
		else if (viewFilter == "archived") {
			assetCommentsQuery.append(" and ac.date_resolved is not null ")
			moveEventNewsQuery.append(" and mn.is_archived = 1 ")
		}

		assetCommentsQuery.append(" and ac.comment_type = 'news' ")

		def queryForCommentsList = new StringBuilder(assetCommentsQuery.toString() +" union all "+ moveEventNewsQuery)

		def result = jdbcTemplate.queryForList(queryForCommentsList.toString()).collect {[
			createdAt: it.createdAt,
			createdBy: it.createdBy,
			commentType: it.commentType,
			comment: it.comment,
			resolution: it.resolution,
			resolvedAt: it.resolvedAt,
			resolvedBy: it.resolvedBy,
			newsId: it.id
		]}
		render result as JSON
	}

	/**
	 * @return assetComment / moveEventNews object based on comment Type as JSON object
	 */
	@HasPermission(Permission.NewsView)
	def retrieveCommetOrNewsData() {
		def personResolvedObj
		def personCreateObj
		def dtCreated
		def dtResolved
		def assetName
		def commentType = params.commentType
		Object commentObject
		if (commentType == 'issue' || commentType == 'I') {
			commentObject = AssetComment.get(params.id)
			if (commentObject?.resolvedBy) {
				personResolvedObj = commentObject.resolvedBy?.toString()
				dtResolved = TimeUtil.formatDateTime(commentObject.dateResolved, TimeUtil.FORMAT_DATE_TIME_9)
			}
			assetName = commentObject.assetEntity.assetName
		} else {
			commentObject = MoveEventNews.get(params.id)
			if (commentObject?.archivedBy) {
				personResolvedObj = commentObject.archivedBy?.toString()
				dtResolved = TimeUtil.formatDateTime(commentObject.dateArchived, TimeUtil.FORMAT_DATE_TIME_9)
			}
		}

		if (commentObject?.createdBy) {
			personCreateObj = commentObject.createdBy?.toString()
			dtCreated = TimeUtil.formatDateTime(commentObject.dateCreated, TimeUtil.FORMAT_DATE_TIME_9)
		}

		List commentList = [
			[
				commentObject: commentObject,
				personCreateObj: personCreateObj,
				personResolvedObj: personResolvedObj,
				dtCreated: dtCreated ?: '',
				dtResolved: dtResolved ?: '',
				assetName: assetName
			]
		]
		render commentList as JSON
	}

	/**
	 * Saves the data and redirects to action newsEditorList
	 */
	@HasPermission(Permission.NewsEdit)
	def updateNewsOrComment() {
		String commentType = params.commentType
		if (commentType == "issue") {
			AssetComment assetComment = AssetComment.get(params.id)
			if (params.isResolved == '1' && !assetComment.isResolved()) {
				assetComment.resolvedBy = securityService.loadCurrentPerson()
				assetComment.dateResolved = new Date()
			}
			else if (!(params.isResolved == '1' && assetComment.isResolved())) {
				assetComment.resolvedBy = null
				assetComment.dateResolved = null
			}

			assetComment.properties = params
			assetComment.save(flush:true)
		}
		else if (commentType == "news") {
			MoveEventNews moveEventNews = MoveEventNews.get(params.id)
			if (params.isResolved == '1' && moveEventNews.isArchived == 0) {
				moveEventNews.isArchived = 1
				moveEventNews.archivedBy = securityService.loadCurrentPerson()
				moveEventNews.dateArchived = new Date()
			}
			else if (!(params.isResolved == '1' && moveEventNews.isArchived == 1)) {
				moveEventNews.isArchived = 0
				moveEventNews.archivedBy = null
				moveEventNews.dateArchived = null
			}
			moveEventNews.message = params.comment
			moveEventNews.resolution = params.resolution
			moveEventNews.save(flush:true)
		}

		redirect(action: "newsEditorList",
			      params: [moveBundle: params.moveBundle, viewFilter: params.viewFilter])
	}

	/**
	 * Used to create new MoveEventNews records that returns AJax Response for error or redirects if successful to newsEditorList
	 * @param mode - indicates the mode to respond to the request, if 'ajax' then it uses the ServiceResponse format otherwise does the redirect
	 * @param moveEvent.id
	 * @param message
	 * @param resolution
	 * @param isArchived
	 * @param resolution
	 */
	@HasPermission(Permission.NewsCreate)
	def saveNews() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			flash.message = null
			return
		}

		String error
		MoveEvent me

		// Check to make sure that the moveEvent id exists and is associated to the project
		Long meId = params.long('moveEvent.id')
		if (meId == null || meId < 1) {
			error = 'Invalid move event id specified'
		}
		else {
			// Check that the move event exists and is assoicated to the user's project
			me = MoveEvent.get(meId)
			if (!me) {
				error = 'Move event was not found'
			}
			else if (me.project.id != project.id) {
				securityService.reportViolation("Accessing move event ($meId) not associated with project ($project.id)")
				error = 'Invalid move event id specified'
			}
		}

		if (error) {
			renderErrorJson(error)
			return
		}

		// Create the new news domain
		def men = new MoveEventNews(moveEvent: me, createdBy: securityService.loadCurrentPerson())

		saveUpdateNewsHandler(project, men, params, this)
	}

	/**
	 * Used to update an exiting MoveEventNews record that returns AJax Response
	 * @param message
	 * @param resolution
	 * @param isArchived
	 * @param resolution
	 */
	@HasPermission(Permission.NewsEdit)
	def updateNews() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			flash.message = null
			return
		}

		String error
		MoveEventNews men

		// Check to make sure that the MoveEventNews id exists and is associated to the project
		Long id = NumberUtil.toLong(params['id'])
		if (id == null || id < 1) {
			error = 'Invalid news id specified'
		} else {
			// Check that the move event news id exists and is assoicated to the user's project
			men = MoveEventNews.get(id)
			if (!id) {
				error = 'News id was not found'
			} else {
				if (men.moveEvent.project.id != project.id) {
					securityService.reportViolation("Accessing MoveEventNews ($id) not associated with project ($project.id)")
					error = 'Invalid news id specified'
				}
			}
		}
		if (error) {
			renderErrorJson(error)
			return
		}

		saveUpdateNewsHandler(project, men, params, this)
	}

	/**
	 * Used by the saveNews and updateNews controller methods to perform the actual update of the new or existing MoveEventNews domain record
	 */
	private void saveUpdateNewsHandler(Project project, MoveEventNews men, params, controller) {

		men.message = params.message
		men.resolution = params.resolution

		if (params.isArchived == '1') {
			men.isArchived = 1
			men.archivedBy = securityService.loadCurrentPerson()
			men.dateArchived = TimeUtil.nowGMT()
		} else {
			men.isArchived = 0
		}

		if (!men.validate() || !men.save(flush:true)) {
			error = "Error saving news : ${GormUtil.allErrorsString(men)}"
			log.info "saveNews() user:$securityService.currentUsername, project:$project.id - $error"
			renderErrorJson(error)
			return
		}

		String mode = params.mode ?: 'redirect'

		if (mode == 'ajax') {
			Map menModel = [id:men.id, message:men.message, resolution:men.resolution,
			                isArchived:men.isArchived, moveEventId: men.moveEvent.id]
			renderSuccessJson(moveEventNews: menModel)
		} else {
			controller.redirect(
				action: "newsEditorList",
				params: [ moveBundle: params.moveBundle, viewFilter:params.viewFilter, moveEvent:params.moveEvent.id]
			)
		}
	}

	/**
	 * Used to update an exiting MoveEventNews record that returns AJax Response
	 * @param id
	 */
	@HasPermission(Permission.NewsDelete)
	def deleteNews() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			flash.message = null
			return
		}

		String error
		MoveEventNews men

		// Check to make sure that the MoveEventNews id exists and is associated to the project
		Long id = NumberUtil.toLong(params['id'])
		if (id == null || id < 1) {
			error = 'Invalid news id specified'
		} else {
			// Check that the move event news id exists and is assoicated to the user's project
			men = MoveEventNews.get(id)
			if (!id) {
				error = 'News id was not found'
			} else {
				if (men.moveEvent.project.id != project.id) {
					securityService.reportViolation("Accessing MoveEventNews ($id) not associated with project ($project.id)")
					error = 'Invalid news id specified'
				}
			}
		}

		if (!error) {
			try {
				men.delete(flush:true)
			} catch (e) {
				error = "Delete failed :  ${GormUtil.allErrorsString(men)}"
			}
		}

		if (error) {
			renderErrorJson(error)
			return
		}

		renderSuccessJson()
	}
}
