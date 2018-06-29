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
	List<Map> list(Project currentProject, String name = null, String description = null, Date dateCreated = null, Date lastUpdated = null) {
		List whereFilters = ['t.project = :project']

		Map params = [
			project        : currentProject
		]

		if (name) {
			whereFilters << 't.name like :name'
			params.name = "%$name%"
		}

		if (description) {
			whereFilters << 't.description like :description'
			params.description = "%$description%"
		}

		if (dateCreated) {
			whereFilters << '(t.dateCreated >= :dateCreatedStart AND t.dateCreated < :dateCreatedEnd)'
			params.dateCreatedStart = dateCreated.clearTime()
			params.dateCreatedEnd = dateCreated.clearTime() + 1
		}

		if (lastUpdated) {
			whereFilters << '(t.lastUpdated >= :lastUpdatedStart AND t.lastUpdated < :lastUpdatedEnd)'
			params.lastUpdatedStart = lastUpdated.clearTime()
			params.lastUpdatedEnd = lastUpdated.clearTime() + 1
		}

		String where = ''
		if (whereFilters) {
			where = "WHERE ${whereFilters.join(' and ')}"
		}


		List<Map> tags =  Tag.executeQuery("""
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
			$where
			GROUP BY t.id""".stripIndent(), params)

		tags = tags.collect{ Map tag ->
			tag.css = tag.Color.css
			tag.Color = tag.Color.name()
			return tag
		}

		return tags

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
				newTag.save()
				log.debug "Cloned tag ${tag.name} for project ${targetProject.toString()}"
			}
		}
	}
}
