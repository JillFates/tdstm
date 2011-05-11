
class ModelSync {
	long modelTempId
	String modelName
	String description
	String assetType
	Integer powerUse
	String aka
	Integer usize
	byte[] frontImage
	byte[] rearImage
	Integer useImage
	
	// Blade chassis fields
	Integer bladeRows
	Integer bladeCount
	Integer bladeLabelCount
	
	// files to sync data for multiple Transition Manager instances
	Integer sourceTDS
	Integer sourceTDSVersion
	long manufacturerTempId
	String manufacturerName
	
	String importStatus
	ModelSyncBatch batch
	
	static belongsTo = [ manufacturer : ManufacturerSync ]
	
	static hasMany = [ modelConnectors : ModelConnectorSync ]
	
	static constraints = {
		modelName( blank:false, nullable:false )
		manufacturer( blank:false, nullable:false )
		description( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		powerUse( blank:true, nullable:true )
		usize( blank:true, nullable:true, inList:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42] )
		frontImage( blank:true, nullable:true )
		rearImage( blank:true, nullable:true )
		bladeRows( blank:true, nullable:true )
		bladeCount( blank:true, nullable:true )
		bladeLabelCount( blank:true, nullable:true )
		aka( blank:true, nullable:true)
		sourceTDS( blank:true, nullable:true )
		sourceTDSVersion( blank:true, nullable:true )
		importStatus(blank:true, nullable:true )
	}
	
	static mapping  = {	
		version false
		columns {
			id column:'model_id'
			modelName column: 'name'
			frontImage sqlType:'LONGBLOB'
			rearImage sqlType:'LONGBLOB'
			useImage sqltype: 'tinyint'
			sourceTDS sqltype: 'tinyint'
		}
	}
	String toString(){
		modelName
	}
}
