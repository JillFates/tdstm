package net.transitionmanager.service

import com.tds.asset.AssetComment
import com.tds.asset.TaskDependency
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.GormUtil
import net.transitionmanager.domain.Person
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.TransactionStatus

/**
 * A subset of the TaskService class for the purpose of updateTaskSuccessors which requires a non-transactional service in order to control when the
 * transaction actual starts due to the concurrency of the update of tasks through the Quartz Job and the database transaction isolation level.
 *
 * @author John Martin
 */
class TaskNonTranService implements ServiceMethods {

	static transactional = false

	CommentService commentService
	JdbcTemplate jdbcTemplate
	TaskService taskService
	UserPreferenceService userPreferenceService


	/**
	 * This is invoked by the AssetComment.beforeUpdate method in order to handle any status changes
	 * that may result in the updating of other tasks successor tasks.
	 */
	String updateTaskSuccessors(taskId, String status, whomId, boolean isPM, int tries) {

		// log.info "updateTaskSuccessors: securityService=${securityService ? securityService.getClass() : 'Undefined'} for task $taskId"

		def whom = Person.read(whomId)
		if (! whom) {
			log.error "updateTaskSuccessors: for task(#:$task.taskNumber Id:$task.id) unable to find person $whomId"
			return	'failed'
		}

		AssetComment task = AssetComment.read(taskId)

		log.info "updateTaskSuccessors: task(#:$task.taskNumber Id:$task.id) invoked by $whom, isPM $isPM, $tries tries"

		if (task.status != status) {
			log.error "updateTaskSuccessors: task(#:$task.taskNumber Id:$task.id) status ($task.status) not as expected '$status' - $whom, retrying"
			// taskService.triggerUpdateTaskSuccessors(taskId, status, tries, whom, isPM)
			return 'reschedule'
		}

		// In here to test the rescheduling of the job
		//if (tries < 3) {
		//	return 'reschedule'
		//}

		boolean success = true
		String msg = ''

		def predCountSQL = 'SELECT COUNT(*) FROM asset_comment ac ' +
			'JOIN task_dependency td ON td.predecessor_id=ac.asset_comment_id AND ac.status<>"' + AssetCommentStatus.COMPLETED +
			'" WHERE td.asset_comment_id='

		AssetComment.withTransaction { TransactionStatus transactionStatus ->

			// TODO: taskStatusChangeEvent : Add logic to handle READY for the SS predecessor type and correct the current code to not assume SF type

			//
			// Now mark any successors as Ready if all of the successors' predecessors are COMPLETED
			//
			if ( status ==	AssetCommentStatus.COMPLETED ) {
				def successorDeps = TaskDependency.findAllByPredecessor(task)
				log.info "updateTaskSuccessors: task(#:$task.taskNumber Id:$task.id) found ${successorDeps ? successorDeps.size() : '0'} successors - $whom"
				def i = 1
				for (succDepend in successorDeps) {
					AssetComment successorTask = succDepend.assetComment
					log.info "updateTaskSuccessors: task(#:$task.taskNumber Id:$task.id) Processing (#${i++}) successorTask(#:$successorTask.taskNumber Id:$successorTask.id) - $whom"

					// If the Successor Task is in the Planned or Pending state, we can check to see if it makes sense to set to READY
					if ([AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING].contains(successorTask.status)) {

						// See if there are any predecessor tasks dependencies for the successor that are not COMPLETED
						String sql = "$predCountSQL $successorTask.id"
						int predCount = jdbcTemplate.queryForObject(sql, Integer)
						//def predCount = jdbcTemplate.queryForInt(predCountSQL, [successorTask.id]) -- this was NOT working...
						// log.info "updateTaskSuccessors: predCount=$predCount, $sql"
						if (predCount > 0) {
							log.info "updateTaskSuccessors: found $predCount task(s) not in the COMPLETED state"
						} else {

							def setStatusTo = AssetCommentStatus.READY
                            if (successorTask.isAutomatic() && ! successorTask.hasAction()) {
								// If this is an automated task, we'll mark it COMPLETED instead of READY and indicate that it was completed by
								// the Automated Task person.
								setStatusTo = AssetCommentStatus.COMPLETED
								// whom = taskService.getAutomaticPerson()	// don't need this since it is duplicated
							}

							log.info "updateTaskSuccessors: pred task(#:$task.taskNumber Id:$task.id) triggering successor task (#:$successorTask.taskNumber Id:$successorTask.id) to $setStatusTo by $whom"
							taskService.setTaskStatus(successorTask, setStatusTo, whom, isPM)
							// log.info "taskStatusChangeEvent: successorTask($successorTask.id) Making READY - Successful"
							msg = "updateTaskSuccessors: task(#:$task.taskNumber Id:$task.id)"
							// Validates whether a notification should be issued. This should done before validate() because of the implementation of shouldSendNotification
							boolean notificationRequired = commentService.shouldSendNotification(successorTask, whom, false, false, true)
							msg = "updateTaskSuccessors: task(#:$task.taskNumber Id:$task.id)"
							if ( ! successorTask.validate() ) {
								msg = "$msg failed READY of successor task(#:$successorTask.taskNumber Id:$successorTask.id) - $whom : ${GormUtil.allErrorsString(successorTask)}"
								log.error msg
							} else {
								if ( successorTask.save(flush:true) ) {
									msg = "$msg successor task(#:$successorTask.taskNumber Id:$successorTask.id) Saved - $whom"
									success = true
									if (notificationRequired) {
										String tzId = userPreferenceService.timeZone
										String userDTFormat = userPreferenceService.dateFormat
										// Dispatches the email notification
										commentService.dispatchTaskEmail([taskId: successorTask.id, tzId: tzId, isNew: false, userDTFormat: userDTFormat])
									}
								} else {
									msg = "$msg failed setting successor task(#:$successorTask.taskNumber Id:$successorTask.id) to READY - $whom : ${GormUtil.allErrorsString(successorTask)}"
									log.error msg
									success=false
								}
							}
						}
					} else {
						log.warn "updateTaskSuccessors: taskId(#:$task.taskNumber Id:$task.id) found successor task(#:$successorTask.taskNumber Id:$successorTask.id) in unexpected status ($successorTask.status by $whom"
					}
				} // succDependencies?.each()
			} // if ( status ==	AssetCommentStatus.COMPLETED )

			// Rollback the transaction if we ran into any errors
			if ( ! success ) {
				transactionStatus.setRollbackOnly()
			}

		} // AssetComment.withTransaction {}

		if (success) {
			log.info msg
			'success'
		} else {
			log.error msg
			'failed'
		}
	}
}
