package net.transitionmanager.project

import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.AssetOptionsService
import net.transitionmanager.asset.AssetType
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.asset.graph.AssetGraph
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.spreadsheet.SheetWrapper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import net.transitionmanager.bulk.change.BulkChangeTag
import net.transitionmanager.command.MoveBundleCommand
import net.transitionmanager.common.ProgressService
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleStep
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventSnapshot
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyType
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.project.StateEngineService
import net.transitionmanager.project.StepSnapshot
import net.transitionmanager.security.SecurityService
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.TagService
import net.transitionmanager.task.TaskService
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.hibernate.transform.Transformers
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Transactional
class MoveBundleService implements ServiceMethods {

	AssetEntityService       assetEntityService
	JdbcTemplate             jdbcTemplate
	PartyRelationshipService partyRelationshipService
	ProgressService          progressService
	StateEngineService       stateEngineService
	TaskService              taskService
	UserPreferenceService    userPreferenceService
	TagService               tagService
	AssetOptionsService      assetOptionsService
	SecurityService          securityService
	ProjectService           projectService

	private static final Map<String, Number> defaultsSmall =  [force: -500, linkSize:  90, friction: 0.7, theta: 1, maxCutAttempts: 200]
	private static final Map<String, Number> defaultsMedium = [force: -500, linkSize: 100, friction: 0.7, theta: 1, maxCutAttempts: 150]
	private static final Map<String, Number> defaultsLarge =  [force: -500, linkSize: 120, friction: 0.7, theta: 1, maxCutAttempts: 100]

	static final String dependecyBundlingAssetTypesJoined = '("' + MoveBundle .dependecyBundlingAssetTypes.join('","') + '")'

	/**
	 * @param  : moveBundleId
	 * @return : assets count for a specified move bundle
	 */
	int assetCount(moveBundleId) {
		jdbcTemplate.queryForObject('select count(a.asset_entity_id) from asset_entity a where a.move_bundle_id = ?',
				NumberUtil.toLong(moveBundleId), Integer)
	}

	/**
	* Return all of the Transitions from the XML based on the workflow_code of the project that the move_bundle is associated with.
	 * @param moveBundleId
	 * @return Map[step,movebundleStep,snapshot]
	 */
	def getAllDashboardSteps(MoveBundle moveBundle) {
		List<MoveBundleStep> moveBundleSteps = MoveBundleStep.findAllByMoveBundle(moveBundle, [sort: 'transitionId'])
		def dashboardSteps = []
		try{
			stateEngineService.getDashboardSteps(moveBundle.workflowCode).each {
				MoveBundleStep moveBundleStep
				StepSnapshot stepSnapshot
				int stepIndex = moveBundleSteps.transitionId.indexOf(it.id)
				if(stepIndex != -1){
					moveBundleStep = moveBundleSteps[stepIndex]
					stepSnapshot = StepSnapshot.findByMoveBundleStep(moveBundleStep, [sort: 'dateCreated', order: 'DESC'])
					moveBundleSteps.remove(stepIndex)
				}
				dashboardSteps << [step :it, moveBundleStep: moveBundleStep, stepSnapshot: stepSnapshot]
			}
		} catch(NullPointerException e) {
			log.error e.message, e
		}

		[dashboardSteps: dashboardSteps, remainingSteps: moveBundleSteps?.transitionId]
	}

	/**
	 * Updates the moveBundles with moveEvent
	 */
	void assignMoveEvent(MoveEvent moveEvent, List moveBundleIds) {
		MoveBundle.executeUpdate('UPDATE MoveBundle SET moveEvent=null where moveEvent=:me', [me: moveEvent])
		for (id in moveBundleIds) {
			moveEvent.addToMoveBundles(MoveBundle.get(id))

		}
	}

	MoveBundleStep createMoveBundleStep(MoveBundle moveBundle, transitionId, Map params) {

		def beGreen = params["beGreen_"+transitionId]
		MoveBundleStep moveBundleStep = MoveBundleStep.findOrCreateByMoveBundleAndTransitionId(moveBundle, transitionId)
		if(params["calcMethod_"+transitionId]){
			moveBundleStep.calcMethod = params["calcMethod_"+transitionId]
		}
		moveBundleStep.label = params["dashboardLabel_"+transitionId]
		moveBundleStep.planStartTime = TimeUtil.parseDateTime((String) params['startTime_' + transitionId])
		moveBundleStep.planCompletionTime = TimeUtil.parseDateTime((String) params['completionTime_' + transitionId])

		//show the step progress in green when user select the beGreen option
		moveBundleStep.showInGreen = beGreen == 'on' ? 1 : 0

		save(moveBundleStep)
	}

	/**
	 * Delete a MoveBundleStep and associated records.
	 * @param  moveBundleStep to be deleted
	 */
	def deleteMoveBundleStep(MoveBundleStep moveBundleStep) {
		StepSnapshot.executeUpdate 'DELETE StepSnapshot where moveBundleStep=?', [moveBundleStep]
		moveBundleStep.delete()
	}

