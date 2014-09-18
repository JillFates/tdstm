import grails.converters.JSON

import java.text.SimpleDateFormat

import jxl.*
import jxl.read.biff.*
import jxl.write.*

import org.apache.commons.lang.math.NumberUtils
import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import java.util.regex.Matcher

import com.tds.asset.Application
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetOptions
import com.tds.asset.AssetTransition
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil

class AssetEntityService {

	// TODO : JPM 9/2014 : determine if customLabels is used as it does NOT have all of the values it should
	protected static customLabels = ['Custom1','Custom2','Custom3','Custom4','Custom5','Custom6','Custom7','Custom8','Custom9','Custom10',
		'Custom11','Custom12','Custom13','Custom14','Custom15','Custom16','Custom17','Custom18','Custom19','Custom20','Custom21','Custom22','Custom23','Custom24']
	
	// TODO : JPM 9/2014 : determine if bundleMoveAndClientTeams is used as the team functionality has been RIPPED out of TM
	protected static bundleMoveAndClientTeams = ['sourceTeamMt','sourceTeamLog','sourceTeamSa','sourceTeamDba','targetTeamMt','targetTeamLog','targetTeamSa','targetTeamDba']

	static transactional = true
	def jdbcTemplate
	def projectService
	def assetEntityAttributeLoaderService
	def userPreferenceService
	def securityService
	def progressService
	def partyRelationshipService
	def taskService
	
	/**
	 * This method is used to update dependencies for all entity types
	 * @param params : params map received from client side
	 * @param assetEntity : instance of entity including Server, Application, Database, Files
	 * @param loginUser : Instance of current logged in user
	 * @param project : Instance of current project
	 * @return errorMsg : String of error came while updating dependencies (if any)
	 */
	
	def createOrUpdateAssetEntityDependencies(def params, def assetEntity, loginUser, project) {
		def errorMsg = ""
		AssetDependency.withTransaction(){status->
			try{
				validateAssetList([assetEntity.id], project) // Verifying assetEntity Exist in same project or not
				
				//Collecting deleted deps ids and fetching there instances list
				def deletedDepIds = params.deletedDep ? params.deletedDep.split(",").collect{NumberUtils.toDouble(it, 0).round()} : []
				def deletedDeps = params.deletedDep ? AssetDependency.findAllByIdInList(deletedDepIds) : []
				
				if(deletedDeps){
					//Sending deleted dep list from browser to ensure all ids are of current project
					validateAssetList(deletedDeps?.asset?.id+deletedDeps?.dependent?.id, project)
					//After ensuring deleting deleted dependencies from dialog
					AssetDependency.executeUpdate("delete AssetDependency where id in ( :deletedDeps ) ", [deletedDeps:deletedDeps.id])
				}
				//Update supporting dependencies 
				def supports = AssetDependency.findAll("FROM AssetDependency ad WHERE ad.dependent = :asset AND ad.dependent.project = :project",
								[asset:assetEntity, project:project])
				
				def supportMsg = addOrUpdateMultipleDeps(assetEntity, supports,  params, errorMsg, [user:loginUser, type:"support", key:"addedSupport", project:project] )
				//Update dependents dependencies 
				
				def deps = AssetDependency.findAll("FROM AssetDependency ad WHERE ad.asset = :asset AND ad.dependent.project = :project",
					[asset:assetEntity, project:project])
				def depMsg = addOrUpdateMultipleDeps(assetEntity, deps,  params, errorMsg, [user:loginUser, type:"dependent", key:"addedDep", project:project])
				
				errorMsg = supportMsg + depMsg
			} catch (RuntimeException rte){
				status.setRollbackOnly()
				return rte.getMessage()
			}
		}
		return errorMsg
	}
	
	/**
	 * A common method to forward dependency update or create request to next step based on conditions. 
	 * @param entity : instance of Entities including AssetEntity, Application, Database, Files .
	 * @param deps : list of dependents or supporters for entities as they need to updated .
	 * @param params : map of params received from client side .
	 * @param errorMsg : Reference of errorMsg String .
	 * @param paramsMap : A map in argument contains additional params like loggedUser, type, key and project .
	 * @return errorMsg : String of error came while updating dependencies (if any)
	 */
	def addOrUpdateMultipleDeps(def entity, def deps, def params, errorMsg, def paramsMap ){
		//Collecting all received supports and dependent entities and sending to validate for current project
		def assetIds = deps.collect{dep-> return NumberUtils.toDouble(params["asset_${paramsMap.type}_"+dep.id], 0).round()}
		validateAssetList(assetIds, paramsMap.project)
		//If everything is all right then processing it further for transaction
		deps.each{dep->
			errorMsg += addOrUpdateDeps(dep, dep.id, entity, params, paramsMap.user, paramsMap.type, false)
			updateBundle((paramsMap.type== "support" ? dep.asset : dep.dependent), params["moveBundle_${paramsMap.type}_"+dep.id])
			
		}  
		if(params.containsKey(paramsMap.key) && params[paramsMap.key] != "0"){
			//Collecting all received supports and dependent added entities and sending to validate for current project
			def newdepAssetIds = (0..(NumberUtils.toDouble(params[paramsMap.key], 0).round()+1)).collect{dep-> 
				return NumberUtils.toDouble(params["asset_${paramsMap.type}_"+dep], 0).round()
			}
			validateAssetList(newdepAssetIds, paramsMap.project)
			
			(0..(NumberUtils.toDouble(params[paramsMap.key], 0).round()+1)).each{addedDep->
				errorMsg += addOrUpdateDeps(new AssetDependency(), addedDep, entity, params, paramsMap.user, paramsMap.type, true)
			}
		}
		return errorMsg
	}

	/**
	 * This common method is used to updating dependencies or create dependencies for given asset(entity)
	 * @param type : instance of AssetDependency 
	 * @param idSuf : idSuf could be id of dependency in case of updating and -1,-2 decrement-ed integer in case of saving 
	 * @param assetEntity : instance of AssetEntity for which dependencies are storing
	 * @param params : params received from client side
	 * @param loginUser: Instance of currently logged in user .
	 * @param depType : a flag to determine it is "support" or "dependent" 
	 * @param createNew : a flag to determine whether the record is new or not
	 * @return  errMsg String of error came while updating dependencies (if any)
	 */
	def addOrUpdateDeps(def type, def idSuf, def assetEntity, def params, def loginUser, def depType, def createNew = false){
		//looking in DB whether added dependency exist or not
		def depEntity = AssetEntity.get(NumberUtils.toDouble(params["asset_${depType}_"+idSuf],0).round())

		def errMsg = "" // Initializing var to return error message (if came)
		if(depEntity){
			updateBundle(depEntity, params["moveBundle_${depType}_"+idSuf])

			def alreadyExist = false //Initializing var to save dependency if dependency already exist
			// if flag is true for creating record need to check whether that record is not there in DB
			if(createNew) 
				alreadyExist = depType=="dependent" ? AssetDependency.findByAssetAndDependent(assetEntity, depEntity) :
					AssetDependency.findByAssetAndDependent(depEntity, assetEntity)
			//Going update or save record if asset and dependency is belonging to same project and if a
			//new record came to update already there in DB or not 		
			if(!alreadyExist){
				type.dataFlowFreq = params["dataFlowFreq_${depType}_"+idSuf]
				type.type = params["dtype_${depType}_"+idSuf]
				type.status = params["status_${depType}_"+idSuf]
				type.asset = depType=="dependent" ? assetEntity : depEntity
				type.dependent = depType=="dependent" ? depEntity : assetEntity
				type.updatedBy = loginUser?.person
				type.comment = params["comment_${depType}_"+idSuf]
				if(createNew){
					type.createdBy = loginUser?.person
				}
				if(!type.save(flush:true)){
					log.error GormUtil.allErrorsString( type )
					errMsg += "<li>Unable to ${createNew ? 'add' : 'update'} dependency between ${assetEntity.assetName} and ${depEntity.assetName}"
				}
			} else {
				errMsg += "<li>The dependency between ${assetEntity.assetName} and ${depEntity.assetName} already exists and therefore ignored"
			}
		}
	  return errMsg
	}
	
	/**
	 * this method is used to verify asset belongs to current project or not, if Bad id found will throw a run time exception
	 * @param assetIdList : list of assetIds
	 * @param project : instance of project
	 * @return : Void
	 */
	def validateAssetList(def assetIdList, def project){
		def assetList = AssetEntity.findAllByIdInList( assetIdList )
		def wrongId = assetList.find{it.project.id != project.id}
		if(wrongId){
			log.error "Updated ${wrongId.assetName} dependency  does not exist in current Project ${project.name}"
			throw new RuntimeException("Updated ${wrongId.assetName} dependency  does not exist in current Project ${project.name}")
		}
	}
	
	/**
	 * @patams, files path, file name startsWith
	 * Delete all files that are match with params criteria
	 */
	def deleteTempGraphFiles(path, startsWith){
		def filePath = ApplicationHolder.application.parentContext.getResource(path).file
		// Get file path
		def dir = new File( "${filePath.absolutePath}" )
		def children = dir.list()
		if ( children ) {
			for (int i=0; i<children.length; i++) {
				// Get filename
				def filename = children[i]
				if ( filename.startsWith(startsWith) ) {
					def jsonFile =  ApplicationHolder.application.parentContext.getResource( "${path}/${filename}" ).getFile()
					jsonFile?.delete()
				}
			}
		}
	}
	/**
	 * @param project
	 * @return list of entities 
	 */
	def getSpecialExportData( project ){
		String queryForSpecialExport = """ ( SELECT
				server.asset_entity_id AS server_id,
				app.asset_entity_id AS app_id,
				server.asset_name AS server_name,
				server.asset_type AS server_type,
				IFNULL(app.asset_name,'') AS app_name,
				IFNULL(sme,'') AS tru, IFNULL(sme2,'') AS tru2,
				IFNULL(mb.name,'') AS move_bundle,
				IF(mb.name='mx','',IFNULL(date_format(mb.start_time,'%m/%d'),'')) AS move_date,
				adb.dependency_bundle AS group_id,
				IFNULL(server.plan_status,'') AS status,
				IFNULL(server.environment, '') AS environment,
				IFNULL(application.criticality, '') AS criticality
			FROM asset_entity server
			JOIN asset_dependency srvappdep ON server.asset_entity_id = srvappdep.dependent_id
			JOIN asset_entity app ON app.asset_entity_id = srvappdep.asset_id AND app.asset_type = 'Application'
			LEFT OUTER JOIN application ON application.app_id = app.asset_entity_id
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=server.move_bundle_id
			LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id = server.asset_entity_id
			WHERE
				server.project_id=${project.id}
				AND server.asset_type IN ('Server','VM', 'Load Balancer','Network', 'Storage', 'Blade')
				ORDER BY app_name, server_name
			)
			UNION DISTINCT
			
			 ( SELECT
				dbsrv.asset_entity_id AS server_id,
				app.asset_entity_id AS app_id,
				dbsrv.asset_name AS server_name,
				dbsrv.asset_type server_type,
				IFNULL(app.asset_name,'') AS app_name,
				IFNULL(sme,'') AS tru, IFNULL(sme2,'') AS tru2,
				IFNULL(mb.name,'') AS move_bundle,
				IF(mb.name='mx','',IFNULL(date_format(mb.start_time,'%m/%d'),'')) AS move_date,
				adb.dependency_bundle AS group_id,
				IFNULL(dbsrv.plan_status,'') AS status,
				IFNULL(app.environment, '') AS environment,
				IFNULL(applic.criticality, '') AS criticality
			FROM asset_entity app
			JOIN application applic ON applic.app_id=app.asset_entity_id
			JOIN asset_dependency appdbdep ON appdbdep.asset_id = app.asset_entity_id #AND appdbdep.type='DB'
			JOIN asset_entity db ON db.asset_entity_id = appdbdep.dependent_id AND db.asset_type = 'Database'
			JOIN asset_dependency dbsrvdep ON dbsrvdep.asset_id = db.asset_entity_id
			JOIN asset_entity dbsrv ON dbsrv.asset_entity_id = dbsrvdep.dependent_id AND dbsrv.asset_type IN ('Server','VM')
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=dbsrv.move_bundle_id
			LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id = dbsrv.asset_entity_id
			WHERE
				app.project_id=${project.id}
				AND app.asset_type = 'Application'
			)
			UNION DISTINCT
			( SELECT
				clustersrv.asset_entity_id AS server_id,
				app.asset_entity_id AS app_id,
				clustersrv.asset_name AS server_name,
				clustersrv.asset_type server_type,
				IFNULL(app.asset_name,'') AS app_name,
				IFNULL(sme,'') AS tru, IFNULL(sme2,'') AS tru2,
				IFNULL(mb.name,'') AS move_bundle,
				IF(mb.name='mx','',IFNULL(date_format(mb.start_time,'%m/%d'),'')) AS move_date,
				adb.dependency_bundle AS group_id,
				IFNULL(clustersrv.plan_status,'') AS status,
				IFNULL(app.environment, '') AS environment,
				IFNULL(applic.criticality, '') AS criticality
			 FROM asset_entity app
			JOIN application applic ON applic.app_id=app.asset_entity_id
			JOIN asset_dependency appdbdep ON appdbdep.asset_id = app.asset_entity_id # AND appdbdep.type='DB'
			JOIN asset_entity db ON db.asset_entity_id = appdbdep.dependent_id AND db.asset_type = 'Database'
			JOIN asset_dependency dbclusterdep ON dbclusterdep.asset_id = db.asset_entity_id
			JOIN asset_entity dbcluster ON dbcluster.asset_entity_id = dbclusterdep.dependent_id AND dbcluster.asset_type = 'Database'
			JOIN asset_dependency clustersrvdep ON clustersrvdep.asset_id = dbcluster.asset_entity_id
			JOIN asset_entity clustersrv ON clustersrv.asset_entity_id = clustersrvdep.dependent_id AND clustersrv.asset_type in ('Server','VM')
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=clustersrv.move_bundle_id
			LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id = clustersrv.asset_entity_id
			WHERE
				app.project_id=${project.id}
				AND app.asset_type = 'Application' )"""

	  def splList =   jdbcTemplate.queryForList( queryForSpecialExport )
											
	  return splList
	}
	
