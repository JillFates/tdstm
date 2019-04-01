package net.transitionmanager.exception

import groovy.transform.CompileStatic

@CompileStatic
class InvalidRequestException extends RuntimeException {
	InvalidRequestException() {
		super()
	}

	InvalidRequestException(CharSequence message) {
		super(message.toString())
	}
}
