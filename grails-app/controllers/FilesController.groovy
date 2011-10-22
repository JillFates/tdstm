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
class FilesController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def assetEntityService
	def index ={
	     redirect(action : list)
		 	
	}
	def list={
		def projectId = params.projectId ? params.projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def fileInstanceList = Files.findAllByProject(project)
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
			filesEntity.setplanStatus(fileentity.planStatus)
			filesEntity.setDepUp(AssetDependency.countByDependent(assetEntity))
			filesEntity.setDepDown(AssetDependency.countByAsset(assetEntity))
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
		def servers = AssetEntity.findAllByAssetTypeAndProject('Server',project)
		def applications = Application.findAllByAssetTypeAndProject('Application',project)
		def dbs = Database.findAllByAssetTypeAndProject('Database',project)
		def files = Files.findAllByAssetTypeAndProject('Files',project)
		try{
			tableFacade.items = filesList
			Limit limit = tableFacade.limit
			if(limit.isExported()){
				tableFacade.setExportTypes(response,limit.getExportType())
				tableFacade.setColumnProperties("id","fileFormat","fileSize","moveBundle","planStatus","assetTag","manufacturer","model","assetType","ipAddress","os","sourceLocation","sourceRoom","sourceRack","sourceRackPosition","sourceBladeChassis","sourceBladePosition","targetLocation","targetRoom","targetRack","targetRackPosition","targetBladeChassis","targetBladePosition","custom1","custom2","custom3","custom4","custom5","custom6","custom7","custom8","moveBundle","sourceTeamMt","targetTeamMt","sourceTeamLog","targetTeamLog","sourceTeamSa","targetTeamSa","sourceTeamDba","targetTeamDba","truck","cart","shelf","railType","appOwner","appSme","priority")
				tableFacade.render()
			}else
				return [filesList : filesList , projectId: projectId ,assetDependency: new AssetDependency(),
					servers : servers, applications : applications, dbs : dbs, files : files]
		}catch(Exception e){
			return [filesList : null,projectId: projectId,
					servers : servers, applications : applications, dbs : dbs, files : files]
		}
		
	}
	def create ={
		def fileInstance = new Files(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusAttribute = EavAttribute.findByAttributeCode('planStatus')
		def planStatusOptions = EavAttributeOption.findAllByAttribute(planStatusAttribute)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project)

		[fileInstance:fileInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId]
	}
	def save = {
		
				def filesInstance = new Files(params)
				if(!filesInstance.hasErrors() && filesInstance.save()) {
					flash.message = "File ${filesInstance.assetName} created"
					assetEntityService.createOrUpdateFilesDependencies(params, filesInstance)
			        redirect(action:list,id:filesInstance.id)
				}
				else {
					flash.message = "File not created"
					filesInstance.errors.allErrors.each{ flash.message += it}
					redirect(action:list,id:filesInstance.id)
				}
		
			
	 }
	def show ={
		def id = params.id
		def filesInstance = Files.get( id )
		if(!filesInstance) {
			flash.message = "File not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			def assetEntity = AssetEntity.get(id)
			def dependentAssets = AssetDependency.findAllByAsset(assetEntity)
			def supportAssets = AssetDependency.findAllByDependent(assetEntity)
			[ filesInstance : filesInstance,supportAssets: supportAssets, dependentAssets:dependentAssets]
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
		def fileInstance = Files.get( id )
		if(!fileInstance) {
			flash.message = "File not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			def assetEntity = AssetEntity.get(id)
			def dependentAssets = AssetDependency.findAllByAsset(assetEntity)
			def supportAssets = AssetDependency.findAllByDependent(assetEntity)

			[fileInstance:fileInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList, project : project,
						planStatusOptions:planStatusOptions?.value, projectId:projectId, supportAssets: supportAssets, dependentAssets:dependentAssets]
		}
		
	}
	def update ={
		def filesInstance = Files.get(params.id)
		filesInstance.properties = params
		if(!filesInstance.hasErrors() && filesInstance.save()) {
			flash.message = "File ${filesInstance.assetName} Updated"
			assetEntityService.createOrUpdateFilesDependencies(params, filesInstance)
			redirect(action:list,id:filesInstance.id)
		}
		else {
			flash.message = "File not created"
			filesInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list,id:filesInstance.id)
		}

    }
	def delete = {
		def filesInstance = Files.get( params.id )
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
			Files.executeUpdate("delete from Database d where d.id = ${filesInstance.id}")
			flash.message = "Files ${assetName} deleted"
		}
		else {
			flash.message = "Files not found with id ${params.id}"
		}
		redirect( action:list )
	}
}
