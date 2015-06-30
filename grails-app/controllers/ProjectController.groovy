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
import com.tdsops.tm.enums.domain.ValidationType;
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.StringUtil
import org.apache.commons.lang.math.NumberUtils
import com.tdsops.common.lang.ExceptionUtil

class ProjectController {
	def userPreferenceService
	def partyRelationshipService
	def stateEngineService
	def projectService
	def securityService
	def controllerService

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
		if (currProjectInstance) {
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
			def projectLogo
			if(currProjectInstance){
				projectLogo = ProjectLogo.findByProject(currProjectInstance)
			}
			def imageId
			if(projectLogo){
				imageId = projectLogo.id
			}
			session.setAttribute("setImage",imageId) 
			def projectLogoForProject = ProjectLogo.findByProject(currProjectInstance)
			def projectPartners = partyRelationshipService.getProjectPartners( currProjectInstance )
			def projectManagers = projectService.getProjectManagersByProject(currProjectInstance.id)

			return [ projectInstance : currProjectInstance, projectPartners:projectPartners, 
					 projectManagers:projectManagers,
					 projectLogoForProject:projectLogoForProject ]
		}
	}

	def delete() {
		def project = controllerService.getProjectForPage(this, "ProjectDelete")
		if (project) {
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
		} else {
			flash.message = "Project not found with id ${params.id}"
			redirect(action:"list")
		}
	}

	def edit() {
		def projectInstance = controllerService.getProjectForPage(this, "ProjectEditView")
		def projectDetails
		def moveBundles
		if (projectInstance) {
			projectDetails = projectService.getprojectEditDetails(projectInstance,[:])
			moveBundles = MoveBundle.findAllByProject(projectInstance)
			return [ projectInstance : projectInstance, projectPartners: projectDetails.projectPartners, projectManagers: projectDetails.projectManagers, 
				 companyPartners: projectDetails.companyPartners,
				 projectLogoForProject: projectDetails.projectLogoForProject, workflowCodes: projectDetails.workflowCodes, moveBundles:moveBundles ]
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
		def projectInstance = controllerService.getProjectForPage(this, "ProjectEditView")
		
		if( projectInstance ) {
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

			params.runbookOn =  params.runbookOn ? 1 : 0
			projectInstance.properties = params

			//Get the Partner Image file from the multi-part request
			def file = request.getFile('partnerImage')
			def image			
			// List of OK mime-types
			if( file ) {
				def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
				if( file.getContentType() && file.getContentType() != "application/octet-stream"){
					if(params.projectPartner == ""){
						def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
						flash.message = " Please select Associated Partner to upload Image. "
						render( view:'edit', model:[ projectInstance : projectInstance, projectPartners: projectDetails.projectPartners, projectManagers: projectDetails.projectManagers, 
													 companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
													 projectLogoForProject: projectDetails.projectLogoForProject, prevParam:params] )
						return;
					} else if (! okcontents.contains(file.getContentType())) {
						def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
						flash.message = "Image must be one of: ${okcontents}"
						render( view:'edit', model:[ projectInstance : projectInstance, projectPartners: projectDetails.projectPartners, projectManagers: projectDetails.projectManagers, 
													 companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
													 projectLogoForProject: projectDetails.projectLogoForProject, prevParam:params] )
						return;
					}
				}
				
				//save image
				/*def imageInstance = ProjectLogo.findByProject(projectInstance)
				
				if(imageInstance){
					imageInstance.delete(flush:true)
				}*/
				
				image = ProjectLogo.fromUpload(file)		   
				image.project = projectInstance
				def imageSize = image.getSize()
				if( imageSize > 50000 ) {
					def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
					flash.message = " Image size is too large. Please select proper Image"
					render( view:'edit', model:[ projectInstance : projectInstance, projectPartners: projectDetails.projectPartners, projectManagers: projectDetails.projectManagers, 
													 companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
													 projectLogoForProject: projectDetails.projectLogoForProject, prevParam:params] )
					return;
				}
				if(file.getContentType() == "application/octet-stream"){
					//Nonthing to perform.
				} else if(params.projectPartner){
					if(!image.save()){
						def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
						flash.message = " Image Upload Error."
						render( view:'edit', model:[ projectInstance : projectInstance, projectPartners: projectDetails.projectPartners, projectManagers: projectDetails.projectManagers, 
													 companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
													 projectLogoForProject: projectDetails.projectLogoForProject, prevParam:params] )
						return;
					}
				}
			}else {
				image = ProjectLogo.findByProject(projectInstance)
				if(image && !params.projectPartner){
					image.delete(flush:true)
				}
			}
			
			if( !projectInstance.hasErrors() && projectInstance.save() ) {
				
				def partnersIds
				
				//-------------------------------
				// Statements to re-insert Partner
				//-------------------------------
				if (params.projectPartners instanceof String) {
					partnersIds = [params.projectPartners]
				} else {
					partnersIds = params.projectPartners
				}

				projectService.updateProjectPartners(projectInstance, partnersIds)

				flash.message = "Project ${projectInstance} updated"
				redirect(action:"show")
				
			}
			else {
				flash.message = "Project ${projectInstance} not updated"
				def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
				def moveBundles = MoveBundle.findAllByProject(projectInstance)
				projectInstance.discard()
				render( view:'edit', model:[ projectInstance : projectInstance, projectPartners: projectDetails.projectPartners, projectManagers: projectDetails.projectManagers, 
											 companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
											 projectLogoForProject: projectDetails.projectLogoForProject, prevParam:params, moveBundles:moveBundles] )
			}
		}
	}

	/*
	 * Populate and present the create view for a new project
	 */
	def create() {
		if (!controllerService.checkPermission(this, 'CreateProject')) 
			return

		def projectInstance = new Project()
		projectInstance.properties = params
		def projectDetails = projectService.getProjectPatnerAndManagerDetails()
		def defaultTimeZone = TimeUtil.defaultTimeZone
		def userTimeZone = userPreferenceService.get(TimeUtil.TIMEZONE_ATTR)
		if (userTimeZone) {
			defaultTimeZone = userTimeZone
		}

		return [ projectInstance:projectInstance, clients:projectDetails.clients , partners:projectDetails.partners , 
					workflowCodes: projectDetails.workflowCodes, defaultTimeZone: defaultTimeZone ]
	}

	/*
	 * create the project and PartyRelationships for the fields prompted
	 */
	def save() {
		if (!controllerService.checkPermission(this, 'CreateProject')) 
			return

		//projectInstance.dateCreated = new Date()
		def startDate = params.startDate
		def completionDate = params.completionDate   
		if(startDate){
			params.startDate = TimeUtil.parseDate(getSession(), startDate)
		}
		if(completionDate){
			params.completionDate = TimeUtil.parseDate(getSession(), completionDate)
		}
		params.runbookOn =  params.runbookOn ? 1 : 0
		params.timezone = retrievetimeZone(params.timezone)
		def projectInstance = new Project(params)

		def file = request.getFile('partnerImage')

		def result = projectService.saveProject(projectInstance, file, params.projectPartners, params.projectManagerId)
		
		flash.message = result.message
		if (result.success) {
			redirect( action:"show",  imageId: result.imageId )
		} else {
			def projectDetails = projectService.getProjectPatnerAndManagerDetails()
			render( view:'create', model:[ projectInstance:projectInstance, clients:projectDetails.clients, partners:projectDetails.partners,
						 workflowCodes: projectDetails.workflowCodes ,prevParam:params] )
		}
	}

	/*
	 *  Action to render partner staff as JSON  
	 */
	def retrievePartnerStaffList() {

		def q = params.q
		def partners = params["partners[]"]
		def filterByName = (q && (q != ""))
		def client = params.client
		def partner = params.partner
		def role = params.role
		def json = []
		def pStaff = []
		def cStaff = []
		def compStaff = []
		def tdsParty = PartyGroup.findByName( "TDS" ).id

		if (filterByName) {
			q = q.toUpperCase()
		}

		if (StringUtil.isBlank(role)) {
			role = "STAFF"
		}

		// get list of all STAFF relationship to Client
		def tdsStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = '$role' " )
		tdsStaff.sort{it.partyIdTo.lastName}
		tdsStaff.each{partyRelationship ->
			def fullName = partyRelationship.partyIdTo.lastName ? partyRelationship.partyIdTo.lastName+", "+partyRelationship.partyIdTo.firstName : partyRelationship.partyIdTo.firstName
			def title = partyRelationship.partyIdTo.title ? " - "+partyRelationship.partyIdTo.title : ""
			if (filterByName) {
				if (fullName.toUpperCase().indexOf(q) != -1) {
					compStaff <<[id:partyRelationship.partyIdTo.id, text:fullName+title]	
				}
			} else {
				compStaff <<[id:partyRelationship.partyIdTo.id, text:fullName+title]
			}
		}
		json << [text: "TDS", children: compStaff]

		if ( client && client != "") {
			def clientParty = PartyGroup.findById( client )
			// get list of all STAFF relationship to Client
			def clientStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $clientParty.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = '$role' " )
			clientStaff.sort{it.partyIdTo.lastName}
			clientStaff.each{partyRelationship ->
				def fullName = partyRelationship.partyIdTo.lastName ? partyRelationship.partyIdTo.lastName+", "+partyRelationship.partyIdTo.firstName : partyRelationship.partyIdTo.firstName
				def title = partyRelationship.partyIdTo.title ? " - "+partyRelationship.partyIdTo.title : "" 
				if (filterByName) {
					if (fullName.toUpperCase().indexOf(q) != -1) {
						cStaff <<[id:partyRelationship.partyIdTo.id, text:fullName+title]
					}
				} else {
					cStaff <<[id:partyRelationship.partyIdTo.id, text:fullName+title]
				}
			}
			json << [text: clientParty.name, children: cStaff]
		}
		if ( partners && partners.size() > 0 ) {
			def partnersIds
			if (partners instanceof String) {
				partnersIds = partners
			} else {
				partnersIds = partners.join(",")
			}
			// get list of all STAFF relationship to Client
			def partnerStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom in ($partnersIds) and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = '$role' " )
			partnerStaff.sort{it.partyIdTo.lastName}
			partnerStaff.each{partyRelationship ->
				def fullName = partyRelationship.partyIdTo.lastName ? partyRelationship.partyIdTo.lastName+", "+partyRelationship.partyIdTo.firstName : partyRelationship.partyIdTo.firstName
				def title = partyRelationship.partyIdTo.title ? " - "+partyRelationship.partyIdTo.title : ""
				if (filterByName) {
					if (fullName.toUpperCase().indexOf(q) != -1) {
						pStaff <<[id:partyRelationship.partyIdTo.id, text:fullName+title]
					}
				} else {
					pStaff <<[id:partyRelationship.partyIdTo.id, text:fullName+title]
				}
			}
			json << [text: "Partners Staff", children: pStaff]
		}

		render ([results: json] as JSON)
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
		return [project:project]
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
		def entityType = request.JSON.entityType
		def parseData = projectService.generateDefaultConfig(entityType)
		render parseData as JSON
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

}
