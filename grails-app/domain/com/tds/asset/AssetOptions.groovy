package com.tds.asset

import groovy.transform.CompileStatic

class AssetOptions {

	@CompileStatic
	static enum AssetOptionsType {
		STATUS_OPTION,
		PRIORITY_OPTION,
		DEPENDENCY_TYPE,
		DEPENDENCY_STATUS,
		ENVIRONMENT_OPTION
	}

	AssetOptionsType type
	String value

	static mapping = {
		version false
	}

	static constraints = {
		value blank: false, unique: 'type'
	}
}
