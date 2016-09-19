import UserPreferenceEnum as PREF
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetOptions
import com.tds.asset.AssetType
import com.tdsops.tm.asset.graph.AssetGraph
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import grails.converters.JSON
import org.apache.poi.ss.usermodel.Cell
import org.codehaus.groovy.grails.web.util.WebUtils
import grails.transaction.Transactional
import org.hibernate.SessionFactory
import org.springframework.jdbc.core.JdbcTemplate

@Transactional
class MoveBundleService {

	AssetEntityService assetEntityService
	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	ProgressService progressService
	SecurityService securityService
	SessionFactory sessionFactory
	StateEngineService stateEngineService
	TaskService taskService
	UserPreferenceService userPreferenceService

	/*----------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : moveBundleId
	 * @return : assets count for a specified move bundle
	 *---------------------------------------------*/
	def assetCount( def moveBundleId ) {
		def assetsCountInBundle = jdbcTemplate.queryForInt("select count(a.asset_entity_id) from asset_entity a where a.move_bundle_id = ${moveBundleId}" )
		return assetsCountInBundle
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
		def session = WebUtils.retrieveGrailsWebRequest().session
		moveBundleStep.calcMethod = params["calcMethod_"+transitionId]
		moveBundleStep.label = params["dashboardLabel_"+transitionId]
		moveBundleStep.planStartTime = TimeUtil.parseDateTime(session, params["startTime_"+transitionId])
		moveBundleStep.planCompletionTime = TimeUtil.parseDateTime(session, params["completionTime_"+transitionId])

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
		 def stepSnapshot = StepSnapshot.executeUpdate("DELETE from StepSnapshot ss where ss.moveBundleStep = ?",[moveBundleStep])
		 moveBundleStep.delete(flush:true)
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
		return summaryResults
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
			//remove the AssetEntity and associated
			def assets = AssetEntity.findAllByMoveBundle(moveBundleInstance)
			if(assets){
				ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset in (:assets)", [assets:assets])
				AssetComment.executeUpdate("delete from AssetComment ac where ac.assetEntity in (:assets)", [assets:assets])
				AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar av where av.assetEntity in (:assets)", [assets:assets])
				ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.asset in (:assets)", [assets:assets])
				AssetCableMap.executeUpdate("delete AssetCableMap where assetFrom in (:assets)", [assets:assets])
				AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
												assetToPort=null where assetTo in (:assets)""", [assets:assets])
				ProjectTeam.executeUpdate("Update ProjectTeam pt SET pt.latestAsset = null where pt.latestAsset in (:assets)", [assets:assets])
				AssetDependency.executeUpdate("delete AssetDependency where asset in (:assets) or dependent in (:deps)",
												[assets:assets, deps:assets] )
			    AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset in (:assets)", [assets:assets])
				AssetEntity.executeUpdate("delete from AssetEntity ae where a.moveBundle = ${moveBundleInstance.id}")
			}

		} catch(Exception ex){
			message = "Unable to remove the $moveBundleInstance Assets Error:"+ex
		}
		return message
	}

	def deleteBundle(moveBundleInstance, project){
		def msg = null
		def defaultBundle = project.defaultBundle
		if(moveBundleInstance.id == defaultBundle.id){
			msg = "The project default bundle can not be deleted"
		}else{
			def projectBundles = MoveBundle.findAllByProject(project)
			def isRelated = projectBundles.any{it.id == moveBundleInstance.id}
			if(!isRelated){
				msg = "Unable to locate specified bundle"
			}
			if(moveBundleInstance && project) {
				AssetEntity.withTransaction { status ->
					try{
						// Update asset associations
						AssetEntity.executeUpdate("UPDATE AssetEntity SET moveBundle = ? WHERE moveBundle = ?",
							[project.defaultBundle, moveBundleInstance])
						// Delete Bundle and associations
						deleteMoveBundleAssociates(moveBundleInstance)

						moveBundleInstance.delete(flush:true)

						msg = "MoveBundle ${moveBundleInstance} deleted"

					}catch(Exception ex){
						ex.printStackTrace()
						status.setRollbackOnly()
						msg = "Unable to delete bundle " + moveBundleInstance.name
					}
				}
			} else {
				msg = "MoveBundle not found with id ${params.id}"
			}
		}

	}

	/**
	 *  Delete MoveBundle associated records
	 */
	def deleteMoveBundleAssociates( def moveBundleInstance ){
		def message
		try{

			jdbcTemplate.update("DELETE FROM user_preference WHERE value = ${moveBundleInstance.id}")

		jdbcTemplate.update("DELETE FROM party_relationship where party_id_from_id  = ${moveBundleInstance.id} or party_id_to_id = ${moveBundleInstance.id}")

			MoveBundleStep.executeUpdate("delete from MoveBundleStep mbs where mbs.moveBundle = ${moveBundleInstance.id}")

		} catch(Exception ex){
			message = "Unable to remove the $moveBundleInstance Error:"+ex
		}
		return message
	}

	/*
	 * Used by several controller functions to generate the mapping arguments used by the dependencyConsole view
	 * @param projectId - the project Id to lookup the map data for
	 * @param moveBundleId - move bundle id to filter for bundle
	 * @return MapArray of properties
	 */
	def dependencyConsoleMap (def projectId, def moveBundleId, def isAssigned, def dependencyBundle, def graph = false) {
		def startAll = new Date()
		def projectInstance = Project.get(projectId)
		def dependencyConsoleList = []

		// This will hold the totals of each, element 0 is all and element 1 will be All minus group 0
		def stats = [
			app: [0,0],
			db: [0,0],
			server: [0,0],
			vm: [0,0],
			storage: [0,0]
		]

		def depSql = new StringBuffer("""SELECT
			adb.dependency_bundle AS dependencyBundle,
			COUNT(distinct adb.asset_id) AS assetCnt,
			CONVERT( group_concat(distinct a.move_bundle_id) USING 'utf8') AS moveBundles,
			SUM(if(a.plan_status='${AssetEntityPlanStatus.ASSIGNED}',1,0)) AS statusAssigned,
			SUM(if(a.plan_status='${AssetEntityPlanStatus.MOVED}',1,0)) AS statusMoved,
			SUM(if(a.validation<>'BundleReady',1,0)) AS notBundleReady,
			SUM(if(a.asset_class = '${AssetClass.DEVICE.toString()}'
				AND if(m.model_id > -1, m.asset_type in ( ${AssetType.getPhysicalServerTypesAsString()} ), a.asset_type in ( ${AssetType.getPhysicalServerTypesAsString()} )), 1, 0)) AS serverCount,
			SUM(if(a.asset_class = '${AssetClass.DEVICE.toString()}'
				AND if(m.model_id > -1, m.asset_type in ( ${AssetType.getVirtualServerTypesAsString()} ), a.asset_type in ( ${AssetType.getVirtualServerTypesAsString()} )), 1, 0)) AS vmCount,
			SUM(if((a.asset_class = '${AssetClass.STORAGE.toString()}')
				OR (a.asset_class = '${AssetClass.DEVICE.toString()}'
				AND if(m.model_id > -1, m.asset_type in ( ${AssetType.getStorageTypesAsString()} ), a.asset_type in ( ${AssetType.getStorageTypesAsString()} )) ), 1, 0)) AS storageCount,
			SUM(if(a.asset_class = '${AssetClass.DATABASE.toString()}', 1, 0)) AS dbCount,
			SUM(if(a.asset_class = '${AssetClass.APPLICATION.toString()}', 1, 0)) AS appCount,
			( select
				SUM(if(ad1.status in
					(${AssetDependencyStatus.getReviewCodesAsString()}) OR ad2.status in (${AssetDependencyStatus.getReviewCodesAsString()}), 1,0)
				)
				from asset_entity sa join asset_dependency_bundle sadb ON sa.asset_entity_id=sadb.asset_id
				left join asset_dependency ad1 ON ad1.asset_id=sa.asset_entity_id
				left join asset_dependency ad2 ON ad2.asset_id=sa.asset_entity_id
				where sadb.project_id=$projectId and sadb.dependency_bundle = adb.dependency_bundle
			) AS needsReview

			FROM asset_dependency_bundle adb
			JOIN asset_entity a ON a.asset_entity_id=adb.asset_id
			LEFT OUTER JOIN model m ON a.model_id=m.model_id
			WHERE adb.project_id=${projectId}""")

			if (dependencyBundle) {
				def depGroups = JSON.parse(WebUtils.retrieveGrailsWebRequest().session.getAttribute('Dep_Groups'))
				if (depGroups.size() == 0) {
					depGroups = [-1]
				}
				if (dependencyBundle.isNumber() )
					depSql.append(" AND adb.dependency_bundle = ${dependencyBundle}")
				else if (dependencyBundle == 'onePlus')
					depSql.append(" AND adb.dependency_bundle IN (${WebUtil.listAsMultiValueString(depGroups-[0])})")
				else if (dependencyBundle == 'all')
					depSql.append(" AND adb.dependency_bundle IN (${WebUtil.listAsMultiValueString(depGroups)})")
			}

			depSql.append(" GROUP BY adb.dependency_bundle ORDER BY adb.dependency_bundle ")

		def dependList = jdbcTemplate.queryForList(depSql.toString())

		if( moveBundleId )
		 	dependList = dependList.collect{ if( it.moveBundles.contains(moveBundleId)){ it } }

		dependList.removeAll([null])
		def groups = dependList.dependencyBundle
		if (dependencyBundle == null)
			WebUtils.retrieveGrailsWebRequest().session.setAttribute( 'Dep_Groups', (groups as JSON).toString() )

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

		// if this is being used for the dependency graph, this is all we need
		if (graph)
			return stats

		if (isAssigned == "1")
			dependencyConsoleList = dependencyConsoleList.findAll{it.statusClass != "depGroupDone"}


		def entities = assetEntityService.entityInfo( projectInstance )

		// Used by the Assignment Dialog
		def allMoveBundles = MoveBundle.findAllByProject(projectInstance,[sort:'name'])
		def planningMoveBundles = allMoveBundles.findAll{return it.useForPlanning}
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def assetDependencyList = AssetDependencyBundle.executeQuery("SELECT distinct(dependencyBundle) FROM AssetDependencyBundle WHERE project=$projectInstance.id")

		// JPM - don't think that this is required
		// def personList = partyRelationshipService.getCompanyStaff( projectInstance.client?.id )
		def companiesList = PartyGroup.findAll( "from PartyGroup as p where partyType = 'COMPANY' order by p.name " )

		def availabaleRoles = partyRelationshipService.getStaffingRoles()

		def depGrpCrt = projectInstance.depConsoleCriteria ? JSON.parse( projectInstance.depConsoleCriteria ) : [:]
		def session = WebUtils.retrieveGrailsWebRequest().session
		def generatedDate = depGrpCrt.modifiedDate ? TimeUtil.formatDateTime(session, depGrpCrt.modifiedDate):''
		def staffRoles = taskService.getRolesForStaff()
		def compactPref = userPreferenceService.getPreference(PREF.DEP_CONSOLE_COMPACT)
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
			staffRoles:staffRoles,
			availabaleRoles:availabaleRoles,
			moveBundleId : moveBundleId,
			isAssigned:isAssigned,
			moveBundleList:allMoveBundles,
			depGrpCrt:depGrpCrt,
			compactPref:compactPref
		]
		log.info "dependencyConsoleMap() : OVERALL took ${TimeUtil.elapsed(startAll)}"

		return map
	}

	/* Calculates the default parameters for the dependency map based on the number of nodes
	 * @param nodeCount the number of nodes in the map
	 * @return a map of values for the dependency map to use as parameters
	 */
	def getMapDefaults (def nodeCount) {

		def defaultsSmall = [ 'force':-500, 'linkSize':90, 'friction':0.7, 'theta':1, 'maxCutAttempts':200 ]
		def defaultsMedium = [ 'force':-500, 'linkSize':100, 'friction':0.7, 'theta':1, 'maxCutAttempts':150 ]
		def defaultsLarge = [ 'force':-500, 'linkSize':120, 'friction':0.7, 'theta':1, 'maxCutAttempts':100 ]

		return (nodeCount <= 50) ? (defaultsSmall) : ( (nodeCount<=200) ? (defaultsMedium) : (defaultsLarge) )
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
	def issueExport (Collection exportList, def columnList, def sheet, def tzId, userDTFormat, def startRow = 0, def viewUnpublished = false) {
		//Just in case that we get a NULL list
		exportList = exportList ?: []

		def dateFormatter = TimeUtil.createFormatterForType(userDTFormat, TimeUtil.FORMAT_DATE)

		def formatDateTimeForExport = { dateValue ->
			return (dateValue ? TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, dateValue) : '')
		}

		exportList.eachWithIndex{ assetComment, r ->
			columnList.eachWithIndex{ attribName, col ->
				def cellValue

				boolean isNumber=false
				// log.debug "*** attribName isa ${attribName.getClass().getName()} ${ (it instanceof Closure) }"
				switch (attribName) {
					case { it instanceof Closure }:
						cellValue = attribName.call( assetComment )
						break
					case "taskDependencies":
						if (viewUnpublished){
							cellValue = WebUtil.listAsPipeSepratedString(assetComment[attribName].collect({ e -> e.predecessor == null ? '' : e.predecessor.taskNumber + ' ' + e.predecessor.comment?.toString()}))
						}else{
							cellValue = WebUtil.listAsPipeSepratedString(assetComment[attribName].findAll{it.predecessor?.isPublished}.collect({ e -> (e.predecessor == null ? '' : e.predecessor.taskNumber) + ' ' + e.predecessor.comment?.toString()}))
						}
						cellValue = StringUtil.ellipsis(cellValue, 32767)
						break
					case 'taskNumber':
						cellValue = assetComment.taskNumber
						isNumber = true
						break
					case "assetEntity":
						cellValue = assetComment[attribName]?.assetType == "Application" ?  String.valueOf(assetComment[attribName]?.assetName) : ''
						break
					case "assetClass":
						cellValue = assetComment["assetEntity"]? String.valueOf(assetComment["assetEntity"]?.assetType) : ''
						break
					case "assetId":
						cellValue = assetComment["assetEntity"]? String.valueOf(assetComment["assetEntity"]?.id) : ''
						break
					case "commentAssetEntity":
						cellValue = assetComment.assetEntity ?  String.valueOf(assetComment.assetEntity?.assetName) : ''
						break
					case "notes":
						cellValue = assetComment.notes ?  String.valueOf(WebUtil.listAsMultiValueString(assetComment.notes)) : ''
						break
					case "instructionsLink":
						cellValue = assetComment.instructionsLink ?  String.valueOf(assetComment.instructionsLink) : ''
						break
					case "workflow":
						cellValue = assetComment.workflowTransition ? String.valueOf(assetComment.workflowTransition?.name) : ''
						 break

					case ~/actStart|actFinish|dateResolved|dateCreated|estStart|estFinish/:
						cellValue = formatDateTimeForExport( assetComment[attribName] )
						break

					case "datePlanned":
						 cellValue = assetComment.estStart ? TimeUtil.formatDate(assetComment.estStart, dateFormatter) : ''
						 break
					case "dueDate":
						 cellValue = assetComment.dueDate ? TimeUtil.formatDate(assetComment.dueDate, dateFormatter) : ''
						 break
					case "duration":
						 def duration = assetComment.duration
						 cellValue = duration ?: 0
						 isNumber = true
						 break
					case "taskBatchId":
						 cellValue = (assetComment.taskBatch?.id) ?: ""
						 isNumber = true
						 break
					case "":
						cellValue = ""
						break
					default:
						cellValue = String.valueOf(assetComment[attribName] ?: '')
						break
				}

				//write to row Num + startRow
				def rowToWrite = r + startRow
				if (isNumber) {
					WorkbookUtil.addCell(sheet, col, rowToWrite, cellValue, Cell.CELL_TYPE_NUMERIC)
				} else {
					WorkbookUtil.addCell(sheet, col, rowToWrite, cellValue)
				}
			}
		}
	}

	/**
	 * Performs an analysis of the interdependencies of assets for a project and creates assetDependencyBundle records appropriately. It will
	 * find all assets assigned to bundles that which are set to be used for planning, sorting the assets so that those with the most dependency
	 * relationships are processed first.
	 * @param projectId : Related project
	 * @param connectionTypes : filter for asset types
	 * @param statusTypes : filter for status tyoes
	 * @param isChecked : check if should use the new criteria
	 * @param progressKey : progress key
	 * @return String message information
	 */
	def generateDependencyGroups(projectId, connectionTypes, statusTypes, isChecked, userLoginName, progressKey) {

		String sqlTime = TimeUtil.formatDateTimeAsGMT(TimeUtil.nowGMT(), TimeUtil.FORMAT_DATE_TIME_14)
		def projectInstance = Project.get(projectId)

		// Get array of the valid status and connection types to check against in the inner loop
		def statusList = statusTypes.replaceAll(', ',',').replaceAll("'",'').tokenize(',')
		def connectionList = connectionTypes.replaceAll(', ',',').replaceAll("'",'').tokenize(',')

		// User previous setting if exists else set to empty
		def depCriteriaMap = projectInstance.depConsoleCriteria ? JSON.parse(projectInstance.depConsoleCriteria) : [:]
		if (isChecked == "1") {
			depCriteriaMap = ["statusTypes": statusList, "connectionTypes":connectionList]
		}
		depCriteriaMap << ["modifiedBy":userLoginName, "modifiedDate":TimeUtil.nowGMT().getTime()]
		projectInstance.depConsoleCriteria = depCriteriaMap as JSON
		projectInstance.save(flush:true)

		// Find all move bundles that are flagged for Planning in the project and then get all assets in those bundles
		String moveBundleText = MoveBundle.findAllByUseForPlanningAndProject(true,projectInstance).id
		moveBundleText = GormUtil.asCommaDelimitedString(moveBundleText)

		// Get array of moveBundle ids
		def moveBundleList = moveBundleText.replaceAll(', ',',').tokenize(',')
		def errMsg

		if (moveBundleText) {
			def started = new Date()
			progressService.update(progressKey, 10I, ProgressService.STARTED, "Search assets and dependencies")

			def results = searchForAssetDependencies(moveBundleText, connectionTypes, statusTypes)

			log.info "Dependency groups generation - Search assets and dependencies time ${TimeUtil.elapsed(started)}"
			started = new Date()
			progressService.update(progressKey, 20I, ProgressService.STARTED, "Load asset results")

			def graph = new AssetGraph()
			graph.loadFromResults(results)

			log.info "Dependency groups generation - Load results time ${TimeUtil.elapsed(started)}"
			started = new Date()
			progressService.update(progressKey, 30I, ProgressService.STARTED, "Clean dependencies")

			cleanDependencyGroupsStatus(projectId)

			log.info "Dependency groups generation - Clean dependencies time ${TimeUtil.elapsed(started)}"
			started = new Date()
			progressService.update(progressKey, 40I, ProgressService.STARTED, "Group dependencies")

			def groups = graph.groupByDependencies(statusList, connectionList, moveBundleList, MoveBundleController.dependecyBundlingAssetTypeMap)
			groups.sort { a, b -> b.size() <=> a.size() }

			log.info "Dependency groups generation - Group dependencies time ${TimeUtil.elapsed(started)}"
			started = new Date()
			progressService.update(progressKey, 70I, ProgressService.STARTED, "Save dependencies")

			saveDependencyGroups(projectInstance, groups, sqlTime)

			log.info "Dependency groups generation - Save dependencies time ${TimeUtil.elapsed(started)}"
			started = new Date()
			progressService.update(progressKey, 80I, ProgressService.STARTED, "Add straggles assets")

			// Last step is to put all the straggler assets that were not grouped into group 0
			addStragglerDepsToGroupZero(projectId, moveBundleText, MoveBundleController.dependecyBundlingAssetType, sqlTime)

			log.info "Dependency groups generation - Add straggles time ${TimeUtil.elapsed(started)}"
			started = new Date()
			progressService.update(progressKey, 90I, ProgressService.STARTED, "Finishing")

			graph.destroy()

			log.info "Dependency groups generation - Destroy graph time ${TimeUtil.elapsed(started)}"
			started = new Date()
			progressService.update(progressKey, 100I, ProgressService.COMPLETED, "Finished")

		} else {
			errMsg = "Please associate appropriate assets to one or more 'Planning' bundles before continuing."
		}
	}

	/**
	 * Performs a search for all the assets and dependencies that match the parameters.
	 * @param connectionTypes : filter for asset types on connection
	 * @param statusTypes : filter for status tyoes
	 * @param moveBundleText : bundle ids to analyze
	 * @return List of records
	 */
	private def searchForAssetDependencies(moveBundleText, connectionTypes, statusTypes) {

		// Query to fetch dependent asset list with dependency type and status and move bundle list with use for planning .
		def queryForAssets = """SELECT a.asset_entity_id as assetId, ad.asset_id as assetDepFromId, ad.dependent_id as assetDepToId, a.move_bundle_id as moveBundleId, ad.status as status, ad.type as type, a.asset_type as assetType FROM asset_entity a
			LEFT JOIN asset_dependency ad on a.asset_entity_id = ad.asset_id OR ad.dependent_id = a.asset_entity_id
			WHERE a.move_bundle_id in (${moveBundleText}) """
		queryForAssets += connectionTypes ? " AND ad.type in (${connectionTypes}) " : ""
		queryForAssets += statusTypes ? " AND ad.status in (${statusTypes}) " : ""
		queryForAssets += " ORDER BY a.asset_entity_id DESC  "

		log.info "SQL used to find assets: ${queryForAssets}"

		return jdbcTemplate.queryForList(queryForAssets)
	}

	/**
	 * Clean all dependency groups for the specified project
	 * @param projectId : related project
	 */
	private def cleanDependencyGroupsStatus(projectId) {
		jdbcTemplate.execute("UPDATE asset_entity SET dependency_bundle=0 WHERE project_id = $projectId ")

		// Deleting previously generated dependency bundle table .
		jdbcTemplate.execute("DELETE FROM asset_dependency_bundle where project_id = $projectId")
		// TODO: THIS SHOULD NOT BE NECESSARY GOING FORWARD - THIS COLUMN is being dropped.
		jdbcTemplate.execute("UPDATE asset_entity SET dependency_bundle=NULL WHERE project_id = $projectId")

		// Reset hibernate session since we just cleared out the data directly and we don't want to be looking up assets in stale cache
		sessionFactory.getCurrentSession().flush()
		sessionFactory.getCurrentSession().clear()
	}

	/**
	 * Store all groups in the database
	 * @param projectInstance : related project
	 * @param groups : groups to be stored
	 */
	private def saveDependencyGroups(projectInstance, groups, sqlTime) {
		def dependency_source
		int groupNum = 0
		groups.each { group ->

			def count = 0
			groupNum++

			def insertSQL = "INSERT INTO asset_dependency_bundle (asset_id, dependency_bundle, dependency_source, last_updated, project_id) VALUES "
			def first = true

			group.each { asset ->
				dependency_source = (count++ == 0 ? "Initial" : "Dependency")
				if (!first) {
					insertSQL += ","
				}
				insertSQL += "($asset.id,$groupNum,'$dependency_source','$sqlTime',$projectInstance.id)"
				first = false
			}

			jdbcTemplate.execute(insertSQL)
		}
	}

	/**
	 * put all the straggler assets that were not grouped into group 0
	 * @param projectId : related project
	 * @param moveBundleText : bundle ids to analyze
	 * @param assetTypeList : filter for asset types
	 */
	private def addStragglerDepsToGroupZero(projectId, moveBundleText, assetTypeList, sqlTime) {
		def stragglerSQL = """INSERT INTO asset_dependency_bundle (asset_id, dependency_bundle, dependency_source, last_updated, project_id)
			SELECT ae.asset_entity_id, 0, "Straggler", "$sqlTime", ae.project_id
			FROM asset_entity ae
			LEFT OUTER JOIN asset_dependency_bundle adb ON ae.asset_entity_id=adb.asset_id
			WHERE ae.project_id = ${projectId} # AND ae.dependency_bundle IS NULL
			AND adb.asset_id IS NULL
			AND move_bundle_id in (${moveBundleText})
			AND ae.asset_type in ${assetTypeList}"""

		jdbcTemplate.execute(stragglerSQL)
	}

}
