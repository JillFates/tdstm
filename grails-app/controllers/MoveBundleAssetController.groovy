import grails.converters.JSON
class MoveBundleAssetController {
	def partyRelationshipService
	def assetEntityAttributeLoaderService
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ moveBundleAssetInstanceList: AssetEntity.list( params ) ]
    }

    def show = {
        def moveBundleAssetInstance = AssetEntity.get( params.id )

        if(!moveBundleAssetInstance) {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ moveBundleAssetInstance : moveBundleAssetInstance ] }
    }

    def delete = {
        def moveBundleAssetInstance = AssetEntity.get( params.id )
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
        def moveBundleAssetInstance = AssetEntity.get( params.id )

        if(!moveBundleAssetInstance) {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ moveBundleAssetInstance : moveBundleAssetInstance ]
        }
    }

    def update = {
        def moveBundleAssetInstance = AssetEntity.get( params.id )
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
        def moveBundleAssetInstance = new AssetEntity()
        moveBundleAssetInstance.properties = params
        return ['moveBundleAssetInstance':moveBundleAssetInstance]
    }

    def save = {
        def moveBundleAssetInstance = new AssetEntity(params)
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
    	def currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $moveBundleInstance.id")
    	def moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null ")
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
				items <<[id:bundleAsset.id, assetName:bundleAsset.assetName, assetTag:bundleAsset.assetTag, application:bundleAsset.application, srcLocation:bundleAsset.sourceLocation  +"/"+bundleAsset.sourceRack  ]
	    	}
    	} else {
    		def assetEntities = AssetEntity.findAll("from AssetEntity where moveBundle = null ")
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
			def bundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $bundleId ")
			bundleAssets.each{bundleAsset ->

				items <<[id:bundleAsset.id, assetName:bundleAsset.assetName, assetTag:bundleAsset.assetTag, application:bundleAsset.application, srcLocation:bundleAsset.sourceLocation +"/"+bundleAsset.sourceRack ]
	         
			}
		}else{
			def assetEntities = AssetEntity.findAll("from AssetEntity where moveBundle = null")
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
    	def cartAssetCounts
    	def assetEntitysRacks
    	def rackPlan
    	def assetEntityList = AssetEntity.findAllByMoveBundle( bundleInstance )
    	if( params.rackPlan == 'RerackPlan') {
    		rackPlan = "RerackPlan"
    		assetEntitysRacks = AssetEntity.findAll("from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.targetRack")
    		projectTeamInstanceList.each{projectTeam ->
    			def assetCount = AssetEntity.countByMoveBundleAndTargetTeam( bundleInstance, projectTeam )
    			teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		def cartList = AssetEntity.findAll("from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.cart")
			cartAssetCounts = assetEntityAttributeLoaderService.getCartAssetCounts(bundleId, cartList)
    		render(view:'bundleTeamAssignment',model:[assetEntityInstanceList: assetEntityList, moveBundleInstance:bundleInstance, projectTeamInstance:projectTeamInstanceList, teamAssetCount:teamAssetCounts, assetEntitysRacks:assetEntitysRacks, rack:rackPlan, cartAssetCountList:cartAssetCounts ])
    	}else 
    	{
    		rackPlan = "UnrackPlan"
    		assetEntitysRacks = AssetEntity.findAll("from AssetEntity ma  where ma.moveBundle = $bundleInstance.id group by ma.sourceRack") 
    		projectTeamInstanceList.each{projectTeam ->
				def assetCount = AssetEntity.countByMoveBundleAndSourceTeam( bundleInstance, projectTeam )
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		render(view:'bundleTeamAssignment',model:[assetEntityInstanceList: assetEntityList, moveBundleInstance:bundleInstance, projectTeamInstance:projectTeamInstanceList, teamAssetCount:teamAssetCounts, assetEntitysRacks:assetEntitysRacks, rack:rackPlan ])
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
    			if(rackPlan == "UnrackPlan" ) {
    				assetEntityInstance.sourceTeam = projectTeamInstance
    				
    			}else {
    				assetEntityInstance.targetTeam = projectTeamInstance
    			}
    			assetEntityInstance.save()
    			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleId, rackPlan )
    		}
    	}else {
    		def assetEntityInstance = AssetEntity.findById(asset)
			if( rackPlan == "UnrackPlan" ) {
				assetEntityInstance.sourceTeam = null
			}else {
				assetEntityInstance.targetTeam = null
			}
    		assetEntityInstance.save()
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
    	def assetEntityList
    	def teamAssetList = []
    	def assetList
    	def bundleInstance = MoveBundle.findById(bundleId)
    	
    	if( team == "UnAssign") {
    		
    		for( int assetRow = 0; assetRow < assets.size(); assetRow ++ ) {
				def assetEntityInstance = AssetEntity.findById(assets[ assetRow ] )
				if( rackPlan == "UnrackPlan" ) {
					assetEntityInstance.sourceTeam = null
				}else {
					assetEntityInstance.targetTeam = null
				}
				assetEntityInstance.save()
				assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id ")
				assetList = assetEntityAttributeLoaderService.getAssetList( assetEntityList, rackPlan, bundleInstance)
				teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleId, rackPlan )
			}
    		
    	}else {
    		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
    		if( projectTeamInstance ) {
    			for( int assetRow = 0; assetRow < assets.size(); assetRow ++ ) {
    				
    				def assetEntityInstance = AssetEntity.findById(assets[assetRow] )
    				if( rackPlan == "UnrackPlan" || team == "UnAssign" ) {
    					assetEntityInstance.sourceTeam = projectTeamInstance
    				}else {
    					assetEntityInstance.targetTeam = projectTeamInstance
    				}
    				assetEntityInstance.save()
    				assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id ")
    				assetList = assetEntityAttributeLoaderService.getAssetList( assetEntityList, rackPlan, bundleInstance)
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
    	def assetEntity = []
    	def projectTeam = []
    	def assetEntityList
    	def projectTeamInstanceList = ProjectTeam.findAllByMoveBundle( bundleInstance )
       	projectTeamInstanceList.each{teams ->
				
            projectTeam << [ teamCode: teams.teamCode ]
        }
    	if(team == "" || team == null) {
    		assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id ")
    	}else if(rackPlan == "UnrackPlan"){
    		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
    		assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and ma.sourceTeam = $projectTeamInstance.id ")
    	}else {
    		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
    		assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and ma.targetTeam = $projectTeamInstance.id ")
    	}
    	for( int assetRow = 0; assetRow < assetEntityList.size(); assetRow++) {
    		def displayTeam  
    		if( rackPlan == "RerackPlan" ) {
    			displayTeam = assetEntityList[assetRow]?.targetTeam?.teamCode
    		}else {
    			displayTeam = assetEntityList[assetRow]?.sourceTeam?.teamCode
    		}
    		def assetEntityInstance = AssetEntity.findById( assetEntityList[assetRow]?.id )
    		assetEntity <<[id:assetEntityInstance?.id, assetName:assetEntityInstance?.assetName, model:assetEntityInstance?.model, sourceLocation:assetEntityInstance?.sourceLocation, sourceRack:assetEntityInstance?.sourceRack, targetLocation:assetEntityInstance?.targetLocation, targetRack:assetEntityInstance?.targetRack, sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, uSize:assetEntityInstance?.usize,team:displayTeam,, cart:assetEntityList[assetRow]?.cart, shelf:assetEntityList[assetRow]?.shelf, projectTeam:projectTeam ]
    	}
    	render assetEntity as JSON
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
       		moveBundleAssetList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id ")
       	}else if(rackPlan == "UnrackPlan"){
       		moveBundleAssetList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and ma.sourceRack = '${rack}' ")
       	}else {
       		moveBundleAssetList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and ma.targetRack = '${rack}' ")
       	}
    	if(moveBundleAssetList != null) {
    		for( int assetRow = 0; assetRow < moveBundleAssetList.size(); assetRow++) {
    			def displayTeam  
    			if( rackPlan == "RerackPlan" ) {
    				displayTeam = moveBundleAssetList[assetRow]?.targetTeam?.teamCode
    			}else {
    				displayTeam = moveBundleAssetList[assetRow]?.sourceTeam?.teamCode
    			}
    			def assetEntityInstance = AssetEntity.findById( moveBundleAssetList[assetRow]?.id )
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
	    def cartAssetCounts 
	    def bundleInstance = MoveBundle.findById(bundleId)
	    if( cart != null && cart != "")
	    {
	    	def assetEntityInstance = AssetEntity.findById(asset)
	    	def moveBundleAsset = AssetEntity.executeUpdate(" update AssetEntity ma set ma.cart = $cart where ma.moveBundle = $bundleInstance.id and  ma.id = $assetEntityInstance.id ")
	    }
		def cartList = AssetEntity.findAll("from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.cart")
		cartAssetCounts = assetEntityAttributeLoaderService.getCartAssetCounts(bundleId, cartList)
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
	    	def moveBundleAsset = AssetEntity.executeUpdate(" update AssetEntity ma set ma.shelf = '$shelf' where ma.moveBundle = $bundleInstance.id and  ma.id = $assetEntityInstance.id ")
	    }
	    render teamAssetCounts as JSON
	}
	
    //Generate Report Dialog
    def getBundleListForReportDialog = {
    	def reportId = params.reportId
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
        def projectId = currProj.CURR_PROJ
        def projectInstance = Project.findById( projectId )
        def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
        if (reportId == "Rack Layout") {
        	render(view:'rackLayout',model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
        } else {
        	render(view:'teamWorkSheets',model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
        }
    	     
     }
    
    //get teams for selected bundles.
    def getTeamsForBundles = {
    	def bundleId = params.bundleId
    	def projectId = params.projectId
    	def projectInstance = Project.findById( projectId )
    	def TeamInstance 
    		if( bundleId == "") {
    			TeamInstance = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle in ( select m.id from MoveBundle m where m.project = $projectId ) " )
    		} else {
    			def moveBundleInstance = MoveBundle.findById( bundleId )
    			TeamInstance = ProjectTeam.findAllByMoveBundle( moveBundleInstance )
    		}
    		def teams = []
    		TeamInstance.each {
        		teams <<[id:it.id, name:it.teamCode]    		
        	}
        	render teams as JSON
    }
    //Generate a jasper Report 
    def teamSheetReport = {
    	if(params.moveBundle == "null") {    		
    		flash.message = " Please Select Bundles. "
    		redirect( action:'getBundleListForReportDialog', params:[reportId: 'Team Worksheets'] )
    	} else {
    	def moveBundleInstance = MoveBundle.findById(params.moveBundle)
    	def projectTeamInstance
   		def location = params.location
   		def reportFields = []
    	def bundleName = "All Bundles"
    	def teamName = "All Teams"
   		def assetEntityList
   		def targetAssetEntitylist
   		def teamMembers =""
   		if(params.teamFilter != "null"){
   	   		projectTeamInstance = ProjectTeam.findById( params.teamFilter )
   	   	}
   		if( moveBundleInstance ) {
   			bundleName = moveBundleInstance?.name
   			if( projectTeamInstance ) {
   				teamName = projectTeamInstance?.teamCode
   				def teamMemberList = partyRelationshipService.getBundleTeamMembers( projectTeamInstance )
   				teamMemberList.each {
   					teamMembers += it.get("name") +"-"
   				}
   				
   				if(location == "source"){
   					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.sourceTeam = $projectTeamInstance.id order By asset.sourceTeam")
   				}else if(location == "target"){
   					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.targetTeam = $projectTeamInstance.id order By asset.targetTeam")
   				}else {
   					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.sourceTeam = $projectTeamInstance.id order By asset.sourceTeam")
   					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.targetTeam = $projectTeamInstance.id order By asset.targetTeam")
   				}
   			}else {
   				
   				if(location == "source"){
   					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id  order By asset.sourceTeam")
   				}else if(location == "target"){
   					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id  order By asset.targetTeam")
   				}else {
   					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id  order By asset.sourceTeam")
   					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id  order By asset.targetTeam")
   				}
   			}
   		 }else {
   			if( projectTeamInstance  ) {
   				def teamMemberList = partyRelationshipService.getBundleTeamMembers( projectTeamInstance )
   				teamMemberList.each {
   					teamMembers += it.get("name") +"-"
   				}
   				teamName = projectTeamInstance?.teamCode
   				if(location == "source"){
   					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.sourceTeam = $projectTeamInstance.id order By asset.sourceTeam")
   				}else if(location == "target"){
   					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.targetTeam = $projectTeamInstance.id order By asset.targetTeam")
   				}else {
   					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.sourceTeam = $projectTeamInstance.id  order By asset.sourceTeam")
   					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.targetTeam = $projectTeamInstance.id order By asset.targetTeam")
   				}
   			}else {
   				if(location == "source"){
   					assetEntityList = AssetEntity.findAll("from AssetEntity asset  order By asset.sourceTeam")
   				}else if(location == "target"){
   					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  order By asset.targetTeam")
   				}else {
   					assetEntityList = AssetEntity.findAll("from AssetEntity asset  order By asset.sourceTeam")
   					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  order By asset.targetTeam")
   				}
   			}
   		}
   		def currProj = getSession().getAttribute( "CURR_PROJ" )
        def projectId = currProj.CURR_PROJ
        def projectInstance = Project.findById( projectId )
        def partyGroupInstance = PartyGroup.get(projectInstance.id)
   		assetEntityList.each { asset ->
   			 def bundleInstance = MoveBundle.findById(asset.moveBundle.id)
   			 def bundlePartyGroup = PartyGroup.findById(bundleInstance?.id)
   			 def teamPartyGroup = PartyGroup.findById(asset.sourceTeam?.id)
   			 def assetCommentList = AssetComment.findByAssetEntity(asset)
    		def assetCommentString =""
    		assetCommentList.each { assetComment ->
    		assetCommentString = assetCommentString + assetComment.comment +"\n"
    		}
   			 
   			 reportFields <<['assetName':asset.assetName , 'assetTag':asset.assetTag, "assetType":asset.assetType, "manufacturer":asset.manufacturer, "model":asset.model, "sourceTargetrack":asset.sourceRack, "position":asset.sourceRackPosition, "sourceTargetPos":asset.sourceLocation+ "(source/ unracking)", "usize":asset.usize, "cart":asset.cart, "shelf":asset.shelf,"source_team_id":asset.sourceTeam.id, "move_bundle_id":asset.moveBundle.id, "clientName":projectInstance.client.name,
   			                 'projectName':partyGroupInstance.name,'startAt':projectInstance.startDate, 'completedAt':projectInstance.completionDate, 'bundleName':bundlePartyGroup.name, 'teamName':teamPartyGroup.name, 'teamMembers':teamMembers,'location':"Source Team", 'rack':"SourceRack",'rackPos':"SourceRackPosition",'truck':asset.truck, 'room':asset.sourceRoom, 'PDU':asset.powerPort,'NIC':asset.nicPort,'kvmPort':asset.kvmDevice ? asset.kvmDevice : '' + asset.kvmPort ? asset.kvmPort :'', 'hbaPort':asset.fiberCabinet + asset.hbaPort, 'instructions':assetCommentString, 'sourcetargetLoc':"s"]
   		}
    	targetAssetEntitylist.each { asset ->
    		
    		def bundleInstance = MoveBundle.findById(asset.moveBundle.id)
    		def bundlePartyGroup = PartyGroup.findById(bundleInstance?.id)
    		def teamPartyGroup = PartyGroup.findById(asset.targetTeam?.id)
    		def assetCommentList = AssetComment.findByAssetEntity(asset)
    		def assetCommentString =""
    		assetCommentList.each { assetComment ->
    		assetCommentString = assetCommentString + assetComment.comment +"\n"
    		}
    		reportFields <<['assetName':asset.assetName , 'assetTag':asset.assetTag, "assetType":asset.assetType, "manufacturer":asset.manufacturer, "model":asset.model, "sourceTargetrack":asset.targetRack, "position":asset.targetRackPosition, "sourceTargetPos":asset.targetLocation+ "(target/ reracking)", "usize":asset.usize, "cart":asset.cart, "shelf":asset.shelf,"source_team_id":asset.targetTeam.id, "move_bundle_id":asset.moveBundle.id, "clientName":projectInstance.client.name,
  			                 'projectName':partyGroupInstance.name,'startAt':projectInstance.startDate, 'completedAt':projectInstance.completionDate, 'bundleName':bundlePartyGroup.name, 'teamName':teamPartyGroup.name, 'teamMembers':teamMembers,'location':"Target Team", 'rack':"TargetRack",'rackPos':"TargetRackPosition",'truck':asset.truck,'room':asset.targetRoom,'PDU':asset.powerPort,'NIC':asset.nicPort, 'kvmPort':asset.kvmDevice ? asset.kvmDevice : '' + asset.kvmPort ? asset.kvmPort : '', 'hbaPort':asset.fiberCabinet ? asset.fiberCabinet : '' + asset.hbaPort ? asset.hbaPort : '','instructions':assetCommentString,'sourcetargetLoc':"t"]
  		}
   		chain(controller:'jasper',action:'index',model:[data:reportFields],params:params)
    	}
    }
    //Get the List of Racks corresponding to Selected Bundle
    def getRacksForBundles = {
    	def bundleId = params.bundleId
        def projectId = params.projectId
        def projectInstance = Project.findById( projectId )
        def movebundleInstance = MoveBundle.findById(bundleId)
       	def assetEntityList = AssetEntity.findAllByMoveBundle(movebundleInstance)
       	def racks = []
    	assetEntityList.each {
           	racks <<[id:it.sourceRack, name:it.sourceRack]    		
    	}
    	render racks as JSON
    }
    //RackLayout Report
    def rackLayoutReport = {
      if(params.moveBundle == "null") {        
        flash.message = " Please Select Bundles. "
        redirect( action:'getBundleListForReportDialog', params:[reportId: 'Rack Layout'] )
        } else {
    	def moveBundleInstance = MoveBundle.findById(params.moveBundle)
    	def reportFields = []
    	def assetEntityList
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
    	def projectId = currProj.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def bundleName = "All Bundles"
    	def partyGroupInstance = PartyGroup.get(projectInstance.id)
    	if( moveBundleInstance ) {
    		bundleName = moveBundleInstance?.name
    		assetEntityList = AssetEntity.findAll("from AssetEntity asset where asset.moveBundle = $moveBundleInstance.id order By asset.sourceTeam")
    	}else {	
    		assetEntityList = AssetEntity.findAll("from AssetEntity asset  order By asset.sourceTeam")
    	}
    	assetEntityList.each { 
      		 reportFields <<['asset_name':it.assetName , 'asset_tag':it.assetTag, "asset_type":it.assetType, "manufacturer":it.manufacturer, "model":it.model, "source_rack":it.sourceRack, "source_rack_position":it.sourceRackPosition, "usize":it.usize, "cart":it.cart, "shelf":it.shelf,"source_team_id":it.sourceTeam.id, "move_bundle_id":it.moveBundle.id, "clientName":projectInstance.client.name,
      		                 'projectName':partyGroupInstance.name, 'bundleName':bundleName, 'teamName':"", 'teamMembers':"",'location':"Source Team", 'rack':"SourceRack",'rackPos':"SourceRackPosition"]
      	}
    	chain(controller:'jasper',action:'index',model:[data:reportFields],params:params)
       }
    }
     
}
