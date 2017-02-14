package net.transitionmanager.service

import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetType
import com.tds.asset.FieldImportance
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.transaction.Transactional
import net.transitionmanager.ProjectDailyMetric
import net.transitionmanager.domain.*
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.jdbc.core.JdbcTemplate

class ProjectService implements ServiceMethods {

	AuditService auditService
	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	SecurityService securityService
	SequenceService sequenceService
	StateEngineService stateEngineService
	UserPreferenceService userPreferenceService

	/**
	 * Returns a list of projects that a person is assigned as staff
	 * @param person - the person to search for their projects
	 * @param ProjectStatus projectStatus=ProjectStatus.ANY
	 * @return List of projects
	 */
	List<Project> getProjectsWherePersonIsStaff(Person person, ProjectStatus projectStatus=ProjectStatus.ACTIVE) {
		String query = '''
			from Project p
			where p.id in (
				select partyIdFrom.id from PartyRelationship pr
				where pr.partyRelationshipType.id = 'PROJ_STAFF'
				  and pr.roleTypeCodeFrom.id = 'PROJECT'
				  and pr.roleTypeCodeTo.id = 'STAFF'
				  and pr.partyIdTo = :person)
		'''
		Map<String, Object> args = [person: person]

		if (projectStatus != ProjectStatus.ANY) {
			query += 'and p.completionDate ' + (projectStatus == ProjectStatus.ACTIVE ? '>=' : '<') + ' :date'
			args.date = new Date()
		}

		Project.executeQuery(query.toString(), args).sort { it.name }
	}

	/**
	 * Returns a list of projects that the user has access to. If showAllProjPerm is true then the user has access to all
	 * projects and the list will be filtered by the projectState and possibly the pagination params. If showAllProjPerm
	 * is false then the list will be restricted to those that the user has been assigned to via a relation in the
	 * PartyRelationship table.
	 *
	 * @param showAllProjPerm - flag if the user has the ShowAllProject permission (default false)
	 * @param projectStatus - the status of the project, options [any | active | completed] (default any)
	 * @param sortOn - field used to sort, could be name or projectCode
	 * @param sortOrder - sort order, could be asc or desc
	 * @param userLogin - the user to lookup projects for
	 * @return list of projects
	 */
	List<Project> getUserProjectsOrderBy(Boolean showAllProjPerm=false, ProjectStatus projectStatus=ProjectStatus.ANY,
		                                  ProjectSortProperty sortOn = ProjectSortProperty.NAME,
		                                  SortOrder sortOrder = SortOrder.ASC, UserLogin userLogin = null) {
		return getUserProjects(showAllProjPerm, projectStatus, [sortOn: sortOn, sortOrder: sortOrder], userLogin)
	}

	/**
	 * Returns a list of projects that the user has access to. If showAllProjPerm is true then the user has access to all
	 * projects affiliated with the individual's company. If showAllProjPerm is false then the list will be restricted to those that
	 * the user has been assigned to via an association in the PartyRelationship table. The list will be filtered by the projectState
	 * and possibly the pagination params.
	 *
	 * @param showAllProjPerm - flag if the user has the ShowAllProject permission (default false)
	 * @param projectStatus - the status of the project, options [any | active | completed] (default any)
	 * @param searchParams - parameters to manage the resultset/pagination [maxRows, currentPage, sortOn, orderBy]
	 * @param userLogin - the user to lookup projects for or null to use the authenticated user
	 * @return list of projects
	 *
	 * TODO: <SL> This returns a PagedResultList not a List
	 */
	List<Project> getUserProjects(Boolean showAllProjPerm=false, ProjectStatus projectStatus=ProjectStatus.ANY,
	                              Map searchParams=[:], UserLogin userLogin = null) {
		def projectIds = []
		def timeNow = new Date()

		if (!userLogin) {
			userLogin = securityService.userLogin
		}
		if (projectStatus == null) {
			projectStatus=ProjectStatus.ANY
		}
		searchParams = searchParams ?: [:]
		int maxRows = searchParams.maxRows ? searchParams.maxRows.toInteger() : Project.count()
		int currentPage = searchParams.currentPage ? searchParams.currentPage.toInteger() : 1
		int rowOffset = (currentPage - 1) * maxRows
		String sortOn = searchParams.sortOn ?: ProjectSortProperty.PROJECT_CODE
		String sortOrder = searchParams.sortOrder ?: SortOrder.ASC
		Map projParams = searchParams.params ?: [:]
		def personId = searchParams.personId ?: userLogin.personId
		def person = Person.get(personId)
		def companyParty = person.company

		// If !showAllProjPerm, then need to find distinct project ids where the PartyRelationship.partyIdTo.id = userLogin.person.id
		// and PartyRelationshipType=PROJ_STAFF and RoleTypeCodeFrom=PROJECT
		if (showAllProjPerm) {
			// Find all the projects that are available for the user's company as client or as partner or owner
			projectIds = partyRelationshipService.companyProjects(companyParty).id
		} else {
			projectIds = getProjectsWherePersonIsStaff(person, projectStatus).id
		}
		if (!projectIds) {
			return []
		}

		List<Date> startDates = projParams.startDate ? Project.executeQuery(
				'select startDate from Project where str(startDate) like :sd',
				[sd: '%' + projParams.startDate + '%']) : []
		List<Date> completionDates = projParams.completionDate ? Project.executeQuery(
				'select completionDate from Project where str(completionDate) like :cd',
				[cd: '%' + projParams.completionDate + '%']) : []
		// if !showAllProjPerm then filter in('id', userProjectIds)
		// If projectState = active, filter ge("completionDate", timeNow)
		// If projectState = completed then filter lt('completionDate', timeNow)
		// if params has pagination params, then add to the filtering

		Project.createCriteria().list(max: maxRows, offset: rowOffset) {
			if (projectIds){
				'in'('id', projectIds)
			}

			if (projParams.projectCode) {
				ilike('projectCode', "%$projParams.projectCode%")
			}

			if (projParams.name) {
				ilike('name', "%$projParams.name%")
			}

			if (projParams.comment) {
				ilike('comment', "%$projParams.comment%")
			}

			if (startDates) {
				'in'('startDate' , startDates)
			}

			if (completionDates) {
				'in'('completionDate' , completionDates)
			}

			if (projectStatus != ProjectStatus.ANY) {
				if (projectStatus == ProjectStatus.ACTIVE) {
					ge("completionDate", timeNow)
				} else {
					lt('completionDate', timeNow)
				}
			}

			order(sortOn.toString(), sortOrder.toString())
		}

	}

	/**
	 * Get the project managers for requested project.
	 * @param project - The project object or id to find Project Manager associated to
	 * @return List of Person instances
	 */
	List<Person> getProjectManagers(Project project) {
		getStaff(project, 'PROJ_MGR')
	}

