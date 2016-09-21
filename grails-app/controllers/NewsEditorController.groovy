
import com.tds.asset.AssetComment
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import org.apache.shiro.SecurityUtils
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF

class NewsEditorController {

	def jdbcTemplate

	def controllerService
	def securityService
	def userPreferenceService

    def index() {
    	redirect( action:"newsEditorList", params:params )
	}

    /*---------------------------------------------------------
     * @author : Lokanada Reddy
     * @param  : project, bundle, and filters
     * @return : Union of assets issues and move event news
     *--------------------------------------------------------*/
	def newsEditorList() {

		def projectId =  session.getAttribute('CURR_PROJ').CURR_PROJ
		def projectInstance = securityService.getUserCurrentProject()
		if (!projectInstance) {
			flash.message = "Please select project to view News"
			redirect(controller:'project',action:'list')
			return
		}
		def moveEventsList = MoveEvent.findAllByProject(projectInstance)
		def moveBundlesList
		def moveEventId = params.moveEvent
		def moveEvent
		if(moveEventId){
			userPreferenceService.setPreference(PREF.MOVE_EVENT, "${moveEventId}" )
			moveEvent = MoveEvent.get(moveEventId)
		} else {
			userPreferenceService.loadPreferences(PREF.MOVE_EVENT)
			def defaultEvent = session.getAttribute("MOVE_EVENT")
			if(defaultEvent?.MOVE_EVENT){
				moveEvent = MoveEvent.get(defaultEvent.MOVE_EVENT)
				if( moveEvent?.project?.id != Integer.parseInt(projectId) ){
					moveEvent = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[projectInstance])
				}
			} else {
				moveEvent = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[projectInstance])
			}
		}

		if(moveEvent){
			moveBundlesList = MoveBundle.findAll("from MoveBundle mb where mb.moveEvent = ${moveEvent?.id} order by mb.name asc")
		} else {
			moveBundlesList = MoveBundle.findAll("from MoveBundle mb where mb.project = ${projectId} order by mb.name asc")
		}
		return [moveEventId : moveEvent.id, viewFilter : params.viewFilter,  bundleId :  params.moveBundle, moveBundlesList:moveBundlesList,
			moveEventsList:moveEventsList]
    }

	/**
	 *
	 *
	 */
	def listEventNewsJson() {

		def projectId =  session.getAttribute('CURR_PROJ').CURR_PROJ
		def projectInstance = Project.get( projectId )
		def bundleId = params.moveBundle
		def viewFilter = params.viewFilter
		def moveBundleInstance = null
		def assetCommentsList
		def moveEventNewsList
		def offset = params.offset
		userPreferenceService.loadPreferences(PREF.CURR_BUNDLE)
		def defaultBundle = session.getAttribute("CURR_BUNDLE")
		def moveEventsList = MoveEvent.findAllByProject(projectInstance)
		def moveEvent = MoveEvent.read(params.moveEvent)
		def moveBundlesList
		if(bundleId){
			moveBundleInstance = MoveBundle.get(bundleId)
		}
		def sortIndex = params.sidx ?: 'comment'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def assetCommentsQuery = new StringBuffer( """select ac.asset_comment_id as id, date_created as createdAt, display_option as displayOption,
									CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy,
									ac.comment_type as commentType, comment , resolution, date_resolved as resolvedAt, ae.asset_entity_id as assetEntity
									from asset_comment ac
									left join asset_entity ae on (ae.asset_entity_id = ac.asset_entity_id)
									left join move_bundle mb on (mb.move_bundle_id = ae.move_bundle_id)
									left join project p on (p.project_id = ae.project_id) left join person p1 on (p1.person_id = ac.created_by)
									left join person p2 on (p2.person_id = ac.resolved_by) where ac.comment_type = 'issue' and """ )

		def moveEventNewsQuery = new StringBuffer( """select mn.move_event_news_id as id, date_created as createdAt, 'U' as displayOption,
											CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy,
											'news' as commentType, message as comment ,	resolution, date_archived as resolvedAt, null as assetEntity
											from move_event_news mn
											left join move_event me on ( me.move_event_id = mn.move_event_id )
											left join project p on (p.project_id = me.project_id) left join person p1 on (p1.person_id = mn.created_by)
											left join person p2 on (p2.person_id = mn.archived_by) where """ )


		if(moveBundleInstance != null){
			assetCommentsQuery.append(" mb.move_bundle_id = ${moveBundleInstance.id}  ")
		} else {
			assetCommentsQuery.append(" mb.move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = ${moveEvent?.id} )")
		}

		if(moveEvent){
			moveEventNewsQuery.append(" mn.move_event_id = ${moveEvent?.id}  and p.project_id = ${projectInstance.id} ")
		} else {
			moveEventNewsQuery.append(" p.project_id = ${projectInstance.id} ")
		}
		if(viewFilter == "active"){
			assetCommentsQuery.append(" and ac.is_resolved = 0 ")
			moveEventNewsQuery.append(" and mn.is_archived = 0 ")
		} else if(viewFilter == "archived"){
			assetCommentsQuery.append(" and ac.is_resolved = 1 ")
			moveEventNewsQuery.append(" and mn.is_archived = 1 ")
		}
		if(params.comment){
			assetCommentsQuery.append("and ac.comment like '%${params.comment}%'")
			moveEventNewsQuery.append("and mn.message like '%${params.comment}%'")
		}
		if(params.createdAt){
			assetCommentsQuery.append("and ac.date_created like '%${params.createdAt}%'")
			moveEventNewsQuery.append("and mn.date_created like '%${params.createdAt}%'")
		}
		if(params.resolvedAt){
			assetCommentsQuery.append("and ac.date_resolved like '%${params.resolvedAt}%'")
			moveEventNewsQuery.append("and mn.date_archived like '%${params.resolvedAt}%'")
		}
		if(params.createdBy ){
			assetCommentsQuery.append("and p1.first_name like '%${params.createdBy}%'")
			moveEventNewsQuery.append("and p1.first_name like '%${params.createdBy}%'")
		}
		if(params.resolvedBy ){
			assetCommentsQuery.append("and p2.first_name like '%${params.resolvedBy}%'")
			moveEventNewsQuery.append("and p2.first_name like '%${params.resolvedBy}%'")
		}
		if(params.commentType){
			assetCommentsQuery.append("and ac.comment_type like '%${params.commentType}%'")
			moveEventNewsQuery.append("and news like '%${params.commentType}%'")
		}
		if(params.resolution){
			assetCommentsQuery.append("and ac.resolution like '%${params.resolution}%'")
			moveEventNewsQuery.append("and mn.resolution like '%${params.resolution}%'")
		}

		assetCommentsQuery.append(" and ac.comment_type = 'news' ")

		def queryForCommentsList = new StringBuffer(assetCommentsQuery.toString() +" union all "+ moveEventNewsQuery.toString())

		queryForCommentsList.append("order by $sortIndex $sortOrder ")
		def totalComments = jdbcTemplate.queryForList( queryForCommentsList.toString() )

		def totalRows = totalComments.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)

		def results = totalComments?.collect {
			[ cell: [ it.createdAt ? TimeUtil.formatDate(session, it.createdAt):'',
					it.createdBy, it.commentType, it.comment, it.resolution,
					it.resolvedAt ? TimeUtil.formatDate(session, it.resolvedAt):'',
					it.resolvedBy], id: it.id]
			}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}

	/**
	 *
	 *
	 */
	def getEventNewsList() {

		def projectId =  session.getAttribute('CURR_PROJ').CURR_PROJ
		def projectInstance = Project.get( projectId )
		def bundleId = params.moveBundle
		def viewFilter = params.viewFilter
		def moveBundleInstance = null
		userPreferenceService.loadPreferences(PREF.CURR_BUNDLE)
		def moveEvent = MoveEvent.read(params.moveEvent)
		if(bundleId){
			moveBundleInstance = MoveBundle.get(bundleId)
		}

		def assetCommentsQuery = new StringBuffer( """select ac.asset_comment_id as id, date_created as createdAt, display_option as displayOption,
									CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy,
									ac.comment_type as commentType, comment , resolution, date_resolved as resolvedAt, ae.asset_entity_id as assetEntity
									from asset_comment ac
									left join asset_entity ae on (ae.asset_entity_id = ac.asset_entity_id)
									left join move_bundle mb on (mb.move_bundle_id = ae.move_bundle_id)
									left join project p on (p.project_id = ae.project_id) left join person p1 on (p1.person_id = ac.created_by)
									left join person p2 on (p2.person_id = ac.resolved_by) where ac.comment_type = 'issue' and """ )

		def moveEventNewsQuery = new StringBuffer( """select mn.move_event_news_id as id, date_created as createdAt, 'U' as displayOption,
											CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy,
											'news' as commentType, message as comment ,	resolution, date_archived as resolvedAt, null as assetEntity
											from move_event_news mn
											left join move_event me on ( me.move_event_id = mn.move_event_id )
											left join project p on (p.project_id = me.project_id) left join person p1 on (p1.person_id = mn.created_by)
											left join person p2 on (p2.person_id = mn.archived_by) where """ )


		if(moveBundleInstance != null){
			assetCommentsQuery.append(" mb.move_bundle_id = ${moveBundleInstance.id}  ")
		} else {
			assetCommentsQuery.append(" mb.move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = ${moveEvent?.id} )")
		}

		if(moveEvent){
			moveEventNewsQuery.append(" mn.move_event_id = ${moveEvent?.id}  and p.project_id = ${projectInstance.id} ")
		} else {
			moveEventNewsQuery.append(" p.project_id = ${projectInstance.id} ")
		}

		if(viewFilter == "active"){
			assetCommentsQuery.append(" and ac.is_resolved = 0 ")
			moveEventNewsQuery.append(" and mn.is_archived = 0 ")
		} else if(viewFilter == "archived"){
			assetCommentsQuery.append(" and ac.is_resolved = 1 ")
			moveEventNewsQuery.append(" and mn.is_archived = 1 ")
		}

		assetCommentsQuery.append(" and ac.comment_type = 'news' ")

		def queryForCommentsList = new StringBuffer(assetCommentsQuery.toString() +" union all "+ moveEventNewsQuery.toString())

		def totalComments = jdbcTemplate.queryForList( queryForCommentsList.toString() )

		def result = totalComments?.collect {
			[
			  createdAt: it.createdAt ? TimeUtil.formatDate(session, it.createdAt):'',
			  createdBy: it.createdBy,
			  commentType: it.commentType,
			  comment: it.comment,
			  resolution: it.resolution,
			  resolvedAt: it.resolvedAt ? TimeUtil.formatDate(session, it.resolvedAt):'',
			  resolvedBy: it.resolvedBy,
			  newsId: it.id
			]
		}

		render result as JSON
	}
	/*-------------------------------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : id and comment type
	 * @return : assetComment / moveEventNews object based on comment Type as JSON object
	 *-------------------------------------------------------------------*/
	def retrieveCommetOrNewsData() {
		def commentList = []
		def personResolvedObj
		def personCreateObj
		def dtCreated
		def dtResolved
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetName
		def commentType = params.commentType
		def commentObject
		if( commentType == 'issue' || commentType == 'I'){
			commentObject = AssetComment.get( params.id )
			if(commentObject?.resolvedBy){
				personResolvedObj = Person.find("from Person p where p.id = $commentObject.resolvedBy.id")?.toString()
				dtResolved = TimeUtil.formatDateTime(session, commentObject.dateResolved, TimeUtil.FORMAT_DATE_TIME_9)
			}
			assetName = commentObject.assetEntity.assetName
		} else {
			commentObject = MoveEventNews.get( params.id )
			if(commentObject?.archivedBy){
				personResolvedObj = Person.find("from Person p where p.id = $commentObject.archivedBy.id")?.toString()
				dtResolved = TimeUtil.formatDateTime(session, commentObject.dateArchived, TimeUtil.FORMAT_DATE_TIME_9)
			}
		}
		if(commentObject?.createdBy){
			personCreateObj = Person.find("from Person p where p.id = $commentObject.createdBy.id")?.toString()
			dtCreated = TimeUtil.formatDateTime(session, commentObject.dateCreated, TimeUtil.FORMAT_DATE_TIME_9)
		}
		commentList<<[ commentObject:commentObject,personCreateObj:personCreateObj,
					   personResolvedObj:personResolvedObj,dtCreated:dtCreated?dtCreated:"",
					   dtResolved:dtResolved?dtResolved:"",assetName : assetName]
		render commentList as JSON
	}

	/*---------------------------------------------------------
     * @author : Lokanada Reddy
     * @param  : project, bundle, and filters, assetComment / moveEventNews updated data
     * @return : will save the data and redirect to action : newsEditorList
     *--------------------------------------------------------*/
	def updateNewsOrComment() {
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		def commentType = params.commentType
		if(commentType == "issue"){
			def assetCommentInstance = AssetComment.get(params.id)
			if(params.isResolved == '1' && assetCommentInstance.isResolved == 0 ){
				assetCommentInstance.resolvedBy = loginUser.person
				assetCommentInstance.dateResolved = new Date()
			}else if(params.isResolved == '1' && assetCommentInstance.isResolved == 1){
			}else{
				assetCommentInstance.resolvedBy = null
				assetCommentInstance.dateResolved = null
			}
			assetCommentInstance.properties = params
			assetCommentInstance.save(flush:true)
		} else if(commentType == "news"){

			def moveEventNewsInstance = MoveEventNews.get(params.id)
			if(params.isResolved == '1' && moveEventNewsInstance.isArchived == 0 ){
				moveEventNewsInstance.isArchived = 1
				moveEventNewsInstance.archivedBy = loginUser.person
				moveEventNewsInstance.dateArchived = new Date()
			}else if(params.isResolved == '1' && moveEventNewsInstance.isArchived == 1){
			}else{
				moveEventNewsInstance.isArchived = 0
				moveEventNewsInstance.archivedBy = null
				moveEventNewsInstance.dateArchived = null
			}
			moveEventNewsInstance.message = params.comment
			moveEventNewsInstance.resolution = params.resolution
			moveEventNewsInstance.save(flush:true)

		}

		redirect(action:"newsEditorList", params:[moveBundle : params.moveBundle, viewFilter:params.viewFilter])
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
	def saveNews() {

		def (project, userLogin) = controllerService.getProjectAndUserForPage(this)
		if (!project) {
			flash.message = null
			return
		}

		// Check for permission
		if (! securityService.hasPermission(userLogin, 'CreateNews') ) {
			ServiceResults.unauthorized(response)
			return
		}

		String error
		MoveEvent me

		// Check to make sure that the moveEvent id exists and is associated to the project
		Long meId = NumberUtil.toLong(params['moveEvent.id'])
		if (meId == null || meId < 1) {
			error = 'Invalid move event id specified'
		} else {
			// Check that the move event exists and is assoicated to the user's project
			me = MoveEvent.get(meId)
			if (!me) {
				error = 'Move event was not found'
			} else {
				if (me.project.id != project.id) {
					securityService.reportViolation("Accessing move event ($meId) not associated with project (${project.id})", userLogin)
					error = 'Invalid move event id specified'
				}
			}
		}
		if (error) {
			render ServiceResults.errors(error) as JSON
			return
		}

		// Create the new news domain
		def men = new MoveEventNews()
		men.moveEvent = me
		men.createdBy = userLogin.person

		saveUpdateNewsHandler(project, userLogin, men, params, this)
	}


	/**
	 * Used to update an exiting MoveEventNews record that returns AJax Response
	 * @param message
	 * @param resolution
	 * @param isArchived
	 * @param resolution
	 */
	def updateNews() {

		def (project, userLogin) = controllerService.getProjectAndUserForPage(this)
		if (!project) {
			flash.message = null
			return
		}

		// Check for permission
		if (! securityService.hasPermission(userLogin, 'CreateNews') ) {
			ServiceResults.unauthorized(response)
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
					securityService.reportViolation("Accessing MoveEventNews ($id) not associated with project (${project.id})", userLogin)
					error = 'Invalid news id specified'
				}
			}
		}
		if (error) {
			render ServiceResults.errors(error) as JSON
			return
		}

		saveUpdateNewsHandler(project, userLogin, men, params, this)
	}

	/**
	 * Used by the saveNews and updateNews controller methods to perform the actual update of the new or existing MoveEventNews domain record
	 */
	private void saveUpdateNewsHandler(Project project, UserLogin userLogin, MoveEventNews men, params, controller) {

		men.message = params.message
		men.resolution = params.resolution

		if (params.isArchived == '1') {
			men.isArchived = 1
			men.archivedBy = userLogin.person
			men.dateArchived = TimeUtil.nowGMT()
		} else {
			men.isArchived = 0
		}

		if (! men.validate() || !men.save(flush:true) ) {
			error = "Error saving news : ${GormUtil.allErrorsString(men)}"
			log.info "saveNews() user:$userLogin, project:${project.id} - $error"
			render ServiceResults.errors(error) as JSON
			return
		}

		String mode = params.mode ?: 'redirect'

		if (mode == 'ajax') {
			Map menModel = [id:men.id, message:men.message, resolution:men.resolution, isArchived:men.isArchived, moveEventId: men.moveEvent.id]
			render ServiceResults.success([moveEventNews: menModel]) as JSON
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
	def deleteNews() {

		def (project, userLogin) = controllerService.getProjectAndUserForPage(this)
		if (!project) {
			flash.message = null
			return
		}

		// Check for permission
		if (! securityService.hasPermission(userLogin, 'CreateNews') ) {
			ServiceResults.unauthorized(response)
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
					securityService.reportViolation("Accessing MoveEventNews ($id) not associated with project (${project.id})", userLogin)
					error = 'Invalid news id specified'
				}
			}
		}

		if (! error) {
			try {
				men.delete(flush:true)
			} catch (e) {
				error = "Delete failed :  ${GormUtil.allErrorsString(men)}"
			}
		}

		if (error) {
			render ServiceResults.errors(error) as JSON
			return
		}

		render ServiceResults.success() as JSON
	}
}
