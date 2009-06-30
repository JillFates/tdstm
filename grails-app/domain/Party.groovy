class Party {
	Date dateCreated = new Date()
	Date lastUpdated
	PartyType partyType
	
	/*
	 * Fields Validations
	 */
	static constraints = {
		dateCreated( nullable:false )
		lastUpdated( nullable:true )
		partyType( nullable:true )
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
