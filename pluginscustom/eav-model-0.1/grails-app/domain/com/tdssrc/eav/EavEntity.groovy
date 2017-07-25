package com.tdssrc.eav

class EavEntity {

	Date dateCreated
	Date lastUpdated

	// JPM - I don't think that we really need this.  It seems to be
	// unecessary but have left in until it is worked out. (see EavEntityType)
	// EavEntityType	entityType
	static hasMany = [entityAttribute: EavEntityAttribute]

	// Removed by TM-6779 - this is not being used since field specs fields implementation
//	static belongsTo = [attributeSet: EavAttributeSet]

	static constraints = {
		dateCreated nullable: true
		lastUpdated nullable: true
	}

	static mapping = {
		autoTimestamp false
		tablePerHierarchy false
		columns {
			id column: 'entity_id'
		}
	}
}
