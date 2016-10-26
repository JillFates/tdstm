package com.tdsops.common.security.spring

import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.AuditService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.UserService
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.core.Authentication
import org.springframework.util.Assert

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TdsAuthenticationSuccessHandler extends AjaxAwareAuthenticationSuccessHandler implements InitializingBean {

	AuditService auditService
	SecurityService securityService
	UserPreferenceService userPreferenceService
	UserService userService

	@Transactional
	void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
			throws ServletException, IOException {

		Assert.isInstanceOf(UsernamePasswordAuthorityAuthenticationToken, auth,
				'This workflow expects an UsernamePasswordAuthorityAuthenticationToken instance here')

		try {
			UsernamePasswordAuthorityAuthenticationToken authentication = (UsernamePasswordAuthorityAuthenticationToken) auth

			UserLogin userLogin = securityService.userLogin

			auditService.saveUserAudit UserAuditBuilder.login()

			// pre-load some common preference values
			userPreferenceService.getCurrentProjectId()
			userPreferenceService.getMoveBundleId()
			userPreferenceService.getMoveEventId()
			userPreferenceService.getTimeZone()
			userPreferenceService.getDateFormat()

			userService.updateLastLogin()

			// checkFailsCount

			// most of the work for this check is done in the failure handler; here just
			// reset the count to zero after a successful authentication
			if (userLogin.failedLoginAttempts) {
				userLogin.failedLoginAttempts = 0
			}

			String userAgent = authentication.userAgent
			boolean browserTestiPad = userAgent.toLowerCase().contains('ipad')
			boolean browserTest = userAgent.toLowerCase().contains('mobile')

			String redirectUri
			if (browserTest) {
				if (browserTestiPad) {
					redirectUri = '/projectUtil'
				}
				else {
					redirectUri = '/task/listUserTasks?viewMode=mobile'
				}
			}
			else {
				redirectUri = authentication.savedUrlForwardURI ?:
				              authentication.targetUri ?:
				              requestCache.getRequest(request, response)?.redirectUrl ?:
				              redirectToPrefPage()
			}

			redirectStrategy.sendRedirect request, response, redirectUri
		}
		finally {
			// always remove the saved request
			requestCache.removeRequest request, response
		}
	}

	private String redirectToPrefPage() {
		String startPage = userPreferenceService.getPreference(PREF.START_PAGE)
		if (userPreferenceService.getCurrentProjectId()) {
			if (startPage == 'Project Settings') {
				return '/projectUtil'
			}
			if (startPage == 'Current Dashboard') {
				return securityService.hasPermission('MoveBundleShowView') ? '/moveBundle/planningStats' : '/projectUtil'
			}
			if (startPage == 'Admin Portal') {
				return '/admin/home'
			}
		}

		if (startPage == 'User Dashboard' || startPage == null) {
			'/dashboard/userPortal'
		}
		else {
			'/projectUtil'
		}
	}

	void afterPropertiesSet() {
		Assert.notNull auditService, 'auditService is required'
		Assert.notNull securityService, 'securityService is required'
		Assert.notNull userPreferenceService, 'userPreferenceService is required'
		Assert.notNull userService, 'userService is required'
	}
}
