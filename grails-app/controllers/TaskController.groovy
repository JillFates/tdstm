import grails.converters.JSON

import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.GormUtil
import org.apache.commons.lang.StringEscapeUtils
import com.tdssrc.grails.TimeUtil
import com.tds.asset.TaskDependency
import org.apache.commons.lang.math.NumberUtils
import java.text.SimpleDateFormat

class TaskController {
	
	def securityService
	def runbookService
	def commentService
	def taskService
	def userPreferenceService
    def jdbcTemplate
    def reportsService

    def index = { }
	
	/**
	* Used by the myTasks and Task Manager to update tasks appropriately.
	*/
	def update = {
		def map = commentService.saveUpdateCommentAndNotes(session, params, false, flash)

		if (params.view == 'myTask') {		
			if (map.error) {
				flash.message = map.error
			}
		
			def redirParams = [view:params.view]
			if (params.containsKey('tab') && params.tab) {
				redirParams << [tab:params.tab]
			}
			if (params.containsKey('sort') && params.sort) {
				redirParams << [sort:params.sort]
			}
			if (params.status == AssetCommentStatus.DONE) {
				redirParams << [sync:1]
			}
			forward(controller:'clientTeams', action:'listTasks', params:redirParams)			
		} else {
			// Coming from the Task Manager
			render map as JSON
		}
	}
		
	/**
	 * Used to assign assignTo through ajax call from MyTasks
	 * @params : id, status
	 * @return : user full name and errorMessage if status changed by accident.
	 */
	def assignToMe = {
		def task = AssetComment.get(params.id)
		def userLogin = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()
		def commentProject = task.project
		def errorMsg = ''
		def assignedTo=''
		
		if (task) {
			if (commentProject.id != project.id) {
				log.error "assignToMe - Task(#${task.taskNumber} id:${task.id}/${commentProject}) not associated with user(${userLogin}) project (${project})"
				errorMsg = "It appears that you do not have permission to change the specified task"
			} else {
				
				// Double check to see if the status changed while the user was reassigning so that they 
				if (! errorMsg && params.status) {
					if (task.status != params.status) {
						log.warn "assignToMe - Task(#:${task.taskNumber} id:${task.id}) status changed around when ${userLogin} was assigning to self"
						def whoDidIt = (task.status == AssetCommentStatus.DONE) ? task.resolvedBy : task.assignedTo
						switch (task.status) {
							case AssetCommentStatus.STARTED:
								errorMsg = "The task was STARTED by ${whoDidIt}"; break
							case AssetCommentStatus.DONE:
								errorMsg = "The task was COMPLETED by ${whoDidIt}"; break
							default:
								errorMsg = "The task status was changed to '${task.status}'"
						}
					}
				}				
				
				if (! errorMsg ) {
					// If there were no errors then try reassign the Task
					def belongedTo = task.assignedTo ? task.assignedTo.toString() : 'Unassigned'
					task.assignedTo = userLogin.person
					if (task.save(flush:true)){
						assignedTo = userLogin.person.toString()
						if (task.isRunbookTask()) taskService.addNote( task, userLogin.person, "Assigned task to self, previously assigned to $belongedTo")					
					} else {
						log.error "assignToMe - Task(#:${task.taskNumber} id:${task.id}) failed while trying to reassign : " + GormUtil.allErrorsString(task)
						errorMsg = "An unexpected error occured while assigning the task to you."
					}
				}
			}
		} else {
			errorMsg = "Task Not Found : Was unable to find the Task for the specified id - ${params.id}"
		}
		
		def map = [assignedTo:assignedTo, errorMsg:errorMsg]
		render map as JSON
	}
	
	/**
	 *  Generate action bar for a selected comment in Task Manager
	 *  @params id - the task (aka AssetComment) id number for the task bark
	 *  @return : actions bar as HTML (Start, Done, Details, Assign To Me)
	 */
	def genActionBarHTML = {
		def comment = AssetComment.get(params.id)
		def actionBar = getActionBarData(comment); 
		render actionBar.toString()
	}
	
