import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ProgressService
import com.tdsops.common.security.spring.HasPermission

/**
 * Handles WS calls of the ProgressService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsProgressController')
class WsProgressController implements ControllerMethods {

	ProgressService progressService

	/**
	 * Gets the status of the progress of a async task
	 */
	@HasPermission(Permission.ProgressView)
	def retrieveStatus() {
		renderSuccessJson(progressService.get(params.id))
	}

	/**
	 * Gets the status of the progress of a async task
	 */
	@HasPermission(Permission.ProgressView)
	def retrieveData() {
		renderSuccessJson(progressService.getData(params.id, params.dataKey))
	}

	/**
	 * Returns the list of pending progresses
	 */
	@HasPermission(Permission.ProgressList)
	def list() {
		renderSuccessJson(progressService.list())
	}

	/**
	 * Returns the list of pending progresses
	 */
	@HasPermission(Permission.ProgressView)
	def demo() {
		renderSuccessJson(progressService.demo())
	}

	/**
	 * Returns the list of pending progresses
	 */
	@HasPermission(Permission.ProgressView)
	def demoFailed() {
		renderSuccessJson(progressService.demoFailed())
	}
}
