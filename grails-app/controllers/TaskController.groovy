import grails.converters.JSON

import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.GormUtil
import org.apache.commons.lang.StringEscapeUtils

class TaskController {
	
	def securityService
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
		} else {
			log.warn "genActionBarHTML - invalid comment id (${params.id}) from user ${userLogin}"
			actionBar.append('<td>An unexpected error occurred</td>')
		}

		actionBar.append(""" <td colspan='${cols}'>&nbsp;</td>
			</tr></table>""")
		
		return actionBar
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
			render "Invalid task id supplied"
			return
		}	
		def project = securityService.getUserCurrentProject()
		def rootTask = AssetComment.findByIdAndProject(taskId, project) {
			render "Task not found"
			return
		}

		def depList = taskService.getNeighborhood(taskId)
		if (depList.size() == 0) {
			render "No task dependencies found"
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
				// def url = HtmlUtil.createLink([controller:'task', action:'neighborhoodGraph', id:task.id, absolute:true])
				def url = HtmlUtil.createLink([id:task.id, absolute:true])

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
					style = "dashed, bold"
				} else {
					style = styleDef
				}

				attribs = "color=\"${color}\", fillcolor=\"${fillcolor}\", fontcolor=\"${fontcolor}\", fontsize=\"${fontsize}\""

				dotText << "\t${task.taskNumber} [label=\"${label}\" URL=\"$url\", style=\"$style\", $attribs, tooltip=\"${tooltip}\"];\n"
			}

		}

		// Iterate over the dependency list outputting the two nodes for each and the relationship
		depList.each() { d ->
			outputTaskNode(d.assetComment, taskId)
			outputTaskNode(d.predecessor, taskId)

			dotText << "\t${d.predecessor.taskNumber} -> ${d.assetComment.taskNumber};\n"

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

		def categories = GormUtil.asQuoteCommaDelimitedString(AssetComment.moveDayCategories)
		
		def query = """
			SELECT 
			  t.task_number, 
			  GROUP_CONCAT(s.task_number SEPARATOR ',') AS successors,
			  IFNULL(a.asset_name,'') as asset, 
			  t.comment as task, 
			  t.role,
			  t.status,
			  -- IF(t.hard_assigned=1,t.role,'') as hard_assign, 
			  IFNULL(CONCAT(first_name,' ', last_name),'') as hard_assign,
			  t.duration
			  -- IFNULL(t.est_start,'') AS est_start
			FROM asset_comment t
			LEFT OUTER JOIN task_dependency d ON d.predecessor_id=t.asset_comment_id
			LEFT OUTER JOIN asset_comment s ON s.asset_comment_id=d.asset_comment_id
			LEFT OUTER JOIN asset_entity a ON t.asset_entity_id=a.asset_entity_id
			LEFT OUTER JOIN person ON t.owner_id=person.person_id
			WHERE t.project_id=${projectId} AND t.move_event_id=${moveEventId} AND
			  t.category IN ( ${categories} )
			GROUP BY t.task_number
			"""
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

			log.info "task ${it.comment}: role ${it.role}, ${AssetComment.AUTOMATIC_ROLE}, (${it.role == AssetComment.AUTOMATIC_ROLE ? 'yes' : 'no'})"
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

		    task = (task.size() > 35) ? task[0..34] : task 
			dotText << "\t${it.task_number} [label=\"${task}\" style=\"$style\", $attribs, tooltip=\"${tooltip}\"];\n"
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
			redirect(uri:uri)
		} catch(e) {
			render "<pre>${e.getMessage()}</pre>"
		}				
	}
	
	/**
	 * Used in MyTask to set user preference for printername and quantity .
	 * @param prefFor - Key 
	 * @param selected : value
	 */
	def setLabelQuantityPref ={
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
}
