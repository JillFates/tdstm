import org.springframework.dao.IncorrectResultSizeDataAccessException

import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetTransition
import com.tdssrc.grails.GormUtil

class MoveBundleService {
	
	def jdbcTemplate
	def stateEngineService
	def userPreferenceService
    
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
    
    /**
	 * Determines the number of assets associated with a MoveBundle that have completed a particular transition.
	 * The transition can be that of 
     * @author : Lokanada Reddy
     * @param  : moveBundleId and transitionId
	 * @return : assetCompletionCount for a specified move bundle id and transition id
	 */
    def assetCompletionCount( def moveBundleId, def transitionId ){
		 def sql = """
				SELECT count(*)	FROM(SELECT max(cast(atran.state_to as UNSIGNED INTEGER)) AS maxstate, ae.asset_entity_id 
				FROM asset_entity ae
				LEFT JOIN asset_transition atran ON ( atran.asset_entity_id = ae.asset_entity_id AND atran.voided = 0 ) 
				WHERE ae.move_bundle_id = ${moveBundleId} 
					AND atran.type='process' 
					AND cast(atran.state_to as UNSIGNED INTEGER) >= ${transitionId} 
				GROUP BY ae.asset_entity_id
				
				UNION ALL

				SELECT max(cast(atran.state_to as UNSIGNED INTEGER)) AS maxstate, ae.asset_entity_id 
				FROM asset_entity ae
				LEFT JOIN asset_transition atran ON ( atran.asset_entity_id = ae.asset_entity_id AND atran.voided = 0 ) 
				WHERE ae.move_bundle_id = ${moveBundleId} 
					AND atran.type='boolean' 
					AND cast(atran.state_to as UNSIGNED INTEGER) = ${transitionId} 
				GROUP BY ae.asset_entity_id ) atran GROUP BY atran.asset_entity_id
			"""
		
		def assetCompletionCount = jdbcTemplate.queryForList( sql ).size()
		return assetCompletionCount
    }

