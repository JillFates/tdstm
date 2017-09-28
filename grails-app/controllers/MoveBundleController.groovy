import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.WorkflowTransitionId
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveBundleStep
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.StepSnapshot
import net.transitionmanager.domain.Workflow
import net.transitionmanager.domain.WorkflowTransition
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CommentService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.MoveBundleService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProgressService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.StateEngineService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import org.hibernate.ObjectNotFoundException
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
@Slf4j(value='logger', category='grails.app.controllers.MoveBundleController')
class MoveBundleController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	CommentService commentService
	ControllerService controllerService
	JdbcTemplate jdbcTemplate
	MoveBundleService moveBundleService
	PartyRelationshipService partyRelationshipService
	ProgressService progressService
	Scheduler quartzScheduler
	SecurityService securityService
	StateEngineService stateEngineService
	TaskService taskService
	UserPreferenceService userPreferenceService
    CustomDomainService customDomainService

	@HasPermission(Permission.BundleView)
	def list() {}

	/**
	 * Used to generate the List for Bundles using jqgrid.
	 * @return : list of bundles as JSON
	 */
	@HasPermission(Permission.BundleView)
	def listJson() {
		String sortOrder  = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		Project project = securityService.userCurrentProject

		def startDates = params.startTime ? MoveBundle.executeQuery('''
			from MoveBundle where project =:project and cast(startTime as string) like :startTime
			''', [project: project, startTime: '%' + params.startTime.trim() + '%'])?.startTime : []

		def completionDates = !params.completionTime ? [] :
				MoveBundle.executeQuery('''
					select completionTime from MoveBundle
					where project=:project
					and str(completionTime) like :completionTime''',
					[project: project, completionTime: '%' + params.completionTime + '%'])
	}

	/**
	 * Generates the List for Bundles using Kendo Grid.
	 * @return : list of bundles as JSON
	 */
	@HasPermission(Permission.BundleView)
	def retrieveBundleList() {
		List<MoveBundle> bundleList = MoveBundle.findAllByProject(securityService.loadUserCurrentProject())
		renderAsJson bundleList.collect { MoveBundle entry -> [
			bundleId: entry.id,
			name: entry.name,
			description: entry.description,
			planning: entry.useForPlanning,
			assetqty: entry.assetQty,
			startDate: entry.startTime,
			completion: entry.completionTime
		]}
	}

	@HasPermission(Permission.BundleView)
	def show() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		MoveBundle moveBundle = controllerService.getBundleForPage(this, project, params.id ?: userPreferenceService.moveBundleId)
		if (!moveBundle) {
			redirect(action: 'list')
			return
		}

		userPreferenceService.setMoveBundleId(moveBundle.id)
		def projectManager = partyRelationshipService.getPartyToRelationship("PROJ_BUNDLE_STAFF", moveBundle, "MOVE_BUNDLE", "PROJ_MGR")
		def moveManager = partyRelationshipService.getPartyToRelationship("PROJ_BUNDLE_STAFF", moveBundle, "MOVE_BUNDLE", "MOVE_MGR")

		// get the list of Manual Dashboard Steps that are associated to moveBundle.project
		def moveBundleSteps = MoveBundleStep.executeQuery(
				'FROM MoveBundleStep mbs WHERE mbs.moveBundle = :mb ORDER BY mbs.transitionId',
				[mb: moveBundle])
		def dashboardSteps = []

		moveBundleSteps.each {
			def stepSnapshot = StepSnapshot.executeQuery(
					'FROM StepSnapshot WHERE moveBundleStep=:msb ORDER BY dateCreated DESC',
					[msb: it], [max: 1])[0]
			dashboardSteps << [moveBundleStep : it, stepSnapshot: stepSnapshot]
		}

		[moveBundleInstance: moveBundle, projectId: project.id, projectManager: projectManager, moveManager: moveManager,
		 dashboardSteps: dashboardSteps, isDefaultBundle: moveBundle.id == project.defaultBundle.id]
	}

	@HasPermission(Permission.BundleDelete)
	def delete() {
		String message = moveBundleService.deleteBundle(MoveBundle.get(params.id),
			securityService.loadUserCurrentProject())
		flash.message = message
		redirect(action: 'list')
	}

	@HasPermission(Permission.BundleDelete)
	def deleteBundleAndAssets() {
		MoveBundle moveBundle = MoveBundle.get(params.id)
		if (moveBundle) {
			AssetEntity.withTransaction { status ->
				try{
					moveBundleService.deleteBundleAssetsAndAssociates(moveBundle)
					moveBundleService.deleteMoveBundleAssociates(moveBundle)
					moveBundle.delete()
					flash.message = "MoveBundle $moveBundle deleted"
				}
				catch (e) {
					status.setRollbackOnly()
					flash.message = "Unable to Delete MoveBundle Assosiated with Teams: $e.message"
				}
			}
		}
		else {
			flash.message = "MoveBundle not found with id $params.id"
		}

		redirect(action: 'list')
	}

	@HasPermission(Permission.BundleEdit)
	def edit() {
		MoveBundle moveBundle = MoveBundle.get(params.id)
		if (!moveBundle) {
			flash.message = "MoveBundle not found with id $params.id"
			redirect(action: 'list')
			return
		}

		stateEngineService.loadWorkflowTransitionsIntoMap(moveBundle.workflowCode, 'project')
		Project project = securityService.userCurrentProject
		def managers = partyRelationshipService.getProjectStaff(project.id)
		def projectManager = partyRelationshipService.getPartyToRelationship("PROJ_BUNDLE_STAFF", moveBundle, "MOVE_BUNDLE", "PROJ_MGR")
		def moveManager = partyRelationshipService.getPartyToRelationship("PROJ_BUNDLE_STAFF", moveBundle, "MOVE_BUNDLE", "MOVE_MGR")

		//get the all Dashboard Steps that are associated to moveBundle.project
		def allDashboardSteps = moveBundleService.getAllDashboardSteps(moveBundle)

		[moveBundleInstance: moveBundle, projectId: project.id, managers: managers,
		 projectManager: projectManager?.partyIdToId, moveManager: moveManager?.partyIdToId,
		 dashboardSteps: allDashboardSteps.dashboardSteps?.sort{it["step"].id},
		 remainingSteps: allDashboardSteps.remainingSteps, workflowCodes: stateEngineService.getWorkflowCode(),
		 rooms: Room.findAllByProject(project)]
	}

	@HasPermission(Permission.BundleEdit)
	def update() {

		// TODO : Security : Get User's project and attempt to find the project before blindly updating it

		def moveBundle = MoveBundle.get(params.id)
		if (!moveBundle) {
			flash.message = "MoveBundle not found with id $params.id"
			redirect(action: 'edit', id: params.id)
			return
		}

		def projectManagerId = params.projectManager
		def moveManagerId = params.moveManager

		moveBundle.name = params.name
		moveBundle.description = params.description
		moveBundle.workflowCode = params.workflowCode
		moveBundle.useForPlanning = params.useForPlanning==null? false: params.useForPlanning as Boolean
		if (params.moveEvent.id) {
			moveBundle.moveEvent = MoveEvent.get(params.moveEvent.id)
		} else {
			moveBundle.moveEvent = null
		}
		moveBundle.operationalOrder = params.operationalOrder ? Integer.parseInt(params.operationalOrder) : 1
		def completionTime = params.completionTime

		moveBundle.startTime = TimeUtil.parseDateTime(params.startTime) ?: null

		moveBundle.completionTime = TimeUtil.parseDateTime(completionTime)

		// TODO : SECURITY : Should be confirming that the rooms belong to the moveBundle.project instead of blindly assigning plus should be
		// validating that the rooms even exist.
		moveBundle.sourceRoom = Room.read(params.sourceRoom)
		moveBundle.targetRoom = Room.read(params.targetRoom)

		if (moveBundle.save()) {
			stateEngineService.loadWorkflowTransitionsIntoMap(moveBundle.workflowCode, 'project')
			boolean errorInSteps = false
			stateEngineService.getDashboardSteps(moveBundle.workflowCode).each {
				def checkbox = params["checkbox_" + it.id]
				if (checkbox == 'on') {
					MoveBundleStep step = moveBundleService.createMoveBundleStep(moveBundle, it.id, params)
					if (step.hasErrors()) {
						errorInSteps = true
						return
					}
				} else {
					def moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId(moveBundle, it.id)
					if (moveBundleStep) {
						moveBundleService.deleteMoveBundleStep(moveBundleStep)
					}
				}
			}

			if(errorInSteps){
				flash.message = "Validation error while adding steps."
				redirect(action: "show", id: moveBundle.id)
				return
			}

			//def projectManeger = Party.get(projectManagerId)
			partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundle.id, "MOVE_BUNDLE", projectManagerId, "PROJ_MGR")
			partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundle.id, "MOVE_BUNDLE", moveManagerId, "MOVE_MGR")
			flash.message = "MoveBundle $moveBundle updated"
			//redirect(action:"show",params:[id:moveBundle.id, projectId:projectId])
			redirect(action: "show", id: moveBundle.id)
			return
		}

		//	get the all Dashboard Steps that are associated to moveBundle.project
		def allDashboardSteps = moveBundleService.getAllDashboardSteps(moveBundle)
		def remainingSteps = allDashboardSteps.remainingSteps

		moveBundle.discard()

		String projectId = securityService.userCurrentProjectId

		render(view: 'edit',
		       model: [moveBundleInstance: moveBundle, projectId: projectId, projectManager: projectManagerId,
		               managers: partyRelationshipService.getProjectStaff(projectId),
		               moveManager: moveManagerId, rooms: Room.findAllByProject(Project.load(projectId)),
		               dashboardSteps: allDashboardSteps.dashboardSteps, remainingSteps: remainingSteps,
		               workflowCodes: stateEngineService.getWorkflowCode()])
	}

	@HasPermission(Permission.BundleCreate)
	def create() {
		Project project = securityService.userCurrentProject
		[moveBundleInstance: new MoveBundle(params), managers: partyRelationshipService.getProjectStaff(project.id),
		 projectInstance: project, workflowCodes: stateEngineService.getWorkflowCode(), rooms: Room.findAllByProject(project)]
	}

	@HasPermission(Permission.BundleCreate)
	def save() {

		def startTime = params.startTime
		def completionTime = params.completionTime
		if (startTime){
			params.startTime = TimeUtil.parseDateTime(startTime)
		}
		if (completionTime){
			params.completionTime = TimeUtil.parseDateTime(completionTime)
		}

		params.sourceRoom = Room.read(params.sourceRoom)
		params.targetRoom = Room.read(params.targetRoom)

		MoveBundle moveBundle = new MoveBundle(params)
		Project project = securityService.userCurrentProject
		def projectManager = params.projectManager
		def moveManager = params.moveManager

		moveBundle.useForPlanning = params.useForPlanning == 'true'

		if (!moveBundle.hasErrors() && moveBundle.save()) {
			if (projectManager){
				partyRelationshipService.savePartyRelationship("PROJ_BUNDLE_STAFF", moveBundle, "MOVE_BUNDLE",
						Party.load(projectManager), "PROJ_MGR")
			}
			if (moveManager) {
				partyRelationshipService.savePartyRelationship("PROJ_BUNDLE_STAFF", moveBundle, "MOVE_BUNDLE",
						Party.load(moveManager), "MOVE_MGR")
			}

			flash.message = "MoveBundle $moveBundle created"
			redirect(action: "show", params: [id: moveBundle.id])
			return
		}

		render(view: 'create',
		       model: [moveBundleInstance: moveBundle, moveManager: moveManager, projectManager: projectManager,
		               managers: partyRelationshipService.getProjectStaff(project.id), rooms: Room.findAllByProject(project),
		               workflowCodes: stateEngineService.getWorkflowCode()])
	}

	/**
	 * If the checkbox is subsequently checked and the form submitted, a new MoveBundleStep shall be created for that transition.
	 * @return  new moveBundleStep
	 */
	@HasPermission(Permission.BundleEdit)
	MoveBundleStep createMoveBundleStep() {
		def moveBundle = MoveBundle.get(params.moveBundleId)
		int transitionId = params.int('transitionId')
		MoveBundleStep moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId(moveBundle, transitionId)
		if (!moveBundleStep) {
			moveBundleStep = new MoveBundleStep(moveBundle: moveBundle, transitionId: transitionId, calcMethod: 'L',
			                                    label: stateEngineService.getDashboardLabel(moveBundle.workflowCode, transitionId))
			if (!save(moveBundleStep)) {
				response.sendError(500, "Validation Error")
				return
			}
		}
		render moveBundleStep
	}

	/*-----------------------------------------------------
	 * remote function to verify stepSnapshot records for a list of steps.
	 * if there are more than one snapshots associated with any of the step in list
	 * then return failure otherwise success.
	 * @param  : moveBundleId, list of unchecked steps
	 * @return : success / failure
	 *---------------------------------------------------*/
	@HasPermission(Permission.BundleView)
	def checkStepSnapshotRecord() {
		def steps = params.steps
		MoveBundle moveBundle = MoveBundle.get(params.moveBundleId)
		def transitionIds
		def message = "success"
		if (steps){
			transitionIds = steps.split(",")
		}
		for (transitionId in transitionIds) {
			def moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId(moveBundle, transitionId)
			if (moveBundleStep) {
				int stepSnapshotCount = StepSnapshot.countByMoveBundleStep(moveBundleStep)
				if (stepSnapshotCount > 1) {
					message = "failure"
					break
				}
			}
		}
		render message
	}

	@HasPermission(Permission.BundleView)
	def projectMoveBundles() {
		Project project = securityService.loadUserCurrentProject()
		def moveBundlesList
		if (project) {
			moveBundlesList = MoveBundle.findAllByProject(project, [sort: 'name', order: 'asc'])
		}
		render moveBundlesList as JSON
	}

