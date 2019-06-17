package net.transitionmanager.service

import com.tds.asset.AssetComment
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.RSACodec
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.RemoteCredentialMethod
import com.tdssrc.grails.ThreadLocalUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.command.task.ActionCommand
import net.transitionmanager.domain.Person
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ActionThreadLocalVariable
import net.transitionmanager.integration.ApiActionException
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.security.Permission
import net.transitionmanager.task.TaskFacade
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.dao.CannotAcquireLockException
import org.springframework.web.multipart.MultipartFile

import java.security.Key

/**
 * A service to hand status updates, from invoking remote actions on TMD.
 */
@Transactional
@Slf4j
class TaskActionService implements ServiceMethods {


	ApiActionService  apiActionService
	AssetService      assetService
	CoreService       coreService
	CredentialService credentialService
	FileSystemService fileSystemService
	SecurityService   securityService
	TaskService       taskService

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
		task.apiActionPercentDone = action.progress
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
		invokeReactionScript(ReactionScriptCode.SUCCESS, task, action.message, action.stdout, action.stderr, true, action.data, action.datafile)
		task.apiActionCompletedAt = new Date()
		task.apiActionPercentDone = 100
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
		invokeReactionScript(ReactionScriptCode.ERROR, task, action.message, action.stdout, action.stderr, false)
	}

	/**
	 * Used to indicate that a task's remote action has been started. This will update the
	 * action and task appropriately after which the Action Request object is constructed.
	 *
	 * @param taskId The task that the action command it tied to.
	 * @param currentPerson The currently logged in person.
	 * @return Map that represents the ActionRequest object with additional attributes stuffed in for good measure
	 */
	Map<String,?> recordRemoteActionStarted(Long taskId, Person whom, String publicKey) {
		AssetComment task = fetchTaskById(taskId, whom)

		// task = taskService.recordRemoteActionStarted(task, whom)
		task = markActionStarted(task.id, whom, false)

		ActionRequest actionRequest = apiActionService.createActionRequest(task.apiAction, task)

		Map<String,?> actionRequestMap = actionRequest.toMap()

		// Store Callback information into the options
		String url = coreService.getApplicationUrl()
		Map jwt = securityService.generateJWT()
		actionRequestMap.options.callback = [
			siteUrl: url,
			token: jwt.access_token,
			refreshToken: jwt.refresh_token
		]

		// check if api action has credentials so to include credentials password unencrypted
		// TODO : JM 6/19 : The credential type needs to be determined (server supplied)
		if (actionRequest.options.hasProperty('credentials') && actionRequestMap.options.credentials) {
			// Need to create new map because credentials map originally is immutable
			Map<String, ?> credentials = new HashMap<>(actionRequestMap.options.credentials)
			credentials.password = credentialService.decryptPassword(task.apiAction.credential)
			actionRequestMap.options.credentials = credentials

			 //Encrypt the credentials appropriately
			if (task.apiAction.remoteCredentialMethod == RemoteCredentialMethod.SUPPLIED) {
				RSACodec rsaCodec = new RSACodec()
				Key key = rsaCodec.getPublicKey(publicKey)

				actionRequest.options.credentials.username = rsaCodec.encrypt(key, actionRequest.options.credentials.username)
				actionRequest.options.credentials.password = rsaCodec.encrypt(key, actionRequest.options.credentials.password)
			}
		}

		return actionRequestMap
	}

	/**
	 * Used to invoke an action on the task which will attempt to do so. If the function fails then it will
	 * plan to set the status to HOLD and add a note to the task.
	 * @param task - the Task to invoke the method on
	 * @return The status that the task should be set to
	 */
	@Transactional(noRollbackFor=[Throwable])
	AssetComment invokeLocalAction(Long taskId, Person whom) {
		AssetComment task = fetchTaskById(taskId, whom)

		log.debug "invokeLocalAction() Attempting to invoke action ${task.apiAction.name} for task.id=${task.id}"

		markActionStarted(task.id, whom, true)

		// Try running the action
		apiActionService.invoke(task.apiAction, task)

		return task
	}

	/**
	 * Reset an action so it can be invoked again
	 * @param task
	 * @param whom
	 * @return
	 */
	String resetAction(Long taskId, Person whom) {
		AssetComment task = fetchTaskById(taskId, whom)

		String status = task.status

		if (task.hasAction() && !task.isAutomatic() && status in AssetCommentStatus.AllowedStatusesToResetAction) {
			String errMsg
			try {
				// Update the task so it can be invoked again
				task.apiActionInvokedAt = null
				task.apiActionCompletedAt = null
				task.actStart = null
				task.dateResolved = null

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
				task.status = ACS.HOLD
			}
		} else {
			throwException(InvalidRequestException, 'apiAction.task.message.actionUnableToReset', 'Unable to reset action due to task status or other circumstances')
		}

		return task.status
	}

	/**
	 * Try to mark a task as started by locking it before, if lock fails
	 * or if the task was already started by another user, thows an exception
	 * indicating it so
	 * @param taskId - the id of the task to mark as started
	 * @param whom - the id whos invoking the action
	 * @param invokingLocally - Flag to indicate if the intent is to invoke the action local (true) or remotely (false)
	 */
	@Transactional(noRollbackFor=[Throwable])
	private AssetComment markActionStarted(Long taskId, Person whom, Boolean invokingLocally) {
		log.debug "markActionStarted() Attempting to mark action as started for task.id={}", taskId
		String errMsg
		AssetComment taskWithLock

		try {
			taskWithLock = AssetComment.lock(taskId)
			log.debug 'Locked out AssetComment: {}', taskWithLock

			if (! taskWithLock.status) {
				throw new EmptyResultException('Task was not found')
			}

			if (! AssetCommentStatus.ActionableStatusCodes.contains(taskWithLock.status)) {
				throwException(InvalidRequestException, 'apiAction.task.message.taskNotInActionableState', 'Task status must be in the Ready or Started state in order to invoke an action')
			}

			if (!taskWithLock.hasAction()) {
				throwException(InvalidRequestException, 'apiAction.task.message.noAction', 'Task has not associated action')
			}

			if (taskWithLock.apiActionInvokedAt != null) {
				throwException(ApiActionException, 'apiAction.task.message.alreadyInvoked', 'The action has already been invoked')
			}

			// if (taskWithLock.apiAction.isAsync()) {
			// 	throwException(ApiActionException, 'apiAction.task.message.asyncActionNotSupported', 'Asynchronous actions are not yet supported')
			// }

			// Attempting to invoke a remote action locally?
			if (invokingLocally && taskWithLock.isActionInvocableRemotely()) {
				throwException(ApiActionException, 'apiAction.task.message.notLocalAction', 'Attempted to invoke a remote action as local, which is not allowed')
			}

			// Attempting to invoke a local action remotely?
			if (! invokingLocally && taskWithLock.isActionInvocableLocally()) {
				throwException(ApiActionException, 'apiAction.task.message.notRemoteAction', 'Attepted to invoke a local action as remote, which is not allowed')
			}

			// Make sure the use has permission to invoke the action
			if ( !securityService.hasPermission(whom, Permission.ActionInvoke) ||
				( ! invokingLocally && !securityService.hasPermission(whom, Permission.ActionRemoteAllowed, false)) ) {
				throwException(ApiActionException, 'apiAction.task.message.noPermission', 'Do not have proper permission to invoke actions')
			}

			// Prevent action from occuring if the reaction scripts are invalid
			if (!taskWithLock.apiAction.reactionScriptsValid == 1) {
				throwException(ApiActionException, 'apiAction.task.message.invalidReactionScript', 'The Action Reaction script(s) appear to have errors that need to be resolved before invoking action')
			}
			// Update the task so that the we track that the action was invoked
			taskWithLock.apiActionInvokedAt = new Date()

			// Update the task so that we track the task started at
			taskWithLock.actStart = taskWithLock.apiActionInvokedAt

			// Make sure that the status is STARTED instead
			if (taskWithLock.status != AssetCommentStatus.STARTED) {
				taskService.setTaskStatus(taskWithLock, AssetCommentStatus.STARTED, whom)
			}
			taskWithLock.save(flush: true, failOnError: true)

			// Note to the task indicating that the action was being invoked
			taskService.addNote(taskWithLock, whom, "Invoked action ${taskWithLock.apiAction.name} (${ invokingLocally ? 'Server' : 'Desktop' })")

		// Catch the Exceptions that the error message can be used directly
		} catch (EmptyResultException e) {
			errMsg = e.getMessage()
		} catch (InvalidRequestException e) {
			errMsg = e.getMessage()
		} catch (InvalidConfigurationException e) {
			errMsg = e.getMessage()
		} catch (CannotAcquireLockException e) {
			errMsg = e.getMessage()
		} catch (ApiActionException e) {
			errMsg = e.getMessage()

		} catch (e) {
			errMsg = 'A runtime error occurred while attempting to process the action'
			log.error ExceptionUtil.stackTraceToString('invokeLocalAction() failed ', e)
		}

		if (errMsg) {
			if (! taskWithLock) {
				// The task might not be assigned if there was a lock failure so let's get it now
				taskWithLock = AssetComment.get(taskId)
			}

			if (taskWithLock) {
				taskService.addNote(taskWithLock, whom, "Invoke action ${taskWithLock.apiAction.name} failed : $errMsg")
				taskService.setTaskStatus(taskWithLock, AssetCommentStatus.HOLD, whom)
			}

			// TODO : JPM 6/2019 : We should have a general TM exception that we know the message can displayed to the user
			throw new InvalidParamException(errMsg)
		}

		return taskWithLock
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
	 * @param code The ReactionScriptCode SUCCESS or ERROR
	 * @param task The task that relates to the action, that was run.
	 * @param stdout The standard output of the remote action that was run.
	 * @param stderr The standard error of the remote action that was run.
	 * @param data The Json data context sent back from invoking the remote action.
	 * @param datafile A list of data files sent back as context of invoking the remote action
	 */
	private void invokeReactionScript(
		ReactionScriptCode code,
		AssetComment task,
		String message,
		String stdout,
		String stderr,
		boolean successful,
		JSONObject data = null,
		List<MultipartFile> datafile = null) {

		ActionRequest actionRequest = apiActionService.createActionRequest(task.apiAction, task)
		TaskFacade taskFacade = grailsApplication.mainContext.getBean(TaskFacade.class, task)
		JSONObject reactionScripts = (JSONObject) ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.REACTION_SCRIPTS)
		String script = reactionScripts[code.name()]
		AssetFacade assetFacade = assetService.getAssetFacade(task.assetEntity, true)

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
		} catch (Exception e) {

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
	 * Used by these service methods to access the Task referenced by the task ID. If the
	 * person does not have access to the project then an exception will be thrown
	 *
	 * @param taskId The task that the action command it tied to.
	 * @param currentPerson The currently logged in person.
	 */
	private AssetComment fetchTaskById(Long taskId, Person currentPerson) {
		AssetComment task = AssetComment.get(taskId)
		if (! task) {
			throw new EmptyResultException('Task was not found')
		}
		// Validate that the user has access to the project associated with the task
		securityService.hasAccessToProject(task.project)

		return task
	}

}
