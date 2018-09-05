package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.CommentNote
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.TaskDependency
import com.tdsops.common.exceptions.RecipeException
import com.tdsops.common.exceptions.TaskCompletionException
import com.tdsops.common.lang.CollectionUtils as CU
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.lang.GStringEval
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.domain.RecipeHelper
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentCategory as ACC
import com.tdsops.tm.enums.domain.AssetCommentPropertyEnum
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentStatus as ACS
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.RoleTypeGroup
import com.tdsops.tm.enums.domain.TimeConstraintType
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.text.GStringTemplateEngine as Engine
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.command.task.TaskGenerationCommand
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventStaff
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Recipe
import net.transitionmanager.domain.RecipeVersion
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.domain.TaskBatch
import net.transitionmanager.domain.WorkflowTransition
import net.transitionmanager.security.Permission
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils
// import org.codehaus.groovy.grails.web.json.JSONObject
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.dao.CannotAcquireLockException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.TransactionStatus

import java.text.DateFormat

import static com.tdsops.tm.enums.domain.AssetDependencyStatus.ARCHIVED
import static com.tdsops.tm.enums.domain.AssetDependencyStatus.NA
import static com.tdsops.tm.enums.domain.AssetDependencyType.BATCH
/**
 * Methods useful for working with Task related domain (a.k.a. AssetComment). Eventually we should migrate
 * away from using AssetComment to persist our task functionality.
 *
 * @author John Martin
 */
@Transactional
@Slf4j
class TaskService implements ServiceMethods {

	ApiActionService apiActionService
	ControllerService controllerService
	CookbookService cookbookService
	JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	def partyRelationshipService
	def personService
	def progressService
	Scheduler quartzScheduler
	def sequenceService
	CustomDomainService customDomainService

	private static final List<String> runbookCategories = [ACC.MOVEDAY, ACC.SHUTDOWN, ACC.PHYSICAL, ACC.STARTUP].asImmutable()
	private static final List<String> categoryList = ACC.list
	private static final List<String> statusList = ACS.list

	private static final List<String> ACTIONABLE_STATUSES = [ACS.READY, ACS.STARTED, ACS.COMPLETED]

	// The RoleTypes for Staff (populated in init())
	private static List staffingRoles

	private static final List<String> deviceFilterProperties = [
		'cart',
		'costCenter',
		'ipAddress',
		'maintContract',
		'maintExpDate',
		'os',
		'retireDate',
		'shelf',
		'serialNumber',
		'truck',
		'usize'
	].asImmutable()
	// Note that devide fields: sourceLocation, sourceRoom, sourceRack, targetLocation, targetRoom and targetRack
	// are handled separately in the code that references deviceFilterProperties so are not in the list.

	private static final List<String> coreFilterProperties = [
		'assetClass',
		'assetName',
		'assetTag',
		'costCenter',
		'department',
	    'description',
		'environment',
		'externalRefId',
		'id',
		'planStatus',
	    'priority',
		'supportType',
		'validation'
	].asImmutable()

	// The common asset properties that filtering can be applied to (populated in init())
	private static List<String> commonFilterProperties

	void init() {
		// called in Bootstrap so GORM is available
		commonFilterProperties = (coreFilterProperties +
				(1..Project.CUSTOM_FIELD_COUNT).collect { 'custom' + it }).asImmutable()
	}

