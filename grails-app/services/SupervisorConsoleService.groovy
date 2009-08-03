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
        def rerackedId = stateEngineService.getStateId( "STD_PROCESS", "Reracked" )
        def stagedId = stateEngineService.getStateId( "STD_PROCESS", "Staged" )
        def unrackedId = stateEngineService.getStateId( "STD_PROCESS", "Unracked" )
        def releasedId = stateEngineService.getStateId( "STD_PROCESS", "Release" )
        def queryForConsole = new StringBuffer("select max(at.date_created) as dateCreated, ae.asset_entity_id as id, ae.priority, "+
						"ae.asset_tag as assetTag, ae.asset_name as assetName, ae.source_team_id as sourceTeam, " + 
						"ae.target_team_id as targetTeam, pm.current_state_id as currentState FROM asset_entity ae " +
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
			if(appOwner == "blank"){
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
				if(assetStatus == 'source_avail'){
					queryForConsole.append(" and pm.current_state_id >= $releasedId and pm.current_state_id < $unrackedId ")
				} else if(assetStatus == 'source_done'){
					queryForConsole.append(" and pm.current_state_id >= $unrackedId ")
				} else if(assetStatus == 'target_avail'){
					queryForConsole.append(" and pm.current_state_id >= $stagedId and pm.current_state_id < $rerackedId")
				} else if(assetStatus == 'target_done'){
					queryForConsole.append(" and pm.current_state_id >= $rerackedId ")
				}
			} else {
				queryForConsole.append(" and pm.current_state_id != 10 ")
			}
		}
		if(currentState){
			def stateId = stateEngineService.getStateIdAsInt( "STD_PROCESS", currentState )
			queryForConsole.append(" and pm.current_state_id = $stateId ")
		} else {
			if(type != 'hold'){
				queryForConsole.append(" and ( pm.current_state_id != 10 or pm.current_state_id is null ) ")
			} else {
				queryForConsole.append(" and pm.current_state_id = 10 ")
			}
		}
		queryForConsole.append(" group by ae.asset_entity_id " )
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
    	def assetsDetailsQuery = new StringBuffer("select if(a."+type+"_rack_position,a."+type+"_rack_position,1) as rackPosition, max(cast(if(a.usize != '0' and a.usize,a.usize,'1') as UNSIGNED INTEGER)) as usize, "+
    												"a.power_port as powerPort, nic_port as nicPort,remote_mgmt_port as remoteMgmtPort, "+
    												"CONCAT_WS(' / ',a.fiber_cabinet,a.hba_port ) as fiberCabinet,"+
    												"CONCAT_WS(' / ',a.kvm_device,a.kvm_port ) as kvmDevice,"+
													"count(a.asset_entity_id) as racksize, a.move_bundle_id as bundleId, "+
													"GROUP_CONCAT(CONCAT_WS(' - ',a.asset_tag,a.asset_name ) SEPARATOR '<br>') "+
													"as assetTag from asset_entity a where ")
    	if( bundleId && !includeOtherBundle){
    		assetsDetailsQuery.append(" a.move_bundle_id = $bundleId ")
    	} else {
    		assetsDetailsQuery.append(" a.project_id = $projectId ")
    	}
		if(rackRooms.size() == 3){
	    	if(rackRooms[0]){
				def location = rackRooms[0].replace("'","\\'")
				location = location.replace('"','\\"')
				assetsDetailsQuery.append(" and a."+type+"_location = '${location}' ")
			}
			if(rackRooms[1]){
				def room = rackRooms[1].replace("'","\\'")
				room =room.replace('"','\\"')
				assetsDetailsQuery.append(" and a."+type+"_room = '${room}' ")
			}
			if(rackRooms[2]){
				def rack = rackRooms[2].replace("'","\\'")
				rack = rack.replace('"','\\"')
				assetsDetailsQuery.append(" and a."+type+"_rack = '${rack}' ")
			}
		}
		assetsDetailsQuery.append(" group by a."+type+"_Rack_Position order by (rackPosition + usize - 1) desc")
		return assetsDetailsQuery 
    }
}
