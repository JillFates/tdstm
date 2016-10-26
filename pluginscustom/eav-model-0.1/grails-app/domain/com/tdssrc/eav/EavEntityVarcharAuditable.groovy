package com.tdssrc.eav

/**
 * Should be extended by entity domains that have varchar type attributes and require audit log of changes.
 */
abstract class EavEntityVarcharAuditable extends EavEntityDatatypeAuditable {
	String value
}