	/**
	 * Delete asset and associated records, User this method when we want to delete an Entity
	 * @param assetEntityInstance
	 * @return
	 */
	def deleteAsset(assetEntity){
		ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.asset = :asset",[asset:assetEntity])
		AssetTransition.executeUpdate("delete from AssetTransition ast where ast.assetEntity = :asset",[asset:assetEntity])
		AssetComment.executeUpdate("update AssetComment ac set ac.assetEntity = null where ac.assetEntity = :asset",[asset:assetEntity])
		ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset = :asset",[asset:assetEntity])
		AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar aev where aev.assetEntity = :asset",[asset:assetEntity])
		ProjectTeam.executeUpdate("update ProjectTeam pt set pt.latestAsset = null where pt.latestAsset = :asset",[asset:assetEntity])
		AssetCableMap.executeUpdate("delete AssetCableMap where assetFrom = :asset",[asset:assetEntity])
		AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
											assetToPort=null where assetTo = :asset""",[asset:assetEntity])
		AssetDependency.executeUpdate("delete AssetDependency where asset = :asset or dependent = :dependent ",[asset:assetEntity, dependent:assetEntity])
		AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset = :asset",[asset:assetEntity])
	}
	
	/**
	 * Used to gather all of the assets for a project by asset class with option to return just individual classes
	 * @param project
	 * @param groups - an array of Asset Types to only return those types (optional)
	 * @return Map of the assets by type along with the AssetOptions dependencyType and dependencyStatus lists
	 */
	Map entityInfo(Project project, List groups=null){
		def map = [ servers:[], applications:[], dbs:[], files:[], networks:[], dependencyType:[], dependencyStatus:[] ]

        if (groups == null || groups.contains(AssetType.SERVER.toString())) {
			map.servers = AssetEntity.executeQuery(
				"SELECT a.id, a.assetName FROM AssetEntity a " + 
				"WHERE assetClass=:ac AND assetType in (:types) AND project=:project ORDER BY assetName", 
				[ac:AssetClass.DEVICE, types:AssetType.getAllServerTypes(), project:project] )
		}

		if (groups == null || groups.contains(AssetType.APPLICATION.toString())) {
			map.applications = Application.executeQuery(
				"SELECT a.id, a.assetName FROM Application a " +
				"WHERE assetClass=:ac AND project=:project ORDER BY assetName", 
				[ac:AssetClass.APPLICATION, project:project] )
		}

		if (groups == null || groups.contains(AssetType.DATABASE.toString())) {
			map.dbs = Database.executeQuery(
				"SELECT d.id, d.assetName FROM Database d " +
				"WHERE assetClass=:ac AND project=:project ORDER BY assetName", 
				[ac:AssetClass.DATABASE, project:project] )
		}
		
		if (groups == null || groups.contains(AssetType.STORAGE.toString())) {
			map.files = Files.executeQuery(
				"SELECT f.id, f.assetName FROM Files f " + 
				"WHERE assetClass=:ac AND project=:project ORDER BY assetName", 
				[ac:AssetClass.STORAGE, project:project] )
		}

		// NOTE - the networks is REALLY OTHER devices other than servers
		if (groups == null || groups.contains(AssetType.NETWORK.toString())) {
			map.networks = AssetEntity.executeQuery(
				"SELECT a.id, a.assetName FROM AssetEntity a " + 
				"WHERE assetClass=:ac AND project=:project AND COALESCE(a.assetType,'') NOT IN (:types) ORDER BY assetName",
				[ ac:AssetClass.DEVICE, project:project, types:AssetType.getNonOtherTypes() ] )
		}

		if (groups == null) {
			map.dependencyType = getDependencyTypes()
			map.dependencyStatus = getDependencyStatuses()
		}

		return map
	}

	/**
	 * Used to get the list of Type used to assign to AssetDependency.type
	 * @return List of the types
	 */
	List getDependencyTypes() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
	}

	/**
	 * Used to get the list of Status used to assign to AssetDependency.status
	 * @return List of the types
	 */
	List getDependencyStatuses() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
	}

	/**
	 * Use to get the list of Asset Environment options
	 * @return List of options
	 */
	List getAssetEnvironmentOptions() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
	}

	/**
	 * Use to get the list of Asset Environment options
	 * @return List of options
	 */
	List getAssetPlanStatusOptions() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
	}

	/**
	 * Used to retrieve the assettype attribute object
	 * @param the name of the attribute
	 * @return the Attribute object 
	 */
	Object getPropertyAttribute(String property) {
		EavAttribute.findByAttributeCode(property)
	}

	/**
	 * Use to get the list of Asset Type options
	 * @return List of options
	 */
	List getPropertyOptions(Object propertyAttrib) {
		return EavAttributeOption.findAllByAttribute(propertyAttrib)?.value
	}

	/**
	 * Get dependent assets of an asset
	 * @param AssetEntity - the asset that we're finding the dependent assets
	 * @return List of Assets
	 */
	List getDependentAssets(Object asset) {
		List list
		if (asset)
			list = AssetDependency.findAll("FROM AssetDependency a WHERE asset=? ORDER BY a.dependent.assetType, a.dependent.assetName",[asset])

		return list
	}

	/**
	 * Get supporting assets of an asset
	 * @param AssetEntity - the asset that we're finding the dependent assets
	 * @return List of Assets
	 */
	List getSupportingAssets(Object asset) {
		List list
		if (asset)
			list = AssetDependency.findAll("FROM AssetDependency a WHERE dependent=? ORDER BY a.asset.assetType,a.asset.assetName", [asset])

		return list
	}

	/**
	 * Returns a list of MoveBundles for a project
	 * @param project - the Project object to look for
	 * @return list of MoveBundles
	 */
	List getMoveBundles(Project project) {
		List list
		if (project)
			list = MoveBundle.findAllByProject(project, [sort:'name'])

		return list
	}

	/**
	 * Returns a list of MoveEvents for a project
	 * @param project - the Project object to look for
	 * @return list of MoveEvents
	 */
	List getMoveEvents(Project project) {
		List list
		if (project)
			list = MoveEvent.findAllByProject(project,[sort:'name'])

		return list
	}

	/**
	 * Used to get the user's preference for the asset list size/rows per page
	 * @return the number of rows to display per page
	 */
	String getAssetListSizePref() {
		// TODO - JPM 08/2014 - seems like we could convert the values to Integer
		return ( userPreferenceService.getPreference("assetListSize") ?: '25' )
	}

	/**
	 * Used to provide the default/common properties shared between all of the Asset Edit views
	 * @param
	 * @return a Map that includes the list of common properties
	 */
	Map getDefaultModelForEdits(String type, Project project, Object asset, Object params) {

		//assert ['Database'].contains(type)

		def assetTypeAttribute = getPropertyAttribute('assetType')
		def validationType = asset.validation
		def configMap = getConfig(type, validationType) 
		def highlightMap = getHighlightedInfo(type, asset, configMap)

		def dependentAssets = getDependentAssets(asset)
		def supportAssets = getSupportingAssets(asset)

		// TODO - JPM 8/2014 - Need to see if Edit even uses the servers list at all. If so, this needs to join the model to filter on assetType 
		def servers = AssetEntity.findAll("FROM AssetEntity WHERE project=:project AND assetClass=:ac AND assetType IN (:types) ORDER BY assetName",
			[project:project, ac: AssetClass.DEVICE, types:AssetType.getServerTypes()])

		Map model = [
			assetTypeAttribute: assetTypeAttribute,
			assetTypeOptions: getPropertyOptions(assetTypeAttribute),
			config: configMap.config,
			customs: configMap.customs,
			dependencyStatus: getDependencyStatuses(),
			dependencyType: getDependencyTypes(),
			dependentAssets: dependentAssets,
			environmentOptions: getAssetEnvironmentOptions()?.value,
			// The name of the asset that is quote escaped to prevent lists from erroring with links
			// TODO - this function should be replace with a generic HtmlUtil method
			escapedName: getEscapedName(asset),
			highlightMap: highlightMap,
			moveBundleList: getMoveBundles(project),
			planStatusOptions: getAssetPlanStatusOptions()?.value,
			project: project,
			projectId: project.id,
			// The page to return to after submitting changes
			redirectTo: params.redirectTo,
			servers: servers,
			supportAssets: supportAssets
		]

		return model
	}

	/**
	 * Used to provide the default/common properties shared between all of the Asset Show views
	 * @param
	 * @return a Map that includes the list of common properties
	 */
	Map getDefaultModelForShows(String type, Project project, Object params, Object assetEntity=null) {

		if (assetEntity == null) {
			assetEntity = AssetEntity.read(params.id)
		}

		def assetComment
		if (AssetComment.find("from AssetComment where assetEntity = ${assetEntity?.id} and commentType = ? and isResolved = ?",['issue',0])) {
			assetComment = "issue"
		} else if (AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id)) {
			assetComment = "comment"
		} else {
			assetComment = "blank"
		}

		def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)

		def validationType = assetEntity.validation
		
		def configMap = getConfig(type, validationType)

		def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntity])
		
		def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntity])

		def highlightMap = getHighlightedInfo(type, assetEntity, configMap)

		def prefValue= userPreferenceService.getPreference("showAllAssetTasks") ?: 'FALSE'

		Map model = [
			assetComment:assetComment, 
			assetCommentList:assetCommentList,
			config:configMap.config, 
			customs:configMap.customs, 
			dependencyBundleNumber:AssetDependencyBundle.findByAsset(assetEntity)?.dependencyBundle ,
			dependentAssets:dependentAssets, 
			errors:params.errors, 
			escapedName:getEscapedName(assetEntity) ,
			highlightMap:highlightMap, 
			errors:params.errors, 
			prefValue:prefValue, 
			project:project,
			redirectTo:params.redirectTo, 
			supportAssets:supportAssets
		]

		return model
	}

	/** 
	 * Used to provide the default properties used for the Asset Dependency views
	 * @param listType - indicates the type of list [Files|]
	 * @param moveEvent
	 * @param params - the params from the HTTP request
	 * @param filters - the map of the filter settings
	 * @return a Map that includes all of the common properties shared between all Asset List views
	 */
	Map getDefaultModelForLists(String listType, Project project, Object fieldPrefs, Object params, Object filters) {

		Map model = [
			assetClassOptions: AssetClass.getClassOptions(),
			assetDependency: new AssetDependency(), 
			attributesList: [],		// Set below
			dependencyStatus: getDependencyStatuses(),
			dependencyType: getDependencyTypes(), 
			event:params.moveEvent, 
			fixedFilter: (params.filter ? true : false),
			filter:params.filter,
			hasPerm: RolePermissions.hasPermission("AssetEdit"),
			justPlanning: userPreferenceService.getPreference("assetJustPlanning") ?: 'true',
			modelPref: null,		// Set below
			moveBundleId:params.moveBundleId, 
			moveBundle:filters?.moveBundleFilter ?: '', 
			moveBundleList: getMoveBundles(project),
			moveEvent: null,		// Set below
			planStatus:filters?.planStatusFilter ?:'', 
			plannedStatus:params.plannedStatus, 
			projectId: project.id,
			sizePref: getAssetListSizePref(), 
			sortIndex:filters?.sortIndex, 
			sortOrder:filters?.sortOrder, 
			staffRoles:taskService.getRolesForStaff(),
			toValidate:params.toValidate,
			unassigned: params.unassigned,
			validation:params.validation
		]

		// Override the Use Just For Planning if a URL requests it (e.g. Planning Dashboard)
		/*
		// This was being added to correct the issue when coming from the Planning Dashboard but there are some ill-effects still
		if (params.justPlanning) 
			model.justPlanning=true
		*/
		
		def moveEvent = null
		if (params.moveEvent && params.moveEvent.isNumber()) {
			moveEvent = MoveEvent.findByProjectAndId(project, params.moveEvent )
		}
		model.moveEvent = moveEvent

		// Get the list of attributes that the user can select for columns
		def attributes = projectService.getAttributes(listType)
		def projectCustoms = project.customFieldsShown+1
		def nonCustomList = project.customFieldsShown != Project.CUSTOM_FIELD_COUNT ? (projectCustoms..Project.CUSTOM_FIELD_COUNT).collect{"custom"+it} : []
		
		// Remove the non project specific attributes and sort them by attributeCode
		def appAttributes = attributes.findAll{
			it.attributeCode!="assetName" && 
			!(it.attributeCode in nonCustomList) && 
			(it.attributeCode != "assetType")
		}

		// Used to display column names in jqgrid dynamically
		def modelPref = [:]
		fieldPrefs.each { key, value ->
			modelPref << [(key): getAttributeFrontendLabel(value, attributes.find{it.attributeCode==value}?.frontendLabel)]
		}
		model.modelPref = modelPref

		// Compose the list of Asset properties that the user can select and use for filters
		def attributesList= (appAttributes).collect{ attribute ->
			[attributeCode: attribute.attributeCode, frontendLabel: getAttributeFrontendLabel(attribute.attributeCode, attribute.frontendLabel)]
		}
		// Sorts attributesList alphabetically
		attributesList.sort { it.frontendLabel }
		model.attributesList = attributesList

		return model
	}

	/**
	 * Common logic on updates
	 * @param
	 */
	def applyExpDateAndRetireDate(Object params, Object tzId) {
		def formatter = new SimpleDateFormat("MM/dd/yyyy")
		def maintExpDate = params.maintExpDate
		if (maintExpDate) {
			params.maintExpDate = GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
		}
		def retireDate = params.retireDate
		if (retireDate) {
			params.retireDate = GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
		}
	}

	/**
	 * This method is used to get config by entityType and validation
	 * @param type,validation
	 * @return
	 */
	def getConfig (def type, def validation) {
		def project = securityService.getUserCurrentProject()
		def allconfig = projectService.getConfigByEntity(type)
		def fields = projectService.getFields(type) + projectService.getCustoms()
		def config = [:]
		def validationType
		def valList=ValidationType.getValuesAsMap()
		valList.each{v ->
			if(v.key==validation)
				validationType=v.value
		}
		fields.each{f ->
			if(allconfig[f.label])
			config << [(f.label):allconfig[f.label]['phase'][validationType]]
			
		}
		
		//used to hide the customs whose fieldImportance is "H"
		def customs = []
		def hiddenConfig = []
		(1..(project.customFieldsShown)).each{i->
			customs << i
			if(config.('custom'+i)=='H')
			hiddenConfig << i
		}
		customs.removeAll(hiddenConfig)
		
		return [project:project, config:config, customs:customs]
	}
	
	/**
	 * Resolves the display string for the shutdownBy, startupBy, testingBy fields by either
	 * getting the name of the person or stripping the prefix for SME/AppOwner or Role
	 * @param byValue - application's shutdownBy, startupBy, or testingBy raw value
	 * @param stripPrefix - if true or not specified, the function will remove the # or @ character from the string
	 * @return value to display
	 */
	def resolveByName(byValue, stripPrefix = true) {
		def byObj = ''
		if(byValue) {
			if (byValue.isNumber()) {
				byObj = Person.read(Long.parseLong(byValue))
			} else {
				byObj = ( stripPrefix && ['@','#'].contains(byValue[0])) ? byValue[1..-1] : byValue
			}
		}
		return byObj
	}

	/**
	 * This method is used to get assets by asset type
	 * @param assetType
	 * @return
	 */
	def getAssetsByType(assetType, project=null) {
		def entities = []
		def types

		if (!project) 
			project = securityService.getUserCurrentProject()

		if (assetType) {
			if (AssetType.getAllServerTypes().contains(assetType)) {
				types = AssetType.getAllServerTypes()
			} else if (AssetType.getStorageTypes().contains(assetType)) {
				types = AssetType.getStorageTypes()
			} else if ( [AssetType.APPLICATION.toString(), AssetType.DATABASE.toString()].contains(assetType)) {
				types = [assetType]
			}
		}

		// log.info "getAssetsByType() types=$types"

		if (types) {
		  entities = AssetEntity.findAllByProjectAndAssetTypeInList(project, types, [sort:'assetName'])
		} else {
			log.warn "getAssetsByType() calledwith unhandled type '$assetType'"
		}	

		/*
			if (assetType=='Server' || assetType=='Blade' || assetType=='VM'){
			  entities = AssetEntity.findAll('from AssetEntity where assetType in (:type) and project = :project order by assetName asc ',
				  [type:["Server", "VM", "Blade"], project:project])
			} else if (assetType != 'Application' && assetType != 'Database' && assetType != 'Files'){
			  entities = AssetEntity.findAll('from AssetEntity where assetType not in (:type) and project = :project order by assetName asc ',
				  [type:["Server", "VM", "Blade", "Application", "Database", "Files"], project:project])
			} else{
			  entities = AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? order by assetName asc ',[assetType, project])
			}
		}
		*/
		return entities
	}
	/**
	 * This method is used to delete assets by asset type
	 * @param type
	 * @param assetList - list of ids for which assets are requested to deleted
	 * @return
	 */
	def deleteBulkAssets(type, assetList){
		def resp
		def assetNames = []
		try{
			//Collecting as a list of data type long
			assetList = assetList.collect{ return Long.parseLong(it) }
			if(type == "dependencies"){
				def assetDeps = AssetDependency.findAllByIdInList(assetList)
				assetDeps.each{ad->
					assetNames << ad?.dependent?.assetName+"  AND Asset  "+ad?.asset?.assetName
					ad.delete()
				}
			}else{
				def assetEntity = AssetEntity.findAllByIdInList(assetList)
				assetEntity.each{ae->
					assetNames << ae.assetName
					deleteAsset(ae)
					ae.delete()
				}
			}
			def names = WebUtil.listAsMultiValueString( assetNames )
			resp = "$type $names deleted."
		}catch(Exception e){
				e.printStackTrace()
				resp = "Error while deleting $type"
		}
		return resp
	}
	/**
	 * This method is used to update assets bundle in dependencies.
	 * @param entity
	 * @param moveBundleId 
	 */
	def updateBundle(def entity, def moveBundleId){
		
		if(entity.moveBundle?.id != moveBundleId){
			entity.moveBundle = MoveBundle.read(moveBundleId)
			if(!entity.save()){
				entity.errors.allErrors.each{
					println it
				}
			}
		}
	}
		
	/*
	 * Update assets cabling data for selected list of assets 
	 * @param list
	 * @param list 
	 */
	def updateAssetsCabling( modelAssetsList, existingAssetsList ){
		modelAssetsList.each{ assetEntity->
			AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
				assetToPort=null where assetTo = ? """,[assetEntity])
			
