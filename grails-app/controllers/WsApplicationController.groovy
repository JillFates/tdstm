import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApplicationService
import com.tdsops.common.security.spring.HasPermission

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
	@HasPermission(Permission.AssetView)
	def listInBundle() {
		try {
			renderSuccessJson(list: applicationService.listInBundle(params.id))
		}

		catch (e) {
			handleException e, logger
		}
	}
}
