class RoleTypeController {
    
    def index = { redirect( action:list, params:params ) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [ delete:'POST', save:'POST', update:'POST' ]
    // return list of Roles
    def list = {
        if ( !params.max ) params.max = 10
        [ roleTypeInstanceList: RoleType.list( params ) ]
    }
    // return RoleType details to show form
    def show = {
        def roleTypeInstance = RoleType.get( params.id )

        if ( !roleTypeInstance ) {
            flash.message = "RoleType not found with id ${params.id}"
            redirect( action:list )
        }
        else { return [ roleTypeInstance : roleTypeInstance ] }
    }
    // delete RoleType details 
    def delete = {
        def roleTypeInstance = RoleType.get( params.id )
        if ( roleTypeInstance ) {
            roleTypeInstance.delete()
            flash.message = "RoleType ${params.id} deleted"
            redirect( action:list )
        }
        else {
            flash.message = "RoleType not found with id ${params.id}"
            redirect( action:list )
        }
    }
    //return roleType details to update form
    def edit = {
        def roleTypeInstance = RoleType.get( params.id )

        if ( !roleTypeInstance ) {
            flash.message = "RoleType not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ roleTypeInstance : roleTypeInstance ]
        }
    }
    // update RoleType details
    def update = {
        def roleTypeInstance = RoleType.get( params.roleTypeId )
        if ( roleTypeInstance ) {
            roleTypeInstance.properties = params
            if(!roleTypeInstance.hasErrors() && roleTypeInstance.save()) {
                flash.message = "RoleType ${params.description} updated"
                redirect( action:show, id:roleTypeInstance.id )
            }
            else {
                render( view:'edit', model:[roleTypeInstance:roleTypeInstance] )
            }
        }
        else {
            flash.message = "RoleType not found with id ${params.id}"
            redirect( action:edit, id:params.id )
        }
    }
    // return create form
    def create = {
        def roleTypeInstance = new RoleType()
        roleTypeInstance.properties = params
        return ['roleTypeInstance':roleTypeInstance]
    }
    // save RoleType details 
    def save = {
        def roleTypeInstance = new RoleType(params)
        roleTypeInstance.id = params.id
        if(!roleTypeInstance.hasErrors() && roleTypeInstance.save(insert:true)) {
            flash.message = "RoleType ${roleTypeInstance.id} created"
            redirect( action:show, id:roleTypeInstance.id )
        }
        else {
            render( view:'create', model:[roleTypeInstance:roleTypeInstance] )
        }
    }
}
