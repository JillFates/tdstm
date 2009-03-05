import grails.converters.JSON
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
        println"companyId---->"+companyId
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
                flash.message = "Person ${params.id} updated"
                redirect( action:show, id:personInstance.id, params:[ companyId:companyId ])
            }
            else {
                render( view:'edit',model:[ personInstance:personInstance, companyId:companyId ] )
            }
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:edit, id:params.id, params:[ companyId:companyId ] )
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
            redirect( action:show, id:personInstance.id , params:[companyId:companyId] )
        }
        else {
        	def companyId = params.companyId
            render( view:'create', model:[ personInstance:personInstance, companyId:companyId ] )
        }
    }
}
