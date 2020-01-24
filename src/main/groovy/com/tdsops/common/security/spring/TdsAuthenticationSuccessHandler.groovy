package com.tdsops.common.security.spring

import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.SecurityUtil
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.StartPageEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.JsonUtil
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler
import groovy.transform.CompileStatic
import net.transitionmanager.notice.NoticeService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.person.UserService
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.AuditService
import net.transitionmanager.security.Permission
import net.transitionmanager.security.SecurityService
import net.transitionmanager.security.UserLogin
import net.transitionmanager.session.SessionContext
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.core.Authentication
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import org.springframework.util.Assert

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@CompileStatic
class TdsAuthenticationSuccessHandler extends AjaxAwareAuthenticationSuccessHandler implements InitializingBean {

	AuditService                   auditService
	GrailsApplication              grailsApplication
	SecurityService                securityService
	UserPreferenceService          userPreferenceService
	UserService                    userService
	NoticeService                  noticeService
	HttpSessionCsrfTokenRepository csrfTokenRepository

	// This is a list of the legacy pages that we can allow the user to redirect to after login. Spring Security
	// records all page requests when the user is not logged in, including Ajax calls. Therefore we need this list
	// to know what are acceptable to route to post login. As we refactor the pages to Angular they should be removed
	// from this list.
	static final List<String> LEGACY_PAGE_LIST = [
			'/admin/exportAccounts',
			'/admin/home',
			'/admin/importAccounts',
			'/assetEntity/architectureViewer',
			'/assetEntity/assetImport',
			'/assetEntity/assetOptions',
			'/assetEntity/importTask',
			'/cookbook/index',
			'/dataTransferBatch/list',
			'/manufacturer/list',
			'/model/importExport',
			'/model/list',
			'/moveBundle/dependencyConsole',
			'/moveEvent/exportRunbook',
			'/newsEditor/newsEditorList',
			'/partyGroup/list',
			'/permissions/show',
			'/person/list',
			'/person/manageProjectStaff',
			'/project/userActivationEmailsForm',
			'/rackLayouts/create',
			'/room/list',
			'/task/listUserTasks',
			'/task/taskGraph',
			'/task/taskTimeline',
			'/userLogin/list'
	]

	@Transactional
	void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
			throws ServletException, IOException {

		Assert.isInstanceOf(UsernamePasswordAuthorityAuthenticationToken, auth,
				'This workflow expects an UsernamePasswordAuthorityAuthenticationToken instance here')

		try {
			CsrfToken token = csrfTokenRepository.generateToken(request)
			csrfTokenRepository.saveToken(token, request, response)

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
				List<Project> accessibleProjects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ANY, null, userLogin)
				switch (accessibleProjects.size()) {
					// If they have access to no project, add the error and return.
					case 0:
						responseWriter.print(JsonUtil.toJson([error: 'There are currently no active projects to choose from.  Please contact your administrator for assistance.']))
						responseWriter.flush()
						return
					// If there's exactly one project, automatically set that project as the current one.
					case 1:
						project = accessibleProjects[0]
						userPreferenceService.setPreference(userLogin, UserPreferenceEnum.CURR_PROJ, project.id)
						break
					// If more than project is available, the user will have to select one.
					default:
						alternativeProjects = accessibleProjects
				}
			}

			// This map will contain all the user-related data that needs to be sent in the response's payload.
			Map signInInfoMap = [
				userContext: userService.getUserContext(alternativeProjects).toMap(),
				csrf       : [
					tokenHeaderName: token.headerName,
					token          : token.token
				],
				notices    : [:]
			]

			if (securityService.shouldLockoutAccount(userLogin)) {
				// Lockout the user account
				userService.lockoutAccountByInactivityPeriod(userLogin)
				setAccountLockedOutAttribute(request)

				String redirectUrl = grailsApplication.config.getProperty('grails.plugin.springsecurity.auth.loginFormUrl', String)
				signInInfoMap.notices = [
					redirectUrl: redirectUrl
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
					// The following will attempt to get the URL to redirect the user to from the /ws/user/lastPageUpdate
					// endpoint. If one was not set then we'll set the user's preferred page.
					redirectUri = SessionContext.getLastPageRequested(request.getSession())
					if (! redirectUri) {
						String springLastUriRequest = ((DefaultSavedRequest)request.getSession().getAttribute('SPRING_SECURITY_SAVED_REQUEST'))?.servletPath
						// Check if the URL recorded by Spring matches those we know to be legacy web pages
						if ( springLastUriRequest && LEGACY_PAGE_LIST.find { it.startsWith(springLastUriRequest) }) {
							redirectUri = '/tdstm' + springLastUriRequest
						} else {
							redirectUri = redirectToPrefPage(project)
						}
					}
				}

				// check if user has unacknowledged notices, if so, redirect user to notices page only if the user has a selected project.
				if (project) {
					hasUnacknowledgedNotices = noticeService.hasUnacknowledgedNoticesForLogin(project, request.getSession(), userLogin.person)
					if (hasUnacknowledgedNotices) {
						addAttributeToSession(request, SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES, true)
					}
					signInInfoMap.notices.noticesList = noticeService.fetchPersonPostLoginNotices(securityService.loadCurrentPerson())
				}

				addAttributeToSession(request, SecurityUtil.REDIRECT_URI, redirectUri)

				removeAttributeFromSession(request, TdsHttpSessionRequestCache.SESSION_EXPIRED)
				removeAttributeFromSession(request, SecurityUtil.ACCOUNT_LOCKED_OUT)

				signInInfoMap.notices.redirectUrl = redirectUri

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
			return '/module/project/list'
		}
		if (startPage == StartPageEnum.CURRENT_DASHBOARD.value || startPage == StartPageEnum.PLANNING_DASHBOARD.value) {
			return securityService.hasPermission('BundleView') ? '/module/planning/dashboard' : '/module/project/list'
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
