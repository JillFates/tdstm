/**
 * The SecurityService class provides methods to manage User Roles and Permissions, etc.
 */

import javax.servlet.http.HttpSession
import org.apache.shiro.SecurityUtils

import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestContextHolder

import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.SecurityConfigParser
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.tm.enums.domain.RoleTypeGroup
import com.tdsops.tm.enums.domain.PasswordResetStatus
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdsops.tm.enums.domain.EmailDispatchOrigin
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.GormUtil
import com.tdsops.common.security.SecurityUtil
import net.transitionmanager.PasswordHistory
import net.transitionmanager.PasswordReset
import net.transitionmanager.EmailDispatch
import grails.converters.JSON
import groovy.time.TimeCategory

class SecurityService implements InitializingBean {
	
	static transactional = true

	// IoC
	def jdbcTemplate
	def auditService
	def emailDispatchService
	def serviceHelperService

	def ldapConfigMap = [:]
//	def loginConfigMap = [:]
	def loginConfigMap = [usernamePlaceholder:'enter your freaking username', authorityPrompt:'prompt', authorityLabel:'DOMAIN!', authorityName:'TDS']
	def userLocalConfigMap = [:]

	
	/**
	 * This is a post initialization method to allow late configuration settings to occur
	 */
	public void afterPropertiesSet() throws Exception {

		def config = serviceHelperService.getApplicationConfig()

		println "Parsing Security Login setting options"
		loginConfigMap = SecurityConfigParser.parseLoginSettings(config)

		println "Parsing Security LDAP setting options"
		ldapConfigMap = SecurityConfigParser.parseLDAPSettings(config)

		println "Parsing Local User Configuration Settings options"
		userLocalConfigMap = SecurityConfigParser.parseLocalUserSettings(config)

		println "Validating Security LDAP company/party setting"
		validateLDAPCompanyProjectSettings(ldapConfigMap)
	}

	/**
	 * Used to validate that each of the domains reference a valid company and project
	 * @throws com.tdsops.common.exceptions.ConfigurationException
	 */
	private void validateLDAPCompanyProjectSettings(map) {
		// println "*****validateLDAPCompanyProjectSettings() map====$map"
		if (map.enabled) {
			if (! map.domains || ! (map.domains instanceof Map)) {
				throw new ConfigurationException("Security setting 'domains' is undefined or invalid")
			}

			// TODO : JPM 12/2014 - need to correct validateLDAPCompanyProjectSettings since read has not yet been injected on Domain objects yet
			/*
			map.domains.each { k,d ->
				Project project = Project.read(d.defaultProject)
				if (! project)
					throw new ConfigurationException("Security settings has invalid '${k}.defaultProject' id")

				PartyGroup pg = PartyGroup.read(d.company)
				if (project.client != d.company)
					throw new ConfigurationException("Security settings has invalid '${k}.company' id")

				// TODO : Add logic to validate that the company IS a compnany and that there's some relationship between the Project and Company
			}
			*/
		}
	}
	
	/** 
	 * Returns the configuration map for the login form that is derived from the tdstm-config.groovy settings
	 * @return a map of all of the settings for the login
	 *    String authorityPrompt - select:show select, prompt: prompt for autority, na: do nothing for authority
	 *	  List selectOptions - list of authorities/domains labels
	 */
	public Map getLoginConfig() {
		return this.loginConfigMap
	}

	/** 
	 * Returns the configuration map for the LDAP setting derived from the tdstm-config.groovy settings
	 * @return a map of all of the settings for the login
	 *    String authorityPrompt - select:show select, prompt: prompt for autority, na: do nothing for authority
	 *	  List selectOptions - list of authorities/domains labels
	 */
	public Map getLDAPConfig() {
		return this.ldapConfigMap
	}

	/** 
	 * Returns the configuration map for the User Local Settings derived from the tdstm-config.groovy settings
	 * @return a map of all of the settings for the user local settions
	 */
	public Map getUserLocalConfig() {
		return this.userLocalConfigMap
	}

	/**
	 * Used to determine if the current user has a specified role
	 * @param	role	a String representing a role
	 * @return 	bool	true or false indicating if the user has the role
	 * @Usage  if ( securityService.hasRole( 'PROJ_MGR' ) ...
	 */
	def hasRole( role ) {
		return SecurityUtils.subject.hasRole( role )
	}
	
	/**
	 * Used to determine if the current user has a role within an array of roles
	 * @param	roles	a array of String representing a role
	 * @return 	bool	true or false indicating if the user has the role
	 * @Usage  if ( securityService.hasRole( ['ADMIN','SUPERVISOR']) ...
	 */
	boolean hasRole( java.util.ArrayList roles ) {
		boolean found = false
		roles.each() {
			if (! found && SecurityUtils.subject.hasRole( it ) ) {
				found = true
			}
		}
		return found
	}
	
