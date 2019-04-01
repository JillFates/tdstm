package net.transitionmanager.exception

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
