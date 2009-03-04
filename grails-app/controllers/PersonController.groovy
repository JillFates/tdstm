class PersonController {
    
	def partyRelationshipService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ personInstanceList: Person.list( params ) ]
    }

    def show = {
        def personInstance = Person.get( params.id )

        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
        else { 
        	def company = partyRelationshipService.getSatffCompany( personInstance )
        	return [ personInstance : personInstance, company:company ] }
    }

    def delete = {
        def personInstance = Person.get( params.id )
        if(personInstance) {
            personInstance.delete()
            flash.message = "Person ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
    }
	// return person details to EDIT form
    def edit = {
        def personInstance = Person.get( params.id )

        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
        else {
        	def companies = partyRelationshipService.getCompaniesList()
        	def company = partyRelationshipService.getSatffCompany( personInstance )
            return [ personInstance : personInstance, companies: companies, company:company  ]
        }
    }

    def update = {
        def personInstance = Person.get( params.id )
        personInstance.lastUpdated = new Date()
        if(personInstance) {
            personInstance.properties = params
            if ( !personInstance.hasErrors() && personInstance.save() ) {
            	def companyId = params.company 
            	partyRelationshipService.updateStaffCompany( personInstance, companyId )
                flash.message = "Person ${params.id} updated"
                redirect( action:show, id:personInstance.id )
            }
            else {
            	def companies = partyRelationshipService.getCompaniesList()
            	def company = partyRelationshipService.getSatffCompany( personInstance )
                render( view:'edit',model:[personInstance:personInstance, companies: companies, company:company ] )
            }
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }
    // return person instance and companies 
    def create = {
        def personInstance = new Person()
        personInstance.properties = params
        def companies = partyRelationshipService.getCompaniesList() 
        return [ 'personInstance':personInstance, companies:companies ]
    }
    //Save the Person Detais
    def save = {
        def personInstance = new Person( params )
        	personInstance.dateCreated = new Date()
        if ( !personInstance.hasErrors() && personInstance.save() ) {
        	def companyId = params.company
        	if ( companyId != null ) {
        		def companyParty = Party.findById( companyId )
        		def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", personInstance, "STAFF" )
        	}
            flash.message = "Person ${personInstance.id} created"
            redirect( action:show, id:personInstance.id )
        }
        else {
        	def companies = partyRelationshipService.getCompaniesList()
            render( view:'create', model:[ personInstance:personInstance, companies:companies ] )
        }
    }
}
