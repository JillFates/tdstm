/*
 * MoveTech Login 
 */
import org.jsecurity.authc.AuthenticationException
import org.jsecurity.authc.UsernamePasswordToken
import org.jsecurity.SecurityUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.converters.JSON

class MoveTechController {
    def jsecSecurityManager
    def userPreferenceService
    def partyRelationshipService
    def stateEngineService
    def workflowService
    def jdbcTemplate
    def index = {
    	def browserTest=false
    	if(!request.getHeader("User-Agent").contains("MSIE")) {
    		browserTest = true
    	}	
    	if(params.fMess){
    		flash.clear()
    	}
    	def principal = SecurityUtils.subject.principal
    	// Checking user existence
    	if(principal){
	        if(params.user == "mt"){
	        	def projectTeamInstance = ProjectTeam.findById(params.team)
	            def team = projectTeamInstance.name
	            def teamMembers = partyRelationshipService.getTeamMemberNames(params.team)
	            def location =""
	            if( params.location == 's') {
	                location = "Unracking"
	                projectTeamInstance.currentLocation = "Source"
	                projectTeamInstance.save()
	            }else if( params.location == 't'){
	                location = "Reracking"
	                projectTeamInstance.currentLocation = "Target"
	                projectTeamInstance.save()
	            }
						
	            render(view:'home',model:[projectTeam:team,members:teamMembers,project:params.project,loc:location, bundle:params.bundle,team:params.team,location:params.location])
	        } else if(params.user == "ct") {
	        	def projectTeamInstance = ProjectTeam.findById(params.team)
	            def team = projectTeamInstance.name
	            def teamMembers = partyRelationshipService.getTeamMemberNames(params.team)
	            def teamLocation =""
	            if( params.location == 's') {
	            	projectTeamInstance.currentLocation = "Source"
	            	projectTeamInstance.save()
	            	teamLocation = projectTeamInstance.currentLocation
	            }else if( params.location == 't'){
	            	projectTeamInstance.currentLocation = "Target"
	            	projectTeamInstance.save()
	            	teamLocation = projectTeamInstance.currentLocation
	            }
	            render(view:'cleaningTechHome',model:[projectTeam:team,members:teamMembers,project:params.project,loc:teamLocation, bundle:params.bundle,team:params.team,location:params.location,browserTest:browserTest])
					
	        } else {
	        	redirect(action:'login')
	        }
        } else {
        	// Redirect to Login page when session expired
        	flash.message = "Your login has expired and must login again."
        	redirect(action:'login')
        }
    }
    //moveTech login
    def moveTechLogin = {    	
        redirect(action:'login')
    }
    def login = {
    	def validate = true
    	def message = flash.message
    	if(message){
    		if(message.contains("Unknown") || message.contains("Invalid") || message.contains("No assets assigned") ||message.contains("presently inactive")){
    			validate = false
    		}
    	}
    	if(validate){
            def username = session.getAttribute("USERNAME")
            if(username){
                redirect(action:'signIn',params:["username":username])
            } else {
                return [ username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri ]
            }
        } else {
            return [ username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri ]
        }
    }
    //sign in for moveTech by reading the barcode as userName
    def signIn = {
        def moveBundleInstance
        def projectTeamInstance
        if( params.username ) {
        	session.setAttribute("USERNAME",params.username)
            //Getting current project instance
            def projectInstance
            def barcodeText  = params.username.tokenize("-")
            //checking for valid barcode format or not size is 4 (mt-moveid- teamid- s/t)
            if ( barcodeText.size() == 4) {
            	try{
	                if(barcodeText.get(0) == "mt"){
	                	moveBundleInstance = MoveBundle.findById(barcodeText.get(1))
	                    //checkin for movebundle and team instances
	                    if(moveBundleInstance){
	                    	projectInstance = Project.findById( moveBundleInstance.project.id )
	                    	if(projectInstance){
	                    		projectTeamInstance = ProjectTeam.findById(barcodeText.get(2))
	                    		if(projectTeamInstance){
	                                //Validating is Logindate between startdate and completedate
	                                if( new Date() < projectInstance.startDate || new Date() > projectInstance.completionDate ) {
	                                    flash.message = message(code :"Move bundle presently inactive")
	                                    redirect(action: 'login')
	                                    return;
	                                }else {
	                                    def assetEntityInstance
	                                    if ( barcodeText.get(3) == 's') {
	                                        assetEntityInstance = AssetEntity.find("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id and ae.sourceTeam = $projectTeamInstance.id")
	                                    }else if( barcodeText.get(3) == 't' ){
	                                        assetEntityInstance = AssetEntity.find("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id and ae.targetTeam = $projectTeamInstance.id")
	                                    }
	                                    //checking for Assets corresponding to moveBundle exist or not
	                                    if( assetEntityInstance != null) {
	                                        def moveTech = [ user: barcodeText.get(0) ]
	                                        moveTech['bundle'] = moveBundleInstance.id
	                                        moveTech['team'] = Integer.parseInt(barcodeText.get(2))
	                                        moveTech['location'] = barcodeText.get(3)
	                                        moveTech['project'] = projectInstance.name
	                                        checkAuth(barcodeText.get(0), moveTech)
	                                    }else {
	                                        flash.message = message(code :"No assets assigned to team for move bundle")
	                                        redirect(action: 'login')
	                                        return;
	                                    }
	                                }
	                            } else {
	                            	flash.message = message(code :"Unknown move bundle team")
	                                redirect(action: 'login')
	                                return;
	                            }
	                    	}else{
	                    		flash.message = message(code :"Unknown project")
	                            redirect(action: 'login')
	                            return;
	                    	}
	                    } else {
	                    	flash.message = message(code :"Unknown move bundle")
	                        redirect(action: 'login')
	                        return;
	                    }
	                } else if( barcodeText.get(0) == "ct" ) {
	                    moveBundleInstance = MoveBundle.findById(barcodeText.get(1))
	                    if(moveBundleInstance){
	                    	projectInstance = Project.findById( moveBundleInstance.project.id )
	                    	if(projectInstance){
	                    		projectTeamInstance = ProjectTeam.findById(barcodeText.get(2))
	                    		if(projectTeamInstance != null && projectTeamInstance.teamCode == "Cleaning"){
	                                //Validating is Logindate between startdate and completedate
	                                if( new Date() < projectInstance.startDate || new Date() > projectInstance.completionDate ) {
	                                    flash.message = message(code :"Move bundle presently inactive")
	                                    redirect(action: 'login')
	                                    return;
	                                }else {
	                                    def assetEntityInstance
	                                    if ( barcodeText.get(3) == 's' ) {
	                                        assetEntityInstance = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id ")
	                                    }else if( barcodeText.get(3) == 't' ){
	                                        assetEntityInstance = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id ")
	                                    }
	                                    //checking for Assets corresponding to moveBundle exist or not
	                                    if( assetEntityInstance != null) {
	                                        def moveTech = [ user: barcodeText.get(0) ]
	                                        moveTech['bundle'] = moveBundleInstance.id
	                                        moveTech['team'] = Integer.parseInt(barcodeText.get(2))
	                                        moveTech['location'] = barcodeText.get(3)
	                                        moveTech['project'] = projectInstance.name
	                                        checkAuth(barcodeText.get(0), moveTech)
	                                    }else {
	                                        flash.message = message(code :"No assets assigned to team for move bundle")
	                                        redirect(action: 'login')
	                                        return;
	                                    }
	                                }
	                            }else{
	                            	flash.message = message(code :"Unknown Cleaning team")
	                                redirect(action: 'login')
	                                return;
	                            }
	                    	}else{
	                    		flash.message = message(code :"Unknown project")
	                            redirect(action: 'login')
	                            return;
	                    	}
	                    } else {
	                    	flash.message = message(code :"Unknown move bundle")
	                        redirect(action: 'login')
	                        return;
	                    }
	                } else {
	                    flash.message = message(code :"Invalid username")
	                    redirect(action: 'login')
	                    return;
	                }
            	} catch (Exception e) {
            		 flash.message = message(code :"Invalid Login")
	                 redirect(action: 'login')
	                 return;
				}
            }else {
                flash.message = message(code :"Invalid username format")
                redirect(action: 'login')
                return;
            }
        } else {
        	flash.message = message(code :"Invalid username format")
            redirect(action: 'login')
            return;
        }
    }
		
