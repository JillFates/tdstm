/*---------------------------------------
 * @author : Lokanada Reddy
 *--------------------------------------*/
import javax.servlet.http.HttpSession

import org.apache.shiro.SecurityUtils
import org.springframework.web.context.request.RequestContextHolder

import com.tds.asset.AssetEntity
import com.tds.asset.AssetComment
import com.tds.asset.AssetTransition
import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType

/**
 * Service class used by the Asset Tracking / PMO dashboard that has a number of methods used to gather data for the dashboard
 *
 */
class PmoAssetTrackingService {
		
	// define services
	def workflowService
	def taskService
	def stateEngineService
	def jdbcTemplate
	
    boolean transactional = true
    /*
     * Return current session object
     */
    def HttpSession getSession() {
        return RequestContextHolder.currentRequestAttributes().getSession()
    }
	
	/*
	 *  Will create bulk transition based on user input.
	 */
	def createBulkTransition(def type, def assetEntity, def stateTo, def role, def loginUser, def comment, def bundle ) {
		
		def transitionStatus
		def message = ""
		def workFlowCode
		def assetTransitionQuery = "from AssetTransition t where t.assetEntity = ${assetEntity.id} and t.voided = 0"
		if(bundle!="all"){
			workFlowCode = assetEntity.moveBundle.workflowCode
		}else{
		    workFlowCode = assetEntity.project.workflowCode
		}
		def stateType = stateEngineService.getStateType( workFlowCode,stateTo)
		def stateToId = stateEngineService.getStateId( workFlowCode,stateTo)
		def holdId = stateEngineService.getStateId( workFlowCode,"Hold")
		
		def currStateQuery = "select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
				"where t.asset_entity_id = ${assetEntity.id} and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId )  order by date_created desc, stateTo desc limit 1 "

		// The the menuOption which provides us the validation logic of what the user can do here.
		def menuOption = constructMenuOptions( stateTo, assetEntity, type, stateType )
		
		log.info "createBulkTransition: stateTo:${stateTo}, stateType:${stateType}, stateToId:${stateToId}, menuOption:${menuOption}, type:${type}"
				
		switch (type) {
			case "start" :
				// The concept of a Start action is only applicable for the Task logic and therefore there is no Transition interaction
				log.info "createBulkTransition: menuOption=${menuOption}"
				if ( menuOption == 'rb_doMenu' ) {
					def task = workflowService.getTaskFromAssetAndWorkflow(assetEntity, stateTo)
					if (task) {
						taskService.setTaskStatus(task, AssetCommentStatus.STARTED)
						if (! (task.validate() && task.save(flush:true)) ) {
							log.error "createBulkTransition: Failed to START task [${task.id} : " + GormUtil.allErrorsString(task)
							message = "Unable to Start task due to " + GormUtil.allErrorsString(task)
						 }
					} else {
						log.error "createBulkTransition: unable to find task for asset ${assetEntity.id} and state ${stateTo}"
						message = "Unexpectedly was unable to locate the task in order to start it"
					}
				}
			case "done" :
//				log.info "createBulkTransition: processing 'done'"
				// def validOptions = "doneMenu doMenu"
				if( ['rb_doneMenu', 'doneMenu', 'doMenu'].contains( menuOption ) || 
					( menuOption == "readyMenu" && stateTo == AssetCommentStatus.READY ) ) 
				{
					
					def assetTransition = AssetTransition.find(assetTransitionQuery + " and t.stateTo = $stateToId")
					if(assetTransition){
						assetTransition.voided = 1
						assetTransition.save(flush:true)
					}
					transitionStatus = workflowService.createTransition( workFlowCode, role,stateTo, assetEntity, 
						assetEntity.moveBundle, loginUser, null, comment )
					message = transitionStatus.message
				}
				if(stateType == "boolean" && stateTo=="VMCompleted") {
					transitionStatus = workflowService.createTransition( workFlowCode, role, AssetCommentStatus.DONE, assetEntity, 
						assetEntity.moveBundle, loginUser, null, comment )
					message = transitionStatus.message
				}
				break;
			
			case "void" : 
				def transitions = AssetTransition.findAll(assetTransitionQuery + " and t.stateTo >= $stateToId")
				transitions.each{
					if(it.type != "boolean"){
						it.voided = 1
						it.save(flush:true)
					}
				}
				// TODO: Runbook - undo completed task when a transition is voided
				changeCurrentStatus(currStateQuery,assetEntity)
				break;
					
			case "ready" :
				transitionStatus = workflowService.createTransition( workFlowCode, role, AssetCommentStatus.READY, assetEntity, 
						assetEntity.moveBundle, loginUser, null, comment )
				message = transitionStatus.message
				break;
			case "NA" :
				if(stateType == "boolean"){
					def assetTransition = AssetTransition.find(assetTransitionQuery + " and t.stateTo = $stateToId")
					if(assetTransition){
						assetTransition.voided = 1
						assetTransition.save(flush:true)
					}
					transitionStatus = workflowService.createTransition( workFlowCode, role,stateTo, assetEntity, 
						assetEntity.moveBundle, loginUser, null, comment )
					message = transitionStatus.message
					
					// Find the latest created transition and mark it as NA
					def sql = "SELECT asset_transition_id FROM asset_transition WHERE asset_entity_id=${assetEntity.id} " + 
						"AND voided=0 ORDER BY date_created DESC LIMIT 1"
					def atId = jdbcTemplate.queryForInt(sql)
					if (atId) {
						def currentTransition = AssetTransition.get(atId)
						currentTransition.isNonApplicable = 1
						currentTransition.save(flush:true)						
					}
					/*
					log.info "createBulkTransition: sql=${sql} results=${results}"
					def query = AssetTransition.where { id == assetEntity.id && voided == 0	}
					def currentTransition = query.list(sort:'dateCreated', order:'desc', max:1)
					//def currentTransition = AssetTransition.findAllByIdAndVoided(assetEntity.id, 0,
						//"from AssetTransition t where t.assetEntity = ${assetEntity.id} AND t.voided = 0",
					//	sort:'dateCreated', order:'desc', max:1) 
					// " ORDER BY dateCreated DESC")
					if (currentTransition && currentTranstion.size() == 1) {
						currentTransition[0].isNonApplicable = 1
						currentTransition[0].save(flush:true)						
					}
					*/
				}
				break;
			case "pending" :
				if(stateType == "boolean") {
					def assetTransition = AssetTransition.find(assetTransitionQuery + " and t.stateTo=?", [stateToId])
					if(assetTransition){
						assetTransition.voided = 1
						assetTransition.save(flush:true)
					}
					changeCurrentStatus(currStateQuery,assetEntity)
				}
				break;
		}
		return message
	}
	/* -----------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Entity and  currStateQuery
	 * @return : will update the currState of Asset 
	 *----------------------------------------------------*/
	def changeCurrentStatus( def currStateQuery, def assetEntity ){
		def currTransition = jdbcTemplate.queryForList(currStateQuery)
		def currState
		if(currTransition.size()){
			currState = currTransition[0].stateTo
		}
		def projectAssetMap = ProjectAssetMap.findByAsset( assetEntity )
		if(currState && projectAssetMap){
			projectAssetMap.currentStateId = currState
			projectAssetMap.save()
		} else if( projectAssetMap ) {
			projectAssetMap.delete()
		}
		assetEntity.currentStatus = currState 
		assetEntity.save()
	}

