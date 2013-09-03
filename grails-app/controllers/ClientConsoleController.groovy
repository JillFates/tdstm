import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.shiro.SecurityUtils

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetTransition
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tds.asset.AssetOptions

class ClientConsoleController {
	
	 protected static customLabels = ['Custom1','Custom2','Custom3','Custom4','Custom5','Custom6','Custom7','Custom8']
	 def stateEngineService
	 def userPreferenceService
	 def securityService
	 def workflowService
	 def jdbcTemplate
	 def pmoAssetTrackingService
	 def taskService
	 def assetEntityService
	 def index = { 
		 redirect(action:list,params:params)
	 }
	/*-----------------------------------------------------
	 *  List of asset for client console
	 *  @author : Lokanath Reddy
	 *  @param  : asset filters and movebundle and project
	 *  @return : AssetEntity Details and AssetTransition details
	 *---------------------------------------------------*/
    def list={
		
		def project = securityService.getUserCurrentProject()
		def browserTest = request.getHeader("User-Agent").contains("MSIE")
		def moveEventId = params.moveEvent
		
		if(moveEventId){
			userPreferenceService.setPreference( "MOVE_EVENT", "${moveEventId}" )
		} else {
			userPreferenceService.loadPreferences("MOVE_EVENT")
			moveEventId = securityService.getUserCurrentMoveEventId()
		}
		
		def moveEventInstance = moveEventId ? MoveEvent.findByIdAndProject(moveEventId, project ) : null
		// Event is not associated with current project fetch 1st one from list
		if(!moveEventInstance){
			userPreferenceService.removePreference( "MOVE_EVENT" )
		 	moveEventInstance = MoveEvent.findByProject(project,[sort:'name',order:'asc'] )
		}
		
		if(!moveEventInstance){
			flash.message = "Please create move event and bundle to view PMO Dashboard"
			redirect(controller:"moveEvent",action:"list")
		} else{
			def bundleId = params.moveBundle
			def moveBundleInstance
			def bundles
			def moveBundleInstanceList = MoveBundle.findAll("from MoveBundle mb where mb.moveEvent = ? order by mb.name asc",[moveEventInstance])
			if( bundleId && bundleId != "all"  ){
				userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
				moveBundleInstance = MoveBundle.findByIdAndMoveEvent(bundleId, moveEventInstance )
				bundles = "("+bundleId+")"
			}
			if(moveBundleInstanceList.size() > 0 && !moveBundleInstance){
				def defaultBundleId = securityService.getUserCurrentMoveBundleId()
				if(bundleId == "all"){
					userPreferenceService.removePreference( "CURR_BUNDLE" )
					def moveBundleIds = moveBundleInstanceList.id
					bundles = (moveBundleInstanceList.id).toString().replace("[","(").replace("]",")")
					bundleId = moveBundleIds[0]

				} else if(defaultBundleId){ // check to see if there is any pref bundle exist
					def defalutBundle = MoveBundle.get(defaultBundleId)
					if(defalutBundle?.moveEvent?.id != moveEventInstance.id){ // check to see if preff bundle belongs to current event , if not remove the pref bundle
						userPreferenceService.removePreference( "CURR_BUNDLE" )
						def moveBundleIds =  moveBundleInstanceList.id
						bundles = (moveBundleIds).toString().replace("[","(").replace("]",")")
						bundleId = moveBundleIds[0]
					} else {
						moveBundleInstance = defalutBundle
						bundles = "("+defaultBundleId+")"
						bundleId = moveBundleInstance.id
					}
				} else  { // use all the bundles if pref bundle not exist
				    def moveBundleIds =  moveBundleInstanceList.id
					bundles = (moveBundleIds).toString().replace("[","(").replace("]",")")
					bundleId = moveBundleIds[0]
				}
			}
			
			def workflowCode
			if(moveBundleInstance){
				workflowCode = moveBundleInstance.workflowCode
			}else{
				workflowCode = bundleId ? MoveBundle.read(bundleId).workflowCode : project.workflowCode
			}
			Set uniqueWorkflowCodes = moveBundleInstanceList.workflowCode
			def showAllOption = uniqueWorkflowCodes.size() == 1 && moveBundleInstanceList.size() > 1
			
			stateEngineService.loadWorkflowTransitionsIntoMap(workflowCode, 'project')
			def headerCount = getHeaderNames(bundleId, workflowCode)
			
			//def projectMap
			def moveEventsList = MoveEvent.findAll("from MoveEvent me where me.project = ? order by me.name asc",[project])
			def columns = userPreferenceService.setAssetTrackingPreference(params.column1Attribute, params.column2Attribute, params.column3Attribute, params.column4Attribute)
			
			// set Pmo assetsInView into current session when changed
			if ( !params.assetsInView ){ 
				params.assetsInView = getSession().getAttribute("PMO_ASSETS_INVIEW")
			} else {
				getSession().setAttribute("PMO_ASSETS_INVIEW",params.assetsInView)
			}
	    	
			def resultList
    		def column1List
			def column2List
			def column3List
			def column4List
			def totalAssets = 0
			if(bundles){
				/*-------------get filter details----------------*/
				def temp1List = AssetEntity.executeQuery("""SELECT DISTINCT ae.${columns?.column1.field}, COUNT(ae.id) FROM AssetEntity ae 
																WHERE  ae.moveBundle.id in ${bundles} GROUP BY ae.${columns?.column1.field} ORDER BY ae.${columns?.column1.field}""")
				column1List = pmoAssetTrackingService.splitFilterExpansion( temp1List, columns?.column1.field, workflowCode )
				
				def temp2List = AssetEntity.executeQuery("""SELECT DISTINCT ae.${columns?.column2.field}, COUNT(ae.id) FROM AssetEntity ae 
																WHERE  ae.moveBundle.id in ${bundles} GROUP BY ae.${columns?.column2.field} ORDER BY ae.${columns?.column2.field}""")
				column2List = pmoAssetTrackingService.splitFilterExpansion( temp2List, columns?.column2.field, workflowCode  )
				
				def temp3List = AssetEntity.executeQuery("""SELECT DISTINCT ae.${columns?.column3.field}, COUNT(ae.id) FROM AssetEntity ae 
																WHERE  ae.moveBundle.id in ${bundles} GROUP BY ae.${columns?.column3.field} ORDER BY ae.${columns?.column3.field}""")
				column3List = pmoAssetTrackingService.splitFilterExpansion( temp3List, columns?.column3.field, workflowCode  )
				
				def temp4List = AssetEntity.executeQuery("""SELECT DISTINCT ae.${columns?.column4.field}, COUNT(ae.id) FROM AssetEntity ae 
																WHERE  ae.moveBundle.id in ${bundles} GROUP BY ae.${columns?.column4.field} ORDER BY ae.${columns?.column4.field}""")
				column4List = pmoAssetTrackingService.splitFilterExpansion( temp4List, columns?.column4.field, workflowCode  )
                
				/*-------------get asset details----------------*/
				def returnValue = pmoAssetTrackingService.getAssetsForListView( project.id, bundles, columns, params )
				//log.error "pmoAssetTrackingService.getAssetsForListView:" + returnValue
				resultList = returnValue[0]
				totalAssets = returnValue[1]
			}
    		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def today = GormUtil.convertInToGMT( "now", tzId );
			def lastPoolTime = new java.sql.Timestamp(today.getTime())
			def assetEntityList=[]
			def processTransitionList=[]
			def tempTransitions = []
			
			def processTransitions= stateEngineService.getTasks(workflowCode,"TASK_ID")
			processTransitions.each{
				tempTransitions <<Integer.parseInt(it)
			}
			tempTransitions.sort().each{
				def processTransition = stateEngineService.getState(workflowCode,it)
				def fillColor = stateEngineService.getHeaderColor(workflowCode, processTransition)
				def transId = Integer.parseInt(stateEngineService.getStateId(workflowCode,processTransition))
				processTransitionList << [ 
					header:stateEngineService.getStateLabel(workflowCode,it),
					transId:transId,
					fillColor:fillColor,
					stateType:stateEngineService.getStateType( workflowCode, stateEngineService.getState(workflowCode, transId)) 
				]
			}
			/* user role check*/
			def role = ""
			def subject = SecurityUtils.subject
			if (subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
				role = "SUPERVISOR"
			} else if(subject.hasRole("EDITOR")){
				role = "EDITOR"
			}
			def holdId = Integer.parseInt(stateEngineService.getStateId(workflowCode,"Hold"))
			def releaseId = Integer.parseInt(stateEngineService.getStateId(workflowCode,"Release"))
			def reRackId = Integer.parseInt(stateEngineService.getStateId(workflowCode,"Reracked"))
			def terminatedId = Integer.parseInt(stateEngineService.getStateId(workflowCode,"Terminated"))
			if (project.runbookOn) {
				//
				// Generate the Asset Entity List using Tasks instead of Workflow
				//
				def wftSql = """SELECT task.asset_entity_id AS assetId, task.status, wft.trans_id AS transId 
					FROM asset_comment task
					JOIN workflow_transition wft ON task.workflow_transition_id=wft.workflow_transition_id
					WHERE task.project_id = ?
					ORDER BY assetId, trans_id"""
				def workflowTasks = jdbcTemplate.queryForList(wftSql, project.id)

				// closure to create the TD HTML used in the page which will find the tasks associated with each of the Process Transitions
				def buildHtml = { assetId ->
					def html=[]
					processTransitionList.each() { pt ->
						def task = workflowTasks.find{ wft -> wft.assetId == assetId && wft.transId == pt.transId }
						def css = task ? "task_${task.status.toLowerCase()}" : 'task_na'
						html << "<td id=\"${assetId+"_"+pt.transId}\" class=\"${css} tranCell\"  >&nbsp;</td>"
					}
					return html
				}
				
				// Iterate over the list of assets and build the list
				resultList.each{ asset -> 
					def html = buildHtml(asset.id)
					// TODO : Runbook : Figure out how to determine the currentStatus
					def status = 'TBD'
					assetEntityList << [id:asset.id, asset:asset, transitions:html, currentStatus:status]
				}
					
			} else {
				//
				// Generate the Asset Entity List using the Workflow Transitions
				// 
				resultList.each{
					def stateId = 0
					def assetId = it.id
					def assets = AssetEntity.get(assetId)
					def asset = AssetTransition.findByAssetEntity(assets)
					def htmlTd = []
					def maxstate = it.maxstate
	                def transitionStates = jdbcTemplate.queryForList("""SELECT CAST(t.state_to as UNSIGNED INTEGER) AS stateTo FROM asset_transition t
                        WHERE t.asset_entity_id = $assetId AND t.voided = 0 AND ( t.type = 'process' OR t.state_To = $holdId )
                        ORDER BY date_created desc, stateTo DESC LIMIT 1""")
	                if (transitionStates.size()){
	                    stateId = transitionStates[0].stateTo
	                }
	                
	                def transQuery = "FROM AssetTransition WHERE assetEntity = $assetId AND voided = 0"
	                
	                def assetTransitions = AssetTransition.findAll(transQuery)
	                def isHoldNa = assetTransitions.find { it.type == 'boolean' && it.stateTo == holdId.toString() }
	                processTransitionList.each() { trans ->
	                    def cssClass='task_pending'
	                    def transitionId = trans.transId
	                    def stateType = trans.stateType
	                    if(stateId != terminatedId){
	                        def assetTrans = assetTransitions.find { it.stateTo == transitionId.toString() }
	
	                        if(assetTrans?.type == 'boolean' && assetTrans?.isNonApplicable) {
	                            cssClass='asset_na'
	                        } else if(assetTrans?.type == 'boolean' && stateType == 'boolean') {
	                            if(stateId != holdId || isHoldNa?.isNonApplicable){
	                                cssClass='task_done'
	                            } else {
	                                cssClass = (isHoldNa?.holdTimer && isHoldNa?.holdTimer?.getTime() < today.getTime()) ? 'asset_hold_overtime' : 'asset_hold'
	                            }
	                        }
	                        if(stateType != 'boolean' || transitionId == holdId){
	                        	if(stateId == holdId && !isHoldNa?.isNonApplicable){ /* check the current state, if current state = hold , show all steps in yellow */
	                                cssClass = (isHoldNa?.holdTimer && isHoldNa?.holdTimer?.getTime() < today.getTime()) ? 'asset_hold_overtime' : 'asset_hold'
	                            } else if( transitionId <= maxstate  ){
	                            	if(transitionId != holdId && assetTrans?.type != 'boolean'){
	                                    cssClass = "task_done"
	                                } else if( transitionId == holdId ){
	                                    if(isHoldNa){
	                                        cssClass='asset_pending'
	                                    } else {
	                                        cssClass='task_pending'
	                                    }
	                                } else if(assetTrans?.type == 'boolean' && assetTrans?.isNonApplicable){
	                                    cssClass='asset_pending'
	                                }
	                            }
	                        }
	                        if( assetTrans )
	                           cssClass = getRecentChangeStyle( assetTrans, cssClass )
	                    } else {
	                        cssClass='task_term'
	                    }
	                    htmlTd << "<td id=\"${assetId+"_"+trans.transId}\" class=\"$cssClass tranCell\"  >&nbsp;</td>"
	                }
	                assetEntityList << [id: assetId, asset:it, transitions:htmlTd, 
										currentStatus : it.currentStatus ? stateEngineService.getState(workflowCode,it.currentStatus) : ""]
				}
			}
			
			userPreferenceService.loadPreferences("CLIENT_CONSOLE_REFRESH")
			def timeToUpdate = getSession().getAttribute("CLIENT_CONSOLE_REFRESH")
			
			def assetsInView = params.assetsInView && params.assetsInView != "all"? Integer.parseInt(params.assetsInView) : totalAssets
			if ( !params.max ) params.max = assetsInView
			if ( !params.offset ) params.offset = 0
			
			/* Asset Entity attributes for Filters*/
			def attributes = EavEntityAttribute.findAll()?.attribute
			def attributesList = []
			attributes.each{ attribute ->
				def frontendLabel = attribute.frontendLabel
				if( customLabels.contains( frontendLabel ) ){
					frontendLabel = moveEventInstance.project[attribute.attributeCode] ? moveEventInstance.project[attribute.attributeCode] : frontendLabel 
				}
				attributesList << [attributeCode: attribute.attributeCode, frontendLabel:frontendLabel]
			}
			def entities = assetEntityService.entityInfo( project )
			
            return [moveBundleInstance:moveBundleInstance,moveBundleList:moveBundleInstanceList,assetEntityList:assetEntityList,
				column1List:column1List, column2List:column2List,column3List:column3List, column4List:column4List,projectId:project.id, lastPoolTime : lastPoolTime,
                processTransitionList:processTransitionList,column2Value:params.column2,column1Value:params.column1,
                column3Value:params.column3,column4Value:params.column4,timeToUpdate:timeToUpdate ? timeToUpdate.CLIENT_CONSOLE_REFRESH : "never", 
                headerCount:headerCount,browserTest:browserTest, myForm : params.myForm, role : role,
                moveEventInstance:moveEventInstance, moveEventsList:moveEventsList,
                clientConsoleBulkEditHasPermission:RolePermissions.hasPermission("ClientConsoleBulkEdit"),
				clientConsoleCommentHasPermission:RolePermissions.hasPermission("ClientConsoleComment"),
				clientConsoleCheckBoxHasPermission:RolePermissions.hasPermission("ClientConsoleCheckBox"),
				columns:columns, assetsInView:assetsInView, totalAssets:totalAssets, attributesList:attributesList, servers: entities.servers, 
				applications: entities.applications, dbs : entities.dbs, files : entities.files, networks:entities.networks, assetDependency: new AssetDependency(), project:project,
				showAllOption:showAllOption, bundleId:bundleId, staffRoles:taskService.getRolesForStaff(),dependencyType:entities.dependencyType,
				dependencyStatus:entities.dependencyStatus]
	    	
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
        /* user role check*/
		def role = ""
		def subject = SecurityUtils.subject
		if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
			role = "SUPERVISOR"
		} else if(subject.hasRole("EDITOR")){
			role = "EDITOR"
		}
        /*def projectMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = ${params.assetEntity}")
        if(projectMap != null){
			stateVal = stateEngineService.getState(projectInstance.workflowCode,projectMap.currentStateId)
        }*/
        def holdId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Hold"))
        def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
        												"where t.asset_entity_id = $assetId and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId )"+
        												" order by date_created desc, stateTo desc limit 1 ")
		if(transitionStates.size()){
			stateVal = stateEngineService.getState(projectInstance.workflowCode,transitionStates[0].stateTo)
		}
		if(stateVal){
			temp = stateEngineService.getTasks(projectInstance.workflowCode, role, stateVal)
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
	 * @param  : update time 
	 * @return : update time 
	 *---------------------------------------------------------*/
	def setTimePreference = {
        def timer = params.timer
        def updateTime =[]
        def sessionKey = params.prefFor ? params.prefFor : "CLIENT_CONSOLE_REFRESH"
        if(timer){
            userPreferenceService.setPreference( sessionKey, "${timer}" )
        }
        def timeToUpdate = getSession().getAttribute(sessionKey)
        updateTime <<[updateTime:timeToUpdate]
        render updateTime as JSON
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
        	/* user role check*/
			def role = ""
			def subject = SecurityUtils.subject
			if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
				role = "SUPERVISOR"
			} else if(subject.hasRole("EDITOR")){
				role = "EDITOR"
			}
			def holdId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Hold"))
        	def assetList = assetArray.split(",") 
        	assetList.each{ asset->
        		//def projectAssetMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = $asset")
        		def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
    															"where t.asset_entity_id = $asset and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId )  "+
    															"order by date_created desc, stateTo desc limit 1 ")
        		if(transitionStates.size()){
        			stateVal = stateEngineService.getState(projectInstance.workflowCode,transitionStates[0].stateTo)
                    temp = stateEngineService.getTasks(projectInstance.workflowCode, role ,stateVal)
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
        /* user role check*/
		def role = ""
		def subject = SecurityUtils.subject
		if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
			role = "SUPERVISOR"
		} else if(subject.hasRole("EDITOR")){
			role = "EDITOR"
		}
        assetEnt.each{
	        def bundle = it.moveBundle
	        def principal = SecurityUtils.subject.principal
	        def loginUser = UserLogin.findByUsername(principal)
	        def team = it.sourceTeamMt
			     
	        def workflow = workflowService.createTransition(bundle.workflowCode, role ,params.taskList,it,bundle,loginUser,team,params.enterNote)
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
    
        redirect(action:'list',params:["projectId":session.CURR_PROJ.CURR_PROJ,"moveBundle":params.moveBundle])
			
	       
    }
	
	/**
	 * Returns JSON data for all assets displayed in the Asset Tracker including recent transactions
	 * @author: Lokanada Reddy
	 * @param : MoveBundle, application,appSme,appOwner
	 * @return: AssetEntity object with recent transactions
	 **/
	def getTransitions = {
		def workFlowCode
		def project = securityService.getUserCurrentProject()
		
		def bundleId = params.moveBundle
		if (bundleId == "all") {
		   workFlowCode = project.workflowCode
		} else if (bundleId != null && bundleId.isNumber() ) {
		   def moveBundle = MoveBundle.get(bundleId)
		   if (moveBundle) {
			   def moveEvent = moveBundle.moveEvent
			   if (moveEvent.project != project) {
				// TODO : need to handle error return for Ajax call and put username into the error log
			   	log.error "The project associated with moveBundle Id [${bundleId}] did not match the user's current project"
				return
			   }
		   } else {
		   		log.error "Unable to load moveBundle for id [${bundleId}]"
				// TODO : need to handle error return for Ajax call
				return 
		   }
		   workFlowCode = moveBundle.workflowCode
		} else {
			log.error "moveBundle id was not properly passed to method"
			// TODO : need to handle error return for Ajax call
			return
		}
		
		def moveEventId = params.moveEvent
		def assetEntityList = []
		def assetEntityAndCommentList = []
		DateFormat formatter = new SimpleDateFormat("hh:mm a");
		
		if ( moveEventId && moveEventId.isNumber() ) {
			def moveEvent = MoveEvent.findById(moveEventId)
			if ( moveEvent?.project != project ) {
				log.error "The moveEvent Id param [${moveEventId}] is not associated with the user's current project"
				// TODO : Handle error response from Ajax call
				return
			}
			def bundles
	        if( bundleId == 'all' ){
				def moveBundlesList = MoveBundle.findAll("from MoveBundle mb where mb.moveEvent = ? order by mb.name asc",[moveEvent])
	        	//userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
				if(moveBundlesList.size() > 0) {
					bundles = (moveBundlesList.id).toString().replace("[","(").replace("]",")")
				} else {
					log.error "There were no bundles for MoveEvent Id [${moveEventId}]"
					// TODO : handle error return from Ajax call
					return
				}
	        } else { 
				bundles = "(${bundleId})"
	        }
			
			def lastPoolTime = params.lastPoolTime
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def today = GormUtil.convertInToGMT( "now", tzId );
			def currentPoolTime = new java.sql.Timestamp(today.getTime())
			// TODO : LOK - Why are we persisting the LAST_POOL_TIME since it is passed as an argument in the view
			getSession().setAttribute("LAST_POOL_TIME",currentPoolTime)
			
			def processTransitions= stateEngineService.getTasks(workFlowCode, "TASK_ID")
			/* user role check*/
			def role = ""
			def subject = SecurityUtils.subject 
			if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
				 role = "SUPERVISOR"
			} else if(subject.hasRole("EDITOR")){
				 role = "EDITOR"
			}
			def holdId = Integer.parseInt(stateEngineService.getStateId(workFlowCode,"Hold"))
			def releaseId = Integer.parseInt(stateEngineService.getStateId(workFlowCode,"Release"))
			def reRackId = Integer.parseInt(stateEngineService.getStateId(workFlowCode,"Reracked"))
			def terminatedId = Integer.parseInt(stateEngineService.getStateId(workFlowCode,"Terminated"))
			def columns = userPreferenceService.setAssetTrackingPreference(null, null, null, null)
			def lastUpdated
			
			// get the asset list
			def assetList= pmoAssetTrackingService.getAssetsForPmoUpdate( moveEvent.project.id, bundles, params, lastPoolTime, currentPoolTime)
			assetList.each{
				def stateId = 0
				def assetId = it.id
				def tdId = []
				
				// TODO : LOK - what does this maxstate property do?
				def maxstate = it.maxstate
				
				// This code was refetching the AssetEntity instead of using what being returned from getAssetsForPmoUpdate 
				// def assetEntity = AssetEntity.get(assetId)
				def assetEntity = it
				
				if (project.runbookOn==1) {
					// Build TD data by Task
					tdId = pmoAssetTrackingService.getTransitionRowRb(assetEntity, bundleId)
				} else {
					// Build TD data by Workflow 
					def transitionStates = jdbcTemplate.queryForList("SELECT cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
						"WHERE t.asset_entity_id = $assetId and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId ) "+
						"ORDER BY date_created desc, stateTo desc limit 1 ")
					if (transitionStates.size()){
						stateId = transitionStates[0].stateTo
					}
	                
	                def transQuery = "from AssetTransition where assetEntity = $assetId and voided = 0 "
	                
	                def assetTransitions = AssetTransition.findAll(transQuery)
	                def isHoldNa = assetTransitions.find { it.type == 'boolean' && it.stateTo == holdId.toString() }
					
					processTransitions.each() { trans ->
						def cssClass='task_pending'
	                	def transitionId = Integer.parseInt(trans)
						def stateType = stateEngineService.getStateType( workFlowCode, 
										stateEngineService.getState(workFlowCode, transitionId))
	                    if(stateId != terminatedId){
	                        def assetTrans = assetTransitions.find { it.stateTo == transitionId.toString() }
	                        
	                        if(assetTrans?.type == 'boolean' && assetTrans?.isNonApplicable) {
								cssClass='asset_na'
	                        } else if(assetTrans?.type == 'boolean' && stateType == 'boolean') {
								if(stateId != holdId || isHoldNa?.isNonApplicable){
									cssClass='task_done'
								} else {
									cssClass = (isHoldNa?.holdTimer && isHoldNa?.holdTimer?.getTime() < today.getTime()) ? 'asset_hold_overtime' : 'asset_hold' 
								}
							}
							if(stateType != 'boolean' || transitionId == holdId){
								if(stateId == holdId && !isHoldNa?.isNonApplicable){ /* check the current state, if current state = hold , show all steps in yellow */
									cssClass = (isHoldNa?.holdTimer && isHoldNa?.holdTimer?.getTime() < today.getTime()) ? 'asset_hold_overtime' : 'asset_hold'
								} else if( transitionId <= maxstate  ){
	                                if(transitionId != holdId && assetTrans?.type != 'boolean'){ 
	        							cssClass = "task_done"
	        						} else if( transitionId == holdId ){
										if(isHoldNa){
											cssClass='asset_pending'
										} else {
											cssClass='task_pending'
										}
	                                } else if(assetTrans?.type == 'boolean' && assetTrans.isNonApplicable){
										cssClass='asset_pending'
									}
								}
							}
							if( assetTrans )
		                           cssClass = getRecentChangeStyle( assetTrans, cssClass )
	                    } else {
	                    	cssClass='task_term'
	                    }
						tdId << [id:"${assetId+"_"+trans}", cssClass:cssClass]
					}
				} // if (project.runbookOn) 
				// log.info "getTransitions: updated=[${it.updated}]}" 
				
				try {
					lastUpdated = formatter.format( TimeUtil.convertInToUserTZ(it.updated, tzId) )
				} catch (e) {
					//log.error "getTransitions: formatting updated (${it.updated}) failed, assetId=$assetId"
					lastUpdated = ''
				}
				
				assetEntityList << [id: assetId, column1value:it."${columns.column1.field}" ? columns.column1.field != "currentStatus" ? it."${columns.column1.field}" : stateEngineService.getState(workFlowCode,it."${columns.column1.field}") : "&nbsp;",
					column2value:it."${columns.column2.field}" ? columns.column2.field != "currentStatus" ? it."${columns.column2.field}" : stateEngineService.getState(workFlowCode,it."${columns.column2.field}") : "&nbsp;",
					column3value:it."${columns.column3.field}" ? columns.column3.field != "currentStatus" ? it."${columns.column3.field}" : stateEngineService.getState(workFlowCode,it."${columns.column3.field}") : "&nbsp;",
					column4value:it."${columns.column4.field}" ? columns.column4.field != "currentStatus" ? it."${columns.column4.field}" : stateEngineService.getState(workFlowCode,it."${columns.column4.field}") : "&nbsp;",
					tdId:tdId, lastUpdated:lastUpdated]
			}
			
			def assetCommentsList = []
			def assetsList = AssetEntity.findAll("from AssetEntity where moveBundle in ${bundles}")
			assetsList.each {
				def checkIssueType = AssetComment.find("from AssetComment where assetEntity=$it.id and commentType='issue' and isResolved = 0"+
													" and date_created between SUBTIME('$currentPoolTime','00:10:00') and CURRENT_TIMESTAMP ")
				if ( checkIssueType ) {
					assetCommentsList << ["assetEntityId":it.id, "type":"db_table_red.png"]
				} else {
					checkIssueType = AssetComment.find("from AssetComment where assetEntity=$it.id and date_created between SUBTIME('$currentPoolTime','00:10:00') and CURRENT_TIMESTAMP")
					if ( checkIssueType ) {
						assetCommentsList << ["assetEntityId":it.id, "type":"db_table_bold.png"]
					} /*else if( role ){
						assetCommentsList << ["assetEntityId":it.id, "type":"database_table_light.png"]
					}*/
				}
			}
			assetEntityAndCommentList << [ assetEntityList: assetEntityList, assetCommentsList: assetCommentsList, lastPoolTime : currentPoolTime.toString(), check: RolePermissions.hasPermission("ClientConsoleCheckBox") ]
		}
    	render assetEntityAndCommentList as JSON
    }
	
	
	/* -----------------------------------------------------
	 * get header name details for svg.
	 * @author: Mallikarjun
	 * @return: count of tasks
	 *----------------------------------------------------*/
	def getHeaderNames(moveBundleId, workFlowCode){
		def tempTransitions = []
		def processTransitions= stateEngineService.getTasks(workFlowCode, "TASK_ID")
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
			def processTransition = stateEngineService.getStateLabel(workFlowCode,it)
			def fillColor = stateEngineService.getHeaderColor(workFlowCode, stateEngineService.getState(workFlowCode,it))
			if(count == 0){
				svgHeaderFile.append("<tspan fill='$fillColor' id='$it' onclick='parent.bulkTransitionsByHeader(this.id)'>${processTransition}</tspan>")
			} else {
				svgHeaderFile.append("<tspan x='-11' dy='22' fill='$fillColor' id='$it' onclick='parent.bulkTransitionsByHeader(this.id)'>${processTransition}</tspan>")
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
		def f = ApplicationHolder.application.parentContext.getResource("templates/headerSvg_${moveBundleId}.svg").getFile()
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
	/* -----------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Transition td id 
	 * @return : return the list for menu
	 *----------------------------------------------------*/
	def getMenuList = {
		def id = params.id
		def ids = id.split("_")
		def transId = ids[1]
		def assetEntity = AssetEntity.findById( ids[0] ) 
		def state = stateEngineService.getState( assetEntity.moveBundle.workflowCode, Integer.parseInt( ids[1] ) )
		def stateType = stateEngineService.getStateType( assetEntity.moveBundle.workflowCode, state )
		def situation = ""
		def assetTransition
		def menuOptions = ""
		assetTransition = AssetTransition.find("from AssetTransition t where t.assetEntity = $assetEntity.id and t.stateTo = $transId and t.type ='boolean' and t.voided = 0 ")
		if (assetTransition){
			if(assetTransition.isNonApplicable != 0){
				situation = "NA"
			} else {
				situation = "done"
			}
		}
		log.info "getMenuList: assetTransition=${assetTransition?'yes':'no'}, situation=${situation}, state=${state}, stateType=$stateType} "
		menuOptions = pmoAssetTrackingService.constructMenuOptions( state, assetEntity, situation, stateType )
		
		render menuOptions
	}
	/* -----------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Entity and to toState
	 * @return : Create a transition as selected by User and will return the tannsition asset row details
	 *----------------------------------------------------*/
	def createTransitionForNA = {
		def loginUser = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()

		def type = params.type
		
		// The asset id and stateToId are embedded in the actionId param as assetId_stateToId so we need to parse it appart 
		def actionId = params.actionId
		def actions = actionId.split("_")
		def assetEntity = AssetEntity.findById(actions[0])
		def stateToId = Integer.parseInt(actions[1])

		if (assetEntity.project.id != project.id ) {
			log.error "createTransitionForNA: User($loginUser) attempted to access asset(${assetEntity.id}) that was not associated with current project ${project}"
			// TODO : handle faild call
			return 
		}		
		
		// Now we'll use the movebundle workflow unless the user has selected 'all' bundles then we'll use the project level workflow
		def workFlowCode = (params.bundle=="all") ? assetEntity.project.workflowCode : assetEntity.moveBundle.workflowCode

		def stateTo = stateEngineService.getState( workFlowCode, stateToId )
		
		log.info "createTransitionForNA: starting to create transition for asset(${assetEntity} for state(${stateTo}/${stateToId}"
		
		def role = ""
		def subject = SecurityUtils.subject
		
		if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
			role = "SUPERVISOR"
		} else if(subject.hasRole("EDITOR")){
			role = "EDITOR"
		}
		
		// TODO : cleanup : It doesn't look like assetTransitionQuery is used in this code
		def assetTransitionQuery = "from AssetTransition t where t.assetEntity = ${assetEntity.id} and t.voided = 0"
		def comment = ""
		if (stateTo == AssetCommentStatus.TERMINATED ){
			comment = "Asset has been Terminated"
		}
		def message = ""
		if (role){
			message = pmoAssetTrackingService.createBulkTransition(type, assetEntity, stateTo, role, loginUser, comment, params.bundle)
		} else {
			message = " You don't have permission to create transition"
		}
		def tdId = pmoAssetTrackingService.getTransitionRow( assetEntity, stateTo,params.bundle )
		tdId.add(message:message)
		render tdId as JSON
	}

	/*
	 * Validate and return the message to the user. 
	 */
	def getAssetsCountForBulkTransition = {
		def message = "" 
		def transId = params.transId
		def moveEvent = MoveEvent.findById( params.eventId )
		def type = params.type
		def workFlowCode 
		if(params.bundleId!="all"){
			def moveBundleInstance = MoveBundle.findById(params.bundleId)
			workFlowCode = moveBundleInstance.workflowCode
		}else{
		    workFlowCode = moveEvent.project.workflowCode
		}
		
		def stateTo = stateEngineService.getState( workFlowCode, Integer.parseInt(transId) )
		def stateType = stateEngineService.getStateType(workFlowCode, stateTo)
		def holdId = Integer.parseInt(stateEngineService.getStateId(workFlowCode,"Hold"))

		def assetEntityList = pmoAssetTrackingService.getAssetEntityListForBulkEdit( params )
		
		def totalAssets = assetEntityList.size()
		def possibleAssets = 0
		
		//Set possibleAssets = 0 when stateTo is Hold
		if(stateTo == "Hold"){
			message = "Transition not allowed"
			render message 
			return
		}
		
		// terminate if type is not appplicable 
		if(stateType != "boolean" && (type == "NA" || type == "pending")){
			message = " $type not allowed for Process steps"
			render message 
			return
		}
		if(stateType == "boolean" && type == "void" ){
			message = " Undo not allowed for Boolean steps"
			render message 
			return
		}
		// send the response message when state to as Ready or state type as boolean
		if(stateType == "boolean" && stateTo != "Hold" && type != "void" ) {
			possibleAssets = totalAssets
			message = "Set the $possibleAssets out of $totalAssets assets to $stateTo to $type ?"
			render message 
			return
		}

		def subject = SecurityUtils.subject
		def role = ""
		if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
			role = "SUPERVISOR"
		} else if(subject.hasRole("EDITOR")){
			role = "EDITOR"
		}
		def assetIds = assetEntityList.id.toString().replace("[","(").replace("]",")")
		
		def currentTransitions = jdbcTemplate.queryForList("""SELECT * FROM ( select cast(t.state_to as UNSIGNED INTEGER) as stateTo, t.asset_entity_id as assetId from asset_transition t 
															where t.asset_entity_id in ${assetIds} and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId )
															order by date_created desc, stateTo desc ) tr group by tr.assetId """)
		if(type != "void"){
			assetEntityList.each{ asset ->
				def currentTransition = currentTransitions.find { it.assetId == asset.id && it.stateTo != holdId }
				if( currentTransition ){
					def currentStateId = currentTransition.stateTo
					def currentState = stateEngineService.getState( workFlowCode, currentTransition.stateTo)
					def validate = stateEngineService.canDoTask( workFlowCode, role, currentState, stateTo  ) 
					if(validate || stateTo =="Ready"){
						possibleAssets += 1
					}
				} else if( !currentTransitions.find { it.assetId == asset.id && it.stateTo == holdId } && stateTo =="Ready"){
					possibleAssets += 1
				}
			}
			message = "Set the $possibleAssets out of $totalAssets assets to $stateTo to $type ?"
		} else {
			def undoAssets = 0
			assetEntityList.each{ asset ->
				def currentTransition = currentTransitions.find { it.assetId == asset.id && it.stateTo != holdId && it.stateTo >= Integer.parseInt(transId) }		
				if( currentTransition ){
					undoAssets += 1
				}
			}
			message = "Undo the $undoAssets tasks and any dependent (workflow) transitions. Are you sure?"
		}
		
		render message 
	}
	/*
	 * Bulk edit of transitions by letting the project manager click on column head to transition the displayed assets to that step.
	 * @params : transition Id, transition type, moveBundle, moveEvent
	 */
	def doBulkTransitionsByHeader = {
				
		def transId = params.transId
		def moveEvent = MoveEvent.findById( params.eventId )
		def type = params.type
		def workFlowCode
		if(params.bundleId!="all"){
			def moveBundleInstance = MoveBundle.findById(params.bundleId)
			workFlowCode = moveBundleInstance.workflowCode
		}else{
			workFlowCode = moveEvent.project.workflowCode
		}
		
		def stateTo = stateEngineService.getState( workFlowCode, Integer.parseInt(transId) )
		
		if(stateTo == "Hold"){
			render ""
			return
		}
		
		def stateType = stateEngineService.getStateType(workFlowCode, stateTo)
		def holdId = Integer.parseInt(stateEngineService.getStateId(workFlowCode,"Hold"))
		
		def assetEntityList = pmoAssetTrackingService.getAssetEntityListForBulkEdit( params )
		
		def subject = SecurityUtils.subject
		def role = ""
		if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
			role = "SUPERVISOR"
		} else if(subject.hasRole("EDITOR")){
			role = "EDITOR"
		}
		def loginUser = UserLogin.findByUsername(subject.principal)
		def assetIds = assetEntityList.id.toString().replace("[","(").replace("]",")")
		def currentTransitions = jdbcTemplate.queryForList("""SELECT * FROM ( select cast(t.state_to as UNSIGNED INTEGER) as stateTo, t.asset_entity_id as assetId from asset_transition t 
															where t.asset_entity_id in ${assetIds} and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId )
															order by date_created desc, stateTo desc ) tr group by tr.assetId """)
		assetEntityList.each{ asset ->
		
			def currentTransition = currentTransitions.find { it.assetId == asset.id && it.stateTo != holdId }
	
			if( currentTransition && type != "void"){
				def currentStateId = currentTransition.stateTo
				def currentState = stateEngineService.getState( workFlowCode, currentStateId)
				def validate = stateEngineService.canDoTask( workFlowCode, role, currentState, stateTo  ) 
				if(validate || stateTo =="Ready"){
					pmoAssetTrackingService.createBulkTransition( type, asset, stateTo, role, loginUser, "",params.bundleId)
				}
			} else if( ( !currentTransitions.find { it.assetId == asset.id && it.stateTo == holdId } && stateTo =="Ready") || 
					stateType == "boolean" || type == "void") {
				pmoAssetTrackingService.createBulkTransition( type, asset, stateTo, role, loginUser, "",params.bundleId)
			}
		}
		
