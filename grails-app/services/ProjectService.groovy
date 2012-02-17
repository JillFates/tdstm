import org.jsecurity.SecurityUtils

class ProjectService {

    static transactional = true

    def getCompletedProject( timeNow, projectHasPermission, sort, order ) {
		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
		def projects = []
		if(projectHasPermission){
			projects = Project.createCriteria().list {
				and {
					lt("completionDate", timeNow)
				}
			}
		} else {
			def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
					"and partyIdTo = ${loginUser.person.id} and roleTypeCodeFrom = 'COMPANY' and roleTypeCodeTo = 'STAFF' ")
			def query = "from Project p where p.id in (select pr.partyIdFrom from PartyRelationship pr where "+
					"pr.partyIdTo = ${userCompany?.partyIdFrom?.id} and roleTypeCodeFrom = 'PROJECT') or "+
					"p.client = ${userCompany?.partyIdFrom?.id} order by ${sort} ${order}"
			projects = Project.findAll(query).findAll{it.completionDate && it.completionDate.getTime() < timeNow.getTime()}
		}
		
		return  projects
	}
	def getActiveProject( timeNow, projectHasPermission, sort, order ) {
		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
		def projects = []
		if(projectHasPermission){
			projects = Project.createCriteria().list {
				and {
					ge("completionDate", timeNow)
				}
			}
		} else {
			def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
					"and partyIdTo = ${loginUser.person.id} and roleTypeCodeFrom = 'COMPANY' and roleTypeCodeTo = 'STAFF' ")
			def query = "from Project p where p.id in (select pr.partyIdFrom from PartyRelationship pr where "+
					"pr.partyIdTo = ${userCompany?.partyIdFrom?.id} and roleTypeCodeFrom = 'PROJECT') or "+
					"p.client = ${userCompany?.partyIdFrom?.id} order by ${sort} ${order}"
			projects = Project.findAll(query).findAll{it.completionDate && it.completionDate.getTime() > timeNow.getTime()}
		}
		
		return  projects
	}
}
