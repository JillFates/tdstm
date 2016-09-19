import com.tds.asset.Application
import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.common.security.SecurityConfigParser
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import grails.transaction.Transactional
import org.apache.shiro.authc.AccountException
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.SessionFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.TransactionDefinition
import UserPreferenceEnum as PREF

/**
 * Methods to manage UserLogin domain.
 * @author jmartin
 */
class UserService {

	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProjectService projectService
	SecurityService securityService
	SessionFactory sessionFactory
	TaskService taskService
	UserPreferenceService userPreferenceService

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
	UserLogin findOrProvisionUser( Map userInfo, Map config, String authority ) {

		//if (!initialized)
		//	initialize()

		// The first step is to attempt to find the user/person based on the following search patterns:
		//    1. The UserLogin.externalGuid code matches the person's AD objectguid property
		//    2. The UserLogin.username matches the person's AD UserLogin + '@' + company domain
		//    3. The Person.firstName+lastName match the person's AD givenname + sn properties

		def persons
		def project

		String mn = 'findOrProvisionUser:'

		Map domain = config.domains[authority.toLowerCase()]

		boolean debug = log.isDebugEnabled() || config.debug

		// If the user email was blank then set it based on the username + the company domain in the config
		if (! userInfo.email) {
			userInfo.email = userInfo.username.contains('@') ? userInfo.username : "${userInfo.username}@${domain.fqdn}"
		}

		def personIdentifier = "${userInfo.firstName} ${userInfo.lastName}${(userInfo.email ? ' <'+userInfo.email+'>' : '')}"
		if (debug)
			log.debug "$mn Attempting to find or provision $personIdentifier"

		boolean autoProvision = domain.autoProvision
		Long defaultProject = domain.defaultProject
		String defaultRole = domain.defaultRole ?: ''
		String defaultTimezone = domain.defaultTimezone ?: ''

		def client = PartyGroup.get( userInfo.companyId )
		if (! client) {
			log.error "$mn Unable to find configured company for id ${userInfo.companyId}"
			throw new ConfigurationException("Unable to find user's company by id ${userInfo.companyId}")
		}

		project = Project.get(defaultProject)
		if (! project) {
			log.error "$mn Unable to find configured default project for id ${defaultProject}"
			throw new ConfigurationException("Unable to find the default project for $client")
		}

		if (project.client.id != client.id) {
			log.error "$mn Project (${defaultProject}) not associated with client $client"
			throw new ConfigurationException("Project (${defaultProject}) not associated with client $client")
		}

		// Attempt to lookup the Person and their UserLogin
		def (person, userLogin) = findPersonAndUserLogin(client, userInfo, config, authority, personIdentifier)

		// Check various requirements based on the Authentication Configuration and fall out if we don't
		// have an account and not allowed to create one.
		if (! userLogin && ! userInfo.roles && ! domain.defaultRole) {
			throw new AccountException('No roles defined for the user')
		}
		if (! userLogin && ! person && ! autoProvision) {
			throw new AccountException('UserLogin and/or Person not found and autoProvision is disabled')
		}
		if ( (! person || ! userLogin) && ! autoProvision ) {
			log.warn "findOrProvisionUser: User attempted login but autoProvision is diabled ($personIdentifier)"
			throw new RuntimeException('Auto provisioning is disabled')
		}

		Map nameMap = this.userInfoToNameMap(userInfo)

		if (! person) {

			// Create the person and associate to the client & project if one wasn't found

			if (debug)
				log.debug "$mn Creating new person"

			person = new Person(
				firstName:nameMap.first,
				lastName:nameMap.last,
				middleName: nameMap.middle,
				email: userInfo.email,
				workPhone: userInfo.telephone,
				mobilePhone: userInfo.mobile
			)
			if (! person.save(flush:true)) {
				log.error "$mn Creating user ($personIdentifier) failed due to ${GormUtil.allErrorsString(person)}"
				throw new RuntimeException('Unexpected error while creating Person object')
			}
			// Create Staff relationship with the Company
			if (! partyRelationshipService.addCompanyStaff(client, person)) {
				throw new RuntimeException('Unable to associate new person to client')
			}
			// Create Staff relationship with the default Project
			if (! partyRelationshipService.addProjectStaff(project, person)) {
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
					if (debug)
						log.debug "$mn Updating existing person record : ${person.dirtyPropertyNames}"
					if (! person.save(flush:true)) {
						log.error "$mn Failed updating Person ($person) due to ${GormUtil.allErrorsString(person)}"
						throw new DomainUpdateException("Unexpected error while updating Person object")
					}
				}
			}
		}

