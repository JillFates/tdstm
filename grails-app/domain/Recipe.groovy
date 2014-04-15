
import com.tdsops.tm.enums.domain.ContextType
import com.tdssrc.grails.TimeUtil

/**
 * Recipe Domain Object
 * 
 * <p>Represents a recipe within cookbook that contains various properties of the recipe
 *
 * @author John Martin
 */
class Recipe {

	/** The name or title of the recipe */
	String name
	/** A short description of what the recipe is used for*/
	String description
	/** Used to indicate the context that the recipe is to be used for */
	String context
	/** The project that the recipe is associated with */
	Project project
	/** The current version of the recipe that has been released, which is the version that people should use 
		to generate tasks */
	RecipeVersion releasedVersion

	Date dateCreated
	Date lastUpdated
	
	Boolean archived = false

	static constraints = {	
		name(blank:false, nullable:false, maxLength:40)
		description(blank:true, nullable:true, maxLength:255)
		context(blank:false, nullable:false, inList: ['Event', 'Bundle', 'Application'] )	// TODO : Switch to ENUM RecipeContext
		project(nullable:false)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
		releasedVersion(nullable:true)
	}

	static mapping  = {	
		version true
		autoTimestamp false
		id column: 'recipe_id'
		columns {
			name sqltype: 'varchar(40)'
			description sqltype: 'varchar(255)'
		}
	}

	static hasMany = [ 
		versions : RecipeVersion,
	]
	
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = dateCreated
	}
	
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() {
		name
	}	
	
	ContextType asContextType() {
		switch (this.context) {
			case 'Application' :
				return ContextType.A
				break;
			case 'Bundle' :
				return ContextType.B
				break;
			case 'Event' :
				return ContextType.E
				break;
			default :
				throw new IllegalArgumentException('Invalid context')
				break;
		}
	}
	
}