	/**
	 * Used to generate action Bar for task  
	 * @param comment : instance of asset comment
	 * @return : Action Bar HTML code.
	 */
	def getActionBarData (comment){
		// There are a total of 13 columns so we'll subtract for each conditional button
		def cols=12
		def userLogin = securityService.getUserLogin()
		
		StringBuffer actionBar = new StringBuffer("""<table style="border:0px"><tr>""")
		if (comment) {
			if(comment.status ==  AssetCommentStatus.READY){
				cols--
				actionBar.append( _actionButtonTd(	"startTdId_${comment.id}",
					HtmlUtil.actionButton('Start', 'ui-icon-play', comment.id, 
						"changeStatus('${comment.id}','${AssetCommentStatus.STARTED}','${comment.status}', 'taskManager')")))
			}
		
			if (comment.status in[ AssetCommentStatus.READY, AssetCommentStatus.STARTED]){
				cols--
				actionBar.append( _actionButtonTd("doneTdId_${comment.id}",
					HtmlUtil.actionButton('Done', 'ui-icon-check', comment.id, 
						"changeStatus('${comment.id}','${AssetCommentStatus.DONE}', '${comment.status}', 'taskManager')")))
			}
		
			actionBar.append( 
				_actionButtonTd("assignToMeId_${comment.id}", 
					HtmlUtil.actionButton('Details...', 'ui-icon-zoomin', comment.id, "showAssetComment(${comment.id},'show')")))
		
			if (userLogin.person.id != comment.assignedTo?.id && comment.status in [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED]){
				cols--
				actionBar.append( _actionButtonTd("assignToMeId_${comment.id}", 
					HtmlUtil.actionButton('Assign To Me', 'ui-icon-person', comment.id,
						"assignTask('${comment.id}','${comment.assignedTo}', '${comment.status}', 'taskManager')")))
			}
			def hasDelayPrem = RolePermissions.hasPermission("CommentCrudView")
			if(hasDelayPrem && comment.status ==  AssetCommentStatus.READY && !(comment.category in AssetComment.moveDayCategories)){
				actionBar.append('<td class="delay_taskManager"><span>Delay for:</span></td>')
				actionBar.append( _actionButtonTd(	"1dEst_${comment.id}",
					HtmlUtil.actionButton('1 day', 'ui-icon-seek-next', comment.id,"changeEstTime('1','${comment.id}',this.id)")))
				actionBar.append( _actionButtonTd(	"2dEst_${comment.id}",
					HtmlUtil.actionButton('2 days', 'ui-icon-seek-next', comment.id,"changeEstTime('2','${comment.id}',this.id)")))
				actionBar.append( _actionButtonTd(	"7dEst_${comment.id}",
					HtmlUtil.actionButton('7 days', 'ui-icon-seek-next', comment.id,"changeEstTime('7','${comment.id}',this.id)")))
			} 
		}else {
			log.warn "genActionBarHTML - invalid comment id (${params.id}) from user ${userLogin}"
			actionBar.append('<td>An unexpected error occurred</td>')
		}

		actionBar.append(""" <td colspan='${cols}'>&nbsp;</td>
			</tr></table>""")
		
		return actionBar
	}
	/**
	 * Used to generate action Bar for task details view 
	 * @param asset comment id.
	 * @render : Action Bar HTML code.
	 */
	def genActionBarForShowView = {
		def comment = AssetComment.get(params.id)
		StringBuffer actionBar = new StringBuffer("""<span class="slide" style=" margin-top: 4px;">""")
		def cols=12
		def userLogin = securityService.getUserLogin()
		
		if (comment) {
			if(comment.status ==  AssetCommentStatus.READY){
				cols--
				actionBar.append( "<span id='startTdId_${comment.id}' width='8%' nowrap='nowrap'>"+
					HtmlUtil.actionButton('Start', 'ui-icon-play', comment.id, "changeStatus('${comment.id}','${AssetCommentStatus.STARTED}','${comment.status}', 'taskManager')")+
					"</span>")
			}
			
			if (comment.status in[ AssetCommentStatus.READY, AssetCommentStatus.STARTED]){
				cols--
				actionBar.append("<span id='doneTdId_${comment.id}' width='8%' nowrap='nowrap'>"+
					HtmlUtil.actionButton('Done', 'ui-icon-check', comment.id,
						"changeStatus('${comment.id}','${AssetCommentStatus.DONE}', '${comment.status}', 'taskManager')")+
					"</span>")
			}
		
			if (userLogin.person.id != comment.assignedTo?.id && comment.status in [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED]){
				cols--
				actionBar.append( "<span id='assignToMeId_${comment.id}' width='8%' nowrap='nowrap'>"+
					HtmlUtil.actionButton('Assign To Me', 'ui-icon-person', comment.id,
						"assignTask('${comment.id}','${comment.assignedTo}', '${comment.status}', 'taskManager')")+
					"</span>")
			}
			def hasDelayPrem = RolePermissions.hasPermission("CommentCrudView")
			if(hasDelayPrem && comment.status ==  AssetCommentStatus.READY && !(comment.category in AssetComment.moveDayCategories)){
				actionBar.append( "<span id='1dEst_${comment.id}' width='8%' nowrap='nowrap'>"+
					HtmlUtil.actionButton('1 day', 'ui-icon-seek-next', comment.id,"changeEstTime('1','${comment.id}',this.id)")+"</span>")
				actionBar.append( "<span id='2dEst_${comment.id}' width='8%' nowrap='nowrap'>"+
					HtmlUtil.actionButton('2 days', 'ui-icon-seek-next', comment.id,"changeEstTime('2','${comment.id}',this.id)")+"</span>")
				actionBar.append("<span id='7dEst_${comment.id}' width='8%' nowrap='nowrap'>"+
					HtmlUtil.actionButton('7 days', 'ui-icon-seek-next', comment.id,"changeEstTime('7','${comment.id}',this.id)")+"</span>")
			}
			
		}else {
			log.warn "genActionBarHTML - invalid comment id (${params.id}) from user ${userLogin}"
			actionBar.append('<span> An unexpected error occurred</span> ')
		}

		actionBar.append(""" </span> """)
		render actionBar.toString()
	}
	/**
	* Used by the getActionBarHTML to wrap the button HTML into <td>...</td>
	*/
	def _actionButtonTd(tdId, button) {
		return """<td id="${tdId}" width="8%" nowrap="nowrap">${button}</td>"""	
	}
	
