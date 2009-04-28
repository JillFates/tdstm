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
    def stateEngineService
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
                            redirect(controller:'moveTech',params:moveTech)
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

    // method for home page
    def home = {
    		
        render( view:'home' )
    }
	// Method for my task link
	def assetTask = {
			
        def bundle = params.bundle
        def stateAssetList = []
        def bundleId = MoveBundle.find("from MoveBundle mb where mb.name = '${bundle}'")
        def team = params.team            
        def projectId = bundleId.project.id        
        def projectInstance = Project.findById(projectId)
        def taskList
        if(params.location == "s"){
            taskList = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = ${bundleId.id} and ae.sourceTeam = ${team}")
        }else{
            taskList = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = ${bundleId.id} and ae.targetTeam = ${team}")
        }
        def taskBuffer = new StringBuffer()
        def taskSize = taskList.size()
        for ( int k=0; k< taskSize ; k++ ) {
            if( k != taskSize - 1) {
                taskBuffer.append( taskList[k].id + "," )
            } else {
                taskBuffer.append( taskList[k].id )
            }
        }
        
        def proAssetMap = ProjectAssetMap.findAll("from ProjectAssetMap pam where pam.asset in (${taskBuffer}) and pam.project = ${projectInstance.id}")
   
        proAssetMap.each{
   
            def stateVal = stateEngineService.getState("STD_PROCESS",it.currentStateId)
            stateAssetList << [assetVal:it,stateVal:stateVal]
      
        }
       
        return[bundle:bundle,team:team,project:params.project,location:params.location,stateAssetList:stateAssetList]
	}
	//To open div for my task
	def getServerInfo = {
			
        def assetId = params.assetId        
        def assetItem = AssetEntity.findById(assetId)
        render assetItem as JSON
	}
	
	def assetSearch = {
			
        def assetItem
        def assetCommt
        def projMap
        def team = params.team
        def assetId = params.assetId
        def stateVal
        if(assetId != null){
			assetItem = AssetEntity.findByAssetTag(assetId)					
			
			
			if(assetItem == null){			
				flash.message = message(code :"Asset Tag number '${assetId}' was not located")
			}else{
				def bundleName = assetItem.moveBundle.name
				def teamId = (assetItem.sourceTeam.id).toString()				
				def teamName = assetItem.sourceTeam.name
			
                if(bundleName != params.bundle){
                    flash.message = message(code :"The asset [${assetItem.assetName}] is not part of move bundle [${params.bundle}]")
                }else if(teamId != params.team){
                    flash.message = message(code :"The asset [${assetItem.assetName}] is assigned to team [${teamName}]")
			    
                }else{
                    projMap = ProjectAssetMap.findByAsset(assetItem)
                    stateVal = stateEngineService.getState("STD_PROCESS",projMap.currentStateId)
                    assetCommt = AssetComment.findAllByAssetEntity(assetItem)
				
                }
			}
        }
        return[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location]

	}
	
}
