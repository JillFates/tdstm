package net.transitionmanager.tag

import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.service.ServiceMethods

/**
 * A service for managing the relationship of Tags to Events.
 */
@Transactional
class TagEventService implements ServiceMethods {
	MoveEventService moveEventService

	/**
	 * Gets a list of tagEvents for an event.
	 *
	 * @param event the event to get tagEvents for.
	 *
	 * @return A list of tagEvents for an event.
	 */
	@Transactional(readOnly = true)
	List<TagEvent> list(Project currentProject, Long eventId) {
		MoveEvent event = get(MoveEvent, eventId, currentProject)

		return TagEvent.findAllWhere(event: event)
	}

	/**
	 * Gets a list of tags for an event.
	 *
	 * @param event the event to get tags for.
	 *
	 * @return A list of tags for an event.
	 */
	@Transactional(readOnly = true)
	List<Tag> getTags(Project currentProject, Long eventId) {
		MoveEvent eventToLookUp = get(MoveEvent, eventId, currentProject)

		return TagEvent.where {
			event == eventToLookUp
			projections {
				property("tag")
			}
		}.list(sort: "id", order: "asc")
	}

	/**
	 * Creates tagEvents for a list of tags, to an event, and returns the list of tagEvents.
	 *
	 * @param tagIds The ids of the tags to apply create tagEvents for the event.
	 * @param event The event to make tagEvents for.
	 *
	 * @return A List of tagEvents linking tags, to events.
	 */
	List<TagEvent> applyTags(Project currentProject, List<Long> tagIds, Long eventId) {
		MoveEvent event = get(MoveEvent, eventId, currentProject)

		List<TagEvent> tagEvents = tagIds.collect { Long tagId ->
			Tag tag = get(Tag, tagId, currentProject)

			TagEvent tagEvent = new TagEvent(tag: tag, event: event)
			event.refresh()
			return tagEvent.save()
		}

		// Bump the last updated date for the given asset.
		event.lastUpdated = TimeUtil.nowGMT()
		event.save()

		return tagEvents
	}

	/**
	 * Removes a tagEvents by id.
	 *
	 * @param tagEventIds the id of the TagEvent to remove.
	 */
	void removeTags(Project currentProject, List<Long> tagEventIds) {
		Set<Long> eventIds = []

		tagEventIds.each { Long id ->
			TagEvent tagEvent = get(TagEvent, id, currentProject)

			if (currentProject.id != tagEvent?.tag?.project?.id) {
				securityService.reportViolation("attempted to access event from unrelated project (event ${tagEvent?.id})")
				throw new EmptyResultException()
			}

			eventIds << tagEvent.event.id

			tagEvent.delete(flush: true)
		}

		moveEventService.bulkBumpMoveEventLastUpdated(currentProject, eventIds)
	}
}
