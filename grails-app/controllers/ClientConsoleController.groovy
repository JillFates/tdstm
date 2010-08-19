import grails.converters.JSON
import org.jsecurity.SecurityUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import com.tdssrc.grails.GormUtil
import javax.servlet.http.HttpSession

class ClientConsoleController {
	def stateEngineService
	def userPreferenceService
	def workflowService
    def jdbcTemplate
	def pmoAssetTrackingService
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
    	def headerCount = getHeaderNames()
    	def browserTest = request.getHeader("User-Agent").contains("MSIE")
        def projectId=params.projectId
        def bundleId = params.moveBundle
		def moveEventId = params.moveEvent
        def moveBundleInstance
		def moveEventInstance
		//def projectMap
        def stateVal
        def taskVal
        def check 
        def projectInstance = Project.findById( projectId )
		def moveEventsList = MoveEvent.findAll("from MoveEvent me where me.project = ? order by me.name asc",[projectInstance])
		def columns = userPreferenceService.setAssetTrackingPreference(params.column1Attribute, params.column2Attribute, params.column3Attribute, params.column4Attribute)
		
		def defalutBundleId = getSession().getAttribute("CURR_BUNDLE")?.CURR_BUNDLE
		// set Pmo assetsInView into current session when changed
		if ( !params.assetsInView ){ 
			params.assetsInView = getSession().getAttribute("PMO_ASSETS_INVIEW")
		} else {
			getSession().setAttribute("PMO_ASSETS_INVIEW",params.assetsInView)
		}
    	
