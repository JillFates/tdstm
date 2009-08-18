import grails.converters.JSON
import java.text.SimpleDateFormat
import org.jsecurity.SecurityUtils
class ProjectController {
    def userPreferenceService
    def partyRelationshipService
    def stateEngineService
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
    		def projectList
    		def partyProjectList
    		def isAdmin = SecurityUtils.getSubject().hasRole("ADMIN")
    		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
    	if(isAdmin){	
        	  projectList = Project.findAll( "from Project as p order by p.dateCreated desc" )
    	}else{
    		
    		def query = "from Project p where p.id in (select pr.partyIdFrom from PartyRelationship pr where pr.partyRelationshipType = 'PROJ_STAFF' and pr.partyIdTo = ${loginUser.person.id} and pr.roleTypeCodeFrom = 'PROJECT' )"
    			projectList = Project.findAll(query)
    	}
        return [ projectInstanceList:projectList ]
    }
    /*
     *  return the details of Project
     */
    def show = {
        def projectInstance = Project.get( params.id )
        if(!projectInstance) {
            flash.message = "Project not found with id ${params.id}"
            redirect( action:list )
        } else { 
        	def currProj = session.getAttribute("CURR_PROJ");
        	def currProjectInstance = Project.get( currProj.CURR_PROJ )
        	def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
    		def userCompany = partyRelationshipService.getSatffCompany( loginUser.person )
    		request.getSession(false).setAttribute("PARTYGROUP",userCompany?.partyIdFrom)
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
        	def partnerStaff
        	def projectCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_COMPANY' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'COMPANY' ")
        	//def projectClient = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_CLIENT' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'CLIENT' ")
        	def projectPartner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PARTNER' ")
        	def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PROJ_MGR' ")
        	def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'MOVE_MGR' ")
        	def companyStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectCompany.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
        	companyStaff.each {
        		if( it.partyIdTo.lastName == null ){
        			it.partyIdTo.lastName = ""
        		}
        	}
        	companyStaff.sort{it.partyIdTo.lastName}
        	def clientStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectInstance.client.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
        		clientStaff.each {
        		if( it.partyIdTo.lastName == null ){
        			it.partyIdTo.lastName = ""
        		}
        	}
        	clientStaff.sort{it.partyIdTo.lastName}
        	def companyPartners = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PARTNERS' and p.partyIdFrom = $projectCompany.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' order by p.partyIdTo" )
        	companyPartners.sort{it.partyIdTo.name}
        	if(projectPartner != null){
        		partnerStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectPartner.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
        	    partnerStaff.each {
        		if( it.partyIdTo.lastName == null ){
        			it.partyIdTo.lastName = ""
        		}
        	}
        		partnerStaff.sort{it.partyIdTo.lastName}
        	}
        	clientStaff.each{staff->
        	}
        	return [ projectInstance : projectInstance, projectPartner:projectPartner, projectManager:projectManager, moveManager:moveManager, companyStaff:companyStaff, clientStaff:clientStaff, partnerStaff:partnerStaff, companyPartners:companyPartners,projectLogoForProject:projectLogoForProject ]
        }
    }

    def delete = {
    	
    	def currProj = session.getAttribute("CURR_PROJ").CURR_PROJ;
        if(currProj != params.id){
        	def projectInstance = Project.get( params.id )
	        if(projectInstance) {
	            projectInstance.delete()
	            flash.message = "Project ${projectInstance} deleted"
	            redirect(action:list)
	        }
	        else {
	            flash.message = "Project not found with id ${params.id}"
	            redirect(action:list)
	        }
        } else {
            flash.message = "Unable to Delete the Current Project"
            redirect(action:list)
        }
    }

    def edit = {
        def projectInstance = Project.get( params.id )

        if(!projectInstance) {
            flash.message = "Project not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ projectInstance : projectInstance ]
        }
    }
    /*
     * Update the Project details
     */
    def update = {
        def projectInstance = Project.get( params.id )
        projectInstance.lastUpdated = new Date()
        
        if( projectInstance ) {
            projectInstance.properties = params
            def startDate = params.startDate
            def completionDate = params.completionDate
            //  When the Start date is initially selected and Completion Date is blank, set completion date to the Start date
            if ( startDate != "" && completionDate == "" ) {
                def formatter = new SimpleDateFormat("MM/dd/yyyy");
                projectInstance.completionDate = formatter.parse(startDate);
            }
           //Get the Partner Image file from the multi-part request
            def file = request.getFile('partnerImage')
            def image            
            // List of OK mime-types
            if( file ) {
	            def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
				if(file.getContentType() != "application/octet-stream"){
					if(params.projectPartner == ""){
		           		flash.message = " Please select Associated Partner to upload Image. "
				        redirect(action:'show',id:projectInstance.id )
				        return;
		           	} else if (! okcontents.contains(file.getContentType())) {
		        		flash.message = "Image must be one of: ${okcontents}"
		        		redirect(action:'show',id:projectInstance.id )
		        		return;
		        	}
	        	}
	            
	          //save image
	            /*def imageInstance = ProjectLogo.findByProject(projectInstance)
	            
	            if(imageInstance){
	            	imageInstance.delete()
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
	            	flash.message = " Image size is too large. Please select proper Image"
	            	redirect(action:'show',id:projectInstance.id )
	    	    	return;
	            }
	            if(file.getContentType() == "application/octet-stream"){
	            	//Nonthing to perform.
	            } else if(params.projectPartner){
               		if(!image.save()){
               			flash.message = " Image Upload Error."
               			redirect(action:'show',id:projectInstance.id )
               			return;
               		}
	            }
            }else {
            	image = ProjectLogo.findByProject(projectInstance)
            	if(!params.projectPartner){
               	 image.delete()
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
                redirect(action:show,id:projectInstance.id)
                
            }
            else {
            	flash.message = "Project ${projectInstance} not updated"
                redirect(action:list )
            }
        } else {
            flash.message = "Project not found with id ${params.id}"
            redirect( action:list, id:params.id )
        }
    }
    /*
     * Populate create view
     */
    def create = {
    	def workflowCodes = []
        def projectInstance = new Project()
        projectInstance.properties = params
        /*
        def relationshipTypeClient = PartyRelationshipType.findById( 'PROJ_CLIENT' )
        def relationshipTypePartner = PartyRelationshipType.findById( 'PROJ_PARTNER' )
        def relationshipTypeStaff = PartyRelationshipType.findById( 'PROJ_STAFF' )
         */
        try {
	        def tdsParty = PartyGroup.findByName( 'TDS' )
	        
	        /*def roleTypeCompany = RoleType.findById( 'COMPANY' )
	        def roleTypeClient = RoleType.findById( 'CLIENT' )
	        def roleTypePartner = RoleType.findById( 'PARTNER' )
	        def roleTypeStaff = RoleType.findById( 'STAFF' )
	         */
	        // 	Populate a SELECT listbox with a list of all CLIENTS relationship to COMPANY (TDS)
	        def clients = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'CLIENTS' and p.partyIdFrom = $tdsParty.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'CLIENT' order by p.partyIdTo " ) 
	        clients.sort{it.partyIdTo.name}
	        //	Populate a SELECT listbox with a list of all PARTNERS relationship to COMPANY (TDS)
	        //def partners = PartyRelationship.findAllWhere( partyRelationshipType: relationshipTypePartner, partyIdFrom: tdsParty, roleTypeCodeFrom: roleTypeCompany, roleTypeCodeTo: roleTypePartner )
	        def partners = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PARTNERS' and p.partyIdFrom = $tdsParty.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' order by p.partyIdTo" )
	        partners.sort{it.partyIdTo.name}
	        //	Populate a SELECT listbox with a list of all STAFF relationship to COMPANY (TDS)
	        //def managers = PartyRelationship.findAllWhere( partyRelationshipType: relationshipTypeStaff, partyIdFrom: tdsParty, roleTypeCodeFrom: roleTypeCompany, roleTypeCodeTo: roleTypeStaff )
	        def managers = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $tdsParty.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
	        managers.sort{it.partyIdTo.lastName}
	        workflowCodes = stateEngineService.getWorkflowCode()
	        return [ 'projectInstance':projectInstance, 'clients':clients , 'partners':partners , 'managers':managers, 'workflowCodes': workflowCodes ]
        
        } catch (Exception e) {
        	flash.message = "Company not found"
            redirect(action:list)
        }
    }
    /*
     * create the project and PartyRelationships for the fields prompted
     */
    def save = {
    	def workflowCodes = []
        def projectInstance = new Project(params)
        projectInstance.dateCreated = new Date()
        def startDate = params.startDate
        def completionDate = params.completionDate   

        //  When the Start date is initially selected and Completion Date is blank, set completion date to the Start date
        if ( startDate != "" && completionDate == "" ) {
            def formatter = new SimpleDateFormat("MM/dd/yyyy");
            projectInstance.completionDate = formatter.parse(startDate);
        }
        
        //Get the Partner Image file from the multi-part request
        def file = request.getFile('partnerImage')
        def image      
        // List of OK mime-types
        def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
        if(file.getContentType() != "application/octet-stream"){
        	if(params.projectPartner == ""){
           		flash.message = " Please select Associated Partner to upload Image. "
		        redirect(action:'create' )
		        return;
        	} else if (! okcontents.contains(file.getContentType())) {
	    		flash.message = "Image must be one of: ${okcontents}"
	    		redirect(action:'create')
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
	    	redirect(action:'create')
	    	return;
        }       

        if ( !projectInstance.hasErrors() && projectInstance.save() ) {
        	if(file.getContentType() == "application/octet-stream"){
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
        	
        	flash.message = "Project ${projectInstance.id} created"
            redirect( action:show, id:projectInstance.id, imageId:image.id )
        } else {
            def tdsParty = PartyGroup.findByName( 'TDS' ).id
             
            //	Populate a SELECT listbox with a list of all CLIENTS relationship to COMPANY (TDS)
            def clients = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'CLIENTS' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'CLIENT' order by p.partyIdTo" )
             
            //	Populate a SELECT listbox with a list of all PARTNERS relationship to COMPANY (TDS)
            def partners = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PARTNERS' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' order by p.partyIdTo" )
            
            //	Populate a SELECT listbox with a list of all STAFF relationship to COMPANY (TDS)
            def managers = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
            
            workflowCodes = stateEngineService.getWorkflowCode()
             
            render( view:'create', model:[ projectInstance:projectInstance, clients:clients, partners:partners, managers:managers, workflowCodes: workflowCodes ] )
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
    		
        def projectInstance = Project.findByProjectCode(params.selectProject)
    		
        userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )

        redirect(controller:'project', action:"show", id: projectInstance.id )
    		
    }
    
    def showImage = {
    		if( params.id ) {
	    		def projectLogo = ProjectLogo.findById( params.id )
	     		def image = projectLogo.partnerImage?.binaryStream
	     		response.outputStream << image
    		} else {
    			return;
    		}
     }
    
    def deleteImage = {    		 
         	 def projectInstance = Project.get( params.id )
    		 def imageInstance = ProjectLogo.findByProject(projectInstance)
    		 if(imageInstance){
    			 flash.message = "Image deleted"
    			 imageInstance.delete()
    			 redirect(action:'show',id:projectInstance.id )
    		 } else {
    			 flash.message = "No Image to delete"
    			 redirect(action:'show',id:projectInstance.id )
    		 }
    		 
     }

}
