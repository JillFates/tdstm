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
	@HasPermission(Permission.ProgressView)
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
	@HasPermission(Permission.ProgressList)
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
	@HasPermission(Permission.ProgressView)
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
	@HasPermission(Permission.ProgressView)
	def demoFailed() {
		try {
			renderSuccessJson(progressService.demoFailed())
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * Interrupt a Quartz Job given its ID.
	 *
	 * @param jobKey
	 * @return
	 */
	@HasPermission(Permission.DataTransferBatchProcess)
	def interruptJob(String jobKey){
		progressService.interruptJob(jobKey)
		renderSuccessJson([interrupted: true])
	}
}
