import com.tdssrc.eav.EavAttribute

class DataTransferValue {
	String importValue
	String correctedValue
	String errorText
	Integer rowId
	Integer assetEntityId
	Integer hasError
	EavAttribute eavAttribute
	
	static belongsTo = [ 
		dataTransferBatch : DataTransferBatch
	]
	
	static mapping = {
		version false
		columns {
			id column:'value_id'
			hasError sqlType:'tinyint' 
		}
	}
	
	static constraints = {
		importValue( blank:true, nullable:true, size:0..255 )
		correctedValue( blank:true, nullable:true, size:0..255 )
		correctedValue( blank:true, nullable:true, size:0..255 )
		errorText( blank:true, nullable:true, size:0..255 )
		rowId()
		hasError( nullable:true )
		assetEntityId( nullable:true )
	}

}
