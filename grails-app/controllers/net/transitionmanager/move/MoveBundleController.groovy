package net.transitionmanager.move

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.DependencyAnalyzerTabs
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import grails.gorm.transactions.NotTransactional
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.asset.CommentService
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import net.transitionmanager.command.DependencyConsoleCommand
import net.transitionmanager.command.bundle.AssetsAssignmentCommand
import net.transitionmanager.common.ControllerService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.ProgressService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskService
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class MoveBundleController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	CommentService commentService
	ControllerService controllerService
	JdbcTemplate jdbcTemplate
	MoveBundleService moveBundleService
	PartyRelationshipService partyRelationshipService
	ProgressService progressService
	Scheduler quartzScheduler
	TaskService taskService
	UserPreferenceService userPreferenceService
    CustomDomainService customDomainService
	MoveEventService moveEventService

	@HasPermission(Permission.BundleView)
	def projectMoveBundles() {
		Project project = securityService.loadUserCurrentProject()
		def moveBundlesList
		if (project) {
			moveBundlesList = MoveBundle.findAllByProject(project, [sort: 'name', order: 'asc'])
		}
		render moveBundlesList as JSON
	}

	@HasPermission(Permission.DashboardMenuView)
	@NotTransactional()
	def planningStats() {
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
		def app = AssetType.APPLICATION.toString()
		def server = AssetType.SERVER.toString()
		def vm = AssetType.VM.toString()
		def blade = AssetType.BLADE.toString()

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
		def serversOfPlanningBundle = AssetEntity.findAll(deviceQuery, countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.allServerTypes])
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
			      status: [AssetCommentStatus.READY, AssetCommentStatus.STARTED, AssetCommentStatus.PENDING]])[0]

			openTasks << [moveEvent: moveEvent.id , count: openIssueCount]
		}

		// ----------------------------------------------------------------------------
		// Get the totals count for assigned and unassigned assets
		// ----------------------------------------------------------------------------

		// Find Move bundles that are not assigned to a Event and is the bundle is used for planning
		def unassignedMoveBundles = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.moveEvent IS NULL \
			AND mb.useForPlanning = true AND mb.project = :project ", [project:project])
		def assetTypeQuery = 'AND ae.assetType IN (:type)'

		// Construct the query that will include counts of non-event bundles if any exist just the bundle being NULL
		def assetCountQueryArgs = [project:project]
		def unassignedMBQuery = 'ae.moveBundle IS NULL'
		if (unassignedMoveBundles) {
			 unassignedMBQuery = "(ae.moveBundle IN (:unassignedMoveBundles) OR $unassignedMBQuery)"
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
		// Calculate the Assigned Application Count
		def unassignedAssetCount = unassignedDbCount + unassignedFilesCount + unassignedAppCount + unassignedDeviceCount

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
		def unassignedServerCount = unassignedPhysicalServerCount + unassignedVirtualServerCount
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
		def date = AssetDependencyBundle.findByProject(project,[sort:"lastUpdated",order:"desc"])?.lastUpdated
		time = TimeUtil.formatDateTime(date, TimeUtil.FORMAT_DATE_TIME_8)

		def today = new Date()
		def todayClearedTime = today.clearTime()
		def issueQuery = "from AssetComment a  where a.project =:project and a.category in (:category) and a.status not in (:status) and a.commentType =:type AND a.isPublished = true"
		def issueArgs = [project:project, status: [AssetCommentStatus.COMPLETED, AssetCommentStatus.TERMINATED], type:AssetCommentType.TASK.toString()]

		def openIssue =  AssetComment.findAll(issueQuery,issueArgs + [category : AssetComment.discoveryCategories]).size()
		def dueOpenIssue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs + [category : AssetComment.discoveryCategories, dueDate: todayClearedTime]).size()
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
		def percentagePSToValidate= totalPhysicalServerCount ? percOfCount(psToValidate, totalPhysicalServerCount) :100
		def percentageVMToValidate= totalVirtualServerCount ? percOfCount(vsToValidate, totalVirtualServerCount) : 100
		def percentageDBToValidate= databaseCount ? percOfCount(dbToValidate, databaseCount) :100
		def percentageStorToValidate=phyStorageCount ? percOfCount(phyStorageToValidate, phyStorageCount) :100
		int percentageFilesCount = percOfCount(fileToValidate, fileCount)
		def percentageOtherToValidate= otherAssetCount ? percOfCount(otherToValidate, otherAssetCount) :100
		def percentageUnassignedAppCount = applicationCount ? percOfCount(unassignedAppCount, applicationCount) :100

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

		return [
			appList                       : appList,
			applicationCount              : applicationCount,
			unassignedAppCount            : unassignedAppCount,
			assignedAppPerc               : assignedAppPerc,
			confirmedAppPerc              : confirmedAppPerc,
			movedAppPerc                  : movedAppPerc,
			movedServersPerc              : serversCompletedPercentage,
			appToValidate                 : appToValidate,
			unassignedServerCount         : unassignedServerCount,
			unassignedPhysicalServerCount : unassignedPhysicalServerCount,
			percentagePhysicalServerCount : percentagePhysicalServerCount,
			psToValidate                  : psToValidate,
			unassignedVirtualServerCount  : unassignedVirtualServerCount,
			percVirtualServerCount        : percVirtualServerCount,
			vsToValidate                  : vsToValidate,
			dbList                        : dbList,
			dbCount                       : databaseCount,
			unassignedDbCount             : unassignedDbCount,
			percentageDBCount             : percentageDBCount,
			dbToValidate                  : dbToValidate,
			// Files (aka Storage)
			filesList                     : filesList,
			fileCount                     : fileCount,
			unassignedFilesCount          : unassignedFilesCount,
			percentageFilesCount          : percentageFilesCount,
			fileToValidate                : fileToValidate,
			unAssignedPhyStorageCount     : unAssignedPhyStorageCount,
			phyStorageCount               : phyStorageCount,
			phyStorageList                : phyStorageList,
			phyStorageToValidate          : phyStorageToValidate,
			percentagePhyStorageCount     : percentagePhyStorageCount,
			// Other
			otherTypeList                 : otherTypeList,
			otherAssetCount               : otherAssetCount,
			unassignedOtherCount          : unassignedOtherCount,
			percentageOtherCount          : percentageOtherCount,
			otherToValidate               : otherToValidate,

			// assetList:assetList, assetCount:assetCount,
			totalServerCount              : totalServerCount,
			allServerList                 : allServerList,
			phyServerCount                : totalPhysicalServerCount,
			phyServerList                 : phyServerList,
			virtServerCount               : totalVirtualServerCount,
			virtServerList                : virtServerList,

			unassignedAssetCount          : unassignedAssetCount,
			appDependenciesCount          : appDependenciesCount,
			pendingAppDependenciesCount   : pendingAppDependenciesCount,
			serverDependenciesCount       : serverDependenciesCount,
			pendingServerDependenciesCount: pendingServerDependenciesCount,

			project                       : project,
			moveEventList                 : moveEventList,
			moveBundleList                : moveBundleList,
			planningDashboard             : 'planningDashboard',
			eventStartDate                : eventStartDate,
			date                          : time,

			issuesCount                   : issues.size(),
			openIssue                     : openIssue,
			dueOpenIssue                  : dueOpenIssue,
			openTasks                     : openTasks,
			generalOverDue                : generalOverDue,

			validated                     : applicationCount - unknown,
			planReady                     : planReady,
			movedAppCount                 : movedAppCount,
			assignedAppCount              : assignedAppCount,
			confirmedAppCount             : confirmedAppCount,
			percAppDoneCount              : percAppDoneCount,
			percentageAppToValidate       : percentageAppToValidate,
			percentagePlanReady           : percentagePlanReady,

			percentagePSToValidate        : percentagePSToValidate,
			percentageVMToValidate        : percentageVMToValidate,
			percentageDBToValidate        : percentageDBToValidate,
			percentageStorToValidate      : percentageStorToValidate,
			percentageOtherToValidate     : percentageOtherToValidate,
			percentageUnassignedAppCount  : percentageUnassignedAppCount,

			groupPlanMethodologyCount     : groupPlanMethodologyCount
		]
	} // end-of-planningStats

	/**
	 * Control function to render the Dependency Analyzer (was Dependency Console)
	 * @param  Console command object that contains bundle, tagIds, tagMatch, assinedGroup, subsection, groupId, assetName
	 */
	@HasPermission(Permission.DepAnalyzerView)
	@NotTransactional()
	def dependencyConsole() {
		DependencyConsoleCommand console = populateCommandObject(DependencyConsoleCommand)
		validateCommandObject(console)
		licenseAdminService.checkValidForLicenseOrThrowException()
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		// Check for correct URL params
		if ( console.subsection || console.groupId) { // is the drill-in URL
			if (!(console.subsection && console.groupId)) { // If any exists, then both should be present in the URL
				throw new InvalidParamException("Subsection and Group Id params are both required.")
			}
			String subsec = console.subsection as String // Check for valid tab name
			if (!(subsec.toUpperCase() in (DependencyAnalyzerTabs.values() as String[]))) {
				throw new InvalidParamException("Invalid Subsection name: ${subsec}")
			}
		}
		//Date start = new Date()
		userPreferenceService.setPreference(PREF.ASSIGNED_GROUP,
			console.assignedGroup ?: userPreferenceService.getPreference(PREF.ASSIGNED_GROUP) ?: "1")

		def map = moveBundleService.dependencyConsoleMap(
			project,
			console.bundle,
			console.tagIds,
			console.tagMatch,
			console.assignedGroup,
			null,
			false,
			console.subsection,
			console.groupId,
			console.assetName
		)

		//log.info 'dependencyConsole() : moveBundleService.dependencyConsoleMap() took {}', TimeUtil.elapsed(start)
		return map
	}

	/**
	 * Controller to render the Dependency Bundle Details Table.
	 * This method is called from the Dependency Analyzer in an Ajax call to render the dependency table.
	 * @param  Console command object that contains bundle, tagIds, tagMatch, assinedGroup, subsection, groupId, assetName
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def dependencyBundleDetails() {
		DependencyConsoleCommand console = populateCommandObject(DependencyConsoleCommand)
		validateCommandObject(console)
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		userPreferenceService.setPreference(PREF.ASSIGNED_GROUP,
				console.assignedGroup ?: userPreferenceService.getPreference(PREF.ASSIGNED_GROUP) ?: "1")
		def model = moveBundleService.dependencyConsoleMap(
				project,
				console.bundle,
				console.tagIds,
				console.tagMatch,
				console.assignedGroup,
				null,
				false,
				console.subsection,
				console.groupId,
				console.assetName
		)
		render(template: 'dependencyBundleDetails', model: model)
	}

	@HasPermission(Permission.DepAnalyzerGenerate)
	def generateDependency() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def baseName = "generateDependency"
		def key = baseName + "-" + UUID.randomUUID()
		progressService.create(key)

		def jobName = "TM-" + baseName + "-" + project.id
		log.info 'Initiate Generate Dependency'

		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl(jobName, null, new Date(System.currentTimeMillis() + 2000))
		trigger.jobDataMap.putAll(params)

		String connectionTypes = WebUtil.checkboxParamAsString(request.getParameterValues("connection"))
		String statusTypes = WebUtil.checkboxParamAsString(request.getParameterValues("status"))
		def isChecked = params.saveDefault

		trigger.jobDataMap.key = key
		trigger.jobDataMap.username = securityService.currentUsername
		trigger.jobDataMap.projectId = project.id
		trigger.jobDataMap.connectionTypes = connectionTypes
		trigger.jobDataMap.statusTypes = statusTypes
		trigger.jobDataMap.isChecked = isChecked
		trigger.jobDataMap.userLoginName = securityService.userLoginPerson.toString()

		trigger.setJobName('GenerateDependencyGroupsJob')
		trigger.setJobGroup('tdstm-dependency-groups')
		try{
			quartzScheduler.scheduleJob(trigger)
			progressService.update(key, 1, 'In progress')
		}catch(ex){
			log.warn 'generateDependency failed to create Quartz job : {}', ex.message
			progressService.update(key, 100I, ProgressService.FAILED,
				'It appears that someone else is currently generating dependency groups for this project. Please try again later.')
		}

		renderSuccessJson(key: key)
	}

	/**
	 * Assigns one or more assets to a specified bundle, and add tags
	 */
	@HasPermission(Permission.AssetEdit)
	def assetsAssignment() {
		AssetsAssignmentCommand assetsAssignment = populateCommandObject(AssetsAssignmentCommand)
		validateCommandObject(assetsAssignment)

		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		moveBundleService.assignAssets(assetsAssignment.assets, assetsAssignment.tagIds,assetsAssignment.moveBundle, assetsAssignment.planStatus, project)

		forward(controller: "assetEntity", action: "retrieveLists",
			     params: [entity: params.assetType, dependencyBundle: session.getAttribute('dependencyBundle')])
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

	/**
	 * Sets compactControl preference
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def setCompactControlPref() {
		String key = params.prefFor
		String selected = params.selected
		if (selected) {
			userPreferenceService.setPreference(key, selected)
			session.setAttribute(key,selected)
		}
		render selected
	}
}
