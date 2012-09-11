import grails.converters.JSON

import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentStatus

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
}
