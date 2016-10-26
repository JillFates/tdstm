package net.transitionmanager.service

import groovy.transform.CompileStatic

@CompileStatic
class EmptyResultException extends RuntimeException {
	EmptyResultException() {
		super()
	}

	EmptyResultException(CharSequence message) {
		super(message.toString())
	}
}
