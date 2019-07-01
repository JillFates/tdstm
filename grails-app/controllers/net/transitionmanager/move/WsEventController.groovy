package net.transitionmanager.move

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.event.CreateEventCommand
import net.transitionmanager.command.tag.ListCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.project.EventService
import net.transitionmanager.project.MoveEventService
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
	def saveEvent() {
		CreateEventCommand event = populateCommandObject(CreateEventCommand)
		Project currentProject = securityService.userCurrentProject

		MoveEvent moveEvent = moveEventService.save(event, currentProject)

		if (!moveEvent.hasErrors()) {
			flash.message = "MoveEvent $moveEvent.name created"
			renderSuccessJson()
		}
		else {
			flash.message = moveEvent.errors
			renderErrorJson(flash.message)
		}
	}
}
