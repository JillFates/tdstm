package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum AuthenticationMethod {
	BASIC_AUTH('Basic Auth'),
	COOKIE('Cookie Session'),
	HEADER('Header Session'),
	JWT('JSON Web Tokens')
	// Not yet implemented
	// AWS ('Amazon AWS Security')
	// OAUTH ('OAuth')

	private final String method

	AuthenticationMethod(String method) {
		this.method = method
	}

	static AuthenticationMethod getByValue(String method) {
		return values().find { it.method == method }
	}

	static toMap() {
		values().collectEntries { e ->
			[(e.name()): e.toString()]
		}
	}

	@Override
	String toString() {
		return this.method
	}
}
