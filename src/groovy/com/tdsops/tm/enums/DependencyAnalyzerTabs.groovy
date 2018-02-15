package com.tdsops.tm.enums

import groovy.transform.CompileStatic

/**
 * Enumeration that contains all the Dependency Analyzer tab names.
 * Created by ecantu on 02/14/2018.
 */
@CompileStatic
enum DependencyAnalyzerTabs {
	MAP('MAP'),
	ALL('ALL'),
	APPS('APPS'),
	SERVERS('SERVERS'),
	DATABASES('DATABASES'),
	STORAGE('STORAGE')

	private String value

	DependencyAnalyzerTabs(String value) {
		this.value = value
	}

	@Override
	String toString() {
		return value
	}

	String getValue() {
		return this.value
	}

}
