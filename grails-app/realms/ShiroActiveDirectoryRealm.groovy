import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.CredentialsException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.UnavailableSecurityManagerException

import com.tdsops.common.security.ConnectorActiveDirectory
import com.tdsops.common.security.shiro.MissingCredentialsException
import com.tdsops.common.security.shiro.UnhandledAuthException

import com.tdssrc.grails.HtmlUtil
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.SecurityConfigParser

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Shiro Realm for Active Directory authentication
 */
class ShiroActiveDirectoryRealm {
	
	static final authTokenClass = org.apache.shiro.authc.UsernamePasswordToken
	//private static final grailsApplication = ApplicationHolder.application

	static boolean initialized = false
	static config
	static Log log
	static isEnabled = false
	def ctx

	def credentialMatcher

	public ShiroActiveDirectoryRealm() {
		//initialize()
	}

	/**
	 * Used to load the configuration settings one-time
	 */
	private synchronized void initialize() {
		// isEnabled will get flipped to false if there is no config or AD is disabled
		if (! initialized ) {
			initialized = true
			log = LogFactory.getLog(this.class)
			if (log.isDebugEnabled()) 
				log.debug "initialize()"

			ctx = ApplicationContextHolder.getApplicationContext()
			def securityService = ctx.securityService
			if (securityService) {
				config = securityService.getLDAPConfig()

				// Log the loaded configuration if debugging is enabled
				if (config.debug) {
					java.lang.StringBuffer sb = new java.lang.StringBuffer('ShiroActiveDirectoryRealm() Loaded Configuration:')
					config.each {k,v ->
						if (k=='domains') {
							config.domains.each { dk, domain -> 
								sb.append("\n\tdomains.$dk:")
								domain.each { domainKey, domainValue ->
									if (domainKey.toLowerCase().contains('password'))
										domainValue = '************'
									sb.append("\n\t\t$domainKey=$domainValue") 
								}
							}
						} else {
							sb.append("\n\t$k=$v")
						}
					}
					log.info sb.toString()
				}

				isEnabled = ( config && config.enabled && SecurityConfigParser.hasActiveDirectoryDomain(config) ) 

			} else {
				log.error "ShiroActiveDirectoryRealm: Unable to access security service"			
			}

			if (! config)
				log.error "ShiroActiveDirectoryRealm: Unable to load security configuration settings"					
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
		String logPrefix = 'authenticate()'

		if (! initialized)
			initialize()

		if (! isEnabled) {
			throw new UnavailableSecurityManagerException('Active Directory Realm is disabled')
		}

		Map userInfo
		String username = authToken.username
		String authority = authToken.host?.toLowerCase()
		String msg

		// The password is a char[] in AuthToken so we convert to a string
		// TODO : Security doc mentions that keeping the password as char[] is safer for in-memory 
		def password = authToken.password
		password=password?.toString() 

		// User must enter a username and password for their credentials
		if ( ! username?.size() || ! password?.size() ) {
			throw new MissingCredentialsException('Both the username and password are required')
		}

		// Try to validate the domain using authority which is passed around in the Host
		if (! authToken.host) {
			log.info "$logPrefix Expected authToken.host/authority was missing from request"
			throw new UnhandledAuthException('Authentication authority is missing')
		}

		// Validate that the host is specified in the configuration
		if (! config.domains.containsKey(authToken.host.toLowerCase())) {
			msg = "Invalid authority (${authToken.host}) was specified"
			log.error "$logPrefix $msg"
			throw new UnhandledAuthException(msg)
		}

		if (log.isDebugEnabled() || config.debug)
			log.debug "$logPrefix About to try authenticating $username"

		// Try calling ActiveDirectory to Authenticate the user and get their information
		userInfo = ConnectorActiveDirectory.getUserInfo(authority, username, password, config)
		if (config.debug) {
			log.info "$logPrefix Authenticated user '${userInfo.username}' in AD"
		}

		// Now attempt to lookup the user or provision the Person+UserLogin accordingly
		def userLogin 
		try {
			userLogin = ctx.userService.findOrProvisionUser(userInfo, config, authority)
		} catch (e) {
			log.error "$logPrefix UserService.findOrProvisionUser failed : ${e.getMessage()}"
			throw new DisabledAccountException('Unable to find or provision your TransitionManager account')
		}

		// Ok so check a few things about the account
		if (! userLogin.securityRoleCodes) {
			log.warn "$logPrefix User $username has no assigned roles"
			throw new DisabledAccountException('Your account has no assigned security role')
		}

		// Create a SimpleAccount to hand back to Shiro
		def account = new SimpleAccount(userLogin.username, userLogin.password, 'ShiroActiveDirectoryRealm')

		return account
	}

}