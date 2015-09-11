import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.UnavailableSecurityManagerException

import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil
import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.security.shiro.UserLoginAccount

import org.codehaus.groovy.grails.commons.GrailsApplication


class ShiroDbRealm {
	
	static authTokenClass = org.apache.shiro.authc.UsernamePasswordToken

	private static final long LOCK_INDEFINTELY_TIME = 1000 * 60 * 60 * 24 * 365 * 100 // 100 years

	def credentialMatcher
	def accessRules

	// Service injection in realm generates a bug in resource plugin, so we need to use GrailsApplication
	GrailsApplication grailsApplication

	public ShiroDbRealm() {
		initRules()
	}

	// Initialize all the rules/actions that should be checked to allow the user login in the system
	// Rules are executed in the order that are added
	private void initRules() {
		accessRules = []

		accessRules << checkLocalSecuvityEnabled
		accessRules << checkValidUsername
		accessRules << checkUserLoginExist
		accessRules << checkExpirationDate
		accessRules << execCredentialMatcher
		accessRules << checkLegacyPassword
		accessRules << checkLockOutStatus
		accessRules << checkPasswordExpirationDate
		accessRules << checkFailsCount
		accessRules << checkPasswordAge
		accessRules << checkIfAuthenticated

	}

	private def getSecurityService() {
		return grailsApplication.mainContext.getBean('securityService')
	}

	private def getAuditService() {
		return grailsApplication.mainContext.getBean('auditService')
	}

	// ----------------------------------------------------------------------------------------
	// Check if user local security is enabled
	private def checkLocalSecuvityEnabled = { state ->
		log.debug "Rule: Check if user local security is enabled"
		if (!getSecurityService().getUserLocalConfig().enabled) {
			throw new UnavailableSecurityManagerException('DB Realm is disabled')
		}
	}

	// ----------------------------------------------------------------------------------------
	// Validates username
	private def checkValidUsername = { state ->
		log.debug "Rule: Validates username"
		if (state.authToken.username == null) {
			throw new AccountException('Blank usernames are not allowed by this realm.')
		}
	}

	// ----------------------------------------------------------------------------------------
	// Get the user with the given username. If the user is not found or if it is not a local
	// account (meaning using AD or SSO realm) then they don't have an account and we throw an exception.
	private def checkUserLoginExist = { state ->
		log.debug "Rule: Get the user with the given username. If the user is not found or if it is not a local"
		state.user = UserLogin.findByUsername(state.authToken.username)
		if (!state.user || !state.user.isLocal) {
			throw new UnknownAccountException("No account found for user [${state.authToken.username}]")
		}
		log.debug "Found user '${state.user.username}' in DB"
	}

	// ----------------------------------------------------------------------------------------
	// Check user login expiration date
	private def checkExpirationDate = { state ->
		log.debug "Rule: Check user login expiration date"
		if (state.user.expiryDate.time <= TimeUtil.nowGMT().time) {
			throw new UnknownAccountException("Account expired for user [${state.user.username}]")
		}
	}

	// ----------------------------------------------------------------------------------------
	// Now check the user's password against the hashed value stored in the database.
	// def account = new SimpleAccount(username, user.password, "jsecDbRealm")
	private def execCredentialMatcher = { state ->
		log.debug "Rule: Now check the user's password against the hashed value stored in the database."
		def credentialsSalt
		state.account = new UserLoginAccount(state.user.username, state.user.password, "shiroDbRealm")
		state.account.saltPrefix = state.user.saltPrefix 
		
		log.debug "credentialMatcher: $credentialMatcher"
		// def account = new SimpleAccount(username,user.passwordHash,new org.apache.shiro.util.SimpleByteSource(user.passwordSalt),"DbRealm")
		state.authenticated = credentialMatcher.doCredentialsMatch(state.authToken, state.account)
	}

	// ----------------------------------------------------------------------------------------
	// Check using legacy password encryption
	private def checkLegacyPassword = { state ->
		log.debug "Rule: Check using legacy password encryption"
		if ( !state.authenticated ) {
			if (SecurityUtil.encryptLegacy(new String(state.authToken.password)).equals(state.user.password) ) {
				state.authenticated = true
				if (getSecurityService().getUserLocalConfig().forceUseNewEncryption) {
					// Set the userLogin so that they're forced to change their password
					state.user.forcePasswordChange = 'Y'
					state.user.save(flush:true)
				}
			}
		}
	}

	// ----------------------------------------------------------------------------------------
	// Check lock out status
	private def checkLockOutStatus = { state ->
		log.debug "Rule: Check lock out status"
		if (state.authenticated) {
			if (state.user.lockedOutUntil) {
				// Check to clear password lockouts
				if ( (state.user.lockedOutUntil.time <= TimeUtil.nowGMT().time) && 
					 (getSecurityService().getUserLocalConfig().failedLoginLockoutPeriodMinutes > 0)
				) {
					// Unlock out user because lock out time expired
					state.user.lockedOutUntil = null
					state.user.failedLoginAttempts = 0
					state.user.save(flush:true)
				} else {
					getAuditService().logMessage("User ${state.user} attempted access while account is locked")
					state.authenticated = false
					def timeElapsed = TimeUtil.elapsed(TimeUtil.nowGMT(), state.user.lockedOutUntil)
					throw new UnknownAccountException("Your account has been locked due to excessive login failure attempts. It will unlock in $timeElapsed.  You may contact support to expedite unlocking your account if you wish.")			
				}
			} 
		}
	}

