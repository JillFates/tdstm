package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum RoleTypeGroup {

	STAFF('Staff'),
	SYSTEM('System')

	final String value

	private RoleTypeGroup(String label) {
		value = label
	}

	String value() { value }

	String toString() { value }

	static final List<String> list = (values().collect { RoleTypeGroup it -> it.value } as List).asImmutable()
}