			AssetCableMap.executeUpdate("delete from AssetCableMap where assetFrom = ?",[assetEntity])
			assetEntityAttributeLoaderService.createModelConnectors( assetEntity )
		}
		/*existingAssetsList.each{ assetEntity->
			AssetCableMap.executeUpdate("""Update AssetCableMap set toAssetRack='${assetEntity.targetRack}',
					toAssetUposition=${assetEntity.targetRackPosition} where assetTo = ? """,[assetEntity])
		}*/
	}

	/*
	 * export cabling data.
	 * @param assetCablesList
	 * @param cablingSheet
	 */
	def cablingReportData( assetCablesList, cablingSheet ){
		for ( int r = 2; r <= assetCablesList.size(); r++ ) {
			cablingSheet.addCell( new Label( 0, r, String.valueOf(assetCablesList[r-2].assetFromPort.type )) )
			cablingSheet.addCell( new Label( 1, r, String.valueOf(assetCablesList[r-2].assetFrom ? assetCablesList[r-2].assetFrom?.id : "" )) )
			cablingSheet.addCell( new Label( 2, r, String.valueOf(assetCablesList[r-2].assetFrom ? assetCablesList[r-2].assetFrom.assetName : "" )) )
			cablingSheet.addCell( new Label( 3, r, String.valueOf(assetCablesList[r-2].assetFromPort.label )) )
			cablingSheet.addCell( new Label( 4, r, String.valueOf(assetCablesList[r-2].assetTo ? assetCablesList[r-2].assetTo?.id : "" )) )
			cablingSheet.addCell( new Label( 5, r, String.valueOf(assetCablesList[r-2].assetTo ? assetCablesList[r-2].assetTo?.assetName :"" )) )
			if(assetCablesList[r-2].assetFromPort.type !='Power'){
				cablingSheet.addCell( new Label( 6, r, String.valueOf(assetCablesList[r-2].assetToPort ? assetCablesList[r-2].assetToPort?.label :"" )) )
			}else{
				cablingSheet.addCell( new Label( 6, r, String.valueOf(assetCablesList[r-2].toPower?:"" )) )
			}
			cablingSheet.addCell( new Label( 7, r, String.valueOf(assetCablesList[r-2].cableComment?:"" )) )
			cablingSheet.addCell( new Label( 8, r, String.valueOf(assetCablesList[r-2].cableColor?:"" )) )
			if(assetCablesList[r-2].assetFrom.sourceRoom){
				cablingSheet.addCell( new Label( 9, r, String.valueOf(assetCablesList[r-2].assetFrom?.rackSource?.location+"/"+assetCablesList[r-2].assetFrom?.sourceRoom+"/"+assetCablesList[r-2].assetFrom?.sourceRack )) )
			}else if(assetCablesList[r-2].assetFrom.targetRoom){
				cablingSheet.addCell( new Label( 9, r, String.valueOf(assetCablesList[r-2].assetFrom?.rackTarget?.location+"/"+assetCablesList[r-2].assetFrom?.targetRoom+"/"+assetCablesList[r-2].assetFrom?.targetRack )) )
			}else{
				cablingSheet.addCell( new Label( 9, r, '') )
			}
			cablingSheet.addCell( new Label( 10, r, String.valueOf(assetCablesList[r-2].cableStatus?:"" )) )
			cablingSheet.addCell( new Label( 11, r, String.valueOf(assetCablesList[r-2].assetLoc?: "" )) )
		}
	}

	/**
	 * used to get the frontEndLabel of particular attribute
	 * @param attribute
	 * @return frontEndLabel
	 */
	def getAttributeFrontendLabel(attributeCode, frontendLabel){
		def project = securityService.getUserCurrentProject()
		return (attributeCode.contains('custom') && project[attributeCode])? project[attributeCode]:frontendLabel
	}
	/**
	 * Used to get the customised query based on the application preference
	 * @param appPref(List of key value column preferences)
	 * @return query,joinQuery
	 */
	def getAppCustomQuery(appPref){
		def query = ""
		def joinQuery = ""
		appPref.each{key,value->
			switch(value){
				case 'sme':
						query +="CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS sme,"
						joinQuery +="\n LEFT OUTER JOIN person p ON p.person_id=a.sme_id \n"
						break;
				case 'sme2':
						query +="CONCAT(CONCAT(p1.first_name, ' '), IFNULL(p1.last_name,'')) AS sme2,"
						joinQuery +="\n LEFT OUTER JOIN person p1 ON p1.person_id=a.sme2_id \n"
						break;
				case 'modifiedBy':
						query +="CONCAT(CONCAT(p2.first_name, ' '), IFNULL(p2.last_name,'')) AS modifiedBy,"
						joinQuery +="\n LEFT OUTER JOIN person p2 ON p2.person_id=ae.modified_by \n"
						break;
				case 'lastUpdated':
						query +="ee.last_updated AS ${value},"
						joinQuery +="\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n"
						break;
				case 'moveBundle':
						query +="mb.name AS moveBundle,"
						break;
				case 'event':
						query +="me.move_event_id AS event,"
						joinQuery +="\n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id \n"
						break;
				case 'appOwner':
						query +="CONCAT(CONCAT(p3.first_name, ' '), IFNULL(p3.last_name,'')) AS appOwner,"
						joinQuery +="\n LEFT OUTER JOIN person p3 ON p3.person_id= ae.app_owner_id \n"
						break;
				case ~/appVersion|appVendor|appTech|appAccess|appSource|license|businessUnit|appFunction|criticality|userCount|userLocations|useFrequency|drRpoDesc|drRtoDesc|shutdownFixed|moveDowntimeTolerance|testProc|startupProc|url|shutdownBy|shutdownDuration|startupBy|startupFixed|startupDuration|testingBy|testingFixed|testingDuration/:
						query +="a.${WebUtil.splitCamelCase(value)} AS ${value},"
						break;
				case ~/custom1|custom2|custom3|custom4|custom5|custom6|custom7|custom8|custom9|custom10|custom11|custom12|custom13|custom14|custom15|custom16|custom17|custom18|custom19|custom20|custom21|custom22|custom23|custom24|custom25|custom26|custom27|custom28|custom29|custom30|custom31|custom32|custom33|custom34|custom35|custom36|custom37|custom38|custom39|custom40|custom41|custom42|custom43|custom44|custom45|custom46|custom47|custom48|custom49|custom50|custom51|custom52|custom53|custom54|custom55|custom56|custom57|custom58|custom59|custom60|custom61|custom62|custom63|custom64/:
						query +="ae.${value} AS ${value},"
						break;
				case ~/validation|latency|planStatus/:
				break;
				default:
						query +="ae.${WebUtil.splitCamelCase(value)} AS ${value},"
			}
		}
		return [ query:query, joinQuery:joinQuery ]
	}
	/**
	 * Returns the default optional/customizable columns 
	 * @param prefName - the preference name for the various asset lists
	 * @return appPref
	 */
	 // TODO : JPM 9/2014 : Rename getExistingPref method to getColumnPreferences
	Map getExistingPref(prefName){
		def colPref
		def existingPref = userPreferenceService.getPreference(prefName)
		
		if (existingPref) {	
			// TODO : JPM 9/2014 : I'm assuming that the JSON.parse function could throw an error if the json is corrupt so there should be a try/catch
			colPref = JSON.parse(existingPref)
		}

		if (! colPref) {
		
			switch (prefName) {
				case 'App_Columns':
					colPref = ['1':'sme', '2':'validation', '3':'planStatus', '4':'moveBundle']
					break
				case 'Asset_Columns':
					colPref = ['1':'targetLocation','2':'targetRack','3':'assetTag','4':'serialNumber']
					break
				case 'Physical_Columns':
					colPref = ['1':'targetLocation','2':'targetRack','3':'assetTag','4':'serialNumber']
					break
				case 'Database_Columns':
					colPref = ['1':'dbFormat','2':'size','3':'planStatus','4':'moveBundle']
					break
				case 'Storage_Columns':
					colPref = ['1':'fileFormat','2':'size','3':'planStatus','4':'moveBundle']
					break
				case 'Task_Columns':
					colPref = ['1':'assetName','2':'assetType','3':'assignedTo','4':'role', '5':'category']
					break
				case 'Model_Columns':
					colPref = ['1':'description','2':'assetType','3':'powerUse','4':'modelConnectors']
					break
				case 'Dep_Columns':
					colPref = ['1':'frequency','2':'comment']
					break
			}
		}

		return colPref
	}

	/**
	 * Used to save cabling data to database while importing.
	 * 
	 */
	def saveImportCables(cablingSheet){
		def warnMsg=""
		def cablingSkipped = 0
		def cablingUpdated = 0
		def project = securityService.getUserCurrentProject()
		for ( int r = 2; r < cablingSheet.rows; r++ ) {
			int cols = 0 ;
			def isNew = false
			def cableType=cablingSheet.getCell( cols, r ).contents.replace("'","\\'")
			def fromAsset = AssetEntity.get(NumberUtils.toDouble(cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'"), 0).round())
			def fromAssetName=cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def fromConnectorLabel =cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			//if assetId is not there then get asset from assetname and fromConnector
			if(!fromAsset && fromAssetName){
				fromAsset = AssetEntity.findByAssetNameAndProject( fromAssetName, project)?.find{it.model.modelConnectors?.label.contains(fromConnectorLabel)}
			}
			def toAsset = AssetEntity.get(NumberUtils.toDouble(cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'"), 0).round())
			def toAssetName=cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def toConnectorLabel=cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def toConnectorTemp
			if(cableType=='Power')
				toConnectorTemp = fromConnectorLabel
			else
				toConnectorTemp = toConnectorLabel
			//if toAssetId is not there then get asset from assetname and toConnectorLabel
			if(!toAsset && toAssetName){
				if(cableType!='Power')
					toAsset = AssetEntity.findByAssetNameAndProject( toAssetName, project)?.find{it.model.modelConnectors?.label.contains(toConnectorLabel)}
				else
					toAsset = fromAsset
			}
			def cableComment = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def cableColor = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def room = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def cableStatus = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def roomType = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			if(fromAsset){
				def fromAssetConnectorsLabels= fromAsset.model?.modelConnectors?.label
				if(fromAssetConnectorsLabels && fromAssetConnectorsLabels?.contains(fromConnectorLabel)){
					def fromConnector = fromAsset.model?.modelConnectors.find{it.label == fromConnectorLabel}
					def assetCable = AssetCableMap.findByAssetFromAndAssetFromPort(fromAsset,fromConnector)
					if(toAsset){
						def toAssetconnectorLabels= toAsset.model?.modelConnectors?.label
						if(toAssetconnectorLabels.contains(toConnectorTemp)){
							if(cableType=='Power'){
								assetCable.toPower = toConnectorLabel
							}else{
								def toConnector = toAsset.model?.modelConnectors.find{it.label == toConnectorTemp}
								def previousCable = AssetCableMap.findByAssetToAndAssetToPort(toAsset,toConnector)
								if(previousCable !=assetCable){
									// Release the connection from other port to connect with FromPorts
									AssetCableMap.executeUpdate("""Update AssetCableMap set assetTo=null,assetToPort=null, cableColor=null
																			where assetTo = ? and assetToPort = ? """,[toAsset, toConnector])
								}
								assetCable.assetToPort = toConnector
							}
						}
						assetCable.assetTo = toAsset
					}else {
						assetCable.assetTo = null
						assetCable.assetToPort = null
						assetCable.toPower = ''
					}
					
					if(AssetCableMap.constraints.cableColor.inList.contains(cableColor))
						assetCable.cableColor = cableColor
						
					assetCable.cableComment = cableComment
					assetCable.cableStatus = cableStatus
					
					if(AssetCableMap.constraints.assetLoc.inList.contains(roomType))
						assetCable.assetLoc= roomType
						
					if(assetCable.dirtyPropertyNames.size()){
						assetCable.save(flush:true)
						cablingUpdated+=1
					}
					
				}else{
					warnMsg += "<li>row "+(r+1)+" with connector $fromConnectorLabel and Asset Name $fromAssetName does not exist & skipped.</li>"
					cablingSkipped+=1
				}
				
			}else{
				warnMsg += "<li>row "+(r+1)+" with connector $fromConnectorLabel and Asset Name $fromAssetName does not exist & skipped.</li>"
				cablingSkipped+=1
			}
		}
		return [warnMsg:warnMsg, cablingSkipped:cablingSkipped, cablingUpdated:cablingUpdated]
	}

	/**
	 * Used to create or fetch target asset cables.
	 *
	 */
	def createOrFetchTargetAssetCables(assetEntity){
		def cableExist = AssetCableMap.findAllByAssetFromAndAssetLoc(assetEntity,'T')?:[]
		if(!cableExist){
			def sourceCables=AssetCableMap.findAllByAssetFromAndAssetLoc(assetEntity,'S')
			sourceCables.each{
				def targetCable = new AssetCableMap()
				targetCable.cable = it.cable
				targetCable.cableStatus = AssetCableStatus.EMPTY
				targetCable.assetFrom = it.assetFrom
				targetCable.assetFromPort = it.assetFromPort
				targetCable.assetLoc= 'T'
				if(!targetCable.save(flush:true)){
					def etext = "Unable to create AssetCableMap" +
	                GormUtil.allErrorsString( targetCable )
					println etext
				}
				cableExist << targetCable
			}
		}
		return cableExist
	}

	/**
	 * used to add the css for the labels which fieldImportance is 'C','I'
	 * @param forWhom
	 * @param assetEntity
	 * @param configMap
	 * @return
	 */
	def getHighlightedInfo( forWhom, assetEntity, configMap){
		def fields = projectService.getFields(forWhom) + projectService.getCustoms()
		def highlightMap = [:]
		fields.each{f->
			def configMaps=configMap.config
			if(configMaps.(f.label) in ['C','I'] && (assetEntity && !assetEntity.(f.label))){
				highlightMap << [(f.label):'highField']
			}
		}
		return highlightMap
	}
	
	/** 
	 * This is used to escape quotes in a string to be used in Javascript
	 * TODO : JPM 9/2014 : getEscapeName should be refactored into a reusable function in String or HtmlUtil as it should not be SOOOOO tied to an asset
	 */
	def getEscapedName(assetEntity, ignoreSingleQuotes = false) {
		def name = ''
		def size = assetEntity.assetName?.size() ?: 0
		for (int i = 0; i < size; ++i)
			if (assetEntity.assetName[i] == "'")
				name = name + "\\'"
			else if (ignoreSingleQuotes && assetEntity.assetName[i] == '"')
				name = name + '\\"'
			else
				name = name + assetEntity.assetName[i]
		return name
	}

	def getManufacturers(assetType) {
		return Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
	}

	/**
	 * This method is used to sort model by status full, valid and new to display at Asset CRUD
	 * @param manufacturerInstance : instance of Manufacturer for which model list is requested
	 * @return : model list 
	 */
	def getModelSortedByStatus (manufacturerInstance) {
		def models = Model.findAllByManufacturer( manufacturerInstance,[sort:'modelName',order:'asc'] )
		def modelListFull = models.findAll{it.modelStatus == 'full'}
		def modelListValid = models.findAll{it.modelStatus == 'valid'}
		def modelListNew = models.findAll{!['full','valid'].contains(it.modelStatus)}
		models = ['Validated':modelListValid, 'Unvalidated':modelListFull+modelListNew ]
		
		return models
	}

	def getRooms(project) {
		return Room.findAll("FROM Room WHERE project =:project order by location, roomName", [project:project])
	}


	/**
	 * Used to retrieve the values of DEVICE properties based on the attribute name
	 * @param asset - the asset to retrieve the property from
	 * @param attribute - the attribute name
	 * @return The string value of the object property value
	 */
	Object getAssetAttributeValue(Object asset, String attribute) {
		def value
		def fieldMap = [Location:'location', Room:'roomName', Rack:'tag']
		switch (attribute) {
			case ~/XXX(source|target)(Location|Room|Rack)/:
				def disp = Matcher.lastMatcher[0][1]	// Disposition
				def prop = Matcher.lastMatcher[0][2]

				def assetProperty = (prop == 'Rack' ? 'rack' : 'room') + disp.capitalize()
				def refProperty = fieldMap[prop]
				log.debug "getAssetAttributeValue() assetProperty=$assetProperty,  refProperty=$refProperty, disp=$disp, prop=$prop"
				if (!assetProperty || !refProperty) {
					throw new RuntimeException("getAssetAttributeValue() unable to map '$attribute'")
				}

				value = asset[assetProperty][refProperty]?.toString()
				break
			default:
				value = String.valueOf(asset[attribute])
				log.debug "getAssetAttributeValue() attribute $attribute, value $value"
		}
		return StringUtil.defaultIfEmpty(value, '')
	}

	/**
	 * Download data form Asset Entity table into Excel file
	 * @param Datatransferset,Project,Movebundle
	 **/
	def export(params) {
		def key = params.key
		def projectId = params.projectId

		// Helper closure that returns the size of an object or zero (0) if it is null
		def sizeOf = { obj -> (obj ? obj.size : 0)}

		try {
		
			java.io.File temp = java.io.File.createTempFile("assetEntityExport_" + UUID.randomUUID().toString(),".xls");
			temp.deleteOnExit();
			
			progressService.updateData(key, 'filename', temp.getAbsolutePath())
			
			
			def progressCount = 0
			def progressTotal = 0
			def missingHeader = ""
			
			//get project Id
			def dataTransferSet = params.dataTransferSet
			def bundleNameList = new StringBuffer()
			def bundles = []
			def principal = params.username
			def loginUser = UserLogin.findByUsername(principal)
			def started
			
			def bundle = params.bundle
			def bundleSize = bundle.size()
			bundleNameList.append(bundle[0] != "" ? (bundleSize==1 ? MoveBundle.read( bundle[0] ).name : bundleSize+'Bundles') : 'All')

			def dataTransferSetInstance = DataTransferSet.findById( dataTransferSet )

			def project = Project.get(projectId)
			if ( project == null) {
				progressService.update(key, 100, 'Cancelled', 'Project is required.')
				return;
			}

			// Maps for each class for mapping spreadsheet column names to the domain properties
			def serverDTAMap, appDTAMap, dbDTAMap, fileDTAMap

			// Will hold the list of the assets for each of the classes
			List asset, application, database, files

			def assetEntityInstance

			def serverSheet
			def appSheet
			def dbSheet
			def storageSheet
			def titleSheet
			def exportedEntity = ""

			def serverMap = [:]
			def serverSheetColumnNames = [:]
			def serverColumnNameList = new ArrayList()
			def serverSheetNameMap = [:]
			def serverDataTransferAttributeMapSheetName

			def appMap = [:]
			def appSheetColumnNames = [:]
			def appColumnNameList = new ArrayList()
			def appSheetNameMap = [:]
			def appDataTransferAttributeMapSheetName

			def dbMap = [:]
			def dbSheetColumnNames = [:]
			def dbColumnNameList = new ArrayList()
			def dbSheetNameMap = [:]
			def dbDataTransferAttributeMapSheetName

			def fileMap = [:]
			def storageSheetColumnNames = [:]
			def fileColumnNameList = new ArrayList()
			def storageSheetNameMap = [:]
			def fileDataTransferAttributeMapSheetName


			// Flags to indicate which tabs to export based on which checkboxes selected in the UI
			boolean doDevice = params.asset=='asset'
			boolean doApp = params.application=='application'
			boolean doDB = params.database=='database'
			boolean doStorage = params.files=='files'
			boolean doDependency = params.dependency=='dependency'
			boolean doRoom = params.room=='room'
			boolean doRack = params.rack=='rack'
			boolean doCabling = params.cabling=='cable'
			boolean doComment = params.comment=='comment'

			//
			// Load the asset lists and property map files based on ALL or selected bundles for the classes selected for export
			//
			String query = ' WHERE d.project=:project '
			Map queryParams = [project:project]
			// Setup for multiple bundle selection
			if (bundle[0]) {
				for ( int i=0; i< bundleSize ; i++ ) {
					bundles << bundle[i].toLong()
				}
				query += ' AND d.moveBundle.id IN (:bundles)'
				queryParams.bundles = bundles
			}
			if ( doDevice ) {
				asset = AssetEntity.findAll('FROM AssetEntity d' + query + " AND d.assetClass='${AssetClass.DEVICE}'", queryParams)
			}
			if ( doApp ) {
				application = Application.findAll('From Application d' + query,  queryParams)
				// application = Application.findAll( "from Application m where m.project = :project and m.moveBundle.id in ( :bundles )", [project:project, bundles: bundles] )
			}
			if ( doDB ) {
				database = Database.findAll('From Database d' + query,  queryParams)
				// database = Database.findAll( "from Database m where m.project = :project and m.moveBundle.id in ( :bundles )",	[project:project, bundles: bundles] )
			}
			if ( doStorage ) {
				files = Files.findAll('From Files d' + query,  queryParams)
				// files = Files.findAll( "from Files m where m.project = :project and m.moveBundle.id in ( :bundles )", [project:project, bundles: bundles] )
			}

			// Have to load the maps because we update the column names across the top for all sheets
			serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Devices" )
			appDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Applications" )
			dbDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Databases" )
			fileDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Files" )
			
			// Compute the total assets to be exported, used for progress meter
			progressTotal = sizeOf(asset) + sizeOf(application) + sizeOf(database) + sizeOf(files)
			
			//get template Excel
			def workbook
			def book
			started = new Date()
			def assetDepBundleList = AssetDependencyBundle.findAllByProject(project)
			def filenametoSet = dataTransferSetInstance.templateFilename
			def file =  ApplicationHolder.application.parentContext.getResource(filenametoSet).getFile()
			// Going to use temporary file because we were getting out of memory errors constantly on staging server

			//set MIME TYPE as Excel
			def exportType = filenametoSet.split("/")[2]
			exportType = exportType.substring(0,exportType.indexOf("Master_template.xls"))

			SimpleDateFormat exportFileFormat = new SimpleDateFormat("yyyyMMdd")
			SimpleDateFormat stdDateFormat = new SimpleDateFormat("MM-dd-yyyy")

			def tzId = params.tzId
			def currDate = TimeUtil.convertInToUserTZ(TimeUtil.nowGMT(),tzId)
			def exportDate = exportFileFormat.format(currDate)
			def filename = project?.name?.replace(" ","_")+"-"+bundleNameList.toString()

			log.info "export() - Loading appDepBundle took ${TimeUtil.elapsed(started)}"
			started = new Date()

			//create workbook and sheet
			WorkbookSettings wbSetting = new WorkbookSettings()
			wbSetting.setUseTemporaryFileDuringWrite(true)
			workbook = Workbook.getWorkbook( file, wbSetting )
			book = Workbook.createWorkbook( new FileOutputStream(temp), workbook )
			log.info "export() - Creating workbook took ${TimeUtil.elapsed(started)}"
			started = new Date()

			def sheetNames = book.getSheetNames() as List

			// Helper closure used to retrieve the s
			def getWorksheet = { sheetName ->
				if (sheetNames.contains(sheetName)) {
					def index = sheetNames.findIndexOf( { it == sheetName } )
					return book.getSheet( sheetNames[index] )
				} else {
					throw new RuntimeException("Unable to find sheet $sheetName in the uploaded spreadsheet")
				}
			}

			//
			// Load maps appropriately based on if we're exporting the tab
			//
			def serverCol, appCol, dbCol, filesCol

			// TODO : JPM : The following 4 blocks of code should be able to be reduced to a shared closure

			// Device
			serverDTAMap.eachWithIndex { item, pos ->
				serverMap.put( item.columnName, null )
				serverColumnNameList.add(item.columnName)
				serverSheetNameMap.put( "sheetName", item.sheetName?.trim() )
			}
			serverMap.put("DepGroup", null )
			serverColumnNameList.add("DepGroup")
			serverSheet = getWorksheet('Devices')
			serverCol = serverSheet.getColumns()
			for ( int c = 0; c < serverCol; c++ ) {
				def serverCellContent = serverSheet.getCell( c, 0 ).contents
				serverSheetColumnNames.put(serverCellContent, c)
				if( serverMap.containsKey( serverCellContent ) ) {
					serverMap.put( serverCellContent, c )
				}
			}

			// Application
			appDTAMap.eachWithIndex { item, pos ->
				appMap.put( item.columnName, null )
				appColumnNameList.add(item.columnName)
				appSheetNameMap.put( "sheetName", item.sheetName?.trim() )
			}
			appMap.put("DepGroup", null )
			appColumnNameList.add("DepGroup")
			appSheet = getWorksheet('Applications')
			appCol = appSheet.getColumns()
			for ( int c = 0; c < appCol; c++ ) {
				def appCellContent = appSheet.getCell( c, 0 ).contents
				appSheetColumnNames.put(appCellContent, c)
				if( appMap.containsKey( appCellContent ) ) {
					appMap.put( appCellContent, c )

				}
			}

			// Database
			dbDTAMap.eachWithIndex { item, pos ->
				dbMap.put( item.columnName, null )
				dbColumnNameList.add(item.columnName)
				dbSheetNameMap.put( "sheetName", item.sheetName?.trim() )
			}
			dbMap.put("DepGroup", null )
			dbColumnNameList.add("DepGroup")
			dbSheet = getWorksheet('Databases')
			dbCol = dbSheet.getColumns()
			for ( int c = 0; c < dbCol; c++ ) {
				def dbCellContent = dbSheet.getCell( c, 0 ).contents
				dbSheetColumnNames.put(dbCellContent, c)
				if( dbMap.containsKey( dbCellContent ) ) {
					dbMap.put( dbCellContent, c )
				}
			}

			// Storage
			fileDTAMap.eachWithIndex { item, pos ->
				fileMap.put( item.columnName, null )
				fileColumnNameList.add(item.columnName)
				storageSheetNameMap.put( "sheetName", item.sheetName?.trim() )
			}
			fileMap.put("DepGroup", null )
			fileColumnNameList.add("DepGroup")
			storageSheet = getWorksheet('Storage')
			filesCol = storageSheet.getColumns()
			for ( int c = 0; c < filesCol; c++ ) {
				def fileCellContent = storageSheet.getCell( c, 0 ).contents
				storageSheetColumnNames.put(fileCellContent, c)
				if( fileMap.containsKey( fileCellContent ) ) {
					fileMap.put( fileCellContent, c )
				}
			}

			log.info "export() - Valdating columns took ${TimeUtil.elapsed(started)}"
			started = new Date()

			// Helper closure to create a text list from an array for debugging
			def xportList = { list ->
				def out = ''
				def x = 0
				list.each { out += "$x=$it\n"; x++}
				return out
			}

			//calling method to check for Header
			def serverCheckCol = checkHeader( serverColumnNameList, serverSheetColumnNames, missingHeader )
			def appCheckCol = checkHeader( appColumnNameList, appSheetColumnNames, missingHeader )
			def dbCheckCol = checkHeader( dbColumnNameList, dbSheetColumnNames, missingHeader )
			def filesCheckCol = checkHeader( fileColumnNameList, storageSheetColumnNames, missingHeader )
			// Statement to check Headers if header are not found it will return Error message
			if ( serverCheckCol == false || appCheckCol == false || dbCheckCol == false || filesCheckCol == false) {
				missingHeader = missingHeader.replaceFirst(",","")
				//flash.message = " Column Headers : ${missingHeader} not found, Please check it."
				//redirect( action:assetImport, params:[message:flash.message] )

				progressService.update(key, 100, 'Cancelled', " Column Headers : ${missingHeader} not found, Please check it.")

				return
			} else {
				//Add Title Information to master SpreadSheet
				titleSheet = book.getSheet("Title")
				SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
				if(titleSheet != null) {
					def titleInfoMap = new ArrayList();
					titleInfoMap.add (project.client )
					titleInfoMap.add( projectId )
					titleInfoMap.add( project.name )
					titleInfoMap.add( partyRelationshipService.getProjectManagers(projectId) )
					titleInfoMap.add( format.format( currDate ) )
					titleInfoMap.add( loginUser.person )
					titleInfoMap.add( bundleNameList )
					partyRelationshipService.exportTitleInfo(titleInfoMap,titleSheet)
					titleSheet.addCell(new Label(0,30,"Note: All times are in ${tzId ? tzId : 'EDT'} time zone"))
				}

				//update data from Asset Entity table to EXCEL
				def assetSize = sizeOf(asset)
				def appSize = sizeOf(application)
				def dbSize = sizeOf(database)
				def fileSize = sizeOf(files)
				def serverColumnNameListSize = sizeOf(serverColumnNameList)
				def appcolumnNameListSize = sizeOf(appColumnNameList)
				def dbcolumnNameListSize = sizeOf(dbColumnNameList)
				def filecolumnNameListSize = fileColumnNameList.size()

				// update column header
				updateColumnHeaders(serverSheet, serverDTAMap, serverSheetColumnNames, project)
				updateColumnHeaders(appSheet, appDTAMap, appSheetColumnNames, project)
				updateColumnHeaders(dbSheet, dbDTAMap, dbSheetColumnNames, project)
				updateColumnHeaders(storageSheet, fileDTAMap, storageSheetColumnNames, project)

				log.info "export() - Updating spreadsheet headers took ${TimeUtil.elapsed(started)}"
				started = new Date()
				
				//
				// Device Export
				//
				if ( doDevice ) {
					exportedEntity += "S"
					for ( int r = 1; r <= assetSize; r++ ) {
						progressCount++
						progressService.update(key, ((int)((progressCount / progressTotal) * 100)), 'In progress')
						
						//Add assetId for walkthrough template only.
						if( serverSheetColumnNames.containsKey("assetId") ) {
							def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
							def addAssetId = new Number(0, r, (asset[r-1].id))
							serverSheet.addCell( addAssetId )
						}
						
						for ( int coll = 0; coll < serverColumnNameListSize; coll++ ) {
							def addContentToSheet
							def attribute = serverDTAMap.eavAttribute.attributeCode[coll]
							def colName = serverColumnNameList.get(coll)
							def colNum = serverMap[colName]
							def a = asset[r-1]

							// log.debug "coll=$coll, colNum=$colNum, colName=$colName, attribute=$attribute"

							if (attribute && a.(serverDTAMap.eavAttribute.attributeCode[coll]) == null ) {
								// Skip populating the cell if the value is null
								continue
							}

							switch(colName) {
								case 'DepGroup':
									// TODO : JPM 9/2014 : Should load the dependency bundle list into memory so we don't do queries for each record
									def depGroup = assetDepBundleList.find{it.asset.id==a.id}?.dependencyBundle?.toString()
									addContentToSheet = new Label(colNum, r, StringUtil.defaultIfEmpty(depGroup, ''))
									break
								case 'usize':	
									def usize = a.model?.usize ?: 0
									addContentToSheet = new jxl.write.Number(colNum, r, (Double)usize )
									break
								case ~/SourcePos|TargetPos/:
									def pos = a[attribute] ?: 0
									// Don't bother populating position if it is a zero
									if (pos == 0) 
										continue
									addContentToSheet = new jxl.write.Number(colNum, r, (Double)pos )
									break
								case ~/Retire|MaintExp|Modified Date/:
									addContentToSheet = new Label(colNum, r, stdDateFormat.format(a[attribute]) )
									break

								default:
									def value = StringUtil.defaultIfEmpty( String.valueOf(a[attribute]), '')
									addContentToSheet = new Label(colNum, r, value)
							}

							serverSheet.addCell( addContentToSheet )
						}
					}
					log.info "export() - processing assets took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}

				//
				// Application Export
				//
				if ( doApp ) {
					exportedEntity += 'A'

					// This determines which columns are added as Number vs Label
					def numericCols = []

					// Flag to know if the AppId Column exists
					def idColName = 'appId'
					def hasIdCol = appSheetColumnNames.containsKey(idColName)

					def rowNum = 0
					application.each { app ->
						progressCount++
						progressService.update(key, ((int)((progressCount / progressTotal) * 100)), 'In progress')
						rowNum++

						// Add the appId column to column 0 if it exists
						if (hasIdCol) {
							appSheet.addCell( new jxl.write.Number(appSheetColumnNames[idColName], rowNum, (Double)app.id) )
						}
								
						for (int i=0; i < appColumnNameList.size(); i++) {
							def colName = appColumnNameList[i]

							// If the column isn't in the spreadsheet we'll skip over it
							if ( ! appSheetColumnNames.containsKey(colName)) {
								log.info "export() : skipping column $colName that is not in spreadsheet"
								continue
							}

							// Get the column number in the spreadsheet that contains the column name
							def colNum = appSheetColumnNames[colName]

							// Get the asset column name via the appDTAMap which is indexed to match the appColumnNameList
							def assetColName = appDTAMap.eavAttribute.attributeCode[i]

							def colVal = ''
							switch(colName) {
								case 'AppId':
									colVal = app.id
									break
								case 'AppOwner':
									colVal = app.appOwner
									break
								case 'DepGroup':
									// Find the Dependency Group that this app is bound to
									colVal = assetDepBundleList.find {it.asset.id == app.id }?.dependencyBundle?.toString() ?: ''
									break
								case ~/ShutdownBy|StartupBy|TestingBy/:
									colVal = app[assetColName] ? this.resolveByName(app[assetColName], false)?.toString() : ''
									break
								case ~/ShutdownFixed|StartupFixed|TestingFixed/:
									colVal = app[assetColName] ? 'Yes' : 'No'
									//log.info "export() : field class type=$app[assetColName].className()}"
									break
								case ~/Retire|MaintExp|Modified Date/:
									colVal = app[assetColName] ? stdDateFormat.format(app[assetColName]) : ''
									break
								default:
									colVal = app[assetColName]
							}

							// log.info("export() : rowNum=$rowNum, colNum=$colNum, colVal=$colVal")

							if ( colVal != null) {

								if (colVal?.class.name == 'Person') {
									colVal = colVal.toString()
								}

								def cell
								if ( numericCols.contains(colName) )
									cell = new Number(colNum, rowNum, (Double)colVal)
								else
									cell = new Label( colNum, rowNum, String.valueOf(colVal) )

								appSheet.addCell( cell )
							}
						}
					}
					log.info "export() - processing apps took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}

				//
				// Database
				//
				if ( doDB ) {
					exportedEntity += "D"
					for ( int r = 1; r <= dbSize; r++ ) {
						progressCount++
						progressService.update(key, ((int)((progressCount / progressTotal) * 100)), 'In progress')
						//Add assetId for walkthrough template only.
						if( dbSheetColumnNames.containsKey("dbId") ) {
							def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
							def addDBId = new Number(0, r, (database[r-1].id))
							dbSheet.addCell( addDBId )
						}
						for ( int coll = 0; coll < dbcolumnNameListSize; coll++ ) {
							def addContentToSheet
							def attribute = dbDTAMap.eavAttribute.attributeCode[coll]
							//if attributeCode is sourceTeamMt or targetTeamMt export the teamCode
							def colName = dbColumnNameList.get(coll)
							if(colName == "DepGroup"){
								def depGroup = assetDepBundleList.find{it.asset.id==database[r-1].id}?.dependencyBundle?.toString()
								addContentToSheet = new Label(dbMap[colName], r, depGroup ?:"" )
							} else if(attribute in ["retireDate", "maintExpDate", "lastUpdated"]){
								addContentToSheet = new Label(dbMap[colName], r, (database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll]) ? stdDateFormat.format(database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll])) :''))
							} else if ( database[r-1][attribute] == null ) {
								addContentToSheet = new Label(  dbMap[colName], r, "" )
							}else {
								if( bundleMoveAndClientTeams.contains(dbDTAMap.eavAttribute.attributeCode[coll]) ) {
									addContentToSheet = new Label( dbMap[colName], r, String.valueOf(database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll]).teamCode) )
								}else {
									addContentToSheet = new Label( dbMap[colName], r, String.valueOf(database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll])) )
								}
							}
							dbSheet.addCell( addContentToSheet )
						}
					}
					log.info "export() - processing databases took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}

				//
				// Storage ( files )
				//
				if ( doStorage ) {
					exportedEntity += "F"
					for ( int r = 1; r <= fileSize; r++ ) {
						progressCount++
						progressService.update(key, ((int)((progressCount / progressTotal) * 100)), 'In progress')
						
						// Add assetId for walkthrough template only.
						if ( storageSheetColumnNames.containsKey("filesId") ) {
							def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
							def addFileId = new Number(0, r, (files[r-1].id))
							storageSheet.addCell( addFileId )
						}

						for ( int coll = 0; coll < filecolumnNameListSize; coll++ ) {
							def addContentToSheet
							def attribute = fileDTAMap.eavAttribute.attributeCode[coll]
							def colName = fileColumnNameList.get(coll)
							if (colName == "DepGroup") {
								def depGroup = assetDepBundleList.find{it.asset.id==files[r-1].id}?.dependencyBundle?.toString()
								addContentToSheet = new Label(fileMap[colName], r, depGroup ?:"" )
							} else if(attribute == "retireDate" || attribute == "maintExpDate" || attribute == "lastUpdated"){
								addContentToSheet = new Label(fileMap[colName], r, (files[r-1].(fileDTAMap.eavAttribute.attributeCode[coll]) ? stdDateFormat.format(files[r-1].(fileDTAMap.eavAttribute.attributeCode[coll])) :''))
							} else if ( files[r-1][attribute] == null ) {
								addContentToSheet = new Label( fileMap[colName], r, "" )
							} else {
								addContentToSheet = new Label( fileMap[colName], r, String.valueOf(files[r-1].(fileDTAMap.eavAttribute.attributeCode[coll])) )
							}

							storageSheet.addCell( addContentToSheet )
						}
					}
					log.info "export() - processing storage took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}
				
				//
				// Dependencies
				//
				if ( doDependency ) {
					exportedEntity += "X"
					def dependencySheet = getWorksheet('Dependencies')

					def assetDependent = AssetDependency.findAll("from AssetDependency where asset.project = ? ",[project])
					def dependencyMap = ['AssetId':1,'AssetName':2,'AssetType':3,'DependentId':4,'DependentName':5,'DependentType':6,'Type':7, 'DataFlowFreq':8, 'DataFlowDirection':9, 'status':10, 'comment':11, 'c1':12, 'c2':13, 'c3':14, 'c4':15]
					def dependencyColumnNameList = ['AssetId','AssetName','AssetType','DependentId','DependentName','DependentType','Type', 'DataFlowFreq', 'DataFlowDirection', 'status', 'comment', 'c1', 'c2', 'c3', 'c4']
					def DTAMap = [0:'asset',1:'assetName',2:'assetType',3:'dependent',4:'dependentName',5:'dependentType',6:'type', 7:'dataFlowFreq', 8:'dataFlowDirection', 9:'status', 10:'comment', 11:'c1', 12:'c2', 13:'c3', 14:'c4']
					//def newDTA = ['assetName','assetType','dependentName','dependentType']
					def dependentSize = assetDependent.size()
					for ( int r = 1; r <= dependentSize; r++ ) {
						//Add assetId for walkthrough template only.
						def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
						def addAssetDependentId = new Number(0, r, (assetDependent[r-1].id))
						dependencySheet.addCell( addAssetDependentId )

						for ( int coll = 0; coll < 11; coll++ ) {
							def addContentToSheet
							switch(DTAMap[coll]){
								case "assetName":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].asset ? String.valueOf(assetDependent[r-1].(DTAMap[0])?.assetName) :"")
								break;
								case "assetType":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].asset ? String.valueOf(assetDependent[r-1].(DTAMap[0])?.assetType) :"" )
								break;
								case "dependentName":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].dependent ? String.valueOf(assetDependent[r-1].(DTAMap[3]).assetName) : "" )
								break;
								case "dependentType":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].dependent ? String.valueOf(assetDependent[r-1].(DTAMap[3]).assetType) : "")
								break;
								case "dependent":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].(DTAMap[coll]) ? String.valueOf(assetDependent[r-1].(DTAMap[coll]).id) : "")
								break;
								case "asset":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].(DTAMap[coll]) ? String.valueOf(assetDependent[r-1].(DTAMap[coll]).id) : "")
								break;
								default:
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].(DTAMap[coll]) ? String.valueOf(assetDependent[r-1].(DTAMap[coll])) : "" )
							}
							dependencySheet.addCell( addContentToSheet )
						}
					}
					log.info "export() - processing dependencies took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}

				//
				// Room Export
				//
				if( doRoom ) {
					exportedEntity += "R"

					def roomSheet = getWorksheet('Room')

					def formatter = new SimpleDateFormat("MM/dd/yyyy")
					def rooms = Room.findAllByProject(project)
					def roomSize = rooms.size()
					def roomMap = ['roomId':'id', 'Name':'roomName', 'Location':'location', 'Depth':'roomDepth', 'Width':'roomWidth',
								   'Source':'source', 'Address':'address', 'City':'city', 'Country':'country', 'StateProv':'stateProv',
								   'Postal Code':'postalCode', 'Date Created':'dateCreated', 'Last Updated':'lastUpdated'
								  ]
					
					def roomCol = storageSheet.getColumns()
					def roomSheetColumns = []
					for ( int c = 0; c < roomCol; c++ ) {
						def roomCellContent = roomSheet.getCell( c, 0 ).contents
						roomSheetColumns << roomCellContent
					}
					roomSheetColumns.removeAll('')
					for ( int r = 1; r <= roomSize; r++ ) {
						roomSheetColumns.eachWithIndex{column, i->
							def addContentToSheet
							if(column == 'roomId'){
								def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
								addContentToSheet = new Number(0, r, (rooms[r-1].id))
							} else {
							   if(column=='Date Created' || column=='Last Updated')
									   addContentToSheet = new Label( i, r, rooms[r-1]."${roomMap[column]}" ?
										   String.valueOf( formatter.format(rooms[r-1]."${roomMap[column]}")): "" )
							   else if(column =="Source")
									 addContentToSheet = new Label( i, r, String.valueOf(rooms[r-1]."${roomMap[column]}" ==1 ? "Source" : "Target" ) )
							   else
									addContentToSheet = new Label( i, r, String.valueOf(rooms[r-1]."${roomMap[column]}"?: "" ) )
							}
							roomSheet.addCell( addContentToSheet )
						}
				  }
				  log.info "export() - processing rooms took ${TimeUtil.elapsed(started)}"
				  started = new Date()
				}
				
				//
				// Rack Export
				//
				if( doRack ) {
					exportedEntity +="r"

					def rackSheet = getWorksheet('Rack')

					def racks = Rack.findAllByProject(project)
					def rackSize = racks.size()
					def rackMap = ['rackId':'id', 'Tag':'tag', 'Location':'location', 'Room':'room', 'RoomX':'roomX',
								   'RoomY':'roomY', 'PowerA':'powerA', 'PowerB':'powerB', 'PowerC':'powerC', 'Type':'rackType',
								   'Front':'front', 'Model':'model', 'Source':'source', 'Model':'model'
								  ]
					
					def rackCol = storageSheet.getColumns()
					def rackSheetColumns = []
					for ( int c = 0; c < rackCol; c++ ) {
						def rackCellContent = rackSheet.getCell( c, 0 ).contents
						rackSheetColumns << rackCellContent
					}
					rackSheetColumns.removeAll('')
					for ( int r = 1; r <= rackSize; r++ ) {
						rackSheetColumns.eachWithIndex{column, i->
							def addContentToSheet
							if(column == 'rackId'){
								def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
								addContentToSheet = new Number(0, r, (racks[r-1].id))
							} else {
							   if(column =="Source")
									 addContentToSheet = new Label( i, r, String.valueOf(racks[r-1]."${rackMap[column]}" ==1 ? "Source" : "Target" ) )
							   else
									addContentToSheet = new Label( i, r, String.valueOf(racks[r-1]."${rackMap[column]}"?: "" ) )
							}
							rackSheet.addCell( addContentToSheet )
						}
				  }
				  log.info "export() - processing racks took ${TimeUtil.elapsed(started)}"
				  started = new Date()
				}
				
			}

			//
			// Cabling Export
			// 
			if ( doCabling ) {
				exportedEntity += "c"

				def cablingSheet = getWorksheet("Cabling")

				def cablingList = AssetCableMap.findAll( "from AssetCableMap acm where acm.assetFrom.project.id = $project.id " )

				log.debug("Cabling found ${sizeOf(cablingList)} mappings")

				cablingReportData(cablingList, cablingSheet)
			}

			//
			// Comments Export
			// 
			if ( doComment ) {

				exportedEntity += "M"

				def commentSheet = getWorksheet("Comments")

				def commentIt = new ArrayList()
				SimpleDateFormat createDateFormat = new SimpleDateFormat("MM/dd/yyyy")
				def allAssets

				// TODO : JPM 9/2014 : The way that 
				if (bundle[0] == "" ) {
					allAssets = AssetEntity.findAllByProject( project, params )
				} else {
					allAssets = AssetEntity.findAll( "from AssetEntity m where m.project = :project and m.moveBundle.id in ( :bundles ) ",
						[project:project, bundles: bundles])
				}
				
				allAssets.each{
					commentIt.add(it.id)
				}

				def commentList = new StringBuffer()
				def commentSize = commentIt.size()
				for ( int k=0; k< commentSize ; k++ ) {
					if( k != commentSize - 1) {
						commentList.append( commentIt[k] + "," )
					} else {
						commentList.append( commentIt[k] )
					}
				}
				if (commentList) {
					def assetcomment = AssetComment.findAll("from AssetComment cmt where cmt.assetEntity in ($commentList) and cmt.commentType = 'comment'")
					def assetId
					def createdDate
					def createdBy
					def commentId
					def category
					def comment
					for(int cr=1 ; cr<=assetcomment.size() ; cr++){
						commentId = new Label(0,cr,String.valueOf(assetcomment[cr-1].id))
						commentSheet.addCell(commentId)
						assetId = new Label(1,cr,String.valueOf(assetcomment[cr-1].assetEntity.id))
						commentSheet.addCell(assetId)
						category = new Label(2,cr,String.valueOf(assetcomment[cr-1].category))
						commentSheet.addCell(category)
						createdDate = new Label(3,cr,String.valueOf(assetcomment[cr-1].dateCreated? createDateFormat.format(assetcomment[cr-1].dateCreated) : ''))
						commentSheet.addCell(createdDate)
						createdBy = new Label(4,cr,String.valueOf(assetcomment[cr-1].createdBy))
						commentSheet.addCell(createdBy)
						comment = new Label(5,cr,String.valueOf(assetcomment[cr-1].comment))
						commentSheet.addCell(comment)
					}
				}
			}

			//
			// Wrap up the process
			// 
			filename += "-"+exportedEntity+"-"+exportDate
			book.write()
			book.close()
			progressService.updateData(key, 'header', "attachment; filename=\""+exportType+'-'+filename+".xls\"")
			progressService.update(key, 100, 'Completed')
		
		} catch( Exception exportExp ) {

			exportExp.printStackTrace()
			progressService.update(key, 100, 'Cancelled', "An unexpected exception occurred while exporting to Excel")

			return
		}
	}
	
	/* -------------------------------------------------------
	 * To check the sheet headers
	 * @param attributeList, SheetColumnNames
	 * @author Mallikarjun
	 * @return bollenValue
	 *------------------------------------------------------- */
	def checkHeader( def list, def serverSheetColumnNames, missingHeader = "") {
		def listSize = list.size()
		for ( int coll = 0; coll < listSize; coll++ ) {
			if( serverSheetColumnNames.containsKey( list[coll] ) || list[coll] == "DepGroup") {
				//Nonthing to perform.
			} else {
				missingHeader = missingHeader + ", " + list[coll]
			}
		}
		if( missingHeader == "" ) {
			return true
		} else {
			return false
		}
	}
	
	/**
	 * This method is used to update sheet's column header with custom labels
	 * @param sheet : sheet's instance
	 * @param entityDTAMap : dataTransferEntityMap for entity type
	 * @param sheetColumnNames : column Names
	 * @param project : project instance
	 * @return
	 */
	 // TODO : JPM 9/2014 : The updateColumnHeaders probable won't work beyond 24 custom columns how this is written - should use regex test instead
	 // customLabels is defined as a static at the top
   	def updateColumnHeaders(sheet, entityDTAMap, sheetColumnNames, project){
	   	for ( int head =0; head <= sheetColumnNames.size(); head++ ) {
		   def cellData = sheet.getCell(head,0)?.getContents()
		   def attributeMap = entityDTAMap.find{it.columnName ==  cellData }?.eavAttribute
		   if (attributeMap?.attributeCode && customLabels.contains( cellData )) {
			   def columnLabel = project[attributeMap?.attributeCode] ? project[attributeMap?.attributeCode] : cellData
			   def customColumn = new Label(head,0, columnLabel )
			   sheet.addCell(customColumn)
		   }
	   }
	   return sheet
   }


   /**
    * Used by the AssetEntity List to populate the initial List view
    */
   Map getListModel(Project project, UserLogin userLogin, session, params, tzId) {
		def filters = session.AE?.JQ_FILTERS
		session.AE?.JQ_FILTERS = []

		// def prefType = (listType=='server') ? 'Asset_Columns' : 'Physical_Columns'
		def prefType = 'Asset_Columns'
		def fieldPrefs = getExistingPref(prefType)
		
		// The hack for the dot notation
		fieldPrefs.each {k,v -> fieldPrefs[k] = v }

		Map model = getDefaultModelForLists('AssetEntity', project, fieldPrefs, params, filters)

		// Check the assetEntityService.getDefaultModelForLists before adding to this list. This should ONLY be AssetEntity specific properties
		model.assetName = filters?.assetNameFilter ?:'' 
		model.assetPref = fieldPrefs 
		model.assetTag = filters?.assetTagFilter ?:'' 
		model.assetType = filters?.assetTypeFilter ?:'' 
		model.model = filters?.modelFilter ?:'' 
		model.prefType = prefType 
		model.serialNumber = filters?.serialNumberFilter ?:'' 
		model.sourceLocation = filters?.sourceLocationFilter ?:'' 
		model.sourceRack = filters?.sourceRackFilter ?:''
		model.targetLocation = filters?.targetLocationFilter ?:'' 
		model.targetRack = filters?.targetRackFilter ?:'' 
		// Used for filter toValidate from the Planning Dashboard - want a better parameter (JPM 9/2014)
		model.type = params.type

		// The customized title of the list
		def titleByFilter = [
			all: 'All Devices',
			other: 'Other Devices',
			physical: 'Physical Device',
			physicalServer: 'Physical Server',
			server: 'Server',
			storage:  'Storage Device',
			virtualServer: 'Virtual Server',
		]
		model.title = ( titleByFilter.containsKey(params.filter) ? titleByFilter[params.filter] : titleByFilter['all'] ) + ' List'

		return model
	}

   /** 
    * Used to retrieve the data used by the AssetEntity List
    */
   Map getListData(Project project, UserLogin userLogin, session, params, tzId) {
		def filterParams = [
			assetName: params.assetName, 
			assetType: params.assetType, 
			depConflicts: params.depConflicts, 
			depNumber: params.depNumber, 
			depToResolve: params.depToResolve,
			event: params.event,
			model: params.model, 
			moveBundle: params.moveBundle, 
			planStatus: params.planStatus, 
			sourceLocation: params.sourceLocation, 
			sourceRack: params.sourceRack, 
		]
		def validSords = ['asc', 'desc']
		def sortOrder = (validSords.indexOf(params.sord) != -1) ? (params.sord) : ('asc')
		def maxRows = Integer.valueOf(params.rows) 
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def moveBundleList
		
		def attributes = projectService.getAttributes('AssetEntity')
		
		// def prefType = (listType=='server') ? 'Asset_Columns' : 'Physical_Columns'
		def prefType = 'Asset_Columns'
		def assetPref= getExistingPref(prefType)
		
		def assetPrefVal = assetPref.collect{it.value}
		attributes.each { attribute ->
			if (attribute.attributeCode in assetPrefVal)
				filterParams << [ (attribute.attributeCode): params[(attribute.attributeCode)]]
		}

		// Lookup the field name reference for the sort
		def sortIndex = (params.sidx in filterParams.keySet() ? params.sidx : 'assetName')
		
		// This is used by the JQ-Grid some how
		session.AE = [:]

		userPreferenceService.setPreference("assetListSize", "${maxRows}")
		
		if (params.event && params.event.isNumber()) {
			def moveEvent = MoveEvent.read( params.event )
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useForPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project,true)
		}
		
		def assetType = params.filter ? ApplicationConstants.assetFilters[ params.filter ] : []

		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%${params.moveBundle}%", project) : []
		
		StringBuffer altColumns = new StringBuffer()
		StringBuffer joinQuery = new StringBuffer()

		// Until sourceRack is optional on the list we have to do this one
		altColumns.append("\n, srcRack.tag AS sourceRack, srcRoom.location AS sourceLocation")
		joinQuery.append("\nLEFT OUTER JOIN rack AS srcRack ON srcRack.rack_id=ae.rack_source_id ")
		joinQuery.append("\nLEFT OUTER JOIN room AS srcRoom ON srcRoom.room_id=ae.room_source_id ")

		boolean srcRoomAdded = true 	// Can set to false if the above lines are removed
		boolean tgtRoomAdded = false

		// Tweak the columns selected and addition joins based on the user's selected columns
		assetPref.each { key,value->
			switch (value) {
				case 'appOwner':
					altColumns.append(", CONCAT(CONCAT(p1.first_name, ' '), IFNULL(p1.last_name,'')) AS appOwner")
					joinQuery.append("\nLEFT OUTER JOIN person p1 ON p1.person_id=ae.app_owner_id ")
					break
				case 'os':
					altColumns.append(", ae.hinfo AS os")
					break
				case ~/custom\d{1,}/:
					altColumns.append(", ae.${value} AS ${value}")
					break
				case 'lastUpdated':
					altColumns.append(", ee.last_updated AS ${value}")
					joinQuery.append("\nLEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id ")
					break
				case 'manufacturer':
					altColumns.append(", manu.name AS manufacturer")
					joinQuery.append("\nLEFT OUTER JOIN manufacturer manu ON manu.manufacturer_id=m.manufacturer_id ")
					break
				case 'modifiedBy':
					altColumns.append(", CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS modifiedBy")
					joinQuery.append("\nLEFT OUTER JOIN person p ON p.person_id=ae.modified_by ")
					break

				case ~/source(Location|Room)/:
					// This is a hack for the columns that were moved to the Room domain property map attributes
					def locOrRoom = Matcher.lastMatcher[0][1]

					if (!srcRoomAdded) {
						joinQuery.append("\nLEFT OUTER JOIN room srcRoom ON srcRoom.room_id=ae.room_source_id ")
						srcRoomAdded = true
					}
					if (locOrRoom == 'Location') {
						// Note that this is added by default above 
						// altColumns.append(', srcRoom.location AS sourceLocation')
					} else if (locOrRoom == 'Room') {
						altColumns.append(', srcRoom.room_name AS sourceRoom')
					} else {
						throw new RuntimeException("Unhandled condition for property ($value)")
					}
					break	
				case 'sourceRack':
						// Already handled above
						// Moved to the Rack domain
					break

				case ~/target(Location|Room)/:
					// This is a hack for the columns that were moved to the Room domain property map attributes
					def locOrRoom = Matcher.lastMatcher[0][1]

					if (!tgtRoomAdded) {
						joinQuery.append("\nLEFT OUTER JOIN room tgtRoom ON tgtRoom.room_id=ae.room_target_id ")
						tgtRoomAdded = true
					}
					if (locOrRoom == 'Location') {
						altColumns.append(', tgtRoom.location AS targetLocation')
					} else if (locOrRoom == 'Room') {
						altColumns.append(', tgtRoom.room_name AS targetRoom')
					} else {
						throw new RuntimeException("Unhandled condition for property ($value)")
					}
					break	
				case 'targetRack':
					// Property was moved to the Rack domain
					altColumns.append(", tgtRack.tag AS targetRack")
					joinQuery.append("\nLEFT OUTER JOIN rack tgtRack ON tgtRack.rack_id=ae.rack_target_id ")
					break

				case 'validation':
					break;
				default:
					altColumns.append(", ae.${WebUtil.splitCamelCase(value)} AS ${value} ")
			}
		}

		def query = new StringBuffer(""" 
			SELECT * FROM ( 
				SELECT ae.asset_entity_id AS assetId, ae.asset_name AS assetName, 
				ae.asset_type AS assetType, m.name AS model,  
				IF(at.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus, 
				IF(ac.comment_type IS NULL, 'noComments','comments') AS commentsStatus, 
				me.move_event_id AS event, ae.plan_status AS planStatus, 
				mb.name AS moveBundle, ae.validation AS validation
			""" )

		if (altColumns.length())
			query.append("\n${ altColumns.toString() }")

		query.append("""
				FROM asset_entity ae
				LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
				LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
				LEFT OUTER JOIN model m ON m.model_id=ae.model_id
				LEFT OUTER JOIN asset_comment at ON at.asset_entity_id=ae.asset_entity_id AND at.comment_type = '${AssetCommentType.TASK}'
				LEFT OUTER JOIN asset_comment ac ON ac.asset_entity_id=ae.asset_entity_id AND ac.comment_type = '${AssetCommentType.COMMENT}'
				""")

		if (joinQuery.length())
			query.append(joinQuery)
			
		//
		// Begin the WHERE section of the query	
		//
		query.append("\nWHERE ae.project_id = ${project.id}\nAND ae.asset_class = '${AssetClass.DEVICE}'")

		def justPlanning = userPreferenceService.getPreference("assetJustPlanning")?:'true'
		/*
		// This was being added to correct the issue when coming from the Planning Dashboard but there are some ill-effects still
		if (params.justPlanning)
			justPlanning = params.justPlanning
		*/
		if (justPlanning=='true')
			query.append("\nAND mb.use_for_planning=${justPlanning}")
		
		query.append("\nAND ae.asset_class='${AssetClass.DEVICE}'")

		def filter = params.filter ?: 'all'

		// Filter the list of assets based on if param listType == 'server' to all server types otherwise filter NOT server types
		switch(filter) {
			case 'physical':
				query.append("\nAND COALESCE(ae.asset_type,'') NOT IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getVirtualServerTypes())}) " )
				break
			case 'physicalServer':
				def phyServerTypes = AssetType.getAllServerTypes() - AssetType.getVirtualServerTypes()
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(phyServerTypes)}) " )
				break
			case 'server':
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getAllServerTypes())}) ")
				break
			case 'storage':
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getStorageTypes())}) " )
				break
			case 'virtualServer':
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getVirtualServerTypes())}) " )
				break
			case 'other':
				query.append("\nAND COALESCE(ae.asset_type,'') NOT IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getNonOtherTypes())}) " )
				break

 			case 'all': 
				break
		}

		if (params.event && params.event.isNumber() && moveBundleList)
			query.append( "\nAND ae.move_bundle_id IN (${GormUtil.asQuoteCommaDelimitedString(moveBundleList.id)})" )
			
		if (params.unassigned) {
			def unasgnMB = MoveBundle.findAll("\nFROM MoveBundle mb WHERE mb.moveEvent IS NULL AND mb.useForPlanning=true AND mb.project=:project ", [project:project])
			
			if (unasgnMB) {
				def unasgnmbId = GormUtil.asQuoteCommaDelimitedString(unasgnMB?.id)
				query.append( "\nAND (ae.move_bundle_id IN (${unasgnmbId}) OR ae.move_bundle_id IS NULL)" )
			}
		}
			
		query.append("\nGROUP BY assetId ORDER BY ${sortIndex} ${sortOrder}\n) AS assets")
		
		// Setup a helper closure that is used to set WHERE or AND for the additional query specifications
		def firstWhere = true
		def whereAnd = { 
			if (firstWhere) {
				firstWhere = false
				return ' WHERE'
			} else {
				return ' AND'
			}
		}

		// Handle the filtering by each column's text field
		filterParams.each { fkey, fvalue ->
			if ( fvalue ) {
				// single quotes are stripped from the filter to prevent SQL injection
				query.append( whereAnd() + " assets.${fkey} LIKE '%${fvalue.replaceAll("'", "")}%'")
				firstWhere = false
			}
		}
		
		if (params.moveBundleId) {
			if (params.moveBundleId!='unAssigned') {
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				query.append( whereAnd() + " assets.moveBundle  = '${bundleName}' ")
			} else {
				query.append( whereAnd() + " assets.moveBundle IS NULL ")
			}
		}
		
		if (params.type && params.type == 'toValidate') {
			query.append( whereAnd() + " assets.validation='Discovery' ") //eq ('validation','Discovery')
		}

		// Allow filtering on the Validate
		if (params.toValidate && params.toValidate && ValidationType.getList().contains(params.toValidate)) {
			query.append( whereAnd() + " assets.validation='${params.toValidate}' ")
		}

		if (params.plannedStatus) {
			query.append(whereAnd() + " assets.planStatus='${params.plannedStatus}'")
		}
		
		log.debug  "query = ${query}"
		def assetList = jdbcTemplate.queryForList(query.toString())
		
		// Cut the list of selected applications down to only the rows that will be shown in the grid
		def totalRows = assetList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0)
			assetList = assetList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			assetList = []
			
		def results = assetList?.collect {
			def commentType = it.commentType
			[ 	
				cell: [ 
					'', // The action checkbox
					it.assetName, 
					(it.assetType ?: ''), 
					it.model, 
					it.sourceLocation, 
					it.sourceRack,
					( it[ assetPref['1'] ] ?: ''), 
					( it[ assetPref['2'] ] ?: ''), 
					( it[ assetPref['3'] ] ?: ''), 
					( it[ assetPref['4'] ] ?: ''), 
					it.planStatus, 
					it.moveBundle, 
					/*it.depNumber, (it.depToResolve==0)?(''):(it.depToResolve), (it.depConflicts==0)?(''):(it.depConflicts),*/
					it.tasksStatus, 
					it.assetType, 
					it.event, 
					it.commentsStatus
				], id: it.assetId
			]
		}

		return [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

	}
}