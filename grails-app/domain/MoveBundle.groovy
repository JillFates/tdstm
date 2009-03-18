class MoveBundle {

    Project project
    String name
    String description
    Date startTime
    Date finishTime
    Integer bundleOrder // Order that the bundles are performed in

    /*
	 * Fields Validations
	 */
    static constraints = {
        
        project( blank:false, nullable:false )
		name( blank:false, nullable:false )
		description( blank:true, nullable:true )		
		startTime( blank:true, nullable:true )
		finishTime( blank:true, nullable:true )
        bundleOrder( blank:false, nullable:false )
        
	}

    /*
	 *  mapping for COLUMN Relation
	 */

	static mapping  = {
		version true
		id column:'move_bundle_id'
        tablePerHierarchy false
	}

    String toString(){
		name
	}

}
