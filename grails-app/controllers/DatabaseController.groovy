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
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetOptions
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
		def filterAttributes = [tag_f_assetName:params.tag_f_assetName,tag_f_dbFormat:params.tag_f_dbFormat,tag_f_moveBundle:params.tag_f_moveBundle,tag_f_planStatus:params.tag_f_planStatus,tag_f_depUp:params.tag_f_depUp,tag_f_depDown:params.tag_f_depDown]
		session.setAttribute('filterAttributes', filterAttributes)
		
		def projectId = params.projectId ? params.projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def databaseInstanceList = Database.findAllByProject(project)
		def databaseList = new ArrayList();
		databaseInstanceList.each {dataBaseentity ->
			def assetEntity = AssetEntity.get(dataBaseentity.id)
			AssetEntityBean dataBeanInstance = new AssetEntityBean();
			dataBeanInstance.setId(dataBaseentity.id)
			dataBeanInstance.setAssetType(dataBaseentity.assetType)
			dataBeanInstance.setDbFormat(dataBaseentity.dbFormat)
			dataBeanInstance.setAssetName(dataBaseentity.assetName)
			dataBeanInstance.setMoveBundle(dataBaseentity?.moveBundle?.name)
			dataBeanInstance.setplanStatus(dataBaseentity.planStatus)
			dataBeanInstance.setDepUp(AssetDependency.countByDependentAndStatusNotEqual(assetEntity, "Validated"))
			dataBeanInstance.setDepDown(AssetDependency.countByAssetAndStatusNotEqual(assetEntity, "Validated"))
			dataBeanInstance.setDependencyBundleNumber(AssetDependencyBundle.findByAsset(dataBaseentity)?.dependencyBundle)
			if(AssetComment.find("from AssetComment where assetEntity = ${assetEntity?.id} and commentType = ? and isResolved = ?",['issue',0])){
				dataBeanInstance.setCommentType("issue")
			} else if(AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id)){
				dataBeanInstance.setCommentType("comment")
			} else {
				dataBeanInstance.setCommentType("blank")
			}
			databaseList.add(dataBeanInstance)
		}
		TableFacade tableFacade = new TableFacadeImpl("tag", request)
		def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$projectId order by assetName asc")
		def applications = Application.findAll('from Application where assetType = ? and project =? order by assetName asc',['Application', project])
		def dbs = Database.findAll('from Database where assetType = ? and project =? order by assetName asc',['Database', project])
		def files = Files.findAll('from Files where assetType = ? and project =? order by assetName asc',['Files', project])
		
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)

		try{
			tableFacade.items = databaseList
			Limit limit = tableFacade.limit
			if(limit.isExported()){
				tableFacade.setExportTypes(response,limit.getExportType())
				tableFacade.setColumnProperties("id","dbFormat","dbSize","moveBundle","planStatus")
				tableFacade.render()
			}else
				return [databaseList : databaseList , projectId: projectId ,assetDependency: new AssetDependency(),
					servers : servers, applications : applications, dbs : dbs, files : files, dependencyType:dependencyType,
					dependencyStatus:dependencyStatus]
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
			def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntity])
			def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntity])
			def assetComment
			if(AssetComment.find("from AssetComment where assetEntity = ${databaseInstance?.id} and commentType = ? and isResolved = ?",['issue',0])){
				assetComment = "issue"
			} else if(AssetComment.find('from AssetComment where assetEntity = '+ databaseInstance?.id)){
				assetComment ="comment"
			} else {
				assetComment ="blank"
			}
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)
			
			[ databaseInstance : databaseInstance,supportAssets: supportAssets, dependentAssets:dependentAssets, redirectTo : params.redirectTo, 
			  assetComment:assetComment, assetCommentList:assetCommentList,dependencyBundleNumber:AssetDependencyBundle.findByAsset(databaseInstance)?.dependencyBundle]
		}
	}
	
	def create ={
		def databaseInstance = new Database(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project)

		[databaseInstance:databaseInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId, project:project]
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
					flash.message = "Database ${dbInstance.assetName} created"
					assetEntityService.createOrUpdateDatabaseDependencies(params, dbInstance)
			        redirect(action:list)
		 	    }else {
					flash.message = "Database not created"
					dbInstance.errors.allErrors.each{ flash.message += it  }
					redirect(action:list)
				}
				
		
			
     }
	def edit ={
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
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
			def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntity])
			def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntity])
			def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
			def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
			def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$projectId order by assetName asc")

			[databaseInstance:databaseInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList, project:project,
						planStatusOptions:planStatusOptions?.value, projectId:projectId, supportAssets: supportAssets, 
						dependentAssets:dependentAssets, redirectTo : params.redirectTo, dependencyType:dependencyType, dependencyStatus:dependencyStatus,servers:servers]
		}
		
		}
	
	def update ={
		def attribute = session.getAttribute('filterAttr')
		def filterAttr = session.getAttribute('filterAttributes')
		session.setAttribute("USE_FILTERS","true")
		
		def formatter = new SimpleDateFormat("MM/dd/yyyy")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def maintExpDate = params.maintExpDate
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		if(maintExpDate){
			params.maintExpDate =  GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
		}
		def retireDate = params.retireDate
		if(retireDate){
			params.retireDate =  GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
		}
		def databaseInstance = Database.get(params.id)
		databaseInstance.properties = params
		if(!databaseInstance.hasErrors() && databaseInstance.save(flush:true)) {
			flash.message = "DataBase ${databaseInstance.assetName} Updated"
			assetEntityService.createOrUpdateDatabaseDependencies(params, databaseInstance)
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
				case "application":
					redirect( controller:'application', action:list)
					break;
				case "files":
					redirect( controller:'files', action:list)
					break;
				case "planningConsole":
					forward( controller:'assetEntity',action:'getLists', params:[entity: 'database',dependencyBundle:session.getAttribute("dependencyBundle")])
					break;
				default:
					redirect( action:list,params:[tag_f_assetName:filterAttr.tag_f_assetName, tag_f_dbFormat:filterAttr.tag_f_dbFormat, tag_f_moveBundle:filterAttr.tag_f_moveBundle, tag_f_planStatus:filterAttr.tag_f_planStatus, tag_f_depUp:filterAttr.tag_f_depUp, tag_f_depDown:filterAttr.tag_f_depDown])
			}
		}
		else {
			flash.message = "DataBase not created"
			databaseInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list)
		}
		
	}
	def delete = {
		def databaseInstance = Database.get( params.id )
		def assetEntityInstance = AssetEntity.get(params.id)
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
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
			AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset = ${databaseInstance.id}")
			flash.message = "Database ${assetName} deleted"
			if(params.dstPath =='planningConsole'){
				forward( controller:'assetEntity',action:'getLists', params:[entity: 'database',dependencyBundle:session.getAttribute("dependencyBundle")])
			}else{
				redirect( action:list )
			}
		}
		else {
			flash.message = "Database not found with id ${params.id}"
			redirect( action:list )
		}
		
	}
}
