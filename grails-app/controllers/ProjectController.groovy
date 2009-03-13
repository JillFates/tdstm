import grails.converters.JSON
import java.text.SimpleDateFormat
class ProjectController {
    def userPreferenceService
    def partyRelationshipService
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        
        def query = "from Project as p order by p.dateCreated desc"
        def projectList = Project.findAll( query )
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
        	def partnerStaff
        	def projectCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_COMPANY' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'COMPANY' ")
        	//def projectClient = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_CLIENT' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'CLIENT' ")
        	def projectPartner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PARTNER' ")
        	def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PROJ_MGR' ")
        	def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'MOVE_MGR' ")
        	def companyStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectCompany.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
        	def companyPartners = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PARTNERS' and p.partyIdFrom = $projectCompany.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' order by p.partyIdTo" )
        	if(projectPartner != null){
        		partnerStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $projectPartner.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
        	}
        	return [ projectInstance : projectInstance, projectPartner:projectPartner, projectManager:projectManager, moveManager:moveManager, companyStaff:companyStaff, partnerStaff:partnerStaff, companyPartners:companyPartners ]
        }
    }

    def delete = {
        def projectInstance = Project.get( params.id )
        if(projectInstance) {
            projectInstance.delete()
            flash.message = "Project ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Project not found with id ${params.id}"
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
            	flash.message = "Project ${params.id} updated"
                redirect(action:show,id:projectInstance.id)
                
            }
            else {
                render( view:'edit',model:[projectInstance:projectInstance] )
            }
        } else {
            flash.message = "Project not found with id ${params.id}"
            redirect( action:edit, id:params.id )
        }
    }
    /*
     * Populate create view
     */
    def create = {
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
	        def clients = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'CLIENTS' and p.partyIdFrom = $tdsParty.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'CLIENT' order by p.partyIdTo" ) 
	        
	        //	Populate a SELECT listbox with a list of all PARTNERS relationship to COMPANY (TDS)
	        //def partners = PartyRelationship.findAllWhere( partyRelationshipType: relationshipTypePartner, partyIdFrom: tdsParty, roleTypeCodeFrom: roleTypeCompany, roleTypeCodeTo: roleTypePartner )
	        def partners = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PARTNERS' and p.partyIdFrom = $tdsParty.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' order by p.partyIdTo" )
	       
	        //	Populate a SELECT listbox with a list of all STAFF relationship to COMPANY (TDS)
	        //def managers = PartyRelationship.findAllWhere( partyRelationshipType: relationshipTypeStaff, partyIdFrom: tdsParty, roleTypeCodeFrom: roleTypeCompany, roleTypeCodeTo: roleTypeStaff )
	        def managers = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $tdsParty.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
	        
	        return [ 'projectInstance':projectInstance, 'clients':clients , 'partners':partners , 'managers':managers ]
        
        } catch (Exception e) {
        	flash.message = "Company not found"
            redirect(action:list)
        }
    }
    /*
     * create the project and PartyRelationships for the fields prompted
     */
    def save = {
        def projectInstance = new Project(params)
        projectInstance.dateCreated = new Date()
        def startDate = params.startDate
        def completionDate = params.completionDate
        //  When the Start date is initially selected and Completion Date is blank, set completion date to the Start date
        if ( startDate != "" && completionDate == "" ) {
            def formatter = new SimpleDateFormat("MM/dd/yyyy");
            projectInstance.completionDate = formatter.parse(startDate);
        }
        if ( !projectInstance.hasErrors() && projectInstance.save() ) {
        	
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
            redirect( action:show, id:projectInstance.id )
        } else {
            def tdsParty = PartyGroup.findByName( 'TDS' ).id
             
            //	Populate a SELECT listbox with a list of all CLIENTS relationship to COMPANY (TDS)
            def clients = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'CLIENTS' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'CLIENT' order by p.partyIdTo" )
             
            //	Populate a SELECT listbox with a list of all PARTNERS relationship to COMPANY (TDS)
            def partners = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PARTNERS' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' order by p.partyIdTo" )
            
            //	Populate a SELECT listbox with a list of all STAFF relationship to COMPANY (TDS)
            def managers = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
             
            render( view:'create', model:[ projectInstance:projectInstance, clients:clients, partners:partners, managers:managers ] )
        }
    }
    
    /*
     *  Action to render partner staff as JSON  
     */
    def getPartnerStaffList = {
    		
        def partner = params.partner
        def partnerManagers
        def items = []
        def json = []
        def partnersMap = new HashMap()
        if ( partner != "" && partner != null ) {
            def partnerParty = PartyGroup.findById( partner ).id
            // get list of all STAFF relationship to PARTNER
            partnerManagers = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $partnerParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' " )
	    		
            partnerManagers.each{PartyRelationship ->
                items <<[id:PartyRelationship.partyIdTo.id, name:PartyRelationship.partyIdTo.lastName +", "+PartyRelationship.partyIdTo.firstName+" - "+PartyRelationship.partyIdTo.title]
	             
            }
            json = [ identifier:"id", items:items ]
            
        }
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
}