	/**
	 * Generates a graph of the tasks in the neighborhood around a given task
	 * @param taskId
	 * @return redirect to URI of image or HTML showing the error
	 */
	def neighborhoodGraph = {
		
		def taskId=params.id
		if (! taskId || ! taskId.isNumber()) {
			render "An invalid task id was supplied. Please contact support if this problem persists."
			return
		}	
		def project = securityService.getUserCurrentProject()
		def rootTask = AssetComment.findByIdAndProject(taskId, project) {
			render "Sorry but the task not found. Please contact support if this problem persists."
			return
		}

		def depList = taskService.getNeighborhood(taskId, 2, 5)
		if (depList.size() == 0) {
			render "The task has no interdependencies with other tasks so a map wasn't generated."
			return
		}

		def now = new Date().format('yyyy-MM-dd H:m:s')
		def styleDef = "rounded, filled"

		def dotText = new StringBuffer()

		dotText << """#
# TDS Runbook for Project ${project}, Task ${rootTask}
# Exported on ${now}
# This is  .DOT file format of the project tasks
#
digraph runbook {
	graph [rankdir=LR, margin=0.001];
	node [ fontsize=10, fontname="Helvetica", shape="rect" style="${styleDef}" ]
  
"""
	
		def style=''
		def fontcolor=''
		def fontsize=''
		def attribs
		def color

		style = styleDef

		def tasks = []

		// helper closure that outputs the task info in a dot node format
		def outputTaskNode = { task, rootId ->
			if (! tasks.contains(task.id)) {
				tasks << task.id

			    def label = "${task.taskNumber}:" + org.apache.commons.lang.StringEscapeUtils.escapeHtml(task.comment).replaceAll(/\n/,'').replaceAll(/\r/,'')
			    label = (label.size() < 31) ? label : label[0..30]

			    def tooltip  = "${task.taskNumber}:" + org.apache.commons.lang.StringEscapeUtils.escapeHtml(task.comment).replaceAll(/\n/,'').replaceAll(/\r/,'')
				def colorKey = taskService.taskStatusColorMap.containsKey(task.status) ? task.status : 'ERROR'
				def fillcolor = taskService.taskStatusColorMap[colorKey][1]
				def url = HtmlUtil.createLink([controller:'task', action:'neighborhoodGraph', id:task.id, absolute:false])

				// TODO - JPM - outputTaskNode() the following boolean statement doesn't work any other way which is really screwy
				if ( "${task.role == AssetComment.AUTOMATIC_ROLE ? 'yes' : 'no'}" == 'yes' ) {
					fontcolor = taskService.taskStatusColorMap['AUTO_TASK'][0] 
					color = taskService.taskStatusColorMap['AUTO_TASK'][1]
					fontsize = '8'
				} else {
					fontcolor = taskService.taskStatusColorMap[colorKey][0]
					color = 'black'	// edge color
					fontsize = '10'
				}

				// Make the center root task stand out
				if ("${task.id}" == rootId) {
					style = "dashed, bold, filled"
				} else {
					style = styleDef
				}

				attribs = "color=\"${color}\", fillcolor=\"${fillcolor}\", fontcolor=\"${fontcolor}\", fontsize=\"${fontsize}\""

				dotText << "\t${task.taskNumber} [label=\"${label}\" URL=\"$url\", style=\"$style\", $attribs, tooltip=\"${tooltip}\"];\n"

			}

		}

		// helper closure to output the count node for the adjacent tasks
		def outputOuterNodeCount = { taskNode, isPred, count ->
			log.info "neighborhoodGraph() outputing edge node ${taskNode.taskNumber}, Predecessor? ${isPred?'yes':'no'}"
			def cntNode = "C${taskNode.taskNumber}"
			dotText << "\t$cntNode [label=\"$count\" tooltip=\"There are $count adjacent task(s)\"];\n" 
			// dotText << "\t$cntNode [label=\"$count\" style=\"invis\" tooltip=\"There are $count adjacent task(s)\"];\n" 
			if (isPred) {
				dotText << "\t$cntNode -> ${taskNode.taskNumber};\n"				
			} else {
				dotText << "\t${taskNode.taskNumber} -> $cntNode;\n"
			}
		}

		// Iterate over the task dependency list outputting the two nodes in the relationship. If it is an outer node 
		depList.each() { d ->
			outputTaskNode(d.successor, taskId)
			outputTaskNode(d.predecessor, taskId)

			dotText << "\t${d.predecessor.taskNumber} -> ${d.assetComment.taskNumber};\n"

			// Check for properties predecessorDepCount | successorDepCount to create the outer dependency count nodes
			if (d.metaClass.hasProperty(d, 'successorDepCount')) {
				outputOuterNodeCount(d.successor, false, d.successorDepCount)
			} else if (d.metaClass.hasProperty(d, 'predecessorDepCount')) {
				outputOuterNodeCount(d.predecessor, true, d.predecessorDepCount)
			}
		}

		dotText << "}\n"
		
		try {
			def uri = reportsService.generateDotGraph("neighborhood-$taskId", dotText.toString() )
			redirect(uri:uri)
		} catch(e) {
			render "<pre>${e.getMessage()}</pre>"
		}				
	}

