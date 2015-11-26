import org.apache.shiro.SecurityUtils
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import java.text.SimpleDateFormat
import grails.converters.JSON
import com.tdsops.tm.enums.domain.PasswordResetType

class UserLoginController {
	
	def partyRelationshipService
	def userPreferenceService
	def securityService
	def projectService
	def jdbcTemplate
	def controllerService
	def auditService
	def personService

	def index() { redirect(action:"list",params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list() {
		if (!controllerService.checkPermission(this, "UserLoginView"))
			return

		def listJsonUrl
		
		def companyId = params.companyId ?: 'All'
		if(companyId && companyId != 'All'){
			def map = [controller:'userLogin', action:'listJson', id:"${companyId}"]
			listJsonUrl = HtmlUtil.createLink(map)
		} else {
			def map = [controller:'userLogin', action:'listJson']
			listJsonUrl = HtmlUtil.createLink(map)+'/All'
		}
		
		if(params.activeUsers){
			 session.setAttribute("InActive", params.activeUsers)
		}
		def project = securityService.getUserCurrentProject()
		def active = params.activeUsers ? params.activeUsers : session.getAttribute("InActive")
		if(!active){
			active = 'Y'
		}
		
		def partyGroupList = PartyGroup.findAllByPartyType( PartyType.read("COMPANY")).sort{it.name}
		
		return [companyId:companyId ,partyGroupList:partyGroupList,listJsonUrl:listJsonUrl]
	}
	
	def listJson() {
		if (!controllerService.checkPermission(this, "UserLoginView"))
			return

		def sortIndex = params.sidx ?: 'username'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows?:'25')
		def currentPage = Integer.valueOf(params.page?:'1')
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def companyId
		def userLoginInstanceList
		def userLogin = securityService.getUserLogin()
		def filterParams = ['username':params.username, 'fullname':params.fullname, 'roles':params.roles, 'company':params.company,
			'lastLogin':params.lastLogin, 'dateCreated':params.dateCreated, 'expiryDate':params.expiryDate]
		
		// Validate that the user is sorting by a valid column
		if ( ! sortIndex in filterParams)
			sortIndex = 'username'
			
		def presentDate = TimeUtil.nowGMTSQLFormat()
		
		def active = params.activeUsers ? params.activeUsers : session.getAttribute("InActive")
		if (!active) {
			active = 'Y'
		}

		// Search valid roles to display
		def systemRoles = securityService.getSecurityRoleTypes()
		def systemRolesList = []
		systemRoles.each { sr ->
			systemRolesList << "'${sr.id}'"
		}

		def query = new StringBuffer("""SELECT * FROM ( SELECT GROUP_CONCAT(role_type_id) AS roles, p.person_id AS personId, first_name AS firstName,
			u.username as username, last_name as lastName, CONCAT(CONCAT(first_name, ' '), IFNULL(last_name,'')) as fullname, pg.name AS company, u.active, u.last_login AS lastLogin, u.expiry_date AS expiryDate, 
			u.created_date AS dateCreated, u.user_login_id AS userLoginId, u.is_local AS isLocal, u.locked_out_until AS locked, u.failed_login_attempts AS failedAttempts
			FROM party_role pr 
			LEFT OUTER JOIN person p on p.person_id=pr.party_id 
			LEFT OUTER JOIN user_login u on u.person_id=p.person_id 
			LEFT OUTER JOIN party_relationship r ON r.party_relationship_type_id='STAFF' 
				AND role_type_code_from_id='COMPANY' AND role_type_code_to_id='STAFF' AND party_id_to_id=pr.party_id 
			LEFT OUTER JOIN party_group pg ON pg.party_group_id=r.party_id_from_id 
			WHERE role_type_id in (${systemRolesList.join(", ")}) AND u.active = '${active}'""")
		if (active=='Y')
			query.append(" AND u.expiry_date > '${presentDate}' ")
		else
			query.append(" OR u.expiry_date < '${presentDate}' ")
		if (RolePermissions.hasPermission("ShowAllUsers")) {
			if (params.id && params.id != "All") {
				// If companyId is requested
				companyId = params.id
			}
			if (!companyId && params.id != "All") {
				// Still if no companyId found trying to get companyId from the session
				companyId = session.getAttribute("PARTYGROUP")?.PARTYGROUP
				if (!companyId) {
					// Still if no luck setting companyId as logged-in user's companyId .
					def person = userLogin.person
					companyId = partyRelationshipService.getStaffCompany(person)?.id
				}
			}
			if (companyId) {
				query.append(" AND pg.party_group_id = $companyId ")
			}
			//query.append(" GROUP BY pr.party_id ORDER BY pr.role_type_id, pg.name, first_name, last_name ) as users")
			query.append(" GROUP BY pr.party_id ORDER BY " + sortIndex + " " + sortOrder + ") as users")
		
			// Handle the filtering by each column's text field
			def firstWhere = true
			filterParams.each {
				if (it.getValue())
					if (firstWhere) {
						query.append(" WHERE users.${it.getKey()} LIKE '%${it.getValue()}%'")
						firstWhere = false
					} else {
						query.append(" AND users.${it.getKey()} LIKE '%${it.getValue()}%'")
					}
			}
			
			userLoginInstanceList = jdbcTemplate.queryForList(query.toString())
		} else {
			userLoginInstanceList = []
		}
		
		// Limit the returned results to the user's page size and number
		def totalRows = userLoginInstanceList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0)
			userLoginInstanceList = userLoginInstanceList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			userLoginInstanceList = []
		
		def map = [controller:'userLogin', action:'listJson', id:"${params.companyId}"]
		def listJsonUrl = HtmlUtil.createLink(map)
		def acceptIconUrl = HtmlUtil.resource([dir: 'icons', file: 'accept.png', absolute: true])
		def acceptImgTag = '<img src="' + acceptIconUrl + '"></img>'
		
		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = userLoginInstanceList?.collect {
			[ cell: [ [id:it.userLoginId, username:it.username, lockedOutUntil:it.locked, lockedOutTime:TimeUtil.ago(TimeUtil.nowGMT(), it.locked), failedLoginAttempts:it.failedAttempts], 
			'<a href="'+HtmlUtil.createLink([controller:'userLogin', action:'show', id:"${it.userLoginId}"])+'">'+it.username+'</a>',
			'<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.fullname+'</a>', 
			it.roles, it.company, (it.isLocal) ? (acceptImgTag) : (''), it.lastLogin, it.dateCreated, it.expiryDate ], id: it.userLoginId ]}
			
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		render jsonData as JSON
	}
	
