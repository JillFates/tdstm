package net.transitionmanager.project

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.ProjectCommand
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.PersonService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.security.Permission
/**
 * Handles WS calls of the ProjectsService
 */
@Secured('isAuthenticated()')
@Slf4j
class WsProjectController implements ControllerMethods {
	PartyRelationshipService partyRelationshipService
	CustomDomainService customDomainService
	PersonService personService
	ProjectService projectService
	UserPreferenceService userPreferenceService
	MoveBundleService moveBundleService

	/**
	 * Gets the projects associated to a user
	 */
	def userProjects() {
		def projectStatus = ProjectStatus.lookup(params.status) ?: ProjectStatus.ACTIVE
		// TODO : JPM 2/2018 : This should be a Command Object
		Map searchParams = [
			maxRows: params.maxRows,
			currentPage: params.currentPage,
			sortOn: ProjectSortProperty.valueOfParam(params.sortOn),
			sortOrder: SortOrder.valueOfParam(params.sortOrder)
		]
		def projects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), projectStatus, searchParams)
		Map dataMap = [:]
		List results = []
		projects.each { project ->
			results.add(name: project.name, description: project.description, clientId: project.client.id,
				id: project.id, projectCode: project.projectCode, status: project.getStatus(),
				clientName: project.client.name, completionDate: project.completionDate)
		}

		dataMap.projects = results

		renderSuccessJson(dataMap)
	}

	/**
	 * Returns a list of projects, and their licence data.
	 *
	 * @return A list of projects, and their licence data.
	 */
	@HasPermission(Permission.ProjectView)
	def projects() {
		ProjectStatus projectStatus =  ProjectStatus.ANY
		if (params.status) {
			projectStatus = ProjectStatus.lookup(params.status)
			if (! projectStatus) {
				throw new InvalidParamException('Invalid value for parameter status, options are: ANY, ACTIVE or COMPLETED')
			}
		}
		renderSuccessJson(projectService.projects( projectStatus ))
	}

	@HasPermission(Permission.ProjectView)
	def projectsForProjectList() {
		renderSuccessJson([activeProjects: projectService.projects( ProjectStatus.ACTIVE ), completedProjects: projectService.projects( ProjectStatus.COMPLETED )] )
	}

	/*
	 * Populate and present the create view for a new project
	 */
	@HasPermission(Permission.ProjectCreate)
	def getModelForProjectCreate() {
		PartyGroup company = securityService.userLoginPerson.company
		Map projectDetails = projectService.getCompanyPartnerAndManagerDetails(company)
		// Copy plan methodology field from the default project
		Project defaultProject = Project.defaultProject

		List<Map> planMethodologies = projectService.getPlanMethodologiesValues(defaultProject)
		List<String> projectTypes = com.tdssrc.grails.GormUtil.getConstrainedProperties(Project).projectType.inList

		renderSuccessJson([
				  clients          : projectDetails.clients,
				  company          : company,
				  managers         : projectDetails.managers,
				  partners         : projectDetails.partners,
				  projectInstance  : new Project(params),
				  projectTypes     : projectTypes,
				  planMethodologies: planMethodologies
		])
	}

	@HasPermission(Permission.ProjectView)
	def getModelForProjectViewEdit(String projectId) {
		Project project = Project.get(projectId)
		if (!project) return

		userPreferenceService.setCurrentProjectId(project.id)
		PartyGroup company = securityService.userLoginPerson.company
		PartyGroup projectOwner = projectService.getOwner(project)
		String companyId = company.id
		Map projectDetails = projectService.getCompanyPartnerAndManagerDetails(projectOwner)

		// Save and load various user preferences
		userPreferenceService.setCurrentProjectId(project.id)
		userPreferenceService.setPreference(UserPreferenceEnum.PARTY_GROUP, companyId)

		if (!userPreferenceService.getPreference(UserPreferenceEnum.CURR_POWER_TYPE)) {
			userPreferenceService.setPreference(UserPreferenceEnum.CURR_POWER_TYPE, "Watts")
		}

		def imageId
		ProjectLogo projectLogo = ProjectLogo.findByProject(project)
		if (projectLogo) {
			imageId = projectLogo.id
		}
		session.setAttribute('setImage', imageId)
		boolean isDeleteable = securityService.hasPermission(Permission.ProjectDelete) && !project.isDefaultProject()

		Map planMethodology = [:]
		if (project.planMethodology) {
			planMethodology= customDomainService.findCustomField(project, AssetClass.APPLICATION.toString()) {
				it.field == project.planMethodology
			}
		}

		// Log a warning if the planMethodology field spec was not found but one is defined
		if (!planMethodology && project.planMethodology) {
			log.warn "Project ${project.id} has plan methodlogy define as ${project.planMethodology} but the field is not in field settings"
		}

		List<Map> planMethodologies = projectService.getPlanMethodologiesValues(project)
		List<String> projectTypes = com.tdssrc.grails.GormUtil.getConstrainedProperties(Project).projectType.inList
		List availableBundles = moveBundleService.lookupList(project)

		renderSuccessJson([
				  clients              : projectDetails.clients,
				  projectInstance      : project.toMap(),
				  timezone             : project.timezone?.label ?: '',
				  client               : project.client,
				  defaultBundle        : project.defaultBundle.toMap(),
				  availableBundles     : availableBundles,
				  possiblePartners     : projectDetails.partners,
				  possibleManagers     : projectDetails.managers,
				  projectPartners      : partyRelationshipService.getProjectPartners(project),
				  projectManagers      : projectService.getProjectManagers(project).collect { it -> it.toString() },
				  projectLogoForProject: projectLogo,
				  isDeleteable         : isDeleteable,
				  planMethodology      : planMethodology,
				  projectTypes         : projectTypes,
				  planMethodologies    : planMethodologies
		])
	}

	@HasPermission(Permission.ProjectEdit)
	def saveProject(String projectId) {
		ProjectCommand projectCommand = populateCommandObject(ProjectCommand)

		if (projectId) {
			projectCommand.id = projectId.toLong()
		}
		Project project = projectService.createOrUpdateProject(projectCommand)
		renderSuccessJson(project.toMap())
	}

	/**
	 * Used to delete the user's current project
	 */
	@HasPermission(Permission.ProjectDelete)
	def deleteProject(Long projectId) {
		Project project = fetchDomain(Project, [id: projectId])

		log.info "Project $project.name($project.id) is going to be deleted by $securityService.currentUsername"
		projectService.deleteProject(project.id, true)

		renderSuccessJson("Project $project.name deleted")
	}
}
