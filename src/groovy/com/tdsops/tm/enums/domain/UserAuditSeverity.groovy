package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Defines all the possible user audit severities
 */
@CompileStatic
enum UserAuditSeverity {

	INFO,
	WARNING,
	CRITICAL

	static UserAuditSeverity safeValueOf(String key) {
		values().find { it.name() == key }
	}
}
