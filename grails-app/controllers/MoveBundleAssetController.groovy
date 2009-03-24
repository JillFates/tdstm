class MoveBundleAssetController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ moveBundleAssetInstanceList: MoveBundleAsset.list( params ) ]
    }

    def show = {
        def moveBundleAssetInstance = MoveBundleAsset.get( params.id )

        if(!moveBundleAssetInstance) {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ moveBundleAssetInstance : moveBundleAssetInstance ] }
    }

    def delete = {
        def moveBundleAssetInstance = MoveBundleAsset.get( params.id )
        if(moveBundleAssetInstance) {
            moveBundleAssetInstance.delete()
            flash.message = "MoveBundleAsset ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def moveBundleAssetInstance = MoveBundleAsset.get( params.id )

        if(!moveBundleAssetInstance) {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ moveBundleAssetInstance : moveBundleAssetInstance ]
        }
    }

    def update = {
        def moveBundleAssetInstance = MoveBundleAsset.get( params.id )
        if(moveBundleAssetInstance) {
            moveBundleAssetInstance.properties = params
            if(!moveBundleAssetInstance.hasErrors() && moveBundleAssetInstance.save()) {
                flash.message = "MoveBundleAsset ${params.id} updated"
                redirect(action:show,id:moveBundleAssetInstance.id)
            }
            else {
                render(view:'edit',model:[moveBundleAssetInstance:moveBundleAssetInstance])
            }
        }
        else {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def moveBundleAssetInstance = new MoveBundleAsset()
        moveBundleAssetInstance.properties = params
        return ['moveBundleAssetInstance':moveBundleAssetInstance]
    }

    def save = {
        def moveBundleAssetInstance = new MoveBundleAsset(params)
        if(!moveBundleAssetInstance.hasErrors() && moveBundleAssetInstance.save()) {
            flash.message = "MoveBundleAsset ${moveBundleAssetInstance.id} created"
            redirect(action:show,id:moveBundleAssetInstance.id)
        }
        else {
            render(view:'create',model:[moveBundleAssetInstance:moveBundleAssetInstance])
        }
    }
}
