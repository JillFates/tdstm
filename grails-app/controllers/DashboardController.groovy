import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdsops.common.lang.ExceptionUtil

class DashboardController {
	
	def assetEntityService
	def dashboardService
	def projectService
	def securityService
	def taskService
	def userService
	def userPreferenceService
	
	def index = {
		
		def moveEvent
		def user = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject();
		if (! project) {
			flash.message = "Please select project to view User Dashboard"
			redirect(controller:'project',action:'list')
			return
		}
		
		if (params.moveEvent && params.moveEvent.isInteger()) {
			moveEvent = MoveEvent.findById(params.moveEvent)
			if (moveEvent && moveEvent.project != project) {
				log.warn "SECURITY : User $user attempted to access event ($moveEvent) that was not associate to his current project ($project)"
				flash.message = "Sorry but the event select was not found. Please select another event before retrying."
				redirect(controller:"moveEvent",action:"list")
			}
		} 

		if (! moveEvent) {
			userPreferenceService.loadPreferences("MOVE_EVENT")
			def defaultEvent = getSession().getAttribute("MOVE_EVENT")
			if (defaultEvent.MOVE_EVENT) {
				// Try finding the event in the user's preferences
				moveEvent = MoveEvent.findByIdAndProject(defaultEvent.MOVE_EVENT, project)
			}
			if (!moveEvent) {
				// Last resort, get the first moveEvent in the list ordered by name
				moveEvent = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[project])				
			}
		}

		if (! moveEvent) {
			flash.message = "Please select move event to view Event Dashboard"
			redirect(controller:"moveEvent",action:"list")
		} else {
			// Save the user's preference for the current move event
			userPreferenceService.setPreference( "MOVE_EVENT", "${moveEvent.id}" )

			// Start getting the data to build the data model
			def moveEventsList = MoveEvent.findAllByProject(project,[sort:'name',order:'asc'])
			def projectLogo = ProjectLogo.findByProject(project)
			userPreferenceService.loadPreferences("DASHBOARD_REFRESH")
			def timeToUpdate = getSession().getAttribute("DASHBOARD_REFRESH")
			def moveBundleList = MoveBundle.findAll(" FROM MoveBundle mb where moveEvent = ${moveEvent.id} ORDER BY mb.startTime ")			


			def model = [:]
			model.project = project
			model.projectLogo = projectLogo
			model.moveEvent = moveEvent
			model.moveEventsList = moveEventsList
			model.moveBundleList = moveBundleList
			model.timeToUpdate = timeToUpdate ? timeToUpdate.DASHBOARD_REFRESH : 'never'
			model.manualOverrideViewPermission = RolePermissions.hasPermission('ManualOverride')

			try {
				def taskSummary = dashboardService.getTaskSummaryModel(moveEvent.id, user, project)
				if (taskSummary)
					model.putAll(taskSummary)
			} catch (e) {
				log.warn "Encountered error (${e.getMessage()}) calling getTaskSummaryModel for $moveEvent\n${ExceptionUtil.stackTraceToString(e,60)}"
				flash.error = "Sorry but an unexpected error was encountered and not all data was able to be gathered for the dashboard."
			}

			return model
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
	
	/**
	 * Used to render the Task Summary HTML that appears in the Event dashboard
	 * @param id moveEventId
	 * @return HTML
	 */
	def taskSummary = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def id = params.id
		def currentProject = securityService.getUserCurrentProject()

		try {
			def model = dashboardService.getTaskSummaryModel(id, loginUser, currentProject)
			render(template: 'taskSummary', model:model)
		} catch (e) {
			render e.getMessage()
		}
	}

	/**
	 * user portal details for default project.
	 * 
	 */
	def userPortal = {
		def projectInstance = securityService.getUserCurrentProject()
		def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
		def userProjects = projectService.getUserProjects(securityService.getUserLogin(), projectHasPermission, ProjectStatus.ACTIVE)
		if(!projectInstance){
			flash.message = "Please select project to view User Dashboard"
			redirect(controller:'project',action:'list')
		}else{
			def dispProjs = [projectInstance]
			if (userProjects) {
				dispProjs = userProjects+projectInstance
				dispProjs = dispProjs.unique()
			}
			return [projects:dispProjs, projectInstance:projectInstance, loggedInPerson : securityService.getUserLoginPerson()]
		}
	}
	
	/**
	 * This action will load the event template for User Dashboard.
	 * @param : project id of selected project 
	 * @render : events template
	 * 
	 */
	def getEvents = {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		if(projectInstance!='All'){
			userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )
		}
		render (template :'events', model:[ upcomingEvents:userService.getEventDetails(projectInstance), project:projectInstance,
			staffRoles:taskService.getTeamRolesForTasks()])
	}
	
	/**
	 * This action will load the event news for User Dashboard.
	 * @param : project id of selected project 
	 * @render : eventNews template
	 * 
	 */
	def getEventsNewses = {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		render (template :'eventNews', model:[ newsList:userService.getEventNewses(projectInstance), project:projectInstance])
	}
	
	/**
	 * This action will load the tasks for User Dashboard.
	 * @param : project id of selected project 
	 * @render : tasks template
	 * 
	 */
	def getTaskSummary  ={
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		def taskSummary = userService.getTaskSummary(projectInstance)
		render (template :'tasks', model:[ taskList:taskSummary.taskList, timeInMin:taskSummary.timeInMin, project:projectInstance,
			dueTaskCount:taskSummary.dueTaskCount, personId:taskSummary.personId])
	}
	
	/**
	 * This action will load the apps for User Dashboard.
	 * @param : project id of selected project 
	 * @render : application template 
	 * 
	 */
	def getApplications  ={
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		def appSummary = userService.getApplications(projectInstance)
		render (template :'application', model:[ appList:appSummary.appList, relationList:appSummary.relationList, project:projectInstance ])
	
	}
	
	/**
	 * This action will load the active people for User Dashboard.
	 * @param : project id of selected project 
	 * @render : activePeople template 
	 * 
	 */
	def getActivePeople  ={
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		render (template :'activePeople', model:[ recentLogin:userService.getActivePeople(projectInstance), project:projectInstance ])
	}
	
	/**
	 * This action will load the related Entities User Dashboard that would be hidden.
	 * @param : project id of selected project 
	 * @render : activePeople template 
	 */
	def getRelatedEntities={
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		def entities = [:]
		def moveBundleList = []
		render (template :'relatedEntities', model:[servers:entities.servers, applications:entities.applications, dbs:entities.dbs,
			files:entities.files, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus, assetDependency: new AssetDependency(),
			staffRoles:taskService.getTeamRolesForTasks(), moveBundleList:moveBundleList])
	}
	
}
