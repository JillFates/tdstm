package com.tdsops.tm.enums.domain

/**
 * 
 * @author 
 *
 */
class EntityType {
	static final String AE='AssetEntity'
	static final String APP='Application'
	static final String DB='Database'
	static final String STORAGE='Files'
	static final getList() {
		return [ AE, APP, DB, STORAGE ]
	}
	static final getListAsMap() {
		return [ asset:AE, app:APP, db:DB, file:STORAGE ]
	}
}
