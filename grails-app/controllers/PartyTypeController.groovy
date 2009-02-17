class PartyTypeController {
    
    def index = { redirect( action:list, params:params ) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [ delete:'POST', save:'POST', update:'POST' ]
    //  return list of Party Types
    def list = {
        if ( !params.max ) params.max = 10
        [ partyTypeInstanceList: PartyType.list( params ) ]
    }
    // return PartyType details to show form
    def show = {
        def partyTypeInstance = PartyType.get( params.id )

        if ( !partyTypeInstance ) {
            flash.message = "PartyType not found with id ${params.id}"
            redirect( action:list )
        }
        else { return [ partyTypeInstance : partyTypeInstance ] }
    }
    //  delete PartyType details 
    def delete = {
        def partyTypeInstance = PartyType.get( params.id )
        if ( partyTypeInstance ) {
            partyTypeInstance.delete()
            flash.message = "PartyType ${params.id} deleted"
            redirect( action:list )
        }
        else {
            flash.message = "PartyType not found with id ${params.id}"
            redirect( action:list )
        }
    }
    //  return partyType details to update form
    def edit = {
        def partyTypeInstance = PartyType.get( params.id )

        if ( !partyTypeInstance ) {
            flash.message = "PartyType not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ partyTypeInstance : partyTypeInstance ]
        }
    }
    // update PartyType details
    def update = {
        def partyTypeInstance = PartyType.get( params.partyTypeId )
        if ( partyTypeInstance ) {
            partyTypeInstance.properties = params
            if ( !partyTypeInstance.hasErrors() && partyTypeInstance.save() ) {
                flash.message = "PartyType ${params.id} updated"
                redirect( action:show, id:partyTypeInstance.id )
            }
            else {
                render( view:'edit', model:[partyTypeInstance:partyTypeInstance] )
            }
        }
        else {
            flash.message = "PartyType not found with id ${params.id}"
            redirect( action:edit, id:params.id )
        }
    }
    // return create form
    def create = {
        def partyTypeInstance = new PartyType()
        partyTypeInstance.properties = params
        return ['partyTypeInstance':partyTypeInstance]
    }
    // save RoleType details 
    def save = {
        def partyTypeInstance = new PartyType( params )
        partyTypeInstance.id = params.id
        if ( !partyTypeInstance.hasErrors() && partyTypeInstance.save( insert:true ) ) {
            flash.message = "PartyType ${partyTypeInstance.id} created"
            redirect( action:show, id:partyTypeInstance.id )
        }
        else {
            render( view:'create', model:[partyTypeInstance:partyTypeInstance] )
        }
    }
}