	def show() {
		if (!controllerService.checkPermission(this, "UserLoginView"))
			return

		def userLoginInstance = UserLogin.get( params.id )
		def companyId = params.companyId
		if(!userLoginInstance) {
			flash.message = "UserLogin not found with id ${params.id}"
			redirect( action:"list", params:[ id:companyId ] )
		} else { 
			def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
			def assignedRoles = userPreferenceService.getAssignedRoles( userLoginInstance.person )
			def canResetByAdmin = userLoginInstance.canResetPasswordByAdmin(securityService.getUserLoginPerson())
			def cellValue = [id:userLoginInstance.id, username:userLoginInstance.username, lockedOutUntil:userLoginInstance.lockedOutUntil, lockedOutTime:TimeUtil.ago(TimeUtil.nowGMT(), userLoginInstance.lockedOutUntil), failedLoginAttempts:userLoginInstance.failedLoginAttempts] as JSON
			return [ userLoginInstance : userLoginInstance, companyId:companyId, roleList:roleList, assignedRoles:assignedRoles, cellValue:cellValue, canResetPasswordByAdmin: canResetByAdmin ] 
		}
	}

	def delete() {
		if (!controllerService.checkPermission(this, "UserLoginDelete"))
			return

		def userLoginInstance = UserLogin.get( params.id )
		def companyId = params.companyId
		if(userLoginInstance) {
			userPreferenceService.deleteSecurityRoles(userLoginInstance.person);
			userLoginInstance.delete(flush:true)
			flash.message = "UserLogin ${userLoginInstance} deleted"
			redirect( action:"list", params:[ id:companyId ] )
		}
		else {
			flash.message = "UserLogin not found with id ${params.id}"
			redirect( action:"list", params:[ id:companyId ] )
		}
	}
	/*
	 *  Return userdetails and roles to Edit form
	 */
	def edit() {
		if (!controllerService.checkPermission(this, "EditUserLogin"))
			return

		def userLoginInstance = UserLogin.get( params.id )
		def companyId = params.companyId
		def minPasswordLength = securityService.getUserLocalConfig().minPasswordLength ?: 8
		
		if(!userLoginInstance) {
			flash.message = "UserLogin not found with id ${params.id}"
			redirect( action:"list", params:[ id:companyId ] )
		} else {
			def currentUser = securityService.getUserLogin()
			def person = userLoginInstance.person
			def availableRoles = userPreferenceService.getAvailableRoles( person )
			def assignedRoles = userPreferenceService.getAssignedRoles( person )
			def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
			def projectList = personService.getAvailableProjects(person)
			def projectId = userPreferenceService.getPreferenceByUserAndCode(userLoginInstance, "CURR_PROJ")
			def maxLevel = securityService.getMaxAssignedRole(currentUser.person).level
			def isCurrentUserLogin = (currentUser.id == userLoginInstance.id)
			return [ userLoginInstance : userLoginInstance, availableRoles:availableRoles, assignedRoles:assignedRoles, companyId:companyId, roleList:roleList,
					projectList:projectList,  projectId:projectId, minPasswordLength:minPasswordLength, maxLevel: maxLevel, isCurrentUserLogin: isCurrentUserLogin ]
		}
	}

