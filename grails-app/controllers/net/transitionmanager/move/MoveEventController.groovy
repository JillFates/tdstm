package net.transitionmanager.move

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.TimeUtil
import com.tdsops.common.sql.SqlUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.common.ControllerService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.ServiceException
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.MoveEventSnapshot
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.Permission
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskService
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.Workbook
import org.hibernate.criterion.Order
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.Timestamp

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class MoveEventController implements ControllerMethods, PaginationMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	ControllerService controllerService
	JdbcTemplate jdbcTemplate
	MoveBundleService moveBundleService
	MoveEventService moveEventService
	ProjectService projectService
	TaskService taskService
	UserPreferenceService userPreferenceService

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

			// append recent tasks  whose status is completed
			def transitionComment = new StringBuilder()
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

			def moveEventSnapshot = MoveEventSnapshot.executeQuery('''
				FROM MoveEventSnapshot WHERE moveEvent=?0 AND type=?1
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
	def exportRunbookToExcel(Long eventId) {
		MoveEvent moveEvent = moveEventService.findById(eventId, false)
		if (! moveEvent) return

		// TODO : SLC 11/2018 : this try/catch can be removed when this view gets moved to Angular
		try {
			boolean updateRunbookVersion = params.version == 'on'
			Map<String, ?> result = moveEventService.exportRunbookToExcel(moveEvent, updateRunbookVersion)
			ExportUtil.sendWorkbook(result['book'] as Workbook, response, result['filename'] as String)
		} catch(ServiceException e) {
			log.error('Error exporting Runbook to Excel', e)
		}
	}
}
