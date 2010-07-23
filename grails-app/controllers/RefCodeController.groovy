class RefCodeController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ refCodeInstanceList: RefCode.list( params ) ]
    }

    def show = {
        def refCodeInstance = RefCode.get( params.id )

        if(!refCodeInstance) {
            flash.message = "RefCode not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ refCodeInstance : refCodeInstance ] }
    }

    def delete = {
        def refCodeInstance = RefCode.get( params.id )
        if(refCodeInstance) {
            refCodeInstance.delete(flush:true)
            flash.message = "RefCode ${refCodeInstance} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "RefCode not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def refCodeInstance = RefCode.get( params.id )

        if(!refCodeInstance) {
            flash.message = "RefCode not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ refCodeInstance : refCodeInstance ]
        }
    }

    def update = {
        def refCodeInstance = RefCode.get( params.id )
        if(refCodeInstance) {
            refCodeInstance.properties = params
            if(!refCodeInstance.hasErrors() && refCodeInstance.save()) {
                flash.message = "RefCode ${params.id} updated"
                redirect(action:show,id:refCodeInstance.id)
            }
            else {
                render(view:'edit',model:[refCodeInstance:refCodeInstance])
            }
        }
        else {
            flash.message = "RefCode not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def refCodeInstance = new RefCode()
        refCodeInstance.properties = params
        return ['refCodeInstance':refCodeInstance]
    }

    def save = {
        def refCodeInstance = new RefCode(params)
        if(!refCodeInstance.hasErrors() && refCodeInstance.save()) {
            flash.message = "RefCode ${refCodeInstance.id} created"
            redirect(action:show,id:refCodeInstance.id)
        }
        else {
            render(view:'create',model:[refCodeInstance:refCodeInstance])
        }
    }
}
