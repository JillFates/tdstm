class MoveBundle extends PartyGroup {
	
    Project project
    String name
    String description
    Date startTime
    Date completionTime
    Integer operationalOrder  // Order that the bundles are performed in

    /*
	 * Fields Validations
	 */
    static constraints = {        
        
		name( blank:false, nullable:false )
		project( blank:false, nullable:false )
		description( blank:true, nullable:true )		
		startTime( blank:true, nullable:true )
		completionTime( blank:true, nullable:true )
        operationalOrder( blank:false, nullable:false, range:1..25 )
        
	}

    /*
	 *  mapping for COLUMN Relation
	 */

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
