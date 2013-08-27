package com.tdsops.tm.enums.domain

/**
 * AssetEntityPlanStatus - represents the valid options for the AssetEntity domain property planStatus.
 *
 */
class AssetEntityPlanStatus {
	static final String UNASSIGNED='Unassigned'
	static final String ASSIGNED='Assigned'
	static final String CONFIRMED='Confirmed'
	static final String LOCKED='Locked'
	static final String MOVED='Moved'
	
	static final String[] list = [UNASSIGNED, ASSIGNED, CONFIRMED, LOCKED, MOVED]
	static final getList() {
		return list as List
	}
}
