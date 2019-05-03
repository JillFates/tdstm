package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.domain.AppMoveEvent
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventStaff
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RoleType
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.jdbc.core.JdbcTemplate

@Slf4j
@Transactional
class MoveEventService implements ServiceMethods {

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


	JdbcTemplate jdbcTemplate
	MoveBundleService moveBundleService
	TagEventService tagEventService
	UserPreferenceService userPreferenceService
	ProjectService projectService
	ReportsService reportsService

	MoveEvent create(Project project, String name) {
		MoveEvent me = new MoveEvent([project:project, name:name])
		save me, true
		me
	}

	/**
	 * Used to create a new move event.
	 *
	 * @param event The event command object with the parameters to create a new event.
	 * @param currentProject The current project to create an event for.
	 *
	 * @return the instance of the move event created, which may contain errors if the save doesn't work(failOnError: false)
	 */
	MoveEvent save(CreateEventCommand event, Project currentProject) {
		MoveEvent moveEvent = new MoveEvent(
			project: currentProject,
			name: event.name,
			description: event.description,
			runbookStatus: event.runbookStatus,
			runbookBridge1: event.runbookBridge1,
			runbookBridge2: event.runbookBridge2,
			videolink: event.videolink,
			newsBarMode: event.newsBarMode,
			estStartTime: event.estStartTime,
			estCompletionTime: event.estCompletionTime,
			apiActionBypass: event.apiActionBypass
		)

		if (moveEvent.project.runbookOn == 1) {
			moveEvent.calcMethod = MoveEvent.METHOD_MANUAL
		}

		moveEvent.save(failOnError: false)

		if (!moveEvent.hasErrors()) {
			moveBundleService.assignMoveEvent(moveEvent, event.moveBundle)
			moveBundleService.createManualMoveEventSnapshot(moveEvent)

			if (event.tagIds) {
				tagEventService.applyTags(currentProject, event.tagIds, moveEvent.id)
			}
		}

		return moveEvent
	}

	/**
	 * This function using to verify events for requested project
	 * @param reqEventIds : reqEventIds is list of requested id from browser
	 * @param project : project list
	 * @return
	 */
	@Transactional(readOnly = true)
	def verifyEventsByProject(List reqEventIds, project){
		def nonProjEventIds
		if(project){
			def projEventsId = MoveEvent.findAllByProject( project )?.id
			//Now Checking whether requested event list has any bad or non project associated id.
			nonProjEventIds = reqEventIds - projEventsId
			if(nonProjEventIds){
				log.error "Event ids $nonProjEventIds is not associated with current project.\
						    Kindly request for project associated  Event ids ."
			}
		}
		return nonProjEventIds
	}

	/**
	 * Used to retrieve a person to a move event for a specified team
	 * @param moveEvent - the move event to search for the member
	 * @param person - the team member on the event
	 * @param teamRoleType - a valid TEAM role
	 * @return An error message if unable to create the team otherwise null
	 */
	@Transactional(readOnly = true)
	MoveEventStaff getTeamMember(MoveEvent moveEvent, Person person, RoleType teamRoleType) {
		String query = 'from MoveEventStaff mes where mes.moveEvent=:me and mes.person=:p and mes.role=:teamRole'
		MoveEventStaff.find(query, [me:moveEvent, p:person, teamRole:teamRoleType] )
	}

