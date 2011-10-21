import org.jmesa.limit.Limit

import com.tds.asset.AssetEntity

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

class ApplicationController {
	def partyRelationshipService
	def assetEntityService
	
	def index = {
		redirect(action:list,params:params)
	}

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def  list ={
		def projectId = params.projectId ? params.projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def workFlow = project.workflowCode
		def appEntityList = Application.findAllByProject(project)
		def appBeanList = new ArrayList()
		appEntityList.each { appEntity->
			AssetEntityBean appBeanInstance = new AssetEntityBean();
			appBeanInstance.setId(appEntity.id)
			appBeanInstance.setAssetName(appEntity.assetName)
			appBeanInstance.setAssetType(appEntity.assetType)
			appBeanInstance.setAppOwner(appEntity.appOwner)
			appBeanInstance.setAppSme(appEntity.appSme)
			appBeanInstance.setMoveBundle(appEntity.moveBundle?.name)
			appBeanInstance.setplanStatus(appEntity.planStatus)
			appBeanList.add(appBeanInstance)
		}
		TableFacade tableFacade = new TableFacadeImpl("tag", request)
		def servers = AssetEntity.findAllByAssetTypeAndProject('Server',project)
		def applications = Application.findAllByAssetTypeAndProject('Application',project)
		def dbs = Database.findAllByAssetTypeAndProject('Database',project)
		def files = Files.findAllByAssetTypeAndProject('Files',project)
		try{
			tableFacade.items = appBeanList
			Limit limit = tableFacade.limit
			if(limit.isExported()){
				tableFacade.setExportTypes(response,limit.getExportType())
				tableFacade.setColumnProperties("id","application","appOwner","appSme","movebundle","planStatus")
				tableFacade.render()
			} else {
				return [assetEntityList : appBeanList , projectId: projectId, assetDependency: new AssetDependency(),
					servers : servers, applications : applications, dbs : dbs, files : files]
			}
		}catch(Exception e){
			return [assetEntityList : null, projectId: projectId , servers : servers, applications : applications, dbs : dbs, files : files]
		}
	}
	def create ={
		def applicationInstance = new Application(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusAttribute = EavAttribute.findByAttributeCode('planStatus')
		def planStatusOptions = EavAttributeOption.findAllByAttribute(planStatusAttribute)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project)

		[applicationInstance:applicationInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
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
		def applicationInstance = new Application(params)
		if(!applicationInstance.hasErrors() && applicationInstance.save(flush:true)) {
			flash.message = "Application ${applicationInstance.assetName} created"
			assetEntityService.createOrUpdateApplicationDependencies(params, applicationInstance)
			redirect(action:list,id:applicationInstance.id)
		}
		else {
			flash.message = "Application not created"
			applicationInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list,id:applicationInstance.id)
		}

	}
	def show ={
		def id = params.id
		def applicationInstance = Application.get( id )
		if(!applicationInstance) {
			flash.message = "Application not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			def assetEntity = AssetEntity.get(id)
			def dependentAssets = AssetDependency.findAllByAsset(assetEntity)
			def supportAssets = AssetDependency.findAllByDependent(assetEntity)
			[ applicationInstance : applicationInstance,supportAssets: supportAssets, dependentAssets:dependentAssets]
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
		def applicationInstance = Application.get( id )
		if(!applicationInstance) {
			flash.message = "Application not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			def assetEntity = AssetEntity.get(id)
			def dependentAssets = AssetDependency.findAllByAsset(assetEntity)
			def supportAssets = AssetDependency.findAllByDependent(assetEntity)

			[applicationInstance:applicationInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
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
		def applicationInstance = Application.get(params.id)
		applicationInstance.properties = params
		if(!applicationInstance.hasErrors() && applicationInstance.save(flush:true)) {
			flash.message = "Application ${applicationInstance.assetName} Updated"
			assetEntityService.createOrUpdateApplicationDependencies(params, applicationInstance)
			redirect(action:list,id:applicationInstance.id)
		}
		else {
			flash.message = "Application not created"
			applicationInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list,id:applicationInstance.id)
		}
	}
	def delete = {
		def applicationInstance = Application.get( params.id )
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
			AssetDependency.executeUpdate("delete AssetDependency where asset = ? or dependent = ? ",[applicationInstance, applicationInstance])
			AssetEntity.executeUpdate("delete from AssetEntity ae where ae.id = ${assetEntityInstance.id}")
			Application.executeUpdate("delete from Application a where a.id = ${applicationInstance.id}")
			flash.message = "Application ${assetName} deleted"
		}
		else {
			flash.message = "Application not found with id ${params.id}"
		}
		redirect( action:list )
	}
	
}
