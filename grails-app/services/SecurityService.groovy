/**
 * The SecurityService class provides methods to manage User Roles and Permissions, etc.
 * @author pbwebguy
 *
 */

import javax.servlet.http.HttpSession
import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.hash.Sha1Hash
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestContextHolder

import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.SecurityConfigParser
import com.tdsops.tm.enums.domain.RoleTypeGroup
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil

class SecurityService implements InitializingBean {
	
	static transactional = true
	
	// IoC
	def grailsApplication
	def jdbcTemplate
	def auditService

	def ldapConfigMap = [:]
//	def loginConfigMap = [:]
	def loginConfigMap = [usernamePlaceholder:'enter your freaking username', authorityPrompt:'prompt', authorityLabel:'DOMAIN!', authorityName:'TDS']

	/**
	 * This is a post initialization method to allow late configuration settings to occur
	 */
	public void afterPropertiesSet() throws Exception {

		def config = grailsApplication.config

		println "Parsing Security Login setting options"
		loginConfigMap = SecurityConfigParser.parseLoginSettings(config)

		println "Parsing Security LDAP setting options"
		ldapConfigMap = SecurityConfigParser.parseLDAPSettings(config)

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
	boolean hasPermission(UserLogin user, String permission) {
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
			}
		} else {
			log.debug "Unable to find roles for user $user"
		}

		return hasPerm
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
	 * Checks if a combination of a username and password is secure
	 * @param username	
	 * @param password	
	 * @return boolean	returns true if the password is valid
	 */
	def boolean validPassword(String username, String password){
		def requirements = 0;
		def score = 0;
		if (password && username){
			if (password ==~ /.{8}.*/)
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
			if(!password.toLowerCase().contains(username.toLowerCase()))
				score++;
		}
		return score == 3
	}

	/** 
	 * Encrypts a clear text password
	 * @param String password
	 * @return String Encripted passsword
	 */

	 String encrypt(String text) {
		def etext = new Sha1Hash(text).toHex()
		return etext.toString()
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
	 * Used to get a list of system roles.
	 * 
	 * @return List of roles
	 */
	def getSystemRoleTypes() {
		return RoleType.findAll(" from RoleType rt WHERE rt.description like 'System%' ")
	}

}