	// ----------------------------------------------------------------------------------------
	// Check password expiration date
	private def checkPasswordExpirationDate = { state ->
		log.debug "Rule: Check password expiration date"
		if ( state.authenticated &&
			 state.user.passwordExpirationDate &&
			 state.user.passwordExpirationDate.time <= TimeUtil.nowGMT().time
			) {
				// Set the userLogin so that they're forced to change their password
				state.user.forcePasswordChange = 'Y'
				state.user.save(flush:true)
				state.authenticated = true
		}
	}

	// ----------------------------------------------------------------------------------------
	// Check and increment max fails, lock out on fail
	private def checkFailsCount = { state ->
		log.debug "Rule: Check and increment max fails, lock out on fail"
		if (!state.authenticated) {
			state.user.failedLoginAttempts++
			def maxLoginFailureAttempts = getSecurityService().getUserLocalConfig().maxLoginFailureAttempts
			if ( (maxLoginFailureAttempts > 0) &&
				 (state.user.failedLoginAttempts > maxLoginFailureAttempts)
			) {
				String lockoutTime = 'indefintely'
				def failedLoginLockoutPeriodMinutes = getSecurityService().getUserLocalConfig().failedLoginLockoutPeriodMinutes * 60 * 1000
				def lockedOutUntil
				if (failedLoginLockoutPeriodMinutes > 0) {
					lockedOutUntil = new Date(TimeUtil.nowGMT().time + failedLoginLockoutPeriodMinutes)
					lockoutTime = "for ${failedLoginLockoutPeriodMinutes} minutes"
				} else {
					lockedOutUntil = new Date(TimeUtil.nowGMT().time + LOCK_INDEFINTELY_TIME)
				}
				state.user.failedLoginAttempts = 0
				state.user.lockedOutUntil = lockedOutUntil
				getAuditService().logMessage("User ${state.user}, account locked out $lockoutTime")
			}
			state.user.save(flush:true)
		} else {
			if (state.user.failedLoginAttempts > 0) {
				state.user.failedLoginAttempts = 0
				state.user.save(flush:true)
			}
		}
	}

	// ----------------------------------------------------------------------------------------
	// Required Password Change Every # Days
	private def checkPasswordAge = { state ->
		log.debug "Rule: Required Password Change Every # Days"
		def maxPasswordAgeDays = getSecurityService().getUserLocalConfig().maxPasswordAgeDays
		if ( state.authenticated &&
			 (maxPasswordAgeDays > 0) &&
			 ((state.user.passwordChangedDate.time + (maxPasswordAgeDays * 24 * 60 * 60 * 1000)) < TimeUtil.nowGMT().time)
			) {
				// Set the userLogin so that they're forced to change their password
				state.user.forcePasswordChange = 'Y'
				state.user.save(flush:true)
				state.authenticated = true
		}
	}

	// ----------------------------------------------------------------------------------------
	// Not authenticated (This should be the last rule)
	private def checkIfAuthenticated = { state ->
		log.debug "Rule: Not authenticated (This should be the last rule)."
		if (!state.authenticated) {
			def remoteIp = HtmlUtil.getRemoteIp()
			log.warn "Invalid password (DB realm) - IP ${remoteIp}"
			throw new IncorrectCredentialsException("Invalid password for user '${state.authToken.username}'")
		}
	}

	def authenticate(authToken) {
		log.debug "Attempting to authenticate ${authToken.username} in DB realm..."
		// log.error "authToken: ${authToken}"

		// Defines the state that will be use to validate all the rules
		def state = [
			authenticated: false, 
			user: null,
			account: null, 
			authToken: authToken
		]
		
		// Validate all rules/actions
		accessRules.each { rule ->
			rule(state)
		}

		if (state.authenticated) {
			return state.account
		} else {
			// This should never happen because a previous rule should throw an exception on fail.
			throw new AccountException("Can't authenticate user.")
		}
	}

	/**
	 * Used to determine if a principal (aka user) has a specified role name
	 * @param String principal	- username
	 * @param String roleName - role name (e.g. ADMIN)
	 * @return Boolean	true if user has the role
	*/
	def hasRole(principal, roleName) {
		def roles = []
		def userLogin = UserLogin.findByUsername(principal)
		if( userLogin ){
			def person = userLogin.person
			def roleType = RoleType.read(roleName)
			roles = PartyRole.findAllByPartyAndRoleType(person,roleType)
		}
		// log.debug "hasRole: userLogin:${userLogin ?: principal}, role:${roleName}, found:{$roles.size()}"
		return roles.size() > 0
	}

