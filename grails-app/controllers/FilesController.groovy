import com.tds.asset.AssetOptions
import com.tds.asset.Files
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import UserPreferenceEnum as PREF

class FilesController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def assetEntityService
	def controllerService
	def projectService
	def securityService
	def storageService
	def taskService
	def userPreferenceService

	def jdbcTemplate
	def dataSource

	def index() {
		redirect action:'list', params:params
	}

	def list() {
		def filters = session.FILES?.JQ_FILTERS
		session.FILES?.JQ_FILTERS = []

		def project = controllerService.getProjectForPage( this )
		if (! project)
			return

		def fieldPrefs = assetEntityService.getExistingPref('Storage_Columns')

		Map model = [
			fileFormat: filters?.fileFormatFilter,
			fileName: filters?.assetNameFilter ?:'',
			filesPref: fieldPrefs,
			size: filters?.sizeFilter
		]

		model.putAll( assetEntityService.getDefaultModelForLists(AssetClass.STORAGE, 'Files', project, fieldPrefs, params, filters) )

		return model

	}

	/**
	 * This method is used by JQgrid to load assetList
	 */
	def listJson() {
		def sortIndex = params.sidx ?: 'assetName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def project = securityService.getUserCurrentProject()
		def moveBundleList
		session.FILES = [:]

		userPreferenceService.setPreference(PREF.ASSET_LIST_SIZE, "${maxRows}")
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
		def justPlanning = userPreferenceService.getPreference(PREF.ASSET_JUST_PLANNING)?:'true'
		//TODO:need to move the code to AssetEntityService
		def temp=""
		def joinQuery=""
		filePref.each{key,value->
			switch(value){
			case 'moveBundle':
				temp +="mb.name AS moveBundle,"
			break
			case ~/custom1|custom2|custom3|custom4|custom5|custom6|custom7|custom8|custom9|custom10|custom11|custom12|custom13|custom14|custom15|custom16|custom17|custom18|custom19|custom20|custom21|custom22|custom23|custom24|custom25|custom26|custom27|custom28|custom29|custom30|custom31|custom32|custom33|custom34|custom35|custom36|custom37|custom38|custom39|custom40|custom41|custom42|custom43|custom44|custom45|custom46|custom47|custom48|custom49|custom50|custom51|custom52|custom53|custom54|custom55|custom56|custom57|custom58|custom59|custom60|custom61|custom62|custom63|custom64|custom65|custom66|custom67|custom68|custom69|custom70|custom71|custom72|custom73|custom74|custom75|custom76|custom77|custom78|custom79|custom80|custom81|custom82|custom83|custom84|custom85|custom86|custom87|custom88|custom89|custom90|custom91|custom92|custom93|custom94|custom95|custom96/:
				temp +="ae.${value} AS ${value},"
			break
			case 'fileFormat':
				temp+="f.file_format AS fileFormat,"
			break
			case 'lastUpdated':
				temp +="ee.last_updated AS ${value},"
				joinQuery +="\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n"
			break
			case 'modifiedBy':
				temp +="CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS modifiedBy,"
				joinQuery +="\n LEFT OUTER JOIN person p ON p.person_id=ae.modified_by \n"
			break
			case ~/validation|planStatus/:
			break
			default:
				temp +="ae.${WebUtil.splitCamelCase(value)} AS ${value},"
			}
		}
		def query = new StringBuffer("""SELECT * FROM ( SELECT f.files_id AS fileId, ae.asset_name AS assetName,ae.asset_type AS assetType,
										 me.move_event_id AS event,
										 IF(ac_task.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus, IF(ac_comment.comment_type IS NULL, 'noComments','comments') AS commentsStatus, """)

		if(temp){
			query.append(temp)
		}
		/*COUNT(DISTINCT adr.asset_dependency_id)+COUNT(DISTINCT adr2.asset_dependency_id) AS depResolve, adb.dependency_bundle AS depNumber,
			COUNT(DISTINCT adc.asset_dependency_id)+COUNT(DISTINCT adc2.asset_dependency_id) AS depConflicts */
		query.append(""" ae.validation AS validation,ae.plan_status AS planStatus
				FROM files f
				LEFT OUTER JOIN asset_entity ae ON f.files_id=ae.asset_entity_id
				LEFT OUTER JOIN asset_comment ac_task ON ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue'
				LEFT OUTER JOIN asset_comment ac_comment ON ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment'
				LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id """)
		if(joinQuery)
			query.append(joinQuery)

		query.append("""\n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
				WHERE ae.project_id = ${project.id} """)

		if(justPlanning=='true')
			query.append(" AND mb.use_for_planning=${justPlanning} ")

		if(params.event && params.event.isNumber() && moveBundleList)
			query.append( " AND ae.move_bundle_id IN (${WebUtil.listAsMultiValueString(moveBundleList.id)})" )

		if(params.unassigned){
			def unasgnMB = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.moveEvent IS NULL \
				AND mb.useForPlanning = :useForPlanning AND mb.project = :project ", [useForPlanning:true, project:project])

			if(unasgnMB){
				def unasgnmbId = WebUtil.listAsMultiValueString(unasgnMB?.id)
				query.append( " AND (ae.move_bundle_id IN (${unasgnmbId}) OR ae.move_bundle_id IS NULL)" )
			}
		}

		query.append(" GROUP BY files_id) AS files ")

		/*LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id=ae.asset_entity_id
			LEFT OUTER JOIN asset_dependency adr ON ae.asset_entity_id = adr.asset_id AND adr.status IN (${unknownQuestioned})
			LEFT OUTER JOIN asset_dependency adr2 ON ae.asset_entity_id = adr2.dependent_id AND adr2.status IN (${unknownQuestioned})
			LEFT OUTER JOIN asset_dependency adc ON ae.asset_entity_id = adc.asset_id AND adc.status IN (${validUnkownQuestioned})
				AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.dependent_id) != mb.move_bundle_id
			LEFT OUTER JOIN asset_dependency adc2 ON ae.asset_entity_id = adc2.dependent_id AND adc2.status IN (${validUnkownQuestioned})
				AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.asset_id) != mb.move_bundle_id */
		def whereConditions = []
		def queryParams = [:]
		filterParams.each {key, val ->
			if( val && val.trim().size()){
				whereConditions << SqlUtil.parseParameter(key, val, queryParams, Files)
			}
		}

		if(whereConditions.size()){
			query.append(" WHERE files.${whereConditions.join(" AND files.")}")
		}

		if (params.moveBundleId) {
			if (params.moveBundleId!='unAssigned') {
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				query.append(" WHERE files.moveBundle  = '${bundleName}' ")
			} else {
				query.append(" WHERE files.moveBundle IS NULL ")
			}
		}
		if ( params.toValidate) {
			query.append(" WHERE files.validation='Discovery'")
		}
		if (params.plannedStatus) {
			query.append(" WHERE files.planStatus='${params.plannedStatus}'")
		}
		query.append(" ORDER BY ${sortIndex} ${sortOrder}")

		def filesList = []

		if(queryParams.size()){
			def namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource)
			filesList = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		}else{
			filesList = jdbcTemplate.queryForList(query.toString())
		}

		def totalRows = filesList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0)
			filesList = filesList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			filesList = []

		def results = filesList?.collect {
			def commentType = it.commentType
			[ cell: [
			'',it.assetName, (it[filePref["1"]] ?: ''), (it[filePref["2"]] ?: ''), (it[filePref["3"]] ?: ''), (it[filePref["4"]] ?: ''), (it[filePref["5"]] ?: ''),
					/*it.depNumber, it.depResolve==0?'':it.depResolve, it.depConflicts==0?'':it.depConflicts,*/
					it.tasksStatus, it.assetType, it.commentsStatus], id: it.fileId, escapedName:assetEntityService.getEscapedName(it)
			]}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}

	def create() {
		// TODO : JPM 10/2014 : refactor create to get model from service layer
		def fileInstance = new Files(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def environmentOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		def project = securityService.getUserCurrentProject()
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		//fieldImportance for Discovery by default
		def configMap = assetEntityService.getConfig('Files','Discovery')
		def highlightMap = assetEntityService.getHighlightedInfo('Files', fileInstance, configMap)

		[fileInstance:fileInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
			planStatusOptions:planStatusOptions?.value, projectId:project.id, project:project,
			planStatusOptions:planStatusOptions.value, config:configMap.config, customs:configMap.customs,
			environmentOptions:environmentOptions?.value, highlightMap:highlightMap]
	}

	def show() {

		def project = controllerService.getProjectForPage( this )
		if (! project)
			return

		def id = params.id

		def storage = controllerService.getAssetForPage(this, project, AssetClass.STORAGE, id)

		if (!storage) {
			render "Storage asset was not found with id $id".toString()
		} else {
			def model = storageService.getModelForShow(project, storage, params)

			return model
		}
	}

	def edit() {
		def project = controllerService.getProjectForPage( this )
		if (! project)
			return

		def fileInstance = controllerService.getAssetForPage(this, project, AssetClass.STORAGE, params.id)
		if (!fileInstance) {
			render '<span class="error">Unable to find file to edit</span>'
			return
		}

		def model = [
			fileInstance:fileInstance
		]

		model.putAll( assetEntityService.getDefaultModelForEdits('Files', project, fileInstance, params) )
		// model.each { k,v -> log.debug "edit() $k=$v"}
		return model
	}

	def save() {
		controllerService.saveUpdateAssetHandler(this, session, storageService, AssetClass.STORAGE, params)
		session.FILES?.JQ_FILTERS = params
	}

	def update() {
		controllerService.saveUpdateAssetHandler(this, session, storageService, AssetClass.STORAGE, params)
		session.FILES?.JQ_FILTERS = params

		/*

			session.setAttribute("USE_FILTERS","true")

			switch (params.redirectTo) {
				case "room":
					redirect( controller:'room',action:"list" )
					break;
				case "rack":
					redirect( controller:'rackLayouts',action:'create' )
					break;
				case "console":
					redirect( controller:'assetEntity', action:"dashboardView", params:[showAll:'show'])
					break;
				case "assetEntity":
					redirect( controller:'assetEntity', action:"list")
					break;
				case "database":
					redirect( controller:'database', action:"list")
					break;
				case "application":
					redirect( controller:'application', action:"list")
					break;
				case "listComment":
					redirect( controller:'assetEntity', action:'listComment' , params:[projectId: project.id])
					break;
				case "listTask":
					render "Storage ${filesInstance.assetName} updated."
					break;
				case "dependencyConsole":
					forward( controller:'assetEntity',action:'retrieveLists', params:[entity: params.tabType,dependencyBundle:session.getAttribute("dependencyBundle"),labelsList:'apps'])
					break;
				default:
					session.FILES?.JQ_FILTERS = params
					redirect( action:"list")
			}
		*/

	}

	def delete() {
		def files = Files.get( params.id )
		if ( files ) {
			def assetName = files.assetName
			assetEntityService.deleteAsset( files )
			files.delete()

			flash.message = "Storage ${assetName} deleted"
			if (params.dstPath =='dependencyConsole') {
				forward( controller:'assetEntity',action:'retrieveLists', params:[entity: 'files',dependencyBundle:session.getAttribute("dependencyBundle")])
			} else {
				redirect( action:"list" )
			}
		}
		else {
			flash.message = "Storage not found with id ${params.id}"
			redirect( action:"list" )
		}

	}

	def deleteBulkAsset() {
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
