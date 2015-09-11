package com.tdsops.tm.enums.domain

/**
 * Password Reset Status
 */
enum PasswordResetStatus {

	PENDING,
	COMPLETED,
	VOIDED,
	EXPIRED;

	static PasswordResetStatus safeValueOf(String key) {
		PasswordResetStatus obj
		try {
			obj = key as PasswordResetStatus
		} catch (e) { }
		return obj
	}

}