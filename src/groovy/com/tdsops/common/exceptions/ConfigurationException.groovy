package com.tdsops.common.exceptions

/**
 * Exception used to signal a configuration issue with the application
 */
class ConfigurationException extends RuntimeException {

	public ConfigurationException() {
		super()
	}

	public ConfigurationException(String message) {
		super(message)
	}

	public ConfigurationException(groovy.lang.GString message) {
		super(message.toString())
	}
}
