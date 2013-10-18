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
	def jdbcTemplate
	
	/*
	 * Returns list of completed Project means projects whose completion time is less than today's date
	 */
    def getCompletedProject( timeNow, projectHasPermission, String sortOn='name',String orderBy='desc', def params = [:]) {
		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
		def projects
		def parties
		def companyId
		
		if(!projectHasPermission){
			def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' \
				and partyIdTo = ${loginUser.person.id} and roleTypeCodeFrom = 'COMPANY' and roleTypeCodeTo = 'STAFF' ")
			companyId = userCompany?.partyIdFrom
			parties = PartyRelationship.executeQuery("SELECT pr.partyIdFrom FROM PartyRelationship pr WHERE \
						pr.partyIdTo = ${companyId?.id} AND pr.roleTypeCodeFrom = 'PROJECT' ")
		}
		
		projects = projectFilter( parties, projectHasPermission, companyId, timeNow, params, "completed")
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
	def getActiveProject( def timeNow, def viewAllPerm, String sortOn='name', String orderBy='desc', def params = [:]) {
		Person nullPerson = null
		return getActiveProject(timeNow, viewAllPerm, nullPerson, sortOn, orderBy, params)
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
	def getActiveProject( def timeNow, def viewAllPerm, Person person, String sortOn='name', String orderBy='desc', def params = [:] ) {
		def projects 
		def company
		def parties
		if(!viewAllPerm){
			person = person ?: securityService.getUserLoginPerson()
			company = partyRelationshipService.getStaffCompany( person )
			parties = PartyRelationship.executeQuery("SELECT pr.partyIdFrom FROM PartyRelationship pr WHERE \
							pr.partyIdTo = ${company?.id} AND pr.roleTypeCodeFrom = 'PROJECT' ")
		}
		
		projects = projectFilter( parties, viewAllPerm, company, timeNow, params, "active")
		
		return projects
	}
	
	/**
	 * Returns list of Active Projects based on the filters selected by the user in projectList
	 * @param parties - list of Party to filter project if user do not have viewAll perm
	 * @param viewAllPerm - perm to view all projects
	 * @param company - company instance associated with user
	 * @param timeNow - current time
	 * @return Project[] - an array list of Project objects
	 */
	def projectFilter( def parties, def viewAllPerm,def company ,def timeNow, def params = [:], def active="active"){
		def maxRows = params.rows ? Integer.valueOf(params.rows) : 25
		def currentPage = params.page ? Integer.valueOf(params.page) : 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		
		def startDates = params.startDate ? Project.findAll("from Project where startDate like '%${params.startDate}%'")?.startDate : []
		def completionDates = params.completionDate ? Project.findAll("from Project where completionDate like '%${params.completionDate}%'")?.completionDate : []
		
		def projects = Project.createCriteria().list(max: maxRows, offset: rowOffset) {
			if (!viewAllPerm){
				or {
					if(parties)
						'in'("id",parties.id)
					eq('client', company)
				}
			}
			if(active == "active"){
				ge("completionDate", timeNow)
			}else{
				lt('completionDate', timeNow)
			}
			if (params.projectCode)
				ilike('projectCode', "%${params.projectCode}%")
			if (params.name)
				ilike('name', "%${params.name}%")
			if (params.comment)
				ilike('comment', "%${params.comment}%")
			if (startDates)
				'in'('startDate' , startDates)
			if (completionDates)
				'in'('completionDate' , completionDates)
				
			order(params.sidx ?: 'projectCode', params.sord ?: 'asc')
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
		def attributes = getAttributes(entityType)
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
		parseData = updateConfigForMissingFields(parseData, entityType)
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
		def phases = ValidationType.getListAsMap().keySet()
		if(data)
			returnMap=JSON.parse(data)
		if(!returnMap){
			def attributes = getAttributes(type)?.attributeCode
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
	/**
	 * Update default importance map for missing fields by assigning normal to all
	 * @param entity type,parseData
	 * @return
	 */
	def updateConfigForMissingFields(parseData, type){
		def fields = getFields(type)
		def phases = ValidationType.getListAsMap().keySet()
		fields.each{f->
		if(!parseData[f.label]){
				def pmap = phases.inject([:]){map, item->
					map[item]="N"
					return map
				}
				def labelMap = ['phase': pmap]
				parseData << [(f.label) : labelMap]
			}
		}
		return parseData
	}
	/**
	 *This method used to get attributes from eavAttribute based on EntityType.
	 * @param entityType
	 * @return
	 */
	def getAttributes(entityType){
		def eavEntityType = EavEntityType.findByDomainName(entityType)
		def attributes = EavAttribute.findAllByEntityType( eavEntityType )
		return attributes
	}

	/**
	 * Used to get the next asset tag for the project
	 * @param Project - the project the project that the asset tag is for
	 * @param AssetEntity - the asset that the tag will be generated for
	 * @return String the actual asset tag
	 */
	def getNewAssetTag( project, asset ) {
		def tag = ''
		if (asset.id) {
			tag = "TDS-${asset.id}"
		} else {
			def lastAssetId = project.lastAssetId
			if (! lastAssetId) {
				lastAssetId = jdbcTemplate.queryForInt("select max(asset_entity_id) FROM asset_entity") + 1
			}
			tag = "TDS-${lastAssetId}"
			project.lastAssetId = ++lastAssetId
		}
		return tag
	} 
}
