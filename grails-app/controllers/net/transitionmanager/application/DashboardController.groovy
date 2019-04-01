import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectLogo
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.DashboardService
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.UserService

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
			def moveBundleList = MoveBundle.findAll(" FROM MoveBundle mb where moveEvent = $moveEvent ORDER BY mb.startTime ")

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
}
