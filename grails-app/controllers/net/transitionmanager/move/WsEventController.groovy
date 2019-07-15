package net.transitionmanager.move

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.reporting.DashboardService
import net.transitionmanager.security.Permission
import net.transitionmanager.project.EventService
import net.transitionmanager.project.MoveEventService
/**
 * Handles WS calls of the EventService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
class WsEventController implements ControllerMethods {

	DashboardService dashboardService
	EventService eventService
	MoveEventService moveEventService

	@HasPermission(Permission.EventView)
	def listEventsAndBundles() {
		renderSuccessJson(list: eventService.listEventsAndBundles())
	}

	/**
	 * All bundles associated to a specified move event or if id=0 then unassigned bundles
	 * for the user's current project
	 */
	@HasPermission(Permission.BundleView)
	def listBundles(Long id, Boolean useForPlanning) {
		renderSuccessJson(list: eventService.listBundles(id, useForPlanning))
	}

	/**
	 * List all available Move Events for the user's current project.
	 */
	@HasPermission(Permission.EventView)
	def listEvents() {
		Project project = getProjectForWs()
		List<MoveEvent> events = moveEventService.listMoveEvents(project)
		List<Map> eventsMap = events.collect { event ->
			GormUtil.domainObjectToMap(event, MoveEvent.BASIC_EVENT_FIELDS, null, false)
		}
		renderSuccessJson(eventsMap)
	}

	@HasPermission(Permission.DashboardMenuView)
	def getEventDashboardModel() {
		Long moveEventId = NumberUtil.toPositiveLong(getParamOrPreference('moveEvent', UserPreferenceEnum.MOVE_EVENT))
		if (!moveEventId) {
			throw new InvalidParamException('A valid Move Event ID is needed.')
		}

		Project project = getProjectForWs()
		MoveEvent moveEvent = GormUtil.findInProject(project, MoveEvent, moveEventId, true)

		// handle the view unpublished tasks checkbox
		if (params.containsKey('viewUnpublished')) {
			userPreferenceService.setPreference(UserPreferenceEnum.VIEW_UNPUBLISHED, params.viewUnpublished == '1')
		}

		boolean viewUnpublished = securityService.viewUnpublished()

		// Save the user's preference for the current move event
		userPreferenceService.setMoveEventId(moveEvent.id)

		renderSuccessJson([model: dashboardService.getEventDashboardModel(project, moveEvent, viewUnpublished)])
	}
}
