import org.apache.shiro.SecurityUtils;

/*---------------------------------------
 * @author : Lokanath Reddy
 *--------------------------------------*/
class SupervisorConsoleService {
	def stateEngineService
	def userPreferenceService
    boolean transactional = true
	protected static targetTeamType = ['MOVE_TECH':'target_team_id', 'CLEANER':'target_team_log_id','SYS_ADMIN':'target_team_sa_id',"DB_ADMIN":'target_team_dba_id']
	protected static sourceTeamType = ['MOVE_TECH':'source_team_id', 'CLEANER':'source_team_log_id','SYS_ADMIN':'source_team_sa_id',"DB_ADMIN":'source_team_dba_id']
	
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
		def projectId = params.projectId 
		projectId = projectId ? projectId : userPreferenceService.getSession().getAttribute( "CURR_PROJ" )?.CURR_PROJ
        def projectInstance = Project.findById( projectId )
        def workflowCode = projectInstance.workflowCode
        if(moveBundleInstance instanceof MoveBundle) {
            workflowCode = moveBundleInstance.workflowCode;
        }
		def role 
		if(filterTeam){
			role = ProjectTeam.read(filterTeam)?.role
		}
		if(!role){
			def subject = SecurityUtils.subject
			if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
				role = "SUPERVISOR"
			} else if(subject.hasRole("MANAGER")){
				role = "MANAGER"
			}
		}
		def releasedId = stateEngineService.getStateId( workflowCode, "Release" )
		def holdId = stateEngineService.getStateId( workflowCode, "Hold" )
        def cleanedId = stateEngineService.getStateId( workflowCode, "Cleaned" )
        def onCartId = stateEngineService.getStateId( workflowCode, "OnCart" )
        def onTruckId = stateEngineService.getStateId( workflowCode, "OnTruck" )
	    def offTruckId = stateEngineService.getStateId( workflowCode, "OffTruck" )
		def unrackedId = Integer.parseInt( stateEngineService.getStateId( workflowCode, "Unracked" ) )
		def rerackedId = Integer.parseInt( stateEngineService.getStateId( workflowCode, "Reracked" ) )
		def stagedId = stateEngineService.getStateId( workflowCode, "Staged" )
		
		def swimlane = Swimlane.findByNameAndWorkflow( role, Workflow.findByProcess(workflowCode) )
		
		def minSource = swimlane.minSource ? swimlane.minSource : "Release"
		def minSourceId = Integer.parseInt( stateEngineService.getStateId( workflowCode, minSource ) )
		def minTarget = swimlane.minTarget ? swimlane.minTarget : "Staged"
		def minTargetId = Integer.parseInt( stateEngineService.getStateId( workflowCode, minTarget ) )
		
		def maxSource = swimlane.maxSource ? swimlane.maxSource : "Unracked"
		def maxSourceId = Integer.parseInt( stateEngineService.getStateId( workflowCode, maxSource ) )
		def maxTarget = swimlane.maxTarget ? swimlane.maxTarget : "Reracked"
		def maxTargetId = Integer.parseInt( stateEngineService.getStateId( workflowCode, maxTarget ) )
        
		
		
        def queryForConsole = new StringBuffer("select max(at.date_created) as dateCreated, ae.asset_entity_id as id, ae.priority, "+
						"ae.asset_tag as assetTag, ae.asset_name as assetName,ae.asset_type as assetType, ae.source_team_id as sourceTeamMt, ae.target_team_id as " + 
						"targetTeamMt, pm.current_state_id as currentState, min(cast(at.state_to as UNSIGNED INTEGER)) as minstate FROM asset_entity ae " +
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
			def teamRole = ProjectTeam.findById(filterTeam).role
			if(assetLocation){
				if(assetLocation == "source"){
					queryForConsole.append(" and ae.${sourceTeamType.get(teamRole)} = $filterTeam ")
				} else if(assetLocation == "target"){
					queryForConsole.append(" and ae.${targetTeamType.get(teamRole)} = $filterTeam ")
				}
			} else {
				queryForConsole.append(" and ( ae.${sourceTeamType.get(teamRole)} = $filterTeam or ae.${targetTeamType.get(teamRole)} = $filterTeam ) ")
			}
		}
		
		if(assetStatus){
			if(type != 'hold'){
				
				switch( assetStatus ) {
				
					case "source_avail" 		: queryForConsole.append(" and pm.current_state_id >= $minSourceId and pm.current_state_id < $maxSourceId ")
										  		  break;
					case "source_done"  		: queryForConsole.append(" and pm.current_state_id >= $maxSourceId ")
										  		  break;
					case "target_avail" 		: queryForConsole.append(" and pm.current_state_id >= $minTargetId and pm.current_state_id < $maxTargetId")
										          break;
					case "target_done"  		: queryForConsole.append(" and pm.current_state_id >= $maxTargetId ")
										          break;
					case "source_pend"  		: queryForConsole.append(" and (pm.current_state_id < $minSourceId or pm.current_state_id is null) ")
					  					          break;
					case "target_pend"  		: queryForConsole.append(" and (pm.current_state_id < $minTargetId or pm.current_state_id is null) ")
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
			def stateId = stateEngineService.getStateIdAsInt( workflowCode, currentState )
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
			queryForConsole.append(" order by dateCreated desc ")
		}
        return queryForConsole.toString()
    }
	 /*----------------------------------------
     * @author : Lokanath Reddy
     * @param  : move bundle and request params
     * @return : Query for Rack Elevation  
     *----------------------------------------*/
    def getQueryForRackElevation( def bundleId, def projectId, def includeOtherBundle, def rackRooms, def type ) {
    	def assetsDetailsQuery = new StringBuffer("select asset_entity_id as assetEntityId, if(a."+type+"_rack_position,a."+type+"_rack_position,0) as rackPosition, max(cast(if(m.usize,m.usize,'0') as UNSIGNED INTEGER)) as usize, "+
													"count(a.asset_entity_id) as racksize, a.move_bundle_id as bundleId, "+
													"GROUP_CONCAT(CONCAT_WS(' - ',a.asset_tag,a.asset_name ) SEPARATOR '<br/>') "+
													"as assetTag from asset_entity a left join model m on m.model_id = a.model_id where ")
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
		assetsDetailsQuery.append(" group by a."+type+"_Rack_Position order by ( rackPosition + max(m.usize) ) desc, rackPosition desc")
		return assetsDetailsQuery 
    }
}
