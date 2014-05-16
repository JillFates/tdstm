/**
 * The UserService class provides methods to manage UserLogin domain
 * @author jmartin
 *
 */

import com.tdsops.tm.enums.domain.RoleTypeGroup
import com.tdssrc.grails.GormUtil
import com.tdsops.common.exceptions.ConfigurationException
import org.apache.shiro.authc.AccountException
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import org.apache.commons.lang.math.NumberUtils
import com.tds.asset.AssetComment
import com.tds.asset.Application
import com.tdsops.tm.enums.domain.ProjectStatus

class UserService {
	
	static transactional = true
	static final roleMap = [user:'USER', editor:'EDITOR', supervisor:'SUPERVISOR']
	static roleTypeList = []
	static initialized = false

	// IoC
	def personService
	def partyRelationshipService
	def userPreferenceService
	def taskService
	def projectService
	def securityService

	/**
	 * This is a post initialization method to allow late configuration settings to occur
	 */
	private synchronized void initialize() {
		if (! initialized) {
			// Load the RoleTypes that are used in the findOrProvisionUser method
			roleMap.each { k, v -> roleTypeList << RoleType.read(v) }
			initialized = true
		}
	}

	/**
	 * Used to find a user or provision the user based on the settings in the configuration file
	 * which is used by the AD and SSO integration.
	 * 
	 * @param userInfo - the information provided by the login authentication session
	 * @param config - a map of the authentication connector
	 * @return a the userLogin account that was found or provisioned
	 * @throws ConfigurationException, RuntimeException
	 */

