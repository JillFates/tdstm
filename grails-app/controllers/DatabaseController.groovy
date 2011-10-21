import java.text.SimpleDateFormat

import net.tds.util.jmesa.AssetEntityBean

import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit

import com.tds.asset.Application
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetTransition
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.GormUtil


class DatabaseController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def assetEntityService  
    def index = {
		redirect(action: "list", params: params)
    }
	
	def list ={
		def projectId = params.projectId ? params.projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def databaseInstanceList = Database.findAllByProject(project)
		def databaseList = new ArrayList();
		databaseInstanceList.each {dataBaseentity ->
			AssetEntityBean dataBeanInstance = new AssetEntityBean();
			dataBeanInstance.setId(dataBaseentity.id)
			dataBeanInstance.setAssetType(dataBaseentity.assetType)
			dataBeanInstance.setDbFormat(dataBaseentity.dbFormat)
			dataBeanInstance.setDbSize(dataBaseentity.dbSize)
			dataBeanInstance.setMoveBundle(dataBaseentity?.moveBundle?.name)
			dataBeanInstance.setplanStatus(dataBaseentity.planStatus)
			databaseList.add(dataBeanInstance)
		}
		TableFacade tableFacade = new TableFacadeImpl("tag", request)
		def servers = AssetEntity.findAllByAssetTypeAndProject('Server',project)
		def applications = Application.findAllByAssetTypeAndProject('Application',project)
		def dbs = Database.findAllByAssetTypeAndProject('Database',project)
		def files = Files.findAllByAssetTypeAndProject('Files',project)
		try{
			tableFacade.items = databaseList
			Limit limit = tableFacade.limit
			if(limit.isExported()){
				tableFacade.setExportTypes(response,limit.getExportType())
				tableFacade.setColumnProperties("id","dbFormat","dbSize","moveBundle","planStatus")
				tableFacade.render()
			}else
				return [databaseList : databaseList , projectId: projectId ,assetDependency: new AssetDependency(),
					servers : servers, applications : applications, dbs : dbs, files : files]
		}catch(Exception e){
			return [databaseList : null,projectId: projectId,
					servers : servers, applications : applications, dbs : dbs, files : files]
		}
		
		
		
	}
	
	def show ={
		def id = params.id
		def databaseInstance = Database.get( id )
		if(!databaseInstance) {
			flash.message = "Application not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			def assetEntity = AssetEntity.get(id)
			def dependentAssets = AssetDependency.findAllByAsset(assetEntity)
			def supportAssets = AssetDependency.findAllByDependent(assetEntity)
			[ databaseInstance : databaseInstance,supportAssets: supportAssets, dependentAssets:dependentAssets]
		}
	}
	
	def create ={
		def databaseInstance = new Database(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusAttribute = EavAttribute.findByAttributeCode('planStatus')
		def planStatusOptions = EavAttributeOption.findAllByAttribute(planStatusAttribute)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project)

		[databaseInstance:databaseInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId]
	}
	
	def save = {
		
				def formatter = new SimpleDateFormat("MM/dd/yyyy")
				def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
				def maintExpDate = params.maintExpDate
				if(maintExpDate){
					params.maintExpDate =  GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
				}
				def retireDate = params.retireDate
				if(retireDate){
					params.retireDate =  GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
				}
				def dbInstance = new Database(params)
				if(!dbInstance.hasErrors() && dbInstance.save()) {
					flash.message = "Database ${dbInstance.id} created"
					assetEntityService.createOrUpdateDatabaseDependencies(params, dbInstance)
			        redirect(action:list,id:dbInstance.id)
		 	    }else {
					flash.message = "Database not created"
					dbInstance.errors.allErrors.each{ flash.message += it  }
					redirect(action:list,id:dbInstance.id)
				}
				
		
			
     }
	def edit ={
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusAttribute = EavAttribute.findByAttributeCode('planStatus')
		def planStatusOptions = EavAttributeOption.findAllByAttribute(planStatusAttribute)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project)

		def id = params.id
		def databaseInstance = Database.get( id )
		if(!databaseInstance) {
			flash.message = "DataBase not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			def assetEntity = AssetEntity.get(id)
			def dependentAssets = AssetDependency.findAllByAsset(assetEntity)
			def supportAssets = AssetDependency.findAllByDependent(assetEntity)

			[databaseInstance:databaseInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
						planStatusOptions:planStatusOptions?.value, projectId:projectId, supportAssets: supportAssets, dependentAssets:dependentAssets]
		}
		
		}
	
	def update ={
		def formatter = new SimpleDateFormat("MM/dd/yyyy")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def maintExpDate = params.maintExpDate
		if(maintExpDate){
			params.maintExpDate =  GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
		}
		def retireDate = params.retireDate
		if(retireDate){
			params.retireDate =  GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
		}
		def databaseInstance = Database.get(params.id)
		databaseInstance.properties = params
		if(!databaseInstance.hasErrors() && databaseInstance.save()) {
			flash.message = "DataBase ${databaseInstance.assetName} Updated"
			assetEntityService.createOrUpdateDatabaseDependencies(params, databaseInstance)
			redirect(action:list,id:databaseInstance.id)
		}
		else {
			flash.message = "DataBase not created"
			databaseInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list,id:databaseInstance.id)
		}
		
	}
	def delete = {
		def databaseInstance = Database.get( params.id )
		def assetEntityInstance = AssetEntity.get(params.id)
		if(assetEntityInstance) {
			def assetName = assetEntityInstance.assetName
			ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.asset = ${assetEntityInstance.id}")
			AssetTransition.executeUpdate("delete from AssetTransition ast where ast.assetEntity = ${assetEntityInstance.id}")
			AssetComment.executeUpdate("delete from AssetComment ac where ac.assetEntity = ${assetEntityInstance.id}")
			ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset = ${assetEntityInstance.id}")
			AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar aev where aev.assetEntity = ${assetEntityInstance.id}")
			ProjectTeam.executeUpdate("update ProjectTeam pt set pt.latestAsset = null where pt.latestAsset = ${assetEntityInstance.id}")
			AssetCableMap.executeUpdate("delete AssetCableMap where fromAsset = ? ",[assetEntityInstance])
			AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
										   toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
										   where toAsset = ?""",[assetEntityInstance])
			AssetEntity.executeUpdate("delete from AssetEntity ae where ae.id = ${assetEntityInstance.id}")
			Database.executeUpdate("delete from Database d where d.id = ${databaseInstance.id}")
			flash.message = "Database ${assetName} deleted"
		}
		else {
			flash.message = "Database not found with id ${params.id}"
		}
		redirect( action:list )
	}
}
