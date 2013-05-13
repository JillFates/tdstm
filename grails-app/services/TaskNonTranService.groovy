/**
 * A subset of the TaskService class for the purpose of updateTaskSuccessors which requires a non-transactional service in order to control when the 
 * transaction actual starts due to the concurrency of the update of tasks through the Quartz Job and the database transaction isolation level.
 * 
 * @author John Martin
 *
 */

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.TaskDependency
import com.tdsops.tm.enums.domain.AssetCommentStatus

import com.tdssrc.grails.GormUtil

class TaskNonTranService {

	static transactional = false

	def securityService
	def jdbcTemplate
	def taskService

	/**
	 * This is invoked by the AssetComment.beforeUpdate method in order to handle any status changes
	 * that may result in the updating of other tasks successor tasks.
	 */
	def updateTaskSuccessors( taskId, status, whomId, isPM, tries ) {
		
		// log.info "updateTaskSuccessors: securityService=${securityService ? securityService.getClass() : 'Undefined'} for task $taskId"

		def whom = Person.read(whomId)
		if (! whom) {
			log.error "updateTaskSuccessors: for task(#:${task.taskNumber} Id:${task.id}) unable to find person ${whomId}"
			return	'failed'		
		}

		def task 
				
		task = AssetComment.read(taskId)

		log.info "updateTaskSuccessors: task(#:${task.taskNumber} Id:${task.id}) invoked by $whom, isPM $isPM, $tries tries"

		if (task.status != status) {
			log.error "updateTaskSuccessors: task(#:${task.taskNumber} Id:${task.id}) status (${task.status}) not as expected '${status}' - $whom, retrying"
			// taskService.triggerUpdateTaskSuccessors(taskId, status, tries, whom, isPM) 
			return 'reschedule'
		}

		// In here to test the rescheduling of the job
		//if (tries < 3) {
		//	return 'reschedule'
		//}

		def success=true
		def msg=''

		def predCountSQL = 'SELECT COUNT(*) FROM asset_comment ac ' + 
			'JOIN task_dependency td ON td.predecessor_id=ac.asset_comment_id AND ac.status<>"' + AssetCommentStatus.DONE + '" ' +
			'WHERE td.asset_comment_id=' 

		AssetComment.withTransaction { trxStatus ->

			// TODO: taskStatusChangeEvent : Add logic to handle READY for the SS predecessor type and correct the current code to not assume SF type
		
			//
			// Now mark any successors as Ready if all of the successors' predecessors are DONE
			//
			if ( status ==	AssetCommentStatus.DONE ) {
				def successorDeps = TaskDependency.findAllByPredecessor(task)
				log.info "updateTaskSuccessors: task(#:${task.taskNumber} Id:${task.id}) found ${successorDeps ? successorDeps.size() : '0'} successors - $whom"
				def i = 1
				successorDeps?.each() { succDepend ->
					def successorTask = succDepend.assetComment
					log.info "updateTaskSuccessors: task(#:${task.taskNumber} Id:${task.id}) Processing (#${i++}) successorTask(#:${successorTask.taskNumber} Id:${successorTask.id}) - $whom"
					
					// If the Successor Task is in the Planned or Pending state, we can check to see if it makes sense to set to READY
					if ([AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING].contains(successorTask.status)) {
						
						// See if there are any predecessor tasks dependencies for the successor that are not DONE
						def sql = "$predCountSQL ${successorTask.id}"
						def predCount = jdbcTemplate.queryForInt(sql)
						//def predCount = jdbcTemplate.queryForInt(predCountSQL, [successorTask.id]) -- this was NOT working...
						// log.info "updateTaskSuccessors: predCount=$predCount, $sql"
						if (predCount > 0) {
							log.info "updateTaskSuccessors: found ${predCount} task(s) not in the DONE state"
						} else {

							def setStatusTo = AssetCommentStatus.READY
							if (successorTask.role == AssetComment.AUTOMATIC_ROLE) {
								// If this is an automated task, we'll mark it DONE instead of READY and indicate that it was completed by
								// the Automated Task person.
								setStatusTo = AssetCommentStatus.DONE
								// whom = taskService.getAutomaticPerson()	// don't need this since it is duplicated
							}
								
							log.info "updateTaskSuccessors: pred task(#:${task.taskNumber} Id:${task.id}) triggering successor task (#:${successorTask.taskNumber} Id:${successorTask.id}) to $setStatusTo by $whom"
							taskService.setTaskStatus(successorTask, setStatusTo, whom, isPM)
							// log.info "taskStatusChangeEvent: successorTask(${successorTask.id}) Making READY - Successful"
							if ( ! successorTask.validate() ) {
								msg = "updateTaskSuccessors: task(#:${task.taskNumber} Id:${task.id}) failed READY of successor task(#:${successorTask.taskNumber} Id:${successorTask.id}) - $whom : " + GormUtil.allErrorsString(successorTask)
								log.error msg
							} else {				
								if ( successorTask.save(flush:true) ) {
									msg = "updateTaskSuccessors: task(#:${task.taskNumber} Id:${task.id}) successor task(#:${successorTask.taskNumber} Id:${successorTask.id}) Saved - $whom"
									success = true
								} else {
									msg = "updateTaskSuccessors: task(#:${task.taskNumber} Id:${task.id}) failed setting successor task(#:${successorTask.taskNumber} Id:${successorTask.id}) to READY - $whom : " + 
										GormUtil.allErrorsString(successorTask)
									log.error msg
									success=false
								}
							}
						}
					} else {
						log.warn "updateTaskSuccessors: taskId(#:${task.taskNumber} Id:${task.id}) found successor task(#:${successorTask.taskNumber} Id:${successorTask.id}) " + 
							"in unexpected status (${successorTask.status} by $whom"
					} 
				} // succDependencies?.each()
			} // if ( status ==	AssetCommentStatus.DONE )

			// Rollback the transaction if we ran into any errors
			if ( ! success ) {
				trxStatus.setRollbackOnly()
			}

		} // AssetComment.withTransaction {}

		if (success) {
			log.info msg
			return 'success'
		} else {
			log.error msg
			return 'failed'
		}
	}

}
