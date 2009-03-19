package com.tdssrc.eav

class EavEntityAttribute {

	Integer	sortOrder

	static belongsTo = [ attribute : EavAttribute ]

	static mapping = {
		version false
		columns {
			id column:'entity_attribute_id'
			sortOrder sqlType:'smallint'
		}
	}

	static constraints = {
		sortOrder( size: 0..32767)
	}

}
