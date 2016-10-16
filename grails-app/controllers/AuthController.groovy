import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.shiro.MissingCredentialsException
import com.tdsops.tm.enums.domain.EmailDispatchOrigin
import com.tdsops.tm.enums.domain.PasswordResetType
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import net.transitionmanager.PasswordReset
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.LockedAccountException
import org.apache.shiro.authc.UsernamePasswordToken
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF

class AuthController {

	def shiroSecurityManager

	def auditService
	def controllerService
	def environmentService
	def emailDispatchService
	def securityService
	def userPreferenceService
	def userService
	def NoticeService noticeService
	def Notice notice

	/**
	 * Here whe choose if there's a user logIn we redirect to the preferedPage and to the login otherwise
	 */
	def index() {
		def principal = SecurityUtils.subject?.principal
		if (principal) {
			redirectToPrefPage()
		}else{
			redirect(action:'login', params:params)
		}
	}

	def login() {
		// Get the various security setup settings
		// Adding the X-Login-URL header so that we can catch it in Ajax calls
		def url = HtmlUtil.createLink([controller:'auth', action:'login', absolute:true])
		response.setHeader('X-Login-URL', url)

		def s = securityService.getLoginConfig()
		def buildInfo = environmentService.getVersionText()
		// Fetch the current list Messages where no params means find all of them
		def list = noticeService.fetch();

		return [
			username:params.username,
			authority:params.authority,
			rememberMe:(params.rememberMe != null),
			loginConfig:securityService.getLoginConfig(),
			buildInfo:buildInfo,
			noticeList: list,
			noticeType: Notice.NoticeType
		]
	}

