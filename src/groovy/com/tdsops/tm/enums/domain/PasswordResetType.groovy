package com.tdsops.tm.enums.domain

/**
 * Password Reset Type
 */
enum PasswordResetType {

	FORGOT_MY_PASSWORD,
	ADMIN_RESET,
	WELCOME;

	static PasswordResetType safeValueOf(String key) {
		PasswordResetType obj
		try {
			obj = key as PasswordResetType
		} catch (e) { }
		return obj
	}

}