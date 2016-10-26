package com.tds.asset

import com.tdssrc.eav.EavEntityDatatype

class AssetEntityVarchar extends EavEntityDatatype {

	String value
	String auditAction

	static belongsTo = [assetEntity: AssetEntity]

	static constraints = {
		auditAction blank: false, inList: ['I', 'U', 'D']
	}

	static mapping = {
		tablePerHierarchy false
		version false
	}
}
