s/*
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
    def workflowService
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
        def moveBundleInstance
        def projectTeamInstance
        def token = new StringTokenizer(params.username, "-")
        //Getting current project instance
        def projectInstance
        def barcodeText  = params.username.tokenize("-")
        //checking for valid barcode format or not size is 4 (mt-moveid- teamid- s/t)
        if ( barcodeText.size() == 4) {
        	try {
        		moveBundleInstance = MoveBundle.findById(Integer.parseInt(barcodeText.get(1)))
        		projectTeamInstance = ProjectTeam.findById(Integer.parseInt(barcodeText.get(2)))
        		projectInstance = Project.findById( moveBundleInstance.project.id )
        	}
        	catch (Exception ex) {
        		flash.message = message(code :"Login Failed")
        		redirect(action: 'login')
        	}
        	//checkin for movebundle and team instances
        	if( moveBundleInstance != null && projectTeamInstance != null && projectInstance != null ){
        		//Validating is Logindate between startdate and completedate 
        		if( new Date() < projectInstance.startDate || new Date() > projectInstance.completionDate ) {
        			flash.message = message(code :"Login Disabled")
        			redirect(action: 'login')
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
        		}
        	}else {
        		flash.message = message(code :"Login Failed")
        		redirect(action: 'login')
        	}
        }else {
        	flash.message = message(code :"Login Failed")
        	redirect(action: 'login')
        }
    }
	//SignOut
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
                colorCss = "casset_hold"
            }else if(it.currentStateId == rdyState){
                colorCss = "basset_ready"
            }else if(it.currentStateId == ipState){
                colorCss = "easset_process"
            }else if((it.currentStateId > holdState) && (it.currentStateId < rdyState) ){
                colorCss = "asset_pending"
            }else if((it.currentStateId >= rdyState)){
                colorCss = "dasset_done"
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
      
        return[bundle:bundle,team:team,project:params.project,location:params.location,assetList:assetList,allSize:allSize,todoSize:todoSize]
	}
	//To open div for my task
	def getServerInfo = {
			
        def assetId = params.assetId        
        def assetItem = AssetEntity.findById(assetId)
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
        
        render assetItem as JSON
	}
	
	def assetSearch = {
        def assetItem
        def assetCommt
        def projMap
        def team = params.team
        def search = params.search
        def stateVal
        def taskList
        def holdTask
        def taskSize
        def label
        if(search != null){
			assetItem = AssetEntity.findByAssetTag(search)					
			
			
			if(assetItem == null){			
				flash.message = message(code :"Asset Tag number '${search}' was not located")
				redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
			}else{
				def bundleName = assetItem.moveBundle.name
				def teamId = (assetItem.sourceTeam.id).toString()				
				def teamName = assetItem.sourceTeam.name
			
                if(bundleName != params.bundle){
                    flash.message = message(code :"The asset [${assetItem.assetName}] is not part of move bundle [${params.bundle}]")
                    redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                }else if(teamId != params.team){
                    flash.message = message(code :"The asset [${assetItem.assetName}] is assigned to team [${teamName}]")
                    redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                }else{
                    projMap = ProjectAssetMap.findByAsset(assetItem)
                    stateVal = stateEngineService.getState("STD_PROCESS",projMap.currentStateId)
                    if(stateVal == "Hold"){
                    	flash.message = message(code :"The asset is on Hold. Please contact manager to resolve issue.")
                    	redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
                    }
                    taskList = stateEngineService.getTasks("STD_PROCESS","MOVE_TECH",stateVal)                  
                    taskSize = taskList.size()
                    if(taskSize == 1){
                    	if(taskList.contains("Hold")){
                    		flash.message = message(code :"There is a problem with this asset. Place the asset on hold to alert the move coordinator")	
                    		holdTask = 1
                    	}
                    	
                    }else if(taskSize > 1) {
                    	taskList.each{
                    		if(it != "Hold"){
                                label =	stateEngineService.getStateLabel("STD_PROCESS",Integer.parseInt(stateEngineService.getStateId("STD_PROCESS",it)))
                    		}
                    			
                    	}
                    }
                    assetCommt = AssetComment.findAllByAssetEntity(assetItem)
                    render(view:'assetSearch',model:[projMap:projMap,assetCommt:assetCommt,stateVal:stateVal,bundle:params.bundle,team:params.team,project:params.project,location:params.location,holdTask:holdTask,search:params.search,label:label])
                }
			}
        }
       

	}
	
	def placeHold = {
        def asset = AssetEntity.findByAssetTag(params.search)
        def bundle = asset.moveBundle
        def principal = SecurityUtils.subject.principal
        def loginUser = UserLogin.findByUsername(principal)
        def team
        if(params.location == 's'){
            team = asset.sourceTeam
        }else{
            team = asset.targetTeam
        }
			
        def workflow = workflowService.createTransition("STD_PROCESS","MOVE_TECH","Hold",asset,bundle,loginUser,team,params.assetCommt)
        if(workflow.success){
			redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
        }
	}
	
	def unRack = {
        def asset = AssetEntity.findByAssetTag(params.search)
        def bundle = asset.moveBundle
        def label = params.label
        def principal = SecurityUtils.subject.principal
        def loginUser = UserLogin.findByUsername(principal)
        def team
        if(params.location == 's'){
            team = asset.sourceTeam
        }else{
            team = asset.targetTeam
        }
			
        def workflow = workflowService.createTransition("STD_PROCESS","MOVE_TECH",label,asset,bundle,loginUser,team,params.assetCommt)
        if(workflow.success){
			redirect(action: 'assetTask',params:["bundle":params.bundle,"team":params.team,"project":params.project,"location":params.location,"tab":"Todo"])
        }
			
	}
	
}
