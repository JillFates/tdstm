package net.transitionmanager.service

import com.tds.asset.Application
import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.common.security.SecurityConfigParser
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventNews
import net.transitionmanager.domain.MoveEventStaff
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyRole
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectLogo
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserLoginProjectAccess
import net.transitionmanager.security.Permission
import net.transitionmanager.user.UserContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.TransactionDefinition

/**
 * Methods to manage UserLogin domain.
 * @author jmartin
 */
class UserService implements ServiceMethods {
	private static final int DEFAULT_LOCKED_OUT_YEARS = 100 * 365

	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProjectService projectService
	TaskService taskService
	UserPreferenceService userPreferenceService
	AuditService auditService
	MetricReportingService metricReportingService

	/**
	 * Used to find a user or provision the user based on the settings in the configuration file
	 * which is used by the AD and SSO integration.
	 *
	 * @param userInfo - the information provided by the login authentication session
	 * @param config - a map of the authentication connector
	 * @return a the userLogin account that was found or provisioned
	 * @throws ConfigurationException, RuntimeException
	 */
	@Transactional
	UserLogin findOrProvisionUser(Map userInfo, Map config, String authority ) {

		//if (!initialized)
		//	initialize()

		// The first step is to attempt to find the user/person based on the following search patterns:
		//    1. The UserLogin.externalGuid code matches the person's AD objectguid property
		//    2. The UserLogin.username matches the person's AD UserLogin + '@' + company domain
		//    3. The Person.firstName+lastName match the person's AD givenname + sn properties

		def persons
		def project

		String mn = 'findOrProvisionUser:'
		String em

		Map domain = config.domains[authority]

		boolean debug = log.debugEnabled || config.debug

		// If the user email was blank then set it based on the username + the company domain in the config
		if (!userInfo.email) {
			userInfo.email = userInfo.username.contains('@') ? userInfo.username : userInfo.username + '@' + domain.fqdn
		}

		// TM-7169 Sometimes the name will come in the fullName element which then needs to be parsed apart
		if (! userInfo.firstName && ! userInfo.lastName && userInfo.fullName) {
			Map mappedName = personService.parseName(userInfo.fullName)
			userInfo.firstName = mappedName.first
			userInfo.lastName = mappedName.last
		}

		def personIdentifier = "$userInfo.firstName $userInfo.lastName${userInfo.email ? ' <'+userInfo.email+'>' : ''}"
		log.debug "$mn Attempting to find or provision $personIdentifier"

		boolean autoProvision = domain.autoProvision
		Long defaultProject = domain.defaultProject
		String defaultRole = domain.defaultRole ?: ''
		String defaultTimezone = domain.defaultTimezone ?: ''

		PartyGroup company = PartyGroup.get(userInfo.companyId)
		if (!company) {
			log.error "$mn Unable to find configured company for id $userInfo.companyId"
			throw new ConfigurationException("Unable to find user's company by id (${userInfo.companyId})")
		}

		project = Project.get(defaultProject)
		if (!project) {
			log.error "$mn Unable to find configured default project for id $defaultProject"
			throw new ConfigurationException("Unable to find the default project for $company")
		}

		if (! projectService.companyIsAssociated(project, company) ) {
			em = "The configured default project is not associated with the company in security settings"
			log.error "$mn $me : project=${project.id}, company=${company.id}"
			throw new ConfigurationException(em)
		}

		// Attempt to lookup the Person and their UserLogin
		def (Person person, UserLogin userLogin) = findPersonAndUserLogin(company, userInfo, config, authority, personIdentifier)

		// Check various requirements based on the Authentication Configuration and fall out if we don't
		// have an account and not allowed to create one.
		if (!userLogin && !userInfo.roles && !domain.defaultRole) {
			////// TODO BB throw new AccountException('No roles defined for the user')
			throw new RuntimeException('No security roles have been assigned to your user account')
		}
		if (!userLogin && !person && !autoProvision) {
			////// TODO BB throw new AccountException('UserLogin and/or Person not found and autoProvision is disabled')
			throw new RuntimeException('UserLogin and/or Person not found and autoProvision is disabled')
		}
		if ((!person || !userLogin) && !autoProvision ) {
			log.warn "findOrProvisionUser: User attempted login but autoProvision is diabled ($personIdentifier)"
			throw new RuntimeException('Auto provisioning is disabled')
		}

		Map nameMap = userInfoToNameMap(userInfo)

		if (!person) {

			// Create the person and associate to the company & project if one wasn't found

			log.debug "$mn Creating new person"

			person = new Person(
				firstName:nameMap.first,
				lastName:nameMap.last,
				middleName: nameMap.middle,
				email: userInfo.email,
				workPhone: userInfo.telephone,
				mobilePhone: userInfo.mobile
			)
			if (!person.save(flush:true)) {
				log.error "$mn Creating user ($personIdentifier) failed due to ${GormUtil.allErrorsString(person)}"
				throw new RuntimeException('Data error while creating Person object')
			}
			// Create Staff relationship with the Company
			if (!partyRelationshipService.addCompanyStaff(company, person)) {
				throw new RuntimeException('Unable to associate new person to company')
			}
			// Create Staff relationship with the default Project
			if (!partyRelationshipService.addProjectStaff(project, person)) {
				throw new RuntimeException('Unable to associate new person to project')
			}
		} else {

			// Update the person information if the configuration is set for it
			if (domain.updateUserInfo) {
				GormUtil.overridePropertyValueIfSet(person, 'firstName', nameMap.first)
				GormUtil.overridePropertyValueIfSet(person, 'middleName', nameMap.middle)
				GormUtil.overridePropertyValueIfSet(person, 'lastName', nameMap.last)
				GormUtil.overridePropertyValueIfSet(person, 'email', userInfo.email)
				GormUtil.overridePropertyValueIfSet(person, 'workPhone', userInfo.telephone)
				GormUtil.overridePropertyValueIfSet(person, 'mobilePhone', userInfo.mobile)

				if (person.isDirty()) {
					log.debug "$mn Updating existing person record : $person.dirtyPropertyNames"
					if (!person.save(flush:true)) {
						log.error "$mn Failed updating Person ($person) due to ${GormUtil.allErrorsString(person)}"
						throw new DomainUpdateException("Unexpected error while updating Person object")
					}
				}
			}
		}

		boolean createUser = userLogin == null
		if (createUser) {
			log.debug "$mn Creating new UserLogin"
			userLogin = new UserLogin(person:person )
			// will set all the properties below
		}

		String guid = formatGuid(userInfo)

		// Set any of the variables that need to be set regardless of configuration settings or if it is new/existing
		if (!userLogin.externalGuid && userInfo.guid) {
			userLogin.externalGuid = guid 	// Set to the formulated one with company id
		}
		if (userLogin.isLocal) {
			userLogin.isLocal = false
		}
		if (createUser) {
			userLogin.active = 'Y'
		}
		if (userLogin.forcePasswordChange == 'Y') {
			userLogin.forcePasswordChange = 'N'
		}
		if (userLogin.username?.toLowerCase() != userInfo.username.toLowerCase()) {
			userLogin.username = userInfo.username
		}
		if (userLogin.password != userInfo.guid) {
			userLogin.password = userInfo.guid
		}

		def expiryDate = new Date() + 1
		if (userLogin.expiryDate < expiryDate) {
			userLogin.expiryDate = expiryDate
		}

		if (createUser || userLogin.isDirty()) {
			log.debug "findOrProvisionUser: Persisting UserLogin : $userLogin.dirtyPropertyNames"
			if (!userLogin.save(flush:true)) {
				def action = createUser ? 'creating' : 'updating'
				log.error "findOrProvisionUser: Failed $action UserLogin failed due to ${GormUtil.allErrorsString(userLogin)}"
				throw new RuntimeException("Unexpected error while $action UserLogin object")
			}
		}

		// Setup what the user roles should be based on the configuration and what came back from the userInfo
		List newUserRoles = userInfo.roles.size() ? userInfo.roles : []
		if (defaultRole) {
			if (!newUserRoles.contains(defaultRole)) {
				newUserRoles << defaultRole
			}
		}

		if (createUser) {
			// Add their role(s)
			newUserRoles.each { r ->
				log.info "$mn Adding new security role '$r' to $personIdentifier"
				def rt = RoleType.read(r.toUpperCase())
				if (!rt || !rt.isSecurityRole()) {
					throw new ConfigurationException("Configuration security property domains.${authority}.roleMap[$r] is an invalid code")
				}
				def pr = new PartyRole(party: person, roleType: rt)
				if (!pr.save(flush:true)) {
					log.error "$mn Unable to add new role for person ${GormUtil.allErrorsString(userLogin)}"
					throw new RuntimeException("Unexpected error while assigning security role")
				}
			}
		} else {
			// See about updating their roles
			if (domain.updateRoles) {

				List roleTypeCodes = SecurityConfigParser.getDomainRoleTypeCodes(config, authority)
				List roleTypes = RoleType.getAll(roleTypeCodes).findAll()

				// Get the user roles from DB and compare
				def existingRoles = PartyRole.findAllByPartyAndRoleTypeInList(person, roleTypes)

				log.debug "$mn User has existing roles $existingRoles"
				// See if there are any existing that should be removed
				existingRoles.each { er ->
					if (!newUserRoles*.toUpperCase().contains(er.roleType.id)) {
						log.debug "$mn Deleting security role $er.roleType.id for $personIdentifier"
						er.delete(flush:true)
					}
				}

				// Now go through and add any new roles that aren't in the user's existing list
				newUserRoles.each { nr ->
					// Force the Role Types to uppercase
					nr = nr.toUpperCase()

					if (!existingRoles.find { it.roleType.id == nr }) {
						log.debug "$mn Assigning new security role $nr for $personIdentifier"

						def pr = new PartyRole(party: person, roleType: RoleType.load(nr))
						if (!pr.save(flush:true)) {
							log.error "$mn Unable to update new role for person ${GormUtil.allErrorsString(userLogin)}"
							throw new RuntimeException("Unexpected error while assigning security role")
						}
					}
				}
			}
		}

		//
		// Now setup their preferences if there is no or that they are a new user
		//
		def pref = createUser ? null : userPreferenceService.getCurrentProjectId(userLogin)
		if (createUser || !pref) {
			// Set their default project preference
			userPreferenceService.setCurrentProjectId(userLogin, project.id)
			log.debug "$mn set default project preference to $project"
		}
		if (defaultTimezone) {
			// Set their TZ preference
			if (createUser) {
				userPreferenceService.setTimeZone(userLogin, defaultTimezone)
				log.debug "$mn: set timezone preference $defaultTimezone"
			}
			else {
				userPreferenceService.getTimeZone(userLogin, defaultTimezone)
			}
		}

		log.debug "$mn FINISHED UserLogin(id:$userLogin.id, $userLogin), Person(id:$person.id, $person)"

		return userLogin
	}

