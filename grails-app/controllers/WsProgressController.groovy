import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.ProgressService

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
	def retrieveStatus() {
		try {
			renderSuccessJson(progressService.get(params.id))
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * Gets the status of the progress of a async task
	 */
	def retrieveData() {
		try {
			renderSuccessJson(progressService.getData(params.id, params.dataKey))
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * Returns the list of pending progresses
	 */
	def list() {
		try {
			renderSuccessJson(progressService.list())
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * Returns the list of pending progresses
	 */
	def demo() {
		try {
			renderSuccessJson(progressService.demo())
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * Returns the list of pending progresses
	 */
	def demoFailed() {
		try {
			renderSuccessJson(progressService.demoFailed())
		}
		catch (e) {
			handleException e, logger
		}
	}
}
