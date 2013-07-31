
import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
class NewsEditorController {
	
	def userPreferenceService
	def jdbcTemplate
	def securityService
	
    def index = {
    	redirect( action:newsEditorList, params:params )
	}
    /*---------------------------------------------------------
     * @author : Lokanada Reddy
     * @param  : project, bundle, and filters
     * @return : Union of assets issues and move event news
     *--------------------------------------------------------*/
	def newsEditorList = {
		
		def projectId =  getSession().getAttribute('CURR_PROJ').CURR_PROJ
		def projectInstance = Project.findById( projectId )
		def moveEventsList = MoveEvent.findAllByProject(projectInstance)
		def moveBundlesList
		def moveEventId = params.moveEvent
		def moveEvent
		if(moveEventId){
			userPreferenceService.setPreference( "MOVE_EVENT", "${moveEventId}" )
			moveEvent = MoveEvent.findById(moveEventId)
		} else {
			userPreferenceService.loadPreferences("MOVE_EVENT")
			def defaultEvent = getSession().getAttribute("MOVE_EVENT")
			if(defaultEvent?.MOVE_EVENT){
				moveEvent = MoveEvent.findById(defaultEvent.MOVE_EVENT)
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
	def listEventNewsJson = {
		
		def projectId =  getSession().getAttribute('CURR_PROJ').CURR_PROJ
		def projectInstance = Project.findById( projectId )
		def bundleId = params.moveBundle
		def viewFilter = params.viewFilter
		def moveBundleInstance = null
		def assetCommentsList
		def moveEventNewsList
		def offset = params.offset
		userPreferenceService.loadPreferences("CURR_BUNDLE")
		def defaultBundle = getSession().getAttribute("CURR_BUNDLE")
		def moveEventsList = MoveEvent.findAllByProject(projectInstance)
		def moveEvent = MoveEvent.read(params.moveEvent)
		def moveBundlesList
		if(bundleId){
			moveBundleInstance = MoveBundle.findById(bundleId)
		}
		def sortIndex = params.sidx ?: 'comment'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def dueFormatter = new SimpleDateFormat("MM/dd/yyyy")
		
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
		
		def queryForCommentsList = new StringBuffer(assetCommentsQuery.toString() +" union all "+ moveEventNewsQuery.toString())
		
		queryForCommentsList.append("order by $sortIndex $sortOrder ")
		def totalComments = jdbcTemplate.queryForList( queryForCommentsList.toString() )
		
		def totalRows = totalComments.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)

		def results = totalComments?.collect {
			[ cell: [ it.createdAt ? dueFormatter.format(TimeUtil.convertInToUserTZ(it.createdAt, tzId)):'',
					it.createdBy, it.commentType, it.comment, it.resolution,
					it.resolvedAt ? dueFormatter.format(TimeUtil.convertInToUserTZ(it.resolvedAt, tzId)):'',
					it.resolvedBy], id: it.id]
			}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	/*-------------------------------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : id and comment type
	 * @return : assetComment / moveEventNews object based on comment Type as JSON object
	 *-------------------------------------------------------------------*/
	def getCommetOrNewsData = {
		def commentList = []
		def personResolvedObj
		def personCreateObj
		def dtCreated 
		def dtResolved 
		DateFormat formatter ; 
		formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetName
		def commentType = params.commentType
		def commentObject
		if( commentType == 'issue' || commentType == 'I'){
			commentObject = AssetComment.get( params.id )
			if(commentObject?.resolvedBy){
				personResolvedObj = Person.find("from Person p where p.id = $commentObject.resolvedBy.id")?.toString()
				dtResolved = formatter.format(GormUtil.convertInToUserTZ(commentObject.dateResolved, tzId));
			} 
			assetName = commentObject.assetEntity.assetName 
		} else {
			commentObject = MoveEventNews.get( params.id )
			if(commentObject?.archivedBy){
				personResolvedObj = Person.find("from Person p where p.id = $commentObject.archivedBy.id")?.toString()
				dtResolved = formatter.format(GormUtil.convertInToUserTZ(commentObject.dateArchived, tzId));
			} 
			
		}
		if(commentObject?.createdBy){
			personCreateObj = Person.find("from Person p where p.id = $commentObject.createdBy.id")?.toString()
			dtCreated = formatter.format(GormUtil.convertInToUserTZ(commentObject.dateCreated, tzId));
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
	def updateNewsOrComment = {
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		def commentType = params.commentType
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		if(commentType == "issue"){
			def assetCommentInstance = AssetComment.get(params.id)
			if(params.isResolved == '1' && assetCommentInstance.isResolved == 0 ){
				assetCommentInstance.resolvedBy = loginUser.person
				assetCommentInstance.dateResolved = GormUtil.convertInToGMT( "now", tzId )
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
				moveEventNewsInstance.dateArchived = GormUtil.convertInToGMT( "now", tzId )
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
	    
		redirect(action:newsEditorList, params:[moveBundle : params.moveBundle, viewFilter:params.viewFilter])
	}
	
	/*---------------------------------------------------------
     * @author : Lokanada Reddy
     * @param  : project, bundle, and filters, moveEventNews data
     * @return : will save the data and redirect to action : newsEditorList
     *--------------------------------------------------------*/
	def saveNews = {
			
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		def moveEventNewsInstance = new MoveEventNews(params)
		moveEventNewsInstance.createdBy = loginUser.person
		
		if(params.isArchived == '1'){
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			moveEventNewsInstance.isArchived = 1
			moveEventNewsInstance.archivedBy = loginUser.person
			moveEventNewsInstance.dateArchived = GormUtil.convertInToGMT( "now", tzId )
		}
		moveEventNewsInstance.save(flush:true)
		redirect(action:newsEditorList, params:[ moveBundle : params.moveBundle, 
												viewFilter:params.viewFilter, moveEvent:params.moveEvent.id])
	}
	def truncate( value ){
		def returnVal = ""
		if(value){
			def length = value.size()
			if(length > 50){
				returnVal = '"'+value.substring(0,50)+'.."'
			} else {
				returnVal = '"'+value+'"'
			}
		}
		return returnVal
	}
}
