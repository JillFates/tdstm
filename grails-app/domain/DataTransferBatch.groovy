class DataTransferBatch {
	Date dateCreated = new Date()
	String statusCode
	Date lastModified
	Integer versionNumber
	String  transferMode
	Date exportDatetime
	Integer hasErrors = 0
	
	static hasMany = [ dataTransferValue:DataTransferValue ]
	
	static belongsTo = [ dataTransferSet : DataTransferSet, project : Project, userLogin : UserLogin ]
	
	static mapping = {
		version false
		columns {
			id column:'batch_id'
			hasErrors sqlType: 'TINYINT(1)'
		}
	}
	static constraints = {
		statusCode( blank:false, size:0..20 )
		dateCreated( nullable:true )
		exportDatetime( nullable:true )
		lastModified( nullable:true )
		transferMode( blank:false, inList:['I', 'E', 'B'] )
		versionNumber( nullable:true )
		hasErrors( nullable:false )
	}

}
