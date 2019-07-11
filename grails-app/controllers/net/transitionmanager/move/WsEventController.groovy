package net.transitionmanager.move

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.command.tag.ListCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.project.EventService
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagEvent
import net.transitionmanager.tag.TagService

/**
 * Handles WS calls of the EventService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
class WsEventController implements ControllerMethods {

	EventService eventService

	MoveEventService moveEventService
	MoveBundleService moveBundleService
	TagService tagService
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
		if (id == null) {
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

			try {
				MoveEvent moveEvent = moveEventService.update(id.toLong(), command)
				moveBundleService.assignMoveEvent(moveEvent, command.moveBundle)
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
}
