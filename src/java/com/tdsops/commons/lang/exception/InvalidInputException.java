package com.tdsops.commons.lang.exception;

import org.apache.commons.lang3.exception.*;

/**
 * A ContextedRuntimeException that is to be used for notify consumer that input parameters were invalid
 * 
 * @see Apache Commons ContextedException for details http://commons.apache.org/lang/api/org/apache/commons/lang3/exception/ContextedException.html
 */

public class InvalidInputException extends ContextedRuntimeException {
	
	public InvalidInputException( String message ) {
		super(message);
	}

	public InvalidInputException( String message, Throwable throwable ) {
		super(message, throwable);
	}

	public InvalidInputException( String message, Throwable throwable, ExceptionContext context ) {
		super(message, throwable, context);
	}
	
	public InvalidInputException( Throwable throwable ) {
		super(throwable);
	}

}
