package com.tdsops.tm.enums.domain

/**
 * Define all the possible user audit severities
 */
enum UserAuditSeverity {

	INFO,
	WARNING,
	CRITICAL;

	static UserAuditSeverity safeValueOf(String key) {
		UserAuditSeverity obj
		try {
			obj = key as UserAuditSeverity
		} catch (e) { }
		return obj
	}

}