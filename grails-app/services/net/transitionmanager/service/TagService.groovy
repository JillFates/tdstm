package net.transitionmanager.service

import com.tdsops.tm.enums.domain.Color
import grails.gorm.transactions.Transactional
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
		List<Long> moveBundleIds = null,
		Long moveEventId = null) {

		List whereFilters = ['t.project = :project']
		Map params = [
			project: currentProject
		]

		handleName(name, whereFilters, params)
		handleDescription(description, whereFilters, params)
		handleDateCreated(dateCreated, whereFilters, params)
		handleLastUpdated(lastUpdated, whereFilters, params)
		String eventBundleJoins = handleMoveIds(moveBundleIds, moveEventId, whereFilters, params)

		String where = ''
		if (whereFilters) {
			where = "WHERE ${whereFilters.join(' and ')}"
		}

		List<Map> tags = Tag.executeQuery("""
			SELECT new Map(
				t.id as id,
				t.name as name,
				t.description as description,
				t.color as color,
				COUNT(tl) as assets,
				t.dateCreated as dateCreated,
				t.lastUpdated as lastModified)
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
	String handleMoveIds(List<Long> moveBundleIds, Long moveEventId, List whereFilters, Map params) {
		boolean returnJoins = false
		String eventBundleJoins = """
			LEFT JOIN tl.asset a
			LEFT JOIN a.moveBundle mb
			LEFT OUTER JOIN mb.moveEvent me
		"""

		if (moveBundleIds) {
			whereFilters << 'mb.id in (:moveBundleIds)'
			params.moveBundleIds = moveBundleIds
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
		return tag.save()
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

		if(description != null) {
			tag.description = description
		}

		if (color) {
			tag.color = color
		}

		return tag.save()
	}

	/**
	 * Deletes a tag.
	 *
	 * @param tag the tag to delete.
	 */
	void delete(Long id, Project currentProject) {
		Tag tag = get(Tag, id, currentProject)
		tag.delete()
	}

	/**
	 * Clone any existing tags associated to sourceProject (if any),
	 * then associate those newly created tags to targetProject.
	 *
	 * @param sourceProject  The project from which the existing tags will be cloned.
	 * @param targetProject  The project to which the new tags will be associated.
	 */
	void cloneProjectTags(Project sourceProject, Project targetProject) {

		List<Tag> tags = Tag.where {
			project == sourceProject
		}.list()

		if (!tags.isEmpty()) {
			tags.each { Tag tag ->
				Tag newTag = new Tag(
						name: tag.name,
						description: tag.description,
						color: tag.color,
						project: targetProject
				)

				newTag.save(failOnError: false)
				log.debug "Cloned tag ${tag.name} for project ${targetProject.toString()}"
			}
		}
	}

	/**
	 * Generates up the query for filtering by tags, if there are any. To use this the asset_entity table must be joined with the alias a.
	 *
	 * @param tagIds The tag ids to filter by.
	 * @param tagMatch To filter multiple tag ids using AND/All Logic,, or use OR/ANY logic.
	 *
	 * @return the query for filtering by tags, using AND/OR, or and empty string, if there are no tags to filter by.
	 */
	String getTagsQuery(List<Long> tagIds, String tagMatch, Map queryParams) {

		if (!tagIds) {
			return ''
		}

		if (tagMatch == 'ANY') {
			queryParams.tagIds = tagIds
			return "AND t.tag_id in (:tagIds)"

		} else {
			queryParams.tagIds = tagIds
			queryParams.tagIdsSize = tagIds.size()
			return "AND a.asset_entity_id in(SELECT ta2.asset_id FROM tag_asset ta2 WHERE ta2.tag_id in (:tagIds) GROUP BY ta2.asset_id HAVING count(*) = :tagIdsSize)"
		}
	}

	/**
	 * Generates the joins used for a query using tags. To use this the asset_entity table must be joined with the alias a.
	 *
	 * @param tagIds The tag ids to filter by.
	 * @param tagMatch To filter multiple tag ids using AND/All Logic,, or use OR/ANY logic.
	 *
	 * @return  the joins for a query that uses tags. If there are tag ids and the tag match is set to any, then the tag joins are added,
	 * otherwise the tag joins are not added, because of the way the tags are filtered in get TagsQuery(sub select).
	 */
	String getTagsJoin(List<Long> tagIds, String tagMatch) {

		if (tagIds && tagMatch == 'ANY') {
			return """
					LEFT OUTER JOIN tag_asset ta ON a.asset_entity_id = ta.asset_id
					LEFT OUTER JOIN tag t ON ta.tag_id = t.tag_id
				"""
		}

		return ''
	}
}
