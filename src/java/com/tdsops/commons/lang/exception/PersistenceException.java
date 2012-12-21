package com.tdsops.commons.lang.exception;

import org.apache.commons.lang3.exception.*;

/**
 * A ContextedRuntimeException to be used for notify consumer that persisting data failed
 * 
 * @see Apache Commons ContextedException for details http://commons.apache.org/lang/api/org/apache/commons/lang3/exception/ContextedException.html
 */

class PersistenceException extends ContextedRuntimeException {
	
	public PersistenceException( String message ) {
		super(message);
	}

	public PersistenceException( String message, Throwable throwable ) {
		super(message, throwable);
	}

	public PersistenceException( String message, Throwable throwable, ExceptionContext context ) {
		super(message, throwable, context);
	}
	
	public PersistenceException( Throwable throwable ) {
		super(throwable);
	}

}