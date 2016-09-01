import org.apache.shiro.SecurityUtils;

/*---------------------------------------
 * @author : Lokanath Reddy
 *--------------------------------------*/
class SupervisorConsoleService {
	def stateEngineService
    boolean transactional = true
	
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
