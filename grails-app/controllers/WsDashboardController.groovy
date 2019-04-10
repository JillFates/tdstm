import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.ValidationType
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
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.TaskService
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class WsDashboardController implements ControllerMethods {

	JdbcTemplate jdbcTemplate
	TaskService taskService
	MoveEventService moveEventService
	CustomDomainService customDomainService

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

	/**
	 * Calculates percentage of Filtered Apps on Total Planned apps.
	 * @param totalAppCount : Total count of Application that is in Planned Bundle
	 * @param filteredAppCount : This is filtered app based on PlanStatus
	 * @return : Percentage of Calculated app
	 */
	@HasPermission(Permission.AssetView)
	def countAppPercentage(int totalAppCount, int filteredAppCount) {
		return totalAppCount ? Math.round((filteredAppCount /  totalAppCount) * 100) : 0
	}

	@HasPermission(Permission.DashboardMenuView)
	def getDataForPlanningDashboard() {
		Project project = securityService.userCurrentProject

		// Get list of all of the MoveBundles that are used for Planning
		def moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project, true, [sort: 'startTime'])

		// Nothing to report if there are no planning bundles so we'll just return with zeros for everything
		if (!moveBundleList) {
			render(view: 'planningStats_NoBundles')
			return
		}

		def appList = []
		def eventStartDate = [:]
		def movedPlan = AssetEntityPlanStatus.MOVED

		// Get the list of Move Events and sort on the start date
		List<MoveEvent> moveEventList = moveBundleList*.moveEvent.unique()
		moveEventList.remove(null)
		def getEventStartDate = {
			Date start = it.estStartTime
			if(!start){
				start = moveEventService.getEventTimes(it.id).start
			}
			return start
		}
		moveEventList = moveEventList.sort {a,b ->
			getEventStartDate(a) <=> getEventStartDate(b)
		}

		// Forming query for multi-uses
		def baseWhere = 'WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)'
		def selectCount = 'SELECT count(ae)'
		def countArgs = [project:project, moveBundles:moveBundleList]

		String countQuery = "$selectCount FROM AssetEntity ae $baseWhere"
		String appQuery = "FROM Application ae $baseWhere"
		String appCountQuery = "$selectCount $appQuery"
		String dbQuery = "FROM Database ae $baseWhere"
		String dbCountQuery = "$selectCount $dbQuery"
		String filesQuery = "FROM Files ae $baseWhere"
		String filesCountQuery = "$selectCount $filesQuery"
		String phyStorageQuery = "FROM AssetEntity ae $baseWhere AND ae.assetClass=:assetClass AND ae.assetType IN (:type)"
		String phyStorageCountQuery = "$selectCount $phyStorageQuery"
		String deviceQuery = "FROM AssetEntity ae $baseWhere AND ae.assetClass=:assetClass AND ae.assetType IN (:type)"
		String deviceCountQuery = "$selectCount $deviceQuery"
		String otherCountQuery = "$selectCount FROM AssetEntity ae $baseWhere AND ae.assetClass=:assetClass AND COALESCE(ae.assetType,'') NOT IN (:type)"

		def databaseCount = Database.executeQuery(dbCountQuery, countArgs)[0]
		def fileCount = Files.executeQuery(filesCountQuery, countArgs)[0]

		// Get the list of apps and servers assigned to planning bundles
		def applicationsOfPlanningBundle = Application.findAll(appQuery, countArgs)
		def appDependenciesCount = applicationsOfPlanningBundle ? AssetDependency.countByAssetInList(applicationsOfPlanningBundle) : 0
		def serversOfPlanningBundle = AssetEntity.findAll(deviceQuery, countArgs + [assetClass: AssetClass.DEVICE, type:AssetType.allServerTypes])
		def serverDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByAssetInList(serversOfPlanningBundle) : 0

		def assignedApplicationCount
		def allServerList = []
		def phyServerList = []
		def virtServerList = []
		def dbList = []
		def phyStorageList = []
		def filesList = []
		def otherTypeList = []
		def openTasks = []

		moveEventList.each { moveEvent->
			// fetching bundles for current moveEvent which was set 'true' for useForPlanning
			def moveBundles = moveEvent.moveBundles?.findAll {it.useForPlanning}
			def eventWiseArgs = [project:project, moveBundles:moveBundles]

			Date startDate = moveEvent.estStartTime
			if(!startDate){
				def eventDates = moveEventService.getEventTimes(moveEvent.id)
				startDate = eventDates.start
			}
			eventStartDate[moveEvent.id] = TimeUtil.formatDateTime(startDate, TimeUtil.FORMAT_DATE_TIME_7)

			// Fetching application count that are assigned to current move event
			assignedApplicationCount = moveBundles ? Application.executeQuery(appCountQuery, eventWiseArgs)[0] : 0
			appList << [count: assignedApplicationCount , moveEvent: moveEvent.id]

			// fetching physicalAsset (e.g. 'Server',blade) and virtualAsset (e.g. 'VM') count that are assigned to current move-event .
			int physicalAssetCount = moveBundles ? AssetEntity.executeQuery(deviceCountQuery,
					eventWiseArgs + [assetClass: AssetClass.DEVICE, type: AssetType.physicalServerTypes])[0] : 0
			int virtualAssetCount = moveBundles ? AssetEntity.executeQuery(deviceCountQuery, eventWiseArgs +
					[assetClass: AssetClass.DEVICE, type: AssetType.virtualServerTypes])[0] : 0
			int serverCnt = moveBundles ? AssetEntity.executeQuery(deviceCountQuery, eventWiseArgs +
					[assetClass: AssetClass.DEVICE, type: AssetType.allServerTypes])[0] : 0

			allServerList << [moveEvent: moveEvent.id , count: serverCnt]
			phyServerList << [moveEvent: moveEvent.id , count: physicalAssetCount]
			virtServerList << [moveEvent: moveEvent.id , count: virtualAssetCount]

			def dbCount = moveBundles ? Database.executeQuery(dbCountQuery, eventWiseArgs)[0] : 0
			dbList << [moveEvent:moveEvent.id , count:dbCount]

			int phyStoragesCount = moveBundles ? AssetEntity.executeQuery(phyStorageCountQuery, eventWiseArgs +
					[assetClass:AssetClass.DEVICE, type:AssetType.storageTypes])[0] : 0
			phyStorageList << [moveEvent: moveEvent.id , count: phyStoragesCount]

			def filesCount = moveBundles ? Files.executeQuery(filesCountQuery, eventWiseArgs)[0] : 0
			filesList << [moveEvent: moveEvent.id , count: filesCount]

			def otherCount = moveBundles ? AssetEntity.executeQuery(otherCountQuery,
					eventWiseArgs + [assetClass:AssetClass.DEVICE, type:AssetType.nonOtherTypes])[0] : 0
			otherTypeList << [moveEvent: moveEvent.id, count: otherCount]

			Long openIssueCount = AssetComment.executeQuery('''
					select count(*)
					FROM AssetComment where project=:project
					 and commentType=:type
					 and status IN (:status)
					 and moveEvent=:event
					 AND isPublished=true
			''', [project: project, type: AssetCommentType.TASK, event: moveEvent,
				  status : [AssetCommentStatus.READY, AssetCommentStatus.STARTED, AssetCommentStatus.PENDING]])[0]

			openTasks << [moveEvent: moveEvent.id , count: openIssueCount]
		}

		// ----------------------------------------------------------------------------
		// Get the totals count for assigned and unassigned assets
		// ----------------------------------------------------------------------------

		// Find Move bundles that are not assigned to a Event and is the bundle is used for planning
		def unassignedMoveBundles = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.moveEvent IS NULL \
			AND mb.useForPlanning = true AND mb.project = :project ", [project:project])

		// Construct the query that will include counts of non-event bundles if any exist just the bundle being NULL
		def assetCountQueryArgs = [project:project]
		if (unassignedMoveBundles) {
			assetCountQueryArgs.unassignedMoveBundles = unassignedMoveBundles
		}

		// Get majority of the unassigned counts
		int unassignedDbCount = 0
		int unassignedFilesCount = 0
		int unassignedAppCount = 0
		int unassignedDeviceCount = 0
		int applicationCount = 0
		int movedAppCount = 0
		int confirmedAppCount = 0
		int assignedAppCount = 0
		int lockedAppCount = 0

		def basicCountsQuery = """SELECT
				assetClass,
				COUNT(ae) AS all1,
				SUM(CASE WHEN ae.planStatus=:unassignStatus THEN 1 ELSE 0 END)  AS allUnassigned2,
				SUM(CASE WHEN ae.planStatus=:movedStatus THEN 1 ELSE 0 END)     AS allMoveded3,
				SUM(CASE WHEN ae.planStatus=:confirmedStatus THEN 1 ELSE 0 END) AS allConfirmed4,
				SUM(CASE WHEN ae.planStatus=:lockedStatus THEN 1 ELSE 0 END) AS allLocked5
			FROM AssetEntity ae
			WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)
			GROUP BY ae.assetClass"""

		def basicCountsParams = [
				project: project,
				moveBundles: moveBundleList,
				unassignStatus: AssetEntityPlanStatus.UNASSIGNED,
				movedStatus: AssetEntityPlanStatus.MOVED,
				confirmedStatus: AssetEntityPlanStatus.CONFIRMED,
				lockedStatus: AssetEntityPlanStatus.LOCKED]

		def basicCountsResults = AssetEntity.executeQuery(basicCountsQuery, basicCountsParams)
		basicCountsResults.each { ua ->
			switch(ua[0]) {
				case AssetClass.APPLICATION:
					applicationCount = ua[1]
					unassignedAppCount = ua[2]
					assignedAppCount = applicationCount - unassignedAppCount
					movedAppCount = ua[3]
					lockedAppCount = ua[5]
					confirmedAppCount = movedAppCount + ua[4] + lockedAppCount
					break
				case AssetClass.DATABASE:
					unassignedDbCount = ua[2]; break
				case AssetClass.DEVICE:
					unassignedDeviceCount = ua[2]; break
				case AssetClass.STORAGE:
					unassignedFilesCount = ua[2]; break
			}
		}

		// Get the various DEVICE types broken out
		def deviceMetricsQuery = """SELECT
			COUNT(ae) AS allDevices,
			COALESCE(SUM(CASE WHEN ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS allDevicesUnassigned,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:phyTypes) THEN 1 ELSE 0 END), 0) AS physicalAll,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:phyTypes) AND ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS physicalUnassigned,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:virtTypes) THEN 1 ELSE 0 END), 0) AS virtualAll,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:virtTypes) AND ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS virtualUnassigned,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:storageTypes) THEN 1 ELSE 0 END), 0) AS storageAll,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:storageTypes) AND ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS storageUnassigned,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:networkTypes) THEN 1 ELSE 0 END), 0) AS networkAll,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:networkTypes) AND ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS networkUnassigned
			FROM AssetEntity ae
			WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles) AND ae.assetClass=:assetClass"""

		def deviceMetricsParams = [
				project:project,
				moveBundles:moveBundleList,
				unassignStatus: AssetEntityPlanStatus.UNASSIGNED,
				assetClass: AssetClass.DEVICE,
				phyTypes: AssetType.physicalServerTypes,
				virtTypes: AssetType.virtualServerTypes,
				storageTypes: AssetType.storageTypes,
				networkTypes: AssetType.networkDeviceTypes
		]
		def deviceMetrics = AssetEntity.executeQuery(deviceMetricsQuery, deviceMetricsParams)[0]

		// Assign all the results into variables by ordinal position
		def (totalDeviceCount, unassignedAllDeviceCount, totalPhysicalServerCount, unassignedPhysicalServerCount,
			 totalVirtualServerCount, unassignedVirtualServerCount, phyStorageCount, unAssignedPhyStorageCount,
			 phyNetworkCount, unAssignedPhyNetworkCount) = deviceMetrics

		// Computed values from previous gathered data points
		def totalServerCount = totalPhysicalServerCount + totalVirtualServerCount
		// def otherAssetCount = totalDeviceCount - totalServerCount - phyStorageCount - phyNetworkCount
		// TODO : JPM 12/2015 TM-4332 : We're including the Network Devices in the Other count for the time being
		def otherAssetCount = totalDeviceCount - totalServerCount - phyStorageCount
		def unassignedOtherCount = unassignedAllDeviceCount - unassignedPhysicalServerCount - unassignedVirtualServerCount -
				unAssignedPhyStorageCount

		// Application Plan Methodology
		def customField = project.planMethodology ?: "''"

		String groupingSumQuery = "SELECT new map(${customField} as key, COUNT(ae) as count) FROM Application ae WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)"

		if (customField) {
			groupingSumQuery += " group by ${customField}"
		}
		def groupValues = Application.executeQuery(groupingSumQuery, [project:project, moveBundles:moveBundleList])

		def groupPlanMethodologyCount = groupValues.inject([:]) { groups, it ->
			def key = it.key
			if(!key) key = Application.UNKNOWN

			if(!groups[key]) groups[key] = 0

			groups[key] += it.count

			groups
		}

		// sort values based on custom field setting configuration
		def customFieldSetting = customDomainService.findCustomField(project, AssetClass.APPLICATION.toString()) {
			it.field == customField
		}

		if (customFieldSetting?.constraints?.values) {
			def sortedMap = customFieldSetting.constraints.values.inject([:]) { result, it ->
				if ( ! it ) {
					result[Application.UNKNOWN] = 0
				} else if (groupPlanMethodologyCount[it]) {
					result[it] = 0
				}
				result
			}
			groupPlanMethodologyCount = sortedMap + groupPlanMethodologyCount;
		}

		// ------------------------------------
		// Calculate the Plan Status values
		// ------------------------------------
		def assignedAppPerc = countAppPercentage(applicationCount, assignedAppCount)
		def confirmedAppPerc = countAppPercentage(applicationCount, confirmedAppCount)
		def movedAppPerc = countAppPercentage(applicationCount, movedAppCount)
		def percAppDoneCount = countAppPercentage(applicationCount, movedAppCount)

		int percentagePhysicalServerCount = moveBundleList ? AssetEntity.executeQuery(deviceCountQuery + " AND ae.planStatus='$movedPlan'",
				countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.physicalServerTypes])[0] : 0

		// Quick closure for calculating the percentage below
		def percOfCount = { count, total ->
			(total > 0 ? ((count/total*100)as double).trunc(2).intValue() : 0)
		}

		String planStatusMovedQuery = " AND ae.planStatus='$movedPlan'"
		int percVirtualServerCount = moveBundleList ?
				AssetEntity.executeQuery(deviceCountQuery + planStatusMovedQuery, countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.virtualServerTypes])[0]
				: 0

		percentagePhysicalServerCount = percOfCount(percentagePhysicalServerCount, totalPhysicalServerCount)
		percVirtualServerCount = percOfCount(percVirtualServerCount, totalVirtualServerCount)

		int percentageDBCount = moveBundleList ? Database.executeQuery(dbCountQuery + planStatusMovedQuery, countArgs)[0] : 0

		percentageDBCount = percOfCount(percentageDBCount, databaseCount)

		int percentagePhyStorageCount = moveBundleList ? AssetEntity.executeQuery(deviceCountQuery + " AND ae.planStatus='$movedPlan'",
				countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.storageTypes])[0] : 0

		percentagePhyStorageCount = percOfCount(percentagePhyStorageCount, phyStorageCount)

		int percentageOtherCount = moveBundleList ? AssetEntity.executeQuery(otherCountQuery + planStatusMovedQuery,
				countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.allServerTypes])[0] : 0
		percentageOtherCount = percOfCount(percentageOtherCount, otherAssetCount)

		def pendingAppDependenciesCount = applicationsOfPlanningBundle ?
				AssetDependency.countByAssetInListAndStatusInList(applicationsOfPlanningBundle,['Unknown','Questioned']) : 0

		def pendingServerDependenciesCount = serversOfPlanningBundle ?
				AssetDependency.countByAssetInListAndStatusInList(serversOfPlanningBundle,['Unknown','Questioned']) : 0

		String time
		def date = AssetDependencyBundle.findByProject(project,[sort:"lastUpdated", order:"desc"])?.lastUpdated

		def today = new Date()
		def issueQuery = "from AssetComment a  where a.project =:project and a.category in (:category) and a.status != :status and a.commentType =:type AND a.isPublished = true"
		def issueArgs = [project:project, status:AssetCommentStatus.COMPLETED, type:AssetCommentType.TASK.toString()]

		def openIssue =  AssetComment.findAll(issueQuery,issueArgs + [category : AssetComment.discoveryCategories]).size()
		def dueOpenIssue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs + [category : AssetComment.discoveryCategories, dueDate:today]).size()
		def issues = AssetComment.findAll("FROM AssetComment a where a.project = :project and a.commentType = :type and a.status =:status  \
			and a.category in (:category) AND a.isPublished = true",[project:project, type:AssetCommentType.TASK, status: AssetCommentStatus.READY , category: AssetComment.planningCategories])
		def generalOverDue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs + [category: AssetComment.planningCategories, dueDate:today]).size()

		// Remove the param 'type' that was used for a while above
		countArgs.remove('type')

		String validationQuery = ' AND ae.validation=:validation'
		String validateCountQuery = countQuery + validationQuery + ' AND ae.assetType IN (:type) AND ae.assetClass=:assetClass'
		String appValidateCountQuery = appCountQuery + validationQuery
		String dbValidateCountQuery = dbCountQuery + validationQuery
		String filesValidateCountQuery = filesCountQuery + validationQuery

		// This section could be consolidated to a simple query instead of a bunch
		def unknown = Application.executeQuery(appValidateCountQuery, countArgs+[validation: ValidationType.UNKNOWN])[0]
		def planReady = Application.executeQuery(appValidateCountQuery, countArgs+[validation: ValidationType.PLAN_READY])[0]

		countArgs.validation = ValidationType.UNKNOWN
		def appToValidate = Application.executeQuery(appValidateCountQuery, countArgs)[0]
		def dbToValidate = Database.executeQuery(dbValidateCountQuery, countArgs)[0]
		def fileToValidate = Files.executeQuery(filesValidateCountQuery, countArgs)[0]
		def phyStorageToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.storageTypes])[0]
		def psToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.physicalServerTypes])[0]
		def vsToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.virtualServerTypes])[0]

		def otherValidateQuery = countQuery + validationQuery + " AND COALESCE(ae.assetType,'') NOT IN (:type) AND ae.assetClass=:assetClass"
		def otherToValidate = AssetEntity.executeQuery(otherValidateQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.nonOtherTypes])[0]

		def percentageAppToValidate = applicationCount ? percOfCount(appToValidate, applicationCount) : 100
		def percentagePlanReady = applicationCount ? percOfCount(planReady, applicationCount) : 0
		int percentageFilesCount = percOfCount(fileToValidate, fileCount)
		def percentageUnassignedAppCount = applicationCount ? percOfCount(unassignedAppCount, applicationCount) :100
		def percentageUnassignedPhyServerCount = totalPhysicalServerCount ? percOfCount(unassignedPhysicalServerCount, totalPhysicalServerCount) :100
		def percentageUnassignedVirtServerCount = totalVirtualServerCount ? percOfCount(unassignedVirtualServerCount, totalVirtualServerCount) :100
		def percentageUnassignedDatabaseCount = databaseCount ? percOfCount(unassignedDbCount, databaseCount) :100
		def percentageUnassignedPhyStorageCount = phyStorageCount ? percOfCount(unAssignedPhyStorageCount, phyStorageCount) :100
		def percentageUnassignedFileCount = fileCount ? percOfCount(unassignedFilesCount, fileCount) :100
		def percentageUnassignedOtherCount = otherAssetCount ? percOfCount(unassignedOtherCount, otherAssetCount) :100

		// Query to obtain the count of Servers in 'Moved' Plan Status
		def serversCountsQuery = """SELECT
				assetClass,
				COUNT(ae) AS all,
				SUM(CASE WHEN ae.planStatus=:movedStatus THEN 1 ELSE 0 END) AS allMoved
			FROM AssetEntity ae
			WHERE ae.project=:project
			AND ae.assetClass = :deviceAssetClass
			AND ae.assetType IN (:allServers)
			AND ae.moveBundle IN (:moveBundles)
			GROUP BY ae.assetClass"""

		def serversCountsQueryParams = [
				project: project,
				moveBundles: moveBundleList,
				movedStatus: AssetEntityPlanStatus.MOVED,
				deviceAssetClass: AssetClass.DEVICE,
				allServers: AssetType.allServerTypes]

		def serversCompletedPercentage = 0
		def serversCountsQueryResults = AssetEntity.executeQuery(serversCountsQuery, serversCountsQueryParams)
		// Make sure this does not return null while getting [0] element.
		if (serversCountsQueryResults.size() > 0) {
			serversCountsQueryResults = serversCountsQueryResults[0]
			def totalServersCount = serversCountsQueryResults[1].intValue()
			// Make sure to prevent Division by zero error while calling countAppPercentage method.
			if (totalServersCount > 0) {
				serversCompletedPercentage = countAppPercentage(totalServersCount, serversCountsQueryResults[2].intValue())
			}
		}

		Map discovery = [
				appsValidatedPercentage : 100 - percentageAppToValidate,
				appsPlanReadyPercentage : percentagePlanReady,
				activeTasks : openIssue,
				overdueTasks : dueOpenIssue,
				appCount : applicationCount,
				appToValidate :  appToValidate,
				phyServerCount :  totalPhysicalServerCount,
				phyServerToValidate :  psToValidate,
				virtServerCount :  totalVirtualServerCount,
				virtServerToValidate :  vsToValidate,
				dbCount : databaseCount,
				dbToValidate : dbToValidate,
				phyStorageCount :  phyStorageCount,
				phyStorageToValidate : phyStorageToValidate,
				fileCount :  fileCount,
				fileToValidate :  fileToValidate,
				otherCount :  otherAssetCount,
				otherToValidate :  otherToValidate
		]

		Map analysis = [
				assignedAppPerc: assignedAppPerc,
				confirmedAppPerc: confirmedAppPerc,
				validated: applicationCount - unknown,
				planReady: planReady,
				appDependenciesCount: appDependenciesCount,
				serverDependenciesCount: serverDependenciesCount,
				pendingAppDependenciesCount: pendingAppDependenciesCount,
				pendingServerDependenciesCount: pendingServerDependenciesCount,
				activeTasks: issues.size(),
				overdueTasks: generalOverDue,
				groupPlanMethodologyCount: groupPlanMethodologyCount
		]

		Map execution = [
				movedAppPerc: movedAppPerc,
				movedServerPerc: serversCompletedPercentage,
				moveEventList: moveEventList,
				openTasks: openTasks,
				unassignedAppCount: unassignedAppCount,
				unassignedAppPerc: percentageUnassignedAppCount,
				appDonePerc: percAppDoneCount,
				unassignedPhyServerCount: unassignedPhysicalServerCount,
				unassignedPhyServerPerc: percentageUnassignedPhyServerCount,
				phyServerDonePerc: percentagePhysicalServerCount,
				unassignedVirtServerCount: unassignedVirtualServerCount,
				unassignedVirtServerPerc: percentageUnassignedVirtServerCount,
				virtServerDonePerc: percVirtualServerCount,
				unassignedDbCount: unassignedDbCount,
				dbDonePercentage: percentageDBCount,
				unassignedDbPerc: percentageUnassignedDatabaseCount,
				unassignedPhyStorageCount: unAssignedPhyStorageCount,
				phyStorageDonePerc: percentagePhyStorageCount,
				unassignedPhyStoragePerc: percentageUnassignedPhyStorageCount,
				unassignedFilesCount: unassignedFilesCount,
				unassignedFilesPerc: percentageUnassignedFileCount,
				filesDonePerc: percentageFilesCount,
				unassignedOtherCount: unassignedOtherCount,
				unassignedOtherPerc: percentageUnassignedOtherCount,
				otherDonePerc: percentageOtherCount,
				appList: appList,
				phyServerList: phyServerList,
				virtServerList: virtServerList,
				dbList: dbList,
				phyStorageList: phyStorageList,
				filesList: filesList,
				otherList: otherTypeList,
				eventStartDate: eventStartDate
		]

		renderSuccessJson([
				discovery : discovery,
				analysis : analysis,
				execution : execution
		])
	}
}
