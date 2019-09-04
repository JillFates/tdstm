package net.transitionmanager.move

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import net.transitionmanager.command.IdsCommand
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.command.tag.ListCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventSnapshot
import net.transitionmanager.project.Project
import net.transitionmanager.reporting.DashboardService
import net.transitionmanager.security.Permission
import net.transitionmanager.project.EventService
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagEvent
import net.transitionmanager.tag.TagEventService
import net.transitionmanager.tag.TagService
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Handles WS calls of the EventService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
class WsEventController implements ControllerMethods {

	DashboardService dashboardService
	EventService eventService
	JdbcTemplate jdbcTemplate
	MoveEventService moveEventService
	MoveBundleService moveBundleService
	TagService tagService
	TagEventService tagEventService
	UserPreferenceService userPreferenceService

	@HasPermission(Permission.EventView)
	def listEventsAndBundles() {
		renderSuccessJson(list: eventService.listEventsAndBundles())
	}

	/**
	 * All bundles associated to a specified move event or if id=0 then unassigned bundles
	 * for the user's current project
	 */
	@HasPermission(Permission.BundleView)
	def listBundles(Long id, Boolean useForPlanning) {
		renderSuccessJson(list: eventService.listBundles(id, useForPlanning))
	}

	/**
	 * List all available Move Events for the user's current project.
	 */
	@HasPermission(Permission.EventView)
	def listEvents() {
		Project project = getProjectForWs()
		List<MoveEvent> events = moveEventService.listMoveEvents(project)
		List<Map> eventsMap = events.collect { event ->
			formatEventsForMap(event)
		}
		renderSuccessJson(eventsMap)
	}

	def formatEventsForMap(MoveEvent event) {
		Map eventMap = GormUtil.domainObjectToMap(event, MoveEvent.BASIC_EVENT_FIELDS, null, false)
		eventMap.put('moveBundlesString', event.getMoveBundlesString())
		return eventMap
	}

	@HasPermission(Permission.EventView)
	def getModelForViewEdit(String id) {
		Project project = getProjectForWs()
        String moveEventId = id
		List availableBundles = moveBundleService.lookupList(project)
		List runbookStatuses = com.tdssrc.grails.GormUtil.getConstrainedProperties(MoveEvent).runbookStatus.inList
		ListCommand filter = populateCommandObject(ListCommand)
		List<Map> tags = tagService.list(
				projectForWs,
				filter.name,
				filter.description,
				filter.dateCreated,
				filter.lastUpdated,
				filter.bundleId ?[filter.bundleId] : [],
				filter.eventId
		)
		if (moveEventId) {
			userPreferenceService.setPreference(UserPreferenceEnum.MOVE_EVENT, moveEventId)
			def moveBundleId = userPreferenceService.moveBundleId
			if (moveBundleId) {
				def moveBundle = MoveBundle.get(moveBundleId)
				if (moveBundle?.moveEvent?.id != Integer.parseInt(moveEventId)) {
					userPreferenceService.removePreference(UserPreferenceEnum.CURR_BUNDLE)
				}
			}
		}
		else {
			moveEventId = userPreferenceService.getPreference(UserPreferenceEnum.MOVE_EVENT)
		}

		if (!moveEventId) {
			renderErrorJson()
			return
		}

		MoveEvent moveEvent = MoveEvent.get(moveEventId)
		if (!moveEvent) {
			flash.message = "MoveEvent not found with id $moveEventId"
			renderErrorJson(flash.message)
			return
		}

		List selectedBundles = moveEvent.moveBundles.collect { MoveBundle moveBundle ->
			[id: moveBundle.id, name: moveBundle.name]
		}

		List selectedTags = moveEvent.tagEvents.collect { TagEvent tagEvent ->
			tagEvent.tag
		}

		renderSuccessJson([moveEventInstance: moveEvent, availableBundles: availableBundles, selectedBundles: selectedBundles, runbookStatuses: runbookStatuses, selectedTags: selectedTags, tags: tags])
	}

	@HasPermission(Permission.EventCreate)
	def getModelForCreate() {
		Project project = securityService.userCurrentProject
		List bundles = moveBundleService.lookupList(project)
		List runbookStatuses = com.tdssrc.grails.GormUtil.getConstrainedProperties(MoveEvent).runbookStatus.inList
		ListCommand filter = populateCommandObject(ListCommand)
		List<Map> tags = tagService.list(
				projectForWs,
				filter.name,
				filter.description,
				filter.dateCreated,
				filter.lastUpdated,
				filter.bundleId ?[filter.bundleId] : [],
				filter.eventId
		)

		renderSuccessJson([bundles: bundles, runbookStatuses: runbookStatuses, tags: tags])
	}

