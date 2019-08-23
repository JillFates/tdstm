package net.transitionmanager.action

import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.ThreadLocalUtil
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.asset.AssetService
import net.transitionmanager.command.task.ActionCommand
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.connector.AbstractConnector
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.InvalidConfigurationException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ActionThreadLocalVariable
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.CredentialService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskFacade
import net.transitionmanager.task.TaskService
import org.grails.web.json.JSONObject
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.multipart.MultipartFile
/**
 * A service to hand status updates, from invoking remote actions on TMD.
 */
@Transactional
class TaskActionService implements ServiceMethods {

	TaskService       taskService
	FileSystemService fileSystemService
	ApiActionService  apiActionService
	AssetService      assetService
	CredentialService credentialService
	SecurityService   securityService
	CoreService       coreService

	/**
	 * Handles updating the action status when the action was started.
	 *
	 * @param action the ActionCommand object
	 * @see net.transitionmanager.command.task.ActionCommand *
	 * @param taskId The task that the action command it tied to.
	 * @param currentPerson The currently logged in person.
	 */
	void actionStarted(ActionCommand action, Long taskId, Person currentPerson) {
		AssetComment task = fetchTaskForAction(action, taskId, currentPerson)
		addMessageToTaskNotes(action.message, task, currentPerson)
		taskService.addNote(task, currentPerson, "${task?.apiAction?.name ?: ''} started at ${new Date().format(TimeUtil.FORMAT_DATE_ISO8601)}")
	}

	/**
	 * Handles updating the action status when there is progress.
	 *
	 * @param action the ActionCommand object
	 * @see net.transitionmanager.command.task.ActionCommand *
	 * @param taskId The task that the action command it tied to.
	 * @param currentPerson The currently logged in person.
	 */
	void actionProgress(ActionCommand action, Long taskId, Person currentPerson) {
		AssetComment task = fetchTaskForAction(action, taskId, currentPerson)
		addMessageToTaskNotes(action.message, task, currentPerson)
		task.percentageComplete = action.progress
		task.save()
	}

	/**
	 * Handles updating the action status when it is done/successful
	 *
	 * @param action the ActionCommand object
	 * @see net.transitionmanager.command.task.ActionCommand *
	 * @param taskId The task that the action command it tied to.
	 * @param currentPerson The currently logged in person.
	 */
	void actionDone(ActionCommand action, Long taskId, Person currentPerson) {
		AssetComment task = fetchTaskForAction(action, taskId, currentPerson)
		addMessageToTaskNotes(action.message, task, currentPerson)
		logForDebug(task, currentPerson,  action.stdout, action.stderr)
		invokeReactionScript(currentPerson, ReactionScriptCode.SUCCESS, task, action.message, action.stdout, action.stderr, true, action.data, action.datafile)
		task.apiActionCompletedAt = new Date()
		task.percentageComplete = 100
		task.save()
	}

	/**
	 * Handles updating the action status in the case of an error.
	 *
	 * @param action the ActionCommand object
	 * @see net.transitionmanager.command.task.ActionCommand *
	 * @param taskId The task that the action command it tied to.
	 * @param currentPerson The currently logged in person.
	 */
	void actionError(ActionCommand action, Long taskId, Person currentPerson) {
		AssetComment task = fetchTaskForAction(action, taskId, currentPerson)
		addMessageToTaskNotes(action.message, task, currentPerson)
		logForDebug(task, currentPerson,  action.stdout, action.stderr)
		invokeReactionScript(currentPerson, ReactionScriptCode.ERROR, task, action.message, action.stdout, action.stderr, false)
	}

	/**
	 * Logs stdOut and stdErr to task notes if the action has debug enabled
	 *
	 * @param task The task to log a note for, if its action has debugEnabled = true
	 * @param currentPerson  The currently logged in person.
	 * @param stdOut Standard output returned.
	 * @param stdErr Standard error output returned.
	 */
	void logForDebug(AssetComment task, Person currentPerson, String stdOut, String stdErr) {
		if (task.apiAction.debugEnabled) {
			addMessageToTaskNotes(stdOut, task, currentPerson)
			addMessageToTaskNotes(stdErr, task, currentPerson)
		}
	}

	/**
	 * Checks the task to see if it's associated with an asset, and adds a note to the task if there is a message.
	 *
	 * @param message the optional message
	 * @param task the task to update
	 * @param currentPerson The currently logged in person.
	 */
	private void addMessageToTaskNotes(String message, AssetComment task, Person currentPerson) {
		if (message) {
			taskService.addNote(task, currentPerson, message)
		}
	}

