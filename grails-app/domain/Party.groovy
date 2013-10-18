import com.tdssrc.grails.TimeUtil

class Party {
	Date dateCreated
	Date lastUpdated
	PartyType partyType
	// String tempForUpdate
	
	static constraints = {
		dateCreated( nullable:true )
		lastUpdated( nullable:true )
		partyType( nullable:true )
		// tempForUpdate(nullable:true)
	}

	static mapping  = {	
		version true
		autoTimestamp false
		tablePerHierarchy false
		id column:'party_id'
	}
		
	String toString(){
		"$id : $dateCreated"
	}
	
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}
}