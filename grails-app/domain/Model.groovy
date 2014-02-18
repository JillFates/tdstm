import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetEntity
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdsops.commons.lang.exception.PersistenceException

class Model {
	// TODO - modelName should be renamed to name (as it is in the db - confusing)
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
		usize( nullable:true, inList:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52])
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
	
	static transients = [
		'findOrCreateAliasByName',
		'createModelByModelName',
		'noOfConnectors',
		'assetTypeList',
		'assetsCount',
		'source',
		'manufacturerName',
		'aliases'
	]
	
	static mapping  = {	
		autoTimestamp false
		columns {
			id column:'model_id'
			modelName column: 'name'
			// TODO : what is the point of the following three statements?
			createdBy column: 'created_by'
			updatedBy column: 'updated_by'
			validatedBy column: 'validated_by'
			// TODO : these are going to be deleted
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
		dateCreated = lastModified = TimeUtil.nowGMT()
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
		prependHttp()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
		
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
		prependHttp()
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
	 * Used to get a ModelAlias object by name and create one (optionally) if it doesn't exist 
	 * @param String name - name of the model alias
	 * @param Boolean createIfNotFound - optional flag to indicating if record should be created (default false)
	 * @return ModelAlias - a ModelAlias object if found or was successfully created , or null if not found or not created 
	 */
	def findOrCreateAliasByName(name, def createIfNotFound = false) {
		name = name.trim()
		def alias = ModelAlias.findByNameAndModel(name,this)
			if( !alias && createIfNotFound){
			alias = new ModelAlias(name:name, model:this, manufacturer:this.manufacturer)
			if (! alias.save()) {
				alias.errors.allErrors.each { log.error it}
				alias = null
			}
		}
        return alias
	}
	
	/**
	 * Used to create a model for a given model name and manufacturer.
	 *
	 * @param modelName - String modelName name of model
	 * @param manufacturer - object of manufacturer for which we creating the model.
	 * @param assetType - (optional param )String assetType asset type of model  (default : 'unknown')
	 * @param usize - (optional param )Integer usize , usize of model  (default : 'unknown')
	 * @return object of Model if created else null.
	 */
	def static createModelByModelName(def modelName, def manufacturer, def assetType='Server',  def usize=1){
		def model = new Model( modelName:modelName, manufacturer:manufacturer, assetType:assetType, sourceTDS : 0, usize :usize )
		if ( !model.validate() || !model.save(flush:true) ) {
			def etext = "Unable to create model" + GormUtil.allErrorsString( model )
			model = null
		} else {
			def powerConnector = new ModelConnector(model : model,
				connector : 1,
				label : "Pwr1",
				type : "Power",
				labelPosition : "Right",
				connectorPosX : 0,
				connectorPosY : 0,
				status: "missing",
			)
				
			if ( !powerConnector.save(flush: true)) {
				throw new PersistenceException("Unable to create Power Connectors for ${model}")
					.addContextValue('messageCode', 'model.create.connector.failure')
					.addContextValue('messageArgs', [model])
					.addContextValue('gorm', GormUtil.allErrorsString( powerConnector ) );
			}
		}
		return model
	}
	
	/**
	 * This method is used to prepend "http" for sourceURL if http:// does not exist.  
	 */
	def prependHttp(){
		if(sourceURL && sourceURL?.size() > 10){
			def isUrl = sourceURL ==~ /(?i)^https?:\/\/.*/
			if (!isUrl) {
				sourceURL = "http://"+sourceURL
			}
		}
	}
	//this method return all fields with their labels which are used in model List jqgrid.
	static getModelFieldsAndlabels(){
		return [ 'description':'Description', 'assetType':'Asset Type','layoutStyle':'Layout Style', 'bladeRows':'Blade Rows','modelScope':'Model Scope',
			     'bladeCount':'Blade Count', 'bladeLabelCount':'Blade Label Count', 'bladeHeight':'Blade Height', 'usize':'USize','useImage':'Use Image',
			     'height':'Height','weight':'Weight','depth':'Depth','width':'Width','powerUse':'Power','powerNameplate':'Power Name Plate',
			     'powerDesign':'Power Design','productLine':'Product Line','modelFamily':'Model Family','endOfLifeDate':'End Of Life Date',
			     'endOfLifeStatus':'endOfLifeStatus','modelConnectors':'No Of Connectors','roomObject':'roomObject','createdBy':'Created By','updatedBy':'Updated By',
			     'validatedBy':'Validated By','dateCreated':'Date Created','lastModified':'Last Modified','sourceURL':'Source URL'
			  ]
	}
	
}