    //check authentication
    def checkAuth(def barcodeText, def actionScreen){
        def authToken = new UsernamePasswordToken(barcodeText, 'xyzzy')
        try{
            // Perform the actual login. An AuthenticationException
            // will be thrown if the username is unrecognised or the
            // password is incorrect.
            this.jsecSecurityManager.login(authToken)
            // Check User and Person Activi status
            if(barcodeText == "ct") {
	            redirect(controller:'moveTech',params:actionScreen)
	            return;
            } else {
            	redirect(controller:'moveTech',action:'moveTechSuccessLogin',params:actionScreen)
            	return;
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
            redirect(action: 'moveTechLogin', params: m)
            return;
        }
    }
	//SignOut
    def signOut = {
        // Log the user out of the application.
        SecurityUtils.subject.logout()

        // For now, redirect back to the login page.
        redirect(action: 'moveTechLogin')
    }
    // method for home page
    def home = {
    	def principal = SecurityUtils.subject.principal
    	if(principal){
    		render( view:'home' )
    	} else {
    		flash.message = "Your login has expired and must login again."
    		redirect(action:'login')
    	}
    }
    // Method for my task link
	def assetTask = {
		if(params.fMess){
			flash.clear()
		}
		def principal = SecurityUtils.subject.principal
		if(principal){
            def bundle = params.bundle
            def tab = params.tab
            def proAssetMap
            def bundleId = MoveBundle.find("from MoveBundle mb where mb.id = '${bundle}'")
            def team = params.team
            def projectId = bundleId.project.id
            def projectInstance = Project.findById(projectId)
            def stateVal
            def todoSize
            def allSize
            def assetList = []
            def colorCss
            def rdyState
            def ipState
            def holdState
            def query = new StringBuffer("select a.asset_entity_id as id, a.asset_tag as assetTag, a.source_rack as sourceRack, a.source_rack_position as sourceRackPosition, a.target_rack as targetRack, a.target_rack_position as targetRackPosition, a.model as model, p.current_state_id as currentStateId from asset_entity a left join project_asset_map p on (a.asset_entity_id = p.asset_id) where a.move_bundle_id = $bundle  ")
            if(params.location == "s"){
                stateVal = stateEngineService.getStateId("STD_PROCESS","Unracked")
                query.append(" and a.source_team_id = $team ")
            }else {
                stateVal = stateEngineService.getStateId("STD_PROCESS","Reracked")
                query.append(" and a.target_team_id = $team ")
            }
            allSize = jdbcTemplate.queryForList(query.toString()).size()
            if(tab == "Todo"){
                query.append(" and p.current_state_id < $stateVal ")
            }
            proAssetMap = jdbcTemplate.queryForList(query.toString())
            todoSize = proAssetMap.size()
            
            holdState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Hold"))
            if(params.location == "s"){
                rdyState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Release"))
                ipState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Unracking"))
            }else{
                rdyState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Cleaned"))
                ipState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Reracking"))
            }
            
            proAssetMap.each{
                if(it.currentStateId){
	                if(it.currentStateId == holdState){
	                    colorCss = "asset_hold"
	                }else if(it.currentStateId == rdyState){
	                    colorCss = "asset_ready"
	                }else if(it.currentStateId == ipState){
	                    colorCss = "asset_process"
	                }else if((it.currentStateId > holdState) && (it.currentStateId < rdyState) ){
	                    colorCss = "asset_pending"
	                }else if((it.currentStateId >= rdyState)){
	                    colorCss = "asset_done"
	                }
                } else{
                	colorCss = "asset_pending"
                }
                assetList<<[item:it,cssVal:colorCss]
            }
            if(tab == "All"){
                query.append(" and p.current_state_id < $stateVal ")
                todoSize = jdbcTemplate.queryForList(query.toString()).size()
            }
            return[bundle:bundle,team:team,project:params.project,location:params.location,assetList:assetList,allSize:allSize,todoSize:todoSize,'tab':tab]
		} else {
			flash.message = "Your login has expired and must login again."
			redirect(action:'login')
		}
	}
	//To open div for my task
	/*
	def getServerInfo = {
		def principal = SecurityUtils.subject.principal
		if(principal){
            def assetList = []
            def assetId = params.assetId
            if(assetId){
                def assetItem = AssetEntity.findById(assetId)
                def proMap = ProjectAssetMap.findByAsset(assetItem)
                def currState = stateEngineService.getState("STD_PROCESS",proMap.currentStateId)
                if(assetItem.sourceLocation == null)
                assetItem.sourceLocation = ""
                if(assetItem.sourceRoom == null)
                assetItem.sourceRoom = ""
                if(assetItem.sourceRack == null)
                assetItem.sourceRack = ""
                if(assetItem.sourceRackPosition == null)
                assetItem.sourceRackPosition = ""
                if(assetItem.targetLocation == null)
                assetItem.targetLocation = ""
                if(assetItem.targetRoom == null)
                assetItem.targetRoom = ""
                if(assetItem.targetRack == null)
                assetItem.targetRack = ""
                if(assetItem.targetRackPosition == null)
                assetItem.targetRackPosition = ""
                if(assetItem.assetName == null)
                assetItem.assetName = ""
                if(assetItem.assetTag == null)
                assetItem.assetTag = ""
                if(assetItem.serialNumber == null)
                assetItem.serialNumber = ""
                if(assetItem.model == null)
                assetItem.model = ""
                if(assetItem.powerPort == null)
                assetItem.powerPort = ""
                if(assetItem.nicPort == null)
                assetItem.nicPort = ""
                if(assetItem.hbaPort == null)
                assetItem.hbaPort = ""
                assetList<<[item:assetItem,state:currState]
            }
            render assetList as JSON
		} else {
			flash.message = "Your login has expired and must login again."
			redirect(action:'login')
		}
	}
	*/
    //  Method for my task asset tag search
	def assetSearch = {
		def principal = SecurityUtils.subject.principal
		if(principal){
            def assetItem
            def assetCommt
            def projMap
            def team = params.team
            def search = params.search
            def stateVal
            def taskList
            def taskSize
            def label
            def actionLabel
            def checkHome = params.home
            def loginTeam
            if(team){
            	loginTeam = ProjectTeam.findById(params.team)
            }
            if(search != null){
            	def query = new StringBuffer("from AssetEntity where assetTag = '$search' and moveBundle = $params.bundle")
            	if(params.location == "s"){
            		query.append(" and sourceTeam = $team ")
            	} else {
            		query.append(" and targetTeam = $team ")
            	}
                assetItem = AssetEntity.find(query.toString())
                if(assetItem == null){
                    flash.message = message(code :"Asset Tag number '${search}' was not located")
                    if(checkHome){
                        redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
                        return;
                    } else {
                        redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                        return;
                    }
                }else{
                    def teamName
                    def teamId
                    def bundleName = assetItem.moveBundle.id        
                    if (bundleName != Integer.parseInt(params.bundle)) {
                        flash.message = message(code :"The asset [${assetItem.assetName}] is not part of move bundle [${params.bundle}]")
                        if(checkHome){
                            redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
                            return;
                        } else {
                            redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                            return;
                        }
                    } else {
                        if(params.location == "s"){
                            if(assetItem.sourceTeam){
                                teamId = (assetItem.sourceTeam.id).toString()
                                teamName = assetItem.sourceTeam.name
                            } else {
                                flash.message = message(code :"The asset [${assetItem.assetName}] is not assigned to team [${loginTeam}]")
                                if(checkHome){
                                    redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
                                    return;
                                } else {
                                    redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                                    return;
                                }
                            }
                        } else {
                            if(assetItem.targetTeam){
                                teamId = (assetItem.targetTeam.id).toString()
                                teamName = assetItem.targetTeam.name
                            } else {
                                flash.message = message(code :"The asset [${assetItem.assetName}] is not assigned to team [${loginTeam}]")
                                if(checkHome){
                                    redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
                                    return;
                                } else {
                                    redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                                    return;
                                }
                            }
                        }
                        if(teamId != params.team){
    	                    	
                            flash.message = message(code :"The asset [${assetItem.assetName}] is assigned to team [${teamName}]")
                            if(checkHome){
                                redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
                                return;
                            } else {
                                redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                                return;
                            }
                        } else {
	                        projMap = ProjectAssetMap.findByAsset(assetItem)
	                        if( !projMap ) {
	                            flash.message = message(code :" The asset has not yet been released ")
	                            if(checkHome){
	                                redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
	                                return;
	                            } else {
	                                redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
	                                return;
	                            }
	                        } else {
		                        stateVal = stateEngineService.getState("STD_PROCESS",projMap.currentStateId)
	                            if(stateVal == "Hold"){
	                                flash.message = message(code :"The asset is on Hold. Please contact manager to resolve issue.")
	                                if(checkHome){
	                                    redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
	                                    return;
	                                } else {
	                                    redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
	                                    return;
	                                }
	                            }
	                            taskList = stateEngineService.getTasks("STD_PROCESS","MOVE_TECH",stateVal)
	                            taskSize = taskList.size()
	                            if(taskSize == 1){
	                                if(taskList.contains("Hold")){
	                                    flash.message = message(code :"There is a problem with this asset. Place the asset on hold to alert the move coordinator")
	                                }
	                    	
	                            }else if(taskSize > 1) {
	
	                                taskList.each{
	                                    if(it != "Hold"){
	                                        actionLabel = it
	                                        label =	stateEngineService.getStateLabel("STD_PROCESS",Integer.parseInt(stateEngineService.getStateId("STD_PROCESS",it)))
	                                    }
	                    			
	                                }
	                            }
	                            assetCommt = AssetComment.findAllByAssetEntity(assetItem)
	                            render(view:'assetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:params.search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
	                        }
	                    }
                    }
                }
            }
		 
        } else {
            flash.message = "Your login has expired and must login again."
            redirect(action:'login')
            return;
        }
	}
	
    //  Method for place on hold action
	def placeHold = {
		def enterNote = params.enterNote
		def asset = getAssetEntity(params.search, params.user)
        //def asset = AssetEntity.findByAssetTag(params.search)
        def bundle = asset.moveBundle
        def principal = SecurityUtils.subject.principal
        if(principal){
            def loginUser = UserLogin.findByUsername(principal)
            def team
            if(params.location == 's'){
                team = asset.sourceTeam
            }else{
                team = asset.targetTeam
            }
            def workflow
            if(params.user == "mt"){
                workflow = workflowService.createTransition("STD_PROCESS","MOVE_TECH","Hold",asset,bundle,loginUser,team,params.enterNote)
                if(workflow.success){
                    def assetComment = new AssetComment()
                    assetComment.comment = enterNote
                    assetComment.assetEntity = asset
                    assetComment.commentType = 'issue'
                  	assetComment.createdBy = loginUser.person
                    assetComment.save()
                    redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                } else {
                    flash.message = message(code :workflow.message)
                    redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                }
          
            }else{
                workflow = workflowService.createTransition("STD_PROCESS","CLEANER","Hold",asset,bundle,loginUser,team,params.enterNote)
                def projMap = []
                def assetCommt = []
                def stateVal = null
                def label = null
                def actionLabel = null
                if(workflow.success){
                    def assetComment = new AssetComment()
                    assetComment.comment = enterNote
                    assetComment.assetEntity = asset
                    assetComment.commentType = 'issue'
                 	assetComment.createdBy = loginUser.person
                    assetComment.save()
                    render(view: 'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,"bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo",label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
                } else {
                    flash.message = message(code :workflow.message)
                    render(view: 'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,"bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo",label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
                }
            }
        } else {
        	flash.message = "Your login has expired and must login again."
        	redirect(action:'login')
        }
	}
	
    //  Method for start unracking action
	def unRack = {
        def principal = SecurityUtils.subject.principal
        if(principal){
        	def asset = getAssetEntity(params.search,params.user)//AssetEntity.findByAssetTag(params.search)
            def bundle = asset.moveBundle
            def actionLabel = params.actionLabel
            def loginUser = UserLogin.findByUsername(principal)
            def team
            def assetCommt = params.assetCommt
            if(assetCommt == '[]'){
                assetCommt = "";
            }
            if(params.location == 's'){
                team = asset.sourceTeam
            }else{
                team = asset.targetTeam
            }
            def workflow = workflowService.createTransition("STD_PROCESS","MOVE_TECH",actionLabel,asset,bundle,loginUser,team,assetCommt)
            if(workflow.success){
                redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
            }else{
                flash.message = message(code :workflow.message)
                redirect(action:'assetSearch',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"search":params.search,"assetCommt":params.assetCommt,"label":params.label,"actionLabel":actionLabel])
            }
        } else {
        	flash.message = "Your login has expired and must login again."
        	redirect(action:'login')
        }
	}
	//MyTasks for Cleaning
	def cleaningAssetTask = {
		def principal = SecurityUtils.subject.principal
		if(principal){
            def bundle = params.bundle
            def tab = params.tab
            def proAssetMap
            def bundleId = MoveBundle.find("from MoveBundle mb where mb.id = '${bundle}'")
            def team = params.team
            def projectId = bundleId.project.id
            def projectInstance = Project.findById(projectId)
            def stateVal
            def todoSize
            def allSize
            def assetList = []
            def colorCss
            def rdyState
            def ipState
            def holdState
            def query = new StringBuffer("select a.asset_entity_id as id, a.asset_tag as assetTag, a.source_rack as sourceRack, a.source_rack_position as sourceRackPosition, a.target_rack as targetRack, a.target_rack_position as targetRackPosition, a.model as model, p.current_state_id as currentStateId from asset_entity a left join project_asset_map p on (a.asset_entity_id = p.asset_id) where a.move_bundle_id = $bundle  ")
            if(params.location == "s"){
                stateVal = stateEngineService.getStateId("STD_PROCESS","Cleaned")
            }else {
                stateVal = stateEngineService.getStateId("STD_PROCESS","Cleaned")
            }
            allSize = jdbcTemplate.queryForList(query.toString()).size()
            if(tab == "Todo"){
                query.append(" and p.current_state_id < $stateVal ")
            }
            proAssetMap = jdbcTemplate.queryForList(query.toString())
            todoSize = proAssetMap.size()
            holdState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Hold"))
            if(params.location == "s"){
                rdyState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Cleaned"))
                ipState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Unracked"))
            }else{
                rdyState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Cleaned"))
                ipState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Staged"))
            }
            proAssetMap.each{
                if(it.currentStateId){
	                if(it.currentStateId == holdState){
	                    colorCss = "asset_hold"
	                }else if(it.currentStateId == ipState){
	                    colorCss = "asset_ready"
	                }else if((it.currentStateId > holdState) && (it.currentStateId < ipState) ){
	                    colorCss = "asset_pending"
	                }else if((it.currentStateId >= rdyState)){
	                    colorCss = "asset_done"
	                }
                }else{
                	colorCss = "asset_pending"
                }
                assetList<<[item:it,cssVal:colorCss]
            }
            assetList.sort{
                it.cssVal
            }
            if(tab == "All"){
                query.append(" and p.current_state_id < $stateVal ")
                todoSize = jdbcTemplate.queryForList(query.toString()).size()
            }
            return[bundle:bundle,team:team,project:params.project,location:params.location,assetList:assetList,allSize:allSize,todoSize:todoSize,'tab':tab]
		} else {
			flash.message = "Your login has expired and must login again."
		    redirect(action:'login')
		}
	}
    def cleaningAssetSearch = {
		def browserTest=false
		if(!request.getHeader("User-Agent").contains("MSIE")) {
			browserTest = true
		}	
        def principal = SecurityUtils.subject.principal
        if(principal){
            def textSearch = params.textSearch
            def assetItem
            def assetCommt = []
            def projMap = []
            def team = params.team
            def search = params.search
            if(textSearch){
                search = textSearch
            }
            def stateVal
            def taskList
            def taskSize
            def label
            def actionLabel
            def teamMembers
            def loginTeam
            if(team){
            	loginTeam = ProjectTeam.findById(params.team)
            }
            if(params.menu == "true") {
            	render(view:'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl(), browserTest:browserTest])
            	return;
            } else if(search != null){
            	def query = "from AssetEntity where assetTag = '$search' and moveBundle = $params.bundle"
                assetItem = AssetEntity.find(query.toString())
                if(assetItem == null){
                    flash.message = message(code :"Asset Tag number '${search}' was not located")
                    if(textSearch){
                        render(view:'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl(), browserTest:browserTest])
                        return;
                    } else {
                        redirect(action: 'cleaningAssetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                        return;
                    }
                }else{
                    teamMembers = partyRelationshipService.getTeamMemberNames(assetItem.sourceTeam?.id)
                    def membersCount = ((teamMembers.toString()).tokenize("/")).size()
                    teamMembers = membersCount + "(" + teamMembers.toString() + ")"
                    def bundleName = assetItem.moveBundle.id
                    def teamId
                    def teamName
                    if(assetItem.sourceTeam){
                        teamId = (assetItem.sourceTeam.id).toString()
                        teamName = assetItem.sourceTeam.name
                    } else {
                        flash.message = message(code :"The asset [${assetItem.assetName}] is not assigned to team [${loginTeam}]")
                        if(textSearch){
                            render(view:'cleaningAssetSearch',model:[teamMembers:teamMembers,projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl(), browserTest:browserTest])
                            return;
                        } else {
                            redirect(action: 'cleaningAssetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                            return;
                        }
                    }
                    if(bundleName != Integer.parseInt(params.bundle)){
                        flash.message = message(code :"The asset [${assetItem.assetName}] is not part of move bundle [${params.bundle}]")
                        if(textSearch){
                            render(view:'cleaningAssetSearch',model:[teamMembers:teamMembers,projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl(), browserTest:browserTest])
                            return;
                        } else {
                            redirect(action: 'cleaningAssetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                            return;
                        }
                    } else {
                        projMap = ProjectAssetMap.findByAsset(assetItem)
                        if( !projMap ) {
                            flash.message = message(code :" The asset has not yet been released ")
                            if(textSearch){
                                render(view:'cleaningAssetSearch',model:[teamMembers:teamMembers,projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl(), browserTest:browserTest])
                                return;
                            } else {
                                redirect(action: 'cleaningAssetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                                return;
                            }
                        } else {
                            stateVal = stateEngineService.getState("STD_PROCESS",projMap.currentStateId)
                            if(stateVal == "Hold"){
                                flash.message = message(code :"The asset is on Hold. Please contact manager to resolve issue.")
                                if(textSearch){
                                    render(view:'cleaningAssetSearch',model:[teamMembers:teamMembers, projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl(), browserTest:browserTest])
                                    return;
                                } else {
                                    redirect(action: 'cleaningAssetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":params.tab])
                                    return;
                                }
                            }
                            taskList = stateEngineService.getTasks("STD_PROCESS","CLEANER",stateVal)
                            taskSize = taskList.size()
                            if(taskSize == 1){
                                if(taskList.contains("Hold")){
                                    flash.message = message(code :"There is a problem with this asset. Place the asset on hold to alert the move coordinator")
                                }
	                        	
                            }else if(taskSize > 1) {
                                taskList.each{
                                    if(it != "Hold"){
                                        actionLabel = it
                                        label =	stateEngineService.getStateLabel("STD_PROCESS",Integer.parseInt(stateEngineService.getStateId("STD_PROCESS",it)))
                                    }
                                }
                            }
                            assetCommt = AssetComment.findAll("from AssetComment ac where ac.assetEntity = $assetItem.id and ac.commentType != 'issue' ")
                            render(view:'cleaningAssetSearch',model:[teamMembers:teamMembers, projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl(),browserTest:browserTest])
                        }
                    }
                }
            }
            
        } else {
            flash.message = "Your login has expired and must login again."
            redirect(action:'login')
        }
	}
	def cleaning = {
        def principal = SecurityUtils.subject.principal
        if(principal){
        	def asset = getAssetEntity(params.search,params.user)//AssetEntity.findByAssetTag(params.search)
            def bundle = asset.moveBundle
            def actionLabel = params.actionLabel
            def loginUser = UserLogin.findByUsername(principal)
            def team
            def assetCommt = params.assetCommt
            if(assetCommt == '[]'){
                assetCommt = "";
            }
            if(params.location == 's'){
                team = asset.sourceTeam
            }else{
                team = asset.targetTeam
            }
            def workflow = workflowService.createTransition("STD_PROCESS","CLEANER",actionLabel,asset,bundle,loginUser,team,assetCommt)
            if(workflow.success){
                def projMap = []
                assetCommt = []
                def stateVal = null
                def label = null
                actionLabel = null
                render(view: 'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,"search":params.search,"bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo",label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
            }else{
                flash.message = message(code :workflow.message)
                redirect(action:'cleaningAssetSearch',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"search":params.search,"assetCommt":params.assetCommt,"label":params.label,"actionLabel":actionLabel])
            }
        } else {
        	flash.message = "Your login has expired and must login again."
        	redirect(action:'login')
        }
	}
	
	/**
	 * Used to construct the URL for the current context
	 * @return String - the URL
	 */
	def labelFormatUrl() {
		def protocol = request.getProtocol()
		def serverName = request.getServerName()
		def serverPort = request.getServerPort()
		def contextPath = request.getContextPath()

		return protocol + serverName + ':' + serverPort + contextPath
	}
	
	//cancel the Asset Search
	def cancelAssetSearch = {
		def principal = SecurityUtils.subject.principal
		if(principal){
			def bundle = asset.moveBundle
			def actionLabel = params.actionLabel
			def asset = getAssetEntity(params.search,params.user)//AssetEntity.findByAssetTag(params.search)
			def loginUser = UserLogin.findByUsername(principal)
			def team
			def assetCommt = params.assetCommt
			if(assetCommt == '[]'){
				assetCommt = "";
			}
			if(params.location == 's'){
				team = asset.sourceTeam
			}else{
				team = asset.targetTeam
			}
			def projMap = []
			assetCommt = []
			def stateVal = null
			def label = null
			actionLabel = null
			render(view: 'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,"search":params.search,"bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo",label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
		}else {
			flash.message = "Your login has expired and must login again."
			redirect(action:'login')
		}
	}

    /* --------------------------------------
     * 	Author : Mallikarjun Haranal
     *	Redirect to MyTask after logged in.
     * -------------------------------------- */

	def moveTechSuccessLogin = {
    	def principal = SecurityUtils.subject.principal
    	// Checking user existence
    	if(principal){	        
            def projectTeamInstance = ProjectTeam.findById(params.team)
            def team = projectTeamInstance.name
            def teamMembers = partyRelationshipService.getTeamMemberNames(params.team)
            def location =""
            if( params.location == 's') {
                location = "Unracking"
                projectTeamInstance.currentLocation = "Source"
                projectTeamInstance.save()
            }else if( params.location == 't'){
                location = "Reracking"
                projectTeamInstance.currentLocation = "Target"
                projectTeamInstance.save()
            }
            redirect(action:'assetTask',params:[projectTeam:team,members:teamMembers,project:params.project,loc:location, bundle:params.bundle,team:params.team,location:params.location,"tab":"Todo"])
    	} else {
			flash.message = "Your login has expired and must login again."
            redirect(action:'login')
		}
	}
	/* 	-----------------------------------------------
	 * 	Lokanath Reddy
	 *  Will return the AssetEntity object for assetTag  
	 *-------------------------------------------------*/
	def getAssetEntity(assetTag,user){
		def loginCode = session.getAttribute("USERNAME")
		def loginDetails = loginCode.split("-")
		def movebundle = loginDetails[1]
		def bundleteam = loginDetails[2]
		def location = loginDetails[3]
		def query = new StringBuffer("from AssetEntity where assetTag = '$assetTag' and moveBundle = $movebundle")
		if(user != "ct"){
	    	if(location == "s"){
	    		query.append(" and sourceTeam = $bundleteam ")
	    	} else {
	    		query.append(" and targetTeam = $bundleteam ")
	    	}
		}
		def asset = AssetEntity.find(query.toString())
		return asset 
	}
}
