class PartyType {
	String id
	String description

	static constraints = {
		id (blank:false, nullable:false, size: 1..32)
		description (blank:true, nullable:true, size: 0..255)
	}

	static mapping  = {
		version false
		id column: 'party_type_code', generator: 'assigned'
	}

	String toString(){
		"$id : $description"
	}
}
