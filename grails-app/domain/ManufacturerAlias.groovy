/**
 * ManufacturerAlias --- Represents individual alias names used to reference the same manufacturer
 * 
 */
class ManufacturerAlias {
	String name
	Manufacturer manufacturer
	Date dateCreated
		
	static constraints = {
		name nullable:false, blank:false, unique:true
		manufacturer nullable:false
    }
		
	def beforeInsert = {
		dateCreated = TimeUtil.convertInToGMT( "now", "EDT" )
	}	
}