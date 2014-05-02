import grails.converters.JSON

import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetType
import com.tds.asset.FieldImportance
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavEntityType
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetTransition
import com.tds.asset.AssetDependencyBundle

class ProjectService {

	static transactional = true
	def securityService
	def partyRelationshipService
	def jdbcTemplate
	def stateEngineService
	def userPreferenceService

	/**
	 * Returns a list of projects that the user has access to. If showAllProjPerm is true then the user has access to all
	 * projects and the list will be filtered by the projectState and possibly the pagination params. If showAllProjPerm
	 * is false then the list will be restricted to those that the user has been assigned to via a relation in the
	 * PartyRelationship table.
	 *
	 * @param userLogin - the user to lookup projects for
	 * @param showAllProjPerm - flag if the user has the ShowAllProject permission (default false)
	 * @param projectStatus - the status of the project, options [any | active | completed] (default any)
	 * @param sortOn - field used to sort, could be name or projectCode
	 * @param sortOrder - sort order, could be asc or desc
	 * @return list of projects
	 */
	List<Project> getUserProjectsOrderBy(UserLogin userLogin, Boolean showAllProjPerm=false, ProjectStatus projectStatus=ProjectStatus.ANY, ProjectSortProperty sortOn = ProjectSortProperty.NAME, SortOrder sortOrder = SortOrder.ASC) {
		def searchParams = [:]
		searchParams.sortOn = sortOn
		searchParams.sortOrder = sortOrder
		return getUserProjects(userLogin, showAllProjPerm, projectStatus, searchParams)
	}

