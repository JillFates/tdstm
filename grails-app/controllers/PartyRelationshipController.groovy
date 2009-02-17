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
            partyRelationshipInstance.delete()
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
    	def partyRelDel = PartyRelationship.get( new PartyRelationship( partyRelationshipType:PartyRelationshipType.get( params.partyRelationshipTypeId ), partyIdFrom:Party.get( params.partyIdFromId ), partyIdTo:Party.get( params.partyIdToId ), roleTypeCodeFrom:RoleType.get( params.roleTypeCodeFromId ), roleTypeCodeTo:RoleType.get( params.roleTypeCodeToId ) ) )
        if ( partyRelDel ) {
        	def partyRelationshipInstance = new PartyRelationship( params )
            if ( !partyRelationshipInstance.hasErrors() && partyRelationshipInstance.save( insert:true ) ) {
            	partyRelDel.delete()
            	flash.message = "PartyRelationship updated"
                redirect( action:show, params:[ partyRelationshipTypeId:partyRelationshipInstance.partyRelationshipType.id, partyIdFromId:partyRelationshipInstance.partyIdFrom.id, partyIdToId:partyRelationshipInstance.partyIdTo.id, roleTypeCodeFromId:partyRelationshipInstance.roleTypeCodeFrom.id, roleTypeCodeToId:partyRelationshipInstance.roleTypeCodeTo.id ] )
            }
            else {
                render( view:'edit', model:[partyRelationshipInstance:partyRelationshipInstance] )
            }
        }
        else {
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
