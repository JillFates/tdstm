package com.tdssrc.eav

class EavEntityAttribute {

	Integer sortOrder

	static belongsTo = [attribute: EavAttribute, eavAttributeSet: EavAttributeSet, eavEntity: EavEntity]

	static mapping = {
		version false
		columns {
			id column: 'entity_attribute_id'
			sortOrder sqlType: 'smallint'
		}
	}

	static constraints = {
		eavEntity nullable: true
		sortOrder range: 0..32767
	}
}
