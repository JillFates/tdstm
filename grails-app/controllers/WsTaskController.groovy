import com.tds.asset.AssetComment
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.ServiceResults
import net.transitionmanager.domain.Person
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CommentService
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.QzSignService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UnauthorizedException
import com.tdsops.common.security.spring.HasPermission

/**
 * Handles WS calls of the TaskService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j
class WsTaskController implements ControllerMethods {

	TaskService taskService
	CommentService commentService
	QzSignService qzSignService
	SecurityService securityService

	/**
	 * Publishes a TaskBatch that has been generated before
	 */
	@HasPermission(Permission.TaskPublish)
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
	@HasPermission(Permission.TaskPublish)
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
	@HasPermission(Permission.TaskBatchDelete)
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
	@HasPermission(Permission.RecipeGenerateTasks)
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
	@HasPermission(Permission.TaskBatchView)
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
	@HasPermission(Permission.TaskBatchView)
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
	@HasPermission(Permission.TaskBatchView)
	def retrieveTaskBatch() {
		try {
			renderSuccessJson(taskBatch: taskService.getTaskBatch(params.id))
		}
		catch (e) {
			preHandleException e
		}
	}

	@HasPermission(Permission.RecipeGenerateTasks)
	def taskReset() {
		try {
			taskService.resetTasksOfTaskBatch(params.id)
			renderSuccessJson()
		}
		catch (e) {
			preHandleException e
		}
	}

	@HasPermission(Permission.TaskBatchView)
	def retrieveTasksOfTaskBatch() {
		try {
			renderSuccessJson(tasks: taskService.getTasksOfBatch(params.id))
		}
		catch (e) {
			preHandleException e
		}
	}

	/**
	 * Sign a Provided Message using the QZCertificate
	 * @see https://qz.io/wiki/2.0-signing-messages
	 */
	@HasPermission(Permission.TaskSignMessage)
	def qzSignMessage() {
		try {
			String message = params.request

			String signatureBase64 = qzSignService.sign(message)

			renderSuccessJson(signed_message: signatureBase64)
		} catch (e) {
			preHandleException e
		}
	}

	@HasPermission(Permission.TaskCreate)
	/**
	 * Return the default values of Create Tasks Properties
	 */
	def taskCreateDefaults() {
		renderSuccessJson([preferences: commentService.getTaskCreateDefaults()])
	}

	/**
	 * Executes an action tied to a task and returns new task status if applies.
	 * @param id - task id
	 * @return
	 */
	def invokeAction() {
		AssetComment assetComment = AssetComment.get(params.id)
		if (assetComment) {
			Person whom = securityService.loadCurrentPerson()
			String status = taskService.invokeAction(assetComment, whom)
			renderSuccessJson([status: status])
		} else {
			def errorMsg = " Task Not Found : Was unable to find the Task for the specified id - $params.id "
			log.error "invokeAction: $errorMsg"
			renderErrorJson([errorMsg])
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
				ServiceResults.internalError(response, log, e)
			}
			else {
				ServiceResults.forbidden(response)
			}
		}
		else if (e instanceof EmptyResultException) {
			if (includeException) {
				ServiceResults.respondWithError(response, e.message)
			}
			else {
				ServiceResults.methodFailure(response)
			}
		}
		else if (e instanceof ValidationException) {
			render(ServiceResults.errorsInValidation(e.errors) as JSON)
		}
		else {
			ServiceResults.internalError(response, log, e)
		}
	}
}
