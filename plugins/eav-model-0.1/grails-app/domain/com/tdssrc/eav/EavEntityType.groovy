package com.tdssrc.eav

class EavEntityType {
	String		typeCode
	String		domainName

	// Don't believe that we are going to need this but have left it in commented out
	// for the time being.  JPM 3/2009 (see EavEntity)
	// static belongsTo = [EavEntity]

	static hasMany = [ attributes : EavAttribute,
	                   attributeSet : EavAttributeSet ]
	
	static belongsTo = [ attributeSet :EavAttributeSet  ]
	
	static mapping = {
		version false
		columns {
			id column:'entity_type_id'
		}
	}

	static constraints = {
		typeCode( blank:false, unique:true, size: 1..64 )
		domainName(  blank:false, unique:true, size: 1..255 )
	}
}