import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.EventService
import com.tdsops.common.security.spring.HasPermission

/**
 * Handles WS calls of the EventService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsEventController')
class WsEventController implements ControllerMethods {

	EventService eventService

	@HasPermission('MoveEventView')
	def listEventsAndBundles() {
		try {
			renderSuccessJson(list: eventService.listEventsAndBundles())
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * All bundles associated to a specified move event or if id=0 then unassigned bundles
	 * for the user's current project
	 */
	@HasPermission('BundleView')
	def listBundles() {
		try {
			renderSuccessJson(list: eventService.listBundles(params.id, params.useForPlanning))
		}
		catch (e) {
			handleException e, logger
		}
	}
}