		render ""
	}
	
	/**
	 * Returns the appropriate CSS class for a task if done that shows the progressive darkening over time
	 * @param Entity that the style is for (assuming this is the transition object TBD )
	 * @param cssClass that is being evaluated
	 * @return String cssClass to use in display
	 */
	// TODO - getRecentChangeStyle: move to TaskService
	// TODO - getRecentChangeStyle: Runbook - will need to revamp this to work without transitions
	private getRecentChangeStyle(def entity, def cssClass) {
        def changedClass = cssClass
        if(cssClass == "task_done") {
            def createdTime = entity?.dateCreated?.getTime()
            def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ                  
            def currentTime = GormUtil.convertInToGMT( "now", tzId ).getTime()
            Integer minutes
            if(createdTime) {
                minutes = (currentTime - createdTime) / 1000
            }
            if( minutes != null ) {
                if(minutes < 120) {
                    changedClass = "task_done2"
                } else if(minutes > 120 && minutes < 330) {
                    changedClass = "task_done5" 
                }
            }
        }
        return changedClass
	}
	/*
	*  Return the list of current status Transitions 
	*/
	def getCurrentStatusOptions = {
		def moveEventId = params.moveEventId
		def columnName = "currentStatus"
		def moveEvent = MoveEvent.get( moveEventId )
		def moveBundlesList = MoveBundle.findAllByMoveEvent( moveEvent )
		def bundles = moveBundlesList.id.toString().replace("[","(").replace("]",")")
		def moveBundleInstance
		def workflowCode    
		
		if (params.bundle == "all"){
		    workflowCode = moveEvent.project.workflowCode
		} else {
			moveBundleInstance = MoveBundle.get(params.bundle) 
			workflowCode = moveBundleInstance.workflowCode
		}
		
		def tempList = AssetEntity.executeQuery("""SELECT DISTINCT ae.${columnName} , count(ae.id) FROM AssetEntity ae
			WHERE  ae.moveBundle.id IN ${bundles} GROUP BY ae.${columnName} ORDER BY ae.${columnName}""")
		
		def columnList = pmoAssetTrackingService.splitFilterExpansion( tempList, columnName, workflowCode )
		render columnList as JSON
	}
	/*
	 * Add a user preference "BULK_WARNING" with a time stamp+24hrs. If the user re-enters bulk edit mode, and BulkWarning time is > now, don't show the popup warning.
	 */
	def setBulkWarning = {
		def dateNow = GormUtil.convertInToGMT( "now", "EDT" )
		userPreferenceService.loadPreferences("BULK_WARNING")
		def bulkWarning = getSession().getAttribute("BULK_WARNING")?.BULK_WARNING
		def status = "true"
		if( !bulkWarning ){
			status = "false"
			userPreferenceService.setPreference("BULK_WARNING", "${dateNow.getTime()+86400000}")
		} else {
			def bulkWarningTime = Long.parseLong( bulkWarning )
			if( bulkWarningTime < dateNow.getTime() ){
				status = "false"
				userPreferenceService.setPreference("BULK_WARNING", "${dateNow.getTime()+86400000}")
			}
		}
		render status
	}
	
	def moveBundleList ={
		def moveEventId = params.moveEvent
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def moveEventInstance = MoveEvent.findByIdAndProject(moveEventId ,(Project.get(projectId)))
		def moveBundlesList = moveEventInstance.moveBundles
		def bundleList = []
		def moveBundles = MoveBundle.findAllByMoveEvent(moveEventInstance)
		Set uniqueWorkflowCodes = moveBundles?.workflowCode
		def showAllOption = uniqueWorkflowCodes.size() == 1 && moveBundles.size() > 1
		moveBundles.each{moveEvent->
			bundleList << [id:moveEvent.id, name:moveEvent.name,showAllOption:showAllOption]
		}
		bundleList.sort { it.name }
		render bundleList as JSON
	}
}