//	@HasPermission(Permission.MoveDashboardView)
	@HasPermission(Permission.DashboardMenuView)
	def planningStats() {
		Project project = securityService.userCurrentProject

		// Get list of all of the MoveBundles that are used for Planning
		def moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project, true, [sort: 'startTime'])

		// Nothing to report if there are no planning bundles so we'll just return with zeros for everything
		if (!moveBundleList) {
			render(view: 'planningStats_NoBundles')
			return
		}

		def appList = []
		def eventStartDate = [:]
		def movedPlan = AssetEntityPlanStatus.MOVED
		def app = AssetType.APPLICATION.toString()
		def server = AssetType.SERVER.toString()
		def vm = AssetType.VM.toString()
		def blade = AssetType.BLADE.toString()

		// Get the list of Move Events and sort on the start date
		List<MoveEvent> moveEventList = moveBundleList*.moveEvent.unique()
		moveEventList.remove(null)
		def getEventStartDate = {
			Date start = it.estStartTime
			if(!start){
				start = it.getEventTimes().start
			}
			return start
		}
		moveEventList = moveEventList.sort {a,b ->
			getEventStartDate(a) <=> getEventStartDate(b)
		}

		// Forming query for multi-uses
		def baseWhere = 'WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)'
		def selectCount = 'SELECT count(ae)'
		def countArgs = [project:project, moveBundles:moveBundleList]

		def countQuery = "$selectCount FROM AssetEntity ae $baseWhere"
		def appQuery = "FROM Application ae $baseWhere"
		def appCountQuery = "$selectCount $appQuery"
		def dbQuery = "FROM Database ae $baseWhere"
		def dbCountQuery = "$selectCount $dbQuery"
		def filesQuery = "FROM Files ae $baseWhere"
		def filesCountQuery = "$selectCount $filesQuery"
		def phyStorageQuery = "FROM AssetEntity ae $baseWhere AND ae.assetClass=:assetClass AND ae.assetType IN (:type)"
		def phyStorageCountQuery = "$selectCount $phyStorageQuery"
		def deviceQuery = "FROM AssetEntity ae $baseWhere AND ae.assetClass=:assetClass AND ae.assetType IN (:type)"
		def deviceCountQuery = "$selectCount $deviceQuery"
		def otherCountQuery = "$selectCount FROM AssetEntity ae $baseWhere AND ae.assetClass=:assetClass AND COALESCE(ae.assetType,'') NOT IN (:type)"

		def databaseCount = Database.executeQuery(dbCountQuery, countArgs)[0]
		def fileCount = Files.executeQuery(filesCountQuery, countArgs)[0]

 		// Get the list of apps and servers assigned to planning bundles
		def applicationsOfPlanningBundle = Application.findAll(appQuery, countArgs)
		def appDependenciesCount = applicationsOfPlanningBundle ? AssetDependency.countByAssetInList(applicationsOfPlanningBundle) : 0
		def serversOfPlanningBundle = AssetEntity.findAll(deviceQuery, countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.allServerTypes])
		def serverDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByAssetInList(serversOfPlanningBundle) : 0

		def assignedApplicationCount
		def allServerList = []
		def phyServerList = []
		def virtServerList = []
		def dbList = []
		def phyStorageList = []
		def filesList = []
		def otherTypeList = []
		def openTasks = []

		moveEventList.each { moveEvent->
			// fetching bundles for current moveEvent which was set 'true' for useForPlanning
			def moveBundles = moveEvent.moveBundles?.findAll {it.useForPlanning}
			def eventWiseArgs = [project:project, moveBundles:moveBundles]

			Date startDate = moveEvent.estStartTime
			if(!startDate){
				def eventDates = moveEvent.getEventTimes()
				startDate = eventDates.start
			}
			eventStartDate[moveEvent.id] = TimeUtil.formatDateTime(startDate, TimeUtil.FORMAT_DATE_TIME_7)

			// Fetching application count that are assigned to current move event
			assignedApplicationCount = moveBundles ? Application.executeQuery(appCountQuery, eventWiseArgs)[0] : 0
			appList << [count: assignedApplicationCount , moveEvent: moveEvent.id]

			// fetching physicalAsset (e.g. 'Server',blade) and virtualAsset (e.g. 'VM') count that are assigned to current move-event .
			int physicalAssetCount = moveBundles ? AssetEntity.executeQuery(deviceCountQuery,
					eventWiseArgs + [assetClass: AssetClass.DEVICE, type: AssetType.physicalServerTypes])[0] : 0
			int virtualAssetCount = moveBundles ? AssetEntity.executeQuery(deviceCountQuery, eventWiseArgs +
					[assetClass: AssetClass.DEVICE, type: AssetType.virtualServerTypes])[0] : 0
			int serverCnt = moveBundles ? AssetEntity.executeQuery(deviceCountQuery, eventWiseArgs +
					[assetClass: AssetClass.DEVICE, type: AssetType.allServerTypes])[0] : 0

			allServerList << [moveEvent: moveEvent.id , count: serverCnt]
			phyServerList << [moveEvent: moveEvent.id , count: physicalAssetCount]
			virtServerList << [moveEvent: moveEvent.id , count: virtualAssetCount]

			def dbCount = moveBundles ? Database.executeQuery(dbCountQuery, eventWiseArgs)[0] : 0
			dbList << [moveEvent:moveEvent.id , count:dbCount]

			int phyStoragesCount = moveBundles ? AssetEntity.executeQuery(phyStorageCountQuery, eventWiseArgs +
					[assetClass:AssetClass.DEVICE, type:AssetType.storageTypes])[0] : 0
			phyStorageList << [moveEvent: moveEvent.id , count: phyStoragesCount]

			def filesCount = moveBundles ? Files.executeQuery(filesCountQuery, eventWiseArgs)[0] : 0
			filesList << [moveEvent: moveEvent.id , count: filesCount]

			def otherCount = moveBundles ? AssetEntity.executeQuery(otherCountQuery,
				eventWiseArgs + [assetClass:AssetClass.DEVICE, type:AssetType.nonOtherTypes])[0] : 0
			otherTypeList << [moveEvent: moveEvent.id, count: otherCount]

			Long openIssueCount = AssetComment.executeQuery('''
					select count(*)
					FROM AssetComment where project=:project
					 and commentType=:type
					 and status IN (:status)
					 and moveEvent=:event
					 AND isPublished=true
			''', [project: project, type: AssetCommentType.TASK, event: moveEvent,
			      status: [AssetCommentStatus.READY, AssetCommentStatus.STARTED, AssetCommentStatus.PENDING]])[0]

			openTasks << [moveEvent: moveEvent.id , count: openIssueCount]
		}

		// ----------------------------------------------------------------------------
		// Get the totals count for assigned and unassigned assets
		// ----------------------------------------------------------------------------

		// Find Move bundles that are not assigned to a Event and is the bundle is used for planning
		def unassignedMoveBundles = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.moveEvent IS NULL \
			AND mb.useForPlanning = true AND mb.project = :project ", [project:project])
		def assetTypeQuery = 'AND ae.assetType IN (:type)'

		// Construct the query that will include counts of non-event bundles if any exist just the bundle being NULL
		def assetCountQueryArgs = [project:project]
		def unassignedMBQuery = 'ae.moveBundle IS NULL'
		if (unassignedMoveBundles) {
			 unassignedMBQuery = "(ae.moveBundle IN (:unassignedMoveBundles) OR $unassignedMBQuery)"
			 assetCountQueryArgs.unassignedMoveBundles = unassignedMoveBundles
		}

		// Get majority of the unassigned counts
		int unassignedDbCount = 0
		int unassignedFilesCount = 0
		int unassignedAppCount = 0
		int unassignedDeviceCount = 0
		int applicationCount = 0
		int movedAppCount = 0
		int confirmedAppCount = 0
		int assignedAppCount = 0

		def basicCountsQuery = """SELECT
				assetClass,
				COUNT(ae) AS all1,
				SUM(CASE WHEN ae.planStatus=:unassignStatus THEN 1 ELSE 0 END)  AS allUnassigned2,
				SUM(CASE WHEN ae.planStatus=:movedStatus THEN 1 ELSE 0 END)     AS allMoveded3,
				SUM(CASE WHEN ae.planStatus=:confirmedStatus THEN 1 ELSE 0 END) AS allConfirmed4
			FROM AssetEntity ae
			WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)
			GROUP BY ae.assetClass"""

		def basicCountsParams = [
			project: project,
			moveBundles: moveBundleList,
			unassignStatus: AssetEntityPlanStatus.UNASSIGNED,
			movedStatus: AssetEntityPlanStatus.MOVED,
			confirmedStatus: AssetEntityPlanStatus.CONFIRMED]

		def basicCountsResults = AssetEntity.executeQuery(basicCountsQuery, basicCountsParams)
		basicCountsResults.each { ua ->
			switch(ua[0]) {
				case AssetClass.APPLICATION:
					applicationCount = ua[1]
					unassignedAppCount = ua[2]
					assignedAppCount = applicationCount - unassignedAppCount
					movedAppCount = ua[3]
					confirmedAppCount = movedAppCount + ua[4]
					break
				case AssetClass.DATABASE:
					unassignedDbCount = ua[2]; break
				case AssetClass.DEVICE:
					unassignedDeviceCount = ua[2]; break
				case AssetClass.STORAGE:
					unassignedFilesCount = ua[2]; break
			}
		}
		// Calculate the Assigned Application Count
		def unassignedAssetCount = unassignedDbCount + unassignedFilesCount + unassignedAppCount + unassignedDeviceCount

		// Get the various DEVICE types broken out
		def deviceMetricsQuery = """SELECT
			COUNT(ae) AS allDevices,
			COALESCE(SUM(CASE WHEN ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS allDevicesUnassigned,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:phyTypes) THEN 1 ELSE 0 END), 0) AS physicalAll,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:phyTypes) AND ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS physicalUnassigned,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:virtTypes) THEN 1 ELSE 0 END), 0) AS virtualAll,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:virtTypes) AND ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS virtualUnassigned,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:storageTypes) THEN 1 ELSE 0 END), 0) AS storageAll,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:storageTypes) AND ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS storageUnassigned,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:networkTypes) THEN 1 ELSE 0 END), 0) AS networkAll,
			COALESCE(SUM(CASE WHEN ae.assetType IN (:networkTypes) AND ae.planStatus=:unassignStatus THEN 1 ELSE 0 END), 0) AS networkUnassigned
			FROM AssetEntity ae
			WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles) AND ae.assetClass=:assetClass"""

		def deviceMetricsParams = [
			project:project,
			moveBundles:moveBundleList,
			unassignStatus: AssetEntityPlanStatus.UNASSIGNED,
			assetClass: AssetClass.DEVICE,
			phyTypes: AssetType.physicalServerTypes,
			virtTypes: AssetType.virtualServerTypes,
			storageTypes: AssetType.storageTypes,
			networkTypes: AssetType.networkDeviceTypes
		]
		def deviceMetrics = AssetEntity.executeQuery(deviceMetricsQuery, deviceMetricsParams)[0]

		// Assign all the results into variables by ordinal position
		def (totalDeviceCount, unassignedAllDeviceCount, totalPhysicalServerCount, unassignedPhysicalServerCount,
			totalVirtualServerCount, unassignedVirtualServerCount, phyStorageCount, unAssignedPhyStorageCount,
			phyNetworkCount, unAssignedPhyNetworkCount) = deviceMetrics

		// Computed values from previous gathered data points
		def unassignedServerCount = unassignedPhysicalServerCount + unassignedVirtualServerCount
		def totalServerCount = totalPhysicalServerCount + totalVirtualServerCount
		// def otherAssetCount = totalDeviceCount - totalServerCount - phyStorageCount - phyNetworkCount
		// TODO : JPM 12/2015 TM-4332 : We're including the Network Devices in the Other count for the time being
		def otherAssetCount = totalDeviceCount - totalServerCount - phyStorageCount
		def unassignedOtherCount = unassignedAllDeviceCount - unassignedPhysicalServerCount - unassignedVirtualServerCount -
				unAssignedPhyStorageCount

		// Application Plan Methodology
		def customField = project.planMethodology ?: "''"

		def groupingSumQuery = "SELECT new map(${customField} as key, COUNT(ae) as count) FROM Application ae WHERE ae.project=:project"

		if (customField) {
			groupingSumQuery += " group by ${customField}"
		}
		def groupValues = Application.executeQuery(groupingSumQuery, [project:project])

        println(groupingSumQuery)
        println(groupValues)

        def customFieldSetting = customDomainService.findCustomField(project, AssetClass.APPLICATION.toString()) {
            it.field == customField
        }

        /*
        * SELECT count(*)
FROM asset_entity ae
-- JOIN application app on app.app_id = ae.application
WHERE ae.project_id = 2445
AND ae.asset_type = 'Application'
AND ae.custom2 is null
-- AND ae. = 'one'
;

SELECT ae.custom2, count(ae.custom2)
FROM asset_entity ae
-- JOIN application app on app.app_id = ae.application
WHERE ae.project_id = 2445
AND ae.asset_type = 'Application'
AND ae.custom2 is not null
group by ae.custom2
-- AND ae. = 'one'
;
        * */
        println(customFieldSetting)

		def groupPlanMethodologyCount = groupValues.inject([:]) { groups, it ->
			def key = it.key
			if(!key) key = Application.UNKNOWN

			if(!groups[key]) groups[key] = 0

			groups[key] += it.count

			groups
		}