	/**
	 * Used to get a list of the security roles that a user has
	 * @param The UserLogin object of the user being queried
	 * @return A list of roles if the user has any
	 */
	List<RoleType> getRoles(UserLogin user) {
		def roles = []
		if( user ) {
			roles = PartyRole.findAllByParty(user.person)
		}
		return roles*.roleType
	}
	
	/**
	 * Used to determine if a UserLogin has a particular permission
	 * @param A UserLogin object for the given user
	 * @param A permission tag name
	 * @return boolean true if the user does have permission
	 */
	boolean hasPermission(UserLogin user, String permission, boolean reportViolation=false) {
		def hasPerm = false
		def roles = getRoles(user)

		if (roles) {
			def permObj = Permissions.findByPermissionItem(permission)
			if (permObj) {
				if (RolePermissions.findByPermissionAndRoleInList(permObj, roles*.id)) {
					hasPerm=true
				}
			} else {
				log.debug "Unable to find permission ($permission) for user ($user) with roles (${roles*.id})"
				if(reportViolation){
					reportViolation("Unable to find permission ($permission) for user ($user) with roles (${roles*.id})")	
				}
				
			}
		} else {
			log.debug "Unable to find roles for user $user"
			if(reportViolation){				
				reportViolation("Unable to find roles for user $user")
			}
		}

		return hasPerm
	}

	/**
	 * Overloaded version of hasPermission used with Person instead of UserLogin
	 */
	boolean hasPermission(Person person, String permission, boolean reportViolation=false) {
		UserLogin user = getPersonUserLogin(person)
		if (user) {
			return hasPermission(user, permission, reportViolation)
		} else {
			return false
		}
	}

	/**
	 * Overload of hasPermission (userLogin, permission, reportViolation) that is used to determine if a the currently logged in user has a particular permission
	 * @param A permission tag name
	 * @param reportViolation - flag that when true and the user doesn't have the specified permission a violation is reported (default false)
	 * @return boolean true if the user does have permission
	 */
	boolean hasPermission(String permission, boolean reportViolation=false) {
		UserLogin userLogin = getUserLogin()

		if (!userLogin) {
			reportViolation("a user that is not logged in attempted an action requiring the $permission permission")
			return false
		}

		return hasPermission(userLogin, permission, reportViolation)
	}

	/**
	 * Used to get a list of roles that have been assigned to a user. The roleTypeGroup provides a filtering for the type of Roles that 
	 * should be returned (e.g. Staff or System). When a project is presented the method will return roles associate to the project otherwise
	 * it return the user's global role.
	 * 
	 * @param user
	 * @param roleType
	 * @param projectId
	 * @return List of roles
	 */
	def getPersonRoles( def person, RoleTypeGroup roleTypeGroup, Project project=null ) {

		def likeFilter = "${roleTypeGroup} : %"
		def prefixSize = "${roleTypeGroup} : ".length()
		def roles=[]
		
		if (project) {
			// Need to lookup the User's Party role to the Project
			def client=project.client
			// TODO: runbook : getPersonRoles not fully implemented when the project is passed.  Need to test...
			// THIS SHOULD BE LOOKING AT PARTY GROUP, NOT party_relationship - don't use
			def sql = """SELECT role_type_code_to_id
				FROM party_relationship
				WHERE party_relationship_type_id='PROJ_STAFF' AND party_id_from_id=${client.id} AND party_id_to_id=${person.id} AND status_code='ENABLED'"""
			// log.error "getPersonRoles: sql=${sql}"
			roles = jdbcTemplate.queryForList(sql)
			
			log.error "Using getPersonRoles in unsupported manor"
			// log.error "*** Getting from PartyRelationship"
			
		} else {
			// Get the User's default role(s)
			PartyRole.findAllByParty( person )?.each() {
				roles << it.roleType.id
			}	
			// log.error "*** Getting from PartyRole: roles=${roles}"
		}	
		return roles	
	}
	
	/** 
	 * Used to get user's current project
	 */
	// TODO : getUserCurrentProject - move to userPreferenceService
	def getUserCurrentProject() {
		def project
		def projectId = RequestContextHolder.currentRequestAttributes().getSession().getAttribute( "CURR_PROJ" )?.CURR_PROJ
		if ( projectId ) {
			project = Project.get( projectId )
		}
		return project
	}
	
	/**
	 * 
	 * Used to get user's current bundleId
	 */
	// TODO : getUserCurrentMoveBundleId - move to userPreferenceService
	def getUserCurrentMoveBundleId() {
		def bundleId = RequestContextHolder.currentRequestAttributes().getSession().getAttribute( "CURR_BUNDLE" )?.CURR_BUNDLE
		return bundleId
	}
	/**
	 *
	 * Used to get user's current MoveEventId
	 */
	// TODO : getUserCurrentMoveEventId - move to userPreferenceService
	def getUserCurrentMoveEventId() {
		def bundleId = RequestContextHolder.currentRequestAttributes().getSession().getAttribute( "MOVE_EVENT" )?.MOVE_EVENT
		return bundleId
	}
	