	/**
	 * Used by the login logic to parse the name from UserInfo
	 */
	Map userInfoToNameMap(Map userInfo) {
		// Parse the person's name and populate a name map
		Map nameMap = [first:'', middle:'', last:userInfo.lastName]
		List names = userInfo.firstName.split(' ')
		if (names.size()) {
			nameMap.first = names[0]
			if (names.size() > 1) {
				nameMap.middle = names[1..-1].join(' ')
			}
		}
		return nameMap
	}

	String formatGuid(Map userInfo) {
		return "$userInfo.companyId-$userInfo.guid"
	}

	/**
	 * Used to look for a Person and optionally their user login account using information supplied from Authentication Realm and
	 * the authority configuration map.
	 *
	 * This assumes that a person account may previously exist but that the UserLogin may not
	 *
	 * @param userInfo - the information provided by the login authentication session
	 * @param config - a map of the authentication connector
	 * @return a with [Person, UserLogin] account that was found or provisioned
	 * @throws ConfigurationException, RuntimeException
	 */
	List findPersonAndUserLogin(PartyGroup company, Map userInfo, Map config, String authority, String personIdentifier) {

		// The first step is to attempt to find the user/person based on the following search patterns:
		//    1. The UserLogin.externalGuid code matches the person's AD objectguid property
		//    2. The UserLogin.username matches the person's AD UserLogin + '@' + company domain
		//    3. The Person.firstName+lastName match the person's AD givenname + sn properties

		Person person
		List persons
		UserLogin userLogin

		// method name
		String mn = 'findPersonAndUserLogin:'

		// Flag for debugging
		boolean debug = log.debugEnabled || config.debug
		Map domain = config.domains[authority.toLowerCase()]

		Map nameMap = userInfoToNameMap(userInfo)
		log.debug "$mn Parsed [$userInfo.firstName : $userInfo.lastName] into $nameMap"

		// Make the GUID unique to the companyId + the GUID from their authority system
		String guid = formatGuid(userInfo)
		userLogin = UserLogin.findByExternalGuid(guid)
		if (userLogin) {
			log.debug "$mn Found user by GUID"

			// Check to verify that the user found is assoicated to the company
			// TODO - implement this validation
			// if (userLogin.person...)

		} else {

			// User wasn't found so let's try by the Person information
			// Try to find the Person in case they were previously loaded or provisioned other than by the login

			// First try to find by their email
			if (userInfo.email) {
				log.debug "$mn Looking up person by email"
				persons = personService.findByCompanyAndEmail(company, userInfo.email)
			}

			// Then try to find by their name
			if (persons.size() == 0) {
				log.debug "$mn Looking up person by name"

				persons = personService.findByCompanyAndName(company, nameMap)
				if (debug)
					log.debug "$mn personService.findByCompanyAndName found ${persons?.size()} people"
			}

			// If we have any persons, try to find their respective user accounts
			if (persons.size() > 0) {
				def users = UserLogin.findAllByPersonInList(persons)
				log.debug "$mn Found these users: $users"

				// If we find more than one account we don't know which to use
				int size = users.size()
				if (size > 1) {
					log.error "$mn found $size users that matched $personIdentifier"
					throw new RuntimeException('Unable to resolve UserLogin account due to multiple matches')
				}

				if (size == 1) {
					userLogin = users[0]
				}
				else {
					size = persons.size()
					if (size > 1) {
						log.error "$mn found $size Person accounts that matched $personIdentifier"
						throw new RuntimeException('Unable to resolve Person account due to multiple matches')
					}

					if (size == 1) {
						// Assign the person
						person = persons[0]
					}
				}
			}
		}

		if (userLogin) {
			person = userLogin.person
		}

		return [person, userLogin]
	}

