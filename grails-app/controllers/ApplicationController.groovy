import com.tds.asset.Application;
import com.tds.asset.AssetDependency;
import com.tds.asset.AssetEntity;

import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit
import net.tds.util.jmesa.AssetEntityBean

import grails.converters.JSON
class ApplicationController {
	def partyRelationshipService
	def index = {

		redirect(action:list,params:params)
	}

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def  list ={
		def projectId = params.projectId ? params.projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def workFlow = project.workflowCode
		def assetEntityList = Application.findAllByProject(project)
		def appBeanList = new ArrayList()
		assetEntityList.each {appAssetEntity->
			AssetEntityBean appBeanInstance = new AssetEntityBean();
			appBeanInstance.setId(appAssetEntity.id)
			appBeanInstance.setApplication(appAssetEntity.application)
			appBeanInstance.setAppOwner(appAssetEntity.appOwner)
			appBeanInstance.setAppSme(appAssetEntity.appSme)
			appBeanInstance.setMoveBundle(appAssetEntity.moveBundle?.name)
			appBeanInstance.setplanStatus(appAssetEntity.planStatus)
			appBeanList.add(appBeanInstance)
		}
		TableFacade tableFacade = new TableFacadeImpl("tag", request)
		try{
			tableFacade.items = appBeanList
			Limit limit = tableFacade.limit
			if(limit.isExported()){
				tableFacade.setExportTypes(response,limit.getExportType())
				tableFacade.setColumnProperties("id","application","assetName","shortName","serialNumber","assetTag","manufacturer","model","assetType","ipAddress","os","sourceLocation","sourceRoom","sourceRack","sourceRackPosition","sourceBladeChassis","sourceBladePosition","targetLocation","targetRoom","targetRack","targetRackPosition","targetBladeChassis","targetBladePosition","custom1","custom2","custom3","custom4","custom5","custom6","custom7","custom8","moveBundle","sourceTeamMt","targetTeamMt","sourceTeamLog","targetTeamLog","sourceTeamSa","targetTeamSa","sourceTeamDba","targetTeamDba","truck","cart","shelf","railType","appOwner","appSme","priority")
				tableFacade.render()
			}else
				return [assetEntityList : appBeanList , projectId: projectId]
		}catch(Exception e){
			return [assetEntityList : null,projectId: projectId]
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

	def update ={
		def applicationInstance = new Application(params)
		if(!applicationInstance.save(flush:true)){
			applicationInstance.errors.allErrors.each { println it }
		}
		redirect(actionName : list)

	}

}
