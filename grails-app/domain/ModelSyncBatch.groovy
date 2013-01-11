import com.tdssrc.grails.TimeUtil

class ModelSyncBatch {
	
	String statusCode = "PENDING"
	String source			// Where the batch originated
	Date dateCreated
	Date lastModified
	Date changesSince		// Date passed to the master for filtering changes
	UserLogin createdBy
	
	static hasMany = [ manufacturerSync:ManufacturerSync, modelSync:ModelSync ]	
	
	// TODO - delete these
	UserLogin userlogin
	
	static mapping = {
		version false
		autoTimestamp false
		columns {
			id column:'batch_id'
			statusCode sqltype: 'varchar(20)'
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
		dateCreated = TimeUtil.convertInToGMT( "now", "EDT" )
		lastModified = TimeUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastModified = TimeUtil.convertInToGMT( "now", "EDT" )
	}
}
