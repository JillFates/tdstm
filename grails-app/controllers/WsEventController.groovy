import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.EventService
import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.service.MoveEventService

/**
 * Handles WS calls of the EventService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsEventController')
class WsEventController implements ControllerMethods {

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
}
