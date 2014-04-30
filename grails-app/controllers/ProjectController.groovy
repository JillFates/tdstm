import java.util.List;
import java.util.Map;

import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder

import com.tds.asset.FieldImportance;

import grails.converters.JSON

import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.web.json.JSONObject

import com.tds.asset.FieldImportance
import com.tdsops.tm.enums.domain.EntityType
import com.tdsops.tm.enums.domain.ValidationType;
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import org.apache.commons.lang.math.NumberUtils

class ProjectController {
	def userPreferenceService
	def partyRelationshipService
	def stateEngineService
	def projectService
	def securityService
	
	def index = { redirect(action:list,params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {
		return [active:params.active?:'active']
	}
	/**
	 * Used to generate the List for projects using jqgrid.
	 * @return : list of projects as JSON
	 */
	def listJson = {
		def sortIndex = params.sidx ?: 'projectCode'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def dueFormatter = new SimpleDateFormat("MM/dd/yyyy")
		
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
			startDate = it.startDate ? dueFormatter.format(TimeUtil.convertInToUserTZ(it.startDate, tzId)) : ''
			completionDate = it.completionDate ? dueFormatter.format(TimeUtil.convertInToUserTZ(it.completionDate, tzId)) : ''
			[ cell: [it.projectCode, it.name, startDate, completionDate,it.comment], id: it.id,]
		}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	/*
	 *  return the details of Project
	 */
	def show = {
		def CURR_PROJ = session.CURR_PROJ?.CURR_PROJ
		if(CURR_PROJ){
			def projectInstance = Project.get( CURR_PROJ )
			if(!projectInstance) {
				flash.message = "Project not found with id ${params.id}"
				redirect( action:list )
			} else { 
				// load transitions details into application memory.
				//stateEngineService.loadWorkflowTransitionsIntoMap(projectInstance.workflowCode, 'project')

				def currProj = session.getAttribute("CURR_PROJ");
				def currProjectInstance = Project.get( currProj.CURR_PROJ )
				def loginPerson = securityService.getUserLoginPerson()
				def userCompany = partyRelationshipService.getStaffCompany( loginPerson )

				// Save and load various user preferences
				userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )
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
				def projectLogoForProject = ProjectLogo.findByProject(projectInstance)
				def projectPartner = projectService.getProjectPartner( projectInstance )
				def projectManager = projectService.getProjectManagerByProject(projectInstance.id)
				def moveManager = projectService.getMoveManagerByProject(projectInstance.id)
	
				return [ projectInstance : projectInstance, projectPartner:projectPartner, 
						 projectManager:projectManager, moveManager:moveManager, 
						 projectLogoForProject:projectLogoForProject ]
			}
		} else {
		flash.message = "Project not found with id ${params.id}"
		redirect(action:list)
		}
	}

	def delete = {
		def projectInstance = Project.get( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		if(projectInstance) {
			try {
				def message = projectService.deleteProject(projectInstance, securityService.getUserLogin())
				projectInstance.delete(flush:true)
				PartyGroup.executeUpdate("delete from Party p where p.id = ${params.id}")
				Party.executeUpdate("delete from Party p where p.id = ${params.id}")
				
				flash.message = "Project ${projectInstance} deleted"
				redirect(controller:"projectUtil", params:['message':flash.message])
			} catch (Exception ex) {
				flash.message = ex.getMessage()
				redirect(action:list)
			}
		} else {
			flash.message = "Project not found with id ${params.id}"
			redirect(action:list)
		}
		
	}

	def edit = {
		def projectInstance = Project.get( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		def projectDetails
		if (!projectInstance) {
			flash.message = "Project not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			projectDetails = projectService.getprojectEditDetails(projectInstance,[:])
		}
		return [ projectInstance : projectInstance, projectPartner: projectDetails.projectPartner, projectManager: projectDetails.projectManager, 
				 moveManager: projectDetails.moveManager, companyStaff: projectDetails.companyStaff, clientStaff: projectDetails.clientStaff, 
				 partnerStaff: projectDetails.partnerStaff, companyPartners: projectDetails.companyPartners,
				 projectLogoForProject: projectDetails.projectLogoForProject, workflowCodes: projectDetails.workflowCodes ]
	}

	/*
	 * Update the Project details
	 */
	def update = {

		// TODO : Security : Need fix update() to check user's permissions

		def projectInstance = Project.get( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		
		if( projectInstance ) {
			//  When the Start date is initially selected and Completion Date is blank, set completion date to the Start date
			def formatter = new SimpleDateFormat("MM/dd/yyyy");
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ

			def startDate = params.startDate
			def completionDate = params.completionDate
			if (startDate) {
				params.startDate =  GormUtil.convertInToGMT(formatter.parse(startDate), tzId)
			}
			if (completionDate){
				params.completionDate =  GormUtil.convertInToGMT(formatter.parse(completionDate), tzId)
			}

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
						render( view:'edit', model:[ projectInstance : projectInstance, projectPartner: projectDetails.projectPartner, projectManager: projectDetails.projectManager, 
													 moveManager: projectDetails.moveManager, companyStaff: projectDetails.companyStaff, clientStaff: projectDetails.clientStaff, 
													 partnerStaff: projectDetails.partnerStaff, companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
													 projectLogoForProject: projectDetails.projectLogoForProject, prevParam:params] )
						return;
					} else if (! okcontents.contains(file.getContentType())) {
						def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
						flash.message = "Image must be one of: ${okcontents}"
						render( view:'edit', model:[ projectInstance : projectInstance, projectPartner: projectDetails.projectPartner, projectManager: projectDetails.projectManager, 
													 moveManager: projectDetails.moveManager, companyStaff: projectDetails.companyStaff, clientStaff: projectDetails.clientStaff, 
													 partnerStaff: projectDetails.partnerStaff, companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
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
				def party
				def partnerImage = params.projectPartner
				if ( partnerImage != null && partnerImage != "" ) {
					party = Party.findById(partnerImage)
				}
				image.party = party
				
				def imageSize = image.getSize()
				if( imageSize > 50000 ) {
					def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
					flash.message = " Image size is too large. Please select proper Image"
					render( view:'edit', model:[ projectInstance : projectInstance, projectPartner: projectDetails.projectPartner, projectManager: projectDetails.projectManager, 
													 moveManager: projectDetails.moveManager, companyStaff: projectDetails.companyStaff, clientStaff: projectDetails.clientStaff, 
													 partnerStaff: projectDetails.partnerStaff, companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
													 projectLogoForProject: projectDetails.projectLogoForProject, prevParam:params] )
					return;
				}
				if(file.getContentType() == "application/octet-stream"){
					//Nonthing to perform.
				} else if(params.projectPartner){
					if(!image.save()){
						def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
						flash.message = " Image Upload Error."
						render( view:'edit', model:[ projectInstance : projectInstance, projectPartner: projectDetails.projectPartner, projectManager: projectDetails.projectManager, 
													 moveManager: projectDetails.moveManager, companyStaff: projectDetails.companyStaff, clientStaff: projectDetails.clientStaff, 
													 partnerStaff: projectDetails.partnerStaff, companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
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
				
				def partnerId = params.projectPartner
				def projectManagerId = params.projectManager
				def moveManagerId = params.moveManager
				//def projectRoleType = RoleType.findById( "PROJECT" ) 
				//def projectStaffRelationshipType = PartyRelationshipType.findById( "PROJ_STAFF" )
				
				//-------------------------------
				// Statements to re-insert Partner
				//-------------------------------
				
				def updateProjectPartnerRel = partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_PARTNER", projectInstance?.id, "PROJECT", partnerId, "PARTNER" )
				/*
				if ( partnerId != "" && partnerId != null ){
					def projectPartner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' and p.partyIdFrom = $projectInstance.id and p.partyIdTo = $partnerId and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PARTNER' ")
					def partnerParty = Party.findById( partnerId )
					def partnerRelationshipType = PartyRelationshipType.findById( "PROJ_PARTNER" )
					def partnerRoleType = RoleType.findById( "PARTNER" )
					// condition to check whether partner has changed or not
					if ( projectPartner == null ) {
						def otherPartner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PARTNER' ")
						if ( otherPartner != null && otherPartner != "" ) {
							//	Delete existing partner and reinsert new partner For Project, if partner changed
							otherPartner.delete()
							def projectPartnerRel = new PartyRelationship( partyRelationshipType:partnerRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:partnerParty, roleTypeCodeTo:partnerRoleType, statusCode:"ENABLED" ).save( insert:true )
						} else {
							// Create Partner if there is no partner for this project
							def projectPartnerRel = new PartyRelationship( partyRelationshipType:partnerRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:partnerParty, roleTypeCodeTo:partnerRoleType, statusCode:"ENABLED" ).save( insert:true )
						}
					}
				} else {
					//	if user select a blank then remove Partner
					def otherPartner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PARTNER' ")
					if ( otherPartner != null && otherPartner != "" ) {
						otherPartner.delete()
					}
				}
				*/
				//---------------------------------------
				// Statements to re-insert ProjectManager
				//---------------------------------------
				
				def updateProjectManagerRel = partyRelationshipService.updatePartyRelationshipPartyIdTo( "PROJ_STAFF", projectInstance?.id, "PROJECT", projectManagerId, "PROJ_MGR" )
				/*
				if ( projectManagerId != "" && projectManagerId != null ) {
					def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.partyIdTo = $projectManagerId and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PROJ_MGR' ")
					def projectManagerParty = Party.findById( projectManagerId )
					def projectManagerRoleType = RoleType.findById( "PROJ_MGR" )
					//	condition to check whether Project Manager has changed or not
					if ( projectManager == null ) {
						def otherprojectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PROJ_MGR' ")
						if ( otherprojectManager != null && otherprojectManager != "" ) {
							//	Delete existing partner and reinsert new partner For Project, if partner changed
							otherprojectManager.delete()
							def projectManagerRel = new PartyRelationship( partyRelationshipType:projectStaffRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:projectManagerParty, roleTypeCodeTo:projectManagerRoleType, statusCode:"ENABLED" ).save( insert:true )
						} else {
							//	Create Project Manager if there is no Project Managet for this project
							def projectManagerRel = new PartyRelationship( partyRelationshipType:projectStaffRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:projectManagerParty, roleTypeCodeTo:projectManagerRoleType, statusCode:"ENABLED" ).save( insert:true )
						}
					}
				} else {
					//	if user select a blank then remove Project Manager
					def otherprojectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PROJ_MGR' ")
					if ( otherprojectManager != null && otherprojectManager != "" ) {
						otherprojectManager.delete()
					}
				}
				*/
				//---------------------------------------
				// Statements to re-insert MoveManager
				//---------------------------------------
				
				def updateMoveManagerRel = partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_STAFF", projectInstance?.id, "PROJECT", moveManagerId, "MOVE_MGR" )
				/*
				if ( moveManagerId != "" && moveManagerId != null ) {
					def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.partyIdTo = $moveManagerId and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'MOVE_MGR' ")
					def moveManagerParty = Party.findById( moveManagerId )
					def moveManagerRoleType = RoleType.findById( "MOVE_MGR" )
					//	condition to check whether Move Manager has changed or not
					
					if ( moveManager == null ) {
						def othermoveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'MOVE_MGR' ")
						if ( othermoveManager != null && othermoveManager != "" ) {
							//	Delete existing partner and reinsert new partner For Move, if partner changed
							othermoveManager.delete()
							def moveManagerRel = new PartyRelationship( partyRelationshipType:projectStaffRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:moveManagerParty, roleTypeCodeTo:moveManagerRoleType, statusCode:"ENABLED" ).save( insert:true )
						} else {
							//	Create Move Manager if there is no Move Managet for this project
							def moveManagerRel = new PartyRelationship( partyRelationshipType:projectStaffRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:moveManagerParty, roleTypeCodeTo:moveManagerRoleType, statusCode:"ENABLED" ).save( insert:true )
						}
					}
				} else {
					// if user select a blank then remove Move Manager 
					def othermoveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'MOVE_MGR' ")
					if ( othermoveManager != null && othermoveManager != "" ) {
						othermoveManager.delete()
					}
				}
				*/
				flash.message = "Project ${projectInstance} updated"
				redirect(action:show)
				
			}
			else {
				flash.message = "Project ${projectInstance} not updated"
				def projectDetails = projectService.getprojectEditDetails(projectInstance,params)
				projectInstance.discard()
				render( view:'edit', model:[ projectInstance : projectInstance, projectPartner: projectDetails.projectPartner, projectManager: projectDetails.projectManager, 
											 moveManager: projectDetails.moveManager, companyStaff: projectDetails.companyStaff, clientStaff: projectDetails.clientStaff, 
											 partnerStaff: projectDetails.partnerStaff, companyPartners: projectDetails.companyPartners, workflowCodes: projectDetails.workflowCodes,
											 projectLogoForProject: projectDetails.projectLogoForProject, prevParam:params] )
			}
		} else {
			flash.message = "Project not found with id ${params.id}"
			redirect( action:list, id:params.id )
		}
	}

	/*
	 * Populate and present the create view for a new project
	 */
	def create = {
		def projectInstance = new Project()
		projectInstance.properties = params
		def projectDetails = projectService.getProjectPatnerAndManagerDetails()

		return [ projectInstance:projectInstance, clients:projectDetails.clients , partners:projectDetails.partners , 
					managers:projectDetails.managers, workflowCodes: projectDetails.workflowCodes ]
	}

	/*
	 * create the project and PartyRelationships for the fields prompted
	 */
	def save = {
		def workflowCodes = []
		//projectInstance.dateCreated = new Date()
		def startDate = params.startDate
		def completionDate = params.completionDate   
		//  When the Start date is initially selected and Completion Date is blank, set completion date to the Start date
		def formatter = new SimpleDateFormat("MM/dd/yyyy");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		if(startDate){
			params.startDate =  GormUtil.convertInToGMT(formatter.parse(startDate), tzId)
		}
		if(completionDate){
			params.completionDate =  GormUtil.convertInToGMT(formatter.parse(completionDate), tzId)
		}
		params.runbookOn =  params.runbookOn ? 1 : 0
		def projectInstance = new Project(params)
		//Get the Partner Image file from the multi-part request
		def file = request.getFile('partnerImage')
		def image	  
		// List of OK mime-types
		def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
		if( file && file.getContentType() && file.getContentType() != "application/octet-stream" ){
			if(params.projectPartner == ""){
		   		flash.message = " Please select Associated Partner to upload Image. "
				def projectDetails = projectService.getProjectPatnerAndManagerDetails()
				render( view:'create', model:[ projectInstance:projectInstance, clients:projectDetails.clients, partners:projectDetails.partners,
											 managers:projectDetails.managers, workflowCodes: projectDetails.workflowCodes ,prevParam:params] )
				return;
			} else if (! okcontents.contains(file.getContentType())) {
				flash.message = "Image must be one of: ${okcontents}"
				def projectDetails = projectService.getProjectPatnerAndManagerDetails()
				render( view:'create', model:[ projectInstance:projectInstance, clients:projectDetails.clients, partners:projectDetails.partners,
											 managers:projectDetails.managers, workflowCodes: projectDetails.workflowCodes ,prevParam:params] )
				return;
			}		
		}
	  //save image
		image = ProjectLogo.fromUpload(file)		   
		image.project = projectInstance
		def party
		def partnerImage = params.projectPartner
		if ( partnerImage != null && partnerImage != "" ) {
			party = Party.findById(partnerImage)
		}
		image.party = party 
		def imageSize = image.getSize()
		if( imageSize > 50000 ) {
			flash.message = " Image size is too large. Please select proper Image"
			def projectDetails = projectService.getProjectPatnerAndManagerDetails()
			render( view:'create', model:[ projectInstance:projectInstance, clients:projectDetails.clients, partners:projectDetails.partners,
											 managers:projectDetails.managers, workflowCodes: projectDetails.workflowCodes ,prevParam:params] )
			return;
		}
		
		if ( !projectInstance.hasErrors() && projectInstance.save() ) {
			if(file && file.getContentType() == "application/octet-stream"){
				//Nonthing to perform.
			} else if(params.projectPartner){
				image.save()
			}
			//def client = params.projectClient
			def partner = params.projectPartner
			def projectManager = params.projectManager
			def moveManager = params.moveManager	  	
			
			def companyParty = PartyGroup.findByName( "TDS" )
			//def companyRelationshipType = PartyRelationshipType.findById( "PROJ_COMPANY" ) 
			//def projectRoleType = RoleType.findById( "PROJECT" ) 
			//def companyRoleType = RoleType.findById( "COMPANY" )
			//def projectStaffRelationshipType = PartyRelationshipType.findById( "PROJ_STAFF" )
			// For Project to Company PartyRelationship
			def projectCompanyRel = partyRelationshipService.savePartyRelationship("PROJ_COMPANY", projectInstance, "PROJECT", companyParty, "COMPANY" )
				//new PartyRelationship( partyRelationshipType:companyRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:companyParty, roleTypeCodeTo:companyRoleType, statusCode:"ENABLED" ).save( insert:true )
			/*
			if ( client != null && client != "" ) {
				
				def clientParty = Party.findById(client)
				def clientRelationshipType = PartyRelationshipType.findById( "PROJ_CLIENT" )
				def clientRoleType = RoleType.findById( "CLIENT" )
				//	For Project to Client PartyRelationship
				def projectClientRel = new PartyRelationship( partyRelationshipType:clientRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:clientParty, roleTypeCodeTo:clientRoleType, statusCode:"ENABLED" ).save( insert:true )
			}
			*/
			if ( partner != null && partner != "" ) {
				
				def partnerParty = Party.findById(partner)
				//def partnerRelationshipType = PartyRelationshipType.findById( "PROJ_PARTNER" )
				//def partnerRoleType = RoleType.findById( "PARTNER" )
				//	For Project to Partner PartyRelationship
				def projectPartnerRel = partyRelationshipService.savePartyRelationship("PROJ_PARTNER", projectInstance, "PROJECT", partnerParty, "PARTNER" )
					//new PartyRelationship( partyRelationshipType:partnerRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:partnerParty, roleTypeCodeTo:partnerRoleType, statusCode:"ENABLED" ).save( insert:true )
			}
			
			if ( projectManager != null && projectManager != "" ) {
				
				def projectManagerParty = Party.findById(projectManager)
				//def projectManagerRoleType = RoleType.findById( "PROJ_MGR" )
				//	For Project to ProjectManager PartyRelationship
				def projectManagerRel = partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectInstance, "PROJECT", projectManagerParty, "PROJ_MGR" )  
					//new PartyRelationship( partyRelationshipType:projectStaffRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:projectManagerParty, roleTypeCodeTo:projectManagerRoleType, statusCode:"ENABLED" ).save( insert:true )
			}
			
			if ( moveManager != null && moveManager != "" ) {
				
				def moveManagerParty = Party.findById(moveManager)
				//def moveManagerRoleType = RoleType.findById( "MOVE_MGR" )
				//	For Project to MoveManager PartyRelationship
				def moveManagerRel = partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectInstance, "PROJECT", moveManagerParty, "MOVE_MGR" )
					//new PartyRelationship( partyRelationshipType:projectStaffRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:moveManagerParty, roleTypeCodeTo:moveManagerRoleType, statusCode:"ENABLED" ).save( insert:true )
			}
			// set the projectInstance as CURR_PROJ
			userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )	   	
			
			flash.message = "Project ${projectInstance} created"
			redirect( action:show,  imageId:image.id )
		} else {
			def projectDetails = projectService.getProjectPatnerAndManagerDetails()
			render( view:'create', model:[ projectInstance:projectInstance, clients:projectDetails.clients, partners:projectDetails.partners,
											 managers:projectDetails.managers, workflowCodes: projectDetails.workflowCodes ,prevParam:params] )
		}
	}
	
	/*
	 *  Action to render partner staff as JSON  
	 */
	def getPartnerStaffList = {
			
		def client = params.client
		def partner = params.partner
		def json = []
		def pStaff = []
		def cStaff = []
		def compStaff = []
		def tdsParty = PartyGroup.findByName( "TDS" ).id
		// get list of all STAFF relationship to Client
		def tdsStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' " )
		tdsStaff.sort{it.partyIdTo.lastName}
		tdsStaff.each{partyRelationship ->
		def fullName = partyRelationship.partyIdTo.lastName ? partyRelationship.partyIdTo.lastName+", "+partyRelationship.partyIdTo.firstName : partyRelationship.partyIdTo.firstName
			def title = partyRelationship.partyIdTo.title ? " - "+partyRelationship.partyIdTo.title : ""
			compStaff <<[id:partyRelationship.partyIdTo.id, name:fullName+title]
		}
		if ( client != "" && client != null ) {
			def clientParty = PartyGroup.findById( client ).id
			// get list of all STAFF relationship to Client
			def clientStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $clientParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' " )
			clientStaff.sort{it.partyIdTo.lastName}
			clientStaff.each{partyRelationship ->
			def fullName = partyRelationship.partyIdTo.lastName ? partyRelationship.partyIdTo.lastName+", "+partyRelationship.partyIdTo.firstName : partyRelationship.partyIdTo.firstName
				def title = partyRelationship.partyIdTo.title ? " - "+partyRelationship.partyIdTo.title : "" 
				cStaff <<[id:partyRelationship.partyIdTo.id, name:fullName+title]
				 
			}
		}
		if ( partner != "" && partner != null ) {
			def partnerParty = PartyGroup.findById( partner ).id
			// get list of all STAFF relationship to Client
			def partnerStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $partnerParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' " )
			partnerStaff.sort{it.partyIdTo.lastName}
			partnerStaff.each{partyRelationship ->
			def fullName = partyRelationship.partyIdTo.lastName ? partyRelationship.partyIdTo.lastName+", "+partyRelationship.partyIdTo.firstName : partyRelationship.partyIdTo.firstName
				def title = partyRelationship.partyIdTo.title ? " - "+partyRelationship.partyIdTo.title : ""
				pStaff <<[id:partyRelationship.partyIdTo.id, name:fullName+title]
				 
			}
		}
		
		json = [ identifier:"id", compStaff:compStaff, clientStaff:cStaff, partnerStaff:pStaff ]
		render json as JSON
	}
	
	def cancel = {
		redirect(controller:'projectUtil')
	}
	/*
	 * Action to setPreferences
	 */
	def addUserPreference = {
		def selectProject = params.id
		if(selectProject){
			def projectInstance = Project.read(params.id)
			userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )
			def browserTest = request.getHeader("User-Agent").toLowerCase().contains("mobile")
			
			
			if ( browserTest || params.mobileSelect )
				redirect(controller:'clientTeams', action:'listTasks', params:[viewMode:'mobile'])
			else
				redirect(controller:'project', action:"show", id: params.id )
		} else {
			flash.message = "Please select Project"
			redirect( action:"list" )
		}
			
	}
	
	def showImage = {
			if( params.id ) {
				def projectLogo = ProjectLogo.findById( params.id )
		 		def image = projectLogo?.partnerImage?.binaryStream
		 		response.contentType = 'image/jpg'		
		 		response.outputStream << image
			} else {
				return;
			}
	 }
	
	def deleteImage = {			 
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
	 * function to set the user preference time zone
	 */
	def setUserTimeZone = {
		def timeZone = params.tz
		userPreferenceService.setPreference( "CURR_TZ", timeZone )
		render timeZone 
	}
	/*
	* function to set the user preference powerType
	*/
	def setPower = {
		def power = params.p
		userPreferenceService.setPreference( "CURR_POWER_TYPE", power )
		render power
	}
	
	/**
	 * Action to render the Field Settings (aka Importance) Show/Edit maintenance form for field importance and field tooltips
	 */
	def fieldImportance = {
		def project = securityService.getUserCurrentProject()
		return [project:project]
	}
	
	/**
	 * To create json data to for a given entity type
	 *@param : entityType type of entity.
	 *@return : json data
	 */
	def getAssetFields ={
		
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
	def getImportance ={
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
	def cancelImportance = {
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
	def updateFieldImportance ={
		def entityType = request.JSON.entityType
		def project = securityService.getUserCurrentProject()
		def allConfig = request.JSON.jsonString as JSON;
		try{
			def assetImp = FieldImportance.find("from FieldImportance where project=:project and entityType=:entityType\
											  ", [project:project, entityType:entityType])
			if(!assetImp)
				assetImp = new FieldImportance(entityType:entityType, config: allConfig.toString(), project:project)
			else{
				assetImp.config = allConfig.toString()
			}
			if(!assetImp.validate() || !assetImp.save()){
				def etext = "updateFieldImportance Unable to create FieldImportance"+GormUtil.allErrorsString( assetImp )
				log.error( etext )
			}
		} catch(Exception ex){
			log.error "An error occurred : ${ex}"
		}
		render "success"
	}
	/**
	 *This action is used to retrive default project field importance and display it to user
	 *@param : entityType type of entity for which user is requested for importance .
	 *@return 
	 */
	def retriveDefaultImportance={
		def entityType = request.JSON.entityType
		def parseData = projectService.generateDefaultConfig(entityType)
		render parseData as JSON
	}
	/**
	 *This action is used to project customFieldsShown
	 *@param : custom count.
	 *@render string 'success'.
	 */
	def updateProjectCustomShown = {
		def project = securityService.getUserCurrentProject()
		project.customFieldsShown = NumberUtils.toInt(request.JSON.customCount,48)
		if(!project.validate() || !project.save(flush:true)){
			def etext = "Project customs unable to Update "+GormUtil.allErrorsString( project )
			log.error( etext )
		}
		render "success"
	}
}
