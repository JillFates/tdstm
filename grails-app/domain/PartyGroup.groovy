class PartyGroup extends Party {

	String name
	String comment

	/*
	 * Fields Validations
	 */
	static constraints = {
		name( blank: false, nullable:false, maxLength:64)
		comment( blank: true, nullable: true )
	}

	/*
	 *  mapping for COLUMN Relation
	 */
	static mapping  = {	
		version false
		autoTimestamp false
		tablePerHierarchy false
		id column:'party_group_id'
		columns {
			name sqlType:'varchar(64)'
		}
	}

	String toString(){
		name
	}

}