/*
		// TODO - this is unnecessary and could just load the map

		def latencyQuery = "SELECT COUNT(ae) FROM Application ae WHERE ae.project=:project AND ae.latency=:latency"
		def likelyLatency = Application.executeQuery(latencyQuery, [project:project, latency:'N'])[0]
		def unlikelyLatency = Application.executeQuery(latencyQuery, [project:project, latency:'Y'])[0]
		def unknownLatency = applicationCount - likelyLatency - unlikelyLatency
*/

		// ------------------------------------
		// Calculate the Plan Status values
		// ------------------------------------
		def assignedAppPerc = countAppPercentage(applicationCount, assignedAppCount)
		def confirmedAppPerc = countAppPercentage(applicationCount, confirmedAppCount)
		def movedAppPerc = countAppPercentage(applicationCount, movedAppCount)
		def percAppDoneCount = countAppPercentage(applicationCount, movedAppCount)

		int percentagePhysicalServerCount = moveBundleList ? AssetEntity.executeQuery(deviceCountQuery + " AND ae.planStatus='$movedPlan'",
			countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.physicalServerTypes])[0] : 0

		// Quick closure for calculating the percentage below
		def percOfCount = { count, total ->
			(total > 0 ? Math.round(count/total*100)  : 0)
		}

		def planStatusMovedQuery = " AND ae.planStatus='$movedPlan'"
		int percVirtualServerCount = moveBundleList ?
			AssetEntity.executeQuery(deviceCountQuery + planStatusMovedQuery, countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.virtualServerTypes])[0]
			: 0

		percentagePhysicalServerCount = percOfCount(percentagePhysicalServerCount, totalPhysicalServerCount)
		percVirtualServerCount = percOfCount(percVirtualServerCount, totalVirtualServerCount)

		int percentageDBCount = moveBundleList ? Database.executeQuery(dbCountQuery + planStatusMovedQuery, countArgs)[0] : 0

		percentageDBCount = percOfCount(percentageDBCount, databaseCount)

		int percentagePhyStorageCount = moveBundleList ? AssetEntity.executeQuery(deviceCountQuery + " AND ae.planStatus='$movedPlan'",
			countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.storageTypes])[0] : 0

		percentagePhyStorageCount = percOfCount(percentagePhyStorageCount, phyStorageCount)

		int percentageFilesCount = moveBundleList ? Files.executeQuery(filesCountQuery + planStatusMovedQuery, countArgs)[0] : 0
		percentageFilesCount = percOfCount(percentageFilesCount, fileCount)

		int percentageOtherCount = moveBundleList ? AssetEntity.executeQuery(otherCountQuery + planStatusMovedQuery,
			countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.allServerTypes])[0] : 0
		percentageOtherCount = percOfCount(percentageOtherCount, otherAssetCount)