	/*
	 * update user details and set the User Roles to the Person
	 */
	def update() {
		UserLogin byWhom = securityService.getUserLogin()
		UserLogin userLogin
		String tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		String errMsg
		try {
			userLogin = securityService.createOrUpdateUserLoginAndPermissions(params, byWhom, tzId, false)
		} catch (UnauthorizedException e) {
			errMsg = e.getMessage()
		} catch (InvalidParamException e) {
			errMsg = e.getMessage()
		} catch (DomainUpdateException e) {
			errMsg = e.getMessage()
		} catch (e) {
			log.error "update() failed : " + ExceptionUtil.stackTraceToString(e)
			errMsg = 'An error occurred the prevented the update of the user'
		}

		if (errMsg) {
			flash.message = errMsg
			Map model = [ companyId:params.companyId, personId: params.personId, projectId: params.projectId]
			redirect( action:"edit", id:params.id, params:model)
			/*
			Person person = userLogin?.person
			List availableRoles = userPreferenceService.getAvailableRoles( person )
			List assignedRoles = userPreferenceService.getAssignedRoles( person )
			render( view: 'edit', model: [
				userLogin:userLogin,
				vailableRoles:availableRoles, 
				assignedRoles:assignedRoles, 
				companyId: params.companyId 
			])
			*/
		} else {
			flash.message = "UserLogin ${userLogin} updated"
			redirect( action:"show", id:userLogin.id, params:[ companyId:params.companyId ] )
		}
	}
	
	// set the User Roles to the Person
	def addRoles() {
			def assignedRoles = params.assignedRoleId.split(',')
			def person = params.person
			def actionType = params.actionType
			if(actionType != "remove"){
				userPreferenceService.setUserRoles(assignedRoles, person)
			} else {
				userPreferenceService.removeUserRoles(assignedRoles, person)
			}
		render true
	}
	
	// return userlogin details to create form
	def create() {
		if (!controllerService.checkPermission(this, "CreateUserLogin"))
			return

		def personId = params.id
		def companyId = params.companyId
		def person
		if ( personId != null ) {
			person = Person.findById( personId )
			if (person.lastName == null) {
				person.lastName = ""
			}
		}
		
		def now = TimeUtil.nowGMT()
		
		def userLoginInstance = new UserLogin()
		userLoginInstance.properties = params
		def expiryDate = new Date(now.getTime() + 7776000000) // 3 Months
		userLoginInstance.expiryDate = expiryDate
		def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.level desc ")
		def project = securityService.getUserCurrentProject()

		def currentUser = securityService.getUserLogin()
		def projectList = personService.getAvailableProjects(person?person:currentUser.person)
		def minPasswordLength = securityService.getUserLocalConfig().minPasswordLength ?: 8
		def maxLevel = securityService.getMaxAssignedRole(currentUser.person).level 
		return ['userLoginInstance':userLoginInstance, personInstance:person, companyId:companyId, roleList:roleList, projectList:projectList,
			 	project:project, minPasswordLength:minPasswordLength, maxLevel: maxLevel]
	}
	/*
	 *  Save the User details and set the user roles for Person
	 */
	def save() {
		if (!controllerService.checkPermission(this, "CreateUserLogin"))
			return

		UserLogin byWhom = securityService.getUserLogin()
		UserLogin userLogin
		String tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		String errMsg

		try {
			userLogin = securityService.createOrUpdateUserLoginAndPermissions(params, byWhom, tzId, true)
		} catch (UnauthorizedException e) {
			errMsg = e.getMessage()
		} catch (InvalidParamException e) {
			errMsg = e.getMessage()
		} catch (DomainUpdateException e) {
			errMsg = e.getMessage()
		} catch (e) {
			log.error "save() failed : " + ExceptionUtil.stackTraceToString(e)
			errMsg = 'An error occurred that prevents creates a user'
		}

		if (errMsg) {
			flash.message = errMsg
			Map model = [ companyId:params.companyId ]
			redirect( action:"create", id:params.personId, params:model)
		} else {
			flash.message = "UserLogin ${userLogin} created"
			redirect( action:"show", id:userLogin.id, params:[ companyId:params.companyId ] )
		}

	}

