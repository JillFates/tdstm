package com.tdsops.tm.enums.domain

/**
 * Email Dispatch Origin
 */
enum EmailDispatchOrigin {

	PASSWORD_RESET,
	TASK,
	ACTIVATION;

	static EmailDispatchOrigin safeValueOf(String key) {
		EmailDispatchOrigin obj
		try {
			obj = key as EmailDispatchOrigin
		} catch (e) { }
		return obj
	}

}