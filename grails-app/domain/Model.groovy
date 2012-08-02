import com.tds.asset.AssetEntity;
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavAttribute
import org.jsecurity.SecurityUtils;
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
	Integer height
	Integer weight 
	Integer depth
	Integer width
	String layoutStyle	
	// Blade chassis fields
	Integer bladeRows
	Integer bladeCount
	Integer bladeLabelCount
	String bladeHeight = 'Half'
	String productLine
	String modelFamily
	Date endOfLifeDate
	String endOfLifeStatus
	Person createdBy
	Person updatedBy
	Person validatedBy
	String sourceURL
	String modelStatus = 'new'
	Project modelScope
	Integer powerNameplate
	Integer powerDesign 
	
	

	
	// files to sync data for multiple Transition Manager instances
	Integer sourceTDS = 1
	Integer sourceTDSVersion = 1
	static belongsTo = [ manufacturer : Manufacturer]
	
	static hasMany = [ modelConnectors : ModelConnector, racks:Rack ]
	
	static constraints = {
		modelName( blank:false, nullable:false )
		manufacturer( nullable:false )
		description( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		powerUse( nullable:true )
		usize( nullable:true, inList:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48])
		height( nullable:true )
		weight( nullable:true )
		depth( nullable:true )
		width( nullable:true )
		layoutStyle( blank:true, nullable:true )
		frontImage( nullable:true )
		rearImage( nullable:true )
		bladeRows( nullable:true )
		bladeCount( nullable:true )
		bladeLabelCount( nullable:true )
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
		sourceTDS( nullable:true )
		sourceTDSVersion( nullable:true )
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
		powerNameplate( nullable:true )
		powerDesign( nullable:true )
	}
	
	static mapping  = {	
		version false
		columns {
			id column:'model_id'
			modelName column: 'name'
			createdBy column: 'created_by'
			updatedBy column: 'updated_by'
			validatedBy column: 'validated_by'
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
		def principal = SecurityUtils.subject?.principal
		if( principal ){
			createdBy  = UserLogin.findByUsername( principal )?.person
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
