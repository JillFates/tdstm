import com.tdssrc.grails.TimeUtil

/**
 * ManufacturerAlias - Represents individual alias names used to reference the same manufacturer
 * 
 */
class ManufacturerAlias {
	String name
	Manufacturer manufacturer
	Date dateCreated
	
	static constraints = {
		name unique:true
    }

	static mapping = {
		version false	
		autoTimestamp false	
	}
	
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}	
}