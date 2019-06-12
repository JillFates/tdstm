package net.transitionmanager.service

import com.tds.asset.AssetComment
import com.tdssrc.grails.ThreadLocalUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.command.task.ActionCommand
import net.transitionmanager.domain.Person
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ActionThreadLocalVariable
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.task.TaskFacade
import org.codehaus.groovy.grails.web.json.JSONObject
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
	SecurityService   securityService

	/**
	 * Handles updating the action status when the action was started.
	 *
	 * @param action the ActionCommand object
	 * @see net.transitionmanager.command.task.ActionCommand *
	 * @param taskId The task that the action command it tied to.
	 * @param currentPerson The currently logged in person.
	 */
	void actionStarted(ActionCommand action, Long taskId, Person currentPerson) {
		securityService.hasAccessToProject(action.project)
		AssetComment task = get(AssetComment, taskId, action.project)
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
		securityService.hasAccessToProject(action.project)
		AssetComment task = get(AssetComment, taskId, action.project)
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
		securityService.hasAccessToProject(action.project)
		AssetComment task = get(AssetComment, taskId, action.project)
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
		securityService.hasAccessToProject(action.project)
		AssetComment task = get(AssetComment, taskId, action.project)
		addMessageToTaskNotes(action.message, task, currentPerson)

		invokeReactionScript(ReactionScriptCode.ERROR, task, action.message, action.stdout, action.stderr, false)
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

}
