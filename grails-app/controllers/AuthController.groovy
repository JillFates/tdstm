import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.tm.enums.domain.EmailDispatchOrigin
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdsops.tm.enums.domain.StartPageEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.EmailDispatch
import net.transitionmanager.PasswordReset
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventSnapshot
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AuditService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.EmailDispatchService
import net.transitionmanager.service.EnvironmentService
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.NoticeService
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.UserService

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

	def index() {
		if (springSecurityService.loggedIn) {
			redirectToPrefPage()
		}
		else {
			redirect(action: 'login', params: params)
		}
	}

	def login() {
		// Adding the X-Login-URL header so that we can catch it in Ajax calls
		response.setHeader('X-Login-URL', createLink(controller: 'auth', action: 'login', absolute: true).toString())

		def noticeList = noticeService.fetch();
		def preLoginList = [];
		def postLoginList = [];
		noticeList.each {
			if (it.typeId == Notice.NoticeType.Prelogin && it.active) {
				preLoginList.push(it)
			}

			if (it.typeId == Notice.NoticeType.Postlogin && it.active) {
				postLoginList.push(it)
			}
		}

		[username: params.username, authority: params.authority, rememberMe: params.rememberMe != null,
		 loginConfig: securityService.getLoginConfig(), buildInfo: environmentService.getVersionText(),
		 preLoginList: preLoginList, postLoginList: postLoginList]
	}

	private signIn() {
		// Now redirect back to the login page.
		redirect(action: 'login')
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
				uri = '/dashboard/userPortal'
			}
			else {
				uri = '/projectUtil'
			}
		}
		else if (startPage == StartPageEnum.USER_DASHBOARD.value || startPage == null) {
			uri = '/dashboard/userPortal'
		}
		else {
			uri = '/projectUtil'
		}

		redirect(uri: uri)
	}

	def signOut() {
		// Log the user out of the application

		if (securityService.loggedIn) {
			String username = securityService.currentUsername
			auditService.saveUserAudit(UserAuditBuilder.logout())
			log.info 'User {} just logged out of the application', username
		}

		// redirect to the uri that the Spring Security logout filter is configured for,
		// and it will redirect to '/' when it's done

		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
	}

	def unauthorized() {

		forward controller: 'errorHandler', action:'unauthorized'
		// flash.message = 'You do not have permission to access this page.'
		// redirectToPrefPage()
	}

	/*
	 *  Action to navigate the admin control home page
	 */
	// TODO: This should be deleted
	def home() {
		Date dateNow = TimeUtil.nowGMT()
		long timeNow = dateNow.time
		def dateNowSQL = TimeUtil.nowGMTSQLFormat()

		// retrieve the list of 20 usernames with the most recent login times
		def recentUsers = UserLogin.findAllByLastLoginIsNotNull([sort: 'lastPage', order: 'desc'], [max: 20])

		// retrieve the list of events in progress
		def moveEventsData = []

		for (MoveEvent moveEvent in MoveEvent.list()) {
			Map<String, Date> eventTimes = moveEventService.getEventTimes(moveEvent.id)
			Date eventStart = eventTimes.start
			Date eventCompletion = eventTimes.completion
			Long completion = eventCompletion?.time
			if (moveEvent.newsBarMode == 'on' || (completion && completion < timeNow && completion + thirtyDaysInMS > timeNow)) {
				MoveEventSnapshot snapshot = MoveEventSnapshot.findByMoveEvent(MoveEvent.list()[3], [sort: 'dateCreated', order: 'desc'])
				String status = ''
				Integer dialInd = snapshot?.dialIndicator
				if (dialInd && dialInd < 25) {
					status = "Red($dialInd)"
				}
				else if (dialInd && dialInd >= 25 && dialInd < 50) {
					status = "Yellow($dialInd)"
				}
				else if (dialInd) {
					status = "Green($dialInd)"
				}
				moveEventsData << [moveEvent: moveEvent, status: status, startTime: eventStart, completionTime: eventCompletion]
			}
		}
		// retrieve the list of 10 upcoming bundles
		def upcomingBundles = MoveBundle.findAll("FROM MoveBundle WHERE startTime>'$dateNowSQL' ORDER BY startTime", [max: 10])

		[recentUsers: recentUsers, moveEventsList: moveEventsData, upcomingBundles: upcomingBundles]
	}

	// TODO: This should be deleted
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
	 * The 2nd step in the passord reset process.
	 * This will send the email notice to the user and then redirect them to the login form with a flash message
	 * explaining the next step.
	 */
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
				pr.userLogin.person.email,
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
}
