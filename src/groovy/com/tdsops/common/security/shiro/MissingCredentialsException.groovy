package com.tdsops.common.security.shiro

import org.apache.shiro.ShiroException

/**
 * Used in user authentication on Realms to indicate that the authentication because the user didn't provide credentials
 */
class MissingCredentialsException extends ShiroException {

	public MissingCredentialsException(String message) {
		super(message)
	}	
}