import com.tdssrc.grails.TimeUtil

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
		name nullable:false, blank:true, unique:['manufacturer']
		manufacturer nullable:false
		model nullable:false
    }
		
	static mapping = {
		version false
		autoTimestamp false
		
		// TODO : the index created is ModelAlias_Name_idx - don't understand why the declared name is wrong plus this index name is screwy
		name index:'modelAliasModelIdx'
	}
	
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}	
}