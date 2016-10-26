package com.tdssrc.groovy.eav

import groovy.transform.CompileStatic

/**
 * Undicates (along with EavEntityDatatypeAuditable) the audit action that occured on a particular attribute.
 * Not using at this time.
 *
 * @author John
 */
@CompileStatic
enum AuditableAction {
	INSERT,
	UPDATE,
	DELETE
}
