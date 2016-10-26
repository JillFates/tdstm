package net.transitionmanager.service

import groovy.transform.CompileStatic

/**
 * Indicates that some source code has invalid syntax.
 */
@CompileStatic
class LogicException extends RuntimeException {
	LogicException(CharSequence message) {
		super(message.toString())
	}
}
