import java.util.Date



class ModelSync {
	long modelTempId
	String modelName
	String description
	String assetType
	Integer powerNameplate
	Integer powerDesign
	Integer powerUse
	String aka
	Integer usize
	byte[] frontImage
	byte[] rearImage
	Integer useImage
	Integer height
	Integer weight
	Integer depth
	Integer width
	String layoutStyle
	
	// Blade chassis fields
	Integer bladeRows
	Integer bladeCount
	Integer bladeLabelCount
	
	// files to sync data for multiple Transition Manager instances
	Integer sourceTDS
	Integer sourceTDSVersion
	long manufacturerTempId
	String manufacturerName
	
	String productLine
	String modelFamily
	Date endOfLifeDate
	String endOfLifeStatus
	Person createdBy
	Person updatedBy
	Person validatedBy
	String sourceURL
	String modelStatus
	Project modelScope
	
	String importStatus
	ModelSyncBatch batch
	
	static belongsTo = [ manufacturer : ManufacturerSync ]
	
	static hasMany = [ modelConnectors : ModelConnectorSync ]
	
	static constraints = {
		modelName( blank:false, nullable:false )
		manufacturer( nullable:false )
		description( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		powerNameplate( nullable:true )
		powerDesign( nullable:true )
		powerUse( nullable:true )
		usize( nullable:true, inList:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42] )
		frontImage( nullable:true )
		rearImage( nullable:true )
		bladeRows( nullable:true )
		bladeCount( nullable:true )
		bladeLabelCount( nullable:true )
		aka( blank:true, nullable:true)
		sourceTDS( nullable:true )
		sourceTDSVersion( nullable:true )
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
		modelScope( nullable:true )
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