	/**
	 * Return MoveEvent Detailed Results for given event.
	 * @return : MoveEvent Detailed Results
	 */
	def getMoveEventDetailedResults(moveEventId) {
		jdbcTemplate.queryForList('''
			SELECT mb.move_bundle_id, mb.name AS bundle_name, ae.asset_entity_id AS asset_id, ae.asset_name,
			       IF(atr.voided=1, "Y", "") AS voided, wtFrom.name AS from_name, wtTo.name AS to_name,
				atr.date_created AS transition_time, username, IF(team_code IS NULL, '', team_code) AS team_name
			FROM move_event me
			JOIN project p ON p.project_id = me.project_id
			JOIN move_bundle mb ON mb.move_event_id = me.move_event_id
			JOIN asset_entity ae ON ae.move_bundle_id = mb.move_bundle_id
			JOIN asset_transition atr ON atr.asset_entity_id = ae.asset_entity_id
			JOIN workflow w on w.process = p.workflow_code
			JOIN workflow_transition wtFrom ON wtFrom.trans_id = CAST(atr.state_from AS UNSIGNED INTEGER) AND wtFrom.workflow_id = w.workflow_id
			JOIN workflow_transition wtTo ON wtTo.trans_id = CAST(atr.state_to AS UNSIGNED INTEGER) AND wtTo.workflow_id = w.workflow_id
			JOIN user_login ul ON ul.user_login_id = atr.user_login_id
			LEFT OUTER JOIN project_team pt ON pt.project_team_id = atr.project_team_id
			WHERE me.move_event_id = ?
				AND atr.is_non_applicable = 0
			ORDER BY move_bundle_id,transition_time''', NumberUtil.toLong(moveEventId))
	}

	/**
	 * Return MoveEvent Summary Results for given event.
	 * @param  : moveEventId
	 * @return : MoveEvent Summary Results
	 */
	def getMoveEventSummaryResults(moveEventId) {
		jdbcTemplate.execute('''
			CREATE TEMPORARY TABLE tmp_step_summary
				SELECT mb.move_bundle_id,mb.name as bundle_name, atr.state_to, wt.name, NOW() AS started, NOW() AS completed
				FROM move_event me
				JOIN project p ON p.project_id = me.project_id
				JOIN move_bundle mb ON mb.move_event_id = me.move_event_id
				JOIN asset_transition atr ON atr.move_bundle_id = mb.move_bundle_id AND atr.voided=0
				JOIN workflow w on w.process = p.workflow_code
				JOIN workflow_transition wt ON wt.trans_id = CAST(atr.state_to AS UNSIGNED INTEGER) AND wt.workflow_id = w.workflow_id
			WHERE me.move_event_id=? AND atr.is_non_applicable = 0
				GROUP BY mb.move_bundle_id, atr.state_to
			ORDER BY mb.move_bundle_id,started asc''', NumberUtil.toLong(moveEventId))

		jdbcTemplate.execute('''
			UPDATE tmp_step_summary SET completed = (
									   SELECT MAX(date_created) FROM asset_transition atr
					WHERE atr.move_bundle_id = tmp_step_summary.move_bundle_id
					  AND atr.state_to = tmp_step_summary.state_to
					  AND atr.voided = 0
					  AND is_non_applicable = 0)''')

		jdbcTemplate.execute('''
			UPDATE tmp_step_summary SET started = (
									   SELECT MIN(date_created) FROM asset_transition atr
					WHERE atr.move_bundle_id = tmp_step_summary.move_bundle_id
					  AND atr.state_to = tmp_step_summary.state_to
					  AND atr.voided = 0
					  AND is_non_applicable = 0)''')

		def summaryResults = jdbcTemplate.queryForList( "SELECT * FROM tmp_step_summary" )
		jdbcTemplate.execute( "DROP TEMPORARY TABLE IF EXISTS tmp_step_summary" )
		return summaryResults
	}

	/**
	 *  Deletes moveBundle, associated data and AssetEntities related to that bundle.
	 *  @param : moveBundle
	 */
	def deleteBundleAndAssets(MoveBundle moveBundle){
		try{
			//remove assets
			List assets = AssetEntity.where {
				moveBundle == moveBundle
			}.projections{
				property 'id'
			}.list()
			assetEntityService.deleteAssets(assets)
			Project project = securityService.loadUserCurrentProject()
			// As a precaution if there are any other AssetEntity types in the database we will want to associate them
			// with the Default project post the deletes.
			// Theoretically this isn't necessary but as a safety precaution
			AssetEntity.where {
				moveBundle == moveBundle
			}.updateAll(moveBundle: projectService.getDefaultBundle(project))

			//remove bundle and associated data
			deleteBundle(moveBundle, project)
		} catch (e) {
			log.error ExceptionUtil.stackTraceToString('deleting bundle and assets', e, 60)
			throw new DomainUpdateException('Unable to remove the bundle and assets')
		}
	}

