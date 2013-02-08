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
	
	def index = {
		redirect(action:list,params:params)
	}

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def  list ={
		def project = securityService.getUserCurrentProject()
		
		/*def filterAttributes = [tag_f_assetName:params.tag_f_assetName,tag_f_appOwner:params.tag_f_appOwner,tag_f_appSme:params.tag_f_appSme,tag_f_planStatus:params.tag_f_planStatus,tag_f_depUp:params.tag_f_depUp,tag_f_depDown:params.tag_f_depDown]
		session.setAttribute('filterAttributes', filterAttributes)
		def projectId =  getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
		String moveBundle = moveBundleList.id
		moveBundle = moveBundle.replace("[","('").replace(",","','").replace("]","')")
		def workFlow = project.workflowCode
		def appEntityList
		if(params.latency=='likely'){
		
			appEntityList= Application.findAllByLatencyAndMoveBundleInList('N',moveBundleList)
			
		}else if(params.latency=='UnKnown'){
		
			appEntityList= Application.findAll("from Application where project = $projectId  and (latency is null or latency = '') and moveBundle in $moveBundle")
			
		}else if(params.latency=='UnLikely'){
		
			appEntityList= Application.findAllByLatencyAndMoveBundleInList('Y',moveBundleList)
			
		}else if(params.moveEvent=='unAssigned'){
		
		    appEntityList= Application.findAll("from Application where project = $projectId and assetType=? and moveBundle in $moveBundle and (planStatus is null or planStatus in ('Unassigned',''))",['Application'])
			
		}else if(params.moveEvent && params.moveEvent!='unAssigned'){
		
		    def moveEvent = MoveEvent.get(params.moveEvent)
			def moveBundles = moveEvent.moveBundles
			def bundles = moveBundles.findAll {it.useOfPlanning == true}
		    appEntityList= Application.findAllByMoveBundleInListAndAssetType(bundles,"Application")
			
		}else if(params.filter=='appToValidate'){
		
		    appEntityList= Application.findAll("from Application  ap where ap.assetType ='Application' and ap.validation = 'Discovery' and ap.project.id = ${projectId}  and (ap.moveBundle.id in ${moveBundle} or ap.moveBundle.id is null)")
			
		}else if(params.filter=='applicationCount'){
		
		    appEntityList = Application.findAll("from Application where  project.id = $projectId and assetType = 'Application' and (moveBundle in ${moveBundle} or moveBundle is null)")
			
		}else{
		
		    appEntityList = Application.findAllByProject(project)
			
		}
		def appBeanList = new ArrayList()
		appEntityList.each { appEntity->
			AssetEntityBean appBeanInstance = new AssetEntityBean();
			appBeanInstance.setId(appEntity.id)
			appBeanInstance.setAssetName(appEntity.assetName)
			appBeanInstance.setAssetType(appEntity.assetType)
			appBeanInstance.setAppOwner(appEntity.appOwner)
			appBeanInstance.setAppSme(appEntity.sme)
			appBeanInstance.setMoveBundle(appEntity.moveBundle?.name)
			appBeanInstance.setPlanStatus(appEntity.planStatus)
			appBeanInstance.setValidation(appEntity.validation)
			appBeanInstance.setDepUp(AssetDependency.countByDependentAndStatusNotEqual(appEntity, "Validated"))
			appBeanInstance.setDepDown(AssetDependency.countByAssetAndStatusNotEqual(appEntity, "Validated"))
			appBeanInstance.setDependencyBundleNumber(AssetDependencyBundle.findByAsset(appEntity)?.dependencyBundle)
			
			if(AssetComment.find("from AssetComment where assetEntity = ${appEntity?.id} and commentType = ? and isResolved = ?",['issue',0])){
				appBeanInstance.setCommentType("issue")
			} else if(AssetComment.find('from AssetComment where assetEntity = '+ appEntity?.id)){
				appBeanInstance.setCommentType("comment")
			} else {
				appBeanInstance.setCommentType("blank")
			}
			
			appBeanList.add(appBeanInstance)
		}*/
		def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$project.id order by assetName asc")
		def applications = Application.findAll('from Application where assetType = ? and project =? order by assetName asc',['Application', project])
		def dbs = Database.findAll('from Database where assetType = ? and project =? order by assetName asc',['Database', project])
		def files = Files.findAll('from Files where assetType = ? and project =? order by assetName asc',['Files', project])
		
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		
		/*try{
			tableFacade.items = appBeanList
			Limit limit = tableFacade.limit
			if(limit.isExported()){
				tableFacade.setExportTypes(response,limit.getExportType())
				tableFacade.setColumnProperties("id","application","appOwner","appSme","movebundle","planStatus")
				tableFacade.render()
			} else {*/
		return [projectId: project.id, assetDependency: new AssetDependency(),
			servers : servers, applications : applications, dbs : dbs, files : files,dependencyType:dependencyType,dependencyStatus:dependencyStatus,
		    staffRoles:taskService.getRolesForStaff()]
			/*}
		}catch(Exception e){
			return [assetEntityList : null, projectId: projectId , servers : servers, applications : applications, dbs : dbs, files : files]
		}*/
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
		def moveBundleList

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
			[ applicationInstance : applicationInstance,supportAssets: supportAssets, dependentAssets:dependentAssets, redirectTo : params.redirectTo, assetComment:assetComment, assetCommentList:assetCommentList,
			  appMoveEvent:appMoveEvent,moveEventList:moveEventList,appMoveEvent:appMoveEventlist,dependencyBundleNumber:AssetDependencyBundle.findByAsset(applicationInstance)?.dependencyBundle]
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
						redirect( action:list,params:[tag_f_assetName:filterAttr?.tag_f_assetName, tag_f_appOwner:filterAttr?.tag_f_appOwner, tag_f_appSme:filterAttr?.tag_f_appSme, tag_f_planStatus:filterAttr?.tag_f_planStatus, tag_f_depUp:filterAttr?.tag_f_depUp, tag_f_depDown:filterAttr?.tag_f_depDown])
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
		def applicationInstance = Application.get( params.id )
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
			AssetDependency.executeUpdate("delete AssetDependency where asset = ? or dependent = ? ",[applicationInstance, applicationInstance])
			
			AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset = ${applicationInstance.id}")
			def appMoveInstance = AppMoveEvent.findAllByApplication(applicationInstance);
			appMoveInstance.each{
			         it.delete(flush:true)
			}
			applicationInstance.delete();
			assetEntityInstance.delete();
			
			flash.message = "Application ${assetName} deleted"
			if(params.dstPath =='planningConsole'){
				forward( controller:'assetEntity',action:'getLists', params:[entity: 'Apps',dependencyBundle:session.getAttribute("dependencyBundle")])
			}else{
				redirect( action:list )
			}
		}
		else {
			flash.message = "Application not found with id ${params.id}"			
		}		
	}
	def deleteBulkAsset={
		def assetArray = params.id
		def assetList
		if(assetArray.class.toString() == "class java.lang.String"){
		  assetList = assetArray.split(",")
		}else{
		  assetList = assetArray
		}
		def assetNames = []
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
			for(int i=0 ; i<assetList.size();i++){
			    def assetEntityInstance = AssetEntity.get( assetList[i] )
			    def applicationInstance = Application.get( assetList[i] )
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
					AssetDependency.executeUpdate("delete AssetDependency where asset = ? or dependent = ? ",[applicationInstance, applicationInstance])
					
					AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset = ${applicationInstance.id}")
					def appMoveInstance = AppMoveEvent.findAllByApplication(applicationInstance);
					appMoveInstance.each{
					         it.delete(flush:true)
					}
					applicationInstance.delete();
					assetEntityInstance.delete();
				}
						String names = assetNames.toString().replace('[','').replace(']','')
						flash.message = "Application ${names} deleted"
			}
	  render "success"
	}	
}
