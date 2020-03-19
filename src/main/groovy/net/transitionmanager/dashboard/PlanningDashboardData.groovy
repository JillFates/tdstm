package net.transitionmanager.dashboard

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.NumberUtil
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.Project
import net.transitionmanager.task.AssetComment

class PlanningDashboardData {

	private Map eventStartDate = [:]


	Project project
	List<MoveBundle> moveBundleList
	List<MoveEvent> moveEventList


	BasicMetrics basicMetrics
	DeviceMetrics deviceMetrics
	DependencyMetrics dependencyMetrics
	IssueMetrics issueMetrics
	MoveEventMetrics moveEventMetrics
	PercentageMetrics percentageMetrics
	PlanMethodologyMetrics planMethodologyMetrics
	ValidateMetrics validateMetrics

	/**
	 * Constructor that takes the user's active project as parameter.
	 * @param project
	 */
	PlanningDashboardData(Project project) {
		this.project = project
	}

	/**
	 * Calculate and return all the data required for the planning dashboard.
	 * @return
	 */
	Map<String, Map> getDataForDashboard() {
		// Fetch all the planning bundles for the user's active project.
		moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project, true, [sort: 'startTime'])
		// Fetch the events for the given bundles, sorted by start date.
		moveEventList = getEventsForBundles()
		// Get the number of assets for each asset type and event.
		moveEventMetrics = PlanningDashboardMetrics.calculateMetrics(MoveEventMetrics, this)
		// Calculate some basic totals
		basicMetrics = PlanningDashboardMetrics.calculateMetrics(BasicMetrics, this)
		// Calculate device related metrics
		deviceMetrics = PlanningDashboardMetrics.calculateMetrics(DeviceMetrics, this)
		// Calculate Plan Methodology metrics
		planMethodologyMetrics = PlanningDashboardMetrics.calculateMetrics(PlanMethodologyMetrics, this)
		// Calculate metrics for assets that need validation
		validateMetrics = PlanningDashboardMetrics.calculateMetrics(ValidateMetrics, this)
		// Get the metrics for Tasks
		issueMetrics = PlanningDashboardMetrics.calculateMetrics(IssueMetrics, this)
		// Fetch different dependency metrics
		dependencyMetrics = PlanningDashboardMetrics.calculateMetrics(DependencyMetrics, this)
		// Calculate percentages
		percentageMetrics = PlanningDashboardMetrics.calculateMetrics(PercentageMetrics, this)


