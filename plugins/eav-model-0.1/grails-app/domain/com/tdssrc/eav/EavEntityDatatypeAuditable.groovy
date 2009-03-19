package com.tdssrc.eav

abstract class EavEntityDatatypeAuditable extends EavEntityDatatype {
	String auditAction

	static mapping = {
		version false
	}

	static constraints = {
		auditAction( blank:false, inList:['I', 'U', 'D'] )
	}

}
