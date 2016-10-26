package com.tdssrc.eav

/**
 * Manages one or more option values that are valid for a particular entity attribute.
 * These option values are used in input types SELECT, RADIO, CHECKBOX.  The values are
 * normalized into this table and the entity attributes reference the record.
 */
class EavAttributeOption {
	String value = ''
	Integer sortOrder = 0

	static belongsTo = [attribute: EavAttribute]

	static mapping = {
		version false
		columns {
			id column: 'option_id'
			sortOrder sqlType: 'smallint'
		}
	}

	static constraints = {
		sortOrder range: 0..32767
		value unique: 'attribute'
	}
}