	/**
	 *  Deletes moveBundle and associated data.
	 *  @param : moveBundle
	 *  @param : project
	 */
	String deleteBundle(MoveBundle moveBundle, Project project) {
		if (moveBundle.id == project.defaultBundleId) {
			return 'The project default bundle can not be deleted'
		}

		List<MoveBundle> projectBundles = MoveBundle.findAllByProject(project)
		boolean isRelated = projectBundles.any { it.id == moveBundle.id }
		if (!isRelated) {
			return 'Unable to locate specified bundle'
		}

		try {
			AssetEntity.executeUpdate("UPDATE AssetEntity SET moveBundle = ? WHERE moveBundle = ?",
					[project.defaultBundle, moveBundle])
			// remove bundle-associated data
			userPreferenceService.removeBundleAssociatedPreferences(securityService.userLogin)
			List<MoveEvent> events = MoveEvent.findAll()
			events.each {
				if (it.moveBundles.contains(moveBundle)) {
					it.removeFromMoveBundles(moveBundle)
				}
			}
			// Finally, delete the Bundle
			moveBundle.delete()
			return "MoveBundle $moveBundle deleted"
		}
		catch (e) {
			log.error e.message, e
			transactionStatus.setRollbackOnly()
			return "Unable to delete bundle " + moveBundle.name
		}
	}

