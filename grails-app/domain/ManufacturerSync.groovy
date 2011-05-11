class ManufacturerSync {
	String name
	String description
	String aka
	String importStatus
	ModelSyncBatch batch
	long manufacturerTempId
	
	static constraints = {
		name( blank:false, nullable:false )
		description( blank:true, nullable:true )
		aka( blank:true, nullable:true)
		importStatus(blank:true, nullable:true )
	}
	
	static mapping  = {	
		version false
		id column:'manufacturer_id'
	}
	
	String toString(){
		name
	}
}
