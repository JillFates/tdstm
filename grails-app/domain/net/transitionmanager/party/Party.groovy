package net.transitionmanager.party

import com.tdssrc.grails.TimeUtil

class Party {

	Date dateCreated
	Date lastUpdated
	PartyType partyType

	static constraints = {
		dateCreated nullable: true
		lastUpdated nullable: true
		partyType   nullable: true
	}

	static mapping = {
		autoTimestamp false
		tablePerHierarchy false
		id column: 'party_id'
	}

	String toString() {
		"$id : $dateCreated"
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}
}
