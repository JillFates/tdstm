package net.transitionmanager.exception

import groovy.transform.CompileStatic

/**
 * RuntimeException Representing that an expected object was not found.
 */
@CompileStatic
class EmptyResultException extends RuntimeException {
	EmptyResultException() {
		super()
	}

	EmptyResultException(CharSequence message) {
		super(message.toString())
	}
}
