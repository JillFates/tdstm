package api.v1_0

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.task.ActionCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.APIProducerService

/**
 * A controller for handling API remote action's status updates, and invoking reaction scripts.
 */
@Secured('isAuthenticated()')
class TaskController implements ControllerMethods {
	static namespace = 'v1'


	static allowedMethods = [
		action: 'POST',
		delete: 'DELETE',
		update: ['POST', 'PUT']
	]

	APIProducerService APIProducerService

	/**
	 * Updates the remote APIs status and runs reaction script depending on the status.
	 *
	 * @param id The task id.
	 * @see net.transitionmanager.command.task.ActionCommand
	 *
	 * @return a success JSON if the status was updated.
	 */
	@HasPermission(Permission.ActionInvoke)
	@HasPermission(Permission.ActionRemoteAllowed)
	def action(Long id) {
		ActionCommand action = populateCommandObject(ActionCommand)
		validateCommandObject(action)
		Person currentPerson = currentPerson()
		Project currentProject = projectForWs

		APIProducerService.updateRemoteActionStatus(action, id, currentPerson, currentProject)

		renderSuccessJson()
	}
}
