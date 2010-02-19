/**
 * The MoveEvent domain represents the concept of an event where one or move bundles that will occur at one logical 
 * period of time.
 */
class MoveEvent {
	
    Project project
    String name
    String description

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
	 * Retrieves the MIN/MAX Start and Completion times of the MoveBundles associate with the MoveEvent
	 * @return Map[start, completion] times for the MoveEvent
	 */
	def getPlanTimes() {
		def planTimes = jdbcTemplate.queryForMap(
			"SELECT MIN(start_time) as start,  MAX(completion_time) as completion FROM move_bundle WHERE move_event_id = ? ", 
			[moveEventId])
		return planTimes
	}
	
    String toString(){
		name
	}

}
