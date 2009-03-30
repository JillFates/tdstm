class MoveBundleAsset {
    
    MoveBundle moveBundle
    AssetEntity asset
    ProjectTeam sourceTeam
    ProjectTeam targetTeam

    /*
	 * Fields Validations
	 */
    static constraints = {

        moveBundle( blank:false, nullable:false )
        asset( blank:false, nullable:false )
        sourceTeam( blank:true, nullable:true )
        targetTeam( blank:true, nullable:true )		

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
