class DataTransferSet {
	
	String title
	String transferMode
	static hasMany = [ dataTransferBatch :DataTransferBatch, dataTransferAttributeMap : DataTransferAttributeMap  ]
	
	static mapping = {
		version false
		columns {
			id column:'data_transfer_id'
		}
	}
	static constraints = {
		title(blank:false, size:0..64)
		transferMode( blank:false, inList:['I', 'E', 'B'])
		
	}
	String toString(){
		"$id : $title"
	}

}
