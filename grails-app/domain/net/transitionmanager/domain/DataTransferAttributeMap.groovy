package net.transitionmanager.domain

import com.tdssrc.eav.EavAttribute

/**
 * Maps the spreadsheet sheet/columns to the entity attributes, used for
 * importing and exporting data of the referenced DataTransferSet.
 */
class DataTransferAttributeMap {

	String sheetName        // Spreadsheet sheet name
	String columnName       // Spreadsheet column name
	String validation       // Validation rules
	Integer isRequired      // Flag if column is a required field
	EavAttribute eavAttribute

	static belongsTo = [dataTransferSet: DataTransferSet]

	static mapping = {
		version false
		eavAttribute lazy: false
		columns {
			isRequired sqlType: 'smallint'
		}
	}

	static constraints = {
		columnName blank: false, size: 0..32
		sheetName blank: false, size: 0..64
	}
}
