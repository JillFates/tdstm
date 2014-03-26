import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.crypto.hash.Sha1Hash
import org.apache.shiro.SecurityUtils
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil

class AuthController {
	
	def shiroSecurityManager
	def userPreferenceService
	def securityService

	def index = { redirect(action: 'login', params: params) }

	def login = {
		def redirectURL = session.REDIRECT_URL
		if(!redirectURL){
			def url = HtmlUtil.createLink([controller:'auth', action:'login', absolute:true])
			// Adding the X-Login-URL header so that we can catch it in Ajax calls
			response.setHeader('X-Login-URL', url)
			return [ username: params.username, rememberMe: (params.rememberMe != null)]
		} else {
			redirect( url:redirectURL)
		}
	}

	def signIn = {
		log.warn 'signIn start'
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
			return m
		}
		
		try {
			def authToken = new UsernamePasswordToken(params.username, params.password)
			// Support for "remember me"
			if (params.rememberMe) {
				authToken.rememberMe = true
			}
	
			try {
				// Perform the actual login. An AuthenticationException
				// will be thrown if the username is unrecognised or the
				// password is incorrect.
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
				} else {
					// If a controller redirected to this page, redirect back
					// to it. Otherwise redirect to the root URI.
					def targetUri = params.targetUri ?: "/"
					
					// log.info "Redirecting to '${targetUri}'."
		
					//redirect(uri: targetUri)
					/*
					 *  call loadPreferences() to load CURR_PROJ MAP into session
					 */
					userPreferenceService.loadPreferences("CURR_PROJ")
					userPreferenceService.loadPreferences("CURR_BUNDLE")
					userPreferenceService.loadPreferences("MOVE_EVENT")
					userPreferenceService.loadPreferences("CURR_TZ")
					/*
					 *  call userPreferenceService.updateLastLogin( params.username ) to update the last login time
					 */
					userPreferenceService.updateLastLogin( params.username )
					def browserTestiPad = request.getHeader("User-Agent").toLowerCase().contains("ipad")
					def browserTest = request.getHeader("User-Agent").toLowerCase().contains("mobile")
					
					Person.loggedInPerson = securityService.getUserLoginPerson();
					
					if(browserTest) {
						if(browserTestiPad) {
							redirect(controller:'projectUtil')
						} else {
							redirect(controller:'clientTeams', action:'listTasks', params:[viewMode:'mobile'])
						}
					} else {
					   if(userPreferenceService.getPreference('CURR_PROJ')){
							if(userPreferenceService.getPreference('START_PAGE')=='Project Settings'){
								redirect(controller:'projectUtil')
							}else if(userPreferenceService.getPreference('START_PAGE')=='Current Dashboard'){
								 if(RolePermissions.hasPermission('MoveBundleShowView')){
									  redirect(controller:'moveBundle',action:'planningStats')
								 }else{
									   redirect(controller:'projectUtil')
								 }
							}else if(userPreferenceService.getPreference('START_PAGE')=='Admin Portal'){
								redirect(action:'home')
							}else{
								redirect(controller:'projectUtil')
							}
					   }else{
							 redirect(controller:'projectUtil')
					   }
					}
					log.info "User '${params.username}' has signed in"
				}
			}
			catch (AuthenticationException ex){
				// Authentication failed, so display the appropriate message
				// on the login page.
				def remoteIp = HtmlUtil.getRemoteIp()
				log.warn "1 Authentication failure user '${params.username}', IP ${remoteIp} : ${ex.getMessage()}"
				flash.message = message(code: "login.failed")
				
				// Now redirect back to the login page.
				redirect(action: 'login', params: loginMap())
			}
	 	} catch(Exception e) {
			def remoteIp = HtmlUtil.getRemoteIp()
			log.warn "2 Authentication failure for user '${params.username}' : IP ${remoteIp} : ${e.printStackTrace()}"
			flash.message = "Authentication failure for user '${params.username}'."
			
			// Now redirect back to the login page.
			redirect(action: 'login', params: loginMap())
		}
	}

	def signOut = {
		// Log the user out of the application.
		SecurityUtils.subject?.logout()

		// For now, redirect back to the login page.
		redirect(uri: '/')
	}

	def unauthorized = {
		flash.message = 'You do not have permission to access this page.'
		render( view:'home' )
	}
	/*
	 *  Action to navigate the admin control home page
	 */
	def home = {

		def dateNow = TimeUtil.nowGMT()
		def timeNow = dateNow.getTime()
		
		// retrive the list of 20 usernames with the most recent login times
		def recentUsers = UserLogin.findAll("FROM UserLogin ul WHERE ul.lastLogin is not null ORDER BY ul.lastLogin DESC",[max:20])

		// retrive the list of events in progress
		def currentLiveEvents = MoveEvent.findAll()
		def moveEventsList = []
		def thirtyDaysInMS = 2592000000
		
		currentLiveEvents.each{ moveEvent  ->
			def completion = moveEvent.getEventTimes()?.completion?.getTime()
			if(moveEvent.inProgress == "true" || (completion && completion < timeNow && completion + thirtyDaysInMS > timeNow)){
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
		def upcomingBundles = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.startTime > '$dateNow' ORDER BY mb.startTime",[max:10])
		
		render( view:'home', model:[ recentUsers:recentUsers, moveEventsList:moveEventsList, moveBundlesList:upcomingBundles ] )
	}
	
	def maintMode = {
		//Do nothing
	}
}
