package net.transitionmanager.imports

import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.person.FavoriteDataview
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import org.grails.web.json.JSONObject

/**
 * Database table mapping for 'report'.
 * Represents the properties configuration and definition of a report
 * which is mainly used across Asset Explorer feature.
 */
class Dataview {

	Project project
	Person  person
	String  name
	Boolean isSystem = false
	Boolean isShared = false
	String  reportSchema
	Date    dateCreated
	Date    lastModified
	Dataview overridesView

	static constraints = {
		name size: 1..255, unique: ['project','person', 'isShared'], validator: uniqueNameValidator()
		person nullable: true
		lastModified nullable: true
		overridesView nullable: true
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
			id        : id,
			name      : name,
			isSystem  : isSystem,
			isShared  : isShared,
			isOwner   : isOwner(currentPersonId),
			isFavorite: isFavorite(currentPersonId),
			isOverride: isOverrideView(),
			schema    : schemaAsJSONObject(),
			createdBy : getOwnerName(),
			createdOn : TimeUtil.formatDate(dateCreated),
			overridesView: overridesView,
			updatedOn : TimeUtil.formatDate(lastModified)
		]
		return data
	}

	/**
	 * Determines if this person dataview is the owner of it.
	 * @param currentPersonId current person in session.
	 * @return boolean
	 */
	boolean isOwner(Long currentPersonId) {
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
	 * Determine if the dataview has an override view
	 * @return
	 */
	boolean isOverrideView() {
		return overridesView != null
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

	/**
	 * Returns the schema as a Map instead of a JSON string
	 * @return
	 */
	JSONObject schemaAsJSONObject() {
		reportSchema ? JsonUtil.parseJson(reportSchema) : null
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
	static Closure uniqueNameValidator() {
		// TODO: Insert constraints: isShared and person
		return { value, target ->
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
}