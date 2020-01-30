package net.transitionmanager.admin

import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.security.spring.TdsHttpSessionRequestCache
import com.tdsops.tm.enums.domain.EmailDispatchOrigin
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdsops.tm.enums.domain.StartPageEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.common.ControllerService
import net.transitionmanager.common.EmailDispatch
import net.transitionmanager.common.EmailDispatchService
import net.transitionmanager.common.EnvironmentService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.ServiceException
import net.transitionmanager.notice.NoticeService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.person.UserService
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.security.AuditService
import net.transitionmanager.security.PasswordReset
import net.transitionmanager.security.Permission
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

@Secured('permitAll')
class AuthController implements ControllerMethods {

	private static final int thirtyDaysInMS = 60 * 24 * 30 * 1000

	AuditService auditService
	ControllerService controllerService
	EmailDispatchService emailDispatchService
	EnvironmentService environmentService
	NoticeService noticeService
	SpringSecurityService springSecurityService
	UserPreferenceService userPreferenceService
	UserService userService
	MoveEventService moveEventService

	// SpringSecurity logout handlers that will get injected by Spring and is used below in the signOut method
	def logoutHandlers

	def index() {
		if (springSecurityService.loggedIn) {
			redirectToPrefPage()
		} else {
			redirect(uri: securityService.loginUrl())
		}
	}

	/**
	 * This is the legacy login method. If the user is logged already then it will redirect the user to their
	 * preferred page otherwise will redirect them to the new login form in Angular
	 * @return
	 */
	def login() {
		index()
	}

	private void redirectToPrefPage() {
		String uri
		String startPage = userPreferenceService.getPreference(PREF.START_PAGE)
		if (userPreferenceService.currentProjectId) {
			if (startPage == StartPageEnum.PROJECT_SETTINGS.value) {
				uri = '/projectUtil'
			}
			else if (startPage == StartPageEnum.CURRENT_DASHBOARD.value) {
				if (securityService.hasPermission(Permission.BundleView)) {
					uri = '/moveBundle/planningStats'
				}
				else {
					uri = '/projectUtil'
				}
			}
			else if (startPage == StartPageEnum.ADMIN_PORTAL.value) {
				uri = '/admin/home'
			}
			else if (startPage == StartPageEnum.USER_DASHBOARD.value || startPage == null) {
				uri = '/module/user/dashboard'
			}
			else {
				uri = '/projectUtil'
			}
		}
		else if (startPage == StartPageEnum.USER_DASHBOARD.value || startPage == null) {
			uri = '/module/user/dashboard'
		}
		else {
			uri = '/projectUtil'
		}

		redirect(uri: uri)
	}

	/**
	 * This is the authorization logout method that will terminate the user's application session and records
	 * in the audit log that the user signed out. It will redirect the user to the login form at the end.
	 * @return
	 */
	def signOut() {
		// TODO : JPM 10/2019 : Move this logic into the SecurityService
		if (securityService.loggedIn) {
			String username = securityService.currentUsername
			auditService.saveUserAudit(UserAuditBuilder.logout())
			log.info 'User {} just logged out of the application', username
		}

		// Force the logout of the session by directly interacting with SpringSecurity
		Authentication auth = SecurityContextHolder.context.authentication
		if (auth) {
			logoutHandlers.each  { handler ->
				handler.logout(request,response,auth)
			}
		}

		if (isAjaxRequest()) {
			renderSuccessJson()
		} else {
			// Redirect the user to the system configured login form
			// Note - Some reason the url with /tdstm in it gets duplicated with /tdstm/tdstm/...
			redirect uri: grailsApplication.config.getProperty('grails.plugin.springsecurity.auth.loginFormUrl', String)
		}
	}

	/**
	 * Controller method used to handle any unauthorized requests
	 * @return
	 */
	def unauthorized() {
		// TODO : JPM 10/2019 : Should log to the audit log that a user tried to access something without authorization
		forward controller: 'errorHandler', action:'unauthorized'
		// flash.message = 'You do not have permission to access this page.'
		// redirectToPrefPage()
	}

