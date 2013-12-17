package com.tdsops.tm.enums.domain


class AssetCableStatus {
	static final String ASSIGNED='Assigned'
	static final String CABLED='Cabled'
	static final String UNKNOWN='Unknown'
	static final String EMPTY='Empty'
	//
	static final getList() {
		return [ UNKNOWN, CABLED, ASSIGNED, EMPTY]
	}
}