		if(moveEventId){
			userPreferenceService.setPreference( "MOVE_EVENT", "${moveEventId}" )
            moveEventInstance = MoveEvent.findById(moveEventId)
		} else {
            userPreferenceService.loadPreferences("MOVE_EVENT")
            def defaultEvent = getSession().getAttribute("MOVE_EVENT")
            if(defaultEvent.MOVE_EVENT){
            	moveEventInstance = MoveEvent.findById(defaultEvent.MOVE_EVENT)
            	if( moveEventInstance?.project?.id != Integer.parseInt(projectId) ){
            		moveEventInstance = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[projectInstance])
            	}
            } else {
            	moveEventInstance = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[projectInstance])
            }
        }
        
    	if( moveEventInstance ){
    		def bundles
    		def moveBundleInstanceList = MoveBundle.findAll("from MoveBundle mb where mb.moveEvent = ? order by mb.name asc",[moveEventInstance])
	        if( bundleId && bundleId != "all"  ){
	        	userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
	            moveBundleInstance = MoveBundle.findById(bundleId)
				bundles = "("+bundleId+")"
	        } else if(moveBundleInstanceList.size() > 0){
	        	if(bundleId == "all"){
	        		userPreferenceService.removePreference( "CURR_BUNDLE" )
					bundles = (moveBundleInstanceList.id).toString().replace("[","(").replace("]",")")
	        	} else if(defalutBundleId){ // check to see if there is any pref bundle exist
	        		def defalutBundle = MoveBundle.get(defalutBundleId)
	        		if(defalutBundle?.moveEvent?.id != moveEventInstance.id){ // check to see if preff bundle belongs to current event , if not remove the pref bundle
	        			userPreferenceService.removePreference( "CURR_BUNDLE" )	
						bundles = (moveBundleInstanceList.id).toString().replace("[","(").replace("]",")")
	        		} else {
	        			moveBundleInstance = defalutBundle
	        			bundles = "("+defalutBundleId+")"
	        		}
	        	} else { // use all the bundles if pref bundle not exist
	        		bundles = (moveBundleInstanceList.id).toString().replace("[","(").replace("]",")")
	        	}
	        	
	        }
    		def resultList
    		def column1List
			def column2List
			def column3List
			def column4List
			def totalAssets = 0
			if(bundles){
				/*-------------get filter details----------------*/
				
				def temp1List = AssetEntity.executeQuery("""select distinct ae.${columns?.column1.field} , count(ae.id) from AssetEntity 
																ae where  ae.moveBundle.id in ${bundles} group by ae.${columns?.column1.field} order by ae.${columns?.column1.field}""")
				column1List = splitFilterExpansion( temp1List )
				
				def temp2List = AssetEntity.executeQuery("""select distinct ae.${columns?.column2.field} , count(ae.id) from AssetEntity 
																ae where  ae.moveBundle.id in ${bundles} group by ae.${columns?.column2.field} order by ae.${columns?.column2.field}""")
				column2List = splitFilterExpansion( temp2List )
				
				def temp3List = AssetEntity.executeQuery("""select distinct ae.${columns?.column3.field} , count(ae.id) from AssetEntity 
																ae where  ae.moveBundle.id in ${bundles} group by ae.${columns?.column3.field} order by ae.${columns?.column3.field}""")
				column3List = splitFilterExpansion( temp3List )
				
				def temp4List = AssetEntity.executeQuery("""select distinct ae.${columns?.column4.field} , count(ae.id) from AssetEntity 
																ae where  ae.moveBundle.id in ${bundles} group by ae.${columns?.column4.field} order by ae.${columns?.column4.field}""")
				column4List = splitFilterExpansion( temp4List )
                
				/*-------------get asset details----------------*/
				def returnValue = pmoAssetTrackingService.getAssetsForListView( projectId, bundles, columns, params )
				resultList = returnValue[0]
				
				totalAssets = returnValue[1]
			}
    		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def today = GormUtil.convertInToGMT( "now", tzId );
			def lastPoolTime = new java.sql.Timestamp(today.getTime())
			def assetEntityList=[]
			def processTransitionList=[]
			def tempTransitions = []
			def processTransitions= stateEngineService.getTasks(projectInstance.workflowCode, "TASK_ID")
			processTransitions.each{
				tempTransitions <<Integer.parseInt(it)
			}
			tempTransitions.sort().each{
				def processTransition = stateEngineService.getState(projectInstance.workflowCode,it)
				def fillColor = stateEngineService.getHeaderColor(projectInstance.workflowCode, stateEngineService.getState(projectInstance.workflowCode,it))
				def transId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,processTransition))
				processTransitionList<<[header:stateEngineService.getStateLabel(projectInstance.workflowCode,it),
										transId:transId,
										fillColor:fillColor,
										stateType:stateEngineService.getStateType( projectInstance.workflowCode, 
                                                        stateEngineService.getState(projectInstance.workflowCode, transId))]
			}
			/* user role check*/
			def role = ""
			def subject = SecurityUtils.subject
			if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
				role = "SUPERVISOR"
			} else if(subject.hasRole("MANAGER")){
				role = "MANAGER"
			}
			def holdId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Hold"))
			def releaseId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Release"))
			def reRackId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Reracked"))
			def terminatedId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Terminated"))
			resultList.each{
				def stateId = 0
				def assetId = it.id
				def htmlTd = []
				def maxstate = it.maxstate
                def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
                                                                "where t.asset_entity_id = $assetId and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId ) "+
                                                                "order by date_created desc, stateTo desc limit 1 ")
                if(transitionStates.size()){
                    stateId = transitionStates[0].stateTo
                }
                if(stateId == 0){
                    check = true
                } else if((stateId > holdId && stateId < releaseId) || (stateId > reRackId)){
                    stateVal = stateEngineService.getState(projectInstance.workflowCode,stateId)
                    taskVal = stateEngineService.getTasks(projectInstance.workflowCode, role ,stateVal)
                    if(taskVal.size() == 0){
                        check = false
                    }else{
                        check = true
                    }
                }else{
                    check = false
                }
                
                def transQuery = "from AssetTransition where assetEntity = $assetId and voided = 0"
                
                def assetTransitions = AssetTransition.findAll(transQuery)
                def isHoldNa = assetTransitions.find { it.type == 'boolean' && it.stateTo == holdId.toString() }

                processTransitionList.each() { trans ->
                    def cssClass='task_pending'
                    def transitionId = trans.transId
                    def stateType = trans.stateType
                    if(stateId != terminatedId){
                        def assetTrans = assetTransitions.find { it.stateTo == transitionId.toString() }

                        if(assetTrans?.type == 'boolean' && assetTrans?.isNonApplicable) {
                            cssClass='asset_pending'
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
                assetEntityList << [id: assetId, asset:it,transitions:htmlTd,checkVal:check]
			}

			userPreferenceService.loadPreferences("CLIENT_CONSOLE_REFRESH")
			def timeToUpdate = getSession().getAttribute("CLIENT_CONSOLE_REFRESH")
			
			def assetsInView = params.assetsInView && params.assetsInView != "all"? Integer.parseInt(params.assetsInView) : totalAssets
			if ( !params.max ) params.max = assetsInView
			if ( !params.offset ) params.offset = 0
            return [moveBundleInstance:moveBundleInstance,moveBundleInstanceList:moveBundleInstanceList,assetEntityList:assetEntityList,
				column1List:column1List, column2List:column2List,column3List:column3List, column4List:column4List,projectId:projectId, lastPoolTime : lastPoolTime,
                processTransitionList:processTransitionList,projectId:projectId,column2Value:params.column2,column1Value:params.column1,
                column3Value:params.column3,column4Value:params.column4,timeToUpdate:timeToUpdate ? timeToUpdate.CLIENT_CONSOLE_REFRESH : "never", 
                headerCount:headerCount,browserTest:browserTest, myForm : params.myForm, role : role,
                moveEventInstance:moveEventInstance, moveEventsList:moveEventsList,
                isAdmin:subject.hasRole("ADMIN"), isManager:subject.hasRole("MANAGER"), isProjManager:subject.hasRole("PROJ_MGR"),
				columns:columns, assetsInView:assetsInView, totalAssets:totalAssets ]
    	
        } else {
    		flash.message = "Please create move event and bundle to view PMO Dashboard"
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
        /* user role check*/
		def role = ""
		def subject = SecurityUtils.subject
		if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
			role = "SUPERVISOR"
		} else if(subject.hasRole("MANAGER")){
			role = "MANAGER"
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
        if(timer){
            userPreferenceService.setPreference( "CLIENT_CONSOLE_REFRESH", "${timer}" )
        }
        def timeToUpdate = getSession().getAttribute("CLIENT_CONSOLE_REFRESH")
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
			} else if(subject.hasRole("MANAGER")){
				role = "MANAGER"
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
		} else if(subject.hasRole("MANAGER")){
			role = "MANAGER"
		}
        assetEnt.each{
	        def bundle = it.moveBundle
	        def principal = SecurityUtils.subject.principal
	        def loginUser = UserLogin.findByUsername(principal)
	        def team = it.sourceTeam
			     
	        def workflow = workflowService.createTransition(projectInstance.workflowCode, role ,params.taskList,it,bundle,loginUser,team,params.enterNote)
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
		def moveEventId = params.moveEvent
		def assetEntityList = []
		def assetEntityAndCommentList = []
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		if (moveEventId) {
			def bundles
			def moveEvent = MoveEvent.findById(moveEventId)
    		def moveBundlesList = MoveBundle.findAll("from MoveBundle mb where mb.moveEvent = ? order by mb.name asc",[moveEvent])
	        if( bundleId && bundleId != 'all' ){
	        	//userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
				bundles = "("+bundleId+")"
	        } else if(moveBundlesList.size() > 0){
	        	bundles = (moveBundlesList.id).toString().replace("[","(").replace("]",")")
	        }
			def lastPoolTime = params.lastPoolTime
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def today = GormUtil.convertInToGMT( "now", tzId );
			def currentPoolTime = new java.sql.Timestamp(today.getTime())
			getSession().setAttribute("LAST_POOL_TIME",currentPoolTime)
			
			// get the assets list
			def resultList= pmoAssetTrackingService.getAssetsForPmoUpdate( moveEvent.project.id, bundles, params, lastPoolTime, currentPoolTime)//jdbcTemplate.queryForList(query.toString())
			
			def processTransitions= stateEngineService.getTasks(projectInstance.workflowCode, "TASK_ID")
			/* user role check*/
			def role = ""
			def subject = SecurityUtils.subject 
			if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
				 role = "SUPERVISOR"
			} else if(subject.hasRole("MANAGER")){
				 role = "MANAGER"
			}
			def holdId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Hold"))
			def releaseId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Release"))
			def reRackId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Reracked"))
			def terminatedId = Integer.parseInt(stateEngineService.getStateId(projectInstance.workflowCode,"Terminated"))
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
																"where t.asset_entity_id = $assetId and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId ) "+
																"order by date_created desc, stateTo desc limit 1 ")
				if(transitionStates.size()){
					stateId = transitionStates[0].stateTo
				}
				if(stateId == 0){
					check = true
				} else if((stateId > holdId && stateId < releaseId) || (stateId > reRackId)){
					def stateVal = stateEngineService.getState(projectInstance.workflowCode,stateId)
					def taskVal = stateEngineService.getTasks(projectInstance.workflowCode,role,stateVal)
					if(taskVal.size() == 0){
						check = false
					}else{
						check = true
					}
				}else{
					check = false
				}
                
                def transQuery = "from AssetTransition where assetEntity = $assetId and voided = 0 "
                
                def assetTransitions = AssetTransition.findAll(transQuery)
                def isHoldNa = assetTransitions.find { it.type == 'boolean' && it.stateTo == holdId.toString() }
				
				processTransitions.each() { trans ->
					def cssClass='task_pending'
                	def transitionId = Integer.parseInt(trans)
					def stateType = stateEngineService.getStateType( projectInstance.workflowCode, 
									stateEngineService.getState(projectInstance.workflowCode, transitionId))
                    if(stateId != terminatedId){
                        def assetTrans = assetTransitions.find { it.stateTo == transitionId.toString() }
                        
                        if(assetTrans?.type == 'boolean' && assetTrans?.isNonApplicable) {
							cssClass='asset_pending'
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
				assetEntityList << [id: assetId, application:it.application ? it.application : "&nbsp;",appOwner:it.appOwner ? it.appOwner : "&nbsp;", 
									appSme:it.appSme ? it.appSme : "&nbsp;",assetName:it.assetName ? it.assetName :"&nbsp;",tdId:tdId,
									check:check]
			}
			
			def assetCommentsList = []
			def assetsList = AssetEntity.findAll("from AssetEntity where moveBundle in ${bundles}")
			assetsList.each {
				def checkIssueType = AssetComment.find("from AssetComment where assetEntity=$it.id and commentType='issue' and isResolved = 0"+
													" and date_created between SUBTIME(CURRENT_TIMESTAMP,'00:10:00') and CURRENT_TIMESTAMP ")
				if ( checkIssueType ) {
					assetCommentsList << ["assetEntityId":it.id, "type":"database_table_red.png"]
				} else {
					checkIssueType = AssetComment.find("from AssetComment where assetEntity=$it.id and date_created between SUBTIME(CURRENT_TIMESTAMP,'00:10:00') and CURRENT_TIMESTAMP")
					if ( checkIssueType ) {
						assetCommentsList << ["assetEntityId":it.id, "type":"database_table_bold.png"]
					} /*else if( role ){
						assetCommentsList << ["assetEntityId":it.id, "type":"database_table_light.png"]
					}*/
				}
			}
			assetEntityAndCommentList << [ assetEntityList: assetEntityList, assetCommentsList: assetCommentsList, lastPoolTime : currentPoolTime.toString() ]
		}
    	render assetEntityAndCommentList as JSON
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
			def fillColor = stateEngineService.getHeaderColor(projectInstance.workflowCode, stateEngineService.getState(projectInstance.workflowCode,it))
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
		def f = ApplicationHolder.application.parentContext.getResource("templates/headerSvg_${projectInstance?.id}.svg").getFile()
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
		menuOptions = pmoAssetTrackingService.constructManuOptions( state, assetEntity, situation, stateType )
		
		render menuOptions
	}
	/* -----------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Entity and to toState
	 * @return : Create a transition as selected by User and will return the tannsition asset row details
	 *----------------------------------------------------*/
	def createTransitionForNA = {
		def actionId = params.actionId
		def type = params.type
		def principal = SecurityUtils.subject.principal
        def loginUser = UserLogin.findByUsername(principal)
		def actions = actionId.split("_")
		def assetEntity = AssetEntity.findById(actions[0])
		def stateToId = Integer.parseInt(actions[1])
		def stateTo = stateEngineService.getState( assetEntity.project.workflowCode, stateToId )
		
		def role = ""
		def subject = SecurityUtils.subject
		if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
			role = "SUPERVISOR"
		} else if(subject.hasRole("MANAGER")){
			role = "MANAGER"
		}
		def assetTransitionQuery = "from AssetTransition t where t.assetEntity = ${assetEntity.id} and t.voided = 0"
		def comment = ""
		if(stateTo == "Terminated"){
				comment = "Asset has been Terminated"
		}
		def message = ""
		if(role){
			message = pmoAssetTrackingService.createBulkTransition(type, assetEntity, stateTo, role, loginUser, comment)
		} else {
			message = " You don't have permission to create transition"
		}
		def tdId = pmoAssetTrackingService.getTransitionRow( assetEntity, stateTo )
		render tdId as JSON
	}
	/*--------------------------------------------------------
	 * Will split the Application, App Owner, and AppSME of entries by a comma inside a given record.  
	 * For example, there are three assets with "Adam", "Bob", and "Adam, Bob,Charlie" in the app owner fields.In the filter dropdown,
	  	we currently show "Adam (1), Bob (1), and Adam,Bob,Charlie(1)" and will split like "Adam(2), Bob(2),Charlie(1)".
	 * @author : Lokanada Reddy
	 * @return : result as list.
	 * -------------------------------------------------------*/
	def splitFilterExpansion(def appsList){
		def applicationMap = new HashMap()
		def resList = []
		appsList.each{ apps ->
			apps[0] = apps[0] ? apps[0] : ""
			def applications = String.valueOf(apps[0]).split(",")
			applications.each{ app ->
				if( ! applicationMap.containsKey( app.trim() ) ){
					applicationMap.put(app.trim(),apps[1])
				} else {
					def appCount = applicationMap.get(app.trim()) + apps[1]
					applicationMap.put( app.trim(), appCount ) 
				}
			}
			
		}
		applicationMap.keySet().each{
			resList << ["key":it, "value":applicationMap.get(it)]  
		}
		resList.sort(){
			it.key
		}
		return resList
	}
	/*
	 * Validate and return the message to the user. 
	 */
	def getAssetsCountForBulkTransition = {
			
		def message = "" 
		def transId = params.transId
		def moveEvent = MoveEvent.findById( params.eventId )
		def type = params.type
		
		def stateTo = stateEngineService.getState( moveEvent.project.workflowCode, Integer.parseInt(transId) )
		def stateType = stateEngineService.getStateType(moveEvent.project.workflowCode, stateTo)
		def holdId = Integer.parseInt(stateEngineService.getStateId(moveEvent.project.workflowCode,"Hold"))

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
		} else if(subject.hasRole("MANAGER")){
			role = "MANAGER"
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
					def currentState = stateEngineService.getState( asset.project.workflowCode, currentTransition.stateTo)
					def validate = stateEngineService.canDoTask( asset.project.workflowCode, role, currentState, stateTo  ) 
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
		
		def stateTo = stateEngineService.getState( moveEvent.project.workflowCode, Integer.parseInt(transId) )
		
		if(stateTo == "Hold"){
			render ""
			return
		}
		
		def stateType = stateEngineService.getStateType(moveEvent.project.workflowCode, stateTo)
		def holdId = Integer.parseInt(stateEngineService.getStateId(moveEvent.project.workflowCode,"Hold"))
		
		def assetEntityList = pmoAssetTrackingService.getAssetEntityListForBulkEdit( params )
		
		def subject = SecurityUtils.subject
		def role = ""
		if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
			role = "SUPERVISOR"
		} else if(subject.hasRole("MANAGER")){
			role = "MANAGER"
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
				def currentState = stateEngineService.getState( asset.project.workflowCode, currentStateId)
				def validate = stateEngineService.canDoTask( asset.project.workflowCode, role, currentState, stateTo  ) 
				if(validate || stateTo =="Ready"){
					pmoAssetTrackingService.createBulkTransition( type, asset, stateTo, role, loginUser, "" )
				}
			} else if( ( !currentTransitions.find { it.assetId == asset.id && it.stateTo == holdId } && stateTo =="Ready") || 
					stateType == "boolean" || type == "void") {
				pmoAssetTrackingService.createBulkTransition( type, asset, stateTo, role, loginUser, "" )
			}
		}
		
		render ""
	}
	
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
}
