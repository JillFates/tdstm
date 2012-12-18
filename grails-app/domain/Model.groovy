import org.jsecurity.SecurityUtils

import com.tds.asset.AssetCableMap;
import com.tds.asset.AssetEntity
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.TimeUtil

class Model {
	String modelName
	String description
	String assetType = 'Server'
	String modelStatus = 'new'
	String layoutStyle
		
	// Blade chassis fields
	Integer bladeRows
	Integer bladeCount
	Integer bladeLabelCount
	String bladeHeight = 'Half'
	
	// Product information 
	Integer usize = 1
	Integer useImage = 0
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

	// Room Associated properties (should be tinyint 0/1)
    Boolean roomObject

	Person createdBy
	Person updatedBy
	Person validatedBy
	Date dateCreated
	Date lastModified
		
	// TO BE DELETED
	byte[] frontImage
	byte[] rearImage
	Project modelScope
	Integer sourceTDS = 1
	Integer sourceTDSVersion = 1
	
	static belongsTo = [ manufacturer : Manufacturer]
	
	static hasMany = [ modelConnectors : ModelConnector, racks:Rack ]
	
	static constraints = {
		modelName( blank:false, nullable:false, unique:['manufacturer'])
		manufacturer( nullable:false )
		description( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		layoutStyle( blank:true, nullable:true )
		modelStatus( blank:true, nullable:true, inList:['new','full','valid'])
		bladeRows( nullable:true )
		bladeCount( nullable:true )
		bladeLabelCount( nullable:true )
		bladeHeight( blank:true, nullable:true, inList:['Half','Full'] )
		productLine( blank:true, nullable:true )
		modelFamily( blank:true, nullable:true )
		endOfLifeDate(nullable:true)
		endOfLifeStatus( blank:true, nullable:true )
		sourceURL( blank:true, nullable:true )
		usize( nullable:true, inList:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48])
		height( nullable:true )
		weight( nullable:true )
		depth( nullable:true )
		width( nullable:true )
		powerUse( nullable:true )
		powerNameplate( nullable:true )
		powerDesign( nullable:true )
        roomObject( nullable:true )

		createdBy( nullable:true )
		updatedBy( nullable:true )
		validatedBy( nullable:true )
		
		// TODO - DELETE THIS
		modelScope( nullable:true )
		frontImage( nullable:true )
		rearImage( nullable:true )
		sourceTDS( nullable:true )
		sourceTDSVersion( nullable:true )
		lastModified( nullable:true )
		dateCreated( nullable:true )
	}
	
	static mapping  = {	
		autoTimestamp false
		columns {
			id column:'model_id'
			modelName column: 'name'
			// TODO : what is the point of the following three statements?
			createdBy column: 'created_by'
			updatedBy column: 'updated_by'
			validatedBy column: 'validated_by'
			frontImage sqlType:'LONGBLOB'
			rearImage sqlType:'LONGBLOB'
			useImage sqltype: 'tinyint'
			sourceTDS sqltype: 'tinyint'
		}
	}
	/*
	 * Date to insert in GMT
	 */
	
	String toString(){
		modelName
	}
	def beforeInsert = {
		dateCreated = TimeUtil.convertInToGMT( "now", "EDT" )
		lastModified = TimeUtil.convertInToGMT( "now", "EDT" )
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
		lastModified = TimeUtil.convertInToGMT( "now", "EDT" )
		
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
	
	def beforeDelete = {
        AssetEntity.withNewSession{
            AssetEntity.executeUpdate("Update AssetEntity set model=null where model = :model",[model:this])
        }
        ModelAlias.withNewSession { aliases*.delete() }
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

	// Get list of alias records for the manufacturer
	def getAliases() {
		ModelAlias.findAllByModel(this, [sort:'name'])
	}
    
	/**
	 * @param : aka -> value of name
	 * @param : createIfNotFound -> flag to determine whether need to create ManufacturerAlias or not
	 * @return : void
	 *
	 */
	
	def findOrCreateByName(name, def createIfNotFound = false){
		def modelAlias = ModelAlias.findByNameAndModel(name,this)
		if(!modelAlias && createIfNotFound){
			modelAlias = new ModelAlias(name:name.trim(), model:this, manufacturer:this.manufacturer)
			if(modelAlias.save()){
				modelAlias.errors.allErrors.each { log.error it}
			}
		}
        return modelAlias
	}
	
}
