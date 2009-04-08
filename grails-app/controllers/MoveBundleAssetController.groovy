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
    	def moveBundleAssets = AssetEntity.findAll("from AssetEntity where id not in (select asset.id from MoveBundleAsset)")
    	render( view:'assignAssets', model:[moveBundles:moveBundles, currentBundleAssets: currentBundleAssets, moveBundleInstance:moveBundleInstance, moveBundleAssets:moveBundleAssets ] )
    }
	/*
	 *  Save Assets for corresponding Bundle
	 */
    def saveAssetsToBundle = {
		def items = []
    	def bundleFrom = params.bundleFrom
    	def bundleTo = params.bundleTo
    	def assets = params.assets
    	def moveBundleAssets = assetEntityAttributeLoaderService.saveAssetsToBundle( bundleTo, bundleFrom, assets )
    	if(moveBundleAssets != null){
	    	moveBundleAssets.each{bundleAsset ->
				items <<[id:bundleAsset.asset.id, assetName:bundleAsset.asset.assetName, assetTag:bundleAsset.asset.assetTag, application:bundleAsset.asset.application, srcLocation:bundleAsset.asset.sourceLocation  +"/"+bundleAsset.asset.sourceRack  ]
	    	}
    	} else {
    		def assetEntities = AssetEntity.findAll("from AssetEntity where id not in (select asset.id from MoveBundleAsset)")
			assetEntities.each{assetEntity ->
				items <<[id:assetEntity.id, assetName:assetEntity.assetName, assetTag:assetEntity.assetTag, application:assetEntity.application, srcLocation:assetEntity.sourceLocation +"/"+assetEntity.sourceRack  ]
			}
    	}
		render items as JSON
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
	        
				items <<[id:bundleAsset.asset.id, assetName:bundleAsset.asset.assetName, assetTag:bundleAsset.asset.assetTag, application:bundleAsset.asset.application, srcLocation:bundleAsset.asset.sourceLocation +"/"+bundleAsset.asset.sourceRack ]
	         
			}
		}else{
			def assetEntities = AssetEntity.findAll("from AssetEntity where id not in (select asset.id from MoveBundleAsset)")
			assetEntities.each{assetEntity ->
				items <<[id:assetEntity.id, assetName:assetEntity.assetName, assetTag:assetEntity.assetTag, application:assetEntity.application, srcLocation:assetEntity.sourceLocation +"/"+assetEntity.sourceRack ]
	         
			}
		}
		render items as JSON
	}
	
    /*
     * Get the Assets and Teams those belongs to Selected  MoveBundle
     */
    def bundleTeamAssignment = {
    	if(!params.max) params.max = 10
    	def bundleId = params.bundleId
    	def bundleInstance = MoveBundle.findById(bundleId)
    	def projectTeamInstanceList = ProjectTeam.findAllByMoveBundle( bundleInstance )
    	def teamAssetCounts = []
    	def cartAssetCounts = []
    	def moveBundleAssetsRacks
    	def rackPlan
    	def moveBundleAssetList = MoveBundleAsset.findAllByMoveBundle( bundleInstance )
    	if( params.rackPlan == 'RerackPlan') {
    		rackPlan = "RerackPlan"
    		moveBundleAssetsRacks = MoveBundleAsset.findAll("from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id  group by ma.asset.targetRack")
    		projectTeamInstanceList.each{projectTeam ->
    			def assetCount = MoveBundleAsset.countByMoveBundleAndTargetTeam( bundleInstance, projectTeam )
    			teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		def cartList = MoveBundleAsset.findAll("from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id  group by ma.cart")
			cartList.each { assetCart ->
				def cartAssetCount = MoveBundleAsset.countByMoveBundleAndCart( bundleInstance, assetCart.cart )
				def moveBundleAssetCartList = MoveBundleAsset.findAllByMoveBundleAndCart(bundleInstance, assetCart.cart)
				def usize = 0 
				for(int moveBundleAssetRow = 0; moveBundleAssetRow < moveBundleAssetCartList.size(); moveBundleAssetRow++ ) {
					usize = usize + Integer.parseInt(moveBundleAssetCartList[moveBundleAssetRow].asset.usize ? moveBundleAssetCartList[moveBundleAssetRow].asset.usize : 0)
				}
                cartAssetCounts << [ cart:assetCart.cart, cartAssetCount:cartAssetCount,usizeUsed:usize ]
			}
    		render(view:'bundleTeamAssignment',model:[moveBundleAssetInstanceList: moveBundleAssetList, moveBundleInstance:bundleInstance, projectTeamInstance:projectTeamInstanceList, teamAssetCount:teamAssetCounts, moveBundleAssetsRacks:moveBundleAssetsRacks, rack:rackPlan, cartAssetCountList:cartAssetCounts ])
    	}else 
    	{
    		rackPlan = "UnrackPlan"
    		moveBundleAssetsRacks = MoveBundleAsset.findAll("from MoveBundleAsset ma  where ma.moveBundle = $bundleInstance.id group by ma.asset.sourceRack") 
    		projectTeamInstanceList.each{projectTeam ->
				def assetCount = MoveBundleAsset.countByMoveBundleAndSourceTeam( bundleInstance, projectTeam )
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		render(view:'bundleTeamAssignment',model:[moveBundleAssetInstanceList: moveBundleAssetList, moveBundleInstance:bundleInstance, projectTeamInstance:projectTeamInstanceList, teamAssetCount:teamAssetCounts, moveBundleAssetsRacks:moveBundleAssetsRacks, rack:rackPlan ])
    	}
    }
    
    /*
     * Assign Asset to Selected MoveBundleTeam
     */ 
    def assetTeamAssign = {
    	def team = params.teamId
    	def asset = params.asset
    	def rackPlan = params.rackPlan
    	def bundleId = params.bundleId
    	def teamAssetCounts = []
    	def bundleInstance = MoveBundle.findById(bundleId)
    	def assetAssigned = []
    	
    	if( team != null && team != "" && team != "null")
    	{
    		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
    		if( projectTeamInstance ) {
    			def assetEntityInstance = AssetEntity.findById(asset)
    			def moveBundleAsset = MoveBundleAsset.find("from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id and  ma.asset = $assetEntityInstance.id ")
    			if(rackPlan == "UnrackPlan" ) {
    				moveBundleAsset.sourceTeam = projectTeamInstance
    			}else {
    				moveBundleAsset.targetTeam = projectTeamInstance
    			}
    			moveBundleAsset.save()
    			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleId, rackPlan )
    		}
    	}else {
    		def assetEntityInstance = AssetEntity.findById(asset)
			def moveBundleAsset = MoveBundleAsset.find("from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id and  ma.asset = $assetEntityInstance.id ")
			if( rackPlan == "UnrackPlan" ) {
				moveBundleAsset.sourceTeam = null
			}else {
				moveBundleAsset.targetTeam = null
			}
			moveBundleAsset.save()
			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleId, rackPlan )
    	}
    	render teamAssetCounts as JSON
    }
    /*
     * AutoFill with Selected Team  for list of Assets
     */
    def autoFillTeamAssign = {
    	def team = params.teamCode
    	def assetString = params.assets
    	def  assets= assetString.split(",")
    	def bundleId = params.bundleId
    	def rackPlan = params.rackPlan
    	def teamAssetCounts
    	def moveBundleAssetList
    	def teamAssetList = []
    	def assetList
    	def bundleInstance = MoveBundle.findById(bundleId)
    	if( team == "UnAssign") {
    		for( int assetRow = 0; assetRow < assets.size(); assetRow ++ ) {
				def assetEntityInstance = AssetEntity.findById(assets[ assetRow ] )
				def moveBundleAsset = MoveBundleAsset.find("from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id and  ma.asset = $assetEntityInstance.id ")
				if( rackPlan == "UnrackPlan" ) {
					moveBundleAsset.sourceTeam = null
				}else {
					moveBundleAsset.targetTeam = null
				}
				moveBundleAsset.save()
				moveBundleAssetList = MoveBundleAsset.findAll( " from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id ")
				assetList = assetEntityAttributeLoaderService.getAssetList( moveBundleAssetList, rackPlan, bundleInstance)
				teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleId, rackPlan )
			}
    		
    	}else {
    		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
    		if( projectTeamInstance ) {
    			for( int assetRow = 0; assetRow < assets.size(); assetRow ++ ) {
    				def assetEntityInstance = AssetEntity.findById(assets[ assetRow ] )
    				def moveBundleAsset = MoveBundleAsset.find("from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id and  ma.asset = $assetEntityInstance.id ")
    				if( rackPlan == "UnrackPlan" || team == "UnAssign" ) {
    					moveBundleAsset.sourceTeam = projectTeamInstance
    				}else {
    					moveBundleAsset.targetTeam = projectTeamInstance
    				}
    				moveBundleAsset.save()
    				moveBundleAssetList = MoveBundleAsset.findAll( " from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id ")
    				assetList = assetEntityAttributeLoaderService.getAssetList( moveBundleAssetList, rackPlan, bundleInstance)
    				teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleId, rackPlan )
    			}
    		}
    	}
    	teamAssetList << [ teamAssetCounts:teamAssetCounts, assetList:assetList ]
    	render teamAssetList as JSON
    }
    /*
     * Filter Assets By Team
     */
    def filterAssetByTeam = {
    	def team = params.teamCode
    	def bundleId = params.bundleId
    	def rackPlan = params.rackPlan
    	def bundleInstance = MoveBundle.findById(bundleId)
    	def moveBundleAsset = []
    	def projectTeam = []
    	def moveBundleAssetList
    	def projectTeamInstanceList = ProjectTeam.findAllByMoveBundle( bundleInstance )
       	projectTeamInstanceList.each{teams ->
				
            projectTeam << [ teamCode: teams.teamCode ]
        }
    	if(team == "" || team == null) {
    		moveBundleAssetList = MoveBundleAsset.findAll( " from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id ")
    	}else if(rackPlan == "UnrackPlan"){
    		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
    		moveBundleAssetList = MoveBundleAsset.findAll( " from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id and ma.sourceTeam = $projectTeamInstance.id ")
    	}else {
    		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
    		moveBundleAssetList = MoveBundleAsset.findAll( " from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id and ma.targetTeam = $projectTeamInstance.id ")
    	}
    	for( int assetRow = 0; assetRow < moveBundleAssetList.size(); assetRow++) {
    		def displayTeam  
    		if( rackPlan == "RerackPlan" ) {
    			displayTeam = moveBundleAssetList[assetRow]?.targetTeam?.teamCode
    		}else {
    			displayTeam = moveBundleAssetList[assetRow]?.sourceTeam?.teamCode
    		}
    		def assetEntityInstance = AssetEntity.findById( moveBundleAssetList[assetRow]?.asset?.id )
    		moveBundleAsset <<[id:assetEntityInstance?.id, assetName:assetEntityInstance?.assetName, model:assetEntityInstance?.model, sourceLocation:assetEntityInstance?.sourceLocation, sourceRack:assetEntityInstance?.sourceRack, targetLocation:assetEntityInstance?.targetLocation, targetRack:assetEntityInstance?.targetRack, sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, uSize:assetEntityInstance?.usize,team:displayTeam,, cart:moveBundleAssetList[assetRow]?.cart, shelf:moveBundleAssetList[assetRow]?.shelf, projectTeam:projectTeam ]
    	}
    	render moveBundleAsset as JSON
    }
    /*
     * Filter Assets By Rack
     */
    def filterAssetByRack = {
    	def rack = params.rack
       	def bundleId = params.bundleId
       	def rackPlan = params.rackPlan
       	def bundleInstance = MoveBundle.findById(bundleId)
       	def moveBundleAsset = []
       	def moveBundleAssetList
       	def projectTeam = []
       	def projectTeamInstanceList = ProjectTeam.findAllByMoveBundle( bundleInstance )
       	projectTeamInstanceList.each{team ->
				
            projectTeam << [ teamCode: team.teamCode ]
        }
       	if(rack == "" || rack == null) {
       		moveBundleAssetList = MoveBundleAsset.findAll( " from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id ")
       	}else if(rackPlan == "UnrackPlan"){
       		moveBundleAssetList = MoveBundleAsset.findAll( " from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id and ma.asset.sourceRack = '${rack}' ")
       	}else {
       		moveBundleAssetList = MoveBundleAsset.findAll( " from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id and ma.asset.targetRack = '${rack}' ")
       	}
    	if(moveBundleAssetList != null) {
    		for( int assetRow = 0; assetRow < moveBundleAssetList.size(); assetRow++) {
    			def displayTeam  
    			if( rackPlan == "RerackPlan" ) {
    				displayTeam = moveBundleAssetList[assetRow]?.targetTeam?.teamCode
    			}else {
    				displayTeam = moveBundleAssetList[assetRow]?.sourceTeam?.teamCode
    			}
    			def assetEntityInstance = AssetEntity.findById( moveBundleAssetList[assetRow].asset.id )
    			moveBundleAsset << [id:assetEntityInstance?.id, assetName:assetEntityInstance?.assetName, model:assetEntityInstance?.model, sourceLocation:assetEntityInstance?.sourceLocation, sourceRack:assetEntityInstance?.sourceRack, targetLocation:assetEntityInstance?.targetLocation, targetRack:assetEntityInstance?.targetRack, sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, uSize:assetEntityInstance?.usize, team:displayTeam, cart:moveBundleAssetList[assetRow]?.cart, shelf:moveBundleAssetList[assetRow]?.shelf, projectTeam:projectTeam ]
    		}
    	}
    	
        render moveBundleAsset as JSON
    }
	
	//Updating #Cart for Selected Asset 
	def assetCartAssign = {
		def cart = params.cartNumber
	    def asset = params.asset
	    def bundleId = params.bundleId
	    def cartAssetCounts = []
	    def bundleInstance = MoveBundle.findById(bundleId)
	    if( cart != null && cart != "")
	    {
	    	def assetEntityInstance = AssetEntity.findById(asset)
	    	def moveBundleAsset = MoveBundleAsset.executeUpdate(" update MoveBundleAsset ma set ma.cart = $cart where ma.moveBundle = $bundleInstance.id and  ma.asset = $assetEntityInstance.id ")
	    }
		def cartList = MoveBundleAsset.findAll("from MoveBundleAsset ma where ma.moveBundle = $bundleInstance.id  group by ma.cart")
		cartList.each { assetCart ->
			def cartAssetCount = MoveBundleAsset.countByMoveBundleAndCart( bundleInstance, assetCart.cart )
			def moveBundleAssetList = MoveBundleAsset.findAllByMoveBundleAndCart(bundleInstance, assetCart.cart)
			def usize = 0
			for(int moveBundleAssetRow = 0; moveBundleAssetRow < moveBundleAssetList.size(); moveBundleAssetRow++ ) {
				usize = usize + Integer.parseInt(moveBundleAssetList[moveBundleAssetRow].asset.usize ? moveBundleAssetList[moveBundleAssetRow].asset.usize : 0)
			}
			  
			cartAssetCounts << [ cart:assetCart.cart, cartAssetCount:cartAssetCount,usizeUsed:usize ]
		}
	    render cartAssetCounts as JSON
	}
	
	//Updating #Shelf for Selected Asset
	def assetShelfAssign = {
		def shelf = params.shelfNumber
	    def asset = params.asset
	    def bundleId = params.bundleId
	    def teamAssetCounts = []
	    def bundleInstance = MoveBundle.findById(bundleId)
	    if( shelf != null && shelf != "")
	    {
	    	def assetEntityInstance = AssetEntity.findById(asset)
	    	def moveBundleAsset = MoveBundleAsset.executeUpdate(" update MoveBundleAsset ma set ma.shelf = '$shelf' where ma.moveBundle = $bundleInstance.id and  ma.asset = $assetEntityInstance.id ")
	    }
	    render teamAssetCounts as JSON
	}
     
}