	/**
	 * Returns the project staff for a project
	 * @param project - a project object or the id of a project
	 * @param team - if provided will filter the staff for a given team or list of teams otherwise defaults to any STAFF
	 * @param includeDisable - a flag to indicate if disabled staff should be included (default false)
	 * @return list of staff
	 */
	List<Person> getStaff(project, team = 'STAFF', boolean includeDisabled = false) {
		project = StringUtil.toLongIfString(project)
		boolean byId = project instanceof Long
		List teamList = CollectionUtils.asList(team)

		List staffList = Person.executeQuery('''
			select distinct pr.partyIdTo from PartyRelationship pr
			where pr.partyRelationshipType.id = 'PROJ_STAFF'
				and pr.partyIdFrom.id = :projectId
				and pr.roleTypeCodeFrom.id = 'PROJECT'
				and pr.roleTypeCodeTo.id in (:teamList)
		''', [projectId: byId ? project : project.id, teamList: teamList])

		if (!includeDisabled) {
			staffList = staffList.findAll { it.enabled }
		}

		staffList.sort { it.toString() }
	}

	/**
	 * Used to get the fields by splitting fields into name/value pairs for the customs field settings
	 * TODO - validate exactly what this definition means...
	 * @param entityType - the class type to get the field setting for
	 * @param projectAttributes - the list of attributes to parse apart
	 * @return a List containing key/value pairs of all of the projectAttributes
	 */
	List<Map<String, String>> getFields(String entityType, List<EavAttribute> projectAttributes = null){
		if (!projectAttributes) {
			projectAttributes = getAttributes(entityType)
		}

		projectAttributes.findAll { !(it.attributeCode.contains('custom')) }.collect { p ->
			[id: p.frontendLabel, label: p.attributeCode]
		}
	}

	/**
	 * Get the custom fields
	 */
	List<Map<String, Object>> getCustoms(List<EavAttribute> projectAttributes = null) {
		Project project = securityService.userCurrentProject
		if (!projectAttributes) {
			projectAttributes = getAttributes('Application')
		}

		projectAttributes.findAll { it.attributeCode.contains('custom') }.collect { p ->
			[id: project[p.attributeCode] ?: p.frontendLabel, label: p.attributeCode]
		}
	}

	/**
	 * Get the config from field importance Table.
	 */
	def getConfigByEntity(entityType) {
		Project project = securityService.userCurrentProject
		def parseData
		def data = FieldImportance.findByProjectAndEntityType(project,entityType)?.config
		if (data) {
			parseData = JSON.parse(data)
		}
		if (!parseData) {
			parseData = generateDefaultConfig(entityType)
		}
		updateConfigForMissingFields(parseData, entityType)
	}

	/**
	 * Create default importance map by assigning normal to all
	 */
	def generateDefaultConfig(type) {
		def defaultProject = Project.findByProjectCode("TM_DEFAULT_PROJECT")
		def data = FieldImportance.findByProjectAndEntityType(defaultProject,type)?.config
		if (data) {
			return JSON.parse(data)
		}

		Set<String> phases = ValidationType.listAsMap.keySet()
		getAttributes(type)*.attributeCode.inject([:]) { Map rmap, String field ->
			def pmap = phases.inject([:]) { Map map, String item -> map[item] = field.contains('custom') ? "H" : "N"; map }
			rmap[field] = [phase: pmap]
			rmap
		}
	}

	/**
	 * Update default importance map for missing fields by assigning normal to all
	 */
	def updateConfigForMissingFields(parseData, String type) {
		List<EavAttribute> projectAttributes = getAttributes(type)
		def fields = getFields(type, projectAttributes) + getCustoms(projectAttributes)
		Set<String> phases = ValidationType.listAsMap.keySet()
		fields.each { f ->
			if (!parseData[f.label]) {
				Map pmap = phases.inject([:]) { Map map, String item -> map[item] = "N"; map }
				parseData[f.label] = [phase: pmap]
			}
		}
		return parseData
	}

	/**
	 * Get attributes from eavAttribute based on EntityType.
	 */
	List<EavAttribute> getAttributes(String entityType) {
		EavEntityType eavEntityType = EavEntityType.findByDomainName(entityType)
		List<EavAttribute> attributes = EavAttribute.findAllByEntityType(eavEntityType, [sort: 'frontendLabel'])

		int firstCustom = attributes.findIndexOf { it.frontendLabel == "Custom1" }
		int lastCustom = attributes.findIndexOf { it.frontendLabel == 'Custom' + Project.CUSTOM_FIELD_COUNT }
		List<EavAttribute> firsts = []
		if ( firstCustom > 0 ) {
			firsts = attributes[0..firstCustom - 1]
		}
		List<EavAttribute> lasts = attributes[lastCustom + 1..attributes.size() - 1]
		List<EavAttribute> nonCustoms = firsts + lasts
		List<EavAttribute>  customs = attributes[firstCustom..lastCustom]
		def oneDigitCustoms = customs.findAll { it.frontendLabel.size() == 7}
		customs.removeAll(oneDigitCustoms)

		attributes = nonCustoms + oneDigitCustoms + customs

		return attributes
	}

	/**
	 * Get next asset tag based on client id of project.
	 * Internally uses sequenceService to generate assetTag
	 * @param project
	 * @return newly formatted assetTag
	 */
	String getNextAssetTag(Project project) {
		"TM-" + String.format("%05d", sequenceService.next(project.clientId, 'AssetTag'))
	}

	/**
	 * Used to get the next asset tag for the project
	 * @param Project - the project the project that the asset tag is for
	 * @param asset  the asset that the tag will be generated for
	 * @return String the actual asset tag
	 */
	String getNewAssetTag(Project project, AssetEntity asset) {
		if (asset.id) {
			'TM-' + asset.id
		} else {
			getNextAssetTag(project)
		}
	}

