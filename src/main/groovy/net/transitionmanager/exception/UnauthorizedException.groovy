package net.transitionmanager.exception

import groovy.transform.CompileStatic

@CompileStatic
class UnauthorizedException extends RuntimeException {

	UnauthorizedException() {
		super()
	}

	UnauthorizedException(CharSequence message) {
		super(message.toString())
	}
}
