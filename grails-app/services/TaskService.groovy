/**
 * The TaskService class contains methods useful for working with Task related domain (a.k.a. AssetComment). Eventually we should migrate away from using AssetComment 
 * to persist our task functionality.
 * 
 * @author John Martin
 *
 */

// Domains
import groovy.text.GStringTemplateEngine as Engine
import groovy.time.TimeCategory
import groovy.time.TimeDuration

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.apache.commons.lang.StringUtils as SU
import org.apache.commons.lang.math.NumberUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.springframework.beans.factory.InitializingBean
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.CommentNote
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.TaskDependency
import com.tdsops.common.lang.CollectionUtils as CU
import com.tdsops.common.lang.GStringEval
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.AssetDependencyType
import com.tdsops.tm.enums.domain.ContextType
import com.tdsops.tm.enums.domain.RoleTypeGroup
import com.tdsops.tm.enums.domain.TimeConstraintType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil

class TaskService implements InitializingBean {

	static transactional = true

	def cookbookService
	def dataSource
	def jdbcTemplate
	def namedParameterJdbcTemplate
	def partyRelationshipService
	def personService
	def securityService
	def quartzScheduler
	def workflowService
	def grailsApplication
	ProgressService progressService
	ExecutorService executors

	static final List runbookCategories = [AssetCommentCategory.MOVEDAY, AssetCommentCategory.SHUTDOWN, AssetCommentCategory.PHYSICAL, AssetCommentCategory.STARTUP]
	static final List categoryList = AssetCommentCategory.getList()
	static final List statusList = AssetCommentStatus.getList()

	// The list of RoleTypes for Staff that will be initialized in the afterPropertiesSet method
	static List staffingRoles = []
	
	// This list will contain all of the common asset properties that filtering can be applied to
	static List commonFilterProperties 

	// Color scheme for status key:[font, background]
	static final Map taskStatusColorMap = [
		(AssetCommentStatus.HOLD):['black', '#FFFF33'],
		(AssetCommentStatus.PLANNED):['black', 'white'],
		(AssetCommentStatus.READY):['white', 'green'],
		(AssetCommentStatus.PENDING):['black', 'white'],
		(AssetCommentStatus.STARTED):['white', 'darkturquoise'],
		(AssetCommentStatus.DONE):['white', '#24488A'],
		(AssetCommentStatus.TERMINATED):['white', 'black'],	
		'AUTO_TASK':['#848484','#848484'],	// [font, edge]
		'ERROR': ['red', 'white'],		// Use if the status doesn't match
	]

	public TaskService() {
		this.executors = Executors.newFixedThreadPool(20)
	}
	