	@HasPermission(Permission.EventCreate)
	def saveEvent(String id) {
		if (id == null || id == 'null') {
			CreateEventCommand event = populateCommandObject(CreateEventCommand)
			Project currentProject = securityService.userCurrentProject

			MoveEvent moveEvent = moveEventService.save(event, currentProject)

			if (!moveEvent.hasErrors()) {
				flash.message = "MoveEvent $moveEvent.name created"
				renderSuccessJson()
			}
			else {
				flash.message = moveEvent.errors.toString()
				renderErrorJson(flash.message)
			}
		}
		else {
			// populate create event command from request
			CreateEventCommand command = populateCommandObject(CreateEventCommand)
			Project project = getProjectForWs()

			try {
				MoveEvent moveEvent = moveEventService.update(id.toLong(), command)
				moveBundleService.assignMoveEvent(moveEvent, command.moveBundle)

                List tagEventsToDelete = moveEvent.tagEvents.findAll { !command.tagIds.contains( it.tagId ) } // Find all tag event ids to delete
				List<Long> tagEventIdsToDelete = tagEventsToDelete.collect { it -> it.id }
                moveEvent.tagEvents.removeAll(tagEventsToDelete)
				
				if (tagEventIdsToDelete.size() > 0) {
					tagEventService.removeTags(project, tagEventIdsToDelete)
				}

				List tagEventTagIds = moveEvent.tagEvents.collect { it -> it.tagId} // Get all the tagIds of the tagEvents
				List tagIdsToAdd = command.tagIds.findAll { !tagEventTagIds.contains( it )} // Find all tags to add

				if (command.tagIds) {
					tagEventService.applyTags(project, tagIdsToAdd, moveEvent.id)
				}
				flash.message = "MoveEvent '$moveEvent.name' updated"
				renderSuccessJson()
			} catch (EmptyResultException e) {
				flash.message = "MoveEvent not found with id $params.id"
				renderErrorJson(flash.message)
			} catch (ValidationException e) {
				renderErrorJson(e.message)
			}
		}
	}

	@HasPermission(Permission.EventDelete)
	def deleteEvent(String id) {
		try {
			MoveEvent moveEvent = MoveEvent.get(id)
			if (moveEvent) {
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
		renderSuccessJson()
	}

	@HasPermission(Permission.DashboardMenuView)
	def getEventDashboardModel() {
		Long moveEventId = NumberUtil.toPositiveLong(getParamOrPreference('moveEvent', UserPreferenceEnum.MOVE_EVENT))
		if (!moveEventId) {
			throw new InvalidParamException('A valid Move Event ID is needed.')
		}

		Project project = getProjectForWs()
		MoveEvent moveEvent = GormUtil.findInProject(project, MoveEvent, moveEventId, true)

		// handle the view unpublished tasks checkbox
		if (params.containsKey('viewUnpublished')) {
			userPreferenceService.setPreference(UserPreferenceEnum.VIEW_UNPUBLISHED, params.viewUnpublished == '1')
		}

		boolean viewUnpublished = securityService.viewUnpublished()

		// Save the user's preference for the current move event
		userPreferenceService.setMoveEventId(moveEvent.id)

		renderSuccessJson([model: dashboardService.getEventDashboardModel(project, moveEvent, viewUnpublished)])
	}


	/*
	 * will update the moveEvent calcMethod = M and create a MoveEventSnapshot for summary dialIndicatorValue
	 * @param  : moveEventId and moveEvent dialIndicatorValue
	 */
	@HasPermission(Permission.EventEdit)
	def updateEventSummary() {
		Map requestParams  = request.JSON ?: params

		MoveEvent moveEvent = MoveEvent.get(requestParams.moveEventId)
		Integer dialIndicator
		if (StringUtil.toBoolean(requestParams.checkbox)) {
			dialIndicator = NumberUtil.toPositiveInteger(requestParams.value)
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
		}
		renderSuccessJson("success")
	}


	/**
	 * Find and return various task-related stats per category for the given event.
	 */
	def taskCategoriesStats(Long moveEventId) {
		Project project = getProjectForWs()
		renderSuccessJson(moveEventService.getTaskCategoriesStats(project, moveEventId))
	}
}
