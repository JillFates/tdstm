import org.jsecurity.authc.AuthenticationException
import org.jsecurity.authc.UsernamePasswordToken
import org.jsecurity.SecurityUtils
class MoveTechController {
	def jsecSecurityManager
    def userPreferenceService
    
    def index = {
        def partyGroupInstance = PartyGroup.findById(params.team)
        def team = partyGroupInstance.name
        def location =""
        if( params.location == 's') {
            location = "Unracking"
        }else if( params.location == 't'){
            location = "Reracking"
        }
			
        render(view:'home',model:[projectTeam:partyGroupInstance,project:params.project,location:location])
	}
    //moveTech login
    def moveTechLogin = {
    	render(view:'login')
			 
    }
    def login = {
        return [ username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri ]
    }

    def signIn = {
        def barcodeText = new ArrayList()
        def moveBundleInstance
        def projectTeamInstance
        def token = new StringTokenizer(params.username, "-")
        def currProj = getSession().getAttribute( "CURR_PROJ" )
        def projectId = currProj.CURR_PROJ
        def projectInstance = Project.findById( projectId )
        while (token.hasMoreTokens()) {
            barcodeText.add(token.nextToken())
        }
        if ( barcodeText.size() == 4) {
            def userInstance = UserLogin.findByUsername(barcodeText.get(0))
            if( userInstance != null && userInstance.active == 'Y') {
                if( new Date() > projectInstance.startDate && new Date() < projectInstance.completionDate ) {
                    moveBundleInstance = MoveBundle.findById(Integer.parseInt(barcodeText.get(1)))
                    projectTeamInstance = ProjectTeam.findById(Integer.parseInt(barcodeText.get(2)))
                    if( moveBundleInstance != null && projectTeamInstance != null ){
                        def moveTech = [ user: userInstance ]
                        moveTech['bundle'] = moveBundleInstance
                        moveTech['team'] = Integer.parseInt(barcodeText.get(2))
                        moveTech['location'] = barcodeText.get(3)
                        moveTech['project'] = projectInstance
                        def authToken = new UsernamePasswordToken(barcodeText.get(0), 'xyzzy')

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
                                userPreferenceService.loadPreferences()
                                redirect(controller:'moveTech',params:moveTech)
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
                    }else {
                        flash.message = message(code :"Login Failed")
                        redirect(action: 'login')
                    }
                }else {
                    flash.message = message(code :"Login Disabled")
                    redirect(action: 'login')
                }
            }else {
                flash.message = message(code :"Login Failed")
                redirect(action: 'login')
            }
					
        }else {
            flash.message = message(code :"Unable to read barcode")
            redirect(action: 'login')
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
