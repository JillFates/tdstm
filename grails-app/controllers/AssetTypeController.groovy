class AssetTypeController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ assetTypeInstanceList: AssetType.list( params ) ]
    }

    def show = {
        def assetTypeInstance = AssetType.get( params.id )

        if(!assetTypeInstance) {
            flash.message = "AssetType not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ assetTypeInstance : assetTypeInstance ] }
    }

    def delete = {
        def assetTypeInstance = AssetType.get( params.id )
        if(assetTypeInstance) {
            assetTypeInstance.delete()
            flash.message = "AssetType ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "AssetType not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def assetTypeInstance = AssetType.get( params.id )

        if(!assetTypeInstance) {
            flash.message = "AssetType not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ assetTypeInstance : assetTypeInstance ]
        }
    }

    def update = {
        def assetTypeInstance = AssetType.get( params.id )
        if(assetTypeInstance) {
            assetTypeInstance.properties = params
            if(!assetTypeInstance.hasErrors() && assetTypeInstance.save()) {
                flash.message = "AssetType ${params.id} updated"
                redirect(action:show,id:assetTypeInstance.id)
            }
            else {
                render(view:'edit',model:[assetTypeInstance:assetTypeInstance])
            }
        }
        else {
            flash.message = "AssetType not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def assetTypeInstance = new AssetType()
        assetTypeInstance.properties = params
        return ['assetTypeInstance':assetTypeInstance]
    }

    def save = {
        def assetTypeInstance = new AssetType(params)
        if(!assetTypeInstance.hasErrors() && assetTypeInstance.save()) {
            flash.message = "AssetType ${assetTypeInstance.id} created"
            redirect(action:show,id:assetTypeInstance.id)
        }
        else {
            render(view:'create',model:[assetTypeInstance:assetTypeInstance])
        }
    }
}
