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
}
