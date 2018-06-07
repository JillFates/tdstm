package net.transitionmanager.service

import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.Color
import grails.transaction.Transactional
import net.transitionmanager.domain.Tag

/**
 * A service for dealing with tags
 */
@Transactional
class TagService {
	SecurityService securityService

	/**
	 * Looks up a tag based on id.
	 *
	 * @param tagId the id of the tag
	 *
	 * @return the looked up tag, if it exist, null otherwise.
	 */
	@Transactional(readOnly = true)
	Tag get(Long tagId) {
		return Tag.get(tagId)
	}

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
	List<Map> list(String name = null, String description = null, Date dateCreated = null, Date lastUpdated = null) {
		List whereFilters = ['t.project = :project']

		Map params = [
			assetClasses   : [ETLDomain.Asset, ETLDomain.Application, ETLDomain.Device, ETLDomain.Storage],
			dependencyClass: ETLDomain.Dependency,
			taskClass      : ETLDomain.Task,
			project        : securityService.userCurrentProject
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
			whereFilters << 't.dateCreated = :dateCreated'
			params.dateCreated = dateCreated
		}

		if (lastUpdated) {
			whereFilters << 't.lastUpdated = :lastUpdated'
			params.lastUpdated = lastUpdated
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
				SUM( case when tl.domain in (:assetClasses) then 1 else 0 end) as Assets,
				SUM( case when tl.domain = :dependencyClass then 1 else 0 end) as Dependencies,
				SUM( case when tl.domain = :taskClass then 1 else 0 end) as Tasks,
				t.dateCreated as DateCreated,
				t.lastUpdated as LastModified)
			FROM Tag t
			LEFT OUTER JOIN t.tagLinks tl
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
	Tag create(String name, String description, Color color) {
		Tag tag = new Tag(name: name, description: description, color: color, project: securityService.userCurrentProject)
		return tag.save() ?: tag
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
	Tag update(Tag tag, String name = null, String description = null, Color color = null) {

		securityService.assertCurrentProject(tag.project)

		if (name) {
			tag.name = name
		}

		if (description) {
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
	void delete(Tag tag) {
		securityService.assertCurrentProject(tag.project)
		tag?.delete()
	}
}
