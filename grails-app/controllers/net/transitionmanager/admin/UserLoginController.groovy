package net.transitionmanager.admin

import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.web.mapping.LinkGenerator
import groovy.sql.GroovyRowResult
import groovy.transform.CompileStatic
import net.transitionmanager.command.UserUpdatePasswordCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.UnauthorizedException
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.party.PartyType
import net.transitionmanager.person.Person
import net.transitionmanager.person.PersonService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.AuditService
import net.transitionmanager.security.Permission
import net.transitionmanager.security.RoleType
import net.transitionmanager.security.UserLogin
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import java.sql.ResultSet
import java.sql.SQLException

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class UserLoginController implements ControllerMethods, PaginationMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	AuditService             auditService
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	PartyRelationshipService partyRelationshipService
	PersonService            personService
	ProjectService           projectService
	UserPreferenceService    userPreferenceService
	int                      indefinitelyThreshold = 10
	LinkGenerator            grailsLinkGenerator

	@HasPermission(Permission.UserView)
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

	@HasPermission(Permission.UserView)
	def listJson() {
		// Get the pagination and set the user preference appropriately
		Integer maxRows = paginationMaxRowValue('rows', PREF.ASSET_LIST_SIZE, true)
		Integer currentPage = paginationPage()
		Integer rowOffset = paginationRowOffset(currentPage, maxRows)

		Long companyId
		List<Map> userLogins = []
		def filterParams = [
			username: params.username,
			fullname: params.fullname,
			roles: params.roles,
			isLocal: params.isLocal,
			company: params.company,
			lastLogin: params.lastLogin,
			dateCreated: params.dateCreated,
			expiryDate: params.expiryDate
		]

		// Deal with sorting
		String sortIndex
		if (filterParams.containsKey(params.sidx)) {
			sortIndex = params.sidx
		} else {
			throw PAGINATION_INVALID_ORDER_BY_EXCEPTION
		}

		// Deal with order
		String sortOrder = paginationSortOrder('sord')

		StringBuilder query = new StringBuilder("""SELECT * FROM (SELECT GROUP_CONCAT(role_type_id) AS roles, p.person_id AS personId, first_name AS firstName,
			u.username as username, last_name as lastName, CONCAT(CONCAT(first_name, ' '), IFNULL(last_name,'')) as fullname, pg.name AS company, u.active, u.last_login AS lastLogin, u.expiry_date AS expiryDate,
			u.created_date AS dateCreated, u.user_login_id AS userLoginId, u.is_local AS isLocal, u.locked_out_until AS locked, u.failed_login_attempts AS failedAttempts
			FROM person p
			LEFT OUTER JOIN party_role pr on p.person_id=pr.party_id
			LEFT OUTER JOIN user_login u on u.person_id=p.person_id
			LEFT OUTER JOIN party_relationship r ON r.party_relationship_type_id='ROLE_STAFF'
				AND role_type_code_from_id='ROLE_COMPANY' AND role_type_code_to_id='ROLE_STAFF' AND party_id_to_id=p.person_id
			LEFT OUTER JOIN party_group pg ON pg.party_group_id=r.party_id_from_id
			WHERE u.active = :ulActive """)

		Map queryParams = [:]
		String active = ['Y', 'N'].contains(params.activeUsers) ?  params.activeUsers : 'Y'
		queryParams.ulActive = active

		String presentDate = TimeUtil.nowGMTSQLFormat()

		if (active=='Y') {
			query.append(" AND u.expiry_date > :uPresentDate ")
		} else {
			query.append(" OR u.expiry_date < :uPresentDate ")
		}

		queryParams.uPresentDate = presentDate

		if (securityService.hasPermission(Permission.UserListAll)) {
			if (params.id && params.id != "All") {
				// If companyId is requested
				companyId = params.long('id')
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
				query.append(" AND pg.party_group_id = :pgCompanyId ")
				queryParams.pgCompanyId = companyId
			}
			query.append(" GROUP BY p.person_id ORDER BY " + sortIndex + " " + sortOrder + ") as users")

			// Handle the filtering by each column's text field
			Boolean firstWhere = true
			filterParams.each {
				if (it.value) {
					firstWhere = SqlUtil.addWhereOrAndToQuery(query, firstWhere)
					query.append("users.${it.key} LIKE :${it.key}")
					queryParams[it.key] = "%${it.value?.trim()}%"
				}
			}

			userLogins = namedParameterJdbcTemplate.query(query.toString(), queryParams, new UserLoginMapper())
		}

		// Limit the returned results to the user's page size and number
		def totalRows = userLogins.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0) {
			userLogins = userLogins[rowOffset..Math.min(rowOffset + maxRows, totalRows - 1)]
		} else {
			userLogins = []
		}

		String acceptImgTag = '<img src="' + "$grailsLinkGenerator.serverBaseURL/assets/icons/accept.png" + '"></img>'
		// If the time difference for the userLogin.lockedOutUntil is greater than 10 years, use 'Indefinitely' instead.
		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = userLogins?.collect {
			[cell: [[id: it.userLoginId, username: it.username, lockedOutUntil: it.locked, lockedOutTime: TimeUtil.hence(it.locked, indefinitelyThreshold), failedLoginAttempts: it.failedAttempts],
			'<a href="' + createLink(controller: 'userLogin', action: 'show', id: it.userLoginId) + '">' + it.username + '</a>',
			'<a href="javascript: Person.showPersonDialog(' + it.personId + ',\'generalInfoShow\')">' + it.fullname + '</a>',
			it.roles, it.company, (it.isLocal) ? (acceptImgTag) : (''), it.lastLogin, it.dateCreated, it.expiryDate], id: it.userLoginId]}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		render jsonData as JSON
	}

	/**
	 * RowMapper that maps each result from the query used in {@code UserLoginController#listJson} method.
	 * It implements one method defined in {@code RowMapper} interface.
	 * This implementation returns an instance of {@code GroovyRowResult}
	 * that is an extension of {@code Map} class.
	 */
	@CompileStatic
	private class UserLoginMapper implements RowMapper {
		GroovyRowResult mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.toRowResult()
		}
	}

	@HasPermission(Permission.UserView)
	def show() {
		UserLogin showUser = UserLogin.get(params.id)
		def companyId = params.companyId
		if (!showUser) {
			flash.message = "UserLogin not found with id $params.id"
			redirect(action: "list", params: [id: companyId])
			return
		}

		List roleList = RoleType.where {
            type == RoleType.SECURITY
        }.list()

		String cellValue = [
			id: showUser.id,
			username: showUser.username,
			lockedOutUntil: showUser.lockedOutUntil,
			lockedOutTime: TimeUtil.hence(showUser.lockedOutUntil, indefinitelyThreshold),
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
	@HasPermission(Permission.UserDelete)
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
	@HasPermission(Permission.UserEdit)
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
        def roleList = RoleType.where {
            type == RoleType.SECURITY
        }.list()
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
	@HasPermission(Permission.UserEdit)
	def update(Long id) {
		UserLogin userLogin
		String errMsg
		Map model = [companyId: params.companyId, personId: params.personId, projectId: params.projectId]

		withForm {
			try {
				userLogin = securityService.createOrUpdateUserLoginAndPermissions(params, false)
			} catch (UnauthorizedException | InvalidParamException | DomainUpdateException e) {
				errMsg = e.message
			} catch (e) {
				log.error "update() failed : ${ExceptionUtil.stackTraceToString(e)}"
				errMsg = 'An error occurred the prevented the update of the user'
			}

			if (errMsg) {
				flash.message = errMsg
				redirect(action: "edit", id: params.id, params: model)
			} else {
				flash.message = "UserLogin $userLogin updated"
				redirect(action: "show", id: userLogin.id, params: [companyId: params.companyId])
			}
		}.invalidToken {
			flash.message = INVALID_CSRF_TOKEN

			if (id) {
				// Make sure the user exists before sending to edit otherwise the CSRF message gets lost
				userLogin = UserLogin.get(params.id)
			}

			if (userLogin) {
				redirect(action: "edit", id: params.id, params: model)
			} else {
				redirect(action: "list")
			}
		}
	}

	// set the User Roles to the Person
	@HasPermission(Permission.UserEdit)
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
	@HasPermission(Permission.UserCreate)
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
        def roleList = RoleType.where {
            type == RoleType.SECURITY
        }.list()

        Person currentPerson = securityService.userLoginPerson

		[userLoginInstance: createUser, personInstance: person, companyId: params.companyId,
		 roleList: roleList, projectList: partyRelationshipService.companyProjects(currentPerson.company),
		 minPasswordLength: securityService.userLocalConfig.minPasswordLength ?: 8,
		 project: securityService.userCurrentProject, maxLevel: securityService.getMaxAssignedRole(currentPerson).level]
	}

	/*
	 *  Save the User details and set the user roles for Person
	 */
	@HasPermission(Permission.UserCreate)
	def save() {
		UserLogin newUserLogin
		String errMsg

		withForm {
			try {
				newUserLogin = securityService.createOrUpdateUserLoginAndPermissions(params, true)
			} catch (UnauthorizedException | InvalidParamException | DomainUpdateException e) {
				errMsg = e.message
			} catch (e) {
				log.error "save() failed : ${ExceptionUtil.stackTraceToString(e)}"
				errMsg = 'An error occurred that prevents creates a user'
			}

			if (errMsg) {
				flash.message = errMsg
				redirect(action: "create", id: params.personId, params: [companyId: params.companyId])
			} else {
				flash.message = "UserLogin $newUserLogin created"
				redirect(action: "show", id: newUserLogin.id, params: [companyId: params.companyId])
			}
		}.invalidToken {
			flash.message = INVALID_CSRF_TOKEN
			redirect(action: "create", id: params.personId, params: [companyId: params.companyId])
		}

	}

	/**
	 * The 1st phase of user changing password during the forced password change process
	 */
	@HasPermission(Permission.UserResetOwnPassword)
	def changePassword() {
		[userLoginInstance: securityService.userLogin,
		 minPasswordLength: securityService.getUserLocalConfig().minPasswordLength ?: 8]
	}

	/**
	 * The 2nd phase (last) of user changing password during the forced password change process.
	 * The new password will be saved during this call.
	 */
	@HasPermission(Permission.UserResetOwnPassword)
	def updatePassword() {
		UserUpdatePasswordCommand command = populateCommandObject(UserUpdatePasswordCommand)
		UserLogin userLogin = securityService.userLogin
		try {
			securityService.updatePassword(userLogin, command)
			flash.message = "Password was successfully updated"
			redirect(controller: 'project', action: 'show', params: [userLoginInstance: userLogin])

		}catch(e) {
			flash.message = e.message
			redirect(action: 'changePassword', params: [userLoginInstance: userLogin])
		}

	}

	/**
	 * Triggers the password reset on a selected account.
	 */
	@HasPermission(Permission.UserResetPassword)
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