	/**
	 * Used to assign a person to a move event for a specified team
	 * @param moveEvent - the move event to assign the person to
	 * @param person - the new team member for the event
	 * @param teamRoleType - a valid TEAM role
	 * @return An error message if unable to create the team otherwise null
	 */
	MoveEventStaff addTeamMember(MoveEvent moveEvent, Person person, RoleType teamRoleType) {
		assert moveEvent
		assert person

		if (teamRoleType.type != RoleType.TEAM) {
			throw new InvalidParamException('Invalid team code $teamRoleType.id was specified')
		}

		// Try finding the assignment first
		MoveEventStaff mes = getTeamMember(moveEvent, person, teamRoleType)

		if (! mes) {
			// TODO : JPM 4/2016 : addTeamMember() should validate that the person is associated with the project

			// Now create the MoveEventStaff record
			mes = new MoveEventStaff()
			mes.person = person
			mes.moveEvent = moveEvent
			mes.role = teamRoleType
			if (! mes.save(flush:true)) {
				log.error "addTeamMember() failed to create MoveEventStaff($person.id, $moveEvent.id, $teamCode) : ${GormUtil.allErrorsString(moveEventStaff)}"
				throw new DomainUpdateException('An error occurred while assigning person to the event')
			}
		}

		return mes
	}

	/**
	 * Used to assign a person to a move event for a specified team
	 * @param moveEvent - the move event to assign the person to
	 * @param person - the new team member for the event
	 * @param teamRoleType - a valid TEAM role
	 * @return An error message if unable to create the team otherwise null
	 */
	MoveEventStaff addTeamMember(MoveEvent moveEvent, Person person, String teamCode) {
		RoleType teamRoleType = teamRoleType(teamCode)
		if (!teamRoleType) {
			throw new InvalidParamException("Invalid team code '$teamCode' was specified")
		}
		return addTeamMember(moveEvent, person, teamRoleType)
	}

	/**
	 * Used to retrieve a TEAM RoleType based on the code
	 * @param teamCode - the TEAM string code
	 * @return the TEAM RoleType if found otherwise null
	 */
	@Transactional(readOnly = true)
	RoleType teamRoleType(String teamCode) {
		RoleType.findByIdAndType(teamCode, RoleType.TEAM)
	}

	/**
	 * Used to unassign a person from a move event for a specified team
	 * @param moveEvent - the move event to unassign the person from
	 * @param person - the team member associated to the event
	 * @param teamRoleType - the team role
	 * @return true if the team member was deleted or false if not found
	 */
	boolean removeTeamMember(MoveEvent moveEvent, Person person, RoleType teamRoleType) {
		assert moveEvent
		assert person
		boolean status = false

		if (teamRoleType.type != RoleType.TEAM) {
			throw new InvalidParamException("Invalid team code '$teamRoleType.id' was specified")
		}

		// Try finding the assignment first
		MoveEventStaff mes = getTeamMember(moveEvent, person, teamRoleType)
		if (mes) {
			status = mes.delete()
		}
		return status
	}

	/**
	 * Used to unassign a person from a move event for a specified team
	 * @param moveEvent - the move event to unassign the person from
	 * @param person - the team member associated to the event
	 * @param teamCode - the team role code
	 * @return true if the team member was deleted or false if not found
	 */
	MoveEventStaff removeTeamMember(MoveEvent moveEvent, Person person, String teamCode) {
		RoleType teamRoleType = teamRoleType(teamCode)
		if (!teamRoleType) {
			throw new InvalidParamException("Invalid team code '$teamCode' was specified")
		}
		return removeTeamMember(moveEvent, person, teamRoleType)
	}

