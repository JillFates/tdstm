package net.transitionmanager.task

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.person.Person

/**
 * RecipeVersion Domain Object
 *
 * <p>Represents a particular version of a recipe within cookbook that contains the source
 *
 * @author John Martin
 */
class RecipeVersion {

	String        sourceCode = ''     // the source code of the recipe (presently Groovy array;
	                           // may make a JSON object in the future)
	String        changelog = ''      // the user-entered details about changes to the recipe (TODO : change to wiki syntax)
	RecipeVersion clonedFrom   // the recipe version that the recipe was originally cloned from */
	Integer       versionNumber = 0  // the version number of the recipe. A zero (0) indicates that the recipe is WIP.
	                           // When published it will increment to the next higher number. Note that once a
	                           // recipe is versioned that the sourceCode can not be changed without going
	                           // through the publish process. Default value (0)
	Person        createdBy           // whom created this version of the recipe
	Date          dateCreated
	Date          lastUpdated

	static belongsTo = [recipe: Recipe]

	static constraints = {
		changelog nullable: true
		clonedFrom nullable: true
		dateCreated nullable: true
		lastUpdated nullable: true
		sourceCode nullable: true, maxSize: 131072
	}

	static mapping = {
		autoTimestamp false
		id column: 'recipe_version_id'
		columns {
			changelog sqltype: 'text'
			sourceCode sqltype: 'text'
			versionNumber sqltype: 'smallint'
		}
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() {
		"$recipe.name (${versionNumber == 0 ? 'WIP' : versionNumber})"
	}
}
