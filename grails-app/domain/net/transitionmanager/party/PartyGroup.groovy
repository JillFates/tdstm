package net.transitionmanager.party

class PartyGroup extends Party {

	String name
	String comment

	static constraints = {
		comment nullable: true
		name blank: false, size: 1..64
	}

	static mapping = {
		version false
		autoTimestamp false
		tablePerHierarchy false
		id column: 'party_group_id'
		columns {
			name sqlType: 'varchar(64)'
		}
	}

	String toString() { name }
}
