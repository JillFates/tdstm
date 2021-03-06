package net.transitionmanager.asset


import com.tdsops.common.security.spring.HasPermission
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.search.FieldSearchData
import net.transitionmanager.security.Permission
import net.transitionmanager.common.ControllerService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.exception.LogicException
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.task.TaskService
import net.transitionmanager.person.UserPreferenceService
import org.apache.commons.lang.StringEscapeUtils
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import static com.tdsops.tm.enums.domain.AssetClass.APPLICATION

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ApplicationController implements ControllerMethods, PaginationMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	ApplicationService applicationService
	AssetEntityService assetEntityService
	ControllerService controllerService
	CustomDomainService customDomainService
	JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	PartyRelationshipService partyRelationshipService
	TaskService taskService
	UserPreferenceService userPreferenceService
	AssetService assetService
	AssetOptionsService assetOptionsService

	@HasPermission(Permission.AssetView)
	def list() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def filters = session.APP?.JQ_FILTERS
		session.APP?.JQ_FILTERS = []

		def fieldPrefs = assetEntityService.getExistingPref(PREF.App_Columns)

		[ appName: filters?.assetNameFilter ?: '', appPref: fieldPrefs, appSme: filters?.appSmeFilter ?: '',
		 availabaleRoles: partyRelationshipService.getStaffingRoles(), // TODO - This should be replaced with the staffRoles which is in the defaultModel already
		 company: project.client, latencys: params.latencys, planMethodology: params.planMethodology,
		 partyGroupList: partyRelationshipService.getCompaniesList(), runbook: params.runbook,
		 validationFilter: filters?.appValidationFilter ?: '',
		 ufp: params.ufp == 'true' ?: ''
		] + assetEntityService.getDefaultModelForLists(APPLICATION, 'Application', project, fieldPrefs, params, filters)
	}

	/**
	 * Used by JQgrid to load appList
	 */
	@HasPermission(Permission.AssetView)
	def listJson() {
		Project project = getProjectForWs()

		// Get the pagination and set the user preference appropriately
		Integer maxRows = paginationMaxRowValue('rows', PREF.ASSET_LIST_SIZE, true)
		Integer currentPage = paginationPage()
		Integer rowOffset = paginationRowOffset(currentPage, maxRows)
		String sortIndex = paginationOrderBy(Application, 'sidx', 'assetName')
		String sortOrder = paginationSortOrder('sord')

		boolean firstWhere = true

		Map filterParams = [
			assetName: params.assetName,
			depNumber: params.depNumber,
			depResolve: params.depResolve,
			depConflicts: params.depConflicts,
			event: params.event
		]

		// Get the list of the user's column preferences
		Map appPref = assetEntityService.getExistingPref(PREF.App_Columns)
		List<String> prefColumns = appPref*.value

		// Get the list of fields for the domain
		Map fieldNameMap = customDomainService.fieldNamesAsMap(project, AssetClass.APPLICATION.toString(), true)

		// Now match the columns selected and if there is a match, add the param[field] to the filter
		for (String fieldName in prefColumns) {
			if (fieldNameMap.containsKey(fieldName)) {
				filterParams[fieldName] = params[fieldName]
			}
		}

		List<MoveBundle> moveBundleList
		session.APP = [:]

		userPreferenceService.setPreference(PREF.ASSET_LIST_SIZE, maxRows)
		if (params.event && params.event.isNumber()) {
			moveBundleList = MoveEvent.read(params.event)?.moveBundles?.findAll { it.useForPlanning }?.flatten() as List<MoveBundle>
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(securityService.loadUserCurrentProject(), true)
		}

		//def unknownQuestioned = "'$AssetDependencyStatus.UNKNOWN','$AssetDependencyStatus.QUESTIONED'"
		//def validUnkownQuestioned = "'$AssetDependencyStatus.VALIDATED'," + unknownQuestioned
		String justPlanning = userPreferenceService.getPreference(PREF.ASSET_JUST_PLANNING) ?: 'true'
		Map<String, String> customizeQuery = assetEntityService.getAppCustomQuery(appPref)

		Map queryParams = [:]
		def query = new StringBuilder('''
			SELECT * FROM (SELECT a.app_id AS appId, ae.asset_name AS assetName, a.latency AS latency,
				(SELECT if (count(ac_task.comment_type) = 0, 'tasks','noTasks') FROM asset_comment ac_task WHERE ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue') AS tasksStatus,
				(SELECT if (count(ac_comment.comment_type = 0), 'comments','noComments') FROM asset_comment ac_comment WHERE ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment') AS commentsStatus,
				me.move_event_id AS event, ''')

		if (customizeQuery.query) {
			query.append(customizeQuery.query)
		}

		query.append(''' ae.asset_type AS assetType,
			ae.validation AS validation,
			ae.plan_status AS planStatus,
			me.runbook_status AS runbookStatus,
			ae.move_bundle_id,
			mb.name as moveBundle
			FROM application a
			LEFT OUTER JOIN asset_entity ae ON a.app_id=ae.asset_entity_id''')

		if (params.planMethodology) {
			String customField = project.planMethodology

			if(customField){
				query.append(" AND ")

				// Unescaping the parameter since it can include HTML encoded characters (like \' == &#39; )
				String planMethodology = StringEscapeUtils.unescapeHtml(params.planMethodology)

				if (planMethodology == Application.PLAN_METHODOLOGY_UNKNOWN) {
					query.append(" (ae.`${customField}` is Null OR ae.`${customField}` = '') ")
				} else {
					query.append(" ae.`${customField}` = :planMethodology ")
					queryParams['planMethodology'] = planMethodology
				}
			}
		}

		if (customizeQuery.joinQuery) {
			query.append(customizeQuery.joinQuery)
		}
		//commented as per craig comments for performance issue
		/*COUNT(DISTINCT adr.asset_dependency_id)+COUNT(DISTINCT adr2.asset_dependency_id) AS depResolve,  adb.dependency_bundle AS depNumber,
		COUNT(DISTINCT adc.asset_dependency_id)+COUNT(DISTINCT adc2.asset_dependency_id) AS depConflicts */

		query.append("""
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
			LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
			WHERE ae.project_id=${project.id}
			""")

		if (justPlanning == 'true' || params.ufp == 'true') {
			query.append(" AND mb.use_for_planning=true ")
		}

		if (params.event && params.event.isNumber() && moveBundleList) {
			query.append(" AND ae.move_bundle_id IN (:bundleListIds)")
			queryParams.bundleListIds = moveBundleList*.id
		}

		// params.unassigned when set will ONLY show assets not assigned to Events
		if (params.unassigned) {

			List<Long> unassignedToEvents = MoveBundle.where {
				project == project
				moveEvent == null
				useForPlanning == true
			}.projections { property 'id' }.list()

			if (unassignedToEvents) {
				query.append(" AND ae.move_bundle_id IN (:unassignedToEvents)")
				queryParams.unassignedToEvents = unassignedToEvents
			}
		}

		query.append('GROUP BY app_id ) AS apps')

		/*LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id=ae.asset_entity_id
		LEFT OUTER JOIN asset_dependency adr ON ae.asset_entity_id = adr.asset_id AND adr.status IN ($unknownQuestioned)
		LEFT OUTER JOIN asset_dependency adr2 ON ae.asset_entity_id = adr2.dependent_id AND adr2.status IN ($unknownQuestioned)
		LEFT OUTER JOIN asset_dependency adc ON ae.asset_entity_id = adc.asset_id AND adc.status IN ($validUnkownQuestioned)
			AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.dependent_id) != mb.move_bundle_id
		LEFT OUTER JOIN asset_dependency adc2 ON ae.asset_entity_id = adc2.dependent_id AND adc2.status IN ($validUnkownQuestioned)
			AND (SELECT move_bundle_id from asset_entity WHERE asset_entity_id = adc.asset_id) != mb.move_bundle_id*/

		// Handle the filtering by each column's text field

		List<String> whereConditions = []

		filterParams.each { column, filter ->
			if (filter?.trim()) {
				FieldSearchData fieldSearchData = new FieldSearchData([
						domain: Application,
						column: column,
						filter: filter,
						columnAlias: "apps.${column}"
				])

				SqlUtil.parseParameter(fieldSearchData)

				whereConditions << fieldSearchData.sqlSearchExpression
				queryParams += fieldSearchData.sqlSearchParameters
			}
		}

		if (whereConditions) {
			query.append(" WHERE ${whereConditions.join(" AND ")}")
			firstWhere = false
		}

		if (params.latencys) {
			firstWhere = SqlUtil.addWhereOrAndToQuery(query, firstWhere)
			if (params.latencys == 'unknown') {
				// Look for latencys not defined or incorrectly defined
				query.append(" coalesce(apps.latency,'') NOT IN ('Y','N') ")
			} else {
				query.append(' apps.latency = :latencysParam')
				queryParams.latencysParam = params.latencys
			}

		}

		if (params.moveBundleId) {
			firstWhere = SqlUtil.addWhereOrAndToQuery(query, firstWhere)
			if (params.moveBundleId != 'unAssigned') {
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				// @TODO : JPM 1/2016 : Fix for SQL injections @SECURITY
				query.append(" apps.moveBundle  = '$bundleName' ")
			} else {
				query.append(' apps.moveBundle IS NULL ')
			}
		}

		// Filter on validation
		if (params.toValidate) {
			ValidationType validationType
			if (params.toValidate == ValidationType.UNKNOWN || params.toValidate == ValidationType.VALIDATED) {
				validationType = ValidationType.UNKNOWN
			} else if (params.toValidate == ValidationType.PLAN_READY) {
				validationType = ValidationType.PLAN_READY
			} else {
				log.warn "listJson called with invalid toValidate parameter {}", params.toValidate
			}

			if (validationType) {
				firstWhere = SqlUtil.addWhereOrAndToQuery(query, firstWhere)
				query.append("apps.validation=:toValidationParam")
				queryParams.toValidationParam = validationType
			}
		}

		if (params.plannedStatus) {
			firstWhere = SqlUtil.addWhereOrAndToQuery(query, firstWhere)
			query.append("apps.planStatus=:plannedStatusParam")
			queryParams.plannedStatusParam = params.plannedStatus
		}

		if (params.runbook) {
			firstWhere = SqlUtil.addWhereOrAndToQuery(query, firstWhere)
			query.append(" apps.runbookStatus='Done' ")
		}

		query << ' ORDER BY ' << sortIndex <<  ' '  << sortOrder

		List appsList
		try {
			if (queryParams) {
				appsList = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
			} else {
				appsList = jdbcTemplate.queryForList(query.toString())
			}
		} catch (e) {
			log.error "listJson() encountered SQL error : ${e.getMessage()}"
			throw new LogicException("Unabled to perform query based on parameters and user preferences")
		}

		// Cut the list of selected applications down to only the rows that will be shown in the grid
		int totalRows = appsList.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0) {
			appsList = appsList[rowOffset..Math.min(rowOffset + maxRows, totalRows - 1)]
		}

		List<Map> results = appsList?.collect {
			[cell: ['', it.assetName, it[appPref['1']] ?: '', it[appPref['2']] ?: '', it[appPref['3']] ?: '',
			        it[appPref['4']] ?: '', it[appPref['5']] ?: '',
			        /*it.depNumber, it.depResolve == 0 ? '' : it.depResolve, it.depConflicts == 0 ? '' : it.depConflicts,*/
			        it.tasksStatus, it.assetType, it.event, it.commentsStatus],
			 id: it.appId, escapedName: assetEntityService.getEscapedName(it)]
		}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		render jsonData as JSON
	}

	/**
	 * used to set the Application custom columns pref as JSON
	 * @param columnValue
	 * @param from
	 * @render true
	 */
	@HasPermission(Permission.AssetView)
	def columnAssetPref() {
		def column = params.columnValue
		String fromKey = params.from
		def prefCode = params.type as PREF
		assert prefCode
		def existingColsMap = assetEntityService.getExistingPref(prefCode)
		String key = existingColsMap.find { it.value == column }?.key
		if (key) {
			existingColsMap[key] = params.previousValue
		}

		existingColsMap[fromKey] = column
		userPreferenceService.setPreference(prefCode, existingColsMap as JSON)
		render 'ok'
	}

	@HasPermission(Permission.AssetCreate)
	@Transactional(readOnly = true)
	def create() {
		Project project = securityService.userCurrentProject
		Application application = new Application()
		application.project = project

		// Set the default values on the custom properties
		assetService.setCustomDefaultValues(application)

		def moveBundleList = MoveBundle.findAllByProject(project,[sort: 'name'])
		List<AssetOptions> planStatusOptions = assetOptionsService.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		List<AssetOptions> environmentOptions = assetOptionsService.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		def moveEventList = MoveEvent.findAllByProject(project,[sort: 'name'])

		def personList = partyRelationshipService.getProjectApplicationStaff(project)
		def availabaleRoles = partyRelationshipService.getStaffingRoles()

		String domain = application.assetClass.toString()
		Map standardFieldSpecs = customDomainService.standardFieldSpecsByField(project, domain)
		List customFields = assetEntityService.getCustomFieldsSettings(project, domain, true)

		[
			applicationInstance: application,
			moveBundleList     : moveBundleList,
			planStatusOptions  : planStatusOptions?.value,
			projectId          : project.id,
			project            : project,
			moveEventList      : moveEventList,
			personList         : personList,
			company            : project.client,
			// TODO : fix misspelled variable availabaleRoles
			availabaleRoles    : availabaleRoles,
			environmentOptions : environmentOptions?.value,
			customs            : customFields,
			standardFieldSpecs : standardFieldSpecs
		]
	}

	@HasPermission(Permission.AssetView)
	def show() {
		def app
		Project project = controllerService.getProjectForPage(this)
		if (project) {
			app = controllerService.getAssetForPage(this, project, APPLICATION, params.id)
		}

		if (!project || !app) {
			def errorMsg = flash.message
			flash.message = null
			errorMsg = errorMsg ?: "Application not found with id $params.id"
			log.debug "show() $errorMsg"
			renderErrorJson(errorMsg)
			return
		}

		applicationService.getModelForShow(project, app, params)
	}

	/**
	 * Render the edit view.
	 * @param : redirectTo
	 * @return : render to edit page based on condition as if 'redirectTo' is roomAudit then redirecting
	 * to auditEdit view
	 */
	@HasPermission(Permission.AssetEdit)
	@Transactional(readOnly = true)
	def edit() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		Application application = controllerService.getAssetForPage(this, project, APPLICATION, params.id)
		if (!application) {
			flash.message = "Application not found with id $params.id"
			redirect(action: 'list')
			return
		}

		// The list to show in the App Owner and SME selects should include ALL staff (project owner and partners)
		// along with ALL of the client staff that their person accounts are active.
		def personList = partyRelationshipService.getProjectApplicationStaff(project)
		def partyGroupList = partyRelationshipService.getCompaniesList()

		[	applicationInstance: application,
			availabaleRoles: partyRelationshipService.getStaffingRoles(),
			personList: personList,
			partyGroupList: partyGroupList
		] + assetEntityService.getDefaultModelForEdits('Application', project, application, params)
	}

	@HasPermission(Permission.AssetCreate)
	def save() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, applicationService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.APP?.JQ_FILTERS = params
	}

	@HasPermission(Permission.AssetEdit)
	def update() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, applicationService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.APP?.JQ_FILTERS = params
		session.setAttribute('USE_FILTERS','true')

		/*

			if (params.updateView == 'updateView') {
				forward(action: 'show', params: [id: params.id, errors: errors])

			}else if (params.updateView == 'closeView') {
				render flash.message
			}else{
				switch(params.redirectTo) {
					case 'room':
						redirect(controller: 'room',action: 'list')
						break
					case 'rack':
						redirect(controller: 'rackLayouts',action: 'create')
						break
					case 'console':
						redirect(controller: 'assetEntity', action: 'dashboardView', params: [showAll: 'show'])
						break
					case 'assetEntity':
						redirect(controller: 'assetEntity', action: 'list')
						break
					case 'database':
						redirect(controller: 'database', action: 'list')
						break
					case 'files':
						redirect(controller: 'files', action: 'list')
						break
					case 'listTask':
						render 'Application $application.assetName updated.'
						break
					case 'dependencyConsole':
						forward(controller: 'assetEntity',action: 'retrieveLists', params: [entity: params.tabType,dependencyBundle: session.getAttribute('dependencyBundle'),labelsList: 'apps'])
						break
				}
			}
		*/
	}

	@HasPermission(Permission.AssetDelete)
	def delete() {
		def application = Application.get(params.id)
		if (!application) {
			flash.message = "Application not found with id $params.id"
			redirect(action: 'list')
			return
		}

		applicationService.deleteApplication(application)

		flash.message = "Application $application.assetName deleted"
		if (params.dstPath == 'dependencyConsole') {
			forward(controller: 'assetEntity', action: 'retrieveLists',
			        params: [entity: 'apps', dependencyBundle: session.getAttribute('dependencyBundle')])
		}
		else {
			redirect(action: 'list')
		}
	}

}