	/**
	 * Generates a graph of the Event Tasks
	 * @param moveEventId
	 * @param mode - flag as to what mode to display the graph as (s=status, ?=default)
	 * @return redirect to URI of image or HTML showing the error
	 */
	def moveEventTaskGraph = {
		
		def project = securityService.getUserCurrentProject()
		def moveEventId=params.moveEventId	
		if (! moveEventId || ! moveEventId.isNumber()) {
			render "Invalid move event id supplied"
			return
		}
		
		def moveEvent = MoveEvent.findByIdAndProject(moveEventId, project)
		if (! moveEvent) {
			render "Move event not found"
			return
		}
		
		def mode = params.mode ?: ''
		if (mode && ! "s".contains(mode)) {
			mode = ''
			log.warn "The wrong mode [$mode] was specified"
		}
		
		def projectId = project.id
		
		def query = """
			SELECT 
			  t.asset_comment_id AS id,
			  t.task_number, 
			  CONVERT( GROUP_CONCAT(s.task_number SEPARATOR ',') USING 'utf8') AS successors,
			  IFNULL(a.asset_name,'') as asset, 
			  t.comment as task, 
			  t.role,
			  t.status,
			  IFNULL(CONCAT(first_name,' ', last_name),'') as hard_assign,
			  t.duration
			FROM asset_comment t
			LEFT OUTER JOIN task_dependency d ON d.predecessor_id=t.asset_comment_id
			LEFT OUTER JOIN asset_comment s ON s.asset_comment_id=d.asset_comment_id
			LEFT OUTER JOIN asset_entity a ON t.asset_entity_id=a.asset_entity_id
			LEFT OUTER JOIN person ON t.owner_id=person.person_id
			WHERE t.project_id=${projectId} AND t.move_event_id=${moveEventId}
			GROUP BY t.task_number
			"""

			//  -- IF(t.hard_assigned=1,t.role,'') as hard_assign, 
			//  -- IFNULL(t.est_start,'') AS est_start

		def tasks = jdbcTemplate.queryForList(query)
		
		def now = new Date().format('yyyy-MM-dd H:m:s')

		def styleDef = "rounded, filled"

		def dotText = new StringBuffer()

		dotText << """#
# TDS Runbook for Project ${project}, Event ${moveEvent.name}
# Exported on ${now}
# This is  .DOT file format of the project tasks
#
digraph runbook {
	graph [rankdir=LR, margin=0.001];
	node [ fontsize=10, fontname="Helvetica", shape="rect" style="${styleDef}" ]
  
"""
	
		def style=''
		def fontcolor=''
		def fontsize=''
		def fillcolor
		def attribs
		def color

		style = styleDef

		tasks.each {
		    def task = "${it.task_number}:" + org.apache.commons.lang.StringEscapeUtils.escapeHtml(it.task).replaceAll(/\n/,'').replaceAll(/\r/,'')
		    def tooltip  = "${it.task_number}:" + org.apache.commons.lang.StringEscapeUtils.escapeHtml(it.task).replaceAll(/\n/,'').replaceAll(/\r/,'')
			def colorKey = taskService.taskStatusColorMap.containsKey(it.status) ? it.status : 'ERROR'

			fillcolor = taskService.taskStatusColorMap[colorKey][1]

			// log.info "task ${it.task}: role ${it.role}, ${AssetComment.AUTOMATIC_ROLE}, (${it.role == AssetComment.AUTOMATIC_ROLE ? 'yes' : 'no'})"
			// if ("${it.roll}" == "${AssetComment.AUTOMATIC_ROLE}" ) {
			if ( "${it.role == AssetComment.AUTOMATIC_ROLE ? 'yes' : 'no'}" == 'yes' ) {
				fontcolor = taskService.taskStatusColorMap['AUTO_TASK'][0] 
				color = taskService.taskStatusColorMap['AUTO_TASK'][1]
				fontsize = '8'
			} else {
				fontcolor = taskService.taskStatusColorMap[colorKey][0]
				fontsize = '10'
				color = 'black'
			}

			// style = mode == 's' ? "fillcolor=\"${taskService.taskStatusColorMap[colorKey][1]}\", fontcolor=\"${fontcolor}\", fontsize=\"${fontsize}\", style=filled" : ''
			attribs = "color=\"${color}\", fillcolor=\"${fillcolor}\", fontcolor=\"${fontcolor}\", fontsize=\"${fontsize}\""

			def url = HtmlUtil.createLink([controller:'task', action:'neighborhoodGraph', id:"${it.id}", absolute:false])

		    task = (task.size() > 35) ? task[0..34] : task 
			dotText << "\t${it.task_number} [label=\"${task}\"  URL=\"$url\", style=\"$style\", $attribs, tooltip=\"${tooltip}\"];\n"
			def successors = it.successors
			if (successors) {
				successors = (successors as Character[]).join('')
				successors = successors.split(',')
				successors.each { s -> 
					if (s.size() > 0) {
						dotText << "\t${it.task_number} -> ${s};\n"
					}
				}
			}	
		}

		dotText << "}\n"
		
		try {
			def uri = reportsService.generateDotGraph("runbook-$moveEventId", dotText.toString() )
			
			// convert the URI into a web-safe format
			uri = uri.replaceAll("\\u005C", "/") // replace all backslashes with forwardslashes
			
			redirect(uri:uri)
		} catch (e) {
			render(text:"<h1>Graph Generation Failed</h1>The error was:<p/><pre>${e.getMessage()}</pre>", status:"503")
		}
	}
	
