import grails.converters.JSON

import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetType
import com.tds.asset.FieldImportance
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import com.tdsops.tm.enums.domain.ValidationType
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavEntityType
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetDependencyBundle
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.WorkbookUtil
import net.transitionmanager.ProjectDailyMetric
import java.text.DateFormat
import java.text.SimpleDateFormat

class ProjectService {

	static transactional = true
	def securityService
	def partyRelationshipService
	def jdbcTemplate
	def stateEngineService
	def userPreferenceService
	def sequenceService

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



	List getStaffList(onlyAssigned, role, projects, companies, sorting){
		
		def query = new StringBuffer("""
			SELECT * FROM (
				SELECT pr.party_id_to_id AS personId, CONCAT(IFNULL(p.first_name, ''), ' ', IFNULL(CONCAT(p.middle_name, ' '), ''), IFNULL(p.last_name, '')) AS fullName, CONCAT('[',pg.name,']') AS company, 
					pr.role_type_code_to_id AS role, SUBSTRING(rt.description, INSTR(rt.description, ":")+2) AS team, p.last_name AS lastName, 
					pr2.party_id_to_id IS NOT NULL AS project, IFNULL(CONVERT(GROUP_CONCAT(mes.move_event_id) USING 'utf8'), 0) AS moveEvents, IFNULL(CONVERT(GROUP_CONCAT(DATE_FORMAT(ed.exception_day, '%Y-%m-%d')) USING 'utf8'),'') AS unavailableDates 
				FROM tdstm.party_relationship pr 
					LEFT OUTER JOIN person p ON p.person_id = pr.party_id_to_id 
					LEFT OUTER JOIN exception_dates ed ON ed.person_id = p.person_id 
					LEFT OUTER JOIN party_group pg ON pg.party_group_id = pr.party_id_from_id 
					LEFT OUTER JOIN role_type rt ON rt.role_type_code = pr.role_type_code_to_id 
					LEFT OUTER JOIN party_relationship pr2 ON pr2.party_id_to_id = pr.party_id_to_id 
						AND pr2.role_type_code_to_id = pr.role_type_code_to_id 
						AND pr2.party_id_from_id IN (${projects}) 
						AND pr2.role_type_code_from_id = 'PROJECT'
					LEFT OUTER JOIN move_event_staff mes ON mes.person_id = p.person_id 
						AND mes.role_id = pr.role_type_code_to_id 
				WHERE pr.role_type_code_from_id IN ('COMPANY') 
					AND pr.party_relationship_type_id IN ('STAFF', 'PROJ_PARTNER') 
					AND pr.party_id_from_id IN (${companies})
                    AND p.active = 'Y' 
				GROUP BY role, personId 
				ORDER BY fullName ASC 
			) AS companyStaff 
			WHERE 1=1
		""")

		if (onlyAssigned == '1'){
			query.append("AND companyStaff.project = 1 ")
		}

		if (role != '0')
			query.append("AND companyStaff.role = '${role}'")
			
		query.append(" ORDER BY ${sorting}")

		return jdbcTemplate.queryForList(query.toString())
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
		def personId = searchParams.personId?:userLogin.person.id
		def person = Person.get(personId)
		def companyParty = partyRelationshipService.getStaffCompany( person )

		// If !showAllProjPerm, then need to find distinct project ids where the PartyRelationship.partyIdTo.id = userLogin.person.id
		// and PartyRelationshipType=PROJ_STAFF and RoleTypeCodeFrom=PROJECT
		if (!showAllProjPerm) {
			def projQuery = "SELECT pr.partyIdFrom.id FROM PartyRelationship pr WHERE \
				( (pr.partyIdTo = ${personId} AND pr.partyRelationshipType = 'PROJ_STAFF') OR \
				  (pr.partyIdTo = ${companyParty.id} AND pr.partyRelationshipType = 'PROJ_COMPANY') ) \
				  AND pr.roleTypeCodeFrom = 'PROJECT' "
			projectIds = PartyRelationship.executeQuery(projQuery)
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
	 * This method is used to get partyRelationShip instance to fetch project managers for requested project.
	 * @param projectId
	 * @return partyRelationShip instance
	 */
	def getProjectManagersByProject(def projectId){
		def projectManagers = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' \
				and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PROJ_MGR' ")

		return projectManagers
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
		def defaultProject = Project.findByProjectCode("TM_DEFAULT_PROJECT")
		def returnMap = [:]
		def data = FieldImportance.findByProjectAndEntityType(defaultProject,type)?.config
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
	 * Used to get next asset tag based on client id of project
	 * Internally uses sequenceService to generate assetTag
	 * @param project
	 * @return newly formatted assetTag
	 */
	def getNextAssetTag( project ){
		return "TDS-"+ String.format("%05d", sequenceService.next(project.clientId, 'AssetTag'))
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
			tag = getNextAssetTag(project) 
		}
		return tag
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
					COUNT(IF(ae.asset_type = "${AssetType.SERVER}",1,NULL)) AS totalServCount,
					COUNT(IF(ae.asset_type = "${AssetType.SERVER}" and mb.use_for_planning ,1,NULL)) AS activeServCount,
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
		def person = securityService.getUserLogin().person
		def companyParty = partyRelationshipService.getStaffCompany( person )

		if (!companyParty) {
			companyParty = PartyGroup.findByName( "TDS" )
		}

		def clients = partyRelationshipService.getCompanyClients(companyParty)//	Populate a SELECT listbox with default list as earlier.
		def partners = partyRelationshipService.getCompanyPartners(companyParty)		
		def workflowCodes = stateEngineService.getWorkflowCode()
		
		return [ clients:clients, partners:partners, workflowCodes: workflowCodes ]
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
		def projectPartners = partyRelationshipService.getProjectPartners( projectInstance )
		def projectManagers = getProjectManagersByProject(projectInstance.id)
		def workflowCodes = stateEngineService.getWorkflowCode()
		def companyPartners = partyRelationshipService.getCompanyPartners( projectCompany.partyIdTo )
		return [projectPartners:projectPartners, projectManagers:projectManagers,
			companyPartners:companyPartners,
			projectLogoForProject:projectLogoForProject, workflowCodes:workflowCodes ]
	}

	/*
	 *The UserPreferenceService.removeProjectAssociates is moved here and renamed as deleteProject
	 *@param project
	 *@param includeProject indicates if should be deleted the project too
	 *@return message
	 */
	def deleteProject(prokectId, includeProject=false) throws UnauthorizedException {
		def message
		def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
		def projects = getUserProjects(securityService.getUserLogin(), projectHasPermission)
		def projectInstance = Project.get(prokectId)
		
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
		ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.project = ${projectInstance.id}")
		AssetCableMap.executeUpdate("delete AssetCableMap where assetFrom in ($assetsQuery)")
		AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
										assetToPort=null where assetTo in ($assetsQuery)""")
		ProjectTeam.executeUpdate("Update ProjectTeam pt SET pt.latestAsset = null where pt.latestAsset in ($assetsQuery)")
		
		AssetEntity.executeUpdate("delete from AssetEntity ae where ae.project = ${projectInstance.id}")
		AssetComment.executeUpdate("delete from AssetComment ac where ac.project = ${projectInstance.id}")
		TaskBatch.executeUpdate("delete from TaskBatch tb where tb.project = ${projectInstance.id}")
		
		// remove DataTransferBatch
		def batchQuery = "select dtb.id from DataTransferBatch dtb where dtb.project = ${projectInstance.id}"
		
		DataTransferComment.executeUpdate("delete from DataTransferComment dtc where dtc.dataTransferBatch in ($batchQuery)")
		DataTransferValue.executeUpdate("delete from DataTransferValue dtv where dtv.dataTransferBatch in ($batchQuery)")
		
		DataTransferBatch.executeUpdate("delete from DataTransferBatch dtb where dtb.project = ${projectInstance.id}")
		
		// remove Move Bundle
		
		AssetEntity.executeUpdate("Update AssetEntity ae SET ae.moveBundle = null where ae.moveBundle in ($bundleQuery)")
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

		def recipesQuery = "select r.id from Recipe r where r.project.id = ${projectInstance.id}"
		Recipe.executeUpdate("update Recipe r set r.releasedVersion=null where r.project.id = ${projectInstance.id}")
		def recipeVersions = RecipeVersion.find("from RecipeVersion rv where rv.recipe.id in ($recipesQuery)")
		if (recipeVersions) {
			recipeVersions.each {
				RecipeVersion.executeUpdate("update RecipeVersion rv set rv.clonedFrom=null where rv.clonedFrom.id = $it.id")
			}
		}
		RecipeVersion.executeUpdate("delete from RecipeVersion rv where rv.recipe.id in ($recipesQuery)")
		Recipe.executeUpdate("delete from Recipe r where r.project.id  = ${projectInstance.id}")

		PartyGroup.executeUpdate("delete from Party p where p.id = ${projectInstance.id}")
		Party.executeUpdate("delete from Party p where p.id = ${projectInstance.id}")

		if (includeProject) {
			Project.executeUpdate("delete from Project p where p.id = ${projectInstance.id}")
		}

		return message
	}
	
	/**
	 * Used retrieve the default Bundle configured for the project or create one if it does not exist
	 * @param project
	 * @return MoveBundle - the default bundle assigned to the project or will create it on the fly
	 */
	MoveBundle getDefaultBundle(Project project ) {
		return project.defaultBundle ?: createDefaultBundle( project )
	}
	
	/**
	 * Method is used to create createDefaultBundle  
	 * @param project
	 * @return project's default move bundle 
	 */
	MoveBundle createDefaultBundle (Project project ) {
		def defaultCode = 'TBD'
		// TODO : JPM 7/2014 - we could run into two separate processes attempting to create the default project at the same time so a lock should be implemented
		if(!project.defaultBundle){
			def moveBundle = MoveBundle.findByNameAndProject(defaultCode, project)
			if( moveBundle )
				return moveBundle
			else
				moveBundle = new MoveBundle(
					name:defaultCode, 
					project:project, 
					useForPlanning:true, 
					workflowCode:project.workflowCode,
					startTime: project.startDate,
					completionTime:project.completionDate
				)
				
			if (!moveBundle.save(flush:true)){
				log.error "createDefaultBundle: failed to create DefaultBundle for project $project: ${GormUtil.allErrorsString(moveBundle)}"
				return null
			} 
			return moveBundle
		}
	}

	def updateProjectPartners(projectInstance, partnersIds) {
		// Get current partners in the database
		def currentPartnersIds = []
		def currentPartners = partyRelationshipService.getProjectPartners(projectInstance)
		if (currentPartners) {
			currentPartners.each{ p ->
				currentPartnersIds << p.id
			}
		}
		// Convert selected partners to long
		def newPartnersIds = []
		if (partnersIds) {
			partnersIds.each{ p ->
				if (!StringUtil.isBlank(p)) {
					newPartnersIds << NumberUtil.toLong(p)	
				}
			}
		}
		// Define which partners should be deleted because are not in the new list
		def toDeletePartners = currentPartnersIds - newPartnersIds
		// Define which partners should be created because are new in the list
		def toAddPartners = newPartnersIds - currentPartnersIds
		def partnerParty = null

		// Add partners to the relationship 
		toAddPartners.each{ partnerId ->
			partnerParty = Party.findById(partnerId)
			partyRelationshipService.savePartyRelationship("PROJ_PARTNER", projectInstance, "PROJECT", partnerParty, "PARTNER" )
		}
		
		// Delete partners from the relationship
		def partnerStaff
		toDeletePartners.each{ partnerId ->
			partnerParty = Party.findById(partnerId)
			def query = "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = :partnerParty and " +
				"p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'"
			partnerStaff = PartyRelationship.findAll( query, [partnerParty:partnerParty] )
			if (partnerStaff.size() > 0) {
				def idsToDelete = []
				partnerStaff.each{ ps ->
					idsToDelete << ps.partyIdTo.id
				}
				def idsToDeleteValue = idsToDelete.join(",")
				jdbcTemplate.update("DELETE FROM party_relationship WHERE party_relationship_type_id = 'PROJ_STAFF' AND " +
						"party_id_from_id = $projectInstance.id AND role_type_code_from_id = 'PROJECT' AND party_id_to_id IN ($idsToDeleteValue)")
			}

			partyRelationshipService.deletePartyRelationship("PROJ_PARTNER", projectInstance, "PROJECT", partnerParty, "PARTNER" )
		}
	}

	def saveProject(projectInstance, file, projectPartners, projectManager) {

		def workflowCodes = []
		//projectInstance.dateCreated = new Date()
		//  When the Start date is initially selected and Completion Date is blank, set completion date to the Start date
		//Get the Partner Image file from the multi-part request

		// List of OK mime-types
		def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
		if( file && file.getContentType() && file.getContentType() != "application/octet-stream" ){
			if (! okcontents.contains(file.getContentType())) {
				return [message: "Image must be one of: ${okcontents}", success: false]
			}		
		}

		//save image
		def image
		image = ProjectLogo.fromUpload(file)		   
		image.project = projectInstance
		def imageSize = image.getSize()
		if( imageSize > 50000 ) {
			return [message: " Image size is too large. Please select proper Image", success: false]
		}
		
		if ( !projectInstance.hasErrors() && projectInstance.save() ) {
			if(file && file.getContentType() == "application/octet-stream"){
				//Nonthing to perform.
			} else {
				image.save()
			}
			//def client = params.projectClient
		
			def person = securityService.getUserLogin().person
			def companyParty = partyRelationshipService.getStaffCompany( person )
			if (!companyParty) {
				companyParty = PartyGroup.findByName( "TDS" )
			}

			// For Project to Company PartyRelationship
			def projectCompanyRel = partyRelationshipService.savePartyRelationship("PROJ_COMPANY", projectInstance, "PROJECT", companyParty, "COMPANY" )

			def partnersIds
			if (projectPartners instanceof String) {
				partnersIds = [projectPartners]
			} else {
				partnersIds = projectPartners
			}

			updateProjectPartners(projectInstance, partnersIds)
			
			if ( projectManager != null && projectManager != "" ) {
				
				def projectManagerParty = Party.findById(projectManager)
				//	For Project to ProjectManager PartyRelationship
				def projectManagerRel = partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectInstance, "PROJECT", projectManagerParty, "PROJ_MGR" )  
			}
			
			// set the projectInstance as CURR_PROJ
			userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )	
			//Will create a bundle name TBD and set it as default bundle for project   
			projectInstance.getProjectDefaultBundle()
			
			return [message: "Project ${projectInstance} created", success: true, imageId: image.id]
		} else {
			return [message: "", success: false]
		}
	}

	/**
	 * This function is used by the daily project metrics job to generate daily metrics.
	 * It search for active projects and for each one retrives specific metrics: assets, deps, users, tasks
	 */
	def activitySnapshot(params) {
		log.info "Project Daily Metrics started."

		// Date format for SQLs
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		def startDate = findProjectDailyMetricsLastRunDay().clearTime()
		def endDate = new Date().clearTime()

		log.info "Project Daily Metrics will run from $startDate to $endDate"

		def metricsByProject
		def metrics
		def projects
		def sqlSearchDate

		for (searchDate in startDate..endDate) {

			sqlSearchDate = formatter.format(searchDate)

			log.info "Project Daily Metrics. Processing date: $sqlSearchDate"

			metricsByProject = [:]
			metrics = []
			projects = []

			// ***************************
			// Retrieve assets information
			fillAssetsMetrics(metrics, metricsByProject, projects, searchDate, sqlSearchDate)

			// ***************************
			// Retrieve tasks information
			fillTasksMetrics(metricsByProject, sqlSearchDate)

			// *********************************
			// Retrieve dependencies information
			fillDependenciesMetrics(metricsByProject, sqlSearchDate)

			// **************************************
			// Retrieve person/user login information
			fillUsersMetrics(metrics, metricsByProject, projects, sqlSearchDate)

			// Deletes any existing record
			def now = formatter.format(new Date())
			jdbcTemplate.update("DELETE FROM project_daily_metric where metric_date = '$sqlSearchDate'")

			metrics.each { metric ->
				//TODO: Remove duplicated
				if (!metric.save(flush:true)) {
					log.error "generateDailyMetrics: failed to create ProjectDailyMetric: ${GormUtil.allErrorsString(metric)}"
				}
			}

			log.info "Project Daily Metrics. End date: $sqlSearchDate"
		}

		log.info "Project Daily Metrics ended."
	}

	/**
	 * Search for the last date that the process had been executed.
	 * If no date is found then it returns current date.
	 */
	private def findProjectDailyMetricsLastRunDay() {

		def result
		def lastDateQuery = new StringBuffer("""
			SELECT max(metric_date) as last_date FROM project_daily_metric
		""")

		def metricsLastDate = jdbcTemplate.queryForList(lastDateQuery.toString())

		if (metricsLastDate[0]['last_date'] == null) {
			result = new Date()
		} else {
			result = new Date(metricsLastDate[0]['last_date'].getTime())
		}

		return result;
	}

	/**
	 * Function used by activitySnapshot to retrive assets information
	 */
	private def fillAssetsMetrics(metrics, metricsByProject, projects, searchDate, sqlSearchDate) {

		def project
		def bundle
		def assetClass
		def assetClassOption
		def projectDailyMetric

		def assetsCountsQuery = new StringBuffer("""
			SELECT ae.project_id, ae.asset_class, m.asset_type, mb.use_for_planning, count(*) as count
			FROM asset_entity ae
			INNER JOIN move_bundle mb ON mb.move_bundle_id = ae.move_bundle_id
			LEFT JOIN model m ON m.model_id = ae.model_id
			INNER JOIN project p ON p.project_id = ae.project_id
			WHERE p.completion_date > '$sqlSearchDate'
			GROUP BY ae.project_id, ae.asset_class, m.asset_type, mb.use_for_planning
			ORDER BY ae.project_id, mb.use_for_planning
		""")

		def assetsCountsList = jdbcTemplate.queryForList(assetsCountsQuery.toString())
		assetsCountsList.each {
			if ((project == null) || (project.id != it["project_id"])) {
				project = Project.get(it["project_id"])
				projects << project
				projectDailyMetric = new ProjectDailyMetric()
				projectDailyMetric.project = project
				projectDailyMetric.metricDate = searchDate
				metricsByProject[it["project_id"]] = projectDailyMetric
				metrics << projectDailyMetric
			}

			assetClass = AssetClass.safeValueOf(it["asset_class"])
			assetClassOption = AssetClass.getClassOptionForAsset(assetClass, it["asset_type"])

			if (it["use_for_planning"]) {
				switch(assetClassOption) {
					case 'SERVER-DEVICE':
						projectDailyMetric.planningServers += it["count"]
						break;
					case 'STORAGE-DEVICE':
						projectDailyMetric.planningPhysicalStorages += it["count"]
						break;
					case 'NETWORK-DEVICE':
						projectDailyMetric.planningNetworkDevices += it["count"]
						break;
					case 'OTHER-DEVICE':
						projectDailyMetric.planningOtherDevices += it["count"]
						break;
					case 'APPLICATION':
						projectDailyMetric.planningApplications += it["count"]
						break;
					case 'DATABASE':
						projectDailyMetric.planningDatabases += it["count"]
						break;
					case 'STORAGE-LOGICAL':
						projectDailyMetric.planningLogicalStorages += it["count"]
						break;
				}
			} else {
				switch(assetClassOption) {
					case 'SERVER-DEVICE':
						projectDailyMetric.nonPlanningServers += it["count"]
						break;
					case 'STORAGE-DEVICE':
						projectDailyMetric.nonPlanningPhysicalStorages += it["count"]
						break;
					case 'NETWORK-DEVICE':
						projectDailyMetric.nonPlanningNetworkDevices += it["count"]
						break;
					case 'OTHER-DEVICE':
						projectDailyMetric.nonPlanningOtherDevices += it["count"]
						break;
					case 'APPLICATION':
						projectDailyMetric.nonPlanningApplications += it["count"]
						break;
					case 'DATABASE':
						projectDailyMetric.nonPlanningDatabases += it["count"]
						break;
					case 'STORAGE-LOGICAL':
						projectDailyMetric.nonPlanningLogicalStorages += it["count"]
						break;
				}
			}
		}
	}

	/**
	 * Function used by activitySnapshot to retrieve tasks information
	 */
	private def fillTasksMetrics(metricsByProject, sqlSearchDate) {

		def projectDailyMetric

		def tasksCountsQuery = new StringBuffer("""
			SELECT ac.project_id, count(ac.asset_comment_id) as all_count, count(ac_done.asset_comment_id) as done_count
			FROM project p
			INNER JOIN asset_comment ac ON p.project_id = ac.project_id AND ac.comment_type = 'issue' AND ac.is_published = 1
			LEFT JOIN asset_comment ac_done ON ac_done.asset_comment_id = ac.asset_comment_id AND ac_done.is_resolved = 1
			WHERE p.completion_date > '$sqlSearchDate'
			GROUP BY ac.project_id
			ORDER BY ac.project_id
		""")

		def tasksCountsList = jdbcTemplate.queryForList(tasksCountsQuery.toString())

		tasksCountsList.each {
			projectDailyMetric = metricsByProject[it["project_id"]]
			if (projectDailyMetric) {
				projectDailyMetric.tasksAll = it["all_count"]
				projectDailyMetric.tasksDone = it["done_count"]
			}
		}

	}

	/**
	 * Function used by activitySnapshot to retrieve dependencies information
	 */
	private def fillDependenciesMetrics(metricsByProject, sqlSearchDate) {

		def projectDailyMetric

		def dependenciesCountsQuery = new StringBuffer("""
			SELECT ae.project_id, count(*) as count
			FROM asset_entity ae
			INNER JOIN asset_dependency ad ON ae.asset_entity_id = ad.asset_id
			INNER JOIN project p ON p.project_id = ae.project_id
			WHERE p.completion_date > '$sqlSearchDate'
			GROUP BY ae.project_id 
			ORDER BY ae.project_id
		""")

		def dependenciesCountsList = jdbcTemplate.queryForList(dependenciesCountsQuery.toString())

		dependenciesCountsList.each {
			projectDailyMetric = metricsByProject[it["project_id"]]
			if (projectDailyMetric) {
				projectDailyMetric.dependencyMappings = it["count"]
			}
		}

	}

	/**
	 * Function used by activitySnapshot to retrieve persons and user login information
	 */
	private def fillUsersMetrics(metrics, metricsByProject, projects, sqlSearchDate) {

		def projectDailyMetric
		def companyIds = [0]
		def projectsMapByCompanyId = [:]

		projects.each{ p ->
			companyIds << p.client.id
			projectsMapByCompanyId[p.client.id] = p 
		}

		def cIds = companyIds.join(",")

		def personsCountsQuery = new StringBuffer("""
			SELECT pg.party_group_id as companyId, count(p.person_id) AS totalPersons, count(u.username) as totalUserLogins, count(u_active.username) as activeUserLogins
			FROM person p
			LEFT OUTER JOIN party_relationship r ON r.party_relationship_type_id='STAFF' 
				AND role_type_code_from_id='COMPANY' AND role_type_code_to_id='STAFF' AND party_id_to_id=p.person_id 
			LEFT OUTER JOIN party pa on p.person_id=pa.party_id 
			LEFT OUTER JOIN user_login u on p.person_id=u.person_id 
			LEFT OUTER JOIN user_login u_active on p.person_id=u_active.person_id AND u_active.last_modified > ('$sqlSearchDate' - INTERVAL 1 DAY)
			LEFT OUTER JOIN party_group pg ON pg.party_group_id=r.party_id_from_id 
			WHERE pg.party_group_id in ($cIds)
			GROUP BY pg.party_group_id
		""")

		def personsCountsList = jdbcTemplate.queryForList(personsCountsQuery.toString())
		def project

		personsCountsList.each {
			project = projectsMapByCompanyId[it["companyId"]]
			projectDailyMetric = metricsByProject[project.id]
			if (projectDailyMetric) {
				projectDailyMetric.totalPersons = it["totalPersons"]
				projectDailyMetric.totalUserLogins = it["totalUserLogins"]
				projectDailyMetric.activeUserLogins = it["activeUserLogins"]
			}
		}
	}

	/**
	 * Search activity metrics for the given project ids and in the specified date range
	 */
	def searchProjectActivityMetrics(projectIds, startDate, endDate) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		def sqlStartDate = formatter.format(startDate)
		def sqlEndDate = formatter.format(endDate)
		def projectIdsValue = projectIds.join(',')

		def activitiesMetricsQuery = new StringBuffer("""
			SELECT p.project_code, pdm.*
			FROM project_daily_metric pdm
			INNER JOIN project p ON p.project_id = pdm.project_id
			WHERE pdm.metric_date >= '$sqlStartDate' AND pdm.metric_date <= '$sqlEndDate'
			      AND p.project_id IN ($projectIdsValue)
			ORDER BY p.project_code ASC, pdm.metric_date ASC
		""")

		def activitiesMetrics = jdbcTemplate.queryForList(activitiesMetricsQuery.toString())

		return activitiesMetrics
	}

}