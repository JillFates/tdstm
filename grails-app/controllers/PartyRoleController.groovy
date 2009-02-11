class PartyRoleController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ partyRoleInstanceList: PartyRole.list( params ) ]
    }

    def show = {
        def partyRoleInstance = PartyRole.get( params.id )

        if(!partyRoleInstance) {
            flash.message = "PartyRole not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ partyRoleInstance : partyRoleInstance ] }
    }

    def delete = {
        def partyRoleInstance = PartyRole.get( params.id )
        if(partyRoleInstance) {
            partyRoleInstance.delete()
            flash.message = "PartyRole ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "PartyRole not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def partyRoleInstance = PartyRole.get( params.id )

        if(!partyRoleInstance) {
            flash.message = "PartyRole not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ partyRoleInstance : partyRoleInstance ]
        }
    }

    def update = {
        def partyRoleInstance = PartyRole.get( params.id )
        if(partyRoleInstance) {
            partyRoleInstance.properties = params
            if(!partyRoleInstance.hasErrors() && partyRoleInstance.save()) {
                flash.message = "PartyRole ${params.id} updated"
                redirect(action:show,id:partyRoleInstance.id)
            }
            else {
                render(view:'edit',model:[partyRoleInstance:partyRoleInstance])
            }
        }
        else {
            flash.message = "PartyRole not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def partyRoleInstance = new PartyRole()
        partyRoleInstance.properties = params
        return ['partyRoleInstance':partyRoleInstance]
    }

    def save = {
        def partyRoleInstance = new PartyRole(params)
        println"params---------------"+params
        if(!partyRoleInstance.hasErrors() && partyRoleInstance.save()) {
            flash.message = "PartyRole ${partyRoleInstance.id} created"
            redirect(action:show,id:partyRoleInstance.id)
        }
        else {
            render(view:'create',model:[partyRoleInstance:partyRoleInstance])
        }
    }
}
