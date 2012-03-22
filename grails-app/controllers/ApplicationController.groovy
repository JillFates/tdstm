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
import com.tds.asset.AssetOptions
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
		def filterAttributes = [tag_f_assetName:params.tag_f_assetName,tag_f_appOwner:params.tag_f_appOwner,tag_f_appSme:params.tag_f_appSme,tag_f_planStatus:params.tag_f_planStatus,tag_f_depUp:params.tag_f_depUp,tag_f_depDown:params.tag_f_depDown]
		session.setAttribute('filterAttributes', filterAttributes)
		
		def projectId = params.projectId ? params.projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def workFlow = project.workflowCode
		def appEntityList
		if(params.validation=='Discovery'){
			appEntityList = Application.findAllByValidationAndProject('Discovery',project)
	    }else if(params.validation=='DependencyReview'){
		    appEntityList = Application.findAllByValidationAndProject('DependencyReview',project)
		}else if(params.validation=='DependencyScan'){
			appEntityList = Application.findAllByValidationAndProject('DependencyScan',project)
		}else if(params.validation=='BundleReady'){
			appEntityList = Application.findAllByValidationAndProject('BundleReady',project)
		}else{
		    appEntityList = Application.findAllByProject(project)
		}
		def appBeanList = new ArrayList()
		appEntityList.each { appEntity->
			def assetEntity = AssetEntity.get(appEntity.id)
			AssetEntityBean appBeanInstance = new AssetEntityBean();
			appBeanInstance.setId(appEntity.id)
			appBeanInstance.setAssetName(appEntity.assetName)
			appBeanInstance.setAssetType(appEntity.assetType)
			appBeanInstance.setAppOwner(appEntity.appOwner)
			appBeanInstance.setAppSme(appEntity.appSme)
			appBeanInstance.setMoveBundle(appEntity.moveBundle?.name)
			appBeanInstance.setplanStatus(appEntity.planStatus)
			appBeanInstance.setDepUp(AssetDependency.countByDependentAndStatusNotEqual(assetEntity, "Validated"))
			appBeanInstance.setDepDown(AssetDependency.countByAssetAndStatusNotEqual(assetEntity, "Validated"))
			
			if(AssetComment.find("from AssetComment where assetEntity = ${assetEntity?.id} and commentType = ? and isResolved = ?",['issue',0])){
				appBeanInstance.setCommentType("issue")
			} else if(AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id)){
				appBeanInstance.setCommentType("comment")
			} else {
				appBeanInstance.setCommentType("blank")
			}
			
			appBeanList.add(appBeanInstance)
		}
		TableFacade tableFacade = new TableFacadeImpl("tag", request)
		def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$projectId order by assetName asc")
		def applications = Application.findAll('from Application where assetType = ? and project =? order by assetName asc',['Application', project])
		def dbs = Database.findAll('from Database where assetType = ? and project =? order by assetName asc',['Database', project])
		def files = Files.findAll('from Files where assetType = ? and project =? order by assetName asc',['Files', project])
		
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		
		try{
			tableFacade.items = appBeanList
			Limit limit = tableFacade.limit
			if(limit.isExported()){
				tableFacade.setExportTypes(response,limit.getExportType())
				tableFacade.setColumnProperties("id","application","appOwner","appSme","movebundle","planStatus")
				tableFacade.render()
			} else {
				return [assetEntityList : appBeanList , projectId: projectId, assetDependency: new AssetDependency(),
					servers : servers, applications : applications, dbs : dbs, files : files,dependencyType:dependencyType,dependencyStatus:dependencyStatus]
			}
		}catch(Exception e){
			return [assetEntityList : null, projectId: projectId , servers : servers, applications : applications, dbs : dbs, files : files]
		}
	}
	def create ={
		def applicationInstance = new Application(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def moveEventList = MoveEvent.findAllByProject(project,[sort:'name'])

		[applicationInstance:applicationInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId, project:project,moveEventList:moveEventList]
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
			def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
			def project = Project.read(projectId)
			def moveEventList = MoveEvent.findAllByProject(project).id
			for(int i : moveEventList){
				def okToMove = params["okToMove_"+i]
				def appMoveInstance = new AppMoveEvent()
				appMoveInstance.application = applicationInstance
				appMoveInstance.moveEvent = MoveEvent.get(i)
				appMoveInstance.value = okToMove
				if(!appMoveInstance.save(flush:true)){
					appMoveInstance.errors.allErrors.each { println it }
				}
			}
			redirect(action:list)
		}
		else {
			flash.message = "Application not created"
			applicationInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list)
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
			def assetComment 
			def dependentAssets = AssetDependency.findAllByAsset(assetEntity)
			def supportAssets = AssetDependency.findAllByDependent(assetEntity)
			if(AssetComment.find("from AssetComment where assetEntity = ${applicationInstance?.id} and commentType = ? and isResolved = ?",['issue',0])){
				assetComment = "issue"
			} else if(AssetComment.find('from AssetComment where assetEntity = '+ applicationInstance?.id)){
				assetComment = "comment"
			} else {
				assetComment = "blank"
			}
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)	
			def appMoveEvent = AppMoveEvent.findAllByApplication(applicationInstance)
			def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
			def project = Project.read(projectId)
		    def moveEventList = MoveEvent.findAllByProject(project,[sort:'name'])
			def appMoveEventlist = AppMoveEvent.findAllByApplication(applicationInstance).value
			[ applicationInstance : applicationInstance,supportAssets: supportAssets, dependentAssets:dependentAssets, redirectTo : params.redirectTo ,assetComment:assetComment, assetCommentList:assetCommentList,
			  appMoveEvent:appMoveEvent,moveEventList:moveEventList,appMoveEvent:appMoveEventlist]
		}
	}

	def edit ={

		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])

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
			def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
			def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
			def moveEvent = MoveEvent.findAllByProject(project,,[sort:'name'])
			def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$projectId order by assetName asc")
			moveEvent.each{
			   def appMoveList = AppMoveEvent.findByApplicationAndMoveEvent(applicationInstance,it)
			   if(!appMoveList){
				   def appMoveInstance = new AppMoveEvent()
				   appMoveInstance.application = applicationInstance
				   appMoveInstance.moveEvent = it
				   appMoveInstance.save(flush:true)
			   }
			}
			[applicationInstance:applicationInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList, project : project,
						planStatusOptions:planStatusOptions?.value, projectId:projectId, supportAssets: supportAssets,
						dependentAssets:dependentAssets, redirectTo : params.redirectTo,dependencyType:dependencyType, dependencyStatus:dependencyStatus,
						moveEvent:moveEvent,servers:servers]
		}

	}

	def update ={
		def attribute = session.getAttribute('filterAttr')
		def filterAttr = session.getAttribute('filterAttributes')
		session.setAttribute("USE_FILTERS","true")
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
			def appMoveEventList = AppMoveEvent.findAllByApplication(applicationInstance)?.moveEvent?.id
			if(appMoveEventList.size()>0){
				for(int i : appMoveEventList){
					def okToMove = params["okToMove_"+i]
					def appMoveInstance = AppMoveEvent.findByMoveEventAndApplication(MoveEvent.get(i),applicationInstance)
					    appMoveInstance.value = okToMove
					    appMoveInstance.save(flush:true)
				}
			}			  
			switch(params.redirectTo){
				case "room":
					redirect( controller:'room',action:list )
					break;
				case "rack":
					redirect( controller:'rackLayouts',action:'create' )
					break;
				case "console":
					redirect( controller:'assetEntity', action:"dashboardView", params:[showAll:'show'])
					break;
				case "clientConsole":
					redirect( controller:'clientConsole', action:list)
					break;
				case "assetEntity":
					redirect( controller:'assetEntity', action:list)
					break;
				case "database":
					redirect( controller:'database', action:list)
					break;
				case "files":
					redirect( controller:'files', action:list)
					break;
				default:
					redirect( action:list,params:[tag_f_assetName:filterAttr.tag_f_assetName, tag_f_appOwner:filterAttr.tag_f_appOwner, tag_f_appSme:filterAttr.tag_f_appSme, tag_f_planStatus:filterAttr.tag_f_planStatus, tag_f_depUp:filterAttr.tag_f_depUp, tag_f_depDown:filterAttr.tag_f_depDown])
			}
		}
		else {
			flash.message = "Application not created"
			applicationInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list)
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
			def appMoveInstance = AppMoveEvent.findAllByApplication(applicationInstance);
			appMoveInstance.each{
			         it.delete(flush:true)
			}
			flash.message = "Application ${assetName} deleted"
		}
		else {
			flash.message = "Application not found with id ${params.id}"
		}
		redirect( action:list )
	}	
}
