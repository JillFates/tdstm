package net.transitionmanager.exception

import groovy.transform.CompileStatic

/**
 * Informs invalid behaviour in the service layer.
 */
@CompileStatic
class ServiceException extends Exception {

	final String messageCode
	final List messageArgs

	ServiceException(String message) {
		super(message)
	}

	ServiceException(String message, String messageCode) {
		super(message)
		this.messageCode = messageCode
	}

	ServiceException(String message, String messageCode, List messageArgs) {
		super(message)
		this.messageCode = messageCode
		this.messageArgs = messageArgs
	}
}
