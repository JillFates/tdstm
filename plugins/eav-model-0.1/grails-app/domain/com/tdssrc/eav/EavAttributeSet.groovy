package com.tdssrc.eav

class EavAttributeSet {
	String	attributeSetName
	Integer	sortOrder
	EavEntityType	entityType

	static hasMany = [ entities : EavEntity ]

	static belongsTo = [ EavEntityType ]

	static mapping = {
		version false
		columns {
			id column:'attribute_set_id'
			sortOrder sqlType:'smallint(5)'
		}
	}

	static constraints = {
		attributeSetName( size: 1..64 )
		sortOrder( size: 0..32767)
	}
}