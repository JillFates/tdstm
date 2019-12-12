package net.transitionmanager.project

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SettingType
import com.tdsops.tm.enums.domain.SortOrder
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.action.ApiActionService
import net.transitionmanager.action.ApiCatalogService
import net.transitionmanager.action.Credential
import net.transitionmanager.action.Provider
import net.transitionmanager.asset.ApplicationAssetMap
import net.transitionmanager.asset.AssetCableMap
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.command.ProjectCommand
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.common.KeyValue
import net.transitionmanager.common.SequenceService
import net.transitionmanager.common.Setting
import net.transitionmanager.exception.ConfigurationException
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.exception.UnauthorizedException
import net.transitionmanager.imports.DataScript
import net.transitionmanager.imports.DataScriptService
import net.transitionmanager.imports.DataTransferBatch
import net.transitionmanager.imports.DataTransferComment
import net.transitionmanager.imports.DataTransferValue
import net.transitionmanager.imports.Dataview
import net.transitionmanager.imports.TaskBatch
import net.transitionmanager.license.License
import net.transitionmanager.license.LicenseAdminService
import net.transitionmanager.license.LicenseCommonService
import net.transitionmanager.metric.ProjectDailyMetric
import net.transitionmanager.model.Model
import net.transitionmanager.model.ModelSync
import net.transitionmanager.party.Party
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyRelationship
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.party.PartyRelationshipType
import net.transitionmanager.person.Person
import net.transitionmanager.person.PersonService
import net.transitionmanager.person.UserPreference
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.search.FieldSearchData
import net.transitionmanager.security.AuditService
import net.transitionmanager.security.CredentialService
import net.transitionmanager.security.Permission
import net.transitionmanager.security.RoleType
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.TagService
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.Recipe
import net.transitionmanager.task.RecipeVersion
import org.apache.commons.lang3.time.DateUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.web.util.WebUtils
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import java.text.SimpleDateFormat

class ProjectService implements ServiceMethods {

	AuditService               auditService
	JdbcTemplate               jdbcTemplate
	PartyRelationshipService   partyRelationshipService
	SequenceService            sequenceService
	UserPreferenceService      userPreferenceService
	CustomDomainService        customDomainService
	LicenseAdminService        licenseAdminService
	TagService                 tagService
	ApiCatalogService          apiCatalogService
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	ApiActionService           apiActionService
	ProviderService            providerService
	CredentialService          credentialService
	DataScriptService          dataScriptService
	LicenseCommonService       licenseCommonService
	FileSystemService          fileSystemService
	MoveBundleService		   moveBundleService

	static final String ASSET_TAG_PREFIX = 'TM-'

	static final String DEFAULT_PROJECT_LOGO_DIR = 'tds/web-app/assets/images'

	static final String DEFAULT_TRANSITIONMANAGER_LOGO = 'transitionLogo.svg'

	static final String DEFAULT_LIC_MANAGER_LOGO = 'transitionLogo.svg'

