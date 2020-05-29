package net.transitionmanager.project

import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.NumberUtil
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
import com.tdsops.tm.enums.domain.TimeScale
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.util.logging.Slf4j
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.ServiceException
import net.transitionmanager.party.PartyRelationship
import net.transitionmanager.person.Person
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.reporting.ReportsService
import net.transitionmanager.security.RoleType
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.TagEvent
import net.transitionmanager.tag.TagEventService
import net.transitionmanager.task.AssetComment
import org.apache.poi.ss.usermodel.*
import org.springframework.jdbc.core.JdbcTemplate

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

	// Positions in query results for the getTaskCategoriesStats method
	private static final Integer MAX_ACTUAL_FINISH = 3
	private static final Integer MAX_PLANNED_FINISH = 4
	private static final Integer MAX_EST_FINISH = 5
	private static final Integer REMAINING_TASKS = 6
	private static final Integer COMPLETED_TASKS = 7
	private static final Integer TOTAL_TASKS = 8

	public static final String CLOCK_MODE_NONE 			= 'none'
	public static final String CLOCK_MODE_COUNTDOWN 	= 'countdown'
	public static final String CLOCK_MODE_ELAPSED 		= 'elapsed'
	public static final String CLOCK_MODE_FINISHED 		= 'finished'

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
		List<TagEvent> tagEventsToDelete = moveEvent.tagEvents?.findAll{ TagEvent tagEvent -> !eventCommand.tagIds.contains(tagEvent.tagId ) }
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
			tagEventIdsToAdd = eventCommand.tagIds.findAll { Long tagId -> !existingTagEvents.contains(tagId)}
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
			AppMoveEvent.executeUpdate('DELETE AppMoveEvent WHERE moveEvent.id =  ?0', [moveEvent.id])
			// Nulls out references to this event in comments and tasks.
			AssetComment.executeUpdate("UPDATE AssetComment SET moveEvent = NULL WHERE moveEvent.id = ?0", [moveEvent.id])
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

			if (allAssetIds) {
				unresolvedIssues = AssetComment.executeQuery("""
					from AssetComment
					where assetEntity.id in (:assetIds)
					and dateResolved = null
					and commentType=:commentType
					and category in ('general', 'discovery', 'planning', 'walkthru')
					and isPublished IN (:publishedValues)
			""", [assetIds: allAssetIds, commentType: AssetCommentType.ISSUE,
				  publishedValues: publishedValues])
			}
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
	 * Find the stats for all the tasks by category for the event
	 * @param project  The {@code Project} to which the event belongs to.
	 * @param moveEvent  The move event
	 * @param viewUnpublished - flag to indicate if unpublished tasks should be included in the results when true
	 * @return a list with the task category stats
	 */
	List<Map> getTaskCategoriesStats(Project project, MoveEvent moveEvent, Boolean viewUnpublished) {
		// Query the database for the min/max values and counts for the tasks in the event, grouped by category.
		String hql = """
				select 
					t.category as category,
					min(case when t.actStart is null then t.dateResolved else t.actStart end) as minActualStart,
					min(t.estStart) as minEstStart,
					max(t.dateResolved) as maxActualFinish,
					max(t.latestFinish) as maxPlannedFinish,
					max( case when t.dateResolved is null 
						then 
							case when t.actStart is null 
							then 
								case when t.latestStart >= NOW() then 
									FROM_UNIXTIME( UNIX_TIMESTAMP(t.latestStart) + COALESCE(t.duration,0)*60 )
								else
									FROM_UNIXTIME( UNIX_TIMESTAMP(NOW()) + COALESCE(t.duration,0)*60 )
								end
							else
								case when ( (UNIX_TIMESTAMP(t.actStart) + COALESCE(t.duration,0)*60) > UNIX_TIMESTAMP(NOW()) ) then
									FROM_UNIXTIME( UNIX_TIMESTAMP(t.actStart) + COALESCE(t.duration,0)*60 )
								else
									FROM_UNIXTIME( UNIX_TIMESTAMP(NOW()) + 60)
								end
							end
						else
						  null
						end
					) as maxEstFinish,
					sum( case when t.dateResolved is null then 1 else 0 end ) as remainingTasks,
					sum( case when t.dateResolved is null then 0 else 1 end ) as completedTasks,
					count(*) as totalTasks
				from Task t
				where t.moveEvent =:moveEvent 
					and t.category is not null
					and t.category <> 'general'
					${ (! viewUnpublished ? 'and t.isPublished = true' : '') }
				group by t.category
				order by minEstStart, category
			"""

		List taskCategoriesStatsList = AssetComment.executeQuery(hql, ["moveEvent": moveEvent])

		List<Map> stats = []
		Date now = new Date()

		taskCategoriesStatsList.each { categoryStats ->
			// NOTE if minEstStart or maxEstFinish is empty, just use the event times

			Map catStats = [
					"category": 			categoryStats[0],
					"minActStart": 			categoryStats[1],
					"minEstStart": 			categoryStats[2] ?: moveEvent.estStartTime,
					"maxActFinish": 		categoryStats[MAX_ACTUAL_FINISH],
					// maxPlannedFinish is the latest completion time for the Task where it can finish on time
					"maxPlannedFinish": 	categoryStats[MAX_PLANNED_FINISH] ?: moveEvent.estCompletionTime,
					// maxEstFinish is when we expect the Task to complete, based on the current time
					"maxEstFinish": 		categoryStats[MAX_EST_FINISH] ?: moveEvent.estCompletionTime,
					"remainingTasks":		categoryStats[REMAINING_TASKS],
					"completedTasks": 		categoryStats[COMPLETED_TASKS],
					"totalTasks": 			categoryStats[TOTAL_TASKS],
					"percComp": 			NumberUtil.percentage(categoryStats[TOTAL_TASKS], categoryStats[COMPLETED_TASKS]),
			]
			catStats.color = calculateColumnColor(catStats, now)
			stats << catStats
		}

		return stats
	}

	/**
	 * Returns data used to render the Event Dashboard
	 * @param moveEvent  The MoveEvent
	 * @return JSON map
	 */
	Map eventData(MoveEvent moveEvent) {
		Date sysTime = TimeUtil.nowGMT()
		MoveEventSnapshot moveEventPlannedSnapshot
		TimeDuration dayTime
		String eventString = ""
		Date eventStartTime = moveEvent.estStartTime
		Date eventCompletionTime = moveEvent.estCompletionTime
		String clockMode = CLOCK_MODE_NONE

		if (eventStartTime) {
			if ( eventStartTime > sysTime ) {
				dayTime = TimeCategory.minus(eventStartTime, sysTime)
				eventString = "Countdown Until Event"
				clockMode = CLOCK_MODE_COUNTDOWN
			} else if (eventStartTime < sysTime && ( !eventCompletionTime || eventCompletionTime > sysTime )) {
				dayTime = TimeCategory.minus(sysTime, eventStartTime)
				eventString = "Elapsed Event Time"
				clockMode = CLOCK_MODE_ELAPSED
			} else {
				dayTime = TimeCategory.minus(sysTime, eventCompletionTime)
				eventString = "Time since the event finished"
				clockMode = CLOCK_MODE_FINISHED
			}
		}
		// select the most recent MoveEventSnapshot records for the event for both the P)lanned and R)evised types
		String query = "FROM MoveEventSnapshot mes WHERE mes.moveEvent =:moveEvent AND mes.type =:type ORDER BY mes.dateCreated DESC"
		moveEventPlannedSnapshot = MoveEventSnapshot.findAll( query , [moveEvent: moveEvent, type: MoveEventSnapshot.TYPE_PLANNED] )[0]

		String eventClock = TimeUtil.formatTimeDuration(dayTime)

		return [snapshot: [
				revisedComp: moveEvent?.revisedCompletionTime,
				calcMethod: moveEvent?.calcMethod,
				systime: TimeUtil.formatDateTime(sysTime, TimeUtil.FORMAT_DATE_TIME_11),
				eventStartDate: eventStartTime,
				eventCompletionDate: eventCompletionTime,
				planSum: [
						dialInd: moveEventPlannedSnapshot?.dialIndicator,
						compTime: moveEvent.estCompletionTime,
						dayTime: eventClock,
						clockMode: clockMode,
						eventDescription: moveEvent?.description,
						eventString: eventString,
						eventRunbook: moveEvent?.runbookStatus
				]
		]]
	}

	/**
	 * Returns the color that the column should have for the given category in the Event Dashboard based on the time,
	 * if the tasks are finished and when the finished in comparison to the category planned finish time.
	 * @See TM-15896/ TM-16319
	 * @param categoryStats - The properties for the category
	 * @return - The calculated color for the category column.
	 */
	private String calculateColumnColor(Map categoryStats, Date now) {

		Long remainingTasks = categoryStats.remainingTasks
		Date maxPlannedFinish = categoryStats.maxPlannedFinish
		Date maxActualFinish = categoryStats.maxActFinish
		Date maxEstFinish = categoryStats.maxEstFinish

		String color = ""
		if (remainingTasks == 0) {
			color = maxActualFinish <= maxPlannedFinish ? '#24488A' : "red"
		} else if (remainingTasks > 0) {
			if (now > maxPlannedFinish) {
				color = "red"
			} else if (maxEstFinish > maxPlannedFinish) {
				// The projected finish is after the planned finish
				color = "yellow"
			} else {
				color = "green"
			}
		}
	}
}
