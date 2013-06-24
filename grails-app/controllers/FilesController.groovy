import grails.converters.JSON
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
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.ApplicationConstants

class FilesController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def assetEntityService
	def taskService
	def securityService
	def userPreferenceService
	def index ={
	     redirect(action : list)
		 	
	}
	def list={
		def filters = session.FILES?.JQ_FILTERS
		session.FILES?.JQ_FILTERS = []
		def project = securityService.getUserCurrentProject()
		def entities = assetEntityService.entityInfo( project )
		def sizePref = userPreferenceService.getPreference("assetListSize")?: '25'
		
		return [assetDependency: new AssetDependency(), servers : entities.servers, applications : entities.applications, dbs : entities.dbs, networks : entities.networks ,
			files : entities.files, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus,
			event:params.moveEvent, filter:params.filter, plannedStatus:params.plannedStatus, validation:params.validation,
			staffRoles:taskService.getRolesForStaff(), moveBundleId:params.moveBundleId, fileName:filters?.assetNameFilter ?:'', 
			fileFormat:filters?.fileFormatFilter, fileSize:filters?.fileSizeFilter,
			moveBundle:filters?.moveBundleFilter ?:'', planStatus:filters?.planStatusFilter ?:'', sizePref:sizePref]
		
	}
	/**
	 * This method is used by JQgrid to load assetList
	 */
	def listJson = {
		def sortIndex = params.sidx ?: 'assetName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def project = securityService.getUserCurrentProject()
		def moveBundleList
		session.FILES = [:]
		
		userPreferenceService.setPreference("assetListSize", "${maxRows}")
		if(params.event && params.event.isNumber()){
			def moveEvent = MoveEvent.read( params.event )
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useOfPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
		}
		
		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%${params.moveBundle}%", project) : []
		
		def filesSize = params.fileSize ? Files.findAll("from Files where fileSize like '%${params.fileSize}%' and project =:project",[project:project])?.fileSize : []
		
		def files = AssetEntity.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			if (params.assetName)
				ilike('assetName', "%${params.assetName}%")
			if (params.fileFormat)
				ilike('fileFormat', "%${params.fileFormat}%")
				
			if (params.planStatus)
				ilike('planStatus', "%${params.planStatus}%")
			if (bundleList)
				'in'('moveBundle', bundleList)
			if(filesSize){
			  'in'('fileSize',filesSize)	
			}
			
			eq("assetType",  AssetType.FILES.toString() )
			
			if(params.moveBundleId){
				if(params.moveBundleId =='unAssigned'){
					isNull('moveBundle')
				} else {
					eq('moveBundle', MoveBundle.read(params.moveBundleId))
				}
			}
			
			if(params.filter){
				or{
					and {
						'in'('moveBundle', moveBundleList)
					}
					and {
						isNull('moveBundle')
					}
				}
				
				if( params.validation)
					eq ('validation', params.validation)
				
				if(params.plannedStatus)
					eq("planStatus", params.plannedStatus)
				
			}

			order(sortIndex, sortOrder).ignoreCase()
		}

		def totalRows = files.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)

		def results = files?.collect { [ cell: ['',it.assetName, it.fileFormat, it.fileSize, it.planStatus,
					it.moveBundle?.name, AssetDependencyBundle.findByAsset(it)?.dependencyBundle,
					AssetDependency.countByDependentAndStatusNotEqual(it, "Validated"),
					AssetDependency.countByAssetAndStatusNotEqual(it, "Validated"),
					AssetComment.find("from AssetComment ac where ac.assetEntity=:entity and commentType=:type and status!=:status",
					[entity:it, type:'issue', status:'completed']) ? 'issue' :
					(AssetComment.find("from AssetComment ac where ac.assetEntity=:entity",[entity:it]) ? 'comment' : 'blank'),
					it.assetType], id: it.id,
			]}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	def create ={
		def fileInstance = new Files(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project)
		//fieldImportance for Discovery by default
		def configMap = assetEntityService.getConfig('Files','Discovery')
		
		[fileInstance:fileInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId, project:project,
					planStatusOptions:planStatusOptions.value, config:configMap.config, customs:configMap.customs]
	}
	def save = {
				params.assetType = "Files"
				def filesInstance = new Files(params)
				if(!filesInstance.hasErrors() && filesInstance.save()) {
					flash.message = "Storage ${filesInstance.assetName} created"
					assetEntityService.createOrUpdateFilesDependencies(params, filesInstance)
			        session.FILES?.JQ_FILTERS = params
					redirect( action:list)
				}
				else {
					flash.message = "Storage not created"
					filesInstance.errors.allErrors.each{ flash.message += it}
					session.FILES?.JQ_FILTERS = params
					redirect( action:list)
				}
		
			
	 }
	def show ={
		def id = params.id
		def project = securityService.getUserCurrentProject()
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
			//field importance styling for respective validation.
			def validationType = assetEntity.validation
			def configMap = assetEntityService.getConfig('Files',validationType)
			
			def prefValue= userPreferenceService.getPreference("showAllAssetTasks") ?: 'FALSE'
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)
			[ filesInstance : filesInstance,supportAssets: supportAssets, dependentAssets:dependentAssets, redirectTo : params.redirectTo ,assetComment:assetComment, assetCommentList:assetCommentList,
			  dependencyBundleNumber:AssetDependencyBundle.findByAsset(filesInstance)?.dependencyBundle, project:project ,prefValue:prefValue,
			   config:configMap.config, customs:configMap.customs]
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
			//fieldImportance Styling for default validation.
			def validationType = fileInstance.validation
			def configMap = assetEntityService.getConfig('Files',validationType)
			
			[fileInstance:fileInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList, project : project,
						planStatusOptions:planStatusOptions?.value, projectId:projectId, supportAssets: supportAssets, 
						dependentAssets:dependentAssets, redirectTo : params.redirectTo, dependencyType:dependencyType, 
						dependencyStatus:dependencyStatus,servers:servers, config:configMap.config, customs:configMap.customs]
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
					case "listTask":
						render "Storage ${filesInstance.assetName} updated."
						break;
				    case "planningConsole":
						forward( controller:'assetEntity',action:'getLists', params:[entity: params.tabType,dependencyBundle:session.getAttribute("dependencyBundle"),labelsList:'apps'])
						break;
					default:
						session.FILES?.JQ_FILTERS = params
						redirect( action:list)
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
		def files = Files.get( params.id )
		if( files ) {
			def assetName = files.assetName
			assetEntityService.deleteAsset( files )
			files.delete()
			
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
		def assetList = params.id.split(",")
		def assetNames = []
	
		assetList.each{ assetId->
			def files = Files.get( assetId )
			if( files ) {
				assetNames.add(files.assetName)
				assetEntityService.deleteAsset( files )
				
				files.delete()
			}
		}
	  String names = assetNames.toString().replace('[','').replace(']','')
	  render "Files ${names} deleted"
	}
}