	/**
	 * Called by the PMO AssetTracker to get the attributes for the various workflow steps of the specified asset (in Runbook Mode)
	 * 
	 * @param assetEntity - the asset to lookup the state of the taskflow steps for
	 * @param bundleCode - the bundle code or 'all', which is used to determine which workflow to use (either the moveBundle's or the project's)
	 * @return  Map[id, cssClass] list of all steps and the associated CSS class names to use in the display
	 */
	def getTransitionRowRb(def assetEntity, def bundleId ) {	

		// Need to use the Project Workflow if user has selected "All" bundles otherwise use the moveBundle's workflow
		// thereby only showing the appropriate workflow steps in the row.
		def moveBundle
		def project
		def workflowCode
		
		// Note that sometimes AssetEntity is an array and other times is the AssetEntity domain class
		if ( assetEntity.projectId ) {
			project = Project.read(assetEntity.projectId)
			if (bundleId == 'all') {
				workflowCode = project.workflowCode
			} else {
				moveBundle = MoveBundle.read(bundleId)
				workflowCode = moveBundle.workflowCode
			} 	
		} else {
			project = assetEntity.project
			if (bundleId == 'all') {
				workflowCode = project.workflowCode
			} else {
				moveBundle = assetEntity.moveBundle 
				workflowCode = moveBundle.workflowCode
			}
		}
		
		// log.info "getTransitionRowRb: bundleCode:${bundleId}, moveBundle:${moveBundle}, project:${project}, asset:${assetEntity.toString()}"
		
		// Get all of the Workflow Transition Ids that we want to report for the asset
		def sql = "SELECT task.asset_entity_id AS assetId, wft.trans_id AS step, task.status FROM workflow w " +  
			"JOIN workflow_transition wft ON wft.workflow_id=w.workflow_id " +
			"LEFT OUTER JOIN asset_comment task ON task.workflow_transition_id=wft.workflow_transition_id AND task.asset_entity_id=${assetEntity.id} " +
			"WHERE w.process='${workflowCode}' ORDER BY step"
		def assetSteps = jdbcTemplate.queryForList(sql)
		def map=[]
		// TODO : need to handle hold overtime and completed 2/5
		assetSteps.each() { step ->
			def id = "${assetEntity.id}_${step.step}"
			def css = taskService.getCssClassForStatus(step.status)
			map << [id:id, cssClass:css]
		}
		
		return map
	}
	