	/**
     * Checks whether current user is allow to edit pending status for a task or not.
	 */
	 def isChangePendingStatusAllowed() {
	 	return hasPermission(getUserLogin(), 'ChangePendingStatus')
	 }


	/**
	 * Used to get the UserLogin object of the currently logged in user
	 * @return UserLogin object or null if user is not logged in
	 */
	def getUserLogin() {
		def subject = SecurityUtils.subject
		def principal = subject.principal
		def userLogin
		if (principal)
			userLogin = UserLogin.findByUsername( principal )
		//if (log.isDebugEnabled())
		//	log.debug "getUserLogin: principal=${principal} userLogin=${userLogin}"
		return userLogin
	}

	/**
	 * Used to get the UserLogin for a specifed person
	 * @param person - the Person object
	 * @return The UserLogin if one exists for the person
	 */
	UserLogin getPersonUserLogin(Person person) {
		UserLogin u = UserLogin.findByPerson(person)
		return u
	}

	/**
	 * Used to get the person (Party) object associated with the currently logged in user	
	 * @return Party object of the user or null if not logged in
	 */
	def getUserLoginPerson() {
		def userLogin = getUserLogin()
		return userLogin?.person
	}
	
	/**
	 * Returns the name of a RoleType which currently contains a "GROUP : " prefix that this method strips off
	 * @param roleCode
	 * @return String 
	 */
	def getRoleName( roleCode ) {
		def name=''
		def roleType =  RoleType.get(roleCode)?.description
		// log.error "getRoleName: roleType=${roleType}"
		if (roleType) name = roleType.substring(roleType.lastIndexOf(':')+1)
		return name
	}
	
