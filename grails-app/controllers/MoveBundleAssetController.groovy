import grails.converters.JSON
class MoveBundleAssetController {
    
	def assetEntityAttributeLoaderService
    
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
	/*
	 *  Return asset details to assignAssets page
	 */
    def assignAssetsToBundle = {
    	def bundleId = params.bundleId
    	def moveBundleInstance = MoveBundle.findById( bundleId )
    	def moveBundles = MoveBundle.findAll("from MoveBundle where project.id = $moveBundleInstance.project.id")
    	def currentBundleAssets = MoveBundleAsset.findAll("from MoveBundleAsset where moveBundle.id = $moveBundleInstance.id")
    	//def moveBundleAssets = MoveBundleAsset.findAll("from MoveBundleAsset where asset.id not in (select asset.id from MoveBundleAsset where moveBundle.id = 22) group by asset.id")
    	render( view:'assignAssets', model:[moveBundles:moveBundles, currentBundleAssets: currentBundleAssets, moveBundleInstance:moveBundleInstance ] )
    }
	/*
	 *  Save Assets for corresponding Bundle
	 */
    def saveAssetsToBundle = {
    	def bundleFrom = params.bundleFrom
    	def bundleTo = params.bundleTo
    	def assets = params.assets
    	def moveBundleAssets = assetEntityAttributeLoaderService.saveAssetsToBundle( bundleTo, bundleFrom, assets )
    	return moveBundleAssets as JSON
    }
	/*
	 *   Return the list of assets for a selected bundle
	 */
	def getBundleAssets = {
		def bundleId = params.bundleId
		def items = []
		if(bundleId){
			def bundleAssets = MoveBundleAsset.findAll("from MoveBundleAsset where moveBundle.id = $bundleId ")
			bundleAssets.each{bundleAsset ->
	        
				items <<[id:bundleAsset.asset.id, name:bundleAsset.asset.id+" : "+bundleAsset.asset.serverName+" : "+bundleAsset.asset.sourceRack+" : "+bundleAsset.asset.sourceLocation ]
	         
			}
		}else{
			def assetEntities = AssetEntity.list()
			assetEntities.each{assetEntity ->
	        
				items <<[id:assetEntity.id, name:assetEntity.id+" : "+assetEntity.serverName+" : "+assetEntity.sourceRack+" : "+assetEntity.sourceLocation ]
	         
			}
		}
		render items as JSON
	}
}
