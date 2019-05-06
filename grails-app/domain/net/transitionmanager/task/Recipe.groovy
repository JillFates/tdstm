package net.transitionmanager.task

import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.project.Project

/**
 * Recipe Domain Object
 *
 * <p>Represents a recipe within cookbook that contains various properties of the recipe
 *
 * @author John Martin
 */
class Recipe {

	String        name                    // the name or title of the recipe
	String        description             // a short description of what the recipe is used for
	String        context                 // the context that the recipe is to be used for
	Project       project                // the project that the recipe is associated with
	RecipeVersion releasedVersion  // The current version of the recipe that has been released,
	                               // which is the version that people should use to generate tasks

	Date dateCreated
	Date lastUpdated

	Boolean archived = false


	static constraints = {
		context nullable: true
		dateCreated nullable: true
		description nullable: true
		lastUpdated nullable: true
		name blank: false, size: 1..40
		releasedVersion nullable: true
	}

	static mapping = {
		autoTimestamp false
		id column: 'recipe_id'
		columns {
			context sqltype: 'varchar(45)'
			name sqltype: 'varchar(40)'
		}
	}

	static hasMany = [versions: RecipeVersion]

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = dateCreated
	}

	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() { name }

	/**
	 * Gets the context as a map.
	 *
	 * @return the context including event id and tags as a map.
	 */
	Map context(){
		context ? JsonUtil.convertJsonToMap(context) : [:]
	}
}
