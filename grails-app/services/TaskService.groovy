/**
 * The TaskService class contains methods useful for working with Task related domain (a.k.a. AssetComment). Eventually we should migrate away from using AssetComment 
 * to persist our task functionality.
 * 
 * @author John Martin
 *
 */

import org.apache.shiro.SecurityUtils

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.CommentNote
import com.tds.asset.TaskDependency
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.AssetDependencyType
import com.tdsops.tm.enums.domain.RoleTypeGroup
import com.tdsops.common.lang.GStringEval
import com.tdsops.common.sql.SqlUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil

import org.quartz.SimpleTrigger
import org.quartz.Trigger

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import groovy.time.TimeDuration
import groovy.time.TimeCategory

class TaskService {

	static transactional = true

	def dataSource
	def jdbcTemplate
	def namedParameterJdbcTemplate
	def partyRelationshipService
	def securityService
	def quartzScheduler
	def workflowService
	
	static final List runbookCategories = [AssetCommentCategory.MOVEDAY, AssetCommentCategory.SHUTDOWN, AssetCommentCategory.PHYSICAL, AssetCommentCategory.STARTUP]
	static final List categoryList = AssetCommentCategory.getList()
	static final List statusList = AssetCommentStatus.getList()
	
	/**
	 * The getUserTasks method is used to retreive the user's TODO and ALL tasks lists or the count results of the lists. The list results are based 
	 * on the Person and Project.
	 * @param person	A Person object representing the individual to get tasks for
	 * @param project	A Project object representing the project to get tasks for
	 * @param countOnly Flag that when true will only return the counts of the tasks otherwise method returns list of tasks
	 * @param limitHistory	A numeric value when set will limit the done tasks completed in the N previous days specificed by param 
	 * @param search	A String value when provided will provided will limit the results to just the AssetEntity.AssetTag	
	 * @param sortOn	A String value when provided will sort the task lists by the specified column (limited to present list of columns), default sort on score
	 * @param sortOrder A String value with valid options [asc|desc], default [desc]
	 * @return Map	A map containing keys all and todo. Values will contain the task lists or counts based on countOnly flag
	 */
	def getUserTasks(person, project, countOnly=false, limitHistory=7, sortOn=null, sortOrder=null, search=null ) {
		// Need to initialize the NamedParameterJdbcTemplate to pass named params to a SQL statement
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource)

		// log.info "getUserTasks: limitHistory=${limitHistory}, sortOn=${sortOn}, sortOrder=${sortOrder}, search=${search}"
		
		// Get the user's functions (PKA roles) for the current project
		def roles = partyRelationshipService.getProjectStaffFunctions(project.id, person.id)?.id
		
		def type=AssetCommentType.TASK
		
		def now = TimeUtil.nowGMT()
		def minAgo = TimeUtil.adjustSeconds(now, -60)
		// log.info "getUserTasks: now=${now}, minAgo=${minAgo}"

		// List of statuses that user should be able to see in when soft assigned to others and user has proper role
		def statuses = [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED, AssetCommentStatus.COMPLETED, AssetCommentStatus.HOLD]
		
		def sqlParams = [projectId:project.id, assignedToId:person.id, type:type, roles:roles, statuses:statuses]
		
		//log.error "person:${person.id}"
		// Find all Tasks assigned to the person OR assigned to a role that is not hard assigned (to someone else)
		
		StringBuffer sql = new StringBuffer("""SELECT t.asset_comment_id AS id, 
			t.task_number AS taskNumber, 
			t.role,
			t.comment, 
			t.est_finish AS estFinish, 
			t.due_date AS dueDate, 
			t.last_updated AS lastUpdated,
			t.status_updated AS statusUpdated, 
			t.asset_entity_id AS assetEntity, 
			t.status, t.assigned_to_id AS assignedTo, 
			IFNULL(a.asset_name,'') AS assetName, 
			p.first_name AS firstName, p.last_name AS lastName,
			t.hard_assigned AS hardAssigned, 
			t.category""")

