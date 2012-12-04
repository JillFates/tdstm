/**
 * ModelAlias --- Represents individual alias names used to reference the same model
 * 
 */
class ModelAlias {
	String name
	Manufacturer manufacturer
	Model model
	Date dateCreated
		
	static constraints = {
		name nullable:false, blank:false, unique:['manufacturer']
		manufacturer nullable:false
		model nullable:false
    }
		
	static mapping = {
		version false
		name index:'modelAliasModelIdx'
	}
	
	def beforeInsert = {
		dateCreated = TimeUtil.convertInToGMT( "now", "EDT" )
	}	
}