	/**
	 * Used in MyTask to set user preference for printername and quantity .
	 * @param prefFor - Key 
	 * @param selected : value
	 */
	def setLabelQuantityPref = {
		def key = params.prefFor
		def selected=params.list('selected[]')[0] ?:params.selected
		if(selected){
			userPreferenceService.setPreference( key, selected )
			session.setAttribute(key,selected)
		}
		render true
	}
	
	/**
	 * Used in Task Manager auto open action bar which status is ready or started.
	 * @param : id[] : list of id whose status is ready or started
	 * @return : map consist of id of task and action bar 
	 */
	def genBulkActionBarHTML = {
		def taskIds =  params.list("id[]")
		def resultMap = [:]
		taskIds.each{
			def comment = AssetComment.read(it)
			if( comment ){
				def actionBar = getActionBarData( comment );
				resultMap << [(it): actionBar.toString()]
			}
		}
		render resultMap as JSON
	}
	
	/**
	 * Used in Task Manager action bar to change estTime.
	 * @param : day : 1,2 or 7days.
	 * @param : commentId.
	 * @return : retMap. 
	 */
	def changeEstTime = {
		def etext = ""
		def comment
		def commentId = NumberUtils.toInt(params.commentId)
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def estformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		if (commentId >0) {
			def day = NumberUtils.toInt(params.day)
			def project = securityService.getUserCurrentProject()
			comment = AssetComment.findByIdAndProject(commentId,project)
			def estDay = [1,2,7].contains(day) ? day : 0
			if (comment) {
				comment.estStart = TimeUtil.nowGMT().plus(estDay)
				
				if (!comment.estFinish || comment.estStart > comment.estFinish )
					comment.estFinish = comment.estStart.plus(1)
					
				if (!comment.hasErrors() && !comment.save(flush:true)) {
					etext = "unable to update estTime"+GormUtil.allErrorsString( comment )
					log.error etext 
				}
			} else {
				etext = "Requested comment does not exist. "
			}
		} else {
				etext = "Requested comment does not exist. "
		}
		def retMap=[etext:etext, estStart : comment?.estStart ? estformatter.format(TimeUtil.convertInToUserTZ(comment?.estStart, tzId)) : '' ,
					 estFinish: comment?.estFinish ? estformatter.format(TimeUtil.convertInToUserTZ(comment?.estFinish, tzId)) : '' ]
		render retMap as JSON
	}
	