	def signIn() {
		// helper closure used a few times
		def loginMap = {
			// Keep the username and "remember me" setting so that the
			// user doesn't have to enter them again.
			def m = [ username: params.username ]
			if (params.rememberMe) {
				m['rememberMe'] = true
			}

			// Remember the target URI too.
			if (params.targetUri) {
				m['targetUri'] = params.targetUri
			}

			// Remember the authority that the user selected
			if (params.authority) {
				m.authority = params.authority
			}

			// Remember the username that the user entered
			if (params.username) {
				m.username = params.username
			}

			return m
		}

		String failureMsg = ''
		String userMsg
		try {
			if (! params.username || ! params.password) {
				throw new AuthenticationException("Missing user credentials")
			}

			def authToken = new UsernamePasswordToken(params.username, params.password, params.authority)
			// Support for "remember me"
			if (params.rememberMe) {
				authToken.rememberMe = true
			}

			try {
				// Perform the actual login. An AuthenticationException will be thrown if the username is unrecognised or the password is incorrect
				if (log.isDebugEnabled())
					log.debug "signIn: About to call SecurityUtils.subject.login(authToken) : $authToken"
				SecurityUtils.subject.login(authToken)

				if (log.isDebugEnabled())
					log.debug "signIn: About to call securityService.getUserLogin()"

				def userLogin = securityService.getUserLogin()
				if (! userLogin) {
					log.error "signIn() : unable to locate UserLogin for ${params.username}"
					throw new AuthenticationException('Unable to locate user account')
				}

				// If the user is no longer active, redirect them to the login page and display a message
				if ( ! userLogin.userActive() ) {
					log.info "User ${params.username} attempted to login but account is inactive"
					SecurityUtils.subject.logout()
					flash.message = message(code: 'userLogin.accountDisabled.message')
					redirect(action: 'login', params:loginMap())
					return
				} else {
					// If a controller redirected to this page, redirect back
					// to it. Otherwise redirect to the root URI.

					auditService.saveUserAudit( UserAuditBuilder.login(userLogin))

					def targetUri = params.targetUri ?: "/"

					userPreferenceService.loadPreferences(PREF.CURR_PROJ)
					userPreferenceService.loadPreferences(PREF.CURR_BUNDLE)
					userPreferenceService.loadPreferences(PREF.MOVE_EVENT)
					userPreferenceService.loadPreferences(TimeUtil.TIMEZONE_ATTR)
					userPreferenceService.loadPreferences(TimeUtil.DATE_TIME_FORMAT_ATTR)

					// If the user don't have a time zone selected initialize it with GMT
					if (session.getAttribute( TimeUtil.TIMEZONE_ATTR ).CURR_TZ == null) {
						userPreferenceService.setPreference( TimeUtil.TIMEZONE_ATTR, TimeUtil.defaultTimeZone )
						userPreferenceService.loadPreferences(TimeUtil.TIMEZONE_ATTR)
					}

					// If the user don't have a date format selected initialize it
					if (session.getAttribute( TimeUtil.DATE_TIME_FORMAT_ATTR ).CURR_DT_FORMAT == null) {
						userPreferenceService.setPreference( TimeUtil.DATE_TIME_FORMAT_ATTR, TimeUtil.getDefaultFormatType() )
						userPreferenceService.loadPreferences(TimeUtil.DATE_TIME_FORMAT_ATTR)
					}

					userService.updateLastLogin( params.username.toString(), session)

					def browserTestiPad = request.getHeader("User-Agent").toLowerCase().contains("ipad")
					def browserTest = request.getHeader("User-Agent").toLowerCase().contains("mobile")

					if (browserTest) {
						if (browserTestiPad) {
							redirect(controller:'projectUtil')
						} else {
							redirect(controller:'task', action:'listUserTasks', params:[viewMode:'mobile'])
						}
					} else {
						targetUri = session.savedUrlForwardURI
						log.info "SESSION targetUri: " + targetUri
						if (targetUri != null) {
							redirect(uri: targetUri)
						} else {
							redirectToPrefPage()
						}
					}
					log.info "User '${params.username}' has signed in"
					return
				}
			} catch (LockedAccountException e) {
				failureMsg = e.getMessage()
				userMsg = e.getMessage()
			} catch (DisabledAccountException e) {
				failureMsg = e.getMessage()
				userMsg = e.getMessage()
			} catch (MissingCredentialsException e) {
				failureMsg = e.getMessage()
				userMsg = 'Username and password are required'
			} catch (AuthenticationException e) {
				failureMsg = e.getMessage()
			}

		} catch(org.apache.shiro.authc.UnknownAccountException e) {
			failureMsg = e.getMessage()
		} catch(Exception e) {
			failureMsg = e.getMessage()
			log.error "Unexpected Authentication Exception for user '${params.username}' \n${ExceptionUtil.stackTraceToString(e)}"
		}
		if (userMsg) {
			flash.message = userMsg
		} else {
			flash.message = message(code: 'login.failed')
		}

		auditService.logMessage("${params.username} login attempt failed - $failureMsg")

		def remoteIp = HtmlUtil.getRemoteIp()
		auditService.logMessage("${params.username} ($remoteIp) login attempt failed")

		// Now redirect back to the login page.
		redirect(action: 'login', params: loginMap())
	}

	private def redirectToPrefPage() {
		def startPage = userPreferenceService.getPreference(PREF.START_PAGE)

		if (userPreferenceService.getPreference(PREF.CURR_PROJ)) {
			if (startPage =='Project Settings') {
				redirect(uri:'/projectUtil')
			} else if(startPage=='Current Dashboard') {
				if (RolePermissions.hasPermission('MoveBundleShowView')) {
					redirect(uri:'/moveBundle/planningStats')
				} else {
					redirect(uri:'/projectUtil')
				}
			} else if(startPage=='Admin Portal') {
				redirect(uri:'/admin/home')
			} else if(startPage=='User Dashboard' || startPage == null) {
				redirect(uri:'/dashboard/userPortal')
			} else {
				redirect(uri:'/projectUtil')
			}
		} else if(startPage=='User Dashboard' || startPage == null) {
			redirect(uri:'/dashboard/userPortal')
		} else {
			redirect(uri:'/projectUtil')
		}
	}

	def signOut() {
		// Log the user out of the application
		UserLogin userLogin = securityService.getUserLogin()

		if (userLogin) {
			auditService.saveUserAudit(UserAuditBuilder.logout(userLogin))
		}
		log.info "User $userLogin just logged out of the application"
		SecurityUtils.subject?.logout()

		// For now, redirect back to the login page.
		redirect(uri: '/')
	}

