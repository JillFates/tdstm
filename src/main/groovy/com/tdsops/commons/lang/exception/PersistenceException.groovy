package com.tdsops.commons.lang.exception

import org.apache.commons.lang3.exception.ContextedRuntimeException

/**
 * A ContextedRuntimeException to be used for notify consumer that persisting data failed
 *
 * See Apache Commons ContextedException for details http://commons.apache.org/lang/api/org/apache/commons/lang3/exception/ContextedException.html
 */
class PersistenceException extends ContextedRuntimeException {
	PersistenceException(CharSequence message) {
		super(message.toString())
	}
}