	def getEventDetails(Project project) {
		def projects = getSelectedProject(project)
		def upcomingEvents=[:]
		def dateNow = TimeUtil.nowGMT()

		getEvents(projects, dateNow).each { event, startTime->
			def teams = MoveEventStaff.findAllByMoveEventAndPerson(event, securityService.loadCurrentPerson()).role
			if (teams){
				upcomingEvents[event.id] = [moveEvent: event,
                    teams: WebUtil.listAsMultiValueString(teams.collect { team-> team.toString() }),
                    daysToGo: startTime > dateNow ? startTime - dateNow : (" + " + (dateNow - startTime))
				]
			}
		}

		return upcomingEvents
	}

	private List<Project> getSelectedProject(Project project) {
		if (project == Project.ALL) {
			projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE)
		}
		else {
			[project]
		}
	}

	/**
	 * Gets a list of events for one or more projects
	 * @param projects - a list of one or more projects
	 * @param dateNow - the date to base active events (guessing here because it isn't making a lot of sense)
	 * @return
	 */
	def getEvents(projects, dateNow){
		def moveEventList = MoveEvent.findAllByProjectInList(projects).sort{it.eventTimes.start}
		def thirtyDaysInMS = 2592000000

		def events = [:]

		moveEventList.each { event ->
			def eventCompTimes = event.moveBundles.completionTime.sort().reverse()
			def startTimes = event.moveBundles.startTime.sort()
			startTimes.removeAll([null])
			def startTime = startTimes[0]
			def completionTime = eventCompTimes[0]
			if(completionTime && completionTime > dateNow.minus(30)){
				events[event] = startTime
			}
		}

		return events
	}

	List getEventNews(Project project) {
		List comingEvents = getEvents(getSelectedProject(project), TimeUtil.nowGMT()).keySet().asList()
		// log.debug "getEventNews() comingEvents=$comingEvents"
		if (comingEvents) {
			MoveEventNews.executeQuery(
				'from MoveEventNews where moveEvent in (:events) and isArchived=:isArchived order by dateCreated desc',
				[events: comingEvents, isArchived: 0])
		}
		else {
			[]
		}
	}

	def getTaskSummary(Project project) {
		def timeInMin = 0
		def issueList = []

		getSelectedProject(project).each { proj ->
			def tasks = taskService.getUserTasks(proj, false, 7, 'score')
			def taskList = tasks['user']
			def durationScale = [D:1440, M:1, W:10080, H:60] // minutes per day,week,hour
			taskList.each { task ->
				def css = taskService.getCssClassForStatus(task.status)
				issueList << [item: task, css: css, projectName: proj.name]
			}
			if (taskList) {
				timeInMin += taskList.sum { task -> task.duration ? task.duration * durationScale[task?.durationScale] : 0 }
			}
		}

		if (project == Project.ALL) {
			issueList = issueList.sort { it.item.score }
		}
		def dueTaskCount = issueList.item.findAll { it.duedate && it.duedate < TimeUtil.nowGMT() }.size()

		return [taskList: issueList, totalDuration: TimeUtil.createProperDuration(0,0,timeInMin,0),
		        dueTaskCount:dueTaskCount, personId: securityService.currentPersonId]
	}

	def getApplications(Project project) {

		//to get applications assigned to particular person & there relations.
		def appList = Application.executeQuery('''
			from Application
			where project in (:projects)
			  and (sme=:person or sme2=:person or appOwner=:person)
			order by assetName asc
			''', [projects: getSelectedProject(project), person: securityService.loadCurrentPerson()])

		def relationList = [:]
		long currentUserId = securityService.currentUserLoginId
		appList.each {
			def relation = []
			if (it.sme?.id == currentUserId) {
				relation << 'sme'
			}
			if (it.sme2?.id == currentUserId) {
				relation << 'sme2'
			}
			if (it.appOwner?.id == currentUserId) {
				relation << 'appOwner'
			}
			relationList[it.id] = WebUtil.listAsMultiValueString(relation)
		}

		return [appList: appList, relationList: relationList]
	}

	/**
	 * Used in the dashboard to show which person are active
	 */
	def getActivePeople(Project project) {

		//to get active people of that particular user selected project.
		def recentLogin = []
		def projectIds = [0L]
		projectIds.addAll getSelectedProject(project)*.id
		def projectIdsAsValue = projectIds.join(",")

		def query = new StringBuilder("SELECT p.person_id, ul.user_login_id, p.first_name, p.last_name, p.middle_name, proj.project_id, pg.name AS project_name, ul.last_page AS lastPage")
			.append(" FROM (")
			.append("     SELECT user_login_id, CAST(value AS UNSIGNED INTEGER) AS project_id")
			.append("     FROM user_preference")
			.append("     WHERE preference_code = 'CURR_PROJ' AND value IS NOT NULL)")
			.append(" AS ul_project")
			.append(" INNER JOIN user_login ul ON ul.user_login_id = ul_project.user_login_id")
			.append(" INNER JOIN person p ON p.person_id = ul.person_id")
			.append(" INNER JOIN project proj ON proj.project_id = ul_project.project_id")
			.append(" INNER JOIN party_group pg ON proj.project_id = pg.party_group_id")
			.append(" WHERE ul.last_page > (now() - INTERVAL 30 MINUTE) AND ul_project.project_id IN ($projectIdsAsValue)")

		def users = jdbcTemplate.queryForList(query.toString())
		def personName

		if (users) {
			users.each { r ->
				personName = r.first_name + (r.middle_name ? ' ' + r.middle_name : '') + (r.last_name ? " " + r.last_name : '')
				recentLogin << [personId: r.person_id, projectName: r.project_name,
				                personName: personName, lastActivity: TimeUtil.formatDateTime(r.lastPage)]
			}
		}

		return recentLogin
	}

	/**
	 * Update the User to log the their last login
	 */
	@Transactional
	void updateLastLogin(UserLogin userLogin) {
		// <SL> is this session attribute bein used somehow?
		Person person = userLogin.person
		session.setAttribute 'LOGIN_PERSON', [name: person.firstName, id: person.id]

		def now = TimeUtil.nowGMT()
		userLogin.lastLogin = now
		userLogin.lastPage = now
		userLogin.save()
	}

	/**
	 * Update the User's last page load time. This needs to be done in a separate transaction so that it doesn't
	 * potentially get rolled back with the whole page. It can fail due to Optimistic Locking which we'll just ignore.
	 */
	void updateLastPageLoad() {
		try {
			UserLogin.withTransaction ([propagationBehavior: TransactionDefinition.PROPAGATION_REQUIRES_NEW]) { status ->
				UserLogin userLogin = securityService.userLogin
				def now = TimeUtil.nowGMT()
				if (userLogin) {
					// Only update if the page request is more than 5 seconds to avoid the multi-ajax requests within the same page
					if ((now.getTime() - userLogin.lastPage.getTime()) > 5000) {
						userLogin.lastPage = now
						userLogin.save(flush:true)
					}
				} else {
					log.error "updateLastPageLoad() was unable to find user"
				}
			}
		} catch (e) {
			log.warn "updateLastPageLoad() failed - $e.message"
		}
	}

	/**
	 * Used to retrieve a list of users with recent activity
	 * @param inPastMinutes - the number of minutes since latest activity (default 5)
	 * @return A list of the username
	 * TODO : JPM 3/2016 : Change to use new permission that is more applicable
	 */
	List usernamesWithRecentActivity(int inPastMinutes=5) {
		String query = "SELECT username FROM user_login WHERE last_page > (NOW() - INTERVAL $inPastMinutes MINUTE) ORDER BY username"
		List users = jdbcTemplate.queryForList(query)
		// log.debug "usersWithRecentActivity() users=$users"
		return users*.username
	}

	/**
	 * Updates the following User Preferences:
	 * - Current Project
	 * - Move Event
	 * - Current Bundle
	 * - Current Room
	 *
	 * @param projectId - the id of the project to be assigned to the user.
	 *
	 * @return true: context updated / false: unable to update.
	 */
	@Transactional
	boolean changeProjectContext(long projectId){
		if (!projectService.hasAccessToProject(projectId)) {
			securityService.reportViolation("Attempted to access unavailable project $projectId")
			return false
		}

		userPreferenceService.setCurrentProjectId(projectId)
		// create a new UserLoginProjectAccess to account later for user logins on metric recollection
		createUserLoginProjectAccess(securityService.getUserLogin())
		true
	}

	/**
	 * Reset user account failed login attempts
	 * @param userLogin
	 */
	@Transactional
	void resetFailedLoginAttempts(UserLogin userLogin) {
		userLogin.failedLoginAttempts = 0
		userLogin.save()
	}

	/**
	 * Set user account locked out date
	 * @param userLogin
	 * @param date
	 */
	@Transactional
	void setLockedOutUntil(UserLogin userLogin, Date date) {
		userLogin.lockedOutUntil = date
		userLogin.save()
	}

	/**
	 * Lockout user account
	 * @param userLogin
	 */
	void lockoutAccountByInactivityPeriod(UserLogin userLogin) {
		Date lockedOutUntil = TimeUtil.nowGMT() + DEFAULT_LOCKED_OUT_YEARS
		auditService.saveUserAudit UserAuditBuilder.userAccountWasLockedOutDueToInactivity(userLogin)
		setLockedOutUntil(userLogin, lockedOutUntil)
		updateLastLogin(userLogin)
		securityService.logoutCurrentUser()
	}

	/**
	 * Creates a new record of a UserLoginProjectAccess, if it does not exist already.
	 * If there is another record for the same UserLogin and same Project in the same day,
	 * do nothing. Otherwise create a new record for this day.
	 * This entities will be used to collect metrics of logged users.
	 * See TM-10119
	 * @param userLogin  The UserLogin that will be associated to the new UserLoginProjectAccess
	 */
	boolean createUserLoginProjectAccess(UserLogin userLogin) {
		Date date = TimeUtil.nowGMT().clearTime()
		Project project = userLogin.getCurrentProject()
		// Make sure that is unique
		int count = UserLoginProjectAccess.where {
			userLogin == userLogin
			project == project
			date == date
		}.count()
		if (count == 0 && project) {
			UserLoginProjectAccess userLoginProjectAccess = new UserLoginProjectAccess(
					userLogin: userLogin,
					project: project,
					date: date
			)
			userLoginProjectAccess.save(failOnError: true)
		}
	}

	/**
	 * Populate a UserContext object with the following information:
	 * - User Login
	 * - Person
	 * - Project
	 * - MoveEvent
	 * - MoveBundle
	 * - Timezone
	 * - Date Format
	 * @return a UserContext instance that gathers the user's most relevant information.
	 */
	UserContext getUserContext() {
		Project project = securityService.getUserCurrentProject()
		UserLogin userLogin = securityService.getUserLogin()
		Person person = securityService.loadCurrentPerson()
		MoveEvent moveEvent
		String eventPref = userPreferenceService.getMoveEventId(userLogin)
		if (eventPref) {
			moveEvent = GormUtil.findInProject(project, MoveEvent, NumberUtil.toPositiveLong(eventPref), true)
		}
		MoveBundle moveBundle
		String bundlePref = userPreferenceService.getMoveBundleId()
		if (bundlePref) {
			moveBundle = GormUtil.findInProject(project, MoveBundle, NumberUtil.toPositiveLong(bundlePref), true)
		}

		String timezone = userPreferenceService.getTimeZone(userLogin, TimeUtil.defaultTimeZone)
		String dateFormat = userPreferenceService.getDateFormat(userLogin)
		if (!dateFormat) {
			dateFormat = TimeUtil.defaultFormatType
		}
		Map contextParams = [
		    person: person,
			userLogin: userLogin,
			project: project,
			moveEvent: moveEvent,
			moveBundle: moveBundle,
			timezone: timezone,
			dateFormat: dateFormat,
			logoUrl: projectService.getProjectLogoUrl(project)
		]
		return new UserContext(contextParams)
	}
}
