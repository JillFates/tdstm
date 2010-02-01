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
		if ( !params.max ) params.max = 1
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

		def assetCommentsQuery = new StringBuffer( "select date_created as createdAt, created_by as createdBy, resolved_by as resolvedBy, "+
								" comment_type as commentType, comment , resolution, date_resolved as resolvedAt from asset_comment ac"+
								" left join asset_entity ae on (ae.asset_entity_id = ac.asset_entity_id)"+
								" left join move_bundle mb on (mb.move_bundle_id = ae.move_bundle_id)"+
								" left join project p on (p.project_id = ae.project_id) where ac.comment_type = 'issue' and " )
        def moveEventNewsQuery = new StringBuffer( "select date_created as createdAt, created_by as createdBy, resolved_by as resolvedBy, "+
								" comment_type as commentType, comment , resolution, date_resolved as resolvedAt from move_event_news mn"+
								" left join asset_entity ae on (ae.asset_entity_id = mn.asset_entity_id)"+
								" left join move_bundle mb on (mb.move_bundle_id = ae.move_bundle_id)"+
								" left join move_event me on ( me.move_event_id = mb.move_event_id )"+
								" left join project p on (p.project_id = ae.project_id) where mn.comment_type = 'news' and " )
        
    	if(moveBundleInstance != null){
    		assetCommentsQuery.append(" mb.move_bundle_id = ${moveBundleInstance.id}  ")
    	} else {
    		assetCommentsQuery.append(" p.project_id = ${projectInstance.id} ")
    	}
        
		if(moveEvent){
			moveEventNewsQuery.append(" mb.move_event_id = ${moveBundleInstance.moveEvent.id}  ")
		} else {
			moveEventNewsQuery.append(" p.project_id = ${projectInstance.id} ")			
		}
		if(viewFilter == "active"){
			assetCommentsQuery.append(" and ac.is_resolved = 0 ")
			moveEventNewsQuery.append(" and mn.is_resolved = 0 ")
		} else if(viewFilter == "archived"){
			assetCommentsQuery.append(" and ac.is_resolved = 1 ")
			moveEventNewsQuery.append(" and mn.is_resolved = 1 ")
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

		return [moveBundlesList : moveBundlesList, assetCommentsList : totalComments, projectId : projectId, 
				params : params, totalCommentsSize : totalCommentsSize]
    }
}
