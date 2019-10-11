package com.tdsops.common.security.spring

import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.SecurityUtil
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.StartPageEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.JsonUtil
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.Permission
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

			response.setHeader('content-type', 'application/json')
			PrintWriter responseWriter = response.getWriter()

			String redirectUri
			String unacknowledgedNoticesUri = '/module/notice'
			Boolean hasUnacknowledgedNotices = false
			UsernamePasswordAuthorityAuthenticationToken authentication = (UsernamePasswordAuthorityAuthenticationToken) auth

			UserLogin userLogin = securityService.userLogin
			auditService.saveUserAudit UserAuditBuilder.login()

			List<Project> alternativeProjects = null
			Project project = securityService.userCurrentProject
			if (!project) {
				List<Project> accessibleProjects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE, null, userLogin)
				switch (accessibleProjects.size()) {
					// If they have access to no project, add the error and return.
					case 0:
						responseWriter.print(JsonUtil.toJson([error: "No project available for the given user."]))
						responseWriter.flush()
						return
					// If there's exactly one project, automatically set that project as the current one.
					case 1:
						project = alternativeProjects[0]
						userPreferenceService.setPreference(userLogin, UserPreferenceEnum.CURR_PROJ, project.id)
						break
					// If more than project is available, the user will have to select one.
					default:
						alternativeProjects = accessibleProjects
				}
			}


			// This map will contain all the user-related data that needs to be sent in the response's payload.
			Map signInInfoMap = [
				userContext: userService.getUserContext(alternativeProjects).toMap()
			]

			if (securityService.shouldLockoutAccount(userLogin)) {
				// lock account
				userService.lockoutAccountByInactivityPeriod(userLogin)
				setAccountLockedOutAttribute(request)

				signInInfoMap.notices = [
					redirectUrl: '/module/auth/login'
				]
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
				} else if (userLogin.forcePasswordChange == 'Y') {
					redirectUri = "/module/auth/changePassword"
				} else {
					redirectUri = authentication.savedUrlForwardURI ?: authentication.targetUri ?: redirectToPrefPage(project)
				}

				// check if user has unacknowledged notices, if so, redirect user to notices page only if the user has a selected project.
				if (project) {
					hasUnacknowledgedNotices = noticeService.hasUnacknowledgedNoticesForLogin(project, request.getSession(), userLogin.person)
					if (hasUnacknowledgedNotices) {
						addAttributeToSession(request, SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES, true)
					}
				}

				addAttributeToSession(request, SecurityUtil.REDIRECT_URI, redirectUri)

				removeAttributeFromSession(request, TdsHttpSessionRequestCache.SESSION_EXPIRED)
				removeAttributeFromSession(request, SecurityUtil.ACCOUNT_LOCKED_OUT)

				signInInfoMap.notices = [
					noticesList: noticeService.fetchPersonPostLoginNotices(securityService.loadCurrentPerson()),
					redirectUrl: redirectUri
				]
			}


			responseWriter.print(JsonUtil.toJson(signInInfoMap))
			responseWriter.flush()

		} finally {
			// always remove the saved request
			requestCache.removeRequest request, response
		}
	}

	private String redirectToPrefPage(Project project) {
		String startPage = userPreferenceService.getPreference(PREF.START_PAGE)

		if (startPage == StartPageEnum.PROJECT_SETTINGS.value) {
			return '/projectUtil'
		}
		if (startPage == StartPageEnum.CURRENT_DASHBOARD.value || startPage == StartPageEnum.PLANNING_DASHBOARD.value) {
			return securityService.hasPermission('BundleView') ? '/module/planning/dashboard' : '/projectUtil'
		}
		if (startPage == StartPageEnum.ADMIN_PORTAL.value) {
			return '/admin/home'
		}

		if (startPage == StartPageEnum.USER_DASHBOARD.value || startPage == null) {
			return '/module/user/dashboard'
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

	/**
	 * Accessing the Service this way because the typical injection doesn't work (the service is null).
	 *
	 * @return
	 */
	ProjectService getProjectService() {
		return ApplicationContextHolder.getBean('projectService', ProjectService)
	}

}
