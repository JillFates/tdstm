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
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil

import org.apache.commons.lang.StringEscapeUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate


class DatabaseController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def assetEntityService
	def controllerService  
	def databaseService
	def taskService 
	def securityService
	def jdbcTemplate
	def userPreferenceService
	def projectService

	def dataSource

	def index() {
		redirect action:'list', params:params
	}
	
	def list() {
		def filters = session.DB?.JQ_FILTERS
		session.DB?.JQ_FILTERS = []

		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		def fieldPrefs = assetEntityService.getExistingPref('Database_Columns')
		
		Map model = [
			dbFormat: filters?.dbFormatFilter?:'',
			dbName: filters?.assetNameFilter ?:'',
			dbPref: fieldPrefs
		]

		model.putAll( assetEntityService.getDefaultModelForLists(AssetClass.DATABASE, 'Database', project, fieldPrefs, params, filters) )

		return model
	}
	
	/**
	 * This method is used by JQgrid to load assetList
	 */
	def listJson() {
		def sortIndex = params.sidx ?: 'assetName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows?:'25')?:25
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def project = securityService.getUserCurrentProject()
		def moveBundleList
		session.DB = [:]
		
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
		def dbPref= assetEntityService.getExistingPref('Database_Columns')
		def attributes = projectService.getAttributes('Database')
		def dbPrefVal = dbPref.collect{it.value}
		attributes.each{ attribute ->
			if(attribute.attributeCode in dbPrefVal)
				filterParams << [ (attribute.attributeCode): params[(attribute.attributeCode)]]
		}
		def initialFilter = params.initialFilter in [true,false] ? params.initialFilter : false
		def justPlanning = userPreferenceService.getPreference("assetJustPlanning")?:'true'
		//TODO:need to move the code to AssetEntityService 
		def temp=""
		def joinQuery =""
		dbPref.each{key,value->
			switch(value){
			case 'moveBundle':
				temp +="mb.name AS moveBundle,"
			break;
			case 'lastUpdated':
				temp +="ee.last_updated AS ${value},"
				joinQuery +="\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n"
			break;
			case 'modifiedBy':
				temp +="CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS modifiedBy,"
				joinQuery +="\n LEFT OUTER JOIN person p ON p.person_id=ae.modified_by \n"
			break;
			case ~/custom1|custom2|custom3|custom4|custom5|custom6|custom7|custom8|custom9|custom10|custom11|custom12|custom13|custom14|custom15|custom16|custom17|custom18|custom19|custom20|custom21|custom22|custom23|custom24|custom25|custom26|custom27|custom28|custom29|custom30|custom31|custom32|custom33|custom34|custom35|custom36|custom37|custom38|custom39|custom40|custom41|custom42|custom43|custom44|custom45|custom46|custom47|custom48|custom49|custom50|custom51|custom52|custom53|custom54|custom55|custom56|custom57|custom58|custom59|custom60|custom61|custom62|custom63|custom64|custom65|custom66|custom67|custom68|custom69|custom70|custom71|custom72|custom73|custom74|custom75|custom76|custom77|custom78|custom79|custom80|custom81|custom82|custom83|custom84|custom85|custom86|custom87|custom88|custom89|custom90|custom91|custom92|custom93|custom94|custom95|custom96/:
				temp +="ae.${value} AS ${value},"
			break;
			case 'dbFormat':
				temp+="d.db_format AS dbFormat,"
			break;
			case ~/validation|planStatus/:
			break;
			default:
				temp +="ae.${WebUtil.splitCamelCase(value)} AS ${value},"
			}
		}
		def query = new StringBuffer("""SELECT * FROM ( SELECT d.db_id AS dbId, ae.asset_name AS assetName,ae.asset_type AS assetType,
										 me.move_event_id AS event,
										 IF(ac_task.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus, IF(ac_comment.comment_type IS NULL, 'noComments','comments') AS commentsStatus,""")
		
		if(temp){
			query.append(temp)
		}
		
		/*COUNT(DISTINCT adr.asset_dependency_id)+COUNT(DISTINCT adr2.asset_dependency_id) AS depResolve, adb.dependency_bundle AS depNumber,
			COUNT(DISTINCT adc.asset_dependency_id)+COUNT(DISTINCT adc2.asset_dependency_id) AS depConflicts */
		
		query.append("""  ae.validation AS validation,ae.plan_status AS planStatus
			FROM data_base d 
			LEFT OUTER JOIN asset_entity ae ON d.db_id=ae.asset_entity_id
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id """)
		if(joinQuery)
			query.append(joinQuery)
			
		query.append(""" \n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
			LEFT OUTER JOIN asset_comment ac_task ON ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue'
			LEFT OUTER JOIN asset_comment ac_comment ON ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment'
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
				query.append( " AND (ae.move_bundle_id IN (${unasgnmbId}) OR ae.move_bundle_id IS NULL)" )
			}
		}
		query.append(" GROUP BY db_id) AS dbs ")
		/* LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id=ae.asset_entity_id 
			LEFT OUTER JOIN asset_dependency adr ON ae.asset_entity_id = adr.asset_id AND adr.status IN (${unknownQuestioned}) 
			LEFT OUTER JOIN asset_dependency adr2 ON ae.asset_entity_id = adr2.dependent_id AND adr2.status IN (${unknownQuestioned}) 
			LEFT OUTER JOIN asset_dependency adc ON ae.asset_entity_id = adc.asset_id AND adc.status IN (${validUnkownQuestioned}) 
				AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.dependent_id) != mb.move_bundle_id 
			LEFT OUTER JOIN asset_dependency adc2 ON ae.asset_entity_id = adc2.dependent_id AND adc2.status IN (${validUnkownQuestioned}) 
				AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.asset_id) != mb.move_bundle_id */
		
		def firstWhere = true
		def queryParams = [:]
		filterParams.each {
			if( it.getValue()  && it.getValue().trim().size()){
				
				if (firstWhere) {
					// single quotes are stripped from the filter to prevent SQL injection
					query.append(" WHERE dbs.${it.getKey()} LIKE :${it.getKey()}")
					firstWhere = false
				} else {
					query.append(" AND dbs.${it.getKey()} LIKE :${it.getKey()}")
				}
				def paramValue = ""
				if(it.getValue()){
					paramValue = it.getValue().replaceAll("'", "")
				}
				queryParams << [ (it.getKey()) : "%"+StringEscapeUtils.escapeJava(paramValue)+"%"]
			}
		}
		if(params.moveBundleId){
			if(params.moveBundleId!='unAssigned'){
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				query.append(" WHERE dbs.moveBundle  = '${bundleName}' ")
			}else{
				query.append(" WHERE dbs.moveBundle IS NULL ")
			}
		}
		if( params.toValidate){
			query.append(" WHERE dbs.validation='Discovery'")
		}
		if(params.plannedStatus){
			query.append(" WHERE dbs.planStatus='${params.plannedStatus}'")
		}

		def dbsList = []
		query.append(" ORDER BY ${sortIndex} ${sortOrder}")
		if(queryParams.size()){
			def namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource)
			dbsList = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		}else{
			dbsList = jdbcTemplate.queryForList(query.toString())
		}
		
		
		def totalRows = dbsList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0)
			dbsList = dbsList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			dbsList = []

		def results = dbsList?.collect { 
			def commentType = it.commentType
			[ cell: ['',it.assetName, (it[dbPref["1"]] ?: ''), it[dbPref["2"]], it[dbPref["3"]], it[dbPref["4"]], it[dbPref["5"]], 
					/*it.depNumber, it.depResolve==0?'':it.depResolve, it.depConflicts==0?'':it.depConflicts,*/
					it.tasksStatus,	it.assetType, it.commentsStatus], id: it.dbId, escapedName:assetEntityService.getEscapedName(it)
			]}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	
	def show() {
		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		def dbId = params.id

		def db = controllerService.getAssetForPage(this, project, AssetClass.DATABASE, dbId)

		if (!db) {
			render "Database was not found with id $dbId".toString()
		} else {
			def model = databaseService.getModelForShow(project, db, params)

			return model
		}
	}

	def create() {
		def databaseInstance = new Database(appOwner:'TDS')
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def environmentOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		//fieldImportance for Discovery by default
		def configMap = assetEntityService.getConfig('Database','Discovery')
		def highlightMap = assetEntityService.getHighlightedInfo('Database', databaseInstance, configMap)
		
		[databaseInstance:databaseInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
			planStatusOptions:planStatusOptions?.value, projectId:projectId, project:project, 
			config:configMap.config, customs:configMap.customs, environmentOptions:environmentOptions?.value, highlightMap:highlightMap]
	}
	
	def edit() {
		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return
			
		def databaseInstance = controllerService.getAssetForPage(this, project, AssetClass.DATABASE, params.id)
		if (!databaseInstance) {
			render '<span class="error">Unable to find Database asset to edit</span>'
			return
		}

		def model = assetEntityService.getDefaultModelForEdits('Database', project,databaseInstance, params)

		model.databaseInstance = databaseInstance

		return model		
	}
	
	def save() {
		controllerService.saveUpdateAssetHandler(this, session, databaseService, AssetClass.DATABASE, params)
		session.DB?.JQ_FILTERS = params
     }

	def update() {
		controllerService.saveUpdateAssetHandler(this, session, databaseService, AssetClass.DATABASE, params)
		session.DB?.JQ_FILTERS = params		
	}

	def delete() {
		def database = Database.get( params.id )
		if( database ) {
			def assetName = database.assetName
			assetEntityService.deleteAsset( database )
			database.delete()
			
			flash.message = "Database ${assetName} deleted"
			if (params.dstPath == 'dependencyConsole') {
				forward( controller:'assetEntity',action:'retrieveLists', params:[entity: 'database',dependencyBundle:session.getAttribute("dependencyBundle")])
			} else {
				redirect( action:"list" )
			}
		}
		else {
			flash.message = "Database not found with id ${params.id}"
			redirect( action:"list" )
		}
		
	}
	/**
	 * Delete multiple database.
	 */
	def deleteBulkAsset() {
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
