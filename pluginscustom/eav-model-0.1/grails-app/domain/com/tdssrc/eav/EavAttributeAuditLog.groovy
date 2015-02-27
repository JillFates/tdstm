package com.tdssrc.eav
import org.apache.shiro.SecurityUtils
/*
 * The EavAttributeAuditLog provides an audit log tracking the user, date and
 * reason for the change.  Any domains that extend EavEntityDatatypeAuditable will
 * reference a record in this domain when changes are made.
 */
class EavAttributeAuditLog {
	Integer	partyId		// This is to be set to the person/party that changed the record
	String	comment		// Used to allow user or application to indicate the reason for the change
	Date dateCreated = new Date()
	
	static mapping = {
		version false
		columns {
			id column:'audit_id'
		}
	}

	static constraints = {
		comment( size: 0..255 )
		dateCreated( nullable:true )
	}
}
