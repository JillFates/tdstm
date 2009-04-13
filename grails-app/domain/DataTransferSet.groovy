class DataTransferSet {
	
	String title
	String transferMode
	String templateFilename
	String setCode
	static hasMany = [ dataTransferBatch :DataTransferBatch, dataTransferAttributeMap : DataTransferAttributeMap  ]
	
	static mapping = {
		version false
		columns {
			id column:'data_transfer_id'
			setCode sqlType:'VARCHAR(20)'
		}
	}
	static constraints = {
		title(blank:false, size:0..64)
		transferMode( blank:false, inList:['I', 'E', 'B'])
		templateFilename(blank:true, nullable:true)
		setCode(blank:true, nullable:true)
	}
	String toString(){
		"$id : $title"
	}

}
