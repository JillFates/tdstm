import com.tds.asset.AssetComment
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetCommentCategory
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.AssetCommentSaveUpdateCommand
import net.transitionmanager.command.task.ContextCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CommentService
import net.transitionmanager.service.QzSignService
import net.transitionmanager.service.TaskService

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

	/**
	 * Publishes a TaskBatch that has been generated before
	 */
	@HasPermission(Permission.TaskPublish)
	def publish(Long id) {
		renderSuccessJson(tasksUpdated: taskService.publish(id))
	}

	/**
	 * Unpublishes a TaskBatch that has been generated before
	 */
	@HasPermission(Permission.TaskPublish)
	def unpublish(Long id) {
		renderSuccessJson(tasksUpdated: taskService.unpublish(id))
	}

	/**
	 * Deletes a TaskBatch.
	 */
	@HasPermission(Permission.TaskBatchDelete)
	def deleteBatch(Long id) {
		taskService.deleteBatch(id)
		renderSuccessJson()
	}

	/**
	 * Generates a set of tasks based on a recipe
	 */
	@HasPermission(Permission.RecipeGenerateTasks)
	def generateTasks() {
		ContextCommand context = populateCommandObject(ContextCommand)
		validateCommandObject(context)
		def result = taskService.initiateCreateTasksWithRecipe(context)
		renderSuccessJson(jobId: result.jobId)
	}

	/**
	 * Used to lookup a TaskBatch by the Context and Recipe regardless of the recipe version
	 * @param contextId - the record id number of the context that the TaskBatch was generated for
	 * @param recipeId - the record id of the recipe used to generate the TaskBatch
	 * @return A taskBatch object if found or null
	 */
	@HasPermission(Permission.TaskBatchView)
	def findTaskBatchByRecipeAndContext(Long recipeId, Long contextId) {
		def result = taskService.findTaskBatchByRecipeAndContext(recipeId, contextId, params.logs)
		renderSuccessJson(taskBatch: result)
	}

	/**
	 * List the TaskBatch using the parameters passed in the request
	 */
	@HasPermission(Permission.TaskBatchView)
	def listTaskBatches(Long recipeId) {
		renderSuccessJson(list: taskService.listTaskBatches(recipeId, params.limitDays))
	}

	/**
	 * Gets a TaskBatch based on a id
	 */
	@HasPermission(Permission.TaskBatchView)
	def retrieveTaskBatch(Long id) {
		renderSuccessJson(taskBatch: taskService.getTaskBatch(id))
	}

	@HasPermission(Permission.RecipeGenerateTasks)
	def taskReset(Long id) {
		taskService.resetTasksOfTaskBatch(id)
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

	/**
	 * Return a list with all the AssetCommentCategory values.
	 */
	@HasPermission(Permission.TaskBatchView)
	def assetCommentCategories() {
		renderSuccessJson(AssetCommentCategory.list)
	}

	/**
	 * Delete a comment given its id.
	 */
	@HasPermission(Permission.CommentDelete)
	def deleteComment(Long id) {
		// Retrieve the project for the current user.
		Project project = getProjectForWs()
		// Delete the comment
		commentService.deleteComment(project, id)
		renderSuccessJson("AssetComment deleted.")

	}

	/**
	 * Update an AssetComment
	 */
	@HasPermission(Permission.CommentEdit)
	def updateComment(Long id) {
		// Update the comment.
		saveOrUpdateComment()
		renderSuccessJson("AssetComment updated.")
	}

	/**
	 * Update an AssetComment
	 */
	@HasPermission(Permission.CommentCreate)
	def saveComment() {
		// Save the comment.
		saveOrUpdateComment()
		renderSuccessJson("AssetComment created.")

	}

	/**
	 * Create or Update an AssetComment
	 * @param id
	 * @param command
	 */
	private void saveOrUpdateComment() {
		// Retrieve the project for the user.
		Project project = getProjectForWs()
		// Populate the command object with the data coming from the request
		AssetCommentSaveUpdateCommand command = populateCommandObject(AssetCommentSaveUpdateCommand)
		// Save or update the comment
		commentService.saveOrUpdateAssetComment(project, command)
	}
}