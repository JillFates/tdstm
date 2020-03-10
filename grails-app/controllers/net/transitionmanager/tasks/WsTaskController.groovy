package net.transitionmanager.tasks

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.task.Task
import org.grails.web.json.JSONArray
import net.transitionmanager.action.ApiActionService
import net.transitionmanager.action.TaskActionService
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.asset.CommentService
import net.transitionmanager.command.task.ListTaskCommand
import net.transitionmanager.command.task.RecordRemoteActionStartedCommand
import net.transitionmanager.command.task.TaskGenerationCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.person.Person
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.Project
import net.transitionmanager.security.CredentialService
import net.transitionmanager.security.Permission
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.QzSignService
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.TaskService
/**
 * Handles WS calls of the TaskService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j
class WsTaskController implements ControllerMethods, PaginationMethods {

	CommentService commentService
	QzSignService qzSignService
	TaskService taskService
	TaskActionService taskActionService
	ApiActionService apiActionService
	CredentialService credentialService
    UserPreferenceService userPreferenceService
    AssetEntityService assetEntityService

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
	 * @see "https://qz.io/wiki/2.0-signing-messages"
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
		AssetComment task = taskService.invokeLocalAction(id, currentPerson())

		Map results = [
			assetComment: task,
			task: task.taskToMap(),
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
	def recordRemoteActionStarted(Long id, RecordRemoteActionStartedCommand commandObject) {
		Map actionRequest = taskActionService.recordRemoteActionStarted(id,  currentPerson(), commandObject.publicKey)
		renderAsJson([actionRequest: actionRequest])
	}

	/**
	 * Reset an action tied to a task and returns new task status if applies.
	 * @param id - task id
	 * @return
	 */
	@HasPermission(Permission.ActionReset)
	def resetAction(Long id) {
		AssetComment task = taskActionService.resetAction(id, currentPerson())
		renderAsJson([
			assetComment: task.taskToMap(),
			task: task.taskToMap(),
			status: task.status,
			statusCss: taskService.getCssClassForStatus(task.status)
		])
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

	/**
	 * 	Returns a json with the rows that will be used to render the Task Manager contents.
	 *
	 *@return  The list of tasks as JSON
	 */
	@HasPermission(Permission.TaskManagerView)
	def listTasks(ListTaskCommand params) {
		validateCommandObject(params)
		Map<String, String> definedSortableFields = [
				'actFinish'         : 'dateResolved',
				'actStart'          : 'actStart',
				'apiAction'         : 'apiAction',
				'assetName'         : 'assetName',
				'assetType'         : 'assetType',
				'assignedTo'        : 'assignedTo',
				'bundle'            : 'bundle',
				'category'          : 'category',
				'comment'           : 'comment',
				'createdBy'         : 'createdBy',
				'dateCreated'       : 'dateCreated',
				'dateResolved'      : 'dateResolved',
				'dueDate'           : 'dueDate',
				'duration'          : 'duration',
				'durationScale'     : 'durationScale',
				'estFinish'         : 'estFinish',
				'estStart'          : 'estStart',
				'event'             : 'event',
				'hardAssigned'      : 'hardAssigned',
				'instructionsLink'  : 'instructionsLink',
				'isCriticalPath'    : 'isCriticalPath',
				'isPublished'       : 'isPublished',
				'lastUpdated'       : 'lastUpdated',
				'latestFinish'      : 'latestFinish',
				'latestStart'       : 'latestStart',
				'percentageComplete': 'percentageComplete',
				'priority'          : 'priority',
				'role'              : 'role',
				'score'             : 'score',
				'sendNotification'  : 'sendNotification',
				'slack'				: 'slack',
				'status'            : 'status',
				'statusUpdated'     : 'statusUpdated',
				'taskNumber'        : 'taskNumber',
				'taskSpec'          : 'taskSpec',
				'updatedTime'       : 'statusUpdated'
		].withDefault { key -> '' }

		Project project = getProjectForWs()
		String sortIndex = definedSortableFields[params.sortColumn]
		String sortOrder = params.sortOrder

		// Get the pagination and set the user preference appropriately
		Integer maxRows = paginationMaxRowValue(params.rows, UserPreferenceEnum.TASK_LIST_SIZE)
		Integer currentPage = params.page
		Integer offset = paginationRowOffset(currentPage, maxRows)

		userPreferenceService.setPreference(UserPreferenceEnum.JUST_REMAINING, params.justRemaining)
		userPreferenceService.setPreference(UserPreferenceEnum.MY_TASK, params.justMyTasks)

		if (params.viewUnpublished) {
			securityService.requirePermission Permission.TaskPublish
			userPreferenceService.setPreference(UserPreferenceEnum.VIEW_UNPUBLISHED, true)
		}else{
			userPreferenceService.setPreference(UserPreferenceEnum.VIEW_UNPUBLISHED, false)
		}

		userPreferenceService.setMoveEventId params.moveEvent

		// Determine if only unpublished tasks need to be fetched.
		params.viewUnpublished = securityService.viewUnpublished() ? 1 : 0

		// Fetch the tasks, the total count and the number of pages.
		Map taskRows = taskService.getTaskRows(project, params, sortIndex, sortOrder, maxRows, offset)
		renderAsJson(rows: taskRows.rows, totalCount: taskRows.totalCount)
	}

    @HasPermission(Permission.TaskManagerView)
    def listCustomColumns() {
        Map taskPref = assetEntityService.getExistingPref(UserPreferenceEnum.Task_Columns)
        def assetCommentFields = AssetComment.taskCustomizeFieldAndLabel
        Map modelPref = [:]
        /*String[] modelPref2 = new String[taskPref.keySet().size()]*/
        taskPref.eachWithIndex { key, value, index -> modelPref[index] = value }
        /* taskPref.eachWithIndex { key, value, index -> modelPref['userSelectedCol' + index] = assetCommentFields[value] } */
        renderAsJson(customColumns: modelPref, assetCommentFields: assetCommentFields)
    }

    /**
     * used to set the Application custom columns pref as JSON
     * @param columnValue
     * @param from
     * @render true
     */
    @HasPermission(Permission.TaskManagerView)
    def setCustomColumns() {
        Map requestParams = request.JSON
        def column = requestParams.columnValue
        String fromKey = requestParams.from
        def prefCode = requestParams.type as UserPreferenceEnum
        println(requestParams)
        assert prefCode
        def existingColsMap = assetEntityService.getExistingPref(prefCode)
        String key = existingColsMap.find { it.value == column }?.key
        if (key) {
            existingColsMap[key] = requestParams.previousValue
        }

        existingColsMap[fromKey] = column
        userPreferenceService.setPreference(prefCode, existingColsMap as JSON)
        def assetCommentFields = AssetComment.taskCustomizeFieldAndLabel
        renderAsJson(customColumns: existingColsMap, assetCommentFields: assetCommentFields)
    }

	/**
	 * Create a map containing all the information necessary for the task action bar for a given task
	 * @param task - The task the information is based off.
	 * @return - The map to be used by the task action bar
	 */
	Map createTaskActionBarMap(Task task) {
		Map<String, ?> invokeActionDetails = task.getInvokeActionButtonDetails()
		return [
				taskId: task.id,
				apiActionId: task.apiAction?.id,
				apiActionInvokedAt: task.apiActionInvokedAt,
				apiActionCompletedAt: task.apiActionCompletedAt,
				assignedTo: task.assignedTo?.id,
				assignedToName: task.assignedTo?.toString() ?: '',
				category: task.category,
				invokeActionDetails: invokeActionDetails,
				predecessorsCount: task.taskDependencies.size(),
				status: task.status,
				successorsCount: TaskDependency.countByPredecessor(task)
		]
	}

	/**
	 * Retrieve the information required by the front-end for rendering the Action Bar for a given Task.
	 * @param taskId - the id of the task.
	 * @return the API Action ID (if any), the ID of the person assigned to the task (if any), the number of
	 *  successors and predecessors.
	 */
    @HasPermission(Permission.TaskManagerView)
    def getInfoForActionBar(Long taskId) {
        Project project = getProjectForWs()
        AssetComment task = GormUtil.findInProject(project, AssetComment, taskId, true)
		renderSuccessJson(createTaskActionBarMap(task))
    }

	@HasPermission(Permission.TaskManagerView)
	def getBulkInfoForActionBar() {
		Project project = getProjectForWs()
		JSONArray taskIds = request.JSON
		List<Map> taskList = []
		for (Integer id: taskIds) {
			AssetComment task = GormUtil.findInProject(project, AssetComment, id, true)
			taskList.push(createTaskActionBarMap(task))
		}
		renderSuccessJson(taskList)
	}


    /**
     * Return a list with all the AssetCommentCategory values.
     */
    @HasPermission(Permission.CommentView)
    def assetCommentCategories() {
        renderSuccessJson(AssetCommentCategory.list)
    }

	/**
	 * Looks up the API action and provides it's details as a JSON map.
	 *
	 * @param taskId The id of the task to use to look up the API action details.
	 *
	 * @return A JSON map containing the API action details:
	 *    name,
	 *    script,
	 *    isRemote,
	 *    type,
	 *    connector(name),
	 *    method,
	 *    description,
	 *    methodParams,
	 *    methodParamsValues
	 */
	@HasPermission(Permission.TaskView)
	def actionLookUp(Long taskId) {
		Project project = securityService.userCurrentProject
		render view: 'actionLookUp', model: [apiAction: taskActionService.actionLookup(taskId, project)]
	}

}
