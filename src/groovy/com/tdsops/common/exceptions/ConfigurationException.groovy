package com.tdsops.common.exceptions

import groovy.transform.CompileStatic

/**
 * Signals a configuration issue with the application.
 */
@CompileStatic
class ConfigurationException extends RuntimeException {
	ConfigurationException(CharSequence message) {
		super(message.toString())
	}
}
