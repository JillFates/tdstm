package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum EmailDispatchOrigin {

	PASSWORD_RESET,
	TASK,
	ACTIVATION

	static EmailDispatchOrigin safeValueOf(String key) {
		values().find { it.name() == key }
	}
}