	/**
	 * This is a post initialization method to allow late configuration settings to occur
	 */
	public void afterPropertiesSet() throws Exception {

		// NOTE - This method is only called on startup therefore if code is modified then you will need to restart Grails to see changes

		// Initialize some class level variables used repeatedly by the application
		// TODO : Need to get staffingRoles out of static as the list can change during runtime
		staffingRoles = partyRelationshipService.getStaffingRoles()*.id

		commonFilterProperties = ['assetName','assetTag','assetType', 'priority', 'planStatus', 'department', 'costCenter', 'environment']
		(1..64).each() { commonFilterProperties << "custom$it".toString() }	// Add custom1..custom64
		log.debug "commonFilterProperties include $commonFilterProperties"
	}

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
			a.asset_entity_id AS assetId,
			a.asset_type AS assetType,
			p.first_name AS firstName, p.last_name AS lastName,
			t.hard_assigned AS hardAssigned,
			t.duration AS duration,
			t.duration_scale AS durationScale,
			t.category""")

		// Add in the Sort Scoring Algorithm into the SQL if we're going to return a list
		if ( ! countOnly) {
			sqlParams << [now:now, minAgo:minAgo]
			
			/*
				
				NOTE THAT THIS LOGIC IS DUPLICATED IN THE AssetComment Domain for score formula SO IT NEEDS TO BE MAINTAINED TOGETHER

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
					- AUTO tasks have lessor priority than normal PENDING tasks
				
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
				IF(t.role='AUTO', -100, 0) +
				(6 - t.priority) * 5) AS score """)			
		}
		
		// Add Successor Count
		sql.append(', (SELECT count(*) FROM task_dependency td WHERE predecessor_id=t.asset_comment_id) AS successors ')
		// Add Predecessor Count
		sql.append(', (SELECT count(*) FROM task_dependency td WHERE asset_comment_id=t.asset_comment_id) AS predecessors ')
		
		// The WHERE clause is going to find tasks that are assigned to the user (hard or soft) OR tasks that are assigned to the role(s) that
		// the user has unless the task is hard assigned to someone else in one of the groups.
		sql.append("""FROM asset_comment t 
			LEFT OUTER JOIN asset_entity a ON a.asset_entity_id = t.asset_entity_id 
			LEFT OUTER JOIN person p ON p.person_id = t.assigned_to_id 
			WHERE t.project_id=:projectId AND t.comment_type=:type AND t.is_published = true """)

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
			if ( (limitHistory instanceof Integer) && limitHistory >= 0) {
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
		
		def assignedTasks = allTasks.findAll { task ->
			if (task.taskNumber==374) { log.info "getUserTasks: minAgoFormat:${minAgoFormat} [${task.statusUpdated?.format(format)}]"}
			(task.status == AssetCommentStatus.READY && task.assignedTo == person.id )|| ( task.status == AssetCommentStatus.STARTED && task.assignedTo == person.id ) ||
			(task.status == AssetCommentStatus.DONE && task.assignedTo == person.id && task.statusUpdated?.format(format) >= minAgoFormat )
		}
		if (countOnly) {
			return ['all':allTasks.size(), 'todo':todoTasks.size()]
		} else {
			return ['all':allTasks, 'todo':todoTasks, 'user':assignedTasks]
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

		// Override the whom if this is an automated task being completed
		if (task.role == AssetComment.AUTOMATIC_ROLE && status == AssetCommentStatus.DONE) {
			whom = getAutomaticPerson()
		}

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
				if(previousStatus!= AssetCommentStatus.HOLD){
					task.actStart = null	
				}else if(!task.actStart){
					task.actStart = now
				}		
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
					triggerUpdateTaskSuccessors(task.id, status, whom, isPM)
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
		def rolesForStaff = partyRelationshipService.getStaffingRoles(false)
		return rolesForStaff
	}

	/**
	 * This method is used to get the team rolls that can be assigned to tasks. This is similar to that of Roles for Staff but also includes
	 * Automated.
	 */
	def getTeamRolesForTasks() {
		def rolesForStaff = partyRelationshipService.getStaffingRoles()
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
	* Used to calculate the dial indicator speed that reflects how well the move is going for a given set of datetimes
	*/
	def calcStepDialIndicator ( planStartTime, planCompTime, actualStartTime, actFinish, tasksCount, tasksCompleted) {
		
		// TODO - calcStepDialIndicator() - need to further refine this method and test
		return 0

		// timeAsOf = timeAsOf.getTime() / 1000
		def timeAsOf = TimeUtil.nowGMT().getTime() / 1000  // Remove the millisec

		// def planCompletionTime = (stepSnapshot.moveBundleStep.planCompletionTime.getTime() / 1000 ) + 59  	// 59s added to planCompletion to consider the minuits instead of seconds
		planCompTime = (planCompTime.getTime() / 1000 ) + 59  	// 59s added to planCompletion to consider the minuits instead of seconds
		planStartTime = planStartTime.getTime()  / 1000 + 59

		// log.info "timeAsOf is ${timeAsOf.getClass()}, planCompTime is ${planCompTime.getClass()}, planStartTime is ${planStartTime.getClass()} $planStartTime}"

		def remainingStepTime = timeAsOf > planCompTime ? 0 : planCompTime - timeAsOf 
		//def planTaskPace = stepSnapshot.getPlanTaskPace()
		def planDuration = planCompTime - planStartTime
		def planTaskPace = planDuration / (tasksCount == 0 ? 1 : tasksCount)

		def tasksRemaining = tasksCount - tasksCompleted

		def remainingEffort =  tasksRemaining * planTaskPace

		def projectedMinOver = 0
		if( actualStartTime || tasksCompleted > 0){
			projectedMinOver  = remainingEffort - remainingStepTime
		} else {
			projectedMinOver  =  timeAsOf + planDuration
		}
		def adjust 
		
		if ( remainingEffort && projectedMinOver > 0) {
			adjust =  -50 * (1-(remainingStepTime / remainingEffort))
		} else {
			adjust =  50 * (1-(remainingEffort / (planCompTime - timeAsOf ) ) )
		}
		def result = (50 + adjust).intValue()

		// to show the dial inbetween 0 to 100
		result = result > 100 ? 100 : result
		result = result < 0 ? 0 : result
				
log.info "tasksCount=$tasksCount, timeAsOf=$timeAsOf, planStartTime=$planStartTime, planCompTime=$planCompTime, tasksCompleted=$tasksCompleted, remainingStepTime=$remainingStepTime, planDuration=$planDuration, planTaskPace=$planTaskPace, tasksRemaining=$tasksRemaining, " + 
	"remainingEffort=$remainingEffort, projectedMinOver=$projectedMinOver, adjust=$adjust, result=$result"		

		return result  
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
				nameRole:"${it.role.description.split(':')[1]?.trim()}: ${it.staff.toString()}",
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
	 * Used to retrieve the Person object that represent the person that completes automated tasks
	 * @return Person
	 */
	def getAutomaticPerson() {
		def auto = Person.findByLastNameAndFirstName('Task', 'Automated')
		if (! auto) {
			log.error 'Unable to find Automated Task Person as expected'
		} 
		return auto
	}

	/**
	 * Used to get a list of the neighboring tasks (dependencies) adjacent to a particular task. 
	 * It will move out N number of blocks building the list of dependencies. The outer nodes will have one of two
	 * properties injected into them which represents the adjacent depencies which are just outside the hood. The properties 
	 * will be (predecessorDepCount | successorDepCount) based on which side of the relationship they are. These DepCounts will
	 * be presented in the graph as badges indicating the quantity of the adjacent tasks 
	 *  
	 * @param Integer taskId - the task id number to start with
	 * @param Integer blocks - the number blocks out that the list should retrieve (default 3)
	 * @return List<TaskDependency> - the list of tasks dependencies surrounding the task
	 */
	def getNeighborhood( taskId, blocksLeft=2, blocksRight=2 ) {
		def list = []
		def findProp	// Is set to the TaskDependency property name used to find the current nodes (e.g. 'predecessor' when looking for successors)
		def nextProp	// The opposite property name to findProp
		def findCol		// The db column name that is used to find the adjacent task dep count on the outer edges of the neighborhood
		//def nextCol		// The DB column name that represents the findCol property in the domain object
		def depCountName	// The name of the property that will be injected into the edge nodes with the count (predecessorDepCount | successorDepCount)

		// A recursive helper method that will traverse each of the neighbors out the # of blocks passed in
		def neighbors 
		neighbors = { tId, depth ->
			// log.info "In the hood $depth for $tId"
			if (depth > 0) {
				def td = TaskDependency.findAll("from TaskDependency td where td.${findProp}.id=?", [tId])
				// log.info "Found ${td.size()} going to $predOrSucc"
				if (td.size() > 0) {
					if (depth > 0) {
						td.each { t ->
							// log.info "Neighbor ${t.assetComment.id} : ${t.predecessor}" 
							neighbors( t[nextProp].id, (depth - 1) )
						}
					}

					//
					// Deal with the edge tasks by injecting the outside task dependency counts
					//
					if (depth == 1 ){
						// On the outer nodes, we want to get the quantity of dependencies on the other side of the tracks for
						// each of them correspondingly. It will invoke a query to get a list so as to limit the number of queries
						// to one per level vs one per node. Therefore it needs to iterate through the results to match up with the 
						// corresponding TaskDependency. It will use the meta.setProperty to inject the count appropriately.
						def ids = td[nextProp]*.id
						// isPred = nextProp == 'predecessor'
						
//						def sql = "SELECT $nextProp as id, count(*) as cnt FROM task_dependency WHERE $nextProp in (" .
//							GormUtil.asCommaDelimitedString(ids) . ') group by id'
						def sql = "SELECT $findCol as id, count(*) as cnt FROM task_dependency WHERE $findCol in (" +
							GormUtil.asCommaDelimitedString(ids) + ") group by id"
						log.info "SQL = $sql, ids=$ids"
						def outerDeps = jdbcTemplate.queryForList(sql)
						log.info "getNeighborhood() : found ${outerDeps.size()} tasks on the other side of the tracks of $ids"
						td.each() { t ->
							// Try to match up the outer dep count to the task dependency node
							def outerDep = outerDeps?.find() { od -> od.id == t[nextProp].id }
							def outerDepCount = outerDep ? outerDep.cnt : 0
							t.metaClass.setProperty(depCountName, outerDepCount)	
							log.info "getNeighborhood(): Found $outerDepCount outer depend for task # ${t[findProp].taskNumber} (id:${t[findProp].id})"
						}
					}
					
					if (depth==1) {
						td.each() { t ->
							log.info "getNeighborhood: dependency $t - $depCountName = ${t[depCountName]} outside dep"
						}				
					}

					// tnp.metaClass.setProperty('chainPeerTask', assetsLatestTask[tnp.assetEntity.id])
					list.addAll(td)

				}
			}
		}


		taskId = taskId.toLong()

		// Get the successors
		findProp = 'predecessor'
		nextProp = 'assetComment'
		findCol = 'predecessor_id'
		depCountName = 'successorDepCount'
		neighbors(taskId, blocksRight) 

		// Get the predecessor
		findProp = 'assetComment'
		nextProp = 'predecessor'
		findCol = 'asset_comment_id'
		depCountName = 'predecessorDepCount'
		neighbors(taskId, blocksLeft) 

		// Reduce the list to just the distinct/unique dependency since the above way to find dependencies can have overlap
		list.unique { a, b -> a.id <=> b.id }

		//log.info "getNeighborhood: found ${list.size()} tasks in the hood"
		// list.each() { log.info "dep match: ${it.assetComment.id} : ${it.predecessor.id}"}
		return list
	}






	// ===================================================================================================================================================================================
	// RUN BOOK
	// ===================================================================================================================================================================================

	/**
	 * Used to create a task batch
	 */
	TaskBatch createTaskBatch(Person whom, ContextType contextType, Integer contextId, RecipeVersion recipe) {
		TaskBatch tb = new TaskBatch()
		tb.createdBy = whom
		tb.recipeVersionUsed = recipe
		tb.contextType = contextType
		tb.contextId = contextId
		tb.status = "Pending"
		tb.save(failOnError: true)
	}

	/**
	 * Used to initiate an async task creation process 
	 * This is the service method called by the controller to initiate task generation
	 * @param recipeVersionId - the id of the recipe version
	 * @param publish - used to indicate if the tasks should be published at the time that they are generate, default=false
	 */
	Map initiateCreateTasksWithRecipe(UserLogin user, recipeVersionId, contextId, publish=false) {
		if (!RolePermissions.hasPermission('GenerateTasks')) {
			throw new UnauthorizedException('User doesn\'t have a GenerateTasks permission')
		}
		
		if (publish && !RolePermissions.hasPermission('PublishTasks')) {
			throw new UnauthorizedException('User doesn\'t have a PublishTasks permission')
		}
		
		if (recipeVersionId == null || !recipeVersionId.isInteger()) {
			throw new IllegalArgumentException('Invalid recipe id', e)
		}
		
		def recipe = RecipeVersion.get(recipeVersionId.toInteger())
		
		if (recipe == null) {
			new EmptyResultException('Recipe doesn\'t exists');
		}

		def contextType = recipe.recipe.asContextType()
		publish = publish == null ? false : publish.toBoolean()
		
		def assets = this.getAssocAssets(contextType)

		if (assets.size()) {
			final TaskBatch tb = this.createTaskBatch(user.person, contextType, contextId, recipe);
			def key = "TaskBatch-${tb.id}"
			this.progressService.create(key, 'Pending')

			this.executors.submit(new Runnable() {
				void run() {
					TaskBatch.withTransaction({ status ->
						TaskService.this.generateTasks(tb)
					});
				}
			});
		
			return ['jobId' : key, 'taskBatch' : tb]
		} else {
			throw new IllegalArgumentException('No assets were found')
		}
	}

	/**
	 * Used to find assets associated with a given context type and id
	 * TODO: Esteban - fill in doc and implementation, contextType (enum)
	 * 
	 */
	Object getContextObject(contextType, Integer contextId) {
		def asset
		switch (contextType) {
			default:
			break;
			// For each type lookup the asset
		}
		return asset
	}

	/**
	 * Used to find assets associated with a given context type and id
	 * TODO: Esteban - fill in doc and implementation, contextType (enum)
	 * 
	 */
	List getAssocAssets(contextType, Integer contextId) {
		def asset = getContextObject( contextType, contextId)

		return getAssocAsset( asset )
	}

	/**
	 * Used to find assets associated with a given context
	 * TODO: Esteban - fill in doc and implementation
	 * 
	 */
	List getAssocAssets(ContextType context) {
		def assets = []

		// Load Asset list based on the context
		switch (context) {
			case ContextType.E:
				// See the code below in generateRunbook:
				// get bundle ids
				// get assets in the bundle, could just fall into MoveBundle case
			case ContextType.B:
				// Similar to MoveEvent
				// get assets in bundle
				break
			case ContextType.A:
				// Get assets that it depends on and supports from AssetDependency along with the application
				break
			default:
				// log error for unhandled case with the context.class
				break
		}

		return assets
	}

	/**
	 * This is going to replace the generateRunbook method below
	 * TODO: John to implement
	 */
	void generateTasks(TaskBatch taskBatch) {
		// 
		// def context = getContextObject(taskBatch.contextType, taskBatch.contextId)
		// def assets = getAssocAssets(context)
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
		def taskList = [:]				// an Map list (key AssetComment.id) that will contain list of all tasks that are inserted as they are created
		def taskSpecTasks = [:]			// A map list where the key is the task spec id and value is an array of the tasks created by the task spec
		def collectionTaskSpecIds = []	// An array containing task spec ids that are determined to be collections (sets, trucks, cart, rack, gateway, milestones, etc) 
		def latestMilestone = null		// Will reference the most recent milestone task
		def assetsLatestTask = [:]		// This map array will contain reference of the assets' last assigned task
		def taskSpecList = [:]			// Used to track the ID #s of all of the taskSpecs in the recipe and holds last task created for some special cases
		def groups = [:]				// Used to hold groups of assets as defined in the groups section of the recipe that can then be reference in taskSpec filters
		def terminalTasks = []			// Maintains the list of general tasks indicated that are terminal so that milestones don't connect to them as successors
		def missedDepList = [:].withDefault {[]}	
										// Used to track missing dependencies for an asset. The key will be the (asset.id_asset.category). The map that will 
										// by default contain ArrayList that will be populated with a map including the task and several related objects.
		def deferredList = [:].withDefault {[]}	
										// Used to track assets that have deferred tasks. The array for each asset will list the task ids that were deferred. When a 
										// successor task is added, the array is updated to replace the previous task id with the new one.
										// NOT Sure that we need this variable

		
		def wfList = null				// Will be populated with a List of workflows for each bundle
		
		def isAsset = false
		def isGeneral = false
		def isGroupingSpec = false 		// Flag used to determine if the current taskSpec is a grouping type (e.g. Milestone, Gateway, Set, etc)
		def isAction = false
		def isRequired=true			// Used to hold the taskSpec.predecessor.required param or default to true
		def isInversed=false 		// Used to hold the taskSpec.predecessor.inverse or default to false
		def failure = ''			// Will hold the message if there is an exception
		def lastTaskSpec = null		// Holds the last task spec 
		def deferPred = null 		// Gets populated with the taskSpec.predecessor.defer code if defined. It will either be a string if defined or null
		def deferSucc = null 		// Gets populated with the taskSpec.successor.defer code if defined. It will either be a string if defined or null
		def gatherPred = null		// Gets populated with the taskSpec.predecessor.gather code if defined (it becomes an array) if not null
		def gatherSucc = null		// Gets populated with the taskSpec.successor.gather setting if defined (it becomes an array) if not null
		def settings				// This will get populated with the properties from the TaskSpec for each iteration
		// def waitFor = '' 			// When the predecessor.waitFor is defined then wiring predecessors will wait until a subsequent taskspec with a successor.resumeFor attribute of the same value, 
		// def resumeFor = ''			// Used in conjunction with waitFor

		def newTask
		def msg

		def depCount = 0
		def specCount = 0
		
		/**
		 * A helper closure used by generateRunbook to link a task to its predecessors by asset or milestone
		 * @param AssetComment (aka Task)
		 */
		def linkTaskToMilestone = { taskToLink ->
			if (latestMilestone) {
				log.info "linkTaskToMilestone - $taskToLink"
				log.debug "Calling createTaskDependency from linkTaskToMile"
				depCount += createTaskDependency( latestMilestone, taskToLink, taskList, assetsLatestTask, settings, out)
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
			if ( taskToLink.assetEntity ) {
				if (assetsLatestTask.containsKey(taskToLink.assetEntity.id) ) {
					if ( assetsLatestTask[taskToLink.assetEntity.id].taskNumber != taskToLink.taskNumber ) {
						log.info "linkTaskToLastAssetOrMilestone: creating dependency for task $taskToLink"
						depCount += createTaskDependency( assetsLatestTask[taskToLink.assetEntity.id], taskToLink, taskList, assetsLatestTask, settings, out)
						out.append("Created dependency between ${assetsLatestTask[taskToLink.assetEntity.id]} and $taskToLink<br/>")
					} else {
						exceptions.append("Unexpected binding of task dependencies where a task references itself, task(${taskToLink}), TaskSpec (${taskToLink.taskSpec}) <br/>")
					}
				} else {
					// Record this task as the asset's most recent task
					assetsLatestTask[taskToLink.assetEntity.id] = taskToLink

					linkTaskToMilestone( taskToLink )
				}
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
						assignToAssetsLatestTask(it.predecessor.assetEntity, currTask, assetsLatestTask)
						// assetsLatestTask[it.predecessor.assetEntity.id] = currTask
						log.info "Bumped task $it to task $currTask"
					}									
				}
			}
		}
		
		// The following vars are used by doAssigmnet to retain previously looked up persons
		def resolvedWhoms = [:]
		def whomLastTaskSpec

		// Preload all the staff for the project
		def projectStaff = partyRelationshipService.getAvailableProjectStaffPersons( project )

		log.info '*************************************************************************************'
		log.info "**************** generateRunbook() by $whom for MoveEvent $moveEvent ****************"
		log.info '*************************************************************************************'

		def maxPreviousEstFinish = null		// Holds the max Est Finish from the previous taskSpec
		def workflow
		def recipe 			// The recipe Map
		def recipeId 		// The id of the recipe
		def recipeTasks 	// The list of Tasks within the recipe
		def taskSpecIdList 	// The list of the task spec id #s
				
		def noSuccessorsSql = "select t.asset_comment_id as id \
			from asset_comment t \
			left outer join task_dependency d ON d.predecessor_id=t.asset_comment_id \
			where t.move_event_id=${moveEvent.id} AND d.asset_comment_id is null \
			AND t.auto_generated=true \
			AND t.category IN (${categories}) "
		// log.debug("noSuccessorsSql: $noSuccessorsSql")

		try {
			// Validate the syntax of the recipe before going any further
			def recipeErrors = cookbookService.validateSyntax(moveEvent.runbookRecipe)
			if (recipeErrors) {
				def errMsg = 'The recipe has the following error(s):<ul>'
				recipeErrors.each { e -> errMsg += "<li>${e.reason}: ${e.detail}</li>"}
				errMsg += '</ul>'
				log.debug "Recipe had syntax errors : $errMsg"
				throw new RuntimeException(errMsg)
			}
			recipe = cookbookService.parseRecipeSyntax(moveEvent.runbookRecipe)
			recipeId = recipe?.id
			recipeTasks = recipe?.tasks

			if (! recipeTasks || recipeTasks.size() == 0) {
				throw new RuntimeException('There appears to be no runbook recipe or there is an error in its format')
			}

			// Load the taskSpecList array used for validation, etc
			taskSpecIdList = recipeTasks*.id
			log.info "taskSpecIdList=$taskSpecIdList"

			// Load the groups with the corresponding assets from the recipe group/filters
			groups = fetchGroups( recipe, moveEvent, exceptions )

			out.append('Assets in Groups:<ul>')
			groups.each { n, l ->
				if (l.size() == 0)
					exceptions.append("Found zero (0) assets for group ${n}<br/>")

				out.append("<li><b>$n</b> (contains ${l.size()} assets): ${l*.assetName}")
			}
			out.append('</ul>')

			log.debug "\n\n     ******* BEGIN TASK GENERATION *******\n\n"

			// Now iterate over each of the task specs
			recipeTasks.each { taskSpec ->
				def tasksNeedingPredecessors = []	// Used for wiring up predecessors in 2nd half of method
				isAsset = false
				isGeneral = false
				isGroupingSpec = false				
				isAction = false
				isRequired = true
				isInversed = false
				newTask = null
				deferPred = null
				deferSucc = null
				gatherPred = null
				gatherSucc = null
				def depMode = ''			// Holds [s|r] for assetTask.predecessor.mode to indicate s)upports or r)equires
				def mapMode = ''
				def hasPredecessor = false
				def hasPredGroup = false
				def hasPredTaskSpec = false
				def predecessor = null
				def successor = null
				def ignorePred = false
				def findParent = false		// Flag if the taskspec requires linking predecessor to a parent predecessor for an asset related task
				def assetsForTask = []		// This will contain the list of assets to which tasks are created for asset type taskspec
				def predTaskSpecs = null 		// Will hold an array of predecessor.taskSpec references if defined
				def predTasksByAssetId = [:].withDefault {[]}	// This will hold all of the tasks by asset.id when we're processing dependencies along with taskSpec(s)

				// because it could cause adverse dependency linkings.
				log.info "##### Processing taskSpec $taskSpec"

				// ------------------------------------------------------------------------------------
				// Parse out some of the commonly used properties from the TaskSpec
				// ------------------------------------------------------------------------------------
				specCount++
				
				// Save the taskSpec in a map for later reference
				taskSpecList[taskSpec.id] = taskSpec

				// Setup any common variables used in successor section
				if ( taskSpec.containsKey('successor') ) {
					successor = taskSpec.successor 
					if ( successor.containsKey('defer') ) {
						deferSucc = successor.defer					}
					if ( successor.containsKey('gather') ) {
						gatherSucc = CU.asList(successor.gather)
					}
				}

				// Determine if the taskSpec has the predecessor.required property and if it is of the Boolean type
				if ( taskSpec.containsKey('predecessor') ) {
					predecessor = taskSpec.predecessor
					hasPredecessor = true

					if ( predecessor.containsKey('required') ) {
						if ( ! (predecessor.required instanceof Boolean) ) {
							msg = "TaskSpec (${taskSpec.id}) property 'predecessor.required' has invalid value (${predecessor.required}) "
							log.error("$msg for Event $moveEvent")
							throw new RuntimeException(msg)
						} else {
							isRequired = predecessor.required
						}
					}

					if ( predecessor.containsKey('ignore') ) {
						if (! (predecessor.ignore instanceof Boolean) ) {
							msg = "TaskSpec (${taskSpec.id}) property 'predecessor.ignore' has invalid value (${predecessor.ignore}), options true|false "
							log.error("$msg for Event $moveEvent")
							throw new RuntimeException(msg)
						} else {
							ignorePred = predecessor.ignore
						}
					}
					if ( predecessor.containsKey('defer') ) {
						deferPred = predecessor.defer
					}
					if ( predecessor.containsKey('gather') ) {
						gatherPred = CU.asList(predecessor.gather)
					}
					if ( predecessor.containsKey('mode') ) {
						depMode = predecessor.mode[0].toLowerCase()
					}

					if ( predecessor.containsKey('inverse') ) {
						if ( ! (predecessor.inverse instanceof Boolean) ) {
							msg = "TaskSpec (${taskSpec.id}) property 'predecessor.inverse' has invalid value (${predecessor.inverse}), options: true | false "
							log.error("$msg for Event $moveEvent")
							throw new RuntimeException(msg)
						} else {
							isInversed = predecessor.inverse
						}
					}

					hasPredGroup = predecessor.containsKey('group')
					findParent = predecessor.containsKey('parent')
					hasPredTaskSpec = predecessor.containsKey('taskSpec')

					if (hasPredTaskSpec) {
						predTaskSpecs = CU.asList(predecessor.taskSpec)
					}

					log.debug "hasPredTaskSpec=$hasPredTaskSpec, predTaskSpecs=$predTaskSpecs"

					// Make sure we have one of the these methods to find predecessors
					if (! (depMode || hasPredGroup || hasPredTaskSpec || ignorePred || findParent || deferPred || gatherPred ) ) {
						msg = "Task Spec (${taskSpec.id}) contains 'predecessor' section that requires one of the properties [mode | group | ignore | parent | taskSpec | defer | gather]"
						log.info(msg)
						throw new RuntimeException(msg)							
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
				
				// Get the Workflow code if there is one specified in the task spec and then lookup the code for the workflow details
				def taskWorkflowCode = taskSpec.containsKey('workflow') ? taskSpec.workflow : null
				workflow = taskWorkflowCode ? getWorkflowStep(taskWorkflowCode) : null		
				
				// Validate that the taskSpec has the proper type
				def stepType 

				// A task spec can have 'action' or 'type' otherwise defaults to 'asset'
				if (taskSpec.containsKey('action')) {
					stepType = 'action'
				} else {
					stepType = taskSpec.containsKey('type') ? taskSpec.type : 'asset'
				}
				
				// Collection of the task settings passed around to functions more conveniently
				settings = [type:stepType, isRequired:isRequired, isInversed:isInversed, deferPred:deferPred, deferSucc:deferSucc, gatherPred:gatherPred, gatherSucc:gatherSucc]
				log.debug "##### settings: $settings"

				// ------------------------------------------------------------------------------------
				// Create the Task(s) - the tasks are created within the following case statement
				// ------------------------------------------------------------------------------------
				out.append("<br/>====== Processing Task Spec ${taskSpec.id}-${taskSpec.description} $settings<br/>")
				
				settings.taskSpec = taskSpec

				// Track what tasks were created by the taskSpec
				taskSpecTasks[taskSpec.id] = []

				switch (stepType) {
					
					case 'milestone':
						// -------------------------
						// Handle Milestone tasks
						// -------------------------
						out.append("Creating milestone ${taskSpec.title}<br/>")

						isGroupingSpec = true
						
						newTask = createTaskFromSpec(recipeId, whom, taskList, ++lastTaskNum, moveEvent, taskSpec, projectStaff, exceptions, workflow)
						def prevMilestone = latestMilestone
						latestMilestone = newTask 

						// Indicate that the task is funnelling so that predecessor tasks' assets are moved through the task 
						newTask.metaClass.setProperty('tmpIsFunnellingTask', true)		

						// Identify that this taskSpec is a collection type
						collectionTaskSpecIds << taskSpec.id
						
						log.info "milestone isRequired = $isRequired"
						
						// Find all tasks that don't have have a successor that are not terminal (excluding the current milestone task) 
						// As well, find asset associated tasks that are sitting at the previous milestone and pull it forward if so
						def tasksNoSuccessors = []
						taskList.each() { id, t ->
							// Check to see if the task doesn't have a successor, that it isn't terminal and that it isn't the current Milestone task
							if ( ! t.metaClass.hasProperty(t, 'hasSuccessorTaskFlag') && 
								! terminalTasks.contains(t.id) &&
								t.id != newTask.id ) {
									tasksNoSuccessors << t
									taskSpecTasks[taskSpec.id] << t
							} else if ( t.assetEntity && assetsLatestTask.containsKey(t.assetEntity.id) ) {
							 		// If this is a task with an asset then we'll shuffle the task to the latest Milestone
							 		assetsLatestTask[t.assetEntity.id] = latestMilestone
							}
						}

						//log.info "SQL for noSuccessorsSql: $noSuccessorsSqlFinal"
						log.info "generateRunbook: Found ${tasksNoSuccessors.count()} tasks with no successors for milestone ${taskSpec.id}, $moveEvent"
					
						if (tasksNoSuccessors.size()==0 && taskList.size() > 1 ) {
							if (prevMilestone) {
								log.debug "Calling createTaskDependency from milestone 1"
								depCount += createTaskDependency( prevMilestone, newTask, taskList, assetsLatestTask, settings, out)
							} else {
								out.append("Found no successors for a milestone, which is unexpected but not necessarily wrong - Task $newTask<br/>")
							}
						} 

						tasksNoSuccessors.each() { p ->
							// TODO - switch to get task from task list instead of read call
							// def predecessorTask = AssetComment.read(p.id)
							def predecessorTask = taskList[p.id]
							log.debug "Calling createTaskDependency from milestone 2"
							depCount += createTaskDependency( predecessorTask, newTask, taskList, assetsLatestTask, settings, out )

							// Bump along any predecessor assets to this task if the predecessor was a collection taskSpec
							// TODO - I don't think that this is necessary
							// bumpAssetLastTask(p, newTask)


						}	

						// We are done with predecessors, etc
						break
						
					case 'gateway':
						// Handle GATEWAY tasks
						// We create a simple task and then wire-in the dependencies to tasks generated by taskSpec referenced in predecessor property
						isGroupingSpec = true
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
						
						newTask = createTaskFromSpec(recipeId, whom, taskList, ++lastTaskNum, moveEvent, taskSpec, projectStaff, exceptions, null)

						// Indicate that the task is funnelling so that predecessor tasks' assets are moved through the task 
						newTask.metaClass.setProperty('tmpIsFunnellingTask', true)		

						tasksNeedingPredecessors << newTask
						mapMode = 'DIRECT_MODE'

						msg = "Created GW task $newTask from taskSpec ${taskSpec.id}"
						log.info(msg)
						out.append("$msg<br/>")

						// Identify that this taskSpec is a collection type
						collectionTaskSpecIds << taskSpec.id

						// Track what tasks were created by the taskSpec
						taskSpecTasks[taskSpec.id] = [newTask]
						
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
									taskSpecTasks[taskSpec.id] = rcTasks
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

								isGroupingSpec = true

								// Track that this taskSpec is a collection type
								collectionTaskSpecIds << taskSpec.id

								// Track what tasks were created by the taskSpec
								taskSpecTasks[taskSpec.id] = []

								def actionTasks = createAssetActionTasks(action, moveEvent, lastTaskNum, whom, projectStaff,recipeId, taskSpec, groups, workflow, exceptions)
								if (actionTasks.size() > 0) {
									// Throw the new task(s) into the collective taskList using the id as the key
									actionTasks.each() { t ->
										// Set flag that this is a funnelling task so that the predecessor logic will move all assets through it
										t.metaClass.setProperty('tmpIsFunnellingTask', true)		
										taskList[t.id] = t
										taskSpecTasks[taskSpec.id] << t
									}
									taskSpecTasks[taskSpec.id] = actionTasks
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
					
						assetsForTask = findAllAssetsWithFilter(moveEvent, taskSpec, groups, exceptions)
						log.info "Found ${assetsForTask?.size()} assets for taskSpec ${taskSpec.id}-${taskSpec.description}"
						if ( !assetsForTask || assetsForTask.size()==0) 
							return // aka continue
					
						//
						// Create a task for each asset based on the filtering of the taskSpec
						//
						assetsForTask?.each() { asset ->
							workflow = getWorkflowStep(taskWorkflowCode, asset.moveBundle.id)
							
							newTask = createTaskFromSpec(recipeId, whom, taskList, ++lastTaskNum, moveEvent, taskSpec, projectStaff, exceptions, workflow, asset)
							
							tasksNeedingPredecessors << newTask
							taskSpecTasks[taskSpec.id] << newTask

							out.append("Created asset based task $newTask<br/>")
						} 
						
						// If we have predecessor.mode (s|r) then we'll doing linkage via Asset Dependency otherwise we'll link asset to asset directly
						mapMode = depMode ? 'ASSET_DEP_MODE' : 'DIRECT_MODE'
						
						break
						
					case 'general':
						// ---------------------------
						// Create GENERAL type Task(s)
						// ---------------------------
						isGeneral=true
						def isChain=true
						if (taskSpec.containsKey('chain')) {
							if (taskSpec.chain instanceof Boolean) {
								isChain = taskSpec.chain
							} else {
								throw new RuntimeException("Task Spec (${taskSpec.id}) 'chain' property has invalid value. Acceptible values (true|false)")
							}
						}
						settings[isRequired] = isChain
						
						def genTitles = CU.asList(taskSpec.title)
						def genLastTask = null
						
						genTitles.each() { gt -> 
							// Replace the potential title:[array] with just the current title
							taskSpec.title = gt
							newTask = createTaskFromSpec(recipeId, whom, taskList, ++lastTaskNum, moveEvent, taskSpec, projectStaff, exceptions, null, null)

							taskSpecTasks[taskSpec.id] << newTask

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
									log.debug "Calling createTaskDependency from general"
									depCount += createTaskDependency(genLastTask, newTask, taskList, assetsLatestTask, settings, out)
								}
								genLastTask = newTask
								
							} else {
								// If not chain, link each of them to the predecessor dependencies						
								tasksNeedingPredecessors << newTask
							}
							
						}

						if ( ! isChain && ! predecessor && lastTaskSpec ) {
							// If the task spec doesn't have a predecessor and there is a previous taskSpec, then we'll fake out the system by
							// adding a predecessor clause to the taskSpec so that the task(s) will be successors to the previous taskSpec's task(s).
							predecessor = [ taskSpec: lastTaskSpec.id ]
							// predecessor = predecessor
							hasPredTaskSpec = true
						}
							
						mapMode = 'DIRECT_MODE'
						break
						
					default:
						// We should NEVER get into this code but just in case...
						log.error("Task Spec (${taskSpec.id}) has an unhandled type ($stepType) in the code for event $moveEvent")
						throw new RuntimeException("Task Spec (${taskSpec.id}) has an unhandled type ($stepType) in the code")
						
				} // switch (stepType)

				// ------------------------------------------------------------------------------------
				// Post Creation - Logic to handle post creation before getting into the binding of dependencies
				// ------------------------------------------------------------------------------------


				// tmpDeferPred = [key: [ bindToAsset, otherAsset ] ]

				// Defer Predecessor - Find predecessors for the given asset. If no predecessors, wire to last task for the asset or milestone otherwise defer. Set assetsLatestTask to the the current task so successive tasks are wired correctly, mark the predecessor as having successor so milestones aren't picked up
				// don't go into the default predecessor process below.

				// Defer Successor (DONE)  - Wire up predecessors as normal but don't set assetsLatestTask to the latest task; Inject tmpDefSucc property with key and asset reference to the task

				// Gather Predecessor - This can be done per task below. Update the assetsLatestTask with this task for the predecessor's asset

				// Gather Successor - Perform after wiring up predecessors. This will set the tasks indicated as successors. Need to bump the assets latest tasks

				// Handle gathering and deferred 
				/*
				if ( resumeFor ) {
					// Find all tasks that were previously postponed for the key specified in resumeFor 
					tasksToResume = taskList.findAll { k, t -> t.metaClass.hasProperty(t, 'tmpWaitFor') && t.tmpWaitFor.containsKey(resumeFor) }
					tasksToResume = tasksToResume?.values().toList()
					log.debug "Postponment - Found ${tasksToResume.size()} tasks that were postponed with waitFor key ($resumeFor)"
				}
				*/

				// ------------------------------------------------------------------------------------
				// DEPENDENCY MAPPING - Now wire up the tasks that were just created
				// ------------------------------------------------------------------------------------
				
				// Mapping will be done for the following use-cases:
				//
				//   ASSET_MODE: Map dependencies based on asset relationships.
				//        i. TaskSpec contains predecessor.mode - then wire tasksNeedingPredecessors to their assets' previous step or prior milestone if none was found 
				//       ii. This can support group and other predecessor filter parameters
				//      iii. If group is presented then the tasksNeedingPredecessors will be wired to those found in the group and exceptions are reported for those not found
				//   TASK_MODE: Map dependencies based on predecessor.taskSpec
				//        i. tasksNeedingPredecessors and referenced taskSpec both have assets then they will be wired one-to-one
				//       ii. Either tasksNeedingPredecessors or referenced taskSpec DON'T have assets, all tasksNeedingPredecessors will be wired to all tasks from taskSpec
				//
				// The predecessor.required when false, will not update the asset with the latest task for any of the above use-cases
				

				if (tasksNeedingPredecessors.size() > 0 && ! ignorePred ) {
				
					// Set some vars that will be used in the next iterator 
					def predecessorTasks = []
					def tasksToResume = []	// This is used for waitFor/resumeFor of asset dependencies
								
					// Initialize the Map that will contain the list of assets that we want to bind predecessor tasks to
					def predAssets = [:]
					
					def assetIdsForTask = []

					//
					// Perform some SETUP for various conditions for things shared across all tasks that we're wiring dependencies for
					//

					if (depMode) {
						// Get the list of asset ids that were filtered out for the current task spec
						assetIdsForTask = assetsForTask*.id
					}	

					if (hasPredTaskSpec) {
						// Populate predecessorTasks array with all tasks that were created by the referenced predecessor.taskSpec(s)

						predTaskSpecs.each() { ts -> 
							// Find all predecessor tasks that have the taskSpec ID #
							if ( taskSpecTasks.containsKey( ts ) ) {
								predecessorTasks.addAll(taskSpecTasks[ts])
							} else {
								// This should never happen but just in case we'll log it 
								msg = "Task Spec (${taskSpec.id}) 'predecessor.taskSpec' id ($ts) references missing list of tasks"
								log.error(msg)
								throw new RuntimeException(msg)
							}
						}

						// Flip the predecessor tasks to a Map<List> of the tasks by the associated asset id
						predecessorTasks.each { t -> 
							if (t.assetEntity)
								predTasksByAssetId[t.assetEntity.id] << t
						}

						// log.debug "***%%%*** predTasksByAssetId: $predTasksByAssetId"
					}	

					if ( hasPredGroup ) {
						// If there are any groups defined then preload all of the assets that are included in the groups to be used 
						// in the next each/switch code block.
						log.info "hasPredGroup - here"
						// Put the group property into an array if not already an array
						def taskGroups = CU.asList(predecessor.group)

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
								}									
							} else {
								throw new RuntimeException("Task Spec (${taskSpec.id}) 'predecessor' value ($predecessor) references undefined group.")
							}
						} 
					}

					// ------------------------------------------------------------------------------------
					// Create Dependencies - iterate over all of the tasks just created for this taskSpec and assign dependencies
					// ------------------------------------------------------------------------------------
					out.append("## Creating predecessors for ${tasksNeedingPredecessors.size()} tasks<br/>")
				
					tasksNeedingPredecessors.each() { tnp ->
						log.info("### tasksNeedingPredecessors.each(): Processing $mapMode for task $tnp")

						if (tnp.assetEntity) {
							// Attempt to resolve any missed dependencies that may have occurred during an earlier step in the process.
							// For example, this can occur when there are multiple Application shutdown taskSpecs for various groups of application where there are 
							// dependencies between two or more applications. If the interdependent application shutdown tasks are created in separate task specs we 
							// need a way to bind them together when the subsequent taskspec is processed.
							// These missed dependencies are tracked in the missedDepList map. Missed dependencies are ONLY matched if they both occur in the same 
							// category (e.g. Shutdown).
							def missedDepKey = "${tnp.assetEntity.id}_${tnp.category}"
							log.info "missedDepList lookup for: $missedDepKey"
							if (missedDepList[missedDepKey]) {
								def tnpId = tnp.assetEntity.id
								log.info "Trying to find missed pred for '$missedDepKey' of asset $tnp"
								// Iterate over the missed dependencies and now create them
								def missedDepToRemove = []
								missedDepList[missedDepKey].each() {
									// TODO - get task from taskList instead of using get method
									def prevTask = AssetComment.get(it.taskId)
									if (prevTask) {
										log.info "Resolved missed dependency between $prevTask and $tnp"
										// The missed relationship earlier was that where this asset - huh? 

										// Lets see if we have an inverse relationship (e.g. Auto App Shutdown) so that we can switch the sequence
										// that the tasks are to be completed.
										log.debug "Calling createTaskDependency from tasksNeedingPredecessors.each inverse=${settings.isInversed}"
										if (settings.isInversed) {
											depCount += createTaskDependency(prevTask, tnp, taskList, assetsLatestTask, settings, out)
											log.debug "Inversed task predecessor due to inverse flag"
										} else {
											depCount += createTaskDependency(tnp, prevTask, taskList, assetsLatestTask, settings, out)
										}
										missedDepToRemove << prevTask
									} else {
										msg = "Unable to find task associated with missed dependency (id:${it.taskId})"
										log.error msg
										exceptions.append("${msg}<br/>")
									} 
								}

								// Remove the missing dep from the missing list
								if (missedDepToRemove.size()) {
									def missedDepUpdate = []
									missedDepList[missedDepKey].each() { missedDep ->
										if (! missedDepToRemove.find { it.id == missedDep } ) 
											missedDepUpdate << missedDep
									}
									log.debug "Removed missed predecessors for $missedDepKey, went from ${missedDepList[missedDepKey].size()} to ${missedDepUpdate.size()} missed dependencies"
									missedDepList[missedDepKey] = missedDepUpdate

								}
							}
						}

						def wasWired=false

						//
						// Gather up any deferred successor references if a successor.gather was presented
						//
						if ( settings.gatherSucc ) {
							def gdCnt = gatherDeferment(taskList, assetsLatestTask, tnp, 's', settings.gatherSucc, settings, out)
							if (gdCnt > 0) {
								wasWired = true
								depCount += gdCnt
							}
						}

						//
						// Gather up any deferred predecessor references if a predecessor.gather was presented
						//
						if ( settings.gatherPred ) {
							def gdCnt = gatherDeferment(taskList, assetsLatestTask, tnp, 'p', settings.gatherPred, settings, out)
							if (gdCnt > 0) {
								wasWired = true
								depCount += gdCnt
							}
						}

						switch(mapMode) {
												
							case 'DIRECT_MODE':
								// Link the current task to it's asset's latest task if it exists or to the milestone
								//
								if (findParent) {
									// In this situation we'll look up the parent predecessor task of the last know task of the asset
									log.debug "In findParent of DIRECT_MODE for task $tnp"
									if (tnp.assetEntity && assetsLatestTask[tnp.assetEntity.id]) {
										def parentDep = TaskDependency.findByAssetComment(assetsLatestTask[tnp.assetEntity.id])
										if (parentDep) {
											log.debug "Calling createTaskDependency from DIRECT_MODE"
											depCount += createTaskDependency(parentDep.predecessor, tnp, taskList, assetsLatestTask, settings, out)

											wasWired = true

											// We need to chain the peer task inside of this task so that subsequent task spec for the asset
											// will connect follow the chain and connect to all of the peers.
											tnp.metaClass.setProperty('chainPeerTask', assetsLatestTask[tnp.assetEntity.id])
										}
									}
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
									//   1. If tasks have assets then we bind the current task to the asset's latest task if found in the group. If not found then link current task to milestone
									//   2. If task does NOT have assets, then we bind the current task as successor to latest task of ALL assets in the list
									if (tnp.assetEntity) {
										log.debug "Processing from hasPredGroup #1"
										// Case #1 - Link task to it's asset's latest task if the asset was in the group and there is an previous task otherwise link it to the milestone if one exists
										if (predAssets.containsKey( tnp.assetEntity.id )) {
											// Find the latest asset for the task.assetEntity
											if (assetsLatestTask.containsKey(tnp.assetEntity.id)) {										
												log.debug "Calling createTaskDependency from GROUPS - asset ${tnp.id} ${tnp.assetName}"
												depCount += createTaskDependency(assetsLatestTask[tnp.assetEntity.id], tnp, taskList, assetsLatestTask, settings, out)
												wasWired = true
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
										// Case #2 - Wire latest tasks for all assets in group to this task
										log.debug "Processing from hasPredGroup #2 isRequired=$isRequired"

										// log.info("predAssets=$predAssets")
										// If the last task of the asset was a group task the current one is a group task, then we only want to create a single task dependency
										predAssets.each() { predAssetId, predAsset ->
											//log.info("predAsset=$predAsset")										
											if ( assetsLatestTask.containsKey(predAsset.id)) {
												def predAssetTask = assetsLatestTask[predAsset.id]
												log.debug "Calling createTaskDependency from GROUPS 2 - asset ${predAsset.id} ${predAsset.assetName}"
												depCount += createTaskDependency(predAssetTask, tnp, taskList, assetsLatestTask, settings, out)

												// Check to see if the predecessor and the successor are funnels and if so, then move the asset forward to the new successor
												if ( predAssetTask.metaClass.hasProperty(predAssetTask, 'tmpIsFunnellingTask') && tnp.metaClass.hasProperty(tnp, 'tmpIsFunnellingTask') ) {
													// assetsLatestTask[predAsset.id] = tnp
													assignToAssetsLatestTask(predAsset, tnp, assetsLatestTask)
												}

												wasWired = true
											} else {
												// if (isGroupingSpec) {
												if (tnp.metaClass.hasProperty(tnp, 'tmpIsFunnellingTask')) {
													// So no task previously existed for the asset so we're going to just wire the assets' last task to the gateway task 
													// assetsLatestTask[predAsset.id] = tnp
													assignToAssetsLatestTask(predAsset, tnp, assetsLatestTask)
													log.debug "Funnelled assignment of task $tnp as last task for asset ${predAsset.id} ${predAsset.assetName}"
												}

											}
										}
									}					
								
								} else if ( hasPredTaskSpec && ! depMode) {
									//
									// --- TASKSPEC used W/O mode ---
									//
								
									log.debug "Processing in hasPredTaskSpec of DIRECT_MODE"
								
									// Use the predecessorTasks array that was initialized earlier. If any of those tasks and the current task are associated with the
									// same asset then wire up the predecessor tasks one-to-one for each asset otherwise wire the current task to all tasks in the 
									// predecessorTasks. If both have assets and we are unable to find a predecessor task for the same asset, the current task will 
									// be wired to the most recent milestone if it exists.
									if (predecessorTasks.size() > 0) {
									
										// See if we're linking task to task by asset 
										if (predecessorTasks[0].assetEntity && tnp.assetEntity) {
											// Wire asset-to-asset for tasks if there is a match
											def predTask = predecessorTasks.find { it.assetEntity.id == tnp.assetEntity.id }
											if (predTask) {										
												log.debug "Calling createTaskDependency from TASKSPEC"
												depCount += createTaskDependency(predTask, tnp, taskList, assetsLatestTask, settings, out)
												wasWired = true
											} else {
												msg = "No predecessor task for asset (${tnp.assetEntity}) found to link to task ($tnp) in taskSpec ${taskSpec.id} (DIRECT_MODE/taskSpec)"

												// Push the task onto the missing pred stack to be wired later if possible
												// TODO - Need to validate that this is necessary as it might wire things wrong
												saveMissingDep(missedDepList, "${tnp.assetEntity.id}_${tnp.category ?: 'BLANK'}", taskSpec, tnp, isRequired, latestMilestone)

												//log.info(msg)
												//exceptions.append("${msg}<br/>")
											}
										} else {
											// Link all predecessorTasks to tnp
											// Wire the current task as the successor to all tasks specified in the predecessor.taskSpec property
											log.info "predecessorTasks=${predecessorTasks.class}"
											predecessorTasks.each() { predTask -> 
												log.info "predTask=${predTask.class}"
												log.debug "Calling createTaskDependency from TASKSPEC 2"
												depCount += createTaskDependency(predTask, tnp, taskList, assetsLatestTask, settings, out)

												if ( isRequired ) {
													// Update the Asset's last task based on if the previous task is for an asset or the current one is for an asset
													// TODO - NOT certain that we need this predecessor assignment and need to investigate
													if ( predTask.assetEntity ) {
														// assetsLatestTask[predTask.assetEntity.id] = tnp
														assignToAssetsLatestTask(predTask.assetEntity, tnp, assetsLatestTask)
														log.info "Adding latest task $tnp to asset ${tnp.assetEntity} - 7"
													
													} //else if ( tnp.assetEntity )  {
														// assetsLatestTask[tnp.assetEntity.id] = tnp
														// log.info "Adding latest task $tnp to asset ${tnp.assetEntity} - 8"
													
													// }
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
								log.debug "case 'ASSET_DEP_MODE': depMode=$depMode, asset=$tnp.assetEntity, bundleIds=$bundleIds"

								// Get a list of dependencies for the current asset
								def assetDependencies = getAssetDependencies(tnp.assetEntity, taskSpec, depMode, bundleIds)
								def assetDepCount = assetDependencies.size()

								// Binding Asset Dependencies as task predecessors can be postponed by using the predecessor.defer attribute with a key reference (e.g. 'AppsShutdown'). When 
								// the property is designated then the logic should update the task with deferral information to be gathered later. One exception to this is if the asset doesn't
								// have any dependencies, in which case, the task should be bound to latest task for the asset. tribute into the task being postponed. 
								//
								// Handle the postponing, the gathering is handled below since various mapModes can gather task dependencies
								//
								// For the sack of postponment, we only want to do so if the dependencies returned are associated to the assets in the 
								// present TaskSpec filter so we need to filter down the assetDependencies list appropriately.
								//
								if ( settings.deferPred ) {
									// This is one of the trickiest parts of the generation logic with what way we look at the relationships
									// which is just a guess at this point... 

									def deferMode = depMode
									if (! settings.isInversed) {
										deferMode = depMode == 's' ? 'r' : 's'
									}

									def deferDeps = getAssetDependencies(tnp.assetEntity, taskSpec, deferMode, bundleIds)
									// The getAssetDependencies can return a larger set to dependencies than what we want for this process. In this case, we only want the 
									// dependencies that are contained within assets associated with this taskSpec so we'll filter down the list to just those asset ids.
									deferDeps = deferDeps.findAll { assetIdsForTask.contains(it.asset.id) && assetIdsForTask.contains(it.dependent.id) }

									log.debug "Postponing predecessor binding for task $tnp with key (${settings.deferPred}) to ${deferDeps.size()} predecessors"

									if ( deferDeps.size() ) {

										// Track that the last task for the asset is this task needing predecessors but don't wire up as a successor? 
										assignToAssetsLatestTask( tnp.assetEntity, tnp, assetsLatestTask)

										// For each of the set of dependencies, we need to indicate that this deferred task is the latest task for the dependency asset
										// By comparing the asset and dependent properties being equal to the current task's asset, we can determine which is the dependency
										def defPredProp = tnp.assetEntity.id == deferDeps[0].asset.id ? 'dependent' : 'asset'
										deferDeps.each { dd -> 

											// Lets postpone the wiring of the task by injecting the meta property tmpWaitFor
											setDeferment(tnp, dd[defPredProp], 'p', settings.deferPred, settings)

											// 
											// assetsLatestTask[ dd[defPredProp].id ] = tnp
										}

										wasWired=true
										break
									} else {
										log.debug "Postponing wasn't necessary so just created relation back to last asset task or milestone"
										linkTaskToLastAssetOrMilestone(tnp)
										wasWired = true
										break
									}
								}

								// Check to see if there is a funnel predecessor for the asset and if so, create a dependency to it
								if ( assetsLatestTask.containsKey(tnp.assetEntity.id) && 
									 assetsLatestTask[tnp.assetEntity.id].metaClass.hasProperty(assetsLatestTask[tnp.assetEntity.id], 'tmpIsFunnellingTask')
								) {
									log.debug "Calling createTaskDependency from ASSET_DEP_MODE"
									depCount += createTaskDependency(assetsLatestTask[tnp.assetEntity.id], tnp, taskList, assetsLatestTask, settings, out)
									wasWired=true
								}

								// If there was no previous funnel for the asset and we couldn't find any dependencies, then just wire the task to a previous milestone
								if (assetDependencies.size() == 0 && ! wasWired) {
									exceptions.append("Asset(${tnp.assetEntity}) for Task(${tnp}) has no ${predecessor} relationships<br/>")

									// Link task to the last known milestone or it's asset's previous task
									linkTaskToLastAssetOrMilestone(tnp)
									// TODO : Don't think this if logic is necessary any more
									if (isRequired && ! deferSucc)
										assignToAssetsLatestTask( tnp.assetEntity, tnp, assetsLatestTask)

									wasWired = true
								
								} 

								// If there were any asset dependencies, nows the time that we'll try and wire it up
								if (assetDependencies.size()) {
									// Look over the assets dependencies that are associated to the current task's asset and create predecessor relationships 
									// We will warn on assets that are not part of the moveEvent that have dependency. 
									// TODO : We most likely will want to have tasks for assets not moving in the future but will require discussion.
									log.info "** Iterate over ${assetDependencies.size()} dependencies for asset ${tnp.assetEntity}"
									assetDependencies.each { ad ->

										def predAsset 
										if (depMode=='b') {
											predAsset = (ad.asset.id == tnp.assetEntity.id ? ad.dependent : ad.asset)
										} else {
											// Note that this should be the opposite of that used in the getAssetDependencies 
											predAsset = (depMode == 's' ?  ad.dependent : ad.asset)
										}

										// We have two ways of handling wiring up predecessors:
										//    a) With taskspec references, wire to all of the tasks created for the supporting assets that were created by the taskSpec(s)
										//    b) Wire to the last tasks of the supporting assets
										if (hasPredTaskSpec) {
											// Wiring dependencies referencing tasks created in earlier taskSpecs. We will find all tasks for each asset associated 
											// with the task needing predecessors
											log.debug "Wiring predecessors based on previous taskSpecs for Asset $predAsset"
											if (predTasksByAssetId.containsKey(predAsset.id)) {
												predTasksByAssetId[predAsset.id].each { pt ->
													log.debug "Calling createTaskDependency from ASSET_DEP_MODE.taskSpec"
													depCount += createTaskDependency(pt, tnp, taskList, assetsLatestTask, settings, out)
													wasWired=true
												}
											}
										} else {

											log.debug "Working on dependency asset (${ad.asset}_ depends on (${ad.dependent}) - matching on asset $predAsset"

											// Make sure that the other asset is in one of the bundles in the event
											def predMoveBundle = predAsset.moveBundle
											if (! predMoveBundle || ! bundleIds.contains(predMoveBundle.id)) {
												exceptions.append("Asset dependency references asset not in event: task($tnp) between asset ${tnp.assetEntity} and ${predAsset}<br/>")
											} else {
												// Find the last task for the predAsset to create associations between tasks. If the predecessor was created
												// during the same taskStep, assetsLatestTask may not yet be populated so we can scan the tasks created list for
												// one with the same taskSpec id #
												def previousTask = null
												previousTask = taskList.find { i, t -> t.assetEntity?.id == predAsset.id && t.taskSpec == tnp.taskSpec }?.value
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
													log.debug "Calling createTaskDependency from ASSET_DEP_MODE"
													depCount += createTaskDependency(previousTask, tnp, taskList, assetsLatestTask, settings, out)
													wasWired=true
												} else {
													log.info "No predecessor task found for asset ($predAsset) to link to task ($tnp) ASSET_DEP_MODE"
													// exceptions.append("No predecessor task found for asset ($predAsset) to link to task ($tnp)<br/>")
													
													// Push this task onto the stack to be wired up later on
													saveMissingDep(missedDepList, "${predAsset.id}_${tnp.category}", taskSpec, tnp, isRequired, latestMilestone, ad)
												}
											}
										}
									} // assetDependencies.each

									if (hasPredTaskSpec && ! wasWired) {
										exceptions.append("Task($tnp) No predecessor(s) found using predecessor.taskSpec in taskSpec(${taskSpec.id})<br/>")
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
								
								// This logic is used for the various grouping actions (e.g. cart, truck, set)
								// Iterate over the associated assets that were stuffed into the task.associatedAssets list
								def foundPred=false
								tnp.associatedAssets.each() { assocAsset ->
									// Let's stuff the asset into the task and then wire up the predecessors 
									if (assetsLatestTask.containsKey(assocAsset.id)) {
										tnp.assetEntity = assocAsset
										linkTaskToLastAssetOrMilestone(tnp)
										foundPred = true
									} else {
										assetsLatestTask[assocAsset.id] = tnp
									}
								}
								tnp.assetEntity = null

								if (! foundPred) {
									// If we didn't wire-up the tnp to previous tasks then we need to wire up the tnp the last gateway
									linkTaskToLastAssetOrMilestone(tnp)
								}

								wasWired = true

								break
								
							default:
					
								msg = "Unsupported switch value ($mapMode) for taskSpec (${taskSpec.id}) on processing task $tnp"
								log.info(msg)
								throw new RuntimeException(msg)
							
						
						} // switch(mapMode)

						//
						// Handle the resuming of the waitFor
						//
						if ( false ) {

							depCount += gatherDeferment(taskList, assetsLatestTask, tnp, 's', settings.gatherSucc, settings, out)

							/*

							// App B was postponed due to App A. Task Shutdown B waits until subsequent task for App A to complete to wire up
							//
							// Find all tasks that were previously postponed for the key specified in resumeFor 
							log.debug "Going to try and wire up any postponed tasks with waitFor:'$resumeFor'"
							tasksToResume.each { ttr -> 
								// Iterate over the tmpWaitFor[resumeFor] array to find reference to the current task's asset and wire those found together 
								// and remove the reference from the list

								// Need to determine if the dependency on the postponed task was based on the supports or requires criteria so that it properly 
								// locates the dependencies in the array. It should find the property that doesn't the task's asset id.
								if (ttr.tmpWaitFor[resumeFor].size()) {
									def ppDepKey = ttr.assetEntity.id == ttr.tmpWaitFor[resumeFor][0].asset.id ? 'dependent' : 'asset'
									def ppDep = ttr.tmpWaitFor[resumeFor].find { it[ppDepKey].id == tnp.assetEntity.id }
									if (ppDep) {
										log.debug "Found postponed task $ttr for asset $ttr.assetEntity so wiring it as successor of $tnp"
										// Create the task dependency and delete the dependency with the ttr array
										depCount += createTaskDependency(tnp, ttr, taskList, assetsLatestTask, false, false, false, out)

										if (! ttr.tmpWaitFor[resumeFor].removeAll {it.id == ppDep.id }) {
											log.error "Was unable to remove the taskToResume dependency that was found ($ppdep)"
										} 

										// Mark the resumed task as pending since it is now waiting for a predecessor
										ttr.status = AssetCommentStatus.PENDING

										wasWired = true

									}
								}
							}
							*/
						}
			
						if (! wasWired ) {
							// If the task wasn't wired to any predecessors, then try to wire it to the latest milestone
							linkTaskToLastAssetOrMilestone(tnp)
						}
					
					} // tasksNeedingPredecessors.each()
					
					
				}
				
				lastTaskSpec = taskSpec

			} // recipeTasks.each() {}

			// *******************************************
			// TODO - Iterate over the missedDepList and wire tasks to their milestone
			// *******************************************
		} catch(e)	{
			failure = e.toString()
			// exceptions.append("We BLEW UP damn it!<br/>")
			// exceptions.append(failure)
			e.printStackTrace()
		}
		
		// Check to make sure that all of the deferred tasks have been collected as they should have
		def defTaskList = getOutstandingDeferments(taskList)
		if ( defTaskList.size() ) {
			exception.append("${defTaskList.size()} Outstanding Deferred Tasks were never gathered<br/>")
			log.warn "Outstanding Deferred Tasks were never gathered: $defTaskList"
		}

		TimeDuration elapsed = TimeCategory.minus( new Date(), startedAt )
		
		log.info "A total of ${taskList.size()} Tasks and $depCount Dependencies created in $elapsed"
		if (failure) failure = "Generation FAILED: $failure<br/>"
					
		return ["status":"${failure}${taskList.size()} Tasks and $depCount Dependencies created in $elapsed",
				"exceptions":exceptions.toString(), "Log":out.toString()]
		
	}
	/**
	 * A helper method called by generateRunbook to lookup the AssetDependency records for an Asset as specified in the taskSpec
	 * @param asset - can be any asset type (Application, AssetEntity, Database or Files)
	 * @param taskSpec - a Task specification map
	 * @param depMode - the dependency mode that is s)upports, r)equires or b)oth options
	 * @param bundleIds - a list of the bundle ids for the task generation
	 * @return List<AssetDependency> - list of dependencies
	 */
	def getAssetDependencies(Object asset, Map taskSpec, String depMode, List bundleIds) {
		def list = []
		def finalList = []
	
		assert (asset instanceof AssetEntity)

		// This is the list of properties that can be added to the search criteria from the Filter
		def supportedPredFilterProps = ['status', 'type', 'dataFlowFreq', 'dataFlowDirection']
		
		// AssetEntity  asset			// The asset that that REQUIRES the 'dependent'
		// AssetEntity dependent		// The asset that SUPPORTS 'asset' 
		def currAssetPropName 
		def assocAssetPropName 
		def baseSql
		def map

		if (depMode=='s') {
			// Supports relationship
			currAssetPropName = 'asset' 
			assocAssetPropName = 'dependent'
		} else if (depMode=='r') {
			// Requires relationship
			currAssetPropName = 'dependent' 
			assocAssetPropName = 'asset'
		}

		if ( depMode == 'b' ) {
			baseSql = "from AssetDependency ad where ( ad.dependent.id=:assetId or ad.asset.id=:assetId ) and \
			    ad.dependent.moveBundle.id IN (:moveBundleIds) and ad.asset.moveBundle.id IN (:moveBundleIds) and \
				ad.status not in ('${AssetDependencyStatus.NA}', '${AssetDependencyStatus.ARCHIVED}') \
				and ad.type <> '${AssetDependencyType.BATCH}'"
		} else {
			baseSql = "from AssetDependency ad where ad.${currAssetPropName}.id=:assetId and \
				ad.${assocAssetPropName}.moveBundle.id IN (:moveBundleIds) and \
				ad.status not in ('${AssetDependencyStatus.NA}', '${AssetDependencyStatus.ARCHIVED}') \
				and ad.type <> '${AssetDependencyType.BATCH}'"
		}

		// TODO - Wire in filtering of the bundle ids
		// Note that this should be the opposite of that used in the getAssetDependencies 
		// def predAsset = depMode == 's' ?  ad.dependent : ad.asset

		map = [ assetId:asset.id, moveBundleIds:bundleIds ]

		def sqlMap
		def sql = baseSql

		// Add additional WHERE expresions to the SQL
		supportedPredFilterProps.each() { prop ->
			if (taskSpec.predecessor?.containsKey(prop)) {
				sqlMap = SqlUtil.whereExpression("ad.$prop", taskSpec.predecessor[prop], prop)
				if (sqlMap) {
					sql = SqlUtil.appendToWhere(sql, sqlMap.sql)
					if (sqlMap.param) {
						map[prop] = sqlMap.param
					}								
				} else {
					log.error "SqlUtil.whereExpression unable to resolve ${prop} expression [${taskSpec.predecessor[prop]}]"
					throw new RuntimeException("Unable to resolve filter param:[${prop}] expression:[${taskSpec.predecessor[prop]}] while processing asset ${asset}")
				}
			}
			
		}

		// Add filter on the classification (asset.type presently) if it was declared (this is filtering on the apposing asset.assetType property)
		if (taskSpec.predecessor?.containsKey('classification') && taskSpec.predecessor.classification) {
			sqlMap = SqlUtil.whereExpression("ad.${assocAssetPropName}.assetType", taskSpec.predecessor.classification, 'classification')
			sql = SqlUtil.appendToWhere(sql, sqlMap.sql)
			map['classification'] = sqlMap.param
			log.info "getAssetDependencies: Added classification filter ${sqlMap.sql}, ${sqlMap.param}"
		}
		
		log.info "getAssetDependencies: depMode=$depMode, SQL=$sql, PARAMS=$map"
		list = AssetDependency.findAll(sql, map)
		log.info "getAssetDependencies: found ${list.size()} rows : $list"

		// Filter out only the dependencies where the assets are both in the bundle list
		// list = list.findAll { bundleIds.contains(it.asset.moveBundle.id) && bundleIds.contains( it.dependent.moveBundle.id) }

		// Now need to find the nested logic associations (e.g. APP > DB > SRV or APP > Storage > SAN) and 
		// add the dependency of the logic asset to the current asset's dependencies.
		if (depMode == 'b') {
			// we can't traverse in the both relationship as it makes my head spin...
			finalList = list
		} else {
			// Only traverse if the dependency specification requests it
			if (taskSpec.predecessor?.containsKey('traverse') && taskSpec.predecessor.traverse ) {
				list.each() { dep ->

					def nestedDep 

					// Call the recursive routine that will find any nested dependencies (e.g. App > DB > DB App or App > LUN > Storage App)
					nestedDep=traverseDependencies(asset, dep, currAssetPropName, assocAssetPropName, baseSql, map)
					if (nestedDep.size()) {
						// nestedDep = nestedDep.findAll { bundleIds.contains(it.asset.moveBundle.id) && bundleIds.contains( it.dependent.moveBundle.id) }
						if (nestedDep.size())
							finalList.addAll(nestedDep)
					}

					// TODO - getAssetDependencies() - prevent linking App > VM > VMW Cluster as this is typically unnecessary
					// TODO - getAssetDependencies() - need to determine if we need to bind blades to chassis as dependencies

				}
			} else {
				finalList = list
			}
		}

		// Need to force a unique list because we could end up with a lot of duplicate asset interdepencies
		// We will therefore find all of the unique asset ids on the associated side of the dependency map
		log.info "getAssetDependencies: ${finalList.size()} dependencies after traversing dependencies"
		if (depMode == 'b') {
			// For both, to make sure that the uniquie is always unique, we'll contruct a string of the two ids making sure that the 
			// asset that we're searching on is always the first 
			finalList.unique { ( it.asset.id == asset.id ? "${it.asset.id}:${it.dependent.id}" : "${it.dependent.id}:${it.asset.id}" ) }
		} else {
			finalList.unique { it[assocAssetPropName].id }
		}
		log.info "getAssetDependencies: ${finalList.size()} dependencies after uniquing the list"

		return finalList		
	}

	/**
	 * This is a recursive helper method used by getAssetDependencies to traverse the dependencies of an asset to find other correlating 
	 * assets that should be linked from a dependency standpoint. It will examine the assets in the dependency and if the associated asset
	 * is a logical then it will traverse logical's dependencies to find a real asset. If the origin asset is an application and it finds
	 * a device dependency, it will attempt to traverse to find any dependency application, primarilly looking for database clusters, 
	 * VM Clusters or Storage Apps (e.g. App > Server > logical DB > DB App; or App > logical DB > DB App).
	 *
	 * Rules:
	 *   A) if orig asset is device and 2nd is application - add app and stop
	 *   B) if orig is app and 2nd is app - add app and stop (scenario 3)
	 *   C) if orig is app and 2nd is logical - recurse and if next level is non-logical add (scenario 1)
	 *   D) if orig is app and 2nd is device - add the device and recurse but don't add any additional devices (just looking for apps) (scenario 5)
	 *
	 * @param assetEntity - the originating asset for the dependency walk
	 * @param AssetDependency - the current dependency being inspected 
	 * @param String - the dependency property name that represents the current asset we're focused on 
	 * @param String - the dependency property name that represents the assocated asset
	 * @param String - the SQL used to perform the query on the dependencies
	 * @param Array<AssetDependency> - list of dependencies accumlated by the recursive function
	 * @param Integer - the recursive depth
	 * @return Array<AssetDependency> - the list of dependencies accumlated by the recursive function
	 */
	def traverseDependencies(origAsset, dependency, currAssetPropName, assocAssetPropName, sql, map) {
		log.debug "traverseDependencies() origAsset=$origAsset, dependency=$dependency, currAssetPropName=$currAssetPropName, assocAssetPropName=$assocAssetPropName"
		def depList = []
		def assocAsset = dependency[assocAssetPropName]
		def origIsApp = origAsset.isaApplication()
		def assocIsLogical = assocAsset.isaLogicalType()

		// Only traversing if original asset is an application
		// TODO - traverseDependencies() need to determine if we'll traverse other things besides Applications 
		if (!origIsApp) {
			return [dependency]
		}

		// Determine if the dependency is a logical type then traverse to its dependencies and if we hit a non-logical
		// asset, add it to the dependency list
		//if (assocAsset.isaDatabase() || assocAsset.isaStorage() || assocAsset.isaNetwork() ) {

		// Graph #3 & #4 - this breaks #5
		// If we have an App to App relationship at the first level, no need to go any further, just return the dependency
		if (! assocIsLogical) {
			return [dependency]
		}

		// Graph #1 & #2
		// First level we have a Logical so let's find its dependencies and link up appropriately
		// def currAsset = dependency[currAssetPropName]
		map.assetId = assocAsset.id
		def logicDep = AssetDependency.findAll(sql, map)

		log.info "traverseDependencies: Found ${logicDep.size()} logical dependencies for $origAsset"
		logicDep.each() { d ->
			def depAsset = d[assocAssetPropName]
			if (! depAsset.isaLogicalType() ) {
				// Add any real asset to the dependency list
				depList.add(d)
				log.info "traverseDependencies: Adding dependency on ${d[assocAssetPropName]}"
			}
		}

		return depList
	}
	
	/**
	 * Creates a task for a moveEvent based on a taskSpec from a recipe and defaults the status to READY
	 * @param moveEvent object
	 * @param Map taskSpec that contains the specifications for how to create the task
	 * @param asset - The asset associated with the task if there appropriate
	 * @return AssetComment (aka Task)
	 */
	def createTaskFromSpec(recipeId, whom, taskList, taskNumber, moveEvent, taskSpec, projectStaff, exceptions, workflow=null, asset=null) {
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
			
		def msg 

		// Handle setting the task duration which can have an indirect reference to another property as well as a default value 
		// or a numeric value
		task.priority = taskSpec.containsKey('priority') ? taskSpec.priority : 3
		if (taskSpec.containsKey('duration')) {
			if ( taskSpec.duration instanceof String && taskSpec.duration.size() ) {
				if (taskSpec.duration[0] == '#') {
					// Deal with the indirection which allows for #PropertyName, Default value
					def durParams = taskSpec.duration.split(',')
					def defDur = 0
					if (durParams.size() == 2) {
						log.debug "createTaskFromSpec: found indirect duration (${taskSpec.duration}) with a default for task spec (${taskSpec.id})"
						if (durParams[1]?.trim().isNumber()) {
							defDur = NumberUtils.toInt(durParams[1].trim(), 0)
						} else {
							msg = "Duration default value is an invalid integer (${taskSpec.duration}) , taskSpec=$taskSpec.id"
							log.error msg
							exceptions.append("$msg<br/>")
						}
					}

					// Try and do the lookup and if that fails, use the default
					try {
						def indDur = getIndirectPropertyRef( asset, durParams[0].trim() )
						if (indDur instanceof Integer) {
							task.duration = indDur ?: defDur
						} else if (indDur instanceof String) {
							task.duration = NumberUtils.toInt(indDur, defDur)
						} else {
							task.duration = defDur
							if (indDur != null) {
								msg = "Indirect duration reference (${taskSpec.duration}) referenced invalid data type, asset=$asset, taskSpec=$taskSpec.id"
								log.error msg
								exceptions.append("$msg<br/>")
							}
						}
					} catch (e) {
						msg = "Exception with indirect duration reference (${taskSpec.duration}) - ${e.getMessage()}, asset=$asset, taskSpec=$taskSpec.id"
						log.error msg
						exceptions.append("$msg<br/>")
						task.duration=defDur
					}

				} else if ( taskSpec.duration?.isNumber() ) {
					task.duration = NumberUtils.toInt(taskSpec.duration, 0)
				}
			} else {
				task.duration = taskSpec.duration	
			}
			log.debug "createTaskFromSpec: Set duration to ${task.duration}"
		}

		if (taskSpec.containsKey('category') && taskSpec.category ) 
			task.category = taskSpec.category
		
		def defCat = task.category
		task.category = null
		
		// TODO : Should be able to parse the duration for a character
		task.durationScale = 'm'
		
		try {
			if (asset) {
				task.comment = new GStringEval().toString(taskSpec.title, asset)
			} else {
				task.comment = new GStringEval().toString(taskSpec.title, moveEvent)
			}
		} catch (Exception ex) {
			exceptions.append("Unable to parse title (${taskSpec.title}) for taskSpec ${taskSpec.id}<br/>")
			task.comment = "** Error computing title **"
		}
			
		// Set various values if there is a workflow associated to this taskSpec and Asset
		if (workflow) {
			// log.info "Applying workflow values to task $taskNumber - values=$workflow"
			if (workflow.workflow_transition_id) { task.workflowTransition = WorkflowTransition.read(workflow.workflow_transition_id) }
			// if (! task.role && workflow.role_id) { task.role = workflow.role_id }
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

		// Perform the assignment logic
		def errMsg = assignWhomAndTeamToTask(task, taskSpec, workflow, projectStaff)
		if (errMsg) 
			exceptions.append("${errMsg}<br/>")

		taskList[task.id] = task
		
		log.info "Saved task ${task.id} - ${task}"
		
		// Set any scheduling constraints on the task
		setTaskConstraints(task, taskSpec, workflow, projectStaff, exceptions) 

		return task
	}
	
	/**
	 * Used to create a task dependency between two task that also will examine the predecessor task to see if it has a
	 * chainPeerTask associated to it and if so, will iteratively call itself to create the association with the successor to
	 * the peer task.
	 * @param AssetComment - the predecessor task in the dependency
	 * @param AssetComment - the successor task in the dependency
	 * @param Map<AssetComment> - taskList the list of all tasks so that it can update tasks with the hasSuccessorTaskFlag
	 * @param Map collection of the various taskSpec parameters
	 * @param Integer - Used to count of the number of recursive iterations that occur
	 * @return TaskDependency
	 * @throws RuntimeError if unable to save the dependency
	 */
	def createTaskDependency( predecessor, successor, taskList, assetsLatestTask, settings, out, count=0 ) {

		log.info "createTaskDependency: ** START - Creating dependency count=$count predecessor=$predecessor, successor=$successor"

		// Need to see if the dependency was previously created. In addition, in the case of a missed dependency where there is a reversed
		// taskSpec dependency (e.g. Auto App Shutdowns) we can get into the situation where a dependency is improperly created so we'll
		// only create the first, which is created by the missed dependency (e.g. db -> app and later app -> db)
		// TODO : change to use local memory list
		if ( TaskDependency.findByAssetCommentAndPredecessor(successor, predecessor) || 
			 TaskDependency.findByAssetCommentAndPredecessor(predecessor, successor) ) {
			log.info "createTaskDependency: dependency already exists"

			// See if the current task is a funnelling type task and if so, then we probably want to update the asset of the successor task to this task ?? 
			return count
		}

		if (predecessor.id == successor.id) {
			throw new RuntimeException("Attempted to create dependency where predecessor and successor are the same task ($predecessor)")
		}

		/*
		if (settings.deferPred) {
			throw new RuntimeException("createTaskDependency: shouldn't be called with predecessor.defer:'${settings.deferPred}' ($predecessor), succ ($successor)")			
		}
		*/

		def dependency = new TaskDependency( predecessor:predecessor, assetComment:successor )
		if (! ( dependency.validate() && dependency.save(flush:true) ) ) {
			throw new RuntimeException("Error while trying to create dependency between predecessor=$predecessor, successor=$successor<br/>Error:${GormUtil.errorsAsUL(dependency)}, ")
		}
		out.append("Created dependency (${dependency.id}) between $predecessor and $successor<br/>")
		count++

		// Mark the predecessor task that it has a successor so it doesn't mess up the milestones
		if ( ! predecessor.metaClass.hasProperty(predecessor, 'hasSuccessorTaskFlag') )
			predecessor.metaClass.setProperty('hasSuccessorTaskFlag', true)

		// Mark the Successor task to the PENDING status since it has to wait for the predecessor task to complete before it can begin
		successor.status = AssetCommentStatus.PENDING
		if (! ( successor.validate() && successor.save(flush:true) ) ) {
			throw new RuntimeException("Failed to update successor ($successor) status to PENDING<br/>Errors:${GormUtil.errorsAsUL(dependency)}")
		}

		if (predecessor.assetEntity)
			log.info "createTaskDependency: pred asset latest task? ${assetsLatestTask.containsKey(predecessor.assetEntity.id)} - ${ (assetsLatestTask[predecessor.assetEntity.id] ?: '') }"

		// Handle updating predecessor tasks' asset with successor task if it is a funnel type (e.g. gateway, milestone, set, truck, cart, etc)
		if (predecessor.assetEntity && successor.metaClass.hasProperty(successor, 'tmpIsFunnellingTask') ) {
			assignToAssetsLatestTask(predecessor.assetEntity, successor, assetsLatestTask)
			// assetsLatestTask[predecessor.assetEntity.id] = successor
			log.info "createTaskDependency: Updated assetsLatestTask for asset ${successor.assetEntity} with funnel task $successor"			
		}

		// Update the successor tasks' assets with latest task with the current task if it isRequired and not being deferred 
		if ( successor.assetEntity && settings.isRequired  && ! settings.deferSucc ) {
			assignToAssetsLatestTask(successor.assetEntity, successor, assetsLatestTask)
			//assetsLatestTask[successor.assetEntity.id] = successor
			log.info "createTaskDependency: Updated assetsLatestTask for asset ${successor.assetEntity} with successor $successor"			
		}
/*
		if (settings.isRequired && ! settings.deferSucc) {
			// Update the task in the task list that it has a successor which is used by Milestones
			// TODO - Why wouldn't a task be in the list? if not there, then we might want to warn or add?
			if ( taskList.containsKey(predecessor.id) ) {
				log.info "createTaskDependency: Setting hasSuccessorTaskFlag=true on task ${taskList[predecessor.id]}"
				taskList[predecessor.id].metaClass.setProperty('hasSuccessorTaskFlag', true)
			} else {
				throw new RuntimeException("Unable to find predecessor task $predecessor for successor task $successor in taskList")
//				log.error "createTaskDependency: unable to find predecessor task $predecessor for successor task $successor in taskList"
			}
		}
*/

		// Handling deferring dependency relationships when taskSpec indicates to successor.defer:'key'
		if (settings.deferSucc) {
			setDeferment(successor, successor.assetEntity, 's', settings.deferSucc, settings) 

			// Bump the last task up so that subsequent tasks are bound to this task
//			if (successor.assetEntity)
//				assetsLatestTask[successor.assetEntity.id] = successor
		}

		// Handle gathering all previously deferred tasks for an asset and setup predecessor relationships
/*
		if (settings.gatherSucc) {
			count += gatherDeferment(taskList, assetsLatestTask, successor, 's', settings.gatherSucc, settings, out)
		} 
*/
/*
		// Handle gathering all previously deferred tasks for an asset and setup predecessor relationships
		if (settings.gatherSucc) {
			out.append("createTaskDependency: gatherSucc - successor $successor (${successor.assetEntity}0</br>")
		
			if (successor.assetEntity) {
				def gatheredTasks = taskList.findAll { id, t -> t.metaClass?.hasProperty(t, 'tmpDeferredDep') && 
					t.tmpDeferredDep?.id == successor.assetEntity.id
				}
				log.debug "createTaskDependency: checking for tasks to gather $successor, found ${gatheredTasks} deferred tasks"
				gatheredTasks.each { k, defPredTask ->
					// This is a possibility that the successor task will have been marked as deferred by a previous bump so we have to 
					// double check that the tasks are different.
					if ( defPredTask.id != successor.id ) {
						log.debug "createTaskDependency: gathered predecessor task ($defPredTask) to successor task ($successor)"
						count += createTaskDependency(defPredTask, successor, taskList, assetsLatestTask, settings, out)
						// assetsLatestTask[]
						// Mark the task as having been gathered
						if (defPredTask.assetEntity) 
							assetsLatestTask[defPredTask.assetEntity.id] = successor

						taskList[defPredTask.id].tmpDeferredDep = null
					}
				}
			} else {
				throw new RuntimeException("Invalid use of 'gather' as the task spec (${successor.taskSpec}) must reference an asset")					
			}
		}
*/

		// If this successor is pointing back to a predecessor that has a deferral, then we need to bump the deferral onto the successor
		//if (! deferSucc && ! gatherSucc && predecessor.metaClass.hasProperty(predecessor, 'tmpDeferredDep') && predecessor.tmpDeferredDep) {
		//	log.debug "createTaskDependency: Bumped predecessor deferment to the successor (pred: $predecessor, succ: $successor"
		//	successor.metaClass.setProperty('tmpDeferredDep', predecessor.tmpDeferredDep)
		//	predecessor.tmpDeferredDep = null
		//}

		// Here is the recursive loop if the predecessor has a peer
		if (predecessor.metaClass?.hasProperty(predecessor, 'chainPeerTask')) {
			log.info "createTaskDependency: Invoking recursively due to predecessor having chainPeerTask (${predecessor.chainPeerTask})"
			count += createTaskDependency( predecessor.chainPeerTask, successor, taskList, assetsLatestTask, settings, out, count)
		}

		log.debug "createTaskDependency: ** FINISHED - count=$count"
		
		return count
	}

	/**
	 * Helper function used to assign a task as the latest for a given asset 
	 * @param The asset to update in the assetsLastestTask map
	 * @param The task to assign as the asset's latest task
	 * @param The Map collection of assets ids and the associate latest task
	 * @return void
	 */
	def assignToAssetsLatestTask( AssetEntity asset, AssetComment task, Map assetsLatestTask ) {
		if (asset) {
			log.debug "assignToAssetsLatestTask: for asset $asset FROM: ${assetsLatestTask[asset?.id] ?: 'First task'} TO: task $task"
			def isNewer = true
			if (assetsLatestTask.containsKey(asset.id)) {

				// Determine if the task spec of the task sitting in the assetsLatestTask is newer than the task that we're 
				// about to attempt. When deferring, order of specs can get screwy and would otherwise mess with the order
				isNewer = assetsLatestTask[asset.id].taskSpec < task.taskSpec

				// Only update previous task to hasSuccessorTaskFlag if we're not updating a peer task pointing back at the same asset
				// which will happen on a gather and there are multiple tasks for the same asset that were deferred
				if (assetsLatestTask[asset.id].id != task.id && isNewer) {
					log.debug "assignToAssetsLatestTask: Updated previous task (${assetsLatestTask[asset.id]}) with hasSuccessorTaskFlag"
					// Mark the previous task as having a successor
					if ( ! assetsLatestTask[asset.id].metaClass.hasProperty(assetsLatestTask[asset.id], 'hasSuccessorTaskFlag') )
						assetsLatestTask[asset.id].metaClass.setProperty('hasSuccessorTaskFlag', true)
				}
			}

			// Assign the new task as the latest for the asset
			if (isNewer) {
				assetsLatestTask[asset.id] = task
				log.debug "assignToAssetsLatestTask: asset $asset last task id is NOW: ${assetsLatestTask[asset.id]}"
			} else {
				log.debug "assignToAssetsLatestTask: previous task was newer LIST: ${assetsLatestTask[asset.id].taskSpec} ARG: ${task.taskSpec}"
			}
		}
	}									
	
	/**
	 * Helper function that manages setting deferment details onto a task as long as the task contains an asset
	 * @param task - the task to set the deferment onto
	 * @param asset - the asset that we're deferring the reference on (AssetEntity, Application, Database, Files, etc)
	 * @param predSucc - a flag indicating if this is a deferment of the predecessor or successor, options are p|s.
	 * @param key - the key used to reference the deferment
	 * @param settings - the hash map containing a bunch of settings used for the task spec, etc
	 * @return void
	 * @throws RuntimeException when used in appropriately
	 */
	// com.tds.asset.AssetComment, com.tds.asset.Application, java.lang.String, java.util.ArrayList, java.util.LinkedHashMap
	private void setDeferment(AssetComment task, Object asset, String predSucc, String key, Map settings) {
		def field = predSucc == 'p' ? 'tmpDefPred' : 'tmpDefSucc'

		assert (asset instanceof AssetEntity)

		if (! asset) {
			throw new RuntimeException("Use of ${predSucc=='p' ? 'predecessor' : 'successor'}.defer only allowed for asset based tasks (Task Spec ${settings.taskSpec.id})")	
		}

		if ( ! task.metaClass.hasProperty(task, field) ) {
			// New deferment so just set it up
			task.metaClass.setProperty(field, [:].withDefault {[]} )
			task[field][key] << asset.id
		} else {

			// See if this key exists on the task as a deferment
			def hasKey = task[field].containsKey(key)
			if (! hasKey || (hasKey && ! task[field][key].contains(asset.id) ) ) {
				task[field][key] << asset.id
			}
		}

		log.debug "setDeferment: key:$key, mode:$predSucc, ${task[field]}, task:$task, asset:$asset"
		// Mark the task as already having a successor so as to prevent it from getting wired to a gateway or milestone
		if (! task.metaClass.hasProperty(task, 'hasSuccessorTaskFlag') )
			task.metaClass.setProperty('hasSuccessorTaskFlag', true)

	}

	/**
	 * Use to find list of all tasks that have outstanding deferments
	 * @param Map The list of all of the present tasks
	 * @param String Used to filter types of deferment if desired. Options include p=predecessor, s=successor, e=either (default)
	 * @return List<Map> containing a map of all of the tasks that have outstanding deferments. The map contains task, type (predecessor|successor), the key, asset id 
	 */
	private List<Map> getOutstandingDeferments(Map taskList, predSuccEither='e') {
		def list = []
		if (['p','e'].contains( predSuccEither ) ) {
			taskList.each { id, task -> 
				if (task.metaClass.hasProperty('tmpDefPred') ) {
					task.tmpDefPred.each { key, assets -> 
						assets.each { asset ->
							list << [task:task, type:'predecessor', key:key, assetId:asset]
						}
					}
				}
			}
		}
		if (['s','e'].contains( predSuccEither ) ) {
			taskList.each { id, task -> 
				if (task.metaClass.hasProperty('tmpDefSucc') ) {
					task.tmpDefPred.each { key, assets -> 
						assets.each { asset ->
							list << [task:task, type:'successor', key:key, assetId:asset]
						}
					}
				}
			}
		}
		return list
	}

	/**
	 * Helper function that gather any dependency deferment which will updates the assetsLatestTask, setting tasks hasSuccessor and removes the deferment tracking
	 * @param The Map collection of all generated tasks
	 * @param The Map collection of the assets with their latest task mapping
	 * @param The task to bind the dependenc(y|ies) to
	 * @param Flag indicating if this is a deferment of the predecessor or successor, options are p|s.
	 * @param An array of one or more keys to gather tasks for
	 * @param The taskSpec property settings
	 * @param The string buffer used for logging to the user
	 * @return void
	 */
	private int gatherDeferment(Map taskList, Map assetsLatestTask, AssetComment task, String predSucc, List keys, Map settings, StringBuffer out) {
		def field = predSucc == 'p' ? 'tmpDefPred' : 'tmpDefSucc'
		int depCount = 0
		def assetId = task.assetEntity?.id

		log.debug "gatherDeferment: Starting - assetId:$assetId, task:$task, predSucc:$predSucc, keys:$keys"

		// First attempt to find all tasks that have the specified deferment by looking for the deferment key and then find those with the source asset
		taskList.each { id, t -> 
			if (t.metaClass.hasProperty(t, field) ) {
				keys.each { key ->
					if ( t[field].containsKey(key) && t[field][key].contains(assetId) ) {
						// Setup the proper dependencies as necessary
						if (predSucc == 'p') {
							log.debug "gatherDeferment: Gathered Predecessor - pred($t), succ($task)"
							depCount += createTaskDependency(task, t, taskList, assetsLatestTask, settings, out)
						} else {
							log.debug "gatherDeferment: Gathered Successors - pred($t), succ($task)"
							depCount += createTaskDependency(t, task, taskList, assetsLatestTask, settings, out)
						}

						// Now remove the asset deferral from the task and if the key is empty, then remove that too
						t[field][key].remove( assetId )
						if (t[field][key].size()==0) {
							t[field].remove(key)
							log.debug "gatherDeferment: emptied deferment for ${field}[$key]"
						}
					}
				}
			} 
		}

		return depCount
	}

	/**
	 * This is used to process the 'groups' section of a recipe and load the assets according to the context that the recipe is to be applied
	 *
	 * @param recipe - the recipe map that contains the groups section
	 * @param contextObject - the context for which the groups will be filtered (MoveEvent, MoveBundle) 
	 * @param cid - the id of the context
	 * @return A map of each group name where each value is a List<AssetEntity, Application, Database, File> based on the filter
	 */
	Map<String,List> fetchGroups(recipe, contextObject, exceptions) {
		def groups = [:]

		if ( ! CU.isaMap(recipe) ) {
			throw new InvalidParamException('The receipe must be of the LinkedHashMap type')
		} 

		// First load up the 'groups' if any are defined 
		if ( recipe.containsKey('groups') ) {
			if (! CU.isaList(recipe.groups)) {
				throw new InvalidParamException('The receipe.groups element must the List type')
			}

			def gCount = 0
			recipe.groups.each { g -> 
				gCount++
				if (! g.name || g.name.size() == 0) {
					msg = "Group specification #${gCount} missing required 'name' property"
					throw new InvalidParamException(msg)					
				}

				if ( contextObject instanceof Application ) {
					// If it is an application then we just stuff it into the list - screw the filtering for now...
					groups[g.name] = [ contextObject ]
				} else {
					if ( g.filter?.containsKey('taskSpec') ) {
						msg = "Group specification (${g.name}) references a taskSpec which is not supported"
						throw new InvalidParamException(msg)
					}

					groups[g.name] = findAllAssetsWithFilter(contextObject, g, groups, exceptions)
					if ( groups[g.name].size() == 0 ) {
						// exceptions.append("Found zero (0) assets for group ${g.name}<br/>")
					} else {
						log.info "Group ${g.name} contains: ${groups[g.name].size()} assets"
					}
				}
			}
		}

		return groups
	}

	/**
	 * This special method is used to find all assets of a moveEvent that match the criteria as defined in the filter map
	 * @param contextObject - the object that the filter is applied to (either MoveEvent or MoveBundle) 
	 * @param Map filter - contains various attributes that define the filtering
	 * @param loadedGroups Map<List> - A mapped list of the groups and associated assets that have already been loaded
	 * @return List<AssetEntity, Application, Database, File> based on the filter
	 * @throws RuntimeException if query fails or there is invalid specifications in the filter criteria
	 */
	def findAllAssetsWithFilter(contextObject, groupOrTaskSpec, loadedGroups, exceptions) {
		def assets = []
		def msg
		def addFilters = true
		def where = ''
		//def project = moveEvent.project
		def map = [:]
		def filter 

		if (! groupOrTaskSpec.containsKey('filter')) {
			throw new RuntimeException("Required 'filter' section was missing from $groupOrTaskSpec")
		} else {
			filter = groupOrTaskSpec.filter
		}

		def filterName = groupOrTaskSpec.containsKey('name') ? groupOrTaskSpec.name : 'Inline Filter'

		log.debug "findAllAssetsWithFilter: ** Starting filter for $filterName" 

		if ( filter?.containsKey('group') ) {
			//
			// HANDLE filter.group
			//
			
			//log.info("Groups contains $groups")
			//log.info("findAllAssetsWithFilter: group ${filter.group}")

			// Put the group property into an array if not already an array
			def groups = CU.asList(filter.group)

			// Iterate over the list of groups	
			groups.each() { groupCode -> 
				if (groupCode.size() == 0) {
					log.error("findAllAssetsWithFilter: 'filter.group' value ($filter.group) has undefined group code.")
					throw new RuntimeException("'filter.group' value ($filter.group) has undefined group code.")
				}
				
				// Find all of the assets of the specified GROUP
				// log.info("assetsLatestTask has ${assetsLatestTask.size()} assets")
				if (loadedGroups.containsKey(groupCode)) {
					assets.addAll( loadedGroups[groupCode] )
					log.debug("findAllAssetsWithFilter: added ${loadedGroups[groupCode].size()} asset(s) for group $groupCode")
				} else {
					log.error("findAllAssetsWithFilter: 'filter.group' value ($filter.group) references undefined group ($groupCode).")
					throw new RuntimeException("'filter.group' value ($filter.group) references undefined group ($groupCode).")
				}
			}
			if (assets.size() == 0) {
				log.debug("findAllAssetsWithFilter: 'filter.taskSpec' group filter found no assets.")
				// throw new RuntimeException("''filter.taskSpec' group filter ($groups) contains no assets.")
			}

			// Indicate if we should append filters if we have a group and asset elements in the taskSpec but only if the group has assets
			addFilters = ( filter.containsKey('asset') && CU.isaMap(filter.asset) && assets.size() > 0 )
			if (addFilters) {
				// Update the WHERE clause to only include the assets in the group
				where = SqlUtil.appendToWhere(where, 'a.id in (:groupAssets)')
				map.put('groupAssets', assets*.id)
			}
			
		} else if (filter?.containsKey('taskSpec')) {
			//
			// HANDLE filter.taskSpec
			//

			log.debug("findAllAssetsWithFilter: taskSpec ${filter.taskSpec}")

			// Put the group property into an array if not already an array
			def filterTaskSpecs = CU.asList(filter.taskSpec)

			// Iterate over the list of groups	
			filterTaskSpecs.each() { ts -> 
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
						log.debug("findAllAssetsWithFilter: 'filter.taskSpec' value ($filter.taskSpec) references taskSpec ($groupCode) that does not contain assets.")
						throw new RuntimeException("'filter.group' value ($filter.taskSpec) references taskSpec ($taskSpec) that does not contain assets.")								
					}
				} else {
					log.debug("findAllAssetsWithFilter: 'filter.taskSpec' value ($filter.taskSpec) references undefined taskSpec.ID ($ts).")
					throw new RuntimeException("'filter.taskSpec' value ($filter.taskSpec) references undefined taskSpec.ID ($ts).")
				}	
			}
			
			if (assets.size() == 0) {
				log.debug("findAllAssetsWithFilter: 'filter.taskSpec' taskSpecs filter found no assets .")
				// throw new RuntimeException("''filter.taskSpec' taskSpecs filter ($filterTaskSpecs) contains no assets.")
			}
			addFilters = false
			
		}

		if (addFilters) {
			// 
			// HANDLE performing an actual filter to find assets
			//

			def sql
			def sm
		
			//
			// HANDLE filter.include - This can be use in conjustion with other filter properties handled below
			//
			if ( filter?.containsKey('include') ) {

				// If the task spec references groups with an Include then we don't need the 'class' specification since it is inherent from the group
				// TODO : extract the class from the groups
				// TODO : Verify that if there are multiple includes, that they are from the same class

				// Put the include property into an array if not already an array
				def includes = CU.asList( filter.include )
				def incIds = []

				// Iterate over the list of groups	
				includes.each() { incGroup -> 
					if (incGroup.size() == 0) {
						msg = "filter.include specified without any group codes ${filter.include}"
						log.warn("findAllAssetsWithFilter: $msg")
						throw new RuntimeException(msg)
					}
					
					// Find all of the assets of the specified GROUP and add their IDs to the list
					if (loadedGroups.containsKey(incGroup)) {
						incIds.addAll( loadedGroups[incGroup]*.id )
						log.debug("findAllAssetsWithFilter: added ${loadedGroups[incGroup].size()} asset(s) for group $incGroup")
					} else {
						msg = "filter.include references undefined group ($incGroup)"
						log.warn("findAllAssetsWithFilter: $msg")
						throw new RuntimeException(msg)
					}
				}
				if (incIds.size() == 0) {
					log.debug("findAllAssetsWithFilter: 'filter.include' found no assets")
					// Just return an empty list of assets
					return assets
				} 
				incIds.unique()

				sm = SqlUtil.whereExpression("a.id", incIds, 'assetToInc')
				if (sm) {
					where = SqlUtil.appendToWhere(where, sm.sql)
					map['assetToInc'] = incIds
				} else {
					msg = "Unable to create SQL for filter.include (${filter.include})"
					log.error "SqlUtil.whereExpression error - $msg - $incIds"
					throw new RuntimeException(msg)
				}
			}			

			def queryOn = filter?.containsKey('class') ? filter['class'].toLowerCase() : 'device'
		
			// Now find the bundles if the contextObject is a MoveEvent otherwise we can just use the existing bundle id
			if (contextObject instanceof MoveEvent) {
				def bundleList = contextObject.moveBundles
				def bundleIds = bundleList*.id

				// See if they are trying to filter on the Bundle Name
				if (filter?.asset?.containsKey('bundle')) {
					def moveBundle = MoveBundle.findByProjectAndName(contextObject.project, filter.asset.bundle)
					if (moveBundle) {
						bundleIds = [ moveBundle.id ]
					} else {
						throw new RuntimeException("Bundle name ($filter.moveBundle) was not found for filter: $filter")
					}
				}

				map.bIds = bundleIds
			} else if (contextObject instanceof MoveBundle) {
				// Pretty simple, we're just searching the current bundle
				map.bIds = contextObject.id
			} else {
				throw new InvalidParamException("The context for findAllAssetsWithFilter must be a MoveBundle or MoveEvent ${contextObject.getClass().getName()}")
			}
			// log.info "bundleIds=[$bundleIds]"
			
			/** 
			 * A helper closure used below to manipulate the 'where' and 'map' variables to add additional
			 * WHERE expressions based on the properties passed in the filter
			 * @param String[] - list of the properties to examine
			 */
			def addWhereConditions = { list ->
				log.debug "addWhereConditions: Building WHERE - list:$list, filter=${filter}"
				list.each() { code ->
					if (filter?.asset?.containsKey(code)) {
						log.debug("addWhereConditions: code $code matched")						
						sm = SqlUtil.whereExpression("a.$code", filter.asset[code], code)
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
		
			// Add WHERE clauses based on the following properties being present in the filter.asset (Common across all Asset Classes)
			addWhereConditions( commonFilterProperties )
		
			//
			// Param 'exclude'
			// Handle exclude filter parameter that will add a NOT IN () cause to the where for references to one or more groups
			//
			if (filter?.containsKey('exclude')) {
				def excludes = []
				def excludeProp = CU.asList(filter.exclude)
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
				log.debug "findAllAssetsWithFilter: excluding group(s) [${filter.exclude}] that has ${excludes.size()} assets"
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
							where = SqlUtil.appendToWhere(where, "IFNULL(a.assetType,'') NOT IN ('application', 'database', 'files', 'virtual', 'vm')")
						} else {
							// All Devices
							where = SqlUtil.appendToWhere(where, "IFNULL(a.assetType,'') NOT IN ('application', 'database', 'files')")
						}
					
						// Add any devices specific attribute filters
						addWhereConditions(['supportType', 'environment', 'department', 'description', 'costCenter', 'maintContract', 'maintExpDate', 'retireDate',
							'truck', 'cart', 'shelf', 'sourceLocation', 'sourceRack', 'sourceRoom', 'targetLocation', 'targetRack', 'targetRoom',
							'os', 'serialNumber', 'assetTag', 'usize', 'ipAddress' ] )

						sql = "from AssetEntity a where a.moveBundle.id in (:bIds)" + ( where ? " and $where" : '')
						log.debug "findAllAssetsWithFilter: DEVICE sql=$sql, map=$map"
						assets = AssetEntity.findAll(sql, map)
						break;
					
					case 'application':
						// Add additional WHERE clauses based on the following properties being present in the filter (Application domain specific)
						addWhereConditions( ['appVendor','sme','sme2','businessUnit','criticality', 'shutdownBy', 'startupBy', 'testingBy'] )
						
						sql = "from Application a where a.moveBundle.id in (:bIds)" + ( where ? " and $where" : '')
						log.debug "findAllAssetsWithFilter: APPLICATION sql=$sql, map=$map"
						assets = Application.findAll(sql, map)
						break;
					
					case 'database':
						// Add additional WHERE clauses based on the following properties being present in the filter (Database domain specific)
						addWhereConditions( ['dbFormat','size'] )
						sql = "from Database a where a.moveBundle.id in (:bIds)" + ( where ? " and $where" : '')
						log.debug "findAllAssetsWithFilter: DATABASE sql=$sql, map=$map"
						assets = Database.findAll(sql, map)
						break;

					case ~/files|file|storage/:
						// Add additional WHERE clauses based on the following properties being present in the filter (Database domain specific)
						addWhereConditions( ['fileFormat','size', 'scale', 'LUN'] )
						sql = "from Files a where a.moveBundle.id in (:bIds)" + ( where ? " and $where" : '')
						log.debug "findAllAssetsWithFilter: FILES sql=$sql, map=$map"
						assets = Files.findAll(sql, map)
						break;
												
					default: 
						log.error "Invalid class '$queryOn' specified in filter ($filter)<br/>"
						throw new RuntimeException("Invalid class '$queryOn' specified in filter ($filter)")
						break;
				} 
			} catch (e) {
				msg = "An unexpected error occurred while trying to locate assets for filter $filter" + e.toString()
				log.error "$msg\n"
				throw new RuntimeException("$msg<br/>${e.getMessage()}")
			}

			//
			// Handle filter.dependency if specified and there were assets found
			//
			// This will instead of returning the assets found above, it will return the assets found through dependencies. It also provides
			// some limited filtering of the dependents.
			//
			if (filter?.containsKey('dependency') && assets.size() ) {
				log.debug "findAllAssetsWithFilter: Processing filter.dependency: master list ${assets*.id}"
				try {
					// Now we need to find assets that are associated via the AssetDependency domain
					def depMode = filter.dependency.mode[0].toLowerCase()
					def daProp='asset'
					if (depMode == 'r') {
						sql = 'from AssetDependency ad where ad.dependent.id in (:assetIds)'
					} else {
						sql = 'from AssetDependency ad where ad.asset.id in (:assetIds)'
						daProp = 'dependent'					
					}
					def depAssets = AssetDependency.findAll(sql, [assetIds:assets*.id])
					def daList = []
					queryOn = filter.dependency.containsKey('class') ? filter.dependency['class'].toLowerCase() : 'device'

					def chkVirtual=false
					def chkPhysical=false
					if (filter.dependency.containsKey('asset')) {
						chkVirtual = filter.dependency.asset.containsKey('virtual')
						chkPhysical = filter.dependency.asset.containsKey('physical')
					}

					depAssets.each { da -> 
						def asset = da[daProp]
						def dependent = null
						// Verify that the asset is in the move bundle id list
						// log.debug "findAllAssetsWithFilter: examining asset $asset"
						if ( map.bIds.contains( asset.moveBundle.id ) ) {
							// Now verify the class and attempt to get the asset by it's class type
							switch (queryOn) {
								case 'application':
									if (asset.assetType.toLowerCase() == 'application')
										dependent = Application.get(asset.id)
									break
								case 'database':
									if (asset.assetType.toLowerCase() == 'database')
										dependent = Database.get(asset.id)
									break
								case ~/files|file|storage/:
									if (asset.assetType.toLowerCase() == 'files')
										dependent = Files.get(asset.id)
									break
								case 'device':
									// Make sure that the assets that were found are of the right type
									if (chkVirtual) {
										if ( ['virtual', 'vm'].contains(asset.assetType.toLowerCase() )) 
											dependent = asset
									} else if (chkPhysical) {
										if ( ! ['application', 'database', 'files', 'virtual', 'vm'].contains(asset.assetType.toLowerCase() ))
											dependent = asset
									} else {
										if ( ! ['application', 'database', 'files'].contains(asset.assetType.toLowerCase() ))
											dependent = asset
									}

									break
								default:
									log.error "findAllAssetsWithFilter: Unhandled switch/case for filter.dependency.class='$queryOn'"
									throw new RuntimeException("Unsupported filter.dependency.class '$queryOn' specified in filter ($filter)")
									break
							}

							if (dependent)
								daList << dependent
						}
					}
					assets = daList.unique()
				} catch (e) {
					// We really shouldn't of gotten here so we're going to do a stackdump
					e.printStackTrace()
					log.error "An unexpected error occurred - ${e.getMessage()}"
					throw e
				}
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
	 * @param List<Person> - references of all staff associated with the project
	 * @param Integer recipeId
	 * @param Map taskSpec
	 * @param List? groups
	 * @param StringBuffer exceptions
	 * @return List<AssetComment> the list of tasks that were created
	 */
	def createAssetActionTasks(action, moveEvent, lastTaskNum, whom, projectStaff, recipeId, taskSpec, groups, workflow, exceptions ) {
		def taskList = []
		def loc 			// used for racks
		def msg
		
		// Get all the assets 			
		def assetsForAction = findAllAssetsWithFilter(moveEvent, taskSpec, groups, exceptions)

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
				try {
					task.comment = template.make(map).toString()
				} catch (Exception ex) {
					exceptions.append("Unable to evaluate title ($taskSpec.title) of TaskSpec ${taskSpec.id} with map=$map<br/>")
					task.comment = '** Unable to evaluate title **'
				}
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
						exceptions.append("Unable to find expected assets for $action in TaskSpec(${taskSpec.id})<br/>")
						log.error "$msg on event $moveEvent"
						throw new RuntimeException(msg)
					}
					// log.info "Added ${assocAssets.size()} assets as predecessors to task $task"
					// assocAssets.each() { log.info "Asset $it" }
				}
				task.metaClass.setProperty('associatedAssets', assocAssets)

				// Perform the AssignedTo logic
				msg = assignWhomAndTeamToTask(task, taskSpec, workflow, projectStaff)
				if (msg) 
					exceptions.append("$msg<br/>")

				// Set any scheduling constraints on the task
				setTaskConstraints(task, taskSpec, workflow, projectStaff, exceptions) 

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
	private def createRollcallTasks( moveEvent, lastTaskNum, whom, recipeId, taskSpec ) {
		
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
	

	/**
	 * Used to apply the assignment of the team, person and fixed assignment from the task specification
	 *
	 * 1. This logic will look for the team, whom and whomFixed parameters from the taskSpec and do the assignment accordingly.
	 * 2. The taskSpec.whom property can have several different formats that represent different types of references which are:
	 *    #propertyName - will look up the in person in the asset property accordingly (e.g. #sme2 will look to the sme2 field for the person)
	 *    contains @ - the value will be used to lookup the person associated to the project by their email address (case insensitive)
	 *    Otherwise - will lookup the person by their name whom are associated to the project
	 * 3. References to #propertyName can cause double indirection if the property contains a second #propertyNam
	 * 4. The team property will be set if present in the task spec, which will override the workflow team if workflow is also provided.
	 * 5. In the event that the whom or team are provided and are not found then an error message will be returned
	 *
	 * @param The task that an individual and/or a team will be assigned to based on the task spec
	 * @param The recipe task specification
	 * @param The workflow object associated with the taskSpec
	 * @param The list of staff associated with the project
	 * @return Return null if successfor or a String error message indicating the cause of the failure
	 */
	private String assignWhomAndTeamToTask(AssetComment task, Map taskSpec, workflow, List projectStaff ) {
		def msg
		def person

		while (true) {
			// Set the Team independently of the direct person assignment
			if (taskSpec.containsKey('team')) {
				// Validate that the string is correct
				if (staffingRoles.contains(taskSpec.team)) {
					task.role = taskSpec.team
				} else {
					msg = "Invalid team specified (${taskSpec.team})"
					break
				}
			} else if (workflow && workflow.role_id) {
				task.role = workflow.role_id
			}

			if (taskSpec.containsKey('whom') && taskSpec.whom.size() > 1 ) {
				def whom = taskSpec.whom

				log.debug "assignWhomToTask() whom=$whom, task $task"

				// whom can have one of the three following values
				//    Persons' name (e.g. Banks, Robin J. )
				//    Persons' email (e.g. robin.banks@example.com )
				//    Indirect reference to other asset property (e.g. #testingBy)

				// See if we have an indirect reference and if so, we will lookup the reference value that will result in either a person's name or @TEAM
				if (whom[0] == '#') {
					// log.debug "assignWhomToTask()  performing indirect lookup whom=$whom, task $task"
					if ( ! task.assetEntity ) {
						msg = "Illegally used whom property reference ($whom) on non-asset"
						break
					}

					try {
						whom = getIndirectPropertyRef(task.assetEntity, whom)
						if (whom instanceof Person) {
							// The indirect lookup returned a person so we don't need to go any further!
							person = whom
							break
						} else if ( whom?.isNumber() ) {
							person = projectStaff.find { it.id == whom.toInteger() }
							if ( ! person ) 
								msg = "Indirect references an invalid person id ($whom) for ${taskSpec.whom}"
							break
						} else if (! whom || whom.size() == 0 ) {
							msg = "Unable to resolve indirect whom reference (${taskSpec.whom})"
							break
						}

						// If we got here, then the indirect either referenced a @team or a 'name', which will be resolved below

					} catch (e) {
						e.printStackTrace()
						msg = "${e.getMessage()}, whom (${taskSpec.whom})"
						break
					}
				}

				if (whom[0] == '@') {
					// team reference
					def teamAssign = whom[1..-1]
					if (staffingRoles.contains(teamAssign)) {
						task.role = teamAssign
					} else {
						msg = "Unknown team (${taskSpec.team}) indirectly referenced"
					}
				} else if (whom.contains('@') ) {

					// See if we can locate the person by email address
					person = projectStaff.find { it.email?.toLowerCase() == whom.toLowerCase() }
					if (! person)
						msg = "Staff referenced by email ($whom) not associated with project"
				} else {

					// Assignment by name
					def map = personService.findPerson(whom, task.project, projectStaff)
					if (! map.person ) {
						msg = "Staff referenced by name ($whom) not found"
					} else if ( map.isAmbiguous ) {
						msg = "Staff referenced by name ($whom) was ambiguous"
					} else {
						person = map.person
					}
				}
			}
			break
		}

		// See if the above code ran into any errors
		if (msg != null) {
			msg += " for task #${task.taskNumber} ${task.comment}, taskSpec (${taskSpec.id})"
			log.warn "assignWhomToTask() $msg, project ${task.project.id}"
		} else {
			if (person) {
				task.assignedTo = person
				// Set the fixed/hard assignment appropriately if a person was assigned
				if (taskSpec.containsKey('whomFixed') && taskSpec.whomFixed == true)
					task.hardAssigned = 1
			}
		}

		return msg
	}


	/**
	 * Helper method lookup indirect property references that will recurse once if necessary
	 * This supports two situations:
	 *    1) taskSpec whom:'#prop' and asset.prop contains name/email
	 *    2) taskSpec whom:'#prop' and asset.prop contains #prop2 reference (indirect reference)
	 * @param AssetComment 
	 * @param String propName
	 * @return String - the string (name or email) from the referenced or indirect referenced property
	 * @throws RuntimeException if a reference is made to an invalid fieldname
	 */
	private Object getIndirectPropertyRef( asset, propertyRef, depth=0) {
		log.info "getIndirectPropertyRef() property=$propertyRef, depth=$depth"

		def value
		def property = propertyRef	// Want to hold onto the original value for the exception message
		if (property[0] == '#') {
			// strip off the #
			property = property[1..-1]
		}	

		// Deal with propery name inconsistency
		def crossRef = [ sme1:'sme', sme2:'sme2', owner:'appOwner' ]

		if ( crossRef.containsKey( property.toLowerCase() ) ) {
			property = crossRef.getAt( property.toLowerCase() )
		}

		// Check to make sure that the asset has the field referenced
		if (! asset.metaClass.hasProperty( asset.getClass(), property) ) {
			throw new RuntimeException("Invalid property name ($propertyRef) used in name lookup in asset $asset")
		}

		// TODO : Need to see if we can eliminate the multiple if statements by determining the asset type upfront
		def type = GrailsClassUtils.getPropertyType(asset.getClass(), property)?.getName()
		if (type == 'java.lang.String') {			
			// Check to see if we're referencing a person object vs a string
			log.debug "getIndirectPropertyRef() $property of type $type has value (${asset[property]})"
			if ( asset[property]?.size() && asset[property][0] == '#' ) {
				if (++depth < 3)  {
					value = getIndirectPropertyRef( asset, asset[property], depth)
				} else {
					throw new RuntimeException("Multiple nested indirection unsupported (${property}..${asset[property][0]}) of asset ($asset), depth=$depth")
				}
			} else {
				value = asset[property]
			}
		} else {
			log.debug "getIndirectPropertyRef() indirect references property $property of type $type"
			value = asset[property]
		}

		return value
	}

	/**
	 * Helper closure method used to add missing dependency details onto the stack to wire up tasks later on
	 * @param Map Array - the list used to track all of the missed dependencies
	 * @param String key - the lookup key (missedPredKey - assetId_category)
	 * @param Map Array - the TaskSpec used to create the task(s)
	 * @param AssetComment task - the task that failed the dependency lookup
	 * @param Boolean isRequired - the flag if the dependency was required in the taskspec
	 * @param AssetComment latestMilestoneTask - the current last milestone at the time that the smdTask was created
	 * @param AssetDependency dependency - the dependency record that was used to link assets together (not always present)
	 */
	private def saveMissingDep = { missedDepList, key, taskSpec, task, isRequired, latestMilestoneTask, dependency=null ->
		missedDepList[key].add( [ taskId:task.id, isRequired:isRequired, msTaskId:latestMilestoneTask?.id , dependency:dependency ] )
		log.info "saveMissingDep() Added missing predecessor to stack ($key) for task $task. Now have ${missedDepList[key].size()} missed predecessors"
	}

	/** 
	 * Helper method to set any scheduling constraints on a task
	 * @param task
	 * @param taskSpec
	 * @param 
	 * @return ? 
	 */
	private void setTaskConstraints(AssetComment task, Map taskSpec, workflow, List projectStaff, StringBuffer exceptions) {
		def ctype 
		def cdt
		def dateTime 

		// Check to see if we have a type
		if (taskSpec.containsKey('constraintType')) {
			ctype = TimeConstraintType.asEnum(taskSpec.constraintType)
			if (! ctype) {
				exceptions.append("TaskSpec ${taskSpec.id} constraintType contains invalid value, must be one of ${TimeConstraintType.getKeys()}<br/>")
				return
			}
		}

		// Check for an actual time and parse it if so
		if (taskSpec.containsKey('constraintTime')) {
			cdt = taskSpec.constraintTime
			if (cdt?.size()) {
				if (cdt[0] == '#') {
					if (task.assetEntity) {
						// Get indirect reference to where the DateTime value is stored in the asset
						cdt = getIndirectPropertyRef(task.assetEntity, cdt)						
					} else {
						exceptions.append("TaskSpec ${taskSpec.id} can not use indirect reference ($cdt) for constraintTime of non-asset type task spec<br/>")
						return
					}
				} 

				// Let's try to convert the text into a date time
				if (cdt?.size()) {
					dateTime = TimeUtil.parseDateTime(cdt)
					if (! dateTime) {
						exceptions.append("TaskSpec ${taskSpec.id} for task ($task.taskNumber) has unparsable date ($cdt)<br/>")
						return
					}
				}
			}
		}

		task.constraintType = ctype
		switch (ctype) {
			case TimeConstraintType.FNLT:
			case TimeConstraintType.MSO:
			case TimeConstraintType.SNLT:
				task.constraintTime = dateTime
		}

	}

	/**
	 * This special method is used to find given event task summary ( taskCounts, tatalDuratin, tasks by status)
	 * @param MoveEvent moveEvent object
	 * @param loadedGroups Map<List> - A mapped list of the taskCounts, tatalDuratin, tasks by status
	 */
	def getMoveEventTaskSummary(def moveEvent){
		
		def taskStatusMap =[:]
		def totalDuration=0
		def taskCountByEvent = 0
		
		if (moveEvent) {
			taskCountByEvent = AssetComment.countByMoveEventAndIsPublished(moveEvent, true)
			def durationScale = [d:1440, m:1, w:10080, h:60] // minutes per day,week,hour
			AssetCommentStatus.topStatusList.each{ status->
				def duration = AssetComment.findAllByMoveEventAndStatus(moveEvent, status)
				def timeInMin=duration.sum{d->
					d.isPublished ? d.duration*durationScale[d.durationScale] : 0
				}
				def countTasks = duration.sum { d ->
					d.isPublished ? 1 : 0
				}
				countTasks = countTasks ?: 0
				taskStatusMap << [ (status): [taskCount: countTasks, timeInMin: timeInMin] ]  
				if(timeInMin)
					totalDuration +=timeInMin
			}
		}
		
		return [ taskCountByEvent:taskCountByEvent, taskStatusMap:taskStatusMap, totalDuration:totalDuration]
	}
	/**
	 * This special method is used to find given event task summary ( teamTaskCounts, teamTaskDoneCount)
	 * @param MoveEvent moveEvent object
	 * @return loadedGroups Map<List> - A mapped list of the taskCounts, teamTaskDoneCount.
	 */
	def getMoveEventTeamTaskSummary(def moveEvent){
		def teamTaskMap =[:]
		if (moveEvent) {
			def roles= getTeamRolesForTasks()
			roles.each { role ->
				def teamTask = AssetComment.findAllByMoveEventAndRole(moveEvent, role.id)
				teamTask = teamTask.findAll{ t -> t.isPublished }
				def teamDoneTask = teamTask.findAll { it.status == 'Completed' }
				if(teamTask){
					teamTaskMap << [(role.id): [teamTaskCount:teamTask.size(), teamDoneCount:teamDoneTask.size(), role:role ]]
				}
			}
		}
		return teamTaskMap
	}

	/**
	 * Publishes the asset comment for a specific task
	 * 
	 * @param taskId the task id
	 * @param loginUser the current user
	 * @param currentProject the current project
	 * @return the number of affected tasks
	 */
	def publish(taskId, loginUser, currentProject) {
		return this.basicPublish(taskId, loginUser, currentProject, true, "PublishTasks")
	}

	/**
	 * Unpublishes the asset comment for a specific task
	 * 
	 * @param taskId the task id
	 * @param loginUser the current user
	 * @param currentProject the current project
	 * @return the number of affected tasks
	 */
	def unpublish(taskId, loginUser, currentProject) {
		return this.basicPublish(taskId, loginUser, currentProject, false, "UnpublishTasks")
	}

	/**
	 * Publishes/Unpublishes the asset comment for a specific task
	 * 
	 * @param taskId the task id
	 * @param loginUser the current user
	 * @param currentProject the current project
	 * @param shouldPublish if it should publish or unpublish
	 * @param permission the requested permission
	 * @return the number of affected tasks
	 */
	private def basicPublish(taskId, loginUser, currentProject, shouldPublish, permission) {
		if (!RolePermissions.hasPermission(permission)) {
			throw new UnauthorizedException("User doesn't have a " + permission + " permission")
		}
		
		if (taskId == null || !taskId.isNumber() || currentProject == null) {
			throw new EmptyResultException();
		}
		
		def task = TaskBatch.get(taskId)

		if (task == null) {
			throw new EmptyResultException();
		}
		
		//TODO check access task, currentProject
		log.info((task.isPublished == shouldPublish))
		if (task.isPublished == shouldPublish) {
			throw new IllegalArgumentException('The task is already in that state')
		}
		
		
		def affectedComments = namedParameterJdbcTemplate.update('UPDATE asset_comment SET is_published = :shouldPublish WHERE task_batch_id = :taskId', ['taskId': taskId, 'shouldPublish' : shouldPublish])
		
		task.isPublished = shouldPublish
		task.save(failOnError: true)
		
		return affectedComments
	}
	
	/**
	 * Deletes the batch whose id is taskId
	 *
	 * @param taskId the task id
	 * @param loginUser the current user
	 * @param currentProject the current project
	 */
	def deleteBatch(taskId, loginUser, currentProject) {
		if (!RolePermissions.hasPermission('DeleteTaskBatch')) {
			throw new UnauthorizedException('User does not have a DeleteTaskBatch permission')
		}
		
		if (taskId == null || !taskId.isNumber() || currentProject == null) {
			throw new EmptyResultException();
		}
		
		def task = TaskBatch.get(taskId)

		if (task == null) {
			throw new EmptyResultException();
		}
		
		if (task.recipeVersionUsed != null && !task.recipeVersionUsed.recipe.project.equals(currentProject)) {
			throw new UnauthorizedException('User is trying to delete a batch whose project that is not the current ' + task.recipeVersionUsed.recipe.project.id + ' currentProject ' + currentProject.id)
		}
		
		namedParameterJdbcTemplate.update('DELETE FROM tdstm.asset_comment WHERE task_batch_id = :taskId', ['taskId' : taskId])
		task.delete(failOnError: true)
	}
	
	
	/**
	 * Used to lookup a TaskBatch by the Context and Recipe regardless of the recipe version
	 * 
	 * @param contextId - the record id number of the context that the TaskBatch was generated for
	 * @param recipeId - the record id of the recipe used to generate the TaskBatch
	 * @param includeLogs - whether to include the logs information or not
	 * @param loginUser - the current user
	 * @param currentProject - the current project
	 * @return A taskBatch map if found or null
	 */
	def findTaskBatchByRecipeAndContext(recipeId, contextId, includeLogs, loginUser, currentProject) {
		if (currentProject == null) {
			throw new EmptyResultException('No project selected');
		}
		
		if (recipeId == null || !recipeId.isNumber()) {
			throw new EmptyResultException('Invalid recipeId');
		}
		def recipe = Recipe.get(recipeId.toInteger())
		if (recipe == null) {
			throw new EmptyResultException('Recipe doesn\'t exists');
		}
		if (!recipe.project.equals(currentProject)) {
			throw new IllegalArgumentException('The current project and the Move event project doesn\'t match')
		}
		
		includeLogs = includeLogs == null ? false : includeLogs.toBoolean()
		
		try {
			def taskBatch = namedParameterJdbcTemplate.queryForMap("select * from task_batch inner join recipe_version on task_batch.recipe_version_used_id = recipe_version.recipe_version_id inner join person on task_batch.created_by_id = person.person_id where recipe_version.recipe_id = :recipeId AND task_batch.context_id = :contextId", ['recipeId' : Long.valueOf(recipeId), 'contextId' : contextId.toInteger()])
			def result = [
				'id': taskBatch.task_batch_id,
				'contextType': taskBatch.context_type,
				'contextId': taskBatch.context_id,
				'recipeVersionUsed': taskBatch.recipe_version_id,
				'status': taskBatch.status,
				'taskCount': taskBatch.task_count,
				'exceptionCount': taskBatch.exception_count,
				'createdBy': taskBatch.first_name + " " + taskBatch.last_name,
				'dateCreated': taskBatch.date_created,
				'lastUpdated': taskBatch.last_updated]
			
			if (includeLogs) {
				result['exceptionLog'] = taskBatch.exceptionLog
				result['infoLog'] = taskBatch.infoLog
			}
			
			return result
		} catch (IncorrectResultSizeDataAccessException e) {
			return null
		}
	}
	
	/**
	 * List the TaskBatchs for a specific recipeId
	 *
	 * @param recipeId - the record id of the recipe
	 * @param limitDays - the number of days to limit the search
	 * @param loginUser - the current user
	 * @param currentProject - the current project
	 * @return the list of Task batches
	 */
	def listTaskBatches(recipeId, limitDays, loginUser, currentProject) {
		if (currentProject == null) {
			throw new EmptyResultException('No project selected');
		}
		
		if (recipeId == null || !recipeId.isNumber()) {
			throw new EmptyResultException('Invalid recipeId');
		}
		def recipe = Recipe.get(recipeId.toInteger())
		if (recipe == null) {
			throw new EmptyResultException('Recipe doesn\'t exists');
		}
		if (!recipe.project.equals(currentProject)) {
			throw new IllegalArgumentException('The current project and the Move event project doesn\'t match')
		}
		
		if (limitDays == null || !limitDays.isNumber()) {
			throw new IllegalArgumentException('Not a valid limitDays')
		}
		
		def startCreationDate = new Date()
		startCreationDate = startCreationDate - limitDays.toInteger()
		log.info("Start date" + startCreationDate)
		
		def c = TaskBatch.createCriteria()
		def queryResults = c.list {
			createAlias('recipeVersionUsed', 'rv')
			
			ge("dateCreated", startCreationDate)
			eq("rv.recipe", recipe)
		}
		
		def result = []
		for (TaskBatch taskBatch in queryResults) {
			result.add([
			'id': taskBatch.id,
			'contextName' : taskBatch.contextName(),
			'taskCount': taskBatch.taskCount,
			'exceptionCount': taskBatch.exceptionCount,
			'createdBy': taskBatch.createdBy?.firstName + " " + taskBatch.createdBy?.lastName,
			'dateCreated': taskBatch.dateCreated,
			'status': taskBatch.status,
			'versionNumber' : taskBatch.recipeVersionUsed.versionNumber,
			'isPublished' : taskBatch.isPublished])
		}

 		return result
	}
	
	
	/**
	 * Returns the task batch using the taskBatchId
	 *
	 * @param taskBatchId - the id of the task batch
	 * @param loginUser - the current user
	 * @param currentProject - the current project
	 * @return the task batch
	 */
	def getTaskBatch(taskBatchId, loginUser, currentProject) {
		if (currentProject == null) {
			throw new EmptyResultException('No project selected');
		}
		
		if (taskBatchId == null || !taskBatchId.isNumber()) {
			throw new EmptyResultException('Invalid recipeId');
		}
		TaskBatch taskBatch = TaskBatch.get(taskBatchId.toInteger())
		if (taskBatch == null) {
			throw new EmptyResultException('TaskBatch doesn\'t exists');
		}
		if (!taskBatch.recipeVersionUsed.recipe.project.equals(currentProject)) {
			throw new IllegalArgumentException('The current project and the Task batch project doesn\'t match')
		}
		
		return [
			'id': taskBatch.id,
			'contextName' : taskBatch.contextName(),
			'taskCount': taskBatch.taskCount,
			'exceptionCount': taskBatch.exceptionCount,
			'createdBy': taskBatch.createdBy?.firstName + " " + taskBatch.createdBy?.lastName,
			'dateCreated': taskBatch.dateCreated,
			'status': taskBatch.status,
			'versionNumber' : taskBatch.recipeVersionUsed.versionNumber,
			'isPublished' : taskBatch.isPublished,
			'exceptionLog' : taskBatch.exceptionLog,
			'infoLog' : taskBatch.infoLog,
			];
	}
}