	/**
	 * The getUserTasks method is used to retreive the user's TODO and ALL tasks lists or the count results of the lists. The list results are based
	 * on the Person and Project.
	 * @param project	 the project to get tasks for
	 * @param countOnly  when true will only return the counts of the tasks otherwise method returns list of tasks
	 * @param limitHistory	when set will limit the done tasks completed in the N previous days specificed by param
	 * @param search	when provided will provided will limit the results to just the AssetEntity.AssetTag
	 * @param sortOn	when provided will sort the task lists by the specified column (limited to present list of columns), default sort on score
	 * @param sortOrder  sort options [asc|desc], default [desc]
	 * @param moveEvent the MoveEvent that these tasks belong. If null it will include all.
	 * @return keys are all and todo. Values will contain the task lists or counts based on countOnly flag
	 */
	Map<String, Object> getUserTasks(Project project, boolean countOnly = false, int limitHistory = 7, String sortOn = null,
	                 String sortOrder = null, String search = null, MoveEvent moveEvent = null) {
		// log.info 'getUserTasks: limitHistory={}, sortOn={}, sortOrder={}, search={}', limitHistory, sortOn sortOrder, search

		long personId = securityService.currentPersonId
		// Get the user's functions (PKA roles) for the current project
		def roles = partyRelationshipService.getProjectStaffFunctions(project.id, personId)?.id

		def type = AssetCommentType.TASK

		def now = TimeUtil.nowGMT()
		def minAgo = TimeUtil.adjustSeconds(now, -60)
		// log.info "getUserTasks: now=$now, minAgo=$minAgo"

		// List of statuses that user should be able to see in when soft assigned to others and user has proper role
		def statuses = [ACS.PENDING, ACS.READY, ACS.STARTED, ACS.COMPLETED, ACS.HOLD]

		Map<String, Object> queryArgs = [projectId: project.id, assignedToId: personId, type: type,
		                                 roles: roles, statuses: statuses]

		//log.error "person:personId"
		// Find all Tasks assigned to the person OR assigned to a role that is not hard assigned (to someone else)

		StringBuilder sql = new StringBuilder("""
			SELECT t.asset_comment_id AS id,
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
			a.asset_class as assetClass,
			p.first_name AS firstName, p.last_name AS lastName,
			t.hard_assigned AS hardAssigned,
			t.duration AS duration,
			t.duration_scale AS durationScale,
			t.category,
			t.instructions_link AS instructionsLink,
			t.api_action_id AS apiActionId,
			t.api_action_invoked_at AS apiActionInvokedAt,
			t.api_action_completed_at AS apiActionCompletedAt
			""")

		// Add in the Sort Scoring Algorithm into the SQL if we're going to return a list
		if (!countOnly) {
			queryArgs.now = now
			queryArgs.minAgo = minAgo

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
			// TODO : JPM 11/2015 : The DONE calculation below needs to change due to tickets TM-4249 and TM-4250
			// This calculation is completely wrong.
			sql.append(""",
				((CASE t.status
				WHEN '$ACS.HOLD' THEN 900
				WHEN '$ACS.COMPLETED' THEN IF(t.status_updated >= :minAgo, 800, 200) + UNIX_TIMESTAMP(t.status_updated) / UNIX_TIMESTAMP(:now)
				WHEN '$ACS.STARTED' THEN 700 + 1 - UNIX_TIMESTAMP(IFNULL(t.est_start,:now)) / UNIX_TIMESTAMP(:now)
				WHEN '$ACS.READY' THEN 600 + 1 - UNIX_TIMESTAMP(IFNULL(t.est_start,:now)) / UNIX_TIMESTAMP(:now)
				WHEN '$ACS.PENDING' THEN 500 + 1 - UNIX_TIMESTAMP(IFNULL(t.est_start,:now)) / UNIX_TIMESTAMP(:now)
				ELSE 0
				END) +
				IF(t.assigned_to_id=:assignedToId AND t.status IN('$ACS.STARTED','$ACS.READY'), IF(t.hard_assigned=1, 55, 50), 0) +
				IF(t.assigned_to_id=:assignedToId AND t.status='$ACS.COMPLETED',50, 0) +
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

		// filter tasks to those directly assigned to the user and/or assigned to a team that they're assigne and the task is in one of the proper statuses
		sql.append("AND(t.assigned_to_id=:assignedToId OR \
			(${roles ? 't.role IN (:roles) AND ' : 'false AND '} t.status IN (:statuses) \
			AND t.hard_assigned=0 OR (t.hard_assigned=1 AND t.assigned_to_id=:assignedToId))) ")

		search = StringUtils.trimToNull(search)
		if (search) {
			// Join on the AssetEntity and embed the search criteria
			sql.append('AND a.asset_tag=:search ')
			// Add the search param to the sql params
			queryArgs.search = search
		}

		// Add filter for limitHistory
		if (limitHistory) {
			if ((limitHistory instanceof Integer) && limitHistory >= 0) {
				sql.append("AND (t.date_resolved IS NULL OR t.date_resolved >= SUBDATE(NOW(), $limitHistory)) ")
			} else {
				log.warn "getUserTasks: invalid parameter value for limitHistory ($limitHistory)"
			}
		}

		if (moveEvent) {
			if (moveEvent.project.id == project.id) {
				sql.append("AND t.move_event_id = $moveEvent.id ")
			} else {
				log.warn "getUserTasks: trying to use a move event not belonging to the project"
			}
		}

		if (!countOnly) {
			// If we are returning the lists, then let's deal with the sorting
			sql.append('ORDER BY ')
			def sortableProps = ['number_comment', 'comment', 'estFinish', 'lastUpdated', 'status', 'assetName', 'assignedTo', 'score']
			def sortAndOrder
			if (sortOn) {
				if (sortableProps.contains(sortOn)) {
					sortOrder = ['asc','desc'].contains(sortOrder) ? sortOrder : 'asc'
					sortOrder = sortOrder.toUpperCase()
					switch (sortOn) {
						case 'assignedTo':
							sortAndOrder = "p.first_name $sortOrder, p.last_name $sortOrder"; break
						case  'number_comment':
							sortAndOrder = "taskNumber $sortOrder, comment $sortOrder"; break
						default:
							sortAndOrder = "$sortOn $sortOrder"
					}
					sql.append(sortAndOrder)
				} else {
					log.warn "getUserTasks: called with invalid sort property [$sortOn]"
					sortAndOrder=null
				}
			}
			// add the score sort either as addition or as only ORDER BY parameteters
			sql.append(sortAndOrder ? ', ' : '').append 'score DESC, task_number ASC'
		}

		// log.debug "getUserTasks: SQL: $sql"
		// log.debug "getUserTasks: SQL query args: $queryArgs"

		// Get all tasks from the database and then filter out the TODOs based on a filtering
		def allTasks = namedParameterJdbcTemplate.queryForList(sql.toString(), queryArgs)

		// def allTasks = jdbcTemplate.queryForList(sql.toString(), queryArgs)
		def format = "yyyy/MM/dd hh:mm:ss"
		def minAgoFormat = minAgo.format(format)
		def todoTasks = allTasks.findAll { task ->
			task.status == ACS.READY ||
			(task.status == ACS.HOLD && task.apiActionId != null) ||
			(task.status == ACS.STARTED && task.assignedTo == personId) ||
			(task.status == ACS.COMPLETED && task.assignedTo == personId && task.statusUpdated?.format(format) >= minAgoFormat)
		}

		def assignedTasks = allTasks.findAll { task ->
			(task.status == ACS.READY && task.assignedTo == personId) || (task.status == ACS.STARTED && task.assignedTo == personId) ||
			(task.status == ACS.COMPLETED && task.assignedTo == personId && task.statusUpdated?.format(format) >= minAgoFormat)
		}

		if (countOnly) {
			[all: allTasks.size(), todo: todoTasks.size()]
		}
		else {
			[all: allTasks, todo: todoTasks, user: assignedTasks]
		}
	}

	def getUserTasksOriginal(person, project, search=null, sortOn='c.dueDate', sortOrder='ASC, c.lastUpdated DESC') {

		// TODO : Runbook: getUserTasks - should get the user's project roles instead of global roles

		// List of statuses that user should be able to see in when soft assigned to others and user has proper role

		Map<String, Object> queryArgs = [project: project, assignedTo: person, type: AssetCommentType.TASK,
		                                 roles: securityService.getPersonRoles(person, RoleTypeGroup.STAFF),
		                                 statuses: [ACS.PLANNED, ACS.PENDING, ACS.READY, ACS.STARTED]*.toString()]

		//log.error "person:$person.id"
		// Find all Tasks assigned to the person OR assigned to a role that is not hard assigned (to someone else)

		search = StringUtils.trimToNull(search)

		StringBuilder hql = new StringBuilder('FROM AssetComment AS c')
		if (search) {
			// Join on the AssetEntity and embed the search criteria
			hql.append(', AssetEntity AS a WHERE c.assetEntity.id=a.id AND a.assetTag=:search AND ')
			// Add the search param to the hql params
			queryArgs.search = search
		} else {
			hql.append(' WHERE ')
		}
		hql.append('c.project=:project AND c.commentType=:type ')
		// TODO : runbook : my tasks should only show mine.	 All should show mine and anything that my teams has started or complete
		hql.append('AND (c.assignedTo=:assignedTo OR (c.role IN (:roles) AND c.status IN (:statuses) AND c.hardAssigned != 1)) ')

		// TODO : Security : getUserTasks - sortOn/sortOrder should be defined instead of allowing user to INJECT, also shouldn't the column name have the 'a.' prefix?
		// Add the ORDER to the HQL
		// hql.append("ORDER BY $sortOn $sortOrder")

		// log.error "HQL for userTasks: $hql"

		List<AssetComment> allTasks = AssetComment.executeQuery(hql.toString(), queryArgs)
		List<AssetComment> todoTasks = allTasks.findAll { [AssetCommentStatus.READY, AssetCommentStatus.STARTED].contains(it.status) }

		[all: allTasks, todo: todoTasks]
	}

	/**
	 * Used by routes to update task status/state
	 * @param whom - the Person that is performing the update (e.g. Automatic Task)
	 * @param params - a map containing the values that come from the JSON
	 * @return AssetComement	task that was updated by method
	 */
	/*
	void updateTaskState(Person whom, Map params) {
		Map statusMap = ['running': ACS.STARTED, 'success': ACS.DONE, 'error': ACS.HOLD]
		String status = statusMap[params.status]
		if (!status) {
			throw new InvalidRequestException("Invalid status ${params.status}, valid values are ${statusMap.keySet()}")
		}

		boolean isPM = false
		AssetComment task = AssetComment.get(params.taskId)

		if (!task) {
			throw new InvalidRequestException("Task id ${params.taskId} not found")
		}

		if (status == ACS.STARTED && task.)
		return setTaskStatus(task, status, whom, isPM)
	}
	*/

	/**
	 * Overloaded version of the setTaskStatus that has passes the logged in user's person object to the main method
	 * @param task
	 * @param status
	 * @return AssetComement	task that was updated by method
	 */
	AssetComment setTaskStatus(AssetComment task, String status) {
		def currentPerson = securityService.getUserLoginPerson()
		boolean isPM = partyRelationshipService.staffHasFunction(task.project, currentPerson.id, 'PROJ_MGR')
		return setTaskStatus(task, status, currentPerson, isPM)
	}

	/**
	 * Used to invoke an action on the task which will attempt to do so. If the function fails then it will
	 * plan to set the status to HOLD and add a note to the task.
	 * @param task - the Task to invoke the method on
	 * @return The status that the task should be set to
	 */
	@NotTransactional
	String invokeAction(AssetComment task, Person whom) {
		def status = task.status
		//return status
		// For tasks that are actionable and has an assigned action, this logic will attempt to invoke and
		// update the task accordingly.
		// Actions that are synchronous (future) will fire the action immediately and the task is marked DONE
		// if successful, otherwise for asynchronous actions the status will be marked STARTED.
		if (task.hasAction() && ACTIONABLE_STATUSES.contains(status)) {
			if (task.isActionInvocable()) {
				String errMsg

				if (task.apiAction.isSync()) {
					log.error "invokeAction() task has synchronous action that is not supported (taskId=${task.id})"
					errMsg = 'Task has an synchronous action which are not supported'
				} else {
					try {
						markTaskStarted(task.id)

						log.debug "Attempting to invoke action ${task.apiAction.name} for task.id=${task.id}"

						// Need to refresh the task with changes that were just committed in separate transaction
						AssetComment.withTransaction { TransactionStatus ts ->
							task.refresh()
							status = task.status

							// Kick of the async method and mark the task STARTED
							addNote(task, whom, "Invoked action ${task.apiAction.name}")

							apiActionService.invoke(task.apiAction, task)
						}
					} catch (InvalidRequestException e) {
						errMsg = e.getMessage()
					} catch (InvalidConfigurationException e) {
						errMsg = e.getMessage()
					} catch (CannotAcquireLockException e) {
						errMsg = e.getMessage()
					} catch (e) {
						errMsg = 'A runtime error occurred while attempting to process the action'
						log.error ExceptionUtil.stackTraceToString('invokeAction() failed ', e)
					}
				}

				if (errMsg) {
					log.warn "invokeAction() error $errMsg"
					addNote(task, whom, "Invoke action ${task.apiAction.name} failed : $errMsg")
				}
			}
		}
		return status
	}

	/**
	 * Try to mark a task as started by locking it before, if lock fails
	 * or if the task was already started by another user, thows an exception
	 * indicating it so
	 * @param id - the id of the task to mark as started
	 */
	@NotTransactional
	void markTaskStarted(Long id) {
		try {
			AssetComment.withTransaction { TransactionStatus ts ->
				log.debug "Attempting to mark asset comment as started for task.id={}", id

				def taskWithLock = AssetComment.lock(id)
				log.debug 'Locked out AssetComment: {}', taskWithLock

				if (taskWithLock.status in [ACS.STARTED, ACS.HOLD, ACS.COMPLETED]) {
					throw new Exception('Another user invoked the action before')
				}

				// Update the task so that the we track that the action was invoked
				taskWithLock.apiActionInvokedAt = new Date()

				// Update the task so that we track the task started at
				taskWithLock.actStart = taskWithLock.apiActionInvokedAt

				// Make sure that the status is STARTED instead
				taskWithLock.status = AssetCommentStatus.STARTED
				taskWithLock.save(flush: true, failOnError: true)
			}
		} catch (Exception e) {
			log.error ExceptionUtil.stackTraceToString('markAssetCommentAsStarted() failed ', e)
			throw new CannotAcquireLockException('Another user invoked the action before')
		}
	}

	/**
	 * Reset an action so it can be invoked again
	 * @param task
	 * @param whom
	 * @return
	 */
	String resetAction(AssetComment task, Person whom) {
		String status = task.status
		if (task.hasAction() && !task.isAutomatic() && status == AssetCommentStatus.HOLD) {
			String errMsg
			try {
				// Update the task so it can be invoked again
				task.apiActionInvokedAt = null
				task.apiActionCompletedAt = null
				task.actStart = null
				task.dateResolved = null

				// Log a note that the API Action was reset
				addNote(task, whom, "Reset action ${task.apiAction.name}")

				// Make sure that the status is READY instead
				status = AssetCommentStatus.READY
				task.status = status

			} catch (InvalidRequestException e) {
				errMsg = e.getMessage()
			} catch (InvalidConfigurationException e) {
				errMsg = e.getMessage()
			} catch (e) {
				errMsg = 'A runtime error occurred while attempting to process the action'
				log.error ExceptionUtil.stackTraceToString('resetAction() failed ', e)
			}

			if (errMsg) {
				log.info "resetAction() error $errMsg"
				addNote(task, whom, "Reset action ${task.apiAction.name} failed : $errMsg")
				status = ACS.HOLD
				task.status = status
			}
		}
		return status
	}

	/**
	 * Set the status of the task, which will perform additional updated based on the state.
	 * @param whom  the Person updating the task
	 * @param task  the task (AssetComment)
	 * @param status  the status code (AssetCommentStatus)
	 * @return AssetComment the task object that was updated
	 */
	// TODO : We should probably refactor this into the AssetComment domain class as setStatus
	AssetComment setTaskStatus(AssetComment task, String status, Person whom, boolean isPM=false) {

		// If the current task.status or the persisted value equals the new status, then there's nutt'n to do.
		if (task.status == status || task.getPersistentValue('status') == status) {
			return task
		}

		Date now = new Date()

		// First thing to do, set the status
		task.status = status

		// Update the time that the status has changed which is used to identify tasks that have not been acted on appropriately
		task.statusUpdated = now

		def previousStatus = task.getPersistentValue('status')
		// Determine if the status is being reverted (e.g. going from DONE to READY)
		boolean revertStatus = compareStatus(previousStatus, status) > 0

		log.info "setTaskStatus() task(#:$task.taskNumber Id:$task.id) status=$status, previousStatus=$previousStatus, revertStatus=$revertStatus - $whom"

		// Override the whom if this is an automated task being completed
		if (task.isAutomatic() && status == ACS.COMPLETED) {
			whom = getAutomaticPerson()
		}

		if (task.isAutomatic() && status == ACS.READY) {
			if (ACTIONABLE_STATUSES.contains(status) ) {
				// Attempt to invoke the task action if an ApiAction is set. Depending on the
				// Action excution method (sync vs async), if async the status will be changed to
				// STARTED instead of the default to DONE.
				status = invokeAction(task, whom)
			}
		}

		// Setting of AssignedTO:
		//
		// We are going to update the AssignedTo when the task is marked Started or Completed unless the current user has the
		// PROJ_MGR role because we want to allow for the PM to mark a task as being started or completed on behalf of someone else. The only
		// time we'll set the PM to the AssignedTo property is if it is presently unassigned.
		//
		// Setting Status Backwards (e.g. COMPLETED back to READY):
		//
		// In the rare case that we need to set the status back from a progressive state, we may need to undue some stuff (e.g. mark unresolved, clear
		// resolvedBy, etc).  We will log a note on the task whenever this occurs.
		//
		if (revertStatus) {
			if (previousStatus == ACS.COMPLETED) {
				task.resolvedBy = null
				task.actFinish = null
			}
			// Clear the actual Start if we're moving back before STARTED
			if (compareStatus(ACS.STARTED, status) > 0) {
				if (previousStatus!= ACS.HOLD) {
					task.actStart = null
				} else if (!task.actStart){
					task.actStart = now
				}
			}

			if (task.isRunbookTask() && previousStatus) {
				addNote(task, whom, "Reverted status from '$previousStatus' to '$status'")
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
			case ACS.PENDING:
				// Clear out the ApiAction tracking of invocations for testing
				task.apiActionInvokedAt = null
				task.apiActionCompletedAt = null
				break

			case ACS.HOLD:
				addNote(task, whom, "Placed task on HOLD, previous state was '$previousStatus'")
				break

			case ACS.STARTED:
				task.assignedTo = assignee
				// We don't want to loose the original started time if we are reverting from COMPLETED to STARTED
				if (! revertStatus) {
					task.actStart = now
					addNote(task, whom, "Task was Started")
				}
				break

			case ACS.COMPLETED:
				// If the task being changed to the DONE state then the system should check all successors
				// to make them ready appropriately.
				if (task.isDirty('status') && task.getPersistentValue('status') != status) {
					triggerUpdateTaskSuccessors(task.id, status, whom, isPM)
				}
				task.assignedTo = assignee
				task.resolvedBy = assignee
				task.actFinish = now
				addNote(task, whom, "Task was Completed")
				break

			case ACS.TERMINATED:
				task.resolvedBy = assignee
				task.actFinish = now
				addNote(task, whom, "Task was Terminated")
				break
		}

		task.save(flush: true)
		return task
	}

	/**
	* Triggers the invocation of the UpdateTaskSuccessorsJob Quartz job for the specified task id
	* @param taskId
	* @param String the status that the task is/has been set to
	* @param Person the person that is invoking this method (optional) if not passed it will find user via securityService
	* @param Boolean flag if the whom person is a Project Manager (optional)
	*/
	def triggerUpdateTaskSuccessors(taskId, status, Person whom=null, isPM=false) {
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
				whom = securityService.userLoginPerson
				isPM = securityService.hasRole("PROJ_MGR")
			}
			long startTime = System.currentTimeMillis() + (2000L)
			Trigger trigger = new SimpleTriggerImpl('tm-updateTaskSuccessors-' + taskId + System.currentTimeMillis(), null, new Date(startTime))
			trigger.jobDataMap.taskId = taskId
			trigger.jobDataMap.whomId = whom.id
			trigger.jobDataMap.status = status
			trigger.jobDataMap.isPM = isPM
			trigger.jobDataMap.tries = 0
			trigger.setJobName('UpdateTaskSuccessorsJob')
			trigger.setJobGroup('tdstm-task-update')

			def result = quartzScheduler.scheduleJob(trigger)
			log.info "triggerUpdateTaskSuccessors: scheduled job for task(#:$task.taskNumber Id:$taskId), status=$status, scheduled=$result - $whom"
		}
	}

	/**
	 * Used to determine the CSS class name that should be used when presenting a task, which is based on the task's status
	 * @param status
	 * @return String The appropriate CSS style or task_na if the status is invalid
	 */
	String getCssClassForStatus(status) {
		ACS.list.contains(status) ? 'task_' + status.toLowerCase() : 'task_na'
	}

	/**
	 * Used to determine the CSS class name that should be used inside rack to show relevant status images according to there status
	 * @param status
	 * @return String The appropriate CSS style or task_na if the status is invalid
	 */
	String getCssClassForRackStatus(String status) {
		ACS.list.contains(status) ? 'rack_task_' + status.toLowerCase() : 'task_na'
	}

	/**
	 * Returns the HTML for a SELECT control that contains list of tasks based on a TaskDependency.
	 * This logic assumes that the TaskDependency(ies) pre-exist.
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
		def predecessor = name == "predecessorEdit" ? taskDependency.predecessor : taskDependency.assetComment
		def moveEvent = task.moveEvent
		def category = predecessor.category
		def paramsMap = [selectId: idPrefix + '_' + taskDependency.id, selectName: name, options:[predecessor],
		                 optionKey: "id", optionSelected: predecessor.id,
		                 javascript:"onmouseover=\'generateDepSel($task.id, $taskDependency.id, \"$category\", \"$predecessor.id\", \"$idPrefix\", \"$name\")\'"]
		def selectControl = HtmlUtil.generateSelect(paramsMap)
		//sw.stop()
		//log.info "genSelectForTaskDependency - Stopwatch: ${sw.prettyPrint()}"
		return selectControl
	}

	/**
	 * Returns a list of tasks paginated and filtered
	 * @param project - the project object to filter tasks to include
	 * @param taskToIgnore - an optional task Id that the filtering will use to eliminate as an option and also filter on it's moveEvent
	 * @param moveEventId - an optionel move event to filter on
	 * @param page - page to load
	 * @param pageSize - page size to use
	 * @param searchText - an optional filter to search by either task id or comment
	 * @return
	 */
	def search(Project project, AssetComment taskToIgnore, Long moveEventId, Long page=-1, Long pageSize=50, String searchText=null) {

		StringBuilder queryList = new StringBuilder("FROM AssetComment a ")
		StringBuilder queryCount = new StringBuilder("SELECT count(*) FROM AssetComment a ")
		StringBuilder query = new StringBuilder("WHERE a.project.id = :projectId AND a.commentType = :commentType ")
		Map params = [projectId: project.id, commentType: AssetCommentType.TASK]

		if (searchText) {
			query.append("AND ( ")

			if ( NumberUtils.isDigits(searchText) ) {
				query.append("cast(a.taskNumber as string) like :searchText OR ")
			}

			query.append("a.comment like :searchText )")
			params["searchText"] = "%" + searchText + "%"
		}

		// If there is a task we can add some additional filtering like not including self in the list of predecessors and filtering on moveEvent
		if (taskToIgnore) {
			query.append("AND a.id != :taskId ")
			params["taskId"] = taskToIgnore.id

			// If moveEventId param is given use it as filter, if not then user taskToIgnore.moveEvent (if exists).
			if (moveEventId) {
				query.append("AND a.moveEvent.id = :moveEventId ")
				params["moveEventId"] = moveEventId
			} else if (taskToIgnore.moveEvent) {
				query.append("AND a.moveEvent.id = :moveEventId ")
				params["moveEventId"] = taskToIgnore.moveEvent.id
			}
		} else {
			if (moveEventId) {
				query.append("AND a.moveEvent.id = :moveEventId ")
				params["moveEventId"] = moveEventId
			} else {
				query.append("AND a.moveEvent is null ")
			}
		}

		// execute count query
		queryCount.append(query)
		def resultTotal = AssetComment.executeQuery(queryCount.toString(), params)

		// Add the sort and generate the list
		query.append('ORDER BY a.taskNumber ASC')

		// execute list query
		Map paginationArgs = (page == -1 ? [:] : [max: pageSize, offset: ((page - 1) * pageSize)])
		queryList.append(query)
		def list = AssetComment.executeQuery(queryList.toString(), params, paginationArgs)

		return [list: list, total: resultTotal[0]]
	}

	/**
	 * Calculate the index for the task in the task selection drop-down.
	 *
	 * @param project
	 * @param category
	 * @param task
	 * @param moveEventId
	 * @param taskId
	 * @return 0 if the task doesn't exist, the index if it does.
	 */
	int searchTaskIndexForTask(project, category, task, moveEventId, taskId) {

		// If no task id was given, return 0.
		if (!taskId) {
			return 0
		}
		def taskIndex = 0

		StringBuilder query = new StringBuilder("""
			SELECT rownum FROM (
				SELECT asset_comment_id, task_number,@rownum:=@rownum+1 as rownum
				FROM asset_comment ac, (SELECT @rownum:=0) r
				WHERE ac.project_id=$project.id AND ac.comment_type='$AssetCommentType.TASK'
		""")

		if (category) {
			if (categoryList.contains(category)) {
				query.append("AND ac.category='$category' ")
			} else {
				log.warn "unexpected category filter '$category'"
				category=''
			}
		}

		// If there is a task we can add some additional filtering like not including self in the list of predecessors and filtering on moveEvent
		if (task) {
			if (! category && task.category) {
				query.append("AND ac.category='$task.category' ")
			}
			query.append("AND ac.asset_comment_id != $task.id ")

			if (task.moveEvent) {
				query.append("AND ac.move_event_id=$task.moveEvent.id ")
			}
		} else {
			if (moveEventId) {
				query.append("AND ac.move_event_id=$moveEventId ")
			} else {
				query.append("AND ac.move_event_id is null ")
			}
		}

		// Add the sort and generate the list
		query.append("""
				ORDER BY ac.task_number ASC
			) tasks_rows
			WHERE tasks_rows.asset_comment_id = $taskId
		""")

		def tasksInfo = jdbcTemplate.queryForList(query.toString())

		if (tasksInfo[0] != null) {
			taskIndex = tasksInfo[0]['rownum']
		}

		return taskIndex
	}

	/**
	 * Returns true to warn status is overriding or not
	 * @param  task  the task
	 * @return statusWarn as can
	 */
	boolean canChangeStatus (AssetComment task) {
		// TODO : runbook - add logic to allow PM to change status anytime.
		![ACC.SHUTDOWN, ACC.PHYSICAL, ACC.STARTUP].contains(task.category) ||
		 [ACS.READY, ACS.STARTED].contains(task.status)
	}

	/**
	 * Comparies two statuses and returns -1 if 1st is before 2nd, 0 if equal, or 1 if 1st is after the 2nd
	 * @param from	status moving from
	 * @param to	status moving to
	 */
	private int compareStatus(String from, String to) {
		if (!from && to) return 1
		if (from && !to) return -1

		int fidx = statusList.findIndexOf { it == from }
		int tidx = statusList.findIndexOf { it == to }

		// TODO - need to solve issue when the status from or to is unknown.

		return fidx < tidx ? -1 : fidx == tidx ? 0 : 1
	}

	/**
	 * Used to add a note to a task
	 * @param task	The task (aka AssetComment) to add a note to
	 * @param person	The Person object that is creating the note
	 * @param note	A String that represents the note
	 */
	def addNote(AssetComment task, Person person, String note, int isAudit = 1) {
		def taskNote = new CommentNote(createdBy: person, note: note, isAudit: isAudit, assetComment: task)
		taskNote.validate()
		if (taskNote.hasErrors()) {
			log.error "addNote: failed to save note : ${GormUtil.allErrorsString(taskNote)}"
			return false
		}

		task.addToNotes(taskNote)
		true
	}

	/**
	 * Used to add a note to a task by way of using queue / routes
	 * @param message - a Map containing vital information to process the request
	 */
	void addTaskCommentByMessage(Map message) {
		log.info "addTaskCommentByMessage() received message for task id ${message.taskId}"
		AssetComment task = AssetComment.get(message.taskId)
		String comment = message.comment
		if (task) {
			Person whom
			if (message.byWhomId) {
				whom = Person.get(message.byWhomId)
				if (!whom) {
					log.warn "addTaskCommentByMessage() request reference person (${message.byWhomId}) not found for task id ${message.taskId}"
					comment += " : Unable to find specified user id"
				}
			}
			if (!whom) {
				whom = getAutomaticPerson()
			}

			addNote(task, whom, comment, 0)

			boolean isCallback = message.containsKey('callbackMethod')
			if (isCallback && task.isAutomatic()) {
				log.debug "addTaskCommentByMessage() called as callback so going to close out the task"
				// Close out the task
				task.apiActionCompletedAt = new Date()
				setTaskStatus(task, ACS.COMPLETED, whom)
			}

			log.debug "addTaskCommentByMessage() dirtyProps=${task.dirtyPropertyNames}, status=${task.status}, apiActionCompletedAt=${task.apiActionCompletedAt}"
			task.save()
		}
	}

	/**
	 * Used to add a note to a task by way of using queue / routes
	 * @param message - a Map containing vital information to process the request
	 */
	void updateTaskStateByMessage(Map message) {
		log.info "updateTaskStateByMessage() received message for task id ${message.taskId}"
		AssetComment task = AssetComment.get(message.taskId)
		String comment = message.comment
		if (task) {
			Person whom
			if (message.byWhomId) {
				whom = Person.get(message.byWhomId)
				if (!whom) {
					log.warn "updateTaskStateByMessage() request reference person (${message.byWhomId}) not found for task id ${message.taskId}"
					comment += " : Unable to find specified user id"
				}
			}
			if (!whom) {
				whom = getAutomaticPerson()
			}

			String note
			String newStatus = task.status
			String status = message.status ?: 'invalid'

			switch (status) {
				case 'success':
					boolean isCallback = message.containsKey('callbackMethod')
					// <SL>: What is callBack for and what if task is not automatic but status is success?
//					if (isCallback && task.isAutomatic()) {
						task.apiActionCompletedAt = new Date()
						newStatus = ACS.COMPLETED
//					}
					note = 'Task was completed by API notification'
					break

				case 'error':
					newStatus = ACS.HOLD
					note = 'Unable to validate task status due to error: ' + message.cause
					break

				default:
					newStatus = ACS.HOLD
					note = 'Invalid notification format : ' + message.toString()
					break
			}

			addNote(task, whom, note, 0)
			setTaskStatus(task, newStatus, whom)

			log.debug "updateTaskStateByMessage() dirtyProps=${task.dirtyPropertyNames}, status=${task.status}, apiActionCompletedAt=${task.apiActionCompletedAt}"
			task.save()
		}
	}


	/**
	 * Used to clear all Task data that are associated to a specified event
	 * @param moveEventId
	 */
	def resetTaskData(MoveEvent moveEvent) {
		try {
			def tasksMap = getMoveEventTaskLists(moveEvent.id)
			return resetTaskDataForTasks(tasksMap)
		} catch(e) {
			log.error "An error occurred while trying to Reset tasks for moveEvent $moveEvent on project $moveEvent.project\n$e"
			throw new RuntimeException("An unexpected error occured")
		}
	}

	/**
	 * Used to clear all Task data that are associated to a task batch
	 * @param moveEventId
	 */
	def resetTaskDataForTaskBatch(TaskBatch taskBatch) {
		try {
			def tasksMap = getTaskBatchTaskLists(taskBatch.id)
			return resetTaskDataForTasks(tasksMap)
		} catch(e) {
			log.error "An error occurred while trying to Reset tasks for taskBatch $taskBatch on project $taskBatch.project\n$e"
			throw new RuntimeException("An unexpected error occured")
		}
	}

	def resetTaskDataForTasks(def tasksMap) {
		// We want to find all AssetComment records that are associate with assets that are
		// with bundles that are part of the event. With that list, we will reset the
		// AssetComment status to PENDING by default or READY if there are no predecessors and clear several other properties.
		// We will also delete Task notes that are auto generated during the runbook execution.
		def msg
		log.info("resetTaskData() was called")
		try {
			int taskResetCnt = 0
			int notesDeleted = 0

			String updateHql = '''
				UPDATE AssetComment
				SET status = :status, actStart = null, actStart = null, dateResolved = null,
				    resolvedBy = null, statusUpdated = null
				WHERE id in (:ids)'''

			if (tasksMap.tasksWithPred) {
				taskResetCnt = AssetComment.executeUpdate(updateHql, [status: ACS.PENDING, ids: tasksMap.tasksWithPred])
			}
			if (tasksMap.tasksNoPred) {
				taskResetCnt += AssetComment.executeUpdate(updateHql, [status: ACS.READY, ids: tasksMap.tasksNoPred])
			}
			if (tasksMap.tasksWithNotes) {
				// Delete any of the audit comments that are created during the event
				notesDeleted = CommentNote.executeUpdate("DELETE FROM CommentNote cn WHERE cn.assetComment.id IN (:ids) AND cn.isAudit=1",
					[ids: tasksMap.tasksWithNotes])
			}

			msg = "$taskResetCnt tasks reset and $notesDeleted audit notes were deleted"
		} catch(e) {
			log.error "An error occurred while trying to Reset tasks\n$e"
			throw new RuntimeException("An unexpected error occurred")
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
		return getTaskListsFor(moveEventId, "move_event_id", manualTasks)
	}

	/**
	 * Returns a map containing several lists of AssetComment ids for a specified taskBatch that include keys
	 * tasksWithPred, tasksNoPred, TasksAll and TasksWithNotes
	 * @param taskBatchId
	 * @param manualTasks - boolean flag if manually created tasks should be included in list (default true)
	 * @return map of tasksWithPred, tasksNoPred, TasksAll and TasksWithNotes
	 */
	def getTaskBatchTaskLists(def taskBatchId, def manualTasks = true) {
		return getTaskListsFor(taskBatchId, "task_batch_id", manualTasks)
	}

	/**
	 * Returns a map containing several lists of AssetComment ids for a specified field that include keys
	 * tasksWithPred, tasksNoPred, TasksAll and TasksWithNotes
	 * @param keyId the id of the field
	 * @param field the name of the field
	 * @param manualTasks - boolean flag if manually created tasks should be included in list (default true)
	 * @return map of tasksWithPred, tasksNoPred, TasksAll and TasksWithNotes
	 */
	def getTaskListsFor(keyId, String field, boolean manualTasks = true) {
		String manTasksSQL = manualTasks ? '' : ' AND c.auto_generated = true'
		String query = """SELECT c.asset_comment_id AS id,
				(SELECT count(*) FROM task_dependency t WHERE t.asset_comment_id = c.asset_comment_id) AS predCount,
				(SELECT count(*) FROM comment_note n WHERE n.asset_comment_id = c.asset_comment_id) AS noteCount
			FROM asset_comment c
			WHERE
				c.$field = $keyId AND
				c.category IN (${GormUtil.asQuoteCommaDelimitedString(runbookCategories)}) """ + manTasksSQL
		log.debug "getMoveEventTaskLists: query = $query"
		def tasksList = jdbcTemplate.queryForList(query)

		List<Long> tasksWithPred = []
		List<Long> tasksNoPred = []
		List<Long> tasksWithNotes = []
		List<Long> tasksAll = []

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

		[tasksAll: tasksAll, tasksWithPred: tasksWithPred, tasksNoPred: tasksNoPred, tasksWithNotes: tasksWithNotes]
	}

	List<Map<String, Object>> getTasksOfBatch(String taskBatchId) {

		DateFormat formatter = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)

		AssetComment.findAllByTaskBatch(TaskBatch.load(taskBatchId)).collect { assetComment -> [
			id: assetComment.taskNumber,
			commentId: assetComment.id,
			description: assetComment.comment,
			asset: assetComment.assetEntity?.assetName,
			team: assetComment.role ?: '',
			person: (assetComment.assignedTo ?: '').toString(),
			dueDate: TimeUtil.formatDateTime(assetComment.dueDate, formatter),
			status: assetComment.status,
		]}
	}

	 /**
	  * Provides a list of upstream task predecessors that have not been completed for a given task. Implemented by recursion
	  * @param task
	  * @param taskList list of predecessors collected during the recursive lookup
	  * @return list of predecessor tasks
	  */
	 // TODO: runbook : Refactor getIncompletePredecessors() into the TaskService,
	List<AssetComment> getIncompletePredecessors(AssetComment task, List<AssetComment> taskList = []) {
		 task.taskDependencies?.each { TaskDependency dependency ->
			 AssetComment predecessor = dependency.predecessor

			 // Check to see if we already have the predecessor in the list where there were multiple predecessors that referenced one another
			 boolean skip = false
			 for (int i=0; i < taskList.size(); i++) {
				 if (taskList[i].id == predecessor.id) {
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
	  * complete predecessor tasks if completePredecessors = true.
	  * @param task
	  * @return
	  */
	 def completeTask(taskId, userId, boolean completePredecessors=false) {
		 log.info "completeTask: taskId:$taskId, userId:$userId"
		 def task = AssetComment.get(taskId)
		 def tasksToComplete = [task]

		 List<AssetComment> predecessors = getIncompletePredecessors(task)

		 // If we're not going to automatically complete predecessors (Supervisor role only), the we can't do anything
		 // if there are any incomplete predecessors.
		 if (!completePredecessors && predecessors) {
			throw new TaskCompletionException("Unable to complete task [$task.taskNumber] due to incomplete predecessor task # (${predecessors[0].taskNumber})")
		 }

		 // If automatically completing predecessors, check first to see if there are any on hold and stop
		 if (completePredecessors) {
			 // TODO : Runbook - if/when we want to complete all predecessors, perhaps we should do it recursive since this logic only goes one level I believe.
			 if (predecessors) {
				 // Scan the list to see if any of the predecessor tasks are on hold because we don't want to do anything if that is the case
				 boolean hasHold = predecessors.any { it.status == ACS.HOLD }
				 if (hasHold) {
					 throw new TaskCompletionException("Unable to complete task [$task.taskNumber] due to predecessor task # (${predecessors[0].taskNumber}) being on Hold")
				 }
			 }

			 tasksToComplete += predecessors
		 }

		// Complete the task(s)
		tasksToComplete.each { activeTask ->
			// activeTask.dateResolved = TimeUtil.nowGMT()
			// activeTask.resolvedBy = userLogin.person
			setTaskStatus(activeTask, ACS.COMPLETED)
			if (!save(activeTask)) {
				throw new TaskCompletionException("Unable to complete task # $activeTask.taskNumber due to " +
					GormUtil.allErrorsString(activeTask))
			}
		}

		//
		// Now Mark any successors as Ready if all predecessors are Completed
		//
		def succDependencies = TaskDependency.findByPredecessor(task)
		succDependencies?.each { succDepend ->
			def successorTask = succDepend.assetComment

			// If the Task is in the Planned or Pending state, we can check to see if it makes sense to set to READY
			if ([ACS.PLANNED, ACS.PENDING].contains(successorTask.status)) {
				// Find all predecessors for the successor, other than the current task, and make sure they are completed
				List<TaskDependency> predDependencies = TaskDependency.findByAssetCommentAndPredecessorNot(successorTask, task)
				def makeReady = true
				for (TaskDependency predDependency in predDependencies) {
					def predTask = predDependency.assetEntity  // TODO BB
					if (predTask.status != ACS.COMPLETED) makeReady = false
				}
				if (makeReady) {
					successorTask.status = ACS.READY
					if (!save(successorTask)) {
						throw new TaskCompletionException("Unable to release task # $successorTask.taskNumber due to " +
							 GormUtil.allErrorsString(successorTask))
					 }
				}
			}
		}
	 } // def completeTask()

	/**
	 * Used by the several actions to get the roles that is starts with of 'staff'
	 * @param blank
	 * @return list of roles that is only starts with 'staff'
	 */
	def getRolesForStaff() {
		partyRelationshipService.getStaffingRoles(false)
	}

	/**
	 * Get the team rolls that can be assigned to tasks. This is similar to that of Roles for Staff but also includes
	 * Automated.
	 */
	def getTeamRolesForTasks() {
		partyRelationshipService.getStaffingRoles()
	}

	/**
	 * Generates HTML for a given type with list of dependencies
	 * @param depTasks, list of successors or dependencies
	 * @param task, task for which dependencies table generating
	 * @param dependency, for which table generating for
	 * @return
	 */
	def genTableHtmlForDependencies(depTasks, task, String dependency){
		def html = new StringBuilder("""<table id="${dependency}EditTableId" cellspacing="0" style="border:0px;width:0px"><tbody>""")
		def optionList = AssetComment.constraints.category.inList.toList()
		def i=1
		depTasks.each { depTask ->
			def succecessor = dependency == 'predecessor' ? depTask.predecessor : depTask.assetComment
			def paramsMap = [selectId: 'predecessorCategoryEditId_' + depTask.id, selectName:'category',
				options:optionList, optionSelected:succecessor.category,
				javascript:"onChange=\'fillPredecessor(this.id,this.value,$task.id,\"${dependency}Edit\")\'"]
			def selectCategory = HtmlUtil.generateSelect(paramsMap)
			def selectPred = genSelectForTaskDependency(depTask, task, dependency + 'EditId', dependency + 'Edit')
			html.append("""<tr id="row_Edit_$depTask.id"><td>""")
			html.append(selectCategory)
			html.append("""</td><td id="taskDependencyEditTdId_$depTask.id">""")
			html.append(selectPred)
			html << """</td><td><a href="javascript:deletePredRow('row_Edit_$depTask.id')">""" <<
					'''<span class="clear_filter"><u>X</u></span></a></td>'''
		}
		return html
	}

   /**
	 * Used to calculate the dial indicator speed that reflects how well the move is going for a given set of datetimes
	 */
	def calcStepDialIndicator (planStartTime, planCompTime, actualStartTime, actFinish, int tasksCount, int tasksCompleted) {

		// TODO - calcStepDialIndicator() - need to further refine this method and test
		return 0

		// timeAsOf = timeAsOf.getTime() / 1000
		def timeAsOf = TimeUtil.nowGMT().getTime() / 1000  // Remove the millisec

		// def planCompletionTime = (stepSnapshot.moveBundleStep.planCompletionTime.getTime() / 1000) + 59  	// 59s added to planCompletion to consider the minuits instead of seconds
		planCompTime = (planCompTime.getTime() / 1000) + 59  	// 59s added to planCompletion to consider the minuits instead of seconds
		planStartTime = planStartTime.getTime()  / 1000 + 59

		// log.info "timeAsOf is ${timeAsOf.getClass()}, planCompTime is ${planCompTime.getClass()}, planStartTime is ${planStartTime.getClass()} $planStartTime}"

		def remainingStepTime = timeAsOf > planCompTime ? 0 : planCompTime - timeAsOf
		//def planTaskPace = stepSnapshot.getPlanTaskPace()
		def planDuration = planCompTime - planStartTime
		def planTaskPace = planDuration / (tasksCount == 0 ? 1 : tasksCount)

		def tasksRemaining = tasksCount - tasksCompleted

		def remainingEffort =  tasksRemaining * planTaskPace

		int projectedMinOver
		if(actualStartTime || tasksCompleted > 0){
			projectedMinOver  = remainingEffort - remainingStepTime
		} else {
			projectedMinOver  =  timeAsOf + planDuration
		}
		def adjust

		if (remainingEffort && projectedMinOver > 0) {
			adjust =  -50 * (1-(remainingStepTime / remainingEffort))
		} else {
			adjust =  50 * (1-(remainingEffort / (planCompTime - timeAsOf)))
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

		def projectStaff = partyRelationshipService.getProjectStaff(projectId)

		// Now morph the list into a list of name: Role names
		def list = []
		projectStaff.each {
			String roleDescription = it.role.toString()
			list << [id:it.staff.id,
				nameRole:"${roleDescription}: $it.staff",
				sortOn:"${roleDescription},$it.staff.firstName $it.staff.lastName"
			]
		}
		list.sort { it.sortOn }

		HtmlUtil.generateSelect(selectId: elementId, selectName: elementId, options: list,
			optionKey:'id', optionValue:'nameRole', optionSelected:selectedId,
			firstOption: [value:'', display:'Unassigned'])
	}

	/**
	 * Retrieves the runbook recipe for a specified MoveEvent
	 * @param moveEventId
	 * @return Map containing Tasks[Map] and potentially Resources[Map]
	 */
	def getMoveEventRunbookRecipe(moveEvent) {
		def recipe

		if (moveEvent && moveEvent.runbookRecipe) {
			try {
				recipe = Eval.me("[$moveEvent.runbookRecipe]")
			} catch (e) {
				log.error "There is an error in the runbook recipe for project event $moveEvent.project - $moveEvent\n$e.message"
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
		if (moveEvent) {
			def bundleIds = moveEvent.moveBundles*.id
			// log.info 'bundleIds=[{}] for moveEvent {}', bundleIds, moveEvent.id

			def cartInfo = namedParameterJdbcTemplate.queryForList("""
				SELECT count(*) AS total, SUM(IF(task.status=:status,1,0)) AS done
				FROM asset_entity ae
				JOIN asset_comment task ON task.asset_entity_id=ae.asset_entity_id AND move_event_id=:moveEventId
				WHERE move_bundle_id IN (:moveBundleIds) AND task.role='CLEANER'
				AND ae.cart=:cartName
				ORDER BY cart""",
				[status: ACS.COMPLETED, moveEventId: moveEvent.id, moveBundleIds: bundleIds, cartName: cartName])
			log.info 'moveEvent {}: bundleIds {} : cart {} : info {}', moveEvent.id, bundleIds, cartName, cartInfo
			if (cartInfo) {
				return cartInfo[0]
			}
		}
	}

	/**
	 * Retrieve the Person object that represent the person that completes automated tasks
	 */
	Person getAutomaticPerson() {
		Person a = Person.findByLastNameAndFirstName(Person.SYSTEM_USER_AT.lastName, Person.SYSTEM_USER_AT.firstName)
		if (! a) {
			log.error 'Unable to find Automated Task Person as expected'
		}
		return a
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
	 * @return  - the list of tasks dependencies surrounding the task
	 */
	List<TaskDependency> getNeighborhood(taskId, int blocksLeft = 2, int blocksRight = 2, boolean viewUnpublished = false) {
		def list = []
		def findProp	// Is set to the TaskDependency property name used to find the current nodes (e.g. 'predecessor' when looking for successors)
		def nextProp	// The opposite property name to findProp
		def findCol		// The db column name that is used to find the adjacent task dep count on the outer edges of the neighborhood
		//def nextCol		// The DB column name that represents the findCol property in the domain object
		def depCountName	// The name of the property that will be injected into the edge nodes with the count (predecessorDepCount | successorDepCount)
		boolean isPred = false // Flag for the neighbors closure.

		// A recursive helper method that will traverse each of the neighbors out the # of blocks passed in
		def neighbors
		neighbors = { tId, depth ->
			// log.info "In the hood $depth for $tId"
			if (depth > 0 && (viewUnpublished || AssetComment.read(tId)?.isPublished)) {
				def td = TaskDependency.executeQuery('from TaskDependency td where td.' + findProp + '.id=?', [tId])
				// log.info "Found ${td.size()} going to $predOrSucc"
				if (td.size() > 0) {
					if (depth > 0) {
						td.each { t ->
							// log.info "Neighbor $t.assetComment.id : $t.predecessor"
							neighbors(t[nextProp].id, (depth - 1))
						}
					}

					//
					// Deal with the edge tasks by injecting the outside task dependency counts
					//
					if (depth == 1){
						// On the outer nodes, we want to get the quantity of dependencies on the other side of the tracks for
						// each of them correspondingly. It will invoke a query to get a list so as to limit the number of queries
						// to one per level vs one per node. Therefore it needs to iterate through the results to match up with the
						// corresponding TaskDependency. It will use the meta.setProperty to inject the count appropriately.
						def ids = td[nextProp]*.id
						// isPred = nextProp == 'predecessor'

						def sql = "SELECT $findCol as id, count(*) as cnt FROM task_dependency WHERE $findCol in (" +
							GormUtil.asCommaDelimitedString(ids) + ") group by id"
						log.debug "getNeighborhood: SQL = $sql, ids=$ids"
						def outerDeps = jdbcTemplate.queryForList(sql)
						log.debug "getNeighborhood: found ${outerDeps.size()} tasks on the other side of the tracks of $ids"
						td.each { t ->
							// Try to match up the outer dep count to the task dependency node
							def outerDep = outerDeps?.find() { od -> od.id == t[nextProp].id }
							Integer outerDepCount = outerDep ? outerDep.cnt : 0
							if(isPred){
								t.setTmpPredecessorDepCount(outerDepCount)
							}else{
								t.setTmpSuccessorDepCount(outerDepCount)
							}

							log.debug "getNeighborhood: Found $outerDepCount outer depend for task # ${t[findProp].taskNumber} (id:${t[findProp].id})"
						}
					}

					if (depth==1) {
						td.each { t ->
							//log.info "getNeighborhood: dependency $t - $depCountName = ${t[depCountName]} outside dep"
							log.debug "getNeighborhood: dependency $t"
						}
					}

					// tnp.metaClass.setProperty('chainPeerTask', assetsLatestTask[tnp.assetEntity.id])
					td.each { t ->
						if (viewUnpublished || (t.predecessor?.isPublished && t.assetComment?.isPublished)) {
							list.add(t)
						}
					}
				}
			}
		}

		taskId = taskId.toLong()

		// Get the successors
		findProp = 'predecessor'
		nextProp = 'assetComment'
		findCol = 'predecessor_id'
		depCountName = 'tmpSuccessorDepCount'
		neighbors(taskId, blocksRight)

		// Get the predecessor
		isPred = true
		findProp = 'assetComment'
		nextProp = 'predecessor'
		findCol = 'asset_comment_id'
		depCountName = 'tmpPredecessorDepCount'
		neighbors(taskId, blocksLeft)

		// Reduce the list to just the distinct/unique dependency since the above way to find dependencies can have overlap
		list.unique { a, b -> a.id <=> b.id }

		//log.info "getNeighborhood: found ${list.size()} tasks in the hood"
		// list.each { log.info "dep match: $it.assetComment.id : $it.predecessor.id"}
		return list
	}

	// ===================================================================================================================================================================================
	// Cookbook Related Task methods
	// ===================================================================================================================================================================================

	/**
	 * Used to locate a TaskBatch for a given recipe and context
	 * @param recipe - the recipe used to generate a task batch, regardless of the version
	 * @param event - the event id of the context that was used to generate the task batch
	 * @return the TaskBatch record if found otherwise null
	 */
	List findTaskBatchesForRecipeContext(Recipe recipe, Long eventId) {
		TaskBatch.createCriteria().list {
			eq('eventId', eventId)
			eq('recipe', recipe)
		}
	}

	/**
	 * Used to initiate an async task creation job using a specified recipe for a given context
	 * This is the service method called by the controller to initiate task generation
	 * @param context - the context which can include, MoveEvent, and or tags
	 * @param recipeVersionId - the id number of the RecipeVersion that should be used to generate the tasks
	 * @param deletePrevious - a flag to indicate that we should delete the previously generated tasks for the recipe if any exist
	 * @param useWIP - a flag to indicate if the generation should use the WIP or the current release of the recipe
	 * @param publishTasks - used to indicate if the tasks should be published at the time that they are generate, default=false
	 * @return [jobId:String, taskBatch:Integer]
	 *    jobId - the reference code to the ProgressService job of the batch being generated asynchronously
	 *    taskbatch - the id number of the task batch that was created as part of the process
	 * @throws UnauthorizedException, IllegalArgumentException, EmptyResultException
	 */
	Map initiateCreateTasksWithRecipe(TaskGenerationCommand context, Project currentProject) {
		Long currentProjectId = NumberUtil.toLong(securityService.userCurrentProjectId)
		log.debug "initiateCreateTasksWithRecipe() user=$securityService.currentUsername, project.id=$currentProjectId"

		securityService.requirePermission Permission.RecipeGenerateTasks
		if (context.autoPublish) {
			securityService.requirePermission Permission.TaskPublish
		}

		// Find the Recipe Version that the user selected
		Recipe recipe = get(Recipe, context.recipeId, currentProject)

		def recipeVersion

		// Now check to see if the RecipeVersion that we have is the latest release or if not, are we using WIP?
		if (! context.useWIP) {
			if (recipe.releasedVersion) {
				recipeVersion = recipe.releasedVersion
			} else {
				throw new InvalidRequestException('There is no released version of the recipe to generate tasks with')
			}
		} else {
			recipeVersion = RecipeVersion.findByRecipeAndVersionNumber(recipe, 0)
			if (!recipeVersion) {
				throw new InvalidRequestException('There is no wip version of the recipe to generate tasks with')
			}
		}

		def contextObj = context

		if (! contextObj) {
			throw new IllegalArgumentException('Referenced context was not found')
		}

		if (contextObj.eventId ) {
			MoveEvent event = get(MoveEvent, contextObj.eventId, currentProject)
		}

		def assets = getAssocAssets(contextObj)
		if (!assets) {
			throw new EmptyResultException("The selected event doesn't have associated bundles")
		}

		if(contextObj.eventId) {
			// Delete previous task batches now if the user specified to delete them
			def taskBatches = findTaskBatchesForRecipeContext(recipe, contextObj.eventId)
			if (taskBatches) {
				if (context.deletePrevious) {
					taskBatches*.delete()
				} else {
					throw new RuntimeException('Tasks already exist for this recipe and context')
				}
			}
		}

		TaskBatch tb = createTaskBatch(contextObj, recipe, recipeVersion, context.autoPublish, currentProject)

		String key = taskBatchKey(tb.id)
		progressService.create(key, 'Pending')

		// Kickoff the background job to generate the tasks
		def jobName = "TM-GenerateTasks-${currentProjectId}-${recipe.id}"
		try {
			log.info "initiateCreateTasksWithRecipe : Created taskBatch $tb and about to kickoff job to generate tasks $jobName"
			// Delay 2 seconds to allow this current transaction to commit before firing off the job
			Trigger trigger = new SimpleTriggerImpl(jobName, null, new Date(System.currentTimeMillis() + 500))
			trigger.jobDataMap.taskBatchId = tb.id
			trigger.jobDataMap.publishTasks = context.autoPublish
			trigger.jobDataMap.tries = 0
			trigger.jobDataMap.context = contextObj
			trigger.jobDataMap.project = currentProject
			trigger.setJobName('GenerateTasksJob')
			trigger.setJobGroup('tdstm-generate-tasks')
			quartzScheduler.scheduleJob(trigger)
			log.debug "initiateCreateTasksWithRecipe : dispatched Quartz job"
		} catch (ex) {
			log.error "initiateCreateTasksWithRecipe failed to create Quartz job : $ex.message"
			progressService.update(key, 100I, 'Failed')
			throw new RuntimeException('It appears that someone else is currently generating tasks for this context and recipe.')
		}

		[jobId: key, taskBatch: tb]
	}

	/**
	 * Used to create a task batch
	 * @param contextType - the type of context that the batch is being generated for
	 * @param eventId - the id of the event record
	 * @param recipeVersion - the reference RecipeVersion that will be used to generate the task batch
	 * @param isPublished - a flag that indicates that the tasks generated for the batch have been published
	 * @return the TaskBatch that was created
	 */
	private TaskBatch createTaskBatch(TaskGenerationCommand context, Recipe recipe, RecipeVersion recipeVersion, Boolean isPublished = true, Project project) {
		Map taskBatchContext = [
			eventId : context.eventId,
			tagMatch: context.tagMatch,
			tag     : []
		]

		context.tag.each { Long tagId ->
			Tag tag = get(Tag, tagId, project)

			taskBatchContext.tag << [
				id    : tag.id,
				label : tag.name,
				strike: false,
				css   : tag.color.css
			]
		}

		new TaskBatch(
			project: securityService.loadUserCurrentProject(),
			createdBy: securityService.loadCurrentPerson(),
			recipe: recipe,
			recipeVersionUsed: recipeVersion,
			eventId: context.eventId,
			context: JsonUtil.convertMapToJsonString(taskBatchContext),
			status: "Pending",
			isPublished: isPublished
		).save(failOnError: true)
	}

	/**
	 * Returns the formatted key used to reference a task batch in the ProgressService
	 * @param batchId - the task batch id number
	 * @return the key
	 */
	String taskBatchKey(Long batchId) {
		'TaskBatch-' + batchId
	}

	/**
	 * Used to find assets associated with a given context object. When the context contains one or more tags
	 * then the query will be based solely on Tags and it will find assets associated with ANY of the tags. The event will
	 * not affect the query with Tags. If only an event is specified then the search will be for any asset associated to planning
	 * bundles assigned to the event.
	 * @param context - the context object to find associated assets for
	 * @return list of assets
	 */
	List<AssetEntity> getAssocAssets(Object contextObj) {
		List<AssetEntity> assets = []

		if (contextObj.tag) {
			// Query the TagAsset to get list of Assets with ANY of the tags
			assets = TagAsset.where {
					tag.id in contextObj.getTagIds()
					asset.moveBundle.useForPlanning == true }.projections {
				}.projections {
					property 'asset'
				}.list()
		} else {
			// Legacy get assets by event
			List<Long> bundleIds = getBundleIds(contextObj)

			if (bundleIds) {
				assets = AssetEntity.executeQuery('from AssetEntity WHERE moveBundle.id IN (:bundleIds) AND moveBundle.useForPlanning = true', [bundleIds: bundleIds])
			}
		}

		return assets
	}

	/**
	 * Method used to generate tasks for a given TaskBatch object. This is designed to run asynch an as such will interact with the
	 * ProgressService to update the job with the status.
	 * @param taskBatch - the TaskBatch that contains all of the necessary data needed to generate the tasks
	 */
	void generateTasks(TaskBatch taskBatch, Boolean publishTasks) {
		String progressKey = taskBatchKey(taskBatch.id)
		Boolean errored = false
		def detail = ''

		log.debug "generateTasks(taskBatch:$taskBatch, publishTasks:$publishTasks) called"

		try {
			generateTasks(taskBatch, publishTasks, progressKey)
		} catch (RuntimeException e) {
			errored = true
			detail = e.message
			log.error "$e\n${ExceptionUtil.stackTraceToString(e)}"
		}

		// Mark the job's progress Competed or Failed accordingly
		progressService.update(progressKey, 100I, (errored ? 'Failed' : 'Completed'), detail)
	}

	/**
	 * Method used to generate tasks for a given TaskBatch object. This is designed to run asynch an as such will interact with the
	 * ProgressService to update the job with the status.
	 * @param taskBatch - the TaskBatch that contains all of the necessary data needed to generate the tasks
	 */
	void generateTasks(TaskBatch taskBatch, Boolean publishTasks, String progressKey) {

		log.debug "generateTasks(taskBatch:$taskBatch, publishTasks:$publishTasks, progressKey:$progressKey) called"

		// Mark the job's progress as started
		progressService.update(progressKey, 0I, 'Processing', 'Initializing')

		Project project = taskBatch.project

		// Define a number of vars that will hold cached data about the tasks being generated for easier lookup, dependencies, etc
		Map taskList = [:]				// an Map list (key AssetComment.id) that will contain list of all tasks that are inserted as they are created
		Map taskSpecTasks = [:]			// A map list where the key is the task spec id and value is an array of the tasks created by the task spec
		List<Long> collectionTaskSpecIds = []	// An array containing task spec ids that are determined to be collections (sets, trucks, cart, rack, gateway, milestones, etc)
		def latestMilestone		// Will reference the most recent milestone task
		Map assetsLatestTask = [:]		// This map array will contain reference of the assets' last assigned task
		Map taskSpecList = [:]			// Used to track the ID #s of all of the taskSpecs in the recipe and holds last task created for some special cases
		Map groups = [:]				// Used to hold groups of assets as defined in the groups section of the recipe that can then be reference in taskSpec filters
		List terminalTasks = []			// Maintains the list of general tasks indicated that are terminal so that milestones don't connect to them as successors
		Map missedDepList = [:].withDefault {[]}
										// Used to track missing dependencies for an asset. The key will be the (asset.id_asset.category). The map that will
										// by default contain ArrayList that will be populated with a map including the task and several related objects.
		boolean isAsset = false
		boolean isGeneral = false
		boolean isGroupingSpec = false 		// Flag used to determine if the current taskSpec is a grouping type (e.g. Milestone, Gateway, Set, etc)
		boolean isAction = false
		boolean isRequired = true			// Used to hold the taskSpec.predecessor.required param or default to true
		boolean isInversed = false 		// Used to hold the taskSpec.predecessor.inverse or default to false
		String failure = ''			// Will hold the message if there is an exception
		def lastTaskSpec       		// Holds the last task spec
		def deferPred        		// Gets populated with the taskSpec.predecessor.defer code if defined. It will either be a string if defined or null
		def deferSucc        		// Gets populated with the taskSpec.successor.defer code if defined. It will either be a string if defined or null
		def gatherPred       		// Gets populated with the taskSpec.predecessor.gather code if defined (it becomes an array) if not null
		def gatherSucc       		// Gets populated with the taskSpec.successor.gather setting if defined (it becomes an array) if not null
		def settings = [project: taskBatch.project] // This will get populated with the properties from the TaskSpec for each iteration
		// def waitFor = '' 			// When the predecessor.waitFor is defined then wiring predecessors will wait until a subsequent taskspec with a successor.resumeFor attribute of the same value,
		// def resumeFor = ''			// Used in conjunction with waitFor

		String msg 					// Used to hold temporary messages to be output to logs
		def newTask					// Var to hold newly created tasks

		int depCount = 0
		int specCount = 0

		boolean isDebugEnabled = log.debugEnabled

		// These buffers are used to capture status output for short-term
		StringBuilder out = new StringBuilder()
		StringBuilder exceptions = new StringBuilder()
		Integer exceptCnt = 0

		Date startedAt = new Date()

		/**
		 * Helper closure that is used to fail out of the process and update the taskBatch with the details
		 * @param String - the message/cause as to why we're bailing out of the process
		 */
		def bailOnTheGeneration = { cause ->
			failure = cause
			/*
			// Update the taskBatch
			taskBatch.exceptionLog = exceptions.toString()
			taskBatch.infoLog = out.toString()
			taskBatch.taskCount = taskList.size()
			taskBatch.exceptionCount = ++exceptCnt
			taskBatch.save(flush:true, failOnError:true)
			*/

			log.info(msg)
			throw new RuntimeException(cause)
		}

		Person whom = taskBatch.createdBy

		def contextObj = taskBatch.context()
		def assets = getAssocAssets(contextObj)

		List<Long> bundleIds = []
		if (assets) {
			// Derive the unique bundle ids from the list of assets found
			bundleIds = assets*.moveBundle*.id.unique()
		}

		MoveEvent event = null

		if(contextObj.eventId) {
			event = get(MoveEvent, contextObj.eventId, settings.project)
		}

		settings.event = event

		// Determine the start/completion times for the event
		// TODO : JPM 9/2014 - need to address to support non-Event/Bundle generation
		def eventTimes = event ? event.getEventTimes() : [start:null, completion:null]

		// TODO : Need to change out the categories to support all ???
		def categories = GormUtil.asQuoteCommaDelimitedString(ACC.moveDayCategories)

		// Get the various workflow steps that will be used to populate things like the workflows ids, durations, teams, etc when creating tasks
		def workflowSteps = []
		if (bundleIds.size()) {
			// TODO : JPM 9/2014 - need to address to support non-Event/Bundle generation
			def workflowStepSql = "select mb.move_bundle_id AS moveBundleId, wft.*, mbs.* \
				from move_bundle mb \
				left outer join workflow wf ON wf.process = mb.workflow_code \
				left outer join workflow_transition wft ON wft.workflow_id=wf.workflow_id \
				left outer join move_bundle_step mbs ON mbs.move_bundle_id=mb.move_bundle_id AND mbs.transition_id=wft.trans_id \
				where mb.move_bundle_id IN (${GormUtil.asCommaDelimitedString(bundleIds)})"
			workflowSteps = jdbcTemplate.queryForList(workflowStepSql)
		}

		// log.debug "Workflow steps SQL= $workflowStepSql"
		// log.debug "Found ${workflowSteps.size()} workflow steps for moveEvent $moveEvent"

		/**
		 * A helper closure used by generateRunbook to link a task to its predecessors by asset or milestone
		 * @param AssetComment (aka Task)
		 */
		def linkTaskToMilestone = { taskToLink ->
			log.debug "linkTaskToMilestone - $taskToLink"
			if (latestMilestone) {
				log.debug "Calling createTaskDependency from linkTaskToMile"
				depCount += createTaskDependency(latestMilestone, taskToLink, assetsLatestTask, settings, out)
			} else {
				exceptions.append("Task($taskToLink) has no predecessor tasks<br>")
				exceptCnt++
			}
		}

		/**
		 * A helper closure used by generateRunbook to link a task to its predecessors by asset or milestone
		 * @param AssetComment (aka Task)
		 */
		def linkTaskToLastAssetOrMilestone = { taskToLink ->
			// See if there is an asset and that there are previous tasks for the asset
			log.info "linkTaskToLastAssetOrMilestone: assetsLatestTask=${assetsLatestTask.size()}"
			if (taskToLink.assetEntity) {
				if (assetsLatestTask.containsKey(taskToLink.assetEntity.id)) {
					if (assetsLatestTask[taskToLink.assetEntity.id].taskNumber != taskToLink.taskNumber) {
						log.info "linkTaskToLastAssetOrMilestone: creating dependency for task $taskToLink"
						depCount += createTaskDependency(assetsLatestTask[taskToLink.assetEntity.id], taskToLink, assetsLatestTask, settings, out)
						out.append("Created dependency between ${assetsLatestTask[taskToLink.assetEntity.id]} and $taskToLink<br>")
					} else {
						exceptions.append("Unexpected binding of task dependencies where a task references itself, task($taskToLink), TaskSpec ($taskToLink.taskSpec) <br>")
						exceptCnt++
					}
				} else {
					// Record this task as the asset's most recent task
					assetsLatestTask[taskToLink.assetEntity.id] = taskToLink

					linkTaskToMilestone(taskToLink)
				}
			} else {
				log.info "linkTaskToLastAssetOrMilestone: isRequired=$isRequired, task.asset=$taskToLink.assetEntity"
				linkTaskToMilestone(taskToLink)
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
			def wfsd
			if (moveBundleId && workflowStepCode) {
				wfsd = workflowSteps.find{ it.moveBundleId==moveBundleId && it.code == workflowStepCode }
				if (!wfsd) {
					exceptions.append("Unable to find workflow step code $workflowStepCode for bundleId $moveBundleId<br>")
					exceptCnt++
				}
			} else if (workflowStepCode) {
				// We have a workflow code but don't know which bundle. This will happen on the start and Completed tasks as there are no
				// Assets associated to the step and therefore can't tie it to a bundle. This is a bit of a hack in that it is just going to
				// find the first bundle. We could improve this to find the one with the latest completion time which is what we're getting
				// it for in a Milestone.
				wfsd = workflowSteps.find{ it.code == workflowStepCode }
				if (!wfsd) {
					exceptions.append("Unable to find workflow step code $workflowStepCode<br>")
					exceptCnt++
				}
			}
			return wfsd
		}

		/**
		 * A helper closure that bumps the predecessor task's associated assets to the current task if the previous task
		 * was a collection type task.
		 * @param AssetComment - the predecessor task to the current task
		 * @param AssetComment - the current task to associate the assets to
		 */
		def bumpAssetLastTask = { predTask, currTask ->
			// Examine the task to see if it was generated by a collection type of TaskSpec (e.g. sets, gateways, milestones) as we
			// need to move along all of the assets that funneled through the task.
			// TODO : Could ignore collections that are terminal - need to think that one through though
			if (collectionTaskSpecIds.contains(predTask.taskSpec)) {
				// Find all the assets that have this task as their predecessor and update their latest task to this one
				def tasksToBump = TaskDependency.findAllByAssetComment(predTask)
				tasksToBump.each {
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
		def projectStaff = partyRelationshipService.getAvailableProjectStaffPersons(project)

		if (isDebugEnabled) {
			log.debug '*************************************************************************************'
			log.debug '**************** generateRunbook() by {} for MoveEvent {} ****************', whom, event
			log.debug '*************************************************************************************'
			// log.debug "projectStaff is $projectStaff"
		}

		def recipeVersion 	// Holds reference to the recipeVersion that the TaskBatch points to
		def maxPreviousEstFinish       		// Holds the max Est Finish from the previous taskSpec
		def workflow
		def recipe 			// The recipe Map
		def recipeId 		// The id of the recipe
		def recipeTasks 	// The list of Tasks within the recipe
		def taskSpecIdList 	// The list of the task spec id #s
		TimeDuration elapsed

		try {
			recipeVersion = taskBatch.recipeVersionUsed
			if (! recipeVersion) {
				bailOnTheGeneration('The recipe version was unexpected missing from the task batch that was just created')
			}

			// Validate the syntax of the recipe before going any further
			def recipeErrors = cookbookService.validateSyntax(recipeVersion.sourceCode, project)
			if (recipeErrors) {
				msg = 'There appears to be syntax error(s) in the recipe. Please run the Validate Syntax and resolve reported issue before continuing.'
				log.debug 'Recipe had syntax errors'
				bailOnTheGeneration(msg)
			}
			recipe = cookbookService.parseRecipeSyntax(recipeVersion.sourceCode)
			recipeId = recipe?.id
			recipeTasks = recipe?.tasks

			if (!recipeTasks) {
				bailOnTheGeneration('There appears to be no runbook recipe or there is an error in its format')
			}

			// Load the taskSpecList array used for validation, etc
			taskSpecIdList = recipeTasks*.id
			log.info "taskSpecIdList=$taskSpecIdList"

			// Load the groups with the corresponding assets from the recipe group/filters
			groups = fetchGroups(recipe, contextObj, exceptions, project)

			out.append('Assets in Groups:<ul>')
			groups.each { n, l ->
				if (l.size() == 0) {
					exceptions.append("Found no assets for group $n<br>")
					exceptCnt++
				}

				out.append("<li><b>$n</b> (contains ${l.size()} assets): ${l*.assetName}")
			}

			out.append('</ul>')

			def numOfTaskSpec = recipeTasks.size()
			// Increments include # of taskSpecs + 1 (respresenting the initialization time)
			def percIncrFull = 100 / (numOfTaskSpec + 1)
			def percIncrHalf = percIncrFull / 2
			def percComplete = percIncrFull

			log.debug "\n\n     ******* BEGIN TASK GENERATION *******\n\n"

			// Now iterate over each of the task specs
			recipeTasks.each { taskSpec ->

				progressService.update(progressKey, Math.round(percComplete).toInteger(), 'Processing', "Generating tasks for spec $taskSpec.id")

				if (taskSpec.containsKey('disabled') && taskSpec.disabled) {
					log.debug "Skipping taskSpec $taskSpec.id that is disable"
					percComplete += percIncrFull
					return
				}

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
				String depMode = ''			// Holds [s|r] for assetTask.predecessor.mode to indicate s)upports or r)equires
				String mapMode = ''
				boolean hasPredecessor = false
				boolean hasPredGroup = false
				boolean hasPredTaskSpec = false
				def predecessor
				def successor
				boolean ignorePred = false
				boolean findParent = false		// Flag if the taskspec requires linking predecessor to a parent predecessor for an asset related task
				def assetsForTask = []		// This will contain the list of assets to which tasks are created for asset type taskspec
				def predTaskSpecs 		// Will hold an array of predecessor.taskSpec references if defined
				def predTasksByAssetId = [:].withDefault {[]}	// This will hold all of the predecessor tasks by asset.id for precedecessor.taskSpec element
				def predTasksWithNoAssets = []	// This will hold all of the predecessor tasks that are not associated with Assets for precedecessor.taskSpec element

				// because it could cause adverse dependency linkings.
				log.info "##### Processing taskSpec $taskSpec"

				// ------------------------------------------------------------------------------------
				// Parse out some of the commonly used properties from the TaskSpec
				// ------------------------------------------------------------------------------------
				specCount++

				// Save the taskSpec in a map for later reference
				taskSpecList[taskSpec.id] = taskSpec

				// Setup any common variables used in successor section
				if (taskSpec.containsKey('successor')) {
					successor = taskSpec.successor
					if (successor.containsKey('defer')) {
						deferSucc = successor.defer					}
					if (successor.containsKey('gather')) {
						gatherSucc = CU.asList(successor.gather)
					}
				}

				// Determine if the taskSpec has the predecessor.required property and if it is of the Boolean type
				if (taskSpec.containsKey('predecessor')) {
					predecessor = taskSpec.predecessor
					hasPredecessor = true

					if (predecessor.containsKey('required')) {
						if (! (predecessor.required instanceof Boolean)) {
							msg = "TaskSpec ($taskSpec.id) property 'predecessor.required' has invalid value ($predecessor.required) "
							log.error("$msg for Event $event")
							throw new RuntimeException(msg)
						} else {
							isRequired = predecessor.required
						}
					}

					if (predecessor.containsKey('ignore')) {
						if (! (predecessor.ignore instanceof Boolean)) {
							msg = "TaskSpec ($taskSpec.id) property 'predecessor.ignore' has invalid value ($predecessor.ignore), options true|false "
							log.error("$msg for Event $event")
							throw new RuntimeException(msg)
						} else {
							ignorePred = predecessor.ignore
						}
					}
					if (predecessor.containsKey('defer')) {
						deferPred = predecessor.defer
					}
					if (predecessor.containsKey('gather')) {
						gatherPred = CU.asList(predecessor.gather)
					}
					if (predecessor.containsKey('mode')) {
						depMode = predecessor.mode[0].toLowerCase()
					}

					if (predecessor.containsKey('inverse')) {
						if (! (predecessor.inverse instanceof Boolean)) {
							msg = "TaskSpec ($taskSpec.id) property 'predecessor.inverse' has invalid value ($predecessor.inverse), options: true | false "
							log.error("$msg for Event $event")
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
					if (! (depMode || hasPredGroup || hasPredTaskSpec || ignorePred || findParent || deferPred || gatherPred)) {
						msg = "Task Spec ($taskSpec.id) contains 'predecessor' section that requires one of the properties [mode | group | ignore | parent | taskSpec | defer | gather]"
						log.info(msg)
						bailOnTheGeneration(msg)
					}
				}

				// Flag used to determine if this taskSpec will create task(s) that are terminal (won't get connected to subsequent Milestones)
				def isTerminal = false
				if (taskSpec.containsKey('terminal')) {
					if (taskSpec.terminal instanceof Boolean) {
						isTerminal = taskSpec.terminal
					} else {
						msg = "Task Spec ($taskSpec.id) property 'terminal' has invalid value, options are [true | false]"
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

				// List of all available teams.
				List teamCodeList = partyRelationshipService.getTeamCodes(true)

				ApiAction apiAction = null
				if (taskSpec.containsKey("invoke")) {
					Map invokeSpec = taskSpec["invoke"]
					if (invokeSpec.containsKey("method")) {
						String apiActionName = invokeSpec["method"].trim()
						apiAction = ApiAction.findByName(apiActionName)
					}

				}

				// Collection of the task settings passed around to functions more conveniently
				settings = [
					type        : stepType,
					isRequired  : isRequired,
					isInversed  : isInversed,
					deferPred   : deferPred, deferSucc: deferSucc,
					gatherPred  : gatherPred, gatherSucc: gatherSucc,
					eventTimes  : eventTimes,
					clientId    : project.client.id,
					taskBatch   : taskBatch,
					publishTasks: publishTasks,
					apiAction   : apiAction,
					teamCodes   : teamCodeList,
					apiAction   : apiAction,
					event       : event,
					project     : taskBatch.project
				]

				log.debug "##### settings: $settings"

				// ------------------------------------------------------------------------------------
				// Create the Task(s) - the tasks are created within the following case statement
				// ------------------------------------------------------------------------------------
				out.append("<br>====== Processing Task Spec ${taskSpec.id}-$taskSpec.description $settings<br>")

				settings.taskSpec = taskSpec

				// Track what tasks were created by the taskSpec
				taskSpecTasks[taskSpec.id] = []

				switch (stepType) {

					case 'milestone':
						// -------------------------
						// Handle Milestone tasks
						// -------------------------
						out.append("Creating milestone $taskSpec.title<br>")

						isGroupingSpec = true

						newTask = createTaskFromSpec(recipeId, whom, taskList, taskSpec, projectStaff, settings, exceptions, workflow)
						def prevMilestone = latestMilestone
						latestMilestone = newTask

						// Indicate that the task is funnelling so that predecessor tasks' assets are moved through the task
						newTask.setTmpIsFunnellingTask(true)

						// Identify that this taskSpec is a collection type
						collectionTaskSpecIds << taskSpec.id

						log.info "milestone isRequired = $isRequired"

						// Find all tasks that don't have have a successor that are not terminal (excluding the current milestone task)
						// As well, find asset associated tasks that are sitting at the previous milestone and pull it forward if so
						def tasksNoSuccessors = []
						taskList.each { id, t ->
							// Check to see if the task doesn't have a successor, that it isn't terminal and that it isn't the current Milestone task
							if (! t.getTmpHasSuccessorTaskFlag() &&
								! terminalTasks.contains(t.id) &&
								t.id != newTask.id) {
									tasksNoSuccessors << t
									taskSpecTasks[taskSpec.id] << t
							} else if (t.assetEntity && assetsLatestTask.containsKey(t.assetEntity.id)) {
							 		// If this is a task with an asset then we'll shuffle the task to the latest Milestone
							 		assetsLatestTask[t.assetEntity.id] = latestMilestone
							}
						}

						//log.info "SQL for noSuccessorsSql: $noSuccessorsSqlFinal"
						log.info "generateRunbook: Found ${tasksNoSuccessors.size()} tasks with no successors for milestone $taskSpec.id, $event"

						if (tasksNoSuccessors.size()==0 && taskList.size() > 1) {
							if (prevMilestone) {
								log.debug "Calling createTaskDependency from milestone 1"
								depCount += createTaskDependency(prevMilestone, newTask, assetsLatestTask, settings, out)
							} else {
								out.append("Found no successors for a milestone, which is unexpected but not necessarily wrong - Task $newTask<br>")
							}
						}

						tasksNoSuccessors.each { p ->
							// TODO - switch to get task from task list instead of read call
							// def predecessorTask = AssetComment.read(p.id)
							def predecessorTask = taskList[p.id]
							log.debug "Calling createTaskDependency from milestone 2"
							depCount += createTaskDependency(predecessorTask, newTask, assetsLatestTask, settings, out)

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
							msg = "TaskSpec ($taskSpec.id) of type 'gateway' does not support 'mode' property"
							log.info(msg)
							bailOnTheGeneration(msg)
						}
						if (! hasPredecessor) {
							msg = "Gateway TaskSpec ID $taskSpec.id is missing required 'predecessor' property"
							log.info(msg)
							bailOnTheGeneration(msg)
						}

						newTask = createTaskFromSpec(recipeId, whom, taskList, taskSpec, projectStaff, settings, exceptions, null)

						// Indicate that the task is funnelling so that predecessor tasks' assets are moved through the task
						newTask.setTmpIsFunnellingTask(true)

						tasksNeedingPredecessors << newTask
						mapMode = 'DIRECT_MODE'

						msg = "Created GW task $newTask from taskSpec $taskSpec.id"
						log.info(msg)
						out.append(msg).append('<br>')

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
								def rcTasks = createRollcallTasks(whom, recipeId, taskSpec, settings)
								if (rcTasks) {
									rcTasks.each { rct ->
										taskList[rct.id] = rct
									}
									taskSpecTasks[taskSpec.id] = rcTasks
									tasksNeedingPredecessors.addAll(rcTasks)
									mapMode = 'DIRECT_MODE'
								} else {
									exceptions.append("Roll Call action did not create any tasks<br>")
									exceptCnt++
								}
								out.append("${rcTasks.size()} Roll Call tasks were created<br>")
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

								def actionTasks = createAssetActionTasks(action, contextObj, whom, projectStaff,recipeId, taskSpec, groups, workflow, settings, exceptions)

								if (actionTasks.size() > 0) {
									// Throw the new task(s) into the collective taskList using the id as the key
									actionTasks.each { t ->
										// Set flag that this is a funnelling task so that the predecessor logic will move all assets through it
										t.setTmpIsFunnellingTask(true)
										taskList[t.id] = t
										// taskSpecTasks[taskSpec.id] << t
									}
									taskSpecTasks[taskSpec.id] = actionTasks
									tasksNeedingPredecessors.addAll(actionTasks)
									mapMode = 'MULTI_ASSET_DEP_MODE'
								} else {
									exceptions.append("$action action did not create any tasks for taskSpec($taskSpec.id)<br>")
									exceptCnt++
								}
								out.append("${actionTasks.size()} $action tasks were created for taskSpec($taskSpec.id)<br>")
								break

							default:
								exceptions.append("Action($taskSpec.action) in taskSpec id $taskSpec.id presently not supported<br>")
								exceptCnt++
						}
						break

					case 'asset':
						// -------------------------
						// Create ASSET based Tasks
						// -------------------------
						isAsset = true

						// Normal tasks need to have a filter
						if (! taskSpec.filter || taskSpec.filter.size() == 0) {
							exceptions.append("TaskSpec id $taskSpec.id for asset based task requires a filter<br>")
							exceptCnt++
						}

						assetsForTask = findAllAssetsWithFilter(contextObj, taskSpec, groups, exceptions, project)
						log.info "Found ${assetsForTask?.size()} assets for taskSpec ${taskSpec.id}-$taskSpec.description"
						if (!assetsForTask || assetsForTask.size()==0)
							return // aka continue

						//
						// Create a task for each asset based on the filtering of the taskSpec
						//
						assetsForTask?.each { asset ->
							workflow = getWorkflowStep(taskWorkflowCode, asset.moveBundle.id)

							newTask = createTaskFromSpec(recipeId, whom, taskList, taskSpec, projectStaff, settings, exceptions, workflow, asset)

							tasksNeedingPredecessors << newTask
							taskSpecTasks[taskSpec.id] << newTask

							out.append("Created asset based task $newTask<br>")
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
								bailOnTheGeneration("Task Spec ($taskSpec.id) 'chain' property has invalid value. Acceptible values (true|false)")
							}
						}
						settings[isRequired] = isChain

						def genTitles = CU.asList(taskSpec.title)
						def genLastTask

						genTitles.each { generalTaskTitle ->
							// Replace the potential title:[array] with just the current title
							taskSpec.title = generalTaskTitle
							newTask = createTaskFromSpec(recipeId, whom, taskList, taskSpec, projectStaff, settings, exceptions, null, null)

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
									depCount += createTaskDependency(genLastTask, newTask, assetsLatestTask, settings, out)
								}
								genLastTask = newTask

							} else {
								// If not chain, link each of them to the predecessor dependencies
								tasksNeedingPredecessors << newTask
							}

						}

						if (! isChain && ! predecessor && lastTaskSpec) {
							// If the task spec doesn't have a predecessor and there is a previous taskSpec, then we'll fake out the system by
							// adding a predecessor clause to the taskSpec so that the task(s) will be successors to the previous taskSpec's task(s).
							predecessor = [taskSpec: lastTaskSpec.id]
							// predecessor = predecessor
							hasPredTaskSpec = true
						}

						mapMode = 'DIRECT_MODE'
						break

					default:
						// We should NEVER get into this code but just in case...
						log.error("Task Spec ($taskSpec.id) has an unhandled type ($stepType) in the code for event $moveEvent")
						bailOnTheGeneration("Task Spec ($taskSpec.id) has an unhandled type ($stepType) in the code")

				} // switch (stepType)

				// ------------------------------------------------------------------------------------
				// Post Creation - Logic to handle post creation before getting into the binding of dependencies
				// ------------------------------------------------------------------------------------


				// tmpDeferPred = [key: [bindToAsset, otherAsset]]

				// Defer Predecessor - Find predecessors for the given asset. If no predecessors, wire to last task for the asset or milestone otherwise defer. Set assetsLatestTask to the the current task so successive tasks are wired correctly, mark the predecessor as having successor so milestones aren't picked up
				// don't go into the default predecessor process below.

				// Defer Successor (DONE)  - Wire up predecessors as normal but don't set assetsLatestTask to the latest task; Inject tmpDefSucc property with key and asset reference to the task

				// Gather Predecessor - This can be done per task below. Update the assetsLatestTask with this task for the predecessor's asset

				// Gather Successor - Perform after wiring up predecessors. This will set the tasks indicated as successors. Need to bump the assets latest tasks

				// Handle gathering and deferred
				/*
				if (resumeFor) {
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


				if (tasksNeedingPredecessors.size() > 0 && ! ignorePred) {

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

					// Populate predecessorTasks array with all tasks that were created by the referenced predecessor.taskSpec(s)
					if (hasPredTaskSpec) {
						predTaskSpecs.each { ts ->
							// Find all predecessor tasks that have the taskSpec ID #
							if (taskSpecTasks.containsKey(ts)) {
								predecessorTasks.addAll(taskSpecTasks[ts])
							} else {
								// This should never happen but just in case we'll log it
								msg = "Task Spec ($taskSpec.id) 'predecessor.taskSpec' id ($ts) references missing list of tasks"
								log.error(msg)
								bailOnTheGeneration(msg)
							}
						}

						// Flip the predecessor tasks to a Map<List> of the tasks by the associated asset id
						predecessorTasks.each { t ->
							if (t.assetEntity) {
								if (! predTasksByAssetId.containsKey(t.assetEntity.id)) {
									predTasksByAssetId[t.assetEntity.id] = []
								}
								predTasksByAssetId[t.assetEntity.id] << t
							}
						}
						predTasksWithNoAssets = predecessorTasks.findAll { ! it.assetEntity }

						// log.debug "***%%%*** predTasksByAssetId: $predTasksByAssetId"
					}

					if (hasPredGroup) {
						// If there are any groups defined then preload all of the assets that are included in the groups to be used
						// in the next each/switch code block.
						log.info "hasPredGroup - here"
						// Put the group property into an array if not already an array
						def taskGroups = CU.asList(predecessor.group)

						// Iterate over the list of groups to consolidate one or more groups into
						taskGroups.each { groupCode ->
							if (groupCode.size() == 0) {
								log.info("generateRunbook: 'filter.group' value ($filter.group) has undefined group code.")
								bailOnTheGeneration("'filter.group' value ($filter.group) has undefined group code for taskSpec($taskSpec.id")
							}

							// Find the latest task for all of the assets of the specified GROUP
							log.info("assetsLatestTask has ${assetsLatestTask.size()} assets")
							if (groups.containsKey(groupCode)) {
								//hasPredGroup=true
								groups[groupCode].each { asset -> predAssets[asset.id] = asset }
							} else {
								bailOnTheGeneration("Task Spec ($taskSpec.id) 'predecessor' value ($predecessor) references undefined group.")
							}
						}
					}

					// Update the status of completion
					percComplete += percIncrHalf
					progressService.update(progressKey, Math.round(percComplete).toInteger(), 'Processing', "Creating predecessors for task spec $taskSpec.id")


					// ------------------------------------------------------------------------------------
					// Create Dependencies - iterate over all of the tasks just created for this taskSpec and assign dependencies
					// ------------------------------------------------------------------------------------
					out.append("## Creating predecessors for ${tasksNeedingPredecessors.size()} tasks<br>")

					tasksNeedingPredecessors.each { tnp ->
						log.info("### tasksNeedingPredecessors.each(): Processing $mapMode for task $tnp")

						if (tnp.assetEntity) {
							// Attempt to resolve any missed dependencies that may have occurred during an earlier step in the process.
							// For example, this can occur when there are multiple Application shutdown taskSpecs for various groups of application where there are
							// dependencies between two or more applications. If the interdependent application shutdown tasks are created in separate task specs we
							// need a way to bind them together when the subsequent taskspec is processed.
							// These missed dependencies are tracked in the missedDepList map. Missed dependencies are ONLY matched if they both occur in the same
							// category (e.g. Shutdown).
							String missedDepKey = "${tnp.assetEntity.id}_$tnp.category"
							log.info "missedDepList lookup for: $missedDepKey"
							if (missedDepList[missedDepKey]) {
								def tnpId = tnp.assetEntity.id
								log.info "Trying to find missed pred for '$missedDepKey' of asset $tnp"
								// Iterate over the missed dependencies and now create them
								def missedDepToRemove = []
								missedDepList[missedDepKey].each {
									// TODO - get task from taskList instead of using get method
									def prevTask = AssetComment.get(it.taskId)
									if (prevTask) {
										log.info "Resolved missed dependency between $prevTask and $tnp"
										// The missed relationship earlier was that where this asset - huh?

										// Lets see if we have an inverse relationship (e.g. Auto App Shutdown) so that we can switch the sequence
										// that the tasks are to be completed.
										log.debug "Calling createTaskDependency from tasksNeedingPredecessors.each inverse=$settings.isInversed"
										if (settings.isInversed) {
											depCount += createTaskDependency(prevTask, tnp, assetsLatestTask, settings, out)
											log.debug "Inversed task predecessor due to inverse flag"
										} else {
											depCount += createTaskDependency(tnp, prevTask, assetsLatestTask, settings, out)
										}
										missedDepToRemove << prevTask
									} else {
										msg = "Unable to find task associated with missed dependency (id:$it.taskId)"
										log.error msg
										exceptions.append(msg).append('<br>')
										exceptCnt++
									}
								}

								// Remove the missing dep from the missing list
								if (missedDepToRemove.size()) {
									def missedDepUpdate = []
									missedDepList[missedDepKey].each { missedDep ->
										if (! missedDepToRemove.find { it.id == missedDep })
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
						if (settings.gatherSucc) {
							def gdCnt = gatherDeferment(taskList, assetsLatestTask, tnp, 's', settings.gatherSucc, settings, out)
							if (gdCnt > 0) {
								wasWired = true
								depCount += gdCnt
							}
						}

						//
						// Gather up any deferred predecessor references if a predecessor.gather was presented
						//
						if (settings.gatherPred) {
							def gdCnt = gatherDeferment(taskList, assetsLatestTask, tnp, 'p', settings.gatherPred, settings, out)
							if (gdCnt > 0) {
								wasWired = true
								depCount += gdCnt
							}
						}

						//
						// Wire up based on predecessor.taskSpec setting if it was declared
						//
						if (hasPredTaskSpec) {
							//
							// --- TASKSPEC used W/O mode ---
							//

							log.debug "Processing in hasPredTaskSpec of DIRECT_MODE"

							// Use the predecessorTasks array that was initialized earlier. If any of those tasks and the current task are associated with the
							// same asset then wire up the predecessor tasks one-to-one for each asset otherwise wire the current task to all tasks in the
							// predecessorTasks. If both have assets and we are unable to find a predecessor task for the same asset, the current task will
							// be wired to the most recent milestone if it exists.

							// First try to look for predecessor tasks by asset id
							if (tnp.assetEntity && predTasksByAssetId.containsKey(tnp.assetEntity.id)) {
								predTasksByAssetId[tnp.assetEntity.id].each { pt ->
									log.debug "Calling createTaskDependency from TASKSPEC 1"
									depCount += createTaskDependency(pt, tnp, assetsLatestTask, settings, out)
									wasWired = true
									if (isRequired) {
										// Update the Asset's last task based on if the previous task is for an asset or the current one is for an asset
										// TODO - NOT certain that we need this predecessor assignment and need to investigate
										assignToAssetsLatestTask(tnp.assetEntity, tnp, assetsLatestTask)
										log.info "Adding latest task $tnp to asset $tnp.assetEntity - 7"
									}
								}
							}

							// Now try and find all non-asset predecessor tasks and wire to them
							predTasksWithNoAssets.each { pt ->
								log.debug "Calling createTaskDependency from TASKSPEC 2"
								depCount += createTaskDependency(pt, tnp, assetsLatestTask, settings, out)
								wasWired = true
							}

							if (! wasWired) {
								msg = "No predecessor tasks found for predecessor.taskSpec (spec $taskSpec.id) for task $tnp"
								log.info(msg)
								exceptions.append(msg).append('<br>')
								exceptCnt++
							}
						}

						//
						// Wire up based on the Mapping Mode
						//
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
											depCount += createTaskDependency(parentDep.predecessor, tnp, assetsLatestTask, settings, out)

											wasWired = true

											// We need to chain the peer task inside of this task so that subsequent task spec for the asset
											// will connect follow the chain and connect to all of the peers.
											tnp.setTmpChainPeerTask(assetsLatestTask[tnp.assetEntity.id])
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
										if (predAssets.containsKey(tnp.assetEntity.id)) {
											// Find the latest asset for the task.assetEntity
											if (assetsLatestTask.containsKey(tnp.assetEntity.id)) {
												log.debug "Calling createTaskDependency from GROUPS - asset $tnp.id $tnp.assetName"
												depCount += createTaskDependency(assetsLatestTask[tnp.assetEntity.id], tnp, assetsLatestTask, settings, out)
												wasWired = true
											} else {
												// Wire to last milestone
												msg = "No predecessor task found for asset ($tnp.assetEntity) to link to task ($tnp) in taskSpec $taskSpec.id (DIRECT_MODE/group)"
												log.info(msg)
												exceptions.append(msg).append('<br>')
												exceptCnt++
												linkTaskToMilestone(tnp)
											}
										} else {
											msg = "Asset ($tnp.assetEntity) was not found in group for taskSpec $taskSpec.id (DIRECT_MODE/group)"
											log.info(msg)
											exceptions.append(msg).append('<br>')
											exceptCnt++
										}
									} else {
										// Case #2 - Wire latest tasks for all assets in group to this task
										log.debug "Processing from hasPredGroup #2 isRequired=$isRequired"

										// log.info("predAssets=$predAssets")
										// If the last task of the asset was a group task the current one is a group task, then we only want to create a single task dependency
										predAssets.each { predAssetId, predAsset ->
											//log.info("predAsset=$predAsset")
											if (assetsLatestTask.containsKey(predAsset.id)) {
												def predAssetTask = assetsLatestTask[predAsset.id]
												log.debug "Calling createTaskDependency from GROUPS 2 - asset $predAsset.id $predAsset.assetName"
												depCount += createTaskDependency(predAssetTask, tnp, assetsLatestTask, settings, out)

												// Check to see if the predecessor and the successor are funnels and if so, then move the asset forward to the new successor
												if (predAssetTask.getTmpIsFunnellingTask() != null && tnp.getTmpIsFunnellingTask() != null) {
													// assetsLatestTask[predAsset.id] = tnp
													assignToAssetsLatestTask(predAsset, tnp, assetsLatestTask)
												}

												wasWired = true
											} else {
												// if (isGroupingSpec) {
												if (tnp.getTmpIsFunnellingTask() != null) {
													// So no task previously existed for the asset so we're going to just wire the assets' last task to the gateway task
													// assetsLatestTask[predAsset.id] = tnp
													assignToAssetsLatestTask(predAsset, tnp, assetsLatestTask)
													log.debug "Funnelled assignment of task $tnp as last task for asset $predAsset.id $predAsset.assetName"
												}
											}
										}
									}
								}

								if (! wasWired) {
									log.debug "MapMode $mapMode - Wiring task predecessor to default"
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
								if (settings.deferPred) {
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

									log.debug "Postponing predecessor binding for task $tnp with key ($settings.deferPred) to ${deferDeps.size()} predecessors"

									if (deferDeps.size()) {

										// Track that the last task for the asset is this task needing predecessors but don't wire up as a successor?
										assignToAssetsLatestTask(tnp.assetEntity, tnp, assetsLatestTask)

										// For each of the set of dependencies, we need to indicate that this deferred task is the latest task for the dependency asset
										// By comparing the asset and dependent properties being equal to the current task's asset, we can determine which is the dependency
										def defPredProp = tnp.assetEntity.id == deferDeps[0].asset.id ? 'dependent' : 'asset'
										deferDeps.each { dd ->

											// Lets postpone the wiring of the task by injecting the meta property tmpWaitFor
											setDeferment(tnp, dd[defPredProp], 'p', settings.deferPred, settings)

											//
											// assetsLatestTask[dd[defPredProp].id] = tnp
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
								if (assetsLatestTask.containsKey(tnp.assetEntity.id) &&
									  assetsLatestTask[tnp.assetEntity.id].getTmpIsFunnellingTask() != null) {
									log.debug "Calling createTaskDependency from ASSET_DEP_MODE"
									depCount += createTaskDependency(assetsLatestTask[tnp.assetEntity.id], tnp, assetsLatestTask, settings, out)
									wasWired=true
								}

								// If there was no previous funnel for the asset and we couldn't find any dependencies, then just wire the task to a previous milestone
								if (assetDependencies.size() == 0 && ! wasWired) {
									exceptions.append("Asset($tnp.assetEntity) for Task($tnp) has no $predecessor relationships<br>")
									exceptCnt++

									// Link task to the last known milestone or it's asset's previous task
									linkTaskToLastAssetOrMilestone(tnp)
									// TODO : Don't think this if logic is necessary any more
									if (isRequired && ! deferSucc)
										assignToAssetsLatestTask(tnp.assetEntity, tnp, assetsLatestTask)

									wasWired = true

								}

								// If there were any asset dependencies, nows the time that we'll try and wire it up
								if (assetDependencies.size()) {
									// Look over the assets dependencies that are associated to the current task's asset and create predecessor relationships
									// We will warn on assets that are not part of the moveEvent that have dependency.
									// TODO : We most likely will want to have tasks for assets not moving in the future but will require discussion.
									log.info "** Iterate over ${assetDependencies.size()} dependencies for asset $tnp.assetEntity"
									assetDependencies.each { ad ->

										def predAsset
										// TODO TM-2968 -- This variable should only be an r|s so 'b' seems to be a typo also I don't understand what is going on in this block
										if (depMode=='b') {
											predAsset = (ad.asset.id == tnp.assetEntity.id ? ad.dependent : ad.asset)
										} else {
											// Note that this should be the opposite of that used in the getAssetDependencies
											predAsset = (depMode == 's' ?  ad.dependent : ad.asset)
										}

										// Now Wire to the last tasks of the supporting assets

										log.debug "Working on dependency asset ($ad.asset depends on ($ad.dependent) - matching on asset $predAsset"

										// Make sure that the other asset is in one of the bundles in the event
										def predMoveBundle = predAsset.moveBundle
										if (! predMoveBundle || ! bundleIds.contains(predMoveBundle.id)) {
											exceptions.append("Asset dependency references asset not in event: task($tnp) between asset $tnp.assetEntity and $predAsset<br>")
											exceptCnt++
										} else {
											// Find the last task for the predAsset to create associations between tasks. If the predecessor was created
											// during the same taskStep, assetsLatestTask may not yet be populated so we can scan the tasks created list for
											// one with the same taskSpec id #
											def previousTask = taskList.find { i, t -> t.assetEntity?.id == predAsset.id && t.taskSpec == tnp.taskSpec }?.value
											if (previousTask) {
												log.info "Found task in taskList array - task ($previousTask)"
											} else {
												// Try finding latest task for the asset
												if (assetsLatestTask.containsKey(predAsset.id)) {
													previousTask = assetsLatestTask[predAsset.id]
													log.info "Found task from assetsLatestTask array - task ($previousTask)"
												}
											}
											if (previousTask) {
												log.debug "Calling createTaskDependency from ASSET_DEP_MODE"
												depCount += createTaskDependency(previousTask, tnp, assetsLatestTask, settings, out)
												wasWired=true
											} else {
												log.info "No predecessor task found for asset ($predAsset) to link to task ($tnp) ASSET_DEP_MODE"
												// exceptions.append("No predecessor task found for asset ($predAsset) to link to task ($tnp)<br>")

												// Push this task onto the stack to be wired up later on
												saveMissingDep(missedDepList, "${predAsset.id}_$tnp.category", taskSpec, tnp, isRequired, latestMilestone, ad)
											}
										}
									} // assetDependencies.each

									if (hasPredTaskSpec && ! wasWired) {
										exceptions.append("Task($tnp) No predecessor(s) found using predecessor.taskSpec in taskSpec($taskSpec.id)<br>")
										exceptCnt++
									}
								}
								break

							case 'MULTI_ASSET_DEP_MODE':
								// In this case each task already has multiple assets injected into it so we'll just create the
								// necessary dependencies for the associatedAssets define in the task.
								if (! tnp.getTmpAssociatedAssets()) {
									msg = "Task was missing expected assets for dependencies of task $tnp"
									log.info(msg)
									bailOnTheGeneration(msg)
								}

								// This logic is used for the various grouping actions (e.g. cart, truck, set)
								// Iterate over the associated assets that were stuffed into the task.associatedAssets list
								def foundPred=false
								tnp.getTmpAssociatedAssets().each { assocAsset ->
									// If an asset already has existing task, need to wire up the predecessor
									if (assetsLatestTask.containsKey(assocAsset.id)) {
										// Let's temporially stuff the asset into the task and then wire up the predecessors
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

								msg = "Unsupported switch value ($mapMode) for taskSpec ($taskSpec.id) on processing task $tnp"
								log.info(msg)
								bailOnTheGeneration(msg)


						} // switch(mapMode)

						//
						// Handle the resuming of the waitFor
						//
						if (false) {

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
										ttr.status = ACS.PENDING

										wasWired = true
									}
								}
							}
							*/
						}

						if (! wasWired) {
							// If the task wasn't wired to any predecessors, then try to wire it to the latest milestone
							linkTaskToLastAssetOrMilestone(tnp)
						}

					} // tasksNeedingPredecessors.each()

					// Update the status of completion
					percComplete += percIncrHalf
					progressService.update(progressKey, Math.round(percComplete).toInteger(), 'Processing', "Finished dependencies for task spec $taskSpec.id")
				}

				lastTaskSpec = taskSpec

			} // recipeTasks.each() {}

			percComplete += percIncrHalf

			// *******************************************
			// TODO - Iterate over the missedDepList and wire tasks to their milestone
			// *******************************************
		} catch(e)	{
			// exceptions.append("We BLEW UP damn it!<br>")
			// exceptions.append(failure)
			if (! failure) {
				// If the failed variable is empty then we have an unexpected error so dump the stack for debugging purposes
				failure = e.message
				exceptions.append(failure)
				log.error "$failure\n${ExceptionUtil.stackTraceToString(e,80)}"
			}
		}

		// Check to make sure that all of the deferred tasks have been collected as they should have
		def defTaskList = getOutstandingDeferments(taskList)
		if (defTaskList.size()) {
			exceptions.append("${defTaskList.size()} Outstanding Deferred Tasks were never gathered<br>")
			exceptCnt++
			log.warn "Outstanding Deferred Tasks were never gathered: $defTaskList"
		}

		elapsed = TimeCategory.minus(new Date(), startedAt)
		msg = "A total of ${taskList.size()} Tasks and $depCount Dependencies created in $elapsed"
		log.info msg

		if (failure) failure = "Generation FAILED: $failure<br/>"

		taskBatch.taskCount = taskList.size()
		taskBatch.exceptionCount = exceptCnt
		taskBatch.exceptionLog = exceptions.toString()
		taskBatch.infoLog = msg.toString() + "<br>" + out
		taskBatch.status = failure ? 'Failed' : 'Completed'
		taskBatch.save(flush:true, failOnError:true)

		// TM-2843 - Fix issue with memory leak due to the usage of metaClass.addProperty
		// TODO : JPM 5/2016 : generateTasks() review if the 'taskToWipe.metaClass = null' is still necessary for the memory leak
		// taskList.each { id, taskToWipe -> taskToWipe.metaClass = null }

		if (failure) {
			throw new RuntimeException(failure)
		}
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
		String baseHql
		String baseSql
		def map

		if (depMode == 's') {
			// Supports relationship
			currAssetPropName = 'asset'
			assocAssetPropName = 'dependent'
		} else if (depMode == 'r') {
			// Requires relationship
			currAssetPropName = 'dependent'
			assocAssetPropName = 'asset'
		}

		if (depMode == 'b') {
			baseHql = '''
				from AssetDependency ad
				where (dependent.id=:assetId or asset.id=:assetId)
				and dependent.moveBundle.id IN (:moveBundleIds)
				and asset.moveBundle.id IN (:moveBundleIds)
				and status not in ''' + "('$NA', '$ARCHIVED')" + '''
				and type <> '${AssetDependencyType.BATCH}'
			'''
		} else {
			baseSql = """
				from AssetDependency ad
				where ${currAssetPropName}.id=:assetId
				and ${assocAssetPropName}.moveBundle.id IN (:moveBundleIds)
				and ad.status not in ('$NA', '$ARCHIVED')
				and type <> '$BATCH'
			"""
		}

		// TODO - Wire in filtering of the bundle ids
		// Note that this should be the opposite of that used in the getAssetDependencies
		// def predAsset = depMode == 's' ?  ad.dependent : ad.asset

		map = [assetId:asset.id, moveBundleIds:bundleIds]

		def sqlMap
		def sql = baseSql

		// Add additional WHERE expresions to the SQL
		supportedPredFilterProps.each { prop ->
			if (taskSpec.predecessor?.containsKey(prop)) {
				sqlMap = SqlUtil.whereExpression('ad.' + prop, taskSpec.predecessor[prop], prop)
				if (sqlMap) {
					sql = SqlUtil.appendToWhere(sql, sqlMap.sql)
					if (sqlMap.param) {
						map[prop] = sqlMap.param
					}
				} else {
					log.error "SqlUtil.whereExpression unable to resolve $prop expression [${taskSpec.predecessor[prop]}]"
					throw new RuntimeException("Unable to resolve filter param:[$prop] expression:[${taskSpec.predecessor[prop]}] while processing asset $asset")
				}
			}
		}

		// Add filter on the classification (asset.type presently) if it was declared (this is filtering on the apposing asset.assetType property)
		if (taskSpec.predecessor?.containsKey('classification') && taskSpec.predecessor.classification) {
			def isNot = false
			def c = taskSpec.predecessor.classification
			def match = c =~ /([!|=|<|>]{0,})(.+\w?)/
			if (match[0].size() == 3) {
				c = match[0][2]
				isNot = (['!=', '<>'].contains(match[0][1]))
			}

			def acEnum = AssetClass.safeValueOf(c.toUpperCase())
			log.debug "Dealing with predecessor classification: ${taskSpec.predecessor.classification.toUpperCase()} results in $acEnum"
			sqlMap = SqlUtil.whereExpression('ad.' + assocAssetPropName + '.assetClass', acEnum, 'classification', isNot)
			sql = SqlUtil.appendToWhere(sql, sqlMap.sql)
			map['classification'] = sqlMap.param
			log.info "getAssetDependencies: Added classification filter $sqlMap.sql, $sqlMap.param"
		}

		log.info "getAssetDependencies: depMode=$depMode, SQL=$sql, PARAMS=$map"
		list = AssetDependency.executeQuery(sql, map)
		log.info "getAssetDependencies: found ${list.size()} rows : $list"

		// Filter out only the dependencies where the assets are both in the bundle list
		// list = list.findAll { bundleIds.contains(it.asset.moveBundle.id) && bundleIds.contains(it.dependent.moveBundle.id) }

		// Now need to find the nested logic associations (e.g. APP > DB > SRV or APP > Storage > SAN) and
		// add the dependency of the logic asset to the current asset's dependencies.
		if (depMode == 'b') {
			// we can't traverse in the both relationship as it makes my head spin...
			finalList = list
		} else {
			// Only traverse if the dependency specification requests it
			if (taskSpec.predecessor?.containsKey('traverse') && taskSpec.predecessor.traverse) {
				list.each { dep ->

					def nestedDep

					// Call the recursive routine that will find any nested dependencies (e.g. App > DB > DB App or App > LUN > Storage App)
					nestedDep=traverseDependencies(asset, dep, currAssetPropName, assocAssetPropName, baseSql, map)
					if (nestedDep.size()) {
						// nestedDep = nestedDep.findAll { bundleIds.contains(it.asset.moveBundle.id) && bundleIds.contains(it.dependent.moveBundle.id) }
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
			finalList.unique { (it.asset.id == asset.id ? "$it.asset.id:$it.dependent.id" : "$it.dependent.id:$it.asset.id") }
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
		//if (assocAsset.isaDatabase() || assocAsset.isaStorage() || assocAsset.isaNetwork()) {

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
		logicDep.each { d ->
			def depAsset = d[assocAssetPropName]
			if (! depAsset.isaLogicalType()) {
				// Add any real asset to the dependency list
				depList.add(d)
				log.info "traverseDependencies: Adding dependency on ${d[assocAssetPropName]}"
			}
		}

		return depList
	}

	/**
	 * Used to set the duration on a task from the duration value coming from task specification
	 * The format of the duration that is supported
	 *    Integer - defaults to minutes (e.g. duration:15)
	 *    String time - requires TimeScale values (e.g. duration:'60m')
	 *    Indirect reference (e.g. duration:'#shutdownTime')
	 *    Indirect reference with default value (e.g. duration:'#shutdownTime,15m')
	 * @param task - the task to update
	 * @param duration - the value from the task spec (either Integer or String)
	 * @return null if successful or an error message
	 */
	private String setTaskDuration(AssetComment task, Object duration) {
		def msg
		def defValue = 0
		def defScale = TimeScale.M

		task.durationScale = TimeScale.M 	// Set the default scale

		if (duration instanceof Integer) {
			task.duration = duration
		} else if (duration instanceof String) {
			def indirect
			def match
			log.debug "setTaskDuration: processing string '$duration'"
			while (true) {

				// Just a time with a TimeScale (e.g. 45m)
				match = duration =~ /(\d{1,})(?i)(m|h|d|w)/
				if (match.matches()) {
					log.debug "setTaskDuration: processing string match 1"
					task.duration = match[0][1].toInteger()
					task.durationScale = TimeScale.asEnum(match[0][2].toUpperCase())
					log.debug "setTaskDuration: task duration=$task.duration, scale=$task.durationScale"
					break
				}

				// Purely an indirect reference (e.g. #shutdownTime)
				match = duration =~ /#(\w{1,})/
				if (match.matches()) {
					log.debug "setTaskDuration: processing string match 2"
					indirect = match[0][1]
					break
				}

				// Try Indirect with fully qualified default (e.g. #shutdownTime, 15m)
				match = duration =~ /#(\w{1,}),\s?(\d{1,})(?i)(m|h|d|w)/
				if (match.matches()) {
					log.debug "setTaskDuration: processing string match 3"
					indirect = match[0][1]
					defValue = match[0][2].toInteger()
					defScale = TimeScale.asEnum(match[0][3].toUpperCase())
					break
				}

				// Try Indirect with default and no scale (e.g. #shutdownTime, 15)
				match = duration =~ /#(\w{1,}),\s?(\d{1,})/
				if (match.matches()) {
					log.debug "setTaskDuration: processing string match 4"
					indirect = match[0][1]
					defValue = match[0][2].toInteger()
					defScale = TimeScale.M
					break
				}

				log.debug "setTaskDuration: processing string match failed"
				msg = "Unrecognized duration value '$duration'"
				return msg
			}

			if (indirect) {
				// Try to lookup the indirect propert on the asset
				log.debug "setTaskDuration: processing indirect refer to '$indirect' for '$duration'"
				try {
					def indDur = AssetEntityHelper.getIndirectPropertyRef(task.assetEntity, indirect)
					log.debug "setTaskDuration: Indirect refer found '$indDur' in property '$indirect'"
					if (indDur instanceof Integer) {
						task.duration = indDur ?: defValue
						task.durationScale = TimeScale.M
						log.debug "setTaskDuration: set from indirect '$indDur' integer results=$task.duration$task.durationScale"
					} else if (indDur instanceof String) {
						match = indDur =~ /(\d{1,})(?i)(m|h|d|w)/
						if (match.matches()) {
							task.duration = match[0][1].toInteger()
							task.durationScale = TimeScale.asEnum(match[0][2].toUpperCase())
							log.debug "setTaskDuration: set from indirect '$indDur' results=$task.duration$task.durationScale"
						} else {
							// Set to the default
							if (indDur.isInteger()) {
								log.debug "setTaskDuration: set from indirect '$indDur' results=$indDur default to $task.durationScale"
								task.duration = NumberUtils.toInt(indDur, defValue)
							} else {
								log.debug "setTaskDuration: set from indirect '$indDur' using default value $defValue"
								task.duration = defValue
								task.durationScale = defScale
							}
							task.duration = NumberUtils.toInt(indDur, defValue)
						}

					} else {
						log.debug "setTaskDuration: indirect '$indDur' was not set, using default value $defValue/$defScale"
						task.duration = defValue
						task.durationScale = defScale
						if (indDur != null) {
							msg = "Indirect duration '$duration' referenced invalid property name"
							log.error "$msg, asset=$task.assetEntity"
							return msg
						}
					}
				} catch (e) {
					msg = "Indirect duration '$duration' error $e.message"
					log.error "$msg, asset=$task.assetEntity\n${ExceptionUtil.stackTraceToString(e)}"
					task.duration = defValue
					task.durationScale = defScale
					return msg
				}

			}
		} else {
			// Unexpected data type in the duration field
			msg = "Duration property has unexpected data type ${duration.class}"
			log.debug "setTaskDuration: $msg"
			return msg
		}

		return null
	}

	/**
	 * Creates a task for a moveEvent based on a taskSpec from a recipe and defaults the status to READY
	 * @param moveEvent object
	 * @param Map taskSpec that contains the specifications for how to create the task
	 * @param asset - The asset associated with the task if there appropriate
	 * @return AssetComment (aka Task)
	 */
	def createTaskFromSpec(recipeId, whom, taskList, taskSpec, projectStaff, settings, exceptions, workflow=null, asset=null) {
		def task = new AssetComment(
			taskNumber: sequenceService.next(settings.clientId, 'TaskNumber'),
			taskBatch: settings.taskBatch,
			isPublished: settings.publishTasks,
			sendNotification: taskSpec.sendNotification ?: false,
			project: settings.project,
			moveEvent: settings.event,
			assetEntity: asset,
			commentType: AssetCommentType.TASK,
			status: ACS.READY,
			createdBy: whom,
			displayOption: 'U',
			autoGenerated: true,
			recipe: recipeId,
			taskSpec: taskSpec.id,
			apiAction: settings.apiAction)

		def msg
		def errMsg

		// Handle setting the task duration which can have an indirect reference to another property as well as a default value
		// or a numeric value
		if (taskSpec.containsKey('priority')) {
			task.priority = taskSpec.priority
		}

		if (taskSpec.containsKey("docLink")) {
			try {
				String docLink = RecipeHelper.resolveDocLink(taskSpec["docLink"], task.assetEntity)
				task.instructionsLink = docLink
			} catch (RecipeException re) {
				exceptions.append("Error while setting the docLink for ${taskSpec.id}. ${re.getMessage()} <br>")
			}

		}

		// Set the duration appropriately
		if (taskSpec.containsKey('duration')) {
			errMsg = setTaskDuration(task, taskSpec.duration)
			if (errMsg) {
				exceptions.append("$errMsg for taskSpec $id<br>")
			}
		}

		if (taskSpec.containsKey('durationLocked')) {
			task.durationLocked = taskSpec.durationLocked.toBoolean()
		}

		if (taskSpec.containsKey('category') && taskSpec.category) {
			task.category = taskSpec.category
		}

		// def defCat = task.category
		// task.category = null

		try {
			if (asset) {
				task.comment = new GStringEval().toString(taskSpec.title, asset)
			} else {
				task.comment = new GStringEval().toString(taskSpec.title, settings.event)
			}
		} catch (Exception e) {
			log.debug ExceptionUtil.stackTraceToString(e)
			exceptions.append("Unable to parse title ($taskSpec.title) for taskSpec $taskSpec.id<br>")
			task.comment = "** Error computing title **"
		}

		// Set various values if there is a workflow associated to this taskSpec and Asset
		// if (workflow) {
		// 	// log.info "Applying workflow values to task $taskNumber - values=$workflow"
		// 	if (workflow.workflow_transition_id) { task.workflowTransition = WorkflowTransition.read(workflow.workflow_transition_id) }
		// 	// if (! task.role && workflow.role_id) { task.role = workflow.role_id }
		// 	if (! task.category && workflow.category) { task.category = workflow.category }
		// 	if (! task.estStart && workflow.plan_start_time) { task.estStart = workflow.plan_start_time }
		// 	if (! task.estFinish && workflow.plan_completion_time) { task.estFinish = workflow.plan_completion_time }
		// 	if (! task.duration && workflow.duration != null) {
		// 		task.duration = workflow.duration
		// 		task.durationScale = workflow.duration_scale ?: 'm'
		// 	}
		// }

		if (task.category == null) {
			task.category = defCat
		}

		// log.info "About to save task: $task.category"
		if (! task.save(flush:true, failOnError:false)) {
			log.error("createTaskFromSpec: Failed creating task error=${GormUtil.allErrorsString(task)}, asset=$asset, TaskSpec=$taskSpec")
			throw new RuntimeException("Error while trying to create task. error=${GormUtil.allErrorsString(task)}, asset=$asset, TaskSpec=$taskSpec")
		}

		// Perform the assignment logic
		errMsg = assignWhomAndTeamToTask(task, taskSpec, workflow, projectStaff, settings)
		if (errMsg) {
			exceptions.append("$errMsg<br>")
		}
		taskList[task.id] = task

		log.info 'Saved task {} - {}', task.id, task.toString()

		// Set any scheduling constraints on the task
		setTaskConstraints(task, taskSpec, workflow, projectStaff, exceptions)

		return task
	}

	/**
	 * Used to fetch a Task Dependency by finding the predecessor / successor tasks
	 * @param predecessor - the predecessor task
	 * @param successor - the successor task
	 * @return the TaskDependency if found otherwise null
	 */
	private TaskDependency fetchTaskDependencyByTasks(AssetComment predecessor, AssetComment successor) {

		return TaskDependency.findByAssetCommentAndPredecessor(successor, predecessor)
		// The following was complaining for some reason
		// dependency = TaskDependency.where {
		// 	assetComment.id == successor.id
		// 	predecessor.id == predecessor.id
		// }.find()
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
	 * @throws RuntimeException if unable to save the dependency
	 */
	@CompileStatic
	Integer createTaskDependency(AssetComment predecessor, AssetComment successor, Map assetsLatestTask, Map settings, StringBuilder out, Integer count=0) {

		log.info "createTaskDependency: ** START - Creating dependency count=$count predecessor=$predecessor, successor=$successor"

		if (predecessor.id == successor.id) {
			throw new RuntimeException("Attempted to create dependency where predecessor and successor are the same task ($predecessor)")
		}

		TaskDependency dependency

		// Need to see if the dependency was previously created. In addition, in the case of a missed dependency where there is a reversed
		// taskSpec dependency (e.g. Auto App Shutdowns) we can get into the situation where a dependency is improperly created so we'll
		// only create the first, which is created by the missed dependency (e.g. db -> app and later app -> db)
		//
		// In the case of containers (set, truck, rack, etc) this method will get called repeatedly for each of the assets so we only create the first dependency but
		// then need to bump the latest task for each of the assets appropriately. Keep in mind that the calling logic stuffs each of the assets into the task before
		// calling this method. It's a bit convoluted...

		dependency = fetchTaskDependencyByTasks(successor, predecessor)
		if (!dependency) {
			dependency = fetchTaskDependencyByTasks(predecessor, successor)
		}

		if (dependency) {
			log.info "createTaskDependency: dependency already exists"
		} else {
			dependency = new TaskDependency(predecessor:predecessor, assetComment:successor)
			if (! (dependency.save(flush:true))) {
				throw new RuntimeException("Error while trying to create dependency between predecessor=$predecessor, successor=$successor<br/>Error:${GormUtil.errorsAsUL(dependency)}, ")
			}
			out.append("Created dependency ($dependency.id) between $predecessor and $successor<br/>")
			count++
		}

		if (predecessor.id == successor.id) {
			throw new RuntimeException("Attempted to create dependency where predecessor and successor are the same task ($predecessor)")
		}

		/*
		if (settings.deferPred) {
			throw new RuntimeException("createTaskDependency: shouldn't be called with predecessor.defer:'$settings.deferPred' ($predecessor), succ ($successor)")
		}
		*/

		// Mark the predecessor task that it has a successor so it doesn't mess up the milestones
		if (! predecessor.getTmpHasSuccessorTaskFlag()) {
			predecessor.setTmpHasSuccessorTaskFlag(true)
		}

		// Mark the Successor task to the PENDING status since it has to wait for the predecessor task to complete before it can begin
		successor.status = ACS.PENDING
		if (! successor.save(flush:true)) {
			throw new RuntimeException("Failed to update successor ($successor) status to PENDING<br>Errors:${GormUtil.errorsAsUL(dependency)}")
		}

		if (predecessor.assetEntity) {
			log.info "createTaskDependency: pred asset latest task? ${assetsLatestTask.containsKey(predecessor.assetEntity.id)} - ${assetsLatestTask[predecessor.assetEntity.id] ?: ''}"
		}

		// Handle updating predecessor tasks' asset with successor task if it is a funnel type (e.g. gateway, milestone, set, truck, cart, etc)
		if (predecessor.assetEntity && successor.getTmpIsFunnellingTask() != null) {
			assignToAssetsLatestTask(predecessor.assetEntity, successor, assetsLatestTask)
			// assetsLatestTask[predecessor.assetEntity.id] = successor
			log.info "createTaskDependency: Updated assetsLatestTask for asset $successor.assetEntity with funnel task $successor"
		}

		// Update the successor tasks' assets with latest task with the current task if it isRequired and not being deferred
		if (successor.assetEntity && settings.isRequired  && ! settings.deferSucc) {
			assignToAssetsLatestTask(successor.assetEntity, successor, assetsLatestTask)
			//assetsLatestTask[successor.assetEntity.id] = successor
			log.info "createTaskDependency: Updated assetsLatestTask for asset $successor.assetEntity with successor $successor"
		}

		// Handling deferring dependency relationships when taskSpec indicates to successor.defer:'key'
		if (settings.deferSucc) {
			setDeferment(successor, successor.assetEntity, 's', (String)settings.deferSucc, settings)
		}

		// Here is the recursive loop if the predecessor has a peer
		AssetComment peerTask
		if (peerTask) {
			log.info "createTaskDependency: Invoking recursively due to predecessor having chainPeerTask ($peerTask)"
			count += createTaskDependency(peerTask, successor, assetsLatestTask, settings, out, count)
		}

		log.debug "createTaskDependency: ** FINISHED - count=$count"

		return count
	}

	/**
	 * Helper function used to assign a task as the latest for a given asset
	 * @param The asset to update in the assetsLastestTask map
	 * @param The task to assign as the asset's latest task
	 * @param The Map collection of assets ids and the associate latest task
	 */
	def assignToAssetsLatestTask(AssetEntity asset, AssetComment task, Map assetsLatestTask) {
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
					if (! assetsLatestTask[asset.id].getTmpHasSuccessorTaskFlag())
						assetsLatestTask[asset.id].setTmpHasSuccessorTaskFlag(true)
				}
			}

			// Assign the new task as the latest for the asset
			if (isNewer) {
				assetsLatestTask[asset.id] = task
				log.debug "assignToAssetsLatestTask: asset $asset last task id is NOW: ${assetsLatestTask[asset.id]}"
			} else {
				log.debug "assignToAssetsLatestTask: previous task was newer LIST: ${assetsLatestTask[asset.id].taskSpec} ARG: $task.taskSpec"
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
	 * @throws RuntimeException when used in appropriately
	 */
	private void setDeferment(AssetComment task, AssetEntity asset, String predSucc, String key, Map settings) {

		boolean isPredSucc = predSucc == 'p'

		assert (asset instanceof AssetEntity)

		if (! asset) {
			throw new RuntimeException("Use of ${predSucc=='p' ? 'predecessor' : 'successor'}.defer only allowed for asset based tasks (Task Spec $settings.taskSpec.id)")
		}

		def map = [:].withDefault {[]}

		if(isPredSucc){
			if(task.getTmpDefPred() != null){
				map = task.getTmpDefPred()
			}else{
				task.setTmpDefPred(map)
			}
		}else{
			if(task.getTmpDefSucc() != null){
				map = task.getTmpDefSucc()
			}else{
				task.setTmpDefSucc(map)
			}
		}

		def hasKey = map.containsKey(key)

		if (! hasKey || (hasKey && ! map[key].contains(asset.id))){
				map[key] << asset.id
		}


		log.debug "setDeferment: key:$key, mode:$predSucc, $map, task:$task, asset:$asset"
		// Mark the task as already having a successor so as to prevent it from getting wired to a gateway or milestone
		if (! task.getTmpHasSuccessorTaskFlag()){
			task.setTmpHasSuccessorTaskFlag(true)
		}

	}

	/**
	 * Use to find list of all tasks that have outstanding deferments
	 * @param Map The list of all of the present tasks
	 * @param String Used to filter types of deferment if desired. Options include p=predecessor, s=successor, e=either (default)
	 * @return List<Map> containing a map of all of the tasks that have outstanding deferments. The map contains task, type (predecessor|successor), the key, asset id
	 */
	private List<Map> getOutstandingDeferments(Map taskList, predSuccEither='e') {
		def list = []
		if (['p','e'].contains(predSuccEither)) {
			taskList.each { id, task ->
				if (task.getTmpDefPred()) {
					task.getTmpDefPred().each { key, assets ->
						assets.each { asset ->
							list << [task:task, type:'predecessor', key:key, assetId:asset]
						}
					}
				}
			}
		}
		if (['s','e'].contains(predSuccEither)) {
			taskList.each { id, task ->
				if (task.getTmpDefSucc()) {
					task.getTmpDefSucc().each { key, assets ->
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
	 */
	private int gatherDeferment(Map taskList, Map assetsLatestTask, AssetComment task, String predSucc, List keys, Map settings, StringBuilder out) {
		boolean isPred = predSucc == 'p'


		int depCount = 0
		def assetId = task.assetEntity?.id

		log.debug "gatherDeferment: Starting - assetId:$assetId, task:$task, predSucc:$predSucc, keys:$keys"

		// First attempt to find all tasks that have the specified deferment by looking for the deferment key and then find those with the source asset
		taskList.each { id, t ->

			def map = null
			if(isPred){
				map = t.getTmpDefPred()
			}else{
				map = t.getTmpDefSucc()
			}

			if(map != null){
				keys.each { key ->

					if (map.containsKey(key) && map[key].contains(assetId)) {
						// Setup the proper dependencies as necessary
						if (predSucc == 'p') {
							log.debug "gatherDeferment: Gathered Predecessor - pred($t), succ($task)"
							depCount += createTaskDependency(task, t, assetsLatestTask, settings, out)
						} else {
							log.debug "gatherDeferment: Gathered Successors - pred($t), succ($task)"
							depCount += createTaskDependency(t, task, assetsLatestTask, settings, out)
						}

						// Now remove the asset deferral from the task and if the key is empty, then remove that too
						map[key].remove(assetId)
						if (map[key].size()==0) {
							map.remove(key)
							//log.debug "gatherDeferment: emptied deferment for $field[$key]"
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
	Map<String,List> fetchGroups(recipe, contextObject, exceptions, project) {
		def groups = [:]

		if (!(recipe instanceof Map)) {
			throw new InvalidParamException('The recipe must be of the LinkedHashMap type')
		}

		// First load up the 'groups' if any are defined
		if (recipe.containsKey('groups')) {
			if (!(recipe.groups instanceof List)) {
				throw new InvalidParamException('The recipe.groups element must the List type')
			}

			def gCount = 0
			recipe.groups.each { g ->
				gCount++
				if (!g.name || g.name.size() == 0) {
					msg = "Group specification #$gCount missing required 'name' property"
					throw new InvalidParamException(msg)
				}

				if (g.filter?.containsKey('taskSpec')) {
					msg = "Group specification ($g.name) references a taskSpec which is not supported"
					throw new InvalidParamException(msg)
				}

				groups[g.name] = findAllAssetsWithFilter(contextObject, g, groups, exceptions, project)
				if (groups[g.name].size() == 0) {
					// exceptions.append("Found zero (0) assets for group $g.name<br>")
				} else {
					log.info "Group $g.name contains: ${groups[g.name].size()} assets"
				}
			}
		}

		return groups
	}

	/**
	 * This special method is used to find all assets of a moveEvent that match the criteria as defined in the filter map
	 * TODO : JPM 9/2014 : findAllAssetsWithFilter needs to be updated in order to support generating tasks for contexts other than event and bundle
	 * @param contextObject - the object that the filter is applied to (either MoveEvent or MoveBundle)
	 * @param Map filter - contains various attributes that define the filtering
	 * @param loadedGroups Map<List> - A mapped list of the groups and associated assets that have already been loaded
	 * @return List<AssetEntity, Application, Database, File> based on the filter
	 * @throws RuntimeException if query fails or there is invalid specifications in the filter criteria
	 */
	def findAllAssetsWithFilter(contextObject, groupOrTaskSpec, loadedGroups, exceptions, project) {
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

		if (filter?.containsKey('group')) {
			//
			// HANDLE filter.group
			//

			//log.info("Groups contains $groups")
			//log.info("findAllAssetsWithFilter: group $filter.group")

			// Put the group property into an array if not already an array
			def groups = CU.asList(filter.group)

			// Iterate over the list of groups
			groups.each { groupCode ->
				if (groupCode.size() == 0) {
					log.error("findAllAssetsWithFilter: 'filter.group' value ($filter.group) has undefined group code.")
					throw new RuntimeException("'filter.group' value ($filter.group) has undefined group code.")
				}

				// Find all of the assets of the specified GROUP
				// log.info("assetsLatestTask has ${assetsLatestTask.size()} assets")
				if (loadedGroups.containsKey(groupCode)) {
					assets.addAll(loadedGroups[groupCode])
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
			addFilters = (filter.containsKey('asset') && (filter.asset instanceof Map) && assets.size() > 0)
			if (addFilters) {
				// Update the WHERE clause to only include the assets in the group
				where = SqlUtil.appendToWhere(where, 'a.id in (:groupAssets)')
				map.groupAssets = assets*.id
			}

		} else if (filter?.containsKey('taskSpec')) {
			//
			// HANDLE filter.taskSpec
			//

			log.debug("findAllAssetsWithFilter: taskSpec $filter.taskSpec")

			// Put the group property into an array if not already an array
			def filterTaskSpecs = CU.asList(filter.taskSpec)

			// Iterate over the list of groups
			filterTaskSpecs.each { ts ->
				// Find all predecessor tasks that have the taskSpec ID # and then add the tasks assetEntity to the assets list
				def predecessorTasks = []
				taskList.each { id, t ->
					if (t.taskSpec.toString() == ts.toString()) {
						predecessorTasks << t
					}
				}

				if (predecessorsTasks.size() > 0) {
					if  (predecessorsTasks[0].assetEntity) {
						assets.addAll(predecessorsTasks*.assetEntity)
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
			if (filter?.containsKey('include')) {

				// If the task spec references groups with an Include then we don't need the 'class' specification since it is inherent from the group
				// TODO : extract the class from the groups
				// TODO : Verify that if there are multiple includes, that they are from the same class

				// Put the include property into an array if not already an array
				def includes = CU.asList(filter.include)
				def incIds = []

				// Iterate over the list of groups
				includes.each { incGroup ->
					if (incGroup.size() == 0) {
						msg = "filter.include specified without any group codes $filter.include"
						log.warn("findAllAssetsWithFilter: $msg")
						throw new RuntimeException(msg)
					}

					// Find all of the assets of the specified GROUP and add their IDs to the list
					if (loadedGroups.containsKey(incGroup)) {
						incIds.addAll(loadedGroups[incGroup]*.id)
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
					msg = "Unable to create SQL for filter.include ($filter.include)"
					log.error "SqlUtil.whereExpression error - $msg - $incIds"
					throw new RuntimeException(msg)
				}
			}

			def queryOn = filter?.containsKey('class') ? filter['class'].toLowerCase() : 'device'

            /**
             * A helper closure used below to manipulate the 'where' and 'map' variables to add additional
             * WHERE expressions based on the properties passed in the filter
             * @param String[] - list of the properties FROM fieldSpecs of project asset domain to examine
             */
            def addWhereConditionsForFieldSpecs = { fieldSpecs ->
                fieldSpecs.each { customField ->
                    if (filter?.asset?.containsKey(customField.label)) {
                        sm = SqlUtil.whereExpression('a.' + customField.field, filter.asset[customField.label], customField.field)
                        if (sm) {
                            where = SqlUtil.appendToWhere(where, sm.sql)
                            if (sm.param) {
                                map[customField.field] = sm.param
                            }
                        } else {
                            log.error "SqlUtil.whereExpression unable to resolve $customField.field expression [${filter.asset[customField.field]}]"
                        }
                    }
                }
            }

			/**
			 * A helper closure used below to manipulate the 'where' and 'map' variables to add additional
			 * WHERE expressions based on the properties passed in the filter
			 * @param String[] - list of the properties to examine
			 */
			def addWhereConditions = { list ->
				// log.debug "addWhereConditions: Building WHERE - list:$list, filter=$filter"
				list.each { code ->
					if (filter?.asset?.containsKey(code)) {
						log.debug("addWhereConditions: code $code matched")
						sm = SqlUtil.whereExpression('a.' + code, filter.asset[code], code)
						if (sm) {
							where = SqlUtil.appendToWhere(where, sm.sql)
							if (sm.param) {
								map[code] = sm.param
							}
						} else {
							log.error "SqlUtil.whereExpression unable to resolve $code expression [${filter.asset[code]}]"
						}
					}
				}
			}

			/**
			 * A helper closure used below to manipulate the 'where' and 'map' variables to add additional
			 * WHERE expressions based on the properties passed in the filter
			 * @param String[] - list of the properties to examine
			 */
			def addJoinWhereConditions = { String alias, fieldMap ->
				log.debug "addJoinWhereConditions: Building WHERE - alias:$alias, fieldMap:$fieldMap, filter=$filter"
				fieldMap.each { propertyName, columnName ->
					if (filter?.asset?.containsKey(propertyName)) {
						log.debug("addJoinWhereConditions: $propertyName/$columnName matched")
						sm = SqlUtil.whereExpression(alias + '.' + columnName, filter.asset[propertyName], propertyName)
						if (sm) {
							where = SqlUtil.appendToWhere(where, sm.sql)
							if (sm.param) {
								map[propertyName] = sm.param
							}
						} else {
							log.error "SqlUtil.whereExpression unable to resolve '$propertyName' expression [${filter.asset[propertyName]}]"
						}
					}
				}
			}

			/**
			 * TM-11900 This is a hack to bypass an underlying issue where this method receives a list of tag ids
			 * when fetching groups, but a list of maps when running the task generation. This wasn't the behavior until
			 * a couple of days ago.
			 */
			def getTagIdsList = {List tags ->
				List<Long> tagIds = []
				tags.each {tag ->
					def tagId
					if (NumberUtil.isaNumber(tag)) {
						tagId = tag
					} else {
						tagId = tag.id
					}
					tagIds << NumberUtil.toPositiveLong(tagId)
				}
				return tagIds

			}

            if (filter?.asset && project) {
                def fieldSpecs = customDomainService.fieldSpecs(project, queryOn, CustomDomainService.ALL_FIELDS,['field', 'label'])
                if (fieldSpecs) {
                    // Add WHERE clauses based on the field specs (custom fields) configured by project asset.
                    addWhereConditionsForFieldSpecs(fieldSpecs)
                }
            }
			// Add WHERE clauses based on the following properties being present in the filter.asset (Common across all Asset Classes)
			addWhereConditions(commonFilterProperties)

			//
			// Param 'exclude'
			// Handle exclude filter parameter that will add a NOT IN () cause to the where for references to one or more groups
			//
			if (filter?.containsKey('exclude')) {
				def excludes = []
				def excludeProp = CU.asList(filter.exclude)
				excludeProp.each { exGroup ->
					if (loadedGroups?.containsKey(exGroup)) {
						excludes.addAll(loadedGroups[exGroup])
					} else {
						exceptions.append("Filter 'exclude' reference undefined group ($exGroup) for filter $filter<br>")
					}
				}
				if (excludes.size() > 0) {
					where = SqlUtil.appendToWhere(where, 'a.id NOT in (:excludes)')
					map.excludes = excludes*.id
				}
				log.debug "findAllAssetsWithFilter: excluding group(s) [$filter.exclude] that has ${excludes.size()} assets"
			}

			where = SqlUtil.appendToWhere(where, 'a.moveBundle.useForPlanning = true')

			map.bIds = getBundleIds(contextObject, project, filter)
			String join = ''

			if (contextObject.tag) {
				where = SqlUtil.appendToWhere(where, 't.id in (:tags)')
				map.tags = contextObject.getTagIds()
				join = 'LEFT OUTER JOIN a.tagAssets ta LEFT OUTER JOIN ta.tag t'

				// When using tags, bundles are going to be ignored
				map.remove('bIds')
			} else if (map.bIds) {
				where = SqlUtil.appendToWhere(where, "a.moveBundle.id IN (:bIds)")
			} else {
				throw new IllegalArgumentException('The selected event has no assigned bundles')
			}

			// Assemble the SQL and attempt to execute it
			try {
				switch(queryOn) {

					case 'device':
						where = SqlUtil.appendToWhere(where, 'a.assetClass=:assetClass')
						map.assetClass = AssetClass.DEVICE
						if (filter?.asset?.containsKey('virtual')) {
							// Just Virtual devices
							where = SqlUtil.appendToWhere(where, "a.assetType IN (:vm_types)")
							map.vm_types = AssetType.virtualServerTypes
						} else if (filter?.asset?.containsKey('physical')) {
							// Just Physical devices
							where = SqlUtil.appendToWhere(where, "IFNULL(a.assetType,'') NOT IN (:vm_types)")
							map.vm_types = AssetType.virtualServerTypes
						}

						// Add any devices specific attribute filters
						addWhereConditions( deviceFilterProperties )
						// Deal with 'sourceLocation', 'sourceRack', 'sourceRoom', 'targetLocation', 'targetRack', 'targetRoom', since these are joins

						def sb = new StringBuilder('')

						Map assetFilter = filter?.asset

						if (assetFilter) {
							// Do joins to the Room and Rack domains as necessary and add the appropriate WHERE
							if (assetFilter.keySet().find {['sourceLocation', 'sourceRoom'].contains(it)}) {
								sb.append('LEFT OUTER JOIN a.roomSource as roomSrc ')
								addJoinWhereConditions('roomSrc', ['sourceLocation':'location', 'sourceRoom':'roomName'])
							}

							if (assetFilter.containsKey('sourceRack' )) {
								sb.append('LEFT OUTER JOIN a.rackSource as rackSrc ')
								addJoinWhereConditions('rackSrc', ['sourceRack':'tag'])
							}

							if (assetFilter.keySet().find {['targetLocation', 'targetRoom'].contains(it)}) {
								sb.append('LEFT OUTER JOIN a.roomTarget as roomTgt ')
								addJoinWhereConditions('roomTgt', ['targetLocation':'location', 'targetRoom':'roomName'])
							}

							if (assetFilter.containsKey('targetRack')) {
								sb.append('LEFT OUTER JOIN a.rackTarget as rackTgt ')
								addJoinWhereConditions('rackTgt', ['targetRack':'tag'])
							}
						}


						join = "$join ${sb.toString()}"
						break

					case 'application':
						where = SqlUtil.appendToWhere(where, 'a.assetClass=:assetClass')
						map.assetClass = AssetClass.APPLICATION
						// Add additional WHERE clauses based on the following properties being present in the filter (Application domain specific)
						addWhereConditions(['appVendor','sme','sme2','businessUnit','criticality', 'shutdownBy', 'startupBy', 'testingBy'])
						break

					case 'database':
						where = SqlUtil.appendToWhere(where, 'a.assetClass=:assetClass')
						map.assetClass = AssetClass.DATABASE
						// Add additional WHERE clauses based on the following properties being present in the filter (Database domain specific)
						addWhereConditions(['dbFormat','size'])
						break

					case ~/files|file|storage/:
						where = SqlUtil.appendToWhere(where, 'a.assetClass=:assetClass')
						map.assetClass = AssetClass.STORAGE
						// Add additional WHERE clauses based on the following properties being present in the filter (Database domain specific)
						addWhereConditions(['fileFormat','size', 'scale', 'LUN'])
						break

					default:
						log.error "Invalid class '$queryOn' specified in filter ($filter)<br>"
						throw new RuntimeException("Invalid class '$queryOn' specified in filter ($filter)")
						break
				}

				// Add tag filtering if specified
				where = addTagFilteringToWhere(filter, map,  where, project.id)

				// Construct the HQL that will be used to query for the assets
				sql = "SELECT distinct(a) FROM AssetEntity a $join where $where"

				log.debug "findAllAssetsWithFilter: sql=$sql, map=$map"

				assets = AssetEntity.executeQuery(sql, map)
			} catch (e) {
				msg = "An unexpected error occurred while trying to locate assets for filter $filter:"
				log.error "{}\nSQL: {}\nPARAMS: {}\n{}", msg, sql, map, ExceptionUtil.stackTraceToString(e)
				throw new RuntimeException(msg)
			}

			//
			// Handle filter.dependency if specified and there were assets found
			//
			// This will instead of returning the assets found above, it will return the assets found through dependencies. It also provides
			// some limited filtering of the dependents.
			//
			if (filter?.containsKey('dependency') && assets.size()) {
				log.debug "findAllAssetsWithFilter: Processing filter.dependency: master list ${assets*.id}"
				try {
					// Now we need to find assets that are associated via the AssetDependency domain
					// TODO - need to add ability to filter on the additional fields of an AssetDependency
					// TODO - currently getting duplicate assets at times
					def depMode = filter.dependency.mode[0].toLowerCase()
					def daProp='asset'
					if (depMode == 'r') {
						sql = 'from AssetDependency ad where ad.dependent.id in (:assetIds)'
					} else {
						sql = 'from AssetDependency ad where ad.asset.id in (:assetIds)'
						daProp = 'dependent'
					}

					Map findDepMap = [assetIds:assets*.id]

					// Iterate over the filterable properties for the AssetDependency table
					List dependencyPropsToFilterOn = ['c1', 'c2', 'c3', 'c4', 'comment', 'dataFlowFreq', 'dataFlowDirection', 'type', 'status']
					dependencyPropsToFilterOn.each { depPropName ->
						if (filter.dependency.containsKey(depPropName)) {
							Map sqlWhereMap = SqlUtil.whereExpression(depPropName, filter.dependency[depPropName], depPropName)
							if (sqlWhereMap) {
								sql = SqlUtil.appendToWhere(sql, sqlWhereMap.sql)
								if (sqlWhereMap.param) {
									findDepMap[depPropName] = sqlWhereMap.param
								}
							} else {
								log.error "Unable to resolve filter.dependency.$depPropName expression [${filter.dependency[depPropName]}]"
							}
						}
					}
					log.debug "filter.dependency query=$sql, params=$findDepMap"

					def depAssets = AssetDependency.findAll(sql, findDepMap)
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
						if (map.bIds.contains(asset.moveBundle.id)) {
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
										if (['virtual', 'vm'].contains(asset.assetType.toLowerCase()))
											dependent = asset
									} else if (chkPhysical) {
										if (! ['application', 'database', 'files', 'virtual', 'vm'].contains(asset.assetType.toLowerCase()))
											dependent = asset
									} else {
										if (! ['application', 'database', 'files'].contains(asset.assetType.toLowerCase()))
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
					log.error "An unexpected error occurred - $e.message\n${ExceptionUtil.stackTraceToString(e)}"
					throw e
				}
				// TODO : make this list distinct
			}
		}

		return assets
	}

	// Used by the addTagFilteringToWhere method to build TAG filtering
	static final String TAG_WHERE_SUBSELECT_ANY = 'SELECT DISTINCT(taws.asset.id) FROM TagAsset taws WHERE '
	static final String TAG_WHERE_SUBSELECT_ANY_IN = 'taws.tag.name IN (:tagNameList)'
	static final String TAG_WHERE_SUBSELECT_ALL = 'SELECT taws.asset.id FROM TagAsset taws WHERE '
	static final String TAG_WHERE_SUBSELECT_ALL_GROUPBY = 'GROUP BY taws.asset.id HAVING count(*) = :tagListSize'
	static final String TAG_WHERE_ASSET_PROJECT = 'taws.asset.project.id = :projectId AND ('

	/**
	 * Used to add query logic to incorporate tag filtering that will look at the filter.tag property
	 * and append to the existing SQL WHERE criteria. The query will consider the tags as ANY (default) or ALL
	 * based on the presents of the filter.tagMatch property equaling 'ALL' or 'ANY'.
	 *
	 * @param filter - the filter that is being applied
	 * @param parametersMap - the map that contains all of the parameters in the SQL
	 * @param whereSql - the SQL that is being built up for the overall query
	 * @return the whereSql with additional SQL based on filter.tag being populated
	 */
	String addTagFilteringToWhere(Map filter, Map parametersMap, String whereSql, Long projectId) {
		// Check whether the filter includes tag fields.
		if (filter?.tag) {
			List<String> tagNames = CU.asList(filter.tag)
			int tagListSize = tagNames.size()

			// Pull out the tags that have '%' in them as those will be handled with LIKE criteria
			List<String> likeTags = tagNames.findAll { it.contains('%') }
			if (likeTags) {
				tagNames = tagNames - likeTags
			}

			parametersMap.put('projectId', projectId)

			String query = null

			// Extra separator that might need to be added between the IN and LIKE expressions.
			String clauseSeparator = ''

			// Closure used to build up the WHERE for any whole tags in an IN criteria
			Closure processInTags = {
				// Add exact match tag query
				if (tagNames.size() > 0) {
					query += TAG_WHERE_SUBSELECT_ANY_IN
					parametersMap.put('tagNameList', tagNames)
					clauseSeparator = ' OR '
				}
			}

			// Closure used to build up the WHERE for any tags containing a % as a LIKE statement
			Closure processLikeTags = {
				List<String> likeClauses = []
				// Iterate over the parameters
				for (int i = 0; i < likeTags.size(); i++) {
					String paramName = 'tagName_' + (i+1)
					likeClauses << "taws.tag.name LIKE :$paramName"
					parametersMap.put(paramName, likeTags[i])
				}
				String likeClause = likeClauses.join(' OR ')
				if (likeClause) {
					query = "$query$clauseSeparator$likeClause"
				}
			}

			// Build the query based on the of tagMatch ALL or ANY (AND or OR)
			if (filter.tagMatch == 'ALL') {
				parametersMap.put('tagListSize', NumberUtil.toLong(tagListSize))
				query = TAG_WHERE_SUBSELECT_ALL + TAG_WHERE_ASSET_PROJECT
				processInTags()
				processLikeTags()
				// Add the HAVING that will filter out all assets that do NOT have all of the tags
				query += ') ' + TAG_WHERE_SUBSELECT_ALL_GROUPBY
			} else {
				query = TAG_WHERE_SUBSELECT_ANY + TAG_WHERE_ASSET_PROJECT
				processInTags()
				processLikeTags()
				query += ')'
			}

			return SqlUtil.appendToWhere(whereSql, "a.id IN ($query)")
		} else {
			return whereSql
		}
	}

	/**
	 * Used to retrieve a list of Bundle IDs that are associated with the Recipe Event/Tag context
	 * @param contextObject - the Context JSON parsed
	 * @param project - the current project
	 * @param filter
	 * @return a list of MoveBundle IDs
	 */
	List getBundleIds(contextObject, Project project = null, Map filter = null) {
		List bundleIds = []
		// Now find the bundles if the contextObject had an event id, but not bundle ids
		if (contextObject.eventId) {
			bundleIds = MoveBundle.where {
				moveEvent.id == contextObject.eventId
			}.projections {
				property 'id'
			}.list()

			// See if they are trying to filter on the Bundle Name
			// TODO : JPM 7/2018 : I don't believe that this holds true any longer unless this is used in the recipes themselves
			if (project && filter?.asset?.containsKey('bundle')) {
				MoveBundle moveBundle = MoveBundle.findByProjectAndName(project, filter.asset.bundle)
				if (moveBundle) {
					bundleIds = [moveBundle.id]
				} else {
					throw new RuntimeException("Bundle name ($filter.moveBundle) was not found for filter: $filter")
				}
			}
		}
		return bundleIds
	}

	/**
	 * Generates action tasks that optionally linking assets as predecessors. It presently supports (location, room, rack,
	 * cart and truck) groupings. It will create a task for each unique grouping and inject the list assets in the group into the associatedAssets
	 * property to later be used to create the predecessor dependencies.
	 *
	 * @param String action - options [rack, cart, truck]
	 * @param contextObj - the context for which we are generating the tasks (e.g. MoveEvent, MoveBundle, Application)
	 * @param Person whom - who is creating the tasks
	 * @param List<Person> - references of all staff associated with the project
	 * @param Integer recipeId
	 * @param Map taskSpec
	 * @param List? groups
	 * @param StringBuilder exceptions
	 * @return List<AssetComment> the list of tasks that were created
	 */
	def createAssetActionTasks(action, contextObj, whom, projectStaff, recipeId, taskSpec, groups, workflow, settings, exceptions) {
		def taskList = []
		String loc 			// used for racks
		def msg


		// Get all the assets
		def assetsForAction = findAllAssetsWithFilter(contextObj, taskSpec, groups, exceptions, settings.project)

		// If there were no assets we can bail out of this method
		if (assetsForAction.size() == 0) {
			return taskList
		}

		def tasksToCreate = []	// List of tasks to create

		// Identify the MoveEvent if possible
		MoveEvent moveEvent = settings.event

		// We will put all of the assets into the array. Below the unique method will be invoke to create a subset of entries
		// for which we'll create tasks from.
		tasksToCreate.addAll(0, assetsForAction)

		// The following two variables will be assigned closures in the switch statement below, which will be subsequently used
		// in the loop it go through the array of assetsForAction.
		//
		def findAssets			// Finds all the assets in the assetsForAction list that match the action
		def validateForTask		// Used in iterator over the assets to determine if it should be associated to the current task

		def groupOn = action
		switch(action) {
			case 'set':
				if (! taskSpec.containsKey('setOn') || taskSpec.setOn.size() == 0) {
					throw new RuntimeException("Taskspec ($taskSpec.id) is missing required 'setOn' attribute")
				}
				groupOn = taskSpec.setOn
				// TODO : add logic to convert custom labels to the appropriate custom# entry

				// Validate that the setOn attribute references a valid property
				if (! assetsForAction[0].properties.containsKey(groupOn)) {
					throw new RuntimeException("Taskspec ($taskSpec.id) setOn attribute references an undefined property ($groupOn). <br>Properties include [${assetsForAction[0].properties.keySet().join(', ')}]")
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
				if (taskSpec.containsKey('disposition')) {
					loc = taskSpec.disposition.toLowerCase()
					if (! ['source','target'].contains(loc)) {
						msg = "$action action taskspec ($taskSpec.id) has invalid value ($taskSpec.location) for 'disposition' property"
						log.error "$msg - context $contextObj"
						throw new RuntimeException(msg)
					}
				} else {
					msg = "$action taskspec ($taskSpec.id) requires 'disposition' property"
					log.error "$msg - context $contextObj"
					throw new RuntimeException("$msg (options 'source', 'target')")
				}

				String locationDispositionName = loc + 'LocationName'
				String rackDispositionName =  loc + 'RackName'
				String roomDispositionName = loc + 'RoomName'

				switch(action) {
					case 'location':
						// Get the Distinct Racks from the list which includes the location and room as well
						tasksToCreate.unique { it[locationDispositionName] }
						findAssets = { asset ->
							assetsForAction.findAll { it[locationDispositionName] == asset[locationDispositionName] }
						}
						break
					case 'room':
						// Get the Distinct Racks from the list which includes the location and room as well
						tasksToCreate.unique { it[locationDispositionName] + ':' + it[roomDispositionName] }
						findAssets = { asset ->
							assetsForAction.findAll {
								it[locationDispositionName] == asset[locationDispositionName] &&
								it[roomDispositionName] == asset[locationDispositionName] }
						}
						break
					case 'rack':
						// Get the Distinct Racks from the list which includes the location and room as well
						// log.info "%% assetsForAction before ${assetsForAction.size()}"
						tasksToCreate.unique { it[locationDispositionName] + ':' + it[roomDispositionName] + ':' + it[rackDispositionName] }
						// log.info "%% assetsForAction after ${assetsForAction.size()}"
						findAssets = { asset ->
							assetsForAction.findAll {
								// log.info "Finding match to $asset in ${assetsForAction.size()}"
								it[locationDispositionName] == asset[locationDispositionName] &&
								it[roomDispositionName] == asset[roomDispositionName] &&
								it[rackDispositionName] == asset[rackDispositionName] }
						}
						break

				}
				validateForTask = { asset -> asset[loc + action.capitalize() + 'Name']?.size() > 0 }
				break

			default:
				msg = "Unhandled action ($action) for taskspec ($taskSpec.id) in createAssetActionTasks method"
				log.error "$msg - moveEvent $moveEvent"
				throw new RuntimeException(msg)
		}

		log.info("Found ${tasksToCreate.size()} $action for createAssetActionTasks and assetsForAction=${assetsForAction.size()}")

		def template = new Engine().createTemplate(taskSpec.title)

		tasksToCreate.each { ttc ->

			if (validateForTask(ttc)) {
				def map	= [:]
				// TODO : SequenceService - Change to use new SequenceService
				def task = new AssetComment(
					taskNumber: sequenceService.next(settings.clientId, 'TaskNumber'),
					taskBatch: settings.taskBatch,
					isPublished: settings.publishTasks,
					sendNotification: taskSpec.sendNotification ?: false,
					project: settings.project,
					moveEvent: moveEvent,
					commentType: AssetCommentType.TASK,
					status: ACS.READY,
					createdBy: whom,
					displayOption: 'U',
					autoGenerated: true,
					recipe: recipeId,
					taskSpec: taskSpec.id)

				// Setup the map used by the template
				switch(action) {
					case 'set':
						map = [set: ttc[groupOn]]
						break

					case 'truck':
						map = [truck: ttc.truck]
						break

					case 'cart':
						map = [truck:ttc.truck, cart:ttc.cart]
						break

					// location/room/rack compound adding the details to the map
					case 'rack':
						map.rack = ttc[loc + 'RackName'] ?: ''
					case 'room':
						map.room = ttc[loc + 'RoomName'] ?: ''
					case 'location':
						map.location = ttc[loc + 'LocationName'] ?: ''
						break
				}
				try {
					task.comment = template.make(map).toString()
				} catch (Exception ex) {
					exceptions.append("Unable to evaluate title ($taskSpec.title) of TaskSpec $taskSpec.id with map=$map<br>")
					task.comment = '** Unable to evaluate title **'
				}
				log.info "Creating $action task - $task"

				// Handle the various settings from the taskSpec
				task.priority = taskSpec.containsKey('priority') ? taskSpec.priority : 3
				if (taskSpec.containsKey('duration')) task.duration = taskSpec.duration
				if (taskSpec.containsKey('team')) task.role = taskSpec.team
				if (taskSpec.containsKey('category')) task.category = taskSpec.category

				task.durationLocked = taskSpec.containsKey('durationLocked') ? taskSpec.durationLocked.toBoolean() :  false

				// TODO - Normalize this logic and sadd logic to update from Workflow if exists

				if (!save(task, true)) {
					throw new RuntimeException("Error while trying to create task - error=${GormUtil.allErrorsString(task)}")
				}

				// Determine all of the assets for the action type/key and then inject them into the task, which will be used
				// by the dependency logic above to wire to predecessors.
				def assocAssets = []
				if (! (taskSpec.containsKey('predecessor') && taskSpec.predecessor.containsKey('ignore')))  {
					// If we're not ignorning the predecessor
					assocAssets = findAssets(ttc)
					if (assocAssets.size() == 0) {
						exceptions.append("Unable to find expected assets for $action in TaskSpec($taskSpec.id)<br>")
						log.error "$msg on event $moveEvent"
						throw new RuntimeException(msg)
					}
					// log.info "Added ${assocAssets.size()} assets as predecessors to task $task"
					// assocAssets.each { log.info "Asset $it" }
				}
				task.setTmpAssociatedAssets(assocAssets)

				// Perform the AssignedTo logic
				msg = assignWhomAndTeamToTask(task, taskSpec, workflow, projectStaff, settings)
				if (msg)
					exceptions.append(msg).append('<br>')

				// Set any scheduling constraints on the task
				setTaskConstraints(task, taskSpec, workflow, projectStaff, exceptions)

				taskList << task
			}
		}

		return taskList
	}

	/**
	 * Generate Roll-Call Tasks for a specified event which will create a sample task for each individual
	 * that is assigned to the Event which lists their name and their TEAM assignment(s).
	 * @param moveEvent
	 * @param category
	 * @return List<AssetComment> the list of tasks that were created
	 */
	private List createRollcallTasks(whom, recipeId, taskSpec, settings) {

		def taskList = []
		def staffList = MoveEventStaff.findAllByMoveEvent(settings.moveEvent, [sort:'person'])
		log.info("createRollcallTasks: Found ${staffList.size()} MoveEventStaff records")

		def lastPerson = (staffList && staffList[0]) ? staffList[0].person : null
		def teams = []

		// Create closure because we need to do this twice in the loop logic below
		def createActualTask = {
			def title = "Rollcall for $lastPerson (${teams.join(', ')})"
			log.info "createRollcallTasks: creating task $title"
			// TODO : SequenceService - Change to use new SequenceService
			boolean durationLocked = false
			if (taskSpec.durationLocked) {
				durationLocked = taskSpec.durationLocked.toBoolean()
			}
			def task = new AssetComment(
				taskNumber: sequenceService.next(settings.clientId, 'TaskNumber'),
				taskBatch: settings.taskBatch,
				isPublished: settings.publishTasks,
				sendNotification: taskSpec.sendNotification ?: false,
				comment: title,
				project: settings.moveEvent.project,
				moveEvent: settings.moveEvent,
				commentType: AssetCommentType.TASK,
				status: ACS.READY,
				createdBy: whom,
				displayOption: 'U',
				autoGenerated: true,
				hardAssigned: 1,
				assignedTo: lastPerson,
				recipe: recipeId,
				taskSpec: taskSpec.id,
				apiAction:settings.apiAction,
				durationLocked: durationLocked)

			// Handle the various settings from the taskSpec
			if (taskSpec.containsKey('category')) task.category = taskSpec.category

			if (! (task.save(flush:true))) {
				log.error "createRollcallTasks: failed to create task for $lastPerson on moveEvent $settings.moveEvent"
				throw new RuntimeException("Error while trying to create task. error=${GormUtil.allErrorsString(task)}")
			}

			taskList << task
		}

		// Iterate over the list of Staff and create tasks if they have Logins
		staffList.each { staff ->
			// Create a task if we're at the end of the Staff(person) and the person has a UserLogin
			if (lastPerson && (lastPerson.id != staff?.person?.id)) {
				if (lastPerson.userLogin) {
					createActualTask()
				}
				lastPerson = staff.person
				teams = []
			}
			teams << staff.role.id
		}
		if (teams && lastPerson.userLogin) {
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
	 * @param settings : general settings for task generation.
	 * @return Return null if successfor or a String error message indicating the cause of the failure
	 */
	private String assignWhomAndTeamToTask(AssetComment task, Map taskSpec, workflow, List projectStaff, Map settings) {
		def msg
		def msg1 = assignTeam(task, taskSpec, workflow, projectStaff, settings)
		def msg2 = assignWhom(task, taskSpec, workflow, projectStaff, settings)

		if (msg1) {
			if (msg2) {
				msg = msg1 + " and " + msg2
			} else {
				msg = msg1
			}
		} else {
			msg = msg2
		}

		// See if the above code ran into any errors
		if (msg != null) {
			msg += " for task #$task.taskNumber $task.comment, taskSpec ($taskSpec.id)"
			log.warn "assignWhomToTask() $msg, project $task.project.id"
		}

		return msg
	}

	/**
	 * Used to assign a person to a task based on references in the task spec that handles direct and indirect references
	 * of the 'whom' property. It will handle references of:
	 *    #fieldName - indirect field reference (lookup the value from the referenced field e.g. #testingBy)
	 *    @teamName - name lookup (begins with @) - assigns the team instead of 'whom'
	 *    user@domain.com - lookup person by their email (contains @)
	 *    99999 - person id number
	 *    first lastName - lookup person by their name
	 *
	 * @param task - the task object to perform the assignment on
	 * @param taskSpec - the map with all of the task
	 * @param settings - map of settings for task generation.
	 */
	private String assignWhom(AssetComment task, Map taskSpec, workflow, List projectStaff, Map settings) {
		def person

		if (taskSpec.containsKey('whom') && taskSpec.whom.size() > 1) {
			def whom = taskSpec.whom

			log.debug "assignWhomToTask() whom=$whom, task $task"

			// See if we have an indirect reference and if so, we will lookup the reference value that will result in either a person's name or @TEAM
			if (whom[0] == '#') {
				// log.debug "assignWhomToTask()  performing indirect lookup whom=$whom, task $task"
				if (! task.assetEntity) {
					return "Illegally used whom property reference ($whom) on non-asset"
				}

				try {
					whom = AssetEntityHelper.getIndirectPropertyRef(task.assetEntity, whom)
					if (whom instanceof Person) {
						// The indirect lookup returned a person so we don't need to go any further!
						person = whom
					} else if (whom?.isNumber()) {
						def whomId = whom.toLong()
						person = projectStaff.find { it.id == whomId }
						if (! person) {
							// Look if the person exist
							person = Person.get(whomId)
							if (person) {
								return "Person $person ($whom) is not in project staff ($taskSpec.whom), asset name: ${task?.assetEntity?.assetName}."
							} else {
								return "Person id $whom not exist ($taskSpec.whom), asset name: ${task?.assetEntity?.assetName}."
							}
						}
					} else if (! whom || whom.size() == 0) {
						return "Unable to resolve indirect whom reference ($taskSpec.whom), asset name: ${task?.assetEntity?.assetName}."
					}

					//
					// If we got here, then the indirect either referenced a @team or a 'name', which will be resolved below
					//

				} catch (e) {
					log.error "assignWhom: $e.message\n${ExceptionUtil.stackTraceToString(e)}"
					return "$e.message, whom ($taskSpec.whom), asset name: ${task?.assetEntity?.assetName}."
				}
			}

			if (whom instanceof String) {
				if (whom[0] == '@') {
					// team reference
					def teamAssign = whom[1..-1]
					List teamCodeList = settings["teamCodes"]
					if (teamCodeList.contains(teamAssign)) {
						task.role = teamAssign
					} else {
						return "Unknown team ($taskSpec.team) indirectly referenced"
					}
				} else if (whom.contains('@')) {

					// See if we can locate the person by email address
					person = projectStaff.find { it.email?.toLowerCase() == whom.toLowerCase() }
					if (! person)
						return "Staff referenced by email ($whom) not associated with project"
				} else {

					if (NumberUtil.isLong(whom)) {
						def whomId = NumberUtil.toLong(whom)
						person = projectStaff.find { it.id == whomId }
						if (! person) {
							// Look if the person exist
							person = Person.get(whomId)
							if (person) {
								return "Person $person ($whom) is not in project staff ($taskSpec.whom), asset name: ${task?.assetEntity?.assetName}."
							} else {
								return "Person id $whom not exist ($taskSpec.whom), asset name: ${task?.assetEntity?.assetName}."
							}
						}
					} else {
						// Assignment by name
						def map = personService.findPerson(whom, task.project, projectStaff)
						def personMap = personService.findPersonByFullName(whom)

						if (!map.person && personMap.person) {
							return "Person by name ($whom) found but it is NOT a staff"
						} else if (map.isAmbiguous) {
							return "Staff referenced by name ($whom) was ambiguous"
						} else {
							person = map.person
						}

						if (personMap.person) {
							person = personMap.person
						} else {
							return "Person by name ($whom) NOT found"
						}
					}
				}
			}
		}

		if (person) {
			task.assignedTo = person
			// Set the fixed/hard assignment appropriately if a person was assigned
			if (taskSpec.containsKey('whomFixed') && taskSpec.whomFixed == true)
				task.hardAssigned = 1
		}

		return null
	}

	/**
	 * Assigns a team to a task.
	 *
	 * @param task - current task.
	 * @param taskSpec - a map with all the task info.
	 * @param workflow
	 * @param projectStaff
	 * @param settings : map of settings for task generation.
	 *
	 * @return error msg, null if none.
	 */
	private String assignTeam(AssetComment task, Map taskSpec, workflow, List projectStaff, Map settings) {
		// Set the Team independently of the direct person assignment
		if (taskSpec.containsKey('team')) {
			def team = taskSpec.team
			if (team) {
				// Team can have an indirect reference and optional default team (e.g. '#custom3, SYS_ADMIN')
				if (team[0]=='#') {
					team = team[1..-1]
					def defTeam
					if (team.contains(',')) {
						def split = team.split(',')*.trim()
						if (split.size() != 2) {
							return "Invalid syntax '$team' for 'team' attribute"
						}
						team = split[0]
						defTeam = split[1]
					}
					try {
						def teamProp = team
						team = AssetEntityHelper.getIndirectPropertyRef(task.assetEntity, team)
						if (! team) {
							if (defTeam) {
								team = defTeam
							} else {
								return "Team not defined in property $teamProp for task $task"
							}
						}
					} catch (e) {
						log.error "assignTeam: $e.message\n${ExceptionUtil.stackTraceToString(e)}"
						return "$e.message, team ($team)"
					}
				}
			}
			// Validate that the string is correct
			List teamCodeList = settings["teamCodes"]
			if (teamCodeList.contains(team)) {
				task.role = team
			} else {
				return "Invalid team specified ($taskSpec.team)"
			}
		} else if (workflow && workflow.role_id) {
			// Assign the default role/team for the workflow specified in the taskSpec
			task.role = workflow.role_id
		}

		return null
	}

	/**
	 * Helper method used to add missing dependency details onto the stack to wire up tasks later on
	 * @param Map Array - the list used to track all of the missed dependencies
	 * @param String key - the lookup key (missedPredKey - assetId_category)
	 * @param Map Array - the TaskSpec used to create the task(s)
	 * @param AssetComment task - the task that failed the dependency lookup
	 * @param Boolean isRequired - the flag if the dependency was required in the taskspec
	 * @param AssetComment latestMilestoneTask - the current last milestone at the time that the smdTask was created
	 * @param AssetDependency dependency - the dependency record that was used to link assets together (not always present)
	 */
	private void saveMissingDep(missedDepList, key, taskSpec, task, isRequired, latestMilestoneTask, dependency=null) {
		missedDepList[key].add([taskId:task.id, isRequired:isRequired, msTaskId:latestMilestoneTask?.id , dependency:dependency])
		log.info "saveMissingDep() Added missing predecessor to stack ($key) for task $task. Now have ${missedDepList[key].size()} missed predecessors"
	}

	/**
	 * Helper method to set any scheduling constraints on a task
	 */
	private void setTaskConstraints(AssetComment task, Map taskSpec, workflow, List projectStaff, StringBuilder exceptions) {
		def ctype
		def cdt
		def dateTime

		// Check to see if we have a type
		if (taskSpec.containsKey('constraintType')) {
			ctype = TimeConstraintType.asEnum(taskSpec.constraintType)
			if (! ctype) {
				exceptions.append("TaskSpec $taskSpec.id constraintType contains invalid value, must be one of ${TimeConstraintType.getKeys()}<br>")
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
						cdt = AssetEntityHelper.getIndirectPropertyRef(task.assetEntity, cdt)
					} else {
						exceptions.append("TaskSpec $taskSpec.id can not use indirect reference ($cdt) for constraintTime of non-asset type task spec<br>")
						return
					}
				}

				// Let's try to convert the text into a date time
				if (cdt) {
					dateTime = TimeUtil.parseDateTime(cdt)
					if (! dateTime) {
						exceptions.append("TaskSpec $taskSpec.id for task ($task.taskNumber) has unparsable date ($cdt)<br>")
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
	 * This special method is used to find given event task summary (taskCounts, tatalDuratin, tasks by status)
	 * @param MoveEvent moveEvent object
	 * @param loadedGroups Map<List> - A mapped list of the taskCounts, tatalDuratin, tasks by status
	 */
	Map getMoveEventTaskSummary(MoveEvent moveEvent, def viewUnpublished = false) {
		Map taskStatusMap =[:]
		int totalDuration = 0
		int taskCountByEvent = 0

		if (moveEvent) {
			taskCountByEvent = AssetComment.createCriteria().list {
				projections {
					count()
				}
				eq ('moveEvent', moveEvent)
				if (! viewUnpublished) {
					and {
						eq ('isPublished', true)
					}
				}
			}[0]

			for (status in ACS.topStatusList) {
				List tasks = AssetComment.createCriteria().list {
					eq 'moveEvent', moveEvent
					and {
						eq 'status', status
						if (! viewUnpublished) {
							eq ('isPublished', true)
						}
					}
				}

				def timeInMin = tasks?.sum { task ->
					task.durationInMinutes()
				}
				def countTasks = tasks.size()
				countTasks = countTasks ?: 0
				taskStatusMap[status] = [taskCount: countTasks, timeInMin: timeInMin]
				if (timeInMin) {
					totalDuration += timeInMin
				}
			}
		}

		return [taskCountByEvent:taskCountByEvent, taskStatusMap:taskStatusMap, totalDuration:totalDuration]
	}

	/**
	 * Used to retrieve a breakdown of task summary by team for a given event
	 * @param moveEvent - the event object to fetch data for
	 * @return A map of mapped values (taskCounts, teamTaskDoneCount and role)
	 */
	Map getMoveEventTeamTaskSummary(MoveEvent moveEvent, Boolean viewUnpublished = false){
		Map teamTaskMap =[:]
		List publishedValues = viewUnpublished ? [true,false] : [true]
		if (moveEvent) {
			getTeamRolesForTasks().each { role ->
				def teamTask = AssetComment.findAllByMoveEventAndRoleAndIsPublishedInList(moveEvent, role.id, publishedValues)
				def teamDoneTask = teamTask.findAll { it.status == 'Completed' }
				if (teamTask) {
					teamTaskMap[role.id] = [teamTaskCount:teamTask.size(), teamDoneCount:teamDoneTask.size(), role:role]
				}
			}
		}
		return teamTaskMap
	}

	/**
	 * Used to reset the default status values for a batch of tasks
	 * @param taskBatchId - the id of the TaskBatch to be reset
	 */
	def resetTasksOfTaskBatch(Long taskBatchId, Project currentProject) {
		securityService.requirePermission Permission.TaskPublish
		controllerService.getRequiredProject()

		resetTaskDataForTaskBatch(get(TaskBatch, taskBatchId, currentProject))
	}

	/**
	 * Publishes the asset comment for a specific task
	 *
	 * @param taskId the task id
	 * @return the number of affected tasks
	 */
	def publish(Long taskId, Project currentProject) {
		return basicPublish(taskId, true, Permission.TaskPublish, currentProject)
	}

	/**
	 * Unpublishes the asset comment for a specific task
	 *
	 * @param taskId the task id
	 * @return the number of affected tasks
	 */
	def unpublish(Long taskId, Project currentProject) {
		return basicPublish(taskId, false, Permission.TaskPublish, currentProject)
	}

	/**
	 * Publishes/Unpublishes the asset comment for a specific task
	 *
	 * @param taskId the task id
	 * @param shouldPublish if it should publish or unpublish
	 * @param permission the requested permission
	 * @return the number of affected tasks
	 */
	private basicPublish(Long taskId, shouldPublish, String permission, Project currentProject) {
		securityService.requirePermission permission
		controllerService.getRequiredProject()

		TaskBatch task = get(TaskBatch, taskId, currentProject)

		if (task.isPublished == shouldPublish) {
			throw new IllegalArgumentException('The task is already in that state')
		}

		def affectedComments = namedParameterJdbcTemplate.update('UPDATE asset_comment SET is_published = :shouldPublish WHERE task_batch_id = :taskId',
			[taskId: taskId, shouldPublish: shouldPublish])

		task.isPublished = shouldPublish
		task.save(failOnError: true)

		return affectedComments
	}

	/**
	 * Deletes the batch whose id is taskId
	 *
	 * @param taskBatchId the task id
	 */
	def deleteBatch(Long taskBatchId, Project currentProject) {
		String currentUsername = securityService.currentUsername
		log.debug "User $currentUsername is attempting to delete TaskBatch $taskBatchId"

		securityService.requirePermission Permission.TaskBatchDelete
		controllerService.getRequiredProject()

		TaskBatch taskBatch = get(TaskBatch, taskBatchId, currentProject)

		if (taskBatch.recipe) {
			securityService.assertCurrentProject taskBatch.recipe.project
		}

		log.debug "User $currentUsername is deleting TaskBatch $taskBatch"

		taskBatch.delete(failOnError: true)
	}

	/**
	 * Used to lookup a TaskBatch by the Context and Recipe regardless of the recipe version
	 *
	 * @param eventId - the event id for finding task batches
	 * @param recipeId - the record id of the recipe used to generate the TaskBatch
	 * @param includeLogs - whether to include the logs information or not
	 * @return A taskBatch map if found or null
	 */
	def findTaskBatchByRecipeAndContext(Long recipeId, Long eventId, Project currentProject, includeLogs) {
		controllerService.getRequiredProject()

		Recipe recipe = get(Recipe, recipeId, currentProject)
		securityService.assertCurrentProject recipe.project

		includeLogs = includeLogs == null ? false : includeLogs.toBoolean()

		try {
			def taskBatchs = namedParameterJdbcTemplate.queryForList("""select *
				from task_batch
				inner join recipe_version on task_batch.recipe_version_used_id = recipe_version.recipe_version_id
				inner join person on task_batch.created_by_id = person.person_id
				where recipe_version.recipe_id = :recipeId AND task_batch.event_id = :eventId
				order by task_batch.date_created desc""", ['recipeId' : recipeId, 'eventId' : eventId])

			def result
			if (taskBatchs) {
				//Get latest task batch created
				def taskBatch = taskBatchs[0]
				result = [
					'id': taskBatch.task_batch_id,
					'contextType': taskBatch.context_type,
					'eventId': taskBatch.context_id,
					'recipe': taskBatch.recipe_id,
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
			}

			return result
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("Invalid query", e)
			return null
		}
	}

	/**
	 * List the TaskBatchs for a specific recipeId
	 *
	 * @param recipeId - the record id of the recipe
	 * @param limitDays - the number of days to limit the search
	 * @return the list of Task batches
	 */
	def listTaskBatches(Long recipeId, limitDays, Project currentProject) {
		controllerService.requiredProject

		Recipe recipe = get(Recipe, recipeId, currentProject, false)

		boolean listAll=(limitDays=='All')
		if (!listAll && (limitDays == null || !limitDays.isNumber())) {
			throw new IllegalArgumentException('Not a valid limitDays')
		}

		def startCreationDate = new Date()
		if (!listAll) startCreationDate = startCreationDate - limitDays.toInteger()
		log.debug "listTaskBatches - Start date: $startCreationDate, limitDays=$limitDays, listAll: $listAll, recipeId=$recipeId, recipe=$recipe"

		def c = TaskBatch.createCriteria()
		def queryResults

		if (recipe == null) {
			queryResults = c.list {
				if (!listAll)
					ge("dateCreated", startCreationDate)
				eq("project", currentProject)
				order("dateCreated", "desc")
			}
		} else {
			queryResults = c.list {
				if (!listAll)
					ge("dateCreated", startCreationDate)
				eq("recipe", recipe)
				order("dateCreated", "desc")
			}
		}

		DateFormat formatter = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)

		queryResults.collect { TaskBatch taskBatch ->
			[
				id            : taskBatch.id,
				recipeId      : taskBatch.recipe?.id ?: 0,
				recipeName    : taskBatch.recipe?.name ?: '',
				eventName     : taskBatch.eventName(),
				tagNames      : taskBatch.tagNames(),
				taskCount     : taskBatch.taskCount,
				exceptionCount: taskBatch.exceptionCount,
				createdBy     : taskBatch.createdBy?.firstName + " " + taskBatch.createdBy?.lastName,
				dateCreated   : TimeUtil.formatDateTime(taskBatch.dateCreated, formatter),
				status        : taskBatch.status,
				exceptionLog  : taskBatch.exceptionLog,
				infoLog       : taskBatch.infoLog,
				versionNumber : taskBatch.recipe?.releasedVersion?.versionNumber ?: '',
				isPublished   : taskBatch.isPublished
			]
		}
	}

	/**
	 * Returns the task batch using the taskBatchId
	 *
	 * @param taskBatchId - the id of the task batch
	 * @return the task batch
	 */
	def getTaskBatch(taskBatchId, Project currentProject) {
		TaskBatch taskBatch = get(TaskBatch, taskBatchId, currentProject)

		[
			id            : taskBatch.id,
			taskCount     : taskBatch.taskCount,
			exceptionCount: taskBatch.exceptionCount,
			createdBy     : taskBatch.createdBy?.firstName + " " + taskBatch.createdBy?.lastName,
			dateCreated   : taskBatch.dateCreated,
			status        : taskBatch.status,
			versionNumber : taskBatch.recipeVersionUsed.versionNumber,
			isPublished   : taskBatch.isPublished,
			exceptionLog  : taskBatch.exceptionLog,
			infoLog       : taskBatch.infoLog
		]
	}

	/**
	 *
	 *  Finds All tasks by an assetEntity.
     *
     *  A Task instance is an AssetComment instance with AssetComment#commentType equals to AssetCommentType.TASK
	 *
	 * @param assetEntity
	 */
	def findAllByAssetEntity(def assetEntity) {
        AssetComment.findAllByAssetEntityAndCommentType(assetEntity, AssetCommentType.TASK)
	}

	/**
	 * Fills the methodParams with the Label of the fieldName and the contextDesc that gives a user readable name
	 * to be consistent with the API Action Parameters dialog
	 * @param project where we are getting the fieldSpecs
	 * @param List of maps with the MethodParam info
	 * @return the new methodParams List with the new fields added
	 */
	List<Map> fillLabels(Project project, List<Map> methodParams) {
		Map ASSET_CLASS_FOR_PARAMETERS = [
				  'COMMON': 'Asset',
				  'APPLICATION': 'Application',
				  'DATABASE': 'Database',
				  'DEVICE': 'Device',
				  'STORAGE': 'Storage'
		]



		Map fieldSpecs = customDomainService.fieldSpecsWithCommon(project)
		List<Map> newMethodParams = methodParams.collect {
			Map mparam = new HashMap(it)
			mparam.fieldNameLabel = ''

			if (mparam.context == 'TASK') { // Checking against the TaskPropertyEnum
				//Swap from AssetCommon
				mparam.contextLabel = 'Task'
				Map taskLabels = AssetCommentPropertyEnum.toMap()
				mparam.fieldNameLabel = taskLabels[mparam.fieldName]

			} else if ( ASSET_CLASS_FOR_PARAMETERS.containsKey(mparam.context) ) { //Check against the FieldSpecs
				mparam.contextLabel = ASSET_CLASS_FOR_PARAMETERS[mparam.context]
				mparam.fieldNameLabel = mparam.fieldName

				def searchList =  ((fieldSpecs[mparam.context]?.fields)?:[]) + ((fieldSpecs['COMMON']?.fields)?:[])
				def found = searchList.find { spec ->
					spec.field == mparam.fieldName
				}

				if(found) {
					mparam.fieldNameLabel = found.label
				}
			} else { // USER_DEF
				mparam.contextLabel = 'User Defined'
			}

			return mparam
		}

		return newMethodParams
	}
}