		def createUser = userLogin == null
		if (createUser) {
			if (debug)
				log.debug "$mn Creating new UserLogin"
			userLogin = new UserLogin( person:person )
			// will set all the properties below
		}

		String guid = formatGuid(userInfo)

		// Set any of the variables that need to be set regardless of configuration settings or if it is new/existing
		if (! userLogin.externalGuid && userInfo.guid )
			userLogin.externalGuid = guid 	// Set to the formulated one with company id
		if ( userLogin.isLocal)
			userLogin.isLocal = false
		if (createUser)
			userLogin.active = 'Y'
		if (userLogin.forcePasswordChange == 'Y')
			userLogin.forcePasswordChange = 'N'
		if (userLogin.username?.toLowerCase() != userInfo.username.toLowerCase())
			userLogin.username = userInfo.username
		if (userLogin.password != userInfo.guid)
			userLogin.password = userInfo.guid

		def expiryDate = new Date() + 1
		if (userLogin.expiryDate <  expiryDate)
			userLogin.expiryDate = expiryDate

		if (createUser || userLogin.isDirty()) {
			if (log.isDebugEnabled() || debug)
				log.debug "findOrProvisionUser: Persisting UserLogin : ${userLogin.dirtyPropertyNames}"
			if (! userLogin.save(flush:true)) {
				def action = createUser ? 'creating' : 'updating'
				log.error "findOrProvisionUser: Failed $action UserLogin failed due to ${GormUtil.allErrorsString(userLogin)}"
				throw new RuntimeException("Unexpected error while $action UserLogin object")
			}
		}

		// Setup what the user roles should be based on the configuration and what came back from the userInfo
		List newUserRoles = userInfo.roles.size() ? userInfo.roles : []
		if (defaultRole) {
			if (! newUserRoles.contains(defaultRole)) {
				newUserRoles << defaultRole
			}
		}

