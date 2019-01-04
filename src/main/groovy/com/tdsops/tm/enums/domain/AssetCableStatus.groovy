package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
class AssetCableStatus {

	public static final String ASSIGNED = 'Assigned'
	public static final String CABLED   = 'Cabled'
	public static final String UNKNOWN  = 'Unknown'
	public static final String EMPTY    = 'Empty'

	static final List<String> list = [UNKNOWN, CABLED, ASSIGNED, EMPTY].asImmutable()
}
