import org.apache.shiro.SecurityUtils

class ProjectService {

    static transactional = true
	def securityService
	def partyRelationshipService
	
/*
 * Returns list of completed Project means projects whose completion time is less than today's date
 */

    def getCompletedProject( timeNow, projectHasPermission, sortOn, orderBy ) {
		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
		def projects = []
		if(projectHasPermission){
			projects = Project.createCriteria().list {
				and {
					lt("completionDate", timeNow)
				}
				order(sortOn, orderBy)
			}
		} else {
			def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
					"and partyIdTo = ${loginUser.person.id} and roleTypeCodeFrom = 'COMPANY' and roleTypeCodeTo = 'STAFF' ")
			def query = "from Project p where p.id in (select pr.partyIdFrom from PartyRelationship pr where "+
					"pr.partyIdTo = ${userCompany?.partyIdFrom?.id} and roleTypeCodeFrom = 'PROJECT') or "+
					"p.client = ${userCompany?.partyIdFrom?.id} order by ${sortOn} ${orderBy}"
			projects = Project.findAll(query).findAll{it.completionDate && it.completionDate.getTime() < timeNow.getTime()}
		}
		
		return  projects
	}

	
	/**
	 * Returns list of Active Projects whose completion time is greater than today's date.
	 * @param Time - timeNow - the time in GMT to filter projects on
	 * @param Boolean viewAllPerm - flag indicating if user has permission to view all projects
	 * @param String sortOn - column to sort on ('name')
	 * @param String orderBy - order to which the sort is done (default 'desc')
	 * @return Project[] - an array of Project objects
	 */
	def getActiveProject( def timeNow, def viewAllPerm, String sortOn='name', String orderBy='desc' ) {
		Person nullPerson = null
		return getActiveProject(timeNow, viewAllPerm, nullPerson, sortOn, orderBy)
	}
	
	/**
	 * Returns list of Active Projects whose completion time is greater than today's date limited to parties for the specified person
	 * @param Time - timeNow - the time in GMT to filter projects on
	 * @param Boolean viewAllPerm - flag indicating if user has permission to view all projects
	 * @param Party party - the Party to filter projects on which is usually the company that a person is associated with
	 * @param String sortOn - column to sort on ('name')
	 * @param String orderBy - order to which the sort is done (default 'desc')
	 * @return Project[] - an array of Project objects
	 */
	def getActiveProject( def timeNow, def viewAllPerm, Person person, String sortOn='name', String orderBy='desc' ) {
		def projects = []
		if (viewAllPerm){
			projects = Project.createCriteria().list {
				and {
					ge("completionDate", timeNow)
				}
				order(sortOn, orderBy)
			}
		} else {
			
			// Lookup the logged in user if a person was not passed to the method
			person = person ?: securityService.getUserLoginPerson() 
			def company = partyRelationshipService.getStaffCompany( person )
			
			/*
						def loginPerson = UserLogin.findByUsername(SecurityUtils.subject.principal)
						def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
								"and partyIdTo = ${loginUser.person.id} and roleTypeCodeFrom = 'COMPANY' and roleTypeCodeTo = 'STAFF' ")
			*/
			def query = "FROM Project p WHERE p.id IN " +
			    "( SELECT pr.partyIdFrom FROM PartyRelationship pr WHERE "+
				"pr.partyIdTo = ${company?.id} and roleTypeCodeFrom = 'PROJECT' )" +
				" OR " +
				"p.client = ${company?.id} order by ${sortOn} ${orderBy}"
			projects = Project.findAll(query)
			if (projects)
				projects = projects.findAll{it.completionDate && it.completionDate.getTime() > timeNow.getTime()}
		}
		
		return projects
	}
	/**
	 * This method is used to get partyRelationShip instance to fetch project manager for requested project.
	 * @param projectId
	 * @return partyRelationShip instance
	 */
	def getProjectManagerByProject(def projectId){
		def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' \
				and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PROJ_MGR' ")
		
		return projectManager
	}
	
	/**
	 * This method is used to get partyRelationShip instance to fetch move manager for requested project.
	 * @param projectId
	 * @return partyRelationShip instance
	 */
	def getMoveManagerByProject (def projectId){
		def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' \
			and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'MOVE_MGR' ")
		return moveManager
	}
}
