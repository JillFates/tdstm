package com.tdsops.common.exceptions

/**
 * Exception used to inform invalid behaviour in the service layer.
 */
class ServiceException extends Exception {

	def messageCode
	def messageArgs

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String message, String messageCode, messageArgs) {
		super(message);
		this.messageCode = messageCode
		this.messageArgs = messageArgs
	}

}