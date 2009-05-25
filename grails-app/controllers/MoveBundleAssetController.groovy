import grails.converters.JSON
import org.jsecurity.SecurityUtils
class MoveBundleAssetController {
	def partyRelationshipService
	def assetEntityAttributeLoaderService
	def userPreferenceService
    
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
    	
    	def bundleId = params.moveBundle
    	if( bundleId ){
    		userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
    	} else {
    		bundleId = getSession().getAttribute("CURR_BUNDLE").CURR_BUNDLE
    	}
    	def bundleInstance = MoveBundle.findById(bundleId)
    	/*def moveBundleList = MoveBundle.findAllByProject(bundleInstance.project)
    	println"http://jira.tdsops.com:8080/browse/TM-138------------------------------>"+moveBundleList.name*/
    	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.teamCode != 'Cleaning' and pt.teamCode != 'Transport' " )
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
    		def unAssignCount = AssetEntity.countByMoveBundleAndTargetTeam( bundleInstance, null )
			teamAssetCounts << [ teamCode: "UnAssigned" , assetCount:unAssignCount ]
    		def cartList = AssetEntity.findAll("from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.cart")
			cartAssetCounts = assetEntityAttributeLoaderService.getCartAssetCounts(bundleId)
    		render(view:'bundleTeamAssignment',model:[assetEntityInstanceList: assetEntityList, moveBundleInstance:bundleInstance, projectTeamInstance:projectTeamInstanceList, teamAssetCount:teamAssetCounts, assetEntitysRacks:assetEntitysRacks, rack:rackPlan, cartAssetCountList:cartAssetCounts ])
    	}else 
    	{
    		rackPlan = "UnrackPlan"
    		assetEntitysRacks = AssetEntity.findAll("from AssetEntity ma  where ma.moveBundle = $bundleInstance.id group by ma.sourceRack") 
    		projectTeamInstanceList.each{projectTeam ->
				def assetCount = AssetEntity.countByMoveBundleAndSourceTeam( bundleInstance, projectTeam )
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		def unAssignCount = AssetEntity.countByMoveBundleAndSourceTeam( bundleInstance, null )
			teamAssetCounts << [ teamCode: "UnAssigned" , assetCount:unAssignCount ]
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
    	def teamId= null
    	def teamName = ""
    	def bundleInstance = MoveBundle.findById(bundleId)
    	if( team == "UnAssign") {
    		def modifiedAssetList = []
    		for( int assetRow = 0; assetRow < assets.size(); assetRow ++ ) {
				def assetEntityInstance = AssetEntity.findById(assets[ assetRow ] )
				if( rackPlan == "UnrackPlan" ) {
					assetEntityInstance.sourceTeam = null
				}else {
					assetEntityInstance.targetTeam = null
				}
				assetEntityInstance.save()
				modifiedAssetList.add(assetEntityInstance)
				assetEntityList = modifiedAssetList
				assetList = assetEntityAttributeLoaderService.getAssetList( assetEntityList, rackPlan, bundleInstance)
				teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleId, rackPlan )
			}
    	}else {
    		def modifiedAssetList = []
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
    				modifiedAssetList.add(assetEntityInstance)
    			}
    		}
    		assetEntityList = modifiedAssetList
			assetList = assetEntityAttributeLoaderService.getAssetList( assetEntityList, rackPlan, bundleInstance)
			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleId, rackPlan )
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
    	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.teamCode != 'Cleaning' and pt.teamCode != 'Transport' " )
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
       	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.teamCode != 'Cleaning' and pt.teamCode != 'Transport' " )
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
		cartAssetCounts = assetEntityAttributeLoaderService.getCartAssetCounts(bundleId)
	    render cartAssetCounts as JSON
	}
	//Updating #Shelf for Selected Asset
	def assetShelfAssign = {
		def shelf = params.shelfNumber
	    def asset = params.asset
	    def bundleId = params.bundleId
	    def teamAssetCounts = []
	    def bundleInstance = MoveBundle.findById(bundleId)
	    if( shelf != null )
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
        } else if( reportId == "cart Asset" ){
        	render(view:'cartAssetReport',model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
        }else if( reportId == 'Issue Report' ){
        	render(view:'issueReport',model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
        }else if(reportId == 'Transportation Asset List') {
        	render(view:'transportationAssetReport',model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
        }else {
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
    //Generate a TeamSheet jasper Report  
    def teamSheetReport = {
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
    	def projectId = currProj.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def partyGroupInstance = PartyGroup.get(projectInstance.id)
    	//if no Bundle selected	
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
    		if(params.teamFilter != "null"){
   	   			projectTeamInstance = ProjectTeam.findById( params.teamFilter )
   	   		}
    		//if moveBundleinstance is selected (single moveBundle)
    		if( moveBundleInstance ) {
    			bundleName = moveBundleInstance?.name
    			//if Projectteam and moveBundle both selected
    			if( projectTeamInstance ) {
    				teamName = projectTeamInstance?.teamCode
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.sourceTeam = $projectTeamInstance.id order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.targetTeam = $projectTeamInstance.id order By asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.sourceTeam = $projectTeamInstance.id order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.targetTeam = $projectTeamInstance.id order By asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}else {
    				//source Location selected
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.sourceTeam != null order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    				//target Location selected
    				else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.targetTeam != null order By asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    				//Location Both selected
    				else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.sourceTeam != null order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and asset.targetTeam != null order By asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}
    		}
    		//All Bundles Selected
    		else {
    			//team Selected
    			if( projectTeamInstance  ) {
    				teamName = projectTeamInstance?.teamCode
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.sourceTeam = $projectTeamInstance.id and asset.project.id = $projectInstance.id and asset.moveBundle != null order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.targetTeam = $projectTeamInstance.id and asset.project.id = $projectInstance.id and asset.moveBundle != null order By asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.sourceTeam = $projectTeamInstance.id  and asset.project.id = $projectInstance.id and asset.moveBundle != null order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.targetTeam = $projectTeamInstance.id and asset.project.id = $projectInstance.id and asset.moveBundle != null order By asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}
    			//Team MoveBundle Both not selected (moveBundle="AllBundles)
    			else {
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id and asset.moveBundle != null and asset.sourceTeam != null order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset where asset.project.id = $projectInstance.id and asset.moveBundle != null and asset.targetTeam != null order By asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id and asset.moveBundle != null and asset.sourceTeam != null order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id and asset.moveBundle != null and asset.targetTeam != null order By asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}
    		}
    		//Source List of Assets
    		assetEntityList.each { asset ->
    			def bundleInstance
    			if(asset.moveBundle != null) {
    				bundleInstance = MoveBundle.findById(asset.moveBundle.id)
    			}
    			def teamPartyGroup 
    			def projectTeamLocationInstance
    			if( asset.sourceTeam != null ) {
    				teamPartyGroup = PartyGroup.findById(asset.sourceTeam.id)
    				projectTeamLocationInstance = ProjectTeam.findById(asset.sourceTeam.id)
    			}
    			def assetCommentList = AssetComment.findAllByAssetEntity(asset)
    			def assetCommentString =""
    			assetCommentList.each { assetComment ->
    				assetCommentString = assetCommentString + assetComment.comment +"\n"
   			 	}
    			def teamMembers = partyRelationshipService.getTeamMemberNames(asset.sourceTeam?.id) 
   			 	reportFields <<['assetName':asset.assetName , 'assetTag':asset.assetTag, "assetType":asset.assetType, "manufacturer":asset.manufacturer, "model":asset.model, "sourceTargetrack":asset.sourceRack, "position":asset.sourceRackPosition, "sourceTargetPos":(projectTeamLocationInstance?.currentLocation ? projectTeamLocationInstance?.currentLocation : "") +"(source/ unracking)", "usize":asset.usize, "cart":asset.cart, "shelf":asset.shelf,"source_team_id":asset?.sourceTeam?.id, "move_bundle_id":asset?.moveBundle?.id, "clientName":projectInstance?.client?.name,
   			                 'projectName':partyGroupInstance?.name,'startAt':projectInstance?.startDate, 'completedAt':projectInstance?.completionDate, 'bundleName':bundleInstance?.name, 'teamName':teamPartyGroup?.name +"-"+teamMembers, 'teamMembers':teamMembers,'location':"Source Team", 'rack':"SourceRack",'rackPos':"SourceRackPosition",'truck':asset.truck, 'room':asset.sourceRoom, 'PDU':asset.powerPort,'NIC':asset.nicPort,'kvmPort':asset.kvmDevice ? asset.kvmDevice : '' + asset.kvmPort ? asset.kvmPort :'', 'hbaPort':asset.fiberCabinet + asset.hbaPort, 'instructions':assetCommentString, 'sourcetargetLoc':"s"]
    		}
    		//Target List of Assets
    		targetAssetEntitylist.each { asset ->
                def bundleInstance
                if(asset.moveBundle != null) {
                    bundleInstance = MoveBundle.findById(asset.moveBundle.id)
                }
   				def teamPartyGroup 
   				def projectTeamLocationInstance
   				def teamMembers
   				if(asset.targetTeam != null ) {
   					teamPartyGroup = PartyGroup.findById(asset.targetTeam.id)
   					projectTeamLocationInstance = ProjectTeam.findById(asset.targetTeam.id)
   					teamMembers = partyRelationshipService.getTeamMemberNames(asset.targetTeam.id) 
   				}
   				def assetCommentList = AssetComment.findAllByAssetEntity(asset)
   				def assetCommentString =""
   				assetCommentList.each { assetComment ->
   					assetCommentString = assetCommentString + assetComment?.comment +"\n"
   				}
   				reportFields <<['assetName':asset.assetName , 'assetTag':asset.assetTag, "assetType":asset.assetType, "manufacturer":asset.manufacturer, "model":asset.model, "sourceTargetrack":asset.targetRack, "position":asset.targetRackPosition, "sourceTargetPos":(projectTeamLocationInstance?.currentLocation ? projectTeamLocationInstance?.currentLocation : "") +"(target/ reracking)", "usize":asset.usize, "cart":asset.cart, "shelf":asset.shelf,"source_team_id":asset?.targetTeam?.id, "move_bundle_id":asset?.moveBundle?.id, "clientName":projectInstance?.client?.name,
  			                 'projectName':partyGroupInstance?.name,'startAt':projectInstance?.startDate, 'completedAt':projectInstance?.completionDate, 'bundleName':bundleInstance?.name, 'teamName':teamPartyGroup?.name +"-"+teamMembers, 'teamMembers':teamMembers,'location':"Target Team", 'rack':"TargetRack",'rackPos':"TargetRackPosition",'truck':asset.truck,'room':asset.targetRoom,'PDU':asset.powerPort,'NIC':asset.nicPort, 'kvmPort':asset.kvmDevice ? asset.kvmDevice : '' + asset.kvmPort ? asset.kvmPort : '', 'hbaPort':asset.fiberCabinet ? asset.fiberCabinet : '' + asset.hbaPort ? asset.hbaPort : '','instructions':assetCommentString,'sourcetargetLoc':"t"]
    		}
    		//No assets were found for selected MoveBundle,Team and Location
    		if(reportFields.size() <= 0) {    		
    			flash.message = " No Assets Were found for  selected values  "
    			redirect( action:'getBundleListForReportDialog', params:[reportId: 'Team Worksheets'] )
    		}else {
    			chain(controller:'jasper',action:'index',model:[data:reportFields],params:params)
    		}
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
	//cart Asset report
	def cartAssetReport = {
    	def reportName = params.reportName
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
    	def projectId = currProj.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def partyGroupInstance = PartyGroup.get(projectInstance.id)
    	def teamPartyGroup
    	// if no moveBundle was selected
    	if(params.moveBundle == "null") {
            flash.message = " Please Select Bundles. "
            if(reportName == 'cartAsset') {
            	redirect( action:'getBundleListForReportDialog', params:[reportId: 'cart Asset'] )
            }else {
            	redirect( action:'getBundleListForReportDialog', params:[reportId: 'Transportation Asset List'] )
            }
        } else {
            def moveBundleInstance = MoveBundle.findById(params.moveBundle)
            def reportFields = []
            def bundleName = "All Bundles"
            def teamName = "All Teams"
            def assetEntityList
            def targetAssetEntitylist
            //if moveBundleinstance is selected (single moveBundle)
            if( moveBundleInstance ) {
                bundleName = moveBundleInstance?.name
                assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id  order By asset.moveBundle,asset.cart,asset.shelf")
            }
            //All Bundles Selected
            else {
       			assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id and asset.moveBundle != null order By asset.moveBundle,asset.cart,asset.shelf")
       		}
       		//Source AssetList 
       		if( assetEntityList != null) {
       			assetEntityList.each { asset ->
       				def bundleInstance
       				if(asset.moveBundle != null) {
       					bundleInstance = MoveBundle.findById(asset.moveBundle.id)
       				}
                    if(reportName == 'cartAsset') {
                        teamPartyGroup =  ProjectTeam.findBymoveBundleAndTeamCode(bundleInstance, 'Cleaning')
                    }else {
       					teamPartyGroup =  ProjectTeam.findBymoveBundleAndTeamCode(bundleInstance, 'Transport')
                    }
       				def assetCommentList = AssetComment.findAllByAssetEntity(asset)
       				def moveTeamName
       				if(teamPartyGroup != null ){
       					def moveteamInstance = PartyGroup.findById(teamPartyGroup.id)
       					moveTeamName = moveteamInstance?.name
       				}
       				def assetCommentString =""
       				assetCommentList.each { assetComment ->
       					assetCommentString = assetCommentString + assetComment.comment +"\n"
       				}
       				def cartShelf = (asset.cart ? asset.cart : "")+"/"+ (asset.shelf ? asset.shelf : "")
       				if (cartShelf == "/"){
       					cartShelf = ""
       				}
       				def teamMembers = partyRelationshipService.getTeamMemberNames(teamPartyGroup?.id) 
       				reportFields <<['assetName':asset.assetName , "model":asset.model, "sourceTargetPos":(teamPartyGroup?.currentLocation ? teamPartyGroup?.currentLocation : "") +"(source/ unracking)", "cart":cartShelf, "shelf":asset.shelf,"source_team_id":teamPartyGroup?.id, "move_bundle_id":asset?.moveBundle?.id,dlocation:asset.sourceLocation,
       			                 'projectName':partyGroupInstance?.name,'startAt':projectInstance?.startDate, 'completedAt':projectInstance?.completionDate, 'bundleName':bundleInstance?.name, 'teamName':teamPartyGroup?.teamCode ? teamPartyGroup?.name+" - "+teamMembers : "", 'teamMembers':teamMembers,'location':"Source Team", 'truck':asset.truck, 'room':asset.sourceRoom,'moveTeam':asset?.sourceTeam?.name, 'instructions':assetCommentString, 'sourcetargetLoc':"s", 'usize':asset.usize]
       			}
       		}
       		//No Assets were found for selected moveBundle,team and Location
       		if(reportFields.size() <= 0) {    		
       			flash.message = " No Assets Were found for  selected values  "
       			if(reportName == 'cartAsset') {
       				redirect( action:'getBundleListForReportDialog', params:[reportId: 'cart Asset'] )
        		}else {
        			redirect( action:'getBundleListForReportDialog', params:[reportId: 'Transportation Asset List'] )
        		}
        	}else {
        		chain(controller:'jasper',action:'index',model:[data:reportFields],params:params)
        	}
        }
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
                reportFields <<['asset_name':it.assetName , 'asset_tag':it.assetTag, "asset_type":it.assetType, "manufacturer":it.manufacturer, "model":it.model, "source_rack":it.sourceRack, "source_rack_position":it.sourceRackPosition, "usize":it.usize, "cart":it.cart, "shelf":it.shelf,"source_team_id":it.sourceTeam?.id, "move_bundle_id":it.moveBundle?.id, "clientName":projectInstance?.client?.name,
      		                 'projectName':partyGroupInstance?.name, 'bundleName':bundleName, 'teamName':"", 'teamMembers':"",'location':"Source Team", 'rack':"SourceRack",'rackPos':"SourceRackPosition",'usize':it.usize]
            }
            chain(controller:'jasper',action:'index',model:[data:reportFields],params:params)
        }
    }
	/*
	 * Generate Issue Report
	 */
	def issueReport = {
    	def subject = SecurityUtils.subject
        def principal = subject.principal
    	def personInstance = Person.findByFirstName( principal )
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
    	def projectId = currProj.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def partyGroupInstance = PartyGroup.get(projectInstance.id)
    	def bundleNames = ""
    	def reportFields = []
    	if(params.moveBundle == "null") {    		
    		flash.message = " Please Select Bundles. "
    		redirect( action:'getBundleListForReportDialog', params:[reportId: 'Issue Report'] )
        }
    	else {
    		def moveBundleInstance = MoveBundle.findById(params.moveBundle)
    		def bundleName = "All Bundles"
    		def assetCommentList
    		def targetAssetEntitylist
    		if( moveBundleInstance != null ){
    			assetCommentList = AssetComment.findAll("from AssetComment ac where ac.assetEntity.id in(select ae.id from AssetEntity ae where ae.moveBundle.id = $moveBundleInstance.id ) and ac.commentType= 'issue' order by ac.assetEntity.assetName")
    			bundleNames = moveBundleInstance?.name
    		}else {
    			assetCommentList = AssetComment.findAll("from AssetComment ac where ac.assetEntity.id in(select ae.id from AssetEntity ae where ae.project.id = $projectInstance.id ) and ac.commentType= 'issue' order by ac.assetEntity.assetName")
    			bundleNames = "All"
        	}
    		assetCommentList.each { assetComment ->
    			def createdBy
    			def assetTransitionInstance = AssetTransition.findByAssetEntity(assetComment.assetEntity)
    			if ( assetTransitionInstance?.projectTeam != null ){
    				def createdParty = PartyGroup.findById(assetTransitionInstance.projectTeam.id)
    				createdBy = createdParty?.name
    			}else if ( assetTransitionInstance?.userLogin != null ){
    				def createdParty = Person.findById(assetTransitionInstance.userLogin.id)
    				createdBy = createdParty?.firstName ? createdParty?.firstName : ""+" "+createdParty?.lastName ? createdParty?.lastName : ""
    			}
    			reportFields <<['assetName':assetComment?.assetEntity?.assetName, 'assetTag':assetComment?.assetEntity?.assetTag, 'serialNumber':assetComment?.assetEntity?.serialNumber,'model':assetComment?.assetEntity?.model, 'occuredAt':assetComment?.dateCreated, 'createdBy':createdBy, 'issue':assetComment?.comment, 'bundleNames':bundleNames,'projectName':partyGroupInstance?.name, 'clientName':projectInstance?.client?.name]
    			if(	assetComment.isResolved == 1 ) {
    				reportFields <<['assetName':null, 'assetTag':null, 'serialNumber':null,'model':null, 'occuredAt':assetComment?.dateResolved, 'createdBy':assetComment?.resolvedBy?.firstName+" "+assetComment?.resolvedBy?.lastName, 'issue':assetComment?.resolution, 'bundleNames':bundleNames,'projectName':partyGroupInstance?.name, 'clientName':projectInstance?.client?.name]
    			}
    		}
    		if(reportFields.size() <= 0) {    		
        		flash.message = " No Issues Were found for  selected values  "
        		redirect( action:'getBundleListForReportDialog', params:[reportId: 'Issue Report'] )
        	}else {
        		chain(controller:'jasper',action:'index',model:[data:reportFields],params:params)
        	}
        }
    }
}
