package net.transitionmanager.service

import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.SecurityConfigParser
import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.security.spring.TdsUserDetails
import com.tdsops.common.security.spring.UsernamePasswordAuthorityAuthenticationToken
import com.tdsops.tm.enums.domain.EmailDispatchOrigin
import com.tdsops.tm.enums.domain.PasswordResetStatus
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdsops.tm.enums.domain.RoleTypeGroup
import com.tdsops.tm.enums.domain.StartPageEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import net.transitionmanager.EmailDispatch
import net.transitionmanager.PasswordHistory
import net.transitionmanager.PasswordReset
import net.transitionmanager.command.UserUpdatePasswordCommand
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.PartyRole
import net.transitionmanager.domain.Permissions
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RolePermissions
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.beans.factory.InitializingBean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserCache
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.util.Assert
import org.springframework.web.context.request.RequestContextHolder

import static net.transitionmanager.domain.Permissions.Roles.ROLE_ADMIN
import static net.transitionmanager.domain.Permissions.Roles.ROLE_USER

/**
 * The SecurityService class provides methods to manage User Roles and Permissions, etc.
 */
@Slf4j()
class SecurityService implements ServiceMethods, InitializingBean {
	private static final int ONE_HOUR = 60 * 60 * 1000
	private static final Collection<String> SECURITY_ROLES = ['USER', 'EDITOR', 'SUPERVISOR']

	/**
	 * The default security code that should be assigned to individuals if not is specified.
	 */
	static final String DEFAULT_SECURITY_ROLE_CODE = ROLE_USER.name()

	def auditService
	def emailDispatchService
	def partyRelationshipService
	def personService
	def springSecurityService
	def userPreferenceService
	JdbcTemplate jdbcTemplate

	private Map ldapConfigMap
	private Map loginConfigMap
	private Map userLocalConfigMap

	/*
	 * Time to live data catched for testing status
	 */
	private long forgotMyPasswordResetTTL = 0
	private long accountActivationTTL = 0

	void afterPropertiesSet() {

		def config = grailsApplication.config

		log.info 'Parsing Security Login setting options'
		loginConfigMap = SecurityConfigParser.parseLoginSettings(config).asImmutable()

		log.info 'Parsing Security LDAP setting options'
		ldapConfigMap = SecurityConfigParser.parseLDAPSettings(config).asImmutable()

		log.info 'Parsing Local User Configuration Settings options'
		userLocalConfigMap = SecurityConfigParser.parseLocalUserSettings(config).asImmutable()

		log.info 'Validating Security LDAP company/party setting'
		validateLDAPCompanyProjectSettings(ldapConfigMap)
	}

