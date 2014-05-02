import grails.converters.JSON

import java.text.SimpleDateFormat

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.AssetType
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.AssetDependencyStatus

class ApplicationController {
	def partyRelationshipService
	def assetEntityService
	def taskService
	def securityService
	def userPreferenceService
	def jdbcTemplate
	def projectService
	def index = {
		redirect(action:list,params:params)
	}

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {
		def filters = session.APP?.JQ_FILTERS
		session.APP?.JQ_FILTERS = []
		def project = securityService.getUserCurrentProject()
		
		def entities = assetEntityService.entityInfo( project )
		def sizePref = userPreferenceService.getPreference("assetListSize")?: '25'
		
		
		def companiesList = PartyGroup.findAll( "from PartyGroup as p where partyType = 'COMPANY' order by p.name " )
		def availabaleRoles = partyRelationshipService.getStaffingRoles()
		def company = project.client
		
		def moveEvent = null
		if (params.moveEvent && params.moveEvent.isNumber()) {
			log.info "it's good - ${params.moveEvent}"
			moveEvent = MoveEvent.findByProjectAndId( project, params.moveEvent )
		}
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:"name"])
		def appPref= assetEntityService.getExistingPref('App_Columns')
		def attributes = projectService.getAttributes('Application')
		def projectCustoms = project.customFieldsShown+1
		def nonCustomList = project.customFieldsShown!=64 ? (projectCustoms..Project.CUSTOM_FIELD_COUNT).collect{"custom"+it} : []
		
		// Remove the non project specific attributes and sort them by attributeCode
		def appAttributes = attributes.findAll{it.attributeCode!="assetName" && !(it.attributeCode in nonCustomList)}

		// Used to display column names in jqgrid dynamically
		def modelPref = [:]
		appPref.each{key,value->
			modelPref << [(key): assetEntityService.getAttributeFrontendLabel(value,attributes.find{it.attributeCode==value}?.frontendLabel)]
		}
		/* Asset Entity attributes for Filters*/
		def attributesList= (appAttributes).collect{ attribute ->
			[attributeCode: attribute.attributeCode, frontendLabel:assetEntityService.getAttributeFrontendLabel(attribute.attributeCode, attribute.frontendLabel)]
		}
		def hasPerm = RolePermissions.hasPermission("AssetEdit")
		def fixedFilter = false
		if(params.filter)
			fixedFilter = true
		
		return [projectId: project.id, assetDependency: new AssetDependency(),
			servers: entities.servers, 
			applications: entities.applications, 
			dbs: entities.dbs, 
			files: entities.files, 
			networks: entities.networks, 
			dependencyType:entities.dependencyType, 
			dependencyStatus:entities.dependencyStatus, event:params.moveEvent, filter:params.filter, latencys:params.latencys,
		    staffRoles:taskService.getRolesForStaff(), plannedStatus:params.plannedStatus, appSme : filters?.appSmeFilter ?:'',runbook:params.runbook,
			validation:params.validation, moveBundleId:params.moveBundleId, appName:filters?.assetNameFilter ?:'', sizePref:sizePref,toValidate:params.toValidate, 
			validationFilter:filters?.appValidationFilter ?:'', moveBundle:filters?.moveBundleFilter ?:'', planStatus:filters?.planStatusFilter ?:'',
			partyGroupList:companiesList, availabaleRoles:availabaleRoles, company:company, moveEvent:moveEvent, moveBundleList:moveBundleList,
			attributesList:attributesList, appPref:appPref, modelPref:modelPref, justPlanning:userPreferenceService.getPreference("assetJustPlanning")?:'true',
			hasPerm:hasPerm, fixedFilter:fixedFilter]
	}
	/**
	 * This method is used by JQgrid to load appList 
	 */
	def listJson = {
		def sortIndex = params.sidx ?: 'assetName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows?:'25')?:25
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def project = securityService.getUserCurrentProject()
		
		def filterParams = ['assetName':params.assetName,'depNumber':params.depNumber,'depResolve':params.depResolve,'depConflicts':params.depConflicts,'event':params.event]
		def attributes = projectService.getAttributes('Application')
		
		def appPref= assetEntityService.getExistingPref('App_Columns')
		def appPrefVal = appPref.collect{it.value}
		attributes.each{ attribute ->
			if(attribute.attributeCode in appPrefVal)
				filterParams << [ (attribute.attributeCode): params[(attribute.attributeCode)]]
		}
		def initialFilter = params.initialFilter in [true,false] ? params.initialFilter : false
		
		def moveBundleList = []
		session.APP = [:]
		userPreferenceService.setPreference("assetListSize", "${maxRows}")
		if(params.event && params.event.isNumber()){
			def moveEvent = MoveEvent.read( params.event )
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useForPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project,true)
		}
		
		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%${params.moveBundle}%", project) : []
		
		//def unknownQuestioned = "'${AssetDependencyStatus.UNKNOWN}','${AssetDependencyStatus.QUESTIONED}'"
		//def validUnkownQuestioned = "'${AssetDependencyStatus.VALIDATED}'," + unknownQuestioned
		def justPlanning = userPreferenceService.getPreference("assetJustPlanning")?:'true'
		def customizeQuery = assetEntityService.getAppCustomQuery(appPref)
		def query = new StringBuffer("""SELECT * FROM ( SELECT a.app_id AS appId, ae.asset_name AS assetName,a.latency AS latency,
										ac.status AS commentStatus, ac.comment_type AS commentType,me.move_event_id AS event,""")
		if(customizeQuery.query){
			query.append(customizeQuery.query)
		}	
		
		query.append(""" ae.asset_type AS assetType,ae.validation AS validation,ae.plan_status AS planStatus,me.runbook_status AS runbookStatus
			FROM application a 
			LEFT OUTER JOIN asset_entity ae ON a.app_id=ae.asset_entity_id
			LEFT OUTER JOIN asset_comment ac ON ac.asset_entity_id=ae.asset_entity_id""")
		if(customizeQuery.joinQuery){
			query.append(customizeQuery.joinQuery)
		}
		//commented as per craig comments for performance issue
		/*COUNT(DISTINCT adr.asset_dependency_id)+COUNT(DISTINCT adr2.asset_dependency_id) AS depResolve,  adb.dependency_bundle AS depNumber,
		COUNT(DISTINCT adc.asset_dependency_id)+COUNT(DISTINCT adc2.asset_dependency_id) AS depConflicts */
		
		query.append("""\n LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
			LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id 
			WHERE ae.project_id = ${project.id} """)

		if (justPlanning=='true')
			query.append(" AND mb.use_for_planning=${justPlanning} ")

		query.append("GROUP BY app_id ORDER BY ${sortIndex} ${sortOrder} ) AS apps")
		
		/*LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id=ae.asset_entity_id 
		LEFT OUTER JOIN asset_dependency adr ON ae.asset_entity_id = adr.asset_id AND adr.status IN (${unknownQuestioned})
		LEFT OUTER JOIN asset_dependency adr2 ON ae.asset_entity_id = adr2.dependent_id AND adr2.status IN (${unknownQuestioned})
		LEFT OUTER JOIN asset_dependency adc ON ae.asset_entity_id = adc.asset_id AND adc.status IN (${validUnkownQuestioned})
			AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.dependent_id) != mb.move_bundle_id
		LEFT OUTER JOIN asset_dependency adc2 ON ae.asset_entity_id = adc2.dependent_id AND adc2.status IN (${validUnkownQuestioned})
			AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.asset_id) != mb.move_bundle_id*/
		
		// Handle the filtering by each column's text field
		def firstWhere = true
		filterParams.each {
			if( it.getValue() )
				if (firstWhere) {
					// single quotes are stripped from the filter to prevent SQL injection
					query.append(" WHERE apps.${it.getKey()} LIKE '%${it.getValue().replaceAll("'", "")}%'")
					firstWhere = false
				} else {
					query.append(" AND apps.${it.getKey()} LIKE '%${it.getValue().replaceAll("'", "")}%'")
				}
		}
		if(params.latencys){
			if(params.latencys!='unknown')
				query.append(" WHERE apps.latency = '${params.latencys.replaceAll("'", "")}' ")
			else
				query.append(" WHERE (apps.latency NOT IN ('Y','N') OR apps.latency IS NULL) ")	
		}
		if(params.moveBundleId){
			if(params.moveBundleId!='unAssigned'){
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				query.append(" WHERE apps.moveBundle  = '${bundleName}' ")
			}else{
				query.append(" WHERE apps.moveBundle IS NULL ")
			}
		}
		if( params.toValidate){
			query.append(" WHERE apps.validation='${params.toValidate}'")
		}
		if(params.plannedStatus){
			query.append(" WHERE apps.planStatus='${params.plannedStatus}'")
		}
		if(params.runbook){
			query.append( " Where apps.runbookStatus='Done' " )
		}
		log.info "query = ${query}"
		def appsList = jdbcTemplate.queryForList(query.toString())
		
		// Cut the list of selected applications down to only the rows that will be shown in the grid
		def totalRows = appsList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0)
			appsList = appsList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			appsList = []
			
		def results = appsList?.collect { 
			def commentType = it.commentType
			[ cell: [
			'',it.assetName, (it[appPref["1"]] ?: ''), it[appPref["2"]], it[appPref["3"]], it[appPref["4"]], 
			/*it.depNumber, it.depResolve==0?'':it.depResolve, it.depConflicts==0?'':it.depConflicts,*/
			(it.commentStatus!='Completed' && commentType=='issue')?('issue'):(commentType?:'blank'),	it.assetType, it.event
		], id: it.appId]}
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		
		render jsonData as JSON
	}
	/**
	 * used to set the Application custom columns pref as JSON
	 * @param columnValue
	 * @param from
	 * @render true
	 */
	def columnAssetPref={
		def column= params.columnValue
		def fromKey= params.from
		def existingColsMap = assetEntityService.getExistingPref(params.type)
		def key = existingColsMap.find{it.value==column}?.key
		if(key)
			existingColsMap["${key}"] = params.previousValue

		existingColsMap["${fromKey}"] = column
		userPreferenceService.setPreference( params.type, (existingColsMap as JSON).toString() )
		render true
	}
	def create = {
		def applicationInstance = new Application(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def project = securityService.getUserCurrentProject()
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def environmentOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		def moveEventList = MoveEvent.findAllByProject(project,[sort:'name'])
	
		def personList = partyRelationshipService.getCompanyStaff( project.client?.id )
		def availabaleRoles = partyRelationshipService.getStaffingRoles()
		
		//fieldImportance for Discovery by default
		def configMap = assetEntityService.getConfig('Application','Discovery')
		
		[applicationInstance:applicationInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
			planStatusOptions:planStatusOptions?.value, projectId:project.id, project:project,moveEventList:moveEventList,
			config:configMap.config, customs:configMap.customs, personList:personList, company:project.client, 
			availabaleRoles:availabaleRoles, environmentOptions:environmentOptions?.value]
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
			def loginUser = securityService.getUserLogin()
			def project = securityService.getUserCurrentProject()
			def errors = assetEntityService.createOrUpdateAssetEntityDependencies(params, applicationInstance, loginUser, project)
			flash.message += "</br>"+errors
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
			if(params.showView == 'showView'){
				forward(action:'show', params:[id: applicationInstance.id, errors:errors])
				
			}else if(params.showView == 'closeView'){
				render flash.message
			}else{
				session.APP?.JQ_FILTERS = params
				redirect( action:list)
			}
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
			def prefValue= userPreferenceService.getPreference("showAllAssetTasks") ?: 'FALSE'
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)	
			def appMoveEvent = AppMoveEvent.findAllByApplication(applicationInstance)
			def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
			def project = Project.read(projectId)
		    def moveEventList = MoveEvent.findAllByProject(project,[sort:'name'])
			def appMoveEventlist = AppMoveEvent.findAllByApplication(applicationInstance).value
			
			//field importance styling for respective validation.
			def validationType = assetEntity.validation
			def configMap = assetEntityService.getConfig('Application',validationType)
			
			def shutdownBy = assetEntity.shutdownBy  ? assetEntityService.resolveByName(assetEntity.shutdownBy) : ''
			def startupBy = assetEntity.startupBy  ? assetEntityService.resolveByName(assetEntity.startupBy) : ''
			def testingBy = assetEntity.testingBy  ? assetEntityService.resolveByName(assetEntity.testingBy) : ''
			
			
			
			[ applicationInstance : applicationInstance,supportAssets: supportAssets, dependentAssets:dependentAssets, 
			  redirectTo : params.redirectTo, assetComment:assetComment, assetCommentList:assetCommentList,
			  appMoveEvent:appMoveEvent, moveEventList:moveEventList, appMoveEvent:appMoveEventlist, project:project,
			  dependencyBundleNumber:AssetDependencyBundle.findByAsset(applicationInstance)?.dependencyBundle ,prefValue:prefValue, 
			  config:configMap.config, customs:configMap.customs, shutdownBy:shutdownBy, startupBy:startupBy, testingBy:testingBy,
			  errors:params.errors]
		}
	}
    
	def edit = {
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def environmentOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		def project = securityService.getUserCurrentProject()
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
			def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =${project.id} order by assetName asc")
			moveEvent.each{
			   def appMoveList = AppMoveEvent.findByApplicationAndMoveEvent(applicationInstance,it)
			   if(!appMoveList){
				   def appMoveInstance = new AppMoveEvent()
				   appMoveInstance.application = applicationInstance
				   appMoveInstance.moveEvent = it
				   appMoveInstance.save(flush:true)
			   }
			}
			def personList = partyRelationshipService.getCompanyStaff( project.client?.id )
			
			//fieldImportance Styling for default validation.
			def validationType = applicationInstance.validation
			def configMap = assetEntityService.getConfig('Application',validationType)
			def availabaleRoles = partyRelationshipService.getStaffingRoles()
			
			[applicationInstance:applicationInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList, project : project,
						planStatusOptions:planStatusOptions?.value, projectId:project.id, supportAssets: supportAssets,
						dependentAssets:dependentAssets, redirectTo : params.redirectTo,dependencyType:dependencyType, dependencyStatus:dependencyStatus,
						moveEvent:moveEvent,servers:servers, personList:personList, config:configMap.config, customs:configMap.customs, 
						availabaleRoles:availabaleRoles, environmentOptions:environmentOptions?.value]
		}

	}

	def update = {
		def attribute = session.getAttribute('filterAttr')
		def filterAttr = session.getAttribute('filterAttributes')
		session.setAttribute("USE_FILTERS","true")
		def formatter = new SimpleDateFormat("MM/dd/yyyy")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def project = securityService.getUserCurrentProject()
		def maintExpDate = params.maintExpDate
		if(maintExpDate){
			params.maintExpDate =  GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
		}
		def retireDate = params.retireDate
		if(retireDate){
			params.retireDate =  GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
		}
		def applicationInstance = Application.get(params.id)
		applicationInstance.sme = null
		applicationInstance.sme2 = null
		applicationInstance.appOwner = null
		applicationInstance.properties = params
		applicationInstance.shutdownFixed = params.shutdownFixed ?  1 : 0
		applicationInstance.startupFixed = params.startupFixed ?  1 : 0
		applicationInstance.testingFixed = params.testingFixed ?  1 : 0
		
		if(!applicationInstance.hasErrors() && applicationInstance.save(flush:true)) {
			flash.message = "Application ${applicationInstance.assetName} Updated"
			def loginUser = securityService.getUserLogin()
			def errors = assetEntityService.createOrUpdateAssetEntityDependencies(params, applicationInstance, loginUser, project)
			flash.message += "</br>"+errors
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
				forward(action:'show', params:[id: params.id, errors:errors])
				
			}else if(params.updateView == 'closeView'){
				render flash.message
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
						redirect( controller:'assetEntity', action:'listComment' , params:[projectId: project.id])
						break;
					case "listTask":
						render "Application ${applicationInstance.assetName} updated."
						break;
					case "dependencyConsole":
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
			if(params.dstPath =='dependencyConsole'){
				forward( controller:'assetEntity',action:'getLists', params:[entity: 'apps',dependencyBundle:session.getAttribute("dependencyBundle")])
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
	
	def customColumns={
		def columnName = params.column
		def removeCol = params.fromName
		if(columnName && removeCol){
			existingCol.each{
				println "it---->"+it
			}
		}
		def existingCol= "'Actions','Name', 'App Sme','Validation', 'Plan Status','Bundle','Dep # ','Dep to resolve','Dep Conflicts','id', 'commentType', 'Event'"
		render existingCol as JSON
	}
}