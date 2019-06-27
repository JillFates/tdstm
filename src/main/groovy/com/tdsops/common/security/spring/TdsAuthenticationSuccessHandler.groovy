package com.tdsops.common.security.spring

import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.security.SecurityUtil
import com.tdsops.tm.enums.domain.StartPageEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.JsonUtil
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.security.AuditService
import net.transitionmanager.notice.NoticeService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.person.UserService
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
	NoticeService noticeService

	@Transactional
	void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
			throws ServletException, IOException {

		Assert.isInstanceOf(UsernamePasswordAuthorityAuthenticationToken, auth,
				'This workflow expects an UsernamePasswordAuthorityAuthenticationToken instance here')

		try {
			String redirectUri
			String unacknowledgedNoticesUri = '/module/notice'
			Boolean hasUnacknowledgedNotices = false
			UsernamePasswordAuthorityAuthenticationToken authentication = (UsernamePasswordAuthorityAuthenticationToken) auth

			UserLogin userLogin = securityService.userLogin
			auditService.saveUserAudit UserAuditBuilder.login()

			if (securityService.shouldLockoutAccount(userLogin)) {
				// lock account
				userService.lockoutAccountByInactivityPeriod(userLogin)
				setAccountLockedOutAttribute(request)
				redirectUri = '/module/auth/login'
			} else {
				userService.updateLastLogin(userLogin)
				userService.resetFailedLoginAttempts(userLogin)
				userService.setLockedOutUntil(userLogin, null)
				// create a new UserLoginProjectAccess to account later for user logins on metric recollection
				userService.createUserLoginProjectAccess(userLogin)

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
					redirectUri = authentication.savedUrlForwardURI ?: authentication.targetUri ?: redirectToPrefPage()
				}

				// check if user has unacknowledged notices, if so, redirect user to notices page
				Project project = securityService.userCurrentProject
				hasUnacknowledgedNotices = noticeService.hasUnacknowledgedNoticesForLogin(project, request.getSession(), userLogin.person)
				if (hasUnacknowledgedNotices) {
					addAttributeToSession(request, SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES, true)
					addAttributeToSession(request, SecurityUtil.REDIRECT_URI, redirectUri)
				}

				removeAttributeFromSession(request, TdsHttpSessionRequestCache.SESSION_EXPIRED)
				removeAttributeFromSession(request, SecurityUtil.ACCOUNT_LOCKED_OUT)
			}

			// This map will contain all the user-related data that needs to be sent in the response's payload.
			Map signInInfoMap = [
			    userContext: userService.getUserContext().toMap(),
				notices: [
					noticesList: noticeService.fetchPersonPostLoginNotices(securityService.loadCurrentPerson()),
					redirectUrl: hasUnacknowledgedNotices ? unacknowledgedNoticesUri : redirectUri
				]
			]
			response.setHeader('content-type', 'application/json')
			PrintWriter responseWriter = response.getWriter()
			responseWriter.print(JsonUtil.toJson(signInInfoMap))
			responseWriter.flush()

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
				return securityService.hasPermission('BundleView') ? '/module/planning/dashboard' : '/projectUtil'
			}
			if (startPage == StartPageEnum.ADMIN_PORTAL.value) {
				return '/admin/home'
			}
		}

		if (startPage == StartPageEnum.USER_DASHBOARD.value || startPage == null) {
			'/module/user/dashboard'
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
		Assert.notNull noticeService, 'noticeService is required'
	}

	/**
	 * Set account locked session flag used to show message in login screen
	 * @param request
	 */
	private void setAccountLockedOutAttribute(HttpServletRequest request) {
		addAttributeToSession(request, SecurityUtil.ACCOUNT_LOCKED_OUT,  true)
	}

	/**
	 * Puts an attribute on the session
	 * @param request
	 * @param attribute
	 * @param value
	 */
	private void addAttributeToSession(HttpServletRequest request, String attribute, Object value) {
		HttpSession session = request.getSession()
		if (session) {
			session.setAttribute(attribute,  value)
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
