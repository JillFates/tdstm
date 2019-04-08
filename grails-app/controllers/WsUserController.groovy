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
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.StartPageEnum as STARTPAGE
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.user.SavePreferenceCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.Timezone
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.UserService

import java.text.DateFormat

/**
 * Handles WS calls of the UserService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
class WsUserController implements ControllerMethods {

	UserPreferenceService userPreferenceService
	PersonService personService
	PartyRelationshipService partyRelationshipService
	MoveEventService moveEventService
	CustomDomainService customDomainService
	UserService userService
	ProjectService projectService

	/**
	 * Access a list of one or more user preferences
	 * @param id - a comma separated list of the preference(s) to be retrieved
	 * @example GET ./ws/user/preferences/EVENT,BUNDLE
	 * @return a MAP of the parameters (e.g. preferences:[EVENT:5, BUNDLE:30])
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def preferences(String id) {
        UserLogin userLogin = currentPerson().userLogin
        Map preferences = userPreferenceService.getPreferences(userLogin, id)
    	renderSuccessJson(preferences: preferences)
	}

    /**
     * Used by the User Preference Edit dialog. This will return a List<Map> where the map will
     * consist of the following:
     *    code - the Preference Code
     *    label - the human readable name of the code
     *    value - the value of the preference. Note that references will get substituted (e.g. CURR_PROJ returns the name)
     * @return Success Structure with preferences property containing List<Map>
     */
    def modelForPreferenceManager() {
        Person person = currentPerson()
        UserLogin userLogin = person.userLogin

        Map model = [
            fixedPreferenceCodes: userPreferenceService.FIXED_PREFERENCE_CODES,
            person: person,
            preferences: userPreferenceService.preferenceListForEdit(userLogin)
        ]
    	renderSuccessJson(model)
    }

	/**
	 * Used by the Manage Staff dialog.
	 * @param: id - ID of the person to be requested
	 * This will return the following:
	 *    person - All the info associated with the person requested
	 *    availableTeams - All teams that this person can be assigned to
	 * @return Success Structure with preferences property containing List<Map>
	 */
	def modelForStaffViewEdit(String id) {
		Project project = getProjectForWs()

		Person person = Person.get(Long.valueOf(id))
		List<RoleType> teams = partyRelationshipService.getTeamRoleTypes()

		renderSuccessJson(person: person.toMap(project), availableTeams: teams)
	}

	/**
	 * Used by the User Dashboard
	 * @param id - ID of the user requested
	 * @return Success structure with person, projects, project instance, and moveday categories
	 */
	def modelForUserDashboard(String id) {
		Project project
		Person person = currentPerson()

		if(id && id != "undefined") {
			project = Project.findById(id.toLong())
			userPreferenceService.setCurrentProjectId(project.id)
		} else {
			project = getProjectForWs()
		}

		List projects = [project]
		List userProjects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE)
		if (userProjects) {
			projects = (userProjects + project).unique()
		}

		renderSuccessJson(
				person: person,
				projects: projects,
				projectInstance: project,
				movedayCategories: AssetComment.moveDayCategories
		)
	}

	/**
	 * Get events assigned to current user in the current project
	 * @return list of events
	 */
	def getAssignedEvents() {
		Project project = getProjectForWs()
		List events = userService.getEventDetails(project).values().collect { value -> [
				eventId: value.moveEvent.id, projectName: value.moveEvent.project.name,
				name: value.moveEvent.name, startDate: moveEventService.getEventTimes(value.moveEvent.id).start,
				days: value.daysToGo + ' days', teams: value.teams]
		}
		renderSuccessJson(events: events)
	}

	/**
	 * Get the event news assigned to the current user in the current project
	 * @return list of event news
	 */
	def getAssignedEventNews() {
		Project project = getProjectForWs()
		List eventNews = []
		DateFormat formatter = TimeUtil.createFormatter("MM/dd/yyyy hh:mm a")
		if (project) {
			userService.getEventNews(project).each { news ->
				eventNews << [eventId: news.moveEvent.id,
							  projectName: news.moveEvent.project.name,
							  date: TimeUtil.formatDateTimeWithTZ(TimeUtil.defaultTimeZone, (news.dateCreated != null? news.dateCreated : new Date()), formatter),
							  event: news.moveEvent.name,
							  news: news.message]
			}
		}
		renderSuccessJson(eventNews: eventNews)
	}

	/**
	 * Get the tasks assigned to the current user in the current project and a summary string for the user dashboard
	 * @return list of tasks and summary string
	 */
	def getAssignedTasks() {
		Project project = getProjectForWs()
		List taskList = []
		Map taskSummary = userService.getTaskSummary(project)
		DateFormat formatter = TimeUtil.createFormatter("MM/dd kk:mm")

		taskSummary.taskList.each { task ->
			task.item.estFinish?.clearTime()
			taskList.add([
					projectName: task.projectName,
					taskId: task.item.id,
					task: ((task.item.taskNumber)? task.item.taskNumber + " - " : "") + task.item.comment,
					css: task.css,
					overDue: (task.item.dueDate && task.item.dueDate < TimeUtil.nowGMT()? 'task_overdue' : ''),
					assetClass: task.item.assetClass.toString(),
					assetId: task.item.assetId,
					related: task.item.assetName,
					dueEstFinish: TimeUtil.formatDateTimeWithTZ(TimeUtil.defaultTimeZone,
							(task.item.estFinish != null? task.item.estFinish : new Date()), formatter),
					status: task.item.status,
					successors: task.item.successors,
					predacessors: task.item.predecessors,
					instructionsLink: task.item.instructionsLink,
                    category: task.item.category
			])
		}

		def dueTaskCount = taskSummary.dueTaskCount

		def summaryDetail = 'No active tasks were found.'
		if (taskSummary.taskList.size() > 0) {
			summaryDetail = "${taskSummary.taskList.size()} assigned tasks (${dueTaskCount} ${(dueTaskCount == 1) ? ('is') : ('are')} overdue)"
		}
		renderSuccessJson(tasks: taskList, summaryDetail: summaryDetail)
	}

	/**
	 * Get the applications assigned to the current user in the current project
	 * @return list of applications
	 */
	def getAssignedApplications() {
		Project project = getProjectForWs()
		Map<String, Object> appSummary = userService.getApplications(project)

		def appList = appSummary.appList.collect { Application app -> [
				projectName: app.project.name,
				name: app.assetName,
				appId: app.id,
				assetClass: app.assetClass.toString(),
				planStatus: app.planStatus,
				moveBundle: app.moveBundle.name,
				relation: appSummary.relationList[app.id]
		] }
		renderSuccessJson(applications: appList)
	}

	/**
	 * Get the active people assigned to the current project
	 * @return list of people
	 */
	def getAssignedPeople() {
		Project project = getProjectForWs()

		ArrayList activePeople = userService.getActivePeople(project).collect { login ->
			[personId: login.personId, projectName: login.projectName,
			 personName: login.personName, lastActivity: login.lastActivity]
		}
		renderSuccessJson(activePeople: activePeople)
	}

    /**
     * Used to reset all preferences of a user
     */
    def resetPreferences() {
        userPreferenceService.resetPreferences()
        renderSuccessJson()
    }

    @HasPermission(Permission.UserGeneralAccess)
    def getStartPageOptions() {
        def pageList = [STARTPAGE.PROJECT_SETTINGS.value,
            STARTPAGE.PLANNING_DASHBOARD.value,
            STARTPAGE.ADMIN_PORTAL.value,
            STARTPAGE.USER_DASHBOARD.value]

        renderSuccessJson(pages: pageList)
    }

	/**
	* Sets a user preference through an AJAX call
	* @param code - the preference code for the preference that is being set
	* @param value - the value to set the preference to
	*/
	@HasPermission(Permission.UserGeneralAccess)
	def savePreference(SavePreferenceCommand savePreference) {
		validateCommandObject(savePreference)
		userPreferenceService.setPreference(savePreference.code, savePreference.value)
		renderSuccessJson()
	}

	@HasPermission(Permission.UserGeneralAccess)
	def getUser() {
		UserLogin userLogin = securityService.getUserLogin()
		renderSuccessJson([id: userLogin.id, username: userLogin.username])
	}

	@HasPermission(Permission.UserGeneralAccess)
	def getMapAreas() {
		renderSuccessJson(userPreferenceService.timezonePickerAreas())
	}

	@HasPermission(Permission.UserGeneralAccess)
	def getTimezones() {
		renderSuccessJson(Timezone.findAll())
	}

	@HasPermission(Permission.UserGeneralAccess)
	def getPerson() {
		Person person = securityService.getUserLogin().person
		renderSuccessJson([person:person])
	}

	@HasPermission(Permission.UserGeneralAccess)
	def removePreference(String id) {
		userPreferenceService.removePreference(id)
		renderSuccessJson()
	}

	/**
	 * Update the person account that is invoked by the user himself
	 * @param  : map of settings for the person
	 * @return : the person that has been updated
	 */
	@HasPermission(Permission.UserUpdateOwnAccount)
	def updateAccount() {
		Map settings = request.JSON
        Person person = personService.updatePerson(settings, false)
		Map preferences = [
				START_PAGE     : settings.startPage,
				CURR_POWER_TYPE: settings.powerType
		]
		userPreferenceService.setPreferences(null, preferences)
		renderSuccessJson(person)
    }

    /**
     * Update the person account that is invoked by the admin
     * @param  : map of settings for the person
     * @return : the person that has been updated
     */
	@HasPermission(Permission.ProjectStaffEdit)
    def updateAccountAdmin() {
        Map settings = request.JSON
        Person person = personService.updatePerson(settings, true)
        renderSuccessJson(person)
    }

	@HasPermission(Permission.UserGeneralAccess)
	def saveDateAndTimePreferences() {
		Map requestParams = request.JSON
		// Checks that timezone is valid
		def timezone = TimeZone.getTimeZone(requestParams?.timezone.toString())
		userPreferenceService.setTimeZone timezone.getID()

		// Validate date time format
		def datetimeFormat = TimeUtil.getDateTimeFormatType(requestParams?.datetimeFormat.toString())
		userPreferenceService.setDateFormat datetimeFormat

		renderSuccessJson(timezone: timezone.getID(), datetimeFormat: datetimeFormat)
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
		time = TimeUtil.formatDateTime(date, TimeUtil.FORMAT_DATE_TIME_8)

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
				phyServerDonePerc: percentagePhysicalServerCount,
				unassignedVirtServerCount: unassignedVirtualServerCount,
				virtServerDonePerc: percVirtualServerCount,
				unassignedDbCount: unassignedDbCount,
				dbDonePercentage: percentageDBCount,
				unassignedPhyStorageCount: unAssignedPhyStorageCount,
				phyStorageDonePerc: percentagePhyStorageCount,
				unassignedFilesCount: unassignedFilesCount,
				filesDonePerc: percentageFilesCount,
				unassignedOtherCount: unassignedOtherCount,
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
	} // end-of-planningStats
}
