import com.tds.asset.Application;

import grails.converters.JSON 
class ApplicationController {
	def partyRelationshipService
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    //Get the List of Applications Corresponding to Selected Company
    def list = {
        def companyId = params.id
        def applicationInstanceList
        if ( companyId!= null && companyId != "" ) {
            	
            def query = "from Application a where a.id in (select a.id from Application a where  a.owner = $companyId ) "
            applicationInstanceList = Application.findAll( query )
        } else {
        	flash.message = "Please select Company before navigating to Application"
        	redirect(controller:'partyGroup',action:'list')
        }
        def applicationInstance = new Application()
        applicationInstance.properties = params
        return [ applicationInstanceList: applicationInstanceList, partyId:companyId, 'applicationInstance':applicationInstance ]
    }

    def show = {
        def applicationInstance = Application.get( params.id )

        if(!applicationInstance) {
            flash.message = "Application not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ applicationInstance : applicationInstance ] }
    }

    def delete = {
		try{
	        def applicationInstance = Application.get( params.id )
	        def partyGroupInstance = Party.get( params.id )
	        if(applicationInstance) {
	            //applicationInstance.delete(flush:true)
				def appPartyRelationship = PartyRelationship.findAllWhere( partyRelationshipType:PartyRelationshipType.findById( "APPLICATION" ), partyIdFrom:applicationInstance, roleTypeCodeFrom:RoleType.findById( "APP_ROLE" ))
				appPartyRelationship.each{
	            	it.delete()
	            }
	            if( partyGroupInstance ){
	            	partyGroupInstance.delete(flush:true)
	            }
	            flash.message = "Application ${params.id} deleted"
	            redirect(action:list, id:applicationInstance.owner.id)
	            
	        }
	        else {
	            flash.message = "Application not found with id ${params.id}"
	            redirect(action:list, id:params.id, partyId:params.id)
	        }
		} catch(Exception ex){
    		flash.message = ex
    		 redirect(action:list, id:params.id, partyId:params.id)
    	}
    }

    def edit = {
        def applicationInstance = Application.get( params.id )

        if(!applicationInstance) {
            flash.message = "Application not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ applicationInstance : applicationInstance ]
        }
    }

    def update = {
        def applicationInstance = Application.get( params.id )
        if(applicationInstance) {
            applicationInstance.properties = params
            if(!applicationInstance.hasErrors() && applicationInstance.save()) {
                flash.message = "Application ${params.id} updated"
                redirect(action:show,id:applicationInstance.id)
            }
            else {
                render(view:'edit',model:[applicationInstance:applicationInstance])
            }
        }
        else {
            flash.message = "Application not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
    	def partyId = params.partyId
    	def applicationInstance = new Application()
        applicationInstance.properties = params
        return [ 'applicationInstance':applicationInstance,'partyId':partyId ]
    }

    def save = {
		def partyId = params.owner.id
        def applicationInstance = new Application(params)
        if( !applicationInstance.hasErrors() && applicationInstance.save() ) {
        	//save ApplicationOwner in partyRelationship 
        	def personId = params.applicationOwner
            if ( personId != null && personId != "" ) {
                def person = Party.findById( personId )
                def partyRelationship = partyRelationshipService.savePartyRelationship( "APPLICATION", applicationInstance, "APP_ROLE", person, "APP_OWNER" )
            }
            // save subjectMatterExpert in partyRelationship 
            personId = params.subjectMatterExpert
            if ( personId != null ) {
                def person = Party.findById( personId )
                def partyRelationship = partyRelationshipService.savePartyRelationship( "APPLICATION", applicationInstance, "APP_ROLE", person, "APP_SME" )
            }
            //save primaryContact in partyRelationship 
            personId = params.primaryContact
            if ( personId != null ) {
                def person = Party.findById( personId )
                def partyRelationship = partyRelationshipService.savePartyRelationship( "APPLICATION", applicationInstance, "APP_ROLE", person, "APP_1ST_CONTACT" )
            }
            //save secondContact in partyRelationship
            personId = params.secondContact
            if ( personId != null ) {
                def person = Party.findById( personId )
                def partyRelationship = partyRelationshipService.savePartyRelationship( "APPLICATION", applicationInstance, "APP_ROLE", person, "APP_2ND_CONTACT" )
            }
             
            flash.message = "Application ${applicationInstance.id} created"
            redirect(action:list,id:applicationInstance.owner.id)
        }
        else {
            render(view:'create',model:[applicationInstance:applicationInstance, partyId:partyId])
        }
    }
    //remote link for application dialog
    def editShow = {
      
        def items = []
        def applicationInstance = Application.get( params.id )
        def applicationOwner = partyRelationshipService.getApplicationStaff( params.id,'APP_OWNER' )
        def subjectMatterExpert = partyRelationshipService.getApplicationStaff( params.id,'APP_SME' )
        def primaryContact = partyRelationshipService.getApplicationStaff( params.id,'APP_1ST_CONTACT' )
        def secondContact = partyRelationshipService.getApplicationStaff( params.id,'APP_2ND_CONTACT' )
        if( applicationInstance.id == null ){
            items = [id:applicationInstance.id, appCode:applicationInstance.appCode,  environment:applicationInstance.environment, owner:applicationInstance.owner, name:applicationInstance.name, comment:applicationInstance.comment, applicationOwnerFL:applicationOwner[0], subjectMatterExpert:subjectMatterExpert[0], primaryContact:primaryContact[0], secondContact:secondContact[0] ]
        } else {
            items = [id:applicationInstance.id, appCode:applicationInstance.appCode, environment:applicationInstance.environment, owner:applicationInstance.owner, name:applicationInstance.name, comment:applicationInstance.comment, applicationOwnerFL:applicationOwner[0], subjectMatterExpert:subjectMatterExpert[0], primaryContact:primaryContact[0], secondContact:secondContact[0] ]
        }
       
        render items as JSON
	}
    // End of remote link for application dialog
    
    // update ajax overlay 
    def updateApplication = {
    		
        def applicationDialog= params.applicationDialog.split(',')
	        
        def applicationItems = []
        def applicationInstance = Application.get( applicationDialog[0] )
        if(applicationDialog[1] == null || applicationDialog[1] == "")
        {
            return false
        }
        applicationInstance.appCode = applicationDialog[1]
        applicationInstance.environment = applicationDialog[2]
        applicationInstance.save()
        def partyGroupInstance = PartyGroup.get( applicationDialog[0] )
        partyGroupInstance.name = applicationDialog[7]
        partyGroupInstance.comment = applicationDialog[8]
        partyGroupInstance.save()
			
			 
        for( int appStaff=3; appStaff < 7; appStaff++)
        {
            def personId = applicationDialog[appStaff]
            def roleTypeCodeTo
            //update ApplicationRoleTypeCodeTo
            if ( appStaff == 3 )
            {
                roleTypeCodeTo = 'APP_OWNER'
            }
            else if ( appStaff == 4 )
            {
                roleTypeCodeTo = 'APP_SME'
            }
            else if ( appStaff == 5 )
            {
                roleTypeCodeTo = 'APP_1ST_CONTACT'
            }
            else {
                roleTypeCodeTo = 'APP_2ND_CONTACT'
            }
				
            if ( personId != null && personId != "" ) {
        		 
                partyRelationshipService.updatePartyRelationshipPartyIdTo( 'APPLICATION', applicationDialog[0], 'APP_ROLE', applicationDialog[appStaff], roleTypeCodeTo  )
            }
        }
			
        def applicationOwner = partyRelationshipService.getApplicationStaff( params.id,'APP_OWNER' )
        def subjectMatterExpert = partyRelationshipService.getApplicationStaff( params.id,'APP_SME' )
        def primaryContact = partyRelationshipService.getApplicationStaff( params.id,'APP_1ST_CONTACT' )
        def secondContact = partyRelationshipService.getApplicationStaff( params.id,'APP_2ND_CONTACT' )
        if( applicationInstance.id == null ){
            applicationItems = [id:applicationInstance.id, appCode:applicationInstance.appCode,  environment:applicationInstance.environment, owner:applicationInstance.owner, name:applicationInstance.name, comment:applicationInstance.comment, applicationOwnerFL:applicationOwner[0], subjectMatterExpert:subjectMatterExpert[0], primaryContact:primaryContact[0], secondContact:secondContact[0] ]
        } else {
            applicationItems = [id:applicationInstance.id, appCode:applicationInstance.appCode, environment:applicationInstance.environment, owner:applicationInstance.owner, name:applicationInstance.name, comment:applicationInstance.comment, applicationOwnerFL:applicationOwner[0], subjectMatterExpert:subjectMatterExpert[0], primaryContact:primaryContact[0], secondContact:secondContact[0] ]
        }

        render applicationItems as JSON
    }


}
