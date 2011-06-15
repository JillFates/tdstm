import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavAttribute
class Model {
	String modelName
	String description
	String assetType = 'Server'
	Integer powerUse
	String aka
	Integer usize = 1
	byte[] frontImage
	byte[] rearImage
	Integer useImage = 0
	
	// Blade chassis fields
	Integer bladeRows
	Integer bladeCount
	Integer bladeLabelCount
	String bladeHeight = 'Half'
	
	// files to sync data for multiple Transition Manager instances
	Integer sourceTDS = 1
	Integer sourceTDSVersion = 1
	static belongsTo = [ manufacturer : Manufacturer]
	
	static hasMany = [ modelConnectors : ModelConnector, racks:Rack ]
	
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
		bladeHeight( blank:true, nullable:true, inList:['Half','Full'] )
		aka( blank:true, nullable:true, validator: { val, obj ->
			if(val){
				def isDuplicated = false
				def akaArray = val.split(",")
				def models = Model.findAllByManufacturerAndAkaIsNotNull( obj.manufacturer )
				models = obj.id ? models.findAll{it.id != obj.id } : models
				models?.aka?.each{ akaString->
					akaArray.each{
						if(akaString.toLowerCase().contains( it.toLowerCase() )){
							isDuplicated = true
						}
					}
				}
				if(isDuplicated)
	            return ['invalid.string']
			} else {
				return true
			}
        })
		sourceTDS( blank:true, nullable:true )
		sourceTDSVersion( blank:true, nullable:true )
		
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
	def beforeInsert = {
		if(assetType == "Blade Chassis"){
			if(!bladeRows)
				bladeRows = 2
			if(!bladeCount)
				bladeCount = 10
			if(!bladeLabelCount)
				bladeLabelCount = 5
		} else {
			bladeRows = null
			bladeCount = null
			bladeLabelCount = null
		}
	}
	def beforeUpdate = {
		if(assetType == "Blade Chassis"){
			if(!bladeRows)
				bladeRows = 2
			if(!bladeCount)
				bladeCount = 10
			if(!bladeLabelCount)
				bladeLabelCount = 5
		} else {
			bladeRows = null
			bladeCount = null
			bladeLabelCount = null
		}
	}
	def getAssetTypeList(){
		return EavAttributeOption.findAllByAttribute(EavAttribute.findByAttributeCode("assetType"),[sort:'value',order:'asc'])?.value
	}
	/*******************************************************************
	 * @return : Total number of connectors associated with this model
	 * @param : this model
	 * ****************************************************************/
	def getNoOfConnectors(){
		return ModelConnector.countByModel( this )
	}
	def getAssetsCount(){
		return AssetEntity.countByModel( this )
	}
	def getSource(){
		return this.sourceTDS == 1 ? "TDS" : ""
	}
	def getManufacturerName(){
		return manufacturer.name
	}
}
