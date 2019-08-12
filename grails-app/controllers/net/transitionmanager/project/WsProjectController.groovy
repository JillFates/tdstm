package net.transitionmanager.project

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.common.ControllerService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.Timezone
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyGroupService
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.PersonService
import net.transitionmanager.security.Permission
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.security.RoleType

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
	ControllerService controllerService

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
		List<Map> managers = projectDetails.managers.collect { it -> [name: it.partyIdTo.toString(), id: it.partyIdTo.id ] }
		List<Map> planMethodologies = projectService.getPlanMethodologiesValues(defaultProject)
		List<String> projectTypes = com.tdssrc.grails.GormUtil.getConstrainedProperties(Project).projectType.inList
		params.planMethodology = defaultProject.planMethodology

		renderSuccessJson([
				clients: projectDetails.clients,
				company: company,
				managers: managers,
		 		partners: projectDetails.partners,
				projectInstance: new Project(params),
		 		workflowCodes: projectDetails.workflowCodes,
				projectTypes: projectTypes,
				planMethodologies:planMethodologies ])
	}

	@HasPermission(Permission.ProjectView)
	def getModelForProjectViewEdit(String projectId) {
		Project project = Project.get(projectId)
		if (!project) return

		PartyGroup company = securityService.userLoginPerson.company
		String companyId = company.id
		Map projectDetails = projectService.getCompanyPartnerAndManagerDetails(company)

		// Save and load various user preferences
		userPreferenceService.setCurrentProjectId(project.id)
		userPreferenceService.setPreference(UserPreferenceEnum.PARTY_GROUP, companyId)

		if (!userPreferenceService.getPreference(UserPreferenceEnum.CURR_POWER_TYPE)) {
			userPreferenceService.setPreference(UserPreferenceEnum.CURR_POWER_TYPE, "Watts")
		}

		def imageId
		def projectLogo = ProjectLogo.findByProject(project)
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

		renderSuccessJson([
				projectInstance      : project,
				timezone             : project.timezone?.label ?: '',
				client               : project.client,
				defaultBundle        : project.defaultBundle,
				possiblePartners	 : projectDetails.partners,
				projectPartners      : partyRelationshipService.getProjectPartners(project),
				projectManagers      : projectService.getProjectManagers(project),
				projectLogoForProject: projectLogo,
				isDeleteable         : isDeleteable,
				planMethodology      : planMethodology
		])
	}

	private def retrievetimeZone(timezoneValue) {
		def result
		if (StringUtil.isBlank(timezoneValue)) {
			result = Timezone.findByCode(TimeUtil.defaultTimeZone)
		} else {
			def tz = Timezone.findByCode(timezoneValue)
			if (tz) {
				result = tz
			} else {
				result = Timezone.findByCode(TimeUtil.defaultTimeZone)
			}
		}
		return result
	}

	@HasPermission(Permission.ProjectEdit)
	def saveProject(String projectId) {
		Project.withTransaction { status ->
			def requestParams = request.JSON
			PartyGroup company = securityService.userLoginPerson.company

			//
			// Properly set some of the parameters that before injecting into the Project domain
			//
			def startDate = requestParams.startDate
			def completionDate = requestParams.completionDate
			if (startDate) {
				requestParams.startDate = TimeUtil.parseISO8601Date(startDate)
			}
			if (completionDate) {
				requestParams.completionDate = TimeUtil.parseISO8601Date(completionDate)
			}
			requestParams.runbookOn =  1	// Default to ON
			requestParams.timeZone = retrievetimeZone(requestParams.timeZone)
			requestParams.collectReportingMetrics = requestParams.collectMetrics == "1" ? 1: 0

			Project project = new Project(
					[
							client:  PartyGroup.findById(requestParams.clientId),
							name: requestParams.projectName,
					        projectCode: requestParams.projectCode,
							description: requestParams.description,
							startDate: requestParams.startDate,
							completionDate: requestParams.completionDate,
							workflowCode: requestParams.workflowCode,
							projectType: requestParams.projectType,
							runbookOn: requestParams.runbookOn,
							timezone: requestParams.timeZone
					]
			)

			def partnersIds = requestParams.projectPartners
			params.projectLogo = requestParams.projectLogo

			def logoFile = controllerService.getUploadImageFile(this, 'projectLogo', 50000)

			project.guid = StringUtil.generateGuid()

			if (logoFile instanceof String || project.hasErrors() || !project.save(flush:true)) {
				if (logoFile instanceof String) {
					flash.message = logoFile
				}
				else {
					flash.message = 'Some properties were not properly defined'
				}

				project.discard()

				Map projectDetails = projectService.getCompanyPartnerAndManagerDetails(company)

				List<Map> planMethodologies = projectService.getPlanMethodologiesValues(Project.defaultProject)

				renderErrorJson([
						company: company, projectInstance: project, clients: projectDetails.clients,
						partners: projectDetails.partners, managers: projectDetails.managers,
						workflowCodes: projectDetails.workflowCodes, planMethodologies:planMethodologies, prevParam: requestParams
				] )
				return
			}

			projectService.setOwner(project,company)

			// Save the partners to be related to the project
			projectService.updateProjectPartners(project, partnersIds)

			// Clone any settings from the Default Project
			projectService.cloneDefaultSettings(project)

			// Deal with the Project Manager if one is supplied
			Long projectManagerId = NumberUtil.toPositiveLong(requestParams.projectManagerId, -1)
			if (projectManagerId > 0) {
				personService.addToProjectTeam(project.id.toString(), projectManagerId.toString(), RoleType.CODE_TEAM_PROJ_MGR)
			}

			// Deal with the adding the project logo if one was supplied
			ProjectLogo projectLogo

			if (logoFile) {
				projectLogo = projectService.createOrUpdate(project, logoFile)
			}

			userPreferenceService.setCurrentProjectId(project.id)

			/* Create and assign the default Bundle for this project. Although the bundle
			* is assigned in ProjectService::getDefaultBundle, it's done here too for visibility. */
			project.defaultBundle = projectService.getDefaultBundle(project, (String)requestParams.defaultBundleName)
			project.save()

			flash.message = "Project $project was created"
			renderSuccessJson()

		} // Project.withTransaction
	}
}
