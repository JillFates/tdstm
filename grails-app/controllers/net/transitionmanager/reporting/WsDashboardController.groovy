package net.transitionmanager.reporting

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files

import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.MoveEventSnapshot
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskService
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class WsDashboardController implements ControllerMethods {

	public static final String CLOCK_MODE_NONE 			= 'none'
	public static final String CLOCK_MODE_COUNTDOWN 	= 'countdown'
	public static final String CLOCK_MODE_ELAPSED 		= 'elapsed'
	public static final String CLOCK_MODE_FINISHED 		= 'finished'

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

		Date sysTime = TimeUtil.nowGMT()
		int sysTimeInMs = sysTime.getTime() / 1000

		def planSumCompTime
		def moveEventPlannedSnapshot
		def moveEventRevisedSnapshot
		def revisedComp
		def dayTime
		String clockMode = CLOCK_MODE_NONE
		String eventString = ""
		if (moveEvent) {

			Date eventStartTime = moveEvent.estStartTime
			Date eventComplTime = moveEvent.estCompletionTime

			if (eventStartTime) {
				if ( eventStartTime > sysTime ) {
					dayTime = TimeCategory.minus(eventStartTime, sysTime)
					eventString = "Countdown Until Event"
					clockMode = CLOCK_MODE_COUNTDOWN
				} else if (eventStartTime < sysTime && ( !eventComplTime || eventComplTime > sysTime )) {
					dayTime = TimeCategory.minus(sysTime, eventStartTime)
					eventString = "Elapsed Event Time"
					clockMode = CLOCK_MODE_ELAPSED
				} else {
					dayTime = TimeCategory.minus(sysTime, eventComplTime)
					eventString = "Time since the event finished"
					clockMode = CLOCK_MODE_FINISHED
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

		String eventClock = TimeUtil.formatTimeDuration(dayTime)

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
				dayTime: eventClock,
				clockMode: clockMode,
				eventDescription: moveEvent?.description,
				eventString: eventString,
				eventRunbook: moveEvent?.runbookStatus
			],
			revSum: [dialInd: moveEventRevisedSnapshot?.dialIndicator,
			         compTime: TimeUtil.formatDateTime(revisedComp, TimeUtil.FORMAT_DATE_TIME_11)],
			runbookOn: project.runbookOn,
			eventStartDate: moveEvent.estStartTime
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
			if(!key) key = Application.PLAN_METHODOLOGY_UNKNOWN

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
					result[Application.PLAN_METHODOLOGY_UNKNOWN] = 0
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
		Date date = AssetDependencyBundle.findByProject(project,[sort:"lastUpdated", order:"desc"])?.lastUpdated

		Date today = new Date()
		Date todayClearedTime = today.clearTime()
		String issueQuery = "from AssetComment a  where a.project =:project and a.category in (:category) and a.status not in (:status) and a.commentType =:type AND a.isPublished = true"
		Map issueArgs = [project:project, status: [AssetCommentStatus.COMPLETED, AssetCommentStatus.TERMINATED], type:AssetCommentType.TASK.toString()]

		Integer openIssue =  AssetComment.findAll(issueQuery,issueArgs + [category : AssetComment.discoveryCategories]).size()
		Integer dueOpenIssue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs + [category : AssetComment.discoveryCategories, dueDate: todayClearedTime]).size()
		List<AssetComment> issues = AssetComment.findAll("FROM AssetComment a where a.project = :project and a.commentType = :type and a.status =:status  \
			and a.category in (:category) AND a.isPublished = true",[project:project, type:AssetCommentType.TASK, status: AssetCommentStatus.READY , category: AssetComment.planningCategories])
		Integer generalOverDue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs + [category: AssetComment.planningCategories, dueDate:today]).size()

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
