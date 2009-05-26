import grails.converters.JSON
import org.jsecurity.SecurityUtils
class ClientConsoleController {
	def stateEngineService
	def userPreferenceService
	def workflowService
    def  jdbcTemplate
    def index = { 
    	redirect(action:list,params:params)
    }
	// List of asset for client console
    def list={
        def projectId=params.projectId
        def bundleId = params.moveBundle
        def moveBundleInstance
        def projectMap
        def stateVal
        def taskVal
        def holdId
        def releaseId
        def reRackId
        def check 
        def appValue=params.application
        def appOwnerValue=params.appOwner
        def appSmeValue=params.appSme
        def sortby = params.sort
        def order = params.order
        def projectInstance = Project.findById( projectId )
        def moveBundleInstanceList = MoveBundle.findAll("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
        if(bundleId){
        	userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
            moveBundleInstance = MoveBundle.findById(bundleId)
        } else {
            userPreferenceService.loadPreferences("CURR_BUNDLE")
            def defaultBundle = getSession().getAttribute("CURR_BUNDLE")
            if(defaultBundle.CURR_BUNDLE){
            	moveBundleInstance = MoveBundle.findById(defaultBundle.CURR_BUNDLE)
            	if( moveBundleInstance.project.id != Integer.parseInt(projectId) ){
            	moveBundleInstance = MoveBundle.findByProject(projectInstance)
            	}
            } else {
            	moveBundleInstance = MoveBundle.findByProject(projectInstance)
            }
        }
        def applicationList=AssetEntity.executeQuery("select distinct ae.application from AssetEntity ae where ae.application is not null and ae.project.id="+projectId)
        def appOwnerList=AssetEntity.executeQuery("select distinct ae.appOwner from AssetEntity ae where ae.appOwner is not null and ae.project.id="+projectId)
        def appSmeList=AssetEntity.executeQuery("select distinct ae.appSme from AssetEntity ae where ae.appSme is not null  and ae.project.id="+projectId)
        def query = new StringBuffer("select ae.asset_entity_id as id,ae.application,ae.app_owner as appOwner,ae.app_sme as appSme,ae.asset_name as assetName,GROUP_CONCAT(state_to ORDER BY state_to SEPARATOR ',') as transitions FROM asset_entity ae LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id) where ae.project_id = $projectId and ae.move_bundle_id = ${moveBundleInstance.id}")
        if(appValue!="" && appValue!= null){
        	query.append(" and ae.application ='$appValue'")
        }
        if(appOwnerValue!="" && appOwnerValue!= null){
        	query.append(" and ae.app_owner='$appOwnerValue'")
        }
        if(appSmeValue!="" && appSmeValue!= null){
        	query.append(" and ae.app_sme='$appSmeValue'")
        }
        query.append(" GROUP BY ae.asset_entity_id")
        
        if(sortby != "" && sortby != null){
        	query.append(" order by $sortby")
        }
        if(order != "" && order != null){
        	query.append(" $order ")
        }
        def resultList=jdbcTemplate.queryForList(query.toString())
        def assetEntityList=[]
        def processTransitionList=[]
        def tempTransitions = []
        def processTransitions= stateEngineService.getTasks("STD_PROCESS", "TASK_ID")
        processTransitions.each{
        	tempTransitions <<Integer.parseInt(it)
        }
        tempTransitions.sort().each{
        	def processTransition = stateEngineService.getState("STD_PROCESS",it)
            if(processTransition.length() > 5){
                processTransitionList<<[header:processTransition.substring(0,5), title:stateEngineService.getStateLabel("STD_PROCESS",it),transId:stateEngineService.getStateId("STD_PROCESS",processTransition)]
            } else {
                processTransitionList<<[header:processTransition,title:stateEngineService.getStateLabel("STD_PROCESS",it),transId:stateEngineService.getStateId("STD_PROCESS",processTransition)]
            }
        }
        
        resultList.each{
        	def stateId = 0
        	def assetId = it.id
        	def htmlTd = []
        	def transitions
        	if(it.transitions){
        		transitions =it.transitions.tokenize(',')
        	}
        	def assetEntity = AssetEntity.get(assetId)
        	projectMap = ProjectAssetMap.findByAsset(assetEntity)
        	if(projectMap){
        		stateId = projectMap.currentStateId
        	}
        	holdId = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Hold"))
        	releaseId = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Release"))
        	reRackId = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Reracked"))
        	if(stateId == 0){
        		check = true
        	} else if((stateId > holdId && stateId < releaseId) || (stateId > reRackId)){
        		stateVal = stateEngineService.getState("STD_PROCESS",stateId)
    			taskVal = stateEngineService.getTasks("STD_PROCESS","MANAGER",stateVal)
    			if(taskVal.size() == 0){
    				check = false    				
    			}else{
                    check = true
    			}
        	}else{
        		check = false
        	}
        	processTransitionList.each() { trans ->
	        	def cssClass='task_pending'
                transitions.each() { task ->
	        		if(task == trans.transId){
	        			if(stateId == 10 ){
	        				cssClass = "asset_hold"
	        			} else if(stateId != 10 && Integer.parseInt(task) != 10){
	        				cssClass = "task_done"
	        			}
	        		}
	        	}
	        	htmlTd << "<td id=\"${assetId+"_"+trans.transId}\" class=\"$cssClass\">&nbsp;</td>"	
        	}
        	assetEntityList << [id: assetId, application:it.application,appOwner:it.appOwner,appSme:it.appSme,assetName:it.assetName,transitions:htmlTd,checkVal:check]
        }
        userPreferenceService.loadPreferences("CLIENT_CONSOLE_REFRESH")
        def timeToRefresh = getSession().getAttribute("CLIENT_CONSOLE_REFRESH")
        return [moveBundleInstance:moveBundleInstance,moveBundleInstanceList:moveBundleInstanceList,assetEntityList:assetEntityList,appOwnerList:appOwnerList,applicationList:applicationList,appSmeList:appSmeList,projectId:projectId,processTransitionList:processTransitionList,projectId:projectId,appOwnerValue:appOwnerValue,appValue:appValue,appSmeValue:appSmeValue,timeToRefresh:timeToRefresh ? timeToRefresh.CLIENT_CONSOLE_REFRESH : "never"]
    }
    // To get list of task for an asset through ajax
	def getTask = {
        def stateVal
        def taskList = []
        def temp
        def totalList = []
        def tempTaskList = []
        def assetId = params.assetEntity
        def projectMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = ${params.assetEntity}")
        if(projectMap != null){
			stateVal = stateEngineService.getState("STD_PROCESS",projectMap.currentStateId)
        }
		if(stateVal){
			temp = stateEngineService.getTasks("STD_PROCESS","MANAGER",stateVal)
		} else {
			temp =  ["Ready"]
		}
        temp.each{
			tempTaskList << Integer.parseInt(stateEngineService.getStateId("STD_PROCESS",it))
		}
        tempTaskList.sort().each{
			taskList << [state:stateEngineService.getState("STD_PROCESS",it),label:stateEngineService.getStateLabel("STD_PROCESS",it)]
		}
        totalList<<[item:taskList,asset:assetId]
        render totalList as JSON
	}
    
	// Refresh time selection 
	def setTimePreference = {
        def timer = params.timer
        def refreshTime =[]
        if(timer){
            userPreferenceService.setPreference( "CLIENT_CONSOLE_REFRESH", "${timer}" )
        }
        def timeToRefresh = getSession().getAttribute("CLIENT_CONSOLE_REFRESH")
        refreshTime <<[refreshTime:timeToRefresh]
        render refreshTime as JSON
	}
	
	//To get unique list of task for list of assets through ajax
	def getList = {
    	
        def assetArray = params.assetArray
        Set common = new HashSet()
        def taskList = []
        def checkList = []
        def sortList = []
        def tempTaskList = []
        def temp
        def totalList = []
        def projectMap = ProjectAssetMap.findAll("from ProjectAssetMap pam where pam.asset in ($assetArray)")
        def stateVal
        if(assetArray){
        	def assetList = assetArray.split(",") 
        	assetList.each{ asset->
        		def projectAssetMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = $asset")
        		if(projectAssetMap){
        			stateVal = stateEngineService.getState("STD_PROCESS",projectAssetMap.currentStateId)
                    temp = stateEngineService.getTasks("STD_PROCESS","MANAGER",stateVal)
                    taskList << [task:temp]
        		} else {
        			taskList << [task:["Ready"] ]
        		}
        	}
        	common = (HashSet)(taskList[0].task);
        	for(int i=1; i< taskList.size();i++){
        		common.retainAll((HashSet)(taskList[i].task))
        	}
           	common.each{
           		tempTaskList << Integer.parseInt(stateEngineService.getStateId("STD_PROCESS",it))
       		}
       		tempTaskList.sort().each{
       			sortList << [state:stateEngineService.getState("STD_PROCESS",it),label:stateEngineService.getStateLabel("STD_PROCESS",it)]
       		}
        	totalList << [item:sortList,asset:assetArray]
        }
        render totalList as JSON
    }
	
    // To change the status for an asset
	def changeStatus = {
        def assetId = params.asset
				 
        def assetEnt = AssetEntity.findAll("from AssetEntity ae where ae.id in ($assetId)")
        assetEnt.each{
	        def bundle = it.moveBundle
	        def principal = SecurityUtils.subject.principal
	        def loginUser = UserLogin.findByUsername(principal)
	        def team = it.sourceTeam
			     
	        def workflow = workflowService.createTransition("STD_PROCESS","MANAGER",params.taskList,it,bundle,loginUser,team,params.enterNote)
	        if(workflow.success){
	        	if(params.enterNote != ""){
	                def assetComment = new AssetComment()
	                assetComment.comment = params.enterNote
	                assetComment.commentType = 'issue'
	                assetComment.assetEntity = it
	                assetComment.save()
	            }
	        }else{
	        	flash.message = message(code :workflow.message)		            
	        }
        }
    
        redirect(action:'list',params:["projectId":params.projectId,"moveBundle":params.moveBundle])
			
	       
    }
	
	// Will return updated transitional data
	def getTransitions = {
			def bundleId = params.moveBundle
	        def appValue=params.application
	        def appOwnerValue=params.appOwner
	        def appSmeValue=params.appSme
	        def assetEntityList = []
	        if(bundleId){
		        def moveBundleInstance = MoveBundle.findById( bundleId )
				def query = new StringBuffer("select ae.asset_entity_id as id,ae.application,ae.app_owner as appOwner,ae.app_sme as appSme,ae.asset_name as assetName,GROUP_CONCAT(state_to ORDER BY state_to SEPARATOR ',') as transitions FROM asset_entity ae LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id) where ae.project_id = $moveBundleInstance.project.id and ae.move_bundle_id = ${moveBundleInstance.id}")
		        if(appValue!="" && appValue!= null){
		        	query.append(" and ae.application ='$appValue'")
		        }
		        if(appOwnerValue!="" && appOwnerValue!= null){
		        	query.append(" and ae.app_owner='$appOwnerValue'")
		        }
		        if(appSmeValue!="" && appSmeValue!= null){
		        	query.append(" and ae.app_sme='$appSmeValue'")
		        }
		        query.append(" GROUP BY ae.asset_entity_id")
		        def resultList=jdbcTemplate.queryForList(query.toString())
	            def processTransitions= stateEngineService.getTasks("STD_PROCESS", "TASK_ID")
	            resultList.each{
	            	def stateId = 0
	            	def assetId = it.id
	            	def tdId = []
	            	def transitions
	            	def check
	            	if(it.transitions){
	            		transitions =it.transitions.tokenize(',')
	            	}
	            	def assetEntity = AssetEntity.get(assetId)
	            	def projectMap = ProjectAssetMap.findByAsset(assetEntity)
	            	if(projectMap){
	            		stateId = projectMap.currentStateId
	            	}
	            	def holdId = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Hold"))
	            	def releaseId = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Release"))
	            	def reRackId = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Reracked"))
	            	if(stateId == 0){
	            		check = true
	            	} else if((stateId > holdId && stateId < releaseId) || (stateId > reRackId)){
	            		def stateVal = stateEngineService.getState("STD_PROCESS",stateId)
	        			def taskVal = stateEngineService.getTasks("STD_PROCESS","MANAGER",stateVal)
	        			if(taskVal.size() == 0){
	        				check = false    				
	        			}else{
	                        check = true
	        			}
	            	}else{
	            		check = false
	            	}
	            	processTransitions.each() { trans ->
	    	        	def cssClass='task_pending'
	                    transitions.each() { task ->
	    	        		if(task == trans){
	    	        			if(stateId == 10 ){
	    	        				cssClass = "asset_hold"
	    	        			} else if(stateId != 10 && Integer.parseInt(task) != 10){
	    	        				cssClass = "task_done"
	    	        			}
	    	        		}
	    	        	}
	    	        	tdId << [id:"${assetId+"_"+trans}", cssClass:cssClass]
	            	}
	            	assetEntityList << [id: assetId, application:it.application ? it.application : "",appOwner:it.appOwner ? it.appOwner : "",appSme:it.appSme ? it.appSme : "",assetName:it.assetName ? it.assetName :"",tdId:tdId, check:check]
	            }
	        }
    	render assetEntityList as JSON
    }
}