	/**
	 * Sets up and invokes the reaction script.
	 *
	 * @param whom - the individual that triggered the invocation of the action
	 * @param code The ReactionScriptCode SUCCESS or ERROR
	 * @param task The task that relates to the action, that was run.
	 * @param stdout The standard output of the remote action that was run.
	 * @param stderr The standard error of the remote action that was run.
	 * @param data The Json data context sent back from invoking the remote action.
	 * @param datafile A list of data files sent back as context of invoking the remote action
	 */
	private void invokeReactionScript(
		Person whom,
		ReactionScriptCode code,
		AssetComment task,
		String message,
		String stdout,
		String stderr,
		boolean successful,
		JSONObject data = null,
		List<MultipartFile> datafile = null) {

		ActionRequest actionRequest = apiActionService.createActionRequest(task.apiAction, task)
		TaskFacade taskFacade = grailsApplication.mainContext.getBean(TaskFacade.class, task, whom)
		JSONObject reactionScripts = (JSONObject) ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.REACTION_SCRIPTS)
		String script = reactionScripts[code.name()]

		AssetEntity asset = task.assetEntity
		AssetFacade assetFacade = assetService.getAssetFacade(asset, false)

		List<String> filenames = datafile.collect { MultipartFile file ->
			fileSystemService.writeFile(file, fileSystemService.TMD_PREFIX)
		}
		ApiActionResponse apiActionResponse = new ApiActionResponse(
			originalFilename: datafile ? datafile[0].originalFilename : null,
			filename: filenames ? filenames[0] : null,
			data: data,
			message: message,
			stdout: stdout,
			stderr: stderr,
			successful: successful
		)

