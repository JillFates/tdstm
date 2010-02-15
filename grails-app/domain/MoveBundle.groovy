class MoveBundle extends Party {	
    Project project
    String name
    String description
    Date startTime					// Time that the MoveBundle Tasks will begin
    Date completionTime				// Planned Completion Time of the MoveBundle
	// Integer lastSnapGroupId = 0		// Used when creating Step Snapshot data to group a series of steps (NOT BEING USED)
    Integer operationalOrder  		// Order that the bundles are performed in (NOT BEING USED)
    MoveEvent moveEvent

    static constraints = {        
		name( blank:false, nullable:false )
		project( nullable:false )
		moveEvent( nullable:true )
		description( blank:true, nullable:true )		
		startTime( nullable:true )
		completionTime( nullable:true )
		operationalOrder( nullable:false, range:1..25 )
	}

	static hasMany = [
		assetTransitions : AssetTransition,
		moveBundleSteps  : MoveBundleStep
	]

	static mapping  = {
		version true
		id column:'move_bundle_id'
        columns {
			name sqlType: 'varchar(30)'
		 	startTime sqlType: 'DateTime'
		 	completionTime sqlType: 'DateTime'
		}        
	}

    String toString(){
		name
	}

}
