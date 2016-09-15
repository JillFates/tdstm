class RoleTypeController {
    def idCheck = 0
    def index() { redirect( action:"list", params:params ) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [ delete:'POST', save:'POST', update:'POST' ]
    // return list of Roles
    def list() {
        def c = RoleType.createCriteria()
        if ( !params.max ) params.max = 25
        def roleTypes = c.list{
                            and{
                                order('type','asc')
                                order('level','desc')
                                order('id', 'asc')
                            }
                        }
        [ roleTypeInstanceList: roleTypes]
    }
    // return RoleType details to show form
    def show() {
        def roleTypeInstance = RoleType.get( params.id )

        if ( !roleTypeInstance ) {
            flash.message = "RoleType not found with id ${params.id}"
            redirect( action:"list" )
        }
        else { return [ roleTypeInstance : roleTypeInstance ] }
    }
    // delete RoleType details
    def delete() {
    	try{
    		def roleTypeInstance = RoleType.get( params.id )
	        if ( roleTypeInstance ) {
	            roleTypeInstance.delete(flush:true)
	            flash.message = "RoleType ${params.id} deleted"
	            redirect( action:"list" )
	        }
	        else {
	            flash.message = "RoleType not found with id ${params.id}"
	            redirect( action:"list" )
	        }
    	} catch(Exception ex){
    		flash.message = ex
			redirect( action:"list" )
    	}
    }
    //return roleType details to update form
    def edit() {
        def roleTypeInstance = RoleType.get( params.id )

        if ( !roleTypeInstance ) {
            flash.message = "RoleType not found with id ${params.id}"
            redirect(action:"list")
        }
        else {
            return [ roleTypeInstance : roleTypeInstance ]
        }
    }
    // update RoleType details
    def update() {
        def roleTypeInstance = RoleType.get( params.roleTypeId )
        if ( roleTypeInstance ) {
            roleTypeInstance.properties = params
            if(!roleTypeInstance.hasErrors() && roleTypeInstance.save()) {
                flash.message = "RoleType ${params.description} updated"
                redirect( action:"show", id:roleTypeInstance.id )
            }
            else {
                render( view:'edit', model:[roleTypeInstance:roleTypeInstance] )
            }
        }
        else {
            flash.message = "RoleType not found with id ${params.id}"
            redirect( action:"edit", id:params.id )
        }
    }
    // return create form
    def create() {

        def roleTypeInstance = new RoleType()
        roleTypeInstance.properties = params
        return ['roleTypeInstance':roleTypeInstance]
    }
    // save RoleType details
    def save() {
        RoleType roleTypeInstance = new RoleType(params)
        roleTypeInstance.id = params.id
        def role = RoleType.findById(params.id)
        // condition to check the id
        if ( role != null ) {
            flash.message = "Role Type ${role.id} already exist "
            idCheck = 1
        }else{
            roleTypeInstance.level = 1
            println("params: $params")
            println("properties: ${roleTypeInstance.properties}")
            if(!roleTypeInstance.hasErrors() && idCheck != 1 && roleTypeInstance.save(insert:true) ) {
                flash.message = "RoleType ${roleTypeInstance.id} created"
                redirect( action:"list")
            }
        }

        render( view:'create', model:[roleTypeInstance:roleTypeInstance] )
    }
}
