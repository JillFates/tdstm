import com.tds.asset.AssetComment
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.AssetCommentSaveUpdateCommand
import net.transitionmanager.command.task.TaskGenerationCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.CommentService
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.CredentialService
import net.transitionmanager.service.QzSignService
import net.transitionmanager.service.TaskActionService
import net.transitionmanager.service.TaskService
/**
 * Handles WS calls of the TaskService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j
class WsTaskController implements ControllerMethods {

	ApiActionService apiActionService
	CommentService commentService
	CoreService coreService
	CredentialService credentialService
	QzSignService qzSignService
	TaskActionService taskActionService
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
	 * Executes a local api action tied to a task and returns new task status if applies.
	 * @param id - task id where api action is linked to
	 * @return
	 */
	@HasPermission(Permission.ActionInvoke)
	def invokeLocalAction(Long id) {

		AssetComment task = taskService.invokeLocalAction(getProjectForWs(), id, currentPerson())

		Map results = [
			assetComment: task,
			status: task.status,
			statusCss: taskService.getCssClassForStatus(task.status)
		]

		renderAsJson(results)
	}

	/**
	 * Fetch an api action execution context and return it for remote invocation. If for what ever reason
	 * the action can not be invoked then an exception will be thrown with the cause.
	 * @param id - task id where api action is linked to
	 * @param publicKey - the public key to encrypt the pertainent data
	 * @return JSON object containing an ActionRequest context object
	 */
	@HasPermission( [ Permission.ActionInvoke, Permission.ActionRemoteAllowed ])
	def recordRemoteActionStarted(Long id, String publicKey) {
		Map actionRequest = taskService.recordRemoteActionStarted(id,  currentPerson(), publicKey)
		renderAsJson([actionRequest: actionRequest])
	}

	/**
	 * Reset an action tied to a task and returns new task status if applies.
	 * @param id - task id
	 * @return
	 */
	@HasPermission(Permission.ActionReset)
	def resetAction(Long id) {
		AssetComment task = fetchDomain(AssetComment, params)
		String status = taskService.resetAction(id, currentPerson())
		renderAsJson([assetComment: task, status: status, statusCss: taskService.getCssClassForStatus(task.status)])
	}

	/**
	 * @See TM-13937
	 * Adds a note to a task and returns the list of notes associated to that Task.
	 * @param id  The task id
	 * @param note  The note text
	 * @return  The list of notes associated to the Task.
	 */
	@HasPermission(Permission.CommentEdit)
	def addNote() {
		AssetComment assetComment = fetchDomain(AssetComment, params)
		if (assetComment) {
			Map requestParams = request.JSON
			Person whom = currentPerson()
			Boolean status = taskService.addNote(assetComment, whom, requestParams.note, 0)
			if (!status) {
				def errorMsg = " There was a problem when creating Note for Task whithz id - $params.id "
				log.error "addNote: $errorMsg"
				renderErrorJson([errorMsg])
			}

			// Get a list of the Notes associated with the task/comment
			List notes = []
			List notesList = taskService.getNotes(assetComment)
			for (note in notesList) {
				notes << [
						TimeUtil.formatDateTime(note.dateCreated, TimeUtil.FORMAT_DATE_TIME_3),
						note.createdBy?.toString(),
						note.note,
						note.createdBy?.id]
			}
			renderSuccessJson(notes)
		} else {
			def errorMsg = " Task Not Found : Was unable to find the Task for the specified id - $params.id "
			log.error "addNote: $errorMsg"
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
