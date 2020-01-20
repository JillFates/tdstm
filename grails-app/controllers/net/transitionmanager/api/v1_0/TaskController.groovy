package net.transitionmanager.api.v1_0

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.action.TaskActionService
import net.transitionmanager.command.task.ActionCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.person.Person
import net.transitionmanager.security.Permission
/**
 * A controller for handling API remote action's status updates, and invoking reaction scripts.
 */
@Secured('isAuthenticated()')
class TaskController implements ControllerMethods {
	static namespace = 'v1'


	static allowedMethods = [
		actionStarted: 'POST',
		actionProgress: 'POST',
		actionDone: 'POST',
		actionError: 'POST',
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
	def actionStarted(Long id) {
		ActionCommand action = populateCommandObject(ActionCommand)
		validateCommandObject(action)
		Person currentPerson = currentPerson()

		taskActionService.actionStarted(action, id, currentPerson)

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
	def actionProgress(Long id) {
		ActionCommand action = populateCommandObject(ActionCommand)
		Person currentPerson = currentPerson()

		taskActionService.actionProgress(action, id, currentPerson)

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
	def actionDone(Long id) {
		ActionCommand action = populateCommandObject(ActionCommand)
		Person currentPerson = currentPerson()

		taskActionService.actionDone(action, id, currentPerson)

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
	def actionError(Long id) {
		ActionCommand action = populateCommandObject(ActionCommand)
		Person currentPerson = currentPerson()

		taskActionService.actionError(action, id, currentPerson)

		renderSuccessJson()
	}
}
