import com.tdssrc.grails.GormUtil
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
	
/*	
	static id = {
		idMapping(name:'partyId', column:'party_id', unsavedValue:0)
		generator(class:'assigned')
	}
*/
	
	String toString(){
		"$id : $dateCreated"
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
}
