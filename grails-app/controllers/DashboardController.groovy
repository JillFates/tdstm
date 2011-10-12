import org.jsecurity.SecurityUtils

import com.tds.asset.AssetComment
import com.tdssrc.grails.GormUtil

class DashboardController {
	
	def userPreferenceService
    
	def index = { 
		
		def projectId = params.projectId
		def project
		def moveEventsList
		def projectLogo
		def moveEvent
		def moveBundleList
		
		if( !projectId ){
			project = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		} else {
			project = Project.findById( projectId )
		}
		moveEventsList = MoveEvent.findAllByProject(project)
		projectLogo = ProjectLogo.findByProject(project)
		def moveEventId = params.moveEvent
		
		if(moveEventId){
			userPreferenceService.setPreference( "MOVE_EVENT", "${moveEventId}" )
            moveEvent = MoveEvent.findById(moveEventId)
		} else {
            userPreferenceService.loadPreferences("MOVE_EVENT")
            def defaultEvent = getSession().getAttribute("MOVE_EVENT")
            if(defaultEvent.MOVE_EVENT){
            	moveEvent = MoveEvent.findById(defaultEvent.MOVE_EVENT)
            	if( moveEvent?.project?.id != project?.id ){
            		moveEvent = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[project])
            	}
            } else {
            	moveEvent = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[project])
            }
        }
		userPreferenceService.loadPreferences("DASHBOARD_REFRESH")
        def timeToUpdate = getSession().getAttribute("DASHBOARD_REFRESH")
		def subject = SecurityUtils.subject
        if( moveEvent ){
        	userPreferenceService.setPreference("MOVE_EVENT","${moveEvent.id}")
			moveBundleList = MoveBundle.findAll(" FROM MoveBundle mb where moveEvent = ${moveEvent.id} ORDER BY mb.startTime ")				
		}
		return [ moveEventsList : moveEventsList, moveEvent : moveEvent, project : project, projectLogo : projectLogo, 
				 moveBundleList : moveBundleList, timeToUpdate : timeToUpdate ? timeToUpdate.DASHBOARD_REFRESH : "never",
				 isAdmin:subject.hasRole("ADMIN"), isProjManager:subject.hasRole("PROJ_MGR")]
    }
	
	/*---------------------------------------------------------
	 * Will set user preference for DASHBOARD_REFRESH time
	 * @author : Lokanath Reddy
	 * @param  : refresh time 
	 * @return : refresh time 
	 *---------------------------------------------------------*/
	def setTimePreference = {
        def timer = params.timer
        if(timer){
            userPreferenceService.setPreference( "DASHBOARD_REFRESH", "${timer}" )
        }
        def timeToRefresh = getSession().getAttribute("DASHBOARD_REFRESH")
        render timeToRefresh ? timeToRefresh.DASHBOARD_REFRESH : 'never'
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
		redirect(action:index)
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
	    
		redirect(action:index)
	}
	
}
