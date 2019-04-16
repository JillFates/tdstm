package net.transitionmanager.exception

import org.apache.commons.lang3.exception.ContextedRuntimeException
import org.apache.commons.lang3.exception.ExceptionContext

/**
 * A ContextedRuntimeException that is to be used for notify consumer that input parameters were invalid
 *
 * See Apache Commons ContextedException for details http://commons.apache.org/lang/api/org/apache/commons/lang3/exception/ContextedException.html
 */
class InvalidInputException extends ContextedRuntimeException {

	InvalidInputException(String message) {
		super(message)
	}

	InvalidInputException(String message, Throwable throwable) {
		super(message, throwable)
	}

	InvalidInputException(String message, Throwable throwable, ExceptionContext context) {
		super(message, throwable, context)
	}

	InvalidInputException(Throwable throwable) {
		super(throwable)
	}
}
