import com.tdssrc.grails.GormUtil
class ModelSyncBatch {
	
	String statusCode = "PENDING"
	
	Date dateCreated
	Date lastModified
	
	static belongsTo = [ userLogin : UserLogin ]
	
	static mapping = {
		version false
		autoTimestamp false
		columns {
			id column:'batch_id'
		}
	}
	static constraints = {
		statusCode( blank:false, size:0..20 )
		dateCreated( nullable:true )
		lastModified( nullable:true )
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		//dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		//lastModified = GormUtil.convertInToGMT( "now", "EDT" )
	}
}
