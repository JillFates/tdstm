import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavAttribute
class Model {
	String modelName
	String description
	String assetType
	Integer powerUse
	String aka
	Integer usize
	byte[] frontImage
	byte[] rearImage
	
	
	static belongsTo = [ manufacturer : Manufacturer]
	
	static constraints = {
		modelName( blank:false, nullable:false, unique:'manufacturer' )
		manufacturer( blank:false, nullable:false )
		description( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		powerUse( blank:true, nullable:true, inList:[1000,1100,1200,1300,1400,1500,1600,1700,1800,1900,2000] )
		aka( blank:true, nullable:true )
		usize( blank:true, nullable:true, inList:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42] )
		frontImage( blank:true, nullable:true )
		rearImage( blank:true, nullable:true )
	}
	
	static mapping  = {	
		version false
		columns {
			id column:'model_id'
			modelName column: 'name'
			frontImage sqlType:'LONGBLOB'
			rearImage sqlType:'LONGBLOB'
		}
	}
	def getAssetTypeList(){
		return EavAttributeOption.findAllByAttribute(EavAttribute.findByAttributeCode("assetType"))?.value
	}
	String toString(){
		modelName
	}
}
