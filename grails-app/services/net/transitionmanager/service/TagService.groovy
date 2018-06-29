package net.transitionmanager.service

import com.tdsops.tm.enums.domain.Color
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag

/**
 * A service for dealing with tags
 */
@Transactional
class TagService implements ServiceMethods {

	/**
	 * Lists tags by project with optional filtering.
	 *
	 * @param name Used to filter the list of tags by name, or partial name.
	 * @param description Used to filter the list of tags, by description or partial description.
	 * @param dateCreated Used to filter the list of tags by the date they were created.
	 * @param lastUpdated Used to filter the list of tags, by the date they were last updated.
	 *
	 * @return list of maps containing:
	 * [
	 *     Name        : name,
	 *     Description : description,
	 *     Color       : color.name(),
	 *     ColorCss    : color.css()
	 *     Assets      : assetCount,
	 *     Dependencies: dependencyCount,
	 *     Tasks       : taskCount,
	 *     DateCreated : dateCreated,
	 *     LastModified: lastUpdated
	 * ]
	 *
	 */
	@Transactional(readOnly = true)
	List<Map> list(
		Project currentProject,
		String name = null,
		String description = null,
		Date dateCreated = null,
		Date lastUpdated = null,
		Long moveBundleId = null,
		Long moveEventId = null) {

		List whereFilters = ['t.project = :project']
		Map params = [
			project: currentProject
		]

		handleName(name, whereFilters, params)
		handleDescription(description, whereFilters, params)
		handleDateCreated(dateCreated, whereFilters, params)
		handleLastUpdated(lastUpdated, whereFilters, params)
		String eventBundleJoins = handleMoveIds(moveBundleId, moveEventId, whereFilters, params)

		String where = ''
		if (whereFilters) {
			where = "WHERE ${whereFilters.join(' and ')}"
		}

		List<Map> tags = Tag.executeQuery("""
			SELECT new Map(
				t.id as id,
				t.name as Name,
				t.description as Description,
				t.color as Color,
				COUNT(tl) as Assets,
				t.dateCreated as DateCreated,
				t.lastUpdated as LastModified)
			FROM Tag t
			LEFT OUTER JOIN t.tagAssets tl
			$eventBundleJoins
			$where
			GROUP BY t.id""".stripIndent(), params)

		tags = tags.collect { Map tag ->
			tag.css = tag.color.css
			tag.color = tag.color.name()
			return tag
		}

		return tags

	}

	/**
	 * Handles the name parameter, populating the whereFilters, and the params.
	 *
	 * @param name The name field to filter based off of.
	 * @param whereFilters The where filters to populate if name field is not null or empty string.
	 * @param params The params to map the where filters.
	 */
	void handleName(String name, List whereFilters, Map params) {
		if (name) {
			whereFilters << 't.name like :name'
			params.name = "%$name%"
		}
	}

	/**
	 * Handles the description parameter, populating the whereFilters, and the params.
	 *
	 * @param description The description field to filter based off of.
	 * @param whereFilters The where filters to populate if description field is not null or empty string.
	 * @param params The params to map the where filters.
	 */
	void handleDescription(String description, List whereFilters, Map params) {
		if (description) {
			whereFilters << 't.description like :description'
			params.description = "%$description%"
		}
	}

	/**
	 * Handles the dateCreated parameter, populating the whereFilters, and the params.
	 *
	 * @param dateCreated The dateCreated field to filter based off of.
	 * @param whereFilters The where filters to populate if dateCreated field is not null.
	 * @param params The params to map the where filters.
	 */
	void handleDateCreated(Date dateCreated, List whereFilters, Map params) {
		if (dateCreated) {
			whereFilters << '(t.dateCreated >= :dateCreatedStart AND t.dateCreated < :dateCreatedEnd)'
			params.dateCreatedStart = dateCreated.clearTime()
			params.dateCreatedEnd = dateCreated.clearTime() + 1
		}
	}

	/**
	 * Handles the lastUpdated parameter, populating the whereFilters, and the params.
	 *
	 * @param lastUpdated The lastUpdated field to filter based off of.
	 * @param whereFilters The where filters to populate if lastUpdated field is not null.
	 * @param params The params to map the where filters.
	 */
	void handleLastUpdated(Date lastUpdated, List whereFilters, Map params) {
		if (lastUpdated) {
			whereFilters << '(t.lastUpdated >= :lastUpdatedStart AND t.lastUpdated < :lastUpdatedEnd)'
			params.lastUpdatedStart = lastUpdated.clearTime()
			params.lastUpdatedEnd = lastUpdated.clearTime() + 1
		}
	}

	/**
	 * Handles the moveBundleId, and moveEventId parameters, populating the whereFilters, the params, and returning the eventBundle joins.
	 *
	 * @param moveBundleId The moveBundleId field to filter based off of.
	 * @param moveEventId The name moveEventId to filter based off of.
	 * @param whereFilters The where filters to populate if moveBundle and or the moveEvent Ids field are not null.
	 * @param params The params to map the where filters.
	 *
	 * @return If wither the moveBundleId, or the moveEventId are not null, a join string for the assets, to the moveBundle and moveEvent
	 * is returned, otherwise and empty string in returned.
	 */
	String handleMoveIds(Long moveBundleId, Long moveEventId, List whereFilters, Map params) {
		boolean returnJoins = false
		String eventBundleJoins = """
			LEFT JOIN tl.asset a
			LEFT JOIN a.moveBundle mb
			LEFT OUTER JOIN mb.moveEvent me
		"""

		if (moveBundleId) {
			whereFilters << 'mb.id = :moveBundleId'
			params.moveBundleId = moveBundleId
			returnJoins = true
		}

		if (moveEventId) {
			whereFilters << 'me.id = :moveEventId'
			params.moveEventId = moveEventId
			returnJoins = true
		}

		return returnJoins ? eventBundleJoins : ''
	}

	/**
	 * Creates a new tag.
	 *
	 * @param name The name of the tag.
	 * @param description A description of the tag.
	 * @param color The Color of the tag.
	 *
	 * @return returns the new tag, or the tag object with error set.
	 */
	Tag create(Project currentProject, String name, String description, Color color) {
		Tag tag = new Tag(name: name, description: description, color: color, project: currentProject)
		return tag.save(failOnError: true)
	}

	/**
	 * Updates a tag.
	 *
	 * @param tag The tag to update.
	 * @param name The tags new name, skipped, if blank or null.
	 * @param description The Tags, new description, skipped, if blank or null.
	 * @param color The Tags, new color, skipped if blank or null.
	 *
	 * @return The updated tag
	 */
	Tag update(Long id, Project currentProject, String name = null, String description = null, Color color = null) {
		Tag tag = get(Tag, id, currentProject)

		if (name) {
			tag.name = name
		}

		if (description) {
			tag.description = description
		}

		if (color) {
			tag.color = color
		}

		return tag.save(failOnError: true)
	}

	/**
	 * Deletes a tag.
	 *
	 * @param tag the tag to delete.
	 */
	void delete(Long id, Project currentProject) {
		Tag tag = get(Tag, id, currentProject)
		tag.delete(failOnError: true)
	}
}
