
import grails.converters.JSON
import java.text.DateFormat
import java.text.SimpleDateFormat
import org.jsecurity.SecurityUtils

class NewsEditorController {
	
	def userPreferenceService
	def jdbcTemplate
	
    def index = {
    	redirect( action:newsEditorList, params:params )
	}
    /*---------------------------------------------------------
     * @author : Lokanada Reddy
     * @param  : project, bundle, and filters
     * @return : Union of assets issues and move event news
     *--------------------------------------------------------*/
	def newsEditorList = {
		if ( !params.max ) params.max = 25
    	def projectId =  params.projectId
    	def projectInstance = Project.findById( projectId )
    	def bundleId = params.moveBundle
		def viewFilter = params.viewFilter
		def moveBundleInstance
		def assetCommentsList
		def moveEventNewsList
		def moveEvent
		def offset = params.offset
		userPreferenceService.loadPreferences("CURR_BUNDLE")
        def defaultBundle = getSession().getAttribute("CURR_BUNDLE")
        if(defaultBundle.CURR_BUNDLE){
        	def bundleInSession = MoveBundle.findById(defaultBundle.CURR_BUNDLE)
        	moveEvent = bundleInSession.moveEvent
        }

    	def moveBundlesList
    	if(moveEvent){
    		moveBundlesList = MoveBundle.findAll("from MoveBundle mb where mb.moveEvent = ${moveEvent.id} order by mb.name asc")
    	} else {
    		moveBundlesList = MoveBundle.findAll("from MoveBundle mb where mb.project = ${projectId} order by mb.name asc")
    	}
    	
		if(bundleId){
            moveBundleInstance = MoveBundle.findById(bundleId)
        } 

		def assetCommentsQuery = new StringBuffer( "select ac.asset_comment_id as id, date_created as createdAt, display_option as displayOption, "+
								" CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy, ac.comment_type as commentType, comment , "+
								" resolution, date_resolved as resolvedAt, ae.asset_entity_id as assetEntity from asset_comment ac"+
								" left join asset_entity ae on (ae.asset_entity_id = ac.asset_entity_id)"+
								" left join move_bundle mb on (mb.move_bundle_id = ae.move_bundle_id)"+
								" left join project p on (p.project_id = ae.project_id) left join person p1 on (p1.person_id = ac.created_by)"+
								" left join person p2 on (p2.person_id = ac.resolved_by) where ac.comment_type = 'issue' and " )
        def moveEventNewsQuery = new StringBuffer( "select mn.move_event_news_id as id, date_created as createdAt, 'U' as displayOption, "+
								" CONCAT_WS(' ',p1.first_name, p1.last_name) as createdBy, CONCAT_WS(' ',p2.first_name, p2.last_name) as resolvedBy, 'news' as commentType, message as comment , "+
								" resolution, date_archived as resolvedAt, null as assetEntity from move_event_news mn"+
								" left join move_event me on ( me.move_event_id = mn.move_event_id )"+
								" left join project p on (p.project_id = me.project_id) left join person p1 on (p1.person_id = mn.created_by)"+
								" left join person p2 on (p2.person_id = mn.archived_by) where " )
        
    	if(moveBundleInstance != null){
    		assetCommentsQuery.append(" mb.move_bundle_id = ${moveBundleInstance.id}  ")
    	} else {
    		assetCommentsQuery.append(" p.project_id = ${projectInstance.id} ")
    	}
        
		if(moveEvent){
			moveEventNewsQuery.append(" mn.move_event_id = ${moveEvent.id}  ")
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
		
		def queryForCommentsList = new StringBuffer(assetCommentsQuery.toString() +" union all "+ moveEventNewsQuery.toString())
		    	
    	if(params.sort && params.order){
    		queryForCommentsList.append( " order by ${params.sort} ${params.order}" )
    	} else {
    		queryForCommentsList.append( " order by createdAt desc" )
    	}
		def totalCommentsSize = jdbcTemplate.queryForList( queryForCommentsList.toString() )?.size()
		if(offset){
			queryForCommentsList.append( " limit ${offset}, ${params.max}")
		} else {
			queryForCommentsList.append( " limit ${params.max}")	
		}
		
    	def totalComments = jdbcTemplate.queryForList( queryForCommentsList.toString() )
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		return [moveBundlesList : moveBundlesList, assetCommentsList : totalComments, projectId : projectId, 
				params : params, totalCommentsSize : totalCommentsSize, moveEvent : moveEvent, loginPerson : loginUser.person]
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
		formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
		def assetName
		def commentType = params.commentType
		def commentObject
		if( commentType == 'issue' ){
			commentObject = AssetComment.get( params.id )
			if(commentObject.resolvedBy){
				personResolvedObj = Person.find("from Person p where p.id = $commentObject.resolvedBy.id")
				dtResolved = formatter.format(commentObject.dateResolved);
			} 
			assetName = commentObject.assetEntity.assetName 
		} else {
			commentObject = MoveEventNews.get( params.id )
			if(commentObject.archivedBy){
				personResolvedObj = Person.find("from Person p where p.id = $commentObject.archivedBy.id")
				dtResolved = formatter.format(commentObject.dateArchived);
			} 
			
		}
		if(commentObject.createdBy){
			personCreateObj = Person.find("from Person p where p.id = $commentObject.createdBy.id")
			dtCreated = formatter.format(commentObject.dateCreated);
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
	    
		redirect(action:newsEditorList, params:[projectId:params.projectId, moveBundle : params.moveBundle, viewFilter:params.viewFilter])
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
			moveEventNewsInstance.isArchived = 1
			moveEventNewsInstance.archivedBy = loginUser.person
			moveEventNewsInstance.dateArchived = new Date()
		}
		moveEventNewsInstance.save(flush:true)
		redirect(action:newsEditorList, params:[projectId:params.projectId, moveBundle : params.moveBundle, viewFilter:params.viewFilter])
	}
}
