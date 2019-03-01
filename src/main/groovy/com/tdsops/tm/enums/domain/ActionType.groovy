package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum ActionType {
	GROOVY_SCRIPT('GroovyScript'),
	POWER_SHELL('PowerShell'),
	UNIX_SHELL('UnixShell'),
	WEB_API('WebAPI')

	final String type;

	ActionType(String type) {
		this.type = type
	}

	static toMap() {
		values().collectEntries { e ->
			[(e.name()): e.type]
		}
	}

	@Override
	public String toString() {
		return this.type
	}
}