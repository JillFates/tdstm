import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.ApplicationService

/**
 * Handles WS calls of the ApplicationService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsApplicationController')
class WsApplicationController implements ControllerMethods {

	ApplicationService applicationService

	/**
	 * Provides a list all applications associate to the specified bundle or if id=0 then it returns all unassigned
	 * applications for the user's current project
	 */
	def listInBundle() {
		try {
			renderSuccessJson(list: applicationService.listInBundle(params.id))
		}

		catch (e) {
			handleException e, logger
		}
	}
}
