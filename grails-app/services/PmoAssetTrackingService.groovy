/*---------------------------------------
 * @author : Lokanada Reddy
 *--------------------------------------*/
import javax.servlet.http.HttpSession

import org.jsecurity.SecurityUtils
import org.springframework.web.context.request.RequestContextHolder

import com.tds.asset.AssetEntity
import com.tds.asset.AssetTransition
import com.tdssrc.grails.GormUtil

class PmoAssetTrackingService {

	// define services
	def workflowService
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
	def createBulkTransition(def type, def assetEntity, def stateTo, def role, def loginUser, def comment, def bundle ){
		
		def transitionStatus
		def message = "Transaction created successfully"
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
		
		switch (type) {
			case "done" :
				def menuOption = constructManuOptions( stateTo, assetEntity, "done", stateType )
				def validOptions = "doneMenu doMenu"
				if( validOptions.contains( menuOption ) || ( menuOption == "readyMenu" && stateTo == "Ready") ){
					def assetTeansition = AssetTransition.find(assetTransitionQuery + " and t.stateTo = $stateToId")
					if(assetTeansition){
						assetTeansition.voided = 1
						assetTeansition.save(flush:true)
					}
					transitionStatus = workflowService.createTransition( workFlowCode, role,stateTo, assetEntity, 
																		assetEntity.moveBundle, loginUser, null, comment )
					message = transitionStatus.message
				}
				if(stateType == "boolean" && stateTo=="VMCompleted"){
					transitionStatus = workflowService.createTransition( workFlowCode, role,"Completed", assetEntity, 
																		assetEntity.moveBundle, loginUser, null, comment )
				}
				break;
			
			case "void" : 
				def assetTeansitions = AssetTransition.findAll(assetTransitionQuery + " and t.stateTo >= $stateToId")
				assetTeansitions.each{
					if(it.type != "boolean"){
						it.voided = 1
						it.save(flush:true)
					}
				}
				changeCurrentStatus(currStateQuery,assetEntity)
				break;
					
			case "ready" :
				transitionStatus = workflowService.createTransition( workFlowCode, role,"Ready", assetEntity, 
						assetEntity.moveBundle, loginUser, null, comment )
				message = transitionStatus.message
				break;
			case "NA" :
				if(stateType == "boolean"){
					def assetTeansition = AssetTransition.find(assetTransitionQuery + " and t.stateTo = $stateToId")
					if(assetTeansition){
						assetTeansition.voided = 1
						assetTeansition.save(flush:true)
					}
					transitionStatus = workflowService.createTransition( workFlowCode, role,stateTo, assetEntity, 
																					assetEntity.moveBundle, loginUser, null, comment )
					message = transitionStatus.message
					def currentTransition = AssetTransition.find("from AssetTransition t where t.assetEntity = ${assetEntity.id} "+
																	"and voided = 0 order by dateCreated desc")
					currentTransition.isNonApplicable = 1
					currentTransition.save(flush:true)
				}
				break;
			case "pending" :
				if(stateType == "boolean"){
					def assetTeansition = AssetTransition.find(assetTransitionQuery + " and t.stateTo = $stateToId")
					if(assetTeansition){
						assetTeansition.voided = 1
						assetTeansition.save(flush:true)
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
	/* -----------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Entity and  state
	 * @return : Transition row details  
	 *----------------------------------------------------*/
	def getTransitionRow(def assetEntity, def state, def bundle){
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
	def constructManuOptions( def state, def assetEntity, def situation, def stateType ){
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
	/*-----------------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : event, bundle, four coulmn filters
	 * @return : return the AssetEntity object for the selected filterd
	 *----------------------------------------------------*/
	def getAssetEntityListForBulkEdit(def params ){

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
	def getAssetsForListView( def projectId, def bundles, def columns, def params ){
		
		def column1Value = params.column1
        def column2Value = params.column2
        def column3Value = params.column3
        def column4Value = params.column4
        def sortby = params.sort
        def order = params.order
		def limit = params.assetsInView
		def offset = params.offset
		// TODO : CLEANUP - remove as custom# from SQL statement as it is unnecessary
		def query = new StringBuffer("""SELECT * FROM( select ae.asset_entity_id as id, ae.asset_name as assetName,ae.short_name as shortName,ae.asset_tag as assetTag,
				ae.asset_type as assetType,mf.name as manufacturer, m.name as model, ae.application, ae.app_owner as appOwner, ae.app_sme as appSme,
				ae.ip_address as ipAddress, ae.hinfo as os, ae.serial_number as serialNumber,m.usize, ae.rail_type as railType,
				ae.source_location as sourceLocation, ae.source_room as sourceRoom, ae.source_rack as sourceRack, ae.source_rack_position as sourceRackPosition,
			    ae.target_location as targetLocation, ae.target_room as targetRoom, ae.target_rack as targetRack, ae.target_rack_position as targetRackPosition,
				mb.name as moveBundle, ae.truck,
				ae.new_or_old as planStatus, ae.priority, ae.cart, ae.shelf, 
				sptMt.team_code as sourceTeamMt, tptMt.team_code as targetTeamMt,
				sptLog.team_code as sourceTeamLog, tptLog.team_code as targetTeamLog,
				sptSa.team_code as sourceTeamSa, tptSa.team_code as targetTeamSa,
				sptDba.team_code as sourceTeamDba, tptDba.team_code as targetTeamDba,
				max(cast(at.state_to as UNSIGNED INTEGER)) as maxstate, ae.custom1 as custom1, ae.custom2 as custom2,ae.custom3 as custom3,
				ae.custom3 as custom4,ae.custom5 as custom5,ae.custom6 as custom6,ae.custom7 as custom7,ae.custom8 as custom8,ae.current_status as currentStatus,
				max(at.date_created) as updated
				FROM asset_entity ae
				LEFT JOIN move_bundle mb ON (ae.move_bundle_id = mb.move_bundle_id )
				LEFT JOIN project_team sptMt ON (ae.source_team_id = sptMt.project_team_id )
                LEFT JOIN project_team tptMt ON (ae.target_team_id = tptMt.project_team_id )
                LEFT JOIN project_team sptLog ON (ae.source_team_id = sptLog.project_team_id )
                LEFT JOIN project_team tptLog ON (ae.target_team_id = tptLog.project_team_id )
                LEFT JOIN project_team sptSa ON (ae.source_team_id = sptSa.project_team_id )
                LEFT JOIN project_team tptSa ON (ae.target_team_id = tptSa.project_team_id )
                LEFT JOIN project_team sptDba ON (ae.source_team_id = sptDba.project_team_id )
                LEFT JOIN project_team tptDba ON (ae.target_team_id = tptDba.project_team_id )
				LEFT JOIN model m ON (ae.model_id = m.model_id )
				LEFT JOIN manufacturer mf ON (ae.manufacturer_id = mf.manufacturer_id )
                LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id and at.voided = 0 and at.type='process')
				where ae.project_id = $projectId and ae.move_bundle_id  in ${bundles} and ae.asset_type not in ('Application','Files','Database')
				GROUP BY ae.asset_entity_id ) ae WHERE  1 = 1""")
				
		// TODO : SECURITY - the follow code constructs SQL with user input data from params - possible SQL Injection
		if(column1Value !="" && column1Value!= null){
			if(column1Value == 'blank'){
				query.append(" and ae.${columns?.column1.field} = '' OR ae.${columns?.column1.field} is null")
			} else {
				def app = column1Value.replace("'","\\'")
				query.append(" and ae.${columns?.column1.field} like '%$app%'")
			}
		}
		if(column2Value!="" && column2Value!= null){
			if(column2Value == 'blank'){
				query.append(" and ae.${columns?.column2.field} = '' OR ae.${columns?.column2.field} is null")
			} else {
				def owner = column2Value.replace("'","\\'")
				query.append(" and ae.${columns?.column2.field} like '%$owner%'")
			}

		}	
		if(column3Value!="" && column3Value!= null){
			if(column3Value == 'blank'){
				query.append(" and ae.${columns?.column3.field} = '' OR ae.${columns?.column3.field} is null")
			} else {
				def sme = column3Value.toString().replace("'","\\'")
				query.append(" and ae.${columns?.column3.field} like '%$sme%'")
			}
		}
		if(column4Value!="" && column4Value!= null){
			if(column4Value == 'blank'){
				query.append(" and ae.${columns?.column4.field} = '' OR ae.${columns?.column4.field} is null")
			} else {
				def name = column4Value.toString().replace("'","\\'")
				query.append(" and ae.${columns?.column4.field} like '%$name%'")
			}
		}
		// get the total assets
		// TODO - PERFORMANCE - The following code makes two SQL calls. Perhaps we can just call ONCE and handle the limit/offset in another way.  Also, if offset or limit are not specified we doing the 2nd query for nothing.
		log.debug "SQL to be executed: ${query.toString()}"
		def resultListSize =jdbcTemplate.queryForList(query.toString())?.size()
		if(sortby != "" && sortby != null){
			query.append(" order by $sortby")
		}else {
			query.append(" order by updated ")
		}
		if(order != "" && order != null){
			query.append(" $order ")
		}else {
			query.append(" desc ")
		}
		
		if(limit && limit != "all"){
			if(offset){
				query.append(" limit ${offset},${limit}")
			} else {
				query.append(" limit ${limit}")
			}
		}

		// TODO : SECURITY - Wrap this in a try/catch so user doesn't see the SQL		
		def resultList=jdbcTemplate.queryForList(query.toString())
		
		return [resultList,resultListSize]
	}
	/*----------------------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : Project, bundles, params, lastPoolTime, currentPoolTime
	 * @return : List of assets for PMO to update thru ajax
	 *--------------------------------------------------------*/
	def getAssetsForPmoUpdate( def projectId, def bundles, def params, def lastPoolTime, def currentPoolTime ){

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
		// TODO : CLEANUP - there is no need for the nested select statements in this query.  Remove the outer SELECT"
		def query = new StringBuffer("""SELECT * FROM (SELECT * FROM( select ae.asset_entity_id as id, ae.asset_name as assetName,ae.short_name as shortName,ae.asset_tag as assetTag,
				ae.asset_type as assetType,mf.name as manufacturer, m.name as model, ae.application, ae.app_owner as appOwner, ae.app_sme as appSme,
				ae.ip_address as ipAddress, ae.hinfo as os, ae.serial_number as serialNumber,m.usize, ae.rail_type as railType,
				ae.source_location as sourceLocation, ae.source_room as sourceRoom, ae.source_rack as sourceRack, ae.source_rack_position as sourceRackPosition,
				ae.target_location as targetLocation, ae.target_room as targetRoom, ae.target_rack as targetRack, ae.target_rack_position as targetRackPosition,
			    mb.name as moveBundle, ae.truck,
				ae.new_or_old as planStatus, ae.priority, ae.cart, ae.shelf, 
				sptMt.team_code as sourceTeamMt, tptMt.team_code as targetTeamMt,
				sptLog.team_code as sourceTeamLog, tptLog.team_code as targetTeamLog,
				sptSa.team_code as sourceTeamSa, tptSa.team_code as targetTeamSa,
				sptDba.team_code as sourceTeamDba, tptDba.team_code as targetTeamDba,
				max(cast(at.state_to as UNSIGNED INTEGER)) as maxstate, ae.custom1 as custom1, ae.custom2 as custom2,ae.custom3 as custom3,
				ae.custom3 as custom4,ae.custom5 as custom5,ae.custom6 as custom6,ae.custom7 as custom7,ae.custom8 as custom8, ae.current_status as currentStatus,
				max(at.date_created) as updated
				FROM asset_entity ae
				LEFT JOIN move_bundle mb ON (ae.move_bundle_id = mb.move_bundle_id )
				LEFT JOIN project_team sptMt ON (ae.source_team_id = sptMt.project_team_id )
                LEFT JOIN project_team tptMt ON (ae.target_team_id = tptMt.project_team_id )
                LEFT JOIN project_team sptLog ON (ae.source_team_id = sptLog.project_team_id )
                LEFT JOIN project_team tptLog ON (ae.target_team_id = tptLog.project_team_id )
                LEFT JOIN project_team sptSa ON (ae.source_team_id = sptSa.project_team_id )
                LEFT JOIN project_team tptSa ON (ae.target_team_id = tptSa.project_team_id )
                LEFT JOIN project_team sptDba ON (ae.source_team_id = sptDba.project_team_id )
                LEFT JOIN project_team tptDba ON (ae.target_team_id = tptDba.project_team_id )
				LEFT JOIN model m ON (ae.model_id = m.model_id )
				LEFT JOIN manufacturer mf ON (ae.manufacturer_id = mf.manufacturer_id )
                LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id and at.voided = 0 and at.type='process')
				where ae.project_id = $projectId and ae.move_bundle_id in ${bundles} GROUP BY ae.asset_entity_id ) ae WHERE  1 = 1""")
				
		if(column1Value !="" && column1Value!= null){
			if(column1Value == 'blank'){
				query.append(" and ae.${column1Field} = '' OR ae.${column1Field} is null")
			} else {
				def app = column1Value.replace("'","\\'")
				query.append(" and ae.${column1Field} like '%$app%'")
			}
		}
		if(column2Value!="" && column2Value!= null){
			if(column2Value == 'blank'){
				query.append(" and ae.${column2Field} = '' OR ae.${column2Field} is null")
			} else {
				def owner = column2Value.replace("'","\\'")
				query.append(" and ae.${column2Field} like '%$owner%'")
			}

		}	
		if(column3Value!="" && column3Value!= null){
			if(column3Value == 'blank'){
				query.append(" and ae.${column3Field} = '' OR ae.${column3Field} is null")
			} else {
				def sme = column3Value.toString().replace("'","\\'")
				query.append(" and ae.${column3Field} like '%$sme%'")
			}
		}
		if(column4Value!="" && column4Value!= null){
			if(column4Value == 'blank'){
				query.append(" and ae.${column4Field} = '' OR ae.${column4Field} is null")
			} else {
				def name = column4Value.toString().replace("'","\\'")
				query.append(" and ae.${column4Field} like '%$name%'")
			}
		}
		if(sortby != "" && sortby != null){
			query.append(" order by $sortby")
		}else {
			query.append(" order by updated")
		}
		if(order != "" && order != null){
			query.append(" $order ")
		}else {
			query.append(" desc ")
		}
		if(limit && limit != "all"){
			if(offset){
				query.append(" limit ${offset},${limit} ) a")
			} else {
				query.append(" limit ${limit} ) a")
			}
		} else {
			query.append(" ) a")
		}
		query.append(""" WHERE a.id in	( select t.asset_entity_id from asset_transition t where 
					(t.date_created between SUBTIME('$lastPoolTime','00:15:30') and '$currentPoolTime' OR t.last_updated between SUBTIME('$lastPoolTime','00:15:30') and '$currentPoolTime') )""")
		
		def resultList=jdbcTemplate.queryForList(query.toString())
		
		return resultList
	}
}