	def unauthorized() {
		flash.message = 'You do not have permission to access this page.'
		redirectToPrefPage()
	}
	/*
	 *  Action to navigate the admin control home page
	 */
	def home() {
		def dateNow = TimeUtil.nowGMT()
		def timeNow = dateNow.getTime()
		def dateNowSQL = TimeUtil.nowGMTSQLFormat()

		// retrive the list of 20 usernames with the most recent login times
		def recentUsers = UserLogin.findAll("FROM UserLogin ul WHERE ul.lastLogin is not null ORDER BY ul.lastPage DESC",[max:20])

		// retrive the list of events in progress
		def currentLiveEvents = MoveEvent.findAll()
		def moveEventsList = []
		def thirtyDaysInMS = 60 * 24 * 30 * 1000

		currentLiveEvents.each{ moveEvent  ->
			def completion = moveEvent.getEventTimes()?.completion?.getTime()
			if(moveEvent.newsBarMode == "on" || (completion && completion < timeNow && completion + thirtyDaysInMS > timeNow)){
				def query = "FROM MoveEventSnapshot mes WHERE mes.moveEvent = ?  ORDER BY mes.dateCreated DESC"
				def moveEventSnapshot = MoveEventSnapshot.findAll( query , [moveEvent] )[0]
				def status =""
				def dialInd = moveEventSnapshot?.dialIndicator
				if( dialInd && dialInd < 25){
					status = "Red($dialInd)"
				} else if( dialInd && dialInd >= 25 && dialInd < 50){
					status = "Yellow($dialInd)"
				} else if(dialInd){
					status = "Green($dialInd)"
				}
				moveEventsList << [moveEvent : moveEvent, status : status, startTime : moveEvent.getEventTimes()?.start, completionTime : moveEvent.getEventTimes()?.completion]
			}
		}
		// retrive the list of 10 upcoming bundles
		def upcomingBundles = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.startTime > '$dateNowSQL' ORDER BY mb.startTime",[max:10])

		render( view:'home', model:[ recentUsers:recentUsers, moveEventsList:moveEventsList, upcomingBundles:upcomingBundles] )
	}

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
		} catch (ServiceException se) {
			flash.message = controllerService.getExceptionMessage(this, se)
			success = false
		}

		render( view:'_forgotMyPassword', model: [email: params.email, success: success] )
	}

	/**
	 * The 3rd step in the password reset process where the user is prompted for their email address and their new password.
	 */
	def resetPassword() {
		String token = params.token
		boolean validToken = true
		PasswordReset pr

		try {
			pr = securityService.validateToken(token)
		} catch (ServiceException se) {
			auditService.logWarning("User failed to reset password with token '$token' from IP ${request.getRemoteAddr()}. ${se.getMessage()}.")
			flash.message = controllerService.getExceptionMessage(this, se) + '. Please contact support if you require assistances.'
			redirect(action: 'login')
			return
		}

		render( view:'_resetPassword', model: [
			token: pr.token,
			password: params.password,
			email: params.email,
			validToken: validToken,
			username: pr.userLogin.username,
			minPasswordLength: securityService.getUserLocalConfig().minPasswordLength
		] )
	}

	/**
	 * The 4th and final step in the user resetting their password. If successful the user will be logged in and redirected
	 * to their landing page along with a message that their password was changed. If it fails it will return to the
	 * reset password form.
	 */
	def applyNewPassword() {
		def token = params.token
		def password = params.password
		def email = params.email

		try {
			// Change password
			PasswordReset pr = securityService.applyPasswordFromPasswordReset(token, password, email)

			// Send email to notify about the change
			def ed = emailDispatchService.basicEmailDispatchEntity(
				EmailDispatchOrigin.PASSWORD_RESET,
				"Your TransitionManager password has changed",
				"passwordResetNotif",
				[:] as JSON,
				pr.userLogin.person.email,
				pr.userLogin.person.email,
				pr.userLogin.person,
				pr.userLogin.person
			)
			emailDispatchService.createEmailJob(ed, [:])

			// Login and redirect to home page
			params.username = pr.userLogin.username
			flash.message = 'Your new password was successfully changed.'
		 	signIn()

		} catch (Exception se) {
			flash.message = controllerService.getExceptionMessage(this, se) + '. Please contact support if you require assistances.'
			resetPassword()
		}

	}
}