	def taskGraphViewer = {
		if ( RolePermissions.hasPermission("AdminMenuView") ) {
			// handle project
			long projectId = securityService.getUserCurrentProject().id
			if ( ! projectId ) {
				flash.message = "You must select a project before using the task graph."
				redirect(controller:"project", action:"list")
				return
			}

			// if user used the event selector on the page, update their preferences with the new event
			if (params.moveEventId && params.moveEventId.isLong())
				userPreferenceService.setPreference("MOVE_EVENT", params.moveEventId)

			// handle move events
			def moveEvents = MoveEvent.findAllByProject(Project.get(projectId))
			def eventPref = userPreferenceService.getPreference("MOVE_EVENT") ?: '0'
			long selectedEventId = eventPref.isLong() ? eventPref.toLong() : 0
			
			return [moveEvents:moveEvents, selectedEventId:selectedEventId]
		} else {
			flash.message = "You do not have permission to view the task graph."
			redirect(controller:"project", action:"show")
			return
		}
	}
	
	// gets the JSON object used to populate the task graph timeline
	def taskGraph = {
		
		// handle project
		long projectId = securityService.getUserCurrentProject().id
		if ( ! projectId ) {
			flash.message = "You must select a project before using the task graph."
			redirect(controller:"project", action:"list")
			return
		}

		// if user used the event selector on the page, update their preferences with the new event
		if (params.moveEventId && params.moveEventId.isLong())
			userPreferenceService.setPreference("MOVE_EVENT", params.moveEventId)

		// handle move events
		def moveEvents = MoveEvent.findAllByProject(Project.get(projectId))
		def eventPref = userPreferenceService.getPreference("MOVE_EVENT") ?: '0'
		long selectedEventId = eventPref.isLong() ? eventPref.toLong() : 0
		if (selectedEventId == 0)
			return [data:{}, moveEvents:moveEvents, selectedEventId:selectedEventId];
		
		// get basic task and dependency data
		def me = MoveEvent.get(selectedEventId)
		if (! me) {
			render "Unable to find event $meId"
			return
		}
		def tasks = runbookService.getEventTasks(me)
		def deps = runbookService.getTaskDependencies(tasks)
		def startTime = 0
		
		// generate optimized schedule based on this data
		def dfsMap = runbookService.processDFS( tasks, deps )
		def durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks) 
		def graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks)
		def estFinish = runbookService.computeStartTimes(startTime, tasks, deps, dfsMap.starts, dfsMap.sinks, graphs)

