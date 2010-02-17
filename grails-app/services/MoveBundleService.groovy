import org.springframework.dao.IncorrectResultSizeDataAccessException


class MoveBundleService {
	
	def jdbcTemplate
    
	boolean transactional = true
	
	/*----------------------------------------------
	 * @author : Lokanada Reddy
     * @param  : moveBundleId
	 * @return : assets count for a specified move bundle
	 *---------------------------------------------*/
    def assetCount( def moveBundleId ) {
    	def assetsCountInBundle = jdbcTemplate.queryForInt("select count(a.asset_entity_id) from asset_entity a where a.move_bundle_id = ${moveBundleId}" )
		return assetsCountInBundle
    }
    
    /*----------------------------------------------
     * @author : Lokanada Reddy
     * @param  : moveBundleId and transitionId
	 * @return : assetCompletionCount for a specified move bundle id and transition id
	 *---------------------------------------------*/
    def assetCompletionCount( def moveBundleId, def transitionId ){
    	def queryForAssetsCount = "SELECT max(cast(atran.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_entity ae "+
		" LEFT JOIN asset_transition atran ON ( atran.asset_entity_id = ae.asset_entity_id AND atran.voided = 0 ) "+
		" WHERE ae.move_bundle_id = ${moveBundleId} AND cast(atran.state_to as UNSIGNED INTEGER) >= ${transitionId} GROUP BY ae.asset_entity_id"
		
		def assetCompletionCount = jdbcTemplate.queryForList( queryForAssetsCount ).size()
		return assetCompletionCount
    }
    /*----------------------------------------------
     *@author : Lokanada Reddy
     *@param  : moveBundleId and transitionId 
	 *@return : actualStart and completed datetimes for a specified move bundle and transition id
	 *---------------------------------------------*/
	def getActualTimes( def moveBundleId, def transitionId ) {
    	def queryForActualTimes = "SELECT min(atran.date_created) as started, max(atran.date_created) as completed FROM asset_entity ae "+
									" LEFT JOIN asset_transition atran ON atran.asset_entity_id = ae.asset_entity_id AND atran.voided=0 "+
									" AND cast(atran.state_to as UNSIGNED INTEGER) = ( SELECT min(cast(atran2.state_to as UNSIGNED INTEGER)) as minstate "+
									" FROM asset_transition atran2 WHERE atran2.asset_entity_id = ae.asset_entity_id and atran2.voided = 0 "+
									" and cast(atran2.state_to as UNSIGNED INTEGER) >= ${transitionId} GROUP BY atran2.asset_entity_id ) "+
									" LEFT JOIN project_asset_map pam on pam.asset_id = ae.asset_entity_id "+ 
									" WHERE ae.move_bundle_id = ${moveBundleId} and pam.current_state_id >= $transitionId "+ 
									" GROUP BY ae.move_bundle_id "
		def actualTimes
		try {
			actualTimes = jdbcTemplate.queryForMap( queryForActualTimes )
		} catch (IncorrectResultSizeDataAccessException irsdae) {
			// Common occurrence so we just bale
		}
		return actualTimes
	
    }
}
