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

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class TaskService {

    static transactional = true

	def securityService
	def jdbcTemplate
	def dataSource
	def namedParameterJdbcTemplate
	def workflowService
	
	static final List runbookCategories = [AssetCommentCategory.SHUTDOWN, AssetCommentCategory.PHYSICAL, AssetCommentCategory.STARTUP]

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

		// List of statuses that user should be able to see in when soft assigned to others and user has proper role
		def statuses = [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED]
		
		def sqlParams = [projectId:project.id, assignedToId:person.id, type:type, roles:roles, statuses:statuses]
		
		//log.error "person:${person.id}"
		// Find all Tasks assigned to the person OR assigned to a role that is not hard assigned (to someone else)
		
		StringBuffer sql = new StringBuffer("""SELECT t.asset_comment_id AS id, t.comment, t.due_date AS dueDate, t.last_updated AS lastUpdated,
		 	t.asset_entity_id AS assetEntity, t.status, t.assigned_to_id AS assignedTo, IFNULL(a.asset_name,'') AS assetName, t.role,
		 	t.task_number AS taskNumber """)

		// Add in the Sort Scoring Algorithm into the SQL if we're going to return a list
		if ( ! countOnly) {
			/*
				The sorting scoring will compute a weighted value based on several criteria that includes: 
				- Assigned to me +30
				- Status of Started +30
				- Status of Ready +20
				- Status of Hold +15
				- Status of Pending +10
				- Category of Startup, Physical, Moveday, or Shutdown +10
				- If duedate exists and is older than today +5
				- Priority - Six (6) - <priority value> (so a priority of 5 will add 1 to the score and 1 adds 5)
			*/
			sql.append(""", ( IF(t.assigned_to_id=:assignedToId, 10, 0) + (CASE t.status 
				WHEN '${AssetCommentStatus.STARTED}' THEN 30
				WHEN '${AssetCommentStatus.READY}' THEN 20
				WHEN '${AssetCommentStatus.HOLD}' THEN 15
				WHEN '${AssetCommentStatus.PENDING}' THEN 10
				WHEN '${AssetCommentStatus.DONE}' THEN -10
				ELSE 0
				END) +  
				IF(t.category IN ('${AssetCommentCategory.SHUTDOWN}', '${AssetCommentCategory.PHYSICAL}', '${AssetCommentCategory.STARTUP}'), 10,0) +
				6 - t.priority) AS score """)
		}
		
		sql.append("""FROM asset_comment t 
			LEFT OUTER JOIN asset_entity a ON a.asset_entity_id = t.asset_entity_id
			WHERE t.project_id=:projectId AND t.comment_type=:type AND 
			( t.assigned_to_id=:assignedToId OR (t.role IN (:roles) AND t.status IN (:statuses) AND t.hard_assigned=0) ) """)
		
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
			def sortableProps = ['number_comment', 'comment', 'dueDate', 'lastUpdated', 'status', 'assetEntity']
			if (sortOn) {
				if ( sortableProps.contains(sortOn) ) {
					sortOrder = ['asc','desc'].contains(sortOrder) ? sortOrder : 'asc'
					sortOrder = sortOrder.toUpperCase()
					sql.append(sortOn == "number_comment" ? "taskNumber ${sortOrder}, comment ${sortOrder}" : "${sortOn} ${sortOrder}")
				} else {
					log.warn "getUserTasks: called with invalid sort property [${sortOn}]"
					sortOn=null
				}
			}
			// add the score sort either as addition or as only ORDER BY parameteters
			sql.append( (sortOn ? ', ' : '') + 'score DESC, task_number ASC' )
		}
		
		// log.info "getUserTasks: SQL for userTasks: " + sql.toString()
		
		// Get all tasks from the database and then filter out the TODOs based on a filtering
		def allTasks = namedParameterJdbcTemplate.queryForList( sql.toString(), sqlParams )
		// def allTasks = jdbcTemplate.queryForList( sql.toString(), sqlParams )
		def todoTasks = allTasks.findAll { task ->
			task.status == AssetCommentStatus.READY || ( task.status == AssetCommentStatus.STARTED && task.assignedTo == person.id )
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
		return setTaskStatus( task, status, whom )
	}

	/**
	 * Used to set the status of a task, which will perform additional updated based on the state
	 * @param whom		A Person object that represents the person updating the task
	 * @param task		A AssetComment (aka Task) to change the status on
	 * @param status	A String representing the status code (AssetCommentStatus)
	 * @return AssetComment the task object that was updated
	 */
	// TODO : We should probably refactor this into the AssetComment domain class as setStatus
	def setTaskStatus(task, status, whom) {
		// If the current task.status or the persisted value equals the new status, then there's nutt'n to do.
		if (task.status == status || task.getPersistentValue('status') == status) {
			return
		}

		// First thing to do, set the status
		task.status = status
		
		def previousStatus = task.getPersistentValue('status')
		// Determine if the status is being reverted (e.g. going from DONE to READY)
		def revertStatus = compareStatus(previousStatus, status) > 0

		log.debug "setTaskStatus() status=${status}, previousStatus=${previousStatus}, revertStatus=${revertStatus}"

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
			
			addNote( task, whom, "Reverted task status from '${previousStatus}' to '${status}'")
			
			// TODO : Runbook Look at the successors and do something about them
			// Change READY successors to PENDING
			// Put STARTED task on HOLD and add note that predecessor task was reverted
			
		}
		
		// Now update the task properties according to the new status
		if ([AssetCommentStatus.STARTED, AssetCommentStatus.DONE, AssetCommentStatus.TERMINATED].contains(status)) {
	
			def now =  GormUtil.convertInToGMT( "now", "EDT" )
			def isPM = securityService.hasRole("PROJ_MGR")

			// Properly handle assignment if the user is the PM
			def assignee = whom
			if (task.assignedTo && isPM) {
				assignee = task.assignedTo
			}
			
			switch (status) {
				case AssetCommentStatus.STARTED:
					task.assignedTo = assignee
					// We don't want to loose the original started time if we are reverting from DONE to STARTED
					if (! revertStatus ) {
						task.actStart = now
					}
					break
					
				case AssetCommentStatus.DONE:
					taskStatusChangeEvent(task)
					task.assignedTo = assignee
					task.resolvedBy = assignee
					task.actFinish = now
					break
					
				case AssetCommentStatus.TERMINATED:
					task.resolvedBy = assignee
					task.actFinish = now
					break
			}
		}
		
		return task
	}
 	
	/**
	 * This is invoked by the AssetComment.beforeUpdate method in order to handle any status changes
	 * that may result in the updating of other tasks successor tasks.
	 */
	def taskStatusChangeEvent( task ) {
		log.debug "taskStatusChangeEvent: processing task(${task.id}): ${task}"
		def status = task.status
		
		// Let's bail out if the status isn't dirty or the status isn't STARTED or DONE
		if ( ! task.isDirty('status') ||
			task.getPersistentValue('status') == status || 
			! [AssetCommentStatus.STARTED, AssetCommentStatus.DONE].contains(status) ) {
			return
		}
		
		// Okay so we want to handle the STARTED and DONE states and update accordingly.  We are going
		// to assume that the validation logic prevented the user from changing the status if there were
		// any incomplete predecessors.
			
		// TODO: taskStatusChangeEvent : Add logic to handle READY for the SS predecessor type and correct the current code to not assume SF type
	
		//
		// Now mark any successors as Ready if all of the successors' predecessors are DONE
		//
		if ( status ==  AssetCommentStatus.DONE ) {
			def successorDependents = TaskDependency.findAllByPredecessor(task)
			log.debug "taskStatusChangeEvent: Found ${successorDependents ? successorDependents.size() : '0'} successors for task ${task.id}"
			successorDependents?.each() { succDepend ->
				def dependentTask = succDepend.assetComment
				log.debug "taskStatusChangeEvent: Processing dependentTask(${dependentTask.id}): ${dependentTask}"
				
				// If the Task is in the Planned or Pending state, we can check to see if it makes sense to set to READY
				if ([AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING].contains(dependentTask.status)) {
					
					// Find all predecessor/ tasks for the successor, other than the current task, and make sure they are completed
					def predDependencies = TaskDependency.findByAssetCommentAndPredecessorNot(dependentTask, task)
					def makeReady=true
					log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) predecessors> ${predDependencies}"
					predDependencies?.each() { predDependency ->
						def predTask = predDependency.assetComment
						// TODO : runbook : taskStatusChangeEvent - add the dependency type (SS,FS) logic here
						if (predTask.status != AssetCommentStatus.COMPLETED) { 
							makeReady = false
						}
					}
					if (makeReady) {
						/*
						def taskToReady = AssetComment.get(dependentTask.id)
						setTaskStatus(taskToReady, AssetCommentStatus.READY)
						if ( ! taskToReady.validate() ) {
							log.error "taskStatusChangeEvent: Failed Readying successor task [${taskToReady.id} " + GormUtil.allErrorsString(taskToReady)
						}
//							if ( taskToReady.save(flush:true) ) {
						if ( taskToReady.save() ) {
							log.info "taskStatusChangeEvent: dependentTask(${taskToReady.id}) Saved"
							return
						}
						if (! (  dependentTask.validate() && dependentTask.save(flush:true) ) ) {
							log.error "Failed Readying successor task [${taskToReady.id} " + GormUtil.allErrorsString(taskToReady)
						}
						*/
							
						log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) Making READY "
						setTaskStatus(dependentTask, AssetCommentStatus.READY)
						// log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) Making READY - Successful"
						if ( ! dependentTask.validate() ) {
							log.error "taskStatusChangeEvent: Failed Readying successor task [${dependentTask.id} " + GormUtil.allErrorsString(dependentTask)
						}
						
						// log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) Made it by validate() "
						
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
						if ( dependentTask.save(flush:true) ) {
							log.info "taskStatusChangeEvent: dependentTask(${dependentTask.id}) Saved"
							return
						} else {
						//if (! (  dependentTask.validate() && dependentTask.save(flush:true) ) ) {
							log.error "Failed Readying successor task [${dependentTask.id} " + GormUtil.allErrorsString(dependentTask)
							
							 throw new TaskCompletionException("Unable to READY task ${dependentTask} due to " +
								 GormUtil.allErrorsString(dependentTask) )
						}
					}
				} else {
					log.warn "taskStatusChangeEvent: Found dependentTask(${dependentTask.id}): ${dependentTask} at unexpected status (${dependentTask.status}"
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
		if (AssetCommentStatus.getList().contains(status)) {
			css = "task_${status.toLowerCase()}"
		}
		// log.error "getCssClassForStatus('${status})=${css}"
		return css 
	}

	/**
	 * Returns the HTML for a SELECT control that contains list of tasks based on a TaskDependency
	 * @param task
	 * @return
	 */
	def genSelectForTaskDependency(taskDependency, assetComment, project=null, idPrefix='taskDependencyEditId', name='taskDependencyEdit') {
		def predecessor = taskDependency.predecessor
		def category = predecessor.category
		
		if (project==null) 
			project = securityService.getUserCurrentProject()
		def projectId = project.id
		def assetForComment = assetComment.assetEntity
		def moveEventForComment =  assetForComment ? assetForComment.moveBundle?.moveEvent : assetComment.moveEvent
		def queryForPredecessor = new StringBuffer("""FROM AssetComment a WHERE a.project=${projectId} \
															AND a.category='${category}'\
														    AND a.commentType='${AssetCommentType.TASK}'\
															AND a.id != ${assetComment.id} """)
		if(moveEventForComment){
			queryForPredecessor.append("AND (a.assetEntity.moveBundle.moveEvent.id = ${moveEventForComment?.id} OR a.moveEvent.id = ${moveEventForComment?.id})")
		}
		queryForPredecessor.append(""" ORDER BY a.taskNumber ASC""")
		
		def predecessors = AssetComment.findAll(queryForPredecessor.toString())
		
		def selectControl = HtmlUtil.genHtmlSelect("${idPrefix}_${taskDependency.id}", "${name}", "", predecessors, "id", "", predecessor.id, "")
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
	def addNote( def task, def person, def note ) {
		def taskNote = new CommentNote();
		taskNote.createdBy = person
		taskNote.note = note
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
			SET ac.status = :status, ac.actStart = null, ac.actStart = null, ac.dateResolved = null, ac.resolvedBy = null, ac.isResolved=0 \
			WHERE ac.id in (:ids)"
			
		if (tasksMap.tasksWithPred.size() > 0) {
			AssetComment.executeUpdate(updateSql, ['status':AssetCommentStatus.PENDING, 'ids':tasksMap.tasksWithPred ] )
		}
		if (tasksMap.tasksNoPred.size() > 0) {
			AssetComment.executeUpdate(updateSql, ['status':AssetCommentStatus.READY, 'ids':tasksMap.tasksNoPred ] )
		}
		if (tasksMap.tasksWithNotes.size() > 0) {
			CommentNote.executeUpdate("DELETE FROM CommentNote cn WHERE cn.assetComment.id IN (:ids) AND cn.note LIKE 'Reverted task status from%'", 
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
}
