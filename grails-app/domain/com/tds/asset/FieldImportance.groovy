package com.tds.asset

import com.tdsops.tm.enums.domain.EntityType

class FieldImportance {
	
	String entityType
	Project project
	String config
	String phase

    static constraints = {
		entityType( nullable:false, inList:EntityType.getList())
		project( nullable:false, unique:['entityType','phase'])
		config( nullable:true, blank:true )
		phase( nullable:false, blank:false )
    }
	
	static mapping  = {
		version false
		columns {
			config sqltype: 'text'
		}
	}
}