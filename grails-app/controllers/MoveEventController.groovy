import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventSnapshot
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.MoveBundleService
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.ReportsService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.hibernate.criterion.Order
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.Timestamp

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
@Slf4j(value='logger', category='grails.app.controllers.MoveEventController')
class MoveEventController implements ControllerMethods, PaginationMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	ControllerService controllerService
	JdbcTemplate jdbcTemplate
	MoveBundleService moveBundleService
	MoveEventService moveEventService
	ProjectService projectService
	ReportsService reportsService
	TaskService taskService
	UserPreferenceService userPreferenceService

	private static final List<String> preMoveCols = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment', 'assignedTo',
	                                                 'status', 'estStart', '', '', 'notes', 'duration', 'estStart',
	                                                 'estFinish', 'actStart', 'actFinish', 'workflow']
	private static final List<String> serverCols = ['id', 'application', 'assetName', '','serialNumber', 'assetTag',
	                                                'manufacturer', 'model', 'assetType', '', '', '']
	private static final List<String> scheduleCols = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment', 'role',
	                                                  'assignedTo', 'instructionsLink' , '', 'duration', 'estStart',
	                                                  'estFinish', 'actStart', 'actFinish', 'workflow']
	private static final List<String> postMoveCols = ['taskNumber', 'assetEntity', 'comment','assignedTo', 'status',
	                                                  'estFinish', 'dateResolved' , 'notes', 'taskDependencies', 'duration',
	                                                  'estStart', 'estFinish', 'actStart', 'actFinish', 'workflow']
	private static final List<String> impactedCols = ['id', 'assetName', '', 'startupProc', 'description',
	                                                  'sme', '' ,'' ,'' ,'' ,'' ,'' ]
	private static final List<String> dbCols = ['id', 'assetName', 'dbFormat', 'size', 'description', 'supportType',
	                                            'retireDate', 'maintExpDate', 'environment', 'ipAddress', 'planStatus',
	                                            'custom1', 'custom2', 'custom3', 'custom4', 'custom5', 'custom6',
	                                            'custom7', 'custom8']
	private static final List<String> fileCols = ['id', 'assetName', 'fileFormat', 'size', 'description', 'supportType',
	                                              'retireDate', 'maintExpDate', 'environment', 'ipAddress', 'planStatus',
	                                              'custom1', 'custom2', 'custom3', 'custom4', 'custom5', 'custom6',
	                                              'custom7', 'custom8']

	private static final List<String> otherCols = ['id', 'application', 'assetName', 'shortName', 'serialNumber',
	                                               'assetTag', 'manufacturer', 'model', 'assetType', 'ipAddress', 'os',
	                                               'sourceLocationName', 'sourceRoomName', 'sourceRackName', 'sourceRackPosition',
	                                               'sourceChassis', 'sourceBladePosition', 'targetLocationName', 'targetRoomName',
	                                               'targetRackName', 'targetRackPosition', 'targetChassis',
	                                               'targetBladePosition', 'custom1', 'custom2', 'custom3', 'custom4',
	                                               'custom5', 'custom6', 'custom7', 'custom8', 'moveBundle', 'truck',
	                                               'cart', 'shelf', 'railType', 'priority', 'planStatus', 'usize']

	private static final List<String> unresolvedCols = ['id', 'comment', 'commentType', 'commentAssetEntity',
	                                                    'resolution', 'resolvedBy', 'createdBy', 'dueDate', 'assignedTo',
	                                                    'category', 'dateCreated', 'dateResolved', 'assignedTo', 'status',
	                                                    'taskDependencies', 'duration', 'estStart', 'estFinish',
	                                                    'actStart', 'actFinish', 'workflow']

	@HasPermission(Permission.EventView)
	def list() {}

	@HasPermission(Permission.EventView)
	def listJson() {
		Integer maxRows = paginationMaxRowValue('rows', null, false)
		Integer currentPage = paginationPage()
		Integer rowOffset = paginationRowOffset(currentPage, maxRows)
		String sortIndex = paginationOrderBy(MoveEvent, 'sidx', 'name')
		String sortOrder = paginationSortOrder('sord')

		List<MoveEvent> events = MoveEvent.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", securityService.loadUserCurrentProject())
			if (params.name) {
				ilike('name', SqlUtil.formatForLike(params.name.trim()))
			}
			if (params.description) {
				ilike('description', SqlUtil.formatForLike(params.description.trim()))
			}
			if (params.runbookStatus) {
				ilike('runbookStatus', SqlUtil.formatForLike(params.runbookStatus.trim()))
			}
			def newsBM = retrieveNewsBMList(params.newsBarMode)
			if (newsBM) {
				'in'('newsBarMode', newsBM)
			}
			order(sortOrder == 'DESC' ? Order.desc(sortIndex).ignoreCase() : Order.asc(sortIndex).ignoreCase())
		}

		int totalRows = events.totalCount
		int numberOfPages = Math.ceil(totalRows / maxRows)

		def results = events.collect {
			[cell: [it.name, it.estStartTime, it.estCompletionTime, it.description, message(code: 'event.newsBarMode.' + it.newsBarMode),
			        it.runbookStatus, it.moveBundlesString], id: it.id]
		}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}

	@HasPermission(Permission.EventView)
	def show() {
		String moveEventId = params.id
		if (moveEventId) {
			userPreferenceService.setPreference(PREF.MOVE_EVENT, moveEventId)
			def moveBundleId = userPreferenceService.moveBundleId
			if (moveBundleId) {
				def moveBundle = MoveBundle.get(moveBundleId)
				if (moveBundle?.moveEvent?.id != Integer.parseInt(moveEventId)) {
					userPreferenceService.removePreference(PREF.CURR_BUNDLE)
				}
			}
		}
		else {
			moveEventId = userPreferenceService.getPreference(PREF.MOVE_EVENT)
		}

		if (!moveEventId) {
			redirect(action: 'list')
			return
		}

		MoveEvent moveEvent = MoveEvent.get(moveEventId)
		if (!moveEvent) {
			flash.message = "MoveEvent not found with id $moveEventId"
			redirect(action: 'list')
			return
		}

		[moveEventInstance: moveEvent]
	}

	/**
	 * redirect to list once selected record deleted
	 * @param : MoveEvent Id
	 * @return : list of remaining MoveEvents
	 */
	@HasPermission(Permission.EventDelete)
	def delete() {
		try {
			MoveEvent moveEvent = MoveEvent.get(params.id)
			if (moveEvent) {
				long moveEventId = moveEvent.id
				String moveEventName = moveEvent.name
				moveEventService.deleteMoveEvent(moveEvent)
				flash.message = "MoveEvent $moveEventName deleted"
			}
			else {
				flash.message = "MoveEvent not found with id $params.id"
        	}
		}
		catch (e) {
			log.error(e.message, e)
			flash.message = e
		}
		redirect(action: 'list')
	}

	@HasPermission(Permission.EventEdit)
	def edit() {
		MoveEvent moveEvent = MoveEvent.get(params.id)
		if (!moveEvent) {
			flash.message = "MoveEvent not found with id $params.id"
			redirect(action: 'list')
			return
		}

		[moveEventInstance: moveEvent, moveBundles: MoveBundle.findAllByProject(moveEvent.project)]
	}

	@HasPermission(Permission.EventEdit)
	def update() {
		CreateEventCommand event = populateCommandObject(CreateEventCommand)

		MoveEvent moveEvent = MoveEvent.get(params.id)
		if (!moveEvent) {
			flash.message = "MoveEvent not found with id $params.id"
			redirect(action: 'edit', id: params.id)
			return
		}

		moveEvent.properties = event

		if (!moveEvent.hasErrors() && moveEvent.save()) {
			moveBundleService.assignMoveEvent(moveEvent, request.getParameterValues('moveBundle') as List)
			flash.message = "MoveEvent '$moveEvent.name' updated"
			redirect(action: 'show', id: moveEvent.id)
		}
		else {
			render(view: 'edit', model: [moveEventInstance: moveEvent])
		}
	}

	@HasPermission(Permission.EventCreate)
	def create() {
		Project project = securityService.userCurrentProject
		List bundles = moveBundleService.lookupList(project)
		[moveEventInstance: new MoveEvent(params), bundles: bundles]
	}

	@HasPermission(Permission.EventCreate)
	def save() {
		CreateEventCommand event = populateCommandObject(CreateEventCommand)
		Project currentProject = securityService.userCurrentProject

		MoveEvent moveEvent = moveEventService.save(event, currentProject)

		if (!moveEvent.hasErrors()) {
			flash.message = "MoveEvent $moveEvent.name created"
			redirect(action: "show", id: moveEvent.id)
		}
		else {
			render(view: 'create', model: [moveEventInstance: moveEvent])
		}
	}

	/**
	 * Return the list of active news for a selected moveEvent and status of that evnt.
	 * @param id - the moveEvent to get the news for
	 */
	@HasPermission(Permission.EventView)
	def retrieveMoveEventNewsAndStatus() {

		// Make sure that the user is trying to access a valid event
		Project project = controllerService.getProjectForPage(this)
		if (! project) {
			// TODO - switch to getProjectAndUserForWS when available to avoid the flash.message
			flash.message = ''
			renderWarningJson("User presently has no selected project")
			return
		}
		def moveEvent = controllerService.getEventForPage(this, project, params.id)
		if (!moveEvent) {
			// TODO - switch to getEventForWS when available to avoid the flash.message
			flash.message = ''
			renderWarningJson("Event id was not found")
			return
		}

		def statusAndNewsList = []
		if (moveEvent) {
			def moveEventNewsQuery = """
				SELECT mn.date_created as created, mn.message as message from move_event_news mn
				left join move_event me on (me.move_event_id = mn.move_event_id)
				left join project p on (p.project_id = me.project_id)
				where mn.is_archived = 0 and mn.move_event_id = ${moveEvent.id} and p.project_id = ${moveEvent.project.id} order by created desc
			"""

			def moveEventNews = jdbcTemplate.queryForList(moveEventNewsQuery)

			def news = new StringBuilder()

			moveEventNews.each {
				news.append(String.valueOf(TimeUtil.formatDateTime(it.created) + "&nbsp;:&nbsp;" + it.message + ".&nbsp;&nbsp;"))
			}

			// append recent tasks  whose status is completed, moveEvent is newsBarMode
			def transitionComment = new StringBuilder()
			if (moveEvent.newsBarMode == "on") {
				def today = new Date()
				def currentPoolTime = new Timestamp(today.getTime())
				def tasksCompList = jdbcTemplate.queryForList("""
					SELECT comment, date_resolved AS dateResolved FROM asset_comment
					WHERE project_id=?
					  AND move_event_id=?
					  AND status='Completed'
					  AND (date_resolved BETWEEN SUBTIME(?, '00:15:00') AND ?)
				""", moveEvent.project.id, moveEvent.id, currentPoolTime, currentPoolTime)
				tasksCompList.each {
					transitionComment << it.comment << ":&nbsp;&nbsp;" << TimeUtil.formatDateTime(it.dateResolved) << ".&nbsp;&nbsp;"
				}
			}

			def moveEventSnapshot = MoveEventSnapshot.executeQuery('''
				FROM MoveEventSnapshot WHERE moveEvent=? AND type=?
				ORDER BY dateCreated DESC
			''', [moveEvent , "P"])[0]
			def cssClass = "statusbar_good"
			def status = "GREEN"
			def dialInd = moveEventSnapshot?.dialIndicator
			dialInd = dialInd || dialInd == 0 ? dialInd : 100
			if (dialInd < 25) {
				cssClass = "statusbar_bad"
				status = "RED"
			} else if (dialInd >= 25 && dialInd < 50) {
				cssClass = "statusbar_yellow"
				status = "YELLOW"
			}
			statusAndNewsList << [news: news.toString() + "<span style='font-weight:normal'>" + transitionComment + "</span>",
				cssClass: cssClass, status: status]

		}
		render statusAndNewsList as JSON
	}

	/*
	 * will update the moveEvent calcMethod = M and create a MoveEventSnapshot for summary dialIndicatorValue
	 * @author : Lokanada Reddy
	 * @param  : moveEventId and moveEvent dialIndicatorValue
	 */
	@HasPermission(Permission.EventEdit)
	def updateEventSumamry() {
		MoveEvent moveEvent = MoveEvent.get(params.moveEventId)
		def dialIndicator
		if (params.checkbox == 'true') {
			dialIndicator = params.value
		}
		if (dialIndicator  || dialIndicator == 0) {
			MoveEventSnapshot moveEventSnapshot = new MoveEventSnapshot(moveEvent: moveEvent, planDelta: 0,
				dialIndicator: dialIndicator, type: 'P')
			saveWithWarnings moveEventSnapshot
			if (moveEventSnapshot.hasErrors()) {
				moveEvent.calcMethod = MoveEvent.METHOD_MANUAL
			}
			else {
				moveEvent.calcMethod = MoveEvent.METHOD_LINEAR
			}

			saveWithWarnings moveEvent
			render "success"
		}
	}

	/**
	 * The front-end UI to exporting a Runbook spreadsheet
	 */
	@HasPermission(Permission.TaskView)
	def exportRunbook() {
		Project project = controllerService.getProjectForPage(this, 'to view Export Runbook')
		if (!project) return

		[moveEventList: MoveEvent.findAllByProject(project),
		 viewUnpublished: securityService.viewUnpublished() ? '1' : '0']
	}

	/**
	 * This provides runbookStats that is rendered into a window of the runbook exporting
	 */
	@HasPermission(Permission.TaskView)
	def runbookStats() {
		def moveEventId = params.id
		Project project = securityService.userCurrentProject
		MoveEvent moveEvent = MoveEvent.get(moveEventId)
		def bundles = moveEvent.moveBundles
		int applcationAssigned = 0
		int assetCount = 0
		int databaseCount = 0
		int fileCount = 0
		int otherAssetCount = 0

		if (bundles) {
			List bundlesList = bundles as List
			applcationAssigned = Application.countByMoveBundleInListAndProject(bundlesList, project)
			assetCount = AssetEntity.countByMoveBundleInListAndAssetTypeNotInList(bundlesList,
					['Application', 'Database', 'Logical Storage'], params)
			databaseCount = Database.countByMoveBundleInListAndProject(bundlesList, project)
			fileCount = Files.countByMoveBundleInListAndProject(bundlesList, project)
			otherAssetCount = AssetEntity.countByAssetTypeNotInListAndMoveBundleInList(
					['Server','VM','Blade','Application','Logical Storage','Database'], bundlesList)
		}

		if (params.containsKey('viewUnpublished')) {
			userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED, params.viewUnpublished == '1')
		}

		def publishedValues = securityService.viewUnpublished() ? [true, false] : [true]

		[applcationAssigned: applcationAssigned, assetCount: assetCount, databaseCount: databaseCount,
		 fileCount: fileCount, otherAssetCount: otherAssetCount,
		 preMoveSize: AssetComment.countByMoveEventAndCategoryAndIsPublishedInList(moveEvent, 'premove', publishedValues),
		 scheduleSize: AssetComment.countByMoveEventAndCategoryInListAndIsPublishedInList(
				 moveEvent, ['shutdown','physical','moveday','startup'], publishedValues),
		 postMoveSize: AssetComment.countByMoveEventAndCategoryAndIsPublishedInList(moveEvent, 'postmove', publishedValues),
		 bundles: bundles, moveEventInstance: moveEvent]
	}

	/**
	 * The controller that actually does the runbook export generation to an Excel spreadsheet
	 */
	@HasPermission(Permission.TaskView)
	def exportRunbookToExcel() {

		Project project = controllerService.getProjectForPage(this)
		if (! project) return

		def moveEvent = controllerService.getEventForPage(this, project, params.eventId)
		if (! moveEvent) return

		def currentVersion = moveEvent.runbookVersion

		String tzId = userPreferenceService.timeZone
		String userDTFormat = userPreferenceService.dateFormat
		if (params.version == 'on') {
			if (moveEvent.runbookVersion) {
				moveEvent.runbookVersion = currentVersion + 1
				currentVersion = currentVersion + 1
			} else {
				moveEvent.runbookVersion = 1
				currentVersion = 1
			}
			moveEvent.save(flush:true)
		}

		def bundles = moveEvent.moveBundles
		def today = TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_TIME_6)
		def applications = []
		def assets = []
		def databases = []
		def files = []
		def others = []
		def unresolvedIssues = []
		def preMoveIssue = []
		def postMoveIssue = []

		boolean viewUnpublished = securityService.viewUnpublished()
		List<Boolean> publishedValues = viewUnpublished ? [true, false] : [true]

		if (bundles) {
			List bundlesList = bundles as List
			applications = Application.findAllByMoveBundleInListAndProject(bundlesList, project)
			assets = AssetEntity.findAllByMoveBundleInListAndAssetTypeNotInList(
					bundlesList, ['Application','Database','Logical Storage'])
			databases = Database.findAllByMoveBundleInListAndProject(bundlesList, project)
			files = Files.findAllByMoveBundleInListAndProject(bundlesList, project)
			others = AssetEntity.findAllByAssetTypeNotInListAndMoveBundleInList(
					['Server','VM','Blade','Application','Logical Storage','Database'], bundlesList)
			List<Long> allAssetIds = AssetEntity.findAllByMoveBundleInListAndProject(bundlesList, project).id

			unresolvedIssues = AssetComment.executeQuery("""
				from AssetComment
				where assetEntity.id in (:assetIds)
				  and dateResolved = null
				  and commentType=:commentType
				  and category in ('general', 'discovery', 'planning', 'walkthru')
				  AND isPublished IN (:publishedValues)
			""", [assetIds: allAssetIds, commentType: AssetCommentType.ISSUE,
			      publishedValues: publishedValues])
		}

		preMoveIssue = AssetComment.findAllByMoveEventAndCategoryAndIsPublishedInList(
				moveEvent, 'premove', publishedValues)
		def sheduleIssue = AssetComment.findAllByMoveEventAndCategoryInListAndIsPublishedInList(
				moveEvent, ['shutdown','physical','moveday','startup'], publishedValues)
		postMoveIssue = AssetComment.findAllByMoveEventAndCategoryAndIsPublishedInList(
				moveEvent, 'postmove', publishedValues)

		//TODO - Move controller code into Service .
		def preMoveCheckListError = reportsService.generatePreMoveCheckList(project.id, moveEvent, viewUnpublished).allErrors.size()

		try {
			def book = ExportUtil.loadSpreadsheetTemplate("/templates/Runbook.xlsx")

			Sheet personelSheet = book.getSheet('Staff')
			Sheet postMoveSheet = book.getSheet('Post-move')
			Sheet summarySheet = book.getSheet('Index')
			Sheet scheduleSheet = book.getSheet('Schedule')

			List projManagers = projectService.getProjectManagers(project)

			Font projectNameFont = book.createFont()
			projectNameFont.fontHeightInPoints = (short)14
			projectNameFont.fontName = 'Arial'
			projectNameFont.boldweight = Font.BOLDWEIGHT_BOLD

			CellStyle projectNameCellStyle = book.createCellStyle()
			projectNameCellStyle.font = projectNameFont
			projectNameCellStyle.fillBackgroundColor = IndexedColors.SEA_GREEN.index
			projectNameCellStyle.fillPattern = CellStyle.SOLID_FOREGROUND

			WorkbookUtil.addCell(summarySheet, 1, 1, project.name)
			WorkbookUtil.applyStyleToCell(summarySheet, 1, 1, projectNameCellStyle)
			WorkbookUtil.addCell(summarySheet, 2, 3, project.name)
			WorkbookUtil.addCell(summarySheet, 2, 6, projManagers.join(','))
			WorkbookUtil.addCell(summarySheet, 4, 6, '')
			WorkbookUtil.addCell(summarySheet, 2, 4, moveEvent.name)
			WorkbookUtil.addCell(summarySheet, 2, 10, moveEvent.name)

			moveBundleService.issueExport(assets,           serverCols,     book.getSheet('Servers'),      tzId, userDTFormat, 5, viewUnpublished)
			moveBundleService.issueExport(applications,     impactedCols,   book.getSheet('Applications'), tzId, userDTFormat, 5, viewUnpublished)
			moveBundleService.issueExport(databases,        dbCols,         book.getSheet('Database'),     tzId, userDTFormat, 4, viewUnpublished)
			moveBundleService.issueExport(files,            fileCols,       book.getSheet('Storage'),      tzId, userDTFormat, 1, viewUnpublished)
			moveBundleService.issueExport(others,           otherCols,      book.getSheet('Other'),        tzId, userDTFormat, 1, viewUnpublished)
			moveBundleService.issueExport(unresolvedIssues, unresolvedCols, book.getSheet('Issues'),       tzId, userDTFormat, 1, viewUnpublished)
			moveBundleService.issueExport(sheduleIssue,     scheduleCols,   scheduleSheet,                 tzId, userDTFormat, 7, viewUnpublished)
			moveBundleService.issueExport(preMoveIssue,     preMoveCols,    book.getSheet('Pre-move'),     tzId, userDTFormat, 7, viewUnpublished)
			moveBundleService.issueExport(postMoveIssue,    postMoveCols,   postMoveSheet,                 tzId, userDTFormat, 7, viewUnpublished)

			// Update the Schedule/Tasks Sheet with the correct start/end times
			Map<String, Date> times = moveEvent.getEventTimes()
			WorkbookUtil.addCell(scheduleSheet, 5, 1, TimeUtil.formatDateTime(times.start))
			WorkbookUtil.addCell(scheduleSheet, 5, 3, TimeUtil.formatDateTime(times.completion))

			// Update the project staff
			// TODO : JPM 11/2015 : Project staff should get list from ProjectService instead of querying PartyRelationship
			def projectStaff = PartyRelationship.executeQuery('''
					from PartyRelationship
					where partyRelationshipType='PROJ_STAFF'
					  and partyIdFrom=:project
					  and roleTypeCodeFrom='PROJECT'
			''', [project: project])

			for (int r = 8; r <= projectStaff.size() + 7; r++) {
				WorkbookUtil.addCell(personelSheet, 1, r, projectStaff[r - 8].partyIdTo?.toString())
				WorkbookUtil.addCell(personelSheet, 2, r, projectStaff[r-8].roleTypeCodeTo.toString())
				WorkbookUtil.addCell(personelSheet, 5, r, projectStaff[r-8].partyIdTo?.email ?: '')
			}

			String filename = project.name + ' - ' + moveEvent.name + ' Runbook v' + currentVersion + ' -' + today +
					'.' + ExportUtil.getWorkbookExtension(book)
			ExportUtil.sendWorkbook(book, response, filename)
		}
		catch(e) {
			logger.error 'Exception occurred while exporting data: {}', e.message, e
		}
	}

	/**
	 * Set asset's plan-status to 'Moved' for the specified event.
	 * @usage Ajax
	 * @param moveEventId
	 * @return  Count of record affected with this update or Error Message if any
	 */
	@HasPermission(Permission.AssetEdit)
	def markEventAssetAsMoved() {
		// if (params.containsKey("moveEventId")) {
		// 	if (params.moveEventId.isNumber()) {
		def moveEvent = MoveEvent.get(params.moveEventId)
		if (!moveEvent) {
			logger.error 'markEventAssetAsMoved: Specified moveEvent ({}) was not found})', params.moveEventId

			render 'An unexpected condition with the event occurred that is preventing an update.'
			return
		}

		if (!securityService.isCurrentProjectId(moveEvent.project.id)) {
			logger.error 'markEventAssetAsMoved: moveEvent.project ({}) does not match current project ({})', moveEvent.id, securityService.userCurrentProjectId
			render 'An unexpected condition with the event occurred that is preventing an update'
			return
		}

		int assetAffected = 0

		if (moveEvent.moveBundles) {
			assetAffected = jdbcTemplate.update("update asset_entity  \
			set plan_status = 'Moved', source_location = target_location, room_source_id = room_target_id ,\
				rack_source_id = rack_target_id, source_rack_position = target_rack_position, \
				source_blade_chassis = target_blade_chassis, source_blade_position = target_blade_position, \
				target_location = null, room_target_id = null, rack_target_id = null, target_rack_position = null,\
				target_blade_chassis = null, target_blade_position = null\
			where move_bundle_id in (SELECT mb.move_bundle_id FROM move_bundle mb WHERE mb.move_event_id =  $moveEvent.id) \
				and plan_status != 'Moved' ")
		}

		render assetAffected
	}

	/**
	 * Filters newsBarMode property]; as we are displaying different label in list so user may search
	 * according to displayed label but in DB we have different values what we are displaying in label
	 * e.g. for auto - Auto Start, true - Started ..
	 * @param newsBarMode with what character user filterd newsBarMode property
	 * @return matched db property of newsBarMode
	 */
	@HasPermission(Permission.EventView)
	private List<String> retrieveNewsBMList(String newsBarMode) {
		if (!newsBarMode) return null

		List<String> returnList = []
		['Auto Start': 'auto', Started: 'on', Stopped: 'off'].each { String key, String value ->
			if (StringUtils.containsIgnoreCase(key, newsBarMode)) {
				returnList << value
			}
		}
		return returnList
	}
}
