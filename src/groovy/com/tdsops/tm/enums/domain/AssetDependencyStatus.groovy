package com.tdsops.tm.enums.domain

/**
 * AssetDependencyStatus - represents the valid options for the AssetDependancy.status property
 */ 
class AssetDependencyStatus {
	static final String VALIDATED='Validated'
	static final String NA='Not Applicable'
	static final String QUESTIONED='Questioned'
	static final String UNKNOWN='Unknown'
	static final String ARCHIVED='Archived'
	static final getList() {
		return [ UNKNOWN, QUESTIONED, VALIDATED, ARCHIVED, NA ]
	}
}	
