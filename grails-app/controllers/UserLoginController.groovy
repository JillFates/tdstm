import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.AuditService
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UnauthorizedException
import net.transitionmanager.service.UserPreferenceService
import org.springframework.jdbc.core.JdbcTemplate
import grails.plugin.springsecurity.annotation.Secured

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class UserLoginController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	AuditService auditService
	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProjectService projectService
	SecurityService securityService
	UserPreferenceService userPreferenceService

	@HasPermission('UserView')
	def list() {

		def listJsonUrl

		def companyId = params.companyId ?: 'All'
		if (companyId != 'All') {
			listJsonUrl = createLink(controller: 'userLogin', action: 'listJson', id: companyId)
		} else {
			listJsonUrl = createLink(controller: 'userLogin', action: 'listJson') + '/All'
		}

		if (params.activeUsers) {
			 session.setAttribute('InActive', params.activeUsers)
		}

		def partyGroupList = PartyGroup.findAllByPartyType(PartyType.read("COMPANY")).sort { a, b -> a.name.compareToIgnoreCase b.name }

		[companyId: companyId, partyGroupList: partyGroupList,listJsonUrl: listJsonUrl]
	}

	@HasPermission('UserView')
	def listJson() {
		String sortIndex = params.sidx ?: 'username'
		String sortOrder  = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows

		def companyId
		List<UserLogin> userLogins
		def filterParams = [username: params.username, fullname: params.fullname, roles: params.roles, company: params.company,
		                    lastLogin: params.lastLogin, dateCreated: params.dateCreated, expiryDate: params.expiryDate]

		// Validate that the user is sorting by a valid column
		if (!sortIndex in filterParams) {
			sortIndex = 'username'
		}

		def presentDate = TimeUtil.nowGMTSQLFormat()

		def active = params.activeUsers ?: session.getAttribute("InActive")
		if (!active) {
			active = 'Y'
		}

		// Search valid roles to display
		def systemRolesList = []
		for (RoleType sr in securityService.securityRoleTypes) {
			systemRolesList << "'$sr.id'"
		}

		def query = new StringBuffer("""SELECT * FROM (SELECT GROUP_CONCAT(role_type_id) AS roles, p.person_id AS personId, first_name AS firstName,
			u.username as username, last_name as lastName, CONCAT(CONCAT(first_name, ' '), IFNULL(last_name,'')) as fullname, pg.name AS company, u.active, u.last_login AS lastLogin, u.expiry_date AS expiryDate,
			u.created_date AS dateCreated, u.user_login_id AS userLoginId, u.is_local AS isLocal, u.locked_out_until AS locked, u.failed_login_attempts AS failedAttempts
			FROM party_role pr
			LEFT OUTER JOIN person p on p.person_id=pr.party_id
			LEFT OUTER JOIN user_login u on u.person_id=p.person_id
			LEFT OUTER JOIN party_relationship r ON r.party_relationship_type_id='STAFF'
				AND role_type_code_from_id='COMPANY' AND role_type_code_to_id='STAFF' AND party_id_to_id=pr.party_id
			LEFT OUTER JOIN party_group pg ON pg.party_group_id=r.party_id_from_id
			WHERE role_type_id in (${systemRolesList.join(", ")}) AND u.active = '$active'""")
		if (active=='Y')
			query.append(" AND u.expiry_date > '$presentDate' ")
		else
			query.append(" OR u.expiry_date < '$presentDate' ")
		if (securityService.hasPermission("UserListAll")) {
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
				query.append(" AND pg.party_group_id = $companyId ")
			}
			//query.append(" GROUP BY pr.party_id ORDER BY pr.role_type_id, pg.name, first_name, last_name) as users")
			query.append(" GROUP BY pr.party_id ORDER BY " + sortIndex + " " + sortOrder + ") as users")

			// Handle the filtering by each column's text field
			def firstWhere = true
			for (it in filterParams) {
				if (it.value) {
					if (firstWhere) {
						query.append(" WHERE users.$it.key LIKE '%$it.value%'")
						firstWhere = false
					}
					else {
						query.append(" AND users.$it.key LIKE '%$it.value%'")
					}
				}
			}
			log.debug "listJson() Query for User Login List: $query"
			userLogins = jdbcTemplate.queryForList(query.toString())
		} else {
			userLogins = []
		}

		// Limit the returned results to the user's page size and number
		def totalRows = userLogins.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0)
			userLogins = userLogins[rowOffset..Math.min(rowOffset + maxRows, totalRows - 1)]
		else
			userLogins = []

		String acceptImgTag = '<img src="' + resource(dir: 'icons', file: 'accept.png', absolute: false) + '"></img>'

		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = userLogins?.collect {
			[cell: [[id: it.userLoginId, username: it.username, lockedOutUntil: it.locked, lockedOutTime: TimeUtil.ago(TimeUtil.nowGMT(), it.locked), failedLoginAttempts: it.failedAttempts],
			'<a href="' + createLink(controller: 'userLogin', action: 'show', id: it.userLoginId) + '">' + it.username + '</a>',
			'<a href="javascript: Person.showPersonDialog(' + it.personId + ',\'generalInfoShow\')">' + it.fullname + '</a>',
			it.roles, it.company, (it.isLocal) ? (acceptImgTag) : (''), it.lastLogin, it.dateCreated, it.expiryDate], id: it.userLoginId]}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		render jsonData as JSON
	}

	@HasPermission('UserView')
	def show() {
		UserLogin showUser = UserLogin.get(params.id)
		def companyId = params.companyId
		if (!showUser) {
			flash.message = "UserLogin not found with id $params.id"
			redirect(action: "list", params: [id: companyId])
			return
		}

		// TODO : JPM 10/2016 : Fix team list to drop the description parsing - can where on the type now
		List roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
		String cellValue = [
			id: showUser.id,
			username: showUser.username,
			lockedOutUntil: showUser.lockedOutUntil,
			lockedOutTime: TimeUtil.ago(TimeUtil.nowGMT(), showUser.lockedOutUntil),
			failedLoginAttempts: showUser.failedLoginAttempts
		] as JSON

		[userLoginInstance: showUser, companyId: companyId, roleList: roleList,
		 assignedRoles: securityService.getAssignedRoles(showUser.person),
		 cellValue: cellValue, canResetPasswordByAdmin: showUser.canResetPasswordByAdmin()]
	}

	/**
	 * Used to delete a UserLogin and remove and references to the User
	 * @param id - the ID of the user login to be removed
	 * @param companyId - the ID of the company that is selected for filtering the User List
	 */
	@HasPermission('UserDelete')
	def delete() {
		def companyId = params.companyId
		UserLogin userToDelete = UserLogin.get(params.id)
		if (!userToDelete) {
			flash.message = 'UserLogin was not found'
		} else {
			try {
				securityService.deleteUserLogin(userToDelete)
				flash.message = "UserLogin ${userToDelete} deleted"
			} catch (e) {
				log.error ExceptionUtil.stackTraceToString('delete() failed ', e)
				flash.message = "An error occurred while attempting to delete the user"
			}
		}
		redirect(action: "list", params: [id: companyId])
	}

	/*
	 *  Return userdetails and roles to Edit form
	 */
	@HasPermission('UserEdit')
	def edit() {
		UserLogin editUser = UserLogin.get(params.id)
		def companyId = params.companyId

		if (!editUser) {
			flash.message = "UserLogin not found with id $params.id"
			redirect(action: "list", params: [id: companyId])
			return
		}

		def person = editUser.person
		def availableRoles = securityService.getAvailableRoles(person)
		def assignedRoles = securityService.getAssignedRoles(person)
		def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
		def projectList = personService.getAvailableProjects(person, null, false, new Date() - 30)
		def projectId = userPreferenceService.getPreference(editUser, PREF.CURR_PROJ)
		def maxLevel = securityService.getMaxAssignedRole(securityService.loadCurrentPerson()).level
		def isCurrentUserLogin = securityService.currentUserLoginId == editUser.id
		def minPasswordLength = securityService.userLocalConfig.minPasswordLength ?: 8

		[userLoginInstance: editUser, availableRoles: availableRoles, assignedRoles: assignedRoles,
		 companyId: companyId, roleList: roleList, projectList: projectList,  projectId: projectId,
		 minPasswordLength: minPasswordLength, maxLevel: maxLevel, isCurrentUserLogin: isCurrentUserLogin]
	}

	/*
	 * update user details and set the User Roles to the Person
	 */
	@HasPermission('UserEdit')
	def update() {
		UserLogin userLogin
		String errMsg
		try {
			userLogin = securityService.createOrUpdateUserLoginAndPermissions(params, false)
		}
		catch (UnauthorizedException | InvalidParamException | DomainUpdateException e) {
			errMsg = e.message
		}
		catch (e) {
			log.error "update() failed : ${ExceptionUtil.stackTraceToString(e)}"
			errMsg = 'An error occurred the prevented the update of the user'
		}

		if (errMsg) {
			flash.message = errMsg
			Map model = [companyId: params.companyId, personId: params.personId, projectId: params.projectId]
			redirect(action: "edit", id: params.id, params: model)
			/*
			Person person = userLogin?.person
			List availableRoles = securityService.getAvailableRoles(person)
			List assignedRoles = securityService.getAssignedRoles(person)
			render(view: 'edit', model: [
				userLogin: userLogin,
				vailableRoles: availableRoles,
				assignedRoles: assignedRoles,
				companyId: params.companyId
			])
			*/
		}
		else {
			flash.message = "UserLogin $userLogin updated"
			redirect(action: "show", id: userLogin.id, params: [companyId: params.companyId])
		}
	}

	// set the User Roles to the Person
	@HasPermission('UserEdit')
	def addRoles() {
		List<String> assignedRoles = params.assignedRoleId.split(',') as List
		if (params.actionType != "remove") {
			securityService.setUserRoles(assignedRoles, params.long('person'))
		}
		else {
			securityService.removeUserRoles(assignedRoles, params.person)
		}
		render true
	}

	// return userlogin details to create form
	@HasPermission('UserCreate')
	def create() {
		Person person
		if (params.id) {
			person = Person.get(params.id)
			if (person.lastName == null) {
				person.lastName = ""
			}
		}

		UserLogin createUser = new UserLogin(params)
		createUser.expiryDate = new Date(System.currentTimeMillis() + 7776000000) // 3 Months
		def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.level desc ")
		Person currentPerson = securityService.userLoginPerson

		[userLoginInstance: createUser, personInstance: person, companyId: params.companyId,
		 roleList: roleList, projectList: partyRelationshipService.companyProjects(currentPerson.company),
		 minPasswordLength: securityService.userLocalConfig.minPasswordLength ?: 8,
		 project: securityService.userCurrentProject, maxLevel: securityService.getMaxAssignedRole(currentPerson).level]
	}

	/*
	 *  Save the User details and set the user roles for Person
	 */
	@HasPermission('UserCreate')
	def save() {
		UserLogin newUserLogin
		String errMsg

		try {
			newUserLogin = securityService.createOrUpdateUserLoginAndPermissions(params, true)
		}
		catch (UnauthorizedException | InvalidParamException | DomainUpdateException e) {
			errMsg = e.message
		}
		catch (e) {
			log.error "save() failed : ${ExceptionUtil.stackTraceToString(e)}"
			errMsg = 'An error occurred that prevents creates a user'
		}

		if (errMsg) {
			flash.message = errMsg
			redirect(action: "create", id: params.personId, params: [companyId: params.companyId])
		}
		else {
			flash.message = "UserLogin $newUserLogin created"
			redirect(action: "show", id: newUserLogin.id, params: [companyId: params.companyId])
		}

	}

	/*======================================================
	 *  Update recent page load time into userLogin
	 *=====================================================*/
	@HasPermission('UserGeneralAccess')
	def updateLastPageLoad() {
		UserLogin userLogin = securityService.userLogin
		if (userLogin) {
			userLogin.lastPage = TimeUtil.nowGMT()
			userLogin.save(flush: true)
			session.REDIRECT_URL = params.url
		}
		render "SUCCESS"
	}

	/**
	 * The 1st phase of user changing password during the forced password change process
	 */
	@HasPermission('UserResetOwnPassword')
	def changePassword() {
		[userLoginInstance: securityService.userLogin,
		 minPasswordLength: securityService.getUserLocalConfig().minPasswordLength ?: 8]
	}

	/**
	 * The 2nd phase (last) of user changing password during the forced password change process.
	 * The new password will be saved during this call.
	 */
	@HasPermission('UserResetOwnPassword')
	def updatePassword() {
		UserLogin userLogin = securityService.userLogin
		String msg
		try {
			while (true) {
				if (!userLogin) {
					msg = 'Failed to load your user account'
					break
				}

				try {
					// See if the user account is properly configured to a state that they're allowed to change their password
					securityService.validateAllowedToChangePassword(userLogin)
				}
				catch (e) {
					msg = "You are not allowed to change your password at this time."
					break
				}

				//
				// Made it throught the guantlet of password requirements so lets update the password
				//
				securityService.setUserLoginPassword(userLogin, params.password)

				if (!userLogin.save()) {
					log.warn "updatePassword() failed to update user password for $userLogin : ${GormUtil.allErrorsString(userLogin)}"
					msg = 'An error occured while trying to save your password'
					break
				}
				else {
					auditService.saveUserAudit(UserAuditBuilder.userLoginPasswordChanged())
				}

				flash.message = "Password was successfully updated"
				redirect(controller: 'project', action: 'show', params: [userLoginInstance: userLogin])
				break
			}
		}
		catch (InvalidParamException | DomainUpdateException e) {
			msg = e.message
		}
		catch (e) {
			log.warn "updateAccount() failed : ${ExceptionUtil.stackTraceToString(e)}"
			msg = 'An error occurred during the update process'
		}

		if (msg) {
			flash.message = msg
			redirect(action: 'changePassword', params: [userLoginInstance: userLogin])
		}
	}

	/**
	 * Triggers the password reset on a selected account.
	 */
	@HasPermission('UserResetPassword')
	def sendPasswordReset() {
		UserLogin userLogin = UserLogin.get(params.id)
		if (userLogin.canResetPasswordByAdmin()) {
			def emailParams = [sysAdminEmail: securityService.userLoginPerson.email, username: userLogin.username]
			securityService.sendResetPasswordEmail(userLogin.person.email, request.getRemoteAddr(),
					PasswordResetType.ADMIN_RESET, emailParams)
		}
		def msg = [success: true]
		render msg as JSON
	}
}
