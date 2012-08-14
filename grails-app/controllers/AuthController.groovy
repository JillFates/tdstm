import org.jsecurity.authc.AuthenticationException
import org.jsecurity.authc.UsernamePasswordToken
import org.jsecurity.SecurityUtils
import com.tdssrc.grails.GormUtil
import java.text.SimpleDateFormat
class AuthController {
    def jsecSecurityManager
    def userPreferenceService

    def index = { redirect(action: 'login', params: params) }

    def login = {
        /*def browserTest = request.getHeader("User-Agent").contains("IEMobile")
        if(browserTest) {
            redirect(controller:'moveTech', action:'moveTechLogin')
        } else {*/
            return [ username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri ]
        /*}*/
    }

    def signIn = {
    	try {
	        def authToken = new UsernamePasswordToken(params.username, params.password)
	        // Support for "remember me"
	        if (params.rememberMe) {
	            authToken.rememberMe = true
	        }
	
	        try{
	            // Perform the actual login. An AuthenticationException
	            // will be thrown if the username is unrecognised or the
	            // password is incorrect.
	            this.jsecSecurityManager.login(authToken)
	            // Check User and Person Activi status
	            
	            def activeStatus = userPreferenceService.checkActiveStatus()
	            if(!activeStatus){
	                flash.message = "User Authentication has been Disabled"
	                redirect(action: 'login')
	            } else {
		            // If a controller redirected to this page, redirect back
		            // to it. Otherwise redirect to the root URI.
		            def targetUri = params.targetUri ?: "/"
		            
		            log.info "Redirecting to '${targetUri}'."
		            //redirect(uri: targetUri)
		            /*
		             *  call loadPreferences() to load CURR_PROJ MAP into session
		             */
		            userPreferenceService.loadPreferences("CURR_PROJ")
		            userPreferenceService.loadPreferences("CURR_BUNDLE")
		            userPreferenceService.loadPreferences("MOVE_EVENT")
					/*
					 *  call userPreferenceService.updateLastLogin( params.username ) to update the last login time
					 */
					userPreferenceService.updateLastLogin( params.username )
					def browserTestiPad = request.getHeader("User-Agent").toLowerCase().contains("ipad")
					def browserTest = request.getHeader("User-Agent").toLowerCase().contains("mobile")
					
		            if(browserTest) {
						if(browserTestiPad) {
							redirect(controller:'projectUtil')
						} else {
							redirect(controller:'clientTeams', params:[viewMode:'mobile'])
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
	            }
	        }
	        catch (AuthenticationException ex){
	            // Authentication failed, so display the appropriate message
	            // on the login page.
	            log.info "Authentication failure for user '${params.username}'."
	            flash.message = message(code: "login.failed")
	
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
	
	            // Now redirect back to the login page.
	            redirect(action: 'login', params: m)
	        }
    	} catch(Exception e){
			flash.message = "Authentication failure for user '${params.username}'."
			redirect(action: 'login', params: params)
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

    	def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	def dateNow = dateFormat.format(GormUtil.convertInToGMT( "now", "EDT" ))
		def timeNow = GormUtil.convertInToGMT( "now", "EDT" ).getTime()
		// retrive the list of 20 usernames with the most recent login times
    	def recentUsers = UserLogin.findAll("FROM UserLogin ul WHERE ul.lastLogin is not null ORDER BY ul.lastLogin DESC",[max:20])
		// retrive the list of events in progress
		def currentLiveEvents = MoveEvent.findAll()
		def moveEventsList = []
		currentLiveEvents.each{ moveEvent  ->
			def completion = moveEvent.getEventTimes()?.completion?.getTime()
			if(moveEvent.inProgress == "true" || (completion && completion < timeNow && completion + 2592000000 > timeNow)){
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
}
