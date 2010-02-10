class MoveBundle extends Party {	
    Project project
    String name
    String description
    Date startTime
    Date completionTime
	Integer lastSnapGroupId = 0		// Used when creating Step Snapshot data to group a series of steps
    Integer operationalOrder  		// Order that the bundles are performed in
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
		 	startTime sqlType: 'DateTime'
		 	completionTime sqlType: 'DateTime'
		}        
	}

    String toString(){
		name
	}

}
