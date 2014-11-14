import grails.converters.JSON

import java.text.SimpleDateFormat

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.WebUtil


class ApplicationController {

	def applicationService
	def assetEntityService
	def controllerService
	def partyRelationshipService
	def projectService
	def securityService
	def taskService
	def userPreferenceService

	def jdbcTemplate
	
	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def index = {
		redirect action:'list', params:params
	}

	def list = {
		def filters = session.APP?.JQ_FILTERS
		session.APP?.JQ_FILTERS = []

		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		// TODO - This should be replaced with the staffRoles which is in the defaultModel already
		def availabaleRoles = partyRelationshipService.getStaffingRoles()

		def companiesList = PartyGroup.findAll("from PartyGroup as p where partyType = 'COMPANY' order by p.name ")

		def fieldPrefs = assetEntityService.getExistingPref('App_Columns')
		
		Map model = [
			appName: filters?.assetNameFilter ?:'', 
			appPref: fieldPrefs, 
			appSme: filters?.appSmeFilter ?:'',
			availabaleRoles: availabaleRoles, 
			company: project.client, 
			latencys: params.latencys,
			partyGroupList: companiesList, 
			runbook: params.runbook,
			validationFilter: filters?.appValidationFilter ?:'' 
		]

		model.putAll( assetEntityService.getDefaultModelForLists(AssetClass.APPLICATION, 'Application', project, fieldPrefs, params, filters) )

		return model

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
										IF(ac_task.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus, IF(ac_comment.comment_type IS NULL, 'noComments','comments') AS commentsStatus,me.move_event_id AS event,""")
		if(customizeQuery.query){
			query.append(customizeQuery.query)
		}	
		
		query.append(""" ae.asset_type AS assetType,ae.validation AS validation,ae.plan_status AS planStatus,me.runbook_status AS runbookStatus
			FROM application a 
			LEFT OUTER JOIN asset_entity ae ON a.app_id=ae.asset_entity_id
			LEFT OUTER JOIN asset_comment ac_task ON ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue'
			LEFT OUTER JOIN asset_comment ac_comment ON ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment'
			""")
		if (customizeQuery.joinQuery) {
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
			
		if(params.event && params.event.isNumber() && moveBundleList)
			query.append( " AND ae.move_bundle_id IN (${WebUtil.listAsMultiValueString(moveBundleList.id)})" )
			
		if(params.unassigned){
			def unasgnMB = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.moveEvent IS NULL \
				AND mb.useForPlanning = :useForPlanning AND mb.project = :project ", [useForPlanning:true, project:project])
			
			if(unasgnMB){
				def unasgnmbId = WebUtil.listAsMultiValueString(unasgnMB?.id)
				query.append( " AND (ae.move_bundle_id IN (${unasgnmbId}) OR ae.move_bundle_id is null)" )
			}
		}

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
			[ cell: [
			'',it.assetName, (it[appPref["1"]] ?: ''), it[appPref["2"]], it[appPref["3"]], it[appPref["4"]], it[appPref["5"]],
			/*it.depNumber, it.depResolve==0?'':it.depResolve, it.depConflicts==0?'':it.depConflicts,*/
			it.tasksStatus, it.assetType, it.event, it.commentsStatus
		], id: it.appId, escapedName:assetEntityService.getEscapedName(it)]}
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
		def applicationInstance = new Application()
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
		def highlightMap = assetEntityService.getHighlightedInfo('Application', applicationInstance, configMap)
		
		[applicationInstance:applicationInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
			planStatusOptions:planStatusOptions?.value, projectId:project.id, project:project,moveEventList:moveEventList,
			config:configMap.config, customs:configMap.customs, personList:personList, company:project.client, 
			availabaleRoles:availabaleRoles, environmentOptions:environmentOptions?.value, highlightMap:highlightMap]
	}

	def show = {
		def app
		def project = controllerService.getProjectForPage( this )
		if (project) {
			def assetId = params.id
			app = controllerService.getAssetForPage(this, project, AssetClass.APPLICATION, assetId)

		}

		if (!project || !app) {
			def errorMsg = flash.message
			flash.message=null
			errorMsg = errorMsg ?: "Application not found with id $assetId"
			log.debug "show() $errorMsg"
			render ServiceResults.errors(errorMsg) as JSON
		} else {
			def model = applicationService.getModelForShow(project, app, params)
			// model.each { n,v -> println "$n:\t$v"}
			return model
		}
	}

	/**
	 * This action is used to redirect to edit view .
	 * @param : redirectTo
	 * @return : render to edit page based on condition as if 'redirectTo' is roomAudit then redirecting
	 * to auditEdit view
	 */
	def edit = {
		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		def applicationInstance = controllerService.getAssetForPage(this, project, AssetClass.APPLICATION, params.id)

		if(!applicationInstance) {
			flash.message = "Application not found with id ${params.id}"
			redirect(action:list)
			return
		}

		def availabaleRoles = partyRelationshipService.getStaffingRoles()
		def moveEvent = assetEntityService.getMoveEvents(project)

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

		def model = [
			applicationInstance:applicationInstance, 
			availabaleRoles:availabaleRoles, 
			moveEvent:moveEvent,
			personList:personList
		]

		model.putAll( assetEntityService.getDefaultModelForEdits('Application', project, applicationInstance, params) )

		return model		
	}

	def save = {
		controllerService.saveUpdateAssetHandler(this, session, applicationService, AssetClass.APPLICATION, params)
		session.APP?.JQ_FILTERS = params
	}

	def update = {
		controllerService.saveUpdateAssetHandler(this, session, applicationService, AssetClass.APPLICATION, params)
		session.APP?.JQ_FILTERS = params
		session.setAttribute("USE_FILTERS","true")

		/*

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
					
				}
			}
		*/
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