		return [
			discovery: getDiscoveryResults(),
			analysis: getAnalysisResults(),
			execution: getExecutionResults()
		]
	}

	/**
	 * Put together the discovery results.
	 * @return
	 */
	private Map getDiscoveryResults() {
		return [
			appsValidatedPercentage : 100 - percentageMetrics.appToValidatePerc,
			appsPlanReadyPercentage : percentageMetrics.planReadyPerc,
			activeTasks : issueMetrics.openIssue,
			overdueTasks : issueMetrics.dueOpenIssue,
			appCount : basicMetrics.applicationCount,
			appToValidate :  validateMetrics.appToValidate,
			phyServerCount :  deviceMetrics.totalPhysicalServerCount,
			phyServerToValidate :  validateMetrics.psToValidate,
			virtServerCount :  deviceMetrics.totalVirtualServerCount,
			virtServerToValidate :  validateMetrics.vsToValidate,
			dbCount : basicMetrics.databaseCount,
			dbToValidate : validateMetrics.dbToValidate,
			phyStorageCount :  deviceMetrics.phyStorageCount,
			phyStorageToValidate : validateMetrics.phyStorageToValidate,
			fileCount :  basicMetrics.fileCount,
			fileToValidate :  validateMetrics.fileToValidate,
			otherCount :  deviceMetrics.otherAssetCount,
			otherToValidate :  validateMetrics.otherToValidate
		]
	}

	/**
	 * Put together the analysis results.
	 * @return
	 */
	private Map getAnalysisResults() {
		return [
			assignedAppPerc: percentageMetrics.assignedAppPerc,
			confirmedAppPerc: percentageMetrics.confirmedAppPerc,
			validated: validateMetrics.validated,
			planReady: validateMetrics.planReady,
			appDependenciesCount: dependencyMetrics.appDependenciesCount,
			serverDependenciesCount: dependencyMetrics.serverDependenciesCount,
			pendingAppDependenciesCount: dependencyMetrics.pendingAppDependenciesCount,
			pendingServerDependenciesCount: dependencyMetrics.pendingServerDependenciesCount,
			activeTasks: issueMetrics.issues,
			overdueTasks: issueMetrics.generalOverDue,
			groupPlanMethodologyCount: planMethodologyMetrics.metrics
		]
	}

	/**
	 * Put together the execution results.
	 * @return
	 */
	private Map getExecutionResults() {
		return [
			movedAppPerc: percentageMetrics.movedAppPerc,
			movedServerPerc: percentageMetrics.serversCompletedPercentage,
			moveEventList: moveEventList,
			openTasks: moveEventMetrics.metrics['openTasks'],
			unassignedAppCount: basicMetrics.unassignedApplicationCount,
			unassignedAppPerc: percentageMetrics.unassignedAppPerc,
			appDonePerc: percentageMetrics.doneAppPerc,
			unassignedPhyServerCount: deviceMetrics.unassignedPhysicalServerCount,
			unassignedPhyServerPerc: percentageMetrics.unassignedPhysicalServerPerc,
			phyServerDonePerc: percentageMetrics.physicalServerPerc,
			unassignedVirtServerCount: deviceMetrics.unassignedVirtualServerCount,
			unassignedVirtServerPerc: percentageMetrics.unassignedVirtualServerPerc,
			virtServerDonePerc: percentageMetrics.virtualServerPerc,
			unassignedDbCount: basicMetrics.unassignedDatabaseCount,
			dbDonePercentage: percentageMetrics.movedDatabasePerc,
			unassignedDbPerc: percentageMetrics.unassignedDatabasePerc,
			unassignedPhyStorageCount: deviceMetrics.unAssignedPhyStorageCount,
			phyStorageDonePerc: percentageMetrics.physicalStoragePerc,
			unassignedPhyStoragePerc: percentageMetrics.unassignedPhyStoragePerc,
			unassignedFilesCount: basicMetrics.unassignedFileCount,
			unassignedFilesPerc: percentageMetrics.unassignedFilePerc,
			filesDonePerc: percentageMetrics.doneFilePerc,
			unassignedOtherCount: deviceMetrics.unassignedOtherCount,
			unassignedOtherPerc: percentageMetrics.unassignedOtherPerc,
			otherDonePerc: percentageMetrics.movedOtherPerc,
			appList: moveEventMetrics.metrics['application'],
			phyServerList: moveEventMetrics.metrics['physicalAsset'],
			virtServerList: moveEventMetrics.metrics['virtual'],
			dbList: moveEventMetrics.metrics['database'],
			phyStorageList: moveEventMetrics.metrics['physicalStorage'],
			filesList: moveEventMetrics.metrics['file'],
			otherList: moveEventMetrics.metrics['other'],
			eventStartDate: eventStartDate
		]
	}

	/**
	 * Return the events associated with the planning bundles of the user's active project.
	 * @return
	 */
	private List<MoveEvent> getEventsForBundles() {
		List<MoveEvent> moveEventList = moveBundleList*.moveEvent.unique()
		moveEventList.remove(null)
		return moveEventList.sort { MoveEvent a, MoveEvent b ->
			getEventStartDate(a) <=> getEventStartDate(b)
		}
	}

	/**
	 * Determine the start date for the given move event instance.
	 * @param moveEvent
	 * @return
	 */
	private Date getEventStartDate(MoveEvent moveEvent) {
		Date start = moveEvent.estStartTime
		if(!start){
			start = moveEventService.getEventTimes(moveEvent.id).start
		}
		return start
	}

	private static MoveEventService getMoveEventService() {
		return ApplicationContextHolder.getBean('moveEventService')
	}


	/* ************************************************************************************************************** */

	/**
	 * This class serves as a superclass for the concrete metric classes.
	 */
	private abstract class PlanningDashboardMetrics {

		static final String selectCount = 'SELECT count(ae)'
		static final String baseWhere = 'WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)'
		static final String deviceWhere = "$baseWhere AND ae.assetClass=:assetClass AND ae.assetType IN (:type)"
		static final String otherDeviceWhere = "$baseWhere AND ae.assetClass=:assetClass AND (ae.assetType NOT IN (:type) OR ae.assetType IS NULL)"
		protected PlanningDashboardData planningDashboardData

		/**
		 * This method serves as a factory for the concrete metric classes. It also calls the method for
		 * calculating the actual metrics.
		 *
		 * @param metricsClass
		 * @param pdData
		 * @return
		 */
		static <T extends PlanningDashboardMetrics> T calculateMetrics(Class<T> metricsClass, PlanningDashboardData pdData) {
			T metricInstance = metricsClass.newInstance()
			metricInstance.planningDashboardData = pdData
			metricInstance.calculate()
			return metricInstance
		}

		/**
		 * Count the number of assets that match the given criteria.
		 * @param asset - 'Application', 'Database', etc.
		 * @param countArgs - parameters to inject to the query.
		 * @param additionalClauses - additional clauses that need to be used for querying.
		 * @param isOther - a flag for signaling that the query is for other devices. This is because "Other Devices" query is a little different.
		 * @return
		 */
		Long getAssetCount(String asset, Map countArgs, List<String> additionalClauses = [], boolean isOther = false) {
			String where = (asset == 'AssetEntity' && !additionalClauses) ? (isOther ? otherDeviceWhere : deviceWhere) : baseWhere
			String countQuery = "$selectCount FROM $asset ae $where ${additionalClauses.join(" ")}"
			return AssetEntity.executeQuery(countQuery, countArgs)[0]
		}

		/**
		 * Return the list of assets matching the given criteria.
		 * @param asset
		 * @param args
		 * @return
		 */
		List<AssetEntity> getAssets(String asset, Map args) {
			String where = (asset == 'AssetEntity') ? deviceWhere : baseWhere
			String assetQuery = "FROM $asset ae $where"
			return AssetEntity.executeQuery(assetQuery, args)
		}


		/**
		 * Shortcut to access the project set in the planning dashboard data instance.
		 * @return
		 */
		Project getProject() {
			return planningDashboardData.project
		}

		/**
		 * Shortcut to access the project set in the planning dashboard data instance.
		 * @return
		 */
		List<MoveBundle> getMoveBundleList() {
			return planningDashboardData.moveBundleList
		}

		/**
		 * Shortcut to access the list of events fetched in the planning dashboard data instance.
		 * @return
		 */
		List<MoveEvent> getMoveEventList() {
			return planningDashboardData.moveEventList
		}

		/**
		 * Each concrete metrics class needs to implement here the logic that calculates its metrics.
		 */
		abstract void calculate()
	}

	/* ************************************************************************************************************** */

	/**
	 * This class deals with basic metrics such as the total number of Applications, Databases, etc.
	 */
	private class BasicMetrics extends PlanningDashboardMetrics{

		long applicationCount
		long unassignedApplicationCount
		long assignedApplicationCount
		long movedApplicationCount
		long lockedApplicationCount
		long confirmedApplicationCount

		long databaseCount
		long unassignedDatabaseCount

		long unassignedDeviceCount

		long fileCount
		long unassignedFileCount


		void calculate() {
			String basicCountsQuery = """SELECT
				assetClass,
				COUNT(ae) AS all1,
				SUM(CASE WHEN ae.planStatus=:unassignStatus THEN 1 ELSE 0 END)  AS allUnassigned2,
				SUM(CASE WHEN ae.planStatus=:movedStatus THEN 1 ELSE 0 END)     AS allMoveded3,
				SUM(CASE WHEN ae.planStatus=:confirmedStatus THEN 1 ELSE 0 END) AS allConfirmed4,
				SUM(CASE WHEN ae.planStatus=:lockedStatus THEN 1 ELSE 0 END) AS allLocked5,
				SUM(CASE WHEN ae.planStatus=:assignedStatus THEN 1 ELSE 0 END)  AS allAssigned6,
				SUM(CASE WHEN ae.planStatus=:confirmedStatus THEN 1 ELSE 0 END)  AS allConfirmed7
			FROM AssetEntity ae
			WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)
			GROUP BY ae.assetClass"""

			Map basicCountsParams = [
				project: project,
				moveBundles: moveBundleList,
				unassignStatus: AssetEntityPlanStatus.UNASSIGNED,
				movedStatus: AssetEntityPlanStatus.MOVED,
				confirmedStatus: AssetEntityPlanStatus.CONFIRMED,
				lockedStatus: AssetEntityPlanStatus.LOCKED,
				assignedStatus: AssetEntityPlanStatus.ASSIGNED]

			List basicCountsResults = AssetEntity.executeQuery(basicCountsQuery, basicCountsParams)
			basicCountsResults.each { ua ->
				switch(ua[0]) {
					case AssetClass.APPLICATION:
						applicationCount = ua[1]
						unassignedApplicationCount = ua[2]
						assignedApplicationCount = ua[6]
						movedApplicationCount = ua[3]
						lockedApplicationCount = ua[5]
						confirmedApplicationCount = ua[4]
						break
					case AssetClass.DATABASE:
						unassignedDatabaseCount = ua[2]
						break
					case AssetClass.DEVICE:
						unassignedDeviceCount = ua[2]
						break
					case AssetClass.STORAGE:
						unassignedFileCount = ua[2]
						break
				}
			}

			Map countParams = [project: project, moveBundles: moveBundleList]
			databaseCount = getAssetCount('Database', countParams)
			fileCount = getAssetCount('Files', countParams)

		}
	}

	/* ************************************************************************************************************** */

	class DeviceMetrics extends PlanningDashboardMetrics {

		long totalDeviceCount
		long unassignedAllDeviceCount
		long totalPhysicalServerCount
		long unassignedPhysicalServerCount
		long totalVirtualServerCount
		long unassignedVirtualServerCount
		long phyStorageCount
		long unAssignedPhyStorageCount
		long totalServerCount
		long otherAssetCount
		long unassignedOtherCount

		void calculate() {
			// Get the various DEVICE types broken out
			String deviceMetricsQuery = """SELECT
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

			Map deviceMetricsParams = [
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

			totalDeviceCount = deviceMetrics[0]
			unassignedAllDeviceCount = deviceMetrics[1]
			totalPhysicalServerCount = deviceMetrics[2]
			unassignedPhysicalServerCount = deviceMetrics[3]
			totalVirtualServerCount = deviceMetrics[4]
			unassignedVirtualServerCount = deviceMetrics[5]
			phyStorageCount = deviceMetrics[6]
			unAssignedPhyStorageCount = deviceMetrics[7]
			totalServerCount = totalPhysicalServerCount + totalVirtualServerCount
			otherAssetCount = totalDeviceCount - phyStorageCount - totalServerCount
			unassignedOtherCount = unassignedAllDeviceCount - unassignedPhysicalServerCount - unassignedVirtualServerCount - unAssignedPhyStorageCount
		}
	}

	/* ************************************************************************************************************** */

	/**
	 * This metrics class is responsible for determining plan methodology related metrics.
	 */
	private class PlanMethodologyMetrics extends PlanningDashboardMetrics {

		Map metrics

		void calculate() {
			String customField = project.planMethodology ?: "''"
			String groupingSumQuery = "SELECT new map(${customField} as key, COUNT(ae) as count) FROM Application ae WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)"
			if (customField) {
				groupingSumQuery += " group by ${customField}"
			}
			List groupValues = Application.executeQuery(groupingSumQuery, [project:project, moveBundles:moveBundleList])
			metrics = groupValues.inject([:]) { groups, it ->
				def key = it.key
				if(!key){
					key = Application.PLAN_METHODOLOGY_UNKNOWN
				}
				if(!groups[key]){
					groups[key] = 0
				}
				groups[key] += it.count
				return groups
			}

			// sort values based on custom field setting configuration
			Map customFieldSetting = customDomainService.findCustomField(project, AssetClass.APPLICATION.toString()) {
				it.field == customField
			}

			if (customFieldSetting?.constraints?.values) {
				Map sortedMap = customFieldSetting.constraints.values.inject([:]) { result, it ->
					if ( ! it ) {
						result[Application.PLAN_METHODOLOGY_UNKNOWN] = 0
					} else if (metrics[it]) {
						result[it] = 0
					}
					result
				}
				metrics = sortedMap + metrics
			}
		}

		private static CustomDomainService getCustomDomainService() {
			return ApplicationContextHolder.getBean('customDomainService')
		}

	}

	/* ************************************************************************************************************** */

	/**
	 * This metrics class fetches the number of assets that still need to be validated.
	 */
	class ValidateMetrics extends PlanningDashboardMetrics {

		long appToValidate
		long dbToValidate
		long fileToValidate
		long otherToValidate
		long planReady
		long validated
		long phyStorageToValidate
		long psToValidate
		long vsToValidate

		void calculate() {
			List<String> basicValidateClauses = ['AND ae.validation=:validation']
			List<String> deviceValidateClauses = basicValidateClauses + ['AND ae.assetType IN (:type) AND ae.assetClass=:assetClass']
			List<String> otherValidateClauses = basicValidateClauses + ["AND COALESCE(ae.assetType,'') NOT IN (:type) AND ae.assetClass=:assetClass"]

			Map validateArgs = [project: project, moveBundles: moveBundleList, validation: ValidationType.UNKNOWN]
			Map deviceValidateArgs = validateArgs + [assetClass:AssetClass.DEVICE]
			Map planReadyArgs = [project: project, moveBundles: moveBundleList, validation: ValidationType.PLAN_READY]
			Map validatedArgs = [project: project, moveBundles: moveBundleList, validation: ValidationType.VALIDATED]

			appToValidate = getAssetCount('Application', validateArgs, basicValidateClauses)
			dbToValidate = getAssetCount('Database', validateArgs, basicValidateClauses)
			fileToValidate = getAssetCount('Files', validateArgs, basicValidateClauses)
			otherToValidate = getAssetCount('AssetEntity', deviceValidateArgs + [type:AssetType.nonOtherTypes], otherValidateClauses)
			planReady = getAssetCount('Application', planReadyArgs, basicValidateClauses)
			validated = getAssetCount('Application', validatedArgs, basicValidateClauses)
			phyStorageToValidate = getAssetCount('AssetEntity', deviceValidateArgs + [type:AssetType.storageTypes], deviceValidateClauses)
			psToValidate = getAssetCount('AssetEntity', deviceValidateArgs + [type:AssetType.physicalServerTypes], deviceValidateClauses)
			vsToValidate = getAssetCount('AssetEntity', deviceValidateArgs + [type:AssetType.virtualServerTypes], deviceValidateClauses)
		}
	}

	/* ************************************************************************************************************** */

	/**
	 * Class that calculates basic task related numbers.
	 */
	class IssueMetrics extends PlanningDashboardMetrics {

		long openIssue
		long dueOpenIssue
		long generalOverDue
		long issues

		void calculate() {
			Date today = new Date()
			String issueQuery = """ SELECT COUNT(a.id)
								FROM AssetComment a 
								WHERE a.project =:project AND a.category IN (:category) AND a.status != :status 
								AND a.commentType =:type AND a.isPublished = true"""
			String dueIssueQuery = issueQuery + ' AND a.dueDate < :dueDate '
			String publishedIssueQuery = """SELECT COUNT(a.id)
								FROM AssetComment a WHERE a.project = :project AND a.commentType = :type 
								AND a.status =:status AND a.category IN (:category) AND a.isPublished = true"""

			Map issueArgs = [project: project, status: AssetCommentStatus.COMPLETED, type: AssetCommentType.TASK.toString()]
			Map publishedIssueArgs = [project:project, type:AssetCommentType.TASK, status: AssetCommentStatus.READY , category: AssetComment.planningCategories]

			openIssue = AssetComment.executeQuery(issueQuery,issueArgs + [category : AssetComment.discoveryCategories])[0]
			dueOpenIssue = AssetComment.executeQuery( dueIssueQuery, issueArgs + [category : AssetComment.discoveryCategories, dueDate:today])[0]
			generalOverDue = AssetComment.executeQuery(dueIssueQuery, issueArgs + [category: AssetComment.planningCategories, dueDate:today])[0]
			issues = AssetComment.executeQuery( publishedIssueQuery, publishedIssueArgs)[0]
		}
	}

	/* ************************************************************************************************************** */

	/**
	 * This class determines some basic totals per move event.
	 */
	class MoveEventMetrics extends PlanningDashboardMetrics {

		Map<String, List<Map>> metrics
		Map eventStartDate
		/**
		 * Iterate over the list of Move Events counting the number of assets of each type.
		 * @return a map where the key is the asset type and the value is a list of maps that have the move event id and a count.
		 */
		void calculate() {
			eventStartDate = planningDashboardData.eventStartDate
			metrics = [:].withDefault{ String key -> [] }
			moveEventList.each { MoveEvent moveEvent ->
				processMoveEvent(moveEvent)
			}

		}

		/**
		 * Get the metrics (asset count by type) for the specific event.
		 * @param moveEvent - given move event.
		 */
		private void processMoveEvent(MoveEvent moveEvent) {
			// Determine the planning bundles for the event.
			Set<MoveBundle> moveBundles = moveEvent.moveBundles?.findAll { it.useForPlanning }
			// Add the event's start date to the list.
			eventStartDate[moveEvent.id] = planningDashboardData.getEventStartDate(moveEvent)

			// Map with some basic parameters for querying assets for the event.
			Map eventWiseArgs = [project: project, moveBundles: moveBundles]

			Map<String, Map> queryInfo = [
				application: [domain: 'Application', args: eventWiseArgs],
				database: [domain: 'Database', args: eventWiseArgs],
				file: [domain: 'Files', args: eventWiseArgs],
				other: [domain: 'AssetEntity', args: eventWiseArgs + [assetClass:AssetClass.DEVICE, type:AssetType.nonOtherTypes], other: true],
				physicalAsset: [domain: 'AssetEntity', args: eventWiseArgs + [assetClass: AssetClass.DEVICE, type: AssetType.physicalServerTypes]],
				physicalStorage: [domain: 'AssetEntity', args: eventWiseArgs + [assetClass:AssetClass.DEVICE, type:AssetType.storageTypes]],
				server: [domain: 'AssetEntity', args: eventWiseArgs + [assetClass: AssetClass.DEVICE, type: AssetType.allServerTypes]],
				virtual: [domain: 'AssetEntity', args: eventWiseArgs + [assetClass: AssetClass.DEVICE, type: AssetType.virtualServerTypes]],
			]

			Map<String, Long> assetCounts = [:].withDefault {String key -> 0}

			if (moveBundles) {
				queryInfo.each { String key, Map params ->
					boolean isOther = 'other' in params ? params['other'] : false
					assetCounts[key] = getAssetCount(params['domain'], params['args'], [], isOther)
				}
			}

			queryInfo.each { String key, Map params ->
				metrics[key] << [moveEvent: moveEvent.id, count: assetCounts[key]]
			}

			Map openIssuesParams = [project: project, type: AssetCommentType.TASK, event: moveEvent,
			                        statusExcluded : AssetCommentStatus.COMPLETED ]
			String openIssuesQuery = """SELECT count(*) FROM AssetComment WHERE project=:project
				AND commentType=:type AND status <> :statusExcluded AND moveEvent=:event AND isPublished=true"""
			Long openIssueCount = AssetComment.executeQuery(openIssuesQuery, openIssuesParams)[0]

			metrics['openTasks'] << [moveEvent: moveEvent.id, count: openIssueCount]
		}
	}

	/* ************************************************************************************************************** */

	/**
	 * This metrics class calculates some dependency related numbers.
	 */
	class DependencyMetrics extends PlanningDashboardMetrics {

		long appDependenciesCount
		long pendingAppDependenciesCount
		long pendingServerDependenciesCount
		long serverDependenciesCount

		void calculate() {
			Map queryParams = [project:project, moveBundles:moveBundleList]
			List<Application> applicationsOfPlanningBundle = getAssets('Application', queryParams)
			appDependenciesCount = applicationsOfPlanningBundle ? AssetDependency.countByAssetInList(applicationsOfPlanningBundle) : 0
			pendingAppDependenciesCount = applicationsOfPlanningBundle ?  AssetDependency.countByAssetInListAndStatusInList(applicationsOfPlanningBundle,['Unknown','Questioned']) : 0
			List<AssetEntity> serversOfPlanningBundle = getAssets('AssetEntity', queryParams + [assetClass: AssetClass.DEVICE, type:AssetType.allServerTypes])
			serverDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByAssetInList(serversOfPlanningBundle) : 0
			pendingServerDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByAssetInListAndStatusInList(serversOfPlanningBundle,['Unknown','Questioned']) : 0

		}
	}

	/* ************************************************************************************************************** */

	/**
	 * This class consumes the numbers produced by other metrics objects and calculates percentages.
	 */
	class PercentageMetrics extends PlanningDashboardMetrics {
		long appToValidatePerc
		long assignedAppPerc
		long confirmedAppPerc
		long doneAppPerc
		long doneFilePerc
		long movedAppPerc
		long movedDatabasePerc
		long movedOtherPerc
		long physicalServerPerc
		long physicalStoragePerc
		long planReadyPerc
		long unassignedAppPerc
		long unassignedDatabasePerc
		long unassignedFilePerc
		long unassignedPhysicalServerPerc
		long unassignedPhyStoragePerc
		long unassignedVirtualServerPerc
		long virtualServerPerc
		long serversCompletedPercentage
		long unassignedOtherPerc

		void calculate() {

			String movedClause = "AND ae.planStatus=:planStatus"
			String deviceMovedClause = "AND ae.assetClass=:assetClass AND ae.assetType IN (:type)"
			Map queryParams = [project: project, moveBundles: moveBundleList, planStatus: AssetEntityPlanStatus.MOVED]
			Map deviceQueryParams = queryParams + [assetClass: AssetClass.DEVICE]

			Long movedPhysicalServerCount = getAssetCount('AssetEntity', deviceQueryParams + [type:AssetType.physicalServerTypes], [deviceMovedClause, movedClause])
			Long movedVirtualServerCount = getAssetCount('AssetEntity', deviceQueryParams + [type:AssetType.virtualServerTypes], [deviceMovedClause, movedClause])
			Long movedDbCount = getAssetCount('Database', queryParams, [movedClause])
			Long movedPhyStorageCount = getAssetCount('AssetEntity', deviceQueryParams + [type:AssetType.storageTypes], [deviceMovedClause, movedClause])
			Long movedOtherCount = getAssetCount('AssetEntity', deviceQueryParams + [type:AssetType.allServerTypes], [deviceMovedClause, movedClause])

			assignedAppPerc =  NumberUtil.percentage(basicMetrics.applicationCount, basicMetrics.assignedApplicationCount)
			confirmedAppPerc = NumberUtil.percentage(basicMetrics.applicationCount, basicMetrics.confirmedApplicationCount)
			movedDatabasePerc = NumberUtil.percentage(basicMetrics.databaseCount, movedDbCount)
			doneAppPerc = NumberUtil.percentage(basicMetrics.applicationCount, basicMetrics.movedApplicationCount)
			movedAppPerc = NumberUtil.percentage(basicMetrics.applicationCount, basicMetrics.movedApplicationCount)
			movedOtherPerc = NumberUtil.percentage(deviceMetrics.otherAssetCount, movedOtherCount)
			physicalServerPerc = NumberUtil.percentage(deviceMetrics.totalPhysicalServerCount, movedPhysicalServerCount)
			physicalStoragePerc = NumberUtil.percentage(deviceMetrics.phyStorageCount, movedPhyStorageCount)
			virtualServerPerc = NumberUtil.percentage(deviceMetrics.totalVirtualServerCount, movedVirtualServerCount)
			appToValidatePerc = NumberUtil.percentage(basicMetrics.applicationCount, validateMetrics.appToValidate, 100)
			planReadyPerc = NumberUtil.percentage(basicMetrics.applicationCount, validateMetrics.planReady)
			doneFilePerc = NumberUtil.percentage(basicMetrics.fileCount, validateMetrics.fileToValidate)
			unassignedAppPerc = NumberUtil.percentage(basicMetrics.applicationCount, basicMetrics.unassignedApplicationCount, 100)
			unassignedPhysicalServerPerc = NumberUtil.percentage(deviceMetrics.totalPhysicalServerCount, deviceMetrics.unassignedPhysicalServerCount, 100)
			unassignedVirtualServerPerc = NumberUtil.percentage(deviceMetrics.totalVirtualServerCount, deviceMetrics.unassignedVirtualServerCount, 100)
			unassignedDatabasePerc = NumberUtil.percentage(basicMetrics.databaseCount, basicMetrics.unassignedDatabaseCount, 100)
			unassignedPhyStoragePerc = NumberUtil.percentage(deviceMetrics.phyStorageCount, deviceMetrics.unAssignedPhyStorageCount, 100)
			unassignedFilePerc = NumberUtil.percentage(basicMetrics.fileCount, basicMetrics.unassignedFileCount, 100)
			unassignedOtherPerc = NumberUtil.percentage(deviceMetrics.otherAssetCount, deviceMetrics.unassignedOtherCount, 100)

			// Query to obtain the count of Servers in 'Moved' Plan Status
			String serversCountsQuery = """SELECT
				assetClass,
				COUNT(ae) AS all,
				SUM(CASE WHEN ae.planStatus=:movedStatus THEN 1 ELSE 0 END) AS allMoved
			FROM AssetEntity ae
			WHERE ae.project=:project
			AND ae.assetClass = :deviceAssetClass
			AND ae.assetType IN (:allServers)
			AND ae.moveBundle IN (:moveBundles)
			GROUP BY ae.assetClass"""

			Map serversCountsQueryParams = [
				project: project,
				moveBundles: moveBundleList,
				movedStatus: AssetEntityPlanStatus.MOVED,
				deviceAssetClass: AssetClass.DEVICE,
				allServers: AssetType.allServerTypes]

			def serversCountsQueryResults = AssetEntity.executeQuery(serversCountsQuery, serversCountsQueryParams)
			// Make sure this does not return null while getting [0] element.
			if (serversCountsQueryResults.size() > 0) {
				serversCountsQueryResults = serversCountsQueryResults[0]
				long totalServersCount = serversCountsQueryResults[1].intValue()
				// Make sure to prevent Division by zero error while calling countAppPercentage method.
				if (totalServersCount > 0) {
					serversCompletedPercentage = NumberUtil.percentage(totalServersCount, serversCountsQueryResults[2].intValue())
				}
			}
		}

		BasicMetrics getBasicMetrics() {
			return planningDashboardData.basicMetrics
		}

		ValidateMetrics getValidateMetrics() {
			return planningDashboardData.validateMetrics
		}

		DeviceMetrics getDeviceMetrics() {
			return planningDashboardData.deviceMetrics
		}
	}
}
