package com.tdsops.common.exceptions

/**
 * Exception used to signal a configuration issue with the application
 */
class ConfigurationException extends RuntimeException {

	public ConfigurationException() {
		super()
	}
		
	public ConfigurationException(message) {
		super(message)
	}
}
