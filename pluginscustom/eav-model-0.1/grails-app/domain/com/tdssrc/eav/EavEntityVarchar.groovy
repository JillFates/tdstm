package com.tdssrc.eav

abstract class EavEntityVarchar extends EavEntityDatatype {
	String	value

	static constraints = {
		value( size: 0..255 )
	}
}