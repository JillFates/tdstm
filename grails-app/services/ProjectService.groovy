import org.jsecurity.SecurityUtils

class ProjectService {

    static transactional = true
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
	
/*
 * Returns list of Active Project means projects whose completion time is grater than today's date .
 */

	def getActiveProject( timeNow, projectHasPermission, sortOn, orderBy ) {
		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
		def projects = []
		if(projectHasPermission){
			projects = Project.createCriteria().list {
				and {
					ge("completionDate", timeNow)
				}
				order(sortOn, orderBy)
			}
		} else {
			def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
					"and partyIdTo = ${loginUser.person.id} and roleTypeCodeFrom = 'COMPANY' and roleTypeCodeTo = 'STAFF' ")
			def query = "from Project p where p.id in (select pr.partyIdFrom from PartyRelationship pr where "+
					"pr.partyIdTo = ${userCompany?.partyIdFrom?.id} and roleTypeCodeFrom = 'PROJECT') or "+
					"p.client = ${userCompany?.partyIdFrom?.id} order by ${sortOn} ${orderBy}"
			projects = Project.findAll(query).findAll{it.completionDate && it.completionDate.getTime() > timeNow.getTime()}
		}
		
		return  projects
	}
}