	/**
	 * Validate that each of the domains reference a valid company and project.
	 * @throws ConfigurationException
	 */
	private void validateLDAPCompanyProjectSettings(Map map) {
		// println "*****validateLDAPCompanyProjectSettings() map====$map"
		if (map.enabled) {
			if (!(map.domains instanceof Map)) {
				throw new ConfigurationException("Security setting 'domains' is undefined or invalid")
			}

			// TODO : JPM 12/2014 - need to correct validateLDAPCompanyProjectSettings since read has not yet been injected on Domain objects yet
			/*
			map.domains.each { k,d ->
				Project project = Project.read(d.defaultProject)
				if (!project) {
					throw new ConfigurationException("Security settings has invalid '${k}.defaultProject' id")
				}

				PartyGroup pg = PartyGroup.read(d.company)
				if (project.client != d.company) {
					throw new ConfigurationException("Security settings has invalid '${k}.company' id")
				}

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
	Map getLoginConfig() {
		loginConfigMap
	}

	/**
	 * Returns the configuration map for the LDAP setting derived from the tdstm-config.groovy settings
	 * @return a map of all of the settings for the login
	 *    String authorityPrompt - select:show select, prompt: prompt for autority, na: do nothing for authority
	 *	  List selectOptions - list of authorities/domains labels
	 */
	Map getLDAPConfig() {
		ldapConfigMap
	}

	/**
	 * Returns the configuration map for the User Local Settings derived from the tdstm-config.groovy settings
	 * @return a map of all of the settings for the user local settions
	 */
	Map getUserLocalConfig() {
		userLocalConfigMap
	}

	// TODO : getUserCurrentProject - move to userPreferenceService
	@Transactional(readOnly=true)
	Project getUserCurrentProject() {
		Project project = null
		String projectId = this.getUserCurrentProjectId()
		if(projectId){
			project = Project.get( this.getUserCurrentProjectId() )
		}

		return project
	}

	/**
	 * Returns the current Project or Throws a EmptyResultException
	 */
	Project getUserCurrentProjectOrException() {
		Project project = getUserCurrentProject()

		if(project == null){
			throw new EmptyResultException('No project selected')
		}

		return project
	}

	String getUserCurrentProjectId() {
		userPreferenceService.getPreference(PREF.CURR_PROJ)
	}

	boolean isCurrentProjectId(projectId) {
		this.getUserCurrentProjectId() == projectId?.toString()
	}

	Project loadUserCurrentProject() {
		String id = this.getUserCurrentProjectId()
		id ? Project.load(id as long) : null
	}

	void assertCurrentProject(Project project) {
		if (!isCurrentProjectId(project?.id)) {
			throw new IllegalArgumentException('The object is not associated with the project')
		}
	}

	void assertAuthenticated() {
		Assert.state isLoggedIn(), 'There is no current authenticated user'
	}

	/**
	 * Checks whether current user is allow to edit pending status for a task or not
	 * TODO : JPM 4/2016 : isChangePendingStatusAllowed method here is OBSCURED and should be removed but used in tasks
	 */
	boolean isChangePendingStatusAllowed() {
		hasPermission(Permission.TaskChangeStatus)
	}

	/**
	 * Used to determine if the user has access to the specified project
	 * @param project - a Project domain instance
	 * @param userLogin - the userLogin domain instance of the current user (optional)
	 * @return true if the user has rights to access the project otherswise false
	 */
	Boolean hasAccessToProject(Project project, UserLogin userLogin = null) {
		return hasAccessToProject(project.id, userLogin)
	}

	/**
	 * Used to determine if the user has access to the specified project
	 * @param projectId - the ID of the Project to check access to
	 * @param userLogin - the userLogin domain instance of the current user (optional)
	 * @return true if the user has rights to access the project otherswise false
	 */
	Boolean hasAccessToProject(Long projectId, UserLogin userLogin = null) {
		return projectId in ( getUserProjectIds(projectId, userLogin) )
	}

	/**
	 * Returns a list of project ids that the user has access to
	 * @param projectId - used to only look for a particular projectId (optional)
	 * @param userLogin - the user to lookup projects for or null to use the authenticated user
	 * @return List of project IDs that the user has access to
	 */
	List<Long> getUserProjectIds(Long projectId = null, UserLogin userLogin = null) {
		Person person = userLogin ? userLogin.person : getUserLoginPerson()
		List<Long> projectIds = []
		Boolean showAllProjPerm = hasPermission(Permission.ProjectShowAll)
		Boolean hasAccessToDefaultProject = securityService.hasPermission(person, Permission.ProjectManageDefaults)

		if (showAllProjPerm) {
			// Find all the projects that are available for the user's company as client or as partner or owner
			projectIds = partyRelationshipService.companyProjects(person.getCompany()).id
		} else {
			// Find the projects that the user has been assigned to
			projectIds = PartyRelationship.where {
				partyRelationshipType.id == 'PROJ_STAFF'
				roleTypeCodeFrom.id == 'PROJECT'
				roleTypeCodeTo.id == 'STAFF'
				partyIdTo.id == person.id
				if (projectId) {
					partyIdFrom.id == projectId
				}
			}.projections {
				distinct('partyIdFrom.id')
			}.list()
		}

		// If the user has access to the default project, it should be included in the list.
		if  (hasAccessToDefaultProject && (projectId == Project.DEFAULT_PROJECT_ID || ! projectId) ) {
			projectIds << Project.DEFAULT_PROJECT_ID
		}

		return projectIds
	}


	/**
	 * Get the UserLogin object of the currently logged in user or null if user is not logged in
	 */
	@Transactional(readOnly=true)
	UserLogin getUserLogin() {
		UserLogin.get currentUserLoginId
	}

	/**
	 * The cached UserLogin id of the authenticated user or null if not authenticated.
	 */
	Long getCurrentUserLoginId() {
		(Long) springSecurityService.currentUserId
	}

	/**
	 * The currently logged-in user's username or null if not authenticated
	 */
	String getCurrentUsername() {
		currentUserDetails?.username
	}

	/**
	 * Creates a proxy UserLogin instance for the currently logged in user's UserLogin for use where
	 * the instance data isn't used, e.g. in a dynamic finder or to set an object reference using the FK.
	 * @return the proxy
	 */
	UserLogin loadCurrentUserLogin() {
		if (loggedIn) {
			UserLogin.load currentUserLoginId
		}
	}

	/**
	 * Get the UserLogin for a specifed person.
	 * @param person - the Person object
	 * @return The UserLogin if one exists for the person
	 */
	UserLogin getPersonUserLogin(Person person) {
		person.userLogin
	}

	/**
	 * The current Authentication if logged in, otherwise null.
	 */
	UsernamePasswordAuthorityAuthenticationToken getCurrentAuthentication() {
		isLoggedIn() ? (UsernamePasswordAuthorityAuthenticationToken) springSecurityService.authentication : null
	}

	/**
	 * The IP address stored during authentication.
	 */
	String getIpAddress() {
		currentAuthentication?.ipAddress
	}

	/**
	 * This is the principal instance created when logging in and it caches useful data including granted
	 * roles and permissions, the ids of the associated UserLogin and Person instances. Use the data
	 * cached here to avoid unnecessary database hits.
	 */
	TdsUserDetails getCurrentUserDetails() {
		isLoggedIn() ? (TdsUserDetails) springSecurityService.principal : null
	}

	/**
	 * Get the person (Party) object associated with the currently logged in user.
	 * @return Party object of the user or null if not logged in
	 */
	@Transactional(readOnly=true)
	Person getUserLoginPerson() {
		getUserLogin()?.person
	}

	Long getCurrentPersonId() {
		currentUserDetails?.personId
	}

	/**
	 * Creates a proxy Person instance for the currently logged in user's UserLogin's Person for use where
	 * the instance data isn't used, e.g. in a dynamic finder or to set an object reference using the FK.
	 * @return the proxy
	 */
	Person loadCurrentPerson() {
		Long personId = currentPersonId
		personId ? Person.load(personId) : null
	}

	/**
	 * Quick check to see if the current user is logged in.
	 * @return true if authenticated and not anonymous
	 */
	boolean isLoggedIn() {
		springSecurityService.loggedIn
	}

	/**
	 * Determine if a given string meets the password strength requirements.
	 * @param username
	 * @param password
	 * @return boolean	returns true if the password is valid
	 */
	boolean validPasswordStrength(String username, String password) {
		int requirements = 0
		int score = 0
		int minLength = userLocalConfig.minPasswordLength ?: 8
		if (password && username) {
			if (password.size() >= minLength) {
				score++
			}
			if (password ==~ /.*[a-z]+.*/) {
				requirements++
			}
			if (password ==~ /.*[A-Z]+.*/) {
				requirements++
			}
			if (password ==~ /.*[0-9]+.*/) {
				requirements++
			}
			if (password ==~ /.*[~!@#$%\^&\*_\-\+=`\|\\\(\)\{\}\[\]:;"'<>\,\.?\/]+.*/) {
				requirements++
			}
			if (requirements >= 3) {
				score++
			}
			if (!password.toLowerCase().contains(username.toLowerCase())) {
				score++
			}
		}
		return score == 3
	}

	/**
	 * Reports security violations.
	 * @param message to be reported
	 * @param username - optionally provide the username otherwise it will be looked up
	 */
	void reportViolation(CharSequence message, String username = null) {
		if (!username) {
			try {
				username = getCurrentUsername() ?: 'UnableToDetermine'
			}
			catch (e) {
				log.error 'An exception ({}) while looking up user\n{}', e.message, ExceptionUtil.stackTraceToString(e)
				username = 'UnknownUser'
			}
		}
		log.warn 'SECURITY_VIOLATION : {} by user {}', message, username
		// auditService.logSecurityViolation(username, message)
	}

	/**
	 * The RoleTypes that are for Security roles sorted by highest to lowest privilege level.
	 * @return  the RoleTypes
	 */
	List<RoleType> getSecurityRoleTypes() {
		return RoleType.findAllByType(RoleType.SECURITY, [sort: 'level', order: 'desc'])
	}

	/**
	 * Cleanup expired password reset entries.
	 */
	@Transactional
	void cleanupPasswordReset(dataMap) {
		log.info('Cleanup Password Reset: Started.')

		def retainDays = userLocalConfig.forgotMyPasswordRetainHistoryDays
		//println "retainDays: ${retainDays}"

		jdbcTemplate.update("DELETE FROM password_reset WHERE created_date < (now() - INTERVAL $retainDays DAY)")

		def now = TimeUtil.nowGMT()
		jdbcTemplate.update("UPDATE password_reset SET status = 'EXPIRED' WHERE status <> 'EXPIRED' and expires_after < ?", now)

		log.info('Cleanup Password Reset: Finished.')
	}

	@Transactional
	void updatePassword(UserLogin userLogin, UserUpdatePasswordCommand command) throws ServiceException{
		String msg
		try {
			if (!userLogin) {
				msg = 'Failed to load your user account'

			} else {

				// See if the user account is properly configured to a state that they're allowed to change their password
				securityService.validateAllowedToChangePassword(userLogin)


				//
				// Made it throught the guantlet of password requirements so lets update the password
				//
				securityService.setUserLoginPassword(userLogin, command.password, command.confirmPassword)

				if (!userLogin.save(failOnError: false)) {
					log.warn "updatePassword() failed to update user password for $userLogin : ${GormUtil.allErrorsString(userLogin)}"
					throw new DomainUpdateException('An error occured while trying to save your password')

				} else {
					auditService.saveUserAudit(UserAuditBuilder.userLoginPasswordChanged())

				}

			}

		} catch (InvalidParamException | DomainUpdateException e) {
			msg = e.message

		} catch (ServiceException e) {
			msg = "You are not allowed to change your password at this time."

		} catch (e) {
			log.warn "updateAccount() failed : ${ExceptionUtil.stackTraceToString(e)}"
			msg = 'An error occurred during the update process'
		}

		if ( msg ) {
			throw new ServiceException(msg)
		}
	}

	/**
	 * Unlocks a user's account
	 */
	@Transactional
	void unlockAccount(UserLogin account) {
		requirePermission Permission.UserUnlock

		account.lockedOutUntil = null
		account.lastLogin = TimeUtil.nowGMT()
		auditService.saveUserAudit UserAuditBuilder.userAccountWasUnlockedBy(getCurrentUsername(), account)
		save account
	}

	/**
	 * Creates a random token.
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

		PasswordReset pr = PasswordReset.findWhere(token: token)
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

		if (!pr.userLogin.isLocal) {
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
		if (pr.type == PasswordResetType.FORGOT_MY_PASSWORD && !verifyMinPeriodToChangePswd(pr.userLogin)) {
			throw new ServiceException(
				'Minimun period between password changes was not met',
				'authentication.password.reset.minimun.period.error',
				[userLocalConfig.minPeriodToChangePswd * 60])
		}

		// We will NOT check to see if the account is presently locked out since Admins can issue a password reset for
		// users in the event that their account has been locked out. Note that users can NOT invoke the Forgot My Password
		// while their account is locked

		return pr
	}

	/**
	 * Send a reset password email to the user. This can be invoked by an admin or from the user
	 * via the Forgot My Password.
	 */
	@Transactional
	void sendResetPasswordEmail(String email, String ipAddress, PasswordResetType resetType,
		Map emailParams = [:]) throws ServiceException {

		// Note - we will not throw exceptions indicating that the account exists or not as this is a method
		// that hackers poll systems to find valid accounts. The user will get a message that the email was sent
		// but will only be true if everything is valid. Audit logging will log all exceptions that can be monitored.
		// We will ONLY throw exceptions for errors and invalid input.

		// Check that the fields is not empty
		if (StringUtil.isBlank(email)) {
			throw new ServiceException("A valid email address is required")
		}

		String byWhom = getCurrentUsername()

		while (true) {
			// Check that exist a person with the given email
			def person = Person.findByEmail(email)
			if (person == null) {
				auditService.logWarning("Forgot My Password request with unknown email $email from $ipAddress")
				break
			}

			// Check that the person have
			UserLogin userLogin = person.userLogin
			if (userLogin == null) {
				auditService.logWarning("Forgot My Password request for email $email but person has no user account from $ipAddress")
				break
			}

			// Check account status to be sure is valid
			if (!userLogin.userActive()) {
				auditService.logWarning("Forgot My Password request for email $email but account is Inactive from $ipAddress")
				break
			}
			if (!userLogin.isLocal) {
				auditService.logWarning("Forgot My Password request for email $email but account is Non-Local from $ipAddress")
				break
			}
			if (userLogin.isLockedOut()) {
				auditService.logWarning("Forgot My Password request for email $email but account is Locked Out from $ipAddress")
				break
			}

			// Check if the user is changing the password to often
			if (!verifyMinPeriodToChangePswd(userLogin)) {
				auditService.logWarning("Forgot My Password for email $email was requested to frequently from $ipAddress")
				break
			}

			// TODO - this will change based on resetType to check byAdmin if PasswordResetType.ADMIN_RESET, need to pass person whom is invoking
			if (!userLogin.canResetPasswordByUser()) {
				auditService.logWarning("Forgot My Password request for email $email but not allowed from $ipAddress")
				break
			}

			if (!grailsApplication.config.grails.mail.default.from) {
				auditService.logWarning("grails.mail.default.from not defined in tdstm-config.groovy")
				break
			}

			auditService.logMessage("Forgot My Password was requested for email $email from $ipAddress")
			def token = nextAuthToken()
			emailParams["token"] = token

			def dispatchOrigin = EmailDispatchOrigin.PASSWORD_RESET
			String bodyTemplate = "passwordReset"
			String personFromEmail = grailsApplication.config.grails.mail.default.from
			def createdBy = person
			String subject = "Forgot your password"

			switch(resetType) {
				case PasswordResetType.WELCOME:
					dispatchOrigin = EmailDispatchOrigin.ACTIVATION
					bodyTemplate = "accountActivation"
					personFromEmail = emailParams.from
					createdBy = userLogin.person
					subject = "Welcome to TransitionManager"
					break

				case PasswordResetType.ADMIN_RESET:
					createdBy = userLogin.person
					bodyTemplate = "adminResetPassword"
					subject = "Reset Password Request"
					break

			}

			EmailDispatch ed = emailDispatchService.basicEmailDispatchEntity(dispatchOrigin, subject, bodyTemplate,
					emailParams as JSON, personFromEmail, person.email, person, createdBy)

			String errMsg = "An error occurred and we were unable to send you a password reset. Please contact support for help."
			if (ed) {
				def date = new Date(TimeUtil.nowGMT().time)
				def pr = createPasswordReset(token, ipAddress, userLogin, person, ed, resetType)
				if (pr) {
					String granularity = emailDispatchService.getExpiryGranularity(ed)
					emailParams["expiredTime"] = TimeUtil.elapsed(date, pr.expiresAfter, granularity)
					ed.paramsJson = emailParams as JSON
					save ed
					emailDispatchService.createEmailJob(ed, emailParams)
					log.debug 'sendResetPasswordEmail() created token "{}" for {}', token, userLogin
				}
				else {
					log.error 'Forgot My Password request for email {} but emailDispatchService.basicEmailDispatchEntity failed', email
					throw new ServiceException(errMsg)
				}
			}
			else {
				log.error 'Forgot My Password request for email {} but emailDispatchService.createEmailJob failed', email
				throw new ServiceException(errMsg)
			}

			break
		}
	}

	/**
	 * Creates a new PasswordReset entity and invalidates any existing one
	 */
	@Transactional
	PasswordReset createPasswordReset(String token, String ipAddress, UserLogin userLogin, Person person,
	                                  EmailDispatch emailDispatch, PasswordResetType resetType) {

		// Invalidate any previous token that is PENDING
		for (it in PasswordReset.findAllByUserLoginAndType(userLogin, PasswordResetType.FORGOT_MY_PASSWORD)) {
			PasswordReset pr = (PasswordReset) it
			if (pr.status == PasswordResetStatus.PENDING) {
				pr.status = PasswordResetStatus.VOIDED
				if (!pr.save()) {
					log.error 'Unable to void pending password reset token for {} : {}', userLogin, GormUtil.allErrorsString(pr)
				}
			}
		}

		// Determine the time-to-live for the token
		int tokenTTL
		switch (resetType) {
			case PasswordResetType.ADMIN_RESET:
			case PasswordResetType.FORGOT_MY_PASSWORD:
				tokenTTL = getForgotMyPasswordResetTTL()
				break
			case PasswordResetType.WELCOME:
				tokenTTL = getAccountActivationTTL()
				break
			default:
				log.error 'createPasswordReset() has unhandled switch for option {}', resetType
				throw new ServiceException('Unable to initiate a password reset request')
		}
		Date expTime = new Date(TimeUtil.nowGMT().time + tokenTTL)

		// Create a new token
		PasswordReset pr = save new PasswordReset(
			token: token,
			userLogin: userLogin,
			status: PasswordResetStatus.PENDING,
			type: resetType,
			ipAddress: ipAddress,
			createdBy: person,
			emailDispatch: emailDispatch,
			expiresAfter: expTime)

		if (!pr.hasErrors()) {
			return pr
		}
	}

	/**
	 * Returns the configuration Forgot My Password Reset Time frame in Milliseconds
	 * If the forgotMyPasswordResetTTL is not set (or 0) we will get it from the user configuration,
	 * this works with setForgotMyPasswordResetTTL in a way that we can set our own value i.e. when testing.
	 * @return
	 */
	synchronized
	long getForgotMyPasswordResetTTL(){
		if(!forgotMyPasswordResetTTL){
			forgotMyPasswordResetTTL = userLocalConfig.forgotMyPasswordResetTimeLimit * 60 * 1000
		}
		return forgotMyPasswordResetTTL
	}

	synchronized
	void setForgotMyPasswordResetTTL(long ttlMillis){
		forgotMyPasswordResetTTL = ttlMillis
	}

	/**
	 * Returns the configuration Account Acctivation Time frame in Milliseconds
	 * If the accountActivationTTL is not set (or 0) we will get it from the user configuration,
	 * this works with setAccountActivationTTL in a way that we can set our own value i.e. when testing.
	 * @return
	 */
	synchronized
	long getAccountActivationTTL(){
		if(!accountActivationTTL){
			accountActivationTTL = userLocalConfig.accountActivationTimeLimit * 60 * 1000
		}
		return accountActivationTTL
	}

	synchronized
	void setAccountActivationTTL(long ttlMillis){
		accountActivationTTL = ttlMillis
	}

	/**
	 * Validate the new password and change the user's password.
	 */
	@Transactional
	PasswordReset applyPasswordFromPasswordReset(String token, String password, confirmPassword, String email) throws ServiceException, DomainUpdateException, InvalidParamException {

		PasswordReset pr = validateToken(token)

		if (email?.toLowerCase() != pr.userLogin.person.email?.toLowerCase()) {
			throw new ServiceException("Email address provided does not match that which is associated with token")
		}

		if (!validPasswordStrength(pr.userLogin.username, password)) {
			throw new ServiceException("The password provided does not meet security policy requirements")
		}

		// Perform some validation and then attempt to set the user's new password
		setUserLoginPassword(pr.userLogin, password, confirmPassword)

		String errMsg = 'An error occurred while attempting to save your new password'

		if (!pr.userLogin.save()) {
			log.error 'applyNewPassword() failed to update UserLogin {} : {}', pr.userLogin, GormUtil.allErrorsString(pr.userLogin)
			throw new ServiceException(errMsg)
		}

		pr.status = PasswordResetStatus.COMPLETED
		if (!pr.save()) {
			log.error 'applyNewPassword() failed to update PasswordReset.status for token {} : {}', token, GormUtil.allErrorsString(pr)
			throw new ServiceException(errMsg)
		}

		// Log audit information
		auditService.logMessage("User $pr.userLogin changed own password")

		return pr
	}

	/**
	 * Validates that the user can change the password and if not it will throw an exception
	 */
	void validateAllowedToChangePassword(UserLogin userLogin, boolean byAdmin=false) throws ServiceException {
		// Make sure that the user account details are all set
		// Get the current user to see if the one being changed
		boolean canResetPswd = byAdmin ? userLogin.canResetPasswordByAdmin() : userLogin.canResetPasswordByUser()
		if (!canResetPswd) {
			throw new ServiceException('Not allowed to change the password at this time')
		}
	}

	/**
	 * Verify that the minimum period between password changes has been met.
	 * @param userLogin - the account to verify can change their password
	 * @return true if the period of time between password changes has been met
	 */
	boolean verifyMinPeriodToChangePswd(UserLogin userLogin) {
		// Check to see if minimum period is a requirement first
		if (userLocalConfig.minPeriodToChangePswd > 0 && userLogin.passwordChangedDate) {
			int minTimeLimit = userLocalConfig.minPeriodToChangePswd * ONE_HOUR
			if (TimeUtil.nowGMT().time < (userLogin.passwordChangedDate.time + minTimeLimit)) {
				return false
			}
		}
		return true
	}

	/**
	 * Verify that the new password meets the password history requirements.
	 * @param userLogin - the account to verify can change their password
	 * @param newPassword - the new hashed password
	 * @return true if the period of time between password changes has been met
	 */
	boolean verifyPasswordHistory(UserLogin userLogin, String newPassword) {
		// Check to see if minimum period is a requirement first

		if (userLocalConfig.passwordHistoryRetentionCount > 0) {
			// Check to see if the password was used in the past # passwords
			return 0 == PasswordHistory.countByUserLoginAndPassword(userLogin, newPassword,  [max: 10, sort: 'createdDate', order: 'desc'])
		}

		if (userLocalConfig.passwordHistoryRetentionDays > 0) {
			// Check to see if the password was used in the past # of days
			Date dateSince = new Date() - userLocalConfig.passwordHistoryRetentionDays.intValue()
			return 0 == PasswordHistory.countByUserLoginAndPasswordAndCreatedDateGreaterThan(userLogin, newPassword, dateSince, [max: 10])
		}

		return true
	}

	/**
	 * Set the password on a UserLogin object after verifying that the password is legitimate
	 * and meets the password history requirements otherwise it will thrown an exception.
	 * If the password is set we should set also the expirity date of the password using calculatePasswordExpiration
	 * @param userLogin - the UserLogin object to set the password on
	 * @param unhashedPassword - the cleartext password to set
	 * @throws DomainUpdateException
	 */
	@Transactional
	void setUserLoginPassword(UserLogin userLogin, String unhashedPassword, String confirmPassword, Boolean isNewUser=false) throws DomainUpdateException, InvalidParamException {
		// Check for password confirmation
		if (!(unhashedPassword == confirmPassword)) {
			throw new InvalidParamException('The password and the password confirmation do not match')
		}
		// Make sure that the password strength is legitimate
		if (!validPasswordStrength(userLogin.username, unhashedPassword)) {
			throw new InvalidParamException('The new password does not comply with password requirements')
		}

		String currentPassword = userLogin.password
		String hashedPassword = userLogin.applyPassword(unhashedPassword)

		if (currentPassword == hashedPassword) {
			throw new InvalidParamException('New password must be different from the existing one')
		}

		if (!isNewUser && !verifyPasswordHistory(userLogin, hashedPassword)) {
			throw new DomainUpdateException('Please provide a new password that was not previously used')
		}

		//Set the expirity Date when the password changes (from today)
		Date expirityDate = calculatePasswordExpiration(userLogin, new Date())
		if (expirityDate != null) {
			userLogin.setPasswordExpirationDate(expirityDate)
		}
	}

	/**
	 * Used to delete a UserLogin and clear out any references in other tables
	 * @param userLogin - the UserLogin to be deleted
	 */
	@Transactional
	void deleteUserLogin(UserLogin userLogin) {
		auditService.logMessage("deleting user account $userLogin")
		try {
			GormUtil.deleteOrNullDomainReferences(userLogin, true)
		} catch(e) {
			log.error ExceptionUtil.stackTraceToString('deleteUserLogin()',e)
		}
	}

	/**
	 * This action is used to merge two UserLogin according to the following criteria:
	 * 1. If neither account has a UserLogin - nothing to do
	 * 2. If Person being replaced into the master has a UserLogin but master doesn't, assign the UserLogin to the master Person record.
	 * 3. If both Persons have a UserLogin,select the UserLogin that has the most recent login activity. If neither have login activity,
	 *	  choose the oldest login account.
	 * @param fromUserLogin : instance of fromUserLogin
	 * @param toUserLogin : instance of toUserLogin
	 * @param toPerson: instance of toPerson
	 * @return
	 */
	@Transactional
	void mergePersonsUserLogin(UserLogin byWhom, Person fromPerson, Person toPerson) {
		log.debug "mergePersonsUserLogin() entered"
		UserLogin toUserLogin = toPerson.userLogin
		UserLogin fromUserLogin = fromPerson.userLogin

		// Check to see if there is nothing to do be done
		if (! fromUserLogin) {
			return
		}

		// From account exists but To doesn't then just assign the From UserLogin to the To Person
		if (fromUserLogin && ! toUserLogin) {
			auditService.logMessage "$byWhom reassigned user $fromUserLogin to person $toPerson (${toPerson.id})"
			fromUserLogin.person = toPerson
			fromUserLogin.save(flush:true)
			toPerson.userLogin = fromUserLogin
			toPerson.save()
		} else {

			// We must have both a To and From UserLogin so we need to decide which is going to remain
			// and which will be deleted.
			String whichToKeep = keepWhichOnMerge(fromUserLogin, toUserLogin)

			// Helper closure used to replace certain properties from one userLogin to the other
			def replaceUserFieldValues = { from, to ->
				if (from.createdDate < to.createdDate) {
					to.createdDate = from.createdDate
				}
				if (from.expiryDate && (! to.expiryDate || from.expiryDate > to.expiryDate)) {
					to.expiryDate = from.expiryDate
				}
			}

			if (whichToKeep == 'to') {
				GormUtil.mergeDomainReferences(fromUserLogin, toUserLogin, true)
				replaceUserFieldValues(fromUserLogin, toUserLogin)
				auditService.logMessage "$byWhom merged user $fromUserLogin into $toUserLogin"
			} else {
				GormUtil.mergeDomainReferences(toUserLogin, fromUserLogin, true)
				fromUserLogin.person = toPerson
				save fromUserLogin, true
				toPerson.userLogin = fromUserLogin
				save toPerson, true
				replaceUserFieldValues(toUserLogin, fromUserLogin)
				auditService.logMessage "$byWhom merged user $toUserLogin into $fromUserLogin"
			}
		}
	}

	/**
	 * Logic used to determine which UserLogin to keep when merging two persons' UserLogin accounts
	 * @param fromUserLogin - the UserLogin of the From Person
	 * @param toUserLogin - the UserLogin of the To Person in the merge
	 * @return An indicator as to which UserLogin to use [to|from]
	 */
	private String keepWhichOnMerge(UserLogin fromUserLogin, UserLogin toUserLogin) {
		String whichToKeep = ''
		while (true) {
			// Check for the one with the most recent login
			if (fromUserLogin.lastLogin || toUserLogin.lastLogin) {
				if (fromUserLogin.lastLogin && toUserLogin.lastLogin) {
					whichToKeep = (toUserLogin.lastLogin >  fromUserLogin.lastLogin ? 'to' : 'from')
				} else {
					whichToKeep = (fromUserLogin.lastLogin ? 'from' : 'to')
				}
				// println "keepWhichOnMerge() lastLogin check"
				break
			}

			// If one account is active and the other isn't then use the active one
			boolean fromActive = fromUserLogin.userActive()
			boolean toActive = toUserLogin.userActive()
			if ( (fromActive || toActive) && !(fromActive && toActive) ) {
				whichToKeep = (toActive ? 'to' : 'from')
				// println "keepWhichOnMerge() active check"
				break
			}

			// Use the most recently created one
			whichToKeep = (toUserLogin.createdDate >= fromUserLogin.createdDate ? 'to' : 'from')
			// println "keepWhichOnMerge() createdDate check"
			break
		}
		return whichToKeep
	}

	/**
	 * This method is used to update Person reference from 'fromUser' to  the 'toUser'
	 * @param byWhom - the user that is performing the merge
	 * @param fromUser - the user whom references will be overwritten
	 * @param toUser - the user that will assume any person assigned references
	 */
	private void mergeUserLoginDomainReferences(UserLogin byWhom, UserLogin fromUser, UserLogin toUser) {
		GormUtil.mergeDomainReferences(fromUser, toUser, true)
	}

	/**
	 * Update the UserLogin and the associated permissions for the account.
	 * @param params - request params
	 * @param isNewUser - flag true indicating to create otherwise update an existing user
	 * @return The UserLogin object that was created or updated
	 */
	@Transactional
	UserLogin createOrUpdateUserLoginAndPermissions(GrailsParameterMap params, boolean isNewUser)
			throws InvalidParamException, UnauthorizedException {

		if (StringUtil.isBlank((String) params.username)) {
			throw new InvalidParamException('Username should not be empty')
		}

		if (!NumberUtil.isPositiveLong(params.projectId)) {
			throw new InvalidParamException('A project must be selected')
		}

		UserLogin byWhom = getUserLogin()

		Project project = Project.get(params.long('projectId'))
		if (!project) {
			throw new InvalidParamException('Specified project was not found')
		}

		UserLogin userLogin
		Person person
		if (isNewUser) {
			userLogin = new UserLogin()
			if (!NumberUtil.isPositiveLong(params.personId)) {
				throw new InvalidParamException('Person id was missing or invalid')
			}
			person = Person.get(params.long('personId'))
			if (!person) {
				throw new InvalidParamException('Specified person was not found')
			}
			userLogin.person = person

			//if (StringUtil.isBlank(params.password)) {
			//	throw new DomainUpdateException("Password should not be empty")
			//}
		}
		else {
			// Make sure we have a user to edit
			Long userId = NumberUtil.toPositiveLong(params.id, -1L)
			if (userId == -1L) {
				throw new InvalidParamException('User id was missing or invalid')
			}
			userLogin = UserLogin.get(userId)
			if (!userLogin) {
				throw new InvalidParamException('Specified user was not found')
			}
			person = userLogin.person
		}

		personService.hasAccessToPerson(person, true, true)

		// Determine if the specified person can be assigned to the specified project
		// TODO Restore this feature, it disabled temporally by TM-4100
		//if (!personService.getAssignedProjects(person, project)) {
		//	reportViolation("Attempt to assign user $currentUsername ($currentUserLoginId) to an unassociated project $project.id")
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

		boolean isCurrentUserLogin = currentUserLoginId == userLogin.id
		if (!isCurrentUserLogin && !StringUtil.isBlank((String) params.username) && userLogin.username != params.username) {
			UserLogin newUserNameUserLogin = UserLogin.findByUsername(params.username)
			if (newUserNameUserLogin) {
				throw new InvalidParamException("The username you selected is already in use.")
			}

			userLogin.username = params.username
		}

		userLogin.active = params.active

		// Before checking other password flags, Attempt to set the password if it was set
		if (params.password) {
			setUserLoginPassword(userLogin, (String) params.password, (String) params.confirmPassword, isNewUser)
		}

		if (params.isLocal) {
			userLogin.isLocal = true
			userLogin.forcePasswordChange = params.forcePasswordChange ? 'Y' : 'N'
			userLogin.passwordNeverExpires = params.passwordNeverExpires == 'true'

			String email = params.email ?: ''
			for (p in Person.findAllByEmail(email)) {
				if (((Person)p).id != person.id) {
					throw new InvalidParamException('Email "' + email + '" is already in use by another user')
				}
			}
			if (!email || StringUtil.isBlank(email)) {
				throw new InvalidParamException('Invalid email')
			}
			person.email = email
		}
		else {
			userLogin.isLocal = false
			userLogin.forcePasswordChange = 'N'
		}

		// TODO : Try setting the username if it has changed

		// Attempt to deal with parsing the dates
		String dateField
		try {
			if (params.expiryDate) {
				dateField = 'Expiry'
				userLogin.expiryDate = TimeUtil.parseDateTime((String) params.expiryDate)
			}

			if (params.passwordExpirationDate) {
				dateField = 'Password Expiration'
				userLogin.passwordExpirationDate = TimeUtil.parseDateTime((String) params.passwordExpirationDate)
			}

			// TODO : should changing the locked out until be allowed?
			if (params.lockedOutUntil) {
				dateField = 'Locked Out Until'
				userLogin.lockedOutUntil = TimeUtil.parseDateTime((String) params.lockedOutUntil)
			}
		}
		catch (e) {
			throw new InvalidParamException("The $dateField field has invalid format")
		}

		// Try to save the user changes
		if (!userLogin.save(flush: true)) {
			throw new DomainUpdateException("Unable to update User : " + GormUtil.allErrorsString(userLogin))
		}

		// When enabling user - enable Person
		// When disable user - do NOT change Person

		if (userLogin.active == 'Y') {
			person.active = 'Y'
			if (!person.save(flush: true)) {
				throw new DomainUpdateException('Unable to update person : ' + GormUtil.allErrorsString(person))
			}
		}

		//
		// Now lets deal with the permissions
		//
		List<String> assignedRoles
		if (params.assignedRole instanceof String[]) {
			assignedRoles = params.assignedRole as List
		}
		else {
			assignedRoles = [(String) params.assignedRole]
		}

		// Remove any Roles that are not in the above list
		partyRelationshipService.updatePartyRoleByType(RoleType.SECURITY, person, assignedRoles)

		if (setUserRoles(assignedRoles, person.id)) {
			throw new DomainUpdateException('Unable to update user security roles')
		}

		//
		// Set the default project preference for the user
		//
		if (!userPreferenceService.setPreference(userLogin, PREF.CURR_PROJ, (String) params.projectId)) {
			throw new DomainUpdateException('Unable to save selected project')
		}

		if (isNewUser) {
			userPreferenceService.setPreference(userLogin, PREF.START_PAGE, StartPageEnum.USER_DASHBOARD.value)
			// TODO : JPM 3/2016 : Default preferences for user for TZ/Date format should be based on the selected project
			// The date format should be encapsulated or we should add the default Date format on the project as well.
			userPreferenceService.setPreference(userLogin, PREF.CURR_TZ, TimeUtil.defaultTimeZone)
			userPreferenceService.setPreference(userLogin, PREF.CURR_DT_FORMAT, TimeUtil.getDefaultFormatType())

			auditService.saveUserAudit(UserAuditBuilder.newUserLogin(userLogin.username))
		}

		if (params.projectId) {
			personService.addToProject(byWhom, (String) params.projectId, person.id.toString())
		}

		return userLogin
	}

	// ---------------------------------
	// Role Access related methods
	// ---------------------------------

	/**
	 * Lookup a SECURITY RoleType.
	 * @param roleCode - the role type code
	 * @return the role type if it exists or NULL if the code was invalid
	 */
	RoleType getSecurityRoleType(String roleCode) {
		RoleType rt = RoleType.find('from RoleType rt where rt.id=:code and rt.type=:type',
		                            [code: roleCode, type: RoleType.SECURITY])
		if (!rt) {
			log.warn 'getSecurityRoleType() called with invalid code {}', roleCode
		}
		return rt
	}

	/**
	 * Returns the name of a RoleType which currently contains a "GROUP : " prefix that this method strips off
	 */
	String getRoleName(String roleCode) {
		String name = ''
		String roleType = RoleType.get(roleCode)?.description
		// log.error 'getRoleName: roleType={}', roleType
		if (roleType) {
			roleType.substring(roleType.lastIndexOf(':') + 1)
		}
		else {
			name
		}
	}

	/**
	 * Determines if the current user has the specified role.
	 * @param	role	a Roles enum instance
	 * @return 	true if the user has the role
	 */
	boolean hasRole(Permissions.Roles role) {
		hasRole role.name()
	}

	/**
	 * Determines if the current user has the specified role.
	 * @param	role	the role name
	 * @return 	true if the user has the role
	 */
	boolean hasRole(String roleName) {
		assertAuthenticated()
		currentUserDetails.hasRole roleName
	}

	/**
	 * Determine if the current user has a role within an array of roles.
	 * @param roles  the roles
	 * @return true if the user has any of the roles
	 */
	boolean hasRole(List<Permissions.Roles> roles) {
		for (role in roles*.name()) {
			if (hasRole(role)) {
				return true
			}
		}
	}

	/**
	 * Determine if a UserLogin has a particular permission. The reportViolation
	 * parameter when true will report a security violation if the individual does not
	 * have the specified permission.
	 * @param user - the UserLogin object for the given user
	 * @param permission - the permission name string
	 * @param reportIfViolation - a flag to report assess violation if user doesn't have checked perm (default false)
	 * @return boolean true if the user does have permission
	 */
	boolean hasPermission(UserLogin user, String permission, boolean reportIfViolation = false) {
		if (!user) {
			throw new InvalidParamException('hasPermission() called with null UserLogin')
		}
		if (!permission) {
			throw new InvalidParamException('hasPermission() called with null permission code')
		}

		if (user.id == currentUserLoginId) {
			// short-circuit if this is the authenticated user
			return hasPermission(permission, reportIfViolation)
		}

		List<String> roles = getAssignedRoleCodes(user)
		if (roles) {
			Permissions permObj = Permissions.findByPermissionItem(permission)
			if (permObj) {
				if (RolePermissions.findByPermissionAndRoleInList(permObj, roles)) {
					return true
				}
				if (reportIfViolation) {
					reportViolation("attempted action requiring unallowed permission $permission", user.toString())
				}
			} else {
				log.error 'hasPermission() called with unknown permission code {} by {}', permission, user
				reportIfViolation = false 	// disabling because the error log is good enough
			}
		} else {
			log.error 'hasPermission() called by {} that has no assiged roles', user
		}

		if (reportIfViolation) {
			reportViolation('attempted action requiring unallowed permission ' + permission, user.toString())
		}

		false
	}

	/**
	 * Overloaded version of hasPermission used with Person instead of UserLogin
	 */
	boolean hasPermission(Person person, String permission, boolean reportViolation = false) {
		Assert.notNull person, 'getAssignableRoles() called with null Person'

		if (person.id == currentPersonId) {
			// short-circuit if this is the authenticated user
			return hasPermission(permission, reportViolation)
		}

		UserLogin user = person.userLogin
		if (user) {
			hasPermission(user, permission, reportViolation)
		}
	}

	/**
	 * Overload of hasPermission (userLogin, permission, reportViolation) that is used to determine if a the currently logged in user has a particular permission
	 * @param A permission tag name
	 * @param reportIfViolation - flag that when true and the user doesn't have the specified permission a violation is reported (default false)
	 * @return boolean true if the user does have permission
	 */
	boolean hasPermission(String permission, boolean reportIfViolation = false) {
		TdsUserDetails principal = getCurrentUserDetails()
		if (!principal){
			if(reportIfViolation) {
				reportViolation("an unauthenticated person attempted an action requiring '$permission permission")
			}
			return false
		}

		if (principal.hasPermission(permission)) {
			return true
		} else {
			// Validate that the permission requested is valid, otherwise log an error
			List count = Permissions.createCriteria().list {
				projections { count() }
				eq ('permissionItem', permission)
			}
			if (count[0] != 1) {
				log.error 'hasPermission() called with invalid permission code {}', permission
				throw new RuntimeException("Invalid permission code $permission")
			}
		}

		if (reportIfViolation) {
			reportViolation("attempted action requiring unallowed permission $permission", principal.username)
		}
		return false
	}

	/**
	 * Get a list of roles that have been assigned to a user. The roleTypeGroup provides a filtering for the type of Roles that
	 * should be returned (e.g. Staff or System). When a project is presented the method will return roles associate to the project otherwise
	 * it return the user's global role.
	 */
	List<String> getPersonRoles(Person person, RoleTypeGroup roleTypeGroup, Project project = null) {

		List<String> roles = []

		if (project) {
			// Need to lookup the User's Party role to the Project
			// TODO: runbook : getPersonRoles not fully implemented when the project is passed.  Need to test...
			// THIS SHOULD BE LOOKING AT PARTY GROUP, NOT party_relationship - don't use
			String sql = '''SELECT role_type_code_to_id
				FROM party_relationship
				WHERE party_relationship_type_id='PROJ_STAFF' AND party_id_from_id=? AND party_id_to_id=? AND status_code='ENABLED' '''
			// log.error 'getPersonRoles: sql={}', sql
			roles = (List) jdbcTemplate.queryForList(sql, project.client.id, person.id)

			log.error 'Using getPersonRoles in unsupported manor'
			// log.error '*** Getting from PartyRelationship'
		}
		else {
			// Get the User's default role(s)
			for (pr in PartyRole.findAllByParty(person)) {
				roles << ((PartyRole)pr).roleType.id
			}
			// log.error '*** Getting from PartyRole: roles={}', roles
		}
		return roles
	}

	/**
	 * Retrieve the list of all security RoleType objects in the application
	 * sorted with highest privileged role first. The list can be filtered by the level
	 * where by which if the maxLevel parameter is included only a subset of the roles where
	 * the RoleType.level <= to the parameter.
	 * @param maxLevel - used to filter roles by the security level (default null)
	 */
	List<RoleType> getAllRoles(Integer maxLevel=null) {
		if(maxLevel == null){
			maxLevel = Integer.MAX_VALUE
		}

		return RoleType.findAllByTypeAndLevelLessThanEquals(
				RoleType.SECURITY,
				maxLevel,
				[sort: 'level', order: 'desc']
		)
	}

	/**
	 * Retrieve the list of all security RoleType Codes in the application
	 * sorted with highest privileged role first. The list can be filtered by the level
	 * where by which if the maxLevel parameter is included only a subset of the roles where
	 * the RoleType.level <= to the parameter.
	 * @param maxLevel - used to filter roles by the security level (default null)
	 * @return security codes
	 */
	List<String> getAllRoleCodes(Integer maxLevel=null) {
		(List) getAllRoles(maxLevel).collect { it.id }
	}

	/**
	 * The list of security roles a Person is assigned to which is sorted
	 * on the role level DESC
	 * @param person - the Person object to get the assigned roles for
	 * @return the list of Security RoleTypes
	 */
	List<RoleType> getAssignedRoles(Person person) {
		String query = """from RoleType r where r.type = :type and r.id in
			(select pr.roleType.id from PartyRole pr where pr.party=:person group by pr.roleType.id)
			order by r.level desc"""
		RoleType.executeQuery(query, [person: person, type: RoleType.SECURITY])
	}

	/**
	 * The list of security role codes that a person has sort on id ASC
	 * @param person - the person for whom it will lookup assigned roles
	 * @return a list of the role codes
	 */
	List<String> getAssignedRoleCodes(Person person) {
		return getAssignedRoles(person)*.id
	}

	/**
	 * The list of security role codes that a UserLogin has sort on id ASC
	 * @param user - the UserLogin for whom it will lookup assigned roles
	 * @return a list of the role codes
	 */
	List<String> getAssignedRoleCodes(UserLogin user) {
		getAssignedRoleCodes user.person
	}

	/**
	 * The list of security roles that a person can assign base on the individual's highest priviledged
	 * role. The list should include that role plus all lessor roles. If the person doesn't have
	 * the UserEdit permission then no roles are assignable hence an empty list.
	 * @param person - the person for whom we are determining can assign some roles
	 * @return The list of security role that the individual can assign
	 */
	List<RoleType> getAssignableRoles(Person person) {
		Assert.notNull person, 'getAssignableRoles() called with null Person'

		List assignableRoles = []

		if (hasPermission(person, Permission.UserEdit)) {
			RoleType maxRoleOfPerson = getMaxAssignedRole(person)
			if (maxRoleOfPerson) {
				assignableRoles = getAllRoles(maxRoleOfPerson.level)
			}
		}

		return assignableRoles
		/*
			// JPM 4/2016 : this was some of the logic for filter but wasn't used so stripped out
			def assignableRoles = []
			if (person && hasPermission(person, Permission.UserEdit)) {
				// All roles
				def roles = getAllRoles()
				// Assumes getAssignedRoles sorts by level desc.
				def assignedRoles = getAssignedRoles(person)
				// Assigned role with the highest level.
				def maxAssignedLevel = assignedRoles[0].level
				// List of the roles to exclude from the result (assigned role descriptions).
				def excludeRoles = excludeAssigned ? assignedRoles*.description : []
				// Filter the existing roles, obtaining those with lower (or equal)
				//   level and not included in the list of roles to excude.
				assignableRoles = roles.findAll({(it.level <= maxAssignedLevel) && (!excludeRoles.contains(it.description))})
			}
		*/
	}

	/**
	 * The list of security role codes that a person can assign. If the person
	 * doesn't have _UserEdit_ then no roles are returned.
	 */
	List<String> getAssignableRoleCodes(Person person = null) {
		getAssignableRoles(resolve(person))*.id
	}

	/**
	 * Determine if the person has the permissions to assign a given role.
	 * If excludeAssigned, the roles that the user already has are excluded
	 * from the list.
	 */
	boolean isRoleAssignable(Person person, RoleType roleType) {
		return (roleType ? isRoleAssignable(person, roleType.description) : [])
	}

	/**
	 * Overloaded method that looks up the RoleType first. If excludeAssigned then the roles that the user already
	 * has are excluded from the list.
	 */
	boolean isRoleAssignable(Person person, String roleType) {
		def assignableRoleCodes = getAssignableRoleCodes(person)
		if (assignableRoleCodes) {
			return assignableRoleCodes.contains(roleType)
		}
	}

	/**
	 * Returns the highest level security RoleType that the person has been assigned.
	 */
	RoleType getMaxAssignedRole(Person person) {
		getAssignedRoles(person)[0]
	}

	/**
	 * Assign a security role to a person.
	 * @param person - the person to assign the role to
	 * @param roleCode - the role code to assign
	 * @return The PartyRole that was assign or was previously assigned
	 * @throws InvalidParamException - if the role code is invalid
	 */
	PartyRole assignRoleCode(Person person, String roleCode) {
		RoleType rt = RoleType.get(roleCode)
		if (!rt || rt.type != RoleType.SECURITY) {
			throw new InvalidParamException("Invalid role code $roleCode specified")
		}

		// Check to see if the role has already been assigned
		PartyRole pr = PartyRole.findByPartyAndRoleType(person, rt)
		if (pr) {
			log.warn 'assignRoleCode() called to assign code {} to {} but it already exists', roleCode, person
		}
		else {
			pr = new PartyRole(party: person, roleType: rt)
			pr.save(flush: true, failOnError: true)
		}
		return pr
	}

	/**
	 * Assign a security role to a person.
	 * @param person - the person to assign the role to
	 * @param roleCode - the role code to assign
	 * @return The PartyRole that was assign or was previously assigned
	 * @throws InvalidParamException - if the role code is invalid
	 */
	List<PartyRole> assignRoleCodes(Person person, List<String> roleCodes) {
		roleCodes.collect { String rc -> assignRoleCode(person, rc) }
	}

	/**
	 * Unassign a list of security roles for a given person.
	 * @param person - the person to assign the role to
	 * @param roleCodes - the list of role codes to unassign
	 * @return The number of assignments that were removed
	 * @throws InvalidParamException - if the role code is invalid
	 */
	int unassignRoleCodes(Person person, List<String> roleCodes) {
		int deleted = 0
		String query = 'delete PartyRole pr where pr.party=:person and pr.roleType in (:roles)'

		// Iterate over the list of roleCodes and lookup the RoleType for them
		List rts = []
		for (rc in roleCodes) {
			RoleType rt = getSecurityRoleType(rc)
			if (!rt) {
				log.warn 'removeRoleCodes() called with invalid code {} while updating person {}', rc, person
			}
			else {
				rts << rt
			}
		}

		// If we found some
		if (rts) {
			deleted = PartyRole.executeUpdate(query, [person: person, roles: rts])
		}

		log.info 'removeRoleCodes() deleted {} security roles for person {} {}', deleted, person.id, roleCodes
		return deleted
	}

	/**
	 * Calculate when the user's password should expire based on the following rules
	 *    - User must have a local account otherwise return null
	 *    - Previous saved passwordExpirationDate will be used if date is further out than other calculated values
	 *    - If security config maxPasswordAgeDays is set then calculate expiration of asOfDate param or userLogin.passwordChangedDate
	 *    - if passwordNeverExpires is true then return null ????
	 *    - Otherwise use the userLogin.expiryDate
	 * @param userLogin - the user to determine their password expiration
	 * @param asOfDate - the date to consider as the first day that the password was set, default: userLogin.passwordChangedDate
	 * @return the date time that it expires if user has local account. Returns null if passwordNeverExpires.
	 *
	 * Collision of Logic:
	 * 	If We return Null is because is a remote Account??? or the password never expires???
	 * 	If the password never expires should we return
	 */
	Date calculatePasswordExpiration(UserLogin userLogin, Date asOfDate=null) {
		Date expires
		if (userLogin.isLocal) {
			if (!userLogin.passwordNeverExpires) {
				def maxPasswordAgeDays = userLocalConfig.maxPasswordAgeDays
				if (maxPasswordAgeDays > 0) {
					asOfDate = asOfDate ?: userLogin.passwordChangedDate
					expires = asOfDate + maxPasswordAgeDays.intValue()
				}
				else {
					// Set the password expiration to the user's expiry date
					expires = userLogin.expiryDate
				}

				// If the password expiration was manually set futher out then we should honor that date
				if (userLogin.passwordExpirationDate && userLogin.passwordExpirationDate > expires) {
					expires = userLogin.passwordExpirationDate
				}
			}
		}
		return expires
	}

	void requirePermission(String permission, boolean report = false, String violationMessage = null) {
		requirePermission getCurrentUsername(), permission, report, violationMessage
	}

	void requirePermission(String username, String permission, boolean reportViolation = false, String violationMessage = null) {
		requirePermission Collections.singletonList(permission), reportViolation, violationMessage
	}

	void requirePermission(List<String> permissions, boolean report = false, String violationMessage = null) {
		requirePermission '', permissions, report, violationMessage
	}

	void requirePermission(String username /* TODO BB */, List<String> permissions,
	    boolean report = false, String violationMessage = null) {

		for (permission in permissions) {
			if (hasPermission(permission, report)) {
				return
			}
		}

		if (violationMessage) {
			reportViolation violationMessage
		}

		throw new UnauthorizedException('Missing required permission(s): ' + permissions.join(', '))
	}

	private static final List<String> PARTY_CRUD_CONTROLLERS = ['party', 'partyGroup']
	private static final List<String> PARTY_CRUD_ACTIONS = ['create', 'edit', 'save', 'update', 'delete']
	private static final List<String> CRUD_DELETE_CONTROLLER_EXCEPTIONS = ['project', 'userLogin']

	boolean checkAccess(String controllerName, String actionName) {
		// Creating, modifying, or deleting a Party,person, project,partyGroup requires the ADMIN role.
		if (controllerName in PARTY_CRUD_CONTROLLERS && actionName in PARTY_CRUD_ACTIONS) {
			if (!hasRole(ROLE_ADMIN)) {
				// TODO BB do the same as the annotations
				return false
			}
		}

		// for delete require ADMIN role
		if (actionName == 'delete' && !CRUD_DELETE_CONTROLLER_EXCEPTIONS.contains(controllerName)) {
			if (!hasRole(ROLE_ADMIN)) {
				// TODO BB do the same as the annotations
				return false
			}
		}

		true
	}

	boolean viewUnpublished() {
		hasPermission(Permission.TaskPublish) && userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) == 'true'
	}

	@Transactional
	void forcePasswordChange(UserLogin user = null) {
		if(!user){
			user = getUserLogin()
		}
		if (user.forcePasswordChange != 'Y') {
			user.forcePasswordChange = 'Y'
			user.save(failOnError: true)
		}
	}

	/**
	 * @return true if there is a Security Violation
	 */
	@Transactional
	boolean setUserRoles(List<String> roleTypes, long personId) {
		Person person = Person.get(personId)
		for (String roleCode in roleTypes) {
			if (!roleCode) continue

			if (!isRoleAssignable(getUserLoginPerson(), roleCode)) {
				reportViolation(
						"Attempted to update user $person permission to assign security role $roleCode, which is not permissible")
				return true
			}

			RoleType roleType = RoleType.get(roleCode)
			if (!roleType) {
				reportViolation("attempted to update user $person permission with undefined role $roleCode")
				return true
			}

			// Create Role Preferences to User if it doesn't exist
			PartyRole partyRole = PartyRole.findByPartyAndRoleType(person, roleType)
			if (!partyRole) {
				partyRole = new PartyRole(party: person, roleType: roleType)
				if (!partyRole.save()) {
					log.error 'setUserRoles() failed to add partyRole {}: {}', partyRole, GormUtil.allErrorsString(partyRole)
					return true
				}
			}
		}

		false
	}

	/**
	 *  The Roles Available for User
	 */
	List<RoleType> getAvailableRoles(Person person) {
		RoleType.executeQuery('''
			from RoleType
			where id not in (select roleType.id from PartyRole
			                 where party=?
			                 group by roleType.id)
			  and (type = ? OR type = ?)
			order by description
		''', [person, RoleType.TEAM, RoleType.SECURITY])
	}

	@Transactional
	void removeUserRoles(List<String> roleTypes, long personId) {
		for (role in roleTypes) {
			PartyRole.executeUpdate('delete PartyRole where party.id=:personId and roleType=:type',
					[personId: personId, type: role])
		}
	}

	void deleteSecurityRoles(Person person) {
		List<String> toRemoveRoles = []
		for (r in getAssignedRoles(person)) {
			if (r.id in SECURITY_ROLES) {
				toRemoveRoles << r.id
			}
		}
		if (toRemoveRoles) {
			removeUserRoles(toRemoveRoles, person.id)
		}
	}


	/**
	 * assumeUserIdentity provides the ability to load a User's identity into the current
	 * security context by their username along with their security roles
	 * WARNING - this is a major security concern and should ONLY be called for the following
	 * use-cases:
	 *    1. Integration tests that do not have a user session
	 *    2. Quartz Jobs that do not have a user session
	 * @param username - the username of the UserLogin to be loaded into the Spring Security context
	 * @param preventWebInvocation - flag to control if the method can be used from web requests (default false)
	 * @throws UnauthorizedException if invocation is prevented
	 */
	void assumeUserIdentity(final String username, boolean preventWebInvocation=true) {

		if (preventWebInvocation && RequestContextHolder.getRequestAttributes()) {
			reportViolation 'attempted to invoke assumeUserIdentity with ' + username
			throw new UnauthorizedException('Assuming User Identity is not allowed')
		}

		log.info "SECURITY: assumeUserIdentity called for user $username"

		UserDetailsService userDetailsService = ApplicationContextHolder.getBean("userDetailsService")
		UserCache userCache = ApplicationContextHolder.getBean("userCache")
		UserDetails userDetails = userDetailsService.loadUserByUsername(username)

		UsernamePasswordAuthenticationToken token =
			new UsernamePasswordAuthenticationToken(
				userDetails, userDetails.getPassword(), userDetails.getAuthorities() )

		SecurityContextHolder.getContext().setAuthentication(token)

		// removes previous user permission? if loaded
		userCache.removeUserFromCache(username)
	}

	/**
	 * Logout current user
	 */
	void logoutCurrentUser() {
		SecurityContextHolder.getContext().setAuthentication(null)
	}

	/**
	 * Determine if a user account must be locked out based on its inactivity or it is whitelisted
	 * @param userLogin
	 * @return
	 */
	boolean shouldLockoutAccount(UserLogin userLogin) {
		return !isUserInactivityWhiteListed(userLogin.username) && shouldLockoutAccountByInactivityPeriod(userLogin);
	}

	/**
	 * Verifies if a user account is whitelisted regarding inactivity
	 * @param username
	 * @return
	 */
	private boolean isUserInactivityWhiteListed(String username) {
		List<String> whiteListedUserNames = getLoginConfig().inactivityWhitelist
		if (CollectionUtils.isNotEmpty(whiteListedUserNames)) {
			return whiteListedUserNames*.toLowerCase().contains(username.toLowerCase())
		}
		return false
	}

	/**
	 * Determine if a user account should be locked out based on inactivity period
	 * @param userLogin
	 * @return
	 */
	private boolean shouldLockoutAccountByInactivityPeriod(UserLogin userLogin) {
		Date now = TimeUtil.nowGMT()
		Date lastEvent
		if (userLogin.lastLogin) {
			lastEvent = userLogin.lastLogin
		} else {
			lastEvent = userLogin.createdDate
		}
		lastEvent = lastEvent + getLoginConfig().inactiveDaysLockout
		return lastEvent < now
	}

	private Person resolve(Person person) {
		if (!person && loggedIn) {
			loadCurrentPerson()
		}
		else {
			person
		}
	}

	private UserLogin resolve(UserLogin userLogin) {
		if (!userLogin && loggedIn) {
			loadCurrentUserLogin()
		}
		else {
			userLogin
		}
	}

	/**
	 * Returns the permissions of the current User
	 * @return
	 */
    Map currentUserPermissionMap() {
        Set<String> permissions = (getCurrentUserDetails()?.permissions) ?: []

        permissions.collectEntries {
            [(it): 1]
        }
    }
}