	/*
	 * Used by several controller functions to generate the mapping arguments used by the dependencyConsole view
	 * @param project  the project
	 * @param moveBundleId - move bundle id to filter for bundle
	 * @return MapArray of properties
	 */
	def dependencyConsoleMap(Project project, Long moveBundleId, List<Long> tagIds, String tagMatch, String isAssigned, dependencyBundle, boolean graph = false, subsection =  null, groupId =  null, assetName = null) {
		Date startAll = new Date()
		def dependencyConsoleList = []
		Map queryParams = [:]

		// This will hold the totals of each, element 0 is all and element 1 will be All minus group 0
		def stats = [
			app: [0,0],
			db: [0,0],
			server: [0,0],
			vm: [0,0],
			storage: [0,0]
		]

		String physicalTypes = AssetType.physicalServerTypesAsString
		String virtualTypes = AssetType.virtualServerTypesAsString
		String storageTypes = AssetType.storageTypesAsString
		String reviewCodes = AssetDependencyStatus.reviewCodesAsString
		String tagQuery = tagService.getTagsQuery(tagIds, tagMatch, queryParams)
		String tagJoins = tagService.getTagsJoin(tagIds, tagMatch)

		def depSql = new StringBuilder("""SELECT
			adb.dependency_bundle AS dependencyBundle,
			COUNT(distinct adb.asset_id) AS assetCnt,
			CONVERT( group_concat(distinct a.move_bundle_id) USING 'utf8') AS moveBundles,
			SUM(if(a.plan_status='$AssetEntityPlanStatus.UNASSIGNED',1,0)) AS statusUnassigned,
			SUM(if(a.validation<>'${ValidationType.PLAN_READY}',1,0)) AS notBundleReady,
			SUM(if(a.asset_class = '$AssetClass.DEVICE'
				AND if(m.model_id > -1, m.asset_type in ($physicalTypes), a.asset_type in ($physicalTypes)), 1, 0)) AS serverCount,
			SUM(if(a.asset_class = '$AssetClass.DEVICE'
				AND if(m.model_id > -1, m.asset_type in ($virtualTypes), a.asset_type in ($virtualTypes)), 1, 0)) AS vmCount,
			SUM(if((a.asset_class = '$AssetClass.STORAGE')
				OR (a.asset_class = '$AssetClass.DEVICE'
				AND if(m.model_id > -1, m.asset_type in ($storageTypes), a.asset_type in ($storageTypes))), 1, 0)) AS storageCount,
			SUM(if(a.asset_class = '$AssetClass.DATABASE', 1, 0)) AS dbCount,
			SUM(if(a.asset_class = '$AssetClass.APPLICATION', 1, 0)) AS appCount,
			COALESCE(nr.needsReview, 0) AS needsReview

			FROM asset_dependency_bundle adb
			JOIN asset_entity a ON a.asset_entity_id=adb.asset_id
			LEFT OUTER JOIN model m ON a.model_id=m.model_id
			$tagJoins
			LEFT OUTER JOIN (SELECT adb.dependency_bundle, 1 AS needsReview
				FROM asset_entity ae INNER JOIN asset_dependency_bundle adb ON ae.asset_entity_id=adb.asset_id
				LEFT JOIN asset_dependency ad1 ON ad1.asset_id=ae.asset_entity_id
				LEFT JOIN asset_dependency ad2 ON ad2.dependent_id=ae.asset_entity_id
				WHERE adb.project_id=${project.id} AND (ad1.status IN (${reviewCodes}) OR ad2.status IN (${reviewCodes}))
			GROUP BY adb.dependency_bundle) nr ON nr.dependency_bundle=adb.dependency_bundle
			WHERE adb.project_id=${project.id} $tagQuery""")

		if (dependencyBundle) {
			List depGroups = JSON.parse((String) session.getAttribute('Dep_Groups'))

			if(dependencyBundle == 'onePlus'){
				depGroups = depGroups-[0]
			}

			if (depGroups.size() == 0) {
				depGroups = [-1]
			}

			if(dependencyBundle.isNumber()) {
				depSql.append(" AND adb.dependency_bundle = $dependencyBundle")
			}else if (dependencyBundle in ['all' , 'onePlus'] ) {
				depSql.append(" AND adb.dependency_bundle IN (${WebUtil.listAsMultiValueString(depGroups)})")
			}
		}

		depSql.append(" GROUP BY adb.dependency_bundle ORDER BY adb.dependency_bundle ")

		def dependList = []

		if(queryParams){
			NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(getJdbcTemplate().getDataSource())
			dependList = template.queryForList(depSql.toString(), queryParams)
		} else{
			dependList = jdbcTemplate.queryForList(depSql.toString())
		}

		if (moveBundleId) {
		 	dependList = dependList.collect{ if( it.moveBundles.contains(moveBundleId as String)){ it } }
		}

		dependList.removeAll([null])
		def groups = dependList.dependencyBundle
		if (dependencyBundle == null) {
			session.setAttribute('Dep_Groups', (groups as JSON).toString())
		}

		// TM-8535 Missing Remnants (Group 0)
		if( dependList && dependList[0].dependencyBundle != 0 ) {
			dependList.add(0, [
					  dependencyBundle: 0,
					  appCount: 0,
					  serverCount: 0,
					  vmCount: 0,
					  dbCount: 0,
					  storageCount: 0,
					  statusUnassigned: 0
			])
		}

		dependList.each { group ->
			String statusClass = ''
			if ( group.moveBundles?.contains(',') || group.needsReview > 0 ) {
				// Assets in multiple bundles or dependency status unknown or questioned
				statusClass = 'depGroupConflict'
			} else if ( group.notBundleReady == 0  && group.statusUnassigned == group.assetCnt ) {
				// If all assets are BundleReady and not fully assigned
				statusClass = 'depGroupReady'
			} else if ( group.statusUnassigned == 0 ) {
				// Assets assigned + moved total the number of assets in the group so the group is done
				statusClass = 'depGroupDone'
			}

			// Loop through the list to create map to be used by the view
			dependencyConsoleList << [
				dependencyBundle: group.dependencyBundle,
				appCount: group.appCount,
				serverCount: group.serverCount,
				vmCount: group.vmCount,
				dbCount: group.dbCount,
				storageCount: group.storageCount,
				statusClass: statusClass
			]

			// Accumulate the totals for ALL and 1+ (aka All - 0)
			stats.app[0] +=  group.appCount
			stats.db[0] +=  group.dbCount
			stats.server[0] +=  group.serverCount
			stats.vm[0] +=  group.vmCount
			stats.storage[0] +=  group.storageCount
			if (group.dependencyBundle != 0) {
				stats.app[1] +=  group.appCount
				stats.db[1] +=  group.dbCount
				stats.server[1] +=  group.serverCount
				stats.vm[1] +=  group.vmCount
				stats.storage[1] +=  group.storageCount
			}
		}

		// if this is being used for the dependency graph, this is all we need
		if (graph) {
			return stats
		}

		if (isAssigned == "1") {
			dependencyConsoleList = dependencyConsoleList.findAll{it.statusClass != "depGroupDone" || it.dependencyBundle == 0}
		}

		boolean showTabs = subsection != null

		def entities = assetEntityService.entityInfo(project)

		// Used by the Assignment Dialog
		def allMoveBundles = MoveBundle.findAllByProject(project, [sort: 'name'])
		def planningMoveBundles = allMoveBundles.findAll{return it.useForPlanning}
		List<AssetOptions> planStatusOptions = assetOptionsService.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def assetDependencyList = AssetDependencyBundle.executeQuery(
				'SELECT distinct(dependencyBundle) FROM AssetDependencyBundle WHERE project=?', [project])

		// JPM - don't think that this is required
		// def personList = partyRelationshipService.getCompanyStaff(project.client?.id)
		List<PartyGroup> companiesList = PartyGroup.findAllByPartyType(PartyType.load('COMPANY'), [sort: 'name'])

		def availabaleRoles = partyRelationshipService.getStaffingRoles()

		def depGrpCrt = project.depConsoleCriteria ? JSON.parse(project.depConsoleCriteria) : [:]
		def generatedDate = TimeUtil.formatDateTime(depGrpCrt.modifiedDate)
		def staffRoles = taskService.getRolesForStaff()
		def compactPref = userPreferenceService.getPreference(PREF.DEP_CONSOLE_COMPACT)
		def map = [
			company: project.client,
			asset: 'apps',
			date: generatedDate,
			dependencyType: entities.dependencyType,
			dependencyConsoleList: dependencyConsoleList,
			dependencyStatus: entities.dependencyStatus,
			assetDependency: new AssetDependency(),
			moveBundle: planningMoveBundles,
			allMoveBundles: allMoveBundles,
			planStatusOptions: planStatusOptions,

			gridStats:stats,

			//assetDependencyList: 	assetDependencyList,
			dependencyBundleCount: 	assetDependencyList.size(),
			servers: entities.servers,
			applications: entities.applications,
			dbs: entities.dbs,
			files: entities.files,
			networks:entities.networks,

			partyGroupList:companiesList,
			// personList:personList,
			staffRoles:staffRoles,
			availabaleRoles:availabaleRoles,
			moveBundleId : moveBundleId,
			isAssigned:isAssigned,
			moveBundleList:allMoveBundles,
			depGrpCrt:depGrpCrt,
			compactPref: compactPref,
			showTabs:showTabs,
			tabName: subsection,
			groupId: groupId,
			assetName: assetName,
			// Tags Properties
			tagIds: tagIds,
			tagMatch: tagMatch
		]

		log.info 'dependencyConsoleMap() : OVERALL took {}', TimeUtil.elapsed(startAll)

		return map
	}

