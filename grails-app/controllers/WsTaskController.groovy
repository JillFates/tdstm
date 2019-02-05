import com.tds.asset.AssetComment
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.AssetCommentSaveUpdateCommand
import net.transitionmanager.command.task.TaskGenerationCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CommentService
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.ProjectService
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

	CommentService commentService
	ProjectService projectService
	QzSignService qzSignService
	TaskService taskService

	/**
	 * Publishes a TaskBatch that has been generated before
	 */
	@HasPermission(Permission.TaskPublish)
	def publish(Long id) {
		Project project = securityService.userCurrentProject
		renderSuccessJson(tasksUpdated: taskService.publish(id, project))
	}

	/**
	 * Unpublishes a TaskBatch that has been generated before
	 */
	@HasPermission(Permission.TaskPublish)
	def unpublish(Long id) {
		Project project = securityService.userCurrentProject
		renderSuccessJson(tasksUpdated: taskService.unpublish(id, project))
	}

	/**
	 * Deletes a TaskBatch.
	 */
	@HasPermission(Permission.TaskBatchDelete)
	def deleteBatch(Long id) {
		Project project = securityService.userCurrentProject
		taskService.deleteBatch(id, project)
		renderSuccessJson()
	}

	/**
	 * Generates a set of tasks based on a recipe
	 */
	@HasPermission(Permission.RecipeGenerateTasks)
	def generateTasks() {
		TaskGenerationCommand context = populateCommandObject(TaskGenerationCommand)
		validateCommandObject(context)
		Project project = securityService.userCurrentProject

		def result = taskService.initiateCreateTasksWithRecipe(context, project)
		renderSuccessJson(jobId: result.jobId)
	}

	/**
	 * Used to lookup a TaskBatch by the Context and Recipe regardless of the recipe version
	 * @param eventId - the record id number of the event that the TaskBatch was generated for
	 * @param recipeId - the record id of the recipe used to generate the TaskBatch
	 * @return A taskBatch object if found or null
	 */
	@HasPermission(Permission.TaskBatchView)
	def findTaskBatchByRecipeAndContext(Long recipeId, Long eventId) {
		Project project = securityService.userCurrentProject
		def result = taskService.findTaskBatchByRecipeAndContext(recipeId, eventId, project, params.logs)
		renderSuccessJson(taskBatch: result)
	}

	/**
	 * List the TaskBatch using the parameters passed in the request
	 */
	@HasPermission(Permission.TaskBatchView)
	def listTaskBatches(Long recipeId) {
		Project project = securityService.userCurrentProject
		renderSuccessJson(list: taskService.listTaskBatches(recipeId, params.limitDays, project))
	}

	/**
	 * Gets a TaskBatch based on a id
	 */
	@HasPermission(Permission.TaskBatchView)
	def retrieveTaskBatch(Long id) {
		Project project = securityService.userCurrentProject
		renderSuccessJson(taskBatch: taskService.getTaskBatch(id, project))
	}

	@HasPermission(Permission.RecipeGenerateTasks)
	def taskReset(Long id) {
		Project project = securityService.userCurrentProject
		taskService.resetTasksOfTaskBatch(id, project)
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
		renderSuccessJson()

	}

	/**
	 * Update an AssetComment
	 */
	@HasPermission(Permission.CommentEdit)
	def updateComment(Long id) {
		// Update the comment.
		saveOrUpdateComment()
		renderSuccessJson()
	}

	/**
	 * Update an AssetComment
	 */
	@HasPermission(Permission.CommentCreate)
	def saveComment() {
		// Save the comment.
		saveOrUpdateComment()
		renderSuccessJson()

	}

	/**
	 * Endpoint that returns a list of tasks matching the filters provided. Some of available parameters are:
	 * - project (id)
	 * - event (id)
	 * - justMyTasks (1, Y)
	 * - justRemaining (1, Y)
	 * - justActionable (1, Y)
	 * - viewUnpublished (1, Y)
	 * @return a list with a Map representation of each task.
	 */
	@HasPermission(Permission.TaskView)
	def list() {
		/* This will contain a reference to, either the user's project, or the project specified as
		a parameter (given that they have access to it). */
		Project project
		// Check if the user passed a project id.
		Long projectId = params.long('project')
		if (projectId) {
			// Determine if the user has access to the specified project.
			if (projectService.hasAccessToProject(null, projectId)) {
				project = fetchDomain(Project, ['id': projectId])
			} else {
				throw new EmptyResultException('The user does not have access to the requested project.')
			}
		} else {
			// If no project was specified, use the user's current project.
			project = getProjectForWs()
		}

		// If the params map has an event, validate that it exists and belongs to the project.
		Long eventId = params.long('event')
		if (eventId) {
			// We don't need the reference, just to validate that it exists and fail if it doesn't.
			GormUtil.findInProject(project, MoveEvent, eventId, true)
		}

		Map results = commentService.filterTasks(project, params)
		List<Map> tasks = results.tasks.collect {AssetComment task ->
			task.toMap()
		}
		renderSuccessJson(tasks)

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