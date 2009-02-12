class Party {
	Date dateCreated
	Date lastUpdated
	
	/*
	 * Fields Validations
	 */
	static constraints = {
		dateCreated(blank:false, nullable:false)
		lastUpdated(blank:true, nullable:true)
	}

	/*
	 *  mapping for COLUMN Relation
	 */
	static mapping  = {	
		version false
		id column:'party_id'
		tablePerHierarchy false
	}
	
/*	
	static id = {
		idMapping(name:'partyId', column:'party_id', unsavedValue:0)
		generator(class:'assigned')
	}
*/
	
	String toString(){
		"$id : $dateCreated"
	}
	
}
