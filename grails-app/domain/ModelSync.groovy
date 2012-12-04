import java.util.Date

class ModelSync {
	String modelName
	String description
	String assetType
	String modelStatus
	String layoutStyle

	// Blade chassis fields
	Integer bladeRows
	Integer bladeCount
	Integer bladeLabelCount
	String bladeHeight = 'Half'

	// Product information 
	Integer usize
	Integer useImage
	Integer height
	Integer weight
	Integer depth
	Integer width
	Integer powerUse
	Integer powerNameplate
	Integer powerDesign
	String productLine
	String modelFamily
	Date endOfLifeDate
	String endOfLifeStatus
	String sourceURL		// URL of where model data was derived from	

	// Room Associated properties
    Boolean roomObject		// TODO change to tinyint 0/1
	
	Integer	masterVersion	// This contains the model's version from the master
	
	Person createdBy
	Person updatedBy
	Person validatedBy
	
	// Properties distinct to ModelSync

	// TO BE DELETED
	String importStatus
	Integer sourceTDS
	Integer sourceTDSVersion	
	long modelTempId
	String aka
	long manufacturerTempId
	String manufacturerName
	byte[] frontImage
	byte[] rearImage
	Project modelScope
		
	static belongsTo = [ batch: ModelSyncBatch,
	 	manufacturer: Manufacturer
	]
	
// 	static hasMany = [ modelConnectors : ModelConnectorSync ]
	
	static constraints = Model.constraints
	
	/*
	static constraints = {
		modelName( blank:false, nullable:false )
		manufacturer( nullable:false )
		description( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		powerNameplate( nullable:true )
		powerDesign( nullable:true )
		powerUse( nullable:true )
		usize( nullable:true, inList:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42] )
		bladeRows( nullable:true )
		bladeCount( nullable:true )
		bladeLabelCount( nullable:true )
		importStatus(blank:true, nullable:true )
		height( nullable:true )
		weight( nullable:true )
		depth( nullable:true )
		width( nullable:true )
		layoutStyle( blank:true, nullable:true )
		productLine( blank:true, nullable:true )
		modelFamily( blank:true, nullable:true )
		endOfLifeDate(nullable:true)
		endOfLifeStatus( blank:true, nullable:true )
		createdBy( nullable:true )
		updatedBy( nullable:true )
		validatedBy( nullable:true )
		sourceURL( blank:true, nullable:true )
		modelStatus( blank:true, nullable:true, inList:['new','full','valid'])

		// TO BE DELETED
		frontImage( nullable:true )
		rearImage( nullable:true )
		modelScope( nullable:true )
		aka( blank:true, nullable:true)
		sourceTDS( nullable:true )
		sourceTDSVersion( nullable:true )
		

	}
	*/
	
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
