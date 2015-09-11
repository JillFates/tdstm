package com.tdsops.tm.enums.domain

/**
 * Define all the possible user audit classifications
 */
enum UserAuditClassification {

	LOGIN,
	ADMIN,
	USER_MGMT,
	DATA_MGMT;

	static UserAuditClassification safeValueOf(String key) {
		UserAuditClassification obj
		try {
			obj = key as UserAuditClassification
		} catch (e) { }
		return obj
	}

}