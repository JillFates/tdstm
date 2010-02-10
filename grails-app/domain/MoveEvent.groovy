/**
 * The MoveEvent domain represents the concept of an event where one or move bundles that will occur at one logical 
 * period of time.
 */
class MoveEvent {
	
    Project project
    String name
    String description

	// Not sure if this is going to be stored or not....
    Date actualStartTime
    Date actualCompletionTime

    static constraints = {        
		name( blank:false, nullable:false )
		project( nullable:false )
		description( blank:true, nullable:true )
		actualStartTime(nullable:true )
		actualCompletionTime(nullable:true )
		
	}

	static hasMany = [
		moveBundles : MoveBundle,
		moveEventNewsList : MoveEventNews
	]

	static mapping  = {
		version true
		id column:'move_event_id'
        columns {
		}        
	}

    String toString(){
		name
	}

}
