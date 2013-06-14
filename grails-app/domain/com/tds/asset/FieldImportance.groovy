package com.tds.asset

import com.tdsops.tm.enums.domain.EntityType

class FieldImportance {
	
	String entityType
	Project project
	String config

    static constraints = {
		entityType( nullable:false, inList:EntityType.getList())
		project( nullable:false, unique:['entityType'])
		config( nullable:true, blank:true )
    }
	
	static mapping  = {
		version false
		columns {
			config sqltype: 'text'
		}
	}
}