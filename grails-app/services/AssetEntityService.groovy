import jxl.*
import jxl.read.biff.*
import jxl.write.*
import org.apache.commons.lang.math.NumberUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder

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
import com.tdsops.tm.enums.domain.ValidationType
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.WebUtil
import grails.converters.JSON

class AssetEntityService {

	static transactional = true
	def jdbcTemplate
	def projectService
	def assetEntityAttributeLoaderService
	def userPreferenceService
	def securityService
	
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
		
		updateBundle(depEntity, params["moveBundle_${depType}_"+idSuf])
		
		def errMsg = "" // Initializing var to return error message (if came)
		if(depEntity){
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
	 * 
	 * @param project
	 * @return
	 */
	def entityInfo(def project ){
		def servers = AssetEntity.executeQuery("SELECT a.id, a.assetName FROM AssetEntity a WHERE assetType in ('${AssetType.SERVER.toString()}','${AssetType.VM.toString()}','Blade')\
					AND project=:project ORDER BY assetName", [project:project])

		def applications =  Application.executeQuery("SELECT a.id, a.assetName FROM Application a WHERE assetType =? and project =?\
					ORDER BY assetName", [AssetType.APPLICATION.toString(), project])

		def dbs = Database.executeQuery("SELECT d.id, d.assetName FROM Database d where assetType = ? and project =? \
					order by assetName asc",[AssetType.DATABASE.toString(), project])
		
		def files = Files.executeQuery("SELECT f.id, f.assetName FROM Files f where assetType = ? and project =? \
					order by assetName asc",[AssetType.FILES.toString(), project])
		def nonNetworkTypes = [AssetType.SERVER.toString(),AssetType.APPLICATION.toString(),AssetType.VM.toString(),
								AssetType.FILES.toString(),AssetType.DATABASE.toString(),AssetType.BLADE.toString()]
		def networks = AssetEntity.executeQuery("SELECT a.id, a.assetName FROM AssetEntity a where assetType not in (:type) and project =:project \
			order by assetName asc",[type:nonNetworkTypes, project:project])
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		
		return [servers:servers, applications:applications, dbs:dbs, files:files,
				 dependencyType:dependencyType, dependencyStatus:dependencyStatus, networks:networks]
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
	 * @param byValue : application's shutdownBy, startupBy, or testingBy raw value
	 * @param stripPrefix : if true or not specified, the function will remove the # or @ character from the string
	 * @return : value to display
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
	 * @param : assetList : list of ids for which assets are requested to deleted
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
					cablingSheet.addCell( new Label( 9, r, String.valueOf(assetCablesList[r-2].assetFrom?.sourceLocation+"/"+assetCablesList[r-2].assetFrom?.sourceRoom+"/"+assetCablesList[r-2].assetFrom?.sourceRack )) )
				}else if(assetCablesList[r-2].assetFrom.targetRoom){
					cablingSheet.addCell( new Label( 9, r, String.valueOf(assetCablesList[r-2].assetFrom?.targetLocation+"/"+assetCablesList[r-2].assetFrom?.targetRoom+"/"+assetCablesList[r-2].assetFrom?.targetRack )) )
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
	 * Used to get the existing preference for customized columns
	 * @forWhom 'App_columns' for now
	 * @return appPref
	 */
	def getExistingPref(forWhom){
		def existingPref = userPreferenceService.getPreference(forWhom)
		def appPref
		if(!existingPref){
			switch(forWhom){
				case 'App_Columns':
					appPref = ['1':'sme','2':'validation','3':'planStatus','4':'moveBundle']
				break;
				case 'Asset_Columns':
					appPref = ['1':'targetLocation','2':'targetRack','3':'assetTag','4':'serialNumber']
				break;
				case 'Physical_Columns':
					appPref = ['1':'targetLocation','2':'targetRack','3':'assetTag','4':'serialNumber']
				break;
				case 'Database_Columns':
					appPref = ['1':'dbFormat','2':'size','3':'planStatus','4':'moveBundle']
				break;
				case 'Storage_Columns':
					appPref = ['1':'fileFormat','2':'size','3':'planStatus','4':'moveBundle']
				break;
				case 'Task_Columns':
					appPref = ['1':'assetName','2':'assetType','3':'assignedTo','4':'role', '5':'category']
				break;
				case 'Model_Columns':
					appPref = ['1':'description','2':'assetType','3':'powerUse','4':'modelConnectors']
				break;
				case 'Dep_Columns':
					appPref = ['1':'frequency','2':'comment']
				break;
			}
		}else{
			appPref = JSON.parse(existingPref)
		}
		return appPref
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
}
