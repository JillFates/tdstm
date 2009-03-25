import com.tdssrc.eav.EavAttribute
class DateTransferValue {
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
		importValue(blank:true, size:0..255)
		correctedValue(blank:true, size:0..255)
		correctedValue(blank:true, size:0..255)
		errorText(blank:true, size:0..255)
		rowId(blank:false)
		hasError(blank:true)
	}

}
