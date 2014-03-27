import com.tdsops.tm.enums.domain.ContextType

/**
 * The MoveEvent domain represents the concept of an event where one or move bundles that will occur at one logical 
 * period of time.
 */
class MoveEvent {
		
	static ContextType getContextType() {
		return ContextType.E
	}
	
	static transients = [ "jdbcTemplate" ]
	def jdbcTemplate
	
	static final String METHOD_LINEAR="L"
	static final String METHOD_MANUAL="M"
	
    Project project
    String name
    String description
	String inProgress = "auto" 
	String calcMethod = METHOD_LINEAR
	String runbookStatus
	Integer runbookVersion = 1
	String runbookBridge1
	String runbookBridge2 
	String videolink
	String runbookRecipe

	Date revisedCompletionTime		// Revised Completion Time of the MoveEvent which is only set as an exception

	// Not sure if this is going to be stored or not....
    Date actualStartTime
    Date actualCompletionTime
	Date estStartTime
	Date estCompletionTime

    static constraints = {        
		name( blank:false, nullable:false )
		project( nullable:false )
		description( blank:true, nullable:true )
		actualStartTime(nullable:true )
		actualCompletionTime(nullable:true )
		revisedCompletionTime ( nullable:true )
		inProgress( blank:false, nullable:false, inList:["auto", "true", "false"] )
		calcMethod( blank:false, nullable:false, inList: [METHOD_LINEAR, METHOD_MANUAL] )
		runbookStatus( blank:true, nullable:true , inList:["Pending", "Draft", "Final", "Done"])
		runbookVersion ( nullable:true )
		runbookBridge1( blank:true, nullable:true )
		runbookBridge2( blank:true, nullable:true )
		videolink( blank:true, nullable:true )
		runbookRecipe( blank:true, nullable:true )
		estStartTime( nullable:true )
		estCompletionTime( nullable:true )
	}

	static hasMany = [
		moveBundles : MoveBundle,
		moveEventNewsList : MoveEventNews,
		moveEventSnapshots : MoveEventSnapshot
	]

	static mapping  = {
		version true
		id column:'move_event_id'
        columns {
	 		revisedCompletionTime sqlType: 'DateTime'
			runbookRecipe sqlType: 'Text'
	 		estStartTime sqlType: 'DateTime'
	 		estCompletionTime sqlType: 'DateTime'
		}        
	}

	/**
	 * Retrieves the MIN/MAX Start and Completion times of the MoveBundleSteps associate with the MoveBundle.MoveEvent
	 * @return Map[planStart, planCompletion] times for the MoveEvent
	 */
	def getPlanTimes() {
		def planTimes = jdbcTemplate.queryForMap(
						"""SELECT MIN(mbs.plan_start_time) as start,  MAX(mbs.plan_completion_time) as completion FROM move_bundle mb
						LEFT JOIN move_bundle_step mbs on mb.move_bundle_id = mbs.move_bundle_id WHERE move_event_id = ${id} """
						)
		return planTimes
	}
	/**
	 * Retrieves the MIN/MAX Start and Completion times of the MoveBundles associate with the MoveEvent
	 * @return Map[start, completion] times for the MoveEvent
	 */
	def getEventTimes() {
		def eventTimes = jdbcTemplate.queryForMap("SELECT MIN(start_time) as start,  MAX(completion_time) as completion FROM move_bundle WHERE move_event_id = ${id} ")
		return eventTimes
	}
	/*
	 *  Render moveBundles list as comma separated value string
	 */
	def getMoveBundlesString(){
		return "${this.moveBundles.toString().replace('[','').replace(']','')}"
	}
    String toString(){
		name
	}

	boolean belongsToClient(aClient) {
		return this.project.client.equals(aClient)
	}
}