	/**
	 * Used to determine if a principal (aka user) has a specified role name
	 * @param String principal	- username
	 * @param String roleName - role name (e.g. ADMIN)
	 * @return Boolean	true if principal has the role
	*/
	def hasAnyRole(principal, roles) {
		def userLogin = UserLogin.findByUsername(principal)
		if( userLogin ){
			def person = userLogin.person
			def roleType = RoleType.read(roleName)
			rolesFound = PartyRole.findAllByPartyAndRoleTypeInList(person,roles)
		}
		//log.debug "hasRole: userLogin:${userLogin ?: principal}, roles:${roles}, found:{$rolesFound.size()}"
		return rolesFound.size() > 0
	}

	/**
	 * Used to determine if a principal has all roles specified in an array of roles
	 * @param principal
	 * @param roles
	 * @return Boolean true if principal has all roles
	 */
	def hasAllRoles(principal, roles) {
		def userLogin = UserLogin.findByUsername(principal)
		if( userLogin ){
			def person = userLogin.person
			def roleType = RoleType.read(roleName)
			rolesFound = PartyRole.findAllByPartyAndRoleTypeInList(person,roles)
		}
		//log.debug "hasAllRoles: userLogin:${userLogin ?: principal}, role:${roles}, found:{$rolesFound.size()}"
		return rolesFound.size() == roles.size
	}
	
	// method to get the permission, currently code is commented 
	def isPermitted(principal, requiredPermission) {/*
		// Does the user have the given permission directly associated
		// with himself?
		//
		// First find all the permissions that the user has that match
		// the required permission's type and project code.
		def criteria = JsecUserPermissionRel.createCriteria()
		def permissions = criteria.list {
			user {
				eq('username', principal)
			}
			permission {
				eq('type', requiredPermission.class.name)
			}
		}

		// Try each of the permissions found and see whether any of
		// them confer the required permission.
		def retval = permissions?.find { rel ->
			// Create a real permission instance from the database
			// permission.
			def perm = null
			def constructor = findConstructor(rel.permission.type)
			if (constructor.parameterTypes.size() == 2) {
				perm = constructor.newInstance(rel.target, rel.actions)
			}
			else if (constructor.parameterTypes.size() == 1) {
				perm = constructor.newInstance(rel.target)
			}
			else {
				log.error "Unusable permission: ${rel.permission.type}"
				return false
			}

			// Now check whether this permission implies the required
			// one.
			if (perm.implies(requiredPermission)) {
				// User has the permission!
				return true
			}
			else {
				return false
			}
		}

		if (retval != null) {
			// Found a matching permission!
			return true
		}

		// If not, does he gain it through a role?
		//
		// First, find the roles that the user has.
		def user = UserLogin.findByUsername(principal)
		def roles = JsecUserRoleRel.findAllByUser(user)

		// If the user has no roles, then he obviously has no permissions
		// via roles.
		if (roles.isEmpty()) return false

		// Get the permissions from the roles that the user does have.
		criteria = JsecRolePermissionRel.createCriteria()
		def results = criteria.list {
			'in'('role', roles.collect { it.role })
			permission {
				eq('type', requiredPermission.class.name)
			}
		}

		// There may be some duplicate entries in the results, but
		// at this stage it is not worth trying to remove them. Now,
		// create a real permission from each result and check it
		// against the required one.
		retval = results.find { rel ->
			def perm = null
			def constructor = findConstructor(rel.permission.type)
			if (constructor.parameterTypes.size() == 2) {
				perm = constructor.newInstance(rel.target, rel.actions)
			}
			else if (constructor.parameterTypes.size() == 1) {
				perm = constructor.newInstance(rel.target)
			}
			else {
				log.error "Unusable permission: ${rel.permission.type}"
				return false
			}

			// Now check whether this permission implies the required
			// one.
			if (perm.implies(requiredPermission)) {
				// User has the permission!
				return true
			}
			else {
				return false
			}
		}

		if (retval != null) {
			// Found a matching permission!
			return true
		}
		else {
			return false
		}
	*/}

	// method to create constructor
	def findConstructor(className) {/*
		// Load the required permission class.
		def clazz = this.class.classLoader.loadClass(className)

		// Check the available constructors. If any take two
		// string parameters, we use that one and pass in the
		// target and actions string. Otherwise we try a single
		// parameter constructor and pass in just the target.
		def preferredConstructor = null
		def fallbackConstructor = null
		clazz.declaredConstructors.each { constructor ->
			def numParams = constructor.parameterTypes.size()
			if (numParams == 2) {
				if (constructor.parameterTypes[0].equals(String) &&
						constructor.parameterTypes[1].equals(String)) {
					preferredConstructor = constructor
				}
			}
			else if (numParams == 1) {
				if (constructor.parameterTypes[0].equals(String)) {
					fallbackConstructor = constructor
				}
			}
		}

		return (preferredConstructor != null ? preferredConstructor : fallbackConstructor)
	*/}
}
