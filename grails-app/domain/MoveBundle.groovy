class MoveBundle extends Party {
	
    Project project
    String name
    String description
    Date startTime
    Date completionTime
    Integer operationalOrder  // Order that the bundles are performed in

    static constraints = {        
		name( blank:false, nullable:false )
		project( blank:false, nullable:false )
		description( blank:true, nullable:true )		
		startTime( blank:true, nullable:true )
		completionTime( blank:true, nullable:true )
		operationalOrder( blank:false, nullable:false, range:1..25 )
	}

	static hasMany = [
		assetTransitions : AssetTransition
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
