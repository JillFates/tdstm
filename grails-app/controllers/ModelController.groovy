class ModelController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ modelInstanceList: Model.list( params ) ]
    }

    def show = {
        def modelInstance = Model.get( params.id )

        if(!modelInstance) {
            flash.message = "Model not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ modelInstance : modelInstance ] }
    }

    def delete = {
        def modelInstance = Model.get( params.id )
        if(modelInstance) {
            modelInstance.delete()
            flash.message = "Model ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Model not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def modelInstance = Model.get( params.id )

        if(!modelInstance) {
            flash.message = "Model not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ modelInstance : modelInstance ]
        }
    }

    def update = {
        def modelInstance = Model.get( params.id )
        if(modelInstance) {
            modelInstance.properties = params
            if(!modelInstance.hasErrors() && modelInstance.save()) {
                flash.message = "Model ${params.id} updated"
                redirect(action:show,id:modelInstance.id)
            }
            else {
                render(view:'edit',model:[modelInstance:modelInstance])
            }
        }
        else {
            flash.message = "Model not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def modelInstance = new Model()
        modelInstance.properties = params
        return ['modelInstance':modelInstance]
    }

    def save = {
        def modelInstance = new Model(params)
        if(!modelInstance.hasErrors() && modelInstance.save()) {
            flash.message = "Model ${modelInstance.id} created"
            redirect(action:show,id:modelInstance.id)
        }
        else {
            render(view:'create',model:[modelInstance:modelInstance])
        }
    }
}
