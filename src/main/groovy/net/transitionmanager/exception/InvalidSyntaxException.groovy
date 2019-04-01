package net.transitionmanager.exception

import groovy.transform.CompileStatic

/**
 * Indicates that some source code has invalid syntax.
 */
@CompileStatic
class InvalidSyntaxException extends RuntimeException {
	InvalidSyntaxException(CharSequence message) {
		super(message.toString())
	}
}
