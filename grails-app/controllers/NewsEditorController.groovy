import com.tds.asset.AssetComment
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import net.transitionmanager.command.newseditor.SaveNewsCommand
import net.transitionmanager.command.newseditor.UpdateNewsCommand
import groovy.transform.CompileStatic
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventNews
import net.transitionmanager.domain.Project
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.NewsEditorService
import net.transitionmanager.service.UserPreferenceService

import grails.plugin.springsecurity.annotation.Secured
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import java.sql.ResultSet
import java.sql.SQLException

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class NewsEditorController implements ControllerMethods, PaginationMethods {

	NewsEditorService     newsEditorService
	ControllerService     controllerService
	JdbcTemplate          jdbcTemplate
	ControllerService controllerService
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
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
	def getEventNewsList() {

		Project project = getProjectForWs()
		String viewFilter = params.viewFilter
		MoveEvent moveEvent = fetchDomain(MoveEvent, [id: params.moveEvent])
		def bundleId = params.moveBundle
		MoveBundle moveBundle = bundleId ? fetchDomain(MoveBundle, [id: bundleId]) : null

		StringBuilder assetCommentsQuery = new StringBuilder("""select ac.asset_comment_id as id, ac.date_created as createdAt, display_option as displayOption,
									CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy,
									ac.comment_type as commentType, comment , resolution, date_resolved as resolvedAt, ae.asset_entity_id as assetEntity
									from asset_comment ac
									left join asset_entity ae on (ae.asset_entity_id = ac.asset_entity_id)
									left join move_bundle mb on (mb.move_bundle_id = ae.move_bundle_id)
									left join project p on (p.project_id = ae.project_id) left join person p1 on (p1.person_id = ac.created_by)
									left join person p2 on (p2.person_id = ac.resolved_by) where ac.comment_type = 'issue' and """)

		StringBuilder moveEventNewsQuery = new StringBuilder("""select mn.move_event_news_id as id, mn.date_created as createdAt, 'U' as displayOption,
											CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy,
											'news' as commentType, message as comment ,	resolution, date_archived as resolvedAt, null as assetEntity
											from move_event_news mn
											left join move_event me on (me.move_event_id = mn.move_event_id)
											left join project p on (p.project_id = me.project_id) left join person p1 on (p1.person_id = mn.created_by)
											left join person p2 on (p2.person_id = mn.archived_by) where """)

		Map queryParams = [:]

		if (moveBundle) {
			assetCommentsQuery.append(" mb.move_bundle_id = :acqMoveBundleId ")
			queryParams.acqMoveBundleId = moveBundle.id
		} else if (moveEvent) {
			assetCommentsQuery.append(" mb.move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = :acqMoveEventId)")
			queryParams.acqMoveEventId = moveEvent.id
		}

		if (moveEvent) {
			moveEventNewsQuery.append(" mn.move_event_id = :menqMoveEventId  and p.project_id = :menqProjectId ")
			queryParams.menqMoveEventId = moveEvent.id
		} else {
			moveEventNewsQuery.append(" p.project_id = :menqProjectId")
		}

		queryParams.menqProjectId = project.id

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

		List<Map> result = namedParameterJdbcTemplate.query(queryForCommentsList.toString(), queryParams, new MoveEventNewsMapper()).collect {[
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
		SaveNewsCommand news = populateCommandObject(SaveNewsCommand)
		validateCommandObject(news)

		if (!project) {
			flash.message = null
			return
		}

		MoveEventNews men = newsEditorService.save(project, news.moveEventId, news.message, news.resolution, news.isArchived)
		renderHandler(men, news.mode, news.moveBundle, news.viewFilter, news.moveEventId)
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
		UpdateNewsCommand news = populateCommandObject(UpdateNewsCommand)
		validateCommandObject(news)
		Project project = controllerService.getProjectForPage(this)

		if (!project) {
			flash.message = null
			return
		}

		MoveEventNews men = newsEditorService.update(project, news.id, news.message, news.resolution, news.isArchived)
		renderHandler(men, news.mode, news.moveBundle, news.viewFilter, null)
	}

	/**
	 * Used by the saveNews and updateNews controller methods to perform the actual update of the new or existing MoveEventNews domain record
	 */
	private void renderHandler(MoveEventNews moveEventNews, String mode, String moveBundle, String viewFilter, Long moveEventId) {
		if (mode == 'ajax') {
			Map menModel = [
				id         : moveEventNews.id,
				message    : moveEventNews.message,
				resolution : moveEventNews.resolution,
				isArchived : moveEventNews.isArchived,
				moveEventId: moveEventNews.moveEvent.id
			]

			renderSuccessJson(moveEventNews: menModel)
		} else {
			redirect(
				action: "newsEditorList",
				params: [ moveBundle: moveBundle, viewFilter: viewFilter, moveEvent: moveEventId ?: null]
			)
		}
	}

	/**
	 * Used to update an exiting MoveEventNews record that returns AJax Response
	 * @param id
	 */
	@HasPermission(Permission.NewsDelete)
	def deleteNews(Long id) {
		Project project = controllerService.getProjectForPage(this)

		if (!project) {
			flash.message = null
			return
		}

		newsEditorService.delete(id, project)

		renderSuccessJson()
	}

	/**
	 * RowMapper that maps each result from the query for the Event News list
	 * into a map column -> value that can be sent back to the UI.
	 */
	@CompileStatic
	private class MoveEventNewsMapper implements RowMapper {
		def mapRow(ResultSet rs, int rowNum) throws SQLException {[
			id:      rs.getInt('id'),
			createdAt: rs.getDate('createdAt'),
			createdBy: rs.getString('createdBy'),
			comment: rs.getString('comment'),
			commentType: rs.getString('commentType'),
			resolution: rs.getString('resolution'),
			resolvedAt: rs.getDate('resolvedAt'),
			resolvedBy: rs.getString('resolvedBy')
		]}
	}
}
