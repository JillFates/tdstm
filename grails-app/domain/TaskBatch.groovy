
import com.tdssrc.grails.TimeUtil

/**
 * TaskBatch Domain Object
 * 
 * <p>Represents a batch that is created when a recipe is executed and tasks are generated. This will provide a way of tracking, updating and
 * possibly deleting tasks that were generate in the cookbook.
 *
 * @author John Martin
 */
class TaskBatch {

	/** The event that the batch was created for if context was Event */
	MoveEvent moveEvent
	/** The bundle that the batch was created for if the context was Event */
	MoveBundle moveBundle
	/** The asset reference (e.g. App or Server) that the batch was created for when the context was Application, Server, etc */
	AssetEntity assetEntity
	/** The recipeVersion that was used to generate the batch of tasks */
	RecipeVersion recipeVersionUsed
	/** Number of tasks that were generated by the process (static as tasks could be manually deleted). Default
		value (0) */
	Integer taskCount=0	
	/** Number of exceptions that occurred during the recipe generation. Actual exceptions are listed in the exceptionLog
		property. Default value (0). */
	Integer exceptionCount=0
	/** Flag indicating if the tasks generated where published to users. Default value (false) */
	Boolean isPublished = false
	/** List of the exceptions that were created by the generation of the batch */
	String exceptionLog = ''
	/** List of the debug log/info that were created by the generation of the batch */
	String infoLog = ''
	/** Whom created this version of the recipe */
	Person createdBy

	Date dateCreated
	Date lastUpdated

	static constraints = {
		// One of the following three properties should not be null as a batch will be generate in one of these contexts. 
		// TODO - Add custom constraint to make sure one of these properties are populated.
		moveEvent(nullable:true)		
		moveBundle(nullable:true)
		assetEntity(nullable:true)

		recipeVersionUsed(nullable:true)		// Note that recipes can be deleted which will null out any references
		taskCount(nullable:false)
		exceptionLog(blank:true, nullable:false)
		infoLog(blank:true, nullable:false)

		createdBy(nullable:false)
		lastEditedBy(nullable:true)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
	}

	static mapping  = {	
		version false
		autoTimestamp false
		id column: 'task_batch_id'
		columns {
			changelog sqltype: 'text'
			sourceCode sqltype: 'text'
			versionNumber sqltype: 'smallint'
			taskCount sqltype: 'smallint'
		}
	}
		
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = dateCreated
	}
	
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	/** 
	 * Used to get the name of the object for which the context references
	 */
	def contextName = {
		def name = 'Undefined'
		if (moveEvent) {
			name = moveEvent.toString()
		} else if (moveBundle) {
			name = moveBundle.toString()
		} else if (assetEntity) {
			name = assetEntity.toString()
		}
		return name
	}

	/**
	 * Returns informational representation of the task formated as Context + Context Object + batch (# tasks) 
	 * (e.g. Application VSphere 5.0 Cluster - batch (30 tasks) )
	 * @return String
	 */
	String toString() {
		(recipeVersionUsed ? recipeVersionUsed.recipe.context.toString()+' ' : '' ) + contextName() + " - batch of ${taskCount} tasks" 
	}	
	
}