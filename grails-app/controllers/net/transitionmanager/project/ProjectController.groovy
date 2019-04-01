package net.transitionmanager.project

import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectLogo
import net.transitionmanager.domain.Timezone
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.AuditService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.UserService
import org.apache.commons.lang.StringEscapeUtils
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ProjectController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	AssetEntityService assetEntityService
	AuditService auditService
	ControllerService controllerService
	CustomDomainService customDomainService
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProjectService projectService
	Scheduler quartzScheduler
	UserPreferenceService userPreferenceService
	UserService userService

	@HasPermission(Permission.ProjectView)
	def list() {
		[active: params.active ?: 'active']
	}

	/**
	 * Generate the List for projects using jqgrid.
	 * @return : list of projects as JSON
	 */
	@HasPermission(Permission.ProjectView)
	def listJson() {
		String sortIndex = params.sidx ?: 'projectCode'
		String sortOrder  = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)

		def searchParams = [maxRows: maxRows, currentPage: currentPage,
		                    sortOn: ProjectSortProperty.valueOfParam(sortIndex),
		                    sortOrder: SortOrder.valueOfParam(sortOrder), params: params]

		ProjectStatus projectStatus = ProjectStatus.lookup(params.isActive) ?: ProjectStatus.COMPLETED

		def projectList = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), projectStatus, searchParams)

		int totalRows = projectList?.isEmpty() ? 0 : projectList?.getTotalCount()
		int numberOfPages = totalRows ? Math.ceil(totalRows / maxRows) : 1

		def results = projectList?.collect {
			String startDate = TimeUtil.formatDate(it.startDate)
			String completionDate = TimeUtil.formatDate(it.completionDate)
			[cell: [it.projectCode, it.name, startDate, completionDate, it.comment], id: it.id]
		}

		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	@HasPermission(Permission.ProjectView)
	def show() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		String companyId = securityService.userLoginPerson.company.id

		// Save and load various user preferences
		userPreferenceService.setCurrentProjectId(project.id)
		userPreferenceService.setPreference(PREF.PARTY_GROUP, companyId)

		if (!userPreferenceService.getPreference(PREF.CURR_POWER_TYPE)) {
			userPreferenceService.setPreference(PREF.CURR_POWER_TYPE, "Watts")
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

		[
			projectInstance      : project,
			timezone             : project.timezone.label ?: '',
			client               : project.client,
			defaultBundle        : project.defaultBundle,
			projectPartners      : partyRelationshipService.getProjectPartners(project),
			projectManagers      : projectService.getProjectManagers(project),
			projectLogoForProject: projectLogo,
			isDeleteable         : isDeleteable,
			planMethodology      : planMethodology
		]
	}

	/**
	 * Used to delete the user's current project
	 */
	@HasPermission(Permission.ProjectDelete)
	def delete() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		log.info "Project $project.name($project.id) is going to be deleted by $securityService.currentUsername"
		try {
			def message = projectService.deleteProject(project.id, true)

			flash.message = "Project $project.name deleted"
			redirect(controller:"projectUtil", params:['message':flash.message])
		} catch (Exception ex) {
			flash.message = ex.message
			redirect(action:"list")
		}
	}

	@HasPermission(Permission.ProjectEdit)
	def edit() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def projectPartners = partyRelationshipService.getProjectPartners(project)
		PartyGroup company = projectService.getOwner(project)

		def projectDetails
		def moveBundles
		if (project) {
			projectDetails = projectService.getprojectEditDetails(project)
			moveBundles = MoveBundle.findAllByProject(project)

			List<Map> planMethodologies = projectService.getPlanMethodologiesValues(project)

			List projectManagers = projectService.getProjectManagers(project)
			projectManagers.sort { a,b ->
				a.firstName <=> b.firstName ?: a.lastName <=> b.lastName
			}

			return [
				company: company,
				projectInstance : project,
				projectPartner: projectDetails.projectPartner,
				projectManager: projectDetails.projectManager,
				moveManager: projectDetails.moveManager,
				companyStaff: projectDetails.companyStaff,
				clientStaff: projectDetails.clientStaff,
				partnerStaff: projectDetails.partnerStaff,
				companyPartners: projectDetails.companyPartners,
				projectLogoForProject: projectDetails.projectLogoForProject,
				workflowCodes: projectDetails.workflowCodes,
				projectPartners: projectPartners,
				projectManagers: projectManagers,
				moveBundles:moveBundles,
				planMethodologies: planMethodologies
			]
		}
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

	/*
	 * Update the Project details
	 */
	@HasPermission(Permission.ProjectEdit)
	def update() {
		Project.withTransaction { status ->

			Project project = controllerService.getProjectForPage(this)
			if (!project) return

			PartyGroup company = projectService.getOwner(project)

			//  When the Start date is initially selected and Completion Date is blank, set completion date to the Start date
			def startDate = params.startDate
			def completionDate = params.completionDate
			if (startDate) {
				params.startDate = TimeUtil.parseDate(startDate)
			}
			if (completionDate){
				params.completionDate = TimeUtil.parseDate(completionDate)
			}
			params.timezone = retrievetimeZone(params.timezone)

			params.collectMetrics = params.collectMetrics == "1" ? 1: 0
			params.runbookOn = 1
			project.properties = params

			List<Map> planMethodologies = projectService.getPlanMethodologiesValues(project)

			def logoFile = controllerService.getUploadImageFile(this, 'projectLogo', 50000)
			if (logoFile instanceof String) {
				flash.message = logoFile
				def projectDetails = projectService.getprojectEditDetails(project)
				def projectPartners = partyRelationshipService.getProjectPartners(project)
				def projectManagers = projectService.getProjectManagers(project)
				def moveBundles = MoveBundle.findAllByProject(project)

				def model = [
						company: company,
						projectInstance: project,
						projectPartner: projectDetails.projectPartner,
						projectManager: projectDetails.projectManager,
						moveManager: projectDetails.moveManager,
						companyStaff: projectDetails.companyStaff,
						clientStaff: projectDetails.clientStaff,
						partnerStaff: projectDetails.partnerStaff,
						companyPartners: projectDetails.companyPartners,
						workflowCodes: projectDetails.workflowCodes,
						projectLogoForProject: projectDetails.projectLogoForProject,
						prevParam: params,
						projectPartners: projectPartners,
						projectManagers: projectManagers,
						moveBundles: moveBundles,
						planMethodologies: planMethodologies
				]

				render(view: 'edit', model: model)
				return
			}




			if (!project.hasErrors() && project.save() ) {

				projectService.updateProjectPartners(project, params.projectPartners)

				// Deal with the image

                // if the logo was deleted and no new logo was uploaded, delete the previous logo
                if (params.isLogoDeleted.toBoolean() && !logoFile) {
                    deleteImage()
                }

				if (logoFile) {
					projectService.createOrUpdate(project, logoFile)
				}

				// Audit project changes
				auditService.saveUserAudit(UserAuditBuilder.projectConfig(project))

				flash.message = "Project $project updated"
				redirect(action:"show")

			} else {
				flash.message = 'Some properties were not properly defined'
				def projectDetails = projectService.getprojectEditDetails(project)
				def projectPartners = partyRelationshipService.getProjectPartners(project)
				def projectManagers = projectService.getProjectManagers(project)
				def moveBundles = MoveBundle.findAllByProject(project)

				def model = [
						company: company,
						projectInstance: project,
						projectPartner: projectDetails.projectPartner,
						projectManager: projectDetails.projectManager,
						moveManager: projectDetails.moveManager,
						companyStaff: projectDetails.companyStaff,
						clientStaff: projectDetails.clientStaff,
						partnerStaff: projectDetails.partnerStaff,
						companyPartners: projectDetails.companyPartners,
						workflowCodes: projectDetails.workflowCodes,
						projectLogoForProject: projectDetails.projectLogoForProject,
						prevParam:params,
						projectPartners: projectPartners,
						projectManagers: projectManagers,
						moveBundles:moveBundles,
						planMethodologies: planMethodologies
				]

				render(view: 'edit', model: model)
			}
		} // Project.withTransaction(t) {
	}

	/*
	 * Populate and present the create view for a new project
	 */
	@HasPermission(Permission.ProjectCreate)
	def create() {
		PartyGroup company = securityService.userLoginPerson.company
		Map projectDetails = projectService.getCompanyPartnerAndManagerDetails(company)
		// Copy plan methodology field from the default project
		Project defaultProject = Project.defaultProject
		List<Map> planMethodologies = projectService.getPlanMethodologiesValues(defaultProject)
		params.planMethodology = defaultProject.planMethodology

		[clients: projectDetails.clients, company: company, managers: projectDetails.managers,
		 partners: projectDetails.partners, projectInstance: new Project(params),
		 workflowCodes: projectDetails.workflowCodes, planMethodologies:planMethodologies]
	}

	/**
	 * Used by the Project Create to actually create the project along with the following other tasks:
	 *    - associate partner companies
	 *    - associate the Project Manager
	 *    - save a partner logo
	 *    - create the default 'TBD' bundle
	 */
	@HasPermission(Permission.ProjectCreate)
	def save() {

		Project.withTransaction { status ->

			PartyGroup company = securityService.userLoginPerson.company

			//
			// Properly set some of the parameters that before injecting into the Project domain
			//
			def startDate = params.startDate
			def completionDate = params.completionDate
			if (startDate) {
				params.startDate = TimeUtil.parseDate(startDate)
			}
			if (completionDate) {
				params.completionDate = TimeUtil.parseDate(completionDate)
			}
			params.runbookOn =  1	// Default to ON
			params.timezone = retrievetimeZone(params.timezone)
			params.collectMetrics = params.collectMetrics == "1" ? 1: 0

			Project project = new Project(params)

			def partnersIds = params.projectPartners

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

				render(view: 'create', model: [
					company: company, projectInstance: project, clients: projectDetails.clients,
					partners: projectDetails.partners, managers: projectDetails.managers,
					workflowCodes: projectDetails.workflowCodes, planMethodologies:planMethodologies, prevParam: params
				] )
				return
			}

			projectService.setOwner(project,company)

			// Save the partners to be related to the project
			projectService.updateProjectPartners(project, partnersIds)

			// Clone any settings from the Default Project
			projectService.cloneDefaultSettings(project)

			// Deal with the Project Manager if one is supplied
			Long projectManagerId = NumberUtil.toPositiveLong(params.projectManagerId, -1)
			if (projectManagerId > 0) {
				personService.addToProjectTeam(project.id.toString(), projectManagerId.toString(), "ROLE_PROJ_MGR")
			}

			// Deal with the adding the project logo if one was supplied
			ProjectLogo projectLogo

			if (logoFile) {
				projectLogo = projectService.createOrUpdate(project, logoFile)
			}

			userPreferenceService.setCurrentProjectId(project.id)

			/* Create and assign the default Bundle for this project. Although the bundle
			* is assigned in ProjectService::getDefaultBundle, it's done here too for visibility. */
			project.defaultBundle = projectService.getDefaultBundle(project, (String)params.defaultBundleName)
			project.save()

			flash.message = "Project $project was created"
			redirect( action:"show",  imageId:projectLogo?.id )

		} // Project.withTransaction
	}

	/*
	 * An Ajax web service that is use to retrieve the list of active staff that can be associated with any project. This is typically
	 * used with the Project create process so there won't be a project. There staff list will consist of those from the user's company,
	 * the staff of the partners of the user's company and the staff of the company indicated as the client.
	 *
	 * @params request.JSON.client - the selected client that the project will be for
	 * @params request.JSON.partners - the ids of partners to be associated with the project
	 * @params request.JSON.role - the role to lookup (e.g. PROJ_MGR)
	 * @params request.JSON.q - the search query to filter on persons name
	 * @return JSON array:
 	 * 		staffList: List<Map> of staff [id:person.id, text: "firstName lastName, Company"]
	 */
	@HasPermission(Permission.ProjectStaffList)
	def fetchStaffList() {

		Person whom = securityService.userLoginPerson

		String query = request.JSON.q?.toLowerCase() ?: ''

		def client
		Long clientId = NumberUtil.toPositiveLong(request.JSON.client, -1)
		if (clientId > 0) {
			// Validate that the client is associated with the user's company
			List allClients = partyRelationshipService.getCompanyClients(whom.company)*.partyIdTo
			client = allClients.find { it.id == clientId }
			if (! client ) {
				securityService.reportViolation("attempted to access unassociated client (id $pid)")
			}
		} else {
			if (clientId == -1) {
				log.warn "retrievePartnerStaffList() called with invalid client id ($clientId)"
			}
		}

		List partnersList = []
		if (request.JSON.partners) {
			// Get the list of all of the user's company's partners
			List allPartnersList = partyRelationshipService.getCompanyPartners(whom.company)*.partyIdTo

			// Iterate over the list of partner ids we received and attempt to match up to a partner in allPartnersList
			new JsonSlurper().parseText(request.JSON.partners).each { p ->
				Long pid = NumberUtil.toPositiveLong(p, -1)
				if (pid > 0) {
					def partner = allPartnersList.find { it.id == pid }
					if (partner) {
						partnersList.add(partner)
					} else {
						securityService.reportViolation("attempted to access unassociated partner (id $pid)")
					}
				} else {
					log.warn "retrievePartnerStaffList() called with invalid partner id ($p)"
				}
			}
		}

		// Use the passed role or default all staff
		String staffRole = request.JSON.role ?: 'ROLE_STAFF'
		List<Map> staffList = []
		String staffQuery = "FROM PartyRelationship p \
			WHERE p.partyRelationshipType = 'STAFF' AND p.partyIdFrom = :company \
			AND p.roleTypeCodeFrom.id = 'ROLE_COMPANY' AND p.roleTypeCodeTo.id = :staffRole"

		// Closure to do query and load the staff into the staffList variable for the given company
		def getCompanyStaffClosure = { def company ->
			List companyStaff = PartyRelationship.findAll(staffQuery, [company:company, staffRole:staffRole]).partyIdTo
			String lowerCompany = company.toString().toLowerCase()
			companyStaff.findAll({ it.active == 'Y' }).each { s ->
				String name = s.toString()

				// If there was a query then filter on the name
				if (query && ! ( name.toLowerCase().contains(query) || lowerCompany.contains(query)) ) {
					return
				}

				// Create text as: firstName lastName, Company
				staffList.add([id:s.id, text: "$s, $company.name" ])
			}
		}

		getCompanyStaffClosure(whom.company)

		if (client) {
			getCompanyStaffClosure(client)
		}

		partnersList.each { partner ->
			getCompanyStaffClosure(partner)
		}

		// Now sort the list on the persons' name
		staffList.sort { it.text }

		Map json = [ staffList:staffList ]
		render json as JSON
	}

	@HasPermission(Permission.ProjectEdit)
	def cancel() {
		redirect(controller:'projectUtil')
	}

	/*
	 * Updates the user's project. It also resets the preferences for:
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def addUserPreference() {
		Long selectProject = params.long('id')
		String errMsg
		if(selectProject){
			if(userService.changeProjectContext(selectProject)){
				def browserTest = request.getHeader("User-Agent").toLowerCase().contains("mobile")
				if ( browserTest || params.mobileSelect ) {
					redirect(controller: 'task', action: 'listUserTasks', params: [viewMode: 'mobile'])
				}
				else {
					redirect(action: "show", id: params.id)
				}
			} else {
				errMsg = "Unable to update your Project Preference."
			}
		} else {
			errMsg = "Please select Project"
		}

		if(errMsg){
			flash.message = errMsg
			redirect( action:"list" )
		}
	}

	/**
	 * Used to render a project logo if it exists
	 */
	@HasPermission(Permission.ProjectView)
	def showImage() {
		if( params.id ) {
			ProjectLogo logo = ProjectLogo.get( params.id )
			response.contentType = 'image/jpg'
			if (logo) {
				response.outputStream << logo.partnerImage
			}
			// TODO : JPM 12/2016 : showImage() should have solution for missing logos
			response.outputStream.flush()
		}
	}

	/**
	 * Used to delete the project logo for the project in the users context
	 */
	@HasPermission(Permission.ProjectEdit)
	private def deleteImage() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def pl = ProjectLogo.findByProject(project)
		if (pl) {
			ProjectLogo.withTransaction {
				pl.delete(flush: true)
			}

			flash.message = "Project logo was deleted"
		} else {
			flash.message = "Project logo was not found"
		}

	}

	/*
	* function to set the user preference powerType
	*/
	@HasPermission(Permission.UserUpdateOwnAccount)
	def setPower() {
		userPreferenceService.setPreference(PREF.CURR_POWER_TYPE, params.p)
		render params.p
	}

	/**
	 * Action to render the Field Settings (aka Importance) Show/Edit maintenance form for field importance and field tooltips
	 */
	@HasPermission(Permission.ProjectFieldSettingsView)
	def fieldImportance() {
		throw new RuntimeException('fieldImportance is no longer used')
		[project: securityService.userCurrentProject,
		 hasProjectFieldSettingsEditPermission: securityService.hasPermission(Permission.ProjectFieldSettingsEdit)]
	}

	/**
	 * To create json data to for a given entity type
	 *@param : entityType type of entity.
	 *@return : json data
	 * TM-6617
	 */
	@HasPermission(Permission.AssetView)
	@Deprecated
	def retrieveAssetFields() {
		throw new RuntimeException('retrieveAssetFields is no longer used')
	}

	/**
	 * Used to select project time zone
	 * @param timezone default timezone selected
	 * @render time zone view
	 */
	@HasPermission(Permission.ProjectEdit)
	def showTimeZoneSelect() {
		def timezone = params.timezone ?: TimeUtil.defaultTimeZone
		def timezones = Timezone.findAll()
		def areas = userPreferenceService.timezonePickerAreas()

		render(template:"showTimeZoneSelect",model:[areas: areas, timezones: timezones, currTimeZone: timezone, userPref:false])
	}

	@HasPermission(Permission.ProjectFieldSettingsView)
	@Deprecated
	// TM-6617
	def showImportanceFields() {
		throw new RuntimeException('showImportanceFields is no longer used')
		render( view: "showImportance", model: [])
	}

	@HasPermission(Permission.ProjectFieldSettingsEdit)
	@Deprecated
	// TM-6617
	def editImportanceFields() {
		throw new RuntimeException('editImportanceFields is no longer used')
		render( view: "editImportance", model: [])
	}

	/**
	 * Used to launch the project metrics daily job for testing purposes.
	 */
	@HasPermission(Permission.ReportViewProjectDailyMetrics)
	def launchProjectDailyMetricsJob() {
		def params = [:]
		String key = "ProjectDailyMetrics-" + UUID.randomUUID()
		def jobName = "TM-" + key

		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl(jobName, null, new Date(System.currentTimeMillis() + 2000) )
		trigger.jobDataMap.putAll(params)
		trigger.jobDataMap.key = key
		trigger.setJobName('ProjectDailyMetricsJob')
		trigger.setJobGroup('tdstm-project-daily-metrics')
		quartzScheduler.scheduleJob(trigger)
		render( view: "projectDailyMetrics", model: [success: true])
	}

	/**
	 * Renders the form so Admin's can initiate the bulk Account Activation
	 * Notification process.
	 */
	@HasPermission(Permission.UserSendActivations)
	def userActivationEmailsForm() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		[project: project.name, client: project.client.name,
		 defaultEmail: StringEscapeUtils.escapeHtml(grailsApplication.config.grails.mail.default.from),
		 accounts: projectService.getAccountActivationUsers(project), adminEmail: securityService.userLoginPerson.email]
	}

	/**
	 * Sends out an Activation Email to those accounts selected by the admin.
	 */
	@HasPermission(Permission.UserSendActivations)
	def sendAccountActivationEmails() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		String message
		def selectedAccounts = params.person
		// Validate the user selected at least an account.
		if(selectedAccounts){
			try{
				def accounts = projectService.getAccountActivationUsers(project)
				// Find the accounts that could get the notice and filter only those that were checked in the form
				List accountsToNotify = accounts.findAll{ it.personId.toString() in selectedAccounts}
				if (accountsToNotify) {
					def fromEmail = grailsApplication.config.grails.mail.default.from
					fromEmail = StringEscapeUtils.escapeHtml(fromEmail)
					projectService.sendBulkActivationNotificationEmail(accountsToNotify, params.customMessage, fromEmail, request.getRemoteAddr())
					message = "The Account Activation Notification has been sent out to the users."
				}else{
					message = "No Accounts selected for notification."
				}
			} catch(Exception e) {
				message = "There was an error while processing the email notifications. Please contact Support."
				log.error ExceptionUtil.messageWithStacktrace("sendAccountActivationEmails blew up", e)
			}

		}else{
			message = "No Accounts selected for notification."
		}

		flash.message = message
		forward action: 'userActivationEmailsForm'
	}
}
