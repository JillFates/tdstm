/*---------------------------------------
 * @author : Lokanath Reddy
 *--------------------------------------*/
class SupervisorConsoleService {
	def stateEngineService
    boolean transactional = true
    /*----------------------------------------
     * @author : Lokanath Reddy
     * @param  : move bundle and request params
     * @return : Query for Supervisor console  
     *----------------------------------------*/
    def getQueryForConsole( def moveBundleInstance, def params, def type ) {
    	// filter params
        def application = params.application
        def currentState = params.currentState
        def appOwner = params.appOwner
        def appSme = params.appSme
        def filterTeam = params.team
        def assetLocation = params.assetLocation
        def assetStatus = params.assetStatus
        def sortField = params.sort
        def orderField = params.order
        def holdCheck = true
        def projectInstance = Project.findById( params.projectId )
        def cleanedId = stateEngineService.getStateId( projectInstance.workflowCode, "Cleaned" )
        def onCartId = stateEngineService.getStateId( projectInstance.workflowCode, "OnCart" )
        def onTruckId = stateEngineService.getStateId( projectInstance.workflowCode, "OnTruck" )
	    def offTruckId = stateEngineService.getStateId( projectInstance.workflowCode, "OffTruck" )
        def rerackedId = stateEngineService.getStateId( projectInstance.workflowCode, "Cabled" )
        if(!rerackedId ) {
        	rerackedId = stateEngineService.getStateId( projectInstance.workflowCode, "Reracked" )
        }
        def stagedId = stateEngineService.getStateId( projectInstance.workflowCode, "Staged" )
        def unrackedId = stateEngineService.getStateId( projectInstance.workflowCode, "Unracked" )
        def releasedId = stateEngineService.getStateId( projectInstance.workflowCode, "Release" )
        def holdId = stateEngineService.getStateId( projectInstance.workflowCode, "Hold" )
        def queryForConsole = new StringBuffer("select max(at.date_created) as dateCreated, ae.asset_entity_id as id, ae.priority, "+
						"ae.asset_tag as assetTag, ae.asset_name as assetName, ae.source_team_id as sourceTeam, ae.target_team_id as " + 
						"targetTeam, pm.current_state_id as currentState, min(cast(at.state_to as UNSIGNED INTEGER)) as minstate FROM asset_entity ae " +
						"LEFT JOIN asset_transition at ON ( at.asset_entity_id = ae.asset_entity_id and at.voided = 0 ) " + 
						"LEFT JOIN project_asset_map pm ON (pm.asset_id = ae.asset_entity_id) " + 
						"where ae.project_id = ${moveBundleInstance.project.id} and ae.move_bundle_id = ${moveBundleInstance.id} ")
		if(application){
			if(application == "blank"){
				queryForConsole.append(" and ae.application = '' ")
			} else {
				queryForConsole.append(" and ae.application = '$application' ")
			}
		}
		if(appOwner){
			if(appOwner == "blank"){
				queryForConsole.append(" and ae.app_owner = '' ")
			} else {
				queryForConsole.append(" and ae.app_owner = '$appOwner' ")
			}
		}
		if(appSme){
			if(appSme == "blank"){
				queryForConsole.append(" and ae.app_sme = '' ")
			} else {
				queryForConsole.append(" and ae.app_sme = '$appSme' ")
			}
		}
		if(filterTeam){
			if(assetLocation){
				if(assetLocation == "source"){
					queryForConsole.append(" and ae.source_team_id = $filterTeam ")
				} else if(assetLocation == "target"){
					queryForConsole.append(" and ae.target_team_id = $filterTeam ")
				}
			} else {
				queryForConsole.append(" and ( ae.source_team_id = $filterTeam or ae.target_team_id = $filterTeam ) ")
			}
		}
		
		if(assetStatus){
			if(type != 'hold'){
				
				switch( assetStatus ) {
				
					case "source_avail" 		: queryForConsole.append(" and pm.current_state_id >= $releasedId and pm.current_state_id < $unrackedId ")
										  		  break;
					case "source_done"  		: queryForConsole.append(" and pm.current_state_id >= $unrackedId ")
										  		  break;
					case "target_avail" 		: queryForConsole.append(" and pm.current_state_id >= $stagedId and pm.current_state_id < $rerackedId")
										          break;
					case "target_done"  		: queryForConsole.append(" and pm.current_state_id >= $rerackedId ")
										          break;
					case "source_pend"  		: queryForConsole.append(" and (pm.current_state_id < $releasedId or pm.current_state_id is null) ")
					  					          break;
					case "target_pend"  		: queryForConsole.append(" and (pm.current_state_id < $stagedId or pm.current_state_id is null) ")
					  					          break;
					case "source_pend_clean"  	: queryForConsole.append(" and (pm.current_state_id < $unrackedId or pm.current_state_id is null) ")
					  							  break;
					case "source_avail_clean"  	: queryForConsole.append(" and pm.current_state_id = $unrackedId")
					  							  break;
					case "source_done_clean"  	: queryForConsole.append(" and pm.current_state_id >= $cleanedId")
					  							  break;
					case "source_pend_trans"  	: queryForConsole.append(" and (pm.current_state_id < $cleanedId or pm.current_state_id is null) ")
					  							  break;
					case "source_avail_trans"   : queryForConsole.append(" and pm.current_state_id = $cleanedId")
					  							  break;
					case "source_done_trans"    : queryForConsole.append(" and pm.current_state_id >= $onCartId")
					  							  break;
					case "target_pend_trans"    : queryForConsole.append(" and (pm.current_state_id < $onTruckId or pm.current_state_id is null) ")
					  							  break;
					case "target_avail_trans"   : queryForConsole.append(" and pm.current_state_id >= $onTruckId and pm.current_state_id < $offTruckId")
												  break;
					case "target_done_trans"    : queryForConsole.append(" and pm.current_state_id >= $stagedId")
												  break;
				}
			}
		}
		if(currentState){
			def stateId = stateEngineService.getStateIdAsInt( projectInstance.workflowCode, currentState )
			if(currentState != 'Hold'){
				queryForConsole.append(" and pm.current_state_id = $stateId group by ae.asset_entity_id having minstate != $holdId" )
			} else {
				queryForConsole.append(" group by ae.asset_entity_id having minstate = $stateId")
			}
		} else {
			if(type != 'hold'){
				queryForConsole.append(" group by ae.asset_entity_id having (minstate != $holdId or minstate is null) ")
			} else {
				queryForConsole.append(" group by ae.asset_entity_id having minstate = $holdId")
			}
		}
		//queryForConsole.append(" group by ae.asset_entity_id " )
		if( sortField ) {
			queryForConsole.append(" order by ${sortField} ${orderField}" )
		} else {
			queryForConsole.append(" order by date_created desc ")
		}
		return queryForConsole.toString()
    }
	 /*----------------------------------------
     * @author : Lokanath Reddy
     * @param  : move bundle and request params
     * @return : Query for Rack Elevation  
     *----------------------------------------*/
    def getQueryForRackElevation( def bundleId, def projectId, def includeOtherBundle, def rackRooms, def type ) {
    	def assetsDetailsQuery = new StringBuffer("select if(a."+type+"_rack_position,a."+type+"_rack_position,0) as rackPosition, max(cast(if(a.usize,a.usize,'0') as UNSIGNED INTEGER)) as usize, "+
    												"a.pdu_port as pduPort, nic_port as nicPort,remote_mgmt_port as remoteMgmtPort, "+
    												"CONCAT_WS(' / ',IFNULL(a.fiber_type,'blank'),IFNULL(a.fiber_cabinet,'blank'),IFNULL(a.hba_port,'blank') ) as fiberCabinet,"+
    												"CONCAT_WS(' / ',IFNULL(a.kvm_device,'blank'),IFNULL(a.kvm_port,'blank') ) as kvmDevice,"+
													"count(a.asset_entity_id) as racksize, a.move_bundle_id as bundleId, "+
													"GROUP_CONCAT(CONCAT_WS(' - ',a.asset_tag,a.asset_name ) SEPARATOR '<br>') "+
													"as assetTag from asset_entity a where ")
    	if( bundleId && !includeOtherBundle){
    		assetsDetailsQuery.append(" a.move_bundle_id = $bundleId ")
    	} else {
    		assetsDetailsQuery.append(" a.project_id = $projectId ")
    	}
    	assetsDetailsQuery.append(" and a.asset_type NOT IN ('VM', 'Blade')  ")
		if(rackRooms.size() == 3){
	    	if(rackRooms[0] != "blank"){
				def location = rackRooms[0].replace("'","\\'")
				location = location.replace('"','\\"')
				assetsDetailsQuery.append(" and a."+type+"_location = '${location}' ")
			} else {
				assetsDetailsQuery.append(" and (a."+type+"_location is null or a."+type+"_location = '') ")
			}
			if(rackRooms[1] != "blank"){
				def room = rackRooms[1].replace("'","\\'")
				room =room.replace('"','\\"')
				assetsDetailsQuery.append(" and a."+type+"_room = '${room}' ")
			}else {
				assetsDetailsQuery.append(" and (a."+type+"_room is null or a."+type+"_room = '') ")
			}
			if(rackRooms[2]){
				def rack = rackRooms[2].replace("'","\\'")
				rack = rack.replace('"','\\"')
				assetsDetailsQuery.append(" and a."+type+"_rack = '${rack}' ")
			}
		}
		assetsDetailsQuery.append(" group by a."+type+"_Rack_Position order by ( rackPosition + max(usize) ) desc, rackPosition desc")
		return assetsDetailsQuery 
    }
}
