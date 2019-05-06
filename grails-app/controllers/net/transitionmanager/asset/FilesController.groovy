package net.transitionmanager.asset

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.common.ControllerService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.exception.LogicException
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.search.FieldSearchData
import net.transitionmanager.security.Permission
import net.transitionmanager.task.TaskService
import org.apache.commons.lang3.BooleanUtils
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class FilesController implements ControllerMethods, PaginationMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	AssetEntityService assetEntityService
	ControllerService controllerService
	JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	ProjectService projectService
	StorageService storageService
	TaskService taskService
	UserPreferenceService userPreferenceService
	CustomDomainService customDomainService
	AssetService assetService
	AssetOptionsService assetOptionsService

	@HasPermission(Permission.AssetView)
	def list() {
		def filters = session.FILES?.JQ_FILTERS
		session.FILES?.JQ_FILTERS = []

		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def fieldPrefs = assetEntityService.getExistingPref(PREF.Storage_Columns)

		[fileFormat: filters?.fileFormatFilter, fileName: filters?.assetNameFilter ?:'',
		 filesPref: fieldPrefs, size: filters?.sizeFilter] +
		 assetEntityService.getDefaultModelForLists(AssetClass.STORAGE, 'Files', project, fieldPrefs, params, filters)
	}

	/**
	 * Used by JQgrid to load assetList
	 */
	@HasPermission(Permission.AssetView)
	def listJson() {
		// Get the pagination and set the user preference appropriately
		Integer maxRows = paginationMaxRowValue('rows', PREF.ASSET_LIST_SIZE, true)
		Integer currentPage = paginationPage()
		Integer rowOffset = paginationRowOffset(currentPage, maxRows)
		String sortIndex = paginationOrderBy(Files, 'sidx', 'assetName')
		String sortOrder = paginationSortOrder('sord')


		Project project = securityService.userCurrentProject
		def moveBundleList
		session.FILES = [:]

		if (params.event && params.event.isNumber()) {
			def moveEvent = MoveEvent.read(params.event)
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useForPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project,true)
		}

		def filterParams = [
			assetName: params.assetName,
			depNumber: params.depNumber,
			depResolve: params.depResolve,
			depConflicts: params.depConflicts,
			event: params.event
		]

		// Get the list of fields for the domain
		Map fieldNameMap = customDomainService.fieldNamesAsMap(project, AssetClass.STORAGE.toString(), true)

		Map filePref= assetEntityService.getExistingPref(PREF.Storage_Columns)
		List prefColumns = filePref*.value
		for (String fieldName in prefColumns) {
			if (fieldNameMap.containsKey(fieldName)) {
				filterParams[fieldName] = params[fieldName]
			}
		}

		def justPlanning = BooleanUtils.toBoolean(userPreferenceService.getPreference(PREF.ASSET_JUST_PLANNING) ?: true)

		def query = new StringBuilder("""
			SELECT * FROM (SELECT f.files_id AS fileId, ae.asset_name AS assetName,ae.asset_type AS assetType,
			me.move_event_id AS event,
			(SELECT if (count(ac_task.comment_type) = 0, 'noTasks','tasks') FROM asset_comment ac_task WHERE ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue') AS tasksStatus,
			(SELECT if (count(ac_comment.comment_type = 0), 'comments','noComments') FROM asset_comment ac_comment WHERE ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment') AS commentsStatus,
		""")

		def queryParams = [:]

		//TODO:need to move the code to AssetEntityService
		String temp=""
		String joinQuery=""
		filePref.each { key, value ->
			switch(value) {
				case 'tagAssets':
					temp += """
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
				temp +="mb.name AS moveBundle,"
			break
			case ~/custom1|custom2|custom3|custom4|custom5|custom6|custom7|custom8|custom9|custom10|custom11|custom12|custom13|custom14|custom15|custom16|custom17|custom18|custom19|custom20|custom21|custom22|custom23|custom24|custom25|custom26|custom27|custom28|custom29|custom30|custom31|custom32|custom33|custom34|custom35|custom36|custom37|custom38|custom39|custom40|custom41|custom42|custom43|custom44|custom45|custom46|custom47|custom48|custom49|custom50|custom51|custom52|custom53|custom54|custom55|custom56|custom57|custom58|custom59|custom60|custom61|custom62|custom63|custom64|custom65|custom66|custom67|custom68|custom69|custom70|custom71|custom72|custom73|custom74|custom75|custom76|custom77|custom78|custom79|custom80|custom81|custom82|custom83|custom84|custom85|custom86|custom87|custom88|custom89|custom90|custom91|custom92|custom93|custom94|custom95|custom96/:
				temp +="ae.$value AS $value,"
			break
			case 'fileFormat':
				temp+="f.file_format AS fileFormat,"
			break
			case "LUN":
				temp +="f.lun AS LUN,"
			break
			case 'lastUpdated':
				temp +="ee.last_updated AS $value,"
				joinQuery +="\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n"
			break
			case 'modifiedBy':
				temp +="CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS modifiedBy,"
				joinQuery +="\n LEFT OUTER JOIN person p ON p.person_id=ae.modified_by \n"
			break
			case ~/validation|planStatus/:
			break
			default:
				temp +="ae.${WebUtil.splitCamelCase(value)} AS $value,"
			}
		}

		if (temp) {
			query.append(temp)
		}

		query.append(""" ae.validation AS validation,ae.plan_status AS planStatus
				FROM files f
				LEFT OUTER JOIN asset_entity ae ON f.files_id=ae.asset_entity_id
				LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id """)

		if (joinQuery) {
			query.append(joinQuery)
		}

		query.append("""\n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
				WHERE ae.project_id = :projectId """)
		queryParams.projectId = NumberUtil.toPositiveLong(project.id)

		if (justPlanning) {
			query.append(" AND mb.use_for_planning = true ")
		}

		if (params.event && params.event.isNumber() && moveBundleList) {
			query.append(" AND ae.move_bundle_id IN (:moveBundleIdList) ")
			queryParams.moveBundleIdList = moveBundleList*.id
		}

		if (params.unassigned) {
			List<Long> unassignedMoveBundleIds = MoveBundle.executeQuery('''
				SELECT id
				FROM MoveBundle
				WHERE moveEvent IS NULL
				  AND useForPlanning=:useForPlanning
				  AND project=:project
			''', [useForPlanning: true, project: project])

			if (unassignedMoveBundleIds) {
				query.append(" AND (ae.move_bundle_id IN (:unasgnmbId) OR ae.move_bundle_id IS NULL)")
				queryParams.unasgnmbId = unassignedMoveBundleIds
			}
		}

		query.append(" GROUP BY files_id) AS files ")

		def whereConditions = []
		filterParams.each {key, val ->
			if (val && val.trim().size()) {
				FieldSearchData fieldSearchData = new FieldSearchData([
						domain: Files,
						column: key,
						filter: val,
						columnAlias: "files.${key}"

				])

				SqlUtil.parseParameter(fieldSearchData)

				whereConditions << fieldSearchData.sqlSearchExpression
				queryParams += fieldSearchData.sqlSearchParameters
			}
		}

		if (whereConditions.size()) {
			query.append(" WHERE ${whereConditions.join(" AND ")}")
		}

		if (params.moveBundleId) {
			if (params.moveBundleId!='unAssigned') {
				String bundleName = MoveBundle.get(params.moveBundleId)?.name
				query.append(" WHERE files.moveBundle  = :bundleName ")
				queryParams.bundleName = bundleName
			} else {
				query.append(" WHERE files.moveBundle IS NULL ")
			}
		}
		if (params.toValidate) {
			query.append(" WHERE files.validation='${ValidationType.UNKNOWN}'")
		}
		if (params.plannedStatus) {
			query.append(" WHERE files.planStatus=:plannedStatus")
			queryParams.plannedStatus = params.plannedStatus
		}
		query.append(" ORDER BY $sortIndex $sortOrder")

		def filesList
		try {
			filesList = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		} catch(e) {
			log.error "listJson() encountered SQL error : ${e.getMessage()}"
			throw new LogicException("Unabled to perform query based on parameters and user preferences")
		}

		int totalRows = filesList.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0) {
			filesList = filesList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		}

		def results = filesList?.collect {
			def commentType = it.commentType
			[cell: ['', it.assetName, it[filePref["1"]] ?: '', it[filePref["2"]] ?: '', it[filePref["3"]] ?: '',
			        it[filePref["4"]] ?: '', it[filePref["5"]] ?: '',
			        /*it.depNumber, it.depResolve == 0 ? '' : it.depResolve, it.depConflicts == 0 ? '' : it.depConflicts,*/
			        it.tasksStatus, it.assetType, it.commentsStatus],
			 id: it.fileId, escapedName: assetEntityService.getEscapedName(it)]
		}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}

	@HasPermission(Permission.AssetCreate)
	@Transactional(readOnly = true)
	def create() {
		// TODO : JPM 10/2014 : refactor create to get model from service layer
		Files files = new Files()
		Project project = securityService.userCurrentProject
		files.project = project

		assetService.setCustomDefaultValues(files)

		// Obtains the domain out of the asset type string
		String domain = AssetClass.getDomainForAssetType('Files')
		Map standardFieldSpecs = customDomainService.standardFieldSpecsByField(project, domain)
		def customFields = assetEntityService.getCustomFieldsSettings(project, files.assetClass.toString(), true)

		[
			fileInstance      : files,
			moveBundleList    : MoveBundle.findAllByProject(project, [sort: 'name']),
			planStatusOptions : assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.STATUS_OPTION),
			environmentOptions: assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION),
			standardFieldSpecs: standardFieldSpecs,
			project           : project,
			customs           : customFields
		]
	}

	@HasPermission(Permission.AssetView)
	def show(String id) {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def storage = controllerService.getAssetForPage(this, project, AssetClass.STORAGE, id)
		if (!storage) {
			render 'Storage asset was not found with id ' + id
		} else {
			storageService.getModelForShow(project, storage, params)
		}
	}

	@HasPermission(Permission.AssetEdit)
	@Transactional(readOnly = true)
	def edit() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def fileInstance = controllerService.getAssetForPage(this, project, AssetClass.STORAGE, params.id)
		if (!fileInstance) {
			render '<span class="error">Unable to find file to edit</span>'
			return
		}

		[fileInstance: fileInstance] +
		assetEntityService.getDefaultModelForEdits('Files', project, fileInstance, params)
	}

	@HasPermission(Permission.AssetCreate)
	def save() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, storageService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.FILES?.JQ_FILTERS = params
	}

	@HasPermission(Permission.AssetEdit)
	def update() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, storageService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.FILES?.JQ_FILTERS = params

		/*

			session.setAttribute("USE_FILTERS","true")

			switch (params.redirectTo) {
				case "room":
					redirect(controller:'room',action:"list")
					break
				case "rack":
					redirect(controller:'rackLayouts',action:'create')
					break
				case "console":
					redirect(controller:'assetEntity', action:"dashboardView", params:[showAll:'show'])
					break
				case "assetEntity":
					redirect(controller:'assetEntity', action:"list")
					break
				case "database":
					redirect(controller:'database', action:"list")
					break
				case "application":
					redirect(controller:'application', action:"list")
					break
				case "listTask":
					render "Storage $filesInstance.assetName updated."
					break
				case "dependencyConsole":
					forward(controller:'assetEntity',action:'retrieveLists', params:[entity: params.tabType,dependencyBundle:session.getAttribute("dependencyBundle"),labelsList:'apps'])
					break
				default:
					session.FILES?.JQ_FILTERS = params
					redirect(action:"list")
			}
		*/

	}

	@HasPermission(Permission.AssetDelete)
	def delete() {
		Files files = Files.get(params.id)
		if (!files) {
			flash.message = "Storage not found with id $params.id"
			redirect(action: "list")
			return
		}

		def assetName = files.assetName
		assetEntityService.deleteAsset(files)

		flash.message = "Storage $assetName deleted"
		if (params.dstPath =='dependencyConsole') {
			forward(controller: 'assetEntity', action: 'retrieveLists',
			        params: [entity: 'files', dependencyBundle: session.getAttribute("dependencyBundle")])
		} else {
			redirect(action: "list")
		}
	}

}
