import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.shiro.SecurityUtils

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
import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.ValidationType

class AssetEntityService {

    static transactional = true
	def jdbcTemplate
	def projectService
	
	def createOrUpdateAssetEntityDependencies(def params, def assetEntityInstance) {
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		def supportCount = Integer.parseInt(params.supportCount)
		AssetDependency.executeUpdate("delete AssetDependency where dependent = ? ",[assetEntityInstance])
		
		for(int i=0; i< supportCount; i++){
			def supportAsset = params["asset_support_"+i]
			if(supportAsset){
				def asset = AssetEntity.findByIdAndProject(supportAsset, assetEntityInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(asset,assetEntityInstance)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_support_"+i]
						assetDependency.type = params["dtype_support_"+i]
						assetDependency.status = params["status_support_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : asset,
																dependent : assetEntityInstance,
																dataFlowFreq : params["dataFlowFreq_support_"+i],
																type : params["dtype_support_"+i],
																status : params["status_support_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
		
		def dependentCount = Integer.parseInt(params.dependentCount)
		AssetDependency.executeUpdate("delete AssetDependency where asset = ? ",[assetEntityInstance])
		
		for(int i=0; i< dependentCount; i++){
			def dependentAsset = params["asset_dependent_"+i]
			if(dependentAsset){
				def asset = AssetEntity.findByIdAndProject(dependentAsset, assetEntityInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(assetEntityInstance,asset)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_dependent_"+i]
						assetDependency.type = params["dtype_dependent_"+i]
						assetDependency.status = params["status_dependent_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : assetEntityInstance,
																dependent : asset,
																dataFlowFreq : params["dataFlowFreq_dependent_"+i],
																type : params["dtype_dependent_"+i],
																status : params["status_dependent_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
	}
    def createOrUpdateApplicationDependencies(def params, def applicationInstance) {
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		def supportCount = Integer.parseInt(params.supportCount)
		AssetDependency.executeUpdate("delete AssetDependency where dependent = ? ",[applicationInstance])
		
		for(int i=0; i< supportCount; i++){
			def supportAsset = params["asset_support_"+i]
			if(supportAsset){
				def asset = AssetEntity.findByIdAndProject(supportAsset, applicationInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(asset,applicationInstance)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_support_"+i]
						assetDependency.type = params["dtype_support_"+i]
						assetDependency.status = params["status_support_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : asset,
																dependent : applicationInstance,
																dataFlowFreq : params["dataFlowFreq_support_"+i],
																type : params["dtype_support_"+i],
																status : params["status_support_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
		
		def dependentCount = Integer.parseInt(params.dependentCount)
		AssetDependency.executeUpdate("delete AssetDependency where asset = ? ",[applicationInstance])
		
		for(int i=0; i< dependentCount; i++){
			def dependentAsset = params["asset_dependent_"+i]
			if(dependentAsset){
				def asset = AssetEntity.findByIdAndProject(dependentAsset, applicationInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(applicationInstance,asset)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_dependent_"+i]
						assetDependency.type = params["dtype_dependent_"+i]
						assetDependency.status = params["status_dependent_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : applicationInstance,
																dependent : asset,
																dataFlowFreq : params["dataFlowFreq_dependent_"+i],
																type : params["dtype_dependent_"+i],
																status : params["status_dependent_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
    }
	def createOrUpdateDatabaseDependencies(def params, def dbInstance) {
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		def supportCount = Integer.parseInt(params.supportCount)
		AssetDependency.executeUpdate("delete AssetDependency where dependent = ? ",[dbInstance])
		
		for(int i=0; i< supportCount; i++){
			def supportAsset = params["asset_support_"+i]
			if(supportAsset){
				def asset = AssetEntity.findByIdAndProject(supportAsset, dbInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(asset,dbInstance)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_support_"+i]
						assetDependency.type = params["dtype_support_"+i]
						assetDependency.status = params["status_support_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : asset,
																dependent : dbInstance,
																dataFlowFreq : params["dataFlowFreq_support_"+i],
																type : params["dtype_support_"+i],
																status : params["status_support_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save(flush:true) ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
		
		def dependentCount = Integer.parseInt(params.dependentCount)
		AssetDependency.executeUpdate("delete AssetDependency where asset = ? ",[dbInstance])
		
		for(int i=0; i< dependentCount; i++){
			def dependentAsset = params["asset_dependent_"+i]
			if(dependentAsset){
				def asset = AssetEntity.findByIdAndProject(dependentAsset, dbInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(dbInstance,asset)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_dependent_"+i]
						assetDependency.type = params["dtype_dependent_"+i]
						assetDependency.status = params["status_dependent_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : dbInstance,
																dependent : asset,
																dataFlowFreq : params["dataFlowFreq_dependent_"+i],
																type : params["dtype_dependent_"+i],
																status : params["status_dependent_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
	}
	def createOrUpdateFilesDependencies(def params, def dbInstance) {
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		def supportCount = Integer.parseInt(params.supportCount)
		AssetDependency.executeUpdate("delete AssetDependency where dependent = ? ",[dbInstance])
		
		for(int i=0; i< supportCount; i++){
			def supportAsset = params["asset_support_"+i]
			if(supportAsset){
				def asset = AssetEntity.findByIdAndProject(supportAsset, dbInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(asset,dbInstance)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_support_"+i]
						assetDependency.type = params["dtype_support_"+i]
						assetDependency.status = params["status_support_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : asset,
																dependent : dbInstance,
																dataFlowFreq : params["dataFlowFreq_support_"+i],
																type : params["dtype_support_"+i],
																status : params["status_support_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save(flush:true) ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
		
		def dependentCount = Integer.parseInt(params.dependentCount)
		AssetDependency.executeUpdate("delete AssetDependency where asset = ? ",[dbInstance])
		
		for(int i=0; i< dependentCount; i++){
			def dependentAsset = params["asset_dependent_"+i]
			if(dependentAsset){
				def asset = AssetEntity.findByIdAndProject(dependentAsset, dbInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(dbInstance,asset)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_dependent_"+i]
						assetDependency.type = params["dtype_dependent_"+i]
						assetDependency.status = params["status_dependent_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : dbInstance,
																dependent : asset,
																dataFlowFreq : params["dataFlowFreq_dependent_"+i],
																type : params["dtype_dependent_"+i],
																status : params["status_dependent_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
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
											   IFNULL(server.custom4,'') AS storage_inventory,
											   IFNULL(application.business_unit,'') AS dr_tier,
											   IFNULL(server.new_or_old,'') AS status
											FROM asset_entity server
											JOIN asset_dependency srvappdep ON server.asset_entity_id = srvappdep.dependent_id
											JOIN asset_entity app ON app.asset_entity_id = srvappdep.asset_id AND app.asset_type = 'Application'
											LEFT OUTER JOIN application ON application.app_id = app.asset_entity_id
											LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=server.move_bundle_id
											LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id = server.asset_entity_id
											WHERE
											   server.project_id=${project.id}
											   AND server.asset_type IN ('Server','VM', 'Load Balancer','Network', 'Storage')
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
											   IFNULL(dbsrv.custom4,'') AS storage_inventory,
											   IFNULL(applic.business_unit,'') AS dr_tier,
											   IFNULL(dbsrv.new_or_old,'') AS status
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
											   IFNULL(clustersrv.custom4,'') AS storage_inventory,
											   IFNULL(applic.business_unit,'') AS dr_tier,
											   IFNULL(clustersrv.new_or_old,'') AS status
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
		AssetCableMap.executeUpdate("delete AssetCableMap where fromAsset = :asset",[asset:assetEntity])
		AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
											toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
											where toAsset = :asset""",[asset:assetEntity])
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
		def allconfig = projectService.getConfig(type)
		def fields = projectService.getFields(type)
		def config = [:]
		def validationType
		def valList=ValidationType.getValuesAsMap()
		valList.each{v ->
			if(v.key==validation)
				validationType=v.value
		}
		fields.each{f ->
			config << [(f.label):allconfig[f.label]['phase'][validationType]]
		}
		return config
	}
}
