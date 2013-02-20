import grails.converters.JSON
import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetEntity;

import java.text.SimpleDateFormat
class CartTrackingController {
	def userPreferenceService
	def jdbcTemplate
	def stateEngineService
	def workflowService
	/*---------------------------------
	 * default Index method
	 *---------------------------------*/
    def index = { 
    	redirect(action:cartTracking,params:params)
    }
	/*---------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : bundle and projectId
	 * @return : Asset Details  On Cart and Truck 
	 *---------------------------------------------------------*/
	def cartTracking = {
		def cartAction = params.cartAction
    	def moveBundleInstance
    	def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
    	def bundleId = params.moveBundle
    	def projectInstance = Project.findById( projectId )
    	def allCartTrackingDetails = []
    	def pendingCartTrackingDetails = []
		def cartTrackingDetails = []
        def moveBundleInstanceList = MoveBundle.findAll("from MoveBundle b where b.project = $projectId order by b.name asc")
        if(bundleId){
        	userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
            moveBundleInstance = MoveBundle.findById(bundleId)
        } else {
            userPreferenceService.loadPreferences("CURR_BUNDLE")
            def defaultBundle = getSession().getAttribute("CURR_BUNDLE")
            if(defaultBundle?.CURR_BUNDLE){
            	moveBundleInstance = MoveBundle.findById(defaultBundle.CURR_BUNDLE)
            	if( moveBundleInstance?.project?.id != Integer.parseInt(projectId) ){
            		moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
            	}
            } else {
            	moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
            }
        }
		// get id's for Cleaned and OnTrucks
		def cleanedId = stateEngineService.getStateIdAsInt(projectInstance.workflowCode,"Cleaned")
		def onTruckId = stateEngineService.getStateIdAsInt(projectInstance.workflowCode,"OnTruck")
		def holdId = stateEngineService.getStateIdAsInt(projectInstance.workflowCode,"Hold")
		// query for list of carts and trucks
    	def query = new StringBuffer("""select ae.truck as truck , ae.cart as cart, count(ae.asset_entity_id) as totalAssets,
    								sum(m.usize) as usize from asset_entity ae 
									left join project_asset_map pm on (pm.asset_id = ae.asset_entity_id ) 
    								left join model m on m.model_id = ae.model_id
    								where ae.project_id = ${projectInstance.id}	and ae.move_bundle_id = ${moveBundleInstance.id} 
    								and ae.asset_type != 'VM' group by ae.cart """)
    	def resultList = jdbcTemplate.queryForList( query.toString() )
    	// iterate the carts details for completed and pending assets
    	resultList.each{
			def completed = true
			def pendingAssets = 0
			def completedAssets = 0
    		def assetQuery = "select ae.asset_entity_id as id, max(cast(at.state_to as UNSIGNED INTEGER)) as maxstate from asset_entity ae left join "+
							"asset_transition at on (at.asset_entity_id = ae.asset_entity_id and at.voided = 0 and at.type = 'process') where ae.project_id = ${projectInstance.id} "+
							"and ae.move_bundle_id = ${moveBundleInstance.id} and ae.cart = '$it.cart' and ae.asset_type != 'VM' group by ae.asset_entity_id"
    		def assetTransition = jdbcTemplate.queryForList(assetQuery)
    		assetTransition.each{
    			def currentState = 0
    			def projectAssetMap = ProjectAssetMap.find( "from ProjectAssetMap where asset = $it.id" )
    			if(projectAssetMap){
    				currentState = projectAssetMap.currentStateId
    			}
				if(it.maxstate < onTruckId ){
						completed = false
				}
				if(it.maxstate >= cleanedId && currentState != holdId){
					completedAssets +=1
				}
			}
    		pendingAssets = it.totalAssets - completedAssets			
			if(!completed){
				pendingCartTrackingDetails << [ cartDetails:it, completed:completed, pendingAssets:pendingAssets ]
			}
			allCartTrackingDetails  << [ cartDetails:it, completed:completed, pendingAssets:pendingAssets ]
    	}
		if(cartAction == "allId" ){
			cartTrackingDetails = allCartTrackingDetails
		} else {
			cartTrackingDetails = pendingCartTrackingDetails
		}
    	userPreferenceService.loadPreferences("CART_TRACKING_REFRESH")
		def timeToRefresh = getSession().getAttribute("CART_TRACKING_REFRESH")
		// select distinct trucks 
		def trucks = jdbcTemplate.queryForList("select distinct truck from asset_entity where project_id = ${projectInstance.id} "+
												"and move_bundle_id = ${moveBundleInstance.id} ")
        return [projectId:projectId, moveBundleInstanceList:moveBundleInstanceList, 
			moveBundleInstance:moveBundleInstance, timeToRefresh : timeToRefresh ? timeToRefresh.CART_TRACKING_REFRESH : "never",
			cartTrackingDetails : cartTrackingDetails, cartAction:cartAction, trucks : trucks, myForm : params.myForm
		]
	}
	/*---------------------------------------------------------
	 * Will set user preference for CLIENT_CONSOLE_REFRESH time
	 * @author : Lokanath Reddy
	 * @param  : refresh time 
	 * @return : refresh time 
	 *---------------------------------------------------------*/
	def setTimePreference = {
        def timer = params.timer
        def refreshTime =[]
        if(timer){
            userPreferenceService.setPreference( "CART_TRACKING_REFRESH", "${timer}" )
        }
        def timeToRefresh = getSession().getAttribute("CART_TRACKING_REFRESH")
        refreshTime <<[refreshTime:timeToRefresh]
        render refreshTime as JSON
	}
	/*---------------------------------------------------------
	 * Will update all the assets into selected Truck
	 * @author : Lokanath Reddy
	 * @param  : cart, truck, bundle and projectId
	 * @return : updated truck
	 *---------------------------------------------------------*/
	def changeTruck = {
		def updateQuery = "update asset_entity set truck = '$params.truck' where project_id = ${session.CURR_PROJ.CURR_PROJ} "+
						"and move_bundle_id = $params.bundleId and cart = '$params.cart' "
		jdbcTemplate.update(updateQuery)
		render params.truck
	}
	/*---------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : cart, truck, bundle and projectId
	 * @return : return all the assets which are on Cart
	 *---------------------------------------------------------*/
	def getAssetsOnCart = {
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def projectInstance = Project.findById( projectId )
		def bundleId = params.moveBundle
		def cart = params.cart
		def truck = params.truck
		def assetAction = params.assetAction
		def pendingAssetsOnCart = []
		def allAssetsOnCart = []
		def assetsOnCart = []
		def query = """select ae.asset_entity_id as id,ae.asset_tag as assetTag, ae.asset_name as assetName,
					mf.name as manufacturer, m.name as model, ae.source_team_id as source,
					max(cast(at.state_to as UNSIGNED INTEGER)) as maxstate 
					from asset_entity ae left join model m on (ae.model_id = m.model_id )
					left join manufacturer mf on (ae.manufacturer_id = mf.manufacturer_id )
					left join asset_transition at on (at.asset_entity_id = ae.asset_entity_id and at.voided = 0 and at.type = 'process') 
					where ae.project_id = $projectId and ae.move_bundle_id = $bundleId 
					and ae.cart = '$cart' and ae.asset_type != 'VM' group by ae.asset_entity_id"""
		def resultList = jdbcTemplate.queryForList( query )
		def cleanedId = stateEngineService.getStateIdAsInt(projectInstance.workflowCode,"Cleaned")
		def stagedId = stateEngineService.getStateIdAsInt(projectInstance.workflowCode,"Staged")
		resultList.each{
			def completed = true
			def checked = false
			def currentState = ""
			def projectAssetMap = ProjectAssetMap.find( "from ProjectAssetMap where asset = $it.id" )
			if(projectAssetMap){
				currentState = stateEngineService.getStateLabel(projectInstance.workflowCode,projectAssetMap.currentStateId)
			}
			def team = ""
			if(it.source){
				team = ProjectTeam.findById( it.source ).teamCode
			}
			
			if(it.maxstate >= cleanedId && it.maxstate < stagedId){
				checked = true
			} else if(it.maxstate < cleanedId ){
				completed = false
			}
			if(!completed){
				pendingAssetsOnCart << [ assetDetails:it, currentState:currentState, checked:checked,
				                         completed:completed, team:team, assetAction:assetAction, 
										 model : it.model ? it.model : "", manufacturer : it.manufacturer ? it.manufacturer : "" ]
			}
			allAssetsOnCart << [ assetDetails:it, currentState:currentState, checked:checked,
			                     completed:completed, team:team, assetAction:assetAction, 
								 model : it.model ? it.model : "", manufacturer : it.manufacturer ? it.manufacturer : "" ]
		}
		
		if(assetAction == "allAssetsId" ){
			assetsOnCart = allAssetsOnCart
		} else {
			assetsOnCart = pendingAssetsOnCart
		}
		render assetsOnCart as JSON
	}
	/*---------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : asset entity
	 * @return : return asset Details
	 *---------------------------------------------------------*/
	def getAssetDetails ={
		def assetDetails = []
		def assetId = params.assetId
		def assetEntity = AssetEntity.findById( assetId )
		def transition = jdbcTemplate.queryForList(" select max(cast(at.state_to as UNSIGNED INTEGER)) as maxstate "+
													"from asset_transition at where at.asset_entity_id = $assetEntity.id "+
													"and at.voided = 0 and at.type = 'process' group by at.asset_entity_id")
		def onTruckId = stateEngineService.getStateIdAsInt(assetEntity.project.workflowCode,"OnTruck")
		assetDetails<<[assetEntity:assetEntity,team:assetEntity.sourceTeamMt? assetEntity.sourceTeamMt.teamCode : "", 
		               state: transition[0] ? transition[0].maxstate : "", onTruck :onTruckId, 
		               model : assetEntity.model ? assetEntity.model.modelName: "", manufacturer : assetEntity.manufacturer ? it.manufacturer : ""  ]
		render assetDetails as JSON
	}
	/*---------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : cart, truck, bundle and projectId
	 * @return : update the asset details
	 *---------------------------------------------------------*/
	def reassignAssetOnCart = {
		def assetEntity = AssetEntity.get( params.assetId )
		assetEntity.properties = params
		if ( ! assetEntity.validate() || ! assetEntity.save() ) {
			def etext = "Unable to create asset $assetEntity" +
            GormUtil.allErrorsString( assetEntity )
			println etext
			log.error( etext )
		}
		render assetEntity as JSON
	}
	/*---------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : cart, truck, bundle and projectId
	 * @return : update the all the assets state to OnTruck
	 *---------------------------------------------------------*/
	def moveToOnTruck = {
		def startTime = System.currentTimeMillis()
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def projectInstance = Project.findById( projectId )
		def bundleId = params.moveBundle
		def cart = params.cart
		def truck = params.truck
		def status = ""
		def assetEntityList = AssetEntity.findAll(" from AssetEntity a where a.project = $projectId and "+
												"a.moveBundle = $bundleId and a.cart = '$cart' and a.assetType != 'VM'")
		assetEntityList.each{
			def transactionStatus
			def projectAssetMap = ProjectAssetMap.findByAsset( it )
			if(projectAssetMap){
				def currentStateId = projectAssetMap.currentStateId
				def principal = SecurityUtils.subject.principal
		    	def loginUser = UserLogin.findByUsername(principal)
		    	def currentState = stateEngineService.getState(projectInstance.workflowCode,currentStateId)
		    	if(currentState != "Hold"){
		    		transactionStatus = workflowService.createTransition(projectInstance.workflowCode,"SUPERVISOR", "OnTruck", it, it.moveBundle, loginUser, null, null )
		    	}
				status += ""+transactionStatus.success+":"+it.assetName +"~"
			}
		}
		
		/* Logging statement, which shows the elapsed time of the call */
		def endTime = System.currentTimeMillis()
		def dateFormat = new SimpleDateFormat("HH:mm:ss.SSS")
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
		def elapsedTime = dateFormat.format(new Date(endTime - startTime));
		log.debug("moveToOnTruck took $elapsedTime ")
		
		render status
	}
}