	/**
	 * Calculates the default parameters for the dependency map based on the number of nodes
	 * @param nodeCount the number of nodes in the map
	 * @return a map of values for the dependency map to use as parameters
	 */
	Map<String, Number> getMapDefaults(int nodeCount) {
		nodeCount <= 50 ? defaultsSmall : nodeCount <= 200 ? defaultsMedium : defaultsLarge
	}

	/**
	 * Create Manual MoveEventSnapshot, when project is task driven. So dashboard dial default to manual 50
	 * @param moveEvent
	 * @param dialIndicator
	 * @return
	 */
	def createManualMoveEventSnapshot(MoveEvent moveEvent, int dialIndicator = 50){
		if(moveEvent.project.runbookOn ==1){
			MoveEventSnapshot moveEventSnapshot = new MoveEventSnapshot(moveEvent: moveEvent , dialIndicator: dialIndicator)
			!moveEventSnapshot.save()
		}
	}

	/**
	 * Method help to write data in excel sheet's appropriate column and remove redundant code.
	 * @param exportList : list of data which is being export
	 * @param columnList : list of column of sheet
	 * @param sheet : sheet-name
	 */
	def issueExport(List exportList, columnList, Sheet sheet, String tzId, String userDTFormat,
	                int startRow = 0, boolean viewUnpublished = false) {

		def dateFormatter = TimeUtil.createFormatterForType(userDTFormat, TimeUtil.FORMAT_DATE)

		def formatDateTimeForExport = { Date dateValue ->
			TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, dateValue)
		}

		//Lets build a sheetWrapper to hold the map of the styles and other shared resources of the sheet
		SheetWrapper sheetWrapper = new SheetWrapper(sheet)
		int rowCount = exportList.size() + startRow

