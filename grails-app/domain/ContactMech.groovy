class ContactMech {
    String mechType
    String value
	Party party

	/*
	 * Fields Validations
	 */
	static constraints = {
    	party( nullable: true )
    	mechType( blank: false, unique:true, maxLength: 10 )
    	value( blank: false, nullable: false )
    }

	 /*
	 *  mapping for COLUMN Relation
	 */
	static mapping  = {
		version false
		id column:'contact_mech_id'
		mechType sqlType: 'varchar(10)'
		value sqlType: 'varchar(128)'  
	}

	String toString(){
    	mechType
	}

}