	/*======================================================
	 *  Update recent page load time into userLogin
	 *=====================================================*/
	def updateLastPageLoad() {
		def principal = SecurityUtils.subject?.principal
		if( principal ){
			def userLogin = UserLogin.findByUsername( principal )
			if(userLogin){
				userLogin.lastPage = TimeUtil.nowGMT()
				userLogin.save(flush:true)
				session.REDIRECT_URL = params.url	
			}
		}
		render "SUCCESS"
	}

	/**
	 * The 1st phase of user changing password during the forced password change process
	 */
	def changePassword() {
		def principal = SecurityUtils.subject?.principal
		def userLoginInstance = UserLogin.findByUsername(principal)
		def minPasswordLength = securityService.getUserLocalConfig().minPasswordLength ?: 8
		render(view:'changePassword',model:[ userLoginInstance:userLoginInstance, minPasswordLength:minPasswordLength])
		return [ userLoginInstance : userLoginInstance]
	}

	/**
	 * The 2nd phase (last) of user changing password during the forced password change process.
	 * The new password will be saved during this call.
	 */
	def updatePassword() {
		def subject = SecurityUtils.subject
		def principal = subject.principal
		def userLoginInstance = UserLogin.findByUsername(principal)
		String msg
		try {
			while (true) {
				if (! userLoginInstance) {
					msg = 'Failed to load your user account'
					break
				}

				try {
					// See if the user account is properly configured to a state that they're allowed to change their password
					securityService.validateAllowedToChangePassword(userLoginInstance)
				} catch (e) {
					def emsg = e.getMessage()
					msg = "You are not allowed to change your password at this time."
					break
				}

				//
				// Made it throught the guantlet of password requirements so lets update the password
				//
				securityService.setUserLoginPassword(userLoginInstance, params.password)

				userLoginInstance.forcePasswordChange = 'N'
				if (!userLoginInstance.validate() || !userLoginInstance.save()) {
					log.warn "updatePassword() failed to update user password for $userLoginInstance : " + GormUtil.allErrorsString(userLoginInstance)
					msg = 'An error occured while trying to save your password'
					break
				} else {
					auditService.saveUserAudit(UserAuditBuilder.userLoginPasswordChanged(userLoginInstance))
				}

				flash.message = "Password was successfully updated"
				redirect(controller:'project', action:'show', params:[ userLoginInstance:userLoginInstance ])
				break
			}
		} catch (InvalidParamException e) {
			msg = e.getMessage()
		} catch (DomainUpdateException e) {
			msg = e.getMessage()
		} catch (e) {
			log.warn "updateAccount() failed : ${ExceptionUtil.stackTraceToString(e)}"
			msg = 'An error occurred during the update process'
		}

		if (msg) {
			flash.message = msg
			redirect(action:'changePassword', params:[ userLoginInstance:userLoginInstance ])
		}
	}

	/**
	 * This method triggers the password reset on a selected account.
	 */
	def sendPasswordReset = {
		def userLogin = UserLogin.findById(params.id)
		if(userLogin.canResetPasswordByAdmin(securityService.getUserLoginPerson())){
			def emailParams = [sysAdminEmail : securityService.getUserLogin().person.email, username: userLogin.username]
			securityService.sendResetPasswordEmail(userLogin.person.email, request.getRemoteAddr(), PasswordResetType.ADMIN_RESET, emailParams)
		}else{

		}
		def msg = [success: true]
		render msg as JSON
	}
}
