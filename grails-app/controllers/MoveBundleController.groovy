import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.DependencyAnalyzerTabs
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.DependencyConsoleCommand
import net.transitionmanager.command.MoveBundleCommand
import net.transitionmanager.command.bundle.AssetsAssignmentCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveBundleStep
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.StepSnapshot
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CommentService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.MoveBundleService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProgressService
import net.transitionmanager.service.StateEngineService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import org.hibernate.ObjectNotFoundException
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
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
			try{
				moveBundleService.deleteBundleAndAssets(moveBundle)
				flash.message = "MoveBundle $moveBundle deleted"
			}
			catch (e) {
				flash.message = "Unable to Delete MoveBundle and Assets: $e.message"
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
	def update(Long id) {
		// SL : 11-2018 : doing this here to avoid command validation errors, it will go away when front-end correctly
		// implement the way dates are sent to backend
		params.startTime = TimeUtil.parseDateTime(params.startTime) ?: null
		params.completionTime = TimeUtil.parseDateTime(params.completionTime)

		def projectManagerId = params.projectManager
		def moveManagerId = params.moveManager

		Project currentUserProject = controllerService.getProjectForPage(this)
		MoveBundleCommand command = populateCommandObject(MoveBundleCommand)
		command.useForPlanning = params.getBoolean('useForPlanning', false)
		if (params?.moveEvent?.id) {
			command.moveEvent = GormUtil.findInProject(currentUserProject, MoveEvent, params.moveEvent.id as Long, false)
		}
		command.operationalOrder = params.getInt('operationalOrder', 1)
		command.sourceRoom = GormUtil.findInProject(currentUserProject, Room, params.sourceRoom, false)
		command.targetRoom = GormUtil.findInProject(currentUserProject, Room, params.targetRoom, false)

		if (command.validate()) {
			try {
				MoveBundle moveBundle = moveBundleService.update(id, command)

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

				if (errorInSteps) {
					flash.message = "Validation error while adding steps."
					redirect(action: "show", id: moveBundle.id)
					return
				}

				partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundle.id, "MOVE_BUNDLE", projectManagerId, "PROJ_MGR")
				partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundle.id, "MOVE_BUNDLE", moveManagerId, "MOVE_MGR")

				flash.message = "MoveBundle $moveBundle updated"
				redirect(action: "show", id: moveBundle.id)
				return

			} catch (EmptyResultException e) {
				flash.message = "MoveBundle not found with id $params.id"
				redirect(action: 'edit', id: params.id)
			} catch (DomainUpdateException e) {
				flash.message = "Error updating MoveBundle with id $params.id"
			}
		} else {
			flash.message = 'Unable to update MoveBundle due to: ' + GormUtil.allErrorsString(command)
		}

		// in case of error updating move bundle
		//	get the all Dashboard Steps that are associated to moveBundle.project
		def allDashboardSteps = moveBundleService.getAllDashboardSteps(moveBundleService.findById(id))
		def remainingSteps = allDashboardSteps.remainingSteps

		render(view: 'edit',
				model: [moveBundleInstance: command,
						projectId: currentUserProject.id,
						projectManager: projectManagerId,
						managers: partyRelationshipService.getProjectStaff(currentUserProject.id),
						moveManager: moveManagerId,
						rooms: Room.findAllByProject(currentUserProject),
						dashboardSteps: allDashboardSteps.dashboardSteps,
						remainingSteps: remainingSteps,
						workflowCodes: stateEngineService.getWorkflowCode()
				])
	}

	@HasPermission(Permission.BundleCreate)
	def create() {
		Project project = securityService.userCurrentProject
		[moveBundleInstance: new MoveBundle(params), managers: partyRelationshipService.getProjectStaff(project.id),
		 projectInstance: project, workflowCodes: stateEngineService.getWorkflowCode(), rooms: Room.findAllByProject(project)]
	}

	@HasPermission(Permission.BundleCreate)
	def save() {
		// SL : 11-2018 : doing this here to avoid command validation errors, it will go away when front-end correctly
		// implement the way dates are sent to backend
		params.startTime = TimeUtil.parseDateTime(params.startTime) ?: null
		params.completionTime = TimeUtil.parseDateTime(params.completionTime)

		def projectManagerId = params.projectManager
		def moveManagerId = params.moveManager

		Project currentUserProject = controllerService.getProjectForPage(this)
		MoveBundleCommand command = populateCommandObject(MoveBundleCommand)
		command.useForPlanning = params.getBoolean('useForPlanning', false)
		if (params?.moveEvent?.id) {
			command.moveEvent = GormUtil.findInProject(currentUserProject, MoveEvent, params.moveEvent.id as Long, false)
		}
		command.operationalOrder = params.getInt('operationalOrder', 1)
		command.sourceRoom = GormUtil.findInProject(currentUserProject, Room, params.sourceRoom, false)
		command.targetRoom = GormUtil.findInProject(currentUserProject, Room, params.targetRoom, false)

		if (command.validate()) {
			try {
				MoveBundle moveBundle = moveBundleService.save(command)

				if (projectManagerId) {
					partyRelationshipService.savePartyRelationship("PROJ_BUNDLE_STAFF", moveBundle, "MOVE_BUNDLE",
							Party.load(projectManager), "PROJ_MGR")
				}
				if (moveManagerId) {
					partyRelationshipService.savePartyRelationship("PROJ_BUNDLE_STAFF", moveBundle, "MOVE_BUNDLE",
							Party.load(moveManager), "MOVE_MGR")
				}

				flash.message = "MoveBundle $moveBundle created"
				redirect(action: "show", params: [id: moveBundle.id])
				return

			} catch (ServiceException e) {
				flash.message = e.message
			}
		} else {
			flash.message = 'Unable to save MoveBundle due to: ' + GormUtil.allErrorsString(command)
		}

		// in case of error saving new move bundle
		render(view: 'create',
				model: [moveBundleInstance: command,
						moveManager: moveManagerId,
						projectManager: projectManagerId,
						managers: partyRelationshipService.getProjectStaff(currentUserProject.id),
						rooms: Room.findAllByProject(currentUserProject),
						workflowCodes: stateEngineService.getWorkflowCode()
				])
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
		int lockedAppCount = 0

		def basicCountsQuery = """SELECT
				assetClass,
				COUNT(ae) AS all1,
				SUM(CASE WHEN ae.planStatus=:unassignStatus THEN 1 ELSE 0 END)  AS allUnassigned2,
				SUM(CASE WHEN ae.planStatus=:movedStatus THEN 1 ELSE 0 END)     AS allMoveded3,
				SUM(CASE WHEN ae.planStatus=:confirmedStatus THEN 1 ELSE 0 END) AS allConfirmed4,
				SUM(CASE WHEN ae.planStatus=:lockedStatus THEN 1 ELSE 0 END) AS allLocked5
			FROM AssetEntity ae
			WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)
			GROUP BY ae.assetClass"""

		def basicCountsParams = [
			project: project,
			moveBundles: moveBundleList,
			unassignStatus: AssetEntityPlanStatus.UNASSIGNED,
			movedStatus: AssetEntityPlanStatus.MOVED,
			confirmedStatus: AssetEntityPlanStatus.CONFIRMED,
			lockedStatus: AssetEntityPlanStatus.LOCKED]

		def basicCountsResults = AssetEntity.executeQuery(basicCountsQuery, basicCountsParams)
		basicCountsResults.each { ua ->
			switch(ua[0]) {
				case AssetClass.APPLICATION:
					applicationCount = ua[1]
					unassignedAppCount = ua[2]
					assignedAppCount = applicationCount - unassignedAppCount
					movedAppCount = ua[3]
					lockedAppCount = ua[5]
					confirmedAppCount = movedAppCount + ua[4] + lockedAppCount
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

		def groupingSumQuery = "SELECT new map(${customField} as key, COUNT(ae) as count) FROM Application ae WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)"

		if (customField) {
			groupingSumQuery += " group by ${customField}"
		}
		def groupValues = Application.executeQuery(groupingSumQuery, [project:project, moveBundles:moveBundleList])

		def groupPlanMethodologyCount = groupValues.inject([:]) { groups, it ->
			def key = it.key
			if(!key) key = Application.UNKNOWN

			if(!groups[key]) groups[key] = 0

			groups[key] += it.count

			groups
		}

		// sort values based on custom field setting configuration
		def customFieldSetting = customDomainService.findCustomField(project, AssetClass.APPLICATION.toString()) {
			it.field == customField
		}

		if (customFieldSetting?.constraints?.values) {
			def sortedMap = customFieldSetting.constraints.values.inject([:]) { result, it ->
				if ( ! it ) {
					result[Application.UNKNOWN] = 0
				} else if (groupPlanMethodologyCount[it]) {
					result[it] = 0
				}
				result
			}
			groupPlanMethodologyCount = sortedMap + groupPlanMethodologyCount;
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
			(total > 0 ? ((count/total*100)as double).trunc(2).intValue() : 0)
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
				log.error 'Database inconsistency: {}', e.message, e
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
		def unknown = Application.executeQuery(appValidateCountQuery, countArgs+[validation: ValidationType.UNKNOWN])[0]
		def planReady = Application.executeQuery(appValidateCountQuery, countArgs+[validation: ValidationType.PLAN_READY])[0]

		countArgs.validation = ValidationType.UNKNOWN
		def appToValidate = Application.executeQuery(appValidateCountQuery, countArgs)[0]
		def dbToValidate = Database.executeQuery(dbValidateCountQuery, countArgs)[0]
		def fileToValidate = Files.executeQuery(filesValidateCountQuery, countArgs)[0]
		def phyStorageToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.storageTypes])[0]
		def psToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.physicalServerTypes])[0]
		def vsToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.virtualServerTypes])[0]

		def otherValidateQuery = countQuery + validationQuery + " AND COALESCE(ae.assetType,'') NOT IN (:type) AND ae.assetClass=:assetClass"
		def otherToValidate = AssetEntity.executeQuery(otherValidateQuery, countArgs+[assetClass:AssetClass.DEVICE, type:AssetType.nonOtherTypes])[0]

		def percentageAppToValidate = applicationCount ? percOfCount(appToValidate, applicationCount) : 100
		def percentagePlanReady = applicationCount ? percOfCount(planReady, applicationCount) : 0
		def percentagePSToValidate= totalPhysicalServerCount ? percOfCount(psToValidate, totalPhysicalServerCount) :100
		def percentageVMToValidate= totalVirtualServerCount ? percOfCount(vsToValidate, totalVirtualServerCount) : 100
		def percentageDBToValidate= databaseCount ? percOfCount(dbToValidate, databaseCount) :100
		def percentageStorToValidate=phyStorageCount ? percOfCount(phyStorageToValidate, phyStorageCount) :100
		int percentageFilesCount = percOfCount(fileToValidate, fileCount)
		def percentageOtherToValidate= otherAssetCount ? percOfCount(otherToValidate, otherAssetCount) :100
		def percentageUnassignedAppCount = applicationCount ? percOfCount(unassignedAppCount, applicationCount) :100

		// Query to obtain the count of Servers in 'Moved' Plan Status
		def serversCountsQuery = """SELECT
				assetClass,
				COUNT(ae) AS all,
				SUM(CASE WHEN ae.planStatus=:movedStatus THEN 1 ELSE 0 END) AS allMoved
			FROM AssetEntity ae
			WHERE ae.project=:project
			AND ae.assetClass = :deviceAssetClass
			AND ae.assetType IN (:allServers)
			AND ae.moveBundle IN (:moveBundles)
			GROUP BY ae.assetClass"""

		def serversCountsQueryParams = [
				project: project,
				moveBundles: moveBundleList,
				movedStatus: AssetEntityPlanStatus.MOVED,
				deviceAssetClass: AssetClass.DEVICE,
				allServers: AssetType.allServerTypes]

		def serversCompletedPercentage = 0
		def serversCountsQueryResults = AssetEntity.executeQuery(serversCountsQuery, serversCountsQueryParams)
		// Make sure this does not return null while getting [0] element.
		if (serversCountsQueryResults.size() > 0) {
			serversCountsQueryResults = serversCountsQueryResults[0]
			def totalServersCount = serversCountsQueryResults[1].intValue()
			// Make sure to prevent Division by zero error while calling countAppPercentage method.
			if (totalServersCount > 0) {
				serversCompletedPercentage = countAppPercentage(totalServersCount, serversCountsQueryResults[2].intValue())
			}
		}

		return [
			appList                       : appList,
			applicationCount              : applicationCount,
			unassignedAppCount            : unassignedAppCount,
			assignedAppPerc               : assignedAppPerc,
			confirmedAppPerc              : confirmedAppPerc,
			movedAppPerc                  : movedAppPerc,
			movedServersPerc              : serversCompletedPercentage,
			appToValidate                 : appToValidate,
			unassignedServerCount         : unassignedServerCount,
			unassignedPhysicalServerCount : unassignedPhysicalServerCount,
			percentagePhysicalServerCount : percentagePhysicalServerCount,
			psToValidate                  : psToValidate,
			unassignedVirtualServerCount  : unassignedVirtualServerCount,
			percVirtualServerCount        : percVirtualServerCount,
			vsToValidate                  : vsToValidate,
			dbList                        : dbList,
			dbCount                       : databaseCount,
			unassignedDbCount             : unassignedDbCount,
			percentageDBCount             : percentageDBCount,
			dbToValidate                  : dbToValidate,
			// Files (aka Storage)
			filesList                     : filesList,
			fileCount                     : fileCount,
			unassignedFilesCount          : unassignedFilesCount,
			percentageFilesCount          : percentageFilesCount,
			fileToValidate                : fileToValidate,
			unAssignedPhyStorageCount     : unAssignedPhyStorageCount,
			phyStorageCount               : phyStorageCount,
			phyStorageList                : phyStorageList,
			phyStorageToValidate          : phyStorageToValidate,
			percentagePhyStorageCount     : percentagePhyStorageCount,
			// Other
			otherTypeList                 : otherTypeList,
			otherAssetCount               : otherAssetCount,
			unassignedOtherCount          : unassignedOtherCount,
			percentageOtherCount          : percentageOtherCount,
			otherToValidate               : otherToValidate,

			// assetList:assetList, assetCount:assetCount,
			totalServerCount              : totalServerCount,
			allServerList                 : allServerList,
			phyServerCount                : totalPhysicalServerCount,
			phyServerList                 : phyServerList,
			virtServerCount               : totalVirtualServerCount,
			virtServerList                : virtServerList,

			unassignedAssetCount          : unassignedAssetCount,
/*
			likelyLatency:likelyLatency, likelyLatencyCount:likelyLatencyCount,
			unknownLatency:unknownLatency, unknownLatencyCount:unknownLatencyCount,
			unlikelyLatency:unlikelyLatency, unlikelyLatencyCount:unlikelyLatencyCount,
*/
			appDependenciesCount          : appDependenciesCount,
			pendingAppDependenciesCount   : pendingAppDependenciesCount,
			serverDependenciesCount       : serverDependenciesCount,
			pendingServerDependenciesCount: pendingServerDependenciesCount,

			project                       : project,
			moveEventList                 : moveEventList,
			moveBundleList                : moveBundleList,
			dependencyConsoleList         : dependencyConsoleList,
			dependencyBundleCount         : dependencyBundleCount,
			planningDashboard             : 'planningDashboard',
			eventStartDate                : eventStartDate,
			date                          : time,

			issuesCount                   : issues.size(),
			openIssue                     : openIssue,
			dueOpenIssue                  : dueOpenIssue,
			openTasks                     : openTasks,
			generalOverDue                : generalOverDue,

			validated                     : applicationCount - unknown,
			planReady                     : planReady,
			movedAppCount                 : movedAppCount,
			assignedAppCount              : assignedAppCount,
			confirmedAppCount             : confirmedAppCount,
			percAppDoneCount              : percAppDoneCount,
			percentageAppToValidate       : percentageAppToValidate,
			percentagePlanReady           : percentagePlanReady,

			percentagePSToValidate        : percentagePSToValidate,
			percentageVMToValidate        : percentageVMToValidate,
			percentageDBToValidate        : percentageDBToValidate,
			percentageStorToValidate      : percentageStorToValidate,
			percentageOtherToValidate     : percentageOtherToValidate,
			percentageUnassignedAppCount  : percentageUnassignedAppCount,

			groupPlanMethodologyCount     : groupPlanMethodologyCount
		]
	}

	/**
	 * Control function to render the Dependency Analyzer (was Dependency Console)
	 * @param  Console command object that contains bundle, tagIds, tagMatch, assinedGroup, subsection, groupId, assetName
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def dependencyConsole() {
		DependencyConsoleCommand console = populateCommandObject(DependencyConsoleCommand)
		validateCommandObject(console)
		licenseAdminService.checkValidForLicenseOrThrowException()
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		// Check for correct URL params
		if ( console.subsection || console.groupId) { // is the drill-in URL
			if (!(console.subsection && console.groupId)) { // If any exists, then both should be present in the URL
				throw new InvalidParamException("Subsection and Group Id params are both required.")
			}
			String subsec = console.subsection as String // Check for valid tab name
			if (!(subsec.toUpperCase() in (DependencyAnalyzerTabs.values() as String[]))) {
				throw new InvalidParamException("Invalid Subsection name: ${subsec}")
			}
		}
		//Date start = new Date()
		userPreferenceService.setPreference(PREF.ASSIGNED_GROUP,
			console.assignedGroup ?: userPreferenceService.getPreference(PREF.ASSIGNED_GROUP) ?: "1")

		def map = moveBundleService.dependencyConsoleMap(
			project,
			console.bundle,
			console.tagIds,
			console.tagMatch,
			console.assignedGroup,
			null,
			false,
			console.subsection,
			console.groupId,
			console.assetName
		)

		//log.info 'dependencyConsole() : moveBundleService.dependencyConsoleMap() took {}', TimeUtil.elapsed(start)
		return map
	}

	/**
	 * Controller to render the Dependency Bundle Details Table.
	 * This method is called from the Dependency Analyzer in an Ajax call to render the dependency table.
	 * @param  Console command object that contains bundle, tagIds, tagMatch, assinedGroup, subsection, groupId, assetName
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def dependencyBundleDetails() {
		DependencyConsoleCommand console = populateCommandObject(DependencyConsoleCommand)
		validateCommandObject(console)
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		userPreferenceService.setPreference(PREF.ASSIGNED_GROUP,
				console.assignedGroup ?: userPreferenceService.getPreference(PREF.ASSIGNED_GROUP) ?: "1")
		def model = moveBundleService.dependencyConsoleMap(
				project,
				console.bundle,
				console.tagIds,
				console.tagMatch,
				console.assignedGroup,
				null,
				false,
				console.subsection,
				console.groupId,
				console.assetName
		)
		render(template: 'dependencyBundleDetails',
		       model: model)
	}

	@HasPermission(Permission.DepAnalyzerGenerate)
	def generateDependency() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def baseName = "generateDependency"
		def key = baseName + "-" + UUID.randomUUID()
		progressService.create(key)

		def jobName = "TM-" + baseName + "-" + project.id
		log.info 'Initiate Generate Dependency'

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
			log.warn 'generateDependency failed to create Quartz job : {}', ex.message
			progressService.update(key, 100I, ProgressService.FAILED,
				'It appears that someone else is currently generating dependency groups for this project. Please try again later.')
		}

		renderSuccessJson(key: key)
	}

	/**
	 * Assigns one or more assets to a specified bundle, and add tags
	 */
	@HasPermission(Permission.AssetEdit)
	def assetsAssignment() {
		AssetsAssignmentCommand assetsAssignment = populateCommandObject(AssetsAssignmentCommand)
		validateCommandObject(assetsAssignment)

		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		moveBundleService.assignAssets(assetsAssignment.assets, assetsAssignment.tagIds,assetsAssignment.moveBundle, assetsAssignment.planStatus, project)

		forward(controller: "assetEntity", action: "retrieveLists",
			     params: [entity: params.assetType, dependencyBundle: session.getAttribute('dependencyBundle')])
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
