/**
 * @author David Ontiveros
 */
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
    	name size: 1..255
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
		Boolean isOwner = person ? (person.id == currentPersonId) : false
		Map data = [
				id				: id,
				name			: name,
				isSystem		: isSystem,
				isShared		: isShared,
				isOwner			: isOwner,
				isFavorite		: isFavorite(currentPersonId),
				schema			: JsonUtil.parseJson(reportSchema)
		]
		return data
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


	def beforeInsert = {
		dateCreated = dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}

}