package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Define all the possible user audit classifications
 */
@CompileStatic
enum UserAuditClassification {

	LOGIN,
	ADMIN,
	USER_MGMT,
	DATA_MGMT

	static UserAuditClassification safeValueOf(String key) {
		values().find { it.name() == key }
	}
}
