import com.tdssrc.grails.TimeUtil

class ModelSyncBatch {
	
	String statusCode = "PENDING"
	String source			// Where the batch originated (hostname?)
	Date dateCreated
	Date lastModified
	Date changesSince		// Represents the date passed to master to get changes as of this date
	UserLogin createdBy
	
	static hasMany = [ manufacturerSync:ManufacturerSync, modelSync:ModelSync ]	
	
	static constraints = {
		statusCode( blank:false, inList:['PENDING','ACTIVE','DONE'] )
		dateCreated( nullable:true )
		lastModified( nullable:true )
	}
	
	static mapping = {
		version false
		autoTimestamp false
		columns {
			id column:'batch_id'
			statusCode sqltype: 'varchar(20)'
		}
	}

	def beforeInsert = {
		dateCreated = lastModified = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
}
