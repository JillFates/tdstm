import com.tdssrc.grails.TimeUtil

class Party {
	Date dateCreated
	Date lastUpdated
	PartyType partyType
	String tempForUpdate
	
	/*
	 * Fields Validations
	 */
	static constraints = {
		dateCreated( nullable:true )
		lastUpdated( nullable:true )
		partyType( nullable:true )
		tempForUpdate(nullable:true)
	}

	/*
	 *  mapping for COLUMN Relation
	 */
	static mapping  = {	
		version false
		autoTimestamp false
		tablePerHierarchy false
		id column:'party_id'
	}
		
	String toString(){
		"$id : $dateCreated"
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}
}