import grails.converters.JSON
import org.jsecurity.SecurityUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
class ClientConsoleController {
	def stateEngineService
	def userPreferenceService
	def workflowService
    def  jdbcTemplate
    def index = { 
    	redirect(action:list,params:params)
    }
	/*-----------------------------------------------------
	 *  List of asset for client console
	 *  @author : Lokanath Reddy
	 *  @param  : asset filters and movebundle and project
	 *  @return : AssetEntity Details and AssetTransition details
	 */
    def list={
    	def headerCount = getHeaderNames()
    	def browserTest = request.getHeader("User-Agent").contains("MSIE")
        def projectId=params.projectId
        def bundleId = params.moveBundle
        def moveBundleInstance
		//def projectMap
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
            		moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
            	}
            } else {
            	moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
            }
        }
    	if(moveBundleInstance != null){
			def applicationList=AssetEntity.executeQuery("select distinct ae.application , count(ae.id) from AssetEntity "+
															"ae where  ae.moveBundle=${moveBundleInstance.id} "+
															"group by ae.application order by ae.application")
			def appOwnerList=AssetEntity.executeQuery("select distinct ae.appOwner, count(ae.id) from AssetEntity ae where "+
														"ae.moveBundle=${moveBundleInstance.id} group by ae.appOwner order by ae.appOwner")
			def appSmeList=AssetEntity.executeQuery("select distinct ae.appSme, count(ae.id) from AssetEntity ae where "+
														" ae.moveBundle=${moveBundleInstance.id} group by ae.appSme order by ae.appSme")
			def query = new StringBuffer("select ae.asset_entity_id as id,ae.application,ae.app_owner as appOwner,ae.app_sme as appSme,"+
											"ae.asset_name as assetName,max(cast(at.state_to as UNSIGNED INTEGER)) as maxstate "+
											" FROM asset_entity ae LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id and at.voided = 0 ) "+
											"where ae.project_id = $projectId and ae.move_bundle_id = ${moveBundleInstance.id}")
			if(appValue!="" && appValue!= null){
				if(appValue == 'blank'){
					query.append(" and ae.application = '' ")
				} else {
					def app = appValue.replace("'","\\'")
					query.append(" and ae.application ='$app'")
				}
			}
			if(appOwnerValue!="" && appOwnerValue!= null){
				if(appOwnerValue == 'blank'){
					query.append(" and ae.app_owner = '' ")
				} else {
					def owner = appOwnerValue.replace("'","\\'")
					query.append(" and ae.app_owner='$owner'")
				}
				
			}
			if(appSmeValue!="" && appSmeValue!= null){
				if(appSmeValue == 'blank'){
					query.append(" and ae.app_sme = '' ")
				} else {
					def sme = appSmeValue.toString().replace("'","\\'")
					query.append(" and ae.app_sme='$sme'")
				}
			}
			query.append(" GROUP BY ae.asset_entity_id")
        
			if(sortby != "" && sortby != null){
				query.append(" order by $sortby")
			}
			if(order != "" && order != null){
				query.append(" $order ")
			}
			def resultList=jdbcTemplate.queryForList(query.toString())
			def today = new java.util.Date();
			getSession().setAttribute("LAST_POOL_TIME",new java.sql.Timestamp(today.getTime()))
			def assetEntityList=[]
			def processTransitionList=[]
			def tempTransitions = []
			def processTransitions= stateEngineService.getTasks(projectInstance.workflowCode, "TASK_ID")
			processTransitions.each{
				tempTransitions <<Integer.parseInt(it)
			}
			tempTransitions.sort().each{
				def processTransition = stateEngineService.getState(projectInstance.workflowCode,it)
				processTransitionList<<[header:stateEngineService.getStateLabel(projectInstance.workflowCode,it),transId:stateEngineService.getStateId(projectInstance.workflowCode,processTransition)]
			}
			def htmlTdId = new StringBuffer()
			resultList.each{
				def stateId = 0
				def assetId = it.id
				def htmlTd = []
				def maxstate = it.maxstate
				def assetEntity = AssetEntity.get(assetId)
				/*projectMap = ProjectAssetMap.findByAsset(assetEntity)
				if(projectMap){
					stateId = projectMap.currentStateId
				}*/
				def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
																"where t.asset_entity_id = $assetId and voided = 0 order by date_created desc limit 1 ")
				if(transitionStates.size()){
					stateId = transitionStates[0].stateTo
				}
				holdId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Hold"))
				releaseId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Release"))
				reRackId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Reracked"))
				if(stateId == 0){
					check = true
				} else if((stateId > holdId && stateId < releaseId) || (stateId > reRackId)){
					stateVal = stateEngineService.getState(projectInstance.workflowCode,stateId)
					taskVal = stateEngineService.getTasks(projectInstance.workflowCode,"MANAGER",stateVal)
					if(taskVal.size() == 0){
						check = false
					}else{
						check = true
					}
				}else{
					check = false
				}
				def naTransQuery = "from AssetTransition where assetEntity = $assetId and voided = 0 and type = 'boolean' and isNonApplicable = 1"
				processTransitionList.each() { trans ->
					def cssClass='task_pending'
					def transitionId = Integer.parseInt(trans.transId)
					if( transitionId <= maxstate  ){
						cssClass = "task_done"
						if(stateId == 10 ){
							cssClass = "asset_hold"
						} else if( transitionId == 10 ){
							cssClass='task_pending'
						} else if(AssetTransition.find(naTransQuery+" and stateTo = "+transitionId)){
							cssClass='asset_pending'
						}
					}
					htmlTd << "<td id=\"${assetId+"_"+trans.transId}\" class=\"$cssClass\" onclick=\"${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+'+ assetId, before:'document.showForm.id.value ='+ assetId+';document.editForm.id.value = '+ assetId+';', onComplete:'showAssetDialog(e , \'show\')')}\">&nbsp;</td>"
					htmlTdId.append("${assetId+"_"+trans.transId},")
				}
				assetEntityList << [id: assetId, application:it.application,appOwner:it.appOwner,appSme:it.appSme,assetName:it.assetName,transitions:htmlTd,checkVal:check]
			}
			userPreferenceService.loadPreferences("CLIENT_CONSOLE_REFRESH")
			def timeToRefresh = getSession().getAttribute("CLIENT_CONSOLE_REFRESH")
			return [moveBundleInstance:moveBundleInstance,moveBundleInstanceList:moveBundleInstanceList,assetEntityList:assetEntityList,
				appOwnerList:appOwnerList,applicationList:applicationList,appSmeList:appSmeList,projectId:projectId,
				processTransitionList:processTransitionList,projectId:projectId,appOwnerValue:appOwnerValue,appValue:appValue,
				appSmeValue:appSmeValue,timeToRefresh:timeToRefresh ? timeToRefresh.CLIENT_CONSOLE_REFRESH : "never",
				headerCount:headerCount,browserTest:browserTest, myForm : params.myForm, htmlTdId:htmlTdId]
    	} else {
    		flash.message = "Please create bundle to view PMO Dashboard"
    		redirect(controller:'project',action:'show',params:["id":params.projectId])
    	}
	}
	/*---------------------------------------------------------
	 * To get list of task for an asset through ajax
	 * @author : Bhuvaneshwari
	 * @param  : AssetEntitys  
	 * @return : Tasks list for params asset
	 *---------------------------------------------------------*/
	def getTask = {
        def stateVal
        def taskList = []
        def temp
        def totalList = []
        def tempTaskList = []
        def assetId = params.assetEntity
        def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
        /*def projectMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = ${params.assetEntity}")
        if(projectMap != null){
			stateVal = stateEngineService.getState(projectInstance.workflowCode,projectMap.currentStateId)
        }*/
        def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
        												"where t.asset_entity_id = $assetId and voided = 0 order by date_created desc limit 1 ")
		if(transitionStates.size()){
			stateVal = stateEngineService.getState(projectInstance.workflowCode,transitionStates[0].stateTo)
		}
		if(stateVal){
			temp = stateEngineService.getTasks(projectInstance.workflowCode,"MANAGER",stateVal)
		} else {
			temp =  ["Ready"]
		}
        temp.each{
			tempTaskList << Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,it))
		}
        tempTaskList.sort().each{
			taskList << [state:stateEngineService.getState(projectInstance.workflowCode,it),label:stateEngineService.getStateLabel(projectInstance.workflowCode,it)]
		}
        totalList<<[item:taskList,asset:assetId]
        render totalList as JSON
	}
	/*---------------------------------------------------------
	 * Will set user preference for CLIENT_CONSOLE_REFRESH time
	 * @author : Lokanath Reddy
	 * @param  : refresh time 
	 * @return : refresh time 
	 *---------------------------------------------------------*/
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
	/*---------------------------------------------------------
	 * To get unique list of task for list of assets through ajax
	 * @author : Bhuvaneshwari
	 * @param  : AssetEntitys array 
	 * @return : Tasks list for params asset array 
	 *---------------------------------------------------------*/
	def getList = {
    	def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
        def assetArray = params.assetArray
        Set common = new HashSet()
        def taskList = []
        def checkList = []
        def sortList = []
        def tempTaskList = []
        def temp
        def totalList = []
        //def projectMap = ProjectAssetMap.findAll("from ProjectAssetMap pam where pam.asset in ($assetArray)")
        def stateVal
        if(assetArray){
        	def assetList = assetArray.split(",") 
        	assetList.each{ asset->
        		//def projectAssetMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = $asset")
        		def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
    															"where t.asset_entity_id = $asset and voided = 0 order by date_created desc limit 1 ")
        		if(transitionStates.size()){
        			stateVal = stateEngineService.getState(projectInstance.workflowCode,transitionStates[0].stateTo)
                    temp = stateEngineService.getTasks(projectInstance.workflowCode,"MANAGER",stateVal)
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
           		tempTaskList << Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,it))
       		}
       		tempTaskList.sort().each{
       			sortList << [state:stateEngineService.getState(projectInstance.workflowCode,it),label:stateEngineService.getStateLabel(projectInstance.workflowCode,it)]
       		}
        	totalList << [item:sortList,asset:assetArray]
        }
        render totalList as JSON
    }
	/*---------------------------------------------------------
	 * To change the status for an asset
	 * @author : Bhuvaneshwari
	 * @param  : AssetEntitys array , tostate 
	 * @return : Change the status for params asset array 
	 *---------------------------------------------------------*/
    
	def changeStatus = {
        def assetId = params.asset
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )		 		 
        def assetEnt = AssetEntity.findAll("from AssetEntity ae where ae.id in ($assetId)")
        assetEnt.each{
	        def bundle = it.moveBundle
	        def principal = SecurityUtils.subject.principal
	        def loginUser = UserLogin.findByUsername(principal)
	        def team = it.sourceTeam
			     
	        def workflow = workflowService.createTransition(projectInstance.workflowCode,"MANAGER",params.taskList,it,bundle,loginUser,team,params.enterNote)
	        if(workflow.success){
	        	if(params.enterNote != ""){
	                def assetComment = new AssetComment()
	                assetComment.comment = params.enterNote
	                assetComment.commentType = 'issue'
	             	assetComment.createdBy = loginUser.person
	                assetComment.assetEntity = it
	                assetComment.save()
	            }
	        }else{
	        	flash.message = message(code :workflow.message)		            
	        }
        }
    
        redirect(action:'list',params:["projectId":params.projectId,"moveBundle":params.moveBundle])
			
	       
    }
	
	/* -----------------------------------------------------
	 * @author: Lokanada Reddy
	 * @param : MoveBundle, application,appSme,appOwner
	 * @return: AssetEntity object with recent transactions
	 *----------------------------------------------------*/
	def getTransitions = {
		def bundleId = params.moveBundle
		def appValue=params.application
		def appOwnerValue=params.appOwner
		def appSmeValue=params.appSme
		def assetEntityList = []
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		if(bundleId){
			def moveBundleInstance = MoveBundle.findById( bundleId )
			def lastPoolTime = getSession().getAttribute("LAST_POOL_TIME")
			def today = new java.util.Date();
			def currentPoolTime = new java.sql.Timestamp(today.getTime())
			getSession().setAttribute("LAST_POOL_TIME",currentPoolTime)
			def query = new StringBuffer("select ae.asset_entity_id as id,ae.application,ae.app_owner as appOwner,ae.app_sme as appSme,ae.asset_name "+
											" as assetName,max(cast(at.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_entity ae "+
											" LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id and at.voided = 0 ) where ae.asset_entity_id in "+
											" ( select t.asset_entity_id from asset_transition t where t.voided = 0 and t.date_created between '$lastPoolTime' and '$currentPoolTime' )"+
											" and ae.project_id = $moveBundleInstance.project.id and ae.move_bundle_id = ${moveBundleInstance.id}")
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
			def processTransitions= stateEngineService.getTasks(projectInstance.workflowCode, "TASK_ID")
			resultList.each{
				def stateId = 0
				def assetId = it.id
				def tdId = []
				def check
				def maxstate = it.maxstate
				def assetEntity = AssetEntity.get(assetId)
				/*def projectMap = ProjectAssetMap.findByAsset(assetEntity)
				if(projectMap){
					stateId = projectMap.currentStateId
				}*/
				def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
																"where t.asset_entity_id = $assetId and voided = 0 order by date_created desc limit 1 ")
				if(transitionStates.size()){
					stateId = transitionStates[0].stateTo
				}
				def holdId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Hold"))
				def releaseId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Release"))
				def reRackId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Reracked"))
				if(stateId == 0){
					check = true
				} else if((stateId > holdId && stateId < releaseId) || (stateId > reRackId)){
					def stateVal = stateEngineService.getState(projectInstance.workflowCode,stateId)
					def taskVal = stateEngineService.getTasks(projectInstance.workflowCode,"MANAGER",stateVal)
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
					def transitionId = Integer.parseInt(trans)
					if( transitionId <= maxstate  ){
						cssClass = "task_done"
						if(stateId == 10 ){
							cssClass = "asset_hold"
						} else if( transitionId == 10 ){
							cssClass='task_pending'
						}
					}
					tdId << [id:"${assetId+"_"+trans}", cssClass:cssClass]
				}
				def showCommentIcon = false
				def assetComment = AssetComment.find('from AssetComment where assetEntity = '+assetId+' and commentType = ? and isResolved = ?',['issue',0])
				if(assetComment){
					showCommentIcon = true
				}
				assetEntityList << [id: assetId, application:it.application ? it.application : "&nbsp;",appOwner:it.appOwner ? it.appOwner : "&nbsp;", 
									appSme:it.appSme ? it.appSme : "&nbsp;",assetName:it.assetName ? it.assetName :"&nbsp;",tdId:tdId,
									check:check, showCommentIcon:showCommentIcon]
			}
		}
    	render assetEntityList as JSON
    }
	
	
	/* -----------------------------------------------------
	 * get header name details for svg.
	 * @author: Mallikarjun
	 * @return: count of tasks
	 *----------------------------------------------------*/
	def getHeaderNames = {
		def tempTransitions = []
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		def processTransitions= stateEngineService.getTasks(projectInstance.workflowCode, "TASK_ID")
		processTransitions.each{
			tempTransitions <<Integer.parseInt(it)
		}
	       
		def svgHeaderFile = new StringBuffer()
		svgHeaderFile.append("<?xml version='1.0' encoding='UTF-8' standalone='no'?>")
		svgHeaderFile.append("<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' 'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>")
		svgHeaderFile.append("<svg version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'>")
		svgHeaderFile.append("<script type='text/javascript'>")
		svgHeaderFile.append("<![CDATA[")
		svgHeaderFile.append("//this will create htmljavascriptfunctionname in html document and link it to changeText")
		svgHeaderFile.append("top.htmljavascriptfunctionname = changeText;")
		svgHeaderFile.append("function changeText(txt){")
		svgHeaderFile.append("targetText=document.getElementById('thetext');")
		svgHeaderFile.append("var newText = document.createTextNode(txt);")
		svgHeaderFile.append("targetText.replaceChild(newText,targetText.childNodes[0]);")
		svgHeaderFile.append("}")
		svgHeaderFile.append("// ]]>")
		svgHeaderFile.append("</script>")
		svgHeaderFile.append("<text id='thetext' text-rendering='optimizeLegibility' transform='rotate(270, 90, 0)' font-weight='bold' "+
							"font-size='12' fill='#333333' x='-11' y='-76' font-family='verdana,arial,helvetica,sans-serif'>")
		def count = 0
		tempTransitions.sort().each{
			def processTransition = stateEngineService.getStateLabel(projectInstance.workflowCode,it)
			if(count == 0){
				svgHeaderFile.append("${processTransition}")
			} else {
				svgHeaderFile.append("<tspan x='-11' dy='22'>${processTransition}</tspan>")
			}
			count++
		}
		svgHeaderFile.append("</text>")
		svgHeaderFile.append("<path d='M 22 0 l 0 120")
		def value = 22
		for(int i=0;i<count;i++){
			value = value+22
			svgHeaderFile.append(" M ${value} 0 l 0 120")
		}
		svgHeaderFile.append("' stroke = '#FFFFFF' stroke-width = '1'/>")
		svgHeaderFile.append("</svg>")
		def f = ApplicationHolder.application.parentContext.getResource("templates/headerSvg.svg").getFile()
		def fop=new FileOutputStream(f)
		if(f.exists()){
			fop.write(svgHeaderFile.toString().getBytes())
			fop.flush()
			fop.close()
		} else {
			println("This file is not exist")
		}
		return count
	       
	}
	def getMenuList = {
		def id = params.id
		def ids = id.split("_")
		def transId = ids[1]
		def assetEntity = AssetEntity.findById( ids[0] ) 
		def state = stateEngineService.getState( assetEntity.project.workflowCode, Integer.parseInt( ids[1] ) )
		def stateType = stateEngineService.getStateType( assetEntity.project.workflowCode, state )
		def situation = ""
		def assetTransition
		def menuOptions = ""
		assetTransition = AssetTransition.find("from AssetTransition t where t.assetEntity = $assetEntity.id and t.stateTo = $transId and t.type ='boolean' and t.voided = 0 ")
		if(assetTransition){
			if(assetTransition.isNonApplicable != 0){
				situation = "NA"
			} else {
				situation = "done"
			}
		}
		menuOptions = constructManuOptions( state, assetEntity, situation, stateType )
		
		render menuOptions
	}
	
	def constructManuOptions( def state, def assetEntity, def situation, def stateType ){
		 def menuOptions = ""
		 if(stateType != "process"){
			 if(situation == "NA"){
				 menuOptions = "naMenu"
			 } else if(situation == "done"){
				 menuOptions = "doneMenu"
			 } else {
				 menuOptions = "noTransMenu"
			 }
		 } else {
			 def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
					 "where t.asset_entity_id = ${assetEntity.id} and voided = 0 order by date_created desc limit 1 ")
			 def taskList
			 def role = ""
			 def supervisor = SecurityUtils.subject.hasRole("ADMIN")
			 def manager = SecurityUtils.subject.hasRole("MANAGER")
			 if(supervisor){
				 role = "SUPERVISOR"
			 } else if(manager){
				 role = "MANAGER"
			 }
			 if(transitionStates.size()){
				 def transitionSelected = stateEngineService.getStateIdAsInt( assetEntity.project.workflowCode, state )
				 def currentState = transitionStates[0].stateTo
				 def stateVal = stateEngineService.getState(assetEntity.project.workflowCode,currentState)
				 if(currentState < transitionSelected ){
					 taskList = stateEngineService.getTasks(assetEntity.project.workflowCode,role,stateVal)
					 taskList.each{
						 if(it == state){
							 menuOptions = "doMenu"
						 }
					 }
				 } else {
					 menuOptions = "voidMenu"
				 }
			 } else {
				 menuOptions = "readyMenu"
			 }
		 }
		 if(!menuOptions){
			 
			 menuOptions = "noOption"
		 }
		 return menuOptions.toString()
	}
	
	def createTransitionForNA = {
		def actionId = params.actionId
		def type = params.type
		def currentState = 0
		def transitionStatus
		def principal = SecurityUtils.subject.principal
        def loginUser = UserLogin.findByUsername(principal)
		def actions = actionId.split("_")
		def assetEntity = AssetEntity.findById(actions[0])
		def stateToId = Integer.parseInt(actions[1])
		def stateTo = stateEngineService.getState( assetEntity.project.workflowCode, stateToId )
		def currStateQuery = "select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
		 					"where t.asset_entity_id = ${assetEntity.id} and voided = 0 order by date_created desc limit 1 "
		def transitionStates = jdbcTemplate.queryForList(currStateQuery)
		if(transitionStates.size()){
			currentState = transitionStates[0].stateTo
		}
		def role = ""
		def supervisor = SecurityUtils.subject.hasRole("ADMIN")
		def manager = SecurityUtils.subject.hasRole("MANAGER")
		if(supervisor){
			role = "SUPERVISOR"
		} else if(manager){
			role = "MANAGER"
		}
		def assetTransitionQuery = "from AssetTransition t where t.assetEntity = ${assetEntity.id} and t.voided = 0"
		def comment = ""
		if(stateTo == "Hold"){
				comment = "Transition created for Boolean transition"
		}
		def message = ""
		if(role){
			message = "Transaction created successfully"
			switch (type) {
				case "done" :
							def assetTeansition = AssetTransition.find(assetTransitionQuery + " and t.stateTo = $stateToId")
							if(assetTeansition){
								assetTeansition.voided = 1
								assetTeansition.save()
							}
							transitionStatus = workflowService.createTransition( assetEntity.project.workflowCode, role,stateTo, assetEntity, 
																				assetEntity.moveBundle, loginUser, null, comment )
							message = transitionStatus.message
							break;
					
				case "void" : 
							def assetTeansitions = AssetTransition.findAll(assetTransitionQuery + " and t.stateTo >= $stateToId")
							assetTeansitions.each{
								it.voided = 1
								it.save(flush:true)
							}
							changeCurrentStatus(currStateQuery,assetEntity)
							break;
					
				case "ready" :
							transitionStatus = workflowService.createTransition( assetEntity.project.workflowCode, role,"Ready", assetEntity, 
																		assetEntity.moveBundle, loginUser, null, comment )
							message = transitionStatus.message
							break;
				case "NA" :
							def assetTeansition = AssetTransition.find(assetTransitionQuery + " and t.stateTo = $stateToId")
							if(assetTeansition){
								assetTeansition.voided = 1
								assetTeansition.save()
							}
							transitionStatus = workflowService.createTransition( assetEntity.project.workflowCode, role,stateTo, assetEntity, 
																				assetEntity.moveBundle, loginUser, null, comment )
							message = transitionStatus.message
							def currentTransition = AssetTransition.find("from AssetTransition t where t.assetEntity = ${assetEntity.id} "+
																		"and voided = 0 order by dateCreated desc")
							currentTransition.isNonApplicable = 1
							currentTransition.save()
							break;
				case "pending" :
							def assetTeansition = AssetTransition.find(assetTransitionQuery + " and t.stateTo = $stateToId")
							if(assetTeansition){
								assetTeansition.voided = 1
								assetTeansition.save()
							}
							changeCurrentStatus(currStateQuery,assetEntity)
						break;
			}
		} else {
			message = " You don't have permission to create transition"
		}
		render message
	}
	def changeCurrentStatus( def currStateQuery, def assetEntity ){
		def currTransition = jdbcTemplate.queryForList(currStateQuery)
		def currState
		if(currTransition.size()){
			currState = currTransition[0].stateTo
		}
		def projectAssetMap = ProjectAssetMap.findByAsset( assetEntity )
		if(currState){
			projectAssetMap.currentStateId = currState
			projectAssetMap.save()
		} else {
			projectAssetMap.delete()
		}
	}
}
