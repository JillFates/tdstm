class RoleType {
	String id
	String description
	String help

	static constraints = {
		id (blank:false, nullable:false, size: 1..32)
		description (blank:true, nullable:true, size: 0..255)
		help (blank:true, nullable:true, size: 0..255)
	}

	static mapping  = {
		version false
		id column: 'role_type_code', generator: 'assigned'
	}

	String toString(){
		description
	}
}