		try {
			apiActionService.invokeReactionScript(code, script, actionRequest, apiActionResponse, taskFacade, assetFacade, new ApiActionJob())
			// if asset facade is not null and task as an asset entity
			// let's perform asset validation errors to inform the user about
			// potential hidden errors during reaction scripts invokation
			if (assetFacade && asset) {
				if (!asset.validate()) {
					asset.errors.allErrors.each {
						log.info('Task {}-{} has asset entity field validation errors. {}', task.taskNumber, task.comment, it)
						// add task note with asset entity validation error
						taskService.addNote(task, whom, i18nMessage(it))
					}
					throw new InvalidParamException("Asset: ${asset.assetType}-${asset.assetName} have validation errors.")
				}
			}
		} catch (Exception e) {
			log.info('Reaction script invoke error. ', e)
			taskService.addNote(task, whom, e.message)
			if (code == ReactionScriptCode.ERROR) {
				taskFacade.error("$code script failure: ${e.message}")
			} else {
				try {
					script = reactionScripts[ReactionScriptCode.ERROR.name()]
					apiActionService.invokeReactionScript(ReactionScriptCode.ERROR, script, actionRequest, apiActionResponse, taskFacade, assetFacade, new ApiActionJob())
				} catch (Exception ex) {
					taskFacade.error("$code script failure: ${ex.message}")
				}
			}
		} finally {
			// When the API call has finished the ThreadLocal variables need to be cleared out to prevent a memory leak
			ThreadLocalUtil.destroy(ApiActionService.THREAD_LOCAL_VARIABLES)
		}
	}

	/**
	 * Used by these service methods to fetch the Task referenced in the action command object. If the
	 * person does not have access to the project then an exception will be thrown.
	 *
	 * @param action the ActionCommand object
	 * @see net.transitionmanager.command.task.ActionCommand *
	 * @param taskId The task that the action command it tied to.
	 * @param currentPerson The currently logged in person.
	 */
	private AssetComment fetchTaskForAction(ActionCommand action, Long taskId, Person currentPerson) {
		securityService.hasAccessToProject(action.project)
		return get(AssetComment, taskId, action.project)
	}


	/**
	 * Used to indicate that a task's remote action has been started. This will update the
	 * action and task appropriately after which the Action Request object is constructed.
	 *
	 * @param taskId The task that the action command it tied to.
	 * @param currentPerson The currently logged in person.
	 * @return Map that represents the ActionRequest object with additional attributes stuffed in for good measure
	 *
	 * TODO possibly move this to the TaskActionService, so that it and renderscript can be made unit testable...
	 */
	Map<String,?> recordRemoteActionStarted(Long taskId, Person whom, String publicKey) {
		AssetComment task = taskService.fetchTaskById(taskId, whom)

		task = taskService.markActionStarted(task.id, whom, false)

		ActionRequest actionRequest = apiActionService.createActionRequest(task.apiAction, task)

		Map<String,?> actionRequestMap = actionRequest.toMap()
		String script = (String) actionRequestMap.options.apiAction.script

		actionRequestMap.options.apiAction.script = renderScript(script, task)

		// Store Callback information into the options
		String url = coreService.getApplicationUrl()
		Map jwt = securityService.generateJWT()
		actionRequestMap.options.callback = [
			siteUrl: url,
			token: jwt.access_token
		]

		// check if api action has credentials so to include credentials password unencrypted
		// TODO : JM 6/19 : The credential type needs to be determined (server supplied)
		if (actionRequest.options.hasProperty('credentials') && actionRequestMap.options.credentials) {
			// Need to create new map because credentials map originally is immutable
			Map<String, ?> credentials = new HashMap<>(actionRequestMap.options.credentials)

			// Encrypt the username and password with the Public Key that the client sent to the server
			 credentials.username = securityService.encryptWithPublicKey(task.apiAction.credential.username, publicKey)
			 credentials.password = securityService.encryptWithPublicKey(credentialService.decryptPassword(task.apiAction.credential), publicKey)

			actionRequestMap.options.credentials = credentials
		}

		// Add the Task object to the returned map
		actionRequestMap.task = task.taskToMap()

		return actionRequestMap
	}

	/**
	 * Renders parameters into a script that had parameters in the form of {paramName}
	 *
	 * @param script The String to substitute params into
	 * @param task The task to get the action and build the parameters map with.
	 * @return The script with the parameters filled in
	 */
	String renderScript(String script,  AssetComment task) {
		AbstractConnector connector = apiActionService.connectorInstanceForAction(task.apiAction)
		Map params = connector.buildMethodParamsWithContext(task.apiAction, task)
		params.username = '{username}'
		params.password = '{password}'

		return StringUtil.replacePlaceholders(script, params)
	}

	/**
	 * Reset an action so it can be invoked again
	 * @param taskId - the ID of the task
	 * @param whom
	 * @return
	 */
	AssetComment resetAction(Long taskId, Person whom) {
		AssetComment task = taskService.fetchTaskById(taskId, whom)

		if (task.hasAction() && !task.isAutomatic() && task.status in AssetCommentStatus.AllowedStatusesToResetAction) {
			String errMsg
			try {
				// Update the task so it can be invoked again
				task.apiActionInvokedAt = null
				task.apiActionCompletedAt = null
				task.actStart = null
				task.dateResolved = null
				task.percentageComplete = 0

				// Log a note that the API Action was reset
				taskService.addNote(task, whom, "Reset action ${task.apiAction.name}")

				// Make sure that the status is READY instead
				task.status = AssetCommentStatus.READY

			} catch (InvalidRequestException e) {
				errMsg = e.getMessage()
			} catch (InvalidConfigurationException e) {
				errMsg = e.getMessage()
			} catch (e) {
				errMsg = 'A runtime error occurred while attempting to process the action'
				log.error ExceptionUtil.stackTraceToString('resetAction() failed ', e)
			}

			if (errMsg) {
				log.info "resetAction() error $errMsg"
				taskService.addNote(task, whom, "Reset action ${task.apiAction.name} failed : $errMsg")
				task.status = AssetCommentStatus.HOLD
			}
		} else {
			throwException(InvalidParamException, 'apiAction.task.message.actionUnableToReset', 'Unable to reset action due to task status or other circumstances')
		}
		return task
	}

	/**
	 * This looks up the details of an api action, including parameters, and script, if applicable.
	 *
	 * @param taskId The task to look up and API action for.
	 * @param project The project to use to look up the API action.
	 *
	 * @return A map of API action details including:
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
	Map actionLookup(Long taskId, Project project) {
		AssetComment assetComment = AssetComment.findByIdAndProject(taskId, project)
		if (!assetComment) {
			throw new EmptyResultException("Task $taskId not found.")
		}

		if (assetComment.apiAction) {
			ApiAction apiAction = assetComment.apiAction
			AbstractConnector connector = apiActionService.connectorInstanceForAction(assetComment.apiAction)

			List<Map> methodParamsList = apiAction.methodParamsList
			methodParamsList = taskService.fillLabels(project, methodParamsList)

			return [
				name              : apiAction.name,
				script            : renderScript(apiAction.script, assetComment),
				isRemote          : apiAction.isRemote,
				type              : apiAction.actionType.type,
				connector         : connector?.name,  //adding the ? so that I can unit test this.
				method            : apiAction.connectorMethod,
				description       : apiAction?.description,
				methodParams      : methodParamsList,
				methodParamsValues: apiActionService.buildMethodParamsWithContext(apiAction, assetComment)
			]
		} else {
			throw new AccessDeniedException("Action doesn't exist for users project.")
		}
	}

}
