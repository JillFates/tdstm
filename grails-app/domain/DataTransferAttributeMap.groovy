import com.tdssrc.eav.*
class DataTransferAttributeMap {
	String columnName
	String sheetName
	String validation
	Integer	isRequired
	
	static belongsTo = [ dataTransferSet : DataTransferSet, eavAttribute : EavAttribute ]
	static mapping = {
		version false
		columns {
			id column:'id'
			isRequired sqlType:'smallint'
		}
	}
	static constraints = {
		columnName(blank:false, size:0..32)
		sheetName(blank:false, size:0..64)
		validation(blank:true, size:0..255)
		isRequired(blank:false)
	}
}
