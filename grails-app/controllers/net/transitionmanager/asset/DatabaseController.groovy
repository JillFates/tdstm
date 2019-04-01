package net.transitionmanager.asset


import com.tdsops.common.sql.SqlUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ValidationType
import net.transitionmanager.search.FieldSearchData
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.AssetOptionsService
import net.transitionmanager.service.AssetService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.DatabaseService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.LogicException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class DatabaseController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	AssetEntityService assetEntityService
	ControllerService controllerService
	DatabaseService databaseService
	JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	ProjectService projectService
	TaskService taskService
	UserPreferenceService userPreferenceService
	AssetService assetService
	def customDomainService
	AssetOptionsService assetOptionsService

	@HasPermission(Permission.AssetView)
	def list() {
		def filters = session.DB?.JQ_FILTERS
		session.DB?.JQ_FILTERS = []

		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def fieldPrefs = assetEntityService.getExistingPref(PREF.Database_Columns)

		[dbFormat: filters?.dbFormatFilter ?: '', dbName: filters?.assetNameFilter ?:'', dbPref: fieldPrefs] +
		assetEntityService.getDefaultModelForLists(AssetClass.DATABASE, 'Database', project, fieldPrefs, params, filters)
	}

	/**
	 * Used by JQgrid to load assetList
	 */
	@HasPermission(Permission.AssetView)
	def listJson() {
		String sortIndex = params.sidx ?: 'assetName'
		String sortOrder  = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows

		Project project = securityService.userCurrentProject
		session.DB = [:]

		userPreferenceService.setPreference(PREF.ASSET_LIST_SIZE, maxRows)

		def moveBundleList
		if (params.event?.isNumber()) {
			def moveEvent = MoveEvent.read(params.event)
			moveBundleList = moveEvent?.moveBundles?.findAll { it.useForPlanning }
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project, true)
		}
		//def unknownQuestioned = "'$AssetDependencyStatus.UNKNOWN','$AssetDependencyStatus.QUESTIONED'"
		//def validUnkownQuestioned = "'$AssetDependencyStatus.VALIDATED'," + unknownQuestioned

		def filterParams = [
			assetName: params.assetName,
			depNumber: params.depNumber,
			depResolve: params.depResolve,
			depConflicts: params.depConflicts
		]
		def dbPref = assetEntityService.getExistingPref(PREF.Database_Columns)

		// Get the list of fields for the domain
		Map fieldNameMap = customDomainService.fieldNamesAsMap(project, AssetClass.DATABASE.toString(), true)

		List prefColumns = dbPref*.value
		for (String fieldName in prefColumns) {
			if (fieldNameMap.containsKey(fieldName)) {
				filterParams[fieldName] = params[fieldName]
			}
		}

		def initialFilter = params.initialFilter in [true,false] ? params.initialFilter : false
		def justPlanning = userPreferenceService.getPreference(PREF.ASSET_JUST_PLANNING)?:'true'
		//TODO:need to move the code to AssetEntityService
		String temp = ''
		String joinQuery = ''
		dbPref.each { key, value ->
			switch(value){
			case 'tagAssets':
				temp = """
				CONCAT(
                    '[',
                    if(
                        ta.tag_asset_id,
                        group_concat(
                            json_object('id', ta.tag_asset_id, 'tagId', t.tag_id, 'name', t.name, 'description', t.description, 'color', t.color)
                        ),
                        ''
                    ),
                    ']'
                ) as tagAssets, """
				joinQuery += """
					LEFT OUTER JOIN tag_asset ta on ae.asset_entity_id = ta.asset_id
					LEFT OUTER JOIN tag t on t.tag_id = ta.tag_id
				"""
				break
			case 'moveBundle':
				temp += 'mb.name AS moveBundle,'
				break
			case 'lastUpdated':
				temp +="ee.last_updated AS $value,"
				joinQuery +="\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n"
			break
			case 'modifiedBy':
				temp +="CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS modifiedBy,"
				joinQuery +="\n LEFT OUTER JOIN person p ON p.person_id=ae.modified_by \n"
			break
			case ~/custom1|custom2|custom3|custom4|custom5|custom6|custom7|custom8|custom9|custom10|custom11|custom12|custom13|custom14|custom15|custom16|custom17|custom18|custom19|custom20|custom21|custom22|custom23|custom24|custom25|custom26|custom27|custom28|custom29|custom30|custom31|custom32|custom33|custom34|custom35|custom36|custom37|custom38|custom39|custom40|custom41|custom42|custom43|custom44|custom45|custom46|custom47|custom48|custom49|custom50|custom51|custom52|custom53|custom54|custom55|custom56|custom57|custom58|custom59|custom60|custom61|custom62|custom63|custom64|custom65|custom66|custom67|custom68|custom69|custom70|custom71|custom72|custom73|custom74|custom75|custom76|custom77|custom78|custom79|custom80|custom81|custom82|custom83|custom84|custom85|custom86|custom87|custom88|custom89|custom90|custom91|custom92|custom93|custom94|custom95|custom96/:
				temp +="ae.$value AS $value,"
			break
			case 'dbFormat':
				temp+="d.db_format AS dbFormat,"
			break
			case ~/validation|planStatus/:
			break
			default:
				temp +="ae.${WebUtil.splitCamelCase(value)} AS $value,"
			}
		}
		def query = new StringBuilder("""
			SELECT * FROM (SELECT d.db_id AS dbId, ae.asset_name AS assetName,ae.asset_type AS assetType,
			(SELECT if (count(ac_task.comment_type) = 0, 'tasks','noTasks') FROM asset_comment ac_task WHERE ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue') AS tasksStatus,
			(SELECT if (count(ac_comment.comment_type = 0), 'comments','noComments') FROM asset_comment ac_comment WHERE ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment') AS commentsStatus,""")

		if (temp){
			query.append(temp)
		}

		/*COUNT(DISTINCT adr.asset_dependency_id)+COUNT(DISTINCT adr2.asset_dependency_id) AS depResolve, adb.dependency_bundle AS depNumber,
			COUNT(DISTINCT adc.asset_dependency_id)+COUNT(DISTINCT adc2.asset_dependency_id) AS depConflicts */

		query.append("""  ae.validation AS validation,ae.plan_status AS planStatus
			FROM data_base d
			LEFT OUTER JOIN asset_entity ae ON d.db_id=ae.asset_entity_id
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id """)
		if (joinQuery)
			query.append(joinQuery)

		query.append(""" \n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
			WHERE ae.project_id = $project.id """)

		if (justPlanning == 'true') {
			query.append(" AND mb.use_for_planning=$justPlanning ")
		}

		if (params.event && params.event.isNumber() && moveBundleList)
			query.append(" AND ae.move_bundle_id IN (${WebUtil.listAsMultiValueString(moveBundleList.id)})")

		if (params.unassigned){
			def unasgnMB = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.moveEvent IS NULL \
				AND mb.useForPlanning = :useForPlanning AND mb.project = :project ", [useForPlanning:true, project:project])

			if (unasgnMB){
				def unasgnmbId = WebUtil.listAsMultiValueString(unasgnMB?.id)
				query.append(" AND (ae.move_bundle_id IN ($unasgnmbId) OR ae.move_bundle_id IS NULL)")
			}
		}
		query.append(" GROUP BY db_id) AS dbs ")
		/* LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id=ae.asset_entity_id
			LEFT OUTER JOIN asset_dependency adr ON ae.asset_entity_id = adr.asset_id AND adr.status IN ($unknownQuestioned)
			LEFT OUTER JOIN asset_dependency adr2 ON ae.asset_entity_id = adr2.dependent_id AND adr2.status IN ($unknownQuestioned)
			LEFT OUTER JOIN asset_dependency adc ON ae.asset_entity_id = adc.asset_id AND adc.status IN ($validUnkownQuestioned)
				AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.dependent_id) != mb.move_bundle_id
			LEFT OUTER JOIN asset_dependency adc2 ON ae.asset_entity_id = adc2.dependent_id AND adc2.status IN ($validUnkownQuestioned)
				AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.asset_id) != mb.move_bundle_id */

		def queryParams = [:]
		def whereConditions = []
		filterParams.each { key, val ->
			if (val && val.trim().size()){
				FieldSearchData fieldSearchData = new FieldSearchData([
						domain: Database,
						column: key,
						filter: val,
						columnAlias: "dbs.${key}"

				])

				SqlUtil.parseParameter(fieldSearchData)

				whereConditions << fieldSearchData.sqlSearchExpression
				queryParams += fieldSearchData.sqlSearchParameters
			}
		}

		if (params.moveBundleId) {
			if (params.moveBundleId != 'unAssigned'){
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				whereConditions.add ' dbs.moveBundle = :bundleName '
				queryParams.put 'bundleName', bundleName
			} else {
				whereConditions.add ' dbs.moveBundle IS NULL '
			}
		}
		if (params.toValidate){
			whereConditions.add ' dbs.validation = :validation '
			queryParams.put 'validation', ValidationType.UNKNOWN
		}
		if (params.plannedStatus){
			whereConditions.add ' dbs.planStatus = :planStatus '
			queryParams.put 'planStatus', params.plannedStatus
		}

		if (whereConditions) {
			query.append(" WHERE ${whereConditions.join(" AND ")} ")
		}

		def dbsList = []
		query.append(" ORDER BY $sortIndex $sortOrder")

		try {
			if (queryParams.size()) {
				dbsList = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
			} else {
				dbsList = jdbcTemplate.queryForList(query.toString())
			}
		} catch(e) {
			log.error "listJson() encountered SQL error : ${e.getMessage()}"
			throw new LogicException("Unabled to perform query based on parameters and user preferences")
		}

		def totalRows = dbsList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0) {
			dbsList = dbsList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		}
		else {
			dbsList = []
		}

		def results = dbsList?.collect {
			def commentType = it.commentType
			[ cell: [
			'',it.assetName, (it[dbPref["1"]] ?: ''), (it[dbPref["2"]] ?: ''), (it[dbPref["3"]] ?: ''), (it[dbPref["4"]] ?: ''), (it[dbPref["5"]] ?: ''),
					/*it.depNumber, it.depResolve==0?'':it.depResolve, it.depConflicts==0?'':it.depConflicts,*/
					it.tasksStatus,
					it.assetType,
					it.commentsStatus],
					id: it.dbId,
					escapedName:assetEntityService.getEscapedName(it)
			]}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}

	@HasPermission(Permission.AssetView)
	def show(String id) {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def db = controllerService.getAssetForPage(this, project, AssetClass.DATABASE, id)
		if (!db) {
			render 'Database was not found with id ' + id
		} else {
			databaseService.getModelForShow(project, db, params)
		}
	}

	@HasPermission(Permission.AssetCreate)
	@Transactional(readOnly = true)
	def create() {
		Database databaseInstance = new Database()
		Project project = securityService.userCurrentProject
		databaseInstance.project = project

		assetService.setCustomDefaultValues(databaseInstance)

		List<AssetOptions> planStatusOptions = assetOptionsService.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		List<AssetOptions> environmentOptions = assetOptionsService.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		Map standardFieldSpecs = customDomainService.standardFieldSpecsByField(project, AssetClass.DATABASE)
		def customFields = assetEntityService.getCustomFieldsSettings(project, databaseInstance.assetClass.toString(), true)

		[
			databaseInstance  : databaseInstance,
			moveBundleList    : moveBundleList,
			planStatusOptions : planStatusOptions?.value,
			projectId         : project.id,
			project           : project,
			environmentOptions: environmentOptions?.value,
			standardFieldSpecs: standardFieldSpecs,
			customs           : customFields
		]
	}

	@HasPermission(Permission.AssetEdit)
	@Transactional(readOnly = true)
	def edit() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def databaseInstance = controllerService.getAssetForPage(this, project, AssetClass.DATABASE, params.id)
		if (!databaseInstance) {
			render '<span class="error">Unable to find Database asset to edit</span>'
			return
		}

		Map model = assetEntityService.getDefaultModelForEdits('Database', project, databaseInstance, params)

		model.databaseInstance = databaseInstance

		return model
	}

	@HasPermission(Permission.AssetCreate)
	def save() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, databaseService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.DB?.JQ_FILTERS = params
	}

	@HasPermission(Permission.AssetEdit)
	def update() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, databaseService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.DB?.JQ_FILTERS = params
	}

	@HasPermission(Permission.AssetDelete)
	def delete() {
		def database = Database.get(params.id)
		if (database) {
			def assetName = database.assetName
			assetEntityService.deleteAsset(database)

			flash.message = "Database $assetName deleted"
			if (params.dstPath == 'dependencyConsole') {
				forward(controller: 'assetEntity',action: 'retrieveLists',
				        params: [entity: 'database', dependencyBundle: session.getAttribute("dependencyBundle")])
			} else {
				redirect(action:"list")
			}
		} else {
			flash.message = "Database not found with id $params.id"
			redirect(action:"list")
		}
	}
}
