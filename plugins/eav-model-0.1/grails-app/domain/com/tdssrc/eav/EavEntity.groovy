package com.tdssrc.eav

import org.apache.shiro.SecurityUtils
class EavEntity {

	Date dateCreated
	Date lastUpdated
	
	// JPM - I don't think that we really need this.  It seems to be
	// unecessary but have left in until it is worked out. (see EavEntityType)
	// EavEntityType	entityType
	static hasMany = [ entityAttribute:EavEntityAttribute ]
	
	static belongsTo = [ attributeSet : EavAttributeSet ]
	
	/*
	 * Fields Validations
	 */
	static constraints = {
		dateCreated( nullable:true )
		lastUpdated( nullable:true )
	}
	static mapping = {
		version true
		autoTimestamp false
		tablePerHierarchy false
		columns {
			id column:'entity_id'
		}
	}
}
