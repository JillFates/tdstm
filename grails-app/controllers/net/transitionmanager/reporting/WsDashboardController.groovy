package net.transitionmanager.reporting

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventSnapshot
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.StepSnapshot
import net.transitionmanager.security.Permission
import net.transitionmanager.service.TaskService
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class WsDashboardController implements ControllerMethods {

	JdbcTemplate jdbcTemplate
	TaskService taskService

	/**
	 * Returns the data used to render the Event Dashboard including the work flow steps and the statistics of
	 * what there is to do and what has been accomplished.
	 * @param id - the move bundle id
	 * @param moveEventId
	 * @return JSON map
	 */
	@HasPermission(Permission.DashboardMenuView)
	def bundleData() {
		String error = ""
		Project project = securityService.userCurrentProject
		def moveEventId = params.moveEventId
		def moveBundleId = params.id
		MoveEvent moveEvent
		MoveBundle moveBundle

		// Validate that the user is legitly accessing the proper move event
		if (! moveEventId.isNumber() ) {
			error = "Move event id is invalid"
		} else {
			moveEvent = MoveEvent.findByIdAndProject(moveEventId, project)
			if (!moveEvent) {
				error = "Unable to find referenced move event for your current project"
			} else {
				if (!moveBundleId) {
					// Take the first move bundle in the event
					if (moveEvent.moveBundles) {
						moveBundle = moveEvent.moveBundles[0]
						moveBundleId = moveBundle.id
					}
				} else if (!moveBundleId.isNumber()) {
					error = "Move bundle id is invalid"
				} else {
					moveBundle = MoveBundle.findByIdAndProject(moveBundleId, project)
					if (!moveBundle) {
						error = "Unable to find referenced move bundle for your current project"
					}
				}
			}
		}

		if (error) {
			renderAsJson(error: error)
			return
		}

		List<Map<String, Object>> dataPointsForEachStep = []

		// Get the step data either by runbook tasks or
		if (moveBundle) {
			if (project.runbookOn) {

				// TODO - remove references to mbs MoveBundleStep

				boolean viewUnpublished = securityService.viewUnpublished()

				def taskStatsSql = """
					SELECT
						t.workflow_transition_id AS wfTranId,
						wft.trans_id AS tid,
						0 AS snapshotId,
						mbs.label,
						mbs.calc_method AS calcMethod,
						SUM(IF(t.asset_comment_id IS NULL, 0, 1)) AS tskTot,
						SUM(IF(t.status='Pending',1,0)) AS tskPending,
						SUM(IF(t.status='Ready',1,0)) AS tskReady,
						SUM(IF(t.status='Started',1,0)) AS tskStarted,
						SUM(IF(t.status='Completed',1,0)) AS tskComp,
						SUM(IF(t.status='Hold',1,0)) AS tskHold,
						ROUND(IF(count(*)>0,SUM(IF(t.status='Completed',1,0))/count(*)*100,100)) AS percComp,
						mbs.plan_start_time AS planStart,
						mbs.plan_completion_time AS planComp,
						MIN(IFNULL(t.act_start, t.date_resolved)) AS actStart,
						MAX(t.date_resolved) AS actComp
					FROM asset_entity a
					JOIN asset_comment t ON t.asset_entity_id = a.asset_entity_id
					JOIN workflow_transition wft ON wft.workflow_transition_id=t.workflow_transition_id
					JOIN move_bundle mb ON mb.move_bundle_id=a.move_bundle_id
					JOIN move_bundle_step mbs ON mbs.move_bundle_id=a.move_bundle_id AND mbs.transition_id=wft.trans_id
					WHERE a.move_bundle_id = $moveBundleId AND t.move_event_id = $moveEventId ${viewUnpublished ? '' : 'AND t.is_published = 1'}
					GROUP BY t.workflow_transition_id;
				"""
				dataPointsForEachStep = jdbcTemplate.queryForList(taskStatsSql)
				// log.info "bundleData() SQL = $taskStatsSql"
			} else {

			// def offsetTZ = ( new Date().getTimezoneOffset() / 60 ) * ( -1 )
			/*def offsetTZ = ( new Date().getTimezoneOffset() / 60 )
				log.debug "offsetTZ=$offsetTZ"*/

				/* Get the latest step_snapshot record for each step that has started */
				def latestStepsRecordsQuery = """
					SELECT mbs.transition_id as tid,
						ss.id as snapshotId,
						mbs.label as label,
						mbs.calc_method as calcMethod,
						mbs.plan_start_time as planStart,
						mbs.plan_completion_time as planComp,
						mbs.actual_start_time as actStart,
						mbs.actual_completion_time as actComp,
						ss.date_created as dateCreated,
						ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd
					FROM move_bundle mb
					LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id
					LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id
					INNER JOIN (SELECT move_bundle_step_id, MAX(date_created) as date_created FROM step_snapshot GROUP BY move_bundle_step_id) ss2
					ON ss2.move_bundle_step_id = mbs.id AND ss.date_created = ss2.date_created
					WHERE mb.move_bundle_id = $moveBundle.id
				"""

				/*	Get the steps that have not started / don't have step_snapshot records	*/
				def stepsNotUpdatedQuery = """
					SELECT mbs.transition_id as tid, ss.id as snapshotId, mbs.label as label, mbs.calc_method as calcMethod,
						mbs.plan_start_time as planStart,
						mbs.plan_completion_time as planComp,
						mbs.actual_start_time as actStart,
						mbs.actual_completion_time as actComp,
						ss.date_created as dateCreated,
						ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd
					FROM move_bundle mb
					LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id
					LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id
					WHERE mb.move_bundle_id = $moveBundle.id AND ss.date_created IS NULL AND mbs.transition_id IS NOT NULL
				"""

				dataPointsForEachStep = jdbcTemplate.queryForList( latestStepsRecordsQuery + " UNION " + stepsNotUpdatedQuery )
			}
		}

		Date sysTime = TimeUtil.nowGMT()
		int sysTimeInMs = sysTime.getTime() / 1000

		dataPointsForEachStep.each { data ->

			StepSnapshot snapshot
			int planCompTime = data.planComp.getTime() / 1000
			int planStartTime = data.planStart.getTime() / 1000

			if (data.snapshotId) {
				snapshot = StepSnapshot.get(data.snapshotId)
				data.projComp = TimeUtil.formatDateTime(snapshot.projectedCompletionTime)
				data.statColor = snapshot.statusColor
				if (snapshot.moveBundleStep.showInGreen) {
					data.percentageStyle = "step_statusbar_good"
					return
				}
			} else {
				data.projComp = ''
				data.statColor = 'red'
			}

			def startOverdueDuration = 0
			def compOverdueDuration = 0

			// if the step has been started, calculate the elapsed times for indicating overdue time
			if (data.actStart) {
				startOverdueDuration = TimeUtil.ago(data.planStart, data.actStart)
				compOverdueDuration = TimeUtil.ago(data.planComp, data.actComp ?: TimeUtil.nowGMT())
			}

			if (!data.actComp) {
				// 59s is added to planCompletion to consider the minutes instead of seconds
				if ( sysTimeInMs > planCompTime + 59 && data.tskComp < data.tskTot) {
					data.percentageStyle = "step_statusbar_bad"
				} else {
					int remainingStepTime = planCompTime - sysTimeInMs
					// 20% of planned duration
					def planDurationLeft = (planCompTime - planStartTime) * 0.2
					// 80% of remainin assets
					def remainingTasks =  data.tskTot ? data.tskTot * 0.6 : 0
					if (remainingStepTime <= planDurationLeft && remainingTasks > data.tskComp) {
						data.percentageStyle = "step_statusbar_yellow"
					} else {
						data.percentageStyle = "step_statusbar_good"
					}
				}
				/*if(data.projComp){
    				if( new Date( data.projComp ).getTime() > new Date( data.planComp ).getTime() ){
    					data.percentageStyle = "step_statusbar_bad"
    				} else {
    					data.percentageStyle = "step_statusbar_yellow"
    				}*/
					// commented for now
    				/*if(data.dialInd < 25){
    					data.percentageStyle = "step_statusbar_bad"
    				} else if(data.dialInd >= 25 && data.dialInd < 50){
    					data.percentageStyle = "step_statusbar_yellow"
    				} else {
    					data.percentageStyle = "step_statusbar_good"
    				}
				} else {
					data.percentageStyle = "step_statusbar_good"
				}*/
			} else {
				def actCompTime = data.actComp.getTime() / 1000
				if ( actCompTime > planCompTime+59 ) {  // 59s added to planCompletion to consider the minutes instead of seconds
					data.percentageStyle = "step_statusbar_bad"
				} else {
					data.percentageStyle = "step_statusbar_good"
				}
			}

			int totalNumTasks = data.tskTot ? data.tskTot.intValue() : 0
			int tasksCompleted = data.tskComp ? data.tskComp.intValue() : 0

			def dialIndicator = taskService.calcStepDialIndicator(moveBundle.startTime, moveBundle.completionTime,
				data.actStart, data.actFinish, totalNumTasks, tasksCompleted)

			data.dialInd = dialIndicator
			data.startOverdueDuration = startOverdueDuration
			data.compOverdueDuration = compOverdueDuration
		}

		def planSumCompTime
		def moveEventPlannedSnapshot
		def moveEventRevisedSnapshot
		def revisedComp
		def dayTime
		String eventString = ""
		if (moveEvent) {

			def resultMap = jdbcTemplate.queryForMap( """
				SELECT max(mb.completion_time) as compTime,
				min(mb.start_time) as startTime
				FROM move_bundle mb WHERE mb.move_event_id = $moveEvent.id
				""" )

			planSumCompTime = resultMap?.compTime
			Date eventStartTime = moveEvent.estStartTime
			if (eventStartTime || resultMap?.startTime) {
				if(!eventStartTime){
					eventStartTime = new Date(resultMap.startTime.getTime())
				}
				if (eventStartTime>sysTime) {
					dayTime = TimeCategory.minus(eventStartTime, sysTime)
					eventString = "Countdown Until Event"
				} else {
					dayTime = TimeCategory.minus(sysTime, eventStartTime)
					eventString = "Elapsed Event Time"
				}
			}
			/*
			* select the most recent MoveEventSnapshot records for the event for both the P)lanned and R)evised types.
			*/
			def query = "FROM MoveEventSnapshot mes WHERE mes.moveEvent = ? AND mes.type = ? ORDER BY mes.dateCreated DESC"
			// moveEventPlannedSnapshot = MoveEventSnapshot.find( query , [moveEvent , MoveEventSnapshot.TYPE_PLANNED] )[0]
			// moveEventRevisedSnapshot = MoveEventSnapshot.find( query , [moveEvent, MoveEventSnapshot.TYPE_REVISED] )[0]
			moveEventPlannedSnapshot = MoveEventSnapshot.findAll( query , [moveEvent, "P"] )[0]
			moveEventRevisedSnapshot = MoveEventSnapshot.findAll( query , [moveEvent, "R"] )[0]
			revisedComp = moveEvent.revisedCompletionTime
			if (revisedComp) {
				revisedComp = new Date(revisedComp.time)
			}
		}

		String eventClockCountdown = TimeUtil.formatTimeDuration(dayTime)
		String eventStartDate = moveEvent.estStartTime ? TimeUtil.formatDateTime(moveEvent.estStartTime, TimeUtil.FORMAT_DATE_TIME) : ''

		renderAsJson(snapshot: [
			revisedComp: moveEvent?.revisedCompletionTime,
			moveBundleId: moveBundleId,
			calcMethod: moveEvent?.calcMethod,
			planDelta: moveEventPlannedSnapshot?.planDelta,
			systime: TimeUtil.formatDateTime(sysTime, TimeUtil.FORMAT_DATE_TIME_11),
			planSum: [
				dialInd: moveEventPlannedSnapshot?.dialIndicator,
				confText: 'High',
				confColor: 'green',
				compTime: planSumCompTime,
				dayTime: eventClockCountdown,
				eventDescription: moveEvent?.description,
				eventString: eventString,
				eventRunbook: moveEvent?.runbookStatus
			],
			revSum: [dialInd: moveEventRevisedSnapshot?.dialIndicator,
			         compTime: TimeUtil.formatDateTime(revisedComp, TimeUtil.FORMAT_DATE_TIME_11)],
			steps: dataPointsForEachStep,
			runbookOn: project.runbookOn,
			eventStartDate: eventStartDate
		])
	}
}
