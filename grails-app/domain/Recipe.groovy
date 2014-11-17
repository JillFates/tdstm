
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

	Integer defaultAssetId

	static constraints = {	
		name(blank:false, nullable:false, size: 1..40)
		description(blank:true, nullable:true, size: 0..255)
		// TODO : Switch the context property to ENUM RecipeContext
		context(blank:false, nullable:false, inList: ['Event', 'Bundle', 'Application'], size: 1..45 )	
		project(nullable:false)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
		releasedVersion(nullable:true)
		defaultAssetId(nullable:true)
	}

	static mapping  = {	
		version true
		autoTimestamp false
		id column: 'recipe_id'
		columns {
			name sqltype: 'varchar(40)'
			context sqltype: 'varchar(45)'
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