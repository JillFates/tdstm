/**
 * The MoveEvent domain represents the concept of an event where one or move bundles that will occur at one logical 
 * period of time.
 */
class MoveEvent extends Party {
	
    Project project
    String name
    String description

    static constraints = {        
		name( blank:false, nullable:false )
		project( nullable:false )
		description( blank:true, nullable:true )		
	}

	static hasMany = [
		moveBundles : MoveBundle
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
