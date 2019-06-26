package com.tdsops.common.security.spring

import com.tdssrc.grails.WebUtil
import grails.plugin.springsecurity.web.authentication.GrailsUsernamePasswordAuthenticationFilter
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import net.transitionmanager.service.SecurityService
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.util.Assert

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TdsLocalAuthenticationFilter extends GrailsUsernamePasswordAuthenticationFilter {

	protected boolean postOnly

	SecurityService securityService


	/**
	 * Refactor the method for returning the username provided to work based
	 * on a standard JSON request body, if isAjax, and to get the username from the request parameter otherwise.
	 *
	 * In Grails 2 I can't seem to find a statically compilable way to get at the JSON, as it comes from a private inner class...
	 * @param request The HttpServletRequest
	 *
	 * @return the username as a string
	 */
	@CompileDynamic
	protected String obtainUsername(HttpServletRequest request) {
		if(WebUtil.isAjax(request)) {
			return request.JSON[usernameParameter]
		}else{
			return request.getParameter(this.usernameParameter)
		}
	}

	/**
	 * Refactor the method for returning the password provided to work based
	 * on a standard JSON request body, if isAjax, and to get the password from the request parameter otherwise.
	 * @param request The HttpServletRequest
	 *
	 * In Grails 2 I can't seem to find a statically compilable way to get at the JSON, as it comes from a private inner class...
	 * @return the password as a string.
	 */
	@CompileDynamic
	protected String obtainPassword(HttpServletRequest request) {
		if(WebUtil.isAjax(request)) {
			return request.JSON[passwordParameter]
		}else{
			return request.getParameter(this.passwordParameter)
		}
	}

	Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

		UsernamePasswordAuthorityAuthenticationToken authRequest = null

		try {

			if (postOnly && request.method != 'POST') {
				throw new AuthenticationServiceException('Authentication method not supported: ' + request.method)
			}

			// checkLocalSecurityEnabled
			if (!securityService.userLocalConfig.enabled) {
				logger.warn 'Local security is not enabled'
				throw new AuthenticationServiceException('Local database-backed authentication is disabled')
			}

			String username = obtainUsername(request)?.trim() ?: null
			String password = obtainPassword(request) ?: null

			// checkValidUsername
			if (!username || !password) {
				throw new BadCredentialsException('Missing user credentials')
			}

			logger.debug "Start authentication for username '$username'"

			authRequest = new UsernamePasswordAuthorityAuthenticationToken(
					username, password, request.getParameter('authority'), request.getParameter('rememberMe') == 'true',
					(String) request.getSession(false)?.getAttribute('savedUrlForwardURI'),
					request.getParameter('targetUri'), request.getHeader('User-Agent'))

			setDetails(request, authRequest)


			UsernamePasswordAuthenticationToken authentication =
					(UsernamePasswordAuthenticationToken)authenticationManager.authenticate(authRequest)
			return new UsernamePasswordAuthorityAuthenticationToken(authentication, authRequest)
		}
		catch (AuthenticationException e) {
 			throw new WrappedAuthenticationException(e, authRequest)
		}
	}

	protected void initFilterBean() {
		Assert.notNull securityService, 'securityService is required'
	}

	void setPostOnly(boolean postOnly) {
		this.postOnly = postOnly
		super.setPostOnly postOnly
	}
}
