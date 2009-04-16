class PartyGroupController {
    
	def partyRelationshipService
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	// Will Return PartyGroup list where PartyType = COMPANY
	def list = {
        def query = "from PartyGroup as p where partyType = 'COMPANY' "
        def partyGroupList = PartyGroup.findAll( query )
        [ partyGroupInstanceList: partyGroupList ]
    }

    def show = {
        def partyGroupInstance = PartyGroup.get( params.id )
        request.getSession(false).setAttribute("PARTYGROUP",partyGroupInstance)

        if(!partyGroupInstance) {
            flash.message = "PartyGroup not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ partyGroupInstance : partyGroupInstance ] }
    }

    def delete = {
        def partyGroupInstance = PartyGroup.get( params.id )
        if(partyGroupInstance) {
            partyGroupInstance.delete()
            flash.message = "PartyGroup ${partyGroupInstance} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "PartyGroup not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def partyGroupInstance = PartyGroup.get( params.id )
        request.getSession(false).setAttribute("PARTYGROUP",partyGroupInstance)
        if(!partyGroupInstance) {
            flash.message = "PartyGroup not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ partyGroupInstance : partyGroupInstance ]
        }
    }

    def update = {
        def partyGroupInstance = PartyGroup.get( params.id )
        partyGroupInstance.lastUpdated = new Date()
        if(partyGroupInstance) {
            partyGroupInstance.properties = params
            if(!partyGroupInstance.hasErrors() && partyGroupInstance.save()) {
                flash.message = "PartyGroup ${partyGroupInstance} updated"
                redirect(action:show,id:partyGroupInstance.id)
            }
            else {
                render(view:'edit',model:[partyGroupInstance:partyGroupInstance])
            }
        }
        else {
            flash.message = "PartyGroup not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def partyGroupInstance = new PartyGroup()
        partyGroupInstance.properties = params
        return ['partyGroupInstance':partyGroupInstance]
    }

    def save = {
        def partyGroupInstance = new PartyGroup(params)
        partyGroupInstance.dateCreated = new Date()
        if(!partyGroupInstance.hasErrors() && partyGroupInstance.save()) {
        	def partyType = partyGroupInstance.partyType
        	//	Statements to create CLIENT PartyRelationship with  TDS Company
        	if( partyType != null && partyType.id == "COMPANY" ){
        	
	        	def companyParty = PartyGroup.findByName( "TDS" )
	        	def partyRelationship = partyRelationshipService.savePartyRelationship( "CLIENTS", companyParty, "COMPANY", partyGroupInstance, "CLIENT" )

        	}
            flash.message = "PartyGroup ${partyGroupInstance} created"
            redirect(action:show,id:partyGroupInstance.id)
        }
        else {
            render(view:'create',model:[partyGroupInstance:partyGroupInstance])
        }
    }
}
