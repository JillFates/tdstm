import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectLogo
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.DashboardService
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.UserService

import java.text.DateFormat

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class DashboardController implements ControllerMethods {

	AssetEntityService assetEntityService
	ControllerService controllerService
	DashboardService dashboardService
	ProjectService projectService
	TaskService taskService
	UserPreferenceService userPreferenceService
	UserService userService
	MoveEventService moveEventService

	@HasPermission(Permission.DashboardMenuView)
	def index() {
		licenseAdminService.checkValidForLicenseOrThrowException()
		Project project = controllerService.getProjectForPage(this, 'to view User Dashboard')
		if (!project) return

		def moveEvent
		if (params.moveEvent?.isInteger()) {
			moveEvent = MoveEvent.get(params.moveEvent)
			if (moveEvent && moveEvent.project != project) {
				log.warn "SECURITY : User $securityService.currentUsername attempted to access event ($moveEvent) that was not associate to his current project ($project)"
				flash.message = "Sorry but the event select was not found. Please select another event before retrying."
				redirect(controller: "moveEvent", action: "list")
			}
		}

		if (! moveEvent) {
			def defaultEvent = userPreferenceService.moveEventId
			if (defaultEvent) {
				// Try finding the event in the user's preferences
				moveEvent = MoveEvent.findByIdAndProject(defaultEvent, project)
			}
			if (!moveEvent) {
				// Last resort, get the first moveEvent in the list ordered by name
				moveEvent = MoveEvent.findByProject(project, [sort: 'name', order: 'asc'])
			}
		}

		if (! moveEvent) {
			flash.message = "Please select move event to view Event Dashboard"
			redirect(controller: "moveEvent", action: "list")
		} else {
			// Save the user's preference for the current move event
			userPreferenceService.setMoveEventId(moveEvent.id)

			// Start getting the data to build the data model
			def moveEventsList = MoveEvent.findAllByProject(project,[sort:'name',order:'asc'])
			def projectLogo = ProjectLogo.findByProject(project)
			def timeToUpdate = userPreferenceService.getPreference(PREF.DASHBOARD_REFRESH)
			def moveBundleList = MoveBundle.findAll(" FROM MoveBundle mb where moveEvent = $moveEvent.id ORDER BY mb.startTime ")

			// handle the view unpublished tasks checkbox
			if (params.containsKey('viewUnpublished')) {
				userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED, params.viewUnpublished == '1')
			}

			boolean viewUnpublished = securityService.viewUnpublished()

			def model = [project: project, projectLogo: projectLogo, moveEvent: moveEvent, moveEventsList: moveEventsList,
			             moveBundleList: moveBundleList, timeToUpdate: timeToUpdate ? timeToUpdate.DASHBOARD_REFRESH : 'never',
			             EventDashboardDialOverridePerm: securityService.hasPermission(Permission.EventDashboardDialOverride),
			             viewUnpublished: viewUnpublished ? '1' : '0']
			try {
				def taskSummary = dashboardService.getTaskSummaryModel(moveEvent.id, 6, viewUnpublished)
				if (taskSummary) {
					model.putAll(taskSummary)
				}
			} catch (e) {
				log.warn "Encountered error ($e.message) calling getTaskSummaryModel for $moveEvent\n${ExceptionUtil.stackTraceToString(e,60)}"
				flash.error = "Sorry but an unexpected error was encountered and not all data was able to be gathered for the dashboard."
			}

			return model
		}
	}

	@HasPermission(Permission.DashboardMenuView)
	def taskSummary() {
		try {
			render(template: 'taskSummary',
			       model: dashboardService.getTaskSummaryModel(params.id, 6, securityService.viewUnpublished()))
		} catch (e) {
			render e.message
		}
	}

	@HasPermission(Permission.DashboardMenuView)
	def userPortal() {
		Project project = controllerService.getProjectForPage(this, 'to view User Dashboard')
		if (!project) return

		def projects = [project]
		def userProjects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE)
		if (userProjects) {
			projects = (userProjects + project).unique()
		}
		[projects: projects, projectInstance: project, loggedInPerson: securityService.userLoginPerson]
	}

	/**
	 * Loads the event template for User Dashboard.
	 * @param : project id of selected project
	 * @render : events template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveEventsList() {
		def result = userService.getEventDetails(getProjectOrAll()).values().collect { value -> [
				eventId: value.moveEvent.id, projectName: value.moveEvent.project.name,
				name: value.moveEvent.name, startDate: moveEventService.getEventTimes(value.moveEvent.id).start,
				days: value.daysToGo + ' days', teams: value.teams]
		}
		render result as JSON
	}

	/**
	 * Loads the event template for User Dashboard.
	 * @param : project id of selected project
	 * @render : events template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveEvents() {
		Project project = getProjectOrAll()
		render (template: 'events',
		        model: [upcomingEvents: userService.getEventDetails(project), project: project,
		                staffRoles: taskService.getTeamRolesForTasks()])
	}

	/**
	 * Loads the event news template for User Dashboard.
	 * @param : project id of selected project
	 * @render : events template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveEventsNewsList() {
		// TODO : JPM 6/2016 : SECURITY : Users should only have the projects that they can access
		Map map = [project: null, newsList: []]

		Project project
		Long projectId = NumberUtil.toPositiveLong(params.project, -1)
		if (projectId > 0) {
			project = Project.get(projectId)
			if (project) {
				// TODO : JPM 6/2016 : SECURITY : Should be checking if user has access to project
				userPreferenceService.setCurrentProjectId(projectId)
			} else {
				projectId = -1
			}
		} else if (projectId == 0L) {
			project = Project.ALL
		}
		if (projectId == -1L) {
			project = securityService.userCurrentProject
		}

		List result = []
		if (project) {
			userService.getEventNews(project).each { news ->
				result << [eventId: news.moveEvent.id, projectName: news.moveEvent.project.name,
				           date: news.dateCreated, event: news.moveEvent.name, news: news.message]
			}
		}

		render result as JSON
	}

	/**
	 * Load the event news for User Dashboard.
	 * @param : project id of selected project
	 * @render : eventNews template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveEventsNews() {
		Project project = getProjectOrAll(false)
		render(template: 'eventNews', model: [newsList: userService.getEventNews(project), project: project])
	}

	/**
	 * Load the tasks Template for User Dashboard.
	 * @param : project id of selected project
	 * @render : tasks template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveTaskSummaryList() {
		Project project = getProjectOrAll(false)

		def result = []
		def taskSummary = userService.getTaskSummary(project)
		DateFormat formatter = TimeUtil.createFormatter("MM/dd kk:mm")

		taskSummary.taskList.each { task ->
			task.item.estFinish?.clearTime()
			result.add([
					projectName: task.projectName,
					taskId: task.item.id,
					task: ((task.item.taskNumber)? task.item.taskNumber + " - " : "") + task.item.comment,
					css: task.css,
					overDue: (task.item.dueDate && task.item.dueDate < TimeUtil.nowGMT()? 'task_overdue' : ''),
					assetClass: task.item.assetClass.toString(),
					assetId: task.item.assetId,
					related: task.item.assetName,
					dueEstFinish: TimeUtil.formatDateTimeWithTZ(TimeUtil.defaultTimeZone,
							(task.item.estFinish != null? task.item.estFinish : new Date()), formatter),
					status: task.item.status
			])
		}
		
		def totalDuration = TimeUtil.ago(taskSummary.totalDuration, TimeUtil.SHORT)
		def dueTaskCount = taskSummary.dueTaskCount

		def summaryDetail = 'No active tasks were found.'
		if (taskSummary.taskList.size() > 0) {
			summaryDetail = "${taskSummary.taskList.size()} assigned tasks (${dueTaskCount} ${(dueTaskCount == 1) ? ('is') : ('are')} overdue)"
		}

		Map data = [taskList: result, summaryDetail: summaryDetail]
		render data as JSON
	}

	/**
	 * Load the tasks for User Dashboard.
	 * @param : project id of selected project
	 * @render : tasks template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveTaskSummary() {
		Project project = getProjectOrAll(false)
		def taskSummary = userService.getTaskSummary(project)
		render (template: 'tasks',
		        model: [taskList: taskSummary.taskList, totalDuration: taskSummary.totalDuration,
		                project: project, dueTaskCount: taskSummary.dueTaskCount, personId: taskSummary.personId])
	}

	/**
	 * Load Application template for User Dashboard.
	 * @param : project id of selected project
	 * @render : application template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveApplicationsList() {
		// [appList: List<Application>, relationList: Map<Long, String>]
		Map<String, Object> appSummary = userService.getApplications(getProjectOrAll())

		renderAsJson appSummary.appList.collect { Application app -> [
				projectName: app.project.name,
				name: app.assetName,
				appId: app.id,
				assetClass: app.assetClass.toString(),
				planStatus: app.planStatus,
				moveBundle: app.moveBundle.name,
				relation: appSummary.relationList[app.id]
		] }
	}

	/**
	 * Load the apps for User Dashboard.
	 * @param : project id of selected project
	 * @render : application template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveApplications() {
		Project project = getProjectOrAll(false)
		def appSummary = userService.getApplications(project)
		render(template: 'application',
		       model: [appList: appSummary.appList, relationList: appSummary.relationList, project:project])
	}

	/**
	 * Load the active people Template for User Dashboard.
	 * @param : project id of selected project
	 * @render : activePeople template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveActivePeopleList() {
		renderAsJson userService.getActivePeople(getProjectOrAll()).collect { login ->
			[personId: login.personId, projectName: login.projectName,
			 personName: login.personName, lastActivity: login.lastActivity]
		}
	}

	/**
	 * Load the active people for User Dashboard.
	 * @param : project id of selected project
	 * @render : activePeople template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveActivePeople() {
		Project project = getProjectOrAll(false)
		render (template: 'activePeople', model: [recentLogin:userService.getActivePeople(project), project:project ])
	}

	/**
	 * Loads the related Entities User Dashboard that would be hidden.
	 * @param : project id of selected project
	 * @render : activePeople template
	 */
	@HasPermission(Permission.DashboardMenuView)
	def retrieveRelatedEntities() {
		def entities = [:]
		def moveBundleList = []
		render(template: 'relatedEntities',
		       model: [servers: entities.servers, applications: entities.applications, dbs: entities.dbs,
		               files: entities.files, dependencyType: entities.dependencyType,
		               dependencyStatus: entities.dependencyStatus, assetDependency: new AssetDependency(),
		               staffRoles:taskService.getTeamRolesForTasks(), moveBundleList:moveBundleList])
	}

	private Project getProjectOrAll(boolean updatePreference = true) {
		if (params.project == '0') {
			return Project.ALL
		}

		Project project = Project.get(params.project)
		if (updatePreference) {
			userPreferenceService.setCurrentProjectId(project.id)
		}
		project
	}
}
