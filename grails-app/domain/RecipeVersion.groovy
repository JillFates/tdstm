
import com.tdssrc.grails.TimeUtil

/**
 * RecipeVersion Domain Object
 * 
 * <p>Represents a particular version of a recipe within cookbook that contains the source
 *
 * @author John Martin
 */
class RecipeVersion {

	/** The source code of the recipe (presently Groovy array - may make a JSON object in the future) */
	String sourceCode = ""
	/** The user entered details about changes to the recipe (TODO : change to wiki syntax) */
	String changelog = ""
	/** The recipe version that the recipe was originally cloned from */
	RecipeVersion clonedFrom
	/** The version number of the recipe. A zero (0) indicates that the recipe is WIP. When published it will 
		increment to the next highest number. Note that once a recipe is versioned that the sourceCode can not
		be changed without going through the publish process. Default value (0). */
	Integer versionNumber = 0
	/** Whom created this version of the recipe */
	Person createdBy

	Date dateCreated
	Date lastUpdated

	static belongsTo = [ recipe : Recipe ]	

	static constraints = {	
		sourceCode(blank:true, nullable:true)
		changelog(blank:true, nullable:true)
		clonedFrom(nullable:true)
		versionNumber(nullable:false)
		createdBy(nullable:false)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
		recipe(nullable:false)
	}

	static mapping  = {	
		version true
		autoTimestamp false
		id column: 'recipe_version_id'
		columns {
			sourceCode sqltype: 'text'
			changelog sqltype: 'text'
			versionNumber sqltype: 'smallint'
		}
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = dateCreated
	}
	
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() {
		"${recipe.name} (${versionNumber==0 ? 'WIP' : versionNumber})"
	}	
	
}