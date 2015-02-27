package com.tds.asset
class AssetEntityVarchar extends com.tdssrc.eav.EavEntityDatatype {
	
	String value
	String auditAction

	static mapping = {
		tablePerHierarchy false
		version false
	}

	static constraints = {
		value( size: 0..255 )
		auditAction( blank:false, inList:['I', 'U', 'D'] )
	}

	static belongsTo = [ assetEntity : AssetEntity ]

}