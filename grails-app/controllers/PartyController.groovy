class PartyController {
    
    def index = { redirect( action:list, params:params ) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [ delete:'POST', save:'POST', update:'POST' ]
    
    def list = {
        if( !params.max ) params.max = 10
        [ partyInstanceList: Party.list( params ) ]
    }

    def show = {
        def partyInstance = Party.get( params.id )

        if ( !partyInstance ) {
            flash.message = "Party not found with id ${params.id}"
            redirect( action:list )
        }
        else { return [ partyInstance : partyInstance ] }
    }

    def delete = {
        def partyInstance = Party.get( params.id )
        if ( partyInstance ) {
            partyInstance.delete(flush:true)
            flash.message = "Party ${params.id} deleted"
            redirect( action:list )
        }
        else {
            flash.message = "Party not found with id ${params.id}"
            redirect( action:list )
        }
    }

    def edit = {
        def partyInstance = Party.get( params.id )

        if ( !partyInstance ) {
            flash.message = "Party not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ partyInstance : partyInstance ]
        }
    }

    def update = {
        def partyInstance = Party.get( params.id )
        if ( partyInstance ) {
            partyInstance.properties = params
            if(!partyInstance.hasErrors() && partyInstance.save()) {
                flash.message = "Party ${params.id} updated"
                redirect( action:show, id:partyInstance.id )
            }
            else {
                render( view:'edit', model:[partyInstance:partyInstance] )
            }
        }
        else {
            flash.message = "Party not found with id ${params.id}"
            redirect( action:edit, id:params.id )
        }
    }

    def create = {
        def partyInstance = new Party()
        partyInstance.properties = params
        return ['partyInstance':partyInstance]
    }

    def save = {
        def partyInstance = new Party(params)
        if(!partyInstance.hasErrors() && partyInstance.save()) {
            flash.message = "Party ${partyInstance.id} created"
            redirect( action:show, id:partyInstance.id )
        }
        else {
            render( view:'create', model:[partyInstance:partyInstance] )
        }
    }
}
