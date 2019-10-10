package net.transitionmanager.project

import com.tdsops.tm.enums.domain.TimeScale
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import net.transitionmanager.asset.Application
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.reporting.ReportsService
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.TagEvent
import net.transitionmanager.tag.TagEventService
import net.transitionmanager.task.AssetComment
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import net.transitionmanager.exception.ServiceException
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.party.PartyRelationship
import net.transitionmanager.person.Person
import net.transitionmanager.security.RoleType
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.jdbc.core.JdbcTemplate
import com.tdsops.tm.enums.domain.AssetCommentCategory

import java.sql.Timestamp

@Slf4j
@Transactional
class MoveEventService implements ServiceMethods {

	private static final List<String> preMoveCols = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment', 'assignedTo',
													 'status', 'estStart', '', '', 'notes', 'duration', 'estStart',
													 'estFinish', 'actStart', 'actFinish']
	private static final List<String> serverCols = ['id', 'application', 'assetName', '','serialNumber', 'assetTag',
													'manufacturer', 'model', 'assetType', '', '', '']
	private static final List<String> scheduleCols = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment', 'role',
													  'assignedTo', 'instructionsLink' , '', 'duration', 'estStart',
													  'estFinish', 'actStart', 'actFinish']
	private static final List<String> postMoveCols = ['taskNumber', 'assetEntity', 'comment','assignedTo', 'status',
													  'estFinish', 'dateResolved' , 'notes', 'taskDependencies', 'duration',
													  'estStart', 'estFinish', 'actStart', 'actFinish']
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
														'actStart', 'actFinish']

	JdbcTemplate          jdbcTemplate
	MoveBundleService     moveBundleService
	TagEventService       tagEventService
	UserPreferenceService userPreferenceService
	ProjectService        projectService
	ReportsService        reportsService

	MoveEvent create(Project project, String name) {
		MoveEvent me = new MoveEvent([project:project, name:name])
		save me, true
		me
	}


	/**
	 * Create or update a MoveEvent based on the given CommandObject.
	 * @param project
	 * @param eventCommand
	 * @param moveEventId
	 * @return
	 */
	MoveEvent createOrUpdate(Project project, CreateEventCommand eventCommand, Long moveEventId = null) {
		MoveEvent moveEvent = getOrCreate(MoveEvent, moveEventId, project)
		eventCommand.populateDomain(moveEvent, false, ['moveBundle', 'tagIds'])
		moveEvent.save()

		// Determine if there are any tagEvents to delete.
		List<TagEvent> tagEventsToDelete = moveEvent.tagEvents?.findAll{ TagEvent tagEvent -> !eventCommand.tagIds.contains( tagEvent.tagId ) }
		if (tagEventsToDelete) {
			moveEvent.tagEvents.removeAll(tagEventsToDelete)
			tagEventService.removeTags(project, tagEventsToDelete*.id)
		}

		// Assign the corresponding move bundles.
		moveBundleService.assignMoveEvent(moveEvent, eventCommand.moveBundle)

		// Create a list with the tags (if any) already set for this event instance.
		List<Long> existingTagEvents = moveEvent.tagEvents*.tagId
		// Define the list of tags that need to be added to the event.
		List<Long> tagEventIdsToAdd = eventCommand.tagIds
		// If the event had already some tags, filter those out so we don't add them twice.
		if (existingTagEvents) {
			tagEventIdsToAdd = eventCommand.tagIds.collect { Long tagId -> !existingTagEvents.contains(tagId)}
		}
		if (tagEventIdsToAdd) {
			tagEventService.applyTags(project, tagEventIdsToAdd, moveEvent)
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

		if (teamRoleType.type != RoleType.TYPE_TEAM) {
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
			if (! mes.save(flush:true, failOnError: false)) {
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
		RoleType.findByIdAndType(teamCode, RoleType.TYPE_TEAM)
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

		if (teamRoleType.type != RoleType.TYPE_TEAM) {
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
			projectNameFont.setBold(true)

			CellStyle projectNameCellStyle = book.createCellStyle()
			projectNameCellStyle.font = projectNameFont
			projectNameCellStyle.fillBackgroundColor = IndexedColors.SEA_GREEN.index
			projectNameCellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

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
			moveBundleService.issueExport(files,            fileCols,       book.getSheet('Storage'),      tzId, userDTFormat, 4, viewUnpublished)
			moveBundleService.issueExport(others,           otherCols,      book.getSheet('Other'),        tzId, userDTFormat, 1, viewUnpublished)
			moveBundleService.issueExport(unresolvedIssues, unresolvedCols, book.getSheet('Issues'),       tzId, userDTFormat, 1, viewUnpublished)
			moveBundleService.issueExport(sheduleIssue,     scheduleCols,   scheduleSheet,                    tzId, userDTFormat, 7, viewUnpublished)
			moveBundleService.issueExport(preMoveIssue,     preMoveCols,    book.getSheet('Pre-move'),     tzId, userDTFormat, 7, viewUnpublished)
			moveBundleService.issueExport(postMoveIssue,    postMoveCols,   postMoveSheet,                    tzId, userDTFormat, 7, viewUnpublished)

			// Update the Schedule/Tasks Sheet with the correct start/end times
			Map<String, Date> times =getEventTimes(moveEvent.id)
			WorkbookUtil.addCell(scheduleSheet, 5, 1, TimeUtil.formatDateTime(times.start))
			WorkbookUtil.addCell(scheduleSheet, 5, 3, TimeUtil.formatDateTime(times.completion))

			// Update the project staff
			// TODO : JPM 11/2015 : Project staff should get list from ProjectService instead of querying PartyRelationship
			def projectStaff = PartyRelationship.executeQuery("""
					from PartyRelationship
					where partyRelationshipType='PROJ_STAFF'
					  and partyIdFrom=:project
					  and roleTypeCodeFrom='$RoleType.CODE_PARTY_PROJECT'
			""".toString(), [project: currentProject])

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
	 * Retrieves the MIN/MAX Start and Completion times of the MoveBundles associate with the MoveEvent
	 * @return Map[start , completion] times for the MoveEvent
	 */
	Map<String, Date> getEventTimes(Long id) {
		jdbcTemplate.queryForMap('''
			SELECT MIN(start_time) AS start, MAX(completion_time) AS completion
			FROM move_bundle
			WHERE move_event_id = ?''', id)
	}

	/**
	 * Used to get the list of events that a person is assigned to.
	 * @param person - the person to find assigned event for
	 * @param currentProject - the individual project to find events for, if null then the
	 *  events of all projects that the user is assigned will be returned
	 * @param active - flag that signals if active or past events should be listed.
	 *
	 * @return a list of events (active or completed) for the given project (or projects the person has access to).
	 */
	List<MoveEvent> getAssignedEvents(Person person, Project currentProject = null, boolean active) {
		Date now = TimeUtil.nowGMT()
		String sortProperty
		String sortOrder

		if (active) {
			sortProperty = 'estCompletionTime'
			sortOrder = 'asc'
		} else {
			sortProperty = 'actualCompletionTime'
			sortOrder = 'desc'
		}
		return MoveEvent.where {
			if (currentProject) {
				project == currentProject
			} else {
				project.id in securityService.getUserProjectIds(null, person.userLogin)
			}
			if (active) {
				estCompletionTime > now || estCompletionTime == null
			} else {
				estCompletionTime < now
			}
		}.order(sortProperty, sortOrder).list()

	}

	/**
	 * Find different stats for the given event, grouped by category.
	 * @param project
	 * @param moveEventId
	 * @return a list with the task category stats
	 */
	List<Map> getTaskCategoriesStats(Project project, Long moveEventId) {
		// Fetch the corresponding MoveEvent and throw an exception if not found.
		MoveEvent moveEvent = get(MoveEvent, moveEventId, project, true)
		// Query the database for the min/max dates for tasks in the event grouped by category.
		String hql = """
				select 
					ac.category,
					min(ac.actStart),
					max(ac.dateResolved),
					min(ac.estStart),
					max(ac.estFinish),
					max(ac.duration),
					ac.durationScale,
					count(*),
					sum(case when ac.status = 'Completed' then 1 else 0 end),
					case when (count(*) > 0) then (sum(case when ac.status = 'Completed' then 1 else 0 end)/count(*)*100) else 100 end
				from AssetComment ac
					where moveEvent =:moveEvent
				group by ac.category
			"""

		List taskCategoriesStatsList = AssetComment.executeQuery(hql, ["moveEvent": moveEvent])

		List<Map> stats = []
		taskCategoriesStatsList.each { categoryStats ->
			// Only add to the results if there are any tasks for the category
			if (categoryStats[7] ) {
				stats << [
					"category": 	categoryStats[0],
					"actStart": 	categoryStats[1],
					"actFinish": 	categoryStats[2],
					"estStart": 	categoryStats[3],
					"estFinish": 	categoryStats[4],
					"maxRemaining":	categoryStats[5],
					"maxRScale":	categoryStats[6],
					"tskTot": 		categoryStats[7],
					"tskComp": 		categoryStats[8],
					"percComp": 	categoryStats[9],
					"color":		calculateColumnColor(categoryStats, moveEvent)
				]
			}
			// Sort by the category "natural" sort order
			stats.sort { stat  -> AssetCommentCategory.list.indexOf(stat.category)}
		}
		return stats
	}

	/**
	 * Returns different data used to render the Event Dashboard
	 * @param moveEvent
	 * @param moveBundle
	 * @return
	 */
	Map bundleData(MoveEvent moveEvent, MoveBundle moveBundle) {
		Date sysTime = TimeUtil.nowGMT()

		Date planSumCompTime
		MoveEventSnapshot moveEventPlannedSnapshot
		MoveEventSnapshot moveEventRevisedSnapshot
		Date revisedComp
		TimeDuration dayTime
		String eventString = ""

		if (moveEvent) {

			Map resultMap = jdbcTemplate.queryForMap( """
					SELECT max(mb.completion_time) as compTime,
					min(mb.start_time) as startTime
					FROM move_bundle mb WHERE mb.move_event_id = $moveEvent.id
				""" )

			planSumCompTime = resultMap?.compTime
			Date eventStartTime = moveEvent.estStartTime
			if (eventStartTime || resultMap?.startTime) {
				if(!eventStartTime){
					eventStartTime = new Date(resultMap?.startTime?.getTime())
				}
				if (eventStartTime>sysTime) {
					dayTime = TimeCategory.minus(eventStartTime, sysTime)
					eventString = "Countdown Until Event"
				} else {
					dayTime = TimeCategory.minus(sysTime, eventStartTime)
					eventString = "Elapsed Event Time"
				}
			}

			// select the most recent MoveEventSnapshot records for the event for both the P)lanned and R)evised types
			String query = "FROM MoveEventSnapshot mes WHERE mes.moveEvent = ? AND mes.type = ? ORDER BY mes.dateCreated DESC"
			moveEventPlannedSnapshot = MoveEventSnapshot.findAll( query , [moveEvent, MoveEventSnapshot.TYPE_PLANNED] )[0]
			moveEventRevisedSnapshot = MoveEventSnapshot.findAll( query , [moveEvent, MoveEventSnapshot.TYPE_REVISED] )[0]
			revisedComp = moveEvent.revisedCompletionTime
			if (revisedComp) {
				revisedComp = new Date(revisedComp.time)
			}
		}
		String eventClockCountdown = TimeUtil.formatTimeDuration(dayTime)

		return [snapshot: [
				revisedComp: moveEvent?.revisedCompletionTime,
				moveBundleId: moveBundle.id,
				calcMethod: moveEvent?.calcMethod,
				systime: TimeUtil.formatDateTime(sysTime, TimeUtil.FORMAT_DATE_TIME_11),
				eventStartDate: moveEvent.estStartTime,
				planSum: [
						dialInd: moveEventPlannedSnapshot?.dialIndicator,
						compTime: planSumCompTime,
						dayTime: eventClockCountdown,
						eventDescription: moveEvent?.description,
						eventString: eventString,
						eventRunbook: moveEvent?.runbookStatus
				]
		]]
	}

	/**
	 * Returns the color that the column should have for the given category in the Event Dashboard.
	 * @See TM-15896
	 * @param categoryStats  The category properties
	 * @param moveEvent  The event to which the category properties belong
	 * @return  The calculated color for the category column.
	 */
	private String calculateColumnColor(categoryStats, MoveEvent moveEvent) {

		Date actualStart = categoryStats[1]
		Date actualFinish = categoryStats[2]

		// If tasks estimated start or completion are blank, use the Event estimated start and completion
		Date estimatedStart = categoryStats[3] ?: moveEvent.estStartTime
		Date estimatedFinish = categoryStats[4] ?: moveEvent.estCompletionTime

		Date now = new Date()

		Long totalTasks = categoryStats[7]
		Long completedTasks = categoryStats[8]
		Long percentCompleted = categoryStats[9]

		long longestRemainingInMillis = getLongestRemainingInMillis(categoryStats[5], categoryStats[6])

		// default is green
		String color = "green"
		if (completedTasks == totalTasks && actualFinish < estimatedFinish) {
			color = "#24488a" // completed-blue
		} else {
			if (estimatedFinish < now && percentCompleted < 100) {
				color = "red" // past completed time and tasks remain
			} else {
				if (longestRemainingInMillis && estimatedFinish < new Date(now.getTime() + longestRemainingInMillis)) {
					color = "#FFCC66" // yellow, unlikely to finish in estimate window because the longest task remaining would complete too late
				}
			}
		}
		return color
	}

	/**
	 * Calculates the longest remaining time for a task in milliseconds, or returns 0 if parameters are empty or null.
	 * @param maxRemaining
	 * @param maxRScale
	 * @return  The max remaining time in milliseconds, or zero if anything is empty or null.
	 */
	private long getLongestRemainingInMillis(maxRemaining, TimeScale maxRScale) {

		if (!maxRemaining || !maxRScale) {
			return 0
		} else {
			return maxRScale.toMinutes(maxRemaining) * 60000
		}
	}

}
