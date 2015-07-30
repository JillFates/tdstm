import com.tds.asset.AssetComment
import com.tds.asset.TaskDependency
import com.tds.asset.AssetDependency
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil

import grails.converters.JSON
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import java.text.SimpleDateFormat
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.math.NumberUtils

class TaskController {
	
	def commentService
	def controllerService
	def runbookService
	def securityService

	def taskService
	def userPreferenceService
	def jdbcTemplate
	def reportsService
	def assetEntityService
	def partyRelationshipService

	def index() { }
	
	/**
	* Used by the myTasks and Task Manager to update tasks appropriately.
	*/
	def update() {
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
			forward(controller:'task', action:'listUserTasks', params:redirParams)			
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
	def assignToMe() {
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
		
		def map = [assignedToName:assignedTo, errorMsg:errorMsg]
		render map as JSON
	}
	
	/**
	 *  Generate action bar for a selected comment in Task Manager
	 *  @params id - the task (aka AssetComment) id number for the task bark
	 *  @return : actions bar as HTML (Start, Done, Details, Assign To Me)
	 */
	def genActionBarHTML() {
		def comment = AssetComment.get(params.id)
		def actionBar = retrieveActionBarData(comment); 
		render actionBar.toString()
	}
	
	/**
	 * Used to generate action Bar for task  
	 * @param comment : instance of asset comment
	 * @return : Action Bar HTML code.
	 */
	def retrieveActionBarData(comment) {
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
	def genActionBarForShowView() {
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
	 * Used to generate action Bar for task details view  
	 * @param asset comment id.
	 * @render : Action Bar JSON code.
	 */
	def genActionBarForShowViewJson() {
		def comment = AssetComment.get(params.id)
		def userLogin = securityService.getUserLogin()
		def actionBar = []
		def includeDetails = params.includeDetails?params.includeDetails.toBoolean():false
		def result

		if (comment) {
			if (comment.status in [ AssetCommentStatus.READY, AssetCommentStatus.STARTED]) {
				def enabled = (comment.status ==  AssetCommentStatus.READY)
				actionBar << [label: 'Start', icon: 'ui-icon-play', actionType: 'changeStatus', newStatus: AssetCommentStatus.STARTED, redirect: 'taskManager', disabled: !enabled]

				enabled = (comment.status in [ AssetCommentStatus.READY, AssetCommentStatus.STARTED])
				actionBar << [label: 'Done', icon: 'ui-icon-check', actionType: 'changeStatus', newStatus: AssetCommentStatus.DONE, redirect: 'taskManager', disabled: !enabled]
			}

			if (includeDetails) {
				actionBar << [label: 'Details...', icon: 'ui-icon-zoomin', actionType: 'showDetails']
			}
			
			if ( HtmlUtil.isMarkupURL(comment.instructionsLink)  )
			{
				actionBar << [label: HtmlUtil.parseMarkupURL(comment.instructionsLink)[0], icon: 'ui-icon-document', actionType: 'viewInstructions', redirect: 'taskManager']
			}
			else
			{
				if(HtmlUtil.isURL(comment.instructionsLink))
				{
				actionBar << [label: 'Instructions...', icon: 'ui-icon-document', actionType: 'viewInstructions', redirect: 'taskManager']
				}
			}

			if (userLogin.person.id != comment.assignedTo?.id && comment.status in [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED]){
				actionBar << [label: 'Assign To Me', icon: 'ui-icon-person', actionType: 'assignTask', redirect: 'taskManager']
			}

			
			def hasDelayPrem = RolePermissions.hasPermission("CommentCrudView")
			if(hasDelayPrem && comment.status ==  AssetCommentStatus.READY && !(comment.category in AssetComment.moveDayCategories)){
				actionBar << [label: 'Delay for:']
				actionBar << [label: '1 day', icon: 'ui-icon-seek-next', actionType: 'changeEstTime', delay: '1']
				actionBar << [label: '2 day', icon: 'ui-icon-seek-next', actionType: 'changeEstTime', delay: '2']
				actionBar << [label: '7 day', icon: 'ui-icon-seek-next', actionType: 'changeEstTime', delay: '7']
			}

			def depCount = TaskDependency.countByPredecessor( comment )
			if (depCount > 0) {
				actionBar << [label: 'Neighborhood', icon: 'tds-task-graph-icon', actionType: 'showNeighborhood']	
			}

			result = ServiceResults.success(actionBar) as JSON
		} else {
			result = ServiceResults.fail([error:"invalid comment id (${params.id}) from user ${userLogin}"]) as JSON
		}

		render result
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
	def neighborhoodGraphSvg() {
		def errorMessage = ''

		while (true) {	
			def taskId = params.id
			if (! taskId || ! taskId.isNumber()) {
				errorMessage = "An invalid task id was supplied. Please contact support if this problem persists."
				break
			}

			def project = securityService.getUserCurrentProject()
			if (! project) {
				errorMessage = 'You must first select a project before view graphs'
				break
			}

			def rootTask = AssetComment.read(taskId) 
			if (!rootTask || rootTask.project.id != project.id) {
				errorMessage = "Unable to find the specified task"
				if (rootTask) 
					log.warn "SECURITY : User ${securityService.getUserLogin()} attempted to access graph for task ($taskId) not associated to current project ($project)"
				break
			}

			def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && params.viewUnpublished == '1')
			userPreferenceService.setPreference("viewUnpublished", viewUnpublished.toString())
			
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
			
			def moveEventId = 0
			if (params.moveEventId && params.moveEventId.isNumber())
				moveEventId = params.moveEventId
			
			
			def now = new Date().format('yyyy-MM-dd H:m:s')
			def styleDef = "rounded, filled"

			def dotText = new StringBuffer()

			dotText << """#
# TDS Runbook for Project ${project}, Task ${rootTask.toString().replaceAll(/[\n\r]/,'')}
# Exported on ${now}
# This is  .DOT file format of the project tasks
#
digraph runbook {
	graph [rankdir=LR, margin=0.001];
	node [ fontsize=10, fontname="Helvetica", shape="rect" style="${styleDef}" ]
  
"""
		
			def style = ''
			def fontcolor = ''
			def fontsize = ''
			def attribs
			def color

			style = styleDef

			def tasks = []
			def taskList = []
			def roles = []

			// helper closure that outputs the task info in a dot node format
			def outputTaskNode = { task, rootId ->
				if (! tasks.contains(task.id) && (viewUnpublished || task.isPublished)) {
					tasks << task.id
					
					def label = "${task.taskNumber}:" + org.apache.commons.lang.StringEscapeUtils.escapeHtml(task.comment).replaceAll(/\n/,'').replaceAll(/\r/,'')
					label = (label.size() < 31) ? label : label[0..30]
					
					def tooltip  = "${task.taskNumber}:" + org.apache.commons.lang.StringEscapeUtils.escapeHtml(task.comment).replaceAll(/\n/,'').replaceAll(/\r/,'')
					def colorKey = taskService.taskStatusColorMap.containsKey(task.status) ? task.status : 'ERROR'
					def fillcolor = taskService.taskStatusColorMap[colorKey][1]
					//def url = HtmlUtil.createLink([controller:'task', action:'neighborhoodGraph', id:task.id, absolute:false])

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
					
					// add the task's role to the roles list
					def role = task.role ?: 'NONE'
					if ( task.role && ! (role in roles) )
						roles.push(role)
					
					taskList << task
					
					attribs = "id=\"${task.id}\", color=\"${color}\", fillcolor=\"${fillcolor}\", fontcolor=\"${fontcolor}\", fontsize=\"${fontsize}\""
					
					dotText << "\t${task.taskNumber} [label=\"${label}\", style=\"$style\", $attribs, tooltip=\"${tooltip}\"];\n"
					
				}

			}
			
			// helper closure to output the count node for the adjacent tasks
			def outputOuterNodeCount = { taskNode, isPred, count ->
				if (viewUnpublished || taskNode.isPublished) {
					log.info "neighborhoodGraph() outputing edge node ${taskNode.taskNumber}, Predecessor? ${isPred?'yes':'no'}"
					def cntNode = "C${taskNode.taskNumber}"
					dotText << "\t$cntNode [id=\"placeholder\" label=\"$count\" tooltip=\"There are $count adjacent task(s)\"];\n"
					// dotText << "\t$cntNode [label=\"$count\" style=\"invis\" tooltip=\"There are $count adjacent task(s)\"];\n" 
					if (isPred) {
						dotText << "\t$cntNode -> ${taskNode.taskNumber};\n"				
					} else {
						dotText << "\t${taskNode.taskNumber} -> $cntNode;\n"
					}
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
				
				// convert the URI to a web safe format
				uri = uri.replaceAll("\\u005C", "/") // replace all backslashes with forwardslashes
				def svgFile = new File(grailsApplication.config.graph.targetDir + uri.split('/')[uri.split('/').size()-1])
				
				def svgText = svgFile.text
				def data = [svgText:svgText, roles:roles, tasks:taskList]
				render data as JSON
				
				return false
				
			} catch(e) {
				errorMessage = 'Encounted an unexpected error while generating the graph'
				// TODO : Need to change out permission to ShowDebugInfo
				if (RolePermissions.hasPermission("RoleTypeCreate"))
					errorMessage += "<br><pre>${e.getMessage()}</pre>"
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
	 * Generates a graph of the Event Tasks
	 * @param moveEventId
	 * @param mode - flag as to what mode to display the graph as (s=status, ?=default)
	 * @return redirect to URI of image or HTML showing the error
	 */
	def moveEventTaskGraphSvg() {
		def errorMessage = ''
		
		// Create a loop that we can break out of as we need to	
		while (true) {

			def project = securityService.getUserCurrentProject()
			if (! project) {
				errorMessage = "You must select a project before continuing"
				break
			}

			def moveEventId = params.moveEventId	
			if (! moveEventId || ! moveEventId.isNumber()) {
				errorMessage = "Please select an event to view the graph"
				break
			}

			def moveEvent = MoveEvent.read(moveEventId)
			if (! moveEvent || moveEvent.project.id != project.id) {
				errorMessage = "The event specified was not found"
				if (moveEvent) 
					log.warn "SECURITY : User ${securityService.getUserLogin()} attempted to access graph of event ($moveEventId) not associated to current project ($project)"
				break
			}
			
			log.debug "**** ${project.id} / ${moveEvent.project.id} - $project / $moveEvent "

			def mode = params.mode ?: ''
			if (mode && ! "s".contains(mode)) {
				mode = ''
				log.warn "The wrong mode [$mode] was specified"
			}
			
			def projectId = project.id
			
			def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && params.viewUnpublished == '1')
			userPreferenceService.setPreference("viewUnpublished", viewUnpublished.toString())
			
			
			jdbcTemplate.update('SET SESSION group_concat_max_len = 100000;')

			def query = """
				SELECT 
					t.asset_comment_id AS id,
					t.task_number, 
					CONVERT( GROUP_CONCAT(s.task_number SEPARATOR ',') USING 'utf8') AS successors,
					IFNULL(a.asset_name,'') AS asset, 
					t.comment AS task, 
					t.role,
					t.status,
					IFNULL(CONCAT(first_name,' ', last_name),'') AS hard_assign,
					t.is_published AS isPublished,
					t.duration
				FROM asset_comment t
				LEFT OUTER JOIN task_dependency d ON d.predecessor_id=t.asset_comment_id
				LEFT OUTER JOIN asset_comment s ON s.asset_comment_id=d.asset_comment_id
				${viewUnpublished ? '' : ' AND s.is_published=1 '}
				LEFT OUTER JOIN asset_entity a ON t.asset_entity_id=a.asset_entity_id
				LEFT OUTER JOIN person ON t.owner_id=person.person_id
				WHERE t.project_id=${projectId} AND t.move_event_id=${moveEventId} 
				${viewUnpublished ? '' : ' AND t.is_published=1 '}
				GROUP BY t.task_number
			"""

				//  -- IF(t.hard_assigned=1,t.role,'') as hard_assign, 
				//  -- IFNULL(t.est_start,'') AS est_start
			//log.debug "moveEventTaskGraphSvg() SQL for tasks: $query"

			def tasks = jdbcTemplate.queryForList(query)
			def roles = []
			tasks.each { t ->
				def role = t.role ?: 'NONE'
				if ( t.role && ! (role in roles) )
					roles.push(role)
			}
			
			if (tasks.size()==0) {
				errorMessage = 'No tasks were found for the selected move event'
				break
			}

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
		
			def style = ''
			def fontcolor = ''
			def fontsize = ''
			def fillcolor
			def attribs
			def color

			style = styleDef

			tasks.each {
				def task = "${it.task_number}:" + it.task.encodeAsJSON()
			    def tooltip  = "${it.task_number}:" + it.task.encodeAsJSON()
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
				attribs = "id=\"${it.id}\", color=\"${color}\", fillcolor=\"${fillcolor}\", fontcolor=\"${fontcolor}\", fontsize=\"${fontsize}\""
				
				//def url = HtmlUtil.createLink([controller:'task', action:'neighborhoodGraph', id:"${it.id}", absolute:false])

				task = (task.size() > 35) ? task[0..34] : task 
				dotText << "\t${it.task_number} [label=\"${task}\"  id=\"${it.id}\", style=\"$style\", $attribs, tooltip=\"${tooltip}\"];\n"
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
				// String svgType = grailsApplication.config.graph.graphViz.graphType ?: 'svg'

				def uri = reportsService.generateDotGraph("runbook-$moveEventId", dotText.toString() )
				// convert the URI into a web-safe format
				uri = uri.replaceAll("\\u005C", "/") // replace all backslashes with forwardslashes
				String filename = grailsApplication.config.graph.targetDir + uri.split('/')[uri.split('/').size()-1]
				def svgFile = new File(filename)
				
				def svgText = svgFile.text
				def data = [svgText:svgText, roles:roles, tasks:tasks]
				render(text: data as JSON, contentType: 'application/json', encoding:"UTF-8")
				return false
				
			} catch (e) {
				errorMessage = 'Encounted an unexpected error while generating the graph'
				// TODO : Need to change out permission to ShowDebugInfo
				if (RolePermissions.hasPermission("RoleTypeCreate"))
					errorMessage += "<br><pre>${e.getMessage()}</pre>"
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
	def neighborhoodGraph() {
		Long id = params.id?.isLong() ? params.id.toLong() : 0L
		forward action:'taskGraph', params: ['neighborhoodTaskId':id]
	}

	def taskGraph() {
		// handle project
		def project = securityService.getUserCurrentProject()
		if ( ! project ) {
			flash.message = "You must select a project in order to use the task timeline."
			redirect(controller:"project", action:"list")
			return
		}
		
		// handle neighborhood graph specification
		def neighborhoodTaskId = params.neighborhoodTaskId ?: -1

		// if user used the event selector on the page, update their preferences with the new event
		if (params.moveEventId && params.moveEventId.isLong())
			userPreferenceService.setPreference("MOVE_EVENT", params.moveEventId)

		// handle move events
		def moveEvents = MoveEvent.findAllByProject(project)
		def eventPref = userPreferenceService.getPreference("MOVE_EVENT") ?: '0'
		long selectedEventId = 0
		if (neighborhoodTaskId != -1) {
			selectedEventId = AssetComment.get(neighborhoodTaskId)?.moveEvent?.id
			userPreferenceService.setPreference("MOVE_EVENT", selectedEventId.toString())
		} else if (eventPref.isLong() && eventPref.toLong() != 0) {
			selectedEventId = eventPref.toLong()
		}
		
		def viewUnpublished = userPreferenceService.getPreference("viewUnpublished") == 'true' ? '1' : '0'
		
		return [moveEvents:moveEvents, selectedEventId:selectedEventId, neighborhoodTaskId:neighborhoodTaskId, viewUnpublished:viewUnpublished]
	}
	
	/**
	 * Used in MyTask to set user preference for printername and quantity .
	 * @param prefFor - Key 
	 * @param selected : value
	 */
	def setLabelQuantityPref() {
		def key = params.prefFor
		def selected = params.list('selected[]')[0] ?:params.selected
		if (selected) {
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
	def genBulkActionBarHTML() {
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
	def changeEstTime() {
		def etext = ""
		def comment
		def commentId = NumberUtils.toInt(params.commentId)
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def estformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		if (commentId > 0) {
			def day = NumberUtils.toInt(params.day)
			def project = securityService.getUserCurrentProject()
			comment = AssetComment.findByIdAndProject(commentId,project)
			def estDay = [1,2,7].contains(day) ? day : 0
			if (comment) {
				comment.estStart = TimeUtil.nowGMT().plus(estDay)
				
				if (comment.duration && comment.durationScale && comment.duration > 0) {
					use ( TimeCategory ) {
						switch (comment.durationScale) {
							case TimeScale.M:
								comment.estFinish = comment.estStart + comment.duration.minutes
								break;
							case TimeScale.H:
								comment.estFinish = comment.estStart + comment.duration.hours
								break;
							case TimeScale.D:
								comment.estFinish = comment.estStart.plus(comment.duration)
								break;
							case TimeScale.W:
								comment.estFinish = comment.estStart + comment.duration.weeks
								break;
							default:
								comment.estFinish = comment.estStart.plus(comment.duration)
						}
					}
				} else {
					comment.duration = 1;
					comment.durationScale = "D";
					comment.estFinish = comment.estStart.plus(1)
				}
					
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
	
	def taskTimeline() {
		// handle project
		def project = securityService.getUserCurrentProject()
		if ( ! project ) {
			flash.message = "You must select a project in order to use the task timeline."
			redirect(controller:"project", action:"list")
			return
		}

		// if user used the event selector on the page, update their preferences with the new event
		if (params.moveEventId && params.moveEventId.isLong())
			userPreferenceService.setPreference("MOVE_EVENT", params.moveEventId)

		// handle move events
		def moveEvents = MoveEvent.findAllByProject(project)
		def eventPref = userPreferenceService.getPreference("MOVE_EVENT") ?: '0'
		long selectedEventId = eventPref.isLong() ? eventPref.toLong() : 0
		
		// handle the view unpublished checkbox
		def viewUnpublished = userPreferenceService.getPreference("viewUnpublished") == 'true' ? '1' : '0'
		
		return [moveEvents:moveEvents, selectedEventId:selectedEventId, viewUnpublished:viewUnpublished]
	}
	
	// gets the JSON object used to populate the task graph timeline
	def taskTimelineData() {
		
		// handle project
		long projectId = securityService.getUserCurrentProject().id
		if ( ! projectId ) {
			flash.message = "You must select a project before using the task graph."
			redirect(controller:"project", action:"list")
			return
		}
		
		// handle the view unpublished checkbox
		if (params.viewUnpublished && params.viewUnpublished in ['0', '1']) {
			def viewUnpublishedBoolean = (params.viewUnpublished == '1')
			userPreferenceService.setPreference("viewUnpublished", viewUnpublishedBoolean.toString())
		}
		
		def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && userPreferenceService.getPreference("viewUnpublished") == 'true')
		def publishedValues = [true]
		if (viewUnpublished)
			publishedValues = [true, false]

		// Define default data
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def defaultEstStart = TimeUtil.nowGMT()
		def defaultStartDate = TimeUtil.convertInToUserTZ(defaultEstStart, tzId)
		def data = [items:[], sinks:[], starts:[], roles:[], startDate:defaultStartDate, cyclicals:[:]]

		// if user used the event selector on the page, update their preferences with the new event
		if (params.moveEventId && params.moveEventId.isLong())
			userPreferenceService.setPreference("MOVE_EVENT", params.moveEventId)

		// handle move events
		def moveEvents = MoveEvent.findAllByProject(Project.get(projectId))
		def eventPref = userPreferenceService.getPreference("MOVE_EVENT") ?: '0'
		long selectedEventId = eventPref.isLong() ? eventPref.toLong() : 0
		if (selectedEventId == 0) {
			render ([data:data, moveEvents:moveEvents, selectedEventId:selectedEventId] as JSON)
			return
		}
		
		// get basic task and dependency data
		def me = MoveEvent.get(selectedEventId)
		if (! me) {
			render "Unable to find event $meId"
			return
		}
		def tasks = runbookService.getEventTasks(me).findAll{it.isPublished in publishedValues}
		def deps = runbookService.getTaskDependencies(tasks)
		
		// add any tasks referenced by the dependencies that are not in the task list
		deps.each {
			if ( !(it.predecessor in tasks) && it.predecessor.isPublished in publishedValues)
				tasks.push(it.predecessor)
			if ( !(it.successor in tasks) && it.successor.isPublished in publishedValues)
				tasks.push(it.successor)
		}
		tasks.sort { a, b ->
			return a.id - b.id
		}
		def tmp = runbookService.createTempObject(tasks, deps)
		def startTime = 0

		if (tasks.size() == 0 || deps.size() == 0) {
			render ([data:data, moveEvents:moveEvents, selectedEventId:selectedEventId] as JSON)
			return
		}

		// generate optimized schedule based on this data
		def dfsMap = runbookService.processDFS(tasks, deps, tmp)
		def durMap = runbookService.processDurations(tasks, deps, dfsMap.sinks, tmp)
		def graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks, tmp)
		def estFinish = runbookService.computeStartTimes(startTime, tasks, deps, dfsMap.starts, dfsMap.sinks, graphs, tmp)
		
		def estStart = dfsMap.starts[0].estStart
		if (!estStart) {
			estStart = TimeUtil.nowGMT()
		}
		def startDate = TimeUtil.convertInToUserTZ(estStart, tzId)
		
		
		
		// generate the JSON data used by d3
		def items = []
		def roles = []
		tasks.each { t ->
			def predecessorIds = []
			t.taskDependencies.each { dep ->
				predecessorIds.push(dep.predecessor.id)
			}
			def role = t.role ?: 'NONE'
			if ( t.role && ! (role in roles) )
				roles.push(role)
			
			def task = tmp['tasks'][t.id]
			items.push([ id:t.id, name:t.comment, startInitial:task.tmpEarliestStart, endInitial:task.tmpEarliestStart+t.duration,
			predecessorIds:predecessorIds, criticalPath:task.tmpCriticalPath, assignedTo:t.assignedTo.toString(), status:t.status,
			role:role, number:t.taskNumber])
		}

		// Sort the roles aka teams
		roles.sort()
		
		def sinks = []
		dfsMap.sinks.each { s ->
			sinks.push(s.id)
		}
		
		def starts = []
		dfsMap.starts.each { s ->
			starts.push(s.id)
		}
		
		def cyclicals = [:]
		dfsMap.cyclicals.each {
			cyclicals.put(it.key, it.value.stack)
		}
		data = [items:items, sinks:sinks, starts:starts, roles:roles, startDate:startDate, cyclicals:cyclicals]
		def returnMap = [data:data, moveEvents:moveEvents, selectedEventId:selectedEventId] as JSON
		render returnMap
	}

	def editTask() {
		render( view: "_editTask", model: [])
	}

	
	def showTask() {
		//def instructionsLink = AssetComment.read(params.taskId)?.instructionsLink
		//log.error instructionsLink
		render( view: "_showTask", model: [])
	}

	def list() {
		render( view: "_list", model: [])
	}

	/**
	 * Get task roles
	 */
	def retrieveStaffRoles() {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		try {
			def result = taskService.getRolesForStaff()

			render(ServiceResults.success(result) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	/**
	 * Simply a test page for the runbook optimization
	 * @param params.eventId - the event id to generate the data for or default to the user's current event
	 * @param params.showAll - flag to indicate including all columns of just the planning ones (true|false)
	 */
	def eventTimelineResults() {

		// Get the form parameters
		Boolean showAll = (params.showAll == 'true')
		String meId = params.eventId	

		def (project, user) = controllerService.getProjectAndUserForPage( this )
		if (! project) 
			return

		MoveEvent me = controllerService.getEventForPage(this, project, user, meId) 
		if (! me) {
			render "Unable to find event $meId"
			return
		}

		def startTime = 0
		def tasks, deps, dfsMap, durMap, graphs,estFinish

		StringBuilder results = new StringBuilder("<h1>Timeline Data for Event $me</h1>")

		try {
			tasks = runbookService.getEventTasks(me)
			deps = runbookService.getTaskDependencies(tasks)
			def tmp = runbookService.createTempObject(tasks, deps)

			dfsMap = runbookService.processDFS( tasks, deps, tmp )
			durMap = runbookService.processDurations( tasks, deps, dfsMap.sinks, tmp) 
			graphs = runbookService.determineUniqueGraphs(dfsMap.starts, dfsMap.sinks, tmp)
			estFinish = runbookService.computeStartTimes(startTime, tasks, deps, dfsMap.starts, dfsMap.sinks, graphs, tmp)

			def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ

			results.append("Found ${tasks.size()} tasks and ${deps.size()} dependencies<br/>")
			results.append("Start Vertices: " + (dfsMap.starts.size() > 0 ? dfsMap.starts : 'none') + '<br/>')
			results.append("Sink Vertices: " + (dfsMap.sinks.size() > 0 ? dfsMap.sinks : 'none') + '<br/>')
			results.append("Cyclical Maps: ")
			
			def cyclicals = [:]
			dfsMap.cyclicals.each {
				cyclicals.put(it.key, it.value.stack)
			}
			
			// results.append(dfsMap.cyclicals)
			if (dfsMap.cyclicals?.size()) {
				results.append('<ol>')
				dfsMap.cyclicals.each { c ->
					def task = c.value.loopback
					results.append("<li> Circular Reference Stack: <ul>")
					// def marker = ''
					c.value.stack.each { cycTaskId ->
						task = tasks.find { it.id == cycTaskId }
						results.append("<li>$task.taskNumber $task.comment")
					}
					results.append(" >> $c.value.loopback.taskNumber $c.value.loopback.comment</li>")
					results.append('</ul>')
				}
				results.append('</ol>')
			} else {
				results.append('none')
			}
			results.append('<br/>')
			results.append("Pass 1 Elapsed Time: ${dfsMap.elapsed}<br/>")
			results.append("Pass 2 Elapsed Time: ${durMap.elapsed}<br/>")

			results.append("<b>Estimated Runbook Duration: ${estFinish} for Move Event: $me</b><br/>")

			/*
			results.append("<h1>Edges data</h1><table><tr><th>Id</th><th>Predecessor Task</th><th>Successor Task</th><th>DS Task Count</th><th>Path Duration</th></tr>")
			deps.each { dep ->
				results.append("<tr><td>${dep.id}</td><td>${dep.predecessor}</td><td>${dep.successor}</td><td>${dep.downstreamTaskCount}</td><td>${dep.pathDuration}</td></tr>")
			}
			results.append('</table>')
			*/

			def durationExtra = ''
			def timesExtra = ''
			def tailExtra = ''

			if (showAll) {
				durationExtra = "<th>Act Duration</th><th>Deviation</th>"
				timesExtra = "<th>Act Start</th>"
				tailExtra = "<th>TaskSpec</th><th>Hard Assigned</th><th>Resolved By</th>"
			}

			results.append("""<h1>Tasks Details</h1>
				<table>
					<tr><th>Id</th><th>Task #</th><th>Action</th> 
					<th>Est Duration</th>
					$durationExtra
					<th>Earliest Start</th><th>Latest Start</th><th>Constraint Time</th>
					$timesExtra
					<th>Act Finish</th><th>Priority</th><th>Critical Path</td><th>Team</th><th>Individual</th><th>Category</th>
					$tailExtra
					</tr>""")

			tasks.each { t ->

				def person = t.assignedTo ?: ''
				def team = t.role ?: ''
				def constraintTime = ''
				def actStart = ''
				def actFinish = ''
				TimeDuration actDuration
				def deviation = ''
				def actual=''

				if (t.constraintTime) {
					constraintTime = formatter.format(TimeUtil.convertInToUserTZ(t.constraintTime, tzId)) + " ${t.constraintType}"
				}
				if (t.actStart) {
					actStart = formatter.format(TimeUtil.convertInToUserTZ(t.actStart, tzId))
				}
				if (t.actFinish) {
					actFinish = formatter.format(TimeUtil.convertInToUserTZ(t.actFinish, tzId))
				}

				if (t.actStart && t.actFinish) {
					actDuration = TimeUtil.elapsed(t.actStart, t.actFinish)
					TimeDuration estDuration = new TimeDuration(0, t.durationInMinutes(), 0, 0)
					TimeDuration delta = actDuration.minus(estDuration)
					deviation = TimeUtil.ago(delta)
					actual = TimeUtil.ago(actDuration)
				} else {
					actual = ''
					deviation = ''
				}

				durationExtra = ''
				timesExtra = ''
				tailExtra = ''
				if (showAll) {
					durationExtra = "<td>$actual</td><td>$deviation</td>"
					timesExtra = "<td>$actStart</td>"
					tailExtra = "<td>${t.taskSpec}</td><td>${t.hardAssigned==1 ? 'Yes' : ''}</td><td>${t.resolvedBy ?: ''}</td>"
				}

				// TODO : add in computation for time differences if both constraint time est and/or actual
	 
	 			def criticalPath = (t.duration > 0 && tmp['tasks'][t.id].tmpEarliestStart == tmp['tasks'][t.id].tmpLatestStart ? 'Yes' : '&nbsp;')

				results.append( """<tr>
					<td>${t.id}</td><td>${t.taskNumber}</td><td>${t.comment}</td><td>${t.duration}</td>
					${durationExtra}
					<td>${tmp['tasks'][t.id].tmpEarliestStart}</td><td>${tmp['tasks'][t.id].tmpLatestStart}</td><td>$constraintTime</td>
					${timesExtra}
					<td>$actFinish</td><td>${t.priority}</td><td>$criticalPath</td><td>${team}</td><td>$person</td><td>${t.category}</td>
					$tailExtra
					</tr>""")
			}
			results.append('</table>')
		} catch (e) {
			results.append("<h1>Unable to complete computation</h1>${e.getMessage()}")
		}
		
		render results.toString()

	}

	/**
	 * Generates the user's list of tasks for the current project
	 * params:
	 *		tab - all or todo 
	 */
	def listUserTasks() {
		def project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}
		//log.error "PROJECT: ${project}"
		def person = securityService.getUserLoginPerson()
		//log.error "PERSON=${person}"
		def entities = assetEntityService.entityInfo( project )
		// If the request is being made as a redirect for a previous task update that was being completed, we need to sleep a moment
		// to allow the Quartz job that updates successors to finish so that when the user sees the new results that it may have successors
		// there were updated by the previous update.
		if (params.containsKey('sync')) {
			log.info "listUserTasks - sync'n 500 ms for ${person} on project ${project.id}"
			this.sleep(500)
			log.info "listUserTasks - sunk for ${person} on project ${project.id}"
		}
		
		log.debug "listUserTasks: params=$params, project=$project, person=$person, entities=${entities.size()}"

		def allTasks = false

		// Parameters 		
		def tab
		def taskList
		def todo
		def all
		
		// Deal with the user preferences
		def viewMode = params.viewMode
		def search = params.search
		def sort = params.sort
		
		if (viewMode) { 
			session.setAttribute('TASK_VIEW_MODE', viewMode)
		}
		// log.info "listComment() sort=${params.sort}, order=${params.order}"

		def isCleaner = partyRelationshipService.staffHasFunction(project.id, person.id, 'CLEANER')
		def isMoveTech = partyRelationshipService.staffHasFunction(project.id, person.id, 'MOVE_TECH')

		if (params.event) {
			def eventP = params.event.equals('null') ? "_null" : params.event;
			userPreferenceService.setPreference("MYTASKS_MOVE_EVENT_ID", eventP)
		}
		
		def moveEventId = userPreferenceService.getPreference("MYTASKS_MOVE_EVENT_ID")
		if (moveEventId.equals("_null")) {
			moveEventId = null
		}
		def moveEvent = MoveEvent.get(moveEventId)
		
		// Use the taskService.getUserTasks service to get all of the tasks [all,todo]
		def tasks = taskService.getUserTasks(person, project, false, 7, params.sort, params.order, search, moveEvent)
		
		// Get the size of the lists
		def todoSize = tasks['todo'].size()
		def allSize = tasks['all'].size()

		// Based on which tab the user is viewing we'll set taskList to the appropriate list to be returned to the user
		if (params.tab=='all') {
			tab = 'all'
			taskList = tasks['all']
			allTasks = true
		} else {
			tab = 'todo'
			taskList = tasks['todo']
		}
		
		// Build the list and associate the proper CSS style
		def issueList = []
		taskList.each{ task ->
			def css = taskService.getCssClassForStatus( task.status )
			issueList << ['item':task,'css':css]
		}
		def timeToRefresh =  userPreferenceService.getPreference("MYTASKS_REFRESH")
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		def moveEventList = MoveEvent.findAllByProject(project,[sort:'name'])
		
		// Determine the model and view
		def model = [taskList:issueList, tab:tab, todoSize:todoSize, allSize:allSize, 
			search:search, sort:params.sort, order:params.order,
	 		personId:person.id, isCleaner:isCleaner, isMoveTech:isMoveTech,
			timeToUpdate:timeToRefresh ?: 60, 
			isOnIE:false, person:person,servers : entities.servers, 
			applications : entities.applications, dbs : entities.dbs, 
			files : entities.files,  networks :entities.networks,
			assetDependency : new AssetDependency(), dependencyType:entities.dependencyType, 
			dependencyStatus:entities.dependencyStatus,moveBundleList:moveBundleList,moveEventList:moveEventList, moveEvent: moveEvent]
		
		if(search && taskList.size() > 0){
			model  << [searchedAssetId : taskList*.id[0], searchedAssetStatus : taskList*.status[0]]
		}
		def view = params.view == "myTask" ? "_tasks" : "myIssues"
		model << [timers:session.MY_ISSUE_REFRESH?.MY_ISSUE_REFRESH]
		if ( request.getHeader ( "User-Agent" ).contains ( "MSIE" ) ) {
			model.isOnIE= true
		}

		log.debug "listUserTasks: View is $view"
		
		// Send the user on his merry way
		render (view:view, model:model)
		
	}

	/**
	 * @author Ross Macfarlane
	 * @return JSON response containing the number of tasks assigned to the current user {count:#}
	 */
	def retrieveUserToDoCount() {
		def project = controllerService.getProjectForPage(this)
		if (!project) {
			return
		}

		def person = securityService.getUserLoginPerson()
		def tasksStats = taskService.getUserTasks(person, project, true)
		// log.info "retrieveToDoCount: tasksStats=${tasksStats}"
		def map = [ count:tasksStats['todo'] ]
		render map as JSON
	}

	/**
	 * Used in the MyTasks view to display the details of a Task/Comment
	 * @param issueId
	 * @return HTML that is used by an AJax call
	 */
	def showIssue() {

		def project = securityService.getUserCurrentProject()

		// This is such a hack at the moment but if this errors, the mobile scanner doesn't have any way to get back to the previous screen 
		// so it is a painful experience to close the app, kill the app, restart, login and then get back to the original screen.
		def backButton = 'Please press the Back button to return to the previous screen.<p/><button onclick="goBack()">Back</button>'
		def backScript = """
<script>
function goBack() { window.history.back() }
</script>
"""
		
		log.debug "showIssue: params=$params, project=$project"

		def assetComment = AssetComment.findByIdAndProject(params.issueId, project)

		if (! assetComment) {
			render "${backScript}Unable to locate a task for asset [${params.search}/${params.issueId}]. $backButton"
			return
		}
		

		def cartQty = '	'
		def moveEvent = assetComment.moveEvent
		
		// Determine the cart quantity
		// The quantity only appears on the last label scanned/printed for a particular cart. This is used to notify
		// the logistics and transport people that the cart is ready to wrap up.
		if (moveEvent && assetComment.assetEntity?.cart && assetComment.role == "CLEANER" && assetComment.status != AssetCommentStatus.DONE) {
			def cart = taskService.getCartQuantities(moveEvent, assetComment.assetEntity.cart)
			if (cart && ( cart.total - cart.done ) == 1) {
				// Only set the cartQty if we're printing the LAST set of labels for a cart (done is 1 less than total)
				cartQty = cart.total
			}
		}
		// log.info "cartQty ($cartQty)"
		
		def selectCtrlId = "assignedToEditId_${assetComment.id}"
		def assignToSelect = taskService.assignToSelectHtml(project.id, params.issueId, assetComment.assignedTo?.id, selectCtrlId)
		def person = securityService.getUserLoginPerson()
		
		// Bounce back to the user if we didn't get a legit id, associated with the project
		if (! assetComment ) {
			log.error "${person} attempted an invalide access a task/comment with id ${params.issueId} on project $project"
			render "${backScript}Unable to find specified record. ${backButton}"
			return
		}
		
		def estformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def dueFormatter = new SimpleDateFormat("MM/dd/yyyy");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			
		def isCleaner = partyRelationshipService.staffHasFunction(project.id, person.id, 'CLEANER')
		def canPrint = request.getHeader ( "User-Agent" ).contains ( "MSIE" ) && isCleaner
		
		def noteList = assetComment.notes.sort{it.dateCreated}
		def notes = []
		noteList.each{
			def dateCreated = TimeUtil.convertInToUserTZ(it.dateCreated, tzId).format("E, d MMM 'at ' HH:mma")
			notes << [dateCreated , it.createdBy.toString() ,it.note]
		}
		
		def viewMode = session.getAttribute("TASK_VIEW_MODE")
		
		// Determine if the user should be able to edit the task. The rules are:
		// 1. If ADMIN, CLIENT_ADMIN or CLIENT_MGR can always edit
		// 2. Can ALWAYS add a NOTE.
		// 3. Change person - when task is in the PENDING/READY status? 
		 
		def assignmentPerm = false
		def categoryPerm = false
		def permissionForUpdate = true

		if (securityService.hasRole( ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR'])) {
			assignmentPerm = categoryPerm = true
		} else {
			// AssignmentPerm can be changed if task is not completed/terminated
			assignmentPerm = ! [AssetCommentStatus.DONE, AssetCommentStatus.TERMINATED].contains(assetComment.status)
		}
		
		def dueDate = assetComment.dueDate ? dueFormatter.format(GormUtil.convertInToUserTZ(assetComment.dueDate, tzId)) : ''
		
		def successor = TaskDependency.findAllByPredecessor( assetComment )
		def projectStaff = partyRelationshipService.getProjectStaff( project.id )?.staff
		projectStaff.sort{it.firstName}
	    
		def model = [ assetComment:assetComment,
			notes:notes, 
			statusWarn:taskService.canChangeStatus ( assetComment ) ? 0 : 1,
			permissionForUpdate:permissionForUpdate, 
			assignmentPerm:assignmentPerm, 
			categoryPerm:categoryPerm,
			successor:successor, 
			projectStaff:projectStaff,
			canPrint:canPrint,
			dueDate:dueDate,
			assignToSelect:assignToSelect,
            assetEntity:assetComment.assetEntity,
            cartQty:cartQty,
			project:project
            ]
	
		def view = isCleaner ? '_showCleanerTask' : 'showIssue'
		if(isCleaner){
			def lblQty= session.getAttribute('printLabelQuantity') ?: userPreferenceService.getPreference( "printLabelQuantity" )
			def printerName=session.getAttribute('PRINTER_NAME') ?: userPreferenceService.getPreference( "PRINTER_NAME" )
			model << [lblQty:lblQty, prefPrinter:printerName]
		}
		render (view:view,model:model)
	}

	/**
	 * Used in a email to display the details of a Task/Comment
	 * @param task/comment id
	 * @return HTML that is used to display a task form an email
	 */
	def showTaskforEmail() {
		def (project, userLogin) = controllerService.getProjectAndUserForPage(this)
		if (!project) {
			return
		}

		def isValidUser= false
		def task= AssetComment.get(params.id)
		if(task?.assignedTo.id == userLogin.person.id){
			def isPersonAssignedToProject = partyRelationshipService.isPersonAssignedToProject()
			if(isPersonAssignedToProject)
				isValidUser = true
		}
		
		if(isValidUser){
			// if we are here good to go
			return commentService.showOrEditTask(params)
		} else {
			redirect(controller:"task", action:"listUserTasks", params:[message:"you dont have permissions to access this task ${task.taskNumber}"])
		}
	}

}
