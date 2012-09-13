import grails.converters.JSON

import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.HtmlUtil

class TaskController {
	
	def securityService

    def index = { }
	
	/**
	 * Used to assign assignTo through ajax call from MyTasks
	 * @params : id, status
	 * @return : user full name and errorMessage if status changed by accident.
	 */
	def assignToMe = {
		def comment = AssetComment.get(params.id)
		def userLogin = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()
		def hasPermission = true
		def commentProject = comment.project
		def errorMsg = ""
		def assignedTo
		if(comment) {
			if (commentProject.id != project.id) {
				hasPermission = false
				log.error "assignToMe: Asset(${comment.id}/${commentProject}) not associated with user(${userLogin}) project (${project})"
				errorMsg = "It appears that you do not have permission to change the specified task"
			}
			if(hasPermission){
				comment.assignedTo = userLogin.person
				if(comment.save(flush:true)){
					assignedTo = userLogin.person.firstName +" " + userLogin.person.lastName
				}
				if (params.status) {
					if (comment.status != params.status) {
						log.warn "assignToMe(), Task (${comment.id}) status changed accidentally while assigning to ${userLogin}"
						def whoDidIt = (comment.status == AssetCommentStatus.DONE) ? comment.resolvedBy : comment.assignedTo
						switch (comment.status) {
							case AssetCommentStatus.STARTED:
								errorMsg = "The task was STARTED by ${whoDidIt}"; break
							case AssetCommentStatus.DONE:
								errorMsg = "The task was COMPLETED by ${whoDidIt}"; break
							default:
								errorMsg = "The task status was changed to '${comment.status}'"
						}
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
