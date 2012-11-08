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
class FilesController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def assetEntityService
	def taskService
	def index ={
	     redirect(action : list)
		 	
	}
	def list={
		def filterAttributes = [tag_f_assetName:params.tag_f_assetName,tag_f_fileFormat:params.tag_f_fileFormat,tag_f_fileSize:params.tag_f_fileSize,tag_f_moveBundle:params.tag_f_moveBundle,tag_f_planStatus:params.tag_f_planStatus,tag_f_depUp:params.tag_f_depUp,tag_f_depDown:params.tag_f_depDown]
		session.setAttribute('filterAttributes', filterAttributes)
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
		String moveBundle = moveBundleList.id
		moveBundle = moveBundle.replace("[","('").replace(",","','").replace("]","')")
		def fileInstanceList
		if(params.moveEvent=='unAssigned'){
			fileInstanceList = Files.findAll("from Files where project = $projectId and assetType=? and moveBundle in $moveBundle and (planStatus is null or planStatus in ('Unassigned',''))",['Files'])
		}else if(params.moveEvent && params.moveEvent!='unAssigned' ){
			def moveEvent = MoveEvent.get(params.moveEvent)
			def moveBundles = moveEvent.moveBundles
			def bundles = moveBundles.findAll {it.useOfPlanning == true}
			fileInstanceList= Files.findAllByMoveBundleInListAndAssetType(bundles,"Files")
		}else{
			fileInstanceList = Files.findAllByProject(project)
		}
		def filesList = new ArrayList();
		fileInstanceList.each {fileentity ->
			def assetEntity = AssetEntity.get(fileentity.id)
			AssetEntityBean filesEntity = new AssetEntityBean();
			filesEntity.setId(fileentity.id)
			filesEntity.setAssetType(fileentity.assetType)
			filesEntity.setAssetName(fileentity.assetName)
			filesEntity.setFileFormat(fileentity.fileFormat)
			filesEntity.setFileSize(fileentity.fileSize)
			filesEntity.setMoveBundle(fileentity?.moveBundle?.name)
			filesEntity.setPlanStatus(fileentity.planStatus)
			filesEntity.setDepUp(AssetDependency.countByDependentAndStatusNotEqual(assetEntity, "Validated"))
			filesEntity.setDepDown(AssetDependency.countByAssetAndStatusNotEqual(assetEntity, "Validated"))
			filesEntity.setDependencyBundleNumber(AssetDependencyBundle.findByAsset(fileentity)?.dependencyBundle)
			if(AssetComment.find("from AssetComment where assetEntity = ${assetEntity?.id} and commentType = ? and isResolved = ?",['issue',0])){
				filesEntity.setCommentType("issue")
			} else if(AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id)){
				filesEntity.setCommentType("comment")
			} else {
				filesEntity.setCommentType("blank")
			}
			filesList.add(filesEntity)
		}
		TableFacade tableFacade = new TableFacadeImpl("tag", request)
		def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$projectId order by assetName asc")
		def applications = Application.findAll('from Application where assetType = ? and project =? order by assetName asc',['Application', project])
		def dbs = Database.findAll('from Database where assetType = ? and project =? order by assetName asc',['Database', project])
		def files = Files.findAll('from Files where assetType = ? and project =? order by assetName asc',['Files', project])
		
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		
		try{
			tableFacade.items = filesList
			Limit limit = tableFacade.limit
			if(limit.isExported()){
				tableFacade.setExportTypes(response,limit.getExportType())
				tableFacade.setColumnProperties("id","fileFormat","fileSize","moveBundle","planStatus","assetTag","manufacturer","model","assetType","ipAddress","os","sourceLocation","sourceRoom","sourceRack","sourceRackPosition","sourceBladeChassis","sourceBladePosition","targetLocation","targetRoom","targetRack","targetRackPosition","targetBladeChassis","targetBladePosition","custom1","custom2","custom3","custom4","custom5","custom6","custom7","custom8","moveBundle","sourceTeamMt","targetTeamMt","sourceTeamLog","targetTeamLog","sourceTeamSa","targetTeamSa","sourceTeamDba","targetTeamDba","truck","cart","shelf","railType","appOwner","appSme","priority")
				tableFacade.render()
			}else
				return [filesList : filesList , projectId: projectId ,assetDependency: new AssetDependency(),
					servers : servers, applications : applications, dbs : dbs, files : files,dependencyType:dependencyType,dependencyStatus:dependencyStatus,
					staffRoles:taskService.getRolesForStaff()]
		}catch(Exception e){
			return [filesList : null,projectId: projectId,
					servers : servers, applications : applications, dbs : dbs, files : files]
		}
		
	}
	def create ={
		def fileInstance = new Files(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project)

		[fileInstance:fileInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId, project:project,planStatusOptions:planStatusOptions.value]
	}
	def save = {
				params.assetType = "Files"
				def filesInstance = new Files(params)
				if(!filesInstance.hasErrors() && filesInstance.save()) {
					flash.message = "Storage ${filesInstance.assetName} created"
					assetEntityService.createOrUpdateFilesDependencies(params, filesInstance)
			        redirect(action:list)
				}
				else {
					flash.message = "Storage not created"
					filesInstance.errors.allErrors.each{ flash.message += it}
					redirect(action:list)
				}
		
			
	 }
	def show ={
		def id = params.id
		def filesInstance = Files.get( id )
		if(!filesInstance) {
			flash.message = "Storage not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			def assetEntity = AssetEntity.get(id)
			def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntity])
			def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntity])
			def assetComment
			if(AssetComment.find("from AssetComment where assetEntity = ${filesInstance?.id} and commentType = ? and isResolved = ?",['issue',0])){
				assetComment = "issue"
			} else if(AssetComment.find('from AssetComment where assetEntity = '+ filesInstance?.id)){
				assetComment = "comment"
			} else {
				assetComment = "blank" 
			}
			
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)
			[ filesInstance : filesInstance,supportAssets: supportAssets, dependentAssets:dependentAssets, redirectTo : params.redirectTo ,assetComment:assetComment, assetCommentList:assetCommentList,
			  dependencyBundleNumber:AssetDependencyBundle.findByAsset(filesInstance)?.dependencyBundle]
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
		def fileInstance = Files.get( id )
		if(!fileInstance) {
			flash.message = "Storage not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			def assetEntity = AssetEntity.get(id)
			def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntity])
			def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntity])
			def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
			def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
			def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$projectId order by assetName asc")

			[fileInstance:fileInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList, project : project,
						planStatusOptions:planStatusOptions?.value, projectId:projectId, supportAssets: supportAssets, 
						dependentAssets:dependentAssets, redirectTo : params.redirectTo, dependencyType:dependencyType, dependencyStatus:dependencyStatus,servers:servers]
		}
		
	}
	def update ={
		def attribute = session.getAttribute('filterAttr')
		def filterAttr = session.getAttribute('filterAttributes')
		session.setAttribute("USE_FILTERS","true")
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def filesInstance = Files.get(params.id)
		params.assetType = "Files"
		filesInstance.properties = params
		if(!filesInstance.hasErrors() && filesInstance.save(flush:true)) {
			flash.message = "Storage ${filesInstance.assetName} Updated"
			assetEntityService.createOrUpdateFilesDependencies(params, filesInstance)
			if(params.updateView == 'updateView'){
				forward(action:'show', params:[id: params.id])
				
			}else{
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
					case "application":
						redirect( controller:'application', action:list)
						break;
					case "listComment":
						redirect( controller:'assetEntity', action:'listComment' , params:[projectId: projectId])
						break;
				    case "planningConsole":
						forward( controller:'assetEntity',action:'getLists', params:[entity: params.tabType,dependencyBundle:session.getAttribute("dependencyBundle"),labelsList:'apps'])
						break;
					default:
						redirect( action:list,params:[tag_f_assetName:filterAttr.tag_f_assetName, tag_f_fileFormat:filterAttr.tag_f_fileFormat,tag_f_fileSize:filterAttr.tag_f_fileSize, tag_f_moveBundle:filterAttr.tag_f_moveBundle, tag_f_planStatus:filterAttr.tag_f_planStatus, tag_f_depUp:filterAttr.tag_f_depUp, tag_f_depDown:filterAttr.tag_f_depDown])
				}
			}
		}
		else {
			flash.message = "Storage not created"
			filesInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list)
		}

    }
	def delete = {
		def filesInstance = Files.get( params.id )
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
			AssetDependency.executeUpdate("delete AssetDependency where asset = ? or dependent = ? ",[assetEntityInstance, assetEntityInstance])
			AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset = ${filesInstance.id}")
			
			filesInstance.delete()
			assetEntityInstance.delete()
			flash.message = "Storage ${assetName} deleted"
			if(params.dstPath =='planningConsole'){
				forward( controller:'assetEntity',action:'getLists', params:[entity: 'files',dependencyBundle:session.getAttribute("dependencyBundle")])
			}else{
				redirect( action:list )
			}
		}
		else {
			flash.message = "Storage not found with id ${params.id}"
			redirect( action:list )
		}
		
	}
	def deleteBulkAsset={
		def assetArray = params['assetLists[]']
		def assetList
		if(assetArray.class.toString() == "class java.lang.String"){
		  assetList = assetArray.split(",")
		}else{
		  assetList = assetArray
		}
		def assetNames = []
		def assetEntityInstance
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
	
			for(int i=0 ; i<assetList.size();i++){
				assetEntityInstance = AssetEntity.get( assetList[i] )
				def filesInstance = Files.get(assetList[i] )
				
				if(assetEntityInstance) {
					assetNames.add(assetEntityInstance.assetName)
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
					AssetDependency.executeUpdate("delete AssetDependency where asset = ? or dependent = ? ",[assetEntityInstance, assetEntityInstance])
					AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset = ${filesInstance.id}")
					
					filesInstance.delete()
					assetEntityInstance.delete()
				}
			String names = assetNames.toString().replace('[','').replace(']','')
			flash.message = "Storage ${names} deleted"
		}
	  render "success"
	}
}
