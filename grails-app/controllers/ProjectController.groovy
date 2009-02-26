import grails.converters.JSON
class ProjectController {
    def userPreferenceService
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ projectInstanceList: Project.list( params ) ]
    }
    /*
     *  return the details of Project
     */
    def show = {
        def projectInstance = Project.get( params.id )
        if(!projectInstance) {
            flash.message = "Project not found with id ${params.id}"
            redirect(action:list)
        } else { 
        	//def projectCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_COMPANY' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'COMPANY' ")
        	def projectClient = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_CLIENT' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'CLIENT' ")
        	def projectPartner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PARTNER' ")
        	def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PROJ_MGR' ")
        	def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectInstance.id and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'MOVE_MGR' ")
        	/*
        	def companyStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectCompany.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
        	def partnerStaff = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectPartner.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' " )
        	*/
        	return [ projectInstance : projectInstance, projectClient:projectClient, projectPartner:projectPartner, projectManager:projectManager, moveManager:moveManager, companyStaff:companyStaff, partnerStaff:partnerStaff ]
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
        if( projectInstance ) {
            projectInstance.properties = params
            if( !projectInstance.hasErrors() && projectInstance.save() ) {
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
        def tdsParty = PartyGroup.findByName( 'TDS' ).id
        
        /*def roleTypeCompany = RoleType.findById( 'COMPANY' )
        def roleTypeClient = RoleType.findById( 'CLIENT' )
        def roleTypePartner = RoleType.findById( 'PARTNER' )
        def roleTypeStaff = RoleType.findById( 'STAFF' )
         */
        // 	Populate a SELECT listbox with a list of all CLIENTS relationship to COMPANY (TDS)
        def clients = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PROJ_CLIENT' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'CLIENT' order by p.partyIdTo" ) 
        
        //	Populate a SELECT listbox with a list of all PARTNERS relationship to COMPANY (TDS)
        //def partners = PartyRelationship.findAllWhere( partyRelationshipType: relationshipTypePartner, partyIdFrom: tdsParty, roleTypeCodeFrom: roleTypeCompany, roleTypeCodeTo: roleTypePartner )
        def partners = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' order by p.partyIdTo" )
       
        //	Populate a SELECT listbox with a list of all STAFF relationship to COMPANY (TDS)
        //def managers = PartyRelationship.findAllWhere( partyRelationshipType: relationshipTypeStaff, partyIdFrom: tdsParty, roleTypeCodeFrom: roleTypeCompany, roleTypeCodeTo: roleTypeStaff )
        def managers = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
        
        return [ 'projectInstance':projectInstance, 'clients':clients , 'partners':partners , 'managers':managers ]
    }
    /*
     * create the project and PartyRelationships for the fields prompted
     */
    def save = {
        def projectInstance = new Project(params)
        if ( !projectInstance.hasErrors() && projectInstance.save() ) {
        	
        	def client = params.projectClient
        	def partner = params.projectPartner
        	def projectManager = params.projectManager
        	def moveManager = params.moveManager
        	
        	def companyParty = PartyGroup.findByName( "TDS" )
        	def companyRelationshipType = PartyRelationshipType.findById( "PROJ_COMPANY" ) 
        	def projectRoleType = RoleType.findById( "PROJECT" ) 
        	def companyRoleType = RoleType.findById( "COMPANY" )
        	def projectStaffRelationshipType = PartyRelationshipType.findById( "PROJ_STAFF" )
        	// For Project to Company PartyRelationship
        	def projectCompanyRel = new PartyRelationship( partyRelationshipType:companyRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:companyParty, roleTypeCodeTo:companyRoleType, statusCode:"ENABLED" ).save( insert:true )
        	
        	if ( client != null && client != "" ) {
        		
        		def clientParty = Party.findById(client)
        		def clientRelationshipType = PartyRelationshipType.findById( "PROJ_CLIENT" )
        		def clientRoleType = RoleType.findById( "CLIENT" )
        		//	For Project to Client PartyRelationship
        		def projectClientRel = new PartyRelationship( partyRelationshipType:clientRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:clientParty, roleTypeCodeTo:clientRoleType, statusCode:"ENABLED" ).save( insert:true )
        	}
        	
        	if ( partner != null && partner != "" ) {
        		
        		def partnerParty = Party.findById(partner)
        		def partnerRelationshipType = PartyRelationshipType.findById( "PROJ_PARTNER" )
        		def partnerRoleType = RoleType.findById( "PARTNER" )
        		//	For Project to Partner PartyRelationship
        		def projectClientRel = new PartyRelationship( partyRelationshipType:partnerRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:partnerParty, roleTypeCodeTo:partnerRoleType, statusCode:"ENABLED" ).save( insert:true )
        	}
        	
        	if ( projectManager != null && projectManager != "" ) {
        		
        		def projectManagerParty = Party.findById(projectManager)
        		def projectManagerRoleType = RoleType.findById( "PROJ_MGR" )
        		//	For Project to ProjectManager PartyRelationship
        		def projectClientRel = new PartyRelationship( partyRelationshipType:projectStaffRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:projectManagerParty, roleTypeCodeTo:projectManagerRoleType, statusCode:"ENABLED" ).save( insert:true )
        	}
        	
        	if ( moveManager != null && moveManager != "" ) {
        		
        		def moveManagerParty = Party.findById(moveManager)
        		def moveManagerRoleType = RoleType.findById( "MOVE_MGR" )
        		//	For Project to MoveManager PartyRelationship
        		def projectClientRel = new PartyRelationship( partyRelationshipType:projectStaffRelationshipType, partyIdFrom:projectInstance, roleTypeCodeFrom:projectRoleType, partyIdTo:moveManagerParty, roleTypeCodeTo:moveManagerRoleType, statusCode:"ENABLED" ).save( insert:true )
        	}
        	// set the projectInstance as CURR_PROJ
        	userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )
        	
        	flash.message = "Project ${projectInstance.id} created"
            redirect( action:show, id:projectInstance.id )
        }
        else {
            def tdsParty = PartyGroup.findByName( 'TDS' ).id
             
            //	Populate a SELECT listbox with a list of all CLIENTS relationship to COMPANY (TDS)
            def clients = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_CLIENT' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'CLIENT' order by p.partyIdTo" )
             
            //	Populate a SELECT listbox with a list of all PARTNERS relationship to COMPANY (TDS)
            def partners = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER' order by p.partyIdTo" )
            
            //	Populate a SELECT listbox with a list of all STAFF relationship to COMPANY (TDS)
            def managers = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $tdsParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' order by p.partyIdTo" )
             
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
        def partnersMap = new HashMap()
        if ( partner != "" && partner != null ) {
            def partnerParty = PartyGroup.findById( partner ).id
            // get list of all STAFF relationship to PARTNER
            partnerManagers = PartyRelationship.findAll( "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $partnerParty and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' " )
	    		
            partnerManagers.each{PartyRelationship ->
                items <<[id:PartyRelationship.partyIdTo.id, name:PartyRelationship.partyIdTo.lastName +", "+PartyRelationship.partyIdTo.firstName+" - "+PartyRelationship.partyIdTo.title]
	             
            }
            def json=[ identifier:"id", items:items ]
    		println"json------------------------>"+json
            render json as JSON
        }
    }
}
