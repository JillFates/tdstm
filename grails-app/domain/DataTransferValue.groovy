import com.tdssrc.eav.EavAttribute
class DataTransferValue {
	String importValue
	String correctedValue
	String errorText
	Integer rowId
	Integer assetEntityId
	Integer hasError
	
	static belongsTo = [ dataTransferBatch : DataTransferBatch, eavAttribute : EavAttribute ]
	
	static mapping = {
		version false
		columns {
			id column:'value_id'
			hasError sqlType:'tinyint' 
		}
	}
	
	static constraints = {
		importValue(blank:true, nullable:true, size:0..255)
		correctedValue(blank:true, nullable:true, size:0..255)
		correctedValue(blank:true, nullable:true, size:0..255)
		errorText(blank:true, nullable:true, size:0..255)
		rowId(blank:false)
		hasError(blank:true, nullable:true)
		assetEntityId(blank:true, nullable:true)
	}

}
