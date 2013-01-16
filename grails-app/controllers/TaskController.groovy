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

def jdbcTemplate

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
				redirect(controller:'clientTeams', action:'listTasks', params:redirParams)			
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
		def userLogin = securityService.getUserLogin()
		
		// There are a total of 13 columns so we'll subtract for each conditional button
		def cols=12 
		
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
		render actionBar.toString()
	}
	/**
	* Used by the getActionBarHTML to wrap the button HTML into <td>...</td>
	*/
	def _actionButtonTd(tdId, button) {
		return """<td id="${tdId}" width="8%" nowrap="nowrap">${button}</td>"""	
	}
	
	/**
	 * Generates a graph of the Move Event Tasks
	 * @param moveEventId
	 * @return redirect to URI of image or HTML showing the error
	 */
	def moveEventTaskGraph = {
		
		// TODO : refactor this code into the taskService appropriately
		
		def moveEventId=params.moveEventId	
		def moveEvent = MoveEvent.read(moveEventId)
		
		def tmpPath = "/tmp/"
		def targetPath = "/var/www/tdstm/images/tmp/"
		def targetURI = "/image/tmp/"
		def dotExec = "/usr/bin/dot"
		
		if (! moveEvent) {
			response.status=404
			render "404 Not Found"
		} 
		def project = moveEvent.project
		def projectId = project.id

		def now = new Date().format('yyyy-MM-dd H:m:s')
		
		def filename = "runbook-$moveEventId-${new Date().format('yyyyMMdd-HHmmss')}"
		def dotFN = "${tmpPath}${filename}.dot"
		def dotFile = new File(dotFN);
		def categories = GormUtil.asQuoteCommaDelimitedString(AssetComment.moveDayCategories)
		
		def query = """
			SELECT 
			  t.task_number, 
			  GROUP_CONCAT(s.task_number SEPARATOR ',') AS successors,
			  IFNULL(a.asset_name,'') as asset, 
			  t.comment as task, 
			  t.role,
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
		
		dotFile << """#
# TDS Runbook for Project ${project}, Move Event ${moveEvent.name}
# Exported on ${now}
# This is  .DOT file format of the project tasks
#
digraph runbook {
	graph [rankdir=LR, fontsize=8, margin=0.001];
"""

		tasks.each {
		//	AND t.task_number < 40
		//    println "Record: $it"
		    def task = "${it.task_number}:" + org.apache.commons.lang.StringEscapeUtils.escapeHtml(it.task).replaceAll(/\n/,'').replaceAll(/\r/,'')
		    task = (task.size() > 35) ? task[0..34] : task 
			dotFile << "\t${it.task_number} [label=\"${task}\"];\n"
			def successors = it.successors
			if (successors) {
				successors = (successors as Character[]).join('')
		//		println "successors-a: ${successors}"
				successors = successors.split(',')
				successors.each { s -> 
					if (s.size() > 0) {
						dotFile << "\t${it.task_number} -> ${s};\n"
					}
				}
			}	
		}

		dotFile << "}\n"
		
		def imgFilename = "${filename}.svgz"				
		def proc = "${dotExec} -Tsvgz -v -o ${targetPath}${imgFilename} ${dotFile}".execute()
	 	proc.waitFor()
	
		if (proc.exitValue() == 0) {
			// Delete the dot file because we don't need it
			dotFile.delete()
			redirect(uri:"${targetURI}${imgFilename}")
			
		} else {
			render "<pre>exit code: ${ proc.exitValue()}\n stderr: ${proc.err.text}\n stdout: ${proc.in.text}"
			
			def errFile = new File("${targetPath}${filename}.err")
			errFile << "exit code:\n\n${ proc.exitValue()}\n\nstderr:\n${proc.err.text}\n\nstdout:\n${proc.in.text}"
			
			
		}
				
	}
}
