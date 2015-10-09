package com.tdsops.common.exceptions

/**
 * Exception used to inform invalid behaviour in the service layer.
 */
class ServiceException extends Exception {

	String messageCode
	List messageArgs

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String message, String messageCode) {
		super(message);
		this.messageCode = messageCode
	}

	public ServiceException(String message, String messageCode, List messageArgs) {
		super(message);
		this.messageCode = messageCode
		this.messageArgs = messageArgs
	}

}