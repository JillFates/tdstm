/**
 * The MoveEvent domain represents the concept of an event where one or move bundles that will occur at one logical 
 * period of time.
 */
class MoveEvent {
		
	static transients = [ "jdbcTemplate" ]
	def jdbcTemplate
	
	static final String METHOD_LINEAR="L"
	static final String METHOD_MANUAL="M"
		
		
    Project project
    String name
    String description
	String inProgress = "false" 
	String calcMethod = METHOD_LINEAR

	Date revisedCompletionTime		// Revised Completion Time of the MoveEvent which is only set as an exception

	// Not sure if this is going to be stored or not....
    Date actualStartTime
    Date actualCompletionTime

    static constraints = {        
		name( blank:false, nullable:false )
		project( nullable:false )
		description( blank:true, nullable:true )
		actualStartTime(nullable:true )
		actualCompletionTime(nullable:true )
		revisedCompletionTime ( nullable:true )
		inProgress( blank:false, nullable:false, inList:["true", "false"] )
		calcMethod( blank:false, nullable:false, inList: [METHOD_LINEAR, METHOD_MANUAL] )
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
    String toString(){
		name
	}

}