		// Add in the Sort Scoring Algorithm into the SQL if we're going to return a list
		if ( ! countOnly) {
			sqlParams << [now:now, minAgo:minAgo]
			
			/*
				The objectives are sort the list descending in this order:
					- HOLD 900
						+ last updated factor ASC
					- DONE recently (60 seconds), to allow undo 800
						+ actual finish factor DESC 
					- STARTED tasks		700
						- Hard assigned to user +55
						- by the user	+50
						- + Est Start Factor to sort ASC
					- READY tasks		600
						- Hard assigned to user +55
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
			WHERE t.project_id=:projectId AND t.comment_type=:type """)

		// TODO - CLEANER role no longer exists
		if(roles.contains('CLEANER')){
			sql.append("""AND t.role = 'CLEANER' """)
		}else{
			sql.append("""AND( t.assigned_to_id=:assignedToId OR
						(${roles ? 't.role IN (:roles) AND ' : ''}	t.status IN (:statuses) AND t.hard_assigned=0 OR (t.hard_assigned=1 AND t.assigned_to_id=:assignedToId) ) ) """)
		}
		
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
		
		// log.error "getUserTasks: SQL: " + sql.toString()
		// log.error "getUserTasks: SQL params: " + sqlParams
		
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
		// TODO : runbook : my tasks should only show mine.	 All should show mine and anything that my teams has started or complete
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
		def isPM = partyRelationshipService.staffHasFunction(task.project.id, whom.id, 'PROJ_MGR')
		
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
	def setTaskStatus(task, status, whom, isPM=false) {
		
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
		// we need to make sure that the task has been updated.	 We'll try for 3 seconds before giving up.
		def cnt = 10
		
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
		if ( status ==	AssetCommentStatus.DONE ) {
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
						if (! (	 successorTask.validate() && successorTask.save(flush:true) ) ) {
							log.error "Failed Readying successor task [${taskToReady.id} " + GormUtil.allErrorsString(taskToReady)
						}
						*/
							
						log.info "updateTaskSuccessors - task(#:${task.taskNumber} Id:${task.id}) setting successorTask(#:${successorTask.taskNumber} Id:${successorTask.id}) to READY	- $whom"
						setTaskStatus(successorTask, AssetCommentStatus.READY, whom, isPM)
						// log.info "taskStatusChangeEvent: successorTask(${successorTask.id}) Making READY - Successful"
						if ( ! successorTask.validate() ) {
							log.error "updateTaskSuccessors: task(#:${task.taskNumber} Id:${task.id}) failed READY of successor task(#:${successorTask.taskNumber} Id:${successorTask.id}) - $whom : " + GormUtil.allErrorsString(successorTask)
						}
						
						// log.info "taskStatusChangeEvent: task=${task.id} successorTask(${successorTask.id}) Made it by validate() "
						
						/*
						 * OK - For anybody looking at this logic and questioning it, let me first say I'm confused too.  For some 
						 * reason we do not need to invoke the save method here on the successor tasks that are being updated.	Some how
						 * GORM is handling it auto-magically. The code originally was doing the save but would get a strange error:
						 *		[http-8080-1] ERROR errors.GrailsExceptionResolver	- Index: 1, Size: 0
						 *		java.lang.IndexOutOfBoundsException: Index: 1, Size: 0
						 *		at java.util.ArrayList.RangeCheck(ArrayList.java:547)
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
						"in unexpected status (${successorTask.status}	- $whom"
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
	 * Used to determine the CSS class name that should be used inside rack to show relevant status images according to there status
	 * @param status
	 * @return String The appropriate CSS style or task_na if the status is invalid
	 */
	def getCssClassForRackStatus( status ) {
		def css = 'task_na'
		
		if (AssetCommentStatus.list.contains(status)) {
			css = "rack_task_${status.toLowerCase()}"
		}
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
	def genSelectForTaskDependency(taskDependency, task, idPrefix='taskDependencyEditId', name='predecessorEdit', project=null) {
		//def sw = new org.springframework.util.StopWatch("genSelectForTaskDependency Stopwatch") 
		//sw.start("Get predecessor")
		def predecessor = name=="predecessorEdit" ? taskDependency.predecessor : taskDependency.assetComment
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
							 optionSelected:predecessor.id]
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
	def genSelectForPredecessors(project, category, task, forWhom) {	
		
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
		def selectName = forWhom
		def firstOption = [value:'', display:'Please Select']
		def paramsMap = [ selectId:cssId, selectName:selectName, options:taskList, optionKey:'id', firstOption:firstOption]
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
	 * @param to	status moving to
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
	 *	Used to clear all Task data that are associated to a specified move event  
	 * @param moveEventId
	 * @return void
	 */
	def resetTaskData(def moveEvent) {
		// We want to find all AssetComment records that have the MoveEvent or are associate with assets that are 
		// with move bundles that are part of the move event. With that list, we will reset the
		// AssetComment status to PENDING by default or READY if there are no predecessors and clear several other properties.
		// We will also delete Task notes that are auto generated during the runbook execution.
		def msg
		log.info("resetTaskData() was called for moveEvent(${moveEvent})")
		try {
			def tasksMap = getMoveEventTaskLists(moveEvent.id)
		    def taskResetCnt = notesDeleted = 0
			def updateSql = "UPDATE AssetComment ac \
				SET ac.status = :status, ac.actStart = null, ac.actStart = null, ac.dateResolved = null, ac.resolvedBy = null, \
					ac.isResolved=0, ac.statusUpdated = null \
				WHERE ac.id in (:ids)"
			
			if (tasksMap.tasksWithPred.size() > 0) {
				taskResetCnt = AssetComment.executeUpdate(updateSql, ['status':AssetCommentStatus.PENDING, 'ids':tasksMap.tasksWithPred ] )
			}
			if (tasksMap.tasksNoPred.size() > 0) {
				taskResetCnt += AssetComment.executeUpdate(updateSql, ['status':AssetCommentStatus.READY, 'ids':tasksMap.tasksNoPred ] )
			}
			if (tasksMap.tasksWithNotes.size() > 0) {
				// Delete any of the audit comments that are created during the move event
				notesDeleted = CommentNote.executeUpdate("DELETE FROM CommentNote cn WHERE cn.assetComment.id IN (:ids) AND cn.isAudit=1", 
					[ 'ids':tasksMap.tasksWithNotes ] )
			}
			
			msg = "$taskRestCnt tasks reset and $notesDeleted audit notes were deleted"
		} catch(e) {
			log.error "An error occurred while trying to Reset tasks for moveEvent ${moveEvent} on project ${moveEvent.project}\n$e"
			throw new RuntimeException("An unexpected error occured")
		}
		return msg
	}
	
	/**
	 *	Used to delete all Task data that are associated to a specified move event. This deletes the task, notes and dependencies on other tasks.
	 * @param moveEventId
	 * @param deleteManual - boolean used to determine if manually created tasks should be deleted as well (default false)
	 * @return void
	 * 
	 */
	def deleteTaskData(def moveEvent, def deleteManual=false) {
		def msg
		try {			
			def depDeleted=0
			def taskDeleted=0
			def taskList = AssetComment.findAll('from AssetComment t where t.moveEvent.id=:meId', [meId:moveEvent.id])
			if (taskList.size() > 0) {
				depDeleted = TaskDependency.executeUpdate("delete from TaskDependency td where (td.predecessor in (:tasks) or td.assetComment in (:tasks))",
				   [tasks:taskList])

				taskDeleted = AssetComment.executeUpdate("delete from AssetComment a where a.id in (:ids)",[ids:taskList.id])
			}
			msg = "Deleted $taskDeleted tasks and $depDeleted dependencies"
		} catch(e) {
			log.error "An unexpected error occured while trying to delete autogenerated tasks for move event $moveEvent for project $moveEvent.project\n${e.getMessage()}"
			// e.printStackTrace()
			throw new RuntimeException("An unexpected error occured")
		}
		return msg	   
		
	}
	
	/** 
	 * Returns a map containing several lists of AssetComment ids for a specified moveEvent that include keys
	 * tasksWithPred, tasksNoPred, TasksAll and TasksWithNotes
	 * @param moveEventID
	 * @param manualTasks - boolean flag if manually created tasks should be included in list (default true)
	 * @return map of tasksWithPred, tasksNoPred, TasksAll and TasksWithNotes
	 */
	def getMoveEventTaskLists(def moveEventId, def manualTasks=true) {
		def manTasksSQL = manualTasks ? '' : ' AND c.auto_generated=true'
		def query = """SELECT c.asset_comment_id AS id, 
				( SELECT count(*) FROM task_dependency t WHERE t.asset_comment_id = c.asset_comment_id ) AS predCount,
				( SELECT count(*) FROM comment_note n WHERE n.asset_comment_id = c.asset_comment_id ) AS noteCount
			FROM asset_comment c 
			WHERE 
				c.move_event_id = ${moveEventId} AND
				c.category IN (${GormUtil.asQuoteCommaDelimitedString(runbookCategories)}) $manTasksSQL"""
		log.debug "getMoveEventTaskLists: query = ${query}"
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
	
	/**
	 * This method is used to generat HTML for a given type with list of dependencies
	 * @param depTasks, list of successors or dependencies 
	 * @param task, task for which dependencies table generating 
	 * @param dependency, for which table generating for
	 * @return
	 */
	def genTableHtmlForDependencies(def depTasks, def task, def dependency){
		def html = new StringBuffer("""<table id="${dependency}EditTableId" cellspacing="0" style="border:0px;width:0px"><tbody>""")
		def optionList = AssetComment.constraints.category.inList.toList()
		def i=1
		depTasks.each{ depTask ->
			def succecessor = depTask.assetComment
			def paramsMap = [selectId:"predecessorCategoryEditId_${depTask.id}", selectName:'category', 
				options:optionList, optionSelected:succecessor.category,
				javascript:"onChange=\'fillPredecessor(this.id,this.value,${task.id},\"${dependency}Edit\")\'" ]
			def selectCategory = HtmlUtil.generateSelect(paramsMap)
			def selectPred = genSelectForTaskDependency(depTask, task , "${dependency}EditId" , "${dependency}Edit")
			html.append("""<tr id="row_Edit_${depTask.id}"><td>""")
			html.append(selectCategory)
			html.append("""</td><td id="taskDependencyEditTdId_${depTask.id}">""")
			html.append(selectPred)
			html.append("""</td><td><a href="javascript:deletePredRow('row_Edit_${depTask.id}')"><span class="clear_filter"><u>X</u></span></a></td>""")
		}
		return html
	}
	
   /**
	* Create a Task based on a move bundle workflow step
	* @param workflow
	* @param assetEntity (optional)
	* @return Map [errMsg, stepTask]
	*/
   def createTaskBasedOnWorkflow(Map args){
	   def errMsg = ""
	   def stepTask = new AssetComment()
		   stepTask.comment = "${args.workflow.code}${args.assetEntity ? '-'+args.assetEntity?.assetName:''}"
		   stepTask.role = args.workflow.role?.id
		   stepTask.moveEvent = args.bundleMoveEvent
		   stepTask.category = args.workflow.category ? args.workflow.category : 'general'
		   stepTask.assetEntity = args.assetEntity
		   stepTask.duration = args.workflow.duration ? args.workflow.duration : 0
		   stepTask.priority = args.assetEntity?.priority ? args.assetEntity?.priority : 3
		   stepTask.status	= "Pending"
		   stepTask.workflowTransition = args.workflow
		   stepTask.project = args.project
		   stepTask.commentType = "issue"
		   stepTask.createdBy = args.person
		   stepTask.taskNumber = args.taskNumber
		   stepTask.estStart = MoveBundleStep.findByMoveBundleAndTransitionId(args.bundle, args.workflow.transId)?.planStartTime
		   stepTask.estFinish = MoveBundleStep.findByMoveBundleAndTransitionId(args.bundle, args.workflow.transId)?.planCompletionTime
			stepTask.autoGenerated = true

	   if(!stepTask.save(flush:true)){
		   stepTask.errors.allErrors.each{println it}
		   errMsg = "Failed to create WorkFlow Task. Process Failed"
	   }
	   
	   return [errMsg:errMsg, stepTask:stepTask]
   }
   

	/**
	 * Generates a SELECT control for selecting a staff assign to
	 * @param projectId Id of the project to get staff for
	 * @param taskId - task that 
	 * @param elementId CSS element id
	 * @param defaultId
	 * @return HTML of a SELECT control
	 */
	def assignToSelectHtml(projectId, taskId, defaultId, elementId) {
		def selectedId = 0

		// Find the person assigned to existing comment or default to the current user
		if (taskId) {
			def task = AssetComment.read(taskId)
			selectedId = task.assignedTo ? task.assignedTo.id : defaultId
		} 

		def projectStaff = partyRelationshipService.getProjectStaff( projectId )
	
		// Now morph the list into a list of name: Role names
		def list = []
		projectStaff.each {
			list << [ id:it.staff.id, 
				nameRole:"${it.role.description.split(':')[1]?.trim()}: ${it.staff.firstName} ${it.staff.lastName}",
				sortOn:"${it.role.description.split(':')[1]?.trim()},${it.staff.firstName} ${it.staff.lastName}"
			]
		}
		list.sort { it.sortOn }
	
		def firstOption = [value:'', display:'Unassigned']
		def paramsMap = [selectId:elementId, selectName:elementId, options:list, 
			optionKey:'id', optionValue:'nameRole', 
			optionSelected:selectedId, firstOption:firstOption ]
		def assignedToSelect = HtmlUtil.generateSelect( paramsMap )
		
		return assignedToSelect
	}
	
	
	/**
	 * Retrieves the runbook recipe for a specified MoveEvent
	 * @param moveEventId
	 * @return Map containing Tasks[Map] and potentially Resources[Map]
	 */
	def getMoveEventRunbookRecipe( moveEvent ) {
		def recipe
		
		if (moveEvent && moveEvent.runbookRecipe ) {
			try {
				recipe = Eval.me("[ ${moveEvent.runbookRecipe} ]")				
			} catch (e) {
				log.error "There is an error in the runbook recipe for project move event ${moveEvent.project} - ${moveEvent}\n${e.getMessage}"
			}
		}
		return recipe
	}
	
	/**
	 * Used to fetch the last task number for a project
	 * @param Project
	 * @return Integer
	 */
	def getLastTaskNumber(project) {
		def lastTaskNum = AssetComment.executeQuery('select MAX(a.taskNumber) from AssetComment a where project=?', [project])[0]		
		
		if (! lastTaskNum) lastTaskNum = 0
		// log.info "Last task number is $lastTaskNum"
		
		return lastTaskNum
	}
	
	/**
	 * Used to generate the runbook tasks for a specified project's Move Event
	 * @param Person whom - the individual that is generating the tasks
	 * @param MoveEvent moveEvent - the move event to process
	 * @return String - the messages as to the success of the function
	 */
	def generateRunbook( whom, moveEvent ) {
		def startedAt = new Date()
		
		// List of all move bundles associated with the move event
		def bundleList = moveEvent.moveBundles
		log.info "bundleList=[$bundleList] for moveEvent ${moveEvent.id}"		
		def bundleIds = bundleList*.id
		def project = moveEvent.project

		// List of all assets associated with the move event
		// def assetList = AssetEntity.findAll("from AssetEntity a WHERE a.moveBundle.id IN (:bundleIds)", [bundleIds:bundleIds] )
		
		if (bundleIds.size() == 0) {
			return "There are no Move Bundles assigned to the Move Event, which are required."
		}

		def categories = GormUtil.asQuoteCommaDelimitedString(AssetComment.moveDayCategories) 
		
		// log.info "${assetList.size()} assets found for move event ${moveEvent}"
		
		// Fail if there are already moveday tasks that are autogenerated
		def existingTasks = jdbcTemplate.queryForInt("select count(*) from asset_comment a where a.move_event_id=${moveEvent.id} \
			and a.auto_generated=true and a.category in (${categories})")
		// log.info "existingTask count=$existingTasks"
		if ( existingTasks > 0 ) {
			return "Unable to generate tasks as there are moveday tasks already generated for the project"
		}

		// Get the various workflow steps that will be used to populate things like the workflows ids, durations, teams, etc when creating tasks
		def workflowStepSql = "select mb.move_bundle_id, wft.*, mbs.* \
			from move_bundle mb \
			left outer join workflow wf ON wf.process = mb.workflow_code \
			left outer join workflow_transition wft ON wft.workflow_id=wf.workflow_id \
			left outer join move_bundle_step mbs ON mbs.move_bundle_id=mb.move_bundle_id AND mbs.transition_id=wft.trans_id \
			where mb.move_bundle_id IN (${GormUtil.asCommaDelimitedString(bundleIds)})"
		def workflowSteps = jdbcTemplate.queryForList(workflowStepSql)
			
		// log.info "Workflow steps SQL= ${workflowStepSql}"	
		// log.info "Found ${workflowSteps.size()} workflow steps for moveEvent ${moveEvent}"
		
		// Define a number of vars that will hold cached data about the tasks being generated for easier lookup, dependencies, etc
		def lastTaskNum = getLastTaskNumber(project)		
		def taskList = []			// an array that will contain list of all tasks that are inserted as they are created
		def lastMilestone = null	// Will track the most recent milestone task
		def assetsLatestTask = [:]	// This map array will contain reference of the assets' last assigned task
		def taskSpecIds = [:]		// Used to track the ID #s of all of the taskSpecs in the recipe
		
		def wfList = null			// get list of workflows for each move bundle
		
		def newTask
		def newDep
		
		def recipe = getMoveEventRunbookRecipe( moveEvent )
		def recipeId = recipe?.id
		def recipeTasks = recipe?.tasks		
		if (! recipeTasks || recipeTasks.size() == 0) {
			return "There appears to be no runbook recipe or there is an error in its format"
		}
		// log.info "Runbook recipe: $recipe"

		// These buffers are used to capture status output for short-term
		StringBuffer out = new StringBuffer()
		StringBuffer exceptions = new StringBuffer()
		
		def noSuccessorsSql = "select t.asset_comment_id as id \
	    	from asset_comment t \
	  		left outer join task_dependency d ON d.predecessor_id=t.asset_comment_id \
			where t.move_event_id=${moveEvent.id} AND d.asset_comment_id is null \
			AND t.auto_generated=true \
			AND t.category IN (${categories}) "
		// log.info("noSuccessorsSql: $noSuccessorsSql")
		
		def taskCount = 0
		def depCount = 0
		def specCount = 0
		
		/**
		 * A helper closure used by generateRunbook to link a task to its predecessors by asset or milestone
		 * @param AssetComment (aka Task)
		 */
		def linkTaskToLastAssetOrMilestone = { taskToLink ->
			// See if there is an asset and that there are previous tasks for the asset

			// If the task is for an asset and there is a previous task for the asset
			if ( taskToLink.assetEntity && assetsLatestTask.containsKey(taskToLink.assetEntity.id) ) {
				
				log.info "linkTaskToLastAssetOrMilestone in 1"
				newDep = createTaskDependency( assetsLatestTask[taskToLink.assetEntity.id], taskToLink )
				depCount++
				assetsLatestTask[taskToLink.assetEntity.id] = taskToLink									
				out.append("Created dependency (${newDep.id}) between milestone $lastMilestone and $taskToLink<br/>")
				// Now we can associate this new task as the latest task for the asset									
				assetsLatestTask.put(taskToLink.assetEntity.id, taskToLink)
				
			} else if (lastMilestone) {
				log.info "linkTaskToLastAssetOrMilestone in 2"
				newDep = createTaskDependency( lastMilestone, taskToLink )
				depCount++
				assetsLatestTask.put(taskToLink.assetEntity.id, taskToLink)
				out.append("Created dependency (${newDep.id}) between milestone $lastMilestone and $taskToLink<br/>")									
			} else {
				exceptions.append("Task(${tnd}) has no predecessor tasks<br/>")
			}
			
		}
		/*
		} else {
			exceptions.add "Support dependency for asset ${sd.predecessor} to asset ${sd.asset} that is not in move " +
				"event for taskSpec ${taskSpec}"								 
		*/
		
		// 
		/**
		 * getWorkflowStep - a Closure used to lookup the workflow step from a few parameters
		 * @param String workflowStepCode 
		 * @param Integer moveBundleId (default null)
		 * @return Map from workflowSteps list or null if not found
		 */
		def getWorkflowStep = { workflowStepCode, moveBundleId=null -> 
			def wfsd = null
			if (moveBundleId && workflowStepCode) {
				wfsd = workflowSteps.find{ it.move_bundle_id==moveBundleId && it.code == workflowStepCode }
				if (!wfsd) {
					exceptions.append("Unable to find workflow step code $workflowStepCode for bundleId $moveBundleId<br/>")
				}
			} else if (workflowStepCode) {
				// We have a workflow code but don't know which bundle. This will happen on the start and Completed tasks as there are no
				// Assets associated to the step and therefore can't tie it to a bundle. This is a bit of a hack in that it is just going to
				// find the first move bundle. We could improve this to find the one with the latest completion time which is what we're getting 
				// it for in a Milestone.
				wfsd = workflowSteps.find{ it.code == workflowStepCode }
				if (!wfsd) {
					exceptions.append("Unable to find workflow step code $workflowStepCode<br/>")
				}
			}
			return wfsd
		}
		
		def taskSpecIndex = 0
		def maxPreviousEstFinish = null		// Holds the max Est Finish from the previous taskSpec
		def workflow
		
		try {
			recipeTasks.each { taskSpec ->
				taskSpecIndex++
				
				// Make sure that the user define the taskSpec.id and that it is NOT a duplicate from a previous step
				// because it could cause adverse dependency linkings.
				log.info "##### Processing taskSpec $taskSpec"

				specCount++
				if (! taskSpec.containsKey('id')) {
					throw new RuntimeException("TaskSpec for step $specCount in list is missing the 'id' property")
				}
				if (taskSpecIds.containsKey(taskSpec.id)) {
					throw new RuntimeException("TaskSpec for step $specCount duplicated id ${taskSpec.id} found in step ${taskSpecIds[taskSpec.id]}")
				}
				taskSpecIds.put(taskSpec.id, specCount)
				
				def taskWorkflowCode = taskSpec.containsKey('workflow') ? taskSpec.workflow : null
				
				out.append("=======<br/>Processing taskSpec ${taskSpec.id}-${taskSpec.description}<br/>-------<br/>")
				
				// -------------------------
				// Handle Milestone tasks
				// -------------------------
				if (taskSpec.containsKey('milestone') ) {
					
					// Create the milestone task
					out.append("Creating milestone ${taskSpec.title}<br/>")
					
					workflow = getWorkflowStep(taskWorkflowCode)		
					newTask = createTaskFromSpec(recipeId, whom, ++lastTaskNum, moveEvent, taskSpec, workflow)
					taskList << newTask
					lastMilestone = newTask 
					taskCount++

					// Now find all tasks that don't have have successor (excluding the current milestone task) and
					// create dependency where the milestone is the successor
					def tasksNoSuccessors = jdbcTemplate.queryForList(noSuccessorsSql)
					log.info "Found ${tasksNoSuccessors.count()} tasks with no successors"
					if (tasksNoSuccessors.size()==0 && taskCount > 1 ) {
						out.append("Found no successors for a milestone, which is unexpected but not necessarily wrong - Task $newTask<br/>")
					}
					tasksNoSuccessors.each() { p ->
						// Create dependency as long as we're not referencing the task just created
						if (p.id != newTask.id ) {
							def predecessorTask = AssetComment.read(p.id)
							newDep = createTaskDependency( predecessorTask, newTask )
							depCount++
							out.append("Created dependency (${newDep.id}) between $predecessorTask and $newTask<br/>")
							if (predecessorTask.assetEntity) {
								// Move the asset's last task to the milestone task
								assetsLatestTask.put(predecessorTask.assetEntity.id, newTask)
							}
						}
					}						
				} else if (taskSpec.containsKey('action')) {
					// -------------------------
					// Handle ACTION TaskSpecs (e.g. OnCart, offCart, QARAck) Tasks
					// -------------------------
					exceptions.append("Action(${taskSpec.action}) in taskSpec id ${taskSpec.id} presently not supported<br/>")
					
				} else {
					// -------------------------
					// Create Normal Tasks
					// -------------------------
					def assetsForTask = findAllAssetsWithFilter(moveEvent, taskSpec.filter)
					log.info "Found (${assetsForTask?.size()}) assets for taskSpec ${taskSpec.id}-${taskSpec.description}"
					if ( !assetsForTask || assetsForTask.size()==0) return // aka continue
					
					def tasksNeedingDependencies = []
					def depCode=''

					//
					// Create a task for each asset based on the filtering of the taskSpec
					//
					assetsForTask?.each() { asset ->
						workflow = getWorkflowStep(taskWorkflowCode, asset.moveBundle.id)
						newTask = createTaskFromSpec(recipeId, whom, ++lastTaskNum, moveEvent, taskSpec, workflow, asset)
						taskList << newTask
						taskCount++
						tasksNeedingDependencies << newTask
						out.append("Created task $newTask<br/>")
					}
					
					// Set some vars that will be used in the next iterator 
					def specHasDependency = false		// Set to true if the spec references Dependencies

					// Determine if we are looking at AssetDependency associations and if so if we're looking for Supports or Requires relationships.
					// Supports are usually used in Shutdown and Requires in Startup
					if ( taskSpec.filter?.containsKey('dependency') ) {
						depCode = taskSpec.filter.dependency[0].toLowerCase()
						specHasDependency = 'sr'.contains( depCode )
						if ( ! specHasDependency ) {
							// Someone messed up a Spec 
							throw new RuntimeException("Task Spec ${taskSpec.id} has invalid filter.dependency value (${taskSpec.filter.dependency}) " +
								"- valid options are [supports|requires]")	
						}
					}

					log.info "specHasDependency=$specHasDependency"
					//	
					// Now iterate over all of the tasks just created for this taskSpec and assign dependencies 
					//
					out.append("# Creating dependencies on ${tasksNeedingDependencies.size()}<br/>")
					tasksNeedingDependencies.each() { tnd ->
						log.info "Processing task $tnd"
						if (specHasDependency && tnd.assetEntity) {
							//
							// Process taskSpecs that reference AssetDependency records based on the filter
							//
							def assetDependencies = getAssetDependencies(tnd.assetEntity, taskSpec.filter)
						
							if (assetDependencies.size() == 0) {
								exceptions.append("Asset(${tnd.assetEntity}) for Task(${tnd}) has no ${taskSpec.filter.dependency} relationships<br/>")

								// Link task to the last known milestone or it's asset's previous task
								linkTaskToLastAssetOrMilestone(tnd)
							} else {
								
								// Look over the assets dependencies that are associated to the current task's asset and create predecessor relationships 
								// We will warn on assets that are not part of the moveEvent that have dependency. We most likely will want to have tasks 
								// for assets not moving in the future but will require discussion.
								log.info "Iterate over ${assetDependencies.size()} dependencies for asset ${tnd.assetEntity}"
								def tndHadDependency=false
								assetDependencies.each { ad ->
									// Get the Note that this should be the opposite of that used in the getAssetDependencies 
									def predAsset = depCode == 's' ?  ad.dependent : ad.asset

									// Make sure that the other asset is in one of the move bundles in the move event
									def predMoveBundle = predAsset.moveBundle
									if (! predMoveBundle || ! bundleIds.contains(predMoveBundle.id)) {
										exceptions.append("Asset dependency references asset not in move event: task($tnd) between asset ${tnd.assetEntity} and ${predAsset}<br/>")
									} else {
										// Find the last task for the predAsset to create associations between tasks. If the predecessor was created
										// during the same taskStep, assetsLatestTask may not yet be populated so we can scan the tasks created list for
										// one with the same taskSpec id #
										def previousTask = taskList.find { it.assetEntity?.id == predAsset.id && it.taskSpec == tnd.taskSpec }										
										if (previousTask) {
											log.info "Found task in taskList array - task (${previousTask})"
										} else {
											// Try finding latest task for the asset
											if (assetsLatestTask.containsKey(predAsset.id)) {
												previousTask = assetsLatestTask[predAsset.id]
												log.info "Found task from assetsLatestTask array - task (${previousTask})"
											}
										}
										if (previousTask) {
											newDep = createTaskDependency(previousTask, tnd)
											depCount++
											assetsLatestTask.put(tnd.assetEntity.id, tnd)
											tndHadDependency=true
											out.append("Created dependency (${newDep.id}) between ${previousTask} and $tnd<br/>")
										} else {
											log.info "No predecessor task found for asset ($predAsset) to link to task ($tnd)"
											exceptions.append("No predecessor task found for asset ($predAsset) to link to task ($tnd)<br/>")
										}
									}
								}
								if (! tndHadDependency) {
									// If none of the dependencies worked out we need to tie the task to some other predecessor
									linkTaskToLastAssetOrMilestone(tnd)											
								}
							}
							
						} else  {
							log.info "Alternate dependency for task ${tnd}"
							// Link task to the last known milestone or it's asset's previous task
							linkTaskToLastAssetOrMilestone(tnd)
							
						}
					} // tasksNeedingDependencies.each()
				}
			} 
		} catch(e)	{
			exceptions.append("We BLEW UP damn it!<br/>")
			exceptions.append(e.toString())
			e.printStackTrace()
		}
		
		TimeDuration elapsed = TimeCategory.minus( new Date(), startedAt )
		
		log.info "A total of $taskCount Tasks and $depCount Dependencies created in $elapsed"
		return "<h2>Status:</h2> $taskCount Tasks and $depCount Dependencies created in $elapsed<h2>Exceptions:</h2>" + 
	
		exceptions.toString() + "<h2>Log:</h2>" + out.toString()
		
	}
	
	/**
	 * A helper method called by generateRunbook to lookup the AssetDependency records for an Asset as specified in the taskSpec
	 * @param Object asset - can be any asset type (Application, AssetEntity, Database or Files)
	 * @param Map taskSpec - a Task specification map
	 * @return List<AssetDependency> - list of dependencies
 	 */
	def getAssetDependencies(asset, filter) {
		def list = []
		def depCode = filter.dependency[0].toLowerCase()
		
		// This is the list of properties that can be added to the search criteria from the Filter
		def supportedFilterProps = ['status']
		
		// AssetEntity  asset			// The asset that that REQUIRES the 'dependent'
		// AssetEntity dependent		// The asset that SUPPORTS 'asset' 
		// log.info "depCode=$depCode"
		def relateOn = ( depCode == 's' ? 'asset' : 'dependent' )
		def sql = "from AssetDependency ad where ad.${relateOn}.id=:assetId and \
			ad.status not in ('${AssetDependencyStatus.NA}', '${AssetDependencyStatus.ARCHIVED}') \
			and ad.type <> '${AssetDependencyType.BATCH}'"
			
		def map = ['assetId':asset.id]
		log.info "FILTER: $filter"
		// Add additional WHERE expresions to the SQL
		supportedFilterProps.each() { prop ->
			if (filter?.containsKey(prop)) {
				def sqlMap = SqlUtil.whereExpression("ad.$prop", filter[prop], prop)
				if (sqlMap) {
					sql = SqlUtil.appendToWhere(sql, sqlMap.sql)
					if (sqlMap.param) {
						map[prop] = sqlMap.param
					}								
				} else {
					log.error "SqlUtil.whereExpression unable to resolve ${prop} expression [${filter[prop]}]"
					throw new RuntimeException("Unable to resolve filter param:[${prop}] expression:[${filter[prop]}] while processing asset ${asset}")
				}
			}
			
		}
		
		log.info "getAssetDependencies SQL=$sql, PARAMS=$map"
		list = AssetDependency.findAll(sql, map)
		log.info "getAssetDependencies found ${list.size()} rows : $list"

		return list
		
	}
	
	/**
	 * Creates a task for a moveEvent based on a taskSpec from a recipe and defaults the status to READY
	 * @param moveEvent object
	 * @param Map taskSpec that contains the specifications for how to create the task
	 * @param asset - The asset associated with the task if there appropriate
	 * @return AssetComment (aka Task)
	 */
	def createTaskFromSpec(recipeId, whom, taskNumber, moveEvent, taskSpec, workflow=null, asset=null) {
		def task = new AssetComment(
			taskNumber: taskNumber,
			project: moveEvent.project, 
			moveEvent: moveEvent, 
			assetEntity: asset,
			commentType: AssetCommentType.TASK,
			status: AssetCommentStatus.READY,
			createdBy: whom,
			displayOption: 'U',
			autoGenerated: true,
			recipe: recipeId,
			taskSpec: taskSpec.id )
			
		// Handle the various settings from the taskSpec
		task.priority = taskSpec.containsKey('priority') ? taskSpec.priority : 3
		if (taskSpec.containsKey('duration')) task.duration = taskSpec.duration
		if (taskSpec.containsKey('team')) task.role = taskSpec.team
		if (taskSpec.containsKey('category')) task.category = taskSpec.category
		
		def defCat = task.category
		task.category = null
		
		// TODO : Should be able to parse the duration for a character
		task.durationScale = 'm'
		
		if (asset) {
			task.comment = new GStringEval().toString(taskSpec.title, asset)
		} else {
			task.comment = new GStringEval().toString(taskSpec.title, moveEvent)
		}
			
		// Set various values if there is a workflow associated to this taskSpec and Asset
		if (workflow) {
			// log.info "Applying workflow values to task $taskNumber - values=$workflow"
			if (workflow.workflow_transition_id) { task.workflowTransition = WorkflowTransition.read(workflow.workflow_transition_id) }
			if (! task.role && workflow.role_id) { task.role = workflow.role_id }
			if (! task.category && workflow.category) { task.category = workflow.category }
			if (! task.estStart && workflow.plan_start_time) { task.estStart = workflow.plan_start_time }
			if (! task.estFinish && workflow.plan_completion_time) { task.estFinish = workflow.plan_completion_time }
			if (! task.duration && workflow.duration != null) {
				task.duration = workflow.duration
				task.durationScale = workflow.duration_scale ?: 'm'
			}
		}
		
		if (task.duration == null) task.duration=0
		if (task.category == null) task.category = defCat
		
		// log.info "About to save task: ${task.category}"
		if (! ( task.validate() && task.save(flush:true) ) ) {
			throw new RuntimeException("Error while trying to create task. error=${GormUtil.allErrorsString(task)}, asset=$asset, TaskSpec=$taskSpec")
		}

		log.info "Saved task ${task.id} - ${task}"
		
		return task
	}
	
	/**
	 * Used to create a task dependency between two task
	 * @param precessor
	 * @param successor
	 * @return TaskDependency
	 * @throws RuntimeError if unable to save the dependency
	 */
	def createTaskDependency( predecessor, successor ) {
		log.info "Creating dependency predecessor=$predecessor, successor=$successor"
		def dependency = new TaskDependency(assetComment:successor, predecessor:predecessor)
		if (! ( dependency.validate() && dependency.save(flush:true) ) ) {
			throw new RuntimeException("Error while trying to create dependency between predecessor=$predecessor, successor=$successor<br/>Error=${GormUtil.allErrorsString(dependency)}, ")
		}
		successor.status = AssetCommentStatus.PENDING
		if (! ( successor.validate() && successor.save(flush:true) ) ) {
			throw new RuntimeException("Failed to update successor ($successor) status to PENDING<br/>Error=${GormUtil.allErrorsString(dependency)}")
		}
		
		return dependency
	}
	
	/**
	 * This special method is used to find all assets of a moveEvent that match the criteria as defined in the filter map
	 * @param MoveEvent
	 * @param Map filter - contains various attributes that define the filtering
	 * @return List<AssetEntity, Application, Database, File> based on the filter
	 * @throws RuntimeException if query fails or there is invalid specifications in the filter criteria
	 */
	def findAllAssetsWithFilter(moveEvent, filter) {
		
		def queryOn = filter?.containsKey('class') ? filter['class'].toLowerCase() : 'device'
		if (! ['device','application', 'database', 'files'].contains(queryOn) ) {
			throw new RuntimeException("An invalid 'class' was specified for the filter (valid options are 'device','application', 'database', 'files') for $filter")
		}
		
		def bundleList = moveEvent.moveBundles
		def bundleIds = bundleList*.id

		def assets
		def where = ''
		def project = moveEvent.project
		def map = [:]
		def sql
		
		map.bIds = bundleIds
		log.info "bundleIds=[$bundleIds]"
		
		
		/** 
		 * A helper closure used below to manipulate the 'where' and 'map' variables to add additional
		 * WHERE expressions based on the properties passed in the filter
		 * @param String[] - list of the properties to examine
		 */
		def addWhereConditions = { list ->
			list.each() { code ->
				if (filter?.asset?.containsKey(code)) {
					def sm = SqlUtil.whereExpression("a.$code", filter.asset[code], code)
					if (sm) {
						where = SqlUtil.appendToWhere(where, sm.sql)
						if (sm.param) {
							map[code] = sm.param
						}								
					} else {
						log.error "SqlUtil.whereExpression unable to resolve ${code} expression [${filter.asset[code]}]"
					}
				}
			}
		}
		
		// Add additional WHERE clauses based on the following properties being present in the filter.asset 
		addWhereConditions( ['assetName','assetTag','assetType', 'priority', 'status'] )
		
		try {
			
			switch(queryOn) {
				case 'device':					
					if (filter?.asset?.containsKey('virtual')) {
						// Just Virtual devices
						where = SqlUtil.appendToWhere(where, "a.assetType IN ('virtual', 'vm')")
					} else if (filter?.asset?.containsKey('physical')) {
						// Just Physical devices
						where = SqlUtil.appendToWhere(where, "a.assetType not IN ('application', 'database', 'files', 'virtual', 'vm')")
					} else {
						// All Devices
						where = SqlUtil.appendToWhere(where, "a.assetType not IN ('application', 'database', 'files')")
					}
					
					sql = "from AssetEntity a where a.moveBundle.id in (:bIds) and $where"
					log.info "findAllAssetsWithFilter: sql=$sql, map=$map"
					assets = AssetEntity.findAll(sql, map)
					break;
					
				case 'application':					
					// Add additional WHERE clauses based on the following properties being present in the filter (Application domain specific)
					addWhereConditions( ['appVendor','sme','sme2','businessUnit','criticality'] )
					
					sql = "from Application a where a.moveBundle.id in (:bIds)" + (where.size()>0 ? " and $where" : '')
					log.info "findAllAssetsWithFilter: sql=$sql, map=$map"
					assets = Application.findAll(sql, map)
					break;
				
			} 
		} catch (e) {
			def msg = "An unexpected error occurred while trying to locate assets for filter $filter" + e.toString()
			log.error "$msg\n"
			throw new RuntimeException("$msg<br/>${e.getMessage()}")
		}
		
		return assets
	}	
	
}