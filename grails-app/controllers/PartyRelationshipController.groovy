class PartyRelationshipController {
    
    def index = { redirect( action:list, params:params ) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [ delete:'POST', save:'POST', update:'POST' ]
    
    def list = {
        if( !params.max ) params.max = 10
        [ partyRelationshipInstanceList: PartyRelationship.list( params ) ]
    }
    //  return Party role details by using composite primary Key
    def show = {
    	//	return PartyRelationship object by using composite PrimaryKey
        def partyRelationshipInstance = PartyRelationship.get( new PartyRelationship( partyRelationshipType:PartyRelationshipType.get( params.partyRelationshipTypeId ), partyIdFrom:Party.get( params.partyIdFromId ), partyIdTo:Party.get( params.partyIdToId ), roleTypeCodeFrom:RoleType.get( params.roleTypeCodeFromId ), roleTypeCodeTo:RoleType.get( params.roleTypeCodeToId ) ) )
        
        if ( !partyRelationshipInstance ) {
            flash.message = "PartyRelationship not found "
            redirect( action:list )
        }
        else { return [ partyRelationshipInstance : partyRelationshipInstance ] }
    }
    // delete PartyRelationship using composite PrimaryKey
    def delete = {
    	//	return PartyRelationship object by using composite PrimaryKey
		def partyRelationshipInstance = PartyRelationship.get( new PartyRelationship( partyRelationshipType:PartyRelationshipType.get( params.partyRelationshipTypeId ), partyIdFrom:Party.get( params.partyIdFromId ), partyIdTo:Party.get( params.partyIdToId ), roleTypeCodeFrom:RoleType.get( params.roleTypeCodeFromId ), roleTypeCodeTo:RoleType.get( params.roleTypeCodeToId ) ) )
        if ( partyRelationshipInstance ) {
            partyRelationshipInstance.delete( flush:true )
            flash.message = "PartyRelationship deleted"
            redirect( action:list )
        }
        else {
            flash.message = "PartyRelationship not found "
            redirect( action:list )
        }
    }
    // create update form for PartyRelationship 
    def edit = {
    	//	return PartyRelationship object by using composite PrimaryKey
        def partyRelationshipInstance = PartyRelationship.get( new PartyRelationship( partyRelationshipType:PartyRelationshipType.get( params.partyRelationshipTypeId ), partyIdFrom:Party.get( params.partyIdFromId ), partyIdTo:Party.get( params.partyIdToId ), roleTypeCodeFrom:RoleType.get( params.roleTypeCodeFromId ), roleTypeCodeTo:RoleType.get( params.roleTypeCodeToId ) ) )
        if ( !partyRelationshipInstance ) {
            flash.message = "PartyRelationship not found "
            redirect(action:list)
        }
        else {
            return [ partyRelationshipInstance : partyRelationshipInstance ]
        }
    }
    // update PartyRelationship using composite PrimaryKey
    def update = {
    	//	return PartyRelationship object by using composite PrimaryKey
    	def partyRelationshipInstance = PartyRelationship.get( new PartyRelationship( partyRelationshipType:PartyRelationshipType.get( params.partyRelationshipTypeId ), partyIdFrom:Party.get( params.partyIdFromId ), partyIdTo:Party.get( params.partyIdToId ), roleTypeCodeFrom:RoleType.get( params.roleTypeCodeFromId ), roleTypeCodeTo:RoleType.get( params.roleTypeCodeToId ) ) )
    	def partyRelationshipUpdated = PartyRelationship.get( new PartyRelationship( partyRelationshipType:PartyRelationshipType.get( params.partyRelationshipType.id ), partyIdFrom:Party.get( params.partyIdFrom.id ), partyIdTo:Party.get( params.partyIdTo.id ), roleTypeCodeFrom:RoleType.get( params.roleTypeCodeFrom.id ), roleTypeCodeTo:RoleType.get( params.roleTypeCodeTo.id ) ) )
	    
    	if ( partyRelationshipInstance ) {
	    	// statement to check , whether composite key updated or not
	    	if ( partyRelationshipInstance == partyRelationshipUpdated ) {
				partyRelationshipInstance.properties = params
				if ( !partyRelationshipInstance.hasErrors() && partyRelationshipInstance.save() ) {
		        	flash.message = "PartyRelationship updated"
		        	redirect( action:show, params:[ partyRelationshipTypeId:partyRelationshipInstance.partyRelationshipType.id, partyIdFromId:partyRelationshipInstance.partyIdFrom.id, partyIdToId:partyRelationshipInstance.partyIdTo.id, roleTypeCodeFromId:partyRelationshipInstance.roleTypeCodeFrom.id, roleTypeCodeToId:partyRelationshipInstance.roleTypeCodeTo.id ] )
				} else {
					render( view:'edit', model:[partyRelationshipInstance:partyRelationshipInstance] )
				}
	    	} else {
		    	// if composite key updated insert new record
		    	def partyRelationship = new PartyRelationship( params )
		    	if ( !partyRelationship.hasErrors() && partyRelationship.save( insert: true ) ) {
		        	flash.message = "PartyRelationship updated"
		        	redirect( action:show, params:[ partyRelationshipTypeId:partyRelationship.partyRelationshipType.id, partyIdFromId:partyRelationship.partyIdFrom.id, partyIdToId:partyRelationship.partyIdTo.id, roleTypeCodeFromId:partyRelationship.roleTypeCodeFrom.id, roleTypeCodeToId:partyRelationship.roleTypeCodeTo.id ] )
		    	} else {
		    		render( view:'edit', model:[partyRelationshipInstance:partyRelationship] )
		    	}
	    	}
	    } else {
	    	flash.message = "PartyRelationship not found"
	        redirect( action:edit, id:params.id )
	    }
    }
    // create from for PartyRelationship
    def create = {
        def partyRelationshipInstance = new PartyRelationship()
        partyRelationshipInstance.properties = params
        return ['partyRelationshipInstance':partyRelationshipInstance]
    }
    // save PartyRelationship details
    def save = {
        def partyRelationshipInstance = new PartyRelationship( params )
        if ( !partyRelationshipInstance.hasErrors() && partyRelationshipInstance.save( insert:true ) ) {
            flash.message = "PartyRelationship created"
            redirect( action:show, params:[ partyRelationshipTypeId:partyRelationshipInstance.partyRelationshipType.id, partyIdFromId:partyRelationshipInstance.partyIdFrom.id, partyIdToId:partyRelationshipInstance.partyIdTo.id, roleTypeCodeFromId:partyRelationshipInstance.roleTypeCodeFrom.id, roleTypeCodeToId:partyRelationshipInstance.roleTypeCodeTo.id ] )
        }
        else {
            render( view:'create', model:[partyRelationshipInstance:partyRelationshipInstance] )
        }
    }
}
