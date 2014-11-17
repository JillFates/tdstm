class PartyGroup extends Party {

	String name
	String comment

	/*
	 * Fields Validations
	 */
	static constraints = {
		name( blank: false, nullable:false, size: 1..64)
		comment( blank: true, nullable: true, size: 0..255 )
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
