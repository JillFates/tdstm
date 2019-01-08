package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Represents the various Security roles that users can be assigned to
 */
@CompileStatic
enum SecurityRole {

	ROLE_USER('User'),             // Limited access
	ROLE_EDITOR('Editor'),         // Client user with moderate access
	ROLE_SUPERVISOR('Supervisor'),
	ROLE_ADMIN('Administrator')    // All rights

	final String value

	private SecurityRole(String label) {
		value = label
	}

	String value() { value }

	static SecurityRole asEnum(String key) {
		values().find { it.name() == key }
	}

	static final List<SecurityRole> keys = (values() as List).asImmutable()

	static final List<String> labels = keys.collect { it.value }.asImmutable()

	static List<String> getLabels(String locale = 'en') { labels }
}