	def getProjectReportSummary(Map params) {

		def projects = []

		// check if either of the active/inactive checkboxes are checked
		if(params.active || params.inactive) {
			def query = new StringBuffer(""" SELECT *, totalAssetCount-filesCount-dbCount-appCount AS assetCount FROM
				(SELECT p.project_id AS projId, p.project_code AS projName, p.client_id AS clientId,
					(SELECT COUNT(*) FROM move_event me WHERE me.project_id = p.project_id) AS eventCount,
					COUNT(IF(ae.asset_type = "$AssetType.FILES",1,NULL)) AS filesCount,
					COUNT(IF(ae.asset_type = "$AssetType.DATABASE",1,NULL)) AS dbCount,
					COUNT(IF(ae.asset_type = "$AssetType.APPLICATION",1,NULL)) AS appCount,
					COUNT(IF(ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.allServerTypes)}), 1, NULL)) AS totalServCount,
					COUNT(IF(ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.allServerTypes)}) and mb.use_for_planning and ae.move_bundle_id = mb.move_bundle_id ,1,NULL)) AS inPlanningServCount,
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
			if (params.inactive && !params.active)
				query.append(" WHERE CURDATE() > p.completion_date ")
			if (params.active && !params.inactive)
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
	 * Get all clients, partners, managers and workflowcodes.
	 */
	Map getCompanyPartnerAndManagerDetails(PartyGroup company) {

		//	Populate a SELECT listbox with a list of all STAFF relationship to COMPANY
		def managers = PartyRelationship.executeQuery('''
			from PartyRelationship pr
			where pr.partyRelationshipType.id = 'STAFF'
			  and pr.partyIdFrom = :company
			  and pr.roleTypeCodeFrom.id = 'COMPANY'
			  and pr.roleTypeCodeTo.id = 'STAFF'
			''', [company: company])

		[clients: partyRelationshipService.getCompanyClients(company),
		 partners: partyRelationshipService.getCompanyPartners(company)*.partyIdTo,
		 managers: managers.sort { it.partyIdTo?.lastName },
		 workflowCodes: stateEngineService.getWorkflowCode()]
	}

	/**
	 * This method used to get all clients,patners,managers and workflowcodes for action edit.
	 */
	def getprojectEditDetails(Project projectInstance){
		def userCompany = securityService.userLoginPerson.company
		userPreferenceService.setPreference(PREF.PARTY_GROUP, userCompany?.id)

		ProjectLogo projectLogo
		Project project = securityService.userCurrentProject
		def imageId
		if (project) {
			projectLogo = ProjectLogo.findByProject(project)
			if (projectLogo) {
				imageId = projectLogo.id
			}
		}
		WebUtils.retrieveGrailsWebRequest().session.setAttribute("setImage", imageId)
		def projectLogoForProject = ProjectLogo.findByProject(projectInstance)

		def partnerStaff
		def projectCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_COMPANY' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'COMPANY' ")
		//def projectClient = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_CLIENT' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'CLIENT' ")
		def projectPartner
		def projectPartnerId = projectPartner?.partyIdTo?.id
		def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'MOVE_MGR' ")
		def companyStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectCompany.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo")
		companyStaff.each {
			if (it.partyIdTo?.lastName == null) {
				it.partyIdTo?.lastName = ""
			}
		}
		companyStaff.sort{it.partyIdTo?.lastName}
		def clientStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectInstance.client.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo")
			clientStaff.each {
			if (it.partyIdTo?.lastName == null) {
				it.partyIdTo?.lastName = ""
			}
		}
		clientStaff.sort{it.partyIdTo?.lastName}

		List projectPartners = partyRelationshipService.getProjectPartners(projectInstance)
		List projectManagers = getProjectManagers(projectInstance)
		List companyPartners = partyRelationshipService.getCompanyPartners(projectCompany.partyIdTo)*.partyIdTo
		if (projectPartner != null) {
			partnerStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectPartnerId and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo")
			partnerStaff.each {
				if (it.partyIdTo?.lastName == null) {
					it.partyIdTo?.lastName = ""
				}
			}
			partnerStaff.sort{it.partyIdTo?.lastName}
		}

		return [projectPartners:projectPartners, projectManagers:projectManagers, moveManager:moveManager,
			companyStaff:companyStaff, clientStaff:clientStaff, partnerStaff:partnerStaff, companyPartners:companyPartners,
			projectLogoForProject:projectLogoForProject, workflowCodes: stateEngineService.getWorkflowCode()]
	}

	/**
	 * Used to fetch a list of the companies associated with the project which includes the owner, client and any partners
	 * @param project - the project to query
	 * @return A list of the companies
	 */
	List<PartyGroup> getCompanies(Project project) {
		List companies = []
		companies << project.client
		companies << project.owner
		companies.addAll(project.partners)
		return companies
	}

	/**
	 * Used to fetch a map of the companies associated with the project which include the owner, client and
	 * any partners. The map key will be the name and value the company object. By default it will force to lowercase the
	 * company name.
	 * @param project - the project to query
	 * @param toLowercase - flag if true (default) will force the company names to lowercase otherwise they remain untouched
	 * @return A map of [CompanyName : PartyGroup company]
	 */
	Map getCompaniesMappedByName(Project project, boolean toLowercase=true) {
		List companies = getCompanies(project)
		Map mapped = [:]
		companies.each {
			mapped[toLowercase ? it.name.toLowerCase() : it.name] = it
		}
		return mapped
	}

	/**
	 * Used to fetch a map of the companies associated with the project which include the owner, client and
	 * any partners. The map key will be the id and value the company object.
	 * @param project - the project to query
	 * @return A map of ['id' : PartyGroup company]
	 */
	Map getCompaniesMappedById(Project project) {
		List companies = getCompanies(project)
		Map mapped = [:]
		companies.each {
			mapped[it.id.toString()] = it
		}
		return mapped
	}

	/*
	 * Used to determine if a company is associated with a project
	 * @param company - a company object
	 * @return true if associated otherwise false
	 */
	boolean companyIsAssociated(Project project, Party company) {
		return companyIsAssociated(project, company.id)
	}

	/**
	 * Used to determine if a company is associated with a project
	 * @param company - a company object
	 * @return true if associated otherwise false
	 */
	boolean companyIsAssociated(Project project, Long companyId) {
		List assocCompanies = getCompanies(project)
		if (assocCompanies.find { it.id == companyId }) {
			true
		} else {
			false
		}
	}

	/**
	 *The UserPreferenceService.removeProjectAssociates is moved here and renamed as deleteProject
	 *@param project
	 *@param includeProject indicates if should be deleted the project too
	 *@return message
	 */
	@Transactional
	def deleteProject(prokectId, includeProject=false) throws UnauthorizedException {
		def message
		def projects = getUserProjects(securityService.hasPermission("ShowAllProjects"))
		def projectInstance = Project.get(prokectId)

		if(!(projectInstance in projects)){
			throw new UnauthorizedException('You do not have access to the specified project')
		}

		// remove preferences
		def bundleQuery = "select mb.id from MoveBundle mb where mb.project = $projectInstance.id"
		def eventQuery = "select me.id from MoveEvent me where me.project = $projectInstance.id"
		UserPreference.executeUpdate("delete from UserPreference up where up.value = $projectInstance.id or up.value in ($bundleQuery) or up.value in ($eventQuery) ")
		//remove the AssetEntity
		def assetsQuery = "select a.id from AssetEntity a where a.project = $projectInstance.id"

		ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset in ($assetsQuery)")
		AssetComment.executeUpdate("delete from AssetComment ac where ac.assetEntity in ($assetsQuery)")
		AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar av where av.assetEntity in ($assetsQuery)")
		ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.project = $projectInstance.id")
		AssetCableMap.executeUpdate("delete AssetCableMap where assetFrom in ($assetsQuery)")
		AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='$AssetCableStatus.UNKNOWN',assetTo=null,
										assetToPort=null where assetTo in ($assetsQuery)""")
		ProjectTeam.executeUpdate("Update ProjectTeam pt SET pt.latestAsset = null where pt.latestAsset in ($assetsQuery)")

		AssetEntity.executeUpdate("delete from AssetEntity ae where ae.project = $projectInstance.id")
		AssetComment.executeUpdate("delete from AssetComment ac where ac.project = $projectInstance.id")
		TaskBatch.executeUpdate("delete from TaskBatch tb where tb.project = $projectInstance.id")

		// remove DataTransferBatch
		def batchQuery = "select dtb.id from DataTransferBatch dtb where dtb.project = $projectInstance.id"

		DataTransferComment.executeUpdate("delete from DataTransferComment dtc where dtc.dataTransferBatch in ($batchQuery)")
		DataTransferValue.executeUpdate("delete from DataTransferValue dtv where dtv.dataTransferBatch in ($batchQuery)")

		DataTransferBatch.executeUpdate("delete from DataTransferBatch dtb where dtb.project = $projectInstance.id")

		// remove Move Bundle

		AssetEntity.executeUpdate("Update AssetEntity ae SET ae.moveBundle = null where ae.moveBundle in ($bundleQuery)")
		StepSnapshot.executeUpdate("delete from StepSnapshot ss where ss.moveBundleStep in (select mbs.id from MoveBundleStep mbs where mbs.moveBundle in ($bundleQuery))")
		MoveBundleStep.executeUpdate("delete from MoveBundleStep mbs where mbs.moveBundle in ($bundleQuery)")

		def teamQuery = "select pt.id From ProjectTeam pt where pt.moveBundle in ($bundleQuery)"
		PartyRelationship.executeUpdate("delete from PartyRelationship pr where pr.partyIdFrom in ($teamQuery) or pr.partyIdTo in ($teamQuery)")
		PartyGroup.executeUpdate("delete from Party p where p.id in ($teamQuery)")
		Party.executeUpdate("delete from Party p where p.id in ($teamQuery)")
		ProjectTeam.executeUpdate("delete from ProjectTeam pt where pt.moveBundle in ($bundleQuery)")

		PartyRelationship.executeUpdate("delete from PartyRelationship pr where pr.partyIdFrom in ($bundleQuery) or pr.partyIdTo in ($bundleQuery)")
		Party.executeUpdate("delete from Party p where p.id in ($bundleQuery)")
		MoveBundle.executeUpdate("delete from MoveBundle mb where mb.project = $projectInstance.id")

		// remove Move Event
		MoveBundle.executeUpdate("Update MoveBundle mb SET mb.moveEvent = null where mb.moveEvent in ($eventQuery)")
		MoveEventNews.executeUpdate("delete from MoveEventNews men where men.moveEvent in ($eventQuery)")
		MoveEventSnapshot.executeUpdate("delete from MoveEventSnapshot mes where mes.moveEvent in ($eventQuery)")

		MoveEvent.executeUpdate("delete from MoveEvent me where me.project = $projectInstance.id")

		// remove Project Logo
		ProjectLogo.executeUpdate("delete from ProjectLogo pl where pl.project = $projectInstance.id")
		// remove party relationship
		PartyRelationship.executeUpdate("delete from PartyRelationship pr where pr.partyIdFrom  = $projectInstance.id or pr.partyIdTo = $projectInstance.id")

		// remove associated references e.g. Room, Rack FI, AssetDepBundles, KeyValue .
		Room.executeUpdate("delete from Room r where r.project  = $projectInstance.id")
		Rack.executeUpdate("delete from Rack ra where ra.project  = $projectInstance.id")
		AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle adb where adb.project = $projectInstance.id")
		FieldImportance.executeUpdate("delete from FieldImportance fi where fi.project  = $projectInstance.id")
		KeyValue.executeUpdate("delete from KeyValue kv where kv.project  = $projectInstance.id")

		Model.executeUpdate("update Model mo set mo.modelScope = null where mo.modelScope  = $projectInstance.id")
		ModelSync.executeUpdate("update ModelSync ms set ms.modelScope = null where ms.modelScope  = $projectInstance.id")

		def recipesQuery = "select r.id from Recipe r where r.project.id = $projectInstance.id"
		Recipe.executeUpdate("update Recipe r set r.releasedVersion=null where r.project.id = $projectInstance.id")
		def recipeVersions = RecipeVersion.find("from RecipeVersion rv where rv.recipe.id in ($recipesQuery)")
		if (recipeVersions) {
			recipeVersions.each {
				RecipeVersion.executeUpdate("update RecipeVersion rv set rv.clonedFrom=null where rv.clonedFrom.id = $it.id")
			}
		}
		RecipeVersion.executeUpdate("delete from RecipeVersion rv where rv.recipe.id in ($recipesQuery)")
		Recipe.executeUpdate("delete from Recipe r where r.project.id  = $projectInstance.id")

		PartyGroup.executeUpdate("delete from Party p where p.id = $projectInstance.id")
		Party.executeUpdate("delete from Party p where p.id = $projectInstance.id")

		if (includeProject) {
			Project.executeUpdate("delete from Project p where p.id = $projectInstance.id")
		}

		return message
	}

	/**
	 * Used retrieve the default Bundle configured for the project or create one if it does not exist
	 * @param project
	 * @param defaultBundleName name to be given to the default bundle, should it be created.
	 * @return MoveBundle - the default bundle assigned to the project or will create it on the fly
	 */
	@Transactional
	MoveBundle getDefaultBundle(Project project, String defaultBundleName = null) {
		return project.defaultBundle ?: createDefaultBundle(project, defaultBundleName)
	}

	/**
	 * Method is used to create createDefaultBundle
	 * @param project
	 * @param defaultBundle
	 * @return project's default move bundle
	 */
	@Transactional
	MoveBundle createDefaultBundle (Project project, String defaultBundleName) {
		def defaultCode = defaultBundleName?:'TBD'
		// TODO : JPM 7/2014 - we could run into two separate processes attempting to create the default project at the same time so a lock should be implemented
		if(!project.defaultBundle){
			def moveBundle = MoveBundle.findByNameAndProject(defaultCode, project)
			if(moveBundle)
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

	/**
	 * Used to add/remove partners from a project by comparing the list of partner ids passed. If a partner is
	 * removed from a project, all the partner staff is also removed. Any tasks assigned to the individuals will be
	 * unassigned however historical references of staff will remain.
	 *
	 * @param projectInstance - the project that is being updated
	 * @param partnerIds - a single id or a list of ids
	 * @throws InvalidParamException when the partner ids are invalid or not associated with the owner of the project
	 */
	@Transactional
	void updateProjectPartners(Project projectInstance, def partnersIds) {

		// Get a list of the partners associated to the owner of the project plus the partners assigned to the project
		Party projectOwner = projectInstance.getOwner()
		List ownerPartners = partyRelationshipService.getCompanyPartners(projectOwner)
		List ownerPartnerIds = ownerPartners*.partyIdTo.id

		// Get current partners associated to the project
		List currentPartners = partyRelationshipService.getProjectPartners(projectInstance)
		List currentPartnersIds = currentPartners*.id

		// Convert the Partner Ids parameter into a List if not already (single values appear as a string)
		if (partnersIds == null) {
			partnersIds = []
		} else if (partnersIds instanceof String) {
			log.debug "param partnersIds is ${partnersIds?.getClass().name} : $partnersIds"
			if (partnersIds) {
				partnersIds = [ partnersIds ]
			} else {
				// The string could be null
				partnersIds = []
			}
		} else {
			// Need to cast the Ljava.lang.String to List
			List ids = []
			partnersIds.each { if (it) ids << it }
			partnersIds = ids
			// Lets weed out the possibility of duplicates
			partnersIds.unique()
		}

		// Convert partners to Long
		def newPartnersIds = []
		partnersIds.each { p ->
			Long pid
			if (p instanceof Long) {
				pid = p
			} else {
				pid = NumberUtil.toPositiveLong(p, -1)
				if (pid == -1) {
					throw new InvalidParamException("Invalid partner id was specified ($p)")
				}
			}

			// Make sure that the partner Id is a valid partner assoicated with the company
			if (!ownerPartnerIds.contains(pid)) {
				log.debug "Project $projectInstance owner $projectOwner partner ids are $ownerPartnerIds"
				throw new InvalidParamException("Partner id specified is not associated with project ($pid)")
			}
			newPartnersIds << pid
		}

		// Define which partners should be deleted because are not in the new list
		List toDeletePartners = currentPartnersIds - newPartnersIds
		// Define which partners should be created because are new in the list
		List toAddPartners = newPartnersIds - currentPartnersIds

		Party partnerParty

		// Add partners to the relationship
		toAddPartners.each { partnerId ->
			partnerParty = Party.get(partnerId)
			if (!partnerParty) {
				throw new InvalidParamException("Partner id specified is not found ($partnerId)")
			} else if (partnerParty.partyType.id != 'COMPANY') {
				log.debug "Check partner type failed $partnerParty where type is ${partnerParty?.partyType}"
				throw new InvalidParamException("Partner id specified is not a company ($partnerId)")
			}
			partyRelationshipService.savePartyRelationship("PROJ_PARTNER", projectInstance, "PROJECT", partnerParty, "PARTNER")
			log.info "updateProjectPartners() Added partner $partnerParty to project $projectInstance"
		}

		// Delete partners from the relationship
		String findPartnerStaff = "from PartyRelationship p where p.partyRelationshipType = 'STAFF' " +
			"and p.partyIdFrom = :partner and " +
			"p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'"

		String deleteProjectStaff = "DELETE FROM PartyRelationship pr WHERE pr.partyRelationshipType='PROJ_STAFF' " +
			"AND pr.partyIdFrom = :project AND pr.roleTypeCodeFrom = 'PROJECT' " +
			"AND pr.partyIdTo IN (:staff)"

		String unassignStaffTasks = "UPDATE AssetComment task SET task.assignedTo=NULL WHERE task.project = :project " +
			"AND task.assignedTo IN (:staff)"

		toDeletePartners.each { partnerId ->
			partnerParty = Party.get(partnerId)
			if (partnerParty) {
				log.info "updateProjectPartners() Removing partner $partnerParty from project $projectInstance"

				List partnerStaff = PartyRelationship.findAll(findPartnerStaff, [partner: partnerParty])?.partyIdTo
				if (partnerStaff.size() > 0) {
					def c = PartyRelationship.executeUpdate(deleteProjectStaff, [project:projectInstance, staff:partnerStaff])
					log.info "updateProjectPartners() Removed $c partner staff assignments from project $projectInstance"

					c = AssetComment.executeUpdate(unassignStaffTasks, [project: projectInstance, staff: partnerStaff])
					log.info "updateProjectPartners() Unassigned staff from $c task(s) for project $projectInstance"
				}

				partyRelationshipService.deletePartyRelationship("PROJ_PARTNER", projectInstance, "PROJECT", partnerParty, "PARTNER")
			}
		}
	}

	/**
	 * Used to save a project along with the project logo, partners and project manager declaraitions
	 * that come from the project create form.
	 * @param projectInstance - the instance of the project to update
	 * @param file - the logo for the project
	 * @param projectPartners - a list of company ids representing partners
	 * @param projectManager - a person object that is to be the Project Manager on the project
	 * @return a map containing message:String and success:true|false
	 */
	@Transactional
	Map saveProject(Project projectInstance, file, projectPartners, projectManager) {

		//projectInstance.dateCreated = new Date()
		//  When the Start date is initially selected and Completion Date is blank, set completion date to the Start date
		//Get the Partner Image file from the multi-part request

		// List of OK mime-types
		def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
		if (file?.contentType && file.contentType != "application/octet-stream") {
			if (!okcontents.contains(file.contentType)) {
				return [message: "Image must be one of: $okcontents", success: false]
			}
		}

		//save image
		def image = ProjectLogo.fromUpload(file)
		image.project = projectInstance
		def imageSize = image.getSize()
		if(imageSize > 50000) {
			return [message: " Image size is too large. Please select proper Image", success: false]
		}

		if (!projectInstance.hasErrors() && projectInstance.save()) {
			if(file && file.getContentType() == "application/octet-stream"){
				//Nonthing to perform.
			} else {
				image.save()
			}
			//def client = params.projectClient

			Person person = securityService.userLoginPerson
			def companyParty = person.company
			if (!companyParty) {
				return [message: "Unable to find company for your account", success: false]
			}

			// For Project to Company PartyRelationship
			partyRelationshipService.savePartyRelationship("PROJ_COMPANY", projectInstance, "PROJECT", companyParty, "COMPANY")

			List partnersIds = CollectionUtils.asList(projectPartners)
			updateProjectPartners(projectInstance, partnersIds)

			if (projectManager != null && projectManager != "") {

				def projectManagerParty = Party.get(projectManager)
				//	For Project to ProjectManager PartyRelationship
				partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectInstance, "PROJECT", projectManagerParty, "PROJ_MGR")
			}

			userPreferenceService.setCurrentProjectId(projectInstance.id)

			//Will create a bundle name TBD and set it as default bundle for project
			projectInstance.getProjectDefaultBundle()

			return [message: "Project $projectInstance created", success: true, imageId: image.id]
		} else {
			return [message: "", success: false]
		}
	}

	/**
	 * Used to get a list of projects where the company is the client of project(s)
	 * @param company - the company to find projects for
	 * @param projectStatus - filter on the projects based on being ACTIVE|COMPLETED|ANY (default ACTIVE)
	 * @return the list of projects of the company
	 */
	List<Project> getProjectsWhereClient(PartyGroup company, ProjectStatus projectStatus=ProjectStatus.ACTIVE) {
		StringBuffer query = new StringBuffer("from Project p where p.client = :client")
		Map params = [client:company]
		if (projectStatus != ProjectStatus.ANY) {
			query.append(" and p.completionDate ${projectStatus==ProjectStatus.ACTIVE ? '>=' : '<'} :completionDate")
			params.completionDate = new Date()
		}
		query.append(" order by p.name")
		//println "getProjectsWhereClient() query=$query, params=$params"
		Project.executeQuery(query.toString(), params)
	}

	/**
	 * This function is used by the daily project metrics job to generate daily metrics.
	 * It search for active projects and for each one retrieves specific metrics: assets, deps, users, tasks
	 */
	@Transactional
	def activitySnapshot(Map dataMap) {
		log.info "Project Daily Metrics started."

		Date startDate = findProjectDailyMetricsLastRunDay().clearTime()
		Date endDate = new Date().clearTime()

		log.info "Project Daily Metrics will run from $startDate to $endDate"

		def metricsByProject
		List<ProjectDailyMetric> metrics
		List<Project> projects
		def sqlSearchDate

		for (searchDate in startDate..endDate) {

			sqlSearchDate = TimeUtil.gmtDateSQLFormat(searchDate)

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
			fillUsersMetrics(metricsByProject, projects, sqlSearchDate)

			// Deletes any existing record
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
	private Date findProjectDailyMetricsLastRunDay() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList('SELECT max(metric_date) as last_date FROM project_daily_metric')
		if (rows[0]['last_date'] == null) {
			new Date()
		} else {
			new Date(rows[0]['last_date'].getTime())
		}
	}

	/**
	 * Used to retrieve the companies that are partners of the project
	 * @param project - the project to find the partners for
	 * @return The list of partners associated with the project if any exist
	 */
	List<Party> getPartners(Project project) {
		assert project

		Party.executeQuery('''
			select pr.partyIdTo from PartyRelationship pr
			where pr.partyRelationshipType = 'PROJ_PARTNER'
			  and pr.roleTypeCodeFrom = 'PROJECT'
			  and pr.roleTypeCodeTo = 'PARTNER'
			  and pr.partyIdFrom = :project
		''', [project: project])
	}

	/**
	 * Access the company that owns the project.
	 *
	 * @param project  the project to find the owning company for
	 * @return the company
	 */
	Party getOwner(Project project) {
		assert project

		Party.executeQuery('''
			select pr.partyIdTo from PartyRelationship pr
			where pr.partyRelationshipType = 'PROJ_COMPANY'
			  and pr.roleTypeCodeFrom = 'PROJECT'
			  and pr.roleTypeCodeTo = 'COMPANY'
			  and pr.partyIdFrom = :project
		''', [project: project], [max: 1])[0]
	}

	/**
	 * Used to set the company that owns the project
	 * @param project - the project to set the owner on
	 * @param owner - the company to set the project owner to
	 * @return The project object
	 */
	Project setOwner(Project project, PartyGroup owner) {
		assert project
		assert owner
		assert owner.partyType.id == 'COMPANY'

		partyRelationshipService.savePartyRelationship("PROJ_COMPANY", project, "PROJECT", owner, "COMPANY")

		return project
	}

	/**
	 * This is used to determine what the default expiration date for users should be for a given project
	 * @param p - the Project to determine the exiration on
	 * @return an expiration date
	 */
	Date defaultAccountExpirationDate(Project project) {
		Date exp = project.completionDate
		if (!exp) {
			// TODO : JPM 4/2016 : should try examining the events to see if they have a greater date than the project.completionDate
			exp = new Date() + 90
		}
		return exp
	}

	/**
	 * Function used by activitySnapshot to retrieve assets information
	 */
	private void fillAssetsMetrics(List metrics, Map metricsByProject, List<Project> projects, Date searchDate, String sqlSearchDate) {

		def project
		def assetClass
		def assetClassOption

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

		// This property will get used repeatedly in the following each iterator
		ProjectDailyMetric projectDailyMetric

		List assetsCountsList = jdbcTemplate.queryForList(assetsCountsQuery.toString())
		assetsCountsList.each { Map<String, Object> it ->

			if (project == null || project.id != it.project_id) {
				project = Project.get(it.project_id)
				projects << project
				projectDailyMetric = new ProjectDailyMetric(project: project, metricDate: searchDate)
				metricsByProject[project.id] = projectDailyMetric
				metrics << projectDailyMetric
			}

			if (!projectDailyMetric) {
				log.error "fillAssetsMetrics() projectDailyMetric property was not properly assigned"
				return
			}

			assetClass = AssetClass.safeValueOf(it.asset_class)
			assetClassOption = AssetClass.getClassOptionForAsset(assetClass, it.asset_type)

			if (it.use_for_planning) {
				switch(assetClassOption) {
					case 'SERVER-DEVICE':
						projectDailyMetric.planningServers += it.count
						break
					case 'STORAGE-DEVICE':
						projectDailyMetric.planningPhysicalStorages += it.count
						break
					case 'NETWORK-DEVICE':
						projectDailyMetric.planningNetworkDevices += it.count
						break
					case 'OTHER-DEVICE':
						projectDailyMetric.planningOtherDevices += it.count
						break
					case 'APPLICATION':
						projectDailyMetric.planningApplications += it.count
						break
					case 'DATABASE':
						projectDailyMetric.planningDatabases += it.count
						break
					case 'STORAGE-LOGICAL':
						projectDailyMetric.planningLogicalStorages += it.count
						break
				}
			} else {
				switch(assetClassOption) {
					case 'SERVER-DEVICE':
						projectDailyMetric.nonPlanningServers += it.count
						break
					case 'STORAGE-DEVICE':
						projectDailyMetric.nonPlanningPhysicalStorages += it.count
						break
					case 'NETWORK-DEVICE':
						projectDailyMetric.nonPlanningNetworkDevices += it.count
						break
					case 'OTHER-DEVICE':
						projectDailyMetric.nonPlanningOtherDevices += it.count
						break
					case 'APPLICATION':
						projectDailyMetric.nonPlanningApplications += it.count
						break
					case 'DATABASE':
						projectDailyMetric.nonPlanningDatabases += it.count
						break
					case 'STORAGE-LOGICAL':
						projectDailyMetric.nonPlanningLogicalStorages += it.count
						break
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
			projectDailyMetric = metricsByProject[it.project_id]
			if (projectDailyMetric) {
				projectDailyMetric.tasksAll = it.all_count
				projectDailyMetric.tasksDone = it.done_count
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
			projectDailyMetric = metricsByProject[it.project_id]
			if (projectDailyMetric) {
				projectDailyMetric.dependencyMappings = it.count
			}
		}

	}

	/**
	 * Function used by activitySnapshot to retrieve persons and user login information
	 */
	private void fillUsersMetrics(metricsByProject, List<Project> projects, sqlSearchDate) {

		def projectDailyMetric
		def companyIds = [0]
		Map projectsMapByCompanyId = [:]

		projects.each { p ->
			companyIds << p.client.id
			projectsMapByCompanyId[p.client.id] = p
		}

		String personsCountsQuery = '''
			SELECT pg.party_group_id as companyId, count(p.person_id) AS totalPersons,
			       count(u.username) as totalUserLogins, count(u_active.username) as activeUserLogins
			FROM person p
			LEFT OUTER JOIN party_relationship r ON r.party_relationship_type_id='STAFF'
				AND role_type_code_from_id='COMPANY' AND role_type_code_to_id='STAFF' AND party_id_to_id=p.person_id
			LEFT OUTER JOIN party pa on p.person_id=pa.party_id
			LEFT OUTER JOIN user_login u on p.person_id=u.person_id
			LEFT OUTER JOIN user_login u_active on p.person_id=u_active.person_id AND u_active.last_modified > (? - INTERVAL 1 DAY)
			LEFT OUTER JOIN party_group pg ON pg.party_group_id=r.party_id_from_id
			WHERE pg.party_group_id in (''' + companyIds.collect { NumberUtil.toLong(it) }.join(',') + ''')
			GROUP BY pg.party_group_id
		'''

		jdbcTemplate.queryForList(personsCountsQuery, sqlSearchDate).each {
			def project = projectsMapByCompanyId[it.companyId]
			projectDailyMetric = metricsByProject[project.id]
			if (projectDailyMetric) {
				projectDailyMetric.totalPersons = it.totalPersons
				projectDailyMetric.totalUserLogins = it.totalUserLogins
				projectDailyMetric.activeUserLogins = it.activeUserLogins
			}
		}
	}

	/**
	 * Search activity metrics for the given project ids and in the specified date range
	 */
	List<Map<String, Object>> searchProjectActivityMetrics(projectIds, Date startDate, Date endDate) {
		String sqlStartDate = TimeUtil.gmtDateSQLFormat(startDate)
		String sqlEndDate = TimeUtil.gmtDateSQLFormat(endDate)

		jdbcTemplate.queryForList('''
			SELECT p.project_code, pdm.*
			FROM project_daily_metric pdm
			INNER JOIN project p ON p.project_id = pdm.project_id
			WHERE pdm.metric_date >= ? AND pdm.metric_date <= ?
			      AND p.project_id IN (''' + projectIds.collect { NumberUtil.toLong(it) }.join(',') + ''')
			ORDER BY p.project_code ASC, pdm.metric_date ASC
		''', sqlStartDate, sqlEndDate)
	}

	/**
	 * This method will query for all the accounts that haven't been activated.
	 * Used to retrieve a list project users whom are eligible as activation notices
	 * @param project - the project that the users are associated with which includes anybody assoicated with the project
	 * @return A list of map objects including:
	 *		UserLogin userLogin
	 *		String firstName
	 *		String lastName
	 *		String email
	 *		String company - company name
	 *		List<String> roles - list of security roles the user has been assigned
	 *		Date lastActivationNotice - Date of latest activation notice sent to the user otherwise null
	 *		Date expiry - the expiry date of the user
	 *		Date created - the date the user was created
	 * Rules: Has a userLogin account where lastLogin is null and localAccount=true and Active='Y' and expiry > now()
	 */
	List<Map> getAccountActivationUsers(Project project) {
		assert project

		// All the staff associated with the current project
		List persons = getAssignedStaff(project)
		if (!persons) return Collections.emptyList()

		// Now using that list, perform a join against the UserLogin in order to find the users that are candidates
		String query = '''
			select u, (select max(r.createdDate)
			           from PasswordReset r
			           where r.userLogin = u
			           and r.type=:type) as latestReset
			from UserLogin u
			where u.person in (:persons)
			  and u.person.email is not null
			  and u.active='Y'
			  and u.expiryDate > :expiry
			  and u.lastLogin is null
		'''

		UserLogin.executeQuery(query, [persons: persons, expiry: new Date(), type: PasswordResetType.WELCOME]).collect { Object[] result ->
			UserLogin userLogin = result[0]
			Person person = userLogin.person

			[userLogin: userLogin, personId: person.id, firstName: person.firstName, lastName : person.lastName,
			 email: person.email, company: person.company.name, roles: userLogin.securityRoleCodes,
			 expiry: userLogin.expiryDate, dateCreated: userLogin.createdDate, lastActivationNotice: result[1],
			 currentProject: userLogin.currentProject]
		}
	}

	/**
	 * This method will send the account activation email to a selected list of accounts.
	 * @param accounts: list of email addresses.
	 * @param message: custom message to be included in the body of the email message.
	 * @param from: email address of who is sending the email.
	 * @param ipAddress: IP Address of the client's machine who triggered the notifications.
	 */
	def sendBulkActivationNotificationEmail(List accounts, String message, String from, String ipAddress){
		accounts.each { account ->
			// We instantiate the model map each time to clear out any data from the previous account.
			def model = [customMessage: message, from: from, username: account.userLogin.username ]
			securityService.sendResetPasswordEmail(account.email, ipAddress, PasswordResetType.WELCOME, model)
		}
	}

	/**
	 * Used to get the list of all persons associated to a project
	 * @param project - the Project that the team is associated to
	 * @return The list of persons found that are team members
	 */
	List<Person> getAssignedStaff(Project project, String team='STAFF') {
		Person.executeQuery('''
			from Person
			where id in (SELECT partyIdTo.id FROM PartyRelationship
			             WHERE partyRelationshipType='PROJ_STAFF'
			               AND partyIdFrom=:project
			               AND roleTypeCodeFrom='PROJECT'
			               AND roleTypeCodeTo.id=:team)
			ORDER BY firstName, lastName
		''', [project:project, team:team], [sort: 'firstName'])
	}

	/*
	 * Used to retrieve a list of all staff that could be assigned to a project. The staff that are available will be
	 * contingent on what user is requesting the list and their relationship to the project. The use-cases are:
	 *    - Staff of Owner:   ALL owner, partner(s) and client staff without limitations (when the Only Assigned is not checked)
	 *    - Staff of Partner: ONLY assigned staff to the project
	 *    - Staff of Client:  ONLY assigned staff of Owner and Partner and All Client Staff without limitation
	 *
	 * @param project - the project to look for persons
	 * @return The list consistening of a Map of staff details that includes:
	 *    - PartyGroup company
	 *    - String name
	 *    - teams
	 *    - Person staff
	 *    - boolean assigned
	 */
	List<Map> getAssignableStaff(Project project, Person forWhom) {

		// Get the list of all Staff for the owner, partners and client
		PartyGroup employer = forWhom.company
		PartyGroup owner = project.owner
		PartyGroup client = project.client

		// Get the existing list of assigned staff
		List<Map> assignedStaffDetail = getAssignedStaff(project)
		//assignedStaffDetail.each {
		//	it.assigned = true
		//	it.company = it.company[0]
		//}
		Map assignedStaffIds = assignedStaffDetail*.id.groupBy { it }

		// Based on the whom is making the request and the
		boolean isOwnerStaff = owner.id == employer.id
		boolean isClientStaff = project.client.id == employer.id
		boolean isPartnerStaff = !(isOwnerStaff && isClientStaff)

		// For Owner's staff we'll add all the non-assigned staff of the owner and partner(s)
		if (isOwnerStaff) {

			// Add any Owner staff that are not already assigned to the project
			List ownerStaff = partyRelationshipService.getCompanyStaff(owner.id)
			ownerStaff.each { staff ->
				if (!assignedStaffIds.containsKey(staff.id)) {
					assignedStaffDetail << staff
				}
			}

			// Add any partner staff that are not already associated to the project
			List partnerStaff = []
			List partnerList = getPartners(project)
			partnerList.each { partner ->
				partnerStaff = partyRelationshipService.getCompanyStaff(partner.id)
				partnerStaff.each { staff ->
					if (!assignedStaffIds.containsKey(staff.id)) {
						assignedStaffDetail << staff
					}
				}
			}
		}

		// For Owner or Client Staff we'll add all of the non-assigned client staff to the list
		if (isOwnerStaff || isClientStaff) {
			List clientStaff = partyRelationshipService.getCompanyStaff(client.id)
			clientStaff.each { staff ->
				if (!assignedStaffIds.containsKey(staff.id)) {
					assignedStaffDetail << staff
				}
			}
		}

		// Strip out any inactive staff
		assignedStaffDetail.removeAll { it.active != 'Y' }
		assignedStaffDetail.sort { it.toString() }
		return assignedStaffDetail
	}

	/**
	 * Used to get the list of unique Team members associated with a project. This will allow filtering for a given role and/or person
	 * @param project - the Project that the team is associated to
	 * @param teamRoleType - the RoleType to optional filter on
	 * @param person - the Person to optionally filter on
	 * @return The list of persons found that are team members
	 */
	List<Person> getTeamMembers(Project project, RoleType teamRoleType=null, Person person=null) {
		List persons = []
		List relations = getTeamMemberRelationships(project, teamRoleType, person)
		if (relations) {
			// Get the unique Person objects
			persons = relations*.partyIdTo?.unique { a,b -> a.id <=> b.id}
		}

		return persons
	}

	/**
	 * Used to get the list of unique Team members associated with a project. (overloaded)
	 * @param project - the project to search for members
	 * @param teamCode - a String of the Team code
	 * @param person - used to filter the results to the individual person (optional)
	 */
	List<Person> getTeamMembers(Project project, String teamCode, Person person=null) {
		RoleType rt = RoleType.read(teamCode)
		if (!rt) {
			log.warn "getTeamMembers() called with invalid teamCode $teamCode"
			return null
		}
		return getTeamMembers(project, rt, person)
	}

	/**
	 * Used to associate a person to a project with a certain team
	 * @param project - the project the person will be associated with
	 * @param person - the person to be assigned
	 * @param teamCodes - a single team code or a list of team codes
	 */
	@Transactional
	void addTeamMember(Project project, Person person, teamCodes) {
		partyRelationshipService.addProjectStaff(project, person)

		List currentTeams = person.getTeamsAssignedTo(project).id
		CollectionUtils.asList(teamCodes).each {tc ->
			if (!currentTeams.contains(tc)) {
				partyRelationshipService.addStaffFunction(person, tc, person.company, project)
			}
		}
	}

	/**
	 * Used to remove a person from a team on a project. This will also remove any assignments that the
	 * person may have to a move event.
	 * @param project - the project the person will be associated with
	 * @param person - the person to be assigned
	 * @param teamCodes - a single team code or a list of team codes
	 * @return the number of teams that were deleted
	 */
	@Transactional
	int removeTeamMember(Project project, Person person, teamCodes) {
		teamCodes = CollectionUtils.asList(teamCodes)

		if (teamCodes.contains('STAFF')) {
			// If you are getting this exception you should look at the PersonService.removeFromProject method
			throw new InvalidParamException('STAFF can not be removed by removeTeamMember method')
		}

		// Remove person/team references in the MoveEventStaff table
		MoveEventStaff.executeUpdate('''
			DELETE MoveEventStaff
			where person=:person
			  and role.id in (:teams)
			  and moveEvent.id in (select id from MoveEvent where project=:project)
		''', [project: project, person: person, teams: teamCodes])

		// Remove Team assignments for the individual against the project
		PartyRelationship.executeUpdate('''
			DELETE PartyRelationship
			where partyRelationshipType.id='PROJ_STAFF'
			  and roleTypeCodeFrom.id='PROJECT'
			  and roleTypeCodeTo.id in (:teamCodes)
			  and partyIdFrom=:project
			  and partyIdTo=:person
		''', [project: project, person: person, teamCodes: teamCodes])
	}

	/**
	 * Used to retrieve one or more team member PartyRelationship references to a project
	 * @param project - the project to search for members
	 * @param teamRoleType - The team Role Type code
	 * @param person - used to filter the results to the individual person (optional)
	 */
	List<PartyRelationship> getTeamMemberRelationships(Project project, String teamRoleType = null, Person person = null) {
		RoleType rt

		if (teamRoleType) {
			if (teamRoleType instanceof String) {
				rt = RoleType.read(teamRoleType)
				if (!rt) {
					throw new InvalidParamException("getTeamMemberRelationships called with invalid teamCode $teamRoleType")
				}
			} else {
				if (teamRoleType instanceof RoleType) {
					rt = teamRoleType
				} else {
					throw new InvalidParamException("getTeamMemberRelationships called with unsupported RoleType ${teamRoleType.getClass().name}")
				}
			}
		}

		PartyRelationshipType prtProjectStaff = PartyRelationshipType.read('PROJ_STAFF')
		RoleType rtProject = RoleType.read('PROJECT')
		RoleType rtStaff = RoleType.read('STAFF')

		assert prtProjectStaff
		assert rtProject
		//assert teamRoleType

		return PartyRelationship.withCriteria {
			// resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
			and {
				eq('partyRelationshipType', prtProjectStaff)
				eq('roleTypeCodeFrom', rtProject)
				eq('partyIdFrom', project)
				ne('roleTypeCodeTo', rtStaff)
				if (rt) {
					eq('roleTypeCodeTo', rt)
				}
				if (person) {
					eq('partyIdTo', person)
				}
			}
		}
	}

	boolean hasAccessToProject(UserLogin userLogin = null, long projectId) {
		return projectId in (getUserProjects(securityService.hasPermission("ShowAllProjects"), null, null, userLogin)*.id)
	}

	        /**
        * Get List of projects by the Owner
        * @param owner
        * @return
        */
    List<Project> getProjectsWhereOwner(PartyGroup owner){
        assert owner != null
        def params = [
                owner:owner
           ]
        def projects = PartyRelationship.executeQuery(
						"select partyIdFrom from PartyRelationship pr where \
						pr.partyRelationshipType.id = 'PROJ_COMPANY' and \
						pr.roleTypeCodeFrom.id = 'PROJECT' and \
                        pr.roleTypeCodeTo.id = 'COMPANY' and \
						pr.partyIdTo = :owner", params)

               return projects
       }


}
