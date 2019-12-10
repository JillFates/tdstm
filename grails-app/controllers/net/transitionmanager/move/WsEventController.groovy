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
import com.tdssrc.grails.TimeUtil

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
		List<Map> eventsMap = events.collect { MoveEvent event ->
			Map presetPropertiesMap = ['moveBundlesString': event.getMoveBundlesString()]
			GormUtil.domainObjectToMap(event, MoveEvent.DETAILED_LIST_FIELDS, null, false, presetPropertiesMap)
		}
		renderSuccessJson(eventsMap)
	}

	@HasPermission(Permission.EventView)
	def getModelForViewEdit(Long moveEventId) {
		ListCommand filter = populateCommandObject(ListCommand)
		Project project = getProjectForWs()
		MoveEvent moveEvent

		// Use the event id in the parameter, fetch the user preference otherwise.
		if (moveEventId) {
			// Fail if the move event doesn't exist or it doesn't belong to the user's current project.
			moveEvent = fetchDomain(MoveEvent, [id: moveEventId], project)
			userPreferenceService.setPreference(UserPreferenceEnum.MOVE_EVENT, moveEventId)
			Long moveBundleId = NumberUtil.toLong(userPreferenceService.moveBundleId)
			if (moveBundleId) {
				MoveBundle moveBundle = fetchDomain(MoveBundle, [id: moveBundleId], project)
				if (moveBundle?.moveEvent?.id != moveEventId) {
					userPreferenceService.removePreference(UserPreferenceEnum.CURR_BUNDLE)
				}
			}
		} else {
			moveEventId =  NumberUtil.toLong(userPreferenceService.getPreference(UserPreferenceEnum.MOVE_EVENT))
			moveEvent = fetchDomain(MoveEvent, [id: moveEventId], project)
		}

		renderSuccessJson([
			moveEventInstance: moveEvent,
			availableBundles: moveBundleService.lookupList(project),
			selectedBundles: moveEvent.moveBundles.collect { MoveBundle moveBundle ->
				[id: moveBundle.id, name: moveBundle.name]
			},
			runbookStatuses: GormUtil.getConstrainedProperties(MoveEvent).runbookStatus.inList,
			selectedTags: moveEvent.tagEvents.collect { TagEvent tagEvent ->
				tagEvent.tag
			},
			tags: tagService.list(project, filter.name, filter.description, filter.dateCreated, filter.lastUpdated,
				filter.bundleId ?[filter.bundleId] : [], filter.eventId)
		])
	}

	@HasPermission(Permission.EventCreate)
	def getModelForCreate() {
		Project project = securityService.userCurrentProject
		List<Map> bundles = moveBundleService.lookupList(project)
		List runbookStatuses = GormUtil.getConstrainedProperties(MoveEvent).runbookStatus.inList
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
	def save(Long moveEventId) {
		CreateEventCommand command = populateCommandObject(CreateEventCommand)
		Project project = getProjectForWs()
		MoveEvent moveEvent = moveEventService.createOrUpdate(project, command, moveEventId)
		renderSuccessJson(GormUtil.domainObjectToMap(moveEvent))
	}

	@HasPermission(Permission.EventDelete)
	def delete(Long moveEventId) {
		MoveEvent moveEvent = fetchDomain(MoveEvent, [id: moveEventId])
		moveEventService.deleteMoveEvent(moveEvent)
		renderSuccessJson("Move Event ${moveEvent.name} deleted.")
	}

	/**
	 * Find and return various task-related stats per category for the given event.
	 * @param moveEvent - The event id.
	 * @param viewUnpublished - Whether or not unpublished tasks should be included in the model.
	 * @return  A model with Event Dashboard info
	 */
	@HasPermission(Permission.DashboardMenuView)
	def getEventDashboardModel() {
		Long moveEventId = NumberUtil.toPositiveLong(getParamOrPreference('moveEvent', UserPreferenceEnum.MOVE_EVENT))
		if (!moveEventId) {
			throw new InvalidParamException('A valid Move Event ID is needed.')
		}

		Project project = getProjectForWs()
		MoveEvent moveEvent = GormUtil.findInProject(project, MoveEvent, moveEventId, true)

		boolean viewUnpublished = false
		// handle the view unpublished tasks checkbox
		if (params.containsKey('viewUnpublished') && securityService.hasPermission(Permission.TaskViewUnpublished)) {
			viewUnpublished = params.viewUnpublished == '1'
			userPreferenceService.setPreference(UserPreferenceEnum.VIEW_UNPUBLISHED, viewUnpublished)
		}

		// Save the user's preference for the current move event
		userPreferenceService.setMoveEventId(moveEvent.id)

		renderSuccessJson([model: dashboardService.getEventDashboardModel(project, moveEvent, viewUnpublished)])
	}

	/*
	 * Will update the moveEvent calcMethod = M and create a MoveEventSnapshot for summary dialIndicatorValue
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
				dialIndicator: dialIndicator, type: MoveEventSnapshot.TYPE_PLANNED)
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
     * Used to retrieve the list of categories and various metrics for each for a given event. This is used by the
     * Event Dashboard for the lower category data elements.
     * @param id - (Long) the id of the event to retrieve metrics from
     * @param viewUnpublished - a Boolean flag if true will include unpublished tasks in the results
     */
	def taskCategoriesStats(Long id) {
		Project project = getProjectForWs()
		MoveEvent moveEvent = fetchDomain(MoveEvent, [id: id], project)
		boolean viewUnpublished = false
		// handle the view unpublished tasks checkbox
		if (params.containsKey('viewUnpublished') && securityService.hasPermission(Permission.TaskViewUnpublished)) {
			viewUnpublished = params.viewUnpublished == '1'
			userPreferenceService.setPreference(UserPreferenceEnum.VIEW_UNPUBLISHED, viewUnpublished)
		}
		renderSuccessJson(moveEventService.getTaskCategoriesStats(project, moveEvent, viewUnpublished))
	}
}
