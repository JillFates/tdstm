class MoveBundleAsset {
    
    MoveBundle moveBundle
    Asset asset
    ProjectTeam sourceTeam
    ProjectTeam targetTeam

    /*
	 * Fields Validations
	 */
    static constraints = {

        moveBundle( blank:false, nullable:false )
        asset( blank:false, nullable:false )
        sourceTeam( blank:false, nullable:false )
        targetTeam( blank:false, nullable:false )		

	}

    /*
	 *  mapping for COLUMN Relation
	 */

	static mapping  = {
		version true
		id column:'move_bundleAsset_id'
        tablePerHierarchy false
	}

    String toString(){
		moveBundle
	}

}