    /**
	 * Looks up the first and last datetime of transition for a given move bundle 
     * @author Lokanada Reddy
     * @param moveBundleId 
	 * @param transitionId 
	 * @return Map[started,completed] datetimes for a specified move bundle and transition id
	 */
	def getActualTimes( def moveBundleId, def transitionId ) {
    	def sql = """
			SELECT MIN(atran.date_created) as started, MAX(atran.date_created) as completed 
			FROM asset_entity ae
			LEFT JOIN asset_transition atran ON atran.asset_entity_id = ae.asset_entity_id AND atran.voided=0
			    AND cast(atran.state_to as UNSIGNED INTEGER) = 
			    ( SELECT min(cast(atran2.state_to as UNSIGNED INTEGER)) AS minstate
			      FROM asset_transition atran2 
			      WHERE atran2.asset_entity_id = ae.asset_entity_id AND atran2.voided = 0
			         AND cast(atran2.state_to as UNSIGNED INTEGER) >= ${transitionId}
			      GROUP BY atran2.asset_entity_id 
			    )
			LEFT JOIN project_asset_map pam ON pam.asset_id = ae.asset_entity_id AND CAST(pam.current_state_id AS UNSIGNED INTEGER) >= ${transitionId}
			WHERE ae.move_bundle_id = ${moveBundleId}
			GROUP BY ae.move_bundle_id
		"""
		def actualTimes
		try {
			// actualTimes = jdbcTemplate.queryForMap( queryForActualTimes, [moveBundleId: moveBundleId, transitionId: transitionId] )
			actualTimes = jdbcTemplate.queryForMap( sql )
		} catch (IncorrectResultSizeDataAccessException irsdae) {
			// Common occurrence so we just bale
		}
		return actualTimes
	
    }
    /* return all of the Transitions from the XML based on the workflow_code of the project that the move_bundle is associated with.
     * @author Lokanada Reddy
     * @param moveBundleId 
	 * @return Map[step,movebundleStep,snapshot] 
     */
	def getAllDashboardSteps( def moveBundle ) {
    	
		
		def moveBundleSteps = MoveBundleStep.findAll('FROM MoveBundleStep mbs WHERE mbs.moveBundle = :mb ORDER BY mbs.transitionId',[mb:moveBundle])
		def dashboardSteps = []
		try{
			def stepsList = stateEngineService.getDashboardSteps( moveBundle.workflowCode )
			stepsList.each{
				def moveBundleStep
				def stepSnapshot = []
				def stepIndex = moveBundleSteps.transitionId.indexOf(it.id)
				if(stepIndex != -1){
					moveBundleStep = moveBundleSteps[stepIndex]
					stepSnapshot = StepSnapshot.findAll("FROM StepSnapshot ss WHERE ss.moveBundleStep = :mbs ORDER BY ss.dateCreated DESC",[mbs:moveBundleStep, max:1])
					moveBundleSteps.remove(stepIndex)
				}
				dashboardSteps << [step :it, moveBundleStep : moveBundleStep, stepSnapshot : stepSnapshot[0] ]
			}
		} catch(NullPointerException npe) {	
			npe.printStackTrace()
		}
		return [dashboardSteps : dashboardSteps , remainingSteps : moveBundleSteps?.transitionId ]
    }
    /*----------------------------------------------------
     * will update the moveBundles with moveEvent
     * @author : Lokanada Reddy
     * @param  : moveEvent, moveBundles
     *--------------------------------------------------*/
	def assignMoveEvent( def moveEvent, def moveBundles ){
		MoveBundle.executeUpdate( "UPDATE MoveBundle mb SET mb.moveEvent = null where mb.moveEvent = :me",[ me:moveEvent ] )
		moveBundles.each{
			moveEvent.addToMoveBundles( MoveBundle.get( it ) )
		}
    }
	 /*----------------------------------------------------
     * will update the moveBundles with moveEvent
     * @author : Lokanada Reddy
     * @param  : moveEvent, moveBundles
     *--------------------------------------------------*/
    def createMoveBundleStep(def moveBundle, def transitionId, def params){
		
		def beGreen = params["beGreen_"+transitionId]
		def moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId(moveBundle , transitionId) 
		if( !moveBundleStep ){	
			moveBundleStep = new MoveBundleStep(moveBundle:moveBundle, transitionId:transitionId)
		}
		def tzId = userPreferenceService.getSession().getAttribute( "CURR_TZ" )?.CURR_TZ	
		moveBundleStep.calcMethod = params["calcMethod_"+transitionId]
		moveBundleStep.label = params["dashboardLabel_"+transitionId]
		moveBundleStep.planStartTime = GormUtil.convertInToGMT( new Date( params["startTime_"+transitionId] ),tzId )
		moveBundleStep.planCompletionTime = GormUtil.convertInToGMT( new Date( params["completionTime_"+transitionId] ),tzId )
		
		//show the step progress in green when user select the beGreen option
		if(beGreen && beGreen == 'on'){
			moveBundleStep.showInGreen = 1
		} else {
			moveBundleStep.showInGreen = 0
		}
		
		if ( !moveBundleStep.validate() || !moveBundleStep.save(flush:true) ) {
			def etext = "Unable to create moveBundleStep" +
			GormUtil.allErrorsString( moveBundleStep )
			response.sendError( 500, "Validation Error")
			println etext
		}
		return moveBundleStep 
	}
	 /* -----------------------------------------------
	  * delete moveBundleStep and associsted records
	  * @author : Lokanada Reddy
      * @param  : moveBundleStep
	  *----------------------------------------------*/
	 def deleteMoveBundleStep( def moveBundleStep ){
		 def stepSnapshot = StepSnapshot.executeUpdate("DELETE from StepSnapshot ss where ss.moveBundleStep = ?",[moveBundleStep]);
		 moveBundleStep.delete(flush:true);
	 }
	 /*-----------------------------------------------------
	  * Return MoveEvent Detailed Results for given event.
	  * @author : Lokanada Reddy
	  * @param  : moveEventId
	  * @return : MoveEvent Detailed Results 
	  * --------------------------------------------------*/
	 def getMoveEventDetailedResults(def moveEventId ){
		 def detailedQuery = """SELECT 
			   mb.move_bundle_id, 
			   mb.name AS bundle_name,
			   ae.asset_entity_id AS asset_id, ae.asset_name, 
			   IF(atr.voided=1, "Y", "") AS voided,
			   wtFrom.name AS from_name, wtTo.name AS to_name,
			   atr.date_created AS transition_time, username, IF(team_code IS NULL, '', team_code) AS team_name
			FROM move_event me
			JOIN project p ON p.project_id = me.project_id
			JOIN move_bundle mb ON mb.move_event_id = me.move_event_id 
			JOIN asset_entity ae ON ae.move_bundle_id = mb.move_bundle_id
			JOIN asset_transition atr ON atr.asset_entity_id = ae.asset_entity_id 
			JOIN workflow w on w.process = p.workflow_code
			JOIN workflow_transition wtFrom ON wtFrom.trans_id = CAST(atr.state_from AS UNSIGNED INTEGER) AND wtFrom.workflow_id = w.workflow_id
			JOIN workflow_transition wtTo ON wtTo.trans_id = CAST(atr.state_to AS UNSIGNED INTEGER) AND wtTo.workflow_id = w.workflow_id
			JOIN user_login ul ON ul.user_login_id = atr.user_login_id
			LEFT OUTER JOIN project_team pt ON pt.project_team_id = atr.project_team_id
			WHERE me.move_event_id = ${moveEventId}
			   AND atr.is_non_applicable = 0
			ORDER BY move_bundle_id,transition_time"""
		def detailedResults = jdbcTemplate.queryForList(detailedQuery)
		return detailedResults
	 }
	 /*-----------------------------------------------------
	  * Return MoveEvent Summary Results for given event.
	  * @author : Lokanada Reddy
	  * @param  : moveEventId
	  * @return : MoveEvent Summary Results 
	  * --------------------------------------------------*/
	 def getMoveEventSummaryResults( def moveEventId ){
		 def createTemp = """CREATE TEMPORARY TABLE tmp_step_summary
				SELECT mb.move_bundle_id,mb.name as bundle_name, atr.state_to, wt.name, NOW() AS started, NOW() AS completed
				FROM move_event me
				JOIN project p ON p.project_id = me.project_id
				JOIN move_bundle mb ON mb.move_event_id = me.move_event_id 
				JOIN asset_transition atr ON atr.move_bundle_id = mb.move_bundle_id AND atr.voided=0 
				JOIN workflow w on w.process = p.workflow_code
				JOIN workflow_transition wt ON wt.trans_id = CAST(atr.state_to AS UNSIGNED INTEGER) AND wt.workflow_id = w.workflow_id
				WHERE me.move_event_id = ${moveEventId} AND atr.is_non_applicable = 0
				GROUP BY mb.move_bundle_id, atr.state_to
				ORDER BY mb.move_bundle_id,started asc"""
		jdbcTemplate.execute(createTemp)
		// UPDATE Start Time
		def updateStartTime = """UPDATE tmp_step_summary
									SET completed = (
									   SELECT MAX(date_created) FROM asset_transition atr
									   WHERE atr.move_bundle_id = tmp_step_summary.move_bundle_id AND atr.state_to = tmp_step_summary.state_to 
									   and atr.voided = 0 AND is_non_applicable = 0)"""
		jdbcTemplate.execute(updateStartTime)
		// UPDATE Completion Time
		def updateCompletionTime = """UPDATE tmp_step_summary
									SET started = (
									   SELECT MIN(date_created) FROM asset_transition atr
									   WHERE atr.move_bundle_id = tmp_step_summary.move_bundle_id AND atr.state_to = tmp_step_summary.state_to 
									   and atr.voided = 0 AND is_non_applicable = 0)"""
		jdbcTemplate.execute(updateCompletionTime)
		
		def summaryResults = jdbcTemplate.queryForList( "SELECT * FROM tmp_step_summary" )
		jdbcTemplate.execute( "DROP TEMPORARY TABLE IF EXISTS tmp_step_summary" )
		return summaryResults;
	 }
	 /**
	  *  Delete Bundle AssetEntitys and its associated records
	  */
	 def deleteBundleAssetsAndAssociates( def moveBundleInstance ){
		 def message
		 try{
			 // remove preferences
			 def bundleQuery = "select mb.id from MoveBundle mb where mb.id = ${moveBundleInstance.id}"
			 UserPreference.executeUpdate("delete from UserPreference up where up.value = ${moveBundleInstance.id} ")
			 //remove the AssetEntity
			 def assetsQuery = "select a.id from AssetEntity a where a.moveBundle = ${moveBundleInstance.id}"
			 
			 ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset in ($assetsQuery)")
			 AssetComment.executeUpdate("delete from AssetComment ac where ac.assetEntity in ($assetsQuery)")
			 AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar av where av.assetEntity in ($assetsQuery)")
			 AssetTransition.executeUpdate("delete from AssetTransition at where at.assetEntity in ($assetsQuery)")
			 ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.asset in ($assetsQuery)")
			 AssetCableMap.executeUpdate("delete AssetCableMap where fromAsset in ($assetsQuery)")
			 AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
											 toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
											 where toAsset in ($assetsQuery)""")
			 ProjectTeam.executeUpdate("Update ProjectTeam pt SET pt.latestAsset = null where pt.latestAsset in ($assetsQuery)")
			 
			 AssetEntity.executeUpdate("delete from AssetEntity ae where a.moveBundle = ${moveBundleInstance.id}")
			 
		 } catch(Exception ex){
			 message = "Unable to remove the $moveBundleInstance Assets Error:"+ex
		 }
		 return message
	 }
	 /**
	 *  Delete MoveBundle associated records
	 */
	 def deleteMoveBundleAssociates( def moveBundleInstance ){
		def message
		try{
			def teamQuery = "SELECT project_team_id FROM project_team WHERE move_bundle_id = ${moveBundleInstance.id}"
			jdbcTemplate.update("DELETE FROM party_relationship where party_id_from_id in ($teamQuery) or party_id_to_id in ($teamQuery) ")
			jdbcTemplate.update("DELETE FROM party where party_id in ($teamQuery)")
			jdbcTemplate.update("DELETE FROM party_group where party_group_id in ($teamQuery)")
			jdbcTemplate.update("UPDATE asset_transition SET  project_team_id = null WHERE move_bundle_id = ${moveBundleInstance.id} or project_team_id in ($teamQuery) ")
            jdbcTemplate.update("UPDATE asset_entity set source_team_id = null WHERE source_team_id in ($teamQuery)")
			jdbcTemplate.update("UPDATE asset_entity set target_team_id = null WHERE target_team_id in ($teamQuery)")
			jdbcTemplate.update("DELETE FROM project_team WHERE move_bundle_id = ${moveBundleInstance.id}")
			
			
			jdbcTemplate.update("DELETE FROM user_preference WHERE value = ${moveBundleInstance.id}")
			
        	jdbcTemplate.update("DELETE FROM party_relationship where party_id_from_id  = ${moveBundleInstance.id} or party_id_to_id = ${moveBundleInstance.id}")
			
			AssetTransition.executeUpdate("delete from AssetTransition at where at.moveBundle = ${moveBundleInstance.id}")
			StepSnapshot.executeUpdate("delete from StepSnapshot ss where ss.moveBundleStep in (select mbs.id from MoveBundleStep mbs where mbs.moveBundle = ${moveBundleInstance.id})")
			MoveBundleStep.executeUpdate("delete from MoveBundleStep mbs where mbs.moveBundle = ${moveBundleInstance.id}")
			
		} catch(Exception ex){
			message = "Unable to remove the $moveBundleInstance Error:"+ex
		}
		return message
	}
}
