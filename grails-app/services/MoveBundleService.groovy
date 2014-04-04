import java.text.SimpleDateFormat

import jxl.write.Label

import org.springframework.dao.IncorrectResultSizeDataAccessException

import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetOptions
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetTransition
import com.tds.asset.AssetType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.AssetCableStatus
import grails.converters.JSON


class MoveBundleService {
	
	def jdbcTemplate
	def stateEngineService
	def userPreferenceService
	def sessionFactory
	def assetEntityService
    def partyRelationshipService
	def securityService
	def taskService
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
			AssetCableMap.executeUpdate("delete AssetCableMap where assetFrom in ($assetsQuery)")
			AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null, 
											assetToPort=null where assetTo in ($assetsQuery)""")
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

	/*
	 * Performs an analysis of the interdependencies of assets for a project and creates assetDependencyBundle records appropriately. It will
	 * find all assets assigned to bundles that which are set to be used for planning, sorting the assets so that those with the most dependency
	 * relationships are processed first.
	 */
	def generateDependencyGroups = { projectId, connectionTypes, statusTypes, isChecked ->
		def date = new Date()
		def formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
		String time = formatter.format(date);

		def projectInstance = Project.get(projectId)
		
		// Get array of the valid status and connection types to check against in the inner loop
		def statusList = statusTypes.replaceAll(', ',',').replaceAll("'",'').tokenize(',')
		def connectionList = connectionTypes.replaceAll(', ',',').replaceAll("'",'').tokenize(',')
		
		// User previous setting if exists else set to empty
		def depCriteriaMap = projectInstance.depConsoleCriteria ? JSON.parse(projectInstance.depConsoleCriteria) : [:]
		if(isChecked == "1"){
			depCriteriaMap = ["statusTypes": statusList, "connectionTypes":connectionList]
		}
		depCriteriaMap << ["modifiedBy":securityService.getUserLoginPerson().toString(), "modifiedDate":TimeUtil.nowGMT().getTime()]
		projectInstance.depConsoleCriteria = depCriteriaMap as JSON
		projectInstance.save(flush:true)
		
		// Find all move bundles that are flagged for Planning in the project and then get all assets in those bundles
		String moveBundleText = MoveBundle.findAllByUseForPlanningAndProject(true,projectInstance).id
		moveBundleText = GormUtil.asCommaDelimitedString(moveBundleText)

		// Get array of moveBundle ids
		def moveBundleList = moveBundleText.replaceAll(', ',',').tokenize(',')
		def errMsg
		
		def assetTypeList =  MoveBundleController.dependecyBundlingAssetType
		if (moveBundleText) {
			// Query to fetch dependent asset list with dependency type and status and move bundle list with use for planning .
			def queryForAssets = """SELECT a.asset_entity_id as assetId FROM asset_entity a
				LEFT JOIN asset_dependency ad on a.asset_entity_id = ad.asset_id OR ad.dependent_id = a.asset_entity_id
				WHERE a.asset_type in ${assetTypeList}
					AND a.move_bundle_id in (${moveBundleText}) """
			queryForAssets += connectionTypes == 'null' ? "" : " AND ad.type in (${connectionTypes}) "
			queryForAssets += statusTypes == 'null' ? "" : " AND ad.status in (${statusTypes}) "
			queryForAssets += " GROUP BY a.asset_entity_id ORDER BY COUNT(ad.asset_id) DESC "
	 
			def results = jdbcTemplate.queryForList(queryForAssets )
			def assetIds = results.assetId
			def assetIdsSize = assetIds.size()
			
			jdbcTemplate.execute("UPDATE asset_entity SET dependency_bundle=0 WHERE project_id = $projectId ")
			
			log.info "Found ${assetIdsSize} to bundle"
			log.debug "SQL used to find assets: ${queryForAssets}"
			
			int groupNum = 0
			int loops = 0
			
			def dependencyList = []
			def bundledIds = []			// Used to keep track of Assets that were bundled (AssetDependencyBundle created)
			
			// Deleting previously generated dependency bundle table .
			jdbcTemplate.execute("DELETE FROM asset_dependency_bundle where project_id = $projectId")
			// TODO: THIS SHOULD NOT BE NECESSARY GOING FORWARD - THIS COLUMN is being dropped.
			jdbcTemplate.execute("UPDATE asset_entity SET dependency_bundle=NULL WHERE project_id = $projectId")
			
	 
			// Reset hibernate session since we just cleared out the data directly and we don't want to be looking up assets in stale cache
			sessionFactory.getCurrentSession().flush();
			sessionFactory.getCurrentSession().clear();
			
			// Main loop that will iterate over all found assets for the project
			for (int i = 0; i < assetIdsSize; i++) {
				
				// Skip the loop if asset and it's dependents already bundled
				if (bundledIds.contains(assetIds[i])) continue			
				
				// Add parent asset to dependent list as the Initial asset
				def asset = AssetEntity.get(assetIds[i])
				
				groupNum++
				def groupAssets = [ asset ]
				def groupIds = [ asset.id ]
				def stack = [ asset ]
				
				log.debug "New dependency group started : id=$groupNum"
				while (stack.size() > 0) {
					asset = stack[0] 
					log.debug "Processing asset ${asset.id}:${asset.assetName}:${asset.assetType}:i=${i}:loops=${loops++}:stack=${stack.size()}"
					// cycle through all dependencies that the asset is dependent on
					AssetDependency.findAllByDependent(asset).each { ad ->
						def id = ad.asset.id
						if ( id && statusList.contains(ad.status) && connectionList.contains(ad.type) && 
							! ( groupIds.contains(id) || bundledIds.contains(id) ) && 
							moveBundleList.contains(ad.asset.moveBundle?.id.toString()) 
						  ) {
							stack << ad.asset
							groupAssets << ad.asset
							groupIds << id
						}
					}
					// cycle through all dependencies that the asset supports
					AssetDependency.findAllByAsset(asset)?.each { ad ->
						def id = ad.dependent.id
						if ( id && statusList.contains(ad.status) && connectionList.contains(ad.type) && 
							! ( groupIds.contains(id) || bundledIds.contains(id) ) &&
							moveBundleList.contains(ad.dependent.moveBundle?.id.toString())
						  ) {
							stack << ad.dependent
							groupAssets << ad.dependent
							groupIds << id
						}
					}
					stack.remove(0)
				}
				
				// Add all grouped assets to AssetDependencyBundle with groupNum.
				log.info "Saving ${groupAssets.size()} assets in bundle $groupNum"
				def count=0
				groupAssets.each {
					def assetDependencyBundle = new AssetDependencyBundle()
					assetDependencyBundle.asset = it
					assetDependencyBundle.dependencySource = count++ == 0 ? "Initial" : "Dependency"
					assetDependencyBundle.dependencyBundle = groupNum
					assetDependencyBundle.lastUpdated = date
					assetDependencyBundle.project = projectInstance
					
					if (!assetDependencyBundle.save(flush:true)) {
						assetDependencyBundle.errors.allErrors.each { log.info it }
					}
					// Remember each asset that was bundled
					bundledIds << it.id
				}
			} // for i
			
			// Last step is to put all the straggler assets that were not grouped into group 0
			def assetTypes = '"' + AssetType.SERVER.toString() + '","' + AssetType.VM.toString() + '","' + 
				AssetType.APPLICATION.toString() + '","' + AssetType.DATABASE.toString() + '","' + AssetType.FILES.toString() + '"'
				
			def stragglerSQL = """INSERT INTO asset_dependency_bundle (asset_id, dependency_bundle, dependency_source, last_updated, project_id)
				SELECT ae.asset_entity_id, 0, "Straggler", now(), ae.project_id
				FROM asset_entity ae
				LEFT OUTER JOIN asset_dependency_bundle adb ON ae.asset_entity_id=adb.asset_id
				WHERE ae.project_id = ${projectId} # AND ae.dependency_bundle IS NULL
				AND adb.asset_id IS NULL
				AND move_bundle_id in (${moveBundleText})
				AND ae.asset_type in (${assetTypes})"""
			def x = jdbcTemplate.execute(stragglerSQL)
		} else {
			errMsg="Please associate appropriate assets to one or more 'Planning' bundles before continuing."
		}
		
	}

	/*
	 * Used by several controller functions to generate the mapping arguments used by the dependencyConsole view
	 * @param projectId - the project Id to lookup the map data for
	 * @param moveBundleId - move bundle id to filter for bundle
	 * @return MapArray of properties 
	 */
	def dependencyConsoleMap(projectId, moveBundleId, isAssigned) {
		def startAll = new Date()
		def projectInstance = Project.get(projectId)
		def dependencyConsoleList = []

		// This will hold the totals of each, element 0 is all and element 1 will be All minus group 0
		def stats = [
			app: [0,0],
			db:  [0,0],
			server: [0,0],
			vm: [0,0],
			storage: [0,0]
		]
		
		def depSql = new StringBuffer("""SELECT  
			adb.dependency_bundle as dependencyBundle, 
			count(distinct adb.asset_id) as assetCnt, 
			CONVERT( group_concat(distinct a.move_bundle_id) USING 'utf8') as moveBundles,
			sum(if(a.plan_status='${AssetEntityPlanStatus.ASSIGNED}',1,0)) as statusAssigned,
			sum(if(a.plan_status='${AssetEntityPlanStatus.MOVED}',1,0)) as statusMoved,
			sum(if(a.validation<>'BundleReady',1,0)) as notBundleReady,
			sum(if(a.asset_type in ( ${AssetType.getPhysicalServerTypesAsString()} ), 1, 0)) as serverCount,
			sum(if(a.asset_type in ( ${AssetType.getVirtualServerTypesAsString()} ), 1, 0)) as vmCount,
			sum(if(a.asset_type in ( ${AssetType.getStorageTypesAsString()} ), 1, 0)) as storageCount,
			sum(if(a.asset_type = '${AssetType.DATABASE.toString()}', 1, 0)) as dbCount,
			sum(if(a.asset_type = '${AssetType.APPLICATION.toString()}', 1, 0)) as appCount,
			( select 
		   		sum(if(ad1.status in 
		   			(${AssetDependencyStatus.getReviewCodesAsString()}) OR ad2.status in (${AssetDependencyStatus.getReviewCodesAsString()}), 1,0) 
		   		)
				from asset_entity sa join asset_dependency_bundle sadb ON sa.asset_entity_id=sadb.asset_id 
				left join asset_dependency ad1 ON ad1.asset_id=sa.asset_entity_id 
				left join asset_dependency ad2 ON ad2.asset_id=sa.asset_entity_id
				where sadb.project_id=$projectId and sadb.dependency_bundle = adb.dependency_bundle
			) as needsReview
			
