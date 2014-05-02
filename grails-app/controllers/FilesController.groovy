import grails.converters.JSON

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
import com.tdsops.tm.enums.domain.SizeScale;
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.WebUtil
import com.tdsops.tm.enums.domain.AssetDependencyStatus

class FilesController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def assetEntityService
	def taskService
	def securityService
	def userPreferenceService
	def projectService
	def jdbcTemplate
	def index ={
	     redirect(action : list)
		 	
	}
	def list={
		def filters = session.FILES?.JQ_FILTERS
		session.FILES?.JQ_FILTERS = []
		def project = securityService.getUserCurrentProject()
		def entities = assetEntityService.entityInfo( project )
		def sizePref = userPreferenceService.getPreference("assetListSize")?: '25'
		
		def moveEvent = null
		if (params.moveEvent && params.moveEvent.isNumber()) {
			log.info "it's good - ${params.moveEvent}"
			moveEvent = MoveEvent.findByProjectAndId( project, params.moveEvent )
		}
		
		def filesPref= assetEntityService.getExistingPref('Storage_Columns')
		def attributes = projectService.getAttributes('Files')
		def projectCustoms = project.customFieldsShown+1
		def nonCustomList = project.customFieldsShown!=64 ? (projectCustoms..Project.CUSTOM_FIELD_COUNT).collect{"custom"+it} : []
		// Remove the non project specific attributes and sort them by attributeCode
		def filesAttributes = attributes.findAll{it.attributeCode !='assetName' && !(it.attributeCode in nonCustomList)}
		
		// Used to display column names in jqgrid dynamically
		def modelPref = [:]
		filesPref.each{key,value->
			modelPref << [(key): assetEntityService.getAttributeFrontendLabel(value,attributes.find{it.attributeCode==value}?.frontendLabel)]
		}
		/* Asset Entity attributes for Filters*/
		def attributesList= (filesAttributes).collect{ attribute ->
			[attributeCode: attribute.attributeCode, frontendLabel:assetEntityService.getAttributeFrontendLabel(attribute.attributeCode, attribute.frontendLabel)]
		}
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:"name"])
		def hasPerm = RolePermissions.hasPermission("AssetEdit")
		def fixedFilter = false
		if(params.filter)
			fixedFilter = true
		
		return [assetDependency: new AssetDependency(), servers : entities.servers, applications : entities.applications, dbs : entities.dbs, networks : entities.networks ,
			files : entities.files, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus,
			event:params.moveEvent, moveEvent:moveEvent, filter:params.filter, plannedStatus:params.plannedStatus, validation:params.validation,
			staffRoles:taskService.getRolesForStaff(), moveBundleId:params.moveBundleId, fileName:filters?.assetNameFilter ?:'', 
			fileFormat:filters?.fileFormatFilter, size:filters?.sizeFilter,toValidate:params.toValidate,
			moveBundle:filters?.moveBundleFilter ?:'', planStatus:filters?.planStatusFilter ?:'', sizePref:sizePref, moveBundleList:moveBundleList,
			attributesList:attributesList, filesPref:filesPref, modelPref:modelPref, justPlanning:userPreferenceService.getPreference("assetJustPlanning")?:'true', 
			hasPerm:hasPerm,fixedFilter:fixedFilter]
		
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
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useForPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project,true)
		}
		
		//def unknownQuestioned = "'${AssetDependencyStatus.UNKNOWN}','${AssetDependencyStatus.QUESTIONED}'"
		//def validUnkownQuestioned = "'${AssetDependencyStatus.VALIDATED}'," + unknownQuestioned
		
		def filterParams = ['assetName':params.assetName,'depNumber':params.depNumber,'depResolve':params.depResolve,'depConflicts':params.depConflicts,'event':params.event]
		def filePref= assetEntityService.getExistingPref('Storage_Columns')
		def attributes = projectService.getAttributes('Files')
		def filePrefVal = filePref.collect{it.value}
		attributes.each{ attribute ->
			if(attribute.attributeCode in filePrefVal)
				filterParams << [ (attribute.attributeCode): params[(attribute.attributeCode)]]
		}
		def initialFilter = params.initialFilter in [true,false] ? params.initialFilter : false
		def justPlanning = userPreferenceService.getPreference("assetJustPlanning")?:'true'
		//TODO:need to move the code to AssetEntityService 
		def temp=""
		def joinQuery=""
		filePref.each{key,value->
			switch(value){
			case 'moveBundle':
				temp +="mb.name AS moveBundle,"
			break;
			case ~/custom1|custom2|custom3|custom4|custom5|custom6|custom7|custom8|custom9|custom10|custom11|custom12|custom13|custom14|custom15|custom16|custom17|custom18|custom19|custom20|custom21|custom22|custom23|custom24|custom25|custom26|custom27|custom28|custom29|custom30|custom31|custom32|custom33|custom34|custom35|custom36|custom37|custom38|custom39|custom40|custom41|custom42|custom43|custom44|custom45|custom46|custom47|custom48|custom49|custom50|custom51|custom52|custom53|custom54|custom55|custom56|custom57|custom58|custom59|custom60|custom61|custom62|custom63|custom64/:
				temp +="ae.${value} AS ${value},"
			break;
			case 'fileFormat':
				temp+="f.file_format AS fileFormat,"
			break;
			case 'lastUpdated':
				temp +="ee.last_updated AS ${value},"
				joinQuery +="\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n"
			break;
			case 'modifiedBy':
				temp +="CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS modifiedBy,"
				joinQuery +="\n LEFT OUTER JOIN person p ON p.person_id=ae.modified_by \n"
			break;
			case ~/validation|planStatus/:
			break;
			default:
				temp +="ae.${WebUtil.splitCamelCase(value)} AS ${value},"
			}
		}
		def query = new StringBuffer("""SELECT * FROM ( SELECT f.files_id AS fileId, ae.asset_name AS assetName,ae.asset_type AS assetType,
										 me.move_event_id AS event,ac.status AS commentStatus, """)
		
		if(temp){
			query.append(temp)
		}
		/*COUNT(DISTINCT adr.asset_dependency_id)+COUNT(DISTINCT adr2.asset_dependency_id) AS depResolve, adb.dependency_bundle AS depNumber,
			COUNT(DISTINCT adc.asset_dependency_id)+COUNT(DISTINCT adc2.asset_dependency_id) AS depConflicts */
		query.append("""  ac.comment_type AS commentType,ae.validation AS validation,ae.plan_status AS planStatus
				FROM files f 
				LEFT OUTER JOIN asset_entity ae ON f.files_id=ae.asset_entity_id
				LEFT OUTER JOIN asset_comment ac ON ac.asset_entity_id=ae.asset_entity_id
				LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id """)
		if(joinQuery)
			query.append(joinQuery)
		
		query.append("""\n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id 
				WHERE ae.project_id = ${project.id} """)
		
		if(justPlanning=='true')
			query.append(" AND mb.use_for_planning=${justPlanning} ")
			
		query.append(" GROUP BY files_id ORDER BY ${sortIndex} ${sortOrder}) AS files ")
		
		/*LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id=ae.asset_entity_id 
			LEFT OUTER JOIN asset_dependency adr ON ae.asset_entity_id = adr.asset_id AND adr.status IN (${unknownQuestioned}) 
			LEFT OUTER JOIN asset_dependency adr2 ON ae.asset_entity_id = adr2.dependent_id AND adr2.status IN (${unknownQuestioned}) 
			LEFT OUTER JOIN asset_dependency adc ON ae.asset_entity_id = adc.asset_id AND adc.status IN (${validUnkownQuestioned}) 
				AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.dependent_id) != mb.move_bundle_id 
			LEFT OUTER JOIN asset_dependency adc2 ON ae.asset_entity_id = adc2.dependent_id AND adc2.status IN (${validUnkownQuestioned}) 
				AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.asset_id) != mb.move_bundle_id */
		def firstWhere = true
		filterParams.each {
			if( it.getValue() )
				if (firstWhere) {
					// single quotes are stripped from the filter to prevent SQL injection
					query.append(" WHERE files.${it.getKey()} LIKE '%${it.getValue().replaceAll("'", "")}%'")
					firstWhere = false
				} else {
					query.append(" AND files.${it.getKey()} LIKE '%${it.getValue().replaceAll("'", "")}%'")
				}
		}
		
		if(params.moveBundleId){
			if(params.moveBundleId!='unAssigned'){
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				query.append(" WHERE files.moveBundle  = '${bundleName}' ")
			}else{
				query.append(" WHERE files.moveBundle IS NULL ")
			}
		}
		if( params.toValidate){
			query.append(" WHERE files.validation='Discovery'")
		}
		if(params.plannedStatus){
			query.append(" WHERE files.planStatus='${params.plannedStatus}'")
		}
		def filesList = jdbcTemplate.queryForList(query.toString())
		
		def totalRows = filesList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0)
			filesList = filesList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			filesList = []

		def results = filesList?.collect {
			def commentType = it.commentType
			[ cell: ['',it.assetName, (it[filePref["1"]] ?: ''), it[filePref["2"]], it[filePref["3"]], it[filePref["4"]], 
					/*it.depNumber, it.depResolve==0?'':it.depResolve, it.depConflicts==0?'':it.depConflicts,*/
					(it.commentStatus!='Completed' && commentType=='issue')?('issue'):(commentType?:'blank'),
					it.assetType], id: it.fileId,
			]}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	def create ={
		def fileInstance = new Files(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def environmentOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		def project = securityService.getUserCurrentProject()
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		//fieldImportance for Discovery by default
		def configMap = assetEntityService.getConfig('Files','Discovery')
		
		[fileInstance:fileInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:project.id, project:project,
					planStatusOptions:planStatusOptions.value, config:configMap.config, customs:configMap.customs,
					environmentOptions:environmentOptions?.value]
	}
	def save = {
				params.assetType = "Files"
				def filesInstance = new Files(params)
				def project = securityService.getUserCurrentProject()
				if(!filesInstance.hasErrors() && filesInstance.save()) {
					flash.message = "Storage ${filesInstance.assetName} created"
					def loginUser = securityService.getUserLogin()
					
					def errors = assetEntityService.createOrUpdateAssetEntityDependencies(params, filesInstance, loginUser, project)
					flash.message += "</br>"+errors 
					if(params.showView == 'showView'){
						forward(action:'show', params:[id: filesInstance.id, errors:errors])
					}else if(params.showView == 'closeView'){
						render flash.message
					}else{
				        session.FILES?.JQ_FILTERS = params
						redirect( action:list)
					}
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
			   config:configMap.config, customs:configMap.customs, errors:params.errors]
		}
	}
	def edit ={
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def environmentOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])

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
						dependencyStatus:dependencyStatus,servers:servers, config:configMap.config, customs:configMap.customs,
						environmentOptions:environmentOptions?.value]
		}
		
	}
	def update ={
		def attribute = session.getAttribute('filterAttr')
		def filterAttr = session.getAttribute('filterAttributes')
		session.setAttribute("USE_FILTERS","true")
		def loginUser = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()
		def filesInstance = Files.get(params.id)
		params.assetType = "Files"
		filesInstance.properties = params
		if(!filesInstance.hasErrors() && filesInstance.save(flush:true)) {
			flash.message = "Storage ${filesInstance.assetName} Updated"
			def errors = assetEntityService.createOrUpdateAssetEntityDependencies(params, filesInstance, loginUser, project)
			flash.message +="</br>"+errors 
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
					case "application":
						redirect( controller:'application', action:list)
						break;
					case "listComment":
						redirect( controller:'assetEntity', action:'listComment' , params:[projectId: project.id])
						break;
					case "listTask":
						render "Storage ${filesInstance.assetName} updated."
						break;
				    case "dependencyConsole":
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
			if(params.dstPath =='dependencyConsole'){
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