	/**
	 * Returns a list of projects that a person is assigned as staff
	 * @param person - the person to search for their projects
	 * @param ProjectStatus projectStatus=ProjectStatus.ANY
	 * @return List of projects
	 */
	List<Project> getProjectsWherePersonIsStaff(Person person, ProjectStatus projectStatus=ProjectStatus.ACTIVE) {
		String query = """
			from Project p where p.id in (
				select pr.partyIdFrom.id from PartyRelationship pr
				where pr.partyRelationshipType.id = 'PROJ_STAFF'
					and pr.roleTypeCodeFrom.id = '$RoleType.CODE_PARTY_PROJECT'
					and pr.roleTypeCodeTo.id = '$RoleType.CODE_PARTY_STAFF'
					and pr.partyIdTo = :person)
		"""
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
	List<Project> getUserProjectsOrderBy(
		Boolean showAllProjPerm=false,
		ProjectStatus projectStatus=ProjectStatus.ANY,
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
	 * @param includeDefaultProject - flag signaling if the default project should be included.
	 * @return list of projects
	 *
	 * TODO: <SL> This returns a PagedResultList not a List
	 */
	List<Project> getUserProjects(Boolean showAllProjPerm=false, ProjectStatus projectStatus=ProjectStatus.ANY,
		Map searchParams=[:], UserLogin userLogin = null) {

		def projectIds = []

		// Convert the current time to beginning of the day adjusted to GMT so projects from today are considered active
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtil.FORMAT_DATE_ISO8601)
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"))
		Date today = sdf.parse(sdf.format(DateUtils.truncate(TimeUtil.nowGMT(), Calendar.DAY_OF_MONTH)))

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
		def personId = searchParams.personId ?: userLogin.person.id
		Person person = Person.get(personId)
		def companyParty = person.company

		// If !showAllProjPerm, then need to find distinct project ids where the PartyRelationship.partyIdTo.id = userLogin.person.id
		// and PartyRelationshipType=PROJ_STAFF and RoleTypeCodeFrom=PROJECT
		if (showAllProjPerm) {
			// Find all the projects that are available for the user's company as client or as partner or owner
			projectIds = partyRelationshipService.companyProjects(companyParty).id
		} else {
			projectIds = getProjectsWherePersonIsStaff(person, projectStatus).id
		}

		boolean hasAccessToDefaultProject = securityService.hasPermission(userLogin.person, Permission.ProjectManageDefaults)
		// If the user has access to the default project, it should be included in the list.
		if  (hasAccessToDefaultProject) {
			Project defaultProject = Project.getDefaultProject()
			if (defaultProject) {
				projectIds << defaultProject.id
			}
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
					ge("completionDate", today)
				} else {
					lt('completionDate', today)
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
		getStaff(project, RoleType.CODE_TEAM_PROJ_MGR)
	}

	/**
	 * Returns the project staff for a project
	 * @param project - a project object or the id of a project
	 * @param team - if provided will filter the staff for a given team or list of teams otherwise defaults to any STAFF
	 * @param includeDisable - a flag to indicate if disabled staff should be included (default false)
	 * @return list of staff
	 */
	List<Person> getStaff(project, team = RoleType.CODE_PARTY_STAFF, boolean includeDisabled = false) {
		project = StringUtil.toLongIfString(project)
		boolean byId = project instanceof Long
		List teamList = CollectionUtils.asList(team)

		List staffList = Person.executeQuery("""
			select distinct pr.partyIdTo from PartyRelationship pr
			where pr.partyRelationshipType.id = 'PROJ_STAFF'
				and pr.partyIdFrom.id = :projectId
				and pr.roleTypeCodeFrom.id = '$RoleType.CODE_PARTY_PROJECT'
				and pr.roleTypeCodeTo.id in (:teamList)
		""".toString(), [projectId: byId ? project : project.id, teamList: teamList])

		if (!includeDisabled) {
			staffList = staffList.findAll { it.enabled }
		}

		staffList.sort { it.toString() }
	}

	/**
	 * Get next asset tag based on client id of project.
	 * Internally uses sequenceService to generate assetTag
	 * @param project
	 * @return newly formatted assetTag
	 */
	String getNextAssetTag(Project project) {
		ASSET_TAG_PREFIX + String.format("%05d", sequenceService.next(project.client.id, 'AssetTag'))
	}

	/**
	 * Used to get the next asset tag for the project
	 * @param Project - the project the project that the asset tag is for
	 * @param asset  the asset that the tag will be generated for
	 * @return String the actual asset tag
	 */
	String getNewAssetTag(Project project, AssetEntity asset) {
		if (asset.id) {
			ASSET_TAG_PREFIX + asset.id
		} else {
			getNextAssetTag(project)
		}
	}

	/**
	 * Generates a list of projects and the details including assets and staff counts
	 * @param active - flag to include active projects
	 * @param inactive - flag to include inactive projects
	 * @return A list of projects and their details as a Map
	 */
	List<Map> getProjectReportSummary(Map params) {

		List projects = []

		// check if either of the active/inactive checkboxes are checked
		if (params.active || params.inactive) {
			def query = new StringBuilder(""" SELECT *, totalAssetCount-filesCount-dbCount-appCount AS assetCount FROM
				(SELECT p.project_id AS projId, p.project_code AS projName, p.client_id AS clientId,
					(SELECT COUNT(*) FROM move_event me WHERE me.project_id = p.project_id) AS eventCount,
					COUNT(IF(ae.asset_class = '${AssetClass.STORAGE.toString()}',1,NULL)) AS filesCount,
					COUNT(IF(ae.asset_class = '${AssetClass.DATABASE.toString()}',1,NULL)) AS dbCount,
					COUNT(IF(ae.asset_class = '${AssetClass.APPLICATION.toString()}',1,NULL)) AS appCount,
					COUNT(IF(ae.asset_class = '${AssetClass.DEVICE.toString()}' AND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.allServerTypes)}), 1, NULL)) AS totalServCount,
					COUNT(IF(ae.asset_class = '${AssetClass.DEVICE.toString()}' AND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.allServerTypes)}) and mb.use_for_planning and ae.move_bundle_id = mb.move_bundle_id ,1,NULL)) AS inPlanningServCount,
					COUNT(IF(ae.asset_class = '${AssetClass.DEVICE.toString()}' AND NOT(COALESCE(ae.asset_type,'') IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.virtualServerTypes)})),1,NULL)) AS deviceCount,
					COUNT(*) AS totalAssetCount,
					DATE(p.start_date) AS startDate,
					DATE(p.completion_date) AS completionDate,
					pg.name AS clientName,
					p.description AS description,
					(SELECT GROUP_CONCAT(pg2.name)
						FROM party_relationship pr
						LEFT JOIN party_group pg2 ON pg2.party_group_id = pr.party_id_to_id
                        WHERE (pr.party_relationship_type_id = 'PROJ_PARTNER'
							AND pr.party_id_from_id = p.project_id
							AND pr.role_type_code_from_id = '$RoleType.CODE_PARTY_PROJECT'
                            AND pr.role_type_code_to_id = '$RoleType.CODE_PARTY_PARTNER')
					) AS partnerNames
					FROM asset_entity ae
					LEFT JOIN move_bundle mb ON (mb.move_bundle_id = ae.move_bundle_id)
						AND ((ae.move_bundle_id = NULL) OR (mb.use_for_planning = true))
					LEFT JOIN project p ON (p.project_id = ae.project_id)
					LEFT JOIN party_group pg ON (pg.party_group_id = p.client_id) """)

			// handle active/inactive project specification
			if (params.inactive && !params.active)
				query.append(" WHERE CURDATE() > p.completion_date ")
			if (params.active && !params.inactive)
				query.append(" WHERE CURDATE() < p.completion_date ")

			query.append(""" GROUP BY ae.project_id
					) inside
				ORDER BY inside.projName """)

			projects = jdbcTemplate.queryForList(query.toString())

			// Add the staff count to each project
			projects.each {
				it["staffCount"] = partyRelationshipService.getCompanyStaff(it["clientId"]).size()
			}
		}

		return projects
	}

	/**
	 * Get all clients, partners, managers(id and name only).
	 */
	Map getCompanyPartnerAndManagerDetails(PartyGroup company) {

		//	Populate a SELECT listbox with a list of all STAFF relationship to COMPANY
		List<PartyRelationship> managers = PartyRelationship.executeQuery("""
			from PartyRelationship pr
			where pr.partyRelationshipType.id = 'STAFF'
			  and pr.partyIdFrom = :company
			  and pr.roleTypeCodeFrom.id = '$RoleType.CODE_PARTY_COMPANY'
			  and pr.roleTypeCodeTo.id = '$RoleType.CODE_PARTY_STAFF'
			""".toString(), [company: company])

		Party partyTo
		managers = managers.sort { GrailsHibernateUtil.unwrapIfProxy(it.partyIdTo)?.lastName }
		List<Map<String, ?>> managersMap = managers.collect { it ->
			partyTo = GrailsHibernateUtil.unwrapIfProxy(it.partyIdTo)

			[
				id  : partyTo.id,
				name: partyTo.toString()
			]
		}

		[
			clients : getAllClients(),
			partners: partyRelationshipService.getCompanyPartners(company)*.partyIdTo,
			managers: managersMap
		]
	}

	/**
	 * This method used to get all clients,patners,managers for action edit.
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
		def projectCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_COMPANY' and p.partyIdFrom.id = $projectInstance.id and p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT' and p.roleTypeCodeTo = '$RoleType.CODE_PARTY_COMPANY' ".toString())
		//def projectClient = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_CLIENT' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'CLIENT' ")
		def projectPartner
		def projectPartnerId = projectPartner?.partyIdTo?.id
		def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom.id = $projectInstance.id and p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT' and p.roleTypeCodeTo = '$RoleType.CODE_TEAM_MOVE_MGR' ".toString())
		def companyStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom.id = $projectCompany.partyIdTo.id and p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY' and p.roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF' order by p.partyIdTo".toString())
		companyStaff.each {
			if (it.partyIdTo?.lastName == null) {
				it.partyIdTo?.lastName = ""
			}
		}
		companyStaff.sort{it.partyIdTo?.lastName}
		def clientStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom.id = $projectInstance.client.id and p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY' and p.roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF' order by p.partyIdTo".toString())
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
			partnerStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectPartnerId and p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY' and p.roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF' order by p.partyIdTo".toString())
			partnerStaff.each {
				if (it.partyIdTo?.lastName == null) {
					it.partyIdTo?.lastName = ""
				}
			}
			partnerStaff.sort{it.partyIdTo?.lastName}
		}

		return [
			projectPartners      : projectPartners,
			projectManagers      : projectManagers,
			moveManager          : moveManager,
			companyStaff         : companyStaff,
			clientStaff          : clientStaff,
			partnerStaff         : partnerStaff,
			companyPartners      : companyPartners,
			projectLogoForProject: projectLogoForProject
		]
	}

	/**
	 * Used to fetch a list of the companies associated with the project which includes the owner, client and any partners
	 * @param project - the project to query
	 * @return A list of the companies
	 */
	List<PartyGroup> getCompanies(Project project) {
		List companies = []
		companies << project.client
		companies << getOwner(project)
		companies.addAll(partyRelationshipService.getProjectPartners(project))
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
	 * Used to clone the default settings and add them to the project parameter,
	 * including fieldSpecs and default Tags.
	 * @param project - the project to update with default settings
	 */
	@Transactional
	void cloneDefaultSettings(Project project) {

		// Make sure someone isn't trying to clone the default project onto itself
		if (Project.isDefaultProject(project)) {
			throw new InvalidParamException('cloneDefaultSettings not allowed for the Default project')
		}

		// Make sure this wasn't called before for the project
		int existingSettings = Setting.findAllByProjectAndType(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC).size()
		if (existingSettings > 0) {
			throw new InvalidRequestException('Asset Field Settings already exist for project')
		}

		// Clone the Field Specifications Setting records
		Project defProject = Project.get(Project.DEFAULT_PROJECT_ID)
		List fieldSpecs = Setting.findAllByProjectAndType(defProject, SettingType.CUSTOM_DOMAIN_FIELD_SPEC)
		if (fieldSpecs.size() < 4) {
			throw new ConfigurationException('The Default project is missing the Asset Field Settings')
		}

		// Loop over the FieldSpecs loaded and create duplicates for the new project
		for (spec in fieldSpecs) {
			Setting s = new Setting(
				project: project,
				type: spec.type,
				key: spec.key,
				json: spec.json)
			if (!s.save(flush:true, failOnError: false)) {
				log.error 'cloneDefaultSettings failed : ' + GormUtil.allErrorsString(s)
				throw new DomainUpdateException('An error occurred while creating the Asset Field Settings')
			}
			log.debug "Created field spec ${spec.key} for project ${project.id}"
		}
		// Clone the Default Project Tags (if it has any) and add them to the new project
		tagService.cloneProjectTags(defProject, project)

		// Clone the Default Project Providers (if it has any) and add them to the new project
		providerService.cloneProjectProviders(defProject, project)

		// Clone the Default Project Credentials (if it has any) and add them to the new project
		credentialService.cloneProjectCredentials(defProject, project)

		// Clone the Default Project Data Scripts (if it has any) and add them to the new project
		dataScriptService.cloneProjectDataScripts(defProject, project)

		// Clone the Default Project Api Catalogs (if it has any) and add them to the new project
		apiCatalogService.cloneProjectApiCatalogs(defProject, project)

		// Clone the Default Project Api Actions (if it has any) and add them to the new project
		apiActionService.cloneProjectApiActions(defProject, project)
	}

	/**
	 *The UserPreferenceService.removeProjectAssociates is moved here and renamed as deleteProject
	 *@param project
	 *@param includeProject indicates if should be deleted the project too
	 *@return message
	 */
	@Transactional
	void deleteProject(Long projectId, includeProject=false) throws UnauthorizedException {

		if(projectId == Project.DEFAULT_PROJECT_ID) {
			throw new InvalidParamException('The default project cannot be deleted.')
		}

		if (!securityService.hasAccessToProject(projectId)) {
			throw new UnauthorizedException('You do not have access to the specified project')
		}

		Project projectInstance = Project.get(projectId)

		// remove preferences
		String bundleQuery = "select mb.id from MoveBundle mb where mb.project = :project"
		String eventQuery = "select me.id from MoveEvent me where me.project = :project"
		String roomQuery = " select ro.id from Room ro where ro.project = $projectInstance.id"
		List projectCodes = [UserPreferenceEnum.CURR_PROJ.name()]
		List bundleCodes = [UserPreferenceEnum.MOVE_BUNDLE.name(), UserPreferenceEnum.CURR_BUNDLE.name()]
		List eventCodes = [UserPreferenceEnum.MOVE_EVENT.name(), UserPreferenceEnum.MYTASKS_MOVE_EVENT_ID.name()]
		String roomCode = UserPreferenceEnum.CURR_ROOM.name()
		String prefDelSql = """
			delete from UserPreference up where
			(up.preferenceCode in(:projectCodesList) and up.value = :projectId) or
			(up.preferenceCode in (:bundleCodesList) and up.value in ($bundleQuery)) or
			(up.preferenceCode in (:eventCodesList) and up.value in ($eventQuery)) or
			(up.preferenceCode = '$roomCode' and up.value in ($roomQuery))
			"""
		Map prefDelMap = [projectCodesList: projectCodes, bundleCodesList: bundleCodes, eventCodesList: eventCodes, projectId: projectInstance.id.toString(), project: projectInstance]
		Integer cant = UserPreference.executeUpdate(prefDelSql, prefDelMap)

		// Setting Configuration settings
		Setting.executeUpdate('delete from Setting s where s.project=:p', [p:projectInstance])

		//remove the AssetEntity
		String assetsQuery = "select a.id from AssetEntity a where a.project = :project"

		ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset in ($assetsQuery)".toString(), [project: projectInstance])
		AssetComment.executeUpdate("delete from AssetComment ac where ac.assetEntity in ($assetsQuery)".toString(), [project: projectInstance])
		ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.project = $projectInstance")
		AssetCableMap.executeUpdate("delete AssetCableMap where assetFrom in ($assetsQuery)".toString(), [project: projectInstance])
		AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='$AssetCableStatus.UNKNOWN',assetTo=null,
										assetToPort=null where assetTo in ($assetsQuery)""".toString(), [project: projectInstance])
		ProjectTeam.executeUpdate("Update ProjectTeam pt SET pt.latestAsset = null where pt.latestAsset in ($assetsQuery)".toString(), [project: projectInstance])

		AssetEntity.executeUpdate("delete from AssetEntity ae where ae.project = $projectInstance")
		AssetComment.executeUpdate("delete from AssetComment ac where ac.project = $projectInstance")
		TaskBatch.executeUpdate("delete from TaskBatch tb where tb.project = $projectInstance")

		// remove DataTransferBatch
		String batchQuery = "select dtb.id from DataTransferBatch dtb where dtb.project.id = :projectId"

		DataTransferComment.executeUpdate("delete from DataTransferComment dtc where dtc.dataTransferBatch in ($batchQuery)".toString(), [projectId: projectInstance.id ])
		DataTransferValue.executeUpdate("delete from DataTransferValue dtv where dtv.dataTransferBatch in ($batchQuery)".toString(), [projectId: projectInstance.id ])

		DataTransferBatch.executeUpdate("delete from DataTransferBatch dtb where dtb.project = $projectInstance")

		// remove Move Bundle

		AssetEntity.executeUpdate("Update AssetEntity ae SET ae.moveBundle = null where ae.moveBundle in ($bundleQuery)".toString(), [project: projectInstance ])

		String teamQuery = "select pt.id From ProjectTeam pt where pt.moveBundle in ($bundleQuery)"
		PartyRelationship.executeUpdate("delete from PartyRelationship pr where pr.partyIdFrom in ($teamQuery) or pr.partyIdTo in ($teamQuery)".toString(), [project: projectInstance ])
		PartyGroup.executeUpdate("delete from Party p where p.id in ($teamQuery)".toString(), [project: projectInstance ])
		Party.executeUpdate("delete from Party p where p.id in ($teamQuery)".toString(), [project: projectInstance ])
		ProjectTeam.executeUpdate("delete from ProjectTeam pt where pt.moveBundle in ($bundleQuery)".toString(), [project: projectInstance ])

		PartyRelationship.executeUpdate("delete from PartyRelationship pr where pr.partyIdFrom in ($bundleQuery) or pr.partyIdTo in ($bundleQuery)".toString(), [project: projectInstance ])
		Party.executeUpdate("delete from Party p where p.id in ($bundleQuery)".toString(), [project: projectInstance ])
		MoveBundle.executeUpdate("delete from MoveBundle mb where mb.project = $projectInstance")

		// remove Move Event
		MoveBundle.executeUpdate("Update MoveBundle mb SET mb.moveEvent = null where mb.moveEvent in ($eventQuery)".toString(), [project: projectInstance ])
		MoveEventNews.executeUpdate("delete from MoveEventNews men where men.moveEvent in ($eventQuery)".toString(), [project: projectInstance])
		MoveEventSnapshot.executeUpdate("delete from MoveEventSnapshot mes where mes.moveEvent in ($eventQuery)".toString(), [project: projectInstance ])

		MoveEvent.executeUpdate("delete from MoveEvent me where me.project = $projectInstance")

		// remove Project Logo
		ProjectLogo.executeUpdate("delete from ProjectLogo pl where pl.project = $projectInstance")
		// remove party relationship
		PartyRelationship.executeUpdate("delete from PartyRelationship pr where pr.partyIdFrom  = $projectInstance or pr.partyIdTo = $projectInstance")

		// remove associated references e.g. Room, Rack FI, AssetDepBundles, KeyValue .
		Room.executeUpdate("delete from Room r where r.project  = $projectInstance")
		Rack.executeUpdate("delete from Rack ra where ra.project  = $projectInstance")
		AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle adb where adb.project = $projectInstance")
		KeyValue.executeUpdate("delete from KeyValue kv where kv.project  = $projectInstance")

		Model.executeUpdate("update Model mo set mo.modelScope = null where mo.modelScope  = $projectInstance")
		ModelSync.executeUpdate("update ModelSync ms set ms.modelScope = null where ms.modelScope  = $projectInstance")

		String recipesQuery = "select r.id from Recipe r where r.project.id =:projectId"
		Recipe.executeUpdate("update Recipe r set r.releasedVersion=null where r.project.id = $projectInstance.id")
		def recipeVersions = RecipeVersion.executeQuery("from RecipeVersion rv where rv.recipe.id in (" + recipesQuery + ")", [projectId: projectInstance.id ])
		if (recipeVersions) {
			recipeVersions.each {
				RecipeVersion.executeUpdate("update RecipeVersion rv set rv.clonedFrom=null where rv.clonedFrom.id = $it.id")
			}
		}
		RecipeVersion.executeUpdate("delete from RecipeVersion rv where rv.recipe.id in (" + recipesQuery + ")", [projectId: projectInstance.id ])
		Recipe.executeUpdate("delete from Recipe r where r.project.id  = $projectInstance.id")

		Dataview.executeUpdate("delete from Dataview dv where dv.project.id = $projectInstance.id")

		PartyGroup.executeUpdate("delete from Party p where p.id = $projectInstance.id")
		Party.executeUpdate("delete from Party p where p.id = $projectInstance.id")

		DataScript.executeUpdate("delete from DataScript ds where ds.project.id = :projectId", [projectId: projectInstance.id])

		Credential.executeUpdate("delete from Credential c where c.project.id = :projectId", [projectId: projectInstance.id])

		Provider.executeUpdate("delete from Provider p where p.project.id = :projectId", [projectId: projectInstance.id])

		if (includeProject) {
			Project.executeUpdate("delete from Project p where p.id = :projectId", [projectId: projectInstance.id])
		}

	}

	/**
	 * Used retrieve the default Bundle configured for the project or create one if it does not exist
	 * @param project
	 * @param defaultBundleName name to be given to the default bundle, should it be created.
	 * @return MoveBundle - the default bundle assigned to the project or will create it on the fly
	 */
	@Transactional()
	MoveBundle getDefaultBundle(Project project, String defaultBundleName = null) {
		return project.defaultBundle ?: createDefaultBundle(project, defaultBundleName)
	}

	/**
	 * Method is used to create createDefaultBundle
	 * @param project
	 * @param defaultBundle
	 * @return project's default move bundle
	 */
	@Transactional()
	MoveBundle createDefaultBundle(Project project, String defaultBundleName='TBD') {
		MoveBundle moveBundle
		// TODO : JPM 7/2014 - we could run into two separate processes attempting to create the default project at the same time so a lock should be implemented
		if (!project.defaultBundle) {

			// TM-8319 - the name was being set to null
			String bundleName = defaultBundleName ?: 'TBD'

			moveBundle = MoveBundle.findByNameAndProject(bundleName, project)
			if (! moveBundle) {
				moveBundle = new MoveBundle(
					name: bundleName,
					project: project,
					useForPlanning: true,
					startTime: project.startDate,
					completionTime: project.completionDate
				)

				if (!moveBundle.save(flush: true, failOnError: false)) {
					log.error "createDefaultBundle: failed to create DefaultBundle for project $project: ${GormUtil.allErrorsString(moveBundle)}"
					throw new RuntimeException('Unable to create default bundle')
				}
				log.info "Default bundle ${moveBundle.name} was created for project $project"
			}
			if (moveBundle) {
				project.defaultBundle = moveBundle
			}
		}
		return moveBundle
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
		Party projectOwner = getOwner(projectInstance)
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
				throw new InvalidParamException("Partner ID ($pid) specified is not associated with Project.Owner ($projectOwner.id)")
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
			partyRelationshipService.savePartyRelationship("PROJ_PARTNER", projectInstance, RoleType.CODE_PARTY_PROJECT, partnerParty, RoleType.CODE_PARTY_PARTNER)
			log.info "updateProjectPartners() Added partner $partnerParty to project $projectInstance"
		}

		// Delete partners from the relationship
		String findPartnerStaff = "from PartyRelationship p where p.partyRelationshipType = 'STAFF' " +
			"and p.partyIdFrom = :partner and " +
			"p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT' and p.roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF'"

		String deleteProjectStaff = "DELETE FROM PartyRelationship pr WHERE pr.partyRelationshipType='PROJ_STAFF' " +
			"AND pr.partyIdFrom = :project AND pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT' " +
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

				partyRelationshipService.deletePartyRelationship("PROJ_PARTNER", projectInstance, RoleType.CODE_PARTY_PROJECT, partnerParty, RoleType.CODE_PARTY_PARTNER)
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

		if (projectInstance.save(failOnError: false)) {
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
			partyRelationshipService.savePartyRelationship("PROJ_COMPANY", projectInstance, RoleType.CODE_PARTY_PROJECT, companyParty, RoleType.CODE_PARTY_COMPANY)

			List partnersIds = CollectionUtils.asList(projectPartners)
			updateProjectPartners(projectInstance, partnersIds)

			if (projectManager != null && projectManager != "") {

				def projectManagerParty = Party.get(projectManager)
				//	For Project to ProjectManager PartyRelationship
				partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectInstance, RoleType.CODE_PARTY_PROJECT, projectManagerParty, RoleType.CODE_TEAM_PROJ_MGR)
			}

			userPreferenceService.setCurrentProjectId(projectInstance.id)
			
			//Will create a bundle name TBD and set it as default bundle for project
			getDefaultBundle(projectInstance)

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
		StringBuilder query = new StringBuilder("from Project p where p.client = :client")
		Map params = [client:company]
		if (projectStatus != ProjectStatus.ANY) {
			query.append(" and p.completionDate ${projectStatus==ProjectStatus.ACTIVE ? '>=' : '<'} :completionDate")
			params.completionDate = new Date()
		}
		query.append(" order by p.name")
		Project.executeQuery(query.toString(), params)
	}

	/**
	 * This function is used by the daily project metrics job to generate daily metrics.
	 * It search for active projects and for each one retrieves specific metrics: assets, deps, users, tasks
	 */
	@Transactional
	def activitySnapshot() {
		log.info "Project Daily Metrics started."

		Date startingDate = findProjectDailyMetricsLastRunDay().clearTime()
		Date endDate = new Date().clearTime()

		List<ProjectDailyMetric> metrics
		def sqlSearchDate

		List<Project> projects = Project.where {
			completionDate >= endDate
		}.list()

		log.info "Project Daily Metrics will run from $startingDate  to $endDate, for Projects ${projects*.id}"

		for (searchDate in startingDate..endDate) {

			sqlSearchDate = TimeUtil.gmtDateSQLFormat(searchDate)

			log.info "Project Daily Metrics. Processing date: $sqlSearchDate"

			// create a ProjectDailyMetric for each Project (this list will be new for each iteration of searchDate)
			metrics = projects.collect { project ->
				new ProjectDailyMetric(project: project, metricDate: searchDate)
			}

			// ***************************
			// Retrieve assets information
			fillAssetsMetrics(metrics, projects)

			// ***************************
			// Retrieve tasks information
			fillTasksMetrics(metrics, projects)

			// *********************************
			// Retrieve dependencies information
			fillDependenciesMetrics(metrics, projects)

			// **************************************
			// Retrieve person/user login information
			fillUsersMetrics(metrics, projects, sqlSearchDate)

			// Deletes any existing record
			ProjectDailyMetric.where{metricDate == searchDate}.deleteAll()
			//jdbcTemplate.update("DELETE FROM project_daily_metric where metric_date = '$sqlSearchDate'")

			metrics.each { metric ->
				metric.save(flush:true)
			}

			log.info "Project Daily Metrics. End date: $sqlSearchDate"
		}

		log.info "Project Daily Metrics ended."
	}

	/**
	 * Search for the last date that the process had been executed.
	 * If no date is found then it returns current date.
	 */
	Date findProjectDailyMetricsLastRunDay() {
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

		Party.executeQuery("""
			select pr.partyIdTo from PartyRelationship pr
			where pr.partyRelationshipType = 'PROJ_PARTNER'
			  and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
			  and pr.roleTypeCodeTo = '$RoleType.CODE_PARTY_PARTNER'
			  and pr.partyIdFrom = :project
		""".toString(), [project: project])
	}

	/**
	 * Used to retrieve the list of partner IDs that are associated to a project
	 * @param project - the Project object to find the partners of
	 * @return the list if IDs of partners
	 */
	List<Long> getPartnerIds(Project project) {
		getPartnerIds(project?.id)
	}

	/**
	 * Used to retrieve the list of partner IDs that are associated to a project
	 * @param projectId - the ID of the Project to find the partners of
	 * @return the list if IDs of partners
	 */
	List<Long> getPartnerIds(Long projectId) {
		PartyRelationship.where {
			partyRelationshipType.id == 'PROJ_PARTNER'
			roleTypeCodeFrom.id == RoleType.CODE_PARTY_PROJECT
			roleTypeCodeTo.id == RoleType.CODE_PARTY_PARTNER
			partyIdFrom.id == projectId
		}.projections {
			property 'partyIdTo.id'
		}.list()
	}

	/**
	 * Used to retrieve the list of staff IDs that are associated to a PartyGroup (company) by object reference
	 * @param partyGroup - the PartyGroup object to find staff of object
	 * @return the list if IDs of partners
	 */
	List<Long> getCompanyStaffIds(PartyGroup partyGroup) {
		getCompanyStaffIds(partyGroup?.id)
	}

	/**
	 * Used to retrieve the list of staff IDs that are associated to a single PartyGroup (company) by ID
	 * @param partyGroupId - the PartyGroup ID to use to find staff of PartyGroup
	 * @return the list if IDs of partners
	 */
	List<Long> getCompanyStaffIds(Long partyGroupId) {
		getCompanyStaffIds([partyGroupId])
	}

	/**
	 * Used to retrieve the list of staff IDs that are associated to one or more PartyGroups (company)
	 * @param partyGroupIds - A list of the PartyGroup Ids to use to find staff of those PartyGroups
	 * @return the list if IDs of partners
	 */
	List<Long> getCompanyStaffIds(List<Long> partyGroupIds) {
		PartyRelationship.where {
			partyRelationshipType.id == 'STAFF'
			roleTypeCodeFrom.id == RoleType.CODE_PARTY_COMPANY
			roleTypeCodeTo.id == RoleType.CODE_PARTY_STAFF
			partyIdFrom.id in partyGroupIds
		}.projections {
			property 'partyIdTo.id'
		}.list()
	}

	/**
	 * Used to retrieve the IDs of all of the Staff that are associated to a project. This will include the
	 * following:
	 *     - ALL of the staff of the project's client company
	 *     - Staff of the project owner company that are directly assigned to the project
	 *     - Staff of the project partner companies that are directly assigned to the project
	 */
	List<Long> getAssociatedStaffIds(Project project) {

		// Get the list of staff ids of the project owner and partners of the project
		PartyGroup owner = getOwner(project)
		List<Long> companyIds = getPartnerIds(project)
		companyIds << owner.id
		List<Long> nonClientStaffIds = getCompanyStaffIds(companyIds)

		// Now get the list of the nonClientStaffIds that are associated with the project
		List<Long> staffIds = PartyRelationship.where {
			partyRelationshipType.id == 'PROJ_STAFF'
			roleTypeCodeFrom.id == RoleType.CODE_PARTY_PROJECT
			roleTypeCodeTo.id == RoleType.CODE_PARTY_STAFF
			partyIdFrom.id == project.id
			partyIdTo.id in nonClientStaffIds
		}
		.projections { property 'partyIdTo.id' }.list()

		// Add to the staffIds list all of the staff of the project client as they're all fair game
		PartyGroup client = project.client
		List<Long> clientStaffIds = getCompanyStaffIds(client)
		staffIds.addAll(clientStaffIds)

		return staffIds
	}

	/**
	 * Using the same filtering capabilities available for assets, this method looks up
	 * the staff associated with this project (Owner, Client, partners) whose name matches
	 * the filter provided.
	 *
	 * @param project
	 * @param nameFilter
	 * @return
	 */
	List<Person> getAssociatedStaffByName(Project project, String nameFilter) {
		List<Person> personList

		// Fetch the ids of the staff associated with the project
		List<Long> staffIds = getAssociatedStaffIds(project)

		if( staffIds ) {
			// Query that should retrieve all the invidividuals for this project matching the filter, if any.
			String hqlQuery = "FROM Person p where id in (:staffIds)"

			Map params = [staffIds: staffIds]
			if (nameFilter && nameFilter.trim()) {
				FieldSearchData fieldSearchData = new FieldSearchData([
						  column     : SqlUtil.personFullName(),
						  columnAlias: "personName",
						  domain     : Person,
						  filter     : nameFilter
				])

				SqlUtil.parseParameter(fieldSearchData)

				String nameCondition = fieldSearchData.sqlSearchExpression
				params.putAll(fieldSearchData.sqlSearchParameters)
				hqlQuery += " AND $nameCondition"
			}

			personList = Person.executeQuery ( hqlQuery, params )

		} else {
			personList = []
		}

		return personList
	}


	/**
	 * Access the company that owns the project.
	 *
	 * @param project  the project to find the owning company for
	 * @return the company
	 */
	Party getOwner(Project project) {
		assert project

		Party.executeQuery("""
			select pr.partyIdTo from PartyRelationship pr
			where pr.partyRelationshipType = 'PROJ_COMPANY'
			  and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
			  and pr.roleTypeCodeTo = '$RoleType.CODE_PARTY_COMPANY'
			  and pr.partyIdFrom = :project
		""".toString(), [project: project], [max: 1])[0]
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

		partyRelationshipService.savePartyRelationship("PROJ_COMPANY", project, RoleType.CODE_PARTY_PROJECT, owner, RoleType.CODE_PARTY_COMPANY)

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
	private void fillAssetsMetrics(List metrics, List<Project> projects) {

		def assetClass
		def assetClassOption

		def assetsCountsQuery = new StringBuilder("""
			SELECT ae.project_id, ae.asset_class, m.asset_type, mb.use_for_planning, count(*) as count
			FROM asset_entity ae
			INNER JOIN move_bundle mb ON mb.move_bundle_id = ae.move_bundle_id
			LEFT JOIN model m ON m.model_id = ae.model_id
			INNER JOIN project p ON p.project_id = ae.project_id
			WHERE p.project_id in (""" + (projects*.id).join(',') + """)
			GROUP BY ae.project_id, ae.asset_class, m.asset_type, mb.use_for_planning
			ORDER BY ae.project_id, mb.use_for_planning
		""")

		// This property will get used repeatedly in the following each iterator
		ProjectDailyMetric projectDailyMetric

		List assetsCountsList = jdbcTemplate.queryForList(assetsCountsQuery.toString())
		assetsCountsList.each { Map<String, Object> it ->

			projectDailyMetric = metrics.find { metric ->
				metric.project.id == it.project_id
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
	private def fillTasksMetrics(metrics, projects) {

		def projectDailyMetric

		def tasksCountsQuery = new StringBuilder("""
			SELECT ac.project_id, count(ac.asset_comment_id) as all_count, count(ac_done.asset_comment_id) as done_count
			FROM project p
			INNER JOIN asset_comment ac ON p.project_id = ac.project_id AND ac.comment_type = 'issue' AND ac.is_published = 1
			LEFT JOIN asset_comment ac_done ON ac_done.asset_comment_id = ac.asset_comment_id AND ac_done.date_resolved IS NOT NULL
			WHERE p.project_id in (""" + (projects*.id).join(',') + """)
			GROUP BY ac.project_id
			ORDER BY ac.project_id
		""")

		def tasksCountsList = jdbcTemplate.queryForList(tasksCountsQuery.toString())


		tasksCountsList.each {
			projectDailyMetric = metrics.find { metric ->
				metric.project.id == it.project_id
			}
			if (projectDailyMetric) {
				projectDailyMetric.tasksAll = it.all_count
				projectDailyMetric.tasksDone = it.done_count
			}
		}

	}

	/**
	 * Function used by activitySnapshot to retrieve dependencies information
	 */
	private def fillDependenciesMetrics(metrics, projects) {

		def projectDailyMetric

		def dependenciesCountsQuery = new StringBuilder("""
			SELECT ae.project_id, count(*) as count
			FROM asset_entity ae
			INNER JOIN asset_dependency ad ON ae.asset_entity_id = ad.asset_id
			INNER JOIN project p ON p.project_id = ae.project_id
			WHERE p.project_id in (""" + (projects*.id).join(',') + """)
			GROUP BY ae.project_id
			ORDER BY ae.project_id
		""")

		def dependenciesCountsList = jdbcTemplate.queryForList(dependenciesCountsQuery.toString())

		dependenciesCountsList.each {
			projectDailyMetric = metrics.find { metric ->
				metric.project.id == it.project_id
			}
			if (projectDailyMetric) {
				projectDailyMetric.dependencyMappings = it.count
			}
		}

	}

	/**
	 * Function used by activitySnapshot to retrieve persons and user login information
	 */
	private void fillUsersMetrics(metrics, List<Project> projects, sqlSearchDate) {

		def projectDailyMetric

		String personsCountsQuery = """
			SELECT
			  pr.party_id_from_id projectId,
			  COUNT(distinct party_id_to_id) as totalPersons,
			  COUNT(distinct u.username) as totalUserLogins,
			  COUNT(distinct ulpa.user_login_id, ulpa.date) as activeUserLogins
			FROM party_relationship pr
			  LEFT OUTER JOIN user_login u ON pr.party_id_to_id = u.person_id
			  LEFT OUTER JOIN user_login_project_access ulpa ON pr.party_id_from_id = ulpa.project_id and ulpa.date = ?
			WHERE
			  pr.role_type_code_from_id='$RoleType.CODE_PARTY_PROJECT' AND
			  pr.party_relationship_type_id='PROJ_STAFF' AND
			  pr.role_type_code_to_id='$RoleType.CODE_PARTY_STAFF'
			  AND pr.party_id_from_id in (''' + (projects*.id).join(',') + ''')
			GROUP BY pr.party_id_from_id
		"""

		jdbcTemplate.queryForList(personsCountsQuery, sqlSearchDate).each {
			projectDailyMetric = metrics.find { metric ->
				metric.project.id == it.projectId
			}
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
		Person.executeQuery("""
			from Person
			where id in (SELECT partyIdTo.id FROM PartyRelationship
			             WHERE partyRelationshipType='PROJ_STAFF'
			               AND partyIdFrom=:project
			               AND roleTypeCodeFrom='$RoleType.CODE_PARTY_PROJECT'
			               AND roleTypeCodeTo.id=:team)
			ORDER BY firstName, lastName
		""".toString(), [project:project, team:team], [sort: 'firstName'])
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
		PartyGroup owner = getOwner(project)
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
		log.debug "addTeamMember() project=${project.id}, person=${person.id}, teams=$teamCodes"
		partyRelationshipService.addProjectStaff(project, person)

		List currentTeams = person.getTeamsAssignedTo(project).id
		log.debug "addTeamMember() currentTeams=$currentTeams"
		CollectionUtils.asList(teamCodes).each { teamcode ->
			log.debug "addTeamMember() checking for teamcode=$teamcode"
			if (!currentTeams.contains(teamcode)) {
				log.debug "addTeamMember() going to add teamcode=$teamcode"
				partyRelationshipService.addStaffFunction(person, teamcode, person.company, project)
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
		PartyRelationship.executeUpdate("""
			DELETE PartyRelationship
			where partyRelationshipType.id='PROJ_STAFF'
			  and roleTypeCodeFrom.id='$RoleType.CODE_PARTY_PROJECT'
			  and roleTypeCodeTo.id in (:teamCodes)
			  and partyIdFrom=:project
			  and partyIdTo=:person
		""".toString(), [project: project, person: person, teamCodes: teamCodes])
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
		RoleType rtProject = RoleType.read(RoleType.CODE_PARTY_PROJECT)
		RoleType rtStaff = RoleType.read(RoleType.CODE_PARTY_STAFF)

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
		return projectId in (getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), null, null, userLogin)*.id)
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
						pr.roleTypeCodeFrom.id = '$RoleType.CODE_PARTY_PROJECT' and \
                        pr.roleTypeCodeTo.id = '$RoleType.CODE_PARTY_COMPANY' and \
						pr.partyIdTo = :owner".toString(), params)

               return projects
       }

	/**
	 * Helper method to get the PlanMethodologies Values of the Select List in the Project CRUD
	 * @param project
	 * @return the list of values or empty list is there is none
	 */
	private List<Map<String, String>> getPlanMethodologiesValues(Project project){
		List<Map> customFields = customDomainService.customFieldsList(
				project,
				AssetClass.APPLICATION.toString(),
				true
		)
		List<Map> planMethodologies = customFields.collect {
			[ field: it.field, label: (it.label ?: it.field) ]
		}
		if(planMethodologies){
			planMethodologies.add(0, [field:'', label:'Select...'])
		}

		return planMethodologies
	}

	/**
	 * Gets a list of project, and licence data to be used by the metrics aggregation server.
	 *
	 * @return A list of maps containing project and licence information.
	 */
	List<Map> projects(ProjectStatus projectStatus=ProjectStatus.ANY) {
//		List<Project> projects = getUserProjectsOrderBy(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE)
		List<Project> projects = getUserProjectsOrderBy(securityService.hasPermission(Permission.ProjectShowAll), projectStatus)

		projects.collect { Project project ->
			Map licenseData = licenseAdminService.getLicenseStateMap(project)

			return [
					id                   : project.id,
					guid                 : project.guid,
					projectName          : project.name,
					projectCode          : project.projectCode,
					clientName           : project.client.toString(),
					description          : project.description ?: '',
					comment				 : project.comment ?: '',
					startDate            : project.startDate,
					completionDate       : project.completionDate,
					licenseType          : licenseData.type == License.Type.MULTI_PROJECT ? 'GLOBAL':'PROJECT',
					licenseActivationDate: licenseData?.goodAfterDate,
					licenseExpirationDate: licenseData?.goodBeforeDate
			]
		}
	}

	/**
	 * Gets the list of clients, where each record has clientName and clientId.
	 * @return  The list of clients.
	 */
	List<Map> getAllClients() {
		Person whom = securityService.userLoginPerson
		def companies
		def query = """
			SELECT name as clientName, party_group_id as clientId
			FROM party_group pg
			INNER JOIN party p ON party_type_id='COMPANY' AND p.party_id=pg.party_group_id
			WHERE party_group_id in (
				SELECT party_id_to_id FROM party_relationship
				WHERE party_relationship_type_id = 'CLIENTS' AND role_type_code_from_id='$RoleType.CODE_PARTY_COMPANY'
				AND role_type_code_to_id='$RoleType.CODE_PARTY_CLIENT' AND party_id_from_id=:whomCompanyId
			) OR party_group_id=:whomCompanyId
			ORDER BY name"""

		companies = namedParameterJdbcTemplate.queryForList(query, [whomCompanyId: whom.company.id])
		return companies
	}


	/**
	 * Return the URL for the project's logo (or the default url).
	 * @param project
	 * @return a String with the path to the project's logo.
	 */
	String getProjectLogoUrl(Project project) {
		ProjectLogo projectLogo = ProjectLogo.findByProject(project)
		String logoUrl
		ApplicationTagLib atl = ApplicationContextHolder.getBean('applicationTagLib', ApplicationTagLib)
		if (projectLogo) {
			logoUrl = atl.createLink(controller: 'project', action: 'showImage', id: projectLogo.id)
		} else {
			String filename
			if (licenseCommonService.isManagerEnabled()) {
				filename = DEFAULT_LIC_MANAGER_LOGO
			} else {
				filename = DEFAULT_TRANSITIONMANAGER_LOGO
			}
			logoUrl = atl.resource(dir: DEFAULT_PROJECT_LOGO_DIR, file: filename)
		}
		return logoUrl
	}

	/**
	 * A factory method to create/update a ProjectLogo object from an File which will be associated to a project. This will
	 * attempt to lookup an existing ProjectLogo by project first and only create one if it doesn't already exist. It will then
	 * update the object with the content from the file object.
	 *
	 * @param project - the project to save logo to
	 * @param file - the File resource to reference
	 * @return the ProjectLogo object
	 */
	ProjectLogo createOrUpdateLogo(Project project, File file, String originalFilename) {
		assert file
		assert project

		ProjectLogo projectLogo = ProjectLogo.findByProject(project) ?: new ProjectLogo(project: project)
		projectLogo.name = originalFilename
		projectLogo.setData(new FileInputStream(file))
		return projectLogo.save(flush: true)
	}

	/**
	 * This methods creates (or updates) a project based on the given CommandObject. It also takes cares of
	 * cloning the default FieldSettings and creating the default bundle.
	 * 
	 * @param projectCommand
	 * @return
	 */
	@Transactional
	Project createOrUpdateProject(ProjectCommand projectCommand) {
		Project project
		if (projectCommand.id > 0) {
			if (securityService.hasAccessToProject(projectCommand.id)) {
				project = Project.get(projectCommand.id)
			} else {
				UserLogin userlogin = securityService.getUserLogin()
				throw new InvalidParamException("User ${userlogin.username} doesn't have access to the project ${projectCommand.id}.")
			}
		} else {
			project = new Project()
		}
		
		project.with {
			client = PartyGroup.findById(projectCommand.clientId)
			comment = projectCommand.comment
			completionDate = projectCommand.completionDate
			description = projectCommand.description
			guid = StringUtil.generateGuid()
			name = projectCommand.projectName
			planMethodology = projectCommand.planMethodology
			collectMetrics = projectCommand.collectMetrics ? 1 : 0
			projectCode = projectCommand.projectCode
			projectType = projectCommand.projectType
			runbookOn = projectCommand.runbookOn
			startDate = projectCommand.startDate
			timezone = getTimezone(projectCommand.timeZone)
		}

		project.save(failOnError: true, flush: true)

		// Set the project's owner.
		setOwner(project, securityService.userLoginPerson.company)

		// Assign partners
		updateProjectPartners(project, projectCommand.partnerIds)

		if (!projectCommand.id) {
			// Clone default field settings
			cloneDefaultSettings(project)

			// Create the default bundle
			createDefaultBundle(project, projectCommand.defaultBundleName)
		} else {
			if (projectCommand.defaultBundle) {
				def bundle = MoveBundle.findById(projectCommand.defaultBundle.id)
				if (bundle) {
					project.defaultBundle = bundle
				}
			}
		}

		// Deal with the Project Manager if one is supplied
		if (projectCommand.projectManagerId > 0) {
			personService.addToProjectTeam(project.id, projectCommand.projectManagerId, RoleType.CODE_TEAM_PROJ_MGR)
		}

		// Deal with the adding the project logo if one was supplied
		if (projectCommand.projectLogo && projectCommand.originalFilename) {
			File logoFile = fileSystemService.openTempFile(projectCommand.projectLogo)
			createOrUpdateLogo(project, logoFile, projectCommand.originalFilename)
		}
		else {
			if (!projectCommand.projectLogo) {
				ProjectLogo projectLogo = ProjectLogo.findByProject(project)
				if(projectLogo) {
					projectLogo.delete()
				}
			}
		}

		// Set the new project as the user's current project.
		userPreferenceService.setCurrentProjectId(project.id)

		project.save()

		return project
	}

	/**
	 * @return the PersonService instance from the container.
	 */
	private PersonService getPersonService() {
		return ApplicationContextHolder.getBean('personService', PersonService)
	}
}
