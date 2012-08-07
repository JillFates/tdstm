/**
 * The SecurityService class provides methods to manage User Roles and Permissions, etc.
 * @author pbwebguy
 *
 */

 import javax.servlet.http.HttpSession
 import org.springframework.web.context.request.RequestContextHolder
 import org.jsecurity.SecurityUtils
 
 import com.tdsops.tm.enums.domain.RoleTypeGroup
 
 class SecurityService {
	
	static transactional = true
	
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
			
			def sql = """SELECT role_type_code_to_id
				FROM party_relationship
				WHERE party_relationship_type_id='PROJ_STAFF' AND party_id_from_id=${client.id} AND party_id_to_id=${person.id} AND status_code='ENABLED'"""
			// log.error "getPersonRoles: sql=${sql}"
			roles = dbcTemplate.queryForList(sql)
			// log.error "*** Getting from PartyRelationship"
			
		} else {
			// Get the User's default role(s)
			PartyRole.findAllByParty( person ).each() {
				roles << it.roleType.id
			}	
			// log.error "*** Getting from PartyRole: roles=${roles}"
		}	
		return roles	
	}
	
	/** 
	 * Used to get user's current project
	 */
	def getUserCurrentProject() {
		def project
		def projectId = RequestContextHolder.currentRequestAttributes().getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if ( projectId ) {
			project = Project.get( projectId )
		}
		return project
	}
	
	/**
	 * Used to get the UserLogin object of the currently logged in user
	 * @return UserLogin object or null if user is not logged in
	 */
	def getUserLogin() {
		def subject = SecurityUtils.subject
		def principal = subject.principal
		def userLogin
		if (principal) {
			userLogin = UserLogin.findByUsername( principal )
		}
		// log.error "getUserLogin: principal=${principal} userLogin=${userLogin}"
		return userLogin
	}

	/**
	 * Used to get the person (Party) object associated with the currently logged in user	
	 * @return Party object of the user or null if not logged in
	 */
	def getUserLoginPerson() {
		def userLogin = getUserLogin()
		def party = userLogin?.person
		return party
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
}
