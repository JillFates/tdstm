package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum PasswordResetType {

	FORGOT_MY_PASSWORD,
	ADMIN_RESET,
	WELCOME

	static PasswordResetType safeValueOf(String key) {
		values().find { it.name() == key }
	}
}
