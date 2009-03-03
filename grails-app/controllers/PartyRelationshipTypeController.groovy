class PartyRelationshipTypeController {

    def idCheck = 0;
    
    def index = { redirect( action:list, params:params ) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [ delete:'POST', save:'POST', update:'POST' ]
    // return PartyRelationshipType List 
    def list = {
        if( !params.max ) params.max = 10
        [ partyRelationshipTypeInstanceList: PartyRelationshipType.list( params ) ]
    }
    // return PartyRelationshipType details
    def show = {
        def partyRelationshipTypeInstance = PartyRelationshipType.get( params.id )

        if ( !partyRelationshipTypeInstance ) {
            flash.message = "PartyRelationshipType not found with id ${params.id}"
            redirect( action:list )
        }
        else { return [ partyRelationshipTypeInstance : partyRelationshipTypeInstance ] }
    }
    // delete PartyRelationshipType details
    def delete = {
        def partyRelationshipTypeInstance = PartyRelationshipType.get( params.id )
        if ( partyRelationshipTypeInstance ) {
            partyRelationshipTypeInstance.delete()
            flash.message = "PartyRelationshipType ${params.id} deleted"
            redirect( action:list )
        }
        else {
            flash.message = "PartyRelationshipType not found with id ${params.id}"
            redirect( action:list )
        }
    }
    // return PartyRelationshipType details to update form
    def edit = {
        def partyRelationshipTypeInstance = PartyRelationshipType.get( params.id )

        if ( !partyRelationshipTypeInstance ) {
            flash.message = "PartyRelationshipType not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ partyRelationshipTypeInstance : partyRelationshipTypeInstance ]
        }
    }
    // update PartyRelationshipType details
    def update = {
        def partyRelationshipTypeInstance = PartyRelationshipType.get( params.partyRelTypeId )
        if ( partyRelationshipTypeInstance ) {
            partyRelationshipTypeInstance.properties = params
            if(!partyRelationshipTypeInstance.hasErrors() && partyRelationshipTypeInstance.save()) {
                flash.message = "PartyRelationshipType ${params.description} updated"
                redirect( action:show, id:partyRelationshipTypeInstance.id )
            }
            else {
                render( view:'edit', model:[partyRelationshipTypeInstance:partyRelationshipTypeInstance] )
            }
        }
        else {
            flash.message = "PartyRelationshipType not found with id ${params.id}"
            redirect( action:edit, id:params.id )
        }
    }
    // provide create form for PartyRelationshipType
    def create = {
        def partyRelationshipTypeInstance = new PartyRelationshipType()
        partyRelationshipTypeInstance.properties = params
        return ['partyRelationshipTypeInstance':partyRelationshipTypeInstance]
    }
    // save PartyRelationshipType details
    def save = {
        def partyRelationshipTypeInstance = new PartyRelationshipType(params)
        partyRelationshipTypeInstance.id = params.id
        def partyRelationship = PartyRelationshipType.findById( params.id )
       // condition to check the Primary Key
        if( partyRelationship != null ){
             
            flash.message = "PartyRelationshipType ${partyRelationship.id} already exists"
            idCheck = 1
        }
        if(!partyRelationshipTypeInstance.hasErrors() && idCheck != 1 && partyRelationshipTypeInstance.save( insert: true )) {
            flash.message = "PartyRelationshipType ${partyRelationshipTypeInstance.id} created"
            redirect( action:show, id:partyRelationshipTypeInstance.id )
        }
        else {
            render( view:'create', model:[partyRelationshipTypeInstance:partyRelationshipTypeInstance] )
        }
    }
}
