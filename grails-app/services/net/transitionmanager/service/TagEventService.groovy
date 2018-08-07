package net.transitionmanager.service

import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagEvent

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
			return tagEvent.save(failOnError: true)
		}

		// Bump the last updated date for the given asset.
		event.lastUpdated = TimeUtil.nowGMT()
		event.save(failOnError: true)

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

		moveEventService.bulkBumpMoveEventLastUpdated(currentProject, ":eventIds", [eventIds: eventIds])
	}



	/**
	 * Coerces the string value passed from the BulkChangeService to a List of longs, and validates them as tag ids.
	 *
	 * @param currentProject the current project passed from the controller for use in validating the tag ids.
	 * @param value The string value that need to be coerce.
	 *
	 * @return a List of longs, that represent tag ids.
	 */
	List<Long> coerceBulkValue(Project currentProject, String value) {
		JsonSlurper jsonSlurper = new JsonSlurper()
		def parsedValue = jsonSlurper.parseText(value)

		if(!(parsedValue instanceof List)){
			throw new InvalidParamException('Value is not a list of numbers')
		}

		List<Long> tagIds = (parsedValue).collect { i -> i.toLong() }

		if(tagIds) {
			validateBulkValues(currentProject, tagIds)
		}

		return tagIds
	}

	/**
	 * Validates the tagId sent in to make sure they are valid for the project, by looking them up.
	 *
	 * @param currentProject the current project passed down through the controller.
	 *
	 * @param tagIds the ids to validate.
	 */
	void validateBulkValues(Project currentProject, List<Long> tagIds) {
		int tagCount = Tag.where {id in tagIds && project == currentProject}.count()

		if(tagCount != tagIds.size()){
			throw new InvalidParamException('One or more tags specified were not found')
		}
	}

	/**
	 * Bulk adds tags to events.
	 *
	 * @param tagIds The ids of the tags to add.
	 * @param eventIds The events to add TagEventLinks to. this can be an empty list meaning that the filtering query will be used instead.
	 * @param eventIdsFilterQuery If eventIds are not specified  this query and params are added to the query to apply to all events,
	 * from the filtering in the frontend.
	 */
	void bulkAdd(List<Long> tagIds, List<Long> eventIds = [], Map eventIdsFilterQuery = null) {
		String queryForEventIds
		Map params = [:]
		Map eventQueryParams = [:]

		if (eventIds && !eventIdsFilterQuery) {
			queryForEventIds = ':eventIds'
			params.eventIds = eventIds
			eventQueryParams.eventIds = eventIds
		} else {
			queryForEventIds = eventIdsFilterQuery.query
			params << eventIdsFilterQuery.params
			eventQueryParams = eventIdsFilterQuery.params
		}

		params.tagIds = tagIds

		String query = """
			INSERT into TagEvent
			(event, tag, dateCreated)
			SELECT
				e as event,
				t as tag,
				current_date() as dateCreated
			FROM MoveEvent e, Tag t
			WHERE e.id in ($queryForEventIds) AND t.id in (:tagIds)
			AND NOT EXISTS(FROM TagEvent te WHERE te.event = e AND te.tag = t)
		"""

		TagEvent.executeUpdate(query, params)
		moveEventService.bulkBumpMoveEventLastUpdated(securityService.userCurrentProject, queryForEventIds, eventQueryParams)
	}

	/**
	 * Bulk clears tags for events.
	 *
	 * @param tagIds should be null because it is not used. The parameter is specified so that all the bulk methods have the same signature.
	 * @param eventIds The ids of the events to remove tags from
	 * @param eventIdsFilterQuery filtering query to use if eventIds are not present
	 */
	void bulkClear(List<Long> tagIds = null, List<Long> eventIds = [], Map eventIdsFilterQuery = null){
		if (tagIds) {
			throw new InvalidParamException("Specifying Tag IDs is invalid when clearing all tags")
		}

		remove([], eventIds, eventIdsFilterQuery)
	}

	/**
	 * Bulk removes tags from events.
	 *
	 * @param tagIds The ids of the tags to be removed.
	 * @param eventIds The ids of the events to remove tags from.
	 * @param eventIdsFilterQuery filtering query to use if eventIds are not present.
	 */
	void bulkRemove(List<Long> tagIds, List<Long> eventIds = [], Map eventIdsFilterQuery = null){
		if(!tagIds){
			throw new InvalidParamException("Tag IDs must be specified for removal")
		}

		remove(tagIds, eventIds, eventIdsFilterQuery)
	}

	/**
	 * Bulk removes tags from events.
	 *
	 * @param tagIds The tags to remove, is it's an empty list, all tags will be removed.
	 * @param eventIds The ids of the events to remove tags from.
	 * @param eventIdsFilterQuery filtering query to use if eventIds are not present.
	 */
	private void remove(List<Long> tagIds, List<Long> eventIds, Map eventIdsFilterQuery = null) {
		String queryForEventIds
		String queryForTagIds = ''
		Map params = [:]
		Map eventQueryParams = [:]

		if (eventIds && !eventIdsFilterQuery) {
			queryForEventIds = ':eventIds'
			params.eventIds = eventIds
			eventQueryParams.eventIds = eventIds
		} else {
			queryForEventIds = eventIdsFilterQuery.query
			params << eventIdsFilterQuery.params
			eventQueryParams = eventIdsFilterQuery.params
		}

		if (tagIds) {
			queryForTagIds = 'and tag.id in(:tagIds)'
			params.tagIds = tagIds
		}

		String query = """
			delete from TagEvent
			where  event.id in($queryForEventIds) $queryForTagIds
		"""

		TagEvent.executeUpdate(query, params)
		moveEventService.bulkBumpMoveEventLastUpdated(securityService.userCurrentProject, queryForEventIds, eventQueryParams)
	}

	/**
	 * Replaces the current tags with new ones.
	 *
	 * @param tagIds The tag ids to replace the current ones for the events.
	 * @param eventIds The ids of the events to replace tags for.
	 * @param eventIdsFilterQuery filtering query to use if eventIds are not present.
	 */
	void bulkReplace(List<Long> tagIds, List<Long> eventIds = [], Map eventIdsFilterQuery = null) {
		bulkClear([], eventIds, eventIdsFilterQuery)
		bulkAdd(tagIds, eventIds, eventIdsFilterQuery)
	}
}
