import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ProgressService
/**
 * Handles WS calls of the ProgressService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
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
