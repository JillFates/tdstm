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
    def index = {
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
						
	            render(view:'cleaningTechHome',model:[projectTeam:team,members:teamMembers,project:params.project,loc:teamLocation, bundle:params.bundle,team:params.team,location:params.location])
					
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
        return [ username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri ]
    }
    //sign in for moveTech by reading the barcode as userName
    def signIn = { 	
    	
        def moveBundleInstance
        def projectTeamInstance
        if( params.username ) {
            def token = new StringTokenizer(params.username, "-")
            //Getting current project instance
            def projectInstance
            def barcodeText  = params.username.tokenize("-")
            //checking for valid barcode format or not size is 4 (mt-moveid- teamid- s/t)
            if ( barcodeText.size() == 4) {
                if(barcodeText.get(0) == "mt"){
                    try {
                        moveBundleInstance = MoveBundle.findById(Integer.parseInt(barcodeText.get(1)))
                        projectTeamInstance = ProjectTeam.findById(Integer.parseInt(barcodeText.get(2)))
                        projectInstance = Project.findById( moveBundleInstance.project.id )
                    }
                    catch (Exception ex) {
                        flash.message = message(code :"Login Failed")
                        redirect(action: 'login')
                        return;
                    }
                    //checkin for movebundle and team instances
                    if( moveBundleInstance != null && projectTeamInstance != null && projectInstance != null ){
                        //Validating is Logindate between startdate and completedate
                        if( new Date() < projectInstance.startDate || new Date() > projectInstance.completionDate ) {
                            flash.message = message(code :"Login Disabled")
                            redirect(action: 'login')
                            return;
                        }else {
                            def assetEntityInstance
                            if ( barcodeText.get(3) == 's') {
                                assetEntityInstance = AssetEntity.find("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id and ae.sourceTeam = $projectTeamInstance.id")
                            }else if( barcodeText.get(3) == 't' ){
                                assetEntityInstance = AssetEntity.find("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id and ae.targetTeam = $projectTeamInstance.id")
                            }
                            //checking for team corresponding to moveBundle exist or not
                            if( assetEntityInstance != null) {
                                def moveTech = [ user: barcodeText.get(0) ]
                                moveTech['bundle'] = moveBundleInstance.name
                                moveTech['team'] = Integer.parseInt(barcodeText.get(2))
                                moveTech['location'] = barcodeText.get(3)
                                moveTech['project'] = projectInstance.name
                                checkAuth(barcodeText.get(0), moveTech)
                            }else {
                                flash.message = message(code :"Login Failed")
                                redirect(action: 'login')
                                return;
                            }
                        }
                    }else {
                        flash.message = message(code :"Login Failed")
                        redirect(action: 'login')
                        return;
                    }
                } else if( barcodeText.get(0) == "ct" ) {
                    try {
                        moveBundleInstance = MoveBundle.findById(Integer.parseInt(barcodeText.get(1)))
                        projectTeamInstance = ProjectTeam.findById(Integer.parseInt(barcodeText.get(2)))
                        projectInstance = Project.findById( moveBundleInstance.project.id )
                    }
                    catch (Exception ex) {
                        flash.message = message(code :"Login Failed")
                        redirect(action: 'login')
                        return;
                    }
                    //checkin for movebundle and team instances
                    if( moveBundleInstance != null && projectTeamInstance != null && projectInstance != null && projectTeamInstance.teamCode == "Cleaning"){
                        //Validating is Logindate between startdate and completedate
                        if( new Date() < projectInstance.startDate || new Date() > projectInstance.completionDate ) {
                            flash.message = message(code :"Login Disabled")
                            redirect(action: 'login')
                            return;
                        }else {
                            def assetEntityInstance
                            if ( barcodeText.get(3) == 's' ) {
                                assetEntityInstance = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id ")
                            }else if( barcodeText.get(3) == 't' ){
                                assetEntityInstance = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id ")
                            }
                            //checking for team corresponding to moveBundle exist or not
                            if( assetEntityInstance != null) {
                                def moveTech = [ user: barcodeText.get(0) ]
                                moveTech['bundle'] = moveBundleInstance.name
                                moveTech['team'] = Integer.parseInt(barcodeText.get(2))
                                moveTech['location'] = barcodeText.get(3)
                                moveTech['project'] = projectInstance.name
                                checkAuth(barcodeText.get(0), moveTech)
                            }else {
                                flash.message = message(code :"Login Failed")
                                redirect(action: 'login', moveTech)
                                return;
                            }
                        }
                    }else {
                        flash.message = message(code :"Login Failed")
                        redirect(action: 'login')
                        return;
                    }
	        	 
	        	 
	        	 
                } else {
                    flash.message = message(code :"Login Failed")
                    redirect(action: 'login')
                    return;
                }
            }else {
                flash.message = message(code :"Login Failed")
                redirect(action: 'login')
                return;
            }
        } else {
        	flash.message = message(code :"Login Failed")
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
            redirect(controller:'moveTech',params:actionScreen)
            return;
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
        //  SecurityUtils.subject?.logout()

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
		def principal = SecurityUtils.subject.principal
		if(principal){
            def bundle = params.bundle
            def tab = params.tab
            def proAssetMap
            def bundleId = MoveBundle.find("from MoveBundle mb where mb.name = '${bundle}'")
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
            def query = new StringBuffer()
            query.append("from ProjectAssetMap pam where pam.project = ${projectInstance.id} ")
            if(params.location == "s"){
                stateVal = stateEngineService.getStateId("STD_PROCESS","Unracked")
                query.append("and pam.asset in (select id from AssetEntity ae where ae.moveBundle = ${bundleId.id} and ae.sourceTeam = ${team}) ")
            }else {
                stateVal = stateEngineService.getStateId("STD_PROCESS","Reracked")
                query.append("and pam.asset in (select id from AssetEntity ae where ae.moveBundle = ${bundleId.id} and ae.targetTeam = ${team}) ")
            }
            allSize = ProjectAssetMap.findAll(query.toString()).size()
            if(tab == "Todo"){
                query.append("and pam.currentStateId < ${stateVal}")
            }
            proAssetMap = ProjectAssetMap.findAll(query.toString())
            todoSize = proAssetMap.size()
            proAssetMap.each{
                holdState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Hold"))
                if(params.location == "s"){
                    rdyState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Release"))
                    ipState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Unracking"))
                }else{
                    rdyState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Cleaned"))
                    ipState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Reracking"))
                }
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
                assetList<<[item:it,cssVal:colorCss]
            }
            if(tab == "All"){
                query.append("and pam.currentStateId < ${stateVal}")
                todoSize = ProjectAssetMap.findAll(query.toString()).size()
            }
            return[bundle:bundle,team:team,project:params.project,location:params.location,assetList:assetList,allSize:allSize,todoSize:todoSize,'tab':tab]
		} else {
			flash.message = "Your login has expired and must login again."
			redirect(action:'login')
		}
	}
	//To open div for my task
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
            if(search != null){
                assetItem = AssetEntity.findByAssetTag(search)
                if(assetItem == null){
                    flash.message = message(code :"Asset Tag number '${search}' was not located")
                    if(checkHome){
                        redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
                        return;
                    } else {
                        redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                        return;
                    }
                }else{
                    def teamName
                    def teamId
                    def bundleName = assetItem.moveBundle.name
                    if(params.location == "s"){
                        teamId = (assetItem.sourceTeam.id).toString()
                        teamName = assetItem.sourceTeam.name
                    }else{
                        teamId = (assetItem.targetTeam.id).toString()
                        teamName = assetItem.targetTeam.name
                    }
			
                    if(bundleName != params.bundle){
                        flash.message = message(code :"The asset [${assetItem.assetName}] is not part of move bundle [${params.bundle}]")
                        if(checkHome){
                            redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
                            return;
                        } else {
                            redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                            return;
                        }
                    }else if(teamId != params.team){
                        flash.message = message(code :"The asset [${assetItem.assetName}] is assigned to team [${teamName}]")
                        if(checkHome){
                            redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
                            return;
                        } else {
                            redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                            return;
                        }
                    }else{
                        projMap = ProjectAssetMap.findByAsset(assetItem)
                        stateVal = stateEngineService.getState("STD_PROCESS",projMap.currentStateId)
                        if(stateVal == "Hold"){
                            flash.message = message(code :"The asset is on Hold. Please contact manager to resolve issue.")
                            if(checkHome){
                                redirect(action: 'index',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"user":"mt"])
                                return;
                            } else {
                                redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
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
        } else {
            flash.message = "Your login has expired and must login again."
            redirect(action:'login')
            return;
        }
	}
	
    //  Method for place on hold action
	def placeHold = {
		def enterNote = params.enterNote
        def asset = AssetEntity.findByAssetTag(params.search)
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
        def asset = AssetEntity.findByAssetTag(params.search)
        def bundle = asset.moveBundle
        def actionLabel = params.actionLabel
        def principal = SecurityUtils.subject.principal
        if(principal){
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
            def bundleId = MoveBundle.find("from MoveBundle mb where mb.name = '${bundle}'")
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
            def query = new StringBuffer()
            query.append("from ProjectAssetMap pam where pam.project = ${projectInstance.id} ")
            if(params.location == "s"){
                stateVal = stateEngineService.getStateId("STD_PROCESS","Cleaned")
                query.append("and pam.asset in (select id from AssetEntity ae where ae.moveBundle = ${bundleId.id} ) ")
            }else {
                stateVal = stateEngineService.getStateId("STD_PROCESS","Cleaned")
                query.append("and pam.asset in (select id from AssetEntity ae where ae.moveBundle = ${bundleId.id} ) ")
            }
            allSize = ProjectAssetMap.findAll(query.toString()).size()
            if(tab == "Todo"){
                query.append("and pam.currentStateId < ${stateVal}")
            }
            proAssetMap = ProjectAssetMap.findAll(query.toString())
            todoSize = proAssetMap.size()
            proAssetMap.each{
                holdState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Hold"))
    				
                if(params.location == "s"){
                    rdyState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Cleaned"))
                    ipState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Unracked"))
    				
                }else{
                    rdyState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Cleaned"))
                    ipState = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Staged"))
                }
                if(it.currentStateId == holdState){
                    colorCss = "asset_hold"
                }else if(it.currentStateId == ipState){
                    colorCss = "asset_ready"
                }else if((it.currentStateId > holdState) && (it.currentStateId < ipState) ){
                    colorCss = "asset_pending"
                }else if((it.currentStateId >= rdyState)){
                    colorCss = "asset_done"
                }
                assetList<<[item:it,cssVal:colorCss]
            }
            assetList.sort{
                it.cssVal
            }
            if(tab == "All"){
                query.append("and pam.currentStateId < ${stateVal}")
                todoSize = ProjectAssetMap.findAll(query.toString()).size()
            }
            return[bundle:bundle,team:team,project:params.project,location:params.location,assetList:assetList,allSize:allSize,todoSize:todoSize,'tab':tab]
		} else {
			flash.message = "Your login has expired and must login again."
		    redirect(action:'login')
		}
	}
    def cleaningAssetSearch = {
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
            try {
                if(search != null){
                    assetItem = AssetEntity.findByAssetTag(search)
                    if(assetItem == null){
                        flash.message = message(code :"Asset Tag number '${search}' was not located")
                        if(textSearch){
                            render(view:'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
                        } else {
                            redirect(action: 'cleaningAssetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                        }
                    }else{
                        def bundleName = assetItem.moveBundle.name
                        def teamId = (assetItem.sourceTeam.id).toString()
                        def teamName = assetItem.sourceTeam.name
    			
                        if(bundleName != params.bundle){
                            flash.message = message(code :"The asset [${assetItem.assetName}] is not part of move bundle [${params.bundle}]")
                            if(textSearch){
                                render(view:'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
                            } else {
                                redirect(action: 'cleaningAssetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                            }
                        } else {
                            projMap = ProjectAssetMap.findByAsset(assetItem)
                            stateVal = stateEngineService.getState("STD_PROCESS",projMap.currentStateId)
                            if(stateVal == "Hold"){
                                flash.message = message(code :"The asset is on Hold. Please contact manager to resolve issue.")
                                if(textSearch){
                                    render(view:'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
                                } else {
                                    redirect(action: 'cleaningAssetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
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
                            assetCommt = AssetComment.findAllByAssetEntity(assetItem)
                            
                            render(view:'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
                        }
                    }
                }
            } catch (Exception ex){
                flash.message = message(code :"The asset is not associated with bundle and team, please check it")
                if(textSearch){
                    render(view:'cleaningAssetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,search:search,label:label,actionLabel:actionLabel,filePath:labelFormatUrl()])
                } else {
                    redirect(action: 'cleaningAssetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                }
		
            }
        } else {
            flash.message = "Your login has expired and must login again."
            redirect(action:'login')
        }
	}
	def cleaning = {
        def asset = AssetEntity.findByAssetTag(params.search)
        def bundle = asset.moveBundle
        def actionLabel = params.actionLabel
        def principal = SecurityUtils.subject.principal
        if(principal){
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
	// get context details to get label format path
    def labelFormatUrl() {
        def tempProtocol = request.getProtocol()
        def protocol = tempProtocol.substring(0,tempProtocol.indexOf("/"))
        def serverName = request.getServerName()
        def serverPort = request.getServerPort()
        def contextPath = request.getContextPath()
        // construct application URL
        def appUrl = protocol + "://" + serverName + ":" + serverPort + contextPath
        // get connection
        def fileUrl = new URL( appUrl )
		return fileUrl
	}
}
