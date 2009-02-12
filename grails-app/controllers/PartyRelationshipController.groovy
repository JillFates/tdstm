class PartyRelationshipController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ partyRelationshipInstanceList: PartyRelationship.list( params ) ]
    }

    def show = {
        def partyRelationshipInstance = PartyRelationship.get( params.id )

        if(!partyRelationshipInstance) {
            flash.message = "PartyRelationship not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ partyRelationshipInstance : partyRelationshipInstance ] }
    }

    def delete = {
        def partyRelationshipInstance = PartyRelationship.get( params.id )
        if(partyRelationshipInstance) {
            partyRelationshipInstance.delete()
            flash.message = "PartyRelationship ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "PartyRelationship not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def partyRelationshipInstance = PartyRelationship.get( params.id )

        if(!partyRelationshipInstance) {
            flash.message = "PartyRelationship not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ partyRelationshipInstance : partyRelationshipInstance ]
        }
    }

    def update = {
        def partyRelationshipInstance = PartyRelationship.get( params.id )
        if(partyRelationshipInstance) {
            partyRelationshipInstance.properties = params
            if(!partyRelationshipInstance.hasErrors() && partyRelationshipInstance.save()) {
                flash.message = "PartyRelationship ${params.id} updated"
                redirect(action:show,id:partyRelationshipInstance.id)
            }
            else {
                render(view:'edit',model:[partyRelationshipInstance:partyRelationshipInstance])
            }
        }
        else {
            flash.message = "PartyRelationship not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def partyRelationshipInstance = new PartyRelationship()
        partyRelationshipInstance.properties = params
        return ['partyRelationshipInstance':partyRelationshipInstance]
    }

    def save = {
        def partyRelationshipInstance = new PartyRelationship(params)
        if(!partyRelationshipInstance.hasErrors() && partyRelationshipInstance.save()) {
            flash.message = "PartyRelationship ${partyRelationshipInstance.id} created"
            redirect(action:show,id:partyRelationshipInstance.id)
        }
        else {
            render(view:'create',model:[partyRelationshipInstance:partyRelationshipInstance])
        }
    }
}
