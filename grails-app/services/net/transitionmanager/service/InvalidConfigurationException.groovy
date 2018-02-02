package net.transitionmanager.service

import groovy.transform.CompileStatic

/**
 * Indicates that there was an error with the configuration
 */
@CompileStatic
class InvalidConfigurationException extends RuntimeException {

	InvalidConfigurationException() {
		super()
	}

	InvalidConfigurationException(CharSequence message) {
		super(message.toString())
	}
}
