import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.CredentialsException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.SimpleAccount

import com.tdsops.common.security.ConnectorActiveDirectory
//import org.codehaus.groovy.grails.commons.ApplicationHolder

import com.tdssrc.grails.HtmlUtil
import com.tdsops.common.grails.ApplicationContextHolder

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Shiro Realm for Active Directory authentication
 */
class ShiroActiveDirectoryRealm {
	
	static final authTokenClass = org.apache.shiro.authc.UsernamePasswordToken
	//private static final grailsApplication = ApplicationHolder.application

	static config
	static Log log
	static isEnabled = true
	def ctx

	def credentialMatcher

	/**
	 * Used to load the configuration settings one-time
	 */
	private synchronized void initialize() {
		// isEnabled will get flipped to false if there is no config or AD is disabled
		if (isEnabled && ! config ) {
			log = LogFactory.getLog(this.class)
			if (log.isDebugEnabled()) 
				log.debug "ShiroActiveDirectoryRealm: initializing"

			ctx = ApplicationContextHolder.getApplicationContext()
			def securityService = ctx.securityService
			if (securityService) {
				config = securityService.getActiveDirectoryConfig()
				if (log.isDebugEnabled()) 
					log.debug "ShiroActiveDirectoryRealm: loaded configuration $config"
				isEnabled = ( config && config.enabled ) 
			} else {
				log.error "ShiroActiveDirectoryRealm: Unable to access security service"			
			}

			if (! config)
				log.error "ShiroActiveDirectoryRealm: Unable to load security settings"					
		}
	}

	/**
	 * The authenticate method invoked by Shiro to verify that the user has authenicated correctly
	 * We will auto-provision the user based on the AD configuration settings
	 * @param authToken - the token passed in by Shiro
	 * @return A SimpleAccount object populated with the user's name, etc
	 * @throws IncorrectCredentialsException, UnknownAccountException, AccountException, AuthenticationException
	 * UnknownAccountException - when not auto provisioning and got a non-found account
	 */
	def authenticate(authToken) {
		if (isEnabled && ! config)
			initialize()

		if (! isEnabled) {
			// todo : switch to more appropriate exception after upgrade of shiro
			throw new AccountException('Active Directory Realm is disabled')
		}

		def userInfo
		def username = authToken.username
		def password = authToken.password

		// TODO : Some reason the password is screwy/encoded (perhaps it has something to do with authTokenClass)
		password=password.toString() 

		// Null username is invalid
		if (! username || ! password) {
			throw new AccountException('Blank username and/or passwords are not allowed')
		}

		if (log.isDebugEnabled() || config.debug)
			log.debug "ShiroActiveDirectoryRealm: About to try authenticating $username"

		// Try calling ActiveDirectory to Authenticate the user and get their information
		try {
			userInfo = ConnectorActiveDirectory.getUserInfo(username, password, config)
		} catch (RuntimeException e) {
			def remoteIp = HtmlUtil.getRemoteIp()
			log.warn "Login attempt failed (ShiroActiveDirectoryRealm) : user $username : IP ${remoteIp}"

			if (e.getMessage() == 'Username not found')
				throw new UnknownAccountException(e.getMessage())
			else 
				throw new IncorrectCredentialsException(e.getMessage())
		}

		if (log.isDebugEnabled() || config.debug)
			log.debug "ShiroActiveDirectoryRealm: Found user '${userInfo.username}' in Active Directory"

		// Now attempt to lookup the user or provision accordingly
		// Note that the findOrProvisionUser method can throw errors that Shiro will catch
		def userLogin

		def userService = ctx.userService
		userLogin = userService.findOrProvisionUser( userInfo, config )

		if (log.isDebugEnabled() || config.debug)
			log.debug "ShiroActiveDirectoryRealm: Returned with user '${userLogin.username}'"

		// Create a SimpleAccount to hand back to Shiro
		def account = new SimpleAccount(userLogin.username, userLogin.password, 'ShiroActiveDirectoryRealm')

		return account
	}

}