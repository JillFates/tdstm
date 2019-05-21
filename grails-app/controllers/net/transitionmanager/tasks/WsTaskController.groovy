package net.transitionmanager.tasks

import net.transitionmanager.task.AssetComment
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.task.TaskGenerationCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.security.Permission
import net.transitionmanager.action.ApiActionService
import net.transitionmanager.asset.CommentService
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.security.CredentialService
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.task.QzSignService
import net.transitionmanager.task.TaskService

/**
 * Handles WS calls of the TaskService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j
class WsTaskController implements ControllerMethods {

	CommentService commentService
	QzSignService qzSignService
	TaskService taskService
	ApiActionService apiActionService
	CredentialService credentialService

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
	def invokeLocalAction() {
		AssetComment assetComment = fetchDomain(AssetComment, params)
		Person whom = securityService.loadCurrentPerson()
		String status = taskService.invokeAction(assetComment, whom, false)
		renderAsJson([assetComment: assetComment, status: status, statusCss: taskService.getCssClassForStatus(assetComment.status)])
	}

	/**
	 * Fetch an api action execution context and return it for remote invocation
	 * @param id - task id where api action is linked to
	 * @return
	 */
	@HasPermission(Permission.ActionInvoke)
	def invokeRemoteAction() {
		Project project = getProjectForWs()
		AssetComment assetComment = fetchDomain(AssetComment, params)
		ActionRequest actionRequest = apiActionService.createActionRequest(assetComment.apiAction)

		if (! actionRequest.options.apiAction.isRemote) {
			throw new InvalidRequestException('Local actions was incorrectly invoke as Remote')
		}
		Map<String,?> actionRequestMap = actionRequest.toMap()

		// check if api action has credentials so to include credentials password unencrypted
		if (actionRequest.options.hasProperty('credentials') && actionRequestMap.options.credentials) {
			// Need to create new map because credentials map originally is immutable
			Map<String, ?> credentials = new HashMap<>(actionRequestMap.options.credentials)
			credentials.password = credentialService.decryptPassword(assetComment.apiAction.credential)
			actionRequestMap.options.credentials = credentials
		}

		renderAsJson([actionRequest: actionRequest.toMap()])
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
			Person whom = securityService.loadCurrentPerson()
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
	 * Used in Task Manager action bar to change estTime.
	 * @param : day : 1, 2 or 7 days.
	 * @param : commentId.
	 * @return : retMap.
	 */
	@HasPermission(Permission.TaskManagerView)
	def changeEstTime() {
		Long commentId = params.id.toLong()
		Map requestParams = request.JSON
		Integer days = requestParams.days.toInteger()
		Map<String, String> retMap = [etext: '', estStart: '', estFinish: '']
		try {
			AssetComment comment = taskService.changeEstTime(commentId, days)
			retMap['estStart'] = TimeUtil.formatDateTime(comment?.estStart)
			retMap['estFinish'] = TimeUtil.formatDateTime(comment?.estFinish)
		} catch (EmptyResultException | InvalidParamException e) {
			retMap['etext'] = e.message
		}
		render retMap as JSON
	}

	@HasPermission(Permission.TaskEdit)
	def updateStatus() {
		Long commentId = params.id.toLong()
		Map requestParams = request.JSON
		def status = requestParams.status
		AssetComment comment = taskService.findById(commentId)
		if(AssetCommentStatus.list.contains(status)) {
			taskService.setTaskStatus(comment,status)
		}
	}
}