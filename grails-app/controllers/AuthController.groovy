import org.jsecurity.authc.AuthenticationException
import org.jsecurity.authc.UsernamePasswordToken
import org.jsecurity.SecurityUtils

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
				/*
				 *  call userPreferenceService.updateLastLogin( params.username ) to update the last login time
				 */
				userPreferenceService.updateLastLogin( params.username )
				
	            def browserTest = request.getHeader("User-Agent").contains("IEMobile")
				
	            if(browserTest) {
	            	redirect(controller:'walkThrough')
	            } else {
	            	redirect(controller:'projectUtil')
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
    // method for home page
    def home = {
        render( view:'home' )
    }
}
