package api.v1_0

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.task.ActionCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.TaskActionService

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

	TaskActionService taskActionService

	/**
	 * Updates the remote APIs status for when the action starts.
	 *
	 * @param id The task id.
	 * @see net.transitionmanager.command.task.ActionCommand*
	 * @return a success JSON if the status was updated.
	 */
	@HasPermission(Permission.ActionInvoke)
	@HasPermission(Permission.ActionRemoteAllowed)
	def actionStarted(Long id) {
		ActionCommand action = populateCommandObject(ActionCommand)
		validateCommandObject(action)
		Person currentPerson = currentPerson()
		Project currentProject = projectForWs

		taskActionService.actionStarted(action, id, currentPerson, currentProject)

		renderSuccessJson()
	}

	/**
	 * Updates the remote APIs status for when the action had progressed.
	 *
	 * @param id The task id.
	 * @see net.transitionmanager.command.task.ActionCommand*
	 * @return a success JSON if the status was updated.
	 */
	@HasPermission(Permission.ActionInvoke)
	@HasPermission(Permission.ActionRemoteAllowed)
	def actionProgress(Long id) {
		ActionCommand action = populateCommandObject(ActionCommand)
		validateCommandObject(action)
		Person currentPerson = currentPerson()
		Project currentProject = projectForWs

		taskActionService.actionProgress(action, id, currentPerson, currentProject)

		renderSuccessJson()
	}

	/**
	 * Updates the remote APIs status for when the action is done.
	 *
	 * @param id The task id.
	 * @see net.transitionmanager.command.task.ActionCommand*
	 * @return a success JSON if the status was updated.
	 */
	@HasPermission(Permission.ActionInvoke)
	@HasPermission(Permission.ActionRemoteAllowed)
	def actionDone(Long id) {
		ActionCommand action = populateCommandObject(ActionCommand)
		validateCommandObject(action)
		Person currentPerson = currentPerson()
		Project currentProject = projectForWs

		taskActionService.actionDone(action, id, currentPerson, currentProject)

		renderSuccessJson()
	}

	/**
	 * Updates the remote APIs status for when there is an error.
	 *
	 * @param id The task id.
	 * @see net.transitionmanager.command.task.ActionCommand*
	 * @return a success JSON if the status was updated.
	 */
	@HasPermission(Permission.ActionInvoke)
	@HasPermission(Permission.ActionRemoteAllowed)
	def actionError(Long id) {
		ActionCommand action = populateCommandObject(ActionCommand)
		validateCommandObject(action)
		Person currentPerson = currentPerson()
		Project currentProject = projectForWs

		taskActionService.actionError(action, id, currentPerson, currentProject)

		renderSuccessJson()
	}
}