	/* -----------------------------------------------------
	 * Called by the PMO AssetTracker to get the attributes for the various workflow steps of the specified asset
	 * @author : Lokanath Reddy
	 * @param  : Asset Entity and  state
	 * @return : Transition row details  
	 *----------------------------------------------------*/
	def getTransitionRow(def assetEntity, def state, def bundle){
		def project = assetEntity.project

		if (project.runbookOn) {
			return getTransitionRowRb( assetEntity, bundle)	
		}
		
		def workFlowCode 
		if(bundle!="all"){
			workFlowCode = assetEntity.moveBundle.workflowCode
		}else{
		    workFlowCode = assetEntity.project.workflowCode
		}
		def maxstate = 0
		def currentstate = 0
		def tdId = []
		def holdId = Integer.parseInt(stateEngineService.getStateId(assetEntity.moveBundle.workflowCode,"Hold"))
		def terminatedId = Integer.parseInt(stateEngineService.getStateId(workFlowCode,"Terminated"))
		def currentTransition = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
														"where t.asset_entity_id = ${assetEntity?.id} and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId )"+
														"order by date_created desc, stateTo desc limit 1 ")
		if(currentTransition.size()){
			currentstate = currentTransition[0].stateTo
		}
		def maxTransition = jdbcTemplate.queryForList("select max(cast(t.state_to as UNSIGNED INTEGER)) as maxState from asset_transition t "+
										"where t.asset_entity_id = ${assetEntity?.id} and t.voided = 0 and t.type = 'process' group by t.asset_entity_id ")
		if(maxTransition){
			maxstate = maxTransition[0].maxState
		}
		def processTransitions= stateEngineService.getTasks(workFlowCode, "TASK_ID")
		
		def naTransQuery = "from AssetTransition where assetEntity = ${assetEntity?.id} and voided = 0 and type = 'boolean' "
		
