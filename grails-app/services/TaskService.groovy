/**
 * The TaskService class contains methods useful for working with Task related domain (a.k.a. AssetComment). Eventually we should migrate away from using AssetComment 
 * to persist our task functionality.
 * 
 * @author John Martin
 *
 */

import org.jsecurity.SecurityUtils

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.CommentNote
import com.tds.asset.TaskDependency
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.RoleTypeGroup
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil

import org.quartz.SimpleTrigger
import org.quartz.Trigger

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class TaskService {

    static transactional = true

	def dataSource
	def jdbcTemplate
	def namedParameterJdbcTemplate
	def securityService
	def quartzScheduler
	def workflowService
	
	static final List runbookCategories = [AssetCommentCategory.SHUTDOWN, AssetCommentCategory.PHYSICAL, AssetCommentCategory.STARTUP]
	static final List categoryList = AssetCommentCategory.getList()
	static final List statusList = AssetCommentStatus.getList()
	
	/**
	 * The getUserTasks method is used to retreive the user's TODO and ALL tasks lists or the count results of the lists. The list results are based 
	 * on the Person and Project.
	 * @param person	A Person object representing the individual to get tasks for
	 * @param project	A Project object representing the project to get tasks for
	 * @param countOnly	Flag that when true will only return the counts of the tasks otherwise method returns list of tasks
	 * @param limitHistory	A numeric value when set will limit the done tasks completed in the N previous days specificed by param 
	 * @param search	A String value when provided will provided will limit the results to just the AssetEntity.AssetTag  
	 * @param sortOn	A String value when provided will sort the task lists by the specified column (limited to present list of columns), default sort on score
	 * @param sortOrder	A String value with valid options [asc|desc], default [desc]
	 * @return Map	A map containing keys all and todo. Values will contain the task lists or counts based on countOnly flag
	 */
    def getUserTasks(person, project, countOnly=false, limitHistory=7, sortOn=null, sortOrder=null, search=null ) {
		// Need to initialize the NamedParameterJdbcTemplate to pass named params to a SQL statement
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource)

		// log.info "getUserTasks: limitHistory=${limitHistory}, sortOn=${sortOn}, sortOrder=${sortOrder}, search=${search}"
		
		// Get the user's roles for the current project
		// TODO : Runbook: getUserTasks - should get the user's project roles instead of global roles
		def roles = securityService.getPersonRoles(person, RoleTypeGroup.STAFF)
		def type=AssetCommentType.TASK
		
		def now = TimeUtil.nowGMT()
		def minAgo = TimeUtil.adjustSeconds(now, -60)
		// log.info "getUserTasks: now=${now}, minAgo=${minAgo}"

		// List of statuses that user should be able to see in when soft assigned to others and user has proper role
		def statuses = [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED, AssetCommentStatus.COMPLETED, AssetCommentStatus.HOLD]
		
		def sqlParams = [projectId:project.id, assignedToId:person.id, type:type, roles:roles, statuses:statuses]
		
		//log.error "person:${person.id}"
		// Find all Tasks assigned to the person OR assigned to a role that is not hard assigned (to someone else)
		
		StringBuffer sql = new StringBuffer("""SELECT t.asset_comment_id AS id, t.comment, t.est_finish AS estFinish, t.last_updated AS lastUpdated,
		 	t.asset_entity_id AS assetEntity, t.status, t.assigned_to_id AS assignedTo, IFNULL(a.asset_name,'') AS assetName, t.role,
		 	t.task_number AS taskNumber, t.est_finish AS estFinish, t.due_date AS dueDate, p.first_name AS firstName, p.last_name AS lastName,
			t.status_updated AS statusUpdated, t.hard_assigned AS hardAssigned""")

		// Add in the Sort Scoring Algorithm into the SQL if we're going to return a list
		if ( ! countOnly) {
			sqlParams << [now:now, minAgo:minAgo]
			
			/*
				The objectives are sort the list descending in this order:
					- HOLD 900
						+ last updated factor ASC
					- DONE recently (60 seconds), to allow undo 800
						+ actual finish factor DESC	
					- STARTED tasks     700
						- Hard assigned to user	+55
						- by the user	+50
						- + Est Start Factor to sort ASC
					- READY tasks		600
						- Hard assigned to user	+55
						- Assigned to user		+50
						- + Est Start factor to sort ASC
					- PENDING tasks		500
						- + Est Start factor to sort ASC
					- DONE tasks		200
						- Assigned to User	+50 
						- + actual finish factor DESC
						- DONE by others	+0 + actual finish factor DESC
					- All other statuses ?
					- Task # DESC (handled outside the score)
				
				The inverse of Priority will be added to any score * 5 so that Priority tasks bubble up above hard assigned to user
				
				DON'T THINK THIS APPLIES ANY MORE - Category of Startup, Physical, Moveday, or Shutdown +10
				- If duedate exists and is older than today +5
				- Priority - Six (6) - <priority value> (so a priority of 5 will add 1 to the score and 1 adds 5)
			*/
			sql.append(""", 
				( ( CASE t.status 
				WHEN '${AssetCommentStatus.HOLD}' THEN 900
				WHEN '${AssetCommentStatus.DONE}' THEN IF(t.status_updated >= :minAgo, 800, 200) + UNIX_TIMESTAMP(t.status_updated) / UNIX_TIMESTAMP(:now)
				WHEN '${AssetCommentStatus.STARTED}' THEN 700 + 1 - UNIX_TIMESTAMP(IFNULL(t.est_start,:now)) / UNIX_TIMESTAMP(:now)
				WHEN '${AssetCommentStatus.READY}' THEN 600 + 1 - UNIX_TIMESTAMP(IFNULL(t.est_start,:now)) / UNIX_TIMESTAMP(:now)
				WHEN '${AssetCommentStatus.PENDING}' THEN 500 + 1 - UNIX_TIMESTAMP(IFNULL(t.est_start,:now)) / UNIX_TIMESTAMP(:now)
				ELSE 0
				END) +  
				IF(t.assigned_to_id=:assignedToId AND t.status IN('${AssetCommentStatus.STARTED}','${AssetCommentStatus.READY}'), IF(t.hard_assigned=1, 55, 50), 0) +
				IF(t.assigned_to_id=:assignedToId AND t.status='${AssetCommentStatus.DONE}',50, 0) +
				(6 - t.priority) * 5) AS score """)			
		}
		
		// Add Successor Count
		sql.append(', (SELECT count(*) FROM task_dependency td WHERE predecessor_id=t.asset_comment_id) AS successors ')
		
		// The WHERE clause is going to find tasks that are assigned to the user (hard or soft) OR tasks that are assigned to the role(s) that
		// the user has unless the task is hard assigned to someone else in one of the groups.
		sql.append("""FROM asset_comment t 
			LEFT OUTER JOIN asset_entity a ON a.asset_entity_id = t.asset_entity_id 
            LEFT OUTER JOIN person p ON p.person_id = t.assigned_to_id 
			WHERE t.project_id=:projectId AND t.comment_type=:type AND 
			( t.assigned_to_id=:assignedToId OR 
				(t.role IN (:roles) AND t.status IN (:statuses) AND t.hard_assigned=0 OR (t.hard_assigned=1 AND t.assigned_to_id=:assignedToId) ) ) """)
		
		search = org.apache.commons.lang.StringUtils.trimToNull(search)
		if (search) {
			// Join on the AssetEntity and embed the search criteria
			sql.append('AND a.asset_tag=:search ')
			// Add the search param to the sql params
			sqlParams << [ search:search ]
		}

		// Add filter for limitHistory
		if (limitHistory) {
			if (limitHistory instanceof Integer && limitHistory >= 0) {
				sql.append("AND (t.date_resolved IS NULL OR t.date_resolved >= SUBDATE(NOW(), ${limitHistory})) ")
			} else {
				log.warn "getUserTasks: invalid parameter value for limitHistory (${limitHistory})"
			}
		}		
		
		if ( ! countOnly ) {
			// If we are returning the lists, then let's deal with the sorting
			sql.append('ORDER BY ')
			def sortableProps = ['number_comment', 'comment', 'estFinish', 'lastUpdated', 'status', 'assetName', 'assignedTo']
			def sortAndOrder = null
			if (sortOn) {
				if ( sortableProps.contains(sortOn) ) {
					sortOrder = ['asc','desc'].contains(sortOrder) ? sortOrder : 'asc'
					sortOrder = sortOrder.toUpperCase()
					switch (sortOn) {
						case 'assignedTo':
							sortAndOrder = "p.first_name ${sortOrder}, p.last_name ${sortOrder}"; break
						case  'number_comment':
							sortAndOrder = "taskNumber ${sortOrder}, comment ${sortOrder}"; break
						default:
							sortAndOrder = "${sortOn} ${sortOrder}"
					}
					sql.append(sortAndOrder)
				} else {
					log.warn "getUserTasks: called with invalid sort property [${sortOn}]"
					sortAndOrder=null
				}
			}
			// add the score sort either as addition or as only ORDER BY parameteters
			sql.append( (sortAndOrder ? ', ' : '') + 'score DESC, task_number ASC' )
		}
		
		//log.info "getUserTasks: SQL: " + sql.toString()
		//log.info "getUserTasks: SQL params: " + sqlParams
		// Get all tasks from the database and then filter out the TODOs based on a filtering
		def allTasks = namedParameterJdbcTemplate.queryForList( sql.toString(), sqlParams )
		// def allTasks = jdbcTemplate.queryForList( sql.toString(), sqlParams )
		def format = "yyyy/MM/dd hh:mm:ss"
		def minAgoFormat = minAgo.format(format)
		def todoTasks = allTasks.findAll { task ->
			if (task.taskNumber==374) { log.info "getUserTasks: minAgoFormat:${minAgoFormat} [${task.statusUpdated?.format(format)}]"}
			task.status == AssetCommentStatus.READY || ( task.status == AssetCommentStatus.STARTED && task.assignedTo == person.id ) ||
			(task.status == AssetCommentStatus.DONE && task.assignedTo == person.id && task.statusUpdated?.format(format) >= minAgoFormat )
		}

		if (countOnly) {
			return ['all':allTasks.size(), 'todo':todoTasks.size()]
		} else {
			return ['all':allTasks, 'todo':todoTasks]
		}
    }

    def getUserTasksOriginal(person, project, search=null, sortOn='c.dueDate', sortOrder='ASC, c.lastUpdated DESC' ) {
		
		// Get the user's roles for the current project
		// TODO : Runbook: getUserTasks - should get the user's project roles instead of global roles
		def roles = securityService.getPersonRoles(person, RoleTypeGroup.STAFF)
		def type=AssetCommentType.TASK
		
		// List of statuses that user should be able to see in when soft assigned to others and user has proper role
		def statuses = [AssetCommentStatus.PLANNED.toString(), AssetCommentStatus.PENDING.toString(), 
			AssetCommentStatus.READY.toString(), AssetCommentStatus.STARTED.toString() ]
		
		def sqlParams = [project:project, assignedTo:person, type:type, roles:roles, statuses:statuses]
		
		//log.error "person:${person.id}"
		// Find all Tasks assigned to the person OR assigned to a role that is not hard assigned (to someone else)
		
		search = org.apache.commons.lang.StringUtils.trimToNull(search)

		StringBuffer sql = new StringBuffer('FROM AssetComment AS c')
		if (search) {
			// Join on the AssetEntity and embed the search criteria
			sql.append(', AssetEntity AS a WHERE c.assetEntity.id=a.id AND a.assetTag=:search AND ')
			// Add the search param to the sql params
			sqlParams << [ search:search ]
		} else {
			sql.append(' WHERE ')
		}
		sql.append('c.project=:project AND c.commentType=:type ')
		// TODO : runbook : my tasks should only show mine.  All should show mine and anything that my teams has started or complete
		sql.append('AND ( c.assignedTo=:assignedTo OR ( c.role IN (:roles) AND c.status IN (:statuses) AND c.hardAssigned != 1 ) ) ')

		// TODO : Security : getUserTasks - sortOn/sortOrder should be defined instead of allowing user to INJECT, also shouldn't the column name have the 'a.' prefix?	
		// Add the ORDER to the SQL
		// sql.append("ORDER BY ${sortOn} ${sortOrder}")
		
		// log.error "SQL for userTasks: " + sql.toString()
		
		def allTasks = AssetComment.findAll( sql.toString(), sqlParams )
		def todoTasks = allTasks.findAll { [AssetCommentStatus.READY, AssetCommentStatus.STARTED].contains(it.status) }

		return ['all':allTasks, 'todo':todoTasks]
    }
	
	/**
	 * Overloaded version of the setTaskStatus that has passes the logged in user's person object to the main method
	 * @param task
	 * @param status
	 * @return AssetComement	task that was updated by method
	 */
	// Refactor to accept the Person
	def setTaskStatus( task, status) {
		def whom = securityService.getUserLoginPerson()
		def isPM = securityService.hasRole("PROJ_MGR")
		return setTaskStatus( task, status, whom, isPM )
	}

	/**
	 * Used to set the status of a task, which will perform additional updated based on the state
	 * @param whom		A Person object that represents the person updating the task
	 * @param task		A AssetComment (aka Task) to change the status on
	 * @param status	A String representing the status code (AssetCommentStatus)
	 * @return AssetComment the task object that was updated
	 */
	// TODO : We should probably refactor this into the AssetComment domain class as setStatus
	def setTaskStatus(task, status, whom, isPM) {
		
		// If the current task.status or the persisted value equals the new status, then there's nutt'n to do.
		if (task.status == status || task.getPersistentValue('status') == status) {
			return
		}

		def now = TimeUtil.nowGMT()
		
		// First thing to do, set the status
		task.status = status
		
		// Update the time that the status has changed which is used to indentify tasks that have not been acted on appropriately
		task.statusUpdated = now
		
		def previousStatus = task.getPersistentValue('status')
		// Determine if the status is being reverted (e.g. going from DONE to READY)
		def revertStatus = compareStatus(previousStatus, status) > 0

		log.info "setTaskStatus - task(#:${task.taskNumber} Id:${task.id}) status=${status}, previousStatus=${previousStatus}, revertStatus=${revertStatus} - $whom"

		// Setting of AssignedTO:
		//
		// We are going to update the AssignedTo when the task is marked Started or Done unless the current user has the 
		// PROJ_MGR role because we want to allow for the PM to mark a task as being started or done on behalf of someone else. The only
		// time we'll set the PM to the AssignedTo property is if it is presently unassigned.
		// 
		// Setting Status Backwards (e.g. DONE back to READY):
		//
		// In the rare case that we need to set the status back from a progressive state, we may need to undue some stuff (e.g. mark unresolved, clear 
		// resolvedBy, etc).  We will log a note on the task whenever this occurs.
		//
		if (revertStatus) {
			if (previousStatus == AssetCommentStatus.DONE) {
				task.resolvedBy = null
				task.actFinish = null
				// isResolved = 0 -- should be set in the domain class automatically
			}
			// Clear the actual Start if we're moving back before STARTED
			if ( compareStatus(AssetCommentStatus.STARTED, status) > 0) {
				task.actStart = null				
			}
			
			if ( task.isRunbookTask() && previousStatus ) {
				addNote( task, whom, "Reverted status from '${previousStatus}' to '${status}'")
			}
			
			// TODO : Runbook Look at the successors and do something about them
			// Change READY successors to PENDING
			// Put STARTED task on HOLD and add note that predecessor task was reverted
			
		}

		// --------
		// Now update the task properties according to the new status
		// --------

		// Properly handle assignment if the user is the PM
		def assignee = whom
		if (task.assignedTo && isPM) {
			assignee = task.assignedTo
		}
		
		switch (status) {
			case AssetCommentStatus.HOLD:
				addNote( task, whom, "Placed task on HOLD, previously was '$previousStatus'")
				break
				
			case AssetCommentStatus.STARTED:
				task.assignedTo = assignee
				// We don't want to loose the original started time if we are reverting from DONE to STARTED
				if (! revertStatus ) {
					task.actStart = now
					if (task.isRunbookTask()) addNote( task, whom, "Task Started")
				}
				break
				
			case AssetCommentStatus.DONE:
				if ( task.isDirty('status') && task.getPersistentValue('status') != status) {						
					triggerUpdateTaskSuccessors(task.id, status)
				}
				task.assignedTo = assignee
				task.resolvedBy = assignee
				task.actFinish = now
				if (task.isRunbookTask()) addNote( task, whom, "Task Completed")
				break
				
			case AssetCommentStatus.TERMINATED:
				task.resolvedBy = assignee
				task.actFinish = now
				break
		}
		
		return task
	}
 	
	/**
	* Triggers the invocation of the UpdateTaskSuccessorsJob Quartz job for the specified task id
	* @param taskId
	* @return void
	*/
	def triggerUpdateTaskSuccessors(taskId, status) {
		def task = AssetComment.read(taskId)
		if (! task) {
			log.error "triggerUpdateTaskSuccessors - unable to find task id $taskId"
		} else {
			def whom = securityService.getUserLoginPerson()
			def isPM = securityService.hasRole("PROJ_MGR")
			long startTime = System.currentTimeMillis() + (250L)
			Trigger trigger = new SimpleTrigger("tm-updateTaskSuccessors-${taskId}" + System.currentTimeMillis(), null, new Date(startTime) )
	        trigger.jobDataMap.putAll( [ taskId:taskId, whomId:whom.id, status:status, isPM:isPM] )
			trigger.setJobName('UpdateTaskSuccessorsJob')
			trigger.setJobGroup('tdstm')
  
			def result = quartzScheduler.scheduleJob(trigger)
			log.info "triggerUpdateTaskSuccessors for task(#:${task.taskNumber} Id:${taskId}), status=$status, scheduled=$result - $whom"
		}
		
	}
	
	/**
	 * This is invoked by the AssetComment.beforeUpdate method in order to handle any status changes
	 * that may result in the updating of other tasks successor tasks.
	 */
	def updateTaskSuccessors( taskId, status, whomId, isPM ) {
		log.info "updateTaskSuccessors - securityService=${securityService ? securityService.getClass() : 'Undefined'}"
		def whom = Person.read(whomId)
		if (! whom) {
			log.error "updateTaskSuccessors - for task(#:${task.taskNumber} Id:${task.id}) unable to find person ${whomId}"
			return			
		}
		
		def task 
		
		// This tasks will run parallel with the thread updating the current task to the state passed to this method. Therefore
		// we need to make sure that the task has been updated.  We'll try for 3 seconds before giving up.
		def cnt = 10
		log.info "updateTaskSuccessors - securityService=${securityService ? securityService.getClass() : 'Undefined'}"
		
		while (cnt-- > 0) {
			task = AssetComment.get(taskId)
			if (! task) {
				log.error "updateTaskSuccessors - unable to find taskId ${taskId}"
				return
			}
			if (task.status == status) {
				break
			} else {
				this.sleep(300)
			}			
		}
		if (task.status != status) {
			log.error "updateTaskSuccessors - task(#:${task.taskNumber} Id:${task.id}) status (${task.status}) not as expected '${status}' - $whom"
			return
		}
		log.info "updateTaskSuccessors - securityService=${securityService ? securityService.getClass() : 'Undefined'}"
		
		log.info "updateTaskSuccessors: Processing task(#:${task.taskNumber} Id:${task.id}) ${task} - $whom"
		//	def currStatus = task.status
			
		// TODO: taskStatusChangeEvent : Add logic to handle READY for the SS predecessor type and correct the current code to not assume SF type
	
		//
		// Now mark any successors as Ready if all of the successors' predecessors are DONE
		//
		if ( status ==  AssetCommentStatus.DONE ) {
			def successorDeps = TaskDependency.findAllByPredecessor(task)
			log.info "updateTaskSuccessors - task(#:${task.taskNumber} Id:${task.id}) found ${successorDeps ? successorDeps.size() : '0'} successors - $whom"
			def i = 1
			successorDeps?.each() { succDepend ->
				def successorTask = succDepend.assetComment
				log.info "updateTaskSuccessors - task(#:${task.taskNumber} Id:${task.id}) Processing (#${i++}) successorTask(#:${successorTask.taskNumber} Id:${successorTask.id}) - $whom"
				
				// If the Successor Task is in the Planned or Pending state, we can check to see if it makes sense to set to READY
				if ([AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING].contains(successorTask.status)) {
					
					// Find all predecessor/tasks dependencies for the successor, other than the current task, and make sure they are completed
					def dependencies = TaskDependency.findAllByAssetComment(successorTask)
					def makeReady=true
					def predTaskNumbers=''
					if (dependencies) {
						dependencies.each() { predTaskNumbers += "#${it.predecessor.taskNumber}," }
					} else {
						predTaskNumbers = 'NONE'
					}
					log.info "updateTaskSuccessors - task(#:${task.taskNumber} Id:${task.id}) successorTask(#:${successorTask.taskNumber} Id:${successorTask.id}) " + 
						"has other predecessors> $predTaskNumbers - $whom"
					dependencies?.each() { dependency ->
						// TODO : runbook : taskStatusChangeEvent - add the dependency type (SS,FS) logic here
						if (makeReady && dependency.predecessor.status != AssetCommentStatus.COMPLETED) { 
							log.info "updateTaskSuccessors - task(#:${task.taskNumber} Id:${task.id}) successorTask(#:${successorTask.taskNumber} Id:${successorTask.id}) " +
								" predecessor(#:${dependency.predecessor.taskNumber} Id:${dependency.predecessor.id}) status '${dependency.predecessor.status}' kicked it out"
							makeReady = false
						}
					}
					if (makeReady) {
						/*
						def taskToReady = AssetComment.get(successorTask.id)
						setTaskStatus(taskToReady, AssetCommentStatus.READY)
						if ( ! taskToReady.validate() ) {
							log.error "taskStatusChangeEvent: Failed Readying successor task [${taskToReady.id} " + GormUtil.allErrorsString(taskToReady)
						}
//							if ( taskToReady.save(flush:true) ) {
						if ( taskToReady.save() ) {
							log.info "taskStatusChangeEvent: successorTask(${taskToReady.id}) Saved"
							return
						}
						if (! (  successorTask.validate() && successorTask.save(flush:true) ) ) {
							log.error "Failed Readying successor task [${taskToReady.id} " + GormUtil.allErrorsString(taskToReady)
						}
						*/
							
						log.info "updateTaskSuccessors - task(#:${task.taskNumber} Id:${task.id}) setting successorTask(#:${successorTask.taskNumber} Id:${successorTask.id}) to READY  - $whom"
						setTaskStatus(successorTask, AssetCommentStatus.READY, whom, isPM)
						// log.info "taskStatusChangeEvent: successorTask(${successorTask.id}) Making READY - Successful"
						if ( ! successorTask.validate() ) {
							log.error "updateTaskSuccessors: task(#:${task.taskNumber} Id:${task.id}) failed READY of successor task(#:${successorTask.taskNumber} Id:${successorTask.id}) - $whom : " + GormUtil.allErrorsString(successorTask)
						}
						
						// log.info "taskStatusChangeEvent: task=${task.id} successorTask(${successorTask.id}) Made it by validate() "
						
						/*
						 * OK - For anybody looking at this logic and questioning it, let me first say I'm confused too.  For some 
						 * reason we do not need to invoke the save method here on the successor tasks that are being updated.  Some how
						 * GORM is handling it auto-magically. The code originally was doing the save but would get a strange error:
						 * 		[http-8080-1] ERROR errors.GrailsExceptionResolver  - Index: 1, Size: 0
						 * 		java.lang.IndexOutOfBoundsException: Index: 1, Size: 0
						 * 		at java.util.ArrayList.RangeCheck(ArrayList.java:547)
						 * I therefore have commented it out.
						 * John 8/2012
						 */
						if ( successorTask.save(flush:true) ) {
							log.info "updateTaskSuccessors - task(#:${task.taskNumber} Id:${task.id}) successor task(#:${successorTask.taskNumber} Id:${successorTask.id}) Saved - $whom"
							return
						} else {
						//if (! (  successorTask.validate() && successorTask.save(flush:true) ) ) {
							log.error "updateTaskSuccessors - task(#:${task.taskNumber} Id:${task.id}) failed setting successor task(#:${successorTask.taskNumber} Id:${successorTask.id}) to READY - $whom : " + 
								GormUtil.allErrorsString(successorTask)
							
							 throw new TaskCompletionException("Unable to READY task ${successorTask} due to " +
								 GormUtil.allErrorsString(successorTask) )
						}
					}
				} else {
					log.warn "updateTaskSuccessors - taskId(#:${task.taskNumber} Id:${task.id}) found successor task(#:${successorTask.taskNumber} Id:${successorTask.id}) " + 
						"in unexpected status (${successorTask.status}  - $whom"
				} 
			} // succDependencies?.each()
		}
	}


	/**
	 * Used to determine the CSS class name that should be used when presenting a task, which is based on the task's status	
	 * @param status
	 * @return String The appropriate CSS style or task_na if the status is invalid
	 */
	def getCssClassForStatus( status ) {
		def css = 'task_na'
		
		if (AssetCommentStatus.list.contains(status)) {
			css = "task_${status.toLowerCase()}"
		}
		// log.error "getCssClassForStatus('${status})=${css}"
		return css 
	}

	/**
	 * Returns the HTML for a SELECT control that contains list of tasks based on a TaskDependency. This logic assumes that the TaskDependency(ies)
	 * pre-exist.
	 * @param taskDependency - reference to the dependency relationship of a task (aka assetComment)
	 * @param task - the task (aka assetComment) that we're building a Predecessor SELECT for
	 * @param project - a project object (optional default to user's current project)
	 * @param idPrefix - prefix to use for the control's ID (default: taskDependencyEditId)
	 * @param name - the name of the control (default taskDependencyEdit)
	 * @return String - HTML of a SELECT control
	 */
	def genSelectForTaskDependency(taskDependency, task, project=null, idPrefix='taskDependencyEditId', name='taskDependencyEdit') {
		//def sw = new org.springframework.util.StopWatch("genSelectForTaskDependency Stopwatch") 
		//sw.start("Get predecessor")
		def predecessor = taskDependency.predecessor
		def category = predecessor.category
		
		if (! project) {
			project = securityService.getUserCurrentProject()
		}
		def projectId = project.id
		def moveEvent = task.moveEvent
		def queryForPredecessor = new StringBuffer("""FROM AssetComment a WHERE a.project=${projectId} \
			AND a.category='${category}'\
			AND a.commentType='${AssetCommentType.TASK}'\
			AND a.id != ${task.id} """)
		if (moveEvent) {
			queryForPredecessor.append("AND a.moveEvent.id=${moveEvent.id}")
		}
		queryForPredecessor.append(""" ORDER BY a.taskNumber ASC""")
		// log.info "genSelectForTaskDependency - SQL ${queryForPredecessor.toString()}"
		def predecessors = AssetComment.findAll(queryForPredecessor.toString())
		def paramsMap = [selectId:"${idPrefix}_${taskDependency.id}", selectName:"${name}", options:predecessors, optionKey:"id",
			     			 optionSelected:predecessor.id, javascript:"onChange='this.className=this.options[this.selectedIndex].className'",
							 optionClass:"", selectClass:HtmlUtil.getCssClassForStatus(predecessor.status)]
		def selectControl = HtmlUtil.generateSelect( paramsMap )
		//sw.stop()
		//log.info "genSelectForTaskDependency - Stopwatch: ${sw.prettyPrint()}"
		return selectControl
	}
	
	/**
	* Used to generate a SELECT control for a project and category with an optional task. When a task is presented the list will
	* also be filtered on tasks from the moveEvent.
	* If a taskId is included, the SELECT will have CSS ID taskDependencyEditId otherwise taskDependencyId and the SELECT name of 
	* taskDependencyEdit or taskDependencySave accordingly since having an Id means that we're in edit mode vs create mode.
	*
	* @param project - the project object to filter tasks to include
	* @param category - a task category to filter on (optional) 
	* @param taskId - an optional task Id that the filtering will use to eliminate as an option and also filter on it's moveEvent
	* @return String the SELECT control
	*/
	def genSelectForPredecessors(project, category, task) {	
		
		StringBuffer query = new StringBuffer("FROM AssetComment a WHERE a.project=${project.id} AND a.commentType='${AssetCommentType.TASK}' ")
		if (category) {
			if ( categoryList.contains(category) ) {
				query.append("AND a.category='${category}' ")
			} else {
				log.warn "genSelectForPredecessors - unexpected category filter '$category'"
				category=''
			}
		}

		// If there is a task we can add some additional filtering like not including self in the list of predecessors and filtering on moveEvent
		if (task) { 
			if (! category && task.category) {
				query.append("AND a.category='${task.category}' ")
			}				
			query.append("AND a.id != ${task.id} ")
		
			if (task.moveEvent) {
				query.append("AND a.moveEvent.id=${task.moveEvent.id} ")
			}
		}
		
		// Add the sort and generate the list
		query.append('ORDER BY a.taskNumber ASC')
		def taskList = AssetComment.findAll( query.toString() )
		
		// Build the SELECT HTML
		def cssId = task ? 'taskDependencyEditId' : 'taskDependencyId'
	    def selectName = task ? 'taskDependencyEdit' : 'taskDependencySave'
		def firstOption = [value:'', display:'Please Select']
		def paramsMap = [ selectId:cssId, selectName:selectName, options:taskList, optionKey:'id', firstOption:firstOption,
						  javascript:"onChange='this.className=this.options[this.selectedIndex].className'",
						  optionClass:""]
		def selectControl = HtmlUtil.generateSelect( paramsMap )
		
		return selectControl
	}
	
	/**
	 * Returns Boolean value to warn status is overriding or not
	 * @param AssetComment as task
	 * @return statusWarn as can
	 */
	def canChangeStatus ( task ){
		def can = true
		// TODO : runbook - add logic to allow PM to change status anytime.
		if ([AssetCommentCategory.SHUTDOWN, AssetCommentCategory.PHYSICAL, AssetCommentCategory.STARTUP].contains( task.category ) && 
			! [ AssetCommentStatus.READY, AssetCommentStatus.STARTED ].contains( task.status )) {
			  can = false 
		}
		return can
	}
	
	/**
	 * Comparies two statuses and returns -1 if 1st is before 2nd, 0 if equal, or 1 if 1st is after the 2nd
	 * @param from	status moving from
	 * @param to 	status moving to
	 * @return int 
	 */
	def compareStatus( from, to ) {
		if (! from && to ) return 1
		if ( from && !to ) return -1
		
		def fidx = statusList.findIndexOf { it == from }
		def tidx = statusList.findIndexOf { it == to }
		
		// TODO - need to solve issue when the status from or to is unknown.
		
		return fidx < tidx ? -1 : fidx == tidx ? 0 : 1
	}
	
	/**
	 * Used to add a note to a task
	 * @param task	The task (aka AssetComment) to add a note to
	 * @param person	The Person object that is creating the note
	 * @param note	A String that represents the note
	 */
	def addNote( def task, def person, def note, def isAudit=1 ) {
		def taskNote = new CommentNote();
		taskNote.createdBy = person
		taskNote.note = note
		taskNote.isAudit = isAudit
		if ( taskNote.hasErrors() ) {
			log.error "addNote: failed to save note : ${GormUtil.allErrorsString(taskNote)}"
			return false
		} else {
			task.addToNotes(taskNote)
			return true
		}
	}
	
	/**
	 * 	Used to clear all Task data that are associated to a specified move event  
	 * @param moveEventId
	 * @return void
	 */
	def cleanTaskData(def moveEventId) {
		// We want to find all AssetComment records that have the MoveEvent or are associate with assets that are 
		// with move bundles that are part of the move event. With that list, we will reset the
		// AssetComment status to PENDING by default or READY if there are no predecessors and clear several other properties.
		// We will also delete Task notes that are auto generated during the runbook execution.
		log.info("cleanTaskData() was called for moveEvent(${moveEventId})")
		def tasksMap = getMoveEventTaskLists(moveEventId)
		
		def updateSql = "UPDATE AssetComment ac \
			SET ac.status = :status, ac.actStart = null, ac.actStart = null, ac.dateResolved = null, ac.resolvedBy = null, \
				ac.isResolved=0, ac.statusUpdated = null \
			WHERE ac.id in (:ids)"
			
		if (tasksMap.tasksWithPred.size() > 0) {
			AssetComment.executeUpdate(updateSql, ['status':AssetCommentStatus.PENDING, 'ids':tasksMap.tasksWithPred ] )
		}
		if (tasksMap.tasksNoPred.size() > 0) {
			AssetComment.executeUpdate(updateSql, ['status':AssetCommentStatus.READY, 'ids':tasksMap.tasksNoPred ] )
		}
		if (tasksMap.tasksWithNotes.size() > 0) {
			CommentNote.executeUpdate("DELETE FROM CommentNote cn WHERE cn.assetComment.id IN (:ids) AND cn.isAudit=1", 
				[ 'ids':tasksMap.tasksWithNotes ] )
		}
	}
	
	/**
	 * 	Used to delete all Task data that are associated to a specified move event
	 * @param moveEventId
	 * @return void
	 * 
	 */
	def deleteTaskData(def moveEventId){
		log.info("deleteTaskData() was called for moveEvent(${moveEventId})")
		def tasksMap = getMoveEventTaskLists(moveEventId)
		
		if (tasksMap.tasksWithNotes.size() > 0) {
			CommentNote.executeUpdate('DELETE FROM CommentNote cn WHERE cn.assetComment.id IN (:ids)', ['ids':tasksMap.tasksWithNotes] )
		}
		if (tasksMap.tasksAll.size() > 0) {
			TaskDependency.executeUpdate('DELETE FROM TaskDependency td WHERE td.assetComment.id IN (:ids) OR td.predecessor.id IN (:ids)', ['ids':tasksMap.tasksAll] )
			AssetComment.executeUpdate('DELETE FROM AssetComment ac WHERE ac.id IN (:ids)', ['ids':tasksMap.tasksAll] )
		}
	}
	
	/** 
	 * Returns a map containing several lists of AssetComment ids for a specified moveEvent that include keys
	 * tasksWithPred, tasksNoPred, TasksAll and TasksWithNotes
	 * @param moveEventID
	 * @return map of tasksWithPred, tasksNoPred, TasksAll and TasksWithNotes
	 */
	def getMoveEventTaskLists(def moveEventId) {
		
		def query = """SELECT c.asset_comment_id AS id, 
				( SELECT count(*) FROM task_dependency t WHERE t.asset_comment_id = c.asset_comment_id ) AS predCount,
				( SELECT count(*) FROM comment_note n WHERE n.asset_comment_id = c.asset_comment_id ) AS noteCount
			FROM asset_comment c 
			WHERE 
				c.move_event_id = ${moveEventId} AND
				c.category IN (${GormUtil.asQuoteCommaDelimitedString(runbookCategories)})"""
		log.info "getMoveEventTaskLists: query = ${query}"
		def tasksList = jdbcTemplate.queryForList(query)

		def tasksWithPred=[]
		def tasksNoPred=[]
		def tasksWithNotes=[]
		def tasksAll=[]
		
		// Iterate over the results and add the ids to the appropriate arrays
		tasksList.each {
			tasksAll << it.id 
			if (it.predCount == 0) {
				tasksNoPred << it.id
			} else {
				tasksWithPred << it.id
			}
			if (it.noteCount > 0) tasksWithNotes << it.id
		}
		
		return [tasksAll:tasksAll, tasksWithPred:tasksWithPred, tasksNoPred:tasksNoPred, tasksWithNotes:tasksWithNotes]
	}
	
	 /**
	  * Provides a list of upstream task predecessors that have not been completed for a given task. Implemented by recursion
	  * @param task
	  * @param taskList list of predecessors collected during the recursive lookup
	  * @return list of predecessor tasks
	  */
	 // TODO: runbook : Refactor getIncompletePredecessors() into the TaskService,
	 def getIncompletePredecessors(task, taskList=[]) {
		 task.taskDependencies?.each { dependency ->
			 def predecessor = dependency.predecessor

			 // Check to see if we already have the predecessor in the list where there were multiple predecessors that referenced one another
			 def skip=false
			 for (int i=0; i < taskList.size(); i++) {
				 if (taskList[i].id == predecessor.id ) {
				 	skip = true
					break
			 	}
			 }
			 
			 // If it is not Completed, add it to the list and recursively look for more predecessors
			 if (! skip && ! predecessor.isDone()) {
				 taskList << predecessor
				 getIncompletePredecessors(predecessor, taskList)
			 }
		 }
		 return taskList
	 }
	
	 /**
	  * Used to set the state of a task (aka AssetComment) to completed and update any dependencies to the ready state appropriately. This will 
	  * complete predecessor tasks if completePredecessors=true.
	  * @param task
	  * @return
	  */
	 def completeTask( taskId, userId, completePredecessors=false ) {
		 log.info "completeTask: taskId:${taskId}, userId:${userId}"
		 def task = AssetComment.get(taskId)
		 def userLogin = UserLogin.get(userId)
		 def tasksToComplete = [task]
		 
		 def predecessors = getIncompletePredecessors(task)
		 
		 // If we're not going to automatically complete predecessors (Supervisor role only), the we can't do anything 
		 // if there are any incomplete predecessors.
		 if (! completePredecessors && predecessors.size() > 0) {
			throw new TaskCompletionException("Unable to complete task [${task.taskNumber}] due to incomplete predecessor task # (${predecessors[0].taskNumber})")
		 }

		 // If automatically completing predecessors, check first to see if there are any on hold and stop 		 
		 if (completePredecessors) {
			 // TODO : Runbook - if/when we want to complete all predecessors, perhaps we should do it recursive since this logic only goes one level I believe.
			 if ( predecessors.size() > 0 ) {
				 // Scan the list to see if any of the predecessor tasks are on hold because we don't want to do anything if that is the case
				 def hasHold=false
				 for (int i=0; i < predecessors.size(); i++) {
					 if (predecessors[i].status == AssetCommentStatus.HOLD) {
						hasHold = true
						break
					 }
				 }
				 if (hasHold) {
					 throw new TaskCompletionException("Unable to complete task [${task.taskNumber}] due to predecessor task # (${predecessors[0].taskNumber}) being on Hold")
				 }
			 }
		 
			 tasksToComplete += predecessors
		 }

	 	// Complete the task(s)
		tasksToComplete.each() { activeTask ->
			// activeTask.dateResolved = GormUtil.convertInToGMT( "now", "EDT" )
			// activeTask.resolvedBy = userLogin.person
			setTaskStatus(activeTask, AssetCommentStatus.DONE)
			if ( ! (activeTask.validate() && activeTask.save(flush:true)) ) {
				throw new TaskCompletionException("Unable to complete task # ${activeTask.taskNumber} due to " +
					GormUtil.allErrorsString(activeTask) )
				log.error "Failed Completing task [${activeTask.id} " + GormUtil.allErrorsString(successorTask)
				return "Unable to complete due to " + GormUtil.allErrorsString(successorTask)
			}
		} 
		
		//
		// Now Mark any successors as Ready if all predecessors are Completed
		//
		def succDependencies = TaskDependency.findByPredecessor(task)
		succDependencies?.each() { succDepend ->
			def successorTask = succDepend.assetComment
			
			// If the Task is in the Planned or Pending state, we can check to see if it makes sense to set to READY
			if ([AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING].contains(successorTask.status)) {
				// Find all predecessors for the successor, other than the current task, and make sure they are completed
				def predDependencies = TaskDependency.findByAssetCommentAndPredecessorNot(successorTask, task)
				def makeReady=true
				predDependencies?.each() { predDependency
					def predTask = predDependency.assetEntity
					if (predTask.status != AssetCommentStatus.COMPLETED) makeReady = false
				}
				if (makeReady) {
					successorTask.status = AssetCommentStatus.READY
					if (! (successorTask.validate() && successorTask.save(flush:true)) ) {
					 	throw new TaskCompletionException("Unable to release task # ${successorTask.taskNumber} due to " +
							 GormUtil.allErrorsString(successorTask) )
						log.error "Failed Readying successor task [${successorTask.id} " + GormUtil.allErrorsString(successorTask)
					 }
				}
			}
		}
	 } // def completeTask()
	
	/**
	 * This method is used by the saveUpdateCommentAndNotes to create workflow transitions in order to support 
	 * backwards compatiblity with legacy code while using runbook mode.
	 * @param assetComment
	 * @param tzId
	 * @return void
	 */
	def createTransition(assetComment, userLogin, status ) {
		def asset = assetComment.assetEntity

		// Only need to do something if task is completed and the task is associated with a workflow
		if (status == AssetCommentStatus.DONE && asset && assetComment.workflowTransition) {

			def moveBundle = asset.moveBundle
			def process = moveBundle.workflowCode
			def role = 'SUPERVISOR'
			def projectTeam = null
			def comment = ''
			def wft = assetComment.workflowTransition
			def toState = wft.code
			def updateTask=false		// we don't want createTransition to call back to TaskService

			log.info "createTransition: task id($assetComment.id) by ${userLogin}, toState=$toState"

			workflowService.createTransition( process, role, toState, asset, moveBundle, userLogin, projectTeam, comment, updateTask )
		}
	}
	/**
	 * This method is used by the several actions to get the roles that is starts with of 'staff'
	 * @param blank
	 * @return list of roles that is only starts with 'staff'
	 */
	def getRolesForStaff( ) {
		def rolesForStaff = RoleType.findAllByDescriptionIlike('staff%',[sort:'description'])
		return rolesForStaff
	}
}
