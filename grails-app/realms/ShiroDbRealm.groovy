import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.SimpleAccount

import com.tdssrc.grails.HtmlUtil

class ShiroDbRealm {
	
    static authTokenClass = org.apache.shiro.authc.UsernamePasswordToken

    def credentialMatcher

    def authenticate(authToken) {
        log.debug "Attempting to authenticate ${authToken.username} in DB realm..."
        def username = authToken.username

		// log.error "authToken: ${authToken}"
		
        // Null username is invalid
        if (username == null) {
            throw new AccountException('Blank usernames are not allowed by this realm.')
        }

        // Get the user with the given username. If the user is not found or if it is not a local
        // account (meaning using AD or SSO realm) then they don't have an account and we throw an exception.
        def user = UserLogin.findByUsername(username)
        if (!user || !user.isLocal) {
            throw new UnknownAccountException("No account found for user [${username}]")
        }

        log.debug "Found user '${user.username}' in DB"

        // Now check the user's password against the hashed value stored
        // in the database.
        // def account = new SimpleAccount(username, user.password, "jsecDbRealm")
        def account = new SimpleAccount(username, user.password, "shiroDbRealm")
		log.error "credentialMatcher: $credentialMatcher"
		// def account = new SimpleAccount(username,user.passwordHash,new org.apache.shiro.util.SimpleByteSource(user.passwordSalt),"DbRealm")
        if (!credentialMatcher.doCredentialsMatch(authToken, account)) {
			def remoteIp = HtmlUtil.getRemoteIp()
            log.warn "Invalid password (DB realm) - IP ${remoteIp}"
            throw new IncorrectCredentialsException("Invalid password for user '${username}'")
        }

        return account
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
		log.debug "hasRole: userLogin:${userLogin ?: principal}, role:${roleName}, found:{$roles.size()}"
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
		log.debug "hasRole: userLogin:${userLogin ?: principal}, roles:${roles}, found:{$rolesFound.size()}"
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
		log.debug "hasAllRoles: userLogin:${userLogin ?: principal}, role:${roles}, found:{$rolesFound.size()}"
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
