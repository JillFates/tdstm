/**
 * The SecurityService class provides methods to manage User Roles and Permissions, etc.
 * @author pbwebguy
 *
 */

import javax.servlet.http.HttpSession
import org.springframework.web.context.request.RequestContextHolder
import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.hash.Sha1Hash
import com.tdsops.tm.enums.domain.RoleTypeGroup
 
import org.springframework.beans.factory.InitializingBean
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.exceptions.ConfigurationException

class SecurityService implements InitializingBean {
	
	static transactional = true
	
	// IoC
	def grailsApplication
	def jdbcTemplate

	def activeDirectoryConfigMap = [:]

	/**
	 * This is a post initialization method to allow late configuration settings to occur
	 */
	public void afterPropertiesSet() throws Exception {

		// Initialize the ActiveDirectory Configuration Map 
		def conf = grailsApplication.config?.tdstm?.security?.ad
		if (conf) {
			activeDirectoryConfigMap.with {
				// A way to enable/disable the account quickly
				enabled = conf.containsKey('enabled') ? conf.enabled : false
				// Flag to indicate the connector type that these settings are used with (ActiveDirectory)
				connector = 'AD'	
				url = conf.url ?: []
				domain = conf.domain ?: ''
				searchBase = conf.searchBase ?: ''
				// groupBase = conf.groupBase ?: ''
				baseDN = conf.baseDN
				roleMap = conf.roleMap ?: []
				username = conf.username ?: ''
				password = conf.password ?: ''
				company = conf.company ?: ''
				autoProvision = conf.containsKey('autoProvision') ? conf.autoProvision : false
				updateUser = conf.containsKey('updateUser') ? conf.updateUser : false
				updateRoles = conf.containsKey('updateRoles') ? conf.updateRoles : false 
				defaultRole = conf.defaultRole ?: ''
				defaultTimezone = conf.defaultTimezone ?: 'EST'
				defaultProject = conf.defaultProject ?: ''
				debug = conf.containsKey('debug') ? conf.debug : false
			}
		} else {
			// log has not yet been injected into the object so we need to use println...
			println 'ERROR - SecurityService: unable to locate Active Directory configuration settings tdstm.ad.*'
		}

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
	 * Used to get the UserLogin object of the currently logged in user
	 * @return UserLogin object or null if user is not logged in
	 */
	def getUserLogin() {
		def subject = SecurityUtils.subject
		def principal = subject.principal
		def userLogin
		if (principal)
			userLogin = UserLogin.findByUsername( principal )
		if (log.isDebugEnabled())
			log.debug "getUserLogin: principal=${principal} userLogin=${userLogin}"
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
	 * Used to retrieve the Active Directory integration configuration settings
	 * @return A map consisting of all of the settings defined in the configuration or various defaults
	 */
	Map getActiveDirectoryConfig() {
		// log.debug "Calling AfterPropertiesSet()"
		// afterPropertiesSet()
		return activeDirectoryConfigMap
	}
	
}
