import grails.converters.JSON

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
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.GormUtil


class DatabaseController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def assetEntityService  
	def taskService 
	def securityService
    def index = {
		redirect(action: "list", params: params)
    }
	
	def list ={
		def project = securityService.getUserCurrentProject()
		def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') \
				and project =:project order by assetName asc",[project:project])
		def applications = Application.findAll('from Application where assetType = ? and project =? order by assetName asc',['Application', project])
		def dbs = Database.findAll('from Database where assetType = ? and project =? order by assetName asc',['Database', project])
		def files = Files.findAll('from Files where assetType = ? and project =? order by assetName asc',['Files', project])
		
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)

		return [assetDependency: new AssetDependency(),
			servers : servers, applications : applications, dbs : dbs, files : files, dependencyType:dependencyType,
			dependencyStatus:dependencyStatus,staffRoles:taskService.getRolesForStaff(),
			event:params.moveEvent, filter:params.filter, plannedStatus:params.plannedStatus, validation:params.validation]
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
		
		if(params.event && params.event.isNumber()){
			def moveEvent = MoveEvent.read( params.event )
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useOfPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
		}
		
		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%${params.moveBundle}%", project) : []
		
		
		def dbs = Database.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			if (params.assetName)
				ilike('assetName', "%${params.assetName}%")
			if (params.dbFormat)
				ilike('dbFormat', "%${params.dbFormat}%")
			if (params.planStatus)
				ilike('planStatus', "%${params.planStatus}%")
			if (bundleList)
				'in'('moveBundle', bundleList)
				
			eq("assetType",  AssetType.DATABASE.toString() )
			
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

		def totalRows = dbs.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)

		def results = dbs?.collect { [ cell: ['',it.assetName, it.dbFormat, it.validation,
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
					case "application":
						redirect( controller:'application', action:list)
						break;
					case "files":
						redirect( controller:'files', action:list)
						break;
					case "listComment":
						redirect( controller:'assetEntity', action:'listComment' , params:[projectId: projectId])
						break;
					case "planningConsole":
						forward( controller:'assetEntity',action:'getLists', params:[entity: params.tabType,dependencyBundle:session.getAttribute("dependencyBundle"),labelsList:'apps'])
						break;
					default:
						redirect( action:list,params:[tag_f_assetName:filterAttr.tag_f_assetName, tag_f_dbFormat:filterAttr.tag_f_dbFormat, tag_f_moveBundle:filterAttr.tag_f_moveBundle, tag_f_planStatus:filterAttr.tag_f_planStatus, tag_f_depUp:filterAttr.tag_f_depUp, tag_f_depDown:filterAttr.tag_f_depDown])
				}
			}
		}
		else {
			flash.message = "DataBase not created"
			databaseInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list)
		}
		
	}
	def delete = {
		def database = Database.get( params.id )
		if( database ) {
			def assetName = database.assetName
			assetEntityService.deleteAsset( database )
			database.delete()
			
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
	/**
	 * Delete multiple database.
	 */
	def deleteBulkAsset={
		def assetList = params.id.split(",")
		def assetNames = []
		assetList.each{ assetId->
			def database = Database.get( assetId )
			if( database ) {
				assetNames.add(database.assetName)
				assetEntityService.deleteAsset( database )
				database.delete()
			}
		}
	  String names = assetNames.toString().replace('[','').replace(']','')
	  
	  render "Database ${names} deleted"
	}
}
