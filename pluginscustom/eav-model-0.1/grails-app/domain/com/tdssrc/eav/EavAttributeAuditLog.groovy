package com.tdssrc.eav

/**
 * Provides an audit log tracking the user, date and reason for the change.  Any
 * domains that extend EavEntityDatatypeAuditable will reference a record in this
 * domain when changes are made.
 */
class EavAttributeAuditLog {

	Integer partyId      // the person/party that changed the record
	String comment       // the reason for the change
	Date dateCreated = new Date()

	static mapping = {
		version false
		columns {
			id column: 'audit_id'
		}
	}

	static constraints = {
		dateCreated nullable: true
	}
}
