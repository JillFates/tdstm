package net.transitionmanager.service

import groovy.transform.CompileStatic

/**
 * Indicates that an invalid parameter was received
 */
@CompileStatic
class InvalidParamException extends RuntimeException {

	InvalidParamException() {
		super()
	}

	InvalidParamException(CharSequence message) {
		super(message.toString())
	}
}
