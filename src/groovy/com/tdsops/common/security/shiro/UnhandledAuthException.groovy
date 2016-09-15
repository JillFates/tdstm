package com.tdsops.common.security.shiro

import org.apache.shiro.ShiroException

/**
 * Used in user authentication on Realms to indicate that the authentication fails and
 * should be used another Realm to validate the user
 */
class UnhandledAuthException extends ShiroException {

	public UnhandledAuthException(String message) {
		super(message)
	}
}
