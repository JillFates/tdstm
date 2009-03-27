class DataTransferBatch {
	Date dateCreated = new Date()
	String statusCode
	Date lastModified
	Integer versionNumber
	String  transferMode 
	
	static hasMany = [ dataTransferValue:DataTransferValue ]
	
	static belongsTo = [ dataTransferSet : DataTransferSet, project : Project, userLogin : UserLogin ]
	
	static mapping = {
		version false
		columns {
			id column:'batch_id'
		}
	}
	static constraints = {
		statusCode(blank:false, size:0..20)
		dateCreated(blank:true, nullable:true)
		lastModified(blank:true, nullable:true)
		transferMode( blank:false, inList:['I', 'E', 'B'])
		versionNumber(blank:true, nullable:true)
	}

}