		def doneTransitionQuery = "from AssetTransition where assetEntity = ${assetEntity?.id} and voided = 0 and type = 'process' " 
		tdId << [id:"${assetEntity?.id}", cssClass:stateEngineService.getState(workFlowCode,currentstate)]
		processTransitions.each() { trans ->
			def cssClass='task_pending'
            if(currentstate != terminatedId){
            	
            	def transitionId = Integer.parseInt(trans)
				def stateType = stateEngineService.getStateType( workFlowCode, 
										stateEngineService.getState(workFlowCode, transitionId))
	            def isHoldNa = AssetTransition.find(naTransQuery+" and isNonApplicable = 1 and stateTo = "+holdId)
				
				if(AssetTransition.find(naTransQuery+" and isNonApplicable = 1 and stateTo = "+transitionId) ){
					cssClass='asset_pending'
				} else if(AssetTransition.find(naTransQuery+" and isNonApplicable = 0 and stateTo = "+transitionId) && stateType == 'boolean') {
					if(currentstate != holdId || isHoldNa){
						cssClass='task_done'
					} else {
						cssClass='asset_hold'
					}
				}
				if(stateType != 'boolean' || transitionId == holdId){
					if(currentstate == holdId && !isHoldNa ){
						cssClass = "asset_hold"
					} else if( transitionId <= maxstate  ){
						if(transitionId != holdId && AssetTransition.find(doneTransitionQuery+"  and stateTo = "+transitionId)){
							cssClass = "task_done"
						} else if( transitionId == holdId ){
							if(isHoldNa){
								cssClass='asset_pending'
							} else {
								cssClass='task_pending'
							}
						} else if(AssetTransition.find(naTransQuery+"  and isNonApplicable = 1 and stateTo = "+transitionId)){
							cssClass='asset_pending'
						}
					}
				}
				cssClass = getRecentChangeStyle( assetEntity?.id, cssClass, trans)
            } else {
            	cssClass='task_term'
			}
			
			tdId << [id:"${assetEntity?.id+"_"+trans}", cssClass:cssClass]
		}
		return tdId
	}
	/* -----------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset, State and statetype  
	 * @return : return the list for menu
	 *----------------------------------------------------*/
	def constructMenuOptions( def state, def assetEntity, def situation, def stateType ) {
		def project = assetEntity.project
		if (project.runbookOn) {
			return constructMenuOptionsRb( state, assetEntity, situation, stateType)
		}
		 def menuOptions = ""
		 def holdId = Integer.parseInt(stateEngineService.getStateId(assetEntity.moveBundle.workflowCode,"Hold"))
		 if(stateType != "process" && state != "Hold"){
			 if(situation == "NA"){
				 menuOptions = "naMenu"
			 } else if(situation == "done"){
				 menuOptions = "doneMenu"
			 } else {
				 menuOptions = "noTransMenu"
			 }
		 } else if(state != "Hold"){
			 def projectAssetMap = ProjectAssetMap.findByAsset(assetEntity)
			 def taskList
			 def role = ""
			 def subject = SecurityUtils.subject
			 if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
				 role = "SUPERVISOR"
			 } else if(subject.hasRole("MANAGER")){
				 role = "MANAGER"
			 }
			 if(projectAssetMap){
				 def transitionSelected = stateEngineService.getStateIdAsInt( assetEntity.moveBundle.workflowCode, state )
				 def currentTransition = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
												"where t.asset_entity_id = ${assetEntity?.id} and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId )"+
												"order by date_created desc, stateTo desc limit 1 ")
				 def currentState = projectAssetMap.currentStateId
                 if(currentTransition.size()){
                	 currentState = currentTransition[0].stateTo
                 }
				 def stateVal = stateEngineService.getState(assetEntity.moveBundle.workflowCode,currentState)
				 if(currentState < transitionSelected ){
					 def roleCheck = stateEngineService.canDoTask( assetEntity.moveBundle.workflowCode, role, stateVal, state  )
					 taskList = stateEngineService.getTasks(assetEntity.moveBundle.workflowCode,role,stateVal)
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
	
	/**
	 * Used by the PMO asset tracker screen to determine what menu should be presented when the user right-clicks on a particular
	 * state.
	 * @param state	- Workflow state that the user clicked on
	 * @param assetEntity - the assetEntity that user clicked on
	 * @param situation - contains 'done', 'NA' or ? (TBD)
	 * @param stateType - contains 'process', 'Hold' or ? 'bool' (TBD) 
	 * @return String menu name to use
	 */
	def constructMenuOptionsRb( def state, def assetEntity, def situation, def stateType ) {
		// TODO: Runbook : add security around the constructMenuOptionsRb method
		
		/*
		 * Menus:
		 *    o rb_doMenu - Start/Done
		 *    o rb_doneMenu - Done/Hold/Undo (TBD)
		 *    o rb_voidMenu - Undo
		 *    o rb_noOption - No option
		 * Cases (task status):
		 *    o Ready - doMenu
		 *    o pre Ready (verify that there are no incomplete predecessors) - doMenu
		 *    o Started - doneMenu
		 *    o Completed - voidMenu (aka undo)
		 *    o Cancelled - noOption
		 *    o Hold - doMenu
		 */
		def task = workflowService.getTaskFromAssetAndWorkflow(assetEntity, state) 
		def menu = 'rb_noOption'
		if (task) {
			switch (task.status) {
				case [ AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING ]:
					def predecessors = workflowService.getIncompletePredecessors(task)
					if (predecessors?.size() == 0) menu = 'rb_doMenu'
					break
					
				case [ AssetCommentStatus.READY, AssetCommentStatus.HOLD ]:
					menu = 'rb_doMenu'
					break
				
				case AssetCommentStatus.STARTED: 
					menu = 'rb_doneMenu'
					break

				case AssetCommentStatus.COMPLETED: 
					menu = 'rb_voidMenu'
					break
			}
		}
		log.info "constructMenuOptionsRb: asset:${assetEntity}, state:${state}, task.status: ${task ? task.status : 'null'}, menu:${menu}"
		return menu
	}
		
	
	/*-----------------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : event, bundle, four coulmn filters
	 * @return : return the AssetEntity object for the selected filterd
	 *----------------------------------------------------*/
	def getAssetEntityListForBulkEdit(def params ) {

		def moveBundleId = params.bundleId
		def moveEventId = params.eventId
		def column1Value = params.c1v
		def column2Value = params.c2v
		def column3Value = params.c3v
		def column4Value = params.c4v
		def column1Field = params.c1f
		def column2Field = params.c2f
		def column3Field = params.c3f
		def column4Field = params.c4f
		def limit = params.max
		def offset = params.offset
		def sortby = params.sort
        def order = params.order
		
		def assetQuery = new StringBuffer(" FROM AssetEntity ae WHERE ae.moveBundle.moveEvent.id = $moveEventId ")
		if(moveBundleId && moveBundleId != 'all'){
			assetQuery .append(" AND ae.moveBundle.id = $moveBundleId ")
		}
		if(column1Value !="" && column1Value!= null){
			if(column1Value == 'blank'){
				assetQuery.append(" AND ae.${column1Field} = '' OR ae.${column1Field} is null")
			} else {
				def app = column1Value.replace("'","\\'")
				assetQuery.append(" AND ae.${column1Field} like '%$app%'")
			}
		}
		if(column2Value!="" && column2Value!= null){
			if(column2Value == 'blank'){
				assetQuery.append(" AND ae.${column2Field} = '' OR ae.${column2Field} is null")
			} else {
				def owner = column2Value.replace("'","\\'")
				assetQuery.append(" AND ae.${column2Field} like '%$owner%'")
			}
			
		}
		if(column3Value!="" && column3Value!= null){
			if(column3Value == 'blank'){
				assetQuery.append(" AND ae.${column3Field} = '' OR ae.${column3Field} is null")
			} else {
				def sme = column3Value.toString().replace("'","\\'")
				assetQuery.append(" AND ae.${column3Field} like '%$sme%'")
			}
		}
		if(column4Value!="" && column4Value!= null){
			if(column4Value == 'blank'){
				assetQuery.append(" AND ae.${column4Field} = '' OR ae.${column4Field} is null")
			} else {
				def name = column4Value.toString().replace("'","\\'")
				assetQuery.append(" AND ae.${column4Field} like '%$name%'")
			}
		}
		if(sortby != "" && sortby != null){
			assetQuery.append(" order by $sortby")
		}else {
			assetQuery.append(" order by ae.application, ae.assetName")
		}
		if(order != "" && order != null){
			assetQuery.append(" $order ")
		}else {
			assetQuery.append(" asc ")
		}
		def assetEntityList 
		if( limit && limit != "all"){
			if(offset){
				assetEntityList = AssetEntity.findAll( assetQuery.toString(), [ max:Integer.parseInt(limit), offset:Integer.parseInt(offset) ] )
			} else {
				assetEntityList = AssetEntity.findAll( assetQuery.toString(), [ max:Integer.parseInt(limit) ] )
			}
		} else {
			assetEntityList = AssetEntity.findAll(assetQuery.toString())
		}
		return assetEntityList
	}
	/*----------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Id, cssClass, TransitionId
	 * @return : Changed CSS class
	 *--------------------------------------------------------*/
	
	def getRecentChangeStyle( def assetId, def cssClass, def transId ){
		// TODO: Runbook - need to interweave with Runbook tasks for getRecentChangeStyle()
		def changedClass = cssClass
		if(cssClass == "task_done"){
			def createdTime = AssetTransition.find("from AssetTransition a where a.assetEntity = $assetId and a.voided = 0 and a.stateTo=${transId}")?.dateCreated?.getTime()
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ					
			def currentTime = GormUtil.convertInToGMT( "now", tzId ).getTime()
			Integer minutes
			if(createdTime){
				minutes = (currentTime - createdTime) / 1000
			}
			if( minutes != null ){
				if(minutes < 120){
					changedClass = "task_done2"
				} else if(minutes > 120 && minutes < 330){
					changedClass = "task_done5"	
				}
			}
		}
		return changedClass
	}

	def getRecentChangeStyle( def cssClass, def trans ){
		// TODO : Runbook - getRecentChangeStyle(2 args) what is this used for
		def changedClass = cssClass
		if(cssClass == "task_done"){
			def createdTime = trans.dateCreated?.getTime()
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ					
			def currentTime = GormUtil.convertInToGMT( "now", tzId ).getTime()
			Integer minutes
			if(createdTime){
				minutes = (currentTime - createdTime) / 1000
			}
			if( minutes != null ){
				if(minutes < 120){
					changedClass = "task_done2"
				} else if(minutes > 120 && minutes < 330){
					changedClass = "task_done5"	
				}
			}
		}
		return changedClass
	}
	/*----------------------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : Project, bundles, params
	 * @return : List of assets for PMO 
	 *--------------------------------------------------------*/
	def getAssetsForListView( def projectId, def bundles, def columns, def params ) {

		def project = Project.get(projectId)		
		def column1Value = params.column1
        def column2Value = params.column2
        def column3Value = params.column3
        def column4Value = params.column4
        def sortby = params.sort
        def order = params.order
		def limit = params.assetsInView
		def offset = params.offset
		def query = new StringBuffer(
			"""SELECT * FROM( SELECT ae.asset_entity_id AS id, ae.asset_name AS assetName,ae.short_name AS shortName,ae.asset_tag AS assetTag,
			ae.asset_type AS assetType,mf.name AS manufacturer, m.name AS model, ae.application, ae.app_owner AS appOwner, ae.app_sme AS appSme,
			ae.ip_address AS ipAddress, ae.hinfo AS os, ae.serial_number AS serialNumber,m.usize, ae.rail_type AS railType, ae.project_id AS projectId,
			ae.source_location AS sourceLocation, ae.source_room AS sourceRoom, ae.source_rack AS sourceRack, ae.source_rack_position AS sourceRackPosition,
		    ae.target_location AS targetLocation, ae.target_room AS targetRoom, ae.target_rack AS targetRack, ae.target_rack_position AS targetRackPosition,
			mb.name AS moveBundle, ae.truck,
			ae.plan_status AS planStatus, ae.priority, ae.cart, ae.shelf, 
			sptMt.team_code AS sourceTeamMt, tptMt.team_code AS targetTeamMt,
			sptLog.team_code AS sourceTeamLog, tptLog.team_code AS targetTeamLog,
			sptSa.team_code AS sourceTeamSa, tptSa.team_code AS targetTeamSa,
			sptDba.team_code AS sourceTeamDba, tptDba.team_code AS targetTeamDba,
			MAX(CAST(at.state_to AS UNSIGNED INTEGER)) AS maxstate,  
			ae.custom1, ae.custom2, ae.custom3,	ae.custom4, ae.custom5, ae.custom6, ae.custom7, ae.custom8,
			ae.current_status AS currentStatus, """)
		
		if (project.runbookOn==1) {
			query.append( 'MAX(IFNULL(task.last_updated,eav.last_updated)) AS updated')	
		} else {
			query.append( 'MAX(at.date_created) AS updated')
		}
		
		query.append(""" FROM asset_entity ae
			JOIN eav_entity eav ON eav.entity_id=ae.asset_entity_id
			LEFT JOIN move_bundle mb ON ae.move_bundle_id = mb.move_bundle_id
			LEFT JOIN project_team sptMt ON ae.source_team_id = sptMt.project_team_id
            LEFT JOIN project_team tptMt ON ae.target_team_id = tptMt.project_team_id
            LEFT JOIN project_team sptLog ON ae.source_team_id = sptLog.project_team_id
            LEFT JOIN project_team tptLog ON ae.target_team_id = tptLog.project_team_id
            LEFT JOIN project_team sptSa ON ae.source_team_id = sptSa.project_team_id
            LEFT JOIN project_team tptSa ON ae.target_team_id = tptSa.project_team_id
            LEFT JOIN project_team sptDba ON ae.source_team_id = sptDba.project_team_id
            LEFT JOIN project_team tptDba ON ae.target_team_id = tptDba.project_team_id
			LEFT JOIN model m ON ae.model_id = m.model_id
			LEFT JOIN manufacturer mf ON ae.manufacturer_id = mf.manufacturer_id
            LEFT JOIN asset_transition at ON at.asset_entity_id = ae.asset_entity_id and at.voided = 0 and at.type='process'
			""")
		if (project.runbookOn==1) {
			query.append( 'LEFT JOIN asset_comment task ON task.asset_entity_id = ae.asset_entity_id')
		}
		query.append("""
			WHERE ae.project_id = $projectId AND ae.move_bundle_id IN ${bundles} AND ae.asset_type NOT IN ('Files','Database')
			GROUP BY ae.asset_entity_id ) ae WHERE  1=1
			""")
				
		// TODO : SECURITY - the follow code constructs SQL with user input data from params - possible SQL Injection
		if(column1Value !="" && column1Value!= null){
			if(column1Value == 'blank'){
				query.append(" AND ae.${columns?.column1.field} = '' OR ae.${columns?.column1.field} IS NULL")
			} else {
				def app = column1Value.replace("'","\\'")
				query.append(" AND ae.${columns?.column1.field} LIKE '%$app%'")
			}
		}
		if(column2Value!="" && column2Value!= null){
			if(column2Value == 'blank'){
				query.append(" AND ae.${columns?.column2.field} = '' OR ae.${columns?.column2.field} IS NULL")
			} else {
				def owner = column2Value.replace("'","\\'")
				query.append(" AND ae.${columns?.column2.field} LIKE '%$owner%'")
			}

		}	
		if(column3Value!="" && column3Value!= null){
			if(column3Value == 'blank'){
				query.append(" AND ae.${columns?.column3.field} = '' OR ae.${columns?.column3.field} IS NULL")
			} else {
				def sme = column3Value.toString().replace("'","\\'")
				query.append(" AND ae.${columns?.column3.field} LIKE '%$sme%'")
			}
		}
		if(column4Value!="" && column4Value!= null){
			if(column4Value == 'blank'){
				query.append(" AND ae.${columns?.column4.field} = '' OR ae.${columns?.column4.field} IS NULL")
			} else {
				def name = column4Value.toString().replace("'","\\'")
				query.append(" AND ae.${columns?.column4.field} LIKE '%$name%'")
			}
		}
		// get the total assets
		// TODO - PERFORMANCE - The following code makes two SQL calls. Perhaps we can just call ONCE and handle the limit/offset in another way.  Also, if offset or limit are not specified we doing the 2nd query for nothing.
		def resultListSize =jdbcTemplate.queryForList(query.toString())?.size()
		if(sortby != "" && sortby != null){
			query.append(" ORDER BY $sortby")
		}else {
			query.append(" ORDER BY updated ")
		}
		if(order != "" && order != null){
			query.append(" $order ")
		}else {
			query.append(" desc ")
		}
		
		if(limit && limit != "all"){
			if(offset){
				query.append(" LIMIT ${offset},${limit}")
			} else {
				query.append(" LIMIT ${limit}")
			}
		}

		try {
			def resultList=jdbcTemplate.queryForList(query.toString())
			return [resultList, resultListSize]
		} catch (e) {
			log.error "SQL Error: ${e.toString()}"
			return null
		}

	}
	/*----------------------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : Project, bundles, params, lastPoolTime, currentPoolTime
	 * @return : List of assets for PMO to update thru ajax
	 *--------------------------------------------------------*/
	def getAssetsForPmoUpdate( def projectId, def bundles, def params, def lastPoolTime, def currentPoolTime ){
		def project = Project.read(projectId)
		def column1Value = params.c1v
		def column2Value = params.c2v
		def column3Value = params.c3v
		def column4Value = params.c4v
		def column1Field = params.c1f
		def column2Field = params.c2f
		def column3Field = params.c3f
		def column4Field = params.c4f
		def limit = params.max
		def offset = params.offset
		def sortby = params.sort
        def order = params.order
		// The SQL has a nested SELECT in order to provide ability to tack on LIMIT and ORDER criteria after the fact
		def query = new StringBuffer("""SELECT * FROM (SELECT * FROM( select ae.asset_entity_id as id, ae.asset_name as assetName,ae.short_name as shortName,ae.asset_tag as assetTag,
				ae.asset_type as assetType,mf.name as manufacturer, m.name as model, ae.application, ae.app_owner as appOwner, ae.app_sme as appSme,
				ae.ip_address as ipAddress, ae.hinfo as os, ae.serial_number as serialNumber,m.usize, ae.rail_type as railType, ae.project_id AS projectId,
				ae.source_location as sourceLocation, ae.source_room as sourceRoom, ae.source_rack as sourceRack, ae.source_rack_position as sourceRackPosition,
				ae.target_location as targetLocation, ae.target_room as targetRoom, ae.target_rack as targetRack, ae.target_rack_position as targetRackPosition,
			    mb.name as moveBundle, ae.truck,
				ae.plan_status as planStatus, ae.priority, ae.cart, ae.shelf, 
				sptMt.team_code as sourceTeamMt, tptMt.team_code as targetTeamMt,
				sptLog.team_code as sourceTeamLog, tptLog.team_code as targetTeamLog,
				sptSa.team_code as sourceTeamSa, tptSa.team_code as targetTeamSa,
				sptDba.team_code as sourceTeamDba, tptDba.team_code as targetTeamDba,
				max(cast(at.state_to as UNSIGNED INTEGER)) as maxstate, ae.custom1 as custom1, ae.custom2 as custom2,ae.custom3 as custom3,
				ae.custom3 as custom4,ae.custom5 as custom5,ae.custom6 as custom6,ae.custom7 as custom7,ae.custom8 as custom8, ae.current_status as currentStatus,
			""")
			
		if (project.runbookOn) {
			query.append( 'MAX(IFNULL(task.last_updated,eav.last_updated)) AS updated')
		} else {
			query.append( 'MAX(at.date_created) AS updated')
		}
		
		query.append(""" FROM asset_entity ae
				JOIN eav_entity eav ON eav.entity_id=ae.asset_entity_id
				LEFT JOIN move_bundle mb ON (ae.move_bundle_id = mb.move_bundle_id )
				LEFT JOIN project_team sptMt ON (ae.source_team_id = sptMt.project_team_id )
				LEFT JOIN project_team tptMt ON ae.target_team_id = tptMt.project_team_id
                LEFT JOIN project_team sptLog ON (ae.source_team_id = sptLog.project_team_id )
                LEFT JOIN project_team tptLog ON (ae.target_team_id = tptLog.project_team_id )
                LEFT JOIN project_team sptSa ON (ae.source_team_id = sptSa.project_team_id )
                LEFT JOIN project_team tptSa ON (ae.target_team_id = tptSa.project_team_id )
                LEFT JOIN project_team sptDba ON (ae.source_team_id = sptDba.project_team_id )
                LEFT JOIN project_team tptDba ON (ae.target_team_id = tptDba.project_team_id )
				LEFT JOIN model m ON (ae.model_id = m.model_id )
				LEFT JOIN manufacturer mf ON (ae.manufacturer_id = mf.manufacturer_id )
                LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id AND at.voided = 0 AND at.type='process')""")

		if (project.runbookOn==1) {
			query.append( 'LEFT JOIN asset_comment task ON task.asset_entity_id = ae.asset_entity_id ')
		}
		
		query.append("""WHERE ae.project_id = $projectId and ae.move_bundle_id in ${bundles} GROUP BY ae.asset_entity_id ) ae WHERE  1 = 1""")

		if(column1Value !="" && column1Value!= null){
			if(column1Value == 'blank'){
				query.append(" AND ae.${column1Field} = '' OR ae.${column1Field} is null")
			} else {
				def app = column1Value.replace("'","\\'")
				query.append(" AND ae.${column1Field} like '%$app%'")
			}
		}
		if(column2Value!="" && column2Value!= null){
			if(column2Value == 'blank'){
				query.append(" AND ae.${column2Field} = '' OR ae.${column2Field} is null")
			} else {
				def owner = column2Value.replace("'","\\'")
				query.append(" AND ae.${column2Field} like '%$owner%'")
			}

		}	
		if(column3Value!="" && column3Value!= null){
			if(column3Value == 'blank'){
				query.append(" AND ae.${column3Field} = '' OR ae.${column3Field} is null")
			} else {
				def sme = column3Value.toString().replace("'","\\'")
				query.append(" AND ae.${column3Field} like '%$sme%'")
			}
		}
		if(column4Value!="" && column4Value!= null){
			if(column4Value == 'blank'){
				query.append(" AND ae.${column4Field} = '' OR ae.${column4Field} is null")
			} else {
				def name = column4Value.toString().replace("'","\\'")
				query.append(" AND ae.${column4Field} like '%$name%'")
			}
		}
		if(sortby != "" && sortby != null){
			query.append(" ORDER BY $sortby")
		}else {
			query.append(" ORDER BY updated")
		}
		if(order != "" && order != null){
			query.append(" $order ")
		}else {
			query.append(" DESC ")
		}
		if(limit && limit != "all"){
			if(offset){
				query.append(" LIMIT ${offset},${limit}")
			} else {
				query.append(" LIMIT ${limit}")
			}
		}
		
		// Add the filter that will restrict what transitions displayed. It will use the tasks if runbook is enabled otherwise look at 
		// the transitions
		if ( project.runbookOn == 1) {
			query.append(""") a WHERE a.id IN ( SELECT asset_entity_id FROM asset_comment WHERE 
				project_id=${projectId} AND comment_type='${AssetCommentType.TASK}' AND 
				last_updated BETWEEN SUBTIME('$lastPoolTime','00:15:30') AND '$currentPoolTime') """)	
		} else {
			query.append(""") a WHERE a.id IN ( SELECT t.asset_entity_id FROM asset_transition t WHERE
				(t.date_created BETWEEN SUBTIME('$lastPoolTime','00:15:30') AND '$currentPoolTime' 
				OR t.last_updated between SUBTIME('$lastPoolTime','00:15:30') AND '$currentPoolTime') )""")
		}
		
		// log.info "getAssetsForPmoUpdate: Query:${query.toString()}"
		try {
			def resultList=jdbcTemplate.queryForList(query.toString())
			return resultList
		} catch (e) {
			log.error "SQL Error: ${e.toString()}"
			return null
		}
	}
	
	/**
	 * Will split the Application, App Owner, and AppSME of entries by a comma inside a given record.
	 * For example, there are three assets with "Adam", "Bob", and "Adam, Bob,Charlie" in the app owner fields.In the filter dropdown,
	 * we currently show "Adam (1), Bob (1), and Adam,Bob,Charlie(1)" and will split like "Adam(2), Bob(2),Charlie(1)".
	 * @author : Lokanada Reddy
	 * @return : result as list.
	 */
	def splitFilterExpansion( def appsList, def assetAttribute, def workflowCode  ){
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
		if(assetAttribute != "currentStatus"){
			applicationMap.keySet().each{
				resList << ["key":it, "value":applicationMap.get(it), 'id':it ]
			}
		} else {
			applicationMap.keySet().each{
				resList << ["key":it ? stateEngineService.getState(workflowCode,Integer.parseInt(it)) : "", "value":applicationMap.get(it), 'id':it]
			}
		}
		resList.sort(){
			it.id
		}
		return resList
	}

}
