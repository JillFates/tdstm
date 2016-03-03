import groovy.json.JsonSlurper

import java.util.List;
import java.util.Map;

import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder

import com.tds.asset.FieldImportance;

import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONObject

import com.tds.asset.FieldImportance
import com.tdsops.tm.enums.domain.EntityType
import com.tdsops.tm.enums.domain.ValidationType
import com.tdsops.common.builder.UserAuditBuilder
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import org.apache.commons.lang.math.NumberUtils
import com.tdsops.common.lang.ExceptionUtil

import org.quartz.SimpleTrigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.quartz.Trigger

import org.apache.commons.lang.StringEscapeUtils

class ProjectController {
	def userPreferenceService
	def partyRelationshipService
	def stateEngineService
	def personService
	def projectService
	def securityService
	def controllerService
	def quartzScheduler
	def grailsApplication
	def auditService
	def assetEntityService

	def index() { redirect(action:"list",params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list() {
		return [active:params.active?:'active']
	}
	/**
	 * Used to generate the List for projects using jqgrid.
	 * @return : list of projects as JSON
	 */
	def listJson() {
		def sortIndex = params.sidx ?: 'projectCode'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		
		def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
		def now = TimeUtil.nowGMT()

		def searchParams = [:]
		searchParams.maxRows = maxRows
		searchParams.currentPage = currentPage
		searchParams.sortOn = ProjectSortProperty.valueOfParam(sortIndex)
		searchParams.sortOrder = SortOrder.valueOfParam(sortOrder)
		searchParams.params = params

		ProjectStatus projectStatus = ProjectStatus.valueOfParam(params.isActive)
		projectStatus = (projectStatus!=null)?projectStatus:ProjectStatus.COMPLETED

		def projectList = projectService.getUserProjects(securityService.getUserLogin(), projectHasPermission, projectStatus, searchParams)
		
		def totalRows = projectList?.totalCount
		def numberOfPages = totalRows ? Math.ceil(totalRows / maxRows) : 1

		def results = projectList?.collect { 
			def startDate = ''
			def completionDate = ''
			startDate = it.startDate ? TimeUtil.formatDate(getSession(), it.startDate) : ''
			completionDate = it.completionDate ? TimeUtil.formatDate(getSession(), it.completionDate) : ''
			[ cell: [it.projectCode, it.name, startDate, completionDate,it.comment], id: it.id,]
		}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	/*
	 *  return the details of Project
	 */
	def show() {

		def currProjectInstance = controllerService.getProjectForPage(this)
		if (!currProjectInstance) {
			return
		}

		// load transitions details into application memory.
		//stateEngineService.loadWorkflowTransitionsIntoMap(projectInstance.workflowCode, 'project')

		def loginPerson = securityService.getUserLoginPerson()
		def userCompany = partyRelationshipService.getStaffCompany( loginPerson )

		// Save and load various user preferences
		userPreferenceService.setPreference( "CURR_PROJ", "${currProjectInstance.id}" )
		userPreferenceService.setPreference( "PARTYGROUP", "${userCompany?.id}" )
		userPreferenceService.loadPreferences("CURR_TZ")
		userPreferenceService.loadPreferences("CURR_BUNDLE")
		userPreferenceService.loadPreferences("MOVE_EVENT")

		def currPowerType = session.getAttribute("CURR_POWER_TYPE")?.CURR_POWER_TYPE
		if(!currPowerType){
			userPreferenceService.setPreference( "CURR_POWER_TYPE", "Watts" )
		}

		def imageId
		def projectLogo = ProjectLogo.findByProject(currProjectInstance)
		if (projectLogo) {
			imageId = projectLogo.id
		}
		session.setAttribute("setImage",imageId) 

		def projectLogoForProject = ProjectLogo.findByProject(currProjectInstance)
		List projectPartners = partyRelationshipService.getProjectPartners( currProjectInstance )

		List projectManagers = projectService.getProjectManagersByProject(currProjectInstance.id)?.partyIdTo
		projectManagers.sort { a,b ->
			a.firstName <=> b.firstName ?: a.lastName <=> b.lastName 
		}

		return [ 
			projectInstance: currProjectInstance, 
			projectPartners: projectPartners, 
			projectManagers: projectManagers,
			projectLogoForProject: projectLogoForProject 
		]
	}

	/** 
	 * Used to delet the user's current project
	 */
	def delete() {
		def (project, user) = controllerService.getProjectAndUserForPage(this, 'ProjectDelete')
		if (!project) {
			return
		}

		def userLogin = securityService.getUserLogin()
		log.info "Project $project.name($project.id) is going to be deleted by $userLogin"
		try {
			def message = projectService.deleteProject(project.id, true)

			flash.message = "Project ${project.name} deleted"
			redirect(controller:"projectUtil", params:['message':flash.message])
		} catch (Exception ex) {
			flash.message = ex.getMessage()
			redirect(action:"list")
		}
	}

	def edit() {
		def (projectInstance, user) = controllerService.getProjectAndUserForPage(this, 'ProjectEditView')
		if (!projectInstance) {
			return
		}

		def projectPartners = partyRelationshipService.getProjectPartners( projectInstance )
		PartyGroup company = projectInstance.owner
		def projectDetails
		def moveBundles
		if (projectInstance) {
			projectDetails = projectService.getprojectEditDetails(projectInstance,[:])
			moveBundles = MoveBundle.findAllByProject(projectInstance)

			List projectManagers = projectService.getProjectManagersByProject(projectInstance.id)?.partyIdTo
			projectManagers.sort { a,b ->
				a.firstName <=> b.firstName ?: a.lastName <=> b.lastName 
			}

			return [ 
				company: company,
				projectInstance : projectInstance, 
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
				moveBundles:moveBundles 
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
	def update() {
		Project.withTransaction { status ->

			def (projectInstance, user) = controllerService.getProjectAndUserForPage(this, 'ProjectEditView')
			if (!projectInstance) {
				return
			}

			PartyGroup company = projectInstance.owner
			
			//  When the Start date is initially selected and Completion Date is blank, set completion date to the Start date
			def startDate = params.startDate
			def completionDate = params.completionDate
			if (startDate) {
				params.startDate = TimeUtil.parseDate(getSession(), startDate)
			}
			if (completionDate){
				params.completionDate = TimeUtil.parseDate(getSession(), completionDate)
			}
			params.timezone = retrievetimeZone(params.timezone)

			params.runbookOn = 1
			projectInstance.properties = params

			//Get the Partner Image file from the multi-part request
			def file = request.getFile('partnerImage')
			def image			
			// List of OK mime-types

			// Closure that will create the standard model used for the view
			def makeModel = {
				return [
					company: company,
					projectInstance: projectInstance, 
					projectPartner: projectDetails.projectPartner, 
					projectManager: projectDetails.projectManager, 
					moveManager: projectDetails.moveManager, 
					companyStaff: projectDetails.companyStaff, 
					clientStaff: projectDetails.clientStaff, 
					partnerStaff: projectDetails.partnerStaff, 
					companyPartners: projectDetails.companyPartners, 
					workflowCodes: projectDetails.workflowCodes,
					projectLogoForProject: projectDetails.projectLogoForProject, 
					prevParam:params
				]
			}

			if( file ) {
				def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
				if( file.getContentType() && file.getContentType() != "application/octet-stream"){
					if(params.projectPartner == ""){
						def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
						flash.message = " Please select Associated Partner to upload Image. "
						render( view:'edit', model:makeModel() )
						return
					} else if (! okcontents.contains(file.getContentType())) {
						def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
						flash.message = "Image must be one of: ${okcontents}"
						render( view:'edit', model:makeModel() )
						return
					}
				}
							
				image = ProjectLogo.fromUpload(file) 
				image.project = projectInstance
				def imageSize = image.getSize()
				if ( imageSize > 50000 ) {
					def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
					flash.message = " Image size is too large. Please select proper Image"
					render( view:'edit', model:makeModel() )
					return
				}
				if( file.getContentType() == "application/octet-stream"){
					//Nonthing to perform.
				} else if (params.projectPartner) {
					if(!image.save()){
						def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
						flash.message = " Image Upload Error."
						render( view:'edit', model:makeModel())
						return
					}
				}
			} else {
				image = ProjectLogo.findByProject(projectInstance)
				if(image && !params.projectPartner){
					image.delete(flush:true)
				}
			}
			
			if( !projectInstance.hasErrors() && projectInstance.save() ) {
				
				def partnersIds = params.projectPartners

				projectService.updateProjectPartners(projectInstance, partnersIds)

				// Audit project changes
				auditService.saveUserAudit(UserAuditBuilder.projectConfig(securityService.getUserLogin(), projectInstance))

				flash.message = "Project ${projectInstance} updated"
				redirect(action:"show")
				
			}
		} // Project.withTransaction(t) {
	}

	/*
	 * Populate and present the create view for a new project
	 */
	def create() {
		if (!controllerService.checkPermission(this, 'CreateProject')) 
			return

		Person whom = securityService.getUserLoginPerson()

		def projectInstance = new Project()
		projectInstance.properties = params
		def projectDetails = projectService.getProjectPatnerAndManagerDetails()
		def defaultTimeZone = TimeUtil.defaultTimeZone
		def userTimeZone = userPreferenceService.get(TimeUtil.TIMEZONE_ATTR)
		if (userTimeZone) {
			defaultTimeZone = userTimeZone
		}

		return [ 
			clients:projectDetails.clients, 
			company: whom.company,
			managers:projectDetails.managers, 
			partners:projectDetails.partners, 
			projectInstance:projectInstance, 
			workflowCodes: projectDetails.workflowCodes ]
	}

	/**
	 * Used by the Project Create to actually create the project along with the following other tasks:
	 *    - associate partner companies
	 *    - associate the Project Manager 
	 *    - save a partner logo 
	 *    - create the default 'TBD' bundle
	 * @permission CreateProject
	 */
	def save() {
		Project.withTransaction { status ->
			if (!controllerService.checkPermission(this, 'CreateProject')) {
				return
			}

			UserLogin user = securityService.getUserLogin()
			Person whom = user.person
			PartyGroup company = whom.company

			//projectInstance.dateCreated = new Date()
			def startDate = params.startDate
			def completionDate = params.completionDate   
			if (startDate) {
				params.startDate = TimeUtil.parseDate(getSession(), startDate)
			}
			if (completionDate) {
				params.completionDate = TimeUtil.parseDate(getSession(), completionDate)
			}
			params.runbookOn =  1	// Default to ON
			params.timezone = retrievetimeZone(params.timezone)
			def projectInstance = new Project(params)

			def file = request.getFile('partnerImage')
			def image	  
			// List of OK mime-types
			def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
			if( file && file.getContentType() && file.getContentType() != "application/octet-stream" ){
				if (params.projectPartner == ""){
			   		flash.message = " Please select Associated Partner to upload Image. "
					def projectDetails = projectService.getProjectPatnerAndManagerDetails()
					render( view:'create', model:[ 
						company: company,
						projectInstance:projectInstance, 
						clients:projectDetails.clients, 
						partners:projectDetails.partners,
						managers:projectDetails.managers, 
						workflowCodes: projectDetails.workflowCodes,
						prevParam:params] )
					return;
				} else if (! okcontents.contains(file.getContentType())) {
					flash.message = "Image must be one of: ${okcontents}"
					def projectDetails = projectService.getProjectPatnerAndManagerDetails()
					render( view:'create', model:[
						company: whom.company, 
						projectInstance:projectInstance, 
						clients:projectDetails.clients, 
						partners:projectDetails.partners,
						managers:projectDetails.managers, 
						workflowCodes: projectDetails.workflowCodes,
						prevParam:params] )
					return;
				}		
			}

			// Save Partner Logo Image
			image = ProjectLogo.fromUpload(file)		   
			image.project = projectInstance

			def imageSize = image.getSize()
			if( imageSize > 50000 ) {
				flash.message = " Image size is too large. Please select proper Image"
				def projectDetails = projectService.getProjectPatnerAndManagerDetails()
				render( view:'create', model:[ 
					company: company,
					projectInstance:projectInstance, 
					clients:projectDetails.clients, 
					partners:projectDetails.partners,
					managers:projectDetails.managers, 
					workflowCodes: projectDetails.workflowCodes,
					prevParam:params] )
				return
			}
			if ( !projectInstance.hasErrors() && projectInstance.save() ) {
				def projectCompanyRel = partyRelationshipService.savePartyRelationship("PROJ_COMPANY", projectInstance, "PROJECT", company, "COMPANY" )

				// Save the partners
				def partnersIds = params.projectPartners
				projectService.updateProjectPartners(projectInstance, partnersIds)

				// Deal with the Project Manager if one is supplied
				Long projectManagerId = NumberUtil.toPositiveLong(params.projectManagerId, -1)
				if (projectManagerId > 0) {
					personService.addToProjectTeam(user, "${projectInstance.id}", "${projectManagerId}", "PROJ_MGR")
				}

				// Deal with the image
				if (file && file.getContentType() == "application/octet-stream"){
					//Nonthing to perform.
				} else if(projectManagerId > 0) {
					image.save()
				}
				
				// set the projectInstance as CURR_PROJ
				userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )	

				// Will create a bundle name TBD and set it as default bundle for project   
				projectInstance.getProjectDefaultBundle()
				
				flash.message = "Project ${projectInstance} was created"
				redirect( action:"show",  imageId:image.id )

			} else {
				def projectDetails = projectService.getProjectPatnerAndManagerDetails()

				render( view:'create', model:[ 
					company: company,
					projectInstance:projectInstance, 
					clients:projectDetails.clients, 
					partners:projectDetails.partners,
					managers:projectDetails.managers, 
					workflowCodes: projectDetails.workflowCodes,
					prevParam:params] )

			}
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
	def fetchStaffList() {

		Person whom = securityService.getUserLoginPerson()

		String query = request.JSON.q ? request.JSON.q.toLowerCase() : ''

		def client
		Long clientId = NumberUtil.toPositiveLong(request.JSON.client, -1)
		if (clientId > 0) {
			// Validate that the client is associated with the user's company
			List allClients = partyRelationshipService.getCompanyClients(whom.company)*.partyIdTo
			client = allClients.find { it.id == clientId }
			if (! client ) {
				securityService.reportViolation("attempted to access unassociated client (id $pid)", user)
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
						securityService.reportViolation("attempted to access unassociated partner (id $pid)", user)
					}
				} else {
					log.warn "retrievePartnerStaffList() called with invalid partner id ($p)"
				}
			}
		}

		// Use the passed role or default all staff
		String staffRole = request.JSON.role ?: 'STAFF'
		List<Map> staffList = []
		String staffQuery = "FROM PartyRelationship p \
			WHERE p.partyRelationshipType = 'STAFF' AND p.partyIdFrom = :company \
			AND p.roleTypeCodeFrom.id = 'COMPANY' AND p.roleTypeCodeTo.id = :staffRole"

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
				staffList.add([id:s.id, text: "${s.toString()}, ${company.name}" ])
			}
		}

		getCompanyStaffClosure(whom.getCompany())
		
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
	
	def cancel() {
		redirect(controller:'projectUtil')
	}
	/*
	 * Action to setPreferences
	 */
	def addUserPreference() {
		def selectProject = params.id
		if(selectProject){
			def projectInstance = Project.read(params.id)
			userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )
			def browserTest = request.getHeader("User-Agent").toLowerCase().contains("mobile")
			
			
			if ( browserTest || params.mobileSelect )
				redirect(controller:'task', action:'listUserTasks', params:[viewMode:'mobile'])
			else
				redirect(controller:'project', action:"show", id: params.id )
		} else {
			flash.message = "Please select Project"
			redirect( action:"list" )
		}
			
	}
	
	def showImage() {
			if( params.id ) {
				def projectLogo = ProjectLogo.findById( params.id )
		 		def image = projectLogo?.partnerImage?.binaryStream
		 		response.contentType = 'image/jpg'		
		 		response.outputStream << image
			} else {
				return;
			}
	 }
	
	def deleteImage() {			 
		 	 def projectInstance = Project.get( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
			 def imageInstance = ProjectLogo.findByProject(projectInstance)
			 if(imageInstance){
				 flash.message = "Image deleted"
				 imageInstance.delete(flush:true)
				 redirect(action:'show',id:projectInstance.id )
			 } else {
				 flash.message = "No Image to delete"
				 redirect(action:'show',id:projectInstance.id )
			 }
			 
	}

	/*
	* function to set the user preference powerType
	*/
	def setPower() {
		def power = params.p
		userPreferenceService.setPreference( "CURR_POWER_TYPE", power )
		render power
	}
	
	/**
	 * Action to render the Field Settings (aka Importance) Show/Edit maintenance form for field importance and field tooltips
	 */
	def fieldImportance() {
		def project = securityService.getUserCurrentProject()
		return [project:project, hasEditProjectFieldSettingsPermission:RolePermissions.hasPermission("EditProjectFieldSettings")]
	}
	
	/**
	 * To create json data to for a given entity type
	 *@param : entityType type of entity.
	 *@return : json data
	 */
	def retrieveAssetFields ={
		
		def assetTypes=EntityType.list
		def fieldMap= [:]
		assetTypes.each{type->
			fieldMap << [(type):projectService.getFields(type)]
		}
		fieldMap<< ['customs':projectService.getCustoms()]
		render fieldMap as JSON
	}
	/**
	 * Initialising importance for a given entity type.
	 *@param : entityType type of entity.
	 *@return : json data, example map
	 *{
		AssetEntity:{
			assetName:{phase:{D:C,V:C,R:H,S:I,B:C}},
			assetTag:{phase:{D:N,V:N,R:N,S:N,B:N}},..............
			environment:{phase:{D:N,V:N,R:N,S:N,B:N}}},
		Application:{
			assetName:{phase:{D:N,V:N:N,S:N,B:N}},
			appVendor:{phase:{D:C,V:H,R:I,S:C,B:H}},....
			custom8:{phase:{D:N,V:N,R:N,N,B:N}}},
		Files:{
			assetName:{phase:{D:N,V:N,R:N,S:N,B:N}},
			fileFormat:{phase:{D:N,V:N,N,S:N,B:N}},........
			url:{phase:{D:N,V:N,R:N,S:N,B:N}}},
		Database:{
			assetName:{phase:{D:N,V:N,R:N,S:N,B:N}},
			dbFormat:{phase:{D:N,V:N,R:N,S:N,B:N}},.............
			custom8:{phase:{D:N,V:N,R:N,S:N,B:N}}}
		}
	 */
	def retrieveImportance() {
		def assetTypes=EntityType.list
		def impMap =[:]
		assetTypes.each{type->
			impMap << [(type):projectService.getConfigByEntity(type)]
		}
		render impMap as JSON
	}
	/**
	 * This action is used to render importance for a given entity type.
	 * @param entity type
	 * @return json data
	 */
	def cancelImportance() {
		def entityType = request.JSON.entityType
		def project = securityService.getUserCurrentProject()
		def parseData = projectService.getConfigByEntity(entityType)
		render parseData as JSON
	}
	
	/**
	 *This action is used to update field importance and display it to user
	 *@param : entityType type of entity for which user is requested for importance .
	 *@return success string 
	 */
	def updateFieldImportance() {
		def project = controllerService.getProjectForPage(this, "EditProjectFieldSettings")
		if (! project)
			return
		
		def entityType = request.JSON.entityType
		def allConfig = request.JSON.jsonString as JSON;
		try {
			def assetImp = FieldImportance.find("from FieldImportance where project=:project and entityType=:entityType", [project:project, entityType:entityType])
			if (!assetImp)
				assetImp = new FieldImportance(entityType:entityType, config: allConfig.toString(), project:project)
			else
				assetImp.config = allConfig.toString()
			if (!assetImp.validate() || !assetImp.save(flush: true)) {
				def etext = "updateFieldImportance Unable to create FieldImportance"+GormUtil.allErrorsString( assetImp )
				log.error( etext )
			}
		} catch(Exception ex) {
			log.error ExceptionUtil.messageWithStacktrace("Updating FieldImportance", e)
		}
		
		render "success"
	}
	/**
	 *This action is used to retrive default project field importance and display it to user
	 *@param : entityType type of entity for which user is requested for importance .
	 *@return 
	 */
	def retriveDefaultImportance() {
		def defaultProject = Project.getDefaultProject()
		def entityType = request.JSON.entityType
		def fieldsData = projectService.generateDefaultConfig(entityType)
		def tooltipsData = assetEntityService.retrieveTooltips(entityType, defaultProject)
		render(ServiceResults.success(['fields' : fieldsData, 'tooltips': tooltipsData]) as JSON)
	}
	/**
	 *This action is used to project customFieldsShown
	 *@param : custom count.
	 *@render string 'success'.
	 */
	def updateProjectCustomShown() {
		if(RolePermissions.hasPermission("EditProjectFieldSettings")){
			def project = securityService.getUserCurrentProject()
			project.customFieldsShown = NumberUtils.toInt(request.JSON.customCount,48)
			if(!project.validate() || !project.save(flush:true)){
				def etext = "Project customs unable to Update "+GormUtil.allErrorsString( project )
				log.error( etext )
			}
		}
		render "success"
	}

	/**
	 * Used to select project time zone
	 * @param timezone default timezone selected
	 * @render time zone view
	 */
	def showTimeZoneSelect() {
		def timezone = params.timezone
		if (StringUtil.isBlank(timezone)) {
			timezone = TimeUtil.defaultTimeZone
		}
		def timezones = Timezone.findAll()
		def areas = userPreferenceService.timezonePickerAreas()

		render(template:"showTimeZoneSelect",model:[areas: areas, timezones: timezones, currTimeZone: timezone])
	}

	def showImportanceFields() {
		render( view: "showImportance", model: [])
	}

	def editImportanceFields() {
		render( view: "editImportance", model: [])
	}

	/**
	 * Used to launch the project metrics daily job for testing purposes.
	 */
	def launchProjectDailyMetricsJob() {
		def success = true
		def errorMessage = ""
		if(RolePermissions.hasPermission("ShowProjectDailyMetrics")){
			def params = [:]
			def key = "ProjectDailyMetrics-" + UUID.randomUUID().toString()
			def jobName = "TM-" + key
			
			// Delay 2 seconds to allow this current transaction to commit before firing off the job
			Trigger trigger = new SimpleTriggerImpl(jobName, null, new Date(System.currentTimeMillis() + 2000) )
			trigger.jobDataMap.putAll(params)
			trigger.jobDataMap.put('key', key)
			trigger.setJobName('ProjectDailyMetricsJob')
			trigger.setJobGroup('tdstm-project-daily-metrics')
			quartzScheduler.scheduleJob(trigger)
		} else {
			success = false
			errorMessage = "User don't have permissions to do this action."
		}
		render( view: "projectDailyMetrics", model: [success: success, errorMessage: errorMessage])
	}

	/**
 	 * Renders the form so Admin's can initiate the bulk Account Activation
 	 * Notification process.
	 */
	def userActivationEmailsForm = {
		Project project 
		UserLogin userLogin 
		(project, userLogin) = controllerService.getProjectAndUserForPage(this, 'SendUserActivations')
		if (!project){
			return
		}

		def accounts = projectService.getAccountActivationUsers( project )

		def defaultProject = Project.getDefaultProject()
		def defaultEmail = grailsApplication.config.grails.mail.default.from
		defaultEmail = StringEscapeUtils.escapeHtml(defaultEmail)

		Map model = [ 
			project: project.name, 
			client:project.client.name, 
			accounts:accounts,
			defaultEmail: defaultEmail, 
			adminEmail: userLogin.person.email
		]		
		render view:"userActivationEmailsForm", model:model 
	}


	/**
	 * Sends out an Activation Email to those accounts selected by the admin.
	 */
	def sendAccountActivationEmails = {
		Project project 
		UserLogin userLogin 
		(project, userLogin) = controllerService.getProjectAndUserForPage(this, 'SendUserActivations')
		if (!project){
			return
		}
		String message = null
		def selectedAccounts = params.person
		// Validate the user selected at least an account.
		if(selectedAccounts){
			try{
				def accounts = projectService.getAccountActivationUsers(project)		
				// Find the accounts that could get the notice and filter only those that were checked in the form
				List accountsToNotify = accounts.findAll{ it.personId.toString() in selectedAccounts}
				if (accountsToNotify) {
					String fromEmail = null
					if (params["sendFrom"] == "DEFAULT") {
						fromEmail = grailsApplication.config.grails.mail.default.from
					} else {
						fromEmail = userLogin.person.email
					}
					fromEmail = StringEscapeUtils.escapeHtml(fromEmail)
					projectService.sendBulkActivationNotificationEmail(accountsToNotify, params["customMessage"], fromEmail, request.getRemoteAddr())	
					message = "The Account Activation Notification has been sent out to the users."
				}else{
					message = "No Accounts selected for notification."
				}
			}catch(Exception e){
				message = "There was an error while processing the email notifications. Please contact Support."
				log.error "An error occurred : ${e}"
			}
			
		}else{
			message = "No Accounts selected for notification."
		}
		
		flash.message = message
		return userActivationEmailsForm()
	}

}
