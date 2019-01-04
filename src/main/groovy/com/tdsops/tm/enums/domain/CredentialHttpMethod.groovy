package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum CredentialHttpMethod {
	POST,
	GET,
	PUT

	static CredentialHttpMethod getByValue(String method) {
		return values().find { it.name() == method }
	}

	static List<String> names() {
		List<String> names = new ArrayList<>()
		for (CredentialHttpMethod e : values()) {
			names.add(e.name())
		}
		return names
	}
}
