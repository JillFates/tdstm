package net.transitionmanager.party

class PartyType {

	String id
	String description

	static constraints = {
		description nullable: true
		id blank: false, size: 1..32
	}

	static mapping = {
		version false
		id column: 'party_type_code', generator: 'assigned'
	}

	String toString() {
		"$id : $description"
	}
}