	// TODO: This should be deleted
	/**
	 * This was originally meant to be used to determine if the application was in a maint mode where it was running
	 * but that end users wouldn't have access. Not sure what the status is of this method as of now JPM 10/2019.
	 */
	def maintMode() {
		//Do nothing
	}

	/**
	 * The 1st step in the Forgot My Password process
	 */
	def forgotMyPassword() {
		render( view:'_forgotMyPassword', model: [email: params.email ] )
	}

	/**
	 * The 2nd step in the password reset process.
	 * This will send the email notice to the user and then redirect them to the login form with a flash message
	 * explaining the next step.
	 */
	@Deprecated
	def sendResetPassword() {
		def email = params.email
		def success = true
		try {
			securityService.sendResetPasswordEmail(email, request.getRemoteAddr(), PasswordResetType.FORGOT_MY_PASSWORD)
		}
		catch (ServiceException e) {
			flash.message = controllerService.getExceptionMessage(this, e)
			success = false
		}

		render( view:'_forgotMyPassword', model: [email: params.email, success: success] )
	}

	/**
	 * Send the reset password email notification for the given email address.
	 * @return whether or not the process was successful.
	 */
	def sendResetPasswordEmail() {
		String email = params.email
		boolean success = true
		try {
			securityService.sendResetPasswordEmail(email, request.getRemoteAddr(), PasswordResetType.FORGOT_MY_PASSWORD)
		} catch (ServiceException e) {
			success = false
		}
		renderSuccessJson([success: success])
	}

	/**
	 * The 3rd step in the password reset process where the user is prompted for their email address and their new password.
	 */
	@Secured('! isAuthenticated()')
	def resetPassword() {
		String token = params.token
		PasswordReset pr

		try {
			pr = securityService.validateToken(token)
		}
		catch (ServiceException e) {
			auditService.logWarning("User failed to reset password with token '$token' from IP ${request.remoteAddr}. $e.message")
			flash.message = controllerService.getExceptionMessage(this, e) + '. Please contact support if you require assistances.'
			redirect(action: 'login')
			return
		}

		render(view: '_resetPassword',
				model: [token: pr.token, password: params.password, email: params.email,
				        minPasswordLength: securityService.getUserLocalConfig().minPasswordLength,
				        validToken: true, username: pr.userLogin.username])
	}

	/**
	 * The 4th and final step in the user resetting their password. If successful the user will be logged in and redirected
	 * to their landing page along with a message that their password was changed. If it fails it will return to the
	 * reset password form.
	 */
	@Secured('! isAuthenticated()')
	def applyNewPassword() {
		try {
			PasswordReset pr = securityService.applyPasswordFromPasswordReset(params.token, params.password, params.confirmPassword, params.email)

			EmailDispatch ed = emailDispatchService.basicEmailDispatchEntity(
				EmailDispatchOrigin.PASSWORD_RESET,
				'Your TransitionManager password has changed',
				'passwordResetNotif',
				[:] as JSON,
				null,
				pr.userLogin.person.email,
				pr.userLogin.person,
				pr.userLogin.person
			)
			emailDispatchService.createEmailJob(ed, [username: pr.userLogin.username])

			// Login and redirect to home page
			params.username = pr.userLogin.username
			flash.message = 'Your password was changed successfully.'
			signIn()
		}
		catch (e) {
			String exceptionMessage = controllerService.getExceptionMessage(this, e)
			flash.message = exceptionMessage + '. Please contact support if you require assistance.'
			log.info("Error applying new password: ${exceptionMessage}")
			resetPassword()
		}
	}

	/**
	 * Fetch the info necessary for the login page
	 * @return build version, pre login notices and session expired and account locked out flags.
	 */
	def getLoginInfo() {
		renderSuccessJson([
			buildVersion: environmentService.getVersionText(),
			config: securityService.getConfigForLogin(),
			notices: noticeService.getPreLoginNotices(),
			sessionExpired: session.getAttribute(TdsHttpSessionRequestCache.SESSION_EXPIRED),
			accountLockedOut: session.getAttribute(SecurityUtil.ACCOUNT_LOCKED_OUT)
		])
	}
}
