package com.tdssrc.eav

/*
 * The EavEntityVarcharAuditable domain class should be extended by entity domains 
 * that have varchar type attributes and require audit log of changes.
 */
abstract class EavEntityVarcharAuditable extends EavEntityDatatypeAuditable {
	String value

	static constraints = {
		value( size: 0..255 )
	}

}