			FROM asset_dependency_bundle adb
			JOIN asset_entity a ON a.asset_entity_id=adb.asset_id
			WHERE adb.project_id=${projectId}""")

			
			depSql.append(" GROUP BY adb.dependency_bundle ORDER BY adb.dependency_bundle ")

		def dependList = jdbcTemplate.queryForList(depSql.toString())

		// log.info "dependencyConsoleMap() : dependList[0]"
 		// log.info "dependencyConsoleMap() : stats=$stats}"

		if( moveBundleId )
		 	dependList = dependList.collect{ if( it.moveBundles.contains(moveBundleId)){ it } }
	 
		dependList.removeAll([null])
		def groups = dependList.dependencyBundle
		userPreferenceService.getSession().setAttribute( 'Dep_Groups', (groups as JSON).toString() )
	
		dependList.each { group ->
	 		def depGroupsDone = group.statusAssigned + group.statusMoved
			def statusClass = ''
			if ( group.moveBundles?.contains(',') || group.needsReview > 0 ) {
				// Assets in multiple bundles or dependency status unknown or questioned
				statusClass = 'depGroupConflict'
			} else if ( group.notBundleReady == 0  && depGroupsDone != group.assetCnt) {
				// If all assets are BundleReady and not fully assigned
				statusClass = 'depGroupReady'
			} else if ( depGroupsDone == group.assetCnt ) {
				// Assets assigned + moved total the number of assets in the group so the group is done
				statusClass = 'depGroupDone'
			}

			// Loop through the list to create map to be used by the view
			dependencyConsoleList << [
				dependencyBundle: group.dependencyBundle,
				appCount: group.appCount,
				serverCount: group.serverCount,
				vmCount: group.vmCount, 
				dbCount: group.dbCount, 
				storageCount: group.storageCount,
				statusClass: statusClass
			]

			// Accumulate the totals for ALL and 1+ (aka All - 0)
			stats.app[0] +=  group.appCount
			stats.db[0] +=  group.dbCount
			stats.server[0] +=  group.serverCount
			stats.vm[0] +=  group.vmCount
			stats.storage[0] +=  group.storageCount
			if (group.dependencyBundle != 0) {
				stats.app[1] +=  group.appCount
				stats.db[1] +=  group.dbCount
				stats.server[1] +=  group.serverCount
				stats.vm[1] +=  group.vmCount
				stats.storage[1] +=  group.storageCount
			}
		}
		
		if(isAssigned=="1")
			dependencyConsoleList = dependencyConsoleList.findAll{it.statusClass != "depGroupDone"}

		// Get list of distinct dependencyBundle ids
		// def assetDependencyList = dependList.groupBy({it.dependencyBundle}).dependencyBundle
		// def assetDependencyList = dependList.dependencyBundle

		// log.info "dependencyConsoleMap() : assetDependencyList = $assetDependencyList"
		log.info "dependencyConsoleMap() : stats = $stats"

		def entities = assetEntityService.entityInfo( projectInstance )
 
		// Used by the Assignment Dialog
		def planningMoveBundles = MoveBundle.findAllByProjectAndUseForPlanning(projectInstance,true,[sort:'name'])
		def allMoveBundles = MoveBundle.findAllByProject(projectInstance,[sort:'name'])
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def assetDependencyList = AssetDependencyBundle.executeQuery("SELECT distinct(dependencyBundle) FROM AssetDependencyBundle WHERE project=$projectInstance.id") 
		
		// JPM - don't think that this is required
		// def personList = partyRelationshipService.getCompanyStaff( projectInstance.client?.id )
		def companiesList = PartyGroup.findAll( "from PartyGroup as p where partyType = 'COMPANY' order by p.name " )

		def availabaleRoles = partyRelationshipService.getStaffingRoles()
 		
 		log.info "dependencyConsoleMap() : stats=$stats}"
		 
		def depGrpCrt = projectInstance.depConsoleCriteria ? JSON.parse( projectInstance.depConsoleCriteria ) : [:]
		def tzId = userPreferenceService.getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def formatter = new SimpleDateFormat('MM/dd/yyyy hh:mm a');
		def generatedDate = depGrpCrt.modifiedDate ? formatter.format(TimeUtil.convertInToUserTZ(new Date(depGrpCrt.modifiedDate), tzId)):''
		def map = [ 
			company:projectInstance.client,
			asset:'apps',
			date: generatedDate,

			dependencyType: 		entities.dependencyType, 
			dependencyConsoleList: 	dependencyConsoleList,
			dependencyStatus: 		entities.dependencyStatus, 
			assetDependency: 		new AssetDependency(), 
			moveBundle: planningMoveBundles, 
			allMoveBundles: allMoveBundles, 
			planStatusOptions: planStatusOptions,

			gridStats:stats,

			//assetDependencyList: 	assetDependencyList, 
			dependencyBundleCount: 	assetDependencyList.size(),
			servers: entities.servers, 
			applications: entities.applications, 
			dbs: entities.dbs, 
			files: entities.files,
			networks:entities.networks,

			partyGroupList:companiesList,
			// personList:personList, 
            staffRoles:taskService.getRolesForStaff(),
			availabaleRoles:availabaleRoles,
			moveBundleId : moveBundleId,
			isAssigned:isAssigned,
			moveBundleList:allMoveBundles,
			depGrpCrt:depGrpCrt,
			compactPref:userPreferenceService.getPreference('depConsoleCompact')
		]
		log.info "dependencyConsoleMap() : OVERALL took ${TimeUtil.elapsed(startAll)}"

		return map
	}
	
	/* Calculates the default paramters for the dependency map based on the number of nodes
	* @param nodeCount the number of nodes in the map
	* @return a map of values for the dependency map to use as parameters
	*/
	def getMapDefaults( def nodeCount) {
		
		def defaultsSmall = [ 'force':-500, 'linkSize':90, 'friction':0.8, 'theta':0.3, 'width':800, 'height':400 ]
		def defaultsMedium = [ 'force':-400, 'linkSize':80, 'friction':0.8, 'theta':0.5, 'width':1200, 'height':600 ]
		def defaultsLarge = [ 'force':-400, 'linkSize':90, 'friction':0.8, 'theta':1, 'width':2000, 'height':1000 ]
		
		return (nodeCount<30) ? (defaultsSmall) : ( (nodeCount<200) ? (defaultsMedium) : (defaultsLarge) )
    }
	
    /**
      * Create Manual MoveEventSnapshot, when project is task driven. So dashboard dial default to manual 50
      * @param moveEvent
      * @param dialIndicator
      * @return
      */
     def createManualMoveEventSnapshot( def moveEvent, def dialIndicator=50 ){
         if(moveEvent.project.runbookOn ==1){
             def moveEventSnapshot = new MoveEventSnapshot(moveEvent : moveEvent , dialIndicator:dialIndicator )
             if ( ! moveEventSnapshot.save( flush : true ) ){
                 log.error("Unable to save changes to MoveEventSnapshot: ${moveEventSnapshot}")
             }
         }
     }
	/**
	 * Method help to write data in excel sheet's appropriate column and remove redundant code.
	 * @param exportList : list of data which is being export
	 * @param columnList : list of column of sheet
	 * @param sheet : sheet-name
	 * @return void
	 */
	def issueExport(def exportList, def columnList, def sheet, def tzId, def startRow = 0 ){
		
		def estformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		for ( int r=startRow; r < (exportList.size()+startRow); r++ ) {
			for (int c =0; c < columnList.size(); c++){
				def cellValue
				def attribName = columnList[c]
				switch(attribName){
					case "taskDependencies":
						cellValue = WebUtil.listAsPipeSepratedString(exportList[r-startRow]."${columnList[c]}".collect({ e -> e.predecessor == null ? '' : e.predecessor.taskNumber + ' ' + e.predecessor.comment?.toString()}))
						break;
					case "assetEntity":
						cellValue = exportList[r-startRow]."${columnList[c]}"?.assetType == "Application" ?  String.valueOf(exportList[r-startRow]."${columnList[c]}"?.assetName) : ''
						break;
					case "duration":
						def duration= exportList[r-startRow].duration ? (exportList[r-startRow].durationScale == "m" ? exportList[r-startRow]."${columnList[c]}" : exportList[r-startRow]."${columnList[c]}"+exportList[r-startRow].durationScale) : ''
					    cellValue = exportList[r-startRow]."${columnList[c]}" ?  String.valueOf(duration) : ''
						break;
					case "commentAssetEntity":
						cellValue = exportList[r-startRow].assetEntity ?  String.valueOf(exportList[r-startRow].assetEntity?.assetName) : ''
						break;
					case "notes":
						cellValue = exportList[r-startRow].notes ?  String.valueOf(WebUtil.listAsMultiValueString(exportList[r-startRow].notes)) : ''
						break;
					case "workflow":
						cellValue = exportList[r-startRow].workflowTransition ? String.valueOf(exportList[r-startRow].workflowTransition?.name) : ''
						 break;
					case "estStart":
						 cellValue = exportList[r-startRow].estStart ? String.valueOf(estformatter.format(TimeUtil.convertInToUserTZ(exportList[r-startRow].estStart, tzId))) : ''
						 break;
				    case "estFinish":
						 cellValue = exportList[r-startRow].estFinish ? String.valueOf(estformatter.format(TimeUtil.convertInToUserTZ(exportList[r-startRow].estFinish, tzId))) : ''
						 break;
					case "actStart":
						 cellValue = exportList[r-startRow].actStart ? String.valueOf(estformatter.format(TimeUtil.convertInToUserTZ(exportList[r-startRow].actStart, tzId))) : ''
						 break;
					case "actFinish":
						 cellValue = exportList[r-startRow].actFinish ? String.valueOf(estformatter.format(TimeUtil.convertInToUserTZ(exportList[r-startRow].dateResolved, tzId))) : ''
						 break;
				    case "":
						cellValue = ""
						break;
					default:
						cellValue = String.valueOf(exportList[r-startRow]?."${columnList[c]}" ?:'')
						break;
				}
				sheet.addCell( new Label( c, r, cellValue) )
			}
		}
	}
}
