class PartyRoleController {
    
    def index = { redirect( action:list, params:params ) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [ delete:'POST', save:'POST', update:'POST' ]
    
    def list = {
        if( !params.max ) params.max = 10
        [ partyRoleInstanceList: PartyRole.list( params ) ]
    }
    // return Party role details by using composite primary Key
    def show = {
    	// return PartyRole object by using composite PrimaryKey
        def partyRoleInstance = PartyRole.get( new PartyRole( party:Party.get( params.partyId ), roleType:RoleType.get( params.roleTypeId ) ) )

        if ( !partyRoleInstance ) {
            flash.message = "PartyRole not found with id ${partyRoleInstance.party},${partyRoleInstance.roleType}"
            redirect( action:list )
        }
        else { return [ partyRoleInstance : partyRoleInstance ] }
    }
    // delete PartyRole details
    def delete = {
    	def partyRoleInstance = PartyRole.get( new PartyRole( party:Party.get( params.partyId ), roleType:RoleType.get( params.roleTypeId ) ) )
        if ( partyRoleInstance ) {
            partyRoleInstance.delete()
            flash.message = "PartyRole ${partyRoleInstance.party},${partyRoleInstance.roleType} deleted"
            redirect( action:list )
        }
        else {
            flash.message = "PartyRole not found with id ${partyRoleInstance.party},${partyRoleInstance.roleType}"
            redirect( action:list )
        }
    }
    // retuen PartyRole details to the Update form
    def edit = {
    	
    	def partyRoleInstance = PartyRole.get( new PartyRole( party:Party.get( params.partyId ), roleType:RoleType.get( params.roleTypeId ) ) )

        if ( !partyRoleInstance ) {
            flash.message = "PartyRole not found with id ${partyRoleInstance.party},${partyRoleInstance.roleType}"
            redirect(action:list)
        }
        else {
            return [ partyRoleInstance : partyRoleInstance ]
        }
    }
    // update PartyRole Details
    def update = {
    	def partyRoleDel = PartyRole.get( new PartyRole( party:Party.get( params.partyId ), roleType:RoleType.get( params.roleTypeId ) ) )
        if ( partyRoleDel ) {
            def partyRoleInstance = new PartyRole( params )
            if ( !partyRoleInstance.hasErrors() && partyRoleInstance.save( insert:true ) ) {
            	partyRoleDel.delete()
                flash.message = "PartyRole ${partyRoleInstance.party},${partyRoleInstance.roleType} Updated"
                redirect( action:show, params:[ partyId:partyRoleInstance.party.id, roleTypeId:partyRoleInstance.roleType.id ] )
            }
            else {
                render( view:'edit', model:[partyRoleInstance:partyRoleInstance] )
            }
        }
        else {
            flash.message = "PartyRole not found with id ${partyRoleInstance.party},${partyRoleInstance.roleType}"
            redirect( action:edit, id:params.id )
        }
    }
    // delete partyRole details
    def create = {
        def partyRoleInstance = new PartyRole()
        partyRoleInstance.properties = params
        return ['partyRoleInstance':partyRoleInstance]
    }
    // save PartyRole details
    def save = {
        def partyRoleInstance = new PartyRole( params )
        if ( !partyRoleInstance.hasErrors() && partyRoleInstance.save( insert:true ) ) {
            flash.message = "PartyRole ${partyRoleInstance.party},${partyRoleInstance.roleType} created"
            redirect( action:show, params:[ partyId:partyRoleInstance.party.id, roleTypeId:partyRoleInstance.roleType.id ] )
        }
        else {
            render( view:'create', model:[partyRoleInstance:partyRoleInstance] )
        }
    }
}
