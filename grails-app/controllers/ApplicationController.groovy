import com.tds.asset.Application
import com.tds.asset.AssetOptions
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.AppMoveEvent
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApplicationService
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import static com.tdsops.tm.enums.domain.AssetClass.APPLICATION

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ApplicationController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	ApplicationService applicationService
	AssetEntityService assetEntityService
	ControllerService controllerService
	CustomDomainService customDomainService
	JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	PartyRelationshipService partyRelationshipService
	ProjectService projectService
	SecurityService securityService
	TaskService taskService
	UserPreferenceService userPreferenceService

	@HasPermission(Permission.AssetView)
	def list() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def filters = session.APP?.JQ_FILTERS
		session.APP?.JQ_FILTERS = []

		def fieldPrefs = assetEntityService.getExistingPref('App_Columns')

		[appName: filters?.assetNameFilter ?: '', appPref: fieldPrefs, appSme: filters?.appSmeFilter ?: '',
		 availabaleRoles: partyRelationshipService.getStaffingRoles(), // TODO - This should be replaced with the staffRoles which is in the defaultModel already
		 company: project.client, latencys: params.latencys, planMethodology: params.planMethodology,
		 partyGroupList: partyRelationshipService.getCompaniesList(), runbook: params.runbook,
		 validationFilter: filters?.appValidationFilter ?: ''] +
		 assetEntityService.getDefaultModelForLists(APPLICATION, 'Application', project, fieldPrefs, params, filters)
	}

	/**
	 * Used by JQgrid to load appList
	 */
	@HasPermission(Permission.AssetView)
	def listJson() {
		String sortIndex = params.sidx ?: 'assetName'
		String sortOrder = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows
		boolean firstWhere = true

		Map filterParams = [assetName: params.assetName, depNumber: params.depNumber, depResolve: params.depResolve,
		                    depConflicts: params.depConflicts, event: params.event]

		Map appPref = assetEntityService.getExistingPref('App_Columns')
		def appPrefVal = appPref.collect { it.value }

		projectService.getAttributes('Application').each { EavAttribute attribute ->
			if (attribute.attributeCode in appPrefVal) {
				filterParams[attribute.attributeCode] = params[attribute.attributeCode]
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
		def query = new StringBuilder('''
			SELECT * FROM (SELECT a.app_id AS appId, ae.asset_name AS assetName, a.latency AS latency,
			                      if (ac_task.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus,
			                      if (ac_comment.comment_type IS NULL, 'noComments','comments') AS commentsStatus,
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
			Project project = securityService.userCurrentProject
			String customField = project.planMethodology
			if(customField){
				query.append(" AND ")

				def planMethodology = params.planMethodology
				if (planMethodology == Application.UNDEFINED) {
					query.append(" (ae.`${customField}` is Null OR ae.`${customField}` = '') ")
				}else{
					query.append(" ae.`${customField}` = '${params.planMethodology}' ")
				}
			}
		}

		query.append('''	
			LEFT OUTER JOIN asset_comment ac_task ON ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue'
			LEFT OUTER JOIN asset_comment ac_comment ON ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment'
			''')

		if (customizeQuery.joinQuery) {
			query.append(customizeQuery.joinQuery)
		}
		//commented as per craig comments for performance issue
		/*COUNT(DISTINCT adr.asset_dependency_id)+COUNT(DISTINCT adr2.asset_dependency_id) AS depResolve,  adb.dependency_bundle AS depNumber,
		COUNT(DISTINCT adc.asset_dependency_id)+COUNT(DISTINCT adc2.asset_dependency_id) AS depConflicts */

		query.append('''\n LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
			LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
			WHERE ae.project_id=''').append(securityService.userCurrentProjectId).append(' ')

		if (justPlanning == 'true')
			query.append(" AND mb.use_for_planning=$justPlanning ")

		if (params.event && params.event.isNumber() && moveBundleList)
			query.append(" AND ae.move_bundle_id IN (${WebUtil.listAsMultiValueString(moveBundleList.id)})")

		if (params.unassigned) {
			def unasgnMB = MoveBundle.executeQuery('FROM MoveBundle mb WHERE mb.moveEvent IS NULL \
				AND mb.useForPlanning = :useForPlanning AND mb.project = :project ',
				[useForPlanning: true, project: securityService.loadUserCurrentProject()])

			if (unasgnMB) {
				def unasgnmbId = WebUtil.listAsMultiValueString(unasgnMB?.id)
				query.append(" AND (ae.move_bundle_id IN ($unasgnmbId) OR ae.move_bundle_id is null)")
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

		def queryParams = [:]
		def whereConditions = []
		filterParams.each { key, val ->
			if (val?.trim()) {
				whereConditions << SqlUtil.parseParameter(key, val, queryParams, Application)
			}
		}

		if (whereConditions) {
			query.append(" WHERE apps.${whereConditions.join(" AND apps.")}")
			firstWhere = false
		}

		if (params.latencys) {
			if (firstWhere) {
				query.append(' WHERE ')
				firstWhere = false
			} else {
				query.append(' AND ')
			}
			if (params.latencys != 'unknown') {
				query.append(" apps.latency = '${params.latencys.replaceAll("'", "")}' ")
			} else {
				query.append(" (apps.latency NOT IN ('Y','N') OR apps.latency IS NULL) ")
			}

		}

		if (params.moveBundleId) {
			if (firstWhere) {
				query.append(' WHERE ')
				firstWhere = false
			} else {
				query.append(' AND ')
			}
			if (params.moveBundleId != 'unAssigned') {
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				// @TODO : JPM 1/2016 : Fix for SQL injections @SECURITY
				query.append(" apps.moveBundle  = '$bundleName' ")
			} else {
				query.append(' apps.moveBundle IS NULL ')
			}
		}

		if (params.toValidate) {
			if (firstWhere) {
				query.append(' WHERE ')
				firstWhere = false
			}
			else {
				query.append(' AND ')
			}
			query.append("apps.validation='$params.toValidate'")
		}

		if (params.plannedStatus) {
			if (firstWhere) {
				query.append(' WHERE ')
				firstWhere = false
			}
			else {
				query.append(' AND ')
			}
			query.append(" apps.planStatus='$params.plannedStatus'")
		}

		if (params.runbook) {
			if (firstWhere) {
				query.append(' WHERE ')
				firstWhere = false
			}
			else {
				query.append(' AND ')
			}
			query.append(''' apps.runbookStatus='Done' ''')
		}

		query << ' ORDER BY ' << sortIndex <<  ' '  << sortOrder

		List appsList
		if (queryParams) {
			appsList = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		}
		else {
			appsList = jdbcTemplate.queryForList(query.toString())
		}

		//def appsList = jdbcTemplate.queryForList(query.toString())

		// Cut the list of selected applications down to only the rows that will be shown in the grid
		int totalRows = appsList.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0) {
			appsList = appsList[rowOffset..Math.min(rowOffset + maxRows, totalRows - 1)]
		}

		def results = appsList?.collect {
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
		def existingColsMap = assetEntityService.getExistingPref(params.type)
		String key = existingColsMap.find { it.value == column }?.key
		if (key) {
			existingColsMap[key] = params.previousValue
		}

		existingColsMap[fromKey] = column
		userPreferenceService.setPreference(params.type, existingColsMap as JSON)
		render true
	}

	@HasPermission(Permission.AssetCreate)
	def create() {
		def application = new Application()
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		Project project = securityService.userCurrentProject
		def moveBundleList = MoveBundle.findAllByProject(project,[sort: 'name'])
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def environmentOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		def moveEventList = MoveEvent.findAllByProject(project,[sort: 'name'])

		def personList = partyRelationshipService.getProjectApplicationStaff(project)
		def availabaleRoles = partyRelationshipService.getStaffingRoles()

		//fieldImportance for Discovery by default
		def configMap = assetEntityService.getConfig('Application','Discovery')
		def highlightMap = assetEntityService.getHighlightedInfo('Application', application, configMap)
		Map standardFieldSpecs = customDomainService.standardFieldSpecsByField("Application")

		[applicationInstance: application, assetTypeOptions: assetTypeOptions?.value, moveBundleList: moveBundleList,
			planStatusOptions: planStatusOptions?.value, projectId: project.id, project: project,moveEventList: moveEventList,
			config: configMap.config, customs: configMap.customs, personList: personList, company: project.client,
			availabaleRoles: availabaleRoles, environmentOptions: environmentOptions?.value, highlightMap: highlightMap, standardFieldSpecs: standardFieldSpecs]
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
	def edit() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		Application application = controllerService.getAssetForPage(this, project, APPLICATION, params.id)
		if (!application) {
			flash.message = "Application not found with id $params.id"
			redirect(action: 'list')
			return
		}

		assetEntityService.getMoveEvents(project).each {
			if (!AppMoveEvent.countByApplicationAndMoveEvent(application, it)) {
				new AppMoveEvent(application: application, moveEvent: it).save()
			}
		}

		// The list to show in the App Owner and SME selects should include ALL staff (project owner and partners)
		// along with ALL of the client staff that their person accounts are active.
		def personList = partyRelationshipService.getProjectApplicationStaff(project)

		[applicationInstance: application, availabaleRoles: partyRelationshipService.getStaffingRoles(),
		  personList: personList] +
		 assetEntityService.getDefaultModelForEdits('Application', project, application, params)
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
					case 'listComment':
						redirect(controller: 'assetEntity', action: 'listComment' , params: [projectId: project.id])
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

	@HasPermission(Permission.AssetDelete)
	def deleteBulkAsset() {
		String ids = params.id
		List<String> assetNames = []
		for (assetId in ids.split(',')) {
			Application application = Application.get(assetId)
			if (application) {
				assetNames << application.assetName
				applicationService.deleteApplication(application)
			}
		}
		render 'Application ' + WebUtil.listAsMultiValueString(assetNames) + ' deleted'
	}
}
