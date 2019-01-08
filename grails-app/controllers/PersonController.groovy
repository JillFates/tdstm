import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import net.transitionmanager.command.PersonCO
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventStaff
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.Timezone
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.InvalidRequestException
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UnauthorizedException
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.UserService
import grails.web.mapping.LinkGenerator
import org.springframework.jdbc.core.JdbcTemplate

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class PersonController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	ControllerService        controllerService
	JdbcTemplate             jdbcTemplate
	PartyRelationshipService partyRelationshipService
	PersonService            personService
	ProjectService           projectService
	TaskService              taskService
	UserPreferenceService    userPreferenceService
	UserService              userService
	LinkGenerator            grailsLinkGenerator

	/**
	 * Generates a list view of persons related to company
	 * @param id - company id
	 * @param companyName - optional search by name or 'ALL'
	 */
	@HasPermission(Permission.PersonStaffList)
	def list() {

		def listJsonUrl
		def company
		def currentCompany = securityService.userCurrentProject?.client
		String companyId = params.companyId ?: currentCompany?.id ?: 'All'
		if (companyId && companyId != 'All') {
			listJsonUrl = createLink(controller: 'person', action: 'listJson', id: companyId)
		} else {
			listJsonUrl = createLink(controller: 'person', action: 'listJson') + '/All'
		}

		//def partyGroupList = PartyGroup.findAllByPartyType(PartyType.read("COMPANY")).sort{it.name}
		def partyGroupList = partyRelationshipService.associatedCompanies(securityService.userLoginPerson)
		// Used to set the default value of company select in the create staff dialog
		if (companyId && companyId != 'All') {
			company = PartyGroup.findByPartyTypeAndId(PartyType.load('COMPANY'), companyId.toLong())
		}
		else {
			company = currentCompany
		}

		userPreferenceService.setPreference(PREF.PARTY_GROUP, companyId)

		//used to show roles in addTeam select
		[companyId: companyId ?: 'All', company: company, partyGroupList: partyGroupList,
		 listJsonUrl: listJsonUrl, availabaleRoles: partyRelationshipService.getStaffingRoles(false)]
	}

	@HasPermission(Permission.PersonStaffList)
	def listJson() {

		String sortIndex = params.sidx ?: 'lastname'
		String sortOrder  = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows
		def companyId
		def personInstanceList
		def filterParams = [
			firstname  : params.firstname,
			middlename : params.middlename,
			lastname   : params.lastname,
			userLogin  : params.userLogin,
			email      : params.email,
			company    : params.company,
			dateCreated: params.dateCreated,
			lastUpdated: params.lastUpdated,
			modelScore : params.modelScore
		]

		// Validate that the user is sorting by a valid column
		if (!(sortIndex in filterParams)) {
			sortIndex = 'lastname'
		}

		def query = new StringBuilder("""SELECT * FROM (SELECT p.person_id AS personId, p.first_name AS firstName,
			IFNULL(p.middle_name,'') as middlename, IFNULL(p.last_name,'') as lastName, IFNULL(u.username, 'CREATE') as userLogin, p.email as email,
			pg.name AS company, u.active, date_created AS dateCreated, last_updated AS lastUpdated, u.user_login_id AS userLoginId,
			IFNULL(p.model_score, 0) AS modelScore
			FROM person p
			LEFT OUTER JOIN party_relationship r ON r.party_relationship_type_id='ROLE_STAFF'
				AND role_type_code_from_id='COMPANY' AND role_type_code_to_id='ROLE_STAFF' AND party_id_to_id=p.person_id
			LEFT OUTER JOIN party pa on p.person_id=pa.party_id
			LEFT OUTER JOIN user_login u on p.person_id=u.person_id
			LEFT OUTER JOIN party_group pg ON pg.party_group_id=r.party_id_from_id
			""")

		if (params.id && params.id != "All") {
			// If companyId is requested
			companyId = params.id
		}
		if (!companyId && params.id != "All") {
			// Still if no companyId found trying to get companyId from the session
			companyId = userPreferenceService.getPreference(PREF.PARTY_GROUP)
			if (!companyId) {
				// Still if no luck setting companyId as logged-in user's companyId .
				companyId = securityService.userLoginPerson.company.id
			}
		}
		if (companyId) {
			query.append(" WHERE pg.party_group_id = $companyId ")
		}

		query.append(" GROUP BY pa.party_id ORDER BY " + sortIndex + " " + sortOrder +
				", IFNULL(p.last_name,'') DESC, p.first_name DESC) as people")

		// Handle the filtering by each column's text field
		def firstWhere = true
		filterParams.each {
			if (it.value) {
				if (firstWhere) {
					query.append(" WHERE people.$it.key LIKE '%$it.value%'")
					firstWhere = false
				}
				else {
					query.append(" AND people.$it.key LIKE '%$it.value%'")
				}
			}
		}

		personInstanceList = jdbcTemplate.queryForList(query.toString())

		// Limit the returned results to the user's page size and number
		int totalRows = personInstanceList.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0) {
			personInstanceList = personInstanceList[rowOffset..Math.min(rowOffset + maxRows, totalRows - 1)]
		}

		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the
		// only simple way to have the links work correctly
		boolean canCreate = securityService.hasPermission(Permission.UserCreate)
		boolean canEdit = securityService.hasPermission(Permission.UserEdit)
		String userLoginCreateLink = createLink(controller: 'userLogin', action: 'create')
		String userLoginEditLink = createLink(controller:'userLogin', action:'edit')
		String userAddPng = "$grailsLinkGenerator.serverBaseURL/assets/icons/user_add.png"
		def results = personInstanceList?.collect {
			[cell: ['<a href="javascript:Person.showPersonDialog(' + it.personId + ',\'generalInfoShow\')">' + it.firstname + '</a>',
			'<a href="javascript:Person.showPersonDialog(' + it.personId + ',\'generalInfoShow\')">' + it.middlename + '</a>',
			'<a href="javascript:Person.showPersonDialog(' + it.personId + ',\'generalInfoShow\')">' + it.lastname + '</a>',
			genCreateEditLink(canCreate, canEdit, userLoginCreateLink, userLoginEditLink, userAddPng, it),
			it.email, it.company, it.dateCreated, it.lastUpdated, it.modelScore], id: it.personId ]}
		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	/**
	 * Creates an anchor for specific user based on user permission
	 *
	 * @param haveUserCreatePerm  if the user has UserCreatePerm
	 * @param haveUserEditPerm  if the user has UserEditPerm
	 * @param createUrl  url used to create a new login for the current person
	 * @param editUrl url used to edit login configuration for the current person
	 * @param personData person data from the database query
	 */
	private String genCreateEditLink(
		boolean haveUserCreatePerm,
		boolean haveUserEditPerm,
		String createUrl,
		String editUrl,
		String addUserIconUrl,
		Map personData) {

		String element
		if (personData.userLoginId) {
			if (haveUserEditPerm) {
				element = '<a href="' + editUrl + '/' + personData.userLoginId + '">' + personData.userLogin + '</a>'
			} else {
				element = personData.userLogin
			}
		} else {
			if (haveUserCreatePerm) {
				element = '<a href="' + createUrl + '/' + personData.personId + '"><img src="' + addUserIconUrl + '" /> Create User</a>'
			} else {
				element = ''
			}
		}
		return element
	}

	/**
	 * Bulk delete Person objects as long as they do not have user accounts or assigned tasks and optionally associated with assets
	 * @param ids[] - a list of person ids to be deleted
	 */
	@HasPermission(Permission.PersonDelete)
	def bulkDelete() {
		def ids = params.get("ids[]")
		if (!ids) {
			renderErrorJson('Please select at least one person to be be bulk deleted.')
			return
		}
		// Convert from Ljava.lang.String or String to a list
		List idsToDelete = (ids instanceof String) ? [ids] : ids

		Person byWhom = securityService.getUserLoginPerson()
		controllerService.checkPermissionForWS(Permission.PersonBulkDelete)

		boolean deleteIfAssocWithAssets = (params.deleteIfAssocWithAssets == 'true')
		Map results = personService.bulkDelete(byWhom, idsToDelete, deleteIfAssocWithAssets)
		renderSuccessJson(results)
	}

	/**
	 * Used to save a new Person domain object
	 * @param forWhom - used to indicate if the submit is from a person form otherwise it is invoked from Ajax call
	 */
	@HasPermission(Permission.PersonCreate)
	def save() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		// When forWhom == 'person' we're working with the company submitted with the form otherwise we're
		// going to use the company associated with the current project.
		def isAjaxCall = params.forWhom != "person"
		def companyId
		if (isAjaxCall) {
			companyId = project.client.id
		} else {
			companyId = NumberUtil.toLong(params.company)
		}

		def errMsg
		def person
		Map personParams = (request.format == 'json') ? request.JSON : params
		def duplicatePersonId
		try {
			person = personService.savePerson(personParams, companyId, project, true)
		} catch (DomainUpdateException e) {
			def exceptionMsg = e.message
			log.error(exceptionMsg, e)
			// The line below is a hack to avoid querying the database.
			duplicatePersonId = exceptionMsg.substring(exceptionMsg.indexOf(":") + 1).toInteger()
			errMsg = "A person with the same first and last name already exists for this Company."
		} catch (e) {
			log.error "save() failed : ${ExceptionUtil.stackTraceToString(e)}"
			errMsg = e.message
		}

		if (isAjaxCall) {
			def map = errMsg ? [errMsg : errMsg] :
				[id: person.id, name:person.toString(), isExistingPerson: false, fieldName:params.fieldName]
			render map as JSON
		} else {
			if (errMsg) {
				errMsg += " Click <a href=\"javascript:Person.showPersonDialog($duplicatePersonId,'generalInfoShow')\"> here </a>"//e.message
				errMsg += "to view the person."
				flash.message = errMsg
			} else {
				// Just add a message for the form submission to know that the person was created
				flash.message = "A record for $person was created"
			}
			redirect(action:"list", params:[ companyId:companyId ])
		}
	}

	/*
	 * Method to add Staff to project through Ajax Overlay
	 */
	@HasPermission(Permission.ProjectStaffEdit)
	def saveProjectStaff() {
		def flag = false
		def message = ''

		if (request.JSON.personId) {
			def personId = request.JSON.personId
			String roleType = request.JSON.roleType
			Project project = securityService.userCurrentProject
			def personParty = Person.get(personId)
			if (NumberUtil.toInteger(request.JSON.val) == 1) {
				partyRelationshipService.deletePartyRelationship("PROJ_STAFF", project, "ROLE_PROJECT", personParty, roleType)
				def moveEvents = MoveEvent.findAllByProject(project)
				MoveEventStaff.executeUpdate(
						"delete from MoveEventStaff where moveEvent in (:moveEvents) and person = :person and role = :role",
						[moveEvents:moveEvents, person:personParty,role:RoleType.load(roleType)])
			} else if (personService.hasAccessToProject(personParty, project) ||
				        (!(partyRelationshipService.isTdsEmployee(personId) && !securityService.hasPermission(Permission.PersonEditTDS)))) {
				partyRelationshipService.savePartyRelationship("PROJ_STAFF", project, "ROLE_PROJECT", personParty, roleType)
			}else{
				message = "This person doesn't have access to the selected project"
			}

			flag = message.size() == 0
		}

		renderSuccessJson(flag: flag, message: message)
	}

	/**
	 * Update the person details and user password, Return person first name
	 * @param  : person details and user password
	 * @return : person firstname
	 */
	@HasPermission(Permission.PersonEdit)
	def updatePerson() {
		try {
			String tzId = userPreferenceService.timeZone
			/*Party newCompany = Party.findByName(params["Company"])
			if (!personService.isAssociatedTo(person, newCompany)) {
				throw new DomainUpdateException("The person $person is not associated with the company $newCompany")
			}*/
			Person person = personService.updatePerson(params, true)
			if (params.tab) {
				forward(action: 'loadGeneral', params :[tab: params.tab, personId:person.id])
			} else {
				renderAsJson(name: person.firstName, tz: tzId)
			}
		} catch (e) {
			log.debug "updatePerson() failed : ${ExceptionUtil.stackTraceToString(e)}"
			renderErrorJson(e.message)
		}
	}

	/**
	 * Will return person details for a given personId as JSON
	 * @param  params.id - the person id
	 * @return person details as JSON
	 */
	@HasPermission(Permission.PersonView)
	def retrievePersonDetails() {
		try {
			Person person
			if(params.id) {
				person = personService.validatePersonAccess(params.id)
			} else {
				person = personService.validatePersonAccess(currentPerson().id)
			}
			UserLogin userLogin = securityService.getPersonUserLogin(person)
			def expiryDate = TimeUtil.formatDateTime(userLogin.expiryDate)

			def personDetails = [person:person, expiryDate: expiryDate, isLocal:userLogin.isLocal]
			render personDetails as JSON
		} catch (e) {
			renderErrorJson(e.message)
		}
	}

	/**
	 * Update the person account that is invoked by the user himself
	 * @param  : person id and input password
	 * @return : pass:"no" or the return of the update method
	 */
	@HasPermission(Permission.UserUpdateOwnAccount)
	def updateAccount() {
		String errMsg = ''
		Map results = [:]
		try {
			//params.id = securityService.currentUserLoginId
			String tzId = userPreferenceService.timeZone
			Person person = personService.updatePerson(params, false)

			if (params.tab) {
				// Funky use-case that we should try to get rid of
				forward(action:'loadGeneral', params:[tab: params.tab, personId:person.id])
				return
			} else {
				results = [ name:person.firstName, tz:tzId ]
			}

		} catch (InvalidParamException | DomainUpdateException e) {
			errMsg = e.message
		} catch (e) {
			log.warn "updateAccount() failed : ${ExceptionUtil.stackTraceToString(e)}"
			errMsg = 'An error occurred during the update process'
		}

		if (errMsg) {
			renderErrorJson(errMsg)
		} else {
			renderSuccessJson(results)
		}
	}

	/*
	 * The primary controller method to bootstrap the Project Staff administration screen which then
	 * leverages the loadFilteredStaff method to populate list in an Ajax call
	 * @return The HTML for the controls at the top of the form and the Javascript to load the data.
	 */
	@HasPermission(Permission.ProjectStaffList)
	def manageProjectStaff() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def start = new Date()

		List roleTypes = partyRelationshipService.getStaffingRoles(false)

		// set the defaults for the checkboxes

		def assigned = userPreferenceService.getPreference(PREF.SHOW_ASSIGNED_STAFF) ?: '1'
		def onlyClientStaff = userPreferenceService.getPreference(PREF.SHOW_CLIENT_STAFF) ?: '1'
		def currRole = params.role ?: userPreferenceService.getPreference(PREF.STAFFING_ROLE) ?: "0"
		List projects = personService.getAvailableProjects(securityService.userLoginPerson)

		[project: project, projects: projects, projectId: project.id, roleTypes: roleTypes,
		 editPermission: securityService.hasPermission(Permission.ProjectStaffEdit), assigned: assigned,
		 onlyClientStaff: onlyClientStaff, currRole: currRole]
	}

	/*
	 * Generates an HTML table of Project Staffing based on a number of filter parameters called by AJAX service.
	 * The list of staff that appear is going to be contingent based on what user is viewing the page. The use-cases are:
	 *    Staff of Owner - see all owner, partner and client staff without limitations (when the Only Assigned is not checked)
	 *    Staff of Partner - ONLY assigned staff to the project
	 *    Staff of Client - ONLY assigned staff of Owner and Partner and All Client Staff without limitation
	 *
	 * @param project - id of project from select, 0 for ALL
	 * @param role - the code for filtering staff list, '0' for all roles
	 * @param assigned - flag if 1 indicates only assigned staff, 0 indicates all
	 * @param onlyClientStaff - flag if 1 indicates only include staff of the client, 0 indicates all available staff
	 * @return HTML table of the data
	 */
	@HasPermission(Permission.ProjectStaffList)
	def loadFilteredStaff() {
		//Date start = new Date()

		Person loginPerson = securityService.userLoginPerson

		// Get the ID of the Automated Task Person that shouldn't be in the list
		Person autoTask = taskService.getAutomaticPerson()
		Long autoTaskId = autoTask ? autoTask.id : 0

		//
		// Deal with filter parameters
		//
		String role = params.role ?: 'AUTO'
		Long projectId = NumberUtil.toPositiveLong(params.project, -1)
		if (projectId < 1) {
			render 'Invalid project number was specified'
			return
		}

		Project project = Project.read(projectId)
		if (!project) {
			render 'Specified Project was not found'
			return
		}

		if (!securityService.isCurrentProjectId(projectId)) {
			userService.changeProjectContext(projectId)
		}

		// log.debug "loadFilteredStaff() phase 1 took ${TimeUtil.elapsed(start)}"
		// start = new Date()

		List accessibleProjects = personService.getAvailableProjects(loginPerson)
		if (!accessibleProjects.find {it.id == projectId }) {
			securityService.reportViolation("attempted to access project staffing for project $project without necessary access rights")
			render 'Specified Project was not found'
			return
		}

		// log.debug "loadFilteredStaff() phase 2 took ${TimeUtil.elapsed(start)}"
		// start = new Date()

		String assigned = params.containsKey('assigned') && '01'.contains(params.assigned) ? params.assigned : '1'
		String onlyClientStaff = params.containsKey('onlyClientStaff') && '01'.contains(params.onlyClientStaff) ?
				params.onlyClientStaff : '1'
		def sortableProps = ['fullName', 'company', 'team']
		def orders = ['asc', 'desc']

		// code which is used to resolve the bug in TM-2585:
		// alphasorting is reversed each time when the user checks or unchecks the two filtering checkboxes.
		if (params.firstProp != 'staff') {
			session.setAttribute("Staff_OrderBy", params.orderBy)
			session.setAttribute("Staff_SortOn", params.sortOn)
		} else {
			params.orderBy = session.getAttribute("Staff_OrderBy") ?: 'asc'
			params.sortOn = session.getAttribute("Staff_SortOn") ?: 'fullName'
		}

		// TODO : JPM 11/2015 : Do not believe the firstProp is used in method loadFilteredStaff
		def paramsMap = [
			sortOn : params.sortOn in sortableProps ? params.sortOn : 'fullName',
			firstProp : params.firstProp,
			orderBy : params.orderBy in orders ? params.orderBy : 'asc'
		]

		String sortString = "$paramsMap.sortOn $paramsMap.orderBy"
		sortableProps.findAll { it != paramsMap["startOn"] }.each {
			sortString += ', ' + it + ' asc'
		}

		// log.debug "loadFilteredStaff() phase 3 took ${TimeUtil.elapsed(start)}"
		// start = new Date()

		// Save the user preferences from the filter (internally it only saves it if the preference has changed)
		userPreferenceService.setPreference(loginPerson.userLogin, UserPreferenceEnum.STAFFING_ROLE, role)
		userPreferenceService.setPreference(loginPerson.userLogin, UserPreferenceEnum.SHOW_CLIENT_STAFF, onlyClientStaff)
		userPreferenceService.setPreference(loginPerson.userLogin, UserPreferenceEnum.SHOW_ASSIGNED_STAFF, assigned)

		// log.debug "loadFilteredStaff() phase 4 took ${TimeUtil.elapsed(start)} (user preferences)"
		// start = new Date()

		// log.debug "loadFilteredStaff() phase 4b took ${TimeUtil.elapsed(start)}"
		// start = new Date()

		List moveEvents
		List projectList = [project]

		// Find all Events for one or more Projects and the Staffing for the projects
		// Limit the list of events to those that completed within the past 30 days or have no completion and have started
		// in the past 90 days
		if (projectList) {
			moveEvents = MoveEvent.executeQuery("from MoveEvent where project in (:projects) order by project.name, name asc",
				[projects:projectList])
			Date now = TimeUtil.nowGMT()

			def eventsOption = params.eventsOption

			if (eventsOption in ["A", "C"])
			moveEvents = moveEvents.findAll {
				def eventTimes = it.eventTimes
				if (eventTimes) {
					if (eventsOption == "A") {
						(eventTimes.completion && eventTimes.completion > now - 30) ||
						(!eventTimes.completion && eventTimes.start && eventTimes.start > now - 90)
					}
					else {
						eventTimes.completion && eventTimes.completion < now
					}
				}

			}
		}
		// log.debug "loadFilteredStaff() phase 5 took ${TimeUtil.elapsed(start)} Get Events"
		// start = new Date()

		String projects = project.id.toString()
		StringBuilder companies = new StringBuilder(project.clientId.toString())
		if (onlyClientStaff == '0') {
			// Add the owner company and any partner companies associated with the project
			companies.append(', ').append(project.owner.id)
			def projectPartners = projectService.getPartners(project)

			// log.debug "loadFilteredStaff() phase 5b took ${TimeUtil.elapsed(start)} Get Partners"
			// start = new Date()

			if (projectPartners) {
				companies.append(',' + projectPartners*.id.join(','))
			}
		}
		// log.debug "loadFilteredStaff() phase 5c took ${TimeUtil.elapsed(start)} Get Companies"
		// start = new Date()

		// Get the list of staff that should be displayed based on strictly assigned or available staff
		List staff
		if (assigned == '1') {
			if (role == '0') {
				staff = projectService.getAssignedStaff(project)
			} else {
				staff = projectService.getAssignedStaff(project, role)
			}
		} else {
			staff = projectService.getAssignableStaff(project, loginPerson)
		}

		// log.debug "loadFilteredStaff() phase 6 took ${TimeUtil.elapsed(start)}"
		// start = new Date()

		List staffList

		if (staff) {
			// The staffIds will help filter down who can appear in the list
			String staffIds = staff.id.join(',')

			def query = new StringBuilder("""
				SELECT * FROM (
					SELECT pr.party_id_to_id AS personId,
						p.last_name AS lastName,
						CONCAT(COALESCE(p.first_name,''),
							if (p.middle_name IS NULL OR p.middle_name = '', '', ' '),
							COALESCE(p.middle_name, ''),
							if (p.last_name IS NULL OR p.last_name = '', '', ' '),
							COALESCE(p.last_name, '')) AS fullName,
						company.name AS company,
						pr.role_type_code_to_id AS role,
						rt.description AS team,
						pr2.party_id_to_id IS NOT NULL AS project,
						IFNULL(CONVERT(GROUP_CONCAT(mes.move_event_id) USING 'utf8'), 0) AS moveEvents,
						IFNULL(CONVERT(GROUP_CONCAT(DATE_FORMAT(ed.exception_day, '%Y-%m-%d')) USING 'utf8'),'') AS unavailableDates
					FROM party_relationship pr
						LEFT OUTER JOIN person p ON p.person_id = pr.party_id_to_id and p.active='Y'
						LEFT OUTER JOIN exception_dates ed ON ed.person_id = p.person_id
						LEFT OUTER JOIN party_group company ON company.party_group_id = pr.party_id_from_id
						LEFT OUTER JOIN role_type rt ON rt.role_type_code = pr.role_type_code_to_id
						LEFT OUTER JOIN party_relationship pr2 ON pr2.party_id_to_id = pr.party_id_to_id
							AND pr2.role_type_code_to_id = pr.role_type_code_to_id
							AND pr2.party_id_from_id IN ($projects)
							AND pr2.role_type_code_from_id = 'PROJECT'
						LEFT OUTER JOIN move_event_staff mes ON mes.person_id = p.person_id
							AND mes.role_id = pr.role_type_code_to_id
					WHERE pr.role_type_code_from_id in ('COMPANY')
						AND pr.party_relationship_type_id in ('STAFF')
						AND pr.party_id_from_id IN ($companies)
						AND p.active = 'Y'
						AND pr.party_id_to_id IN ($staffIds)
						AND pr.party_id_to_id != $autoTaskId
					GROUP BY role, personId
					ORDER BY fullName ASC
				) AS companyStaff
				WHERE 1=1
			""")

			// Filter on the role (aka teams) if there is a filter for it
			if (role != '0') {
				query.append("AND companyStaff.role = '$role' ")
			}

			//query.append(" ORDER BY fullName ASC, team ASC ")
			query.append(" ORDER BY ${sortString}")

			// log.debug "loadFilteredStaff() query=$query"
			staffList = jdbcTemplate.queryForList(query.toString())

			// The template uses this value for the checkboxes
			staffList.each { it -> it.inProjectValue = it.project ? '1' : '0' }
		}

		// log.debug "loadFilteredStaff() phase 7 took ${TimeUtil.elapsed(start)}"
		// start = new Date()

		render(template: "projectStaffTable",
		       model: [staffList: staffList, moveEventList: retrieveBundleHeader(moveEvents), projectId: projectId,
		               firstProp: params.firstProp, editPermission: securityService.hasPermission(Permission.ProjectStaffEdit),
		               project: project, sortOn: params.sortOn, orderBy: paramsMap.orderBy])
	}

	/*
	 *@param tab is name of template where it need to be redirect
	 *@param person Id is id of person
	 *@return NA
	 */
	@HasPermission(Permission.ProjectStaffShow)
	def loadGeneral() {
		log.debug "loadGeneral() class: ${params.personId.getClass()} value: $params.personId"

		Person person = Person.get(params.personId)
		def blackOutdays = person.blackOutDates?.sort{it.exceptionDay}
		def company = person.company
		def personFunctions = []
		// <SL>: Find a better solution to determine if the person we are trying to load if not "Automated"
		// TM-6780
		if (company) {
			personFunctions = partyRelationshipService.getCompanyStaffFunctions(company.id, person.id)
		}
		def availabaleFunctions = partyRelationshipService.getStaffingRoles(false)
		def partyGroupList = partyRelationshipService.associatedCompanies(securityService.userLoginPerson)

		render(template: params.tab ?: 'generalInfoShow',
		       model: [person: person, company: company, personFunctions: personFunctions,
		               availabaleFunctions: availabaleFunctions, sizeOfassigned: personFunctions.size() + 1,
		               blackOutdays: blackOutdays, isProjMgr: securityService.hasRole("PROJ_MGR"),
		               partyGroupList: partyGroupList])
	}

	/*
	 *To get headers of event at project staff table
	 *@param moveEvents list of moveEvent for selected project
	 *@return MAP of bundle header containing projectName ,event name, start time and event id
	 */
	@HasPermission(Permission.EventView)
	@HasPermission(Permission.BundleView)
	@HasPermission(Permission.PersonShowView)
	private List<Map> retrieveBundleHeader(moveEvents) {
		// TODO : JPM 5/2015 : Need to add security controls
		Project project = securityService.userCurrentProject
		def moveEventList = []
		if (project) {
			String bundleStartDate = ''
			for (MoveEvent moveEvent in moveEvents) {
				Collection<MoveBundle> moveBundles = moveEvent?.moveBundles ?: []
				List<Date> startDates = moveBundles*.startTime.sort()
				def eventTimes = moveEvent.eventTimes
				startDates?.removeAll([null])
				if (startDates) {
					if (startDates[0]) {
						bundleStartDate = TimeUtil.formatDateTime(startDates[0], TimeUtil.FORMAT_DATE_TIME_10)
					}
				}
				moveEventList << [project: moveEvent.project.name, name: moveEvent.name, startTime: bundleStartDate,
				                  startDate: TimeUtil.formatDateTime(eventTimes.start, TimeUtil.FORMAT_DATE_ISO8601),
				                  id: moveEvent.id]
			}
		}
		return moveEventList
	}

	/*
	 * Used to save event for staff in moveEvent staff
	 * @param id as composite id contains personId , MoveEventId and roleType id with separated of '-'
	 * @return if updated successful return true else return false
	 */
	@HasPermission(Permission.ProjectStaffEdit)
	def saveEventStaff() {
		// Security is checked in the service method
		Map json = request.JSON
		String message = personService.assignToProjectEvent(json.personId, json.eventId,
				json.roleType, NumberUtil.toInteger(json.val))
		renderSuccessJson(flag: message.size() == 0, message: message)
	}

	/**
	 * Handle Ajax request to delete current user's individual preferences except 'Current Dashboard'
	 * @param prefCode : Preference Code that is requested for being deleted
	 * @return : boolean
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def removeUserPreference() {
		// TODO : JPM 5/2015 : Improve removeUserPreference - validate it was successful, return SecurityService.success
		String prefCode = params.prefCode
		if (prefCode != "Current Dashboard") {
			userPreferenceService.removePreference(prefCode)
		}

		render true
	}

	@HasPermission(Permission.UserGeneralAccess)
	def saveDateAndTimePreferences() {
		// Checks that timezone is valid
		def timezone = TimeZone.getTimeZone(params.timezone)
		userPreferenceService.setTimeZone timezone.getID()

		// Validate date time format
		def datetimeFormat = TimeUtil.getDateTimeFormatType(params.datetimeFormat)
		userPreferenceService.setDateFormat datetimeFormat

		renderSuccessJson(timezone: timezone.getID(), datetimeFormat: datetimeFormat)
	}

	/**
	 * Display current logged user's Preferences with preference
	 * code (converted to comprehensive words) with their corresponding value.
	 *
	 * @return : A Map containing key as preference code and value as map'svalue.
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def editPreference() {
        UserLogin userLogin = currentPerson().userLogin

		render(
			template: "showPreference",
		    model: [
				areas: userPreferenceService.timezonePickerAreas(),
				currTimeZone: TimeUtil.defaultTimeZone,
				currDateTimeFormat: TimeUtil.getDefaultFormatType(),
				currentPersonId: securityService.currentPersonId,
				fixedPreferenceCodes: userPreferenceService.FIXED_PREFERENCE_CODES,
				preferences: userPreferenceService.preferenceListForEdit(userLogin),
				timezones: Timezone.findAll()
			]
		)
	}

	/**
	 * Display the edit form for the current logged user's date format and timezone Preferences
	 * @param N/A :
	 * @return : A Map containing key as preference code and value as map'svalue.
	 */
	@HasPermission(Permission.UserUpdateOwnAccount)
	def editTimezone () {
		def currDateTimeFormat = userPreferenceService.dateFormat ?: TimeUtil.getDefaultFormatType()

		render(template: "../project/showTimeZoneSelect",
		       model: [areas: userPreferenceService.timezonePickerAreas(), timezones: Timezone.findAll(), userPref: true,
		               currTimeZone: userPreferenceService.timeZone, currDateTimeFormat: currDateTimeFormat])
	}

	/**
	 * This action is Used to populate the CompareOrMergePerson dialog with the information regarding the persons selected
	 * @param : ids[] is array of 2 id which user want to compare or merge
	 * @return : all column list , person list and userlogin list which we are display at client side
	 */
	@HasPermission(Permission.PersonEdit)
	def compareOrMerge() {

		Map<Person, Long> personsMap = [:]
		List<UserLogin> userLogins = []
		params.list("ids[]").each {
			Person person = Person.get(it.isLong() ? Long.parseLong(it) : null)

			if (person) {
				if (person.isSystemUser()) {
					flash.message = "$person is a system account that can not be modified"
				} else {
					personsMap << [(person): person.company.id]
					UserLogin userLogin = person.getUserLogin()
					userLogins << userLogin
				}
			}
		}

		// a HashMap as 'columnList' where key is displaying label and value is property of label for Person
		def columnList =  [ 'Merge To':'',
			'First Name': 'firstName', 'Middle Name': 'middleName', 'Last Name': 'lastName',
			'Nick Name': 'nickName' , 'Active':'active','Title':'title',
			'Email':'email', 'Department':'department', 'Location':'location', 'State Prov':'stateProv',
			'Country':'country', 'Work Phone':'workPhone','Mobile Phone':'mobilePhone',
			// 'Model Score':'modelScore',
			// 'Model Score Bonus':'modelScoreBonus',
			'Person Image URL':'personImageURL',
			'KeyWords':'keyWords',
			'Tds Note':'tdsNote', 'Tds Link':'tdsLink', 'Staff Type':'staffType',
			'TravelOK':'travelOK', 'Black Out Dates':'blackOutDates', 'Roles':''
		]

		// a HashMap as 'columnList' where key is displaying label and value is property of label for UserLogin
		def loginInfoColumns = ['Username':'username', 'Active':'active', 'Created Date':'createdDate', 'Last Login':'lastLogin',
			'Last Page':'lastPage', 'Expiry Date':'expiryDate'
		]

		Map model = [personsMap:personsMap, columnList:columnList, loginInfoColumns:loginInfoColumns, userLogins:userLogins]

		render( template:"compareOrMerge", model:model)
	}

	/**
	 * This action is used to merge one or more Person accounts into another Person account which is used to eliminate duplicate
	 * person accounts. The accounts must be different and part of the same company. Merging can not be peformed on accounts that are
	 * system (e.g. Automatic Tasks).
	 * @param toId is requested id of person into which second person will get merge
	 * @param fromId is requested id of person which will be merged
	 * @return The appropriate message after merging completed or error message
	 */
	@HasPermission(Permission.PersonEdit)
	def mergePerson(PersonCO cmdObj) {
		String msg
		UserLogin byWhom = securityService.getUserLogin()

		try {
			msg = personService.processMergePersonRequest(byWhom, cmdObj, params)
			//msg = 'The merge was successful'
		} catch (InvalidParamException e) {
			msg = e.getMessage()
		} catch (DomainUpdateException e) {
			msg = e.getMessage()
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace('mergePerson failed', e, 80)
			msg = 'An error occurred and the merge was not performed'
		}

		render msg
	}

	/*
	 * Ajax service used to add the staff association to a project
	 * @params params.projectId
	 * @params params.personId
	 * @return JSON response
	 */
	@HasPermission(Permission.ProjectStaffEdit)
	def addProjectStaff() {
		String userMsg
		try {
			// Make the service call - note that the Permission is checked within the service
			UserLogin byWhom = securityService.getUserLogin()
			personService.addToProject(byWhom, params.projectId, params.personId)

			renderSuccessJson()
			return

		} catch (DomainUpdateException | InvalidParamException | InvalidRequestException | UnauthorizedException e) {
			userMsg = e.message
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("addProjectTeam()", e)
			userMsg = 'An error occurred while trying to add the person to the project'
		}

		renderErrorJson(userMsg)
	}

	/*
	 * Ajax service used to remove the staff association to a project and events and teams
	 * @params params.projectId
	 * @params params.personId
	 * @return JSON response
	 */
	@HasPermission(Permission.ProjectStaffEdit)
	def removeProjectStaff() {
		String userMsg
		try {
			// Make the service call - note that the Permission is checked within the service
			UserLogin byWhom = securityService.getUserLogin()
			personService.removeFromProject(params.projectId, params.personId)
			renderSuccessJson()
			return
		} catch (DomainUpdateException | InvalidParamException | InvalidRequestException | UnauthorizedException e) {
			userMsg = e.message
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("removeProjectStaff()", e)
			userMsg = 'An error occurred while trying to unassign the person from the project'
		}

		log.debug "removeFromProject($securityService.currentUsername, $params.projectId, $params.personId, $params.teamCode) failed - $userMsg"
		renderErrorJson(userMsg)
	}

	/*
	 * Ajax service used to add the staff association to a project for a given team code
	 * @params params.projectId
	 * @params params.personId
	 * @params params.teamCode
	 * @return JSON response
	 */
	@HasPermission(Permission.ProjectStaffEdit)
	def addProjectTeam() {
		String userMsg
		try {
			// Make the service call - note that the Permission is checked within the service
			personService.addToProjectTeam(params.projectId, params.personId, params.teamCode)
			renderSuccessJson()

		} catch (DomainUpdateException | InvalidParamException | InvalidRequestException | UnauthorizedException e) {
			userMsg = e.message
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("addProjectTeam()", e)
			renderErrorJson(['An error occurred while trying to add the person to the project', e.message])
		}

		if (userMsg) {
			renderErrorJson(userMsg)
		}
	}

	/*
	 * Ajax service used to remove the staff association to a project and events for a given team code
	 * @params params.projectId
	 * @params params.personId
	 * @params params.teamCode
	 * @return JSON response
	 */
	@HasPermission(Permission.ProjectStaffEdit)
	def removeProjectTeam() {
		String userMsg
		try {
			// Make the service call - note that the Permission is checked within the service
			personService.removeFromProjectTeam(params.projectId, params.personId, params.teamCode)
			renderSuccessJson()
		} catch (DomainUpdateException | InvalidParamException | InvalidRequestException | UnauthorizedException e) {
			userMsg = e.message
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("removeProjectTeam()", e)
			renderErrorJson(['An error occurred while trying to unassign the person from the project', e.message])
		}

		if (userMsg) {
			log.debug "removeProjectTeam($securityService.currentUsername, $params.projectId, $params.personId, $params.teamCode) failed - $userMsg"
			renderErrorJson(userMsg)
		}
	}

	/*
	 * Method to add Staff to project through Ajax Overlay
	 * @
	 */
	@HasPermission(Permission.ProjectStaffEdit)
	def addEventStaff() {
		String userMsg
		try {
			// Make the service call - note that the Permission is checked within the service
			personService.addToEvent(params.projectId, params.eventId, params.personId, params.teamCode)
			renderSuccessJson()
		} catch (DomainUpdateException | InvalidParamException | InvalidRequestException | UnauthorizedException e) {
			userMsg = e.message
		} catch (e) {
			log.error ExceptionUtil.stackTraceToString(e)
			renderErrorJson(['An error occurred while trying to add the person to the event', e.message])
		}

		if (userMsg) {
			renderErrorJson(userMsg)
		}
	}

	/*
	 * Method to add Staff to project through Ajax Overlay
	 * @
	 */
	@HasPermission(Permission.ProjectStaffEdit)
	def removeEventStaff() {
		String userMsg
		try {
			// Make the service call - note that the Permission is checked within the service
			personService.removeFromEvent(params.projectId, params.eventId, params.personId, params.teamCode)
			renderSuccessJson()
		} catch (DomainUpdateException | InvalidParamException | InvalidRequestException | UnauthorizedException e) {
			userMsg = e.message
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("removeEventStaff()", e)
			renderErrorJson(['An error occurred while trying to unassign the person from the event', e.message])
		}

		if (userMsg) {
			renderErrorJson(userMsg)
		}
	}
}
