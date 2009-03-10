import grails.converters.JSON
import java.text.SimpleDateFormat
class PersonController {
    
	def partyRelationshipService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	// return Persons which are related to company
    def list = {
        def companyId = params.id
        def personInstanceList
        if ( companyId!= null && companyId != "" ) {
        	
	        def query = "from Person s where s.id in (select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $companyId and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ) "
	        personInstanceList = Person.findAll( query )
        }
		return [ personInstanceList: personInstanceList, companyId:companyId ]
    }

    def show = {
			
        def personInstance = Person.get( params.id )
        def companyId = params.companyId
        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
        else { 
        	//def company = partyRelationshipService.getSatffCompany( personInstance )
        	
        	return [ personInstance : personInstance, companyId:companyId ] 
        }
    }

    def delete = {
        def personInstance = Person.get( params.id )
        def companyId = params.companyId
        if(personInstance) {
            personInstance.delete()
            flash.message = "Person ${params.id} deleted"
            redirect( action:list, params:[ id:companyId ] )
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
    }
	// return person details to EDIT form
    def edit = {
        def personInstance = Person.get( params.id )
        def companyId = params.companyId
        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
        else {
        	
            return [ personInstance : personInstance, companyId:companyId ]
        }
    }

    def update = {       
	        
        def personInstance = Person.get( params.id )
	        
        personInstance.lastUpdated = new Date()
	        
        def companyId = params.companyId
        if(personInstance) {
            personInstance.properties = params
            if ( !personInstance.hasErrors() && personInstance.save() ) {
                flash.message = "Person ${params.firstName} ${params.lastName} updated"
                redirect( action:list, params:[ id:companyId ])
            }
            else {
                flash.message = "Person ${params.firstName} ${params.lastName} not updated "
                redirect( action:list, params:[ id:companyId ])
            }
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ])
        }
    }
    // return person instance and companies 
    def create = {
        def personInstance = new Person()
        personInstance.properties = params
        // def companies = partyRelationshipService.getCompaniesList()
       	def companyId = params.companyId 
        return [ 'personInstance':personInstance, companyId:companyId ]
    }
    //Save the Person Detais
    def save = {
        def personInstance = new Person( params )
        personInstance.dateCreated = new Date()
        if ( !personInstance.hasErrors() && personInstance.save() ) {
            def companyId = params.companyId
            if ( companyId != null ) {
                def companyParty = Party.findById( companyId )
                def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", personInstance, "STAFF" )
            }
            flash.message = "Person ${personInstance.id} created"
            //redirect( action:list, id:personInstance.id , params:[companyId:companyId] )
            redirect( action:list, params:[ id:companyId ] )
        }
        else {
            def companyId = params.companyId
            flash.message = " Person FirstName cannot be blank. "
            redirect( action:list, params:[ id:companyId ] )
        }
    }
    //	Ajax Overlay for show
    def editShow = {
        
        def personInstance = Person.get( params.id )        
        def companyId = params.companyId
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm")
		SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm")
        def dateCreatedByFormat = outputDateFormat.format(inputDateFormat.parse(String.valueOf(personInstance.dateCreated)))
        def lastUpdatedFormat = outputDateFormat.format(inputDateFormat.parse(String.valueOf(personInstance.lastUpdated)))
        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
        else {       	

        	def items = [id: personInstance.id, firstName: personInstance.firstName, lastName: personInstance.lastName, nickName: personInstance.nickName, title: personInstance.title, active: personInstance.active, dateCreated: dateCreatedByFormat, lastUpdated: lastUpdatedFormat, companyId: companyId ]
            render items as JSON
        }
    }
    //ajax overlay for Edit
    def editStaff = {
        
        def personInstance = Person.get( params.id )
        render personInstance as JSON
    }
	/*
	 *  Remote method to update Staff Details
	 */
	def updateStaff = {
    	def personInstance = Person.get( params.id )
    	def projectId = params.projectId
    	personInstance.lastUpdated = new Date()
    	if(personInstance) {
    		personInstance.properties = params
            if ( !personInstance.hasErrors() && personInstance.save() ) {
            	flash.message = "Person ${params.firstName} updated"
                redirect( action:projectStaff, params:[ projectId:projectId ])
            } else {
            	flash.message = "Person ${params.firstName} not updated"
            	redirect( action:projectStaff, params:[ projectId:projectId ])
            }
    	} else {
    		flash.message = "Person not found with id ${params.id}"
    		redirect( action:projectStaff, params:[ projectId:projectId ])
    	}
	}
	/*
	 *  Return Project Staff 
	 */
	def projectStaff = {
		def projectId = params.projectId
		def projectStaff = partyRelationshipService.getProjectStaff( projectId )
		def companiesStaff = partyRelationshipService.getProjectCompaniesStaff( projectId )
		def projectCompanies = partyRelationshipService.getProjectCompanies( projectId )
		return [ projectStaff:projectStaff, companiesStaff:companiesStaff, projectCompanies:projectCompanies, projectId:projectId  ]
	}
	/*
	 *	Method to add project Staff through Ajax Overlay 
	 */
	def saveProjectStaff = {
    	def projectId = params.projectId
    	def personId = params.person
    	def roleType = params.roleType
    	def projectParty = Project.findById( projectId )
    	def personParty = Person.findById( personId )
    	def projectStaff = partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectParty, "PROJECT", personParty, roleType )
    	redirect(action:'projectStaff', params:[projectId:projectId] )
    	
    }
	/*
	 * Method to save person detais and create party relation with Project as well 
	 */
	def savePerson = {
		def personInstance = new Person( params )
		personInstance.dateCreated = new Date()
		def companyId = params.company
		def projectId = params.projectId
		def roleType = params.roleType
		if ( !personInstance.hasErrors() && personInstance.save() ) {
			
			if ( companyId != null && companyId != "" ) {
				def companyParty = Party.findById( companyId )
				def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", personInstance, "STAFF" )
			}
			if ( projectId != null && projectId != "" && roleType != null) {
				def projectParty = Party.findById( projectId )
				def partyRelationship = partyRelationshipService.savePartyRelationship( "PROJ_STAFF", projectParty, "PROJECT", personInstance, roleType )
			}
			flash.message = "Person ${personInstance.id} created"
			redirect( action:'projectStaff', params:[ projectId:projectId ] )
		}
		else {
			flash.message = " Person FirstName cannot be blank. "
			redirect( action:'projectStaff', params:[ projectId:projectId ] )
		}
    }	
}
