package com.tdsops.common.security.spring

import groovy.transform.CompileStatic
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.authentication.WebAuthenticationDetails

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class UsernamePasswordAuthorityAuthenticationToken extends UsernamePasswordAuthenticationToken {

	final String authority
	final boolean rememberMe
	final String savedUrlForwardURI
	final String targetUri
	final String userAgent

	/**
	 * Constructor used by the filter using request params.
	 * @param username  the username
	 * @param password  the password
	 * @param authority  the authority
	 * @param rememberMe  optional boolean param to support remember-me cookies
	 * @param savedUrlForwardURI  optional, likely not used; the value stored in the HTTP session to redirect to
	 * @param targetUri  optional uri to redirect to after successful login
	 * @param userAgent  the browser user-connector header to determine how to redirect
	 */
	UsernamePasswordAuthorityAuthenticationToken(String username, String password, String authority, boolean rememberMe,
	                                             String savedUrlForwardURI, String targetUri, String userAgent) {
		super(username, password)
		this.authority = authority
		this.rememberMe = rememberMe
		this.savedUrlForwardURI = savedUrlForwardURI
		this.targetUri = targetUri
		this.userAgent = userAgent
	}

	/**
	 * Constructor used after a successful authentication to add in the authority from the original
	 * authentication instance. Use getName() to access the username since the principal is now a TdsUserDetails.
	 * @param authentication  the instance created by the auth provider
	 * @param original  the instance created by the filter
	 */
	UsernamePasswordAuthorityAuthenticationToken(UsernamePasswordAuthenticationToken authentication,
	                                             UsernamePasswordAuthorityAuthenticationToken original) {
		super(authentication.principal, authentication.credentials, authentication.authorities)
		details = authentication.details
		authority = original.authority
		rememberMe = original.rememberMe
		savedUrlForwardURI = original.savedUrlForwardURI
		targetUri = original.targetUri
		userAgent = original.userAgent
	}

	/**
	 * Convenience overload with covariant type. We do the casting so you don't have to!
	 * @return  the details
	 */
	WebAuthenticationDetails getDetails() {
		(WebAuthenticationDetails)super.getDetails()
	}

	/**
	 * @return  the IP address captured as part of the 'authentication details' when authenticating
	 */
	String getIpAddress() {
		details.remoteAddress
	}
}
