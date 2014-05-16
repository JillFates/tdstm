import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetComment
import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil

class DashboardController {
	
	def userPreferenceService
    def taskService
	def projectService
	def securityService
	def userService
	
    
	def index = {
		
		def projectId = session.CURR_PROJ.CURR_PROJ
		def moveEvent
		
		def project = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
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
        if(!moveEvent){
            flash.message = "Please select move event to view Event Dashboard"
            redirect(controller:"moveEvent",action:"list")
        } else {
            def moveEventsList = MoveEvent.findAllByProject(project,[sort:'name',order:'asc'])
            def projectLogo = ProjectLogo.findByProject(project)
    		userPreferenceService.loadPreferences("DASHBOARD_REFRESH")
            def timeToUpdate = getSession().getAttribute("DASHBOARD_REFRESH")
    		def subject = SecurityUtils.subject
            //code for task Summary and task progress bars
        	userPreferenceService.setPreference("MOVE_EVENT","${moveEvent.id}")
			def moveBundleList = MoveBundle.findAll(" FROM MoveBundle mb where moveEvent = ${moveEvent.id} ORDER BY mb.startTime ")			
            def results = taskService.getMoveEventTaskSummary(moveEvent)
            def teamTaskResults = taskService.getMoveEventTeamTaskSummary(moveEvent)
            
    		return [ moveEventsList : moveEventsList, moveEvent : moveEvent, project : project, projectLogo : projectLogo, 
    				 moveBundleList : moveBundleList, timeToUpdate : timeToUpdate ? timeToUpdate.DASHBOARD_REFRESH : "never",
    				 manualOverrideViewPermission:RolePermissions.hasPermission("ManualOverride"),
    				 taskCountByEvent:results.taskCountByEvent, taskStatusMap:results.taskStatusMap, totalDuration:results.totalDuration,
					 teamTaskMap:teamTaskResults, roles:teamTaskResults.values().role]
        }
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
	
    /**
     * ajax call to find the given event task summary ( taskCounts, tatalDuratin, tasks by status)
     * @param id moveEventId
     * @param Render taskSummary template
     */
    def taskSummary = {
        
        def moveEvent = MoveEvent.read(params.id)
        def results = taskService.getMoveEventTaskSummary(moveEvent)
		def teamTaskResults = taskService.getMoveEventTeamTaskSummary(moveEvent)
        def taskRoles = teamTaskResults.values().role
		def rolesPercentageMap = [:]
		taskRoles.each{t->
			rolesPercentageMap << [(t.id): params.(t.id)]
		}
		render(template:'taskSummary',
					model:[taskCountByEvent:results.taskCountByEvent, taskStatusMap:results.taskStatusMap, 
					   	   totalDuration:results.totalDuration,teamTaskMap:teamTaskResults,
						   roles:taskRoles, 'taskStartedwidth':params.taskStartedWidthId,
					       'taskReadywidth':params.taskReadyWidthId,'taskDonewidth':params.taskDoneWidthId, 
					       'effortStartedwidth':params.effortStartedWidthId, 'effortReadywidth':params.effortReadyWidthId,
					       'effortDonewidth':params.effortDoneWidthId, rolesPercentageMap:rolesPercentageMap])
        
    }
	/**
	 * user portal details for default project.
	 * 
	 */
	def userPortal = {
		def projectInstance = securityService.getUserCurrentProject()
		def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
		def userProjects = projectService.getUserProjects(securityService.getUserLogin(), projectHasPermission, ProjectStatus.ACTIVE)
		
		def details = userService.getuserPortalDetails(projectInstance)
		
		return [projects:userProjects, projectInstance:projectInstance, taskList:details.taskList, timeInMin:details.timeInMin,
				appList:details.appList, relationList:details.relationList, upcomingEvents:details.upcomingEvents, recentLogin:details.recentLogin,
				newsList:details.newsList]
	}
	/**
	 * ajax function for user portal details for user selected projects.
	 *
	 */
	def userPortalDetails = {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		if(projectInstance!='All'){
			userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )
		}
		def details = userService.getuserPortalDetails(projectInstance)
		render(template :'portal', model:[ taskList:details.taskList, timeInMin:details.timeInMin, newsList:details.newsList,
				appList:details.appList, relationList:details.relationList, upcomingEvents:details.upcomingEvents, recentLogin:details.recentLogin])
	}
}
