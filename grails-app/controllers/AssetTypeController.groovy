class AssetTypeController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if ( !params.max ) params.max = 10
        [ assetTypeInstanceList: AssetType.list( params ) ]
    }
    // Return AssetType details 
    def show = {
        def assetTypeInstance = AssetType.get( params.id )

        if ( !assetTypeInstance ) {
            flash.message = "AssetType not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ assetTypeInstance : assetTypeInstance ] }
    }
    // Delate AssetType details 
    def delete = {
        def assetTypeInstance = AssetType.get( params.id )
        if ( assetTypeInstance ) {
            assetTypeInstance.delete()
            flash.message = "AssetType ${params.id} deleted"
            redirect( action:list )
        }
        else {
            flash.message = "AssetType not found with id ${params.id}"
            redirect( action:list )
        }
    }
    // Return AssetType details to update form
    def edit = {
        def assetTypeInstance = AssetType.get( params.id )

        if(!assetTypeInstance) {
            flash.message = "AssetType not found with id ${params.id}"
            redirect( action:list )
        }
        else {
            return [ assetTypeInstance : assetTypeInstance ]
        }
    }
    // Update AssetType details 
    def update = {
        def assetTypeInstance = AssetType.get( params.id[0] )
        if ( assetTypeInstance ) {
            assetTypeInstance.properties = params
            if( !assetTypeInstance.hasErrors() && assetTypeInstance.save() ) {
                flash.message = "AssetType ${params.description} updated"
                redirect( action:show,id:assetTypeInstance.id )
            }
            else {
                render( view:'edit', model:[assetTypeInstance:assetTypeInstance] )
            }
        }
        else {
            flash.message = "AssetType not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }
    // return AssetType details  to create form
    def create = {
        def assetTypeInstance = new AssetType()
        assetTypeInstance.properties = params
        return ['assetTypeInstance':assetTypeInstance]
    }
    
    // save Asset type details
    def save = {
    		
        def assetTypeInstance = new AssetType( params )
        assetTypeInstance.id = params.id
        if( !assetTypeInstance.hasErrors() && assetTypeInstance.save( insert:true ) ) {
            flash.message = "AssetType ${assetTypeInstance.id} created"
            redirect( action:show, id:assetTypeInstance.id )
        }
        else {
            render( view:'create', model:[assetTypeInstance:assetTypeInstance] )
        }
    }
}
