package com.tdsops.common.security.spring

import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationFailureHandler
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.AuditService
import net.transitionmanager.service.SecurityService
import grails.web.mapping.LinkGenerator
import grails.web.mvc.FlashScope
import  org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.util.Assert
import org.springframework.web.context.request.RequestContextHolder

import javax.security.auth.login.AccountExpiredException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.quartz.DateBuilder.IntervalUnit.MINUTE
import static org.quartz.DateBuilder.futureDate

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TdsAuthenticationFailureHandler extends AjaxAwareAuthenticationFailureHandler {

	private static final Date IN_ONE_HUNDRED_YEARS = new Date() + 100 * 365

	AuditService auditService
	LinkGenerator grailsLinkGenerator
	MessageSource messageSource
	SecurityService securityService

	void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
			throws IOException, ServletException {

		Assert.isInstanceOf WrappedAuthenticationException, e, ''
			'This workflow expects an WrappedAuthenticationException instance here'

		WrappedAuthenticationException wrapped = (WrappedAuthenticationException) e
		e = (AuthenticationException) wrapped.cause
		UsernamePasswordAuthorityAuthenticationToken authentication = wrapped.token
		String username = authentication?.name

		String userMsg
		if (e instanceof LockedException || e instanceof DisabledException) {
			userMsg = e.message
		}
		else if (e instanceof CredentialsExpiredException || e instanceof AccountExpiredException) {
			userMsg = messageSource.getMessage('userLogin.accountDisabled.message', null, LocaleContextHolder.locale)
		}
		else if (e instanceof BadCredentialsException) {
			userMsg = 'Username and password are required'
		}
		else if (e instanceof UsernameNotFoundException) {
		}
		else if (e instanceof AuthenticationException) {
		}
		else {
			logger.error "Unexpected Authentication Exception for user $username'\n${ExceptionUtil.stackTraceToString(e)}"
		}

		FlashScope flash = ((GrailsWebRequest) RequestContextHolder.currentRequestAttributes()).flashScope
		flash.message = userMsg ?: messageSource.getMessage('login.failed', null, LocaleContextHolder.locale)

		auditService.logMessage(username + ' login attempt failed - ' + e.message)
		auditService.logMessage(username + ' (' + HtmlUtil.getRemoteIp(request) + ') login attempt failed')

		checkFailsCount(username)

		Map<String, String> params = [username: username, targetUri: authentication?.targetUri,
		                              authority: authentication?.authority, rememberMe: authentication?.rememberMe ? authentication?.rememberMe as String : null]
		params.keySet().retainAll { params[it] }
		redirectStrategy.sendRedirect(request, response,
				grailsLinkGenerator.link(controller: 'auth', action: 'login', params: params, base: ' ').substring(1))
	}

	/**
	 * checkFailsCount will keep track of login failure attempts and set a lock out after a configurable
	 * quantity of failures.
	 * @param username - the username that failed to authenticate
	 */
	@Transactional
	private void checkFailsCount(String username) {

		Date now = TimeUtil.nowGMT()

		UserLogin userLogin = UserLogin.findWhere(username: username)

		// Users that haven't been provisioned yet (LDAP) won't exist until they successfully authenticate
		if (userLogin) {
			userLogin.failedLoginAttempts++
			int maxLoginFailureAttempts = (int) securityService.userLocalConfig.maxLoginFailureAttempts

			// Lock out the account if they exceeded the max tries and the account isn't already locked
			if (maxLoginFailureAttempts && userLogin.failedLoginAttempts >= maxLoginFailureAttempts &&
					(!userLogin.lockedOutUntil || userLogin.lockedOutUntil.time < now.time)) {
				String lockoutTime = 'indefintely'
				int failedLoginLockoutPeriodMinutes = (int) securityService.userLocalConfig.failedLoginLockoutPeriodMinutes
				Date lockedOutUntil
				if (failedLoginLockoutPeriodMinutes) {
					lockedOutUntil = futureDate(failedLoginLockoutPeriodMinutes, MINUTE)
					lockoutTime = "for ${failedLoginLockoutPeriodMinutes} minutes"
				} else {
					lockedOutUntil = IN_ONE_HUNDRED_YEARS
				}
				userLogin.lockedOutUntil = lockedOutUntil
				auditService.logMessage("User $userLogin.username, account locked out $lockoutTime")
			}
			userLogin.save()
		}
	}

	void afterPropertiesSet() {
		super.afterPropertiesSet()
		Assert.notNull auditService, 'auditService is required'
		Assert.notNull grailsLinkGenerator, 'grailsLinkGenerator is required'
		Assert.notNull messageSource, 'messageSource is required'
	}
}
