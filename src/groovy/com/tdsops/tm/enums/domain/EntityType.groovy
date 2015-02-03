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
	static final String DEVICE=AE
	static final String LOGICAL_STORAGE=STORAGE
	static final getList() {
		return [ AE, APP, DB, STORAGE, DEVICE ]
	}
	static final getListAsMap() {
		return [ asset:AE, app:APP, db:DB, file:STORAGE, device: DEVICE ]
	}
	static final getListAsCategory(type){
		def ctgType=[ (AE):'tt_asset', (APP):'tt_app', (DB):'tt_database', (STORAGE):'tt_storage' ]
		return ctgType.("$type")
	}
	static final getKeyByText( text ){
		def ctgType=[ 'Server' : AE, 'Storage':STORAGE, 'Database':DB, Application:APP, 'Device' : DEVICE, 'Logical Storage' : LOGICAL_STORAGE, 'Files' : STORAGE]
		return ctgType[text]
	}
}
