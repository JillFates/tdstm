import com.tds.asset.AssetDependency
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import org.apache.shiro.SecurityUtils

import java.text.DateFormat
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF

class DashboardController {

	def assetEntityService
	def dashboardService
	def projectService
	def securityService
	def taskService
	def userService
	def userPreferenceService

	def index() {

		def moveEvent
		def user = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()
		if (! project) {
			flash.message = "Please select project to view User Dashboard"
			redirect(controller:'project',action:'list')
			return
		}

		if (params.moveEvent && params.moveEvent.isInteger()) {
			moveEvent = MoveEvent.get(params.moveEvent)
			if (moveEvent && moveEvent.project != project) {
				log.warn "SECURITY : User $user attempted to access event ($moveEvent) that was not associate to his current project ($project)"
				flash.message = "Sorry but the event select was not found. Please select another event before retrying."
				redirect(controller:"moveEvent",action:"list")
			}
		}

		if (! moveEvent) {
			//TODO: OLB remove reloading of Map
			userPreferenceService.loadPreferences(PREF.MOVE_EVENT)
			def defaultEvent = session.getAttribute("MOVE_EVENT")
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
			userPreferenceService.setPreference(PREF.MOVE_EVENT, "${moveEvent.id}" )

			// Start getting the data to build the data model
			def moveEventsList = MoveEvent.findAllByProject(project,[sort:'name',order:'asc'])
			def projectLogo = ProjectLogo.findByProject(project)
			userPreferenceService.loadPreferences(PREF.DASHBOARD_REFRESH)
			def timeToUpdate = session.getAttribute("DASHBOARD_REFRESH")
			def moveBundleList = MoveBundle.findAll(" FROM MoveBundle mb where moveEvent = ${moveEvent.id} ORDER BY mb.startTime ")

			// handle the view unpublished tasks checkbox
			if (params.containsKey('viewUnpublished')) {
				def unpublishVal = (params.viewUnpublished == '1')?'true':'false'
				userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED, unpublishVal)
			}

			def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) == 'true')

			def model = [:]
			model.project = project
			model.projectLogo = projectLogo
			model.moveEvent = moveEvent
			model.moveEventsList = moveEventsList
			model.moveBundleList = moveBundleList
			model.timeToUpdate = timeToUpdate ? timeToUpdate.DASHBOARD_REFRESH : 'never'
			model.manualOverrideViewPermission = RolePermissions.hasPermission('ManualOverride')
			model.viewUnpublished = viewUnpublished ? '1' : '0'

			try {
				def taskSummary = dashboardService.getTaskSummaryModel(moveEvent.id, user, project, 6, viewUnpublished)
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
	 * @author : Lokanada Reddy
	 * @param  : project, bundle, and filters, moveEventNews data
	 * @return : will save the data and redirect to action : newsEditorList
	 *--------------------------------------------------------*/
	def saveNews() {
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		def moveEventNewsInstance = new MoveEventNews(params)
		moveEventNewsInstance.createdBy = loginUser.person

		if(params.isArchived == '1'){
			def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
			moveEventNewsInstance.isArchived = 1
			moveEventNewsInstance.archivedBy = loginUser.person
			moveEventNewsInstance.dateArchived = TimeUtil.nowGMT()
		}
		moveEventNewsInstance.save(flush:true)
		redirect(action:"index")
	}

	/**
	 * Used to render the Task Summary HTML that appears in the Event dashboard
	 * @param id moveEventId
	 * @return HTML
	 */
	def taskSummary() {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def id = params.id
		def currentProject = securityService.getUserCurrentProject()
		def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) == 'true')

		try {
			def model = dashboardService.getTaskSummaryModel(id, loginUser, currentProject, 6, viewUnpublished)
			render(template: 'taskSummary', model:model)
		} catch (e) {
			render e.getMessage()
		}
	}

	/**
	 * user portal details for default project.
	 *
	 */
	def userPortal() {
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
	def retrieveEventsList() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		if(projectInstance!='All'){
			userPreferenceService.setPreference(PREF.CURR_PROJ, "${projectInstance.id}" )
		}

		def result = new ArrayList()
		def upComingEvents = userService.getEventDetails(projectInstance)
		upComingEvents.each{ entry ->
			result.add(
					[
						 eventId: upComingEvents[entry.key].moveEvent.id,
						 projectName: upComingEvents[entry.key].moveEvent.project.name,
						 name: upComingEvents[entry.key].moveEvent.name,
						 startDate: upComingEvents[entry.key].moveEvent.eventTimes.start,
						 days: upComingEvents[entry.key].daysToGo + ' days',
						 teams: upComingEvents[entry.key].teams
					])

		}

		render result as JSON
	}

	/**
	 * This action will load the event template for User Dashboard.
	 * @param : project id of selected project
	 * @render : events template
	 *
	 */
	def retrieveEvents() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		if(projectInstance!='All'){
			userPreferenceService.setPreference(PREF.CURR_PROJ, "${projectInstance.id}" )
		}
		render (template :'events', model:[ upcomingEvents:userService.getEventDetails(projectInstance), project:projectInstance,
			staffRoles:taskService.getTeamRolesForTasks()])
	}

	/**
	 * This action will load the event news template for User Dashboard.
	 * @param : project id of selected project
	 * @render : events template
	 *
	 */
	def retrieveEventsNewsList() {
		// TODO : JPM 6/2016 : SECURITY : Users should only have the projects that they can access
		Map map = [
			project: null,
			newsList: []
		]

		def projectOrAll
		List newsList = []
		Long projectId = NumberUtil.toPositiveLong(params.project, -1)
		if ( projectId > 0) {
			projectOrAll = Project.get(projectId)
			if (projectOrAll) {
				// TODO : JPM 6/2016 : SECURITY : Should be checking if user has access to project
				userPreferenceService.setPreference(PREF.CURR_PROJ, "${projectId}" )
			} else {
				projectId = -1
			}
		} else if (projectId == 0) {
			projectOrAll = 'All'
		}

		if (projectId == -1) {
			projectOrAll = securityService.getUserCurrentProject()
		}

		if (projectOrAll) {
			newsList = userService.getEventNews(projectOrAll)
		}

		/*



		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		if (projectInstance != 'All') {
			userPreferenceService.setPreference(PREF.CURR_PROJ, "${projectInstance.id}" )
		}
		*/
		List result = new ArrayList()
		// def newsList = userService.getEventNews(projectInstance)
		newsList.each { news ->
			result.add(
				[
					eventId: news.moveEvent.id,
					projectName: news.moveEvent.project.name,
					date: news.dateCreated,
					event: news.moveEvent.name,
					news: news.message
				]
			)
		}

		render result as JSON
	}

	/**
	 * This action will load the event news for User Dashboard.
	 * @param : project id of selected project
	 * @render : eventNews template
	 *
	 */
	def retrieveEventsNews() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		render (template :'eventNews', model:[ newsList:userService.getEventNews(projectInstance), project:projectInstance])
	}

	/**
	 * This action will load the tasks Template for User Dashboard.
	 * @param : project id of selected project
	 * @render : tasks template
	 */
	def retrieveTaskSummaryList() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'

		def result = new ArrayList()
		def taskSummary = userService.getTaskSummary(projectInstance)
		def DateFormat formatter = TimeUtil.createFormatter(session, "MM/dd kk:mm")

		taskSummary.taskList.each { task ->
			if(task.item.estFinish != null) {
				task.item.estFinish = task.item.estFinish.clearTime()
			}
			result.add(
					[
							projectName: task.projectName,
							taskId: task.item.id,
							task: ((task.item.taskNumber)? task.item.taskNumber + " - " : "" ) + task.item.comment,
							css: task.css,
							overDue: (task.item.dueDate && task.item.dueDate < TimeUtil.nowGMT()? 'task_overdue' : ''),
							assetClass: task.item.assetClass.toString(),
							assetId: task.item.assetId,
							related: task.item.assetName,
							dueEstFinish: TimeUtil.formatDateTimeWithTZ(TimeUtil.defaultTimeZone, (task.item.estFinish != null? task.item.estFinish : new Date()), formatter),
							status: task.item.status
					])

		}

		def totalDuration = TimeUtil.ago(taskSummary.totalDuration, TimeUtil.SHORT)
		def dueTaskCount = taskSummary.dueTaskCount

		def summaryDetail = 'No active tasks were found.'
		if (taskSummary.taskList.size() > 0)
			summaryDetail = taskSummary.taskList.size() + ' assigned tasks with ' + totalDuration + ' of duration' + (dueTaskCount ? ' (' +  dueTaskCount + ' are over due).' : '.')

		Map data = [
				taskList: result,
				summaryDetail: summaryDetail
		]

		render data as JSON
	}

	/**
	 * This action will load the tasks for User Dashboard.
	 * @param : project id of selected project
	 * @render : tasks template
	 *
	 */
	def retrieveTaskSummary() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		def taskSummary = userService.getTaskSummary(projectInstance)
		render (template :'tasks', model:[ taskList:taskSummary.taskList, totalDuration:taskSummary.totalDuration, project:projectInstance,
			dueTaskCount:taskSummary.dueTaskCount, personId:taskSummary.personId])
	}

	/**
	 *  This action will load Application template for User Dashboard.
	 * @param : project id of selected project
	 * @render : application template
	 *
	 */
	def retrieveApplicationsList() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		if(projectInstance!='All'){
			userPreferenceService.setPreference(PREF.CURR_PROJ, "${projectInstance.id}" )
		}

		def result = new ArrayList()
		def appSummary = userService.getApplications(projectInstance)
		appSummary.appList.each { app ->
			result.add(
					[
							projectName: app.project.name,
							name: app.assetName,
							appId: app.id,
							assetClass: app.assetClass.toString(),
							planStatus: app.planStatus,
							moveBundle: app.moveBundle.name,
							relation: appSummary.relationList[app.id]
					])
		}

		render result as JSON

	}

	/**
	 * This action will load the apps for User Dashboard.
	 * @param : project id of selected project
	 * @render : application template
	 *
	 */
	def retrieveApplications() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		def appSummary = userService.getApplications(projectInstance)
		render (template :'application', model:[ appList:appSummary.appList, relationList:appSummary.relationList, project:projectInstance ])

	}

	/**
	 * This action will load the active people Template for User Dashboard.
	 * @param : project id of selected project
	 * @render : activePeople template
	 *
	 */
	def retrieveActivePeopleList() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		if(projectInstance!='All'){
			userPreferenceService.setPreference(PREF.CURR_PROJ, "${projectInstance.id}" )
		}

		def result = new ArrayList()
		def recentLogin = userService.getActivePeople(projectInstance)
		recentLogin.each { login ->
			result.add(
					[
							personId: login.personId,
							projectName: login.projectName,
							personName: login.personName,
							lastActivity: login.lastActivity,
					])
		}

		render result as JSON
	}

	/**
	 * This action will load the active people for User Dashboard.
	 * @param : project id of selected project
	 * @render : activePeople template
	 *
	 */
	def retrieveActivePeople() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		render (template :'activePeople', model:[ recentLogin:userService.getActivePeople(projectInstance), project:projectInstance ])
	}

	/**
	 * This action will load the related Entities User Dashboard that would be hidden.
	 * @param : project id of selected project
	 * @render : activePeople template
	 */
	def retrieveRelatedEntities() {
		def projectInstance = params.project!='0' ? Project.get(params.project) : 'All'
		def entities = [:]
		def moveBundleList = []
		render (template :'relatedEntities', model:[servers:entities.servers, applications:entities.applications, dbs:entities.dbs,
			files:entities.files, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus, assetDependency: new AssetDependency(),
			staffRoles:taskService.getTeamRolesForTasks(), moveBundleList:moveBundleList])
	}

}