	/**
	 * This method deletes a MoveEvent and, additionally, performs the following
	 * operations:
	 * - Delete all MoveEventSnapshot for the event.
	 * - Delete all news for the event.
	 * - Nulls out reference to the event in Move Bundle.
	 * - Deletes User References pointing to this event.
	 * - Deletes all AppMoveEvents for the event.
	 * - Nulls out references to this event for tasks and comments.
	 * @param moveEvent
	 */
	void deleteMoveEvent(MoveEvent moveEvent) {
		if (moveEvent) {
			// Deletes MoveEventSnapshots for this event.
			jdbcTemplate.update('DELETE FROM move_event_snapshot             WHERE move_event_id = ?', moveEvent.id)
			// Deletes all news for this event.
			jdbcTemplate.update('DELETE FROM move_event_news                 WHERE move_event_id = ?', moveEvent.id)
			// Nulls out the reference to this event in MoveBundle.
			jdbcTemplate.update('UPDATE move_bundle SET move_event_id = NULL WHERE move_event_id = ?', moveEvent.id)
			// Deletes all UserPreference pointing to this event.
			jdbcTemplate.update('DELETE FROM user_preference WHERE preference_code = ? and value = ?', UserPreferenceEnum.MOVE_EVENT as String, moveEvent.id)
			// Deletes all AppMoveEvent related to this event.
			AppMoveEvent.executeUpdate('DELETE AppMoveEvent WHERE moveEvent.id =  ?', [moveEvent.id])
			// Nulls out references to this event in comments and tasks.
			AssetComment.executeUpdate("UPDATE AssetComment SET moveEvent = NULL WHERE moveEvent.id = ?", [moveEvent.id])
			// Deletes the event.
			moveEvent.delete()
		}
	}


	/**
	 * Update the lastUpdated field on a series of assets.
	 *
	 * This method helps to keep consistency, and update assets accordingly,
	 * when performing bulk update operations on objects that have a relationship with assets,
	 * such as TagAsset.
	 *
	 * @param project
	 * @param assetQuery - query that should return a list of asset ids.
	 * @param assetQueryParams - parameters for assetQuery
	 */
	void bulkBumpMoveEventLastUpdated(Project project, Set<Long>eventIds) {
		if (project) {
			String query = """
				UPDATE MoveEvent SET lastUpdated = :lastUpdated
				WHERE id IN (:eventIds) AND project = :project
			"""

			Map params = [project: project, lastUpdated: TimeUtil.nowGMT(), eventIds:eventIds]
			MoveEvent.executeUpdate(query, params)
		}
	}

	/**
	 * Return all MoveEvents for the current Project.
	 * @param project - user's current project
	 * @return a list with all the events for the project.
	 */
	List<MoveEvent> listMoveEvents(Project project) {
		return MoveEvent.where {
			project == project
		}.list()
	}

	/**
	 * Find move event by id
	 * @param id - move event id
	 * @param throwException  - whether to throw an exception if move event is not found
	 * @return
	 */
	MoveEvent findById(Long id, boolean throwException = false) {
		Project currentProject = securityService.getUserCurrentProject()
		return GormUtil.findInProject(currentProject, MoveEvent, id, throwException)
	}

	/**
	 * Update a move event
	 * @param id - move event id to update
	 * @param command - move event command object
	 * @return
	 */
	MoveEvent update(Long id, CreateEventCommand command) {
		MoveEvent moveEvent = findById(id, true)
		moveEvent.properties = command
		if (!moveEvent.hasErrors() && moveEvent.save()) {
			return moveEvent
		} else {
			log.info("Error updating MoveEvent. {}", GormUtil.allErrorsString(moveEvent))
			throw new DomainUpdateException("Error updating move event.")
		}
	}