	/**
	 * Used to determine if a given string meets the password strength requirements
	 * @param username	
	 * @param password	
	 * @return boolean	returns true if the password is valid
	 */
	boolean validPasswordStrength(String username, String password){
		def requirements = 0;
		def score = 0;
		def minLength = getUserLocalConfig().minPasswordLength ?: 8
		if (password && username) {
			if (password.size() >= minLength)
				score++;
			if (password ==~ /.*[a-z]+.*/)
				requirements++;
			if (password ==~ /.*[A-Z]+.*/)
				requirements++;
			if (password ==~ /.*[0-9]+.*/)
				requirements++;
			if (password ==~ /.*[~!@#$%\^&\*_\-\+=`\|\\\(\)\{\}\[\]:;"'<>\,\.?\/]+.*/)
				requirements++;
			if (requirements >= 3)
				score++;
			if (!password.toLowerCase().contains(username.toLowerCase()))
				score++;
		}
		return score == 3
	}

	/**
	 * Used to report security violations
	 * @param message to be reported
	 * @param user - optionally provide the user otherwise it will be looked up automatically
	 */
	void reportViolation(String message, UserLogin user=null) {
		String username
		if (user) {
			username = user.toString()
		} else {
			try {
				username = getUserLogin()?.toString()
				username = username ?: 'UnableToDetermine' 
			} catch (org.apache.shiro.UnavailableSecurityManagerException e) {
				username = 'ProcessRunningAsService'
			} catch (e) {
				log.error "An exception (${e.getMessage()}) while looking up user\n${ExceptionUtil.stackTraceToString(e)}"
				username = 'UnknownUser'
			}
		}
		log.warn "SECURITY_VIOLATION : $message by user $username"
		auditService.logSecurityViolation(username, message)
	}

	/**
	 * Overloaded method to report security violations by passing Person instead of UserLogin
	 * @param message to be reported
	 * @param person - optionally provide the Person otherwise it will be looked up automatically
	 */
	void reportViolation(String message, Person person) {
		UserLogin user
		if (person) {
			user = getPersonUserLogin(person)
		}
		reportViolation(message, user)
	}

	/**
	 * Used to get a list of RoleType that are for Security roles sorted by highest to lowest privilege level
	 * @return List of RoleType
	 */
	List<RoleType> getSecurityRoleTypes() {
		return RoleType.findAllByType(RoleType.SECURITY, [sort:'level', order: 'desc'])
	}

	/**
	 * Used to cleanup expired password reset entires
	 */
	def cleanupPasswordReset(dataMap) {
		log.info("Cleanup Password Reset: Started.")

		def retainDays = getUserLocalConfig().forgotMyPasswordRetainHistoryDays

		jdbcTemplate.update("DELETE FROM password_reset WHERE created_date < (now() - INTERVAL $retainDays DAY) AND password_reset_id > 0 ")

		def expireMinutes = getUserLocalConfig().forgotMyPasswordResetTimeLimit

		jdbcTemplate.update("UPDATE password_reset SET status = 'EXPIRED' WHERE created_date < (now() - INTERVAL $expireMinutes MINUTE) AND password_reset_id > 0 ")

		log.info("Cleanup Password Reset: Finished.")
	}

	/** 
	 * The list of security roles a person is assigned to sort on level DESC 
	 */
	List<RoleType> getAssignedRoles(Person person){
		def assignedRoles = []
		if(person){
			def roleTypes = PartyRole.findAllByParty(person)*.roleType	
			assignedRoles = roleTypes.sort({it.level}).reverse()
		}
		return assignedRoles
	}

	/** 
	 * The list of security role codes that a person has sort on id ASC
	 */
	List<String> getAssignedRoleCodes(Person person){
		def assignedRoles = getAssignedRoles(person)
		return getAssignedRoles.sort({it.id})*.description
	}

	/**
	 * The list of security roles that a person can assign. If the person 
	 * doesn't have _EditUserLogin_ then no roles are returned.
	 * If excludeAssigned, the roles that the user already has are excluded 
	 * from the list.
	 */
	List<RoleType> getAssignableRoles(Person person, boolean excludeAssigned=false){
		def assignableRoles = []
		if(person && hasPermission(getUserLogin(), "EditUserLogin")){
			// All roles
			def roles = getRoles()
			// Assumes getAssignedRoles sorts by level desc.
			def assignedRoles = getAssignedRoles(person)
			// Assigned role with the highest level.
			def maxAssignedLevel = assignedRoles[0].level
			// List of the roles to exclude from the result (assigned role descriptions).
			def excludeRoles = excludeAssigned ? assignedRoles*.description : []
			/* Filter the existing roles, obtaining those with lower (or equal)
			   level and not included in the list of roles to excude. */
			assignableRoles = roles.findAll({(it.level <= maxAssignedLevel) && (!excludeRoles.contains(it.description))})
		}

		return assignableRoles
		
	}
 
 	/** 
	 * The list of security role codes that a person can assign. If the person 
	 * doesn't have _EditUserLogin_ then no roles are returned.
	 * If excludeAssigned, the roles that the user already has are excluded 
	 * from the list.
	 */
	List<String> getAssignableRoleCodes(Person person, boolean excludeAssigned=false){
		return getAssignableRoles(person, excludeAssigned)*.id
	}

	/**
	 * Used to determine if the person has the permissions to assign a given role.
	 * If excludeAssigned, the roles that the user already has are excluded 
	 * from the list.
	 */
	Boolean isRoleAssignable(Person person, RoleType roleType, boolean excludeAssigned=false){
		return  roleType ? isRoleAssignable(person, roleType.description, excludeAssigned) : []
	}

	/**
	 * Overloaded method that looks up the RoleType first. If excludeAssigned then the roles that the user already 
	 * has are excluded from the list.
	 */
	Boolean isRoleAssignable(Person person, String roleType, boolean excludeAssigned=false){
		def isAssignable = false
		def assignableRoleCodes = getAssignableRoleCodes(person, excludeAssigned)
		if (assignableRoleCodes) {
			isAssignable = assignableRoleCodes.contains(roleType)
		}
		return isAssignable
	}

	// Returns a list of ALL security RoleType object sort on level DESC
	List<RoleType> getRoles(){
		return RoleType.list([sort: "level", order:"desc"])
	}

	// Returns a list of ALL security role codes sort on id ASC
	List<String> getRoleCodes(){
		return RoleType.list([sort: "id", order:"asc"])*.description
	}

	/** 
	 * Returns the highest level security RoleType that the person has been assigned. 
	 */
	RoleType getMaxAssignedRole(Person person) {
		return getAssignedRoles(person)[0]
	}

	/** 
	 * Returns the highest level security RoleType defined. 
	 */
	RoleType getMaxRole() {
		return getRoles()[0]
	}

	/** 
	 * Returns the roles that the current user is allow to assign. 
	 */
	List<String> getAssignableRoleCodes(boolean excludeAssigned=false) {
		return getAssignableRoleCodes(getUserLoginPerson(), excludeAssigned)
	}

	/**
	 * Unlocks a user's account 
	 */
	void unlockAccount (UserLogin account) {
		if (hasPermission('UnlockUserLogin')) {
			account.lockedOutUntil = null
			account.save(flush:true)
		} else {
			throw new UnauthorizedException()
		}
	}

	/**
	 * Creates a random token
	 */
	String nextAuthToken() {
		return SecurityUtil.randomString(30)
	}

	/**
	 * Checks to see if a given token is valid and if so returns the PasswordReset object
	 * @param token - the token that represents the Password Reset
	 * @return The PasswordReset object if it was valid
	 * @throws ServiceException with reason token is invalid
	 */
	PasswordReset validateToken(String token) throws ServiceException {
		// Log audit information
		// auditService.logMessage("User attempted reset password using token '$token'")

		if (!token) {
			throw new ServiceException(
				'The password reset token was missing',
				'authentication.password.reset.missing.token.error')
		}

		def pr = PasswordReset.findByToken(token)

		if (!pr) {
			throw new ServiceException(
				'The password reset token has expired or is invalid',
				'authentication.password.reset.expired.token.error')
		}

		if (pr.status != PasswordResetStatus.PENDING) {
			throw new ServiceException(
				'The password reset token is no longer valid',
				'authentication.password.reset.invalid.token.error')
		}

		if (pr.expiresAfter.time < TimeUtil.nowGMT().time) {
			throw new ServiceException(
				'The password reset token has expired',
				'authentication.password.reset.expired.token.error')
		}

		if (! pr.userLogin.isLocal) {
			throw new ServiceException(
				'Your password is not managed in TransitionManager',
				'authentication.password.reset.not.local.user.error')
		}

		if (!pr.userLogin.userActive()) {
			throw new ServiceException(
				'Your user account is disabled',
				'authentication.password.reset.account.disabled.error')
		}

		// TODO - JPM 8/31/2015 - not sure we can do this since if the admin issued a reset, the user should be able to 
		// invoke one.
		if (pr.type == PasswordResetType.FORGOT_MY_PASSWORD && ! verifyMinPeriodToChangePswd(pr.userLogin)) {
			throw new ServiceException(
				'Minimun period between password changes was not met', 
				'authentication.password.reset.minimun.period.error', 
				[getUserLocalConfig().minPeriodToChangePswd * 60] )
		}

		// We will NOT check to see if the account is presently locked out since Admins can issue a password reset for
		// users in the event that their account has been locked out. Note that users can NOT invoke the Forgot My Password
		// while their account is locked

		return pr
	}

	/**
	 * Send a reset password email to the user. This can be invoked by an admin or from the user
	 * via the Forgot My Password.
	 * @param 
	 * @param 
	 * @param 
	 */
	void sendResetPasswordEmail(String email, String ipAddress, PasswordResetType resetType, emailParams = [:]) throws ServiceException {

		// Note - we will not throw exceptions indicating that the account exists or not as this is a method 
		// that hackers poll systems to find valid accounts. The user will get a message that the email was sent
		// but will only be true if everything is valid. Audit logging will log all exceptions that can be monitored.
		// We will ONLY throw exceptions for errors and invalid input.

		// Check that the fields is not empty
		if (StringUtil.isBlank(email)) {
			throw new ServiceException("A valid email address is required")
		}

		while (true) {
			// Check that exist a person with the given email
			def person = Person.findByEmail(email)
			if (person == null) {
				auditService.logWarning("Forgot My Password request with unknown email $email from $ipAddress")
				break
			}

			// Check that the person have
			def userLogin = UserLogin.findByPerson(person)
			if (userLogin == null) {
				auditService.logWarning("Forgot My Password request for email $email but person has no user account from $ipAddress")
				break
			}

			// Check account status to be sure is valid
			if (! userLogin.userActive()) {
				auditService.logWarning("Forgot My Password request for email $email but account is Inactive from $ipAddress")
				break
			}
			if (! userLogin.isLocal) {
				auditService.logWarning("Forgot My Password request for email $email but account is Non-Local from $ipAddress")
				break
			}
			if (userLogin.isLockedOut()) {
				auditService.logWarning("Forgot My Password request for email $email but account is Locked Out from $ipAddress")
				break
			}

			// Check if the user is changing the password to offen
			if (! verifyMinPeriodToChangePswd(userLogin) ) {
				auditService.logWarning("Forgot My Password for email $email was requested to frequently from $ipAddress")
				break
			}

			// TODO - this will change based on resetType to check byAdmin if PasswordResetType.ADMIN_RESET, need to pass person whom is invoking
			if (! userLogin.canResetPasswordByUser() ) {
				auditService.logWarning("Forgot My Password request for email $email but not allowed from $ipAddress")
				break
			}

			auditService.logMessage("Forgot My Password was requested for email $email from $ipAddress")
			def token = nextAuthToken()
			emailParams["token"] = token
			
			def dispatchOrigin = EmailDispatchOrigin.PASSWORD_RESET
			def bodyTemplate = "passwordReset"
			def personFrom = person
			def personFromEmail = person.email
			def createdBy = person
			def subject = "Reset your password"

			if(resetType == PasswordResetType.WELCOME){
				dispatchOrigin = EmailDispatchOrigin.ACTIVATION
				bodyTemplate = "accountActivation"
				personFromEmail = emailParams.from
				createdBy = getUserLogin().person
				subject = "Welcome to TransitionManager"
			}else if(resetType == PasswordResetType.ADMIN_RESET){
				bodyTemplate = "adminResetPassword"
			}

			def ed = emailDispatchService.basicEmailDispatchEntity(
				dispatchOrigin,
				subject,
				bodyTemplate,
				emailParams as JSON,
				personFromEmail,
				person.email,
				person,
				createdBy
			)

			String errMsg = "An error occurred and we were unable to send you a password reset. Please contact support for help."
			if (ed) {
				def date = new Date(TimeUtil.nowGMT().time)
				def pr = createPasswordReset(token, ipAddress, userLogin, person, ed, resetType)		

				if (pr) {
					String granularity = emailDispatchService.getExpiryGranularity(ed)
					emailParams["expiredTime"] = TimeUtil.elapsed(date, pr.expiresAfter, granularity)
					ed.paramsJson = emailParams as JSON
					ed.save(flush:true)
					emailDispatchService.createEmailJob(ed, emailParams)
					log.debug "sendResetPasswordEmail() created token '$token' for $userLogin"
				} else {
					log.error "Forgot My Password request for email $email but emailDispatchService.basicEmailDispatchEntity failed"
					throw new ServiceException(errMsg)
				}

			} else {
				log.error "Forgot My Password request for email $email but emailDispatchService.createEmailJob failed"
				throw new ServiceException(errMsg)
			}

			break
		}
	}

	/**
	 * Creates a new PasswordReset entity and invalidates any existing one
	 */
	PasswordReset createPasswordReset(
		String token, 
		String ipAddress, 
		UserLogin userLogin, 
		Person person, 
		EmailDispatch emailDispatch,
		PasswordResetType resetType) {

		// Invalidate any previous token that is PENDING
		def activeEntities = PasswordReset.findAllByUserLoginAndType(userLogin, PasswordResetType.FORGOT_MY_PASSWORD)
		activeEntities.each { apr ->
			if (apr.status == PasswordResetStatus.PENDING) {
				apr.status = PasswordResetStatus.VOIDED

				if (!apr.validate() || !apr.save(flush:true)) {
					log.error "Unable to void pending password reset token for $userLogin : " + GormUtil.allErrorsString(apr)
				}
			}
		}		

		// Determine the time-to-live for the token
		int tokenTTL
		switch (resetType) {
			case PasswordResetType.ADMIN_RESET:
			case PasswordResetType.FORGOT_MY_PASSWORD:
				tokenTTL = getUserLocalConfig().forgotMyPasswordResetTimeLimit * 60 * 1000
				break
			case PasswordResetType.WELCOME:
				tokenTTL = getUserLocalConfig().accountActivationTimeLimit * 60 * 1000
				break
			default:
				log.error "createPasswordReset() has unhandled switch for option $resetType"
				throw new ServiceException('Unable to initiate a password reset request')
		}
		Date expTime = new Date(TimeUtil.nowGMT().time + tokenTTL)

		// Create a new token
		def pr = new PasswordReset(
			token: token,
			userLogin: userLogin,
			status: PasswordResetStatus.PENDING,
			type: resetType,
			ipAddress: ipAddress,
			createdBy: person,
			emailDispatch: emailDispatch,
			expiresAfter: expTime,
			
		)

		if (!pr.validate() || !pr.save(flush:true)) {
			log.error "createPasswordReset() Unable to create password reset object for $userLogin : " + GormUtil.allErrorsString(pr)
			pr = null
		}

		return pr
	}

	/**
	 * Validates the new password and change the users password
	 */
	PasswordReset applyPasswordFromPasswordReset(String token, String password, String email) throws ServiceException, DomainUpdateException, InvalidParamException {

		PasswordReset pr = validateToken(token)

		if (email?.toLowerCase() != pr.userLogin.person.email?.toLowerCase()) {
			throw new ServiceException("Email address provided does not match that which is associated with token")
		}

		if (! validPasswordStrength(pr.userLogin.username, password)) {
			throw new ServiceException("The password provided does not meet security policy requirements")
		}

		// Perform some validation and then attempt to set the user's new password 
		setUserLoginPassword(pr.userLogin, password)
			
		String errMsg = 'An error occurred while attempting to save your new password'

		if (!pr.userLogin.save()) {
			log.error "applyNewPassword() failed to update UserLogin ${pr.userLogin} : " + GormUtil.allErrorsString(pr.userLogin)
			throw new ServiceException(errMsg)
		}

		pr.status = PasswordResetStatus.COMPLETED
		if ( !pr.save() ) {
			log.error "applyNewPassword() failed to update PasswordReset.status for token $token : " + GormUtil.allErrorsString(pr)
			throw new ServiceException(errMsg)
		}

		// Log audit information
		auditService.logMessage("User ${pr.userLogin} changed own password")

		return pr
	}

	/**
	 * Validates that the user can change the password and if not it will throw an exception
	 */
	void validateAllowedToChangePassword(UserLogin userLogin, boolean byAdmin=false) throws ServiceException {
		// Make sure that the user account details are all set
		// Get the current user to see if the one being changed 
		boolean canResetPswd = true

		if (byAdmin) {
			canResetPswd = userLogin.canResetPasswordByAdmin()
		} else {
			canResetPswd = userLogin.canResetPasswordByUser()
		}

		if ( ! canResetPswd ) {
			throw new ServiceException('Not allowed to change the password at this time')
		}
	}

	/**
	 * Used to verify that the minimun period between password changes has been met 
	 * @param userLogin - the account to verify can change their password
	 * @return true if the period of time between password changes has been met
	 */
	boolean verifyMinPeriodToChangePswd(UserLogin userLogin) {
		// Check to see if minimum period is a requirement first
		if ( getUserLocalConfig().minPeriodToChangePswd > 0 ) {
			int minTimeLimit = getUserLocalConfig().minPeriodToChangePswd * 60 * 60 * 1000
			if (TimeUtil.nowGMT().time < (userLogin.passwordChangedDate.time + minTimeLimit)) {
				return false
			}
		}
		return true
	}

	/**
	 * Used to verify that the new password meets the password history requirements
	 * @param userLogin - the account to verify can change their password
	 * @param newPassword - the new encrypted password
	 * @return true if the period of time between password changes has been met
	 */
	boolean verifyPasswordHistory(UserLogin userLogin, String newPassword) {
		// Check to see if minimum period is a requirement first
		Map cfg = getUserLocalConfig()
		boolean verified = true
		def c = PasswordHistory.createCriteria()

		if (cfg.passwordHistoryRetentionCount > 0) {
			// Check to see if the password was used in the past # passwords
			List list = c.list(max:10) {
		        eq('userLogin', userLogin)
			    order('createdDate', 'desc')
			}
			verified = ! list.find({ it.password == newPassword})

		} else if (cfg.passwordHistoryRetentionDays > 0) {
			// Check to see if the password was used in the past # of days
			Date dateSince
			use (TimeCategory) {
				dateSince = cfg.passwordHistoryRetentionDays.intValue().days.ago
			}
			List list = c.list(max:10) {
		        eq('userLogin', userLogin)
		        and {
		        	eq('password', newPassword)
		        	ge('createdDate', dateSince)
		        }
			}
			verified = list.size() == 0
		}
		return verified
	}

	/**
	 * Used to set the password on a UserLogin object after verifying that the password is legitimate
	 * and meets the password history requirements otherwise it will thrown an exception
	 * @param userLogin - the UserLogin object to set the password on
	 * @param unencryptedPassword - the unencrypted password to set
	 * @throws DomainUpdateException
	 */
	void setUserLoginPassword(UserLogin userLogin, String unencryptedPassword, Boolean isNewUser=false) throws DomainUpdateException, InvalidParamException {
		// Make sure that the password strength is legitimate
		if (! validPasswordStrength(userLogin.username, unencryptedPassword)) {
			throw new InvalidParamException('The new password does not comply with password requirements')
		}

		String currentPassword = userLogin.password
		String encryptedPassword = userLogin.applyPassword(unencryptedPassword)

		if (currentPassword.equals(encryptedPassword)) {
			throw new InvalidParamException('New password must be different from the old password')
		}

		if (!isNewUser && ! verifyPasswordHistory(userLogin, encryptedPassword)) {
			throw new DomainUpdateException('Please provide a new password that was not previously used')
		}

	}

	/**
	 * Used to update the UserLogin and the associated permissions for the account
	 * @param params - the Map of the params coming from the browser form
	 * @param byWhom - the UserLogin that is performing the update
	 * @param tzId - the timezone of the user performing the update
	 * @param isNewUser - flag true indicating to create otherwise update an existing user
	 * @return The UserLogin object that was created or updated 
	 */
	UserLogin createOrUpdateUserLoginAndPermissions(params, UserLogin byWhom, String tzId, isNewUser) 
		throws InvalidParamException, UnauthorizedException {

		UserLogin userLogin
		Person person 
		Project project
		def session = serviceHelperService.getService('userPreference').getSession()

		if (StringUtil.isBlank(params.username)) {
			throw new InvalidParamException('Username should not be empty')
		}

		if (! NumberUtil.isPositiveLong(params.projectId)) {
			throw new InvalidParamException('A project must be selected')
		}

		project = Project.get( params.projectId )
		if (! project) {
			throw new InvalidParamException('Specified project was not found')
		}
		
		if (isNewUser) {
			userLogin = new UserLogin()
			if (! NumberUtil.isPositiveLong(params.personId)) {
				throw new InvalidParamException('Person id was missing or invalid')
			}
			person = Person.get( params.personId )
			if (!person) {
				throw new InvalidParamException('Specified person was not found')
			}
			userLogin.person = person

			if (StringUtil.isBlank(params.password)) {
				throw new DomainUpdateException("Password should not be empty")
			}

		} else {
			// Make sure we have a user to edit
			Long userId = NumberUtil.toPositiveLong(params.id, -1)
			if ( userId == -1 ) {
				throw new InvalidParamException('User id was missing or invalid')
			}
			userLogin = UserLogin.get( userId )
			if (!userLogin) {
				throw new InvalidParamException('Specified user was not found')
			}
			person = userLogin.person
		}

		// Determine if the individual has proper access to the user/person to be editted
		def personService = serviceHelperService.getService('person')

		// Test that the user has access account being updated
		personService.hasAccessToPerson(byWhom.person, person, true, true)

		// Determine if the specified person can be assigned to the specified project
		// TODO Restore this feature, it disabled temporally by TM-4100
		//if (! personService.getAssignedProjects(person, project)) {
		//	reportViolation("Attempt to assign user $userLogin ($userLogin.id) to an unassociated project $project.id", byWhom)
		//	throw new InvalidParamException('User is not associated with the specified project')
		//}

		//
		// Update the UserLogin information

/*
Properties to contend with:
	companyId:
	personId:5665
	passwordNeverExpires:true
	active:Y
	_action_Update:Update

Dealt with:
	expiryDate: 07/06/2016 05:31 PM
	passwordExpirationDate: 09/17/2015 05:13 AM
	passwordNeverExpires: true
	_action_Update: Update
	projectId: 5791
	password:
	assignedRole: ADMIN
	assignedRole: CLIENT_ADMIN
	assignedRole: CLIENT_MGR
	id: 653
	username: abc
	lockedOutUntil:
	personId: 5665
	active: Y
	isLocal: true
*/
		
		if (params.isLocal) {
			userLogin.isLocal = true
			userLogin.forcePasswordChange = (params.forcePasswordChange ? 'Y' : 'N')
			userLogin.passwordNeverExpires = (params.containsKey('passwordNeverExpires') && params.passwordNeverExpires.equals('true'))
			
			def email = params.email ?: ''
			def emailUsers = Person.findAllByEmail(email)
			emailUsers.each {
				if (it.id != person.id)
					throw new InvalidParamException('Email "' + email + '" is already in use by another user')
			}
			if (!email || StringUtil.isBlank(email))
				throw new InvalidParamException('Invalid email')
			person.email = email
		} else {
			userLogin.isLocal = false
			userLogin.forcePasswordChange = 'N'
		}

		// TODO : Try setting the username if it has changed

		// Attempt to deal with parsing the dates
		String dateField
		try {
			if (params.expiryDate) {
				dateField = 'Expiry'
				userLogin.expiryDate = TimeUtil.parseDateTime(session, params.expiryDate)
			}

			if (params.passwordExpirationDate) {
				dateField = 'Password Expiration'
				userLogin.passwordExpirationDate = TimeUtil.parseDateTime(session, params.passwordExpirationDate)
			}

			// TODO : should changing the locked out until be allowed? 
			if (params.lockedOutUntil) {
				dateField = 'Locked Out Until'
				userLogin.lockedOutUntil = TimeUtil.parseDateTime(session, params.lockedOutUntil)
			}
		} catch (e) {
			throw new InvalidParamException("The $dateFile field has invalid format")
		}

		def isCurrentUserLogin = (byWhom.id == userLogin.id)
		if (!isCurrentUserLogin && !StringUtil.isBlank(params.username) && !userLogin.username.equals(params.username)) {
			def newUserNameUserLogin = UserLogin.findByUsername(params.username)
			if (newUserNameUserLogin != null) {
				throw new InvalidParamException("The username you is selected is already in use.")
			} else {
				userLogin.username = params.username
			}
		}

		// Attempt to set the password if it was set
		if (params.password) {
			setUserLoginPassword(userLogin, params.password, isNewUser)
		}

		userLogin.active = params.active

		// Try to save the user changes
		if (! userLogin.save(flush:true) ) {
			throw new DomainUpdateException("Unable to update User : " + GormUtil.allErrorsString(userLogin))
		}

		// When enabling user - enable Person
		// When disable user - do NOT change Person
		
		if (userLogin.active == 'Y') {
			person.active = 'Y'
			if (!person.save(flush:true)) {
				throw new DomainUpdateException('Unable to update person : ' + GormUtil.allErrorsString(person))
			}
		}

		//
		// Now lets deal with the permissions
		//
		List<String> assignedRoles 
		if (params.assignedRole instanceof String [] ) {
			assignedRoles = params.assignedRole
		} else {
			assignedRoles = [params.assignedRole]
		}

		// Remove any Roles that are not in the above list
		def partyRelationshipService = serviceHelperService.getService('partyRelationship')
		partyRelationshipService.updatePartyRoleByType('system', person, assignedRoles)

		def userPreferenceService = serviceHelperService.getService('userPreference')
		if (userPreferenceService.setUserRoles(assignedRoles, person.id)) {
			throw new DomainUpdateException('Unable to update user security roles')
		}

		// 
		// Set the default project preference for the user
		//
		if (! userPreferenceService.addOrUpdatePreferenceToUser(userLogin, "CURR_PROJ", params.projectId)) {
			throw new DomainUpdateException('Unable to save selected project')
		}
		if (isNewUser) {
			userPreferenceService.addOrUpdatePreferenceToUser(userLogin, "START_PAGE", "User Dashboard")
			// TODO : JPM 3/2016 : Default preferences for user for TZ/Date format should be based on the selected project
			// The date format should be encapsulated or we should add the default Date format on the project as well.
			userPreferenceService.addOrUpdatePreferenceToUser(userLogin, TimeUtil.TIMEZONE_ATTR, TimeUtil.defaultTimeZone)
			userPreferenceService.addOrUpdatePreferenceToUser(userLogin, TimeUtil.DATE_TIME_FORMAT_ATTR, TimeUtil.getDefaultFormatType())

			auditService.saveUserAudit(UserAuditBuilder.newUserLogin(byWhom, userLogin.username))
		}

		return userLogin
	}
}
