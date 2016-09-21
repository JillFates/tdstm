package com.tdsops.common.security.shiro

import org.apache.shiro.authc.pam.FirstSuccessfulStrategy
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.realm.Realm

/**
 * Using this strategy if some realm throw an exception then the authentication fails and the exception is propagated.
 */
//public class FirstExceptionStrategy extends AtLeastOneSuccessfulStrategy {
public class FirstExceptionStrategy extends FirstSuccessfulStrategy {

	@Override
	public AuthenticationInfo afterAttempt(
			Realm realm,
			AuthenticationToken token,
			AuthenticationInfo singleRealmInfo,
			AuthenticationInfo aggregateInfo,
			Throwable throwable) throws AuthenticationException {

		// If a realm throws an AuthenticationException then the authentication is stopped and no new realms are checked
		if ( (throwable != null) &&
			 (throwable instanceof AuthenticationException) ) {
			throw (AuthenticationException) throwable
		}

		return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, throwable)
	}

}