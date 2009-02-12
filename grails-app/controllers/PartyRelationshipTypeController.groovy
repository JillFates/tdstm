class PartyRelationshipTypeController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ partyRelationshipTypeInstanceList: PartyRelationshipType.list( params ) ]
    }

    def show = {
        def partyRelationshipTypeInstance = PartyRelationshipType.get( params.id )

        if(!partyRelationshipTypeInstance) {
            flash.message = "PartyRelationshipType not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ partyRelationshipTypeInstance : partyRelationshipTypeInstance ] }
    }

    def delete = {
        def partyRelationshipTypeInstance = PartyRelationshipType.get( params.id )
        if(partyRelationshipTypeInstance) {
            partyRelationshipTypeInstance.delete()
            flash.message = "PartyRelationshipType ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "PartyRelationshipType not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def partyRelationshipTypeInstance = PartyRelationshipType.get( params.id )

        if(!partyRelationshipTypeInstance) {
            flash.message = "PartyRelationshipType not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ partyRelationshipTypeInstance : partyRelationshipTypeInstance ]
        }
    }

    def update = {
        def partyRelationshipTypeInstance = PartyRelationshipType.get( params.id )
        if(partyRelationshipTypeInstance) {
            partyRelationshipTypeInstance.properties = params
            if(!partyRelationshipTypeInstance.hasErrors() && partyRelationshipTypeInstance.save()) {
                flash.message = "PartyRelationshipType ${params.id} updated"
                redirect(action:show,id:partyRelationshipTypeInstance.id)
            }
            else {
                render(view:'edit',model:[partyRelationshipTypeInstance:partyRelationshipTypeInstance])
            }
        }
        else {
            flash.message = "PartyRelationshipType not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def partyRelationshipTypeInstance = new PartyRelationshipType()
        partyRelationshipTypeInstance.properties = params
        return ['partyRelationshipTypeInstance':partyRelationshipTypeInstance]
    }

    def save = {
        def partyRelationshipTypeInstance = new PartyRelationshipType(params)
        if(!partyRelationshipTypeInstance.hasErrors() && partyRelationshipTypeInstance.save()) {
            flash.message = "PartyRelationshipType ${partyRelationshipTypeInstance.id} created"
            redirect(action:show,id:partyRelationshipTypeInstance.id)
        }
        else {
            render(view:'create',model:[partyRelationshipTypeInstance:partyRelationshipTypeInstance])
        }
    }
}