	/**
	 * Export run book to MS Excel
	 * @param moveEvent - move event to export
	 * @param updateRunbookVersion - whether to update
	 * @return
	 */
	Map<String, ?> exportRunbookToExcel(MoveEvent moveEvent, boolean updateRunbookVersion) {
		Project currentProject = securityService.getUserCurrentProject()
		Integer currentVersion = moveEvent.runbookVersion
		String tzId = userPreferenceService.timeZone
		String userDTFormat = userPreferenceService.dateFormat

		if (updateRunbookVersion) {
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
		def today = TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_ISO8601)
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
			applications = Application.findAllByMoveBundleInListAndProject(bundlesList, currentProject)
			assets = AssetEntity.findAllByMoveBundleInListAndAssetTypeNotInList(
					bundlesList, ['Application','Database','Logical Storage'])
			databases = Database.findAllByMoveBundleInListAndProject(bundlesList, currentProject)
			files = Files.findAllByMoveBundleInListAndProject(bundlesList, currentProject)
			others = AssetEntity.findAllByAssetTypeNotInListAndMoveBundleInList(
					['Server','VM','Blade','Application','Logical Storage','Database'], bundlesList)
			List<Long> allAssetIds = AssetEntity.findAllByMoveBundleInListAndProject(bundlesList, currentProject).id

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

		def preMoveCheckListError = reportsService.generatePreMoveCheckList(currentProject.id, moveEvent, viewUnpublished).allErrors.size()

		try {
			def book = ExportUtil.loadSpreadsheetTemplate("/templates/Runbook.xlsx")

			Sheet personelSheet = book.getSheet('Staff')
			Sheet postMoveSheet = book.getSheet('Post-move')
			Sheet summarySheet = book.getSheet('Index')
			Sheet scheduleSheet = book.getSheet('Schedule')

			List projManagers = projectService.getProjectManagers(currentProject)

			Font projectNameFont = book.createFont()
			projectNameFont.fontHeightInPoints = (short)14
			projectNameFont.fontName = 'Arial'
			projectNameFont.boldweight = Font.BOLDWEIGHT_BOLD

			CellStyle projectNameCellStyle = book.createCellStyle()
			projectNameCellStyle.font = projectNameFont
			projectNameCellStyle.fillBackgroundColor = IndexedColors.SEA_GREEN.index
			projectNameCellStyle.fillPattern = CellStyle.SOLID_FOREGROUND

			WorkbookUtil.addCell(summarySheet, 1, 1, currentProject.name)
			WorkbookUtil.applyStyleToCell(summarySheet, 1, 1, projectNameCellStyle)
			WorkbookUtil.addCell(summarySheet, 2, 3, currentProject.name)
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
			moveBundleService.issueExport(sheduleIssue,     scheduleCols,   scheduleSheet,                    tzId, userDTFormat, 7, viewUnpublished)
			moveBundleService.issueExport(preMoveIssue,     preMoveCols,    book.getSheet('Pre-move'),     tzId, userDTFormat, 7, viewUnpublished)
			moveBundleService.issueExport(postMoveIssue,    postMoveCols,   postMoveSheet,                    tzId, userDTFormat, 7, viewUnpublished)

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
			''', [project: currentProject])

			for (int r = 8; r <= projectStaff.size() + 7; r++) {
				WorkbookUtil.addCell(personelSheet, 1, r, projectStaff[r - 8].partyIdTo?.toString())
				WorkbookUtil.addCell(personelSheet, 2, r, projectStaff[r-8].roleTypeCodeTo.toString())
				WorkbookUtil.addCell(personelSheet, 5, r, projectStaff[r-8].partyIdTo?.email ?: '')
			}

			String filename = currentProject.name + ' - ' + moveEvent.name + ' Runbook v' + currentVersion + ' -' + today +
					'.' + ExportUtil.getWorkbookExtension(book)
			return ['book': book, 'filename': filename]
		} catch(e) {
			log.info 'Exception occurred while exporting data: {}', e.message, e
			throw new ServiceException('Exception occurred while exporting data: ' + e.message)
		}
	}

	/**
	 * Used to get the list of events that a person is assigned to.
	 * @param person - the person to find assigned event for
	 * @param currentProject - the individual project to find events for, if null then the
	 *  events of all projects that the user is assigned will be returned
	 * @param completionCutoff - the date cut off the list based on the estCompletionDate.
	 *  If the field is set then only events where the estCompletionDate >= completionCutoff
	 *  or completionCutoff is null will appear.
	 */
	List<MoveEvent> getAssignedEvents(Person person, Project currentProject = null, Date completionCutoff = null) {

		return MoveEvent.where {
			if (currentProject) {
				project == currentProject
			} else {
				project.id in securityService.getUserProjectIds(null, person.userLogin)
			}
			if (completionCutoff) {
				estCompletionTime > completionCutoff || estCompletionTime == null
			}
		}.order('estCompletionTime', 'desc').list()
	}

}
