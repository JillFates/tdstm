/*
 * MoveTech Login 
 */
import org.jsecurity.authc.AuthenticationException
import org.jsecurity.authc.UsernamePasswordToken
import org.jsecurity.SecurityUtils
import grails.converters.JSON
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
			
        render(view:'home',model:[projectTeam:partyGroupInstance,project:params.project,loc:location, bundle:params.bundle,team:params.team,location:params.location])
	}
    //moveTech login
    def moveTechLogin = {
    	render(view:'login')
			 
    }
    def login = {
        return [ username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri ]
    }
	//sign in for moveTech by reading the barcode as userName
    def signIn = {
        def barcodeText = new ArrayList()
        def moveBundleInstance
        def projectTeamInstance
        def token = new StringTokenizer(params.username, "-")
        //Getting current project instance
        def currProj = getSession().getAttribute( "CURR_PROJ" )
        def projectId = currProj.CURR_PROJ
        def projectInstance = Project.findById( projectId )
        while (token.hasMoreTokens()) {
            barcodeText.add(token.nextToken())
        }
        //checking for valid barcode format or not size is 4 (mt-moveid- teamid- s/t)
        if ( barcodeText.size() == 4) {
        	if( new Date() > projectInstance.startDate && new Date() < projectInstance.completionDate ) {
        		try {
        			moveBundleInstance = MoveBundle.findById(Integer.parseInt(barcodeText.get(1)))
        			projectTeamInstance = ProjectTeam.findById(Integer.parseInt(barcodeText.get(2)))
        		}
        		catch (Exception ex) {
        			flash.message = message(code :"Login Failed")
        			redirect(action: 'login')
        		}
        		//checkin for movebundle and team instances
        		if( moveBundleInstance != null && projectTeamInstance != null ){
        			def assetEntityInstance
        			if ( barcodeText.get(3) == 's') {
        				assetEntityInstance = AssetEntity.find("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id and ae.sourceTeam = $projectTeamInstance.id")
        			}else if( barcodeText.get(3) == 't' ){
        				assetEntityInstance = AssetEntity.find("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id and ae.targetTeam = $projectTeamInstance.id")
                    }
        			//checking for team corresponding to moveBundle exist or not
        			if( assetEntityInstance != null) {
        				def moveTech = [ user: barcodeText.get(0) ]
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
	// Method for my task link
	def assetTask = {
    		
        def bundle = params.bundle
        def bundleId = MoveBundle.find("from MoveBundle mb where mb.name = '${bundle}'")
        def team = params.team
        def taskList = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = ${bundleId.id} and ae.sourceTeam = ${team} and (ae.sourceLocation = 'XX-232-YAB' or ae.targetLocation = 'XX-232-YAB')")
        return[taskList:taskList,bundle:bundle,team:team,project:params.project,location:params.location]
	}
	//To open div for my task
	def getServerInfo = {
			
        def assetId = params.assetId
        def assetItem = AssetEntity.findById(assetId)
        render assetItem as JSON
	}
	
}
