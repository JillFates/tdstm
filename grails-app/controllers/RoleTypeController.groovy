class RoleTypeController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ roleTypeInstanceList: RoleType.list( params ) ]
    }

    def show = {
        def roleTypeInstance = RoleType.get( params.id )

        if(!roleTypeInstance) {
            flash.message = "RoleType not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ roleTypeInstance : roleTypeInstance ] }
    }

    def delete = {
        def roleTypeInstance = RoleType.get( params.id )
        if(roleTypeInstance) {
            roleTypeInstance.delete()
            flash.message = "RoleType ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "RoleType not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def roleTypeInstance = RoleType.get( params.id )

        if(!roleTypeInstance) {
            flash.message = "RoleType not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ roleTypeInstance : roleTypeInstance ]
        }
    }

    def update = {
        def roleTypeInstance = RoleType.get( params.id )
        if(roleTypeInstance) {
            roleTypeInstance.properties = params
            if(!roleTypeInstance.hasErrors() && roleTypeInstance.save()) {
                flash.message = "RoleType ${params.id} updated"
                redirect(action:show,id:roleTypeInstance.id)
            }
            else {
                render(view:'edit',model:[roleTypeInstance:roleTypeInstance])
            }
        }
        else {
            flash.message = "RoleType not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def roleTypeInstance = new RoleType()
        roleTypeInstance.properties = params
        return ['roleTypeInstance':roleTypeInstance]
    }

    def save = {
        def roleTypeInstance = new RoleType(params)
        if(!roleTypeInstance.hasErrors() && roleTypeInstance.save()) {
            flash.message = "RoleType ${roleTypeInstance.id} created"
            redirect(action:show,id:roleTypeInstance.id)
        }
        else {
            render(view:'create',model:[roleTypeInstance:roleTypeInstance])
        }
    }
}