//		def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def startDate = TimeUtil.convertInToUserTZ(dfsMap.starts[0].estStart, tzId) ?: TimeUtil.nowGMT()
		
		// generate the JSON data used by d3
		def items = []
		def roles = []
		tasks.each { t ->
			def predecessorIds = []
			t.taskDependencies.each { dep ->
				predecessorIds.push(dep.predecessor.id)
			}
			def role = t.role ?: 'NONE'
			if ( ! (role in roles) )
				roles.push(role)
			items.push([ id:t.id, name:t.comment, startInitial:t.tmpEarliestStart, endInitial:t.tmpEarliestStart+t.duration,
			predecessorIds:predecessorIds, criticalPath:t.tmpCriticalPath, assignedTo:t.assignedTo.toString(), status:t.status,
			role:role])
		}
		def sinks = []
		dfsMap.sinks.each { s ->
			sinks.push(s.id)
		}
		def data = [items:items, sinks:sinks, roles:roles, startDate:startDate] as JSON
		
		// clear out the metaclass objects to prevent a memory leak
		tasks.each { t ->
			t.metaClass = null
		}
		deps.each { d ->
			d.metaClass = null
		}
		
		def returnMap = [data:data, moveEvents:moveEvents, selectedEventId:selectedEventId] as JSON
		render data
	}
}
