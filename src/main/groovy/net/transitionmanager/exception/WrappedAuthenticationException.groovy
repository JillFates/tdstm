package net.transitionmanager.exception

import com.tdsops.common.security.spring.UsernamePasswordAuthorityAuthenticationToken
import groovy.transform.CompileStatic
import org.springframework.security.core.AuthenticationException

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class WrappedAuthenticationException extends AuthenticationException {

	final UsernamePasswordAuthorityAuthenticationToken token

	WrappedAuthenticationException(AuthenticationException e, UsernamePasswordAuthorityAuthenticationToken token) {
		super(e.message, e)
		this.token = token
	}
}