	/** 
	 * Returns a list of projects that the user has access to. If showAllProjPerm is true then the user has access to all 
	 * projects and the list will be filtered by the projectState and possibly the pagination params. If showAllProjPerm
	 * is false then the list will be restricted to those that the user has been assigned to via a relation in the 
	 * PartyRelationship table.
	 *
	 * @param userLogin - the user to lookup projects for
	 * @param showAllProjPerm - flag if the user has the ShowAllProject permission (default false)
	 * @param projectStatus - the status of the project, options [any | active | completed] (default any)
	 * @param params - parameters to manage the resultset/pagination [maxRows, currentPage, sortOn, orderBy]
	 * @return list of projects
	 */
	List<Project> getUserProjects(UserLogin userLogin, Boolean showAllProjPerm=false, ProjectStatus projectStatus=ProjectStatus.ANY, Map searchParams=[:]) {
		def projects = []
		def projectIds = []
		def timeNow = new Date() 
		
		searchParams=searchParams?:[:]
		def maxRows = searchParams.maxRows ? Integer.valueOf(searchParams.maxRows) : Project.count()
		def currentPage = searchParams.currentPage ? Integer.valueOf(searchParams.currentPage) : 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def sortOn = searchParams.sortOn?:ProjectSortProperty.PROJECT_CODE
		def sortOrder = searchParams.sortOrder?:SortOrder.ASC
		def projParams=searchParams.params?: [:]

		// If !showAllProjPerm, then need to find distinct project ids where the PartyRelationship.partyIdTo.id = userLogin.person.id
		// and PartyRelationshipType=PROJ_STAFF and RoleTypeCodeFrom=PROJECT
		if (!showAllProjPerm) {
			projectIds = PartyRelationship.executeQuery("SELECT pr.partyIdFrom.id FROM PartyRelationship pr WHERE \
				pr.partyIdTo = ${userLogin.person.id} AND pr.roleTypeCodeFrom = 'PROJECT' AND pr.partyRelationshipType = 'PROJ_STAFF' ")
			if (!projectIds) return projects;
		}

		def startDates = projParams.startDate ? Project.findAll("from Project where startDate like '%${projParams.startDate}%'")?.startDate : []
		def completionDates = projParams.completionDate ? Project.findAll("from Project where completionDate like '%${projParams.completionDate}%'")?.completionDate : []
		// if !showAllProjPerm then filter in('id', userProjectIds)
		// If projectState = active, filter ge("completionDate", timeNow)
		// If projectState = completed then filter lt('completionDate', timeNow)
		// if params has pagination params, then add to the filtering
		projects = Project.createCriteria().list(max: maxRows, offset: rowOffset) {
			if (projectIds){
				'in'("id", projectIds)
			}
			if(projParams.projectCode)
				ilike('projectCode', "%${projParams.projectCode}%")
			if(projParams.name)
				ilike('name', "%${projParams.name}%")
			if(projParams.comment)
				ilike('comment', "%${projParams.comment}%")
			if (startDates)
				'in'('startDate' , startDates)
			if (completionDates)
				'in'('completionDate' , completionDates)
				
			if (projectStatus != ProjectStatus.ANY) { 
				if(projectStatus == ProjectStatus.ACTIVE){
					ge("completionDate", timeNow)
				}else{
					lt('completionDate', timeNow)
				}
			}
			order(sortOn.toString(), sortOrder.toString())
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
	 * This action is used to get the fields, splitted fields in to two to handle common customs.
	 *@param : entityType type of entity.
	 *@return 
	 */
	def getFields(def entityType){
		def project = securityService.getUserCurrentProject()
		def attributes = getAttributes(entityType)
		def returnMap = attributes.findAll{!(it.attributeCode.contains('custom'))}.collect{ p->
			return ['id':p.frontendLabel, 'label':p.attributeCode]
		}
		return returnMap
	}
	/**
	 * This action is used to get the custom fields
	 *@return 
	 */
	def getCustoms(){
		def project = securityService.getUserCurrentProject()
		def attributes = getAttributes('Application')
		def returnMap = attributes.findAll{it.attributeCode.contains('custom')}.collect{ p->
			return ['id':project[p.attributeCode] ?: p.frontendLabel, 'label':p.attributeCode]
		}
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
		def fields = getFields(type) + getCustoms()
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
		def attributes = EavAttribute.findAllByEntityType( eavEntityType ,[sort:'frontendLabel'])
		(1..Project.CUSTOM_FIELD_COUNT).each {i->
		    def attribute = attributes.find{it.frontendLabel == "Custom"+i}
			attributes.remove(attribute)
			attributes.add(attribute)
		}
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
	/**
	 * NOTE : use this method where ever we are getting project partner.
	 * This method is used to get project partner  for requested project.
	 * @param projectId
	 * @return projectPartner
	 */
	def getProjectPartner( project ) {
		def projectPartner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' \
			and p.partyIdFrom = :project and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PARTNER' ",
			[project:project])
		return projectPartner
	}
	/**
	 * This method gets the data used to populate the project summary report
	 * @param params
	 * @return map
	 */
	def getProjectReportSummary( params ) {
		
		def projects = []
		
		// check if either of the active/inactive checkboxes are checked
		if( params.active || params.inactive ) {
			def query = new StringBuffer(""" SELECT *, totalAssetCount-filesCount-dbCount-appCount AS assetCount FROM
				(SELECT p.project_id AS projId, p.project_code AS projName, p.client_id AS clientId,
					(SELECT COUNT(*) FROM move_event me WHERE me.project_id = p.project_id) AS eventCount,
					COUNT(IF(ae.asset_type = "${AssetType.FILES}",1,NULL)) AS filesCount, 
					COUNT(IF(ae.asset_type = "${AssetType.DATABASE}",1,NULL)) AS dbCount, 
					COUNT(IF(ae.asset_type = "${AssetType.APPLICATION}",1,NULL)) AS appCount,
					COUNT(*) AS totalAssetCount,
					DATE(p.start_date) AS startDate,
					DATE(p.completion_date) AS completionDate,
					pg.name AS clientName,
					pg2.name AS partnerName,
					p.description AS description
					FROM asset_entity ae
					LEFT JOIN move_bundle mb ON (mb.move_bundle_id = ae.move_bundle_id) 
						AND ((ae.move_bundle_id = NULL) OR (mb.use_for_planning = true))
					LEFT JOIN project p ON (p.project_id = ae.project_id)
					LEFT JOIN party_group pg ON (pg.party_group_id = p.client_id)
					LEFT JOIN party_relationship pr ON (pr.party_relationship_type_id = 'PROJ_PARTNER' AND pr.party_id_from_id = p.project_id 
						AND pr.role_type_code_from_id = 'PROJECT' AND pr.role_type_code_to_id = 'PARTNER')
					LEFT JOIN party_group pg2 ON (pg2.party_group_id = pr.party_id_to_id) """)
			
			// handle active/inactive project specification
			if ( params.inactive && ! params.active )
				query.append(" WHERE CURDATE() > p.completion_date ")
			if ( params.active && ! params.inactive )
				query.append(" WHERE CURDATE() < p.completion_date ")
			
			query.append(""" GROUP BY ae.project_id
					) inside
				ORDER BY inside.projName """)
			projects = jdbcTemplate.queryForList(query.toString())
			
			// add the staff count to each project
			projects.each {
				it["staffCount"] = partyRelationshipService.getCompanyStaff(it["clientId"]).size()
			}
		}
		
		return projects
	}
	/**
	 * This method used to get all clients,patners,managers and workflowcodes.
	 */
	def getProjectPatnerAndManagerDetails(){
		def tdsParty = PartyGroup.findByName( 'TDS' )
		
		def clients = partyRelationshipService.getCompanyClients(tdsParty)//	Populate a SELECT listbox with default list as earlier.
		def partners = partyRelationshipService.getCompanyPartners(tdsParty)
		
		//	Populate a SELECT listbox with a list of all STAFF relationship to COMPANY (TDS)
		def managers = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = ${tdsParty.id} and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
		managers?.sort{it.partyIdTo?.lastName}
		
		def workflowCodes = stateEngineService.getWorkflowCode()
		
		return [ clients:clients, partners:partners, managers:managers, workflowCodes: workflowCodes ]
	}
	/**
	 * This method used to get all clients,patners,managers and workflowcodes for action edit.
	 */
	def getprojectEditDetails(projectInstance,prevParam){
		def currProj = userPreferenceService.getSession().getAttribute("CURR_PROJ");
		def currProjectInstance = Project.get( currProj.CURR_PROJ )
		def loginPerson = securityService.getUserLoginPerson()
		def userCompany = partyRelationshipService.getStaffCompany( loginPerson )

		userPreferenceService.setPreference( "PARTYGROUP", "${userCompany?.id}" )
		
		def projectLogo
		if (currProjectInstance) {
			projectLogo = ProjectLogo.findByProject(currProjectInstance)
		}
		def imageId
		if (projectLogo) {
			imageId = projectLogo.id
		}
		userPreferenceService.getSession().setAttribute("setImage",imageId)
		def projectLogoForProject = ProjectLogo.findByProject(projectInstance)
		def partnerStaff
		def projectCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_COMPANY' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'COMPANY' ")
		//def projectClient = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_CLIENT' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'CLIENT' ")
		def projectPartner = getProjectPartner( projectInstance )
		def projectPartnerId
		if (prevParam.projectPartner){
			projectPartnerId = prevParam.projectPartner
		} else {
			projectPartnerId = projectPartner?.partyIdTo?.id
		}
		def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PROJ_MGR' ")
		def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'MOVE_MGR' ")
		def companyStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectCompany.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
		companyStaff.each {
			if ( it.partyIdTo.lastName == null ) {
				it.partyIdTo.lastName = ""
			}
		}
		companyStaff.sort{it.partyIdTo.lastName}
		def clientStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectInstance.client.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
			clientStaff.each {
			if ( it.partyIdTo.lastName == null ) {
				it.partyIdTo.lastName = ""
			}
		}
		clientStaff.sort{it.partyIdTo.lastName}
		def companyPartners = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PARTNERS' and p.partyIdFrom = $projectCompany.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' order by p.partyIdTo" )
		companyPartners.sort{it.partyIdTo.name}
		if (projectPartner != null) {
			partnerStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectPartnerId and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
			partnerStaff.each {
				if ( it.partyIdTo.lastName == null ) {
					it.partyIdTo.lastName = ""
				}
			}
			partnerStaff.sort{it.partyIdTo.lastName}
		}
		clientStaff.each{staff->
		}
		def workflowCodes = stateEngineService.getWorkflowCode()
		return [projectPartner:projectPartner, projectManager:projectManager, moveManager:moveManager,
			companyStaff:companyStaff, clientStaff:clientStaff, partnerStaff:partnerStaff, companyPartners:companyPartners,
			projectLogoForProject:projectLogoForProject, workflowCodes:workflowCodes ]
	}
	
	/*
	 *The UserPreferenceService.removeProjectAssociates is moved here and renamed as deleteProject
	 *@param project
	 *@param UserLogin
	 *@return message
	 */
	def deleteProject( Project projectInstance, UserLogin userLogin) throws UnauthorizedException {
		def message
		def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
		def projects = getUserProjects(securityService.getUserLogin(), projectHasPermission)
		
		if (!RolePermissions.hasPermission('ProjectDelete')) {
			throw new UnauthorizedException('You do not have permission to delete projects')
		}
		
		if(!(projectInstance in projects)){
			throw new UnauthorizedException('You do not have access to the specified project')
		}
		
		// remove preferences
		def bundleQuery = "select mb.id from MoveBundle mb where mb.project = ${projectInstance.id}"
		def eventQuery = "select me.id from MoveEvent me where me.project = ${projectInstance.id}"
		UserPreference.executeUpdate("delete from UserPreference up where up.value = ${projectInstance.id} or up.value in ($bundleQuery) or up.value in ($eventQuery) ")
		//remove the AssetEntity
		def assetsQuery = "select a.id from AssetEntity a where a.project = ${projectInstance.id}"
		
		ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset in ($assetsQuery)")
		AssetComment.executeUpdate("delete from AssetComment ac where ac.assetEntity in ($assetsQuery)")
		AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar av where av.assetEntity in ($assetsQuery)")
		AssetTransition.executeUpdate("delete from AssetTransition at where at.assetEntity in ($assetsQuery)")
		ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.project = ${projectInstance.id}")
		AssetCableMap.executeUpdate("delete AssetCableMap where assetFrom in ($assetsQuery)")
		AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
										assetToPort=null where assetTo in ($assetsQuery)""")
		ProjectTeam.executeUpdate("Update ProjectTeam pt SET pt.latestAsset = null where pt.latestAsset in ($assetsQuery)")
		
		AssetEntity.executeUpdate("delete from AssetEntity ae where ae.project = ${projectInstance.id}")
		TaskBatch.executeUpdate("delete from TaskBatch tb where tb.project = ${projectInstance.id}")
		
		// remove DataTransferBatch
		def batchQuery = "select dtb.id from DataTransferBatch dtb where dtb.project = ${projectInstance.id}"
		
		DataTransferComment.executeUpdate("delete from DataTransferComment dtc where dtc.dataTransferBatch in ($batchQuery)")
		DataTransferValue.executeUpdate("delete from DataTransferValue dtv where dtv.dataTransferBatch in ($batchQuery)")
		
		DataTransferBatch.executeUpdate("delete from DataTransferBatch dtb where dtb.project = ${projectInstance.id}")
		
		// remove Move Bundle
		
		AssetEntity.executeUpdate("Update AssetEntity ae SET ae.moveBundle = null where ae.moveBundle in ($bundleQuery)")
		AssetTransition.executeUpdate("delete from AssetTransition at where at.moveBundle in ($bundleQuery)")
		StepSnapshot.executeUpdate("delete from StepSnapshot ss where ss.moveBundleStep in (select mbs.id from MoveBundleStep mbs where mbs.moveBundle in ($bundleQuery))")
		MoveBundleStep.executeUpdate("delete from MoveBundleStep mbs where mbs.moveBundle in ($bundleQuery)")
		
		def teamQuery = "select pt.id From ProjectTeam pt where pt.moveBundle in ($bundleQuery)"
		PartyRelationship.executeUpdate("delete from PartyRelationship pr where pr.partyIdFrom in ( $teamQuery ) or pr.partyIdTo in ( $teamQuery )")
		PartyGroup.executeUpdate("delete from Party p where p.id in ( $teamQuery )")
		Party.executeUpdate("delete from Party p where p.id in ( $teamQuery )")
		ProjectTeam.executeUpdate("delete from ProjectTeam pt where pt.moveBundle in ($bundleQuery)")
		
		PartyRelationship.executeUpdate("delete from PartyRelationship pr where pr.partyIdFrom in ($bundleQuery) or pr.partyIdTo in ($bundleQuery)")
		Party.executeUpdate("delete from Party p where p.id in ($bundleQuery)")
		MoveBundle.executeUpdate("delete from MoveBundle mb where mb.project = ${projectInstance.id}")
		
		// remove Move Event
		MoveBundle.executeUpdate("Update MoveBundle mb SET mb.moveEvent = null where mb.moveEvent in ($eventQuery)")
		MoveEventNews.executeUpdate("delete from MoveEventNews men where men.moveEvent in ($eventQuery)")
		MoveEventSnapshot.executeUpdate("delete from MoveEventSnapshot mes where mes.moveEvent in ($eventQuery)")
		
		MoveEvent.executeUpdate("delete from MoveEvent me where me.project = ${projectInstance.id}")
		
		// remove Project Logo
		ProjectLogo.executeUpdate("delete from ProjectLogo pl where pl.project = ${projectInstance.id}")
		// remove party relationship
		PartyRelationship.executeUpdate("delete from PartyRelationship pr where pr.partyIdFrom  = ${projectInstance.id} or pr.partyIdTo = ${projectInstance.id}")
		
		// remove associated references e.g. Room, Rack FI, AssetDepBundles, KeyValue .
		Room.executeUpdate("delete from Room r where r.project  = ${projectInstance.id}")
		Rack.executeUpdate("delete from Rack ra where ra.project  = ${projectInstance.id}")
		AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle adb where adb.project = ${projectInstance.id}")
		FieldImportance.executeUpdate("delete from FieldImportance fi where fi.project  = ${projectInstance.id}")
		KeyValue.executeUpdate("delete from KeyValue kv where kv.project  = ${projectInstance.id}")
		
		Model.executeUpdate("update Model mo set mo.modelScope = null where mo.modelScope  = ${projectInstance.id}")
		ModelSync.executeUpdate("update ModelSync ms set ms.modelScope = null where ms.modelScope  = ${projectInstance.id}")
		
		return message
	}
}