		if (createUser) {
			// Add their role(s)
			newUserRoles.each { r ->
				if (debug)
					log.info "$mn Adding new security role '$r' to $personIdentifier"
				def rt = RoleType.read(r.toUpperCase())
				if (! rt || ! rt.isSecurityRole()) {
					throw new ConfigurationException("Configuration security property domains.${authority}.roleMap[$r] is an invalid code")
				}
				def pr = new PartyRole(party:person, roleType:rt)
				if (! pr.save(flush:true)) {
					log.error "$mn Unable to add new role for person ${GormUtil.allErrorsString(userLogin)}"
					throw new RuntimeException("Unexpected error while assigning security role")
				}
			}
		} else {
			// See about updating their roles
			if (domain.updateRoles) {

				List roleTypeCodes = SecurityConfigParser.getDomainRoleTypeCodes(config, authority)
				List roleTypes = RoleType.findAllByIdInList(roleTypeCodes)

				// Get the user roles from DB and compare
				def existingRoles = PartyRole.findAllByPartyAndRoleTypeInList(person, roleTypes)

				if (debug)
					log.debug "$mn User has existing roles $existingRoles"
				// See if there are any existing that should be removed
				existingRoles.each { er ->
					if (! newUserRoles*.toUpperCase().contains(er.roleType.id)) {
						if (debug)
							log.debug "$mn Deleting security role ${er.roleType.id} for $personIdentifier"
						er.delete(flush:true)
					}
				}

				// Now go through and add any new roles that aren't in the user's existing list
				newUserRoles.each { nr ->
					// Force the Role Types to uppercase
					nr = nr.toUpperCase()

					if (! existingRoles.find { it.roleType.id == nr }) {
						if (debug)
							log.debug "$mn Assigning new security role $nr for $personIdentifier"

						def rt = RoleType.read(nr)
						def pr = new PartyRole(party:person, roleType:rt)
						if (! pr.save(flush:true)) {
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
		def pref = userPreferenceService.getPreferenceByUserAndCode(userLogin, PREF.CURR_PROJ)
		if (createUser || ! pref) {
			// Set their default project preference
			userPreferenceService.setPreference(userLogin, PREF.CURR_PROJ, project.id.toString() )
			if (debug)
				log.debug "$mn set default project preference to ${project}"
		}
		if (defaultTimezone)
			// Set their TZ preference
			pref = userPreferenceService.getPreferenceByUserAndCode(userLogin, PREF.CURR_TZ)
			if (createUser || ! pref) {
				userPreferenceService.setPreference(userLogin, PREF.CURR_TZ, defaultTimezone )
				if (debug)
					log.debug "$mn: set timezone preference $defaultTimezone"
			}


		if (debug)
			log.debug "$mn FINISHED UserLogin(id:${userLogin.id}, $userLogin), Person(id:${person.id}, $person)"

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
			if (names.size() > 1)
				nameMap.middle = names[1..-1].join(' ')
		}
		return nameMap
	}

	String formatGuid(Map userInfo) {
		return "${userInfo.companyId}-${userInfo.guid}"
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
	List findPersonAndUserLogin( PartyGroup client, Map userInfo, Map config, String authority, String personIdentifier ) {

		// The first step is to attempt to find the user/person based on the following search patterns:
		//    1. The UserLogin.externalGuid code matches the person's AD objectguid property
		//    2. The UserLogin.username matches the person's AD UserLogin + '@' + company domain
		//    3. The Person.firstName+lastName match the person's AD givenname + sn properties

		def person
		def persons
		def userLogin

		// method name
		String mn = 'findUser:'

		// Flag for debugging
		boolean debug = log.isDebugEnabled() || config.debug
		Map domain = config.domains[authority.toLowerCase()]

		Map nameMap = this.userInfoToNameMap(userInfo)
		if (debug)
			log.debug "$mn Parsed [${userInfo.firstName} : ${userInfo.lastName}] into $nameMap"

		// Make the GUID unique to the companyId + the GUID from their authority system
		String guid = this.formatGuid(userInfo)
		userLogin = UserLogin.findByExternalGuid(guid)
		if (userLogin) {
			if (debug)
				log.debug "$mn Found user by GUID"

			// Check to verify that the user found is assoicated to the company
			// TODO - implement this validation
			// if (userLogin.person...)

		} else {

			// User wasn't found so let's try by the Person information
			// Try to find the Person in case they were previously loaded or provisioned other than by the login

			// First try to find by their email
			if (userInfo.email) {
				if (debug)
					log.debug "$mn Looking up person by email"
				persons = personService.findByClientAndEmail(client, userInfo.email )
			}

			// Then try to find by their name
			if (! persons) {
				if (debug)
					log.debug "$mn Looking up person by name"

				// The nameMap was first,middle,last but throughout the code we refer to firstName,...
				Map correctNameMap = [firstName:nameMap.first, middleName:nameMap.middle, lastName:nameMap.last]
				persons = personService.findByCompanyAndName(client, correctNameMap)
				if (debug)
					log.debug "$mn personService.findByCompanyAndName found ${persons?.size()} people"
			}

			// If we have any persons, try to find their respective user accounts
			if (persons) {
				def users = UserLogin.findAllByPersonInList(persons)
				if (debug)
					log.debug "$mn Found these users: $users"

				// If we find more than one account we don't know which to use
				def size = users.size()
				if (size == 1) {
					userLogin = users[0]
				} else if (size > 1) {
					log.error "$mn found ${users.size} users that matched $personIdentifier"
					throw new RuntimeException('Unable to resolve UserLogin account due to multiple matches')
				} else {
					size = persons.size()
					if (size == 1) {
						// Assign the person
						person = persons[0]
					} else if (size > 1) {
						log.error "$mn found ${persons.size} Person accounts that matched $personIdentifier"
						throw new RuntimeException('Unable to resolve Person account due to multiple matches')
					}
				}
			}
		}

		if (userLogin)
			person = userLogin.person

		return [person, userLogin]
	}

	/**
	 *
	 * @param projectInstance
	 * @return
	 */
	def getEventDetails(projectInstance){
		def currentUser= securityService.getUserLoginPerson()
		def projects = getSelectedProject(projectInstance)
		def upcomingEvents=[:]
		def dateNow = TimeUtil.nowGMT()
		def timeNow = dateNow.getTime()

		getEvents(projects, dateNow).each{ event, startTime->
			def teams = MoveEventStaff.findAllByMoveEventAndPerson(event, currentUser).role
			if(teams){
				upcomingEvents << [(event.id) : ['moveEvent':event,
					'teams':WebUtil.listAsMultiValueString(teams.collect {team-> team.description.replaceFirst("Staff : ", "")}),
					'daysToGo':startTime > dateNow ? (startTime-dateNow) : (" + " + (dateNow - startTime))]]
			}
		}

		return upcomingEvents
	}

	/**
	 *
	 * @param projectInstance
	 * @return
	 */
	def getSelectedProject(projectInstance){
		def projects= []
		if(projectInstance=='All'){
			def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
			def userProjects = projectService.getUserProjects(securityService.getUserLogin(), projectHasPermission, ProjectStatus.ACTIVE)
			projects = userProjects
		}else{
			projects = [projectInstance]
		}
		return projects
	}

	/**
	 *
	 * @param projectInstance
	 * @return
	 */
	def getEvents(projects, dateNow ){
		def moveEventList = MoveEvent.findAllByProjectInList(projects).sort{it.eventTimes.start}
		def thirtyDaysInMS = 2592000000

		def events = [:]

		moveEventList.each{event->
			def eventCompTimes = event.moveBundles.completionTime.sort().reverse()
			def startTimes = event.moveBundles.startTime.sort()
			startTimes.removeAll([null])
			def startTime = startTimes[0]
			def completionTime = eventCompTimes[0]
			if(completionTime && completionTime > dateNow.minus(30)){
				events << [(event):startTime]
			}
		}

		return events
	}

	/**
	 * Used to retgetEventNews
	 * @param project
	 * @return
	 */
	List getEventNews( projectOrAll ){
		List newsList = []
		List comingEvents = getEvents(getSelectedProject(projectOrAll), TimeUtil.nowGMT()).keySet().asList()
		// log.debug "getEventNews() comingEvents=$comingEvents"
		if (comingEvents) {
			String q = 'from MoveEventNews where moveEvent in (:events) and isArchived=:isArchived order by dateCreated desc'
			newsList = MoveEventNews.executeQuery(q, [events:comingEvents, isArchived:0])
		}

		return newsList
	}

	/**
	 *
	 * @param project
	 * @return
	 */
	def getTaskSummary ( project ){

		def timeInMin = 0
		def issueList = []
		def person = securityService.getUserLoginPerson()

		getSelectedProject(project).each{proj->
			def tasks = taskService.getUserTasks(person, proj, false, 7, 'score' )
			def taskList = tasks['user']
			def durationScale = [D:1440, M:1, W:10080, H:60] // minutes per day,week,hour
			taskList.each{ task ->
				def css = taskService.getCssClassForStatus( task.status )
				issueList << ['item':task,'css':css, projectName : proj.name]
			}
			if(taskList){
				timeInMin += taskList.sum{task->
					task.duration ? task.duration*durationScale[task?.durationScale] : 0
				}
			}
		}
		if (project == "All") {
			issueList:issueList.sort{it.item.score}
		}
		def dueTaskCount = issueList.item.findAll {it.duedate && it.duedate < TimeUtil.nowGMT()}.size()
		def totalDuration = TimeUtil.createProperDuration(0,0,timeInMin,0)
		return [taskList:issueList, totalDuration:totalDuration, dueTaskCount:dueTaskCount, personId:person.id]
	}

	/**
	 *
	 * @param project
	 * @return
	 */
	def getApplications( project ){

		//to get applications assigned to particular person & there relations.
		def currentUser= securityService.getUserLoginPerson()
		def appList = Application.findAll("from Application where project in (:projects) and (sme=:person or sme2=:person or appOwner=:person) \
											order by assetName asc", [projects:getSelectedProject(project), person:currentUser])
		def relationList = [:]
		appList.each{
			def relation = []
			if(it.sme==currentUser){
				relation << 'sme'
			}
			if(it.sme2==currentUser){
				relation << 'sme2'
			}
			if(it.appOwner==currentUser){
				relation << 'appOwner'
			}
			relationList << [(it.id) : WebUtil.listAsMultiValueString(relation)]
		}

		return [appList:appList, relationList:relationList]
	}

	/**
	 * Used in the dashboard to show which person are active
	 *
	 * @param project
	 * @return
	 */
	def getActivePeople( project ){

		//to get active people of that particular user selected project.
		def recentLogin = []
		def projects = getSelectedProject( project )
		def currUserLogin = securityService.getUserLogin()
		def projectIds = [0]

		projects.each { p ->
			projectIds << p.id
		}
		def projectIdsAsValue = projectIds.join(",")

		def query = new StringBuffer("SELECT p.person_id, ul.user_login_id, p.first_name, p.last_name, p.middle_name, proj.project_id, pg.name AS project_name, ul.last_page AS lastPage")
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

		if (users.size() > 0) {
			users.each{ r ->
				def person = Person.get(r.person_id)
				personName = ( r.last_name ? "${r.last_name}, ": '' ) + r.first_name + ( r.middle_name ? " $r.middle_name" : '' )
				recentLogin << ['personId': r.person_id, 'projectName': r.project_name, 'personName': person.toString(), 'lastActivity':r.lastPage]
			}
		}

		return recentLogin
	}

	/**
	 * Update the User to log the their last login
	 * @param username - username of the one that just logged in
	 */
	void updateLastLogin( String username, session ){
		if ( username ) {
			UserLogin userLogin = UserLogin.findByUsername( username )
			if (! userLogin) {
				log.error "updateLastLogin() was unable to find user $username"
			} else {
				Person person = userLogin.person
				session.setAttribute( "LOGIN_PERSON", ['name':person.firstName, "id":person.id ])

				def now = TimeUtil.nowGMT()
				userLogin.lastLogin = now
				userLogin.lastPage = now
				userLogin.save(flush:true)
			}
		}
	}

	/**
	 * Update the User's last page load time. This needs to be done in a separate transaction so that it doesn't
	 * potentially get rolled back with the whole page. It can fail due to Optimistic Locking which we'll just ignore.
	 * @param username - username of the one that just logged in
	 */
	void updateLastPageLoad(String username) {
		if (username) {
			try {
				UserLogin.withTransaction ([propagationBehavior: TransactionDefinition.PROPAGATION_REQUIRES_NEW]) { status ->
					UserLogin userLogin = UserLogin.findByUsername(username)
					def now = TimeUtil.nowGMT()
					if (userLogin) {
						// Only update if the page request is more than 5 seconds to avoid the multi-ajax requests within the same page
						if ((now.getTime() - userLogin.lastPage.getTime()) > 5000) {
							userLogin.lastPage = now
							userLogin.save(flush:true)
						}
					} else {
						log.error "updateLastPageLoad() was unable to find user $username"
					}
				}
			} catch (e) {
				log.warn "updateLastPageLoad() failed for user $username - ${e.getMessage()}"
			}
		}
	}

	/**
	 * Used to retrieve a list of users with recent activity
	 * @param inPastMinutes - the number of minutes since latest activity (default 5)
	 * @return A list of the username
	 * @Permission RestartApplication
	 * TODO : JPM 3/2016 : Change to use new permission that is more applicable
	 */
	List usernamesWithRecentActivity(int inPastMinutes=5) {
		String query = "SELECT username FROM user_login WHERE last_page > (NOW() - INTERVAL $inPastMinutes MINUTE) ORDER BY username"
		List users = jdbcTemplate.queryForList(query)
		// log.debug "usersWithRecentActivity() users=$users"
		return users*.username
	}


	/**
	 * This method updates the following User Preferences:
	 * - Current Project
	 * - Move Event
	 * - Current Bundle
	 * - Current Room
	 *
	 * @param user - a given UserLogin
	 * @param projectId - the id of the project to be assigned to the user.
	 *
	 * @return true: context updated / false: unable to update.
	 */
	boolean changeProjectContext(UserLogin user, long projectId){
		def contextUpdated = false
		def projectInstance = Project.read(projectId)
		if(projectService.hasAccessToProject(user, projectInstance)){
			userPreferenceService.setPreference(PREF.CURR_PROJ, "${projectId}")
			userPreferenceService.removePreference(PREF.MOVE_EVENT)
			userPreferenceService.removePreference(PREF.CURR_BUNDLE)
			userPreferenceService.removePreference(PREF.CURR_ROOM)
			contextUpdated = true
		}else{
			securityService.reportViolation("Attempted to access unavailable project $projectId", user)
		}

		return contextUpdated

	}
}
