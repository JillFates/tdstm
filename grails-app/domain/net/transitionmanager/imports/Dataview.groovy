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
		name size: 1..255
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
	Map toMap(Project project, Person whom) {

		Map data = [
			id           : id,
			name         : name,
			hasOverride  : hasOverride(project),
			isGlobal     : this.project.isDefaultProject(),
			isSystem     : isSystem,
			isShared     : isShared,
			isOwner      : isOwner(whom.id),
			isFavorite   : isFavorite(whom.id),
			isOverride   : isOverrideView(),
			schema       : schemaAsJSONObject(),
			createdBy    : getOwnerName(),
			createdOn    : TimeUtil.formatDate(dateCreated),
			overridesView: overridesView,
			updatedOn    : TimeUtil.formatDate(lastModified)
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
		Long dvId = this.id
		int favCount = FavoriteDataview.where {
			dataview.id == dvId
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
	 * Used to determine if the current view has an override
	 * @param project
	 * @return returns true if the current view is a system and there is one or more overridden versions of the view
	 * in the default project and/or in the project referenced.
	 */
	boolean hasOverride(Project project) {
		boolean overridden = false
		if (id && isSystem && project) {
			// Note that the where closure didn't work correctly reference id directly, hence the dvId variable
			Long dvId = this.id		
			overridden = Dataview.where {
					project.id in [project.id, Project.DEFAULT_PROJECT_ID]
					overridesView.id == dvId
				}.count() > 0
		}
		return overridden
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

}
