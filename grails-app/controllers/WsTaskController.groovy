import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UnauthorizedException

/**
 * Handles WS calls of the TaskService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j
@Slf4j(value='logger', category='grails.app.controllers.WsTaskController')
class WsTaskController implements ControllerMethods {

	TaskService taskService

	/**
	 * Publishes a TaskBatch that has been generated before
	 */
	def publish() {
		try {
			renderSuccessJson(tasksUpdated: taskService.publish(params.id))
		}
		catch (e) {
			preHandleException e
		}
	}

	/**
	 * Unpublishes a TaskBatch that has been generated before
	 */
	def unpublish() {
		try {
			renderSuccessJson(tasksUpdated: taskService.unpublish(params.id))
		}
		catch (e) {
			preHandleException e
		}
	}

	/**
	 * Deletes a TaskBatch.
	 */
	def deleteBatch() {
		try {
			taskService.deleteBatch(params.id)
			renderSuccessJson()
		}
		catch (e) {
			preHandleException e
		}
	}

	/**
	 * Generates a set of tasks based on a recipe
	 */
	def generateTasks() {
		try {
			def result = taskService.initiateCreateTasksWithRecipe(params.contextId, params.recipeId,
					params.deletePrevious == 'true', params.useWIP == 'true', params.autoPublish == 'true')
			renderSuccessJson(jobId: result.jobId)
		}
		catch (e) {
			preHandleException e, true
		}
	}

	/**
	 * Used to lookup a TaskBatch by the Context and Recipe regardless of the recipe version
	 * @param contextId - the record id number of the context that the TaskBatch was generated for
	 * @param recipeId - the record id of the recipe used to generate the TaskBatch
	 * @return A taskBatch object if found or null
	 */
	def findTaskBatchByRecipeAndContext() {
		try {
			def result = taskService.findTaskBatchByRecipeAndContext(params.recipeId, params.contextId, params.logs)
			renderSuccessJson(taskBatch: result)
		}
		catch (e) {
			preHandleException e
		}
	}

	/**
	 * List the TaskBatch using the parameters passed in the request
	 */
	def listTaskBatches() {
		try {
			renderSuccessJson(list: taskService.listTaskBatches(params.recipeId, params.limitDays))
		}
		catch (e) {
			preHandleException e
		}
	}

	/**
	 * Gets a TaskBatch based on a id
	 */
	def retrieveTaskBatch() {
		try {
			renderSuccessJson(taskBatch: taskService.getTaskBatch(params.id))
		}
		catch (e) {
			preHandleException e
		}
	}

	def taskReset() {
		try {
			taskService.resetTasksOfTaskBatch(params.id)
			renderSuccessJson()
		}
		catch (e) {
			preHandleException e
		}
	}

	def retrieveTasksOfTaskBatch() {
		try {
			renderSuccessJson(tasks: taskService.getTasksOfBatch(params.id))
		}
		catch (e) {
			preHandleException e
		}
	}

	private void preHandleException(Exception e, boolean includeException = false) {
		if (e instanceof UnauthorizedException) {
			if (includeException) {
				ServiceResults.forbidden(response, e)
			}
			else {
				ServiceResults.forbidden(response)
			}
		}
		else if (e instanceof IllegalArgumentException) {
			if (includeException) {
				ServiceResults.internalError(response, logger, e)
			}
			else {
				ServiceResults.forbidden(response)
			}
		}
		else if (e instanceof EmptyResultException) {
			if (includeException) {
				ServiceResults.internalError(response, logger, e)
			}
			else {
				ServiceResults.methodFailure(response)
			}
		}
		else if (e instanceof ValidationException) {
			render(ServiceResults.errorsInValidation(e.errors) as JSON)
		}
		else {
			ServiceResults.internalError(response, logger, e)
		}
	}
}
