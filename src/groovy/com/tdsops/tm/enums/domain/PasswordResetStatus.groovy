package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum PasswordResetStatus {

	PENDING,
	COMPLETED,
	VOIDED,
	EXPIRED

	static PasswordResetStatus safeValueOf(String key) {
		values().find { it.name() == key }
	}
}
