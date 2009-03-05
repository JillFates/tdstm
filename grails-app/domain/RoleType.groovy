class RoleType {
	String id
	String description

	static constraints = {
		id(blank:false, nullable:false, maxLength:20)
		description(blank:true, nullable:true)
	}

	static mapping  = {
		version false
		id column: 'role_type_code', generator: 'assigned'
	}

	String toString(){
		description
	}
}
