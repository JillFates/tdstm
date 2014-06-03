class ManufacturerSync {
	String name
	String description
	String corporateName
	String corporateLocation
	String website
	String aka
	String importStatus
	ModelSyncBatch batch
	long manufacturerTempId
	
	static constraints = {
		name( blank:false, nullable:false )
		description( blank:true, nullable:true )
		corporateName( blank:true, nullable:true )
		corporateLocation( blank:true, nullable:true )
		website( blank:true, nullable:true )
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