		for (int r = startRow; r < rowCount; r++) {
			// get or create a row to reuse for Speed
			Row row = sheetWrapper.getOrCreateRow(r)

			for (int c = 0; c < columnList.size(); ++c) {
				def cellValue
				def attribName = columnList[c]
				int rowIdx = r - startRow
				boolean isNumber=false
				def currentTask = exportList[rowIdx]
				// log.debug '*** attribName isa {} {}', attribName.getClass().name, it instanceof Closure
				switch (attribName) {
					case { it instanceof Closure }:
						cellValue = attribName.call(currentTask)
						break
					case "taskDependencies":
						def values = exportList[r - startRow]."${columnList[c]}"
						if (!viewUnpublished) {
							values = values.findAll { it.predecessor?.isPublished }

						}
						cellValue = listAsPipeSeparatedString(values.collect { e ->
							(e.predecessor == null ? '' : e.predecessor.taskNumber) + ' ' + e.predecessor.comment }
						)

						cellValue = StringUtil.ellipsis(cellValue, 32767)
						break
					case 'taskNumber':
						cellValue = currentTask.taskNumber == null ? '':currentTask.taskNumber
						isNumber = true
						break
					case "assetEntity":
						cellValue = currentTask."${columnList[c]}"?.assetName
						break
					case "commentAssetEntity":
						cellValue = currentTask.assetEntity ?  String.valueOf(currentTask.assetEntity?.assetName) : ''
						break
					case "notes":
						cellValue = currentTask.notes ?  String.valueOf(WebUtil.listAsMultiValueString(currentTask.notes)) : ''
						break
					case "instructionsLink":
						cellValue = exportList[r-startRow].instructionsLink ?  String.valueOf(exportList[r-startRow].instructionsLink) : ''
						break
					case "workflow":
						cellValue = currentTask.workflowTransition ? String.valueOf(currentTask.workflowTransition?.name) : ''
						 break

					case "assetClass":
						cellValue = currentTask["assetEntity"]? String.valueOf(currentTask["assetEntity"]?.assetType) : ''
						break
					case "assetId":
						cellValue = currentTask["assetEntity"]? String.valueOf(currentTask["assetEntity"]?.id) : ''
						break
					case "durationLocked":
						cellValue = currentTask.durationLocked? "Y" : "N"
						break
					/*
					case "estStart":
						 cellValue = formatDateForExport(currentTask.estStart)
						break
					case "estFinish":
						 cellValue = formatDateForExport(currentTask.estFinish)
						 break
					case "actStart":
						 cellValue = formatDateForExport(currentTask.actStart)
						 break
					case "actFinish":
						 cellValue = formatDateForExport(currentTask.actFinish)
						 break
					*/
					case ~/actStart|dateResolved|dateCreated|estStart|estFinish/:
						cellValue = formatDateTimeForExport(currentTask[attribName])
						 break

					case "dueDate":
						 cellValue = TimeUtil.formatDate(currentTask.dueDate, dateFormatter)
						 break
					case "duration":
						 def duration = currentTask.duration
						 cellValue = duration? String.valueOf(duration):"0"
						 break
					case "":
						cellValue = ""
						break
					default:
						cellValue = String.valueOf(currentTask?."${columnList[c]}" ?:'')
						break
				}
				if(cellValue){
					if (isNumber) {
						sheetWrapper.addCell(row, c, cellValue, Cell.CELL_TYPE_NUMERIC)
					} else {
						sheetWrapper.addCell(row, c, cellValue)
					}
				}

			}
		}
	}

	/**
	 * Performs an analysis of the interdependencies of assets for a project and creates assetDependencyBundle records appropriately. It will
	 * find all assets assigned to bundles that which are set to be used for planning, sorting the assets so that those with the most dependency
	 * relationships are processed first.
	 * @param projectId : Related project
	 * @param connectionTypes : filter for asset types
	 * @param statusTypes : filter for status types
	 * @param isChecked : check if should use the new criteria
	 * @param progressKey : progress key
	 * @return String message information
	 */
	def generateDependencyGroups(projectId, connectionTypes, statusTypes, isChecked, userLoginName, progressKey) {

		String sqlTime = TimeUtil.formatDateTimeAsGMT(TimeUtil.nowGMT(), TimeUtil.FORMAT_DATE_TIME_14)
		Project project = Project.get(projectId)

		// Get array of the valid status and connection types to check against in the inner loop
		def statusList = statusTypes.replaceAll(', ',',').replaceAll("'",'').tokenize(',')
		def connectionList = connectionTypes.replaceAll(', ',',').replaceAll("'",'').tokenize(',')

		// User previous setting if exists else set to empty
		def depCriteriaMap = project.depConsoleCriteria ? JSON.parse(project.depConsoleCriteria) : [:]
		if (isChecked == "1") {
			depCriteriaMap = [statusTypes: statusList, connectionTypes: connectionList]
		}
		depCriteriaMap.modifiedBy = userLoginName
		depCriteriaMap.modifiedDate = TimeUtil.nowGMT().getTime()
		project.depConsoleCriteria = depCriteriaMap as JSON
		project.save(flush:true)

		// Find all move bundles that are flagged for Planning in the project and then get all assets in those bundles
		List<Long> moveBundleIds = MoveBundle.findAllByUseForPlanningAndProject(true,project).id
		String moveBundleText = GormUtil.asCommaDelimitedString(moveBundleIds)
		List<String> moveBundleIdStrings = moveBundleIds*.toString()

		def errMsg

		if (moveBundleText) {
			def started = new Date()
			progressService.update(progressKey, 10I, ProgressService.STARTED, "Search assets and dependencies")

			def results = searchForAssetDependencies(moveBundleText, connectionTypes, statusTypes)

			log.info 'Dependency groups generation - Search assets and dependencies time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 20I, ProgressService.STARTED, "Load asset results")

			def graph = new AssetGraph()
			graph.loadFromResults(results)

			log.info 'Dependency groups generation - Load results time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 30I, ProgressService.STARTED, "Clean dependencies")

			cleanDependencyGroupsStatus(projectId)

			log.info 'Dependency groups generation - Clean dependencies time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 40I, ProgressService.STARTED, "Group dependencies")

			def groups = graph.groupByDependencies(statusList, connectionList, moveBundleIdStrings)
			groups.sort { a, b -> b.size() <=> a.size() }


			log.info 'Dependency groups generation - Group dependencies time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 70I, ProgressService.STARTED, "Save dependencies")

			saveDependencyGroups(project, groups, sqlTime)

			log.info 'Dependency groups generation - Save dependencies time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 80I, ProgressService.STARTED, "Add straggles assets")

			// Last step is to put all the straggler assets that were not grouped into group 0
			addStragglerDepsToGroupZero(projectId, moveBundleText, sqlTime)

			log.info 'Dependency groups generation - Add straggles time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 90I, ProgressService.STARTED, "Finishing")

			graph.destroy()

			log.info 'Dependency groups generation - Destroy graph time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 100I, ProgressService.COMPLETED, "Finished")

		} else {
			errMsg = "Please associate appropriate assets to one or more 'Planning' bundles before continuing."
		}
	}

	/**
	 * Performs a search for all the assets and dependencies that match the parameters.
	 * @param moveBundleText : bundle ids to analyze
	 * @param connectionTypes : filter for asset types on connection
	 * @param statusTypes : filter for status types
	 * @return List of records
	 */
	private def searchForAssetDependencies(moveBundleText, connectionTypes, statusTypes) {
		// First we need the list of assets that belongs to planning bundles - See TM-10261
		String assetIdsSubQuery = "select a.asset_entity_id from asset_entity a where a.move_bundle_id in ( ${moveBundleText} )";

		String filterOptionsCriteria  = " AND ad.asset_id != ad.dependent_id "
		filterOptionsCriteria  += connectionTypes ? "AND ad.type in (${connectionTypes}) " : ""
		filterOptionsCriteria  += statusTypes ? "AND ad.status in (${statusTypes}) " : ""


		// Query to fetch dependent asset list with dependency type and status and move bundle list with use for planning .
		String  queryForAssets = "SELECT pa.asset_entity_id as assetId, ad.asset_id as assetDepFromId, ad.dependent_id as assetDepToId, " +
				"pa.move_bundle_id as moveBundleId, ad.status as status, ad.type as type, pa.asset_type as assetType " +
				"FROM asset_dependency ad " +
				"JOIN asset_entity pa ON pa.asset_entity_id=ad.asset_id " +
				"WHERE ad.asset_id in ( " + assetIdsSubQuery + "  ) " +
				"AND ad.dependent_id IN ( " + assetIdsSubQuery + " ) " +  // < TM-10261
				filterOptionsCriteria  +
				"UNION " +
				"SELECT sa.asset_entity_id as assetId, ad.asset_id as assetDepFromId, ad.dependent_id as assetDepToId, " +
				"sa.move_bundle_id as moveBundleId, ad.status as status, ad.type as type, sa.asset_type as assetType " +
				"FROM asset_dependency ad " +
				"JOIN asset_entity sa ON sa.asset_entity_id=ad.dependent_id " +
				"WHERE ad.asset_id in ( " + assetIdsSubQuery + "  ) " +
				"AND ad.dependent_id IN ( " + assetIdsSubQuery + " ) " +  // < TM-10261
				filterOptionsCriteria  +
				"ORDER BY assetId DESC  "

		log.info 'SQL used to find assets: {}', queryForAssets
		return jdbcTemplate.queryForList(queryForAssets)
	}

	/**
	 * Clean all dependency groups for the specified project
	 * @param projectId : related project
	 */
	private void cleanDependencyGroupsStatus(projectId) {
		jdbcTemplate.execute("UPDATE asset_entity SET dependency_bundle=0 WHERE project_id = $projectId ")

		// Deleting previously generated dependency bundle table .
		jdbcTemplate.execute("DELETE FROM asset_dependency_bundle where project_id = $projectId")
		// TODO: THIS SHOULD NOT BE NECESSARY GOING FORWARD - THIS COLUMN is being dropped.
		jdbcTemplate.execute("UPDATE asset_entity SET dependency_bundle=NULL WHERE project_id = $projectId")

		// Reset hibernate session since we just cleared out the data directly
		GormUtil.flushAndClearSession()
	}

	/**
	 * Store all groups in the database
	 * @param project : related project
	 * @param groups : groups to be stored
	 */
	private void saveDependencyGroups(Project project, groups, String sqlTime) {
		int groupNum = 0
		for (group in groups) {
			int count = 0
			groupNum++

			def insertSQL = "INSERT INTO asset_dependency_bundle (asset_id, dependency_bundle, dependency_source, last_updated, project_id) VALUES "
			def first = true

			group.each { asset ->
				String dependency_source = (count++ == 0 ? "Initial" : "Dependency")
				if (!first) {
					insertSQL += ","
				}
				insertSQL += "($asset.id,$groupNum,'$dependency_source','$sqlTime',$project.id)"
				first = false
			}

			jdbcTemplate.execute(insertSQL)
		}
	}

	/**
	 * put all the straggler assets that were not grouped into group 0
	 * @param projectId : related project
	 * @param moveBundleText : bundle ids to analyze
	 */
	private void addStragglerDepsToGroupZero(projectId, moveBundleText, sqlTime) {

		def sql = """
			INSERT INTO asset_dependency_bundle (asset_id, dependency_bundle, dependency_source, last_updated, project_id)
			SELECT ae.asset_entity_id, 0, "Straggler", "$sqlTime", ae.project_id
			FROM asset_entity ae
			LEFT OUTER JOIN asset_dependency_bundle adb ON ae.asset_entity_id=adb.asset_id
			WHERE ae.project_id = $projectId # AND ae.dependency_bundle IS NULL
			AND adb.asset_id IS NULL
			AND move_bundle_id in ($moveBundleText)
			AND ae.asset_type in $dependecyBundlingAssetTypesJoined
		"""

		jdbcTemplate.execute(sql)
	}

	private String listAsPipeSeparatedString(list) {
		WebUtil.listAsMultiValueString(list).replaceAll(',',  '|')
	}

	/**
	 * This method retrieves a list of bundles for the given project using a
	 * list of fields and a sorting criteria.
	 *
	 * @param project : project instance
	 * @param projectionFields : this is a list of fields to be included in the projection. If none is
	 *        given the method will default to 'id' and 'name'.
	 * @param orderBy : valid property name to sort on (default 'name')
	 *
	 * @return list of bundles (projected fields only).
	 */
	public List<Map> lookupList(Project project, List projectionFields = null, String orderBy = 'name') {
		if (! projectionFields) {
			projectionFields = ['id', 'name']
		}
		List bundles = MoveBundle.createCriteria().list {
			projections{
				projectionFields.each {
					property(it, it)
				}
			}
			and {
				eq('project', project)
			}
			order(orderBy)
			resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
		}

		return bundles
	}

	/**
	 * Returns the bundle with the name associated to the project. If it not exist, a new bundle is created.
	 * @param bundleName the name of the bundle to check for existence
	 * @return project the project
	 */
	MoveBundle createBundleIfNotExist(String bundleName, Project project) {
		def moveBundle = MoveBundle.findByNameAndProject(bundleName, project)

		if (!moveBundle) {
			moveBundle = new MoveBundle(name:bundleName, operationalOrder:1, workflowCode: project.workflowCode, project: project).save()
		}
		return moveBundle
	}

	/**
	 * Bulk assigns moveBundle, planStatus and tags to assets from the dependency analyzer.
	 *
	 * @param assets The assets to updated.
	 * @param tagIds The tagIds to replace the current tags, is there are any.
	 * @param moveBundle The move bundle to assign to the asset.
	 * @param planStatus the plan status to assign to the asset.
	 * @param currentProject the project of the signed in user.
	 */
	void assignAssets(List<Long> assets, List<Long> tagIds, Long moveBundle, String planStatus, Project currentProject) {
		MoveBundle moveBundleInstance = MoveBundle.get(moveBundle)

		assets.each { Long assetId ->
			AssetEntity asset = get(AssetEntity, assetId, currentProject)
			asset.moveBundle = moveBundleInstance
			asset.planStatus = planStatus
			asset.save()
		}

		BulkChangeTag.validateBulkValues(currentProject, tagIds)
		BulkChangeTag.replace(null, tagIds, 'tagAssets', assets)

		session.ASSIGN_BUNDLE = moveBundle
		session.SELECTED_TAG_IDS = tagIds ?: []
	}

	/**
	 * Find move bundle by id
	 * @param id - move bundle id
	 * @param throwException - whether to throw an exception if move bundle is not found
	 * @return
	 */
	MoveBundle findById(Long id, boolean throwException = false) {
		Project currentProject = securityService.getUserCurrentProject()
		return GormUtil.findInProject(currentProject, MoveBundle, id, throwException)
	}

	/**
	 * Update a move bundle
	 * @param id - move bundle id to update
	 * @param command - move bundle command object
	 * @return
	 */
	MoveBundle update(Long id, MoveBundleCommand command) {
		MoveBundle moveBundle = findById(id, true)
		command.populateDomain(moveBundle, false, ['constraintsMap'])

		moveBundle.save()

		return moveBundle
	}

	/**
	 * Save a new move bundle
	 * @param command - move bundle command object
	 * @return
	 */
	MoveBundle save(MoveBundleCommand command) {
		MoveBundle moveBundle = new MoveBundle()
		command.populateDomain(moveBundle, false, ['constraintsMap'])
		moveBundle.project = securityService.getUserCurrentProject()

		moveBundle.save()

		return moveBundle
	}

    /**
     * Returns the list of Move Bundles (id, name, description) by project.
     * @param project Project to filer the list by.
     * @return List of Move Bundles.
     */
    def moveBundlesByProject(Project project) {
        List<MoveBundle> moveBundles = MoveBundle.findAllByProject(project)
        moveBundles.collect { MoveBundle entry -> [
                id: entry.id,
                name: entry.name,
                description: entry.description,
        ]}
    }
}
