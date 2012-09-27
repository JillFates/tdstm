import grails.converters.JSON

import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.HtmlUtil

class TaskController {
	
	def securityService
	def commentService
	def taskService

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
	 *  @params : task id
	 *  @return : actions bar as HTML (Start, Done, Details, Assign To Me)
	 */
	def genActionBarHTML = {
		def commentInstance = AssetComment.get(params.id)
		def userLogin = securityService.getUserLogin()
		StringBuffer actionBar = new StringBuffer("""<table style="border:0px"><tr>""")
		if(commentInstance.status ==  AssetCommentStatus.READY){
			actionBar.append(HtmlUtil.genActionButton(commentInstance,"Start","changeStatus('${commentInstance.id}','${AssetCommentStatus.STARTED}','${commentInstance.status}', 'taskManager')", "startTdId_${commentInstance.id}"))
		}
		if(commentInstance.status in[ AssetCommentStatus.READY,AssetCommentStatus.STARTED]){
			actionBar.append(HtmlUtil.genActionButton(commentInstance,"Done","changeStatus('${commentInstance.id}','${AssetCommentStatus.DONE}', '${commentInstance.status}', 'taskManager')", "doneTdId_${commentInstance.id}"))
		}
		actionBar.append(HtmlUtil.genActionButton(commentInstance,"Details..","showAssetComment(${commentInstance.id},'show')","detailsTdId_${commentInstance.id}"))
		if(userLogin.person.id != commentInstance.assignedTo?.id && commentInstance.status in [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED]){
			actionBar.append(HtmlUtil.genActionButton(commentInstance,"Assign To Me","assignTask('${commentInstance.id}','${commentInstance.assignedTo}', '${commentInstance.status}','taskManager')", "assignMeId_${commentInstance.id}"))
		}
		actionBar.append(""" <td colspan='9'>&nbsp;</td>""")
		actionBar.append(""" </tr></table>""")
		render actionBar.toString()
	}
}