/*
		def likelyLatencyCount=0
		def unlikelyLatencyCount=0
		def unknownLatencyCount=0
*/
		def pendingAppDependenciesCount = applicationsOfPlanningBundle ?
			AssetDependency.countByAssetInListAndStatusInList(applicationsOfPlanningBundle,['Unknown','Questioned']) : 0

		def pendingServerDependenciesCount = serversOfPlanningBundle ?
		AssetDependency.countByAssetInListAndStatusInList(serversOfPlanningBundle,['Unknown','Questioned']) : 0


		def assetDependencyList = jdbcTemplate.queryForList("select dependency_bundle as dependencyBundle from  asset_dependency_bundle \
			where project_id = $project.id  group by dependency_bundle order by dependency_bundle  limit 48")

		String time
		def date = AssetDependencyBundle.findByProject(project,[sort:"lastUpdated",order:"desc"])?.lastUpdated
		time = TimeUtil.formatDateTime(date, TimeUtil.FORMAT_DATE_TIME_8)

		def today = new Date()
		def issueQuery = "from AssetComment a  where a.project =:project and a.category in (:category) and a.status != :status and a.commentType =:type AND a.isPublished = true"
		def issueArgs = [project:project, status:AssetCommentStatus.COMPLETED, type:AssetCommentType.TASK.toString()]

		def openIssue =  AssetComment.findAll(issueQuery,issueArgs + [category : AssetComment.discoveryCategories]).size()
		def dueOpenIssue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs + [category : AssetComment.discoveryCategories, dueDate:today]).size()
		def issues = AssetComment.findAll("FROM AssetComment a where a.project = :project and a.commentType = :type and a.status =:status  \
			and a.category in (:category) AND a.isPublished = true",[project:project, type:AssetCommentType.TASK, status: AssetCommentStatus.READY , category: AssetComment.planningCategories])
		def generalOverDue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs + [category: AssetComment.planningCategories, dueDate:today]).size()

		def dependencyConsoleList = []
		assetDependencyList.each { dependencyBundle ->
			def assetDependentlist = AssetDependencyBundle.findAllByDependencyBundleAndProject(dependencyBundle.dependencyBundle, project)
			def appCount = 0
			def serverCount = 0
			def vmCount = 0
			try {
				appCount = assetDependentlist.findAll { it.asset.assetType == app }.size()
				// TODO : JPM 9/2014 - serverCount should be using the AssetType method since it is not looking at all of the correct assetTypes
				serverCount = assetDependentlist.findAll{ it.asset.assetType in [server, blade] }.size()
				vmCount = assetDependentlist.findAll{ it.asset.assetType == vm }.size()
			} catch (ObjectNotFoundException e) {
				logger.error 'Database inconsistency: {}', e.message, e
			}

			dependencyConsoleList << [dependencyBundle: dependencyBundle.dependencyBundle, appCount: appCount,
			                          serverCount: serverCount, vmCount: vmCount]
		}

		int dependencyBundleCount = jdbcTemplate.queryForObject('''
			select count(distinct dependency_bundle) from asset_dependency_bundle
			where project_id=?''', Integer, project.id)

		// Remove the param 'type' that was used for a while above
		countArgs.remove('type')

		def validationQuery = ' AND ae.validation=:validation'
		def validateCountQuery = countQuery + validationQuery + ' AND ae.assetType IN (:type) AND ae.assetClass=:assetClass'
		def appValidateCountQuery = appCountQuery + validationQuery
		def dbValidateCountQuery = dbCountQuery + validationQuery
		def filesValidateCountQuery = filesCountQuery + validationQuery

		// This section could be consolidated to a simple query instead of a bunch
		def dependencyScan = Application.executeQuery(appValidateCountQuery, countArgs+[validation:'DependencyScan'])[0]
		def validated = Application.executeQuery(appValidateCountQuery, countArgs+[validation:'Validated'])[0]
		def dependencyReview = Application.executeQuery(appValidateCountQuery, countArgs+[validation:'DependencyReview'])[0]
		def bundleReady = Application.executeQuery(appValidateCountQuery, countArgs+[validation:'BundleReady'])[0]

		countArgs.validation = 'Discovery'
		def appToValidate = Application.executeQuery(appValidateCountQuery, countArgs)[0]
		def dbToValidate = Database.executeQuery(dbValidateCountQuery, countArgs)[0]
		def fileToValidate = Files.executeQuery(filesValidateCountQuery, countArgs)[0]
		def phyStorageToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.storageTypes])[0]
		def psToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.physicalServerTypes])[0]
		def vsToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.virtualServerTypes])[0]

		def otherValidateQuery = countQuery + validationQuery + " AND COALESCE(ae.assetType,'') NOT IN (:type) AND ae.assetClass=:assetClass"
		def otherToValidate = AssetEntity.executeQuery(otherValidateQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.nonOtherTypes])[0]

		def percentageAppToValidate = applicationCount ? percOfCount(appToValidate, applicationCount) : 100
		def percentageBundleReady = applicationCount ? percOfCount(bundleReady, applicationCount) : 0
		def percentagePSToValidate= totalPhysicalServerCount ? percOfCount(psToValidate, totalPhysicalServerCount) :100
		def percentageVMToValidate= totalVirtualServerCount ? percOfCount(vsToValidate, totalVirtualServerCount) : 100
		def percentageDBToValidate= databaseCount ? percOfCount(dbToValidate, databaseCount) :100
		def percentageStorToValidate=fileCount ? percOfCount(fileToValidate, fileCount) :100
		def percentageOtherToValidate= otherAssetCount ? percOfCount(otherToValidate, otherAssetCount) :100
		def percentageUnassignedAppCount = applicationCount ? percOfCount(unassignedAppCount, applicationCount) :100

		return [
			appList:appList,
			applicationCount:applicationCount,
			unassignedAppCount:unassignedAppCount,
			assignedAppPerc: assignedAppPerc,
			confirmedAppPerc: confirmedAppPerc,
			movedAppPerc: movedAppPerc,
			appToValidate:appToValidate,
			unassignedServerCount: unassignedServerCount,
			unassignedPhysicalServerCount:unassignedPhysicalServerCount,
			percentagePhysicalServerCount:percentagePhysicalServerCount,
			psToValidate:psToValidate,
			unassignedVirtualServerCount:unassignedVirtualServerCount,
			percVirtualServerCount:percVirtualServerCount,
			vsToValidate:vsToValidate,
			dbList:dbList, dbCount:databaseCount,
			unassignedDbCount:unassignedDbCount,
			percentageDBCount:percentageDBCount,
			dbToValidate:dbToValidate,
			// Files (aka Storage)
			filesList:filesList, fileCount:fileCount,
			unassignedFilesCount:unassignedFilesCount,
			percentageFilesCount:percentageFilesCount,
			fileToValidate:fileToValidate,
			unAssignedPhyStorageCount:unAssignedPhyStorageCount,
			phyStorageCount:phyStorageCount,
			phyStorageList:phyStorageList,
			phyStorageToValidate:phyStorageToValidate,
			percentagePhyStorageCount:percentagePhyStorageCount,
			// Other
			otherTypeList:otherTypeList,
			otherAssetCount:otherAssetCount,
			unassignedOtherCount:unassignedOtherCount,
			percentageOtherCount:percentageOtherCount,
			otherToValidate:otherToValidate,

			// assetList:assetList, assetCount:assetCount,
			totalServerCount: totalServerCount,
			allServerList: allServerList,
			phyServerCount:totalPhysicalServerCount,
			phyServerList: phyServerList,
			virtServerCount:totalVirtualServerCount,
			virtServerList: virtServerList,

			unassignedAssetCount:unassignedAssetCount,
/*
			likelyLatency:likelyLatency, likelyLatencyCount:likelyLatencyCount,
			unknownLatency:unknownLatency, unknownLatencyCount:unknownLatencyCount,
			unlikelyLatency:unlikelyLatency, unlikelyLatencyCount:unlikelyLatencyCount,
*/
			appDependenciesCount:appDependenciesCount, pendingAppDependenciesCount:pendingAppDependenciesCount,
			serverDependenciesCount:serverDependenciesCount, pendingServerDependenciesCount:pendingServerDependenciesCount,

			project:project,
			moveEventList:moveEventList,
			moveBundleList:moveBundleList,
			dependencyConsoleList:dependencyConsoleList,
			dependencyBundleCount:dependencyBundleCount,
			planningDashboard:'planningDashboard',
			eventStartDate:eventStartDate,
			date:time,

			issuesCount:issues.size(),
			openIssue:openIssue, dueOpenIssue:dueOpenIssue,
			openTasks:openTasks, generalOverDue:generalOverDue,

			dependencyScan:dependencyScan, dependencyReview:dependencyReview, validated:validated, bundleReady:bundleReady,
			movedAppCount:movedAppCount, assignedAppCount:assignedAppCount, confirmedAppCount:confirmedAppCount,
			percAppDoneCount:percAppDoneCount, percentageAppToValidate:percentageAppToValidate,
			percentageBundleReady:percentageBundleReady,

			percentagePSToValidate:percentagePSToValidate,
			percentageVMToValidate:percentageVMToValidate,
			percentageDBToValidate:percentageDBToValidate,
			percentageStorToValidate:percentageStorToValidate,
			percentageOtherToValidate:percentageOtherToValidate,
			percentageUnassignedAppCount:percentageUnassignedAppCount,

			groupPlanMethodologyCount: groupPlanMethodologyCount
		]
	}

	/**
	 * Control function to render the Dependency Analyzer (was Dependency Console)
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def dependencyConsole() {
		licenseAdminService.checkValidForLicenseOrThrowException()
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		//Date start = new Date()
		userPreferenceService.setPreference(PREF.ASSIGNED_GROUP,
			params.assinedGroup ?: userPreferenceService.getPreference(PREF.ASSIGNED_GROUP) ?: "1")
		def map = moveBundleService.dependencyConsoleMap(project, params.bundle, params.assinedGroup, null)

		//logger.info 'dependencyConsole() : moveBundleService.dependencyConsoleMap() took {}', TimeUtil.elapsed(start)
		return map
	}

	/*
	 * Controller to render the Dependency Bundle Details
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def dependencyBundleDetails() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		// Now get the model and display results
		def isAssigned = userPreferenceService.getPreference(PREF.ASSIGNED_GROUP)?: "1"
		render(template: 'dependencyBundleDetails',
		       model: moveBundleService.dependencyConsoleMap(project, params.bundle, isAssigned, null))
	}

	@HasPermission(Permission.DepAnalyzerGenerate)
	def generateDependency() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def baseName = "generateDependency"
		def key = baseName + "-" + UUID.randomUUID()
		progressService.create(key)

		def jobName = "TM-" + baseName + "-" + project.id
		logger.info 'Initiate Generate Dependency'

		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl(jobName, null, new Date(System.currentTimeMillis() + 2000))
		trigger.jobDataMap.putAll(params)

		String connectionTypes = WebUtil.checkboxParamAsString(request.getParameterValues("connection"))
		String statusTypes = WebUtil.checkboxParamAsString(request.getParameterValues("status"))
		def isChecked = params.saveDefault

		trigger.jobDataMap.key = key
		trigger.jobDataMap.username = securityService.currentUsername
		trigger.jobDataMap.projectId = project.id
		trigger.jobDataMap.connectionTypes = connectionTypes
		trigger.jobDataMap.statusTypes = statusTypes
		trigger.jobDataMap.isChecked = isChecked
		trigger.jobDataMap.userLoginName = securityService.userLoginPerson.toString()

		trigger.setJobName('GenerateDependencyGroupsJob')
		trigger.setJobGroup('tdstm-dependency-groups')
		try{
			quartzScheduler.scheduleJob(trigger)
			progressService.update(key, 1, 'In progress')
		}catch(ex){
			logger.warn 'generateDependency failed to create Quartz job : {}', ex.message
			progressService.update(key, 100I, ProgressService.FAILED,
				'It appears that someone else is currently generating dependency groups for this project. Please try again later.')
		}

		renderSuccessJson(key: key)
	}

	/**
	 * Assigns one or more assets to a specified bundle
	 */
	@HasPermission(Permission.AssetEdit)
	def saveAssetsToBundle() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def assetArray = params.assetVal
		def moveBundleInstance = MoveBundle.get(params.moveBundle)
		session.ASSIGN_BUNDLE = params.moveBundle
		def assetList = assetArray.split(",")
		assetList.each {assetId ->
			def assetInstance = AssetEntity.get(assetId)
			assetInstance.moveBundle = moveBundleInstance
			assetInstance.planStatus = params.planStatus
			saveWithWarnings assetInstance
		}

		forward(controller: "assetEntity", action: "retrieveLists",
			     params: [entity: params.assetType, dependencyBundle: session.getAttribute('dependencyBundle')])
	}

	/**
	 * To generate Moveday Tasks for a specified Bundle
	 * @param - bundleId - Id of the Bundle to generate the tasks for that bundle
	 * @return - error Message - if any else success Message
	 * TODO -- THIS METHOD CAN BE REMOVED AS IT IS NOT USED ANY MORE
	 */
	@HasPermission(Permission.TaskCreate)
	def createTask() {

		def bundleId = params.bundleId
		def bundle = MoveBundle.get(bundleId)
		def errMsg = ""

		if (bundle.getAssetQty() == 0) {
			errMsg = "No assets are assigned to current bundle ($bundle.name). As such no action was taken."
		} else {

			def bundleMoveEvent = bundle.moveEvent
			Project project = securityService.userCurrentProject
			Person person = securityService.loadCurrentPerson()

			// Get last task # used
			int lastTask = jdbcTemplate.queryForObject('SELECT MAX(task_number) FROM asset_comment WHERE project_id=?', Integer, project.id)

			// Create the Begin Event Task
			def commentToBegin = new AssetComment(
				taskNumber:++lastTask,
				comment:'Begin Event',
			    moveEvent:bundleMoveEvent,
				category:AssetCommentCategory.SHUTDOWN,
				Status: AssetCommentStatus.PENDING,
				commentType:AssetCommentType.TASK,
				project:project,
				estStart:bundle.startTime, estFinish:bundle.completionTime, duration:0,
				createdBy:person, assignedTo:person, role:'PROJ_MGR',
				autoGenerated:true
			)
			saveWithWarnings commentToBegin, true
			if (commentToBegin.hasErrors()) {
				errMsg = "Failed to create Begin Event Task. Process Failed"
			}
			if (!errMsg) {
				def commentToComplete = new AssetComment(
					taskNumber:++lastTask,
					comment:'Event Completed',
					project:project,
				    moveEvent:bundleMoveEvent,
					commentType:AssetCommentType.TASK,
					category:AssetCommentCategory.STARTUP,
					Status:AssetCommentStatus.PENDING,
					duration:0,
					assignedTo:person, role:'PROJ_MGR',
					createdBy:person,
					autoGenerated:true
				)
				saveWithWarnings commentToComplete
				if (commentToComplete.hasErrors()) {
					errMsg = "Failed to create Event Completed Task. Process Failed"
				}
                if (!errMsg) {
                    def bundledAssets = AssetEntity.findAll("from AssetEntity a where a.moveBundle = :bundle and a.project =:project\
						and a.assetType not in('application', 'database', 'files', 'VM')", [bundle:bundle,project:project])
                    def bundleworkFlow = bundle.workflowCode
                    def workFlow = Workflow.findByProcess(bundleworkFlow)
					// Find all sets that can be applied to servers
                    def workFlowSteps = WorkflowTransition.findAllByWorkflow(workFlow,[sort:'transId'])
                    workFlowSteps = workFlowSteps.findAll{
	  					! [WorkflowTransitionId.HOLD, WorkflowTransitionId.READY, WorkflowTransitionId.COMPLETED, WorkflowTransitionId.TERMINATED
						].contains(it.transId)}

					// Create the end of transit task that will be used for Off-Truck tasks afterward
                    def results = taskService.createTaskBasedOnWorkflow(
	   					taskNumber: ++lastTask, workflow: workFlowSteps.find { it.transId == WorkflowTransitionId.TRANSPORT },
	   					bundleMoveEvent: bundleMoveEvent, project: project, person: person, bundle: bundle)
                    def transportTask = results.stepTask

					def previousTask

					// Iterate over each Asset and create the various work flow steps
                    bundledAssets.each {asset ->
                        workFlowSteps.eachWithIndex{ workflow, index->
                            if (workflow.transId != WorkflowTransitionId.TRANSPORT) {
                                results = taskService.createTaskBasedOnWorkflow(
									[	taskNumber:++lastTask,
										workflow:workflow,
										bundleMoveEvent:bundleMoveEvent, assetEntity:asset,
                                    	project:project, person:person, bundle:bundle
									])
                                def stepTask = results.stepTask
                                errMsg = results.errMsg
                                if (index==0) {
									// Create dependency on Begin Move task
                                    commentService.saveAndUpdateTaskDependency(stepTask, commentToBegin, null, null)
                                } else if (index==workFlowSteps.size()-1) {
									// Create task dependency on previous task and Completed task
                                    commentService.saveAndUpdateTaskDependency(commentToComplete, stepTask, null, null)
									commentService.saveAndUpdateTaskDependency(stepTask, previousTask, null, null)
                                } else if (previousTask){
									// Create task dependency on previous task
									commentService.saveAndUpdateTaskDependency(stepTask, previousTask, null, null)
								}
								previousTask = stepTask
                            } else {
								// Create dependency on Transport Ending step
                                commentService.saveAndUpdateTaskDependency(transportTask, previousTask, null, null)
                                previousTask = transportTask
                            }
                        }
                    }
                }
				if (!errMsg) {
					bundle.tasksCreated = true
					saveWithWarnings bundle
					if (bundle.hasErrors()) {
						errMsg = 'An unexpected error occurred while updated bundle'
					}
				}
			}
		}

		render errMsg ?: "Generated Tasks for Bundle - $bundle.name successfully."
	}

	/**
	 * Calculates percentage of Filtered Apps on Total Planned apps.
	 * @param totalAppCount : Total count of Application that is in Planned Bundle
	 * @param filteredAppCount : This is filtered app based on PlanStatus
	 * @return : Percentage of Calculated app
	 */
	@HasPermission(Permission.AssetView)
	def countAppPercentage(int totalAppCount, int filteredAppCount) {
		return totalAppCount ? Math.round((filteredAppCount /  totalAppCount) * 100) : 0
	}

	/**
	 * Sets compactControl preference
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def setCompactControlPref() {
		String key = params.prefFor
		String selected = params.selected
		if (selected) {
			userPreferenceService.setPreference(key, selected)
			session.setAttribute(key,selected)
		}
		render selected
	}
}
