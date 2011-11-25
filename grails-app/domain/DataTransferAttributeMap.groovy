import com.tdssrc.eav.*

/*
 * The DataTransferAttributeMap is used to map the spreadsheet sheet/columns
 * to the entity attributes.  This mapping is used in conjuction for importing
 * and exporting data of the referenced DataTransferSet.
 */
class DataTransferAttributeMap {
	String sheetName			// Spreadsheet sheet name
	String columnName			// Spreadsheet column name
	String validation			// Validation rules
	Integer	isRequired		// Flag if column is a required field
	EavAttribute eavAttribute

	static belongsTo = [ dataTransferSet : DataTransferSet ]
	
	static mapping = {
		version false
		columns {
			id column:'id'
			isRequired sqlType:'smallint'
		}
		eavAttribute lazy: false
		
	}
	
	static constraints = {
		columnName(blank:false, size:0..32)
		sheetName(blank:false, size:0..64)
		validation(blank:true, size:0..255)
		isRequired()
	}
}
