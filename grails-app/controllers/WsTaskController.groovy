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
		renderSuccessJson(tasksUpdated: taskService.publish(params.id))
	}

	/**
	 * Unpublishes a TaskBatch that has been generated before
	 */
	@HasPermission(Permission.TaskPublish)
	def unpublish() {
		renderSuccessJson(tasksUpdated: taskService.unpublish(params.id))

	}

	/**
	 * Deletes a TaskBatch.
	 */
	@HasPermission(Permission.TaskBatchDelete)
	def deleteBatch() {
		taskService.deleteBatch(params.id)
		renderSuccessJson()
	}

	/**
	 * Generates a set of tasks based on a recipe
	 */
	@HasPermission(Permission.RecipeGenerateTasks)
	def generateTasks() {
		def result = taskService.initiateCreateTasksWithRecipe(params.contextId, params.recipeId,
				params.deletePrevious == 'true', params.useWIP == 'true', params.autoPublish == 'true')
		renderSuccessJson(jobId: result.jobId)

	}

	/**
	 * Used to lookup a TaskBatch by the Context and Recipe regardless of the recipe version
	 * @param contextId - the record id number of the context that the TaskBatch was generated for
	 * @param recipeId - the record id of the recipe used to generate the TaskBatch
	 * @return A taskBatch object if found or null
	 */
	@HasPermission(Permission.TaskBatchView)
	def findTaskBatchByRecipeAndContext() {
		def result = taskService.findTaskBatchByRecipeAndContext(params.recipeId, params.contextId, params.logs)
		renderSuccessJson(taskBatch: result)
	}

	/**
	 * List the TaskBatch using the parameters passed in the request
	 */
	@HasPermission(Permission.TaskBatchView)
	def listTaskBatches() {
		renderSuccessJson(list: taskService.listTaskBatches(params.recipeId, params.limitDays))
	}

	/**
	 * Gets a TaskBatch based on a id
	 */
	@HasPermission(Permission.TaskBatchView)
	def retrieveTaskBatch() {
		renderSuccessJson(taskBatch: taskService.getTaskBatch(params.id))
	}

	@HasPermission(Permission.RecipeGenerateTasks)
	def taskReset() {
		taskService.resetTasksOfTaskBatch(params.id)
		renderSuccessJson()
	}

	@HasPermission(Permission.TaskBatchView)
	def retrieveTasksOfTaskBatch() {
		renderSuccessJson(tasks: taskService.getTasksOfBatch(params.id))
	}

	/**
	 * Sign a Provided Message using the QZCertificate
	 * @see https://qz.io/wiki/2.0-signing-messages
	 */
	@HasPermission(Permission.TaskSignMessage)
	def qzSignMessage() {
		String message = params.request
		String signatureBase64 = qzSignService.sign(message)
		renderSuccessJson(signed_message: signatureBase64)
	}

	/**
	 * Return the default values of Create Tasks Properties
	 */
	@HasPermission(Permission.TaskCreate)
	def taskCreateDefaults() {
		renderSuccessJson([preferences: commentService.getTaskCreateDefaults()])
	}

	/**
	 * Executes an action tied to a task and returns new task status if applies.
	 * @param id - task id
	 * @return
	 */
	@HasPermission(Permission.ActionInvoke)
	def invokeAction() {
		AssetComment assetComment = fetchDomain(AssetComment, params)
		Person whom = securityService.loadCurrentPerson()
		String status = taskService.invokeAction(assetComment, whom)
		renderAsJson([assetComment: assetComment, status: status, statusCss: taskService.getCssClassForStatus(assetComment.status)])
	}

	/**
	 * Reset an action tied to a task and returns new task status if applies.
	 * @param id - task id
	 * @return
	 */
	@HasPermission(Permission.ActionReset)
	def resetAction() {
		AssetComment assetComment = fetchDomain(AssetComment, params)
		if (assetComment) {
			Person whom = securityService.loadCurrentPerson()
			String status = taskService.resetAction(assetComment, whom)
			renderAsJson([assetComment: assetComment, status: status, statusCss: taskService.getCssClassForStatus(assetComment.status)])
		} else {
			def errorMsg = " Task Not Found : Was unable to find the Task for the specified id - $params.id "
			log.error "resetAction: $errorMsg"
			renderErrorJson([errorMsg])
		}
	}
}
