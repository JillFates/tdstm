package com.tdssrc.eav

class EavEntity {

	Date dateCreated = new Date()
	Date lastUpdated

	// JPM - I don't think that we really need this.  It seems to be
	// unecessary but have left in until it is worked out. (see EavEntityType)
	// EavEntityType	entityType
	static hasMany = [ entityAttribute:EavEntityAttribute ]
	
	static belongsTo = [ attributeSet : EavAttributeSet ]
	
	static mapping = {
		version false
		tablePerHierarchy false
		columns {
			id column:'entity_id'
		}
	}

}
