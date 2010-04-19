package com.tdssrc.eav

import com.tdssrc.grails.GormUtil
import org.jsecurity.SecurityUtils
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
		version false
		autoTimestamp false
		tablePerHierarchy false
		columns {
			id column:'entity_id'
		}
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}

}
