package net.transitionmanager.domain

import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.TimeUtil

/**
 * Database table mapping for 'report'.
 * Represents the properties configuration and definition of a report
 * which is mainly used across Asset Explorer feature.
 */
class Dataview {

	Project project
	Person person
	String name
	Boolean isSystem
	Boolean isShared
	String reportSchema
	Date dateCreated
	Date lastModified

    static constraints = {
			name size: 1..255, unique: 'project', validator: uniqueNameValidator
			person nullable: true
			lastModified nullable: true
    }

	static mapping = {
		name sqltype: 'varchar(255)'
	}

	/**
	 * Converts current report object to a map.
	 * @param currentPersonId current person in session.
	 * @return
	 */
	Map toMap(Long currentPersonId) {

		Map data = [
				id				: id,
				name			: name,
				isSystem		: isSystem,
				isShared		: isShared,
				isOwner			: isOwner(currentPersonId),
				isFavorite		: isFavorite(currentPersonId),
				schema			: JsonUtil.parseJson(reportSchema),
				createdBy		: getOwnerName(),
				createdOn		: TimeUtil.formatDate(dateCreated),
				updatedOn		: TimeUtil.formatDate(lastModified)
		]
		return data
	}

	/**
	 * Determines if this person dataview is the owner of it.
	 * @param currentPersonId current person in session.
	 * @return boolean
	 */
	boolean isOwner (Long currentPersonId) {
		if (!person) {
			return false
		} else {
			return (person.id == currentPersonId)
		}
	}

	/**
	 * Determine if the instance is a favorite view for the person.
	 * @param currentPersonId
	 * @return
	 */
	boolean isFavorite(Long currentPersonId) {
		int favCount = FavoriteDataview.where {
			dataview == this
			person.id == currentPersonId
		}.count()
		return favCount > 0
	}

	/**
	 * Returns the name of the dataview owner person.
	 * If this dataview doesn't have a person associated, then returns empty string.
	 * @return String
	 */
	String getOwnerName() {
		if (person) {
			return person.toString()
		}
		return ''
	}

	def beforeInsert = {
		dateCreated = dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}

	/**
	 * Used to validate that name is unique within the project
	 */
	static Closure uniqueNameValidator = { value, target ->
		int count = Dataview.where {
			project == target.project
			name == value
			if (id) {
				id != target.id
			}
		}.count()

		if (count > 0) {
			return 'default.not.unique.message'
		}
	}
}
