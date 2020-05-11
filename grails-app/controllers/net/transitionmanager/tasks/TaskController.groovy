package net.transitionmanager.tasks

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.action.ApiActionService
import net.transitionmanager.action.TaskActionService
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.asset.AssetOptionsService
import net.transitionmanager.asset.AssetService
import net.transitionmanager.asset.CommentService
import net.transitionmanager.command.task.SetLabelQuantityPrefCommand
import net.transitionmanager.common.ControllerService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.GraphvizService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.ServiceException
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.Person
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.reporting.ReportsService
import net.transitionmanager.security.Permission
import net.transitionmanager.security.RoleType
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.TaskService
import org.apache.commons.lang3.math.NumberUtils
import org.springframework.context.MessageSource
import org.springframework.jdbc.core.JdbcTemplate

import static com.tdsops.tm.enums.domain.AssetCommentStatus.COMPLETED
import static com.tdsops.tm.enums.domain.AssetCommentStatus.HOLD
import static com.tdsops.tm.enums.domain.AssetCommentStatus.PENDING
import static com.tdsops.tm.enums.domain.AssetCommentStatus.PLANNED
import static com.tdsops.tm.enums.domain.AssetCommentStatus.READY
import static com.tdsops.tm.enums.domain.AssetCommentStatus.STARTED
import static com.tdsops.tm.enums.domain.AssetCommentStatus.TERMINATED
import static net.transitionmanager.security.Permissions.Roles.ROLE_ADMIN
import static net.transitionmanager.security.Permissions.Roles.ROLE_CLIENT_ADMIN
import static net.transitionmanager.security.Permissions.Roles.ROLE_CLIENT_MGR
import static net.transitionmanager.security.SecurityService.AUTOMATIC_ROLE

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class TaskController implements ControllerMethods {

	// Color scheme for status key:[font, background]
	private static final Map taskStatusColorMap = [
		(HOLD):       ['black',   '#FFFF33'],
		(PLANNED):    ['black',   'white'],
		(READY):      ['white',   'green'],
		(PENDING):    ['black',   'white'],
		(STARTED):    ['white',   'darkturquoise'],
		(COMPLETED):       ['white',   '#24488A'],
		(TERMINATED): ['white',   'black'],
		'AUTO_TASK':  ['#848484', '#848484'], // [font, edge]
		'ERROR':      ['red',     'white']    // Use if the status doesn't match
	]

	AssetEntityService       assetEntityService
	AssetService             assetService
	ApiActionService         apiActionService
	CommentService           commentService
	ControllerService        controllerService
	CustomDomainService      customDomainService
	JdbcTemplate             jdbcTemplate
	PartyRelationshipService partyRelationshipService
	ReportsService           reportsService
	TaskService              taskService
	TaskActionService        taskActionService
	UserPreferenceService    userPreferenceService
	GraphvizService          graphvizService
	MessageSource            messageSource
	AssetOptionsService      assetOptionsService

	@HasPermission(Permission.TaskView)
	def index() { }

	/**
	 * Used to get the task information by ID
	 * @param id - the ID of the task to retrieve
	 * @return JSON map of the Task object
	 */
	def task(Long id) {
		AssetComment task = taskService.fetchTaskById(id, currentPerson())
		renderAsJson(task:task.taskToMap())
	}

	/**
	 * Used to change the state of a task between Started, Done and Hold.
	 * This will take the task ID and check to make sure that the user has access vs using the
	 * User CurrentProject.
	 * @param id - the ID of the task
	 * @param currentStatus - the current status that we are expecting the task to be in
	 * @param status - the status to set the task to
	 * @param message - optional message when setting the task on HOLD
	 * @return The task as an JSON object is updated
	 */
	def changeTaskState(Long id) {
		AssetComment task = taskService.updateTaskState(currentPerson(), id, request.JSON.currentStatus, request.JSON.status, request.JSON.message)
		renderAsJson(task:task.taskToMap())
	}

	/**
	 * Used by the myTasks and Task Manager to update tasks appropriately.
	 */
	@HasPermission(Permission.TaskEdit)
	def update() {
		String tzId = userPreferenceService.timeZone
		String userDTFormat = userPreferenceService.dateFormat
		// Deal with legacy view parameters.
		Map requestParams = null

		if (request.format == 'json') {
			requestParams = request.JSON
		} else {
			params.taskDependency = params.list('taskDependency[]')
			params.taskSuccessor = params.list('taskSuccessor[]')
			requestParams = params
		}

		def map = commentService.saveUpdateCommentAndNotes(tzId, userDTFormat, requestParams, false, flash)

		if (requestParams.printers) {
			userPreferenceService.setPreference(PREF.PRINTER_NAME, requestParams.printers)
			userPreferenceService.setPreference(PREF.PRINT_LABEL_QUANTITY, requestParams.printTimes)
		}

		if (requestParams.view == 'myTask') {
			if (map.error) {
				flash.message = map.error
			}

			def redirParams = [view: requestParams.view]
			if (requestParams.containsKey('tab') && requestParams.tab) {
				redirParams.tab = requestParams.tab
			}
			if (requestParams.containsKey('sort') && requestParams.sort) {
				redirParams.sort = requestParams.sort
			}
			if (requestParams.status == COMPLETED) {
				redirParams.sync = 1
			}
			forward(controller: 'task', action: 'listUserTasks', params: redirParams)
		}
		else {
			// Coming from the Task Manager
			render map as JSON
		}
	}

	/**
	 * Used to assign assignTo through ajax call from MyTasks
	 * @params : id, status
	 * @return : user full name and errorMessage if status changed by accident.
	 */
	@HasPermission(Permission.TaskEdit)
	def assignToMe() {
		Map requestParams
		if (request.format == 'json') {
			requestParams = request.JSON
		} else {
			requestParams = params
		}

		String errorMsg = ''
		String assignedToName = ''

		try {
			Person assignedTo = taskService.assignToMe(requestParams.id as Long, requestParams.status)
			assignedToName = assignedTo.toString()
		} catch (EmptyResultException | ServiceException e) {
			errorMsg = e.message
		}

		def map = [assignedToName: assignedToName, errorMsg: errorMsg]
		render map as JSON
	}

	/**
	 *  Generate action bar for a selected comment in Task Manager
	 *  @params id - the task (aka AssetComment) id number for the task bark
	 *  @return : actions bar as HTML (Start, Completed, Details, Assign To Me)
	 */
	@HasPermission(Permission.TaskView)
	def genActionBarHTML() {
		def actionBar = retrieveActionBarData(AssetComment.get(params.id))
		render actionBar.toString()
	}

	/**
	 * Used to generate action Bar for task
	 * @param comment : instance of asset comment
	 * @return : Action Bar HTML code.
	 */
	@HasPermission(Permission.TaskView)
	def retrieveActionBarData(comment) {
		// There are a total of 13 columns so we'll subtract for each conditional button
		def cols = 12

		StringBuilder actionBar = new StringBuilder("""<table style="border:0px"><tr>""")
		if (comment) {
			if(comment.status == READY) {
				cols--
				actionBar << _actionButtonTd("startTdId_$comment.id",
					HtmlUtil.actionButton('Start', 'ui-icon-play', comment.id,
						"changeStatus('$comment.id','$STARTED','$comment.status', 'taskManager')"))
			}

			if (comment.status in [READY, STARTED]) {
				cols--
				actionBar << _actionButtonTd("doneTdId_$comment.id",
					HtmlUtil.actionButton('Done', 'ui-icon-check', comment.id,
						"changeStatus('$comment.id','$COMPLETED', '$comment.status', 'taskManager')"))
			}

			actionBar <<
					_actionButtonTd("assignToMeId_$comment.id",
					HtmlUtil.actionButton('Details...', 'ui-icon-zoomin', comment.id, "showAssetComment($comment.id,'show')"))

			if (securityService.currentPersonId != comment.assignedTo?.id && comment.status in [PENDING, READY, STARTED]) {
				cols--
				actionBar << _actionButtonTd("assignToMeId_$comment.id",
					HtmlUtil.actionButton('Assign To Me', 'ui-icon-person', comment.id,
						"assignTask('$comment.id','$comment.assignedTo', '$comment.status', 'taskManager')"))
			}

			if (securityService.hasPermission(Permission.CommentView) && comment.status == READY &&
			    !(comment.category in AssetCommentCategory.moveDayCategories)) {

				actionBar << '<td class="delay_taskManager"><span>Delay for:</span></td>'
				actionBar << _actionButtonTd(	"1dEst_$comment.id",
					HtmlUtil.actionButton('1 day', 'ui-icon-seek-next', comment.id, "changeEstTime('1','$comment.id',this.id)"))
				actionBar << _actionButtonTd(	"2dEst_$comment.id",
					HtmlUtil.actionButton('2 days', 'ui-icon-seek-next', comment.id, "changeEstTime('2','$comment.id',this.id)"))
				actionBar << _actionButtonTd(	"7dEst_$comment.id",
					HtmlUtil.actionButton('7 days', 'ui-icon-seek-next', comment.id, "changeEstTime('7','$comment.id',this.id)"))
			}
		}else {
			log.warn "genActionBarHTML - invalid comment id ($params.id) from user $securityService.currentUsername"
			actionBar << '<td>An unexpected error occurred</td>'
		}

		actionBar << """ <td colspan='$cols'>&nbsp;</td>
			</tr></table>"""

		return actionBar
	}

	/**
	 * Used to generate action Bar for task details view
	 * @param asset comment id.
	 * @render : Action Bar HTML code.
	 */
	@HasPermission(Permission.TaskView)
	def genActionBarForShowView() {
		AssetComment comment = fetchDomain(AssetComment, params)

		StringBuilder actionBar = new StringBuilder("""<span class="slide" style=" margin-top: 4px;">""")
		int cols = 12

		if (comment) {
			if(comment.status == READY) {
				cols--
				actionBar << "<span id='startTdId_$comment.id' width='8%' nowrap='nowrap'>" <<
						HtmlUtil.actionButton('Start', 'ui-icon-play', comment.id,
						"changeStatus('$comment.id','$STARTED','$comment.status', 'taskManager')") << "</span>"
			}

			if (comment.status in [READY, STARTED]) {
				cols--
				actionBar << "<span id='doneTdId_$comment.id' width='8%' nowrap='nowrap'>" <<
						HtmlUtil.actionButton('Done', 'ui-icon-check', comment.id,
						"changeStatus('$comment.id','$COMPLETED', '$comment.status', 'taskManager')") << "</span>"
			}

			if (securityService.currentPersonId != comment.assignedTo?.id && comment.status in [PENDING, READY, STARTED]) {
				cols--
				actionBar << "<span id='assignToMeId_$comment.id' width='8%' nowrap='nowrap'>" <<
						HtmlUtil.actionButton('Assign To Me', 'ui-icon-person', comment.id,
						"assignTask('$comment.id','$comment.assignedTo', '$comment.status', 'taskManager')") << "</span>"
			}

			if (securityService.hasPermission(Permission.CommentView) && comment.status == READY &&
			    !(comment.category in AssetComment.moveDayCategories)) {

				actionBar << "<span id='1dEst_$comment.id' width='8%' nowrap='nowrap'>" <<
						HtmlUtil.actionButton('1 day', 'ui-icon-seek-next', comment.id, "changeEstTime('1','$comment.id',this.id)") << "</span>"
				actionBar << "<span id='2dEst_$comment.id' width='8%' nowrap='nowrap'>" <<
						HtmlUtil.actionButton('2 days', 'ui-icon-seek-next', comment.id, "changeEstTime('2','$comment.id',this.id)") << "</span>"
				actionBar << "<span id='7dEst_$comment.id' width='8%' nowrap='nowrap'>" <<
						HtmlUtil.actionButton('7 days', 'ui-icon-seek-next', comment.id, "changeEstTime('7','$comment.id',this.id)") << "</span>"
			}
		}
		else {
			log.warn "genActionBarHTML - invalid comment id ($params.id) from user $securityService.currentUsername"
			actionBar << '<span> An unexpected error occurred</span> '
		}

		actionBar << ' </span> '
		render actionBar.toString()
	}

	/**
	 * Used to generate action Bar for task details view
	 * @param asset comment id.
	 * @render : Action Bar JSON code.
	 */
	@HasPermission(Permission.TaskView)
	def genActionBarForShowViewJson() {
		AssetComment comment = fetchDomain(AssetComment, params)

		Project project = securityService.userCurrentProject
		def actionBar = []
		def includeDetails = params.includeDetails?params.includeDetails.toBoolean():false

		if (comment) {
			if (comment.project.id != project.id) {
				renderErrorJson('Task was not found')
				return
			}

			if (comment.status in [READY, STARTED]) {
				actionBar << [label: 'Start', icon: 'ui-icon-play', actionType: 'changeStatus', newStatus: STARTED,
				              redirect: 'taskManager', disabled: comment.status != READY]

				actionBar << [label: 'Done', icon: 'ui-icon-check', actionType: 'changeStatus', newStatus: COMPLETED,
				              redirect: 'taskManager', disabled: !(comment.status in [READY, STARTED])]
			}

			if (securityService.hasPermission(Permission.ActionInvoke)) {
				Map<String, ?> invokeActionDetails = comment.getInvokeActionButtonDetails()
				if (invokeActionDetails) {
					actionBar << invokeActionDetails
				}
			}

			if (securityService.hasPermission(Permission.ActionReset)) {
				if (comment.hasAction() && !comment.isAutomatic() && comment.status == HOLD) {
					actionBar << [
						label: message(code:'task.button.resetAction.label'),
						icon: 'ui-icon-power',
						actionType: 'resetAction', newStatus: READY,
						redirect:'taskManager',
						tooltipText: message(code:'task.button.resetAction.tooltip'),
						disabled: false]
				}
			}

			if (includeDetails) {
				actionBar << [label: 'Details...', icon: 'ui-icon-zoomin', actionType: 'showDetails']
			}

			if (HtmlUtil.isMarkupURL(comment.instructionsLink)) {
				actionBar << [label: HtmlUtil.parseMarkupURL(comment.instructionsLink)[0], icon: 'ui-icon-document',
				              actionType: 'viewInstructions', redirect: 'taskManager']
			}
			else {
				if(HtmlUtil.isURL(comment.instructionsLink)) {
					actionBar << [label: 'Instructions...', icon: 'ui-icon-document',
					              actionType: 'viewInstructions', redirect: 'taskManager']
				}
			}

			if (securityService.currentPersonId != comment.assignedTo?.id && comment.status in [PENDING, READY, STARTED]) {
				actionBar << [label: 'Assign To Me', icon: 'ui-icon-person', actionType: 'assignTask', redirect: 'taskManager']
			}

			if (securityService.hasPermission(Permission.CommentView) && comment.status == READY &&
			    !(comment.category in AssetComment.moveDayCategories)) {

				actionBar << [label: 'Delay for:']
				actionBar << [label: '1 day', icon: 'ui-icon-seek-next', actionType: 'changeEstTime', delay: '1']
				actionBar << [label: '2 day', icon: 'ui-icon-seek-next', actionType: 'changeEstTime', delay: '2']
				actionBar << [label: '7 day', icon: 'ui-icon-seek-next', actionType: 'changeEstTime', delay: '7']
			}

			if (TaskDependency.countByPredecessor(comment)) {
				actionBar << [label: 'Neighborhood', icon: 'tds-task-graph-icon', actionType: 'showNeighborhood']
			}

			renderSuccessJson(actionBar)
		}
		else {
			renderFailureJson(error: "Task was not found")
		}
	}

	/**
	* Used by the getActionBarHTML to wrap the button HTML into <td>...</td>
	*/
	@HasPermission(Permission.TaskView)
	String _actionButtonTd(tdId, button) {
		"""<td id="$tdId" width="8%" nowrap="nowrap">$button</td>"""
	}

	/**
	 * Generates a graph of the tasks in the neighborhood around a given task
	 * @param taskId
	 * @return redirect to URI of image or HTML showing the error
	 */
	@HasPermission(Permission.TaskGraphView)
	def neighborhoodGraphSvg() {
		def errorMessage = ''

		while (true) {
			def taskId = params.id
			if (! taskId || ! taskId.isNumber()) {
				errorMessage = "An invalid task id was supplied. Please contact support if this problem persists."
				break
			}

			Project project = securityService.userCurrentProject
			if (! project) {
				errorMessage = 'You must first select a project before view graphs'
				break
			}

			def rootTask = AssetComment.read(taskId)
			if (!rootTask || rootTask.project.id != project.id) {
				errorMessage = "Unable to find the specified task"
				if (rootTask)
					log.warn "SECURITY : User $securityService.currentUsername attempted to access graph for task ($taskId) not associated to current project ($project)"
				break
			}

			boolean viewUnpublished = params.viewUnpublished && params.viewUnpublished == "1"

			userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED,
				securityService.hasPermission(Permission.TaskPublish) && viewUnpublished)

			// check if the specified task is unpubublished and the user shouldn't see it
			if (!viewUnpublished && !rootTask.isPublished) {
				errorMessage = "Unable to find the specified task"
				break
			}

			def depList = taskService.getNeighborhood(taskId, 2, 5, viewUnpublished)
			if (depList.size() == 0) {
				errorMessage = "The task has no interdependencies with other tasks so a map wasn't generated."
				break
			}

			def now = new Date().format('yyyy-MM-dd H:m:s')
			def styleDef = "rounded, filled"

			def dotText = new StringBuilder()

			dotText << """
				#
				# TDS Runbook for Project $project, Task ${rootTask.toString().replaceAll(/[\n\r]/,'')}
				# Exported on $now
				# This is  .DOT file format of the project tasks
				#
				digraph runbook {
					graph [rankdir=LR, margin=0.001]
					node [ fontsize=10, fontname="Helvetica", shape="rect" style="$styleDef" ]
				
			""".stripIndent()

			def style = ''
			def fontcolor = ''
			def fontsize = ''
			def attribs
			def color
			def automatedTasks = []

			style = styleDef

			def tasks = []
			def taskList = []
			def roles = []

			// helper closure that outputs the task info in a dot node format
			def outputTaskNode = { task, rootId ->
				if (! tasks.contains(task.id) && (viewUnpublished || task.isPublished)) {
					tasks << task.id

					// string escaping: TM-3951, TM-5530, TM-6265
					def label = "${task.taskNumber}:${task.comment}"
					def tooltip = new String(label)
					label = (label.size() < 31) ? label : label[0..30]
					label = StringUtil.sanitizeDotString(label)
					tooltip = StringUtil.sanitizeDotString(tooltip)

					def colorKey = taskStatusColorMap.containsKey(task.status) ? task.status : 'ERROR'
					def fillcolor = taskStatusColorMap[colorKey][1]
					//def url = createLink(controller:'task', action:'neighborhoodGraph', id:task.id, absolute:false)

					// TODO - JPM - outputTaskNode() the following boolean statement doesn't work any other way which is really screwy
					if (task.isAutomatic()) {
						fontcolor = taskStatusColorMap['AUTO_TASK'][0]
						color = taskStatusColorMap['AUTO_TASK'][1]
						fontsize = '8'
						automatedTasks << task.id
					}
					else {
						fontcolor = taskStatusColorMap[colorKey][0]
						color = 'black'	// edge color
						fontsize = '10'
					}

					// Make the center root task stand out
					if (task.id.toString() == rootId) {
						style = "dashed, bold, filled"
					}
					else {
						style = styleDef
					}

					// add the task's role to the roles list
					def role = task.role ?: 'NONE'
					if (task.role && ! (role in roles))
						roles.push(role)

					taskList << task

					attribs = """id="$task.id", color="$color", fillcolor="$fillcolor", fontcolor="$fontcolor", fontsize="$fontsize" """

					dotText << """\t$task.taskNumber [label="$label", style="$style", $attribs, tooltip="$tooltip"];\n"""
				}
			}

			// helper closure to output the count node for the adjacent tasks
			def outputOuterNodeCount = { taskNode, isPred, count ->
				if (viewUnpublished || taskNode.isPublished) {
					log.info "neighborhoodGraph() outputing edge node $taskNode.taskNumber, Predecessor? ${isPred ? 'yes' : 'no' }"
					def cntNode = "C$taskNode.taskNumber"
					dotText << """\t$cntNode [id="placeholder" label="$count" tooltip="There are $count adjacent task(s)"];\n"""
					// dotText << "\t$cntNode [label=\"$count\" style=\"invis\" tooltip=\"There are $count adjacent task(s)\"];\n"
					if (isPred) {
						dotText << "\t$cntNode -> $taskNode.taskNumber;\n"
					}
					else {
						dotText << "\t$taskNode.taskNumber -> $cntNode;\n"
					}
				}
			}

			// Iterate over the task dependency list outputting the two nodes in the relationship. If it is an outer node
			depList.each { d ->
				outputTaskNode(d.successor, taskId)
				outputTaskNode(d.predecessor, taskId)

				dotText << "\t$d.predecessor.taskNumber -> $d.assetComment.taskNumber;\n"

				// Check for properties predecessorDepCount | successorDepCount to create the outer dependency count nodes
				if (d.metaClass.hasProperty(d, 'successorDepCount')) {
					outputOuterNodeCount(d.successor, false, d.successorDepCount)
				}
				else if (d.metaClass.hasProperty(d, 'predecessorDepCount')) {
					outputOuterNodeCount(d.predecessor, true, d.predecessorDepCount)
				}
			}

			dotText << "}\n"

			try {
				String svgText = graphvizService.generateSVGFromDOT("neighborhood-${taskId}", dotText.toString())
				def data = [svgText:svgText, roles:roles, tasks:taskList, automatedTasks: automatedTasks]
				render(text: data as JSON, contentType: 'application/json', encoding:"UTF-8")
				return false

			} catch(e) {
				errorMessage = 'Encountered an unexpected error while generating the graph'
				// TODO : Need to change out permission to ShowDebugInfo
				if (securityService.hasPermission(Permission.RoleTypeCreate)) {
					errorMessage += "<br><pre>$e.message</pre>"
				}
			}

			break
		} // while

		if (errorMessage) {
			response.status = 203	// Partial data
			response.contentType = 'text/html'
			render errorMessage
		}

	}

	/**
	 * Generates JSON object of the tasks in the neighborhood around a given task
	 * @param taskId
	 */
	@HasPermission(Permission.TaskGraphView)
	def neighborhood() {

		String currentProjectId = userPreferenceService.getCurrentProjectId()
		Person currentPerson = securityService.loadCurrentPerson()

		List<String> teams = partyRelationshipService.getProjectStaffFunctions(currentProjectId, currentPerson.id)?.id

		def taskId = params.id
		if (! taskId || ! taskId.isNumber()) {
			renderErrorJson("An invalid task id was supplied. Please contact support if this problem persists.")
			return
		}

		Project project = securityService.userCurrentProject
		if (! project) {
			renderErrorJson('You must first select a project before view graphs')
			return
		}

		def rootTask = Task.read(taskId)
		if (!rootTask || rootTask.project.id != project.id) {
			if (rootTask) {
				log.warn "SECURITY : User $securityService.currentUsername attempted to access graph for task ($taskId) not associated to current project ($project)"
			}
			renderErrorJson("Unable to find the specified task")
			return
		}

		boolean viewUnpublished = params.viewUnpublished && params.viewUnpublished == "1"

		userPreferenceService.setPreference(
				  PREF.VIEW_UNPUBLISHED,
				  securityService.hasPermission(Permission.TaskPublish) && viewUnpublished
		)

		// check if the specified task is unpubublished and the user shouldn't see it
		if (!viewUnpublished && !rootTask.isPublished) {
			renderErrorJson("Unable to find the specified task")
			return
		}

		def depList = taskService.getNeighborhood(taskId, 2, 5, viewUnpublished)
		if (depList.size() == 0) {
			renderErrorJson("The task has no interdependencies with other tasks so a map wasn't generated.")
			return
		}

		Map<String, Map> nodesMap = [:]
		def tasks = []

		// helper closure that creates the definition of a node
		def outputTaskNode = { AssetComment task, rootId ->
			if (! tasks.contains(task.id) && (viewUnpublished || task.isPublished)) {
				tasks << task.id

				Map taskMap = task.mapForGraphs()
				taskMap.isMyTask = task.isMyTask(currentPerson, teams)

				nodesMap[task.taskNumber] = [
						  task: taskMap,
						  successors: []
				]
			}
		}

		// Iterate over the task dependency list outputting the two nodes in the relationship. If it is an outer node
		depList.each { d ->
			outputTaskNode(d.successor, taskId)
			outputTaskNode(d.predecessor, taskId)

			nodesMap[d.predecessor.taskNumber].successors << d.assetComment.taskNumber
		}

		List<Map> nodes = nodesMap.collect { key, value -> value }
		renderSuccessJson(nodes)
	}

	/**
	 * Generates a graph of the Event Tasks
	 * @param moveEventId
	 * @param mode - flag as to what mode to display the graph as (s=status, ?=default)
	 * @return redirect to URI of image or HTML showing the error
	 */
	@HasPermission(Permission.TaskGraphView)
	def moveEventTaskGraphSvg() {
		def errorMessage

		// Create a loop that we can break out of as we need to
		while (true) {

			Project project = securityService.userCurrentProject
			if (!project) {
				errorMessage = "You must select a project before continuing"
				break
			}

			def moveEventId = params.moveEventId
			MoveEvent moveEvent = null
			if (! moveEventId || ! moveEventId.isNumber()) {
				errorMessage = "Please select an event to view the graph"
				break
			} else {
				moveEvent = fetchDomain(MoveEvent, [id: moveEventId], project)
				userPreferenceService.setMoveEventId(moveEventId)
				log.debug "**** $project.id / $moveEvent.project.id - $project / $moveEvent "
			}


			def mode = params.mode ?: ''
			if (mode && ! "s".contains(mode)) {
				mode = ''
				log.warn "The wrong mode [$mode] was specified"
			}

			def viewUnpublished = securityService.hasPermission(Permission.TaskPublish) && params.viewUnpublished == '1'
			userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED, viewUnpublished)

			jdbcTemplate.update('SET SESSION group_concat_max_len = 100000;')

			def query = """
				SELECT
					t.asset_comment_id AS id,
					t.task_number,
					CONVERT(GROUP_CONCAT(s.task_number SEPARATOR ',') USING 'utf8') AS successors,
					IFNULL(a.asset_name,'') AS asset,
					t.comment AS task,
					t.role,
					t.status,
					IFNULL(CONCAT(first_name,' ', last_name),'') AS hard_assign,
					t.is_published AS isPublished,
					t.duration,
					first_name,
					last_name
				FROM asset_comment t
				LEFT OUTER JOIN task_dependency d ON d.predecessor_id=t.asset_comment_id
				LEFT OUTER JOIN asset_comment s ON s.asset_comment_id=d.asset_comment_id
				${viewUnpublished ? '' : ' AND s.is_published=1 '}
				LEFT OUTER JOIN asset_entity a ON t.asset_entity_id=a.asset_entity_id
				LEFT OUTER JOIN person ON t.owner_id=person.person_id
				WHERE t.project_id=$project.id AND t.move_event_id=$moveEventId
				${viewUnpublished ? '' : ' AND t.is_published=1 '}
				GROUP BY t.task_number, t.asset_comment_id
			"""

				//  -- IF(t.hard_assigned=1,t.role,'') as hard_assign,
				//  -- IFNULL(t.est_start,'') AS est_start
			//log.debug "moveEventTaskGraphSvg() SQL for tasks: $query"

			def tasks = jdbcTemplate.queryForList(query)
			def roles = []
			tasks.each { t ->
				def role = t.role ?: 'NONE'
				if (t.role && ! (role in roles))
					roles.push(role)
			}

			if (tasks.size()==0) {
				errorMessage = 'No tasks were found for the selected move event'
				break
			}

			def now = new Date().format('yyyy-MM-dd H:m:s')

			def styleDef = "rounded, filled"

			def dotText = new StringBuilder()

			dotText << """#
# TDS Runbook for Project $project, Event $moveEvent.name
# Exported on $now
# This is  .DOT file format of the project tasks
#
digraph runbook {
	graph [rankdir=LR, margin=0.001];
	node [ fontsize=10, fontname="Helvetica", shape="rect" style="$styleDef" ]

"""

			def style = ''
			def fontcolor = ''
			def fontsize = ''
			def fillcolor
			def attribs
			def color
			def automatedTasks = []

			style = styleDef

			tasks.each {

				def task = "${it.task_number}:${it.task}"
			    def tooltip  = new String(task)

				def colorKey = taskStatusColorMap.containsKey(it.status) ? it.status : 'ERROR'

				fillcolor = taskStatusColorMap[colorKey][1]

				if(it.role == AUTOMATIC_ROLE ||
				   (Person.SYSTEM_USER_AT.firstName == it.firstName && Person.SYSTEM_USER_AT.lastName == it.lastName)){
					fontcolor = taskStatusColorMap['AUTO_TASK'][0]
					color = taskStatusColorMap['AUTO_TASK'][1]
					fontsize = '8'
					automatedTasks << it.id
				} else {
					fontcolor = taskStatusColorMap[colorKey][0]
					fontsize = '10'
					color = 'black'
				}

				// style = mode == 's' ? "fillcolor=\"${taskStatusColorMap[colorKey][1]}\", fontcolor=\"$fontcolor\", fontsize=\"$fontsize\", style=filled" : ''
				attribs = """id="$it.id", color="$color", fillcolor="$fillcolor", fontcolor="$fontcolor", fontsize="$fontsize" """

				//def url = createLink(controller:'task', action:'neighborhoodGraph', id:"$it.id", absolute:false)

				task = (task.size() > 35) ? task[0..34] : task

				// string escaping: TM-3951, TM-5530, TM-6265
				task = StringUtil.sanitizeDotString(task)
				tooltip = StringUtil.sanitizeDotString(tooltip)

				dotText << """\t$it.task_number [label="$task"  id="$it.id", style="$style", $attribs, tooltip="$tooltip"];\n"""
				def successors = it.successors

				if (successors) {
					successors = (successors as Character[]).join('')
					successors = successors.split(',')
					successors.each { s -> if (s) dotText << "\t$it.task_number -> $s;\n" }
				}
			}

			dotText << "}\n"

			try {
				String svgText = graphvizService.generateSVGFromDOT("runbook-${moveEventId}", dotText.toString())
				def data = [svgText:svgText, roles:roles, tasks:tasks, automatedTasks: automatedTasks]
				render(text: data as JSON, contentType: 'application/json', encoding:"UTF-8")
				return false

			} catch (e) {
				errorMessage = 'Encountered an unexpected error while generating the graph'
				// TODO : Need to change out permission to ShowDebugInfo
				if (securityService.hasPermission(Permission.RoleTypeCreate)) {
					errorMessage += "<br><pre>$e.message</pre>"
				}
			}

			break

		} // Outer while loop

		if (errorMessage) {
			response.status = 203	// Partial data
			response.contentType = 'text/html'
			render errorMessage
		}
	}



	/**
	 * Used to render neighborhood task graphs by passing the id argument to the taskGraph
	 */
	@HasPermission(Permission.TaskGraphView)
	def neighborhoodGraph() {
		forward action:'taskGraph', params: ['neighborhoodTaskId': params.id]
	}

	/**
	 * Generates the main view for Event Task and Neighborhood Task Graphs
	 */
	@HasPermission(Permission.TaskGraphView)
	def taskGraph() {
		licenseAdminService.checkValidForLicenseOrThrowException()
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		// Lookup a neighborhood task and adjust accordingly if the user passed a valid id
		AssetComment neighborTask
		def neighborhoodTaskId = NumberUtil.toPositiveLong(params.neighborhoodTaskId, -1)
		if (neighborhoodTaskId > 0) {
			neighborTask = AssetComment.get(neighborhoodTaskId)
			if (neighborTask) {
				// Make sure the user is trying to reference a task associated with their present project
				if (neighborTask.project.id != project.id) {
					securityService.reportViolation("Attempt to access task ($neighborhoodTaskId) not associated with project $project.projectCode")
					neighborhoodTaskId = -1
					neighborTask = null
				}
			}
		}

		// if user used the event selector on the page, update their preferences with the new event
		// TODO : JPM 2/2016 Refactor this into a method
		Long meId
		if (params.moveEventId) {
			meId = NumberUtil.toPositiveLong(params.moveEventId, -1)
			if (meId > 0) {
				MoveEvent me = MoveEvent.get(meId)
				if (me) {
					if (me.project.id == project.id) {
						userPreferenceService.setPreference(PREF.MOVE_EVENT, params.moveEventId)
					} else {
						securityService.reportViolation("Attempt to reference event id ($meId) not associated with project $project.projectCode")
						meId = 0
					}
				}
			}
		}

		String viewUnpublished = userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) == 'true' ? '1' : '0'

		List eventList = MoveEvent.findAllByProject(project)
		def selectedEventId = 0

		String eventPref = userPreferenceService.getPreference(PREF.MOVE_EVENT) ?: '0'
		Long eventPrefId = NumberUtil.toPositiveLong(eventPref)

		// Ask if there's an event id first to avoid iterating unnecessarily over all the events.
		if (eventPrefId > 0 && !(eventList.find {it.id == eventPrefId})) {
			// The user preference references an invalid event so we should clear it out
			eventPref = "0"
			userPreferenceService.removePreference(PREF.MOVE_EVENT)
		}

		// Determine what the Selected Event should be with the following rules
		//    1. If user clicked on a neighborhood task, get the event from that task. If the task is not assigned to an event - try next
		//    2. Otherwise if the user specified an event use it
		//	  3. Lastly use the user's saved preference
		if (neighborTask) {
			selectedEventId = neighborTask.moveEvent?.id
		}
		if (! selectedEventId) {
			selectedEventId = meId ?: eventPref
		}

		[moveEvents: eventList, selectedEventId: selectedEventId,
		 neighborhoodTaskId: neighborhoodTaskId, viewUnpublished: viewUnpublished]
	}

	/**
	 * Used in MyTask to set user preference for printername and quantity, we can get a key-value or
	 * a json with a list of permissions to Change
	 * @param preference - Key
	 * @param value
	 */
	@HasPermission(Permission.TaskView)
	def setLabelQuantityPref() {
		Map preferencesMap = [:]
		SetLabelQuantityPrefCommand commandObject = populateCommandObject(SetLabelQuantityPrefCommand, false)
		if (commandObject.hasErrors()) {
			sendInvalidInput( renderAsJson( GormUtil.validateErrorsI18n(commandObject) ) )
			return
		}
	/*  If there are json properties present, set them in preferenceMap */
		preferencesMap = commandObject.preferenceMap
	/* If there are values sent in key-value format, set them in preferenceMap */
		if(preferencesMap.value) {
			preferencesMap[params.preference] = params.value
		}
		preferencesMap.each { key, value ->
			userPreferenceService.setPreference(key, value)
		}
		render true
	}

	/**
	 * Used in Task Manager auto open action bar which status is ready or started.
	 * @param : id[] : list of id whose status is ready or started
	 * @return : map consist of id of task and action bar
	 */
	@HasPermission(Permission.TaskView)
	def genBulkActionBarHTML() {
		def taskIds =  params.list("id[]")
		def resultMap = [:]
		taskIds.each {
			def comment = AssetComment.read(it)
			if(comment) {
				resultMap[it] = getActionBarData(comment).toString()
			}
		}
		render resultMap as JSON
	}

	/**
	 * Used in Task Manager action bar to change estTime.
	 * @param : day : 1, 2 or 7 days.
	 * @param : commentId.
	 * @return : retMap.
	 */
	@HasPermission(Permission.TaskEdit)
	def changeEstTime(Long commentId, Integer day) {
		Map<String, String> retMap = [etext: '', estStart: '', estFinish: '']
		try {
			AssetComment comment = taskService.changeEstTime(commentId, day)
			retMap['estStart'] = TimeUtil.formatDateTime(comment?.estStart)
			retMap['estFinish'] = TimeUtil.formatDateTime(comment?.estFinish)
		} catch (EmptyResultException | InvalidParamException e) {
			retMap['etext'] = e.message
		}
		render retMap as JSON
	}

	@HasPermission(Permission.TaskTimelineView)
	def taskTimeline() {
		licenseAdminService.checkValidForLicenseOrThrowException()
		Project project = controllerService.getProjectForPage(this, 'to use the task timeline')
		if (!project) return

		// if user used the event selector on the page, update their preferences with the new event
		if (params.moveEventId && params.moveEventId.isLong()) {
			userPreferenceService.setPreference(PREF.MOVE_EVENT, params.moveEventId)
		}

		// handle move events
		def moveEvents = MoveEvent.findAllByProject(project)
		def eventPref = userPreferenceService.getPreference(PREF.MOVE_EVENT) ?: '0'
		long selectedEventId = eventPref.isLong() ? eventPref.toLong() : 0

		// handle the view unpublished checkbox
		def viewUnpublished = userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) == 'true' ? '1' : '0'

		return [moveEvents:moveEvents, selectedEventId:selectedEventId, viewUnpublished:viewUnpublished]
	}

	@HasPermission([Permission.TaskCreate, Permission.TaskEdit])
	def editTask() {
		Project project = controllerService.getProjectForPage(this)
		if (! project) return
		def apiActionList = apiActionService.list(project, true,[producesData:0] )

		render(view: "_editTask", model: [apiActionList: apiActionList, categories: assetOptionsService.taskCategories()])
	}

	@HasPermission(Permission.TaskView)
	def actionLookUp(Long apiActionId, Long commentId) {
		Project project = securityService.userCurrentProject
		render(view: "_actionLookUp", model: [apiAction: taskActionService.actionLookup(commentId, project)])
	}

	@HasPermission(Permission.TaskView)
	def showTask() {
		//def instructionsLink = AssetComment.read(params.taskId)?.instructionsLink
		//log.error instructionsLink
		render(view: "_showTask", model: [])
	}

	/**
	 * Endpoint that returns a list of tasks matching the filters provided. Some of available parameters are:
	 * - projectId - project (id)
	 * - eventId - event (id)
	 * - justMyTasks (1, Y)
	 * - justRemaining (1, Y)
	 * - justActionable (1, Y)
	 * - viewUnpublished (1, Y)
	 * @return a list with a Map representation of each task.
	 */
	@HasPermission(Permission.TaskView)
	def list() {
		// This will contain a reference to, either the user's project, or the project specified as
		// a parameter (given that they have access to it)
		Project project = getProjectForWs()

		// If the params map has an event, validate that it exists and belongs to the project.
		String eventIdParam = 'eventId'
		Long eventId = params.long(eventIdParam)
		if (eventId) {
			// We don't need the reference, just to validate that it exists and fail if it doesn't.
			GormUtil.findInProject(project, MoveEvent, eventId, true)
			params.put('moveEvent', eventId)
			params.remove(eventIdParam)
		}

		Map results = commentService.filterTasks(project, params)
		List<Map> tasks = results.tasks.collect { AssetComment task ->
			task.taskToMap()
		}
		renderSuccessJson(tasks)
	}

	/**
	 * Get task roles
	 */
	@HasPermission(Permission.TaskView)
	def retrieveStaffRoles() {
		renderSuccessJson(taskService.getRolesForStaff())
	}

	/**
	 * Generates the user's list of tasks for the current project
	 * params:
	 *		tab - all or todo
	 */
	@HasPermission(Permission.TaskView)
	def listUserTasks() {
		licenseAdminService.checkValidForLicenseOrThrowException()
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		//log.error "PROJECT: $project"
		Person person = securityService.userLoginPerson
		//log.error "PERSON=$person"
		def entities = assetEntityService.entityInfo(project)
		// If the request is being made as a redirect for a previous task update that was being completed, we need to sleep a moment
		// to allow the Quartz job that updates successors to finish so that when the user sees the new results that it may have successors
		// there were updated by the previous update.
		if (params.containsKey('sync')) {
			log.info "listUserTasks - sync'n 500 ms for $person on project $project.id"
			sleep(500)
			log.info "listUserTasks - sunk for $person on project $project.id"
		}

		log.debug "listUserTasks: params=$params, project=$project, person=$person, entities=${entities.size()}"

		def allTasks = false

		// Parameters
		def tab
		def taskList

		// Deal with the user preferences
		def viewMode = params.viewMode
		def search = params.search

		if (viewMode) {
			session.setAttribute('TASK_VIEW_MODE', viewMode)
		}
		// log.info "listComment() sort=$params.sort, order=$params.order"

		def isCleaner = partyRelationshipService.staffHasFunction(project, person.id, RoleType.CODE_TEAM_CLEANER)
		def isMoveTech = partyRelationshipService.staffHasFunction(project, person.id, RoleType.CODE_TEAM_MOVE_TECH)

		if (params.event) {
			userPreferenceService.setPreference(PREF.MYTASKS_MOVE_EVENT_ID,
				params.event == 'null' ? "_null" : params.event)
		}

		def moveEventId = userPreferenceService.getPreference(PREF.MYTASKS_MOVE_EVENT_ID)
		if (moveEventId == "_null") {
			moveEventId = null
		}
		def moveEvent = MoveEvent.get(moveEventId)

		// Use the taskService.getUserTasks service to get all of the tasks [all,todo]
		def tasks = taskService.getUserTasks(project, false, 7, params.sort, params.order, search, moveEvent)

		// Get the size of the lists
		def todoSize = tasks['todo'].size()
		def allSize = tasks['all'].size()

		// Based on which tab the user is viewing we'll set taskList to the appropriate list to be returned to the user
		if (params.tab=='none' && params.id != null) {
			tab = 'all'
			allTasks = true
			def taskId = NumberUtils.toLong(params.id)
			taskList = tasks['all'].findAll { it.id == taskId }
		} else {
			if (params.tab=='all') {
				tab = 'all'
				taskList = tasks['all']
				allTasks = true
			} else {
				tab = 'todo'
				taskList = tasks['todo']
			}
		}

		// Build the list and associate the proper CSS style
		def issueList = []
		taskList.each { task ->
			def css = taskService.getCssClassForStatus(task.status)
			issueList << [item: task, css: css]
		}
		def timeToRefresh =  userPreferenceService.getPreference(PREF.MYTASKS_REFRESH)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		def moveEventList = MoveEvent.findAllByProject(project,[sort:'name'])

		// Determine the model and view
		def model = [
			taskList:issueList,
			tab:tab,
			todoSize:todoSize,
			allSize:allSize,
			search:search,
			sort:params.sort,
			order:params.order,
	 		personId: person.id,
	 		isCleaner: isCleaner,
	 		isMoveTech:isMoveTech,
			timeToUpdate: timeToRefresh ?: 60,
			networks: entities.networks,
			assetDependency: new AssetDependency(),
			dependencyType: entities.dependencyType,
			dependencyStatus: entities.dependencyStatus,
			moveBundleList: moveBundleList,
			moveEventList: moveEventList,
			moveEvent: moveEvent,
			selectedTaskId: params.id,
			categories: assetOptionsService.taskCategories()
		]

		if (search && taskList) {
			model.searchedAssetId = taskList*.id[0]
			model.searchedAssetStatus = taskList*.status[0]
		}

		def view = params.view == "myTask" ? "_tasks" : "myIssues"
		model.timers = session.MY_ISSUE_REFRESH?.MY_ISSUE_REFRESH
		if (request.getHeader ("User-Agent").contains ("MSIE")) {
			model.isOnIE = true
		}

		log.debug "listUserTasks: View is $view"

		// Send the user on his merry way
		render(view:view, model:model)
	}

	/**
	 * @author Ross Macfarlane
	 * @return JSON response containing the number of tasks assigned to the current user {count:#}
	 */
	@HasPermission(Permission.TaskView)
	def retrieveUserToDoCount() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def tasksStats = taskService.getUserTasks(project, true)
		// log.info "retrieveToDoCount: tasksStats=$tasksStats"
		def map = [count: tasksStats['todo']]
		render map as JSON
	}

	/**
	 * Used in the MyTasks view to display the details of a Task/Comment
	 * @param issueId
	 * @return HTML that is used by an AJax call
	 */
	@HasPermission(Permission.TaskView)
	def showIssue() {

		Project project = securityService.userCurrentProject

		// This is such a hack at the moment but if this errors, the mobile scanner doesn't have any way to get back to the previous screen
		// so it is a painful experience to close the app, kill the app, restart, login and then get back to the original screen.
		def backButton = 'Please press the Back button to return to the previous screen.<p/><button onclick="goBack()">Back</button>'
		def backScript = """
<script>
function goBack() { window.history.back() }
</script>
"""

		log.debug "showIssue: params=$params, project=$project"

		AssetComment assetComment = AssetComment.findByIdAndProject(params.issueId, project)
		if (! assetComment) {
			render "${backScript}Unable to locate a task for asset [$params.search/$params.issueId]. $backButton"
			return
		}


		def cartQty = '	'
		def moveEvent = assetComment.moveEvent

		// Determine the cart quantity
		// The quantity only appears on the last label scanned/printed for a particular cart. This is used to notify
		// the logistics and transport people that the cart is ready to wrap up.
		if (moveEvent && assetComment.assetEntity?.cart && assetComment.role == RoleType.CODE_TEAM_CLEANER && assetComment.status != COMPLETED) {
			def cart = taskService.getCartQuantities(moveEvent, assetComment.assetEntity.cart)
			if (cart && (cart.total - cart.done) == 1) {
				// Only set the cartQty if we're printing the LAST set of labels for a cart (done is 1 less than total)
				cartQty = cart.total
			}
		}
		// log.info "cartQty ($cartQty)"

		String selectCtrlId = 'assignedToEditId_' + assetComment.id
		def assignToSelect = taskService.assignToSelectHtml(project.id, params.issueId, assetComment.assignedTo?.id, selectCtrlId)

		// Bounce back to the user if we didn't get a legit id, associated with the project
		if (! assetComment) {
			log.error "$securityService.currentUsername attempted an invalide access a task/comment with id $params.issueId on project $project"
			render "${backScript}Unable to find specified record. $backButton"
			return
		}

		def isCleaner = partyRelationshipService.staffHasFunction(project, securityService.currentPersonId, RoleType.CODE_TEAM_CLEANER)
		def canPrint = isCleaner

		def noteList = assetComment.notes.sort{it.dateCreated}
		def notes = []
		noteList.each {
			def dateCreated = TimeUtil.formatDateTime(it.dateCreated, TimeUtil.FORMAT_DATE_TIME_3)
			notes << [dateCreated , it.createdBy.toString(), it.note]
		}

		// Determine if the user should be able to edit the task. The rules are:
		// 1. If ADMIN, CLIENT_ADMIN or CLIENT_MGR can always edit
		// 2. Can ALWAYS add a NOTE.
		// 3. Change person - when task is in the PENDING/READY status?

		boolean assignmentPerm
		boolean categoryPerm = false

		if (securityService.hasRole([ROLE_ADMIN, ROLE_CLIENT_ADMIN, ROLE_CLIENT_MGR])) {
			assignmentPerm = categoryPerm = true
		} else {
			// AssignmentPerm can be changed if task is not completed/terminated
			assignmentPerm = ![COMPLETED, TERMINATED].contains(assetComment.status)
		}

		def dueDate = TimeUtil.formatDate(assetComment.dueDate)

		def successor = TaskDependency.findAllByPredecessor(assetComment)
		def projectStaff = partyRelationshipService.getProjectStaff(project.id)*.staff.sort { it.firstName }

		// Retrieve the custom field specs if there's an asset associated with the AssetComment
		List customs = []
		if (assetComment?.assetEntity) {
			String domain = assetComment.assetEntity?.assetClass.toString()
			customs = customDomainService.fieldSpecs(project, domain, CustomDomainService.CUSTOM_USER_FIELD, ["field"])
		}


		def model = [assetComment: assetComment, notes: notes, permissionForUpdate: true,
		             statusWarn: taskService.canChangeStatus(assetComment) ? 0 : 1, assignmentPerm: assignmentPerm,
		             categoryPerm: categoryPerm, successor: successor, projectStaff: projectStaff, canPrint: canPrint,
		             dueDate: dueDate, assignToSelect: assignToSelect, assetEntity: assetComment.assetEntity,
		             cartQty: cartQty, project: project, customs: customs,
					 categories: assetOptionsService.taskCategories()
		]
		if (isCleaner) {
			model.lblQty = userPreferenceService.getPreference(PREF.PRINT_LABEL_QUANTITY) ?: PREF.DEFAULT_VALUES[PREF.PRINT_LABEL_QUANTITY]
			model.prefPrinter = userPreferenceService.getPreference(PREF.PRINTER_NAME)
		}

		render(view: isCleaner ? '_showCleanerTask' : 'showIssue', model: model)
	}

	/**
	 * Used in a email to display the details of a Task/Comment
	 * @param task/comment id
	 * @return HTML that is used to display a task form an email
	 */
	@HasPermission(Permission.TaskView)
	def userTask() {
		def task
		if (params.id != null) {
			task = AssetComment.get(params.id)
			if (task) {
				controllerService.switchContextToProject(task.project)
			}
		}

		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		boolean isValidUser = task && task.assignedTo && task.assignedTo.id == securityService.currentPersonId
		if (!isValidUser) {
			if (task && task.taskNumber) {
				flash.message = "You don't have permissions to access this task $task.taskNumber"
			} else {
				flash.message = "Task not found"
			}
		}
		params.tab='none'
		listUserTasks()
	}
}
