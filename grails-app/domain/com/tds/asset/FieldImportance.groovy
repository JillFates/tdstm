package com.tds.asset

import com.tdsops.tm.enums.domain.EntityType
import net.transitionmanager.domain.Project

class FieldImportance {

	String entityType
	Project project
	String config

	static constraints = {
		config nullable: true
		entityType inList: EntityType.list
		project unique: ['entityType']
	}

	static mapping = {
		version false
		columns {
			config sqltype: 'text'
		}
	}
}
