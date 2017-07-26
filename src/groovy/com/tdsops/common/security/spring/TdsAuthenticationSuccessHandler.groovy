package com.tdsops.common.security.spring

import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.security.SecurityUtil
import com.tdsops.tm.enums.domain.StartPageEnum
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
import javax.servlet.http.HttpSession

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
			String redirectUri
			UsernamePasswordAuthorityAuthenticationToken authentication = (UsernamePasswordAuthorityAuthenticationToken) auth

			UserLogin userLogin = securityService.userLogin
			auditService.saveUserAudit UserAuditBuilder.login()

			if (securityService.shouldLockoutAccount(userLogin)) {
				// lock account
				userService.lockoutAccountByInactivityPeriod(userLogin)
				setAccountLockedOutAttribute(request)
				redirectUri = '/auth/login'
			} else {
				userService.updateLastLogin(userLogin)
				userService.resetFailedLoginAttempts(userLogin)
				userService.setLockedOutUntil(userLogin, null)

				String userAgent = authentication.userAgent
				boolean browserTestiPad = userAgent.toLowerCase().contains('ipad')
				boolean browserTest = userAgent.toLowerCase().contains('mobile')

				if (browserTest) {
					if (browserTestiPad) {
						redirectUri = '/projectUtil'
					} else {
						redirectUri = '/task/listUserTasks?viewMode=mobile'
					}
				} else {
					redirectUri = authentication.savedUrlForwardURI ?:
							authentication.targetUri ?:
									requestCache.getRequest(request, response)?.redirectUrl ?:
											redirectToPrefPage()
				}

				removeAttributeFromSession(request, TdsHttpSessionRequestCache.SESSION_EXPIRED)
				removeAttributeFromSession(request, SecurityUtil.ACCOUNT_LOCKED_OUT)
			}

			redirectStrategy.sendRedirect request, response, redirectUri
		} finally {
			// always remove the saved request
			requestCache.removeRequest request, response
		}
	}

	private String redirectToPrefPage() {
		String startPage = userPreferenceService.getPreference(PREF.START_PAGE)
		if (userPreferenceService.getCurrentProjectId()) {
			if (startPage == StartPageEnum.PROJECT_SETTINGS.value) {
				return '/projectUtil'
			}
			if (startPage == StartPageEnum.CURRENT_DASHBOARD.value || startPage == StartPageEnum.PLANNING_DASHBOARD.value) {
				return securityService.hasPermission('BundleView') ? '/moveBundle/planningStats' : '/projectUtil'
			}
			if (startPage == StartPageEnum.ADMIN_PORTAL.value) {
				return '/admin/home'
			}
		}

		if (startPage == StartPageEnum.USER_DASHBOARD.value || startPage == null) {
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

	/**
	 * Set account locked session flag used to show message in login screen
	 * @param request
	 */
	private void setAccountLockedOutAttribute(HttpServletRequest request) {
		HttpSession session = request.getSession()
		if (session) {
			session.setAttribute(SecurityUtil.ACCOUNT_LOCKED_OUT,  true)
		}
	}

	/**
	 * Removes an attribute from session
	 * @param request
	 * @param attribute
	 */
	private void removeAttributeFromSession(HttpServletRequest request, String attribute) {
		HttpSession session = request.getSession()
		if (session && session[attribute]) {
			session.removeAttribute(attribute)
		}
	}

}
