package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * The valid options for the AssetEntity domain property planStatus.
 */
@CompileStatic
class AssetEntityPlanStatus {

	public static final String UNASSIGNED = 'Unassigned'
	public static final String ASSIGNED   = 'Assigned'
	public static final String CONFIRMED  = 'Confirmed'
	public static final String LOCKED     = 'Locked'
	public static final String MOVED      = 'Moved'

	static final List<String> list = [UNASSIGNED, ASSIGNED, CONFIRMED, LOCKED, MOVED].asImmutable()
}
