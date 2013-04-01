import org.jmesa.limit.Limit
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

class ApplicationController {
	def partyRelationshipService
	def assetEntityService
	def taskService
	def securityService
	def userPreferenceService
	
	def index = {
		redirect(action:list,params:params)
	}

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def  list ={
		def filters = session.APP?.JQ_FILTERS
		session.APP?.JQ_FILTERS = []
		def project = securityService.getUserCurrentProject()
		
		def entities = assetEntityService.entityInfo( project )
		def sizePref = userPreferenceService.getPreference("assetListSize")?: '25'
		
		return [projectId: project.id, assetDependency: new AssetDependency(),
			servers : entities.servers, applications : entities.applications, dbs : entities.dbs, files : entities.files, networks : entities.networks, dependencyType:entities.dependencyType, 
			dependencyStatus:entities.dependencyStatus,event:params.moveEvent, filter:params.filter, latency:params.latency,
		    staffRoles:taskService.getRolesForStaff(), plannedStatus:params.plannedStatus, appSme : filters?.appSmeFilter ?:'',
			validation:params.validation, moveBundleId:params.moveBundleId, appName:filters?.assetNameFilter ?:'', sizePref:sizePref, 
			validationFilter:filters?.appValidationFilter ?:'', moveBundle:filters?.moveBundleFilter ?:'', planStatus:filters?.planStatusFilter ?:''
			]
	}
	/**
	 * This method is used by JQgrid to load appList
	 */
	def listJson = {
		def sortIndex = params.sidx ?: 'assetName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def project = securityService.getUserCurrentProject()
		
		def moveBundleList = []
		session.APP = [:]
		userPreferenceService.setPreference("assetListSize", "${maxRows}")
		if(params.event && params.event.isNumber()){
			def moveEvent = MoveEvent.read( params.event )
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useOfPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
		}
		
		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%${params.moveBundle}%", project) : []
		
		def apps = Application.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			eq("assetType",  AssetType.APPLICATION.toString() )
			if (params.assetName)
				ilike('assetName', "%${params.assetName}%")
			if (params.sme)
				ilike('sme', "%${params.sme}%")
			if (params.validation)
				ilike('validation', "%${params.validation}%")
			if (params.planStatus)
				ilike('planStatus', "%${params.planStatus}%")
			if (bundleList)
				'in'('moveBundle', bundleList)
			if(params.plannedStatus)
				eq("planStatus", params.plannedStatus)
				
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
				
				if( params.validationFilter)
					eq ('validation', params.validationFilter)
				
			    if( params.latency && params.latency =='unknown'){
					or{
						and {
							eq('latency', '')
						}
						and {
							isNull('latency')
						}
					}
				} else if ( params.latency && params.latency !='unknown') {
					eq('latency', params.latency)
				}
				
				if(params.plannedStatus)
					eq("planStatus", params.plannedStatus)
				
			}

			order(sortIndex, sortOrder).ignoreCase()
		}

		def totalRows = apps.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)

		def results = apps?.collect { [ cell: ['',it.assetName, it.sme, it.validation, it.planStatus,
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
			session.APP?.JQ_FILTERS = params
			redirect( action:list)
		}
		else {
			flash.message = "Application not created"
			applicationInstance.errors.allErrors.each{ flash.message += it }
			session.APP?.JQ_FILTERS = params
			redirect( action:list)
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
			def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntity])
			def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntity])
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
			[ applicationInstance : applicationInstance,supportAssets: supportAssets, dependentAssets:dependentAssets, 
			  redirectTo : params.redirectTo, assetComment:assetComment, assetCommentList:assetCommentList,
			  appMoveEvent:appMoveEvent, moveEventList:moveEventList, appMoveEvent:appMoveEventlist, project:project,
			  dependencyBundleNumber:AssetDependencyBundle.findByAsset(applicationInstance)?.dependencyBundle]
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
			def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntity])
			def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntity])
			def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
			def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
			def moveEvent = MoveEvent.findAllByProject(project,[sort:'name'])
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
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
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
						session.APP?.JQ_FILTERS = params
						redirect( action:list)
				}
			}
		}
		else {
			flash.message = "Application not created"
			applicationInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list)
		}
	}
	def delete = {
		def application = Application.get( params.id)
		if(application) {
				assetEntityService.deleteAsset( application )
				// deleting all appmoveEvent associated records .
				def appMove = AppMoveEvent.findAllByApplication( application );
				AppMoveEvent.withNewSession{appMove*.delete()}
				application.delete();
			
			flash.message = "Application ${application.assetName} deleted"
			if(params.dstPath =='planningConsole'){
				forward( controller:'assetEntity',action:'getLists', params:[entity: 'Apps',dependencyBundle:session.getAttribute("dependencyBundle")])
			}else{
				redirect( action:list )
			}
		}
		else {
			flash.message = "Application not found with id ${params.id}"	
			redirect( action:list )
		}		
	}
	/*
	 * Delete multiple Application 
	 */
	def deleteBulkAsset={
		def assetList = params.id.split(",")
		def assetNames = []
		assetList.each{ assetId->
		    def application = Application.get( assetId )
			if( application ) {
				assetNames.add(application.assetName)
				assetEntityService.deleteAsset( application )
				
				// deleting all appmoveEvent associated records .
				def appMove = AppMoveEvent.findAllByApplication( application );
				AppMoveEvent.withNewSession{appMove*.delete()}
				
				application.delete();
			}
		}	
		String names = assetNames.toString().replace('[','').replace(']','')
		
	  render "Aplication $names Deleted."
	}	
}
