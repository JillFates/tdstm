package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * AuthenticationRequestMode represents the different modes that the Authentication Login request will pass the credentials
 * to the target system.
 */
@CompileStatic
enum AuthenticationRequestMode {
    BASIC_AUTH('Basic Auth'),
    FORM_VARS('Form Variables')

	private final String label

	AuthenticationRequestMode(String label) {
		this.label = label
	}

	static AuthenticationRequestMode getByValue(String label) {
		return values().find { it.label == label }
	}

	static toMap() {
		values().collectEntries { e ->
			[(e.name()): e.toString()]
		}
	}

	@Override
	String toString() {
		return this.label
	}
}