	UserLogin findOrProvisionUser( Map userInfo, Map config ) {

		if (!initialized)
			initialize()

		// The first step is to attempt to find the user/person based on the following search patterns:
		//    1. The UserLogin.externalGuid code matches the person's AD objectguid property
		//    2. The UserLogin.username matches the person's AD UserLogin + '@' + company domain
		//    3. The Person.firstName+lastName match the person's AD givenname + sn properties

		def person
		def persons
		def userLogin
		def project

		// If the user email was blank then set it based on the username + the company domain in the config
		if (! userInfo.email) {
			userInfo.email = userInfo.username.contains('@') ? userInfo.username : "${userInfo.username}@${config.domain}"
		}

		// Set the username to the user's email as this is how they will be provisioned into TM
		if (! userInfo.username.contains('@'))
			userInfo.username = userInfo.email

		def personIdentifier = "${userInfo.firstName} ${userInfo.lastName}${(userInfo.email ? ' <'+userInfo.email+'>' : '')}"
		if (log.isDebugEnabled() || config.debug)
			log.debug "findOrProvisionUser: Attempting to find or provision $personIdentifier"

		def client = PartyGroup.read( userInfo.company )
		if (! client) {
			log.error "findOrProvisionUser: Unable to find configured company for id ${userInfo.company}"
			throw new ConfigurationException("Unable to find user's company by id ${userInfo.company}")
		}

		project = Project.read(config.defaultProject)
		if (! project) {
			log.error "findOrProvisionUser: Unable to find configured default project for id ${userInfo.defaultProject}"
			throw new ConfigurationException("Unable to find the default project for $client")
		}
		if (project.client.id != client.id) {
			log.error "findOrProvisionUser: Project (${config.defaultProject}) not associated with client $client"
			throw new ConfigurationException("Project (${config.defaultProject}) not associated with client $client")
		}

		// Parse the person's name and populate a name map
		def nameMap = [first:'', middle:'', last:userInfo.lastName]
		def names = userInfo.firstName.split(' ')
		if (names.size()) { 
			nameMap.first = names[0]
			if (names.size() > 1)
				nameMap.middle = names[1..-1].join(' ')
		}
		if (log.isDebugEnabled())
			log.debug "findOrProvisionUser: parsed [${userInfo.firstName} : ${userInfo.lastName}] into $nameMap"

		// Make the GUID unique to the companyId + the GUID from their authority system
		def guid = "${userInfo.company}-${userInfo.guid}"	
		userLogin = UserLogin.findByExternalGuid(guid)
		if (userLogin) {
			if (log.isDebugEnabled() || config.debug)
				log.debug "findOrProvisionUser: Found user by GUID"
		} else {

			// Now try to get the Person
			// Try to find the Person in case they were previously loaded or provisioned

			// First try to find by their email 
			if (userInfo.email) {
				if (log.isDebugEnabled() || config.debug)
					log.debug "findOrProvisionUser: Looking up person by email"
				persons = personService.findByClientAndEmail(client, userInfo.email )
			}

			// Then try to find by their name
			if (! persons) {
				if (log.isDebugEnabled() || config.debug)
					log.debug "findOrProvisionUser: Looking up person by name"
				persons = personService.findByClientAndName(client, nameMap)
			} 

			// If we have any persons, try to find their respective user accounts
			if (persons) {
				def users = UserLogin.findAllByPersonInList(persons)
				if (log.isDebugEnabled() || config.debug)
					log.debug "findOrProvisionUser: Found these users: $users"				

				// If we find more than one account we don't know which to use
				if (users.size() > 1) {
					log.error "findOrProvisionUser: found ${users.size} users that matched $personIdentifier"
					throw new RuntimeException('Unable to resolve multiple UserLogin accounts')
				}

				if (users.size() == 1) 
					userLogin = users[0]
			}
		}

		if (! userLogin && !userInfo.roles && !config.defaultRole) {
			throw new AccountException('No roles defined for the user')
		}

		if (! userLogin && ! person && ! config.autoProvision) {
			throw new AccountException('UserLogin or Person not found and autoProvision is disabled')
		}

		if (userLogin)
			person = userLogin.person

		if ( (! person || ! userLogin) && ! config.autoProvision ) {
			log.warn "findOrProvisionUser: User attempted login but autoProvision is diabled ($personIdentifier)"
			throw new RuntimeException('Auto provisioning is disabled')
		}

		// Create the person and associate to the client & project if one wasn't found
		if (! person) {
			if (log.isDebugEnabled() || config.debug)
				log.debug "findOrProvisionUser: Creating new person"

			person = new Person( 
				firstName:nameMap.first,
				lastName:nameMap.last,
				middleName: nameMap.middle,
				email: userInfo.email,
				staffType:'Salary',
				active:'Y',
				workPhone: userInfo.telephone,
				mobilePhone: userInfo.mobile
			)
			if (! person.save(flush:true)) {
				log.error "findOrProvisionUser: Creating user ($personIdentifier) failed due to ${GormUtil.allErrorsString(person)}"
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
			if (config.updateUser) {
				if (person.firstName != nameMap.first)
					person.firstName = nameMap.first
				if (person.middleName != nameMap.middle)
					person.middleName = nameMap.middle
				if (person.lastName != nameMap.last)
					person.lastName = nameMap.last
				if (person.email != userInfo.email)
					person.email = userInfo.email
				if (person.active != 'Y')
					person.active = 'Y'
				if (person.workPhone != userInfo.telephone)
					person.workPhone = userInfo.telephone
				if (person.mobilePhone != userInfo.mobile)
					person.mobilePhone = userInfo.mobile
				if (person.isDirty()) {
					if (log.isDebugEnabled() || config.debug)
						log.debug "findOrProvisionUser: Updating existing person record : ${person.dirtyPropertyNames}"
					if (! person.save(flush:true)) {
						log.error "findOrProvisionUser: Failed updating Person due to ${GormUtil.allErrorsString(person)}"
						throw new RuntimeException("Unexpected error while updating Person object")
					}
				}
			}
		}

		def createUser = userLogin == null
		if (createUser) {
			if (log.isDebugEnabled() || config.debug)
				log.debug "findOrProvisionUser: Creating new UserLogin"
			userLogin = new UserLogin( person:person )
		}

		// Set any of the variables that need to be set regardless of configuration settings or if it is new/existing
		if (! userLogin.externalGuid && userInfo.guid )
			userLogin.externalGuid = guid 	// Set to the formulated one with company id
		if ( userLogin.isLocal)
			userLogin.isLocal = false
		if (userLogin.active != 'Y')
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
			if (log.isDebugEnabled() || config.debug)
				log.debug "findOrProvisionUser: Persisting UserLogin : ${userLogin.dirtyPropertyNames}"
			if (! userLogin.save(flush:true)) {
				def action = createUser ? 'creating' : 'updating'
				log.error "findOrProvisionUser: Failed $action UserLogin failed due to ${GormUtil.allErrorsString(userLogin)}"
				throw new RuntimeException("Unexpected error while $action UserLogin object")
			}
		}

		// Setup what the user roles should be based on the configuration and what came back from the userInfo
		def newUserRoles = []
		if (config.roleMap) {
			// iterate over the user's roles and cross ref to the roleMap
			userInfo.roles.each { r -> 
				if (roleMap.containsKey(r))
					newUserRoles << roleMap[r]
			}
		} else {
			if (config.defaultRole && roleMap.containsKey(config.defaultRole))
				newUserRoles << roleMap[config.defaultRole]
		}
		if ( (createUser || config.updateRoles) && newUserRoles.size()==0) {
			log.warn "findOrProvisionUser: No roles were determined for $personIdentifier"
			throw new RuntimeException("Unable to determine security roles")
		}

		if (createUser) {
			// Add their role(s)
			newUserRoles.each { r -> 
				if (log.isDebugEnabled() || config.debug)
					log.debug "findOrProvisionUser: Adding new security role $r for $personIdentifier"
				def rt = RoleType.read(r)
				def pr = new PartyRole(party:person, roleType:rt)
				if (! pr.save(flush:true)) {
					log.error "findOrProvisionUser: Unable to add new role for person ${GormUtil.allErrorsString(userLogin)}"
					throw new RuntimeException("Unexpected error while assigning security role")
				}
			}
		} else {
			// See about updating their roles
			if (config.updateRoles) {

				// Get the user roles from DB and compare
				def existingRoles = PartyRole.findAllByPartyAndRoleTypeInList(person, roleTypeList)

				if (log.isDebugEnabled() || config.debug)
					log.debug "findOrProvisionUser: User has existing roles $existingRoles"
				// See if there are any existing that should be removed
				existingRoles.each { er -> 
					if (! newUserRoles.contains(er.roleType.id)) {
						if (log.isDebugEnabled() || config.debug)
							log.debug "findOrProvisionUser: Deleting security role ${er.roleType.id} for $personIdentifier"
						er.delete(flush:true)
					}
				}

				// Now go through and add any new roles that aren't in the user's existing list
				newUserRoles.each { nr -> 
					if (! existingRoles.find { it.roleType.id == nr}) {
						if (log.isDebugEnabled() || config.debug)
							log.debug "findOrProvisionUser: Assigning new security role $nr for $personIdentifier"
						def rt = RoleType.read(nr)
						def pr = new PartyRole(party:person, roleType:rt)
						if (! pr.save(flush:true)) {
							log.error "findOrProvisionUser: Unable to update new role for person ${GormUtil.allErrorsString(userLogin)}"
							throw new RuntimeException("Unexpected error while assigning security role")
						}
					}
				}
			}
		}

		//
		// Now setup their preferences if there is no or that they are a new user
		//
		def pref = userPreferenceService.getPreferenceByUserAndCode(userLogin, 'CURR_PROJ')
		if (createUser || ! pref) {
			// Set their default project preference
			userPreferenceService.setPreference(userLogin, 'CURR_PROJ', project.id.toString() )
			if (log.isDebugEnabled() || config.debug)
				log.debug "findOrProvisionUser: set default project preference to ${project}"
		}
		if (config.defaultTimezone)
			// Set their TZ preference
			pref = userPreferenceService.getPreferenceByUserAndCode(userLogin, 'CURR_TZ')
			if (createUser || ! pref) {
				userPreferenceService.setPreference(userLogin, 'CURR_TZ', config.defaultTimezone )
				if (log.isDebugEnabled() || config.debug)
					log.debug "findOrProvisionUser: set timezone preference ${config.defaultTimezone}"
			}


		if (log.isDebugEnabled() || config.debug)
			log.debug "findOrProvisionUser: FINISHED UserLogin(id:${userLogin.id}, $userLogin), Person(id:${person.id}, $person)"

		return userLogin
	}
	/**
	 * get the user portal Details for a project or list of projects.
	 * @param projectInstance
	 * @return
	 */
	def getuserPortalDetails(projectInstance){
		def currentUser= securityService.getUserLoginPerson()
		def dateNow = TimeUtil.nowGMT()
		def timeNow = dateNow.getTime()
		
		def projects= []
		if(projectInstance=='All'){
			def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
			def userProjects = projectService.getUserProjects(securityService.getUserLogin(), projectHasPermission, ProjectStatus.ACTIVE)
			projects = userProjects
		}else{
			projects = [projectInstance]
		}
		//to get task Summary.
		def issueList = []
		def timeInMin =0
		projects.each{project->
			def tasks = taskService.getUserTasks(currentUser, project, false, 7 )
			def taskList = tasks['user']
			def durationScale = [d:1440, m:1, w:10080, h:60] // minutes per day,week,hour
			taskList.each{ task ->
				def css = taskService.getCssClassForStatus( task.status )
				issueList << ['item':task,'css':css]
			}
			if(taskList){
				timeInMin += taskList.sum{task->
					task.duration ? task.duration*durationScale[task?.durationScale] : 0
				}
			}
		}
		//to get applications assigned to particular person & there relations.
		def appList = Application.findAll("from Application where project in (:projects) and (sme=:person or sme2=:person or appOwner=:person)",
											[projects:projects, person:currentUser])
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
		
		//to get active people of that particular user selected project.
		def loginPersons = UserLogin.findAll()
		def recentLogin = [:]
		(loginPersons-securityService.getUserLogin()).each{
			if(it?.lastLogin && it?.lastLogin.getTime()>timeNow-1800000){
				def loginProjId = UserPreference.executeQuery("SELECT value FROM UserPreference where userLogin.id=? and preferenceCode=?",[it.id,'CURR_PROJ'])[0]
				if(NumberUtils.toLong(loginProjId) in projects?.id){
					recentLogin << [(it.id) : ['name':it.person, 'project':Project.get(loginProjId)]]
				}
			}
		}
		
		//to get all moveEvent list assigned to team.
		def moveEventList = MoveEvent.findAllByProjectInList(projects)
		def thirtyDaysInMS = 2592000000
		def upcomingEvents=[:]
		moveEventList.each{event->
			def startTime = event.moveBundles.startTime.sort()[0]
			if(startTime && startTime>dateNow && startTime < dateNow.plus(30)){
				def teams = ProjectTeam.executeQuery("from ProjectTeam where moveBundle in (:bundles)",[bundles:event.moveBundles])
				if(teams){
					upcomingEvents << [(event.id) : ['moveEvent':event, 'teams':WebUtil.listAsMultiValueString(teams.teamCode),'daysToGo':startTime-dateNow]]
				}
			}
		}
		def newsList = []
		def comingEvents =upcomingEvents.keySet().asList()
		if(comingEvents){
			newsList = MoveEventNews.findAll("from MoveEventNews where moveEvent.id in (:events)",[events:comingEvents])
		}
		return [taskList:issueList, timeInMin:timeInMin, appList:appList, relationList:relationList, upcomingEvents:upcomingEvents,
				 recentLogin:recentLogin, newsList:newsList]
	}
	
}