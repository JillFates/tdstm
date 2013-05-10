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
import groovy.text.GStringTemplateEngine as Engine

import grails.util.GrailsNameUtils

import org.hibernate.SessionFactory;
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

class TaskService {

	static transactional = true

	def dataSource
	def jdbcTemplate
	// def namedParameterJdbcTemplate
	def partyRelationshipService
	def securityService
	def quartzScheduler
	def workflowService
	def grailsApplication

	def ctx = AH.application.mainContext
    def sessionFactory = ctx.sessionFactory
	
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
		def namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource)

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

			sql.append("AND( t.assigned_to_id=:assignedToId OR \
				(${roles ? 't.role IN (:roles) AND ' : ''}	t.status IN (:statuses) \
				AND t.hard_assigned=0 OR (t.hard_assigned=1 AND t.assigned_to_id=:assignedToId) ) ) ")
		
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
	* @param String the status that the task is/has been set to
	* @param Person the person that is invoking this method (optional) if not passed it will find user via securityService
	* @param Boolean flag if the whom person is a Project Manager (optional)
	* @return void
	*/
	def triggerUpdateTaskSuccessors(taskId, status, whom=null, isPM=false) {
		def task = AssetComment.read(taskId)

		/*
		if (++tries > 10) {
			log.error "triggerUpdateTaskSuccessors: aborting after $tries tries for task $task"
			return
		}
		*/

		if (! task) {
			log.error "triggerUpdateTaskSuccessors: unable to find task id $taskId"
		} else {
			if (whom == null) {
				whom = securityService.getUserLoginPerson()
				isPM = securityService.hasRole("PROJ_MGR")
			}
			long startTime = System.currentTimeMillis() + (300L)
			Trigger trigger = new SimpleTrigger("tm-updateTaskSuccessors-${taskId}" + System.currentTimeMillis(), null, new Date(startTime) )
			trigger.jobDataMap.putAll( [ taskId:taskId, whomId:whom.id, status:status, isPM:isPM, tries:0L ] )
			trigger.setJobName('UpdateTaskSuccessorsJob')
			trigger.setJobGroup('tdstm-task-update')
  
			def result = quartzScheduler.scheduleJob(trigger)
			log.info "triggerUpdateTaskSuccessors: scheduled job for task(#:${task.taskNumber} Id:${taskId}), status=$status, scheduled=$result - $whom"
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
		def moveEvent = task.moveEvent
		def category = predecessor.category
		def paramsMap = [selectId:"${idPrefix}_${taskDependency.id}", selectName:"${name}", options:[predecessor] , optionKey:"id",
						  optionSelected:predecessor.id, 
					      javascript:"onmouseover=\'generateDepSel(${task.id}, ${taskDependency.id}, \"${category}\", \"${predecessor.id}\", \"${idPrefix}\", \"${name}\")\'"]
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
	 *	Used to clear all Task data that are associated to a specified event  
	 * @param moveEventId
	 * @return void
	 */
	def resetTaskData(def moveEvent) {
		// We want to find all AssetComment records that have the MoveEvent or are associate with assets that are 
		// with bundles that are part of the event. With that list, we will reset the
		// AssetComment status to PENDING by default or READY if there are no predecessors and clear several other properties.
		// We will also delete Task notes that are auto generated during the runbook execution.
		def msg
		log.info("resetTaskData() was called for moveEvent(${moveEvent})")
		try {
			def tasksMap = getMoveEventTaskLists(moveEvent.id)
		    def (taskResetCnt, notesDeleted) = [0, 0]
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
				// Delete any of the audit comments that are created during the event
				notesDeleted = CommentNote.executeUpdate("DELETE FROM CommentNote cn WHERE cn.assetComment.id IN (:ids) AND cn.isAudit=1", 
					[ 'ids':tasksMap.tasksWithNotes ] )
			}
			
			msg = "$taskResetCnt tasks reset and $notesDeleted audit notes were deleted"
		} catch(e) {
			log.error "An error occurred while trying to Reset tasks for moveEvent ${moveEvent} on project ${moveEvent.project}\n$e"
			throw new RuntimeException("An unexpected error occured")
		}
		return msg
	}
	
	/**
	 *	Used to delete all Task data that are associated to a specified event. This deletes the task, notes and dependencies on other tasks.
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
			log.error "An unexpected error occured while trying to delete autogenerated tasks for event $moveEvent for project $moveEvent.project\n${e.getMessage()}"
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
			def succecessor = dependency == 'predecessor' ? depTask.predecessor : depTask.assetComment
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
	* Create a Task based on a bundle workflow step
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
				log.error "There is an error in the runbook recipe for project event ${moveEvent.project} - ${moveEvent}\n${e.getMessage}"
			}
		}
		return recipe
	}
	
	/**
	 * Determines the number of assets and how many are completed on a particular cart
	 * @param MoveEvent object
	 * @param String cartName
	 * @return Map - containing [total:#, done:#] or null if cart not found
	 **/
	def getCartQuantities(moveEvent, cartName) {
		def map = null
		if (moveEvent) {			
			def bundleIds = moveEvent.moveBundles*.id
			// log.info "bundleIds=[$bundleIds] for moveEvent ${moveEvent.id}"	
		
			def cartQtySQL = """SELECT count(*) AS total, SUM(IF(task.status=:status,1,0)) AS done 
				FROM asset_entity ae
				JOIN asset_comment task ON task.asset_entity_id=ae.asset_entity_id AND move_event_id=:moveEventId
				WHERE move_bundle_id IN (:moveBundleIds) AND task.role='CLEANER' 
				AND ae.cart=:cartName
				ORDER BY cart"""
			
			def namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource)
			def params = [status:AssetCommentStatus.DONE, moveEventId:moveEvent.id, moveBundleIds:bundleIds, cartName:cartName]
			def cartInfo = namedParameterJdbcTemplate.queryForList( cartQtySQL, params )
			log.info "moveEvent ${moveEvent.id} : bundleIds $bundleIds : cart $cartName : info $cartInfo"
			if (cartInfo) map=cartInfo[0]

		}
		return map
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
	 * Used to generate the runbook tasks for a specified project's Event
	 * @param Person whom - the individual that is generating the tasks
	 * @param MoveEvent moveEvent - the event to process
	 * @return String - the messages as to the success of the function
	 */
	def generateRunbook( whom, moveEvent ) {
		def startedAt = new Date()
		
		// List of all bundles associated with the event
		def bundleList = moveEvent.moveBundles
		log.info "bundleList=[$bundleList] for moveEvent ${moveEvent.id}"		
		def bundleIds = bundleList*.id
		def project = moveEvent.project

		// These buffers are used to capture status output for short-term
		StringBuffer out = new StringBuffer()
		StringBuffer exceptions = new StringBuffer()
		
		// List of all assets associated with the event
		// def assetList = AssetEntity.findAll("from AssetEntity a WHERE a.moveBundle.id IN (:bundleIds)", [bundleIds:bundleIds] )
		
		if (bundleIds.size() == 0) {
			return "There are no Bundles assigned to the Event, which are required."
		}

		def categories = GormUtil.asQuoteCommaDelimitedString(AssetComment.moveDayCategories) 
		
		// log.info "${assetList.size()} assets found for event ${moveEvent}"
		
		// Fail if there are already moveday tasks that are autogenerated
		def existingTasks = jdbcTemplate.queryForInt("select count(*) from asset_comment a where a.move_event_id=${moveEvent.id} \
			and a.auto_generated=true and a.category in (${categories})")
		// log.info "existingTask count=$existingTasks"
		if ( existingTasks > 0 ) {
			return "Unable to generate tasks as there are moveday tasks already generated for the project"
		}

		// Get the various workflow steps that will be used to populate things like the workflows ids, durations, teams, etc when creating tasks
		def workflowStepSql = "select mb.move_bundle_id AS moveBundleId, wft.*, mbs.* \
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
		def taskList = [:]			// an array that will contain list of all tasks that are inserted as they are created
		def collectionTaskSpecIds = []	// An array of the task spec ids that are determined to be collections (sets, trucks, cart, rack, gateway, milestones, etc) 
		def lastMilestone = null	// Will track the most recent milestone task
		def assetsLatestTask = [:]	// This map array will contain reference of the assets' last assigned task
		def taskSpecList = [:]		// Used to track the ID #s of all of the taskSpecs in the recipe and holds last task created for some special cases
		def groups = [:]			// Used to hold groups of assets as defined in the groups section of the recipe that can then be reference in taskSpec filters
		def terminalTasks = []		// Maintains the list of general tasks indicated that are terminal so that milestones don't connect to them as successors
		
		def wfList = null			// get list of workflows for each bundle
		
		def isMilestone=false		// Will flag if the current taskSpec is a milestone
		def isGateway = false
		def isAsset = false
		def isGeneral = false
		def isAction = false
		def isRequired=true			// Used to hold the taskSpec.predecessor.required param or default to true
		def failure = ''			// Will hold the message if there is an exception
		def lastTaskSpec = null		// Holds the last task spec 

		def newTask
		def msg
		
		def recipe = getMoveEventRunbookRecipe( moveEvent )
		def recipeId = recipe?.id
		def recipeGroups = recipe?.groups
		def recipeTasks = recipe?.tasks		
		if (! recipeTasks || recipeTasks.size() == 0) {
			return "There appears to be no runbook recipe or there is an error in its format"
		}
		// log.info "Runbook recipe: $recipe"

		// Load the taskSpecList array used for validation, etc
		def taskSpecIdList = recipeTasks*.id
		log.info "taskSpecIdList=$taskSpecIdList"
				
		def noSuccessorsSql = "select t.asset_comment_id as id \
	    	from asset_comment t \
	  		left outer join task_dependency d ON d.predecessor_id=t.asset_comment_id \
			where t.move_event_id=${moveEvent.id} AND d.asset_comment_id is null \
			AND t.auto_generated=true \
			AND t.category IN (${categories}) "
		// log.info("noSuccessorsSql: $noSuccessorsSql")
		
		def depCount = 0
		def specCount = 0
		
		/**
		 * A helper closure used by generateRunbook to link a task to its predecessors by asset or milestone
		 * @param AssetComment (aka Task)
		 */
		def linkTaskToMilestone = { taskToLink ->
			if (lastMilestone) {
				log.info "linkTaskToMilestone - $taskToLink"
				depCount += createTaskDependency( lastMilestone, taskToLink, taskList, isRequired, out)

				if (isRequired && taskToLink.assetEntity) {
					assetsLatestTask[taskToLink.assetEntity.id] = taskToLink
					log.info "Added latest task $taskToLink to asset ${taskToLink.assetEntity} - 1"
				}
				// assetsLatestTask.put(taskToLink.assetEntity.id, taskToLink)									
			} else {
				log.info "linkTaskToMilestone Task(${taskToLink}) has no predecessor tasks"
				exceptions.append("Task(${taskToLink}) has no predecessor tasks<br/>")
			}			
		}
		
		/**
		 * A helper closure used by generateRunbook to link a task to its predecessors by asset or milestone
		 * @param AssetComment (aka Task)
		 */
		def linkTaskToLastAssetOrMilestone = { taskToLink ->
			// See if there is an asset and that there are previous tasks for the asset
			log.info "linkTaskToLastAssetOrMilestone: assetsLatestTask=${assetsLatestTask.size()}"
			if ( taskToLink.assetEntity 
				 && assetsLatestTask.containsKey(taskToLink.assetEntity.id) 
				 && assetsLatestTask[taskToLink.assetEntity.id].taskNumber != taskToLink.taskNumber 
			) {
				log.info "linkTaskToLastAssetOrMilestone - $taskToLink"
				depCount += createTaskDependency( assetsLatestTask[taskToLink.assetEntity.id], taskToLink, taskList, isRequired, out)

				if ( (isMilestone || isRequired) ) {
					assetsLatestTask[taskToLink.assetEntity.id] = taskToLink
					log.info "linkTaskToLastAssetOrMilestone: Added latest task $taskToLink to asset ${taskToLink.assetEntity} - 2"
					
				}
				out.append("Created dependency between ${assetsLatestTask[taskToLink.assetEntity.id]} and $taskToLink<br/>")
				// Now we can associate this new task as the latest task for the asset									
				// assetsLatestTask.put(taskToLink.assetEntity.id, taskToLink)
				
			} else {
				log.info "linkTaskToLastAssetOrMilestone: isRequired=$isRequired, task.asset=${taskToLink.assetEntity}"
				linkTaskToMilestone( taskToLink )
			}			
		}
		
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
				wfsd = workflowSteps.find{ it.moveBundleId==moveBundleId && it.code == workflowStepCode }
				if (!wfsd) {
					exceptions.append("Unable to find workflow step code $workflowStepCode for bundleId $moveBundleId<br/>")
				}
			} else if (workflowStepCode) {
				// We have a workflow code but don't know which bundle. This will happen on the start and Completed tasks as there are no
				// Assets associated to the step and therefore can't tie it to a bundle. This is a bit of a hack in that it is just going to
				// find the first bundle. We could improve this to find the one with the latest completion time which is what we're getting 
				// it for in a Milestone.
				wfsd = workflowSteps.find{ it.code == workflowStepCode }
				if (!wfsd) {
					exceptions.append("Unable to find workflow step code $workflowStepCode<br/>")
				}
			}
			return wfsd
		}

		/**
		 * A helper closure that bumps the predecessor task's associated assets to the current task if the previous task
		 * was a collection type task.
		 * @param AssetComment - the predecessor task to the current task
		 * @param AssetComment - the current task to associate the assets to
		 * @return void
		 */
		def bumpAssetLastTask = { predTask, currTask ->
			// Examine the task to see if it was generated by a collection type of TaskSpec (e.g. sets, gateways, milestones) as we 
			// need to move along all of the assets that funneled through the task.
			// TODO : Could ignore collections that are terminal - need to think that one through though
			if (collectionTaskSpecIds.contains(predTask.taskSpec)) {
				// Find all the assets that have this task as their predecessor and update their latest task to this one
				def tasksToBump = TaskDependency.findAllByAssetComment(predTask)
				tasksToBump.each() {
					if (it.predecessor.assetEntity) {
						assetsLatestTask[it.predecessor.assetEntity.id] = currTask
						log.info "Bumped task $it to task $currTask"
					}									
				}
			}
		}
		
		
		def maxPreviousEstFinish = null		// Holds the max Est Finish from the previous taskSpec
		def workflow
		try {

			// First load up the 'groups' if any are defined 
			if ( recipeGroups ) {
				def gCount = 0
				recipeGroups.each() { g -> 
					gCount++
					if (! g.name || g.name.size() == 0) {
						msg = "Group specification #${gCount} missing required 'name' property"
						throw new RuntimeException(msg)					
					}
					if ( g.filter?.containsKey('taskSpec') ) {
						msg = "Group specification (${g.name}) references a taskSpec which is not supported"
						throw new RuntimeException(msg)
					}
					groups[g.name] = findAllAssetsWithFilter(moveEvent, g.filter, groups, exceptions)
					if ( groups[g.name].size() == 0 ) {
						exceptions.append("Found zero (0) assets for group ${g.name}<br/>")
					} else {
						log.info "Group ${g.name} contains: ${groups[g.name]}"
					}
					out.append("Group ${g.name} has ${groups[g.name].size()} asset(s)<br/>")
				}
			}

			// Now iterate over each of the task specs
			recipeTasks.each { taskSpec ->
				def tasksNeedingPredecessors = []	// Used for wiring up predecessors in 2nd half of method
				isMilestone = false
				isGateway = false
				isAsset = false
				isGeneral = false
				isAction = false
				isRequired = true
				newTask = null
				def depMode = ''			// Holds [s|r] for assetTask.predecessor.mode to indicate s)upports or r)equires
				def mapMode = ''
				def hasPredecessor = false
				def hasPredGroup = false
				def hasTaskSpec = false
				def predecessor = null
				def ignorePred = false
				def findParent = false		// Flag if the taskspec requires linking predecessor to a parent predecessor for an asset related task
				
				// because it could cause adverse dependency linkings.
				log.info "##### Processing taskSpec $taskSpec"

				// ----
				// VALIDATION of the TaskSpec
				// ----
				// Make sure that the user define the taskSpec.id and that it is NOT a duplicate from a previous step
				specCount++
				if (! taskSpec.containsKey('id')) {
					throw new RuntimeException("TaskSpec for step $specCount in list is missing the 'id' property")
				}
				if (taskSpecList.containsKey(taskSpec.id)) {
					throw new RuntimeException("TaskSpec for step $specCount duplicated id ${taskSpec.id} found in step ${taskSpecList[taskSpec.id]}")
				}
				
				// Save the taskSpec in a map for later reference
				taskSpecList[taskSpec.id] = taskSpec

				// Determine if the taskSpec has the predecessor.required property and if it is of the Boolean type
				if ( taskSpec.containsKey('predecessor') ) {
					predecessor = taskSpec.predecessor
					if ( ! ( predecessor instanceof java.util.LinkedHashMap) ) {
						throw new RuntimeException("TaskSpec (${taskSpec.id}) has invalid syntax for parameter 'predecessor' (${predecessor} ISA ${predecessor.getClass()}).")
					} else {
						hasPredecessor = true
						if ( predecessor.containsKey('required') && ! (predecessor.required instanceof Boolean) ) {
							msg = "TaskSpec (${taskSpec.id}) property 'predecessor.required' has invalid value (${predecessor.required}) "
							log.error("$msg for Event $moveEvent")
							throw new RuntimeException(msg)
						} else {
							isRequired = predecessor.containsKey('required') ? predecessor.required : true
						}
						if ( predecessor.containsKey('ignore') && ! (predecessor.ignore instanceof Boolean) ) {
							msg = "TaskSpec (${taskSpec.id}) property 'predecessor.ignore' has invalid value (${predecessor.ignore}) "
							log.error("$msg for Event $moveEvent")
							throw new RuntimeException(msg)
						} else {
							ignorePred = predecessor.containsKey('ignore') ? predecessor.ignore : false
						}
						if ( predecessor.containsKey('mode') ) {
							depMode = predecessor.mode[0].toLowerCase()
							if ( ! 'sr'.contains( depMode ) ) {
								log.info("Task Spec (${taskSpec.id}) has invalid predecessor.mode value (${predecessor.mode})")
								throw new RuntimeException("Task Spec (${taskSpec.id}) has invalid predecessor value (${predecessor.mode}) " +
									"- valid options are [supports|requires]")	
							}
						}
						
						hasPredGroup = predecessor.containsKey('group')
						hasTaskSpec = predecessor.containsKey('taskSpec')
						findParent = predecessor.containsKey('parent')
							
						if ( depMode && ! taskSpec.filter ) {
							log.info("Task Spec (${taskSpec.id}) contains predecessor.mode which also requires a filter for assets")
							throw new RuntimeException("Task Spec (${taskSpec.id}) contains predecessor.mode which also requires a filter for assets")
						}
						
						// Check for mutually exclusive mode, group and taskSpec
						if ( (depMode && ( hasPredGroup || hasTaskSpec )) || (hasPredGroup && hasTaskSpec) ) {
							msg = "Task Spec (${taskSpec.id}) contains predecessor 'mode', 'group' and/or 'taskSpec' which mutually exclusive"
							log.info(msg)
							throw new RuntimeException(msg)							
						}	
						
						// Make sure we have one of the these methods to find predecessors
						if (! (depMode || hasPredGroup || hasTaskSpec || ignorePred || findParent ) ) {
							msg = "Task Spec (${taskSpec.id}) contains 'predecessor' section that requires one of the properties [mode|group|ignore|parent|taskSpec]"
							log.info(msg)
							throw new RuntimeException(msg)							
						}	
					}
				}

				// Flag used to determine if this taskSpec will create task(s) that are terminal (won't get connected to subsequent Milestones)
				def isTerminal = false
				if (taskSpec.containsKey('terminal')) {
					if (taskSpec.terminal instanceof Boolean) {
						isTerminal = taskSpec.terminal
					} else {
						msg = "Task Spec (${taskSpec.id}) property 'terminal' has invalid value, options are [true | false]"
					}
				}
 				
				// ----
				
				def taskWorkflowCode = taskSpec.containsKey('workflow') ? taskSpec.workflow : null
				
				// Validate that the taskSpec has the proper type
				def stepType 

				if (taskSpec.containsKey('action')) {
					stepType = 'action'
				} else {
					stepType = taskSpec.containsKey('type') ? taskSpec.type.toLowerCase() : 'asset'
					def validStepTypes = ['action','asset','general','gateway','milestone']
					if ( ! validStepTypes.contains(stepType) ) {
						msg = "TaskSpec (${taskSpec.id}) has invalid 'type' value ($stepType)"
						log.error("$msg  for Event $moveEvent")
						throw new RuntimeException("$msg - valid types are [${validStepTypes.join('|')}]")
					}
				}
				
				out.append("=======<br/>Processing taskSpec ${taskSpec.id}-${taskSpec.description} ($stepType):<br/>")

				switch (stepType) {
					
					case 'milestone':
						// -------------------------
						// Handle Milestone tasks
						// -------------------------
						out.append("Creating milestone ${taskSpec.title}<br/>")
						
						isMilestone = true
						workflow = getWorkflowStep(taskWorkflowCode)		
						newTask = createTaskFromSpec(recipeId, whom, taskList, ++lastTaskNum, moveEvent, taskSpec, workflow)
						lastMilestone = newTask 

						// Identify that this taskSpec is a collection type
						collectionTaskSpecIds << taskSpec.id
						
						log.info "milestone isRequired = $isRequired"
						
						// Adjust the SQL query to exclude any terminal tasks 
						//def noSuccessorsSqlFinal = (terminalTasks.size() == 0) ? noSuccessorsSql : 
						//	"$noSuccessorsSql AND t.asset_comment_id NOT IN (${GormUtil.asCommaDelimitedString(terminalTasks)})"
						
						// Now find all tasks that don't have have successor (excluding the current milestone task) and
						// create dependency where the milestone is the successor
						def tasksNoSuccessors = []
						taskList.each() { id, t ->
							if (! t.metaClass.hasProperty(t, 'hasSuccessorTaskFlag') && ! terminalTasks.contains(t.id) ) {
								tasksNoSuccessors << t
							}
						}

						//def tasksNoSuccessors = jdbcTemplate.queryForList(noSuccessorsSqlFinal)
						//log.info "SQL for noSuccessorsSql: $noSuccessorsSqlFinal"
						log.info "generateRunbook: Found ${tasksNoSuccessors.count()} tasks with no successors for milestone ${taskSpec.id}, $moveEvent"
					
						if (tasksNoSuccessors.size()==0 && taskList.size() > 1 ) {
							out.append("Found no successors for a milestone, which is unexpected but not necessarily wrong - Task $newTask<br/>")
						}
						tasksNoSuccessors.each() { p ->
							// Create dependency as long as we're not referencing the task just created
							if (p.id != newTask.id ) {
								def predecessorTask = AssetComment.read(p.id)
								depCount += createTaskDependency( predecessorTask, newTask, taskList, isRequired, out )

								if (predecessorTask.assetEntity) {
									// Move the asset's last task to the milestone task
									// log.info "Predecessor task was to an asset"
									assetsLatestTask[predecessorTask.assetEntity.id] = newTask
									log.info "Added latest task $newTask to asset ${predecessorTask.assetEntity} - 3"
								}
							}

							// Bump along any predecessor assets to this task if the predecessor was a collection taskSpec
							bumpAssetLastTask(p, newTask)


						}	
						// We are done with predecessors, etc.
						break
						
					case 'gateway':
						// Handle GATEWAY tasks
						// We create a simple task and then wire-in the dependencies to tasks generated by taskSpec referenced in predecessor property
						isGateway = true
						if (depMode) {
							msg = "TaskSpec (${taskSpec.id}) of type 'gateway' does not support 'mode' property"
							log.info(msg)
							throw new RuntimeException(msg)
						}
						if (! hasPredecessor ) {
							msg = "Gateway TaskSpec ID ${taskSpec.id} is missing required 'predecessor' property"
							log.info(msg)
							throw new RuntimeException(msg)							
						}
						
						newTask = createTaskFromSpec(recipeId, whom, taskList, ++lastTaskNum, moveEvent, taskSpec, null)
						tasksNeedingPredecessors << newTask
						mapMode = 'DIRECT_MODE'

						msg = "Created GW task $newTask from taskSpec ${taskSpec.id}"
						log.info(msg)
						out.append("$msg<br/>")

						// Identify that this taskSpec is a collection type
						collectionTaskSpecIds << taskSpec.id
						
						break

					case 'action':
						// Handle ACTION TaskSpecs (e.g. OnCart, offCart, QARAck) Tasks
						isAction = true
						def action = taskSpec.action.toLowerCase()
						switch(action) {
							
							// RollCall Tasks for each staff involved in the Move Event
							case 'rollcall':
								def rcTasks = createRollcallTasks( moveEvent, lastTaskNum, whom, recipeId, taskSpec )
								if (rcTasks.size() > 0) {
									rcTasks.each() { rct ->
										taskList[rct.id] = rct
									}
									tasksNeedingPredecessors.addAll(rcTasks)
									lastTaskNum = rcTasks.last().taskNumber
									mapMode = 'DIRECT_MODE'
								} else {
									exceptions.append("Roll Call action did not create any tasks<br/>")
								}
								out.append("${rcTasks.size()} Roll Call tasks were created<br/>")
								break
								
							// Create a task for each Rack that is associated with Assets in the filter and connect them 
							// with the appropriate predecessors.
							case 'rack':
							case 'truck':
							case 'room':
							case 'cart':
							case 'location':
							case 'set':

								// Track that this taskSpec is a collection type
								collectionTaskSpecIds << taskSpec.id

								def actionTasks = createAssetActionTasks(action, moveEvent, lastTaskNum, whom, recipeId, taskSpec, groups, exceptions)
								if (actionTasks.size() > 0) {
									// Throw the new task(s) into the collective taskList using the id as the key
									actionTasks.each() { t ->
										taskList[t.id] = t
									}
									tasksNeedingPredecessors.addAll(actionTasks)
									lastTaskNum = actionTasks.last().taskNumber
									mapMode = 'MULTI_ASSET_DEP_MODE'
								} else {
									exceptions.append("$action action did not create any tasks for taskSpec(${taskSpec.id})<br/>")
								}
								out.append("${actionTasks.size()} $action tasks were created for taskSpec(${taskSpec.id})<br/>")
								break								
							
							default:
								exceptions.append("Action(${taskSpec.action}) in taskSpec id ${taskSpec.id} presently not supported<br/>")
						}
						break
					
					case 'asset':
					
						// -------------------------
						// Create ASSET based Tasks
						// -------------------------
						isAsset = true
						
						// Normal tasks need to have a filter
						if (! taskSpec.filter || taskSpec.filter.size() == 0) {
							exceptions.append("TaskSpec id ${taskSpec.id} for asset based task requires a filter<br/>")						
						}
					
						def assetsForTask = findAllAssetsWithFilter(moveEvent, taskSpec.filter, groups, exceptions)
						log.info "Found (${assetsForTask?.size()}) assets for taskSpec ${taskSpec.id}-${taskSpec.description}"
						if ( !assetsForTask || assetsForTask.size()==0) return // aka continue
					
						//
						// Create a task for each asset based on the filtering of the taskSpec
						//
						assetsForTask?.each() { asset ->
							workflow = getWorkflowStep(taskWorkflowCode, asset.moveBundle.id)
							newTask = createTaskFromSpec(recipeId, whom, taskList, ++lastTaskNum, moveEvent, taskSpec, workflow, asset)
							tasksNeedingPredecessors << newTask
							out.append("Created task $newTask<br/>")
						} 
						
						// If we have predecessor.mode (s|r) then we'll doing linkage via Asset Dependency otherwise we'll link asset to asset directly
						mapMode = depMode ? 'ASSET_DEP_MODE' : 'DIRECT_MODE'
						
						break
						
					case 'general':
						// ---------------------------
						// Create GENERAL type Task(s)
						// ---------------------------
						isGeneral=true
						def isChain = true
						if (taskSpec.containsKey('chain')) {
							if (taskSpec.chain instanceof Boolean) {
						   		isChain = taskSpec.chain
							} else {
								throw new RuntimeException("Task Spec (${taskSpec.id}) 'chain' property has invalid value. Acceptible values (true|false)")
							}
						}
						def genTitles = (taskSpec.title instanceof java.util.ArrayList) ? taskSpec.title : [ taskSpec.title ]
						def genLastTask = null
						
						genTitles.each() { gt -> 
							// Replace the potential title:[array] with just the current title
							taskSpec.title = gt
							newTask = createTaskFromSpec(recipeId, whom, taskList, ++lastTaskNum, moveEvent, taskSpec, null, null)

							if (isTerminal) {
								// Track all terminal tasks so that they don't get linked by milestones
								terminalTasks << newTask.id
							}

							if (isChain) {
								// If is chain then we link first task to the predecessor dependencies and remainder to each other
								
								// Track the last task created for anything linking to the this task spec								
								taskSpecList[taskSpec.id].lastTask = newTask
								
								if (! genLastTask) {
									// Only add the first task to get normal predecessor linkage or if it is NOT chained tasks
									tasksNeedingPredecessors << newTask
								} else {
									depCount += createTaskDependency(genLastTask, newTask, taskList, isChain, out)
								}
								genLastTask = newTask
								
							} else {
								// If not chain, link each of them to the predecessor dependencies						
								tasksNeedingPredecessors << newTask
							}
							
						}
						if ( ! isChain && ! predecessor && lastTaskSpec ) {
							// If the taskSpec doesn't have a predecessor and there is a previous taskSpec, then we'll fake out the system by
							// adding a predecessor clause to the taskSpec so that the task(s) will be successors to the previous taskSpec's task(s).
							predecessor = [ taskSpec: lastTaskSpec.id ]
							taskSpec.predecessor = predecessor
							hasTaskSpec = true
						}
							
						mapMode = 'DIRECT_MODE'
						break
						
					default:
						// We should NEVER get into this code but just in case...
						log.error("Task Spec (${taskSpec.id}) has an unhandled type ($stepType) in the code for event $moveEvent")
						throw new RuntimeException("Task Spec (${taskSpec.id}) has an unhandled type ($stepType) in the code")
						
				} // switch (stepType)
				
				// ------------------------------------------------------------------------------------
				// DEPENDENCY MAPPING - Now wire up the tasks that were just created
				// ------------------------------------------------------------------------------------
				
				// Mapping will be done for the following use-cases:
				//
				//   ASSET_MODE: Map dependencies based on asset relationships.
				//        i. TaskSpec contains predecessor.mode - then wire tasksNeedingPredecessors to their assets' previous step or prior milestone if none was found 
				//       ii. This can support group and other predecessor filter parameters
				//      iii. If group is presented then the tasksNeedingPredecessors will be wired to those found in the group and exceptions reported for those not found
				//   TASK_MODE: Map dependencies based on predecessor.taskSpec
				//        i. tasksNeedingPredecessors and referenced taskSpec both have assets then they will be wired one-to-one
				//       ii. Either tasksNeedingPredecessors or referenced taskSpec DON'T have assets, all tasksNeedingPredecessors will be wired to all tasks from taskSpec
				//
				// The predecessor.required when false, will not update the asset with the latest task for any of the above use-cases
				log.info "&&&&& ignorePred=$ignorePred"
				if (tasksNeedingPredecessors.size() > 0 && ! ignorePred ) {
				
					// Set some vars that will be used in the next iterator 
					def predecessorTasks = []

					// Make sure predecessor is an array
					if ( ! taskSpec.predecessor instanceof java.util.ArrayList) {
						log.info("Task Spec (${taskSpec.id}) predecessor property is not properly formatted.")
						throw new RuntimeException("Task Spec (${taskSpec.id}) predecessor property is not properly formatted.")
					}
				
					// Determine if the predecessor tasks will be required to flow through the tasksNeedingPredecessors or just that there is an successor relationship
					// If false, we just don't update the assets' latest task in the array.
					isRequired = hasPredecessor && taskSpec.predecessor.containsKey('required') ? taskSpec.predecessor.required : true
					if ( ! (isRequired instanceof java.lang.Boolean) ) {
						log.info("Task Spec (${taskSpec.id}) has invalid value for taskSpec.predecessor.required (${isRequired.class})")
						throw new RuntimeException("Task Spec (${taskSpec.id}) has invalid value for taskSpec.predecessor.required")
					}
				
					def predAssets = [:]
				
					// Perform some setup based on the mode
					switch(mapMode) {
					
						case 'ASSET_DEP_MODE':
						case 'MULTI_ASSET_DEP_MODE':
							break
						
						case 'DIRECT_MODE':
					
							// Cases 
							// = using taskSpec - preload predecessorTasks with tasks matching taskSpec
							// = using group - preload predAssets map with assets within the group
							// = has no predecessor defined - n/a 
						
							if ( hasPredGroup ) {
								// If there are any groups defined then preload all of the assets that are included in the groups to be used 
								// in the next each/switch code block.
								log.info "hasPredGroup - here"
								// Put the group property into an array if not already an array
								def taskGroups = ( taskSpec.predecessor.group instanceof java.util.ArrayList ) ? taskSpec.predecessor.group : [taskSpec.predecessor.group]

								// Iterate over the list of groups to consolidate one or more groups into 	
								taskGroups.each() { groupCode -> 
									if (groupCode.size() == 0) {
										log.info("generateRunbook: 'filter.group' value ($filter.group) has undefined group code.")
										throw new RuntimeException("'filter.group' value ($filter.group) has undefined group code for taskSpec(${taskSpec.id}")
									}

									// Find the latest task for all of the assets of the specified GROUP
									log.info("assetsLatestTask has ${assetsLatestTask.size()} assets")
									if (groups.containsKey(groupCode)) {
										//hasPredGroup=true
										groups[groupCode].each() { asset ->
											predAssets.put(asset.id, asset)
									
											//if (assetsLatestTask.containsKey(asset.id)) {
											//	predecessorTasks[asset.id] = assetsLatestTask[asset.id]											
											//} else {
											//	exceptions.append("Task Spec (${taskSpec.id}) 'predecessor' unable to find previous task for asset $asset<br/>")
											//}
									
										}									
									} else {
										throw new RuntimeException("Task Spec (${taskSpec.id}) 'predecessor' value ($taskSpec.predecessor) references undefined group.")
									}
								} 
					
								//log.info("Processing taskSpec.predecessor and found ${predecessorTasks.size()} tasks")
								//if (predecessorTasks.size() == 0) {
									// We SHOULD of found some tasks so bomb if we don't
								//	log.info("Task Spec (${taskSpec.id}) 'predecessor' (${taskSpec.predecessor}) found NO predecessor tasks")
								//	throw new RuntimeException("Task Spec (${taskSpec.id}) 'predecessor' (${taskSpec.predecessor}) found NO predecessor tasks")
								//}
							} else if (hasTaskSpec) {
								// Populate predecessorTasks with all tasks referenced in the taskSpec.predecessor.taskSpec

								// Put the group property into an array if not already an array
								def taskSpecs = ( taskSpec.predecessor.taskSpec instanceof java.util.ArrayList ) ? taskSpec.predecessor.taskSpec : [taskSpec.predecessor.taskSpec]
								log.info("taskSpec (${taskSpec.id}) has taskSpecs of $taskSpecs")
								// Iterate over the list of taskSpec IDs	
								taskSpecs.each() { ts -> 
									// Find all predecessor tasks that have the taskSpec ID #
									if ( taskSpecIdList.contains( ts ) ) {
										//if ( taskSpecList[taskSpec.id].containsKey('lastTask') ) {
											// This particular taskSpec only wants to link to the last task (e.g. general chained tasks)
										//	predecessorTasks << taskSpecList[taskSpec.id].lastTask
										//} else {
											taskList.each() { id, t -> 
												if (t.taskSpec.toString() == ts.toString()) {
													predecessorTasks << t
													log.info "Added task to predecessorTasks ${t.class}"
												}
											}
											// predecessorTasks.addAll( taskList.findAll { id, t -> t.taskSpec.toString() == ts.toString() } )	
										//}
									} else {
										msg = "Task Spec (${taskSpec.id}) 'predecessor.taskSpec' value ($ts) references undefined taskSpec.ID."
										log.info(msg)
										throw new RuntimeException(msg)
									}
								}
							}
											
							break
						
						default:
							throw new RuntimeException("Unhandled switch statement for Task Spec (${taskSpec.id}) ($mapMode)")

					} // switch(mapMode)
				
					//	
					// Now iterate over all of the tasks just created for this taskSpec and assign dependencies 
					//
					out.append("# Creating predecessors for ${tasksNeedingPredecessors.size()} tasks<br/>")
				
					tasksNeedingPredecessors.each() { tnp ->
						log.info("tasksNeedingPredecessors.each(): Processing $mapMode for task $tnp")
						def wasWired=false

						switch(mapMode) {
												
							case 'DIRECT_MODE':
								// Link the current task to it's asset's latest task if it exists or to the milestone
								//
								if (findParent) {
									// In this situation we'll look up the parent predecessor task of the last know task of the asset
									if (tnp.assetEntity && assetsLatestTask[tnp.assetEntity.id]) {
										def parentDep = TaskDependency.findByAssetComment(assetsLatestTask[tnp.assetEntity.id])
										if (parentDep) {
											depCount += createTaskDependency(parentDep.predecessor, tnp, taskList, isRequired, out)

											wasWired = true

											// We need to chain the peer task inside of this task so that subsequent task spec for the asset
											// will connect follow the chain and connect to all of the peers.
											tnp.metaClass.setProperty('chainPeerTask', assetsLatestTask[tnp.assetEntity.id])
										}
									}
									assetsLatestTask[tnp.assetEntity.id] = tnp
									break
								}
														
								// Find the last task for the predAsset to create associations between tasks. If the predecessor was created
								// during the same taskStep, assetsLatestTask may not yet be populated so we can scan the tasks created list for
								// one with the same taskSpec id #
								if (hasPredGroup) {
									//
									// --- GROUPS ---
									//
								
									// The predecessor.group was defined so we are dealing with ASSETS and will link tasks one of two ways:
									//   1. If tasks have assets then we bind the current task to the asset's latest task if found in the group. If no found then link current task to milestone
									//   2. If task does NOT have assets, then we bind the current task as successor to latest task of ALL assets in the list
									if (tnp.assetEntity) {
									
										log.info "Processing from hasPredGroup #1"
										// #1 - Link task to it's asset's latest task if the asset was in the group and there is an previous task otherwise link it to the milestone if one exists
										if (predAssets.containsKey( tnp.assetEntity.id )) {
											// Find the latest asset for the task.assetEntity
											if (assetsLatestTask.containsKey(tnp.assetEntity.id)) {										
												depCount += createTaskDependency(assetsLatestTask[tnp.assetEntity.id], tnp, taskList, isRequired, out)

												wasWired = true
												if (isRequired) {
													assetsLatestTask[tnp.assetEntity.id] = tnp
													log.info "Adding latest task $tnp to asset ${tnp.assetEntity} - 4"
												}
											} else {
												// Wire to last milestone
												msg = "No predecessor task found for asset ($tnp.assetEntity) to link to task ($tnp) in taskSpec ${taskSpec.id} (DIRECT_MODE/group)"
												log.info(msg)
												exceptions.append("${msg}<br/>")
												linkTaskToMilestone(tnp)
											}
										} else {
											msg = "Asset ($tnp.assetEntity) was not found in group for taskSpec ${taskSpec.id} (DIRECT_MODE/group)"
											log.info(msg)
											exceptions.append("${msg}<br/>")										
										}
									} else {
										// #2 - Wire latest tasks for all assets in group to this task
										log.info "Processing from hasPredGroup #2 isRequired=$isRequired"

										// log.info("predAssets=$predAssets")
										predAssets.each() { predAssetId, predAsset ->
											//log.info("predAsset=$predAsset")										
											if ( assetsLatestTask.containsKey(predAsset.id)) {
												depCount += createTaskDependency(assetsLatestTask[predAsset.id], tnp, taskList, isRequired, out)
												wasWired = true
												if (isRequired) {
													assetsLatestTask[predAsset.id] = tnp
													log.info "Adding latest task $tnp to asset ${tnp.assetEntity ?: "Non-Asset task"} - 5"
												
												}
											}
										}
									}					
								
								} else if ( hasTaskSpec ) {
									//
									// --- TASKSPEC ---
									//
								
									log.info "Processing from hasTaskSpec"
								
									// Use the predecessorTasks array that was initialized earlier and if those tasks and the current task have assets then wire up
									// the predecessor one-to-one for each asset otherwise wire the current task to all tasks in the predecessorTasks. If both have assets and 
									// we are unable to find a predecessor task for the same asset, the current task will be wired to the most recent milestone if it exists.
									if (predecessorTasks.size() > 0) {
									
										// See if we're linking task to task by asset 
										if (predecessorTasks[0].assetEntity && tnp.assetEntity) {
											// Wire asset-to-asset for tasks if there is a match
											def predTask = predecessorTasks.find { it.assetEntity.id == tnp.assetEntity.id }
											if (predTask) {										
												depCount += createTaskDependency(predTask, tnp, taskList, isRequired, out)

												if (isRequired) {
													assetsLatestTask[tnp.assetEntity.id] = tnp
													log.info "Adding latest task $tnp to asset ${tnp.assetEntity} - 6"
												
												}
												wasWired = true
											} else {
												msg = "No predecessor task for asset (${tnp.assetEntity}) found to link to task ($tnp) in taskSpec ${taskSpec.id} (DIRECT_MODE/taskSpec)"
												log.info(msg)
												exceptions.append("${msg}<br/>")
											}
										} else {
											// Link all predecessorTasks to tnp
											// Wire the current task as the successor to all tasks specified in the predecessor.taskSpec property
											log.info "predecessorTasks=${predecessorTasks.class}"
											predecessorTasks.each() { predTask -> 
												log.info "predTask=${predTask.class}"
												depCount += createTaskDependency(predTask, tnp, taskList, isRequired, out)

												if ( isRequired ) {
													// Update the Asset's last task based on if the previous task is for an asset or the current one is for an asset
													if ( predTask.assetEntity ) {
														assetsLatestTask[predTask.assetEntity.id] = tnp
														log.info "Adding latest task $tnp to asset ${tnp.assetEntity} - 7"
													
													} else if ( tnp.assetEntity )  {
														assetsLatestTask[tnp.assetEntity.id] = tnp
														log.info "Adding latest task $tnp to asset ${tnp.assetEntity} - 8"
													
													}
												}
											}
											wasWired = predecessorTasks.size() > 0										
										} 
									} else {
										msg = "Predecessor task list was empty for taskSpec in taskSpec ${taskSpec.id} (DIRECT_MODE/taskSpec)"
										log.info(msg)
										exceptions.append("${msg}<br/>")
									}
																	
								} else {
									//
									// --- BASIC ASSET PREDECESSOR --
									//
								
									log.info "Processing from Basic Asset Predecessor"
								
									linkTaskToLastAssetOrMilestone(tnp)
									wasWired = true
								}
							
								break
								
							case 'ASSET_DEP_MODE':
								//
								// HANDLE TaskSpecs that reference AssetDependency records based on the filter
								//
								def assetDependencies = getAssetDependencies(tnp.assetEntity, taskSpec, depMode)
				
								if (assetDependencies.size() == 0) {
									exceptions.append("Asset(${tnp.assetEntity}) for Task(${tnp}) has no ${taskSpec.predecessor} relationships<br/>")

									// Link task to the last known milestone or it's asset's previous task
									linkTaskToLastAssetOrMilestone(tnp)
									assetsLatestTask[tnp.assetEntity.id] = tnp
									wasWired = true
								
								} else {
									// Look over the assets dependencies that are associated to the current task's asset and create predecessor relationships 
									// We will warn on assets that are not part of the moveEvent that have dependency. 
									// TODO : We most likely will want to have tasks for assets not moving in the future but will require discussion.
									log.info "Iterate over ${assetDependencies.size()} dependencies for asset ${tnp.assetEntity}"
									assetDependencies.each { ad ->


										// Note that this should be the opposite of that used in the getAssetDependencies 
										def predAsset = depMode == 's' ?  ad.dependent : ad.asset

										// Make sure that the other asset is in one of the bundles in the event
										def predMoveBundle = predAsset.moveBundle
										if (! predMoveBundle || ! bundleIds.contains(predMoveBundle.id)) {
											exceptions.append("Asset dependency references asset not in event: task($tnp) between asset ${tnp.assetEntity} and ${predAsset}<br/>")
										} else {
											// Find the last task for the predAsset to create associations between tasks. If the predecessor was created
											// during the same taskStep, assetsLatestTask may not yet be populated so we can scan the tasks created list for
											// one with the same taskSpec id #
											def previousTask = null
											taskList.each() { id, t -> 
												if (t.assetEntity?.id == predAsset.id && t.taskSpec == tnp.taskSpec) {
													previousTask = t
												}
											}
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
												depCount += createTaskDependency(previousTask, tnp, taskList, isRequired, out)

												if (isRequired) {
													assetsLatestTask[tnp.assetEntity.id] = tnp
													log.info "Adding latest task $tnp to asset ${tnp.assetEntity} - 9"
												
												}
												wasWired=true
											} else {
												log.info "No predecessor task found for asset ($predAsset) to link to task ($tnp)"
												exceptions.append("No predecessor task found for asset ($predAsset) to link to task ($tnp)<br/>")
												// linkTaskToMilestone(tnp)
											}
										}
									}
								}							
								break
								
							case 'MULTI_ASSET_DEP_MODE':
								// In this case each task already has multiple assets injected into it so we'll just create the 
								// necessary dependencies for the associatedAssets define in the task.	
								if (! tnp.metaClass.hasProperty(tnp, 'associatedAssets') ) {
									msg = "Task was missing expected assets for dependencies of task $tnp"
									log.info(msg)
									throw new RuntimeException(msg)
								}
								
								// Iterate over the associated assets, setting it to the task and creating the predecessor
								tnp.associatedAssets.each() { assocAsset ->
									tnp.assetEntity = assocAsset
									linkTaskToLastAssetOrMilestone(tnp)
								}
								tnp.assetEntity = null
								wasWired = true

								break
								
							default:
					
								msg = "Unsupported switch value ($mapMode) for taskSpec (${taskSpec.id}) on processing task $tnp"
								log.info(msg)
								throw new RuntimeException(msg)
							
						
						} // switch(mapMode)
			
						if (! wasWired ) {
							// If the task wasn't wired to any predecessors, then try to wire it to the latest milestone
							linkTaskToMilestone(tnp)
						}
					
					} // tasksNeedingPredecessors.each()
					
					
				}
				
				lastTaskSpec = taskSpec

			} // recipeTasks.each() {}
		} catch(e)	{
			failure = e.toString()
			// exceptions.append("We BLEW UP damn it!<br/>")
			// exceptions.append(failure)
			e.printStackTrace()
		}
		
		TimeDuration elapsed = TimeCategory.minus( new Date(), startedAt )
		
		log.info "A total of ${taskList.size()} Tasks and $depCount Dependencies created in $elapsed"
		if (failure) failure = "Generation FAILED: $failure<br/>"
		return "<h2>Status:</h2>${failure}${taskList.size()} Tasks and $depCount Dependencies created in $elapsed<h2>Exceptions:</h2>" + 
	
		exceptions.toString() + "<h2>Log:</h2>" + out.toString()
		
	}
	
	/**
	 * A helper method called by generateRunbook to lookup the AssetDependency records for an Asset as specified in the taskSpec
	 * @param Object asset - can be any asset type (Application, AssetEntity, Database or Files)
	 * @param Map taskSpec - a Task specification map
	 * @return List<AssetDependency> - list of dependencies
 	 */
	def getAssetDependencies(asset, taskSpec, depMode) {
		def list = []
		def filter = taskSpec.filter
		
		// This is the list of properties that can be added to the search criteria from the Filter
		def supportedFilterProps = ['status', 'type', 'dataFlowFreq', 'dataFlowDirection']
		
		// AssetEntity  asset			// The asset that that REQUIRES the 'dependent'
		// AssetEntity dependent		// The asset that SUPPORTS 'asset' 
		def relateOn = ( depMode == 's' ? 'asset' : 'dependent' )
		def sql = "from AssetDependency ad where ad.${relateOn}.id=:assetId and \
			ad.status not in ('${AssetDependencyStatus.NA}', '${AssetDependencyStatus.ARCHIVED}') \
			and ad.type <> '${AssetDependencyType.BATCH}'"
			
		def map = ['assetId':asset.id]
		// log.info "FILTER: $filter"
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
	def createTaskFromSpec(recipeId, whom, taskList, taskNumber, moveEvent, taskSpec, workflow=null, asset=null) {
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
			log.error("createTaskFromSpec: Failed creating task error=${GormUtil.allErrorsString(task)}, asset=$asset, TaskSpec=$taskSpec")
			throw new RuntimeException("Error while trying to create task. error=${GormUtil.allErrorsString(task)}, asset=$asset, TaskSpec=$taskSpec")
		}

		taskList[task.id] = task
		
		log.info "Saved task ${task.id} - ${task}"
		
		return task
	}
	
	/**
	 * Used to create a task dependency between two task that also will examine the predecessor task to see if it has a
	 * chainPeerTask associated to it and if so, will iteratively call itself to create the association with the successor to
	 * the peer task.
	 * @param AssetComment - the predecessor task in the dependency
	 * @param AssetComment - the successor task in the dependency
	 * @param Map<AssetComment> - taskList the list of all tasks so that it can update tasks with the hasSuccessorTaskFlag
	 * @param Boolean - isRequired is a flag that when true will update the hasSuccessorTaskFlag flag on the task in the list
	 * @param Integer - Used to count of the number of recursive iterations that occur
	 * @return TaskDependency
	 * @throws RuntimeError if unable to save the dependency
	 */
	def createTaskDependency( predecessor, successor, taskList, isRequired, out, count=0 ) {
		count++

		log.info "Creating dependency $count predecessor=$predecessor, successor=$successor "
		/*		
		if (predecessor.metaClass?.hasProperty(predecessor, 'hasSuccessorTaskFlag')) {
			log.info "predecessor has hasSuccessorTaskFlag flag"
		}
		if (successor.metaClass?.hasProperty(successor, 'hasSuccessorTaskFlag')) {
			log.info "successor has hasSuccessorTaskFlag flag"
		}
		*/
		
		def dependency = new TaskDependency(assetComment:successor, predecessor:predecessor)
		if (! ( dependency.validate() && dependency.save(flush:true) ) ) {
			throw new RuntimeException("Error while trying to create dependency between predecessor=$predecessor, successor=$successor<br/>Error=${GormUtil.allErrorsString(dependency)}, ")
		}
		out.append("Created dependency (${dependency.id}) between $predecessor and $successor<br/>")

		successor.status = AssetCommentStatus.PENDING
		if (! ( successor.validate() && successor.save(flush:true) ) ) {
			throw new RuntimeException("Failed to update successor ($successor) status to PENDING<br/>Error=${GormUtil.allErrorsString(dependency)}")
		}
		
		if (isRequired) {
			// Update the task in the task list that it has a successor which is used by Milestones
			if ( taskList.containsKey(predecessor.id) ) {
				log.info "Setting hasSuccessorTaskFlag=true on task ${taskList[predecessor.id]}"
				taskList[predecessor.id].metaClass.setProperty('hasSuccessorTaskFlag', true)
			} 
		}

		// Here is the recursive loop if the predecessor has a peer
		if (predecessor.metaClass?.hasProperty(predecessor, 'chainPeerTask')) {
			count = createTaskDependency( predecessor.chainPeerTask, successor, taskList, isRequired, out, count)
		}
		
		return count
	}
	
	/**
	 * This special method is used to find all assets of a moveEvent that match the criteria as defined in the filter map
	 * @param MoveEvent moveEvent object
	 * @param Map filter - contains various attributes that define the filtering
	 * @param loadedGroups Map<List> - A mapped list of the groups and associated assets that have already been loaded
	 * @return List<AssetEntity, Application, Database, File> based on the filter
	 * @throws RuntimeException if query fails or there is invalid specifications in the filter criteria
	 */
	def findAllAssetsWithFilter(moveEvent, filter, loadedGroups, exceptions) {
		def assets = []
		
		if ( filter?.containsKey('group') ) {
			//
			// HANDLE filter.group
			//
			
			//log.info("Groups contains $groups")
			log.info("findAllAssetsWithFilter: group ${filter.group}")

			// Put the group property into an array if not already an array
			def groups = ( filter.group instanceof java.util.ArrayList ) ? filter.group : [filter.group]
				
			// Iterate over the list of groups	
			groups.each() { groupCode -> 
				if (groupCode.size() == 0) {
					log.info("findAllAssetsWithFilter: 'filter.group' value ($filter.group) has undefined group code.")
					throw new RuntimeException("'filter.group' value ($filter.group) has undefined group code.")
				}
				
				// Find all of the assets of the specified GROUP
				// log.info("assetsLatestTask has ${assetsLatestTask.size()} assets")
				if (loadedGroups.containsKey(groupCode)) {
					assets.addAll( loadedGroups[groupCode] )
					log.info("findAllAssetsWithFilter: added ${loadedGroups[groupCode].size()} asset(s) for group $groupCode")
				} else {
					log.error("findAllAssetsWithFilter: 'filter.group' value ($filter.group) references undefined group ($groupCode).")
					throw new RuntimeException("'filter.group' value ($filter.group) references undefined group ($groupCode).")
				}
			}
			if (assets.size() == 0) {
				log.info("findAllAssetsWithFilter: 'filter.taskSpec' group filter found no assets .")
				// throw new RuntimeException("''filter.taskSpec' group filter ($groups) contains no assets.")
			}
			
		} else if (filter?.containsKey('taskSpec')) {
			//
			// HANDLE filter.taskSpec
			//

			log.info("findAllAssetsWithFilter: taskSpec ${filter.taskSpec}")

			// Put the group property into an array if not already an array
			def taskSpecs = ( filter.taskSpec instanceof java.util.ArrayList ) ? filter.taskSpec : [filter.taskSpec]
				
			// Iterate over the list of groups	
			taskSpecs.each() { ts -> 
				// Find all predecessor tasks that have the taskSpec ID # and then add the tasks assetEntity to the assets list 
				def predecessorTasks = []
				taskList.each() { id, t -> 
					if (t.taskSpec.toString() == ts.toString()) {
					 	predecessorTasks << t
					}
				}
				
				if ( predecessorsTasks.size() > 0) {
					if  (predecessorsTasks[0].assetEntity ) {
						assets.addAll( predecessorsTasks*.assetEntity )
					} else {
						log.info("findAllAssetsWithFilter: 'filter.taskSpec' value ($filter.taskSpec) references taskSpec ($groupCode) that does not contain assets.")
						throw new RuntimeException("'filter.group' value ($filter.taskSpec) references taskSpec ($taskSpec) that does not contain assets.")								
					}
				} else {
					log.info("findAllAssetsWithFilter: 'filter.taskSpec' value ($filter.taskSpec) references undefined taskSpec.ID ($ts).")
					throw new RuntimeException("'filter.taskSpec' value ($filter.taskSpec) references undefined taskSpec.ID ($ts).")
				}	
			}
			
			if (assets.size() == 0) {
				log.info("findAllAssetsWithFilter: 'filter.taskSpec' taskSpecs filter found no assets .")
				// throw new RuntimeException("''filter.taskSpec' taskSpecs filter ($taskSpecs) contains no assets.")
			}
			
		} else {
			// 
			// HANDLE performing an actual filter to find assets
			//
			def queryOn = filter?.containsKey('class') ? filter['class'].toLowerCase() : 'device'
			if (! ['device','application', 'database', 'files'].contains(queryOn) ) {
				throw new RuntimeException("An invalid 'class' was specified for the filter (valid options are 'device','application', 'database', 'files') for filter: $filter")
			}
		
			def bundleList = moveEvent.moveBundles
			def bundleIds = bundleList*.id

			// See if they are trying to filter on the Bundle Name
			if (filter?.asset?.containsKey('bundle')) {
				def moveBundle = MoveBundle.findByProjectAndName(moveEvent.project, filter.asset.bundle)
				if (moveBundle) {
					bundleIds = moveBundle.id
				} else {
					throw new RuntimeException("Bundle name ($filter.moveBundle) was not found for filter: $filter")
				}
			}

			def where = ''
			def project = moveEvent.project
			def map = [:]
			def sql
		
			map.bIds = bundleIds
			// log.info "bundleIds=[$bundleIds]"
			
			/** 
			 * A helper closure used below to manipulate the 'where' and 'map' variables to add additional
			 * WHERE expressions based on the properties passed in the filter
			 * @param String[] - list of the properties to examine
			 */
			def addWhereConditions = { list ->
				list.each() { code ->
					if (filter?.asset?.containsKey(code)) {
						// log.info("addWhereConditions: code $code matched")						
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
			def validProperties = ['assetName','assetTag','assetType', 'priority', 'truck', 'cart', 'sourceLocation', 'targetLocation', 'planStatus']
			(1..24).each() { validProperties.add("custom$it".toString()) }	// Add custom1..custom24
			log.info("findAllAssetsWithFilter: validProperties=$validProperties")
			addWhereConditions( validProperties )
		
			//
			// Param 'exclude'
			// Handle exclude filter parameter that will add a NOT IN () cause to the where for references to one or more groups
			//
			if (filter?.containsKey('exclude')) {
				def excludes = []
				def excludeProp = filter.exclude instanceof java.util.ArrayList ? filter.exclude : [filter.exclude]				
				excludeProp.each() { exGroup -> 
					if (loadedGroups?.containsKey(exGroup)) {
						excludes.addAll(loadedGroups[exGroup])							
					} else {
						exceptions.append("Filter 'exclude' reference undefined group ($exGroup) for filter $filter<br/>")
					}
				}
				if (excludes.size() > 0) {
					where = SqlUtil.appendToWhere(where, 'a.id NOT in (:excludes)')
					map.put('excludes', excludes*.id)
				}
				log.info "findAllAssetsWithFilter: excluding group(s) [${filter.exclude}] that has ${excludes.size()} assets"
			}
			
			// Assemble the SQL and attempt to execute it
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
						log.info "findAllAssetsWithFilter: DEVICE sql=$sql, map=$map"
						assets = AssetEntity.findAll(sql, map)
						break;
					
					case 'application':
						// Add additional WHERE clauses based on the following properties being present in the filter (Application domain specific)
						addWhereConditions( ['appVendor','sme','sme2','businessUnit','criticality'] )
					
						sql = "from Application a where a.moveBundle.id in (:bIds)" + (where.size()>0 ? " and $where" : '')
						log.info "findAllAssetsWithFilter: APPLICATION sql=$sql, map=$map"
						assets = Application.findAll(sql, map)
						break;
					
					case 'database':
						// Add additional WHERE clauses based on the following properties being present in the filter (Database domain specific)
						addWhereConditions( ['dbFormat','dbSize'] )
						sql = "from Database a where a.moveBundle.id in (:bIds)" + (where.size()>0 ? " and $where" : '')
						log.info "findAllAssetsWithFilter: DATABASE sql=$sql, map=$map"
						assets = Database.findAll(sql, map)
						break;

					case ~/files|file|storage/:
						// Add additional WHERE clauses based on the following properties being present in the filter (Database domain specific)
						addWhereConditions( ['fileFormat','fileSize', 'sizeUnit', 'LUN'] )
						sql = "from Files a where a.moveBundle.id in (:bIds)" + (where.size()>0 ? " and $where" : '')
						log.info "findAllAssetsWithFilter: FILES sql=$sql, map=$map"
						assets = Files.findAll(sql, map)
						break;
						
						
					default: 
						log.error "Invalid class '$queryOn' specified in filter ($filter)<br/>"
						throw new RuntimeException("Invalid class '$queryOn' specified in filter ($filter)")
						break;
				} 
			} catch (e) {
				def msg = "An unexpected error occurred while trying to locate assets for filter $filter" + e.toString()
				log.error "$msg\n"
				throw new RuntimeException("$msg<br/>${e.getMessage()}")
			}
		}
	
		return assets
	}	
	
	/** 
	 * This method generates action tasks that optionally linking assets as predecessors. It presently supports (location, room, rack, 
	 * cart and truck) groupings. It will create a task for each unique grouping and inject the list assets in the group into the associatedAssets
	 * property to later be used to create the predecessor dependencies.
	 *
	 * @param String action - options [rack, cart, truck]
	 * @param moveEvent - MoveEvent object
	 * @param Integer lastTaskNum
	 * @param Person whom - who is creating the tasks
	 * @param Integer recipeId
	 * @param Map taskSpec
	 * @param List? groups
	 * @param StringBuffer exceptions
	 * @return List<AssetComment> the list of tasks that were created
	 */
	def createAssetActionTasks(action, moveEvent, lastTaskNum, whom, recipeId, taskSpec, groups, exceptions ) {
		def taskList = []
		def loc 			// used for racks
		def msg
		
		// Get all the assets 			
		def assetsForAction = findAllAssetsWithFilter(moveEvent, taskSpec.filter, groups, exceptions)

		// If there were no assets we can bail out of this method
		if (assetsForAction.size() == 0) {
			return taskList
		}

		def tasksToCreate = []	// List of tasks to create

		// We will put all of the assets into the array. Below the unique method will be invoke to create a subset of entries
		// for which we'll create tasks from.
		tasksToCreate.addAll(0, assetsForAction)

		// The following two variables will be assigned closures in the switch statement below, which will be subsequently used
		// in the loop it go through the array of assetsForAction.
		def findAssets			// Finds all the assets in the assetsForAction list that match the action
		def validateForTask		// Used in iterator over the assets to determine if it should be associated to the current task
		
		def groupOn = action
		switch(action) {
			case 'set':
				if (! taskSpec.containsKey('setOn') || taskSpec.setOn.size() == 0 ) {
					throw new RuntimeException("Taskspec (${taskSpec.id}) is missing required 'setOn' attribute")
				}
				groupOn = taskSpec.setOn
				// TODO : add logic to convert custom labels to the appropriate custom# entry

				// Validate that the setOn attribute references a valid property
				if (! assetsForAction[0].properties.containsKey(groupOn)) {
					throw new RuntimeException("Taskspec (${taskSpec.id}) setOn attribute references an undefined property ($groupOn). <br/>Properties include [${assetsForAction[0].properties.keySet().join(', ')}]")
				}

				// Now fall into the case 'cart' below to finish up the setup

			case 'truck':
			case 'cart':
				// Get the unique list of tasks based on the setOn attribute that MUST reference a property on the asset
				tasksToCreate.unique { it[groupOn] }
				// Define the closure used to find the assets that is used below to gather the assets for each of the tasks.
				findAssets = { asset ->
					assetsForAction.findAll { it[groupOn] == asset[groupOn] }
				}
				validateForTask = { asset -> asset[groupOn] }
				break

			case 'rack':
			case 'room':
			case 'location':
				// Validate that there is the required location poperty and is source or target
				if (taskSpec.containsKey('disposition') ) {
					loc = taskSpec.disposition.toLowerCase()
					if (! ['source','target'].contains(loc) ) {
						msg = "$action action taskspec (${taskSpec.id}) has invalid value (${taskSpec.location}) for 'disposition' property" 
						log.error "$msg - moveEvent ${moveEvent}"
						throw new RuntimeException(msg)
					}
				} else {
					msg = "$action taskspec (${taskSpec.id}) requires 'disposition' property" 
					log.error "$msg - moveEvent ${moveEvent}"			
					throw new RuntimeException("$msg (options 'source', 'target')")			
				}
				
				switch(action) {
					case 'location':
						// Get the Distinct Racks from the list which includes the location and room as well
						tasksToCreate.unique { it["${loc}Location"] }
						findAssets = { asset -> 
							assetsForAction.findAll { it["${loc}Location"] == asset["${loc}Location"] }
						}
						break
					case 'room':
						// Get the Distinct Racks from the list which includes the location and room as well
						tasksToCreate.unique { it["${loc}Location"] + ':' + it["${loc}Room"] }
						findAssets = { asset -> 
							assetsForAction.findAll { 
								it["${loc}Location"] == asset["${loc}Location"] && 
								it["${loc}Room"] == asset["${loc}Room"] }
						}
						break
					case 'rack':
						// Get the Distinct Racks from the list which includes the location and room as well
						// log.info "%% assetsForAction before ${assetsForAction.size()}"
						tasksToCreate.unique { it["${loc}Location"] + ':' + it["${loc}Room"] + ':' + it["${loc}Rack"] }
						// log.info "%% assetsForAction after ${assetsForAction.size()}"
						findAssets = { asset -> 
							assetsForAction.findAll { 
								// log.info "Finding match to $asset in ${assetsForAction.size()}"
								it["${loc}Location"] == asset["${loc}Location"] && 
								it["${loc}Room"] == asset["${loc}Room"] &&
								it["${loc}Rack"] == asset["${loc}Rack"] }
						}
						break

				}
				validateForTask = { asset -> asset["${loc}${action.capitalize()}"]?.size() > 0 }
				break

			default:
				msg = "Unhandled action ($action) for taskspec (${taskSpec.id}) in createAssetActionTasks method" 
				log.error "$msg - moveEvent ${moveEvent}"
				throw new RuntimeException(msg)
		}

		log.info("Found ${tasksToCreate.size()} $action for createAssetActionTasks and assetsForAction=${assetsForAction.size()}")

		def template = new Engine().createTemplate(taskSpec.title)
		
		tasksToCreate.each() { ttc ->

			if ( validateForTask(ttc) ) {
				def map	= [:]	
				def task = new AssetComment(
					taskNumber: ++lastTaskNum,
					project: moveEvent.project, 
					moveEvent: moveEvent, 
					commentType: AssetCommentType.TASK,
					status: AssetCommentStatus.READY,
					createdBy: whom,
					displayOption: 'U',
					autoGenerated: true,
					recipe: recipeId,
					taskSpec: taskSpec.id )

				// Setup the map used by the template
				switch(action) {
					case 'set':
						map = [ set: ttc[groupOn] ]
						break

					case 'truck':
						map = [ truck: ttc.truck ]
						break

					case 'cart':
						map = [ truck:ttc.truck, cart:ttc.cart ]
						break

					// location/room/rack compound adding the details to the map
					case 'rack': 
						map.rack = ttc["${loc}Rack"] ?: ''
					case 'room': 
						map.room = ttc["${loc}Room"] ?: ''
					case 'location': 
						map.location = ttc["${loc}Location"] ?: ''
						break
				}

				task.comment = template.make(map).toString()
				log.info "Creating $action task - $task"

				// Handle the various settings from the taskSpec
				task.priority = taskSpec.containsKey('priority') ? taskSpec.priority : 3
				if (taskSpec.containsKey('duration')) task.duration = taskSpec.duration
				if (taskSpec.containsKey('team')) task.role = taskSpec.team
				if (taskSpec.containsKey('category')) task.category = taskSpec.category
				// TODO - Normalize this logic and sadd logic to update from Workflow if exists

				if (! ( task.validate() && task.save(flush:true) ) ) {
					log.error "createAssetActionTasks failed to create task ($task) on moveEvent $moveEvent"
					throw new RuntimeException("Error while trying to create task - error=${GormUtil.allErrorsString(task)}")
				}

				// Determine all of the assets for the action type/key and then inject them into the task, which will be used
				// by the dependency logic above to wire to predecessors.
				def assocAssets = []
				if (! ( taskSpec.containsKey('predecessor') && taskSpec.predecessor.containsKey('ignore') ) )  {
					// If we're not ignorning the predecessor
					assocAssets = findAssets(ttc)			
					if (assocAssets.size() == 0) {
						msg = "Unable to find expected assets for $action in TaskSpec(${taskSpec.id})"
						log.error "$msg on event $moveEvent"
						throw new RuntimeException(msg)
					}
					// log.info "Added ${assocAssets.size()} assets as predecessors to task $task"
					// assocAssets.each() { log.info "Asset $it" }
				}
				task.metaClass.setProperty('associatedAssets', assocAssets)
				taskList << task
			}
		}

		return taskList	
	}
	/** 
	 * This method is used to generate Roll-Call Tasks for a specified event which will create a sample task for each individual
	 * that is assigned to the Event which lists their name and their TEAM assignment(s). 
	 * @param moveEvent
	 * @param category
	 * @return List<AssetComment> the list of tasks that were created
	 */
	def createRollcallTasks( moveEvent, lastTaskNum, whom, recipeId, taskSpec ) {
		
		def taskList = []
		def staffList = MoveEventStaff.findAllByMoveEvent(moveEvent, [sort:'person'])
		log.info("createRollcallTasks: Found ${staffList.count()} MoveEventStaff records")

		def lastPerson = (staffList && staffList[0]) ? staffList[0].person : null
		def teams = []		
		
		// Create closure because we need to do this twice in the loop logic below
		def createActualTask = {
			def title = "Rollcall for $lastPerson (${teams.join(', ')})"
			log.info "createRollcallTasks: creating task $title"		
			def task = new AssetComment(
				taskNumber: ++lastTaskNum,
				comment: title,
				project: moveEvent.project, 
				moveEvent: moveEvent, 
				commentType: AssetCommentType.TASK,
				status: AssetCommentStatus.READY,
				createdBy: whom,
				displayOption: 'U',
				autoGenerated: true,
				hardAssigned: 1,
				assignedTo: lastPerson,
				recipe: recipeId,
				taskSpec: taskSpec.id )

			// Handle the various settings from the taskSpec
			if (taskSpec.containsKey('category')) task.category = taskSpec.category
			
			if (! ( task.validate() && task.save(flush:true) ) ) {
				log.error "createRollcallTasks: failed to create task for $lastPerson on moveEvent $moveEvent"
				throw new RuntimeException("Error while trying to create task. error=${GormUtil.allErrorsString(task)}")
			}

			taskList << task
		}

		// Iterate over the list of Staff and create tasks if they have Logins
		staffList.each() { staff ->
			// Create a task if we're at the end of the Staff(person) and the person has a UserLogin
			if (lastPerson && lastPerson.id != staff?.person?.id && UserLogin.findByPerson(staff.person)) {
				createActualTask()
				lastPerson = staff.person
				teams = []
			}
			teams << staff.role.id
		}
		if (teams.size() > 0 && UserLogin.findByPerson(lastPerson)) {
			// Catch the last staff record
			createActualTask()
		}
		
		log.info "createRollcallTasks: total tasks created: ${taskList.size()}"
		return taskList	
	}
	
}