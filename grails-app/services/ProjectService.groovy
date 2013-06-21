import org.apache.shiro.SecurityUtils
import grails.converters.JSON
import com.tds.asset.FieldImportance
import com.tdsops.tm.enums.domain.ValidationType;
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavEntityType

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
	/**
	 * To action is used to get the fields
	 *@param : entityType type of entity.
	 *@return 
	 */
	def getFields(def entityType){
		def project = securityService.getUserCurrentProject()
		def eavEntityType = EavEntityType.findByDomainName(entityType)
		def attributes = EavAttribute.findAllByEntityType( eavEntityType )
		def returnMap = attributes.collect{ p->
			return ['id':(p.attributeCode.contains('custom') && project[p.attributeCode])? project[p.attributeCode]:p.frontendLabel, 'label':p.attributeCode]
		}
		return returnMap
	}
	
	/**
	 * This action useed to get the config from field importance Table.
	 * @param entity type
	 * @return
	 */
	def getConfigByEntity(def entityType){
		def project = securityService.getUserCurrentProject()
		def parseData= [:]
		def data = FieldImportance.findByProjectAndEntityType(project,entityType)?.config
		if(data)
			parseData=JSON.parse(data)
		if(!parseData){
			parseData = generateDefaultConfig(entityType)
		}
		
		return parseData
	}
	/**
	 * Create default importance map by assigning normal to all
	 * @param entity type
	 * @return
	 */
	def generateDefaultConfig(def type){
		def defautlProject = Project.findByProjectCode("TM_DEFAULT_PROJECT")
		def returnMap = [:]
		def data = FieldImportance.findByProjectAndEntityType(defautlProject,type)?.config
		if(data)
			returnMap=JSON.parse(data)
		if(!returnMap){
			def eavEntityType = EavEntityType.findByDomainName(type)
			def attributes = EavAttribute.findAllByEntityType( eavEntityType )?.attributeCode
			def phases = ValidationType.getListAsMap().keySet()
			returnMap = attributes.inject([:]){rmap, field->
				def pmap = phases.inject([:]){map, item->
					map[item]="N"
					return map
				}
				rmap[field] = ['phase': pmap]
				return rmap
			}
		}
		return returnMap
	}
}
