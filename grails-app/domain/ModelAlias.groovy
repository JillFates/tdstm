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
		name unique:['manufacturer','name']
    }
		
	static mapping = {
		version false
		autoTimestamp false
		sort 'name'
	}
	
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}	
}