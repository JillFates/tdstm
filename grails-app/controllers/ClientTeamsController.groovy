import grails.converters.JSON

import org.jsecurity.SecurityUtils

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil


class ClientTeamsController {
	
	def userPreferenceService
	def partyRelationshipService
	def stateEngineService 
	def securityService 
	def taskService
	def jdbcTemplate
	def clientTeamsService
	def workflowService

	def static final statusDetails = ["missing":"Unknown", "cabledDetails":"Cabled with Details","empty":"Empty","cabled":"Cabled"]
	protected static targetTeamColumns = ['MOVE_TECH':'target_team_id', 'CLEANER':'target_team_log_id','SYS_ADMIN':'target_team_sa_id',"DB_ADMIN":'target_team_dba_id']
	protected static sourceTeamColumns = ['MOVE_TECH':'source_team_id', 'CLEANER':'source_team_log_id','SYS_ADMIN':'source_team_sa_id',"DB_ADMIN":'source_team_dba_id']
	protected static targetTeamType = ['MOVE_TECH':'targetTeamMt', 'CLEANER':'targetTeamLog','SYS_ADMIN':'targetTeamSa',"DB_ADMIN":'targetTeamDba']
	protected static sourceTeamType = ['MOVE_TECH':'sourceTeamMt', 'CLEANER':'sourceTeamLog','SYS_ADMIN':'sourceTeamSa',"DB_ADMIN":'sourceTeamDba']
	
    def index = { redirect(action: list ,params:params) }
	/**
	 * @author : Lokanada Reddy
	 * @param :
	 * @return : List of teams that are belongs to current project, and if project user preference not exist list all teams
	 */
	def list = {
		def sourceTeams = []
		def targetTeams = []
		userPreferenceService.loadPreferences("CURR_PROJ")
		String projectId = session.getAttribute("CURR_PROJ")?.CURR_PROJ;
		def viewMode = params.viewMode
		if(!viewMode){
			viewMode = session.getAttribute("TEAM_VIEW_MODE") ? session.getAttribute("TEAM_VIEW_MODE") : 'web'
		}
		session.setAttribute("TEAM_VIEW_MODE", viewMode)
	
		def subject = SecurityUtils.subject
		def hasClientTeamsRole = RolePermissions.hasPermission("ClientTeamsList")
		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
		def now = GormUtil.convertInToGMT( "now","EDT" )
		def timeNow = now.getTime()
		def moveBundles = MoveBundle.getActiveBundlesByProject( Integer.parseInt(projectId), now )
		moveBundles.each{ moveBundle ->
			def bundleAssetsList = AssetEntity.findAllWhere( moveBundle : moveBundle )
			partyRelationshipService.getBundleTeamInstanceList( moveBundle ).each {
				if( hasClientTeamsRole || it.teamMembers.id.contains(loginUser.id) ){
					def teamId = it.projectTeam.id
					def headColor = 'done'
					def role = it.projectTeam?.role ? it.projectTeam?.role : "MOVE_TECH"
					def swimlane = Swimlane.findByNameAndWorkflow(role ? role : "MOVE_TECH", Workflow.findByProcess(moveBundle.workflowCode) )
					
					def hasSourceAssets = AssetEntity.find("from AssetEntity WHERE sourceTeamMt = $teamId OR sourceTeamSa = $teamId OR sourceTeamDba = $teamId")
					if(hasSourceAssets || role =="CLEANER"){
						def sourceAssetsList = bundleAssetsList.findAll{it[sourceTeamType.get(role)]?.id == teamId }
						def sourceAssets = sourceAssetsList.size()
						
						def minSource = swimlane.minSource ? swimlane.minSource : "Release"
						def minSourceId = Integer.parseInt( stateEngineService.getStateId( moveBundle.workflowCode, minSource ) )
						
						def maxSource = swimlane.maxSource ? swimlane.maxSource : "Unracked"
						def maxSourceId = Integer.parseInt( stateEngineService.getStateId( moveBundle.workflowCode, maxSource ) )
						
						def unrackingAssets = sourceAssetsList.findAll{it.currentStatus > minSourceId &&  it.currentStatus < maxSourceId }.size()
						
						if(role =="CLEANER"){
							minSourceId = Integer.parseInt( stateEngineService.getStateId( moveBundle.workflowCode, "Unracked" ) )
							maxSourceId = Integer.parseInt( stateEngineService.getStateId( moveBundle.workflowCode, "Cleaned" ) )
							unrackingAssets = sourceAssetsList.findAll{it.currentStatus == minSourceId }.size()
						}
						def sourceAvailassets = sourceAssetsList.findAll{it.currentStatus >= minSourceId && it.currentStatus < maxSourceId }.size()
						def unrackedAssets = sourceAssetsList.findAll{it.currentStatus >= maxSourceId }.size()
						
						if(unrackingAssets > 0 && sourceAssets > 0){
							headColor = 'process'
						} else if(sourceAvailassets > 0){
							headColor = 'ready'
						} else if(sourceAssets != unrackedAssets && sourceAssets > 0){
							headColor = 'pending'
						}
						sourceTeams << [team:it,moveBundle:moveBundle.name,cssClass:headColor]
					}
					headColor = "done"
					def hasTargetAssets = AssetEntity.find("from AssetEntity WHERE targetTeamMt = $teamId OR targetTeamSa = $teamId OR targetTeamDba = $teamId")
					if(hasTargetAssets && !(role =="CLEANER")){
						def minTarget = swimlane.minTarget ? swimlane.maxTarget : "Staged"
						def minTargetId = Integer.parseInt( stateEngineService.getStateId( moveBundle.workflowCode, minTarget ) )
						
						def maxTarget = swimlane.maxTarget ? swimlane.maxTarget : "Reracked"
						def maxTargetId = Integer.parseInt( stateEngineService.getStateId( moveBundle.workflowCode, maxTarget ) )
						def rerackingId = Integer.parseInt( stateEngineService.getStateId( moveBundle.workflowCode, "Reracking" ) )
						
						
						def targetAssetsList = bundleAssetsList.findAll{it[targetTeamType.get(role)]?.id == teamId }
						def targetAssets = targetAssetsList.size()
							
						def rerackingAssets = targetAssetsList.findAll{it.currentStatus > minTargetId &&  it.currentStatus < maxTargetId }.size()
						
						def rerackedAssets = targetAssetsList.findAll{it.currentStatus >= maxTargetId }.size()
								
						def targetAvailAssets = targetAssetsList.findAll{it.currentStatus >= minTargetId && it.currentStatus < maxTargetId }.size()
		
						
						if(rerackingAssets > 0 && targetAssets > 0){
							headColor = 'process'
						} else if(targetAvailAssets > 0){
							headColor = 'ready'
						} else if(targetAssets != rerackedAssets && targetAssets > 0){
							headColor = 'pending'
						}
						
						targetTeams << [team:it,moveBundle:moveBundle.name,cssClass:headColor]
					}
				}
			}
		}
/*		def headColor = 'done'
		if(projectTeam.currentLocation != "Target"){
			if(unrackingAssets > 0 && sourceAssets > 0){
				headColor = 'process'
			} else if(sourceAvailassets > 0){
				headColor = 'ready'
			} else if(sourceAssets != unrackedAssets && sourceAssets > 0){
				headColor = 'pending'
			}
		} else {
			if(rerackingAssets > 0 && targetAssets > 0){
				headColor = 'process'
			} else if(targetAvailAssets > 0){
				headColor = 'ready'
			} else if(targetAssets != rerackedAssets && targetAssets > 0){
				headColor = 'pending'
			}
		}*/
		def projectInstance = Project.get(projectId)
		def userId = session.getAttribute("LOGIN_PERSON").id
		def personInstance = Person.get(userId)
		def todo = AssetComment.findAll("From AssetComment a where a.project = :project AND commentType=:type AND assignedTo = :assignedTo AND (status is null OR status in('','Ready' , 'Started')) order by dueDate asc , dateCreated desc",[project:projectInstance,assignedTo:personInstance,type:'issue'])
		def cssTodo = todo.size() > 0 ? 'buttonTodo' : 'buttonBlank'
		if( viewMode != 'web'){
			render( view:'list_m', model:[ sourceTeams:sourceTeams , targetTeams:targetTeams, projectId:projectId,todo:todo.size(),cssTodo:cssTodo] )
		} else {
			return [ sourceTeams:sourceTeams , targetTeams:targetTeams , projectId:projectId,todo:todo.size(),cssTodo:cssTodo]
		}
		
	}
	/**
	 * @author : lokanada
	 * @params : userLogin in the form of role-bundleId-teamId-location.
	 * @return : Project, teamName, bundleName, TeamMambers.
	 */
	def home = {
		def viewMode = session.getAttribute("TEAM_VIEW_MODE")
		def bundleId = params.bundleId
		def teamId = params.teamId
		def location = params.location
		def projectTeamInstance = ProjectTeam.findById( teamId )
		def teamName = projectTeamInstance.name
		def teamMembers = partyRelationshipService.getTeamMemberNames( teamId )
		def bundleInstance = MoveBundle.findById( bundleId )
		if ( location == 'source' ) {
			projectTeamInstance.currentLocation = "Source"
			projectTeamInstance.save()
		} else if ( location == 'target' ) {
			projectTeamInstance.currentLocation = "Target"
			projectTeamInstance.save()
		}
		def projectId = session.getAttribute("CURR_PROJ")?.CURR_PROJ;
		
		if(viewMode != 'web'){
			   render ( view:'home_m',model:[ projectTeam:teamName, members:teamMembers, project:Project.read(projectId), loc:location, 
						bundleId:bundleId, bundleName:bundleInstance.name, teamId: teamId, location: location ])
		}else{
			 return [projectTeam:teamName, members:teamMembers, project:Project.read(projectId), loc:location, 
						bundleId:bundleId, bundleName:bundleInstance.name, teamId: teamId, location: location]
		}		
	}
	
	/**
	 * @author : lokanada
	 * @param  : bundleId,teamId,location,project,tab
	 * @return : Assets list
	 */
	def myTasks = {
		if ( params.fMess ) {
			flash.clear()
		}
		String message = flash.message
        def bundleId = params.bundleId
        def tab = params.tab
        def proAssetMap
        def teamId = params.teamId
        def stateVal
        def todoSize
        def allSize
        def assetList = []
        def colorCss
        def rdyState
		def viewMode = session.getAttribute("TEAM_VIEW_MODE")        
        def ipState = new ArrayList()
        def moveBundleInstance = MoveBundle.findById( bundleId )
		def projectTeam = ProjectTeam.read( teamId )
		def workflowCode = moveBundleInstance.project.workflowCode
		def role = projectTeam.role ? projectTeam.role : "MOVE_TECH"		
		def workflow = Workflow.findByProcess(workflowCode)
	    def swimlane = Swimlane.findByNameAndWorkflow(role, workflow )
		flash.message = ""
        def holdState = stateEngineService.getStateIdAsInt( workflowCode, "Hold" ) 
		
        def countQuery = """select a.asset_entity_id as id, a.asset_tag as assetTag, a.asset_name as assetName, a.source_rack as sourceRack, 
						a.source_rack_position as sourceRackPosition, a.target_rack as targetRack,
			            min(cast(t.state_to as UNSIGNED INTEGER)) as minstate,
			            a.target_rack_position as targetRackPosition, m.name as model, p.current_state_id as currentStateId 
			            from asset_entity a left join project_asset_map p on (a.asset_entity_id = p.asset_id) 
			            left join asset_transition t on(a.asset_entity_id = t.asset_entity_id and t.voided = 0)
        				left join model m on (a.model_id = m.model_id )
			            where a.move_bundle_id = $bundleId"""
        def query = new StringBuffer (countQuery)
        if ( params.location == "source" ) {
			def maxSource = swimlane.maxSource ? swimlane.maxSource : "Unracked" 
            stateVal = stateEngineService.getStateIdAsInt ( workflowCode, maxSource )
			def minSource = swimlane.minSource ? swimlane.minSource : "Release"
			rdyState = stateEngineService.getStateIdAsInt( workflowCode, minSource )
			
            query.append (" and a.${sourceTeamColumns.get(role)} = $teamId" )
            countQuery +=" and a.${sourceTeamColumns.get(role)} = $teamId"
        } else {
			def maxTarget = swimlane.maxTarget ? swimlane.maxTarget : "Reracked"
        	stateVal = stateEngineService.getStateIdAsInt ( workflowCode, maxTarget )
			def minTarget = swimlane.minTarget ? swimlane.maxTarget : "Staged"
			rdyState = stateEngineService.getStateIdAsInt( workflowCode, minTarget )
			query.append (" and a.${targetTeamColumns.get(role)} = $teamId" )
            countQuery += " and a.${targetTeamColumns.get(role)} = $teamId" 
        }
        allSize = jdbcTemplate.queryForList ( query.toString() + " group by a.asset_entity_id ").size()
        if ( tab == "Todo" ) {
            query.append (" and ( p.current_state_id < $stateVal or t.state_to = $holdState )")
        }
        query.append(" group by a.asset_entity_id ")
        if( params.sort != null ){
        	if( params.sort == "source_rack" ) {
        		query.append(" order by min(cast(t.state_to as UNSIGNED INTEGER)) = $holdState desc ,"+
        					"(p.current_state_id < ${stateVal} and p.current_state_id > $rdyState ) desc, p.current_state_id > $rdyState desc, "+
        					"p.current_state_id < $rdyState desc , a.$params.sort $params.order, a.source_rack_position $params.order" )
        	}else {
        		query.append(" order by min(cast(t.state_to as UNSIGNED INTEGER)) = $holdState desc ,"+
        					"(p.current_state_id < ${stateVal} and p.current_state_id > $rdyState ) desc, p.current_state_id > $rdyState desc, "+
        					"p.current_state_id < $rdyState desc , a.$params.sort $params.order" )
        	}
        }else {
        	query.append(" order by min(cast(t.state_to as UNSIGNED INTEGER)) = $holdState desc ,"+
        				"(p.current_state_id < ${stateVal} and p.current_state_id > $rdyState ) desc, p.current_state_id > $rdyState desc, "+
        				"p.current_state_id < $rdyState desc , a.source_rack, a.source_rack_position" )
        }
	 	proAssetMap = jdbcTemplate.queryForList ( query.toString() )
        todoSize = proAssetMap.size()
		def sortOrder = 5
        proAssetMap.each {
            if ( it.currentStateId ) {
                if ( it.minstate == holdState ) {
                    colorCss = "asset_hold"
					sortOrder = 1
                } else if ( it.currentStateId == rdyState ) {
                    colorCss = "asset_ready"
					sortOrder = 3
                } else if ( it.currentStateId < stateVal  && it.currentStateId > rdyState ) {
                    colorCss = "asset_process"
					sortOrder = 2
                } else if ( ( it.currentStateId > holdState ) && ( it.currentStateId < rdyState ) ) {
                    colorCss = "asset_pending"
					sortOrder = 4
                } else if ( ( it.currentStateId >= stateVal ) ) {
                    colorCss = "asset_done"
					sortOrder = 5
                }
            } else {
            	colorCss = "asset_pending"
				sortOrder = 4
            }
			def stateValue = stateEngineService.getState(workflow.process , it.currentStateId)
		
			def currentStateId = it.currentStateId
			
			def taskLists = stateEngineService.getTasks(workflow.process , role , stateValue)?.findAll{
										stateEngineService.getStateIdAsInt(workflow.process , it) > currentStateId }

			def nextStatus = taskLists.size() > 0 ? taskLists[0] : ""
			  
            assetList << [ item:it, cssVal:colorCss, sortOrder:sortOrder , nextStatus : nextStatus ]
			
        }
		   
		assetList.sort {
			it.sortOrder
		}
        if ( tab == "All" ) {
        	countQuery += " and (p.current_state_id < $stateVal or t.state_to = $holdState) group by a.asset_entity_id" 
            todoSize = jdbcTemplate.queryForList ( countQuery ).size()
            
        }
        if(!flash.message){
        	flash.message = message
        }
		
        if (viewMode !='web'){
            render (view:'myTasks_m', model:[ bundleId:bundleId, teamId:teamId, projectId:session.getAttribute("CURR_PROJ")?.CURR_PROJ, location:params.location, 
                    assetList:assetList, allSize:allSize, todoSize:todoSize, 'tab':tab])
		} else{ 
		      return[bundleId:bundleId, teamId:teamId,moveBundleInstance:moveBundleInstance, projectId:session.getAttribute("CURR_PROJ")?.CURR_PROJ, location:params.location, projectTeam:projectTeam,
              assetList:assetList, allSize:allSize, todoSize:todoSize, 'tab':tab,workflowCode:workflowCode,workflow:workflow,swimlane:swimlane]
		}
	}
	/**
	* @author : Lokanada
	* @param  : String search, String team, String location
	* @return : Searched asset details
	**/
   def assetSearch = {
	   if(flash.message?.contains("was not located")){
		   flash.clear()
	   }
	   flash.message= ""
	   def assetItem
	   def assetComment
	   def projMap
	   def teamId = params.teamId
	   def search = params.search
	   def bundleId = params.bundleId
	   def projectId = session.CURR_PROJ.CURR_PROJ
	   def viewMode = session.getAttribute("TEAM_VIEW_MODE")
	   def stateVal
	   def taskList
	   def taskSize
	   def label
	   def actionLabel
	   def checkHome = params.home
	   def moveBundleInstance = MoveBundle.findById( bundleId )
	   def loginTeam
	   if ( teamId ) {
		   loginTeam = ProjectTeam.findById( teamId )
	   }
	   def commentsList = clientTeamsService.getCommentsFromRemainderList( session )
	   if ( search != null ) {
		   def query = new StringBuffer ("from AssetEntity ae where ae.moveBundle=${moveBundleInstance.id} and ae.assetTag = :search ")
		   assetItem = AssetEntity.find( query.toString(), [ search : search ] )
		   if ( assetItem == null ) {
			   flash.message += message ( code : "<li>Asset Tag number '${search}' was not located</li>" )
			   if ( checkHome ) {
				   redirect ( action : 'index',
					   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
							   "location":params.location, "user":"mt"
							   ])
				   return;
			   } else {
				   redirect ( action : 'myTask',
					   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
							   "location":params.location,"tab":params.tab
							   ])
				   return;
			   }
		   } else {
			   def teamName
			   def teamIdFromDB
				   if ( params.location == "source" ) {
					   if ( assetItem[sourceTeamType.get(loginTeam.role)] ) {
						   teamIdFromDB = ( assetItem[sourceTeamType.get(loginTeam.role)]?.id ).toString()
						   teamName = assetItem[sourceTeamType.get(loginTeam.role)].name
					   } else {
						   flash.message += message ( code : "<li>The asset [${assetItem.assetName}] is not assigned to team [${loginTeam}] </li>" )
						   if ( checkHome ) {
							   redirect ( action: 'index',
								   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
										   "location":params.location, "user":"mt"
										   ])
							   return;
						   } else {
							   redirect ( action: 'myTasks',
								   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
										   "location":params.location, "tab":params.tab
										   ])
							   return;
						   }
					   }
				   } else {
					   if ( assetItem[targetTeamType.get(loginTeam.role)] ) {
						   teamIdFromDB = ( assetItem[targetTeamType.get(loginTeam.role)].id ).toString()
						   teamName = assetItem[targetTeamType.get(loginTeam.role)].name
					   } else {
						   flash.message += message( code : "<li>The asset [${assetItem.assetName}] is not assigned to team [${loginTeam}] </li>" )
						   if ( checkHome ) {
							   redirect ( action: 'index',
								   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
										   "location":params.location, "user":"mt"
										   ])
							   return;
						   } else {
							   redirect ( action: 'myTasks',
								   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
										   "location":params.location, "tab":params.tab
										   ])
							   return;
						   }
					   }
				   }
				   if ( teamIdFromDB != teamId ) {
					   flash.message += message ( code : "<li>The asset [${assetItem.assetName}] is assigned to team [${teamName}] </li>" )
				   }
				   def holdId = stateEngineService.getStateId( moveBundleInstance.project.workflowCode, "Hold" )
				   def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
																   "where t.asset_entity_id = ${assetItem.id} and voided = 0 and ( t.type = 'process' or t.state_To = $holdId ) "+
																   "order by date_created desc, stateTo desc limit 1 ")
				   projMap = ProjectAssetMap.findByAsset( assetItem )
				   if( !transitionStates.size() ) {
					   flash.message += message ( code :"<li> No actions for this asset </li>" )
					   if ( checkHome ) {
						   redirect ( action: 'index',
							   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
									   "location":params.location, "user":"mt"
									   ])
						   return;
					   } else {
						   redirect ( action: 'myTasks',
							   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
									   "location":params.location, "tab":params.tab
									   ])
						   return;
					   }
				   } else {
					   stateVal = stateEngineService.getState( moveBundleInstance.project.workflowCode, transitionStates[0].stateTo )
					   if ( stateVal == "Hold" ) {
						   flash.message += message ( code : "<li>The asset is on Hold. Please contact manager to resolve issue.</li>" )
						   if( checkHome ) {
							   redirect ( action: 'index',
								   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
										   "location":params.location, "user":"mt"
										   ])
							   return;
						   } else {
							   redirect ( action: 'myTasks',
								   params:["bundleId":bundleId, "teamId":teamId, "projectId":projectId,
										   "location":params.location, "tab":params.tab
										   ])
							   return;
						   }
					   }
					   taskList = stateEngineService.getTasks ( moveBundleInstance.project.workflowCode, loginTeam.role, stateVal )?.findAll{
										stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode, it) > stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode , stateVal)}
					   taskSize = taskList.size()
					   if ( taskSize == 1 ) {
						   if ( taskList.contains ( "Hold" ) ) {
							   flash.message += message ( code : "<li>NO ACTIONS FOR ASSET. You may place it on hold to alert the move coordinator </li>" )
						   } else {
							   actionLabel = taskList[0]
							   label =	stateEngineService.getStateLabel ( moveBundleInstance.project.workflowCode, stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode,actionLabel) )
						   }
					   } else if ( taskSize > 1 ) {
						   
						   taskList.each {
							   if ( it != "Hold" && !actionLabel) {
								   actionLabel = it
								   label =	stateEngineService.getStateLabel ( moveBundleInstance.project.workflowCode, stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode,it) )
								   return;
							   }
							   
						   }
					   } else {
					   		flash.message += message ( code : "<li>NO ACTIONS FOR ASSET. Please contact manager </li>" )
					   }
					   assetComment = AssetComment.findAllByAssetEntityAndCommentType( assetItem,'instruction' )
					   def stateLabel = stateEngineService.getStateLabel( moveBundleInstance.project.workflowCode, transitionStates[0].stateTo )
					   def modelConnectors
					   if(assetItem.model)
						   modelConnectors = ModelConnector.findAllByModel( assetItem.model )
						   
					   def assetCableMapList = AssetCableMap.findAllByFromAsset( assetItem )
					   def assetCablingDetails = []
					   assetCableMapList.each {
						   
						   def rackUposition = it.toConnectorNumber ? it.toAssetRack+"/"+it.toAssetUposition+"/"+it.toConnectorNumber.label : ""
						   if(it.fromConnectorNumber.type == "Power"){
							   rackUposition = it.toPower ? it.toAssetRack+"/"+it.toAssetUposition+"/"+it.toPower : ""
						   }
						   assetCablingDetails << [connector : it.fromConnectorNumber.connector, type:it.fromConnectorNumber.type,
												   labelPosition:it.fromConnectorNumber.labelPosition, label:it.fromConnectorNumber.label,
												   status:it.status,displayStatus:statusDetails[it.status], color:it.color ? it.color : "",
												   connectorPosX:it.fromConnectorNumber.connectorPosX, connectorPosY:it.fromConnectorNumber.connectorPosY,
												   hasImageExist:assetItem.model.rearImage && assetItem.model?.useImage ? true : false,
												   rackUposition : rackUposition ]
					   }
					   if (viewMode!='web'){
					       render ( view:'assetSearch_m',model:[ projMap:projMap, assetComment:assetComment?assetComment :"", stateVal:stateVal, bundleId:bundleId,
								   teamId:teamId, projectId:projectId, location:params.location, search:params.search, label:label,
								   actionLabel:actionLabel, commentsList: commentsList, stateLabel: stateLabel, assetCablingDetails : assetCablingDetails])
					   }else{
					         return [ projMap:projMap, assetComment:assetComment?assetComment :"", stateVal:stateVal, bundleId:bundleId,
								      teamId:teamId, projectId:projectId, location:params.location, search:params.search, label:label,
								      actionLabel:actionLabel, commentsList: commentsList, stateLabel: stateLabel, assetCablingDetails : assetCablingDetails]
					   
					   }
				   }
		   }
	   }
   }
   /**------------------------------------------------------------------------------------------------------
   * To do the transition
   * @author Lokanada
   * @param  assetComment, team, location, actionLabel, search, user
   * @return redirect to Asset details page if transition flag is busy otherwise redirect to asset task page
   *--------------------------------------------------------------------------------------------------------*/
   def doTransition = {
		def bundleId = params.bundleId
		def moveBundleInstance = MoveBundle.findById( bundleId )
		def query = new StringBuffer ("from AssetEntity ae where ae.moveBundle=${moveBundleInstance.id} and ae.assetTag = :search ")
		def asset = AssetEntity.find( query.toString(), [ search : params.search ] )
    	if(asset){
            def loginTeam = ProjectTeam.findById(params.teamId)
            def actionLabel = params.actionLabel
            //def projectAssetMap = ProjectAssetMap.findByAsset( asset )
            def holdId = stateEngineService.getStateId( moveBundleInstance.project.workflowCode, "Hold" )
            def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
                    										"where t.asset_entity_id = ${asset.id} and voided = 0 and ( t.type = 'process' or t.state_To = $holdId )"+
                											"order by date_created desc, stateTo desc limit 1 ")
            def currentState = ""
            if(transitionStates.size()){
            	currentState = stateEngineService.getState( moveBundleInstance.project.workflowCode, transitionStates[0].stateTo )
            }
            def flags = stateEngineService.getFlags( moveBundleInstance.project.workflowCode, loginTeam.role, currentState, actionLabel )
            def loginUser = UserLogin.findByUsername( SecurityUtils.subject.principal )
            def workflow = workflowService.createTransition( moveBundleInstance.project.workflowCode, loginTeam.role, actionLabel, asset, moveBundleInstance, loginUser, loginTeam, params.enterNote )
            if ( workflow.success ) {
            	if(params.location == 'source' && asset[sourceTeamType.get(loginTeam.role)].id != loginTeam.id ){
        			asset[sourceTeamType.get(loginTeam.role)] = loginTeam
					asset.save(flush:true)
        		} else if(params.location == 'target' && asset[targetTeamType.get(loginTeam.role)].id != loginTeam.id ){
        			asset[targetTeamType.get(loginTeam.role)] = loginTeam
					asset.save(flush:true)
        		}
            	if(flags?.contains("busy")){
            		flash.message = message ( code : workflow.message )
                    redirect ( action:'assetSearch', params:params)
            	} else {
            		redirect ( action: 'myTasks', 
            			params:[ "bundleId":params.bundleId, "teamId":params.teamId, "projectId":session.CURR_PROJ.CURR_PROJ,
            			         "location":params.location, "tab":"Todo" 
            			         ])
            	}
            } else {
                flash.message = message ( code : workflow.message )
                redirect ( action:'assetSearch',params:params)
            }
    	} else {
    		flash.message = 'Asset not found'
            redirect ( action:'assetSearch',params:params)
    	}
	}
   /**------------------------------------------------------------------------------
    * To change the state of an asset to hold
    * @author Lokanada Reddy
    * @param  String enterNote, String team, String location, String bundle
    * @return boolean for indication of transitions
    *------------------------------------------------------------------------------*/
   def placeOnHold = {
	  def enterNote = params.enterNote
	  def moveBundleInstance = MoveBundle.findById( params.bundleId )
	  if ( params.similarComment == 'nosimilar' ) {
		  clientTeamsService.appendCommentsToRemainderList( params, session )
	  }
	  def loginTeam = ProjectTeam.findById(params.teamId)
	  def role = loginTeam.role
	  def action = role != "CLEANER" ? "assetSearch" : "logisticsAssetSearch"
	  def query = new StringBuffer ("from AssetEntity ae where ae.moveBundle=${moveBundleInstance.id} and ae.assetTag = :search ")
	  def asset = AssetEntity.find( query.toString(), [ search : params.search ] )
	  def redirectAction = "myTasks"
	  if(asset){
		  action = role != "CLEANER" ? "myTasks" : "logisticsMyTasks"
		  def loginUser = UserLogin.findByUsername ( SecurityUtils.subject.principal )
		  def workflow
			  workflow = workflowService.createTransition ( moveBundleInstance.project.workflowCode, role, "Hold", asset,moveBundleInstance, loginUser, loginTeam, params.enterNote )
			  if ( workflow.success ) {
				 
				  if(params.location == 'source' && asset[sourceTeamType.get(role)].id != loginTeam.id ){
					  asse[sourceTeamType.get(role)] = loginTeam
					  asset.save(flush:true)
				  } else if(params.location == 'target' && asset[targetTeamType.get(role)]?.id != loginTeam.id ){
					  asset[targetTeamType.get(role)] = loginTeam
					  asset.save(flush:true)
				  }
				 
				  def assetComment = new AssetComment()
				  assetComment.comment = enterNote
				  assetComment.assetEntity = asset
				  assetComment.commentType = 'issue'
				  assetComment.category = 'moveday'
				  assetComment.createdBy = loginUser.person
				  assetComment.save()
				  redirect ( action: action,
					  			params:["bundleId":params.bundleId, "teamId":params.teamId, "projectId":session.CURR_PROJ.CURR_PROJ,
									  	"location":params.location, "tab":"Todo"
								  		])
			 } else {
				 flash.message = message ( code : workflow.message )
				 redirect ( action : action,
							 params:["bundleId":params.bundleId, "teamId":params.teamId, "projectId":session.CURR_PROJ.CURR_PROJ,
									 "location":params.location, "tab":"Todo"
									 ])
			 }

		 } else {
			flash.message = 'Asset not found'
            redirect ( action:action,params:params)
		 }
  	}
   /**----------------------------------------------------------------------------------
    * @author Lokanada Reddy
    * @param  String assetTag, project,bundle
    * @return Create a Comment for AssetEntity from client team station
    *----------------------------------------------------------------------------------*/
   def addComment = {
	   def moveBundleInstance = MoveBundle.findById( params.bundleId )
	   def loginTeam = ProjectTeam.findById(params.teamId)
	   def role = loginTeam.role
	   def action = role != "CLEANER" ? "assetSearch" : "logisticsAssetSearch"
	   def loginUser = UserLogin.findByUsername ( SecurityUtils.subject.principal )
	   def query = new StringBuffer ("from AssetEntity ae where ae.moveBundle=${moveBundleInstance.id} and ae.assetTag = :search ")
	   def asset = AssetEntity.find( query.toString(), [ search : params.search ] )
	   if(asset){
		   def assetComment = new AssetComment()
		   assetComment.comment = params.enterNote
		   if ( params.similarComment == "nosimilar" ) {
			   clientTeamsService.appendCommentsToRemainderList( params, session )
		   }
		   assetComment.assetEntity = asset
		   assetComment.commentType = 'comment'
		   assetComment.category = 'moveday'
		   assetComment.createdBy = loginUser.person
		   assetComment.save()
	   } else {
	   	   flash.message = "Asset not found"
	   }
	   redirect ( action: action, params:params)
   }
   /**--------------------------------------------
    * @author lokanada
    * @params : logistics team Id, bundle id
    * @return : Team details
    *--------------------------------------------*/
   def logisticsHome = {
	   def bundleId = params.bundleId
	   def teamId = params.teamId
	   def location = params.location
	   def projectTeamInstance = ProjectTeam.findById( teamId )
	   def teamMembers = partyRelationshipService.getTeamMemberNames( teamId )
	   def bundleInstance = MoveBundle.findById(params.bundleId)	
	   projectTeamInstance.currentLocation = "Source"
	   projectTeamInstance.save()
	   def teamLocation = projectTeamInstance.currentLocation

	 render ( view:'logisticsHome' , model:[ projectTeam:projectTeamInstance, members:teamMembers, projectId:session.CURR_PROJ.CURR_PROJ,
												   loc:teamLocation, bundleId:params.bundleId ,bundleName:bundleInstance.name, teamId:teamId,
												   location:location, project: bundleInstance.project])
			 
   }
   /**-------------------------------------------------------------------------------------------
    * To view the list of assets for that particular bundleId, teamId and location on logistics screen
    * @author Mallikarjun
    * @param  String bundleId, String teamId, String location
    * @return Array of arguments  
    *--------------------------------------------------------------------------------------------*/
   def logisticsMyTasks = {
	   def bundleId = params.bundleId
	   def tab = params.tab
	   def teamId = params.teamId
	   def assetList = []
	   def colorCss
	   def issuecomments
	   def assetIssueCommentListSize
	   def moveBundleInstance = MoveBundle.findById( bundleId )
	   def query = new StringBuffer("""select a.asset_entity_id as id, a.asset_tag as assetTag,
				 a.source_rack as sourceRack, a.source_rack_position as sourceRackPosition,
				 a.target_rack as targetRack, a.target_rack_position as targetRackPosition,
				 min(cast(t.state_to as UNSIGNED INTEGER)) as minstate,
				 m.name as model, p.current_state_id as currentStateId from asset_entity a
				 left join asset_transition t on(a.asset_entity_id = t.asset_entity_id and t.voided = 0)
				 left join project_asset_map p on (a.asset_entity_id = p.asset_id)
				 left join model m on (a.model_id = m.model_id)
				 where a.move_bundle_id = $bundleId """)
		 
	   def stateVal = stateEngineService.getStateId ( moveBundleInstance?.project.workflowCode, "Cleaned" )
	   def allSize = jdbcTemplate.queryForList( query.toString() +" group by a.asset_entity_id" ).size()
	   def holdState = stateEngineService.getStateIdAsInt( moveBundleInstance?.project.workflowCode, "Hold" )
	   if ( tab == "Todo" ) {
		   query.append ( " and ( p.current_state_id < $stateVal or t.state_to = $holdState )" )
	   }
	   def proAssetMap = jdbcTemplate.queryForList ( query.toString() +" group by a.asset_entity_id" )
	   def todoSize = proAssetMap.size()
	   def rdyState = stateEngineService.getStateIdAsInt( moveBundleInstance?.project.workflowCode, "Cleaned" )
	   def ipState = stateEngineService.getStateIdAsInt( moveBundleInstance?.project.workflowCode, "Unracked" )
	   def sortOrder = 4
	   proAssetMap.each {
		   if ( it.currentStateId ) {
			   if ( it.minstate == holdState ) {
				   colorCss = "asset_hold"
				   sortOrder = 1
			   } else if ( it.currentStateId == ipState ) {
			   		colorCss = "asset_ready"
					sortOrder = 2
			   } else if ( ( it.currentStateId > holdState ) && ( it.currentStateId < ipState ) ) {
			   		colorCss = "asset_pending"
					sortOrder = 3
			   } else if ( ( it.currentStateId >= rdyState ) ) {
			   		colorCss = "asset_done"
					sortOrder = 4
			   }
		   } else {
		   		colorCss = "asset_pending"
				sortOrder = 3
		   }
		   		assetList << [ item:it, cssVal:colorCss, sortOrder:sortOrder ]
	   }
	   assetList.sort {
		   it.sortOrder
	   }
	   if ( tab == "All" ) {
		   query.append (" and (p.current_state_id < $stateVal or t.state_to = $holdState ) group by a.asset_entity_id")
		   todoSize = jdbcTemplate.queryForList ( query.toString() ).size()
	   }
	   def assetIssueCommentList
	   if ( params.issueAssetId ) {
		   def assetItem = AssetEntity.findById( params.issueAssetId )
		   assetIssueCommentList = AssetComment.findAll("from AssetComment ac where ac.assetEntity = ${assetItem.id} and ac.commentType = 'issue' and ac.isResolved = 0 ")
		   assetIssueCommentListSize = assetIssueCommentList.size()
	   }
	   return[ bundleId:bundleId, teamId:teamId, projectId:session.CURR_PROJ.CURR_PROJ, location:params.location,
		   		assetList:assetList, allSize:allSize, todoSize:todoSize, 'tab':tab, issuecomments: assetIssueCommentList,
				assetIssueCommentListSize: assetIssueCommentListSize
	   		]
   }
   /**-----------------------------------------------------------------------------
    * To view the details and to change the state of an asset for logistics screen
   	* @author Mallikarjun
    * @param  String search, String teamId, String location
    * @return Array of arguments   
    *-----------------------------------------------------------------------------*/
	def logisticsAssetSearch = {
		def browserTest = false
		if ( !request.getHeader( "User-Agent" ).contains( "MSIE" ) ) {
			browserTest = true
		}
		def textSearch = params.textSearch
		def assetItem
		def assetComment
		def projMap = []
		def teamId = params.teamId
		def search = params.search
		if ( textSearch ) {
			search = textSearch
		}
		def stateVal
		def taskList
		def taskSize
		def label
		def actionLabel
		def teamMembers
		def loginTeam
		def issuecomments
		def assetIssueCommentListSize
		def moveBundleId = params.bundleId
		moveBundleId = moveBundleId ? moveBundleId : session.getAttribute( "CURR_BUNDLE" )?.CURR_BUNDLE
		def moveBundleInstance = MoveBundle.findById( moveBundleId )
		if ( teamId ) {
			loginTeam = ProjectTeam.findById ( params.teamId )
		}
		def commentsList = clientTeamsService.getCommentsFromRemainderList( session )
		flash.message = ""
		if ( params.menu == "true" ) {
			render(view:'logisticsAssetSearch',
					model:[ projMap:projMap, assetComment:assetComment, stateVal:stateVal, bundleId:moveBundleId,
						teamId:params.teamId, projectId:session.CURR_PROJ.CURR_PROJ, location:params.location, search:search,
						label:label, actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList
					])
			return;
		} else if ( search != null ) {
			def query = "from AssetEntity where moveBundle=${moveBundleInstance.id} and assetTag = :search "
			assetItem = AssetEntity.find ( query.toString(), [ search : search ] )
			if ( assetItem == null ) {
				flash.message = message ( code : "Asset Tag number '${search}' was not located" )
				if ( textSearch ) {
					render ( view:'logisticsAssetSearch',
							model:[ projMap:projMap, assetComment:assetComment, stateVal:stateVal, bundleId:moveBundleId,
								teamId:params.teamId, projectId:session.CURR_PROJ.CURR_PROJ, location:params.location,
								search:search, label:label, actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList
							])
					return;
				} else {
					redirect( action:'logisticsTask',
							params:[ "bundleId":moveBundleId, "teamId":params.teamId, "projectId":session.CURR_PROJ.CURR_PROJ,
								"location":params.location, "tab":params.tab
							])
					return;
				}
			} else {
				teamMembers = partyRelationshipService.getTeamMemberNames( assetItem.sourceTeamMt?.id )
				def membersCount = ( ( teamMembers.toString() ).tokenize("/") ).size()
				teamMembers = membersCount + "(" + teamMembers.toString() + ")"
				def bundleId = assetItem.moveBundle?.id
				def holdId = stateEngineService.getStateId( moveBundleInstance.project.workflowCode, "Hold" )
				def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
						"where t.asset_entity_id = ${assetItem.id} and voided = 0 and ( t.type = 'process' or t.state_To = $holdId )"+
						"order by date_created desc, stateTo desc limit 1 ")
				projMap = ProjectAssetMap.findByAsset( assetItem )
				if( !transitionStates.size()) {
					flash.message = message ( code : " No actions for this asset " )
					if ( textSearch ) {
						render(view:'logisticsAssetSearch',
								model:[ teamMembers:teamMembers, projMap:projMap, assetComment:assetComment, stateVal:stateVal,
									bundleId:moveBundleId, teamId:params.teamId, projectId:session.CURR_PROJ.CURR_PROJ, location:params.location,
									search:search, label:label, actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList
								])
						return;
					} else {
						redirect ( action: 'logisticsMyTasks',
								params:[ "bundleId":moveBundleId, "teamId":params.teamId, "projectId":session.CURR_PROJ.CURR_PROJ,
									"location":params.location,"tab":params.tab
								])
						return;
					}
				} else {
					stateVal = stateEngineService.getState ( moveBundleInstance.project.workflowCode, transitionStates[0].stateTo )
					if ( stateVal == "Hold" ) {
						def assetIssueCommentList = AssetComment.findAll("from AssetComment ac where ac.assetEntity = ${assetItem.id} and ac.commentType = 'issue' and ac.isResolved = 0")
						assetIssueCommentListSize = assetIssueCommentList.size()
						flash.message = message ( code :"The asset is currently on HOLD because: " )
						if ( textSearch ) {
							render ( view:'logisticsAssetSearch',
									model:[	teamMembers:teamMembers, projMap:projMap,assetComment:assetComment, stateVal:stateVal,
										bundleId:moveBundleId, teamId:params.teamId, projectId:session.CURR_PROJ.CURR_PROJ, location:params.location,
										search:search, label:label, actionLabel:actionLabel, browserTest:browserTest,
										issuecomments: assetIssueCommentList, assetIssueCommentListSize: assetIssueCommentListSize,
										commentsList: commentsList
									])
							return;
						} else {
							redirect ( action: 'logisticsMyTasks',
									params:[ "bundleId":moveBundleId, "teamId":params.teamId, "projectId":session.CURR_PROJ.CURR_PROJ,
										"location":params.location,"tab":params.tab, "issueAssetId" : String.valueOf( assetItem.id )
									])
							return;
						}
					}
					taskList = stateEngineService.getTasks ( moveBundleInstance.project.workflowCode, "CLEANER", stateVal )
					taskSize = taskList.size()
					if ( taskSize == 1 ) {
						if ( taskList.contains ( "Hold" ) ) {
							flash.message = message ( code : "NO ACTIONS FOR ASSET. You may place it on hold to alert the move coordinator" )
						} else {
							actionLabel = taskList[0]
							label =	stateEngineService.getStateLabel ( moveBundleInstance.project.workflowCode, stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode,actionLabel) )
						}

					} else if ( taskSize > 1 ) {
						actionLabel = taskList.find{it=="Cleaned"}
						label =	!actionLabel ?: stateEngineService.getStateLabel ( moveBundleInstance.project.workflowCode, stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode,actionLabel) )
					}
					assetComment = AssetComment.findAllByAssetEntityAndCommentType( assetItem,'instruction' )
					def cleanedId = stateEngineService.getStateIdAsInt( moveBundleInstance.project.workflowCode, "Cleaned" )
					def cartAssetCountQuery = new StringBuffer(" select count(a.asset_entity_id) as assetCount from asset_entity a "+
							"left join project_asset_map p on ( a.asset_entity_id = p.asset_id  ) " +
							"where a.cart = '$assetItem.cart' and a.move_bundle_id = $bundleId "+
							"and p.project_id = $assetItem.project.id and p.current_state_id < $cleanedId ")
					def cartAssetCount = jdbcTemplate.queryForInt( cartAssetCountQuery.toString() )
					def cartQty
					if ( cartAssetCount == 1 ) {
						def totalCartAssetCountQuery = new StringBuffer(" select count(a.asset_entity_id) as assetCount from asset_entity a "+
								"where a.cart = '$assetItem.cart' and a.move_bundle_id = $bundleId "+
								"and a.project_id = $assetItem.project.id ")
						cartQty = jdbcTemplate.queryForInt( totalCartAssetCountQuery.toString() )
						flash.message = "This is the last asset for cart "+assetItem.cart+" which should contain "+cartQty+" assest(s)"
					}
					render ( view:'logisticsAssetSearch',
							model:[ teamMembers:teamMembers, projMap:projMap, assetComment:assetComment ? assetComment :"", stateVal:stateVal, bundleId:moveBundleId,
								teamId:params.teamId, projectId:session.CURR_PROJ.CURR_PROJ, location:params.location, search:search, label:label,
								actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList, cartQty: cartQty
							])
				}
			}
		}

	}
	/**--------------------------------------------------------------------------------------------------------
	 * To change the state of an asset to CLEANER
	 * @author lokanada
	 * @param  String assetComment, String teamId, String location, String actionLabel, String search, String user
	 * @return Message with boolean for indication of transitions
	 *-------------------------------------------------------------------------------------------------------*/
	def cleaning = {
		def query = "from AssetEntity where moveBundle=${params.bundleId} and assetTag = :search "
		def asset = AssetEntity.find ( query.toString(), [ search : params.search ] )
		if(asset){
			def bundleId = asset.moveBundle
			//def moveBundleInstance = MoveBundle.findById( params.bundleId )
			def actionLabel = params.actionLabel
			def loginUser = UserLogin.findByUsername ( SecurityUtils.subject.principal )
			def loginTeam = ProjectTeam.findById(params.teamId)
			def workflow = workflowService.createTransition ( asset.project.workflowCode, "CLEANER", actionLabel, asset, bundleId, loginUser, loginTeam, params.enterNote )
			if ( workflow.success ) {
				def projMap = []
				def stateVal = null
				def label = null
				actionLabel = null
				render(view: 'logisticsAssetSearch',
						model:[ projMap:projMap, stateVal:stateVal, "search":params.search, "bundleId":params.bundleId,
							"teamId":params.teamId, "projectId":session.CURR_PROJ.CURR_PROJ, "location":params.location, "tab":"Todo", label:label,
							actionLabel:actionLabel
						])
			} else {
				flash.message = message ( code : workflow.message )
				redirect ( action:'logisticsAssetSearch',
						params:[ "bundleId":params.bundleId, "teamId":params.teamId, "projectId":session.CURR_PROJ.CURR_PROJ, "location":params.location,
							"search":params.search, "label":params.label, "actionLabel":actionLabel
						])
			}
		} else {
			flash.message = "Asset not found"
			redirect ( action:'logisticsAssetSearch', params:params)
		}
	}

	/**--------------------------------------------------------------------------------------
	 * To cancel asset search in logistics screen
	 * @author Lokanada
	 * @param  String actionLabel, String teamId, String location, String search, String user
	 * @return render the logisticsAssetSearch with required params
	 *--------------------------------------------------------------------------------------*/
	def cancelAssetSearch = {
		def query = "from AssetEntity where moveBundle=${params.bundleId} and assetTag = :search "
		def asset = AssetEntity.find ( query.toString(), [ search : params.search ] )
		if(asset){
			def bundleId = asset.moveBundle
			def teamId
			def projMap = []
			render(view: 'logisticsAssetSearch',
					model:[	projMap:projMap, "search":params.search, "bundleId":params.bundleId, "teamId":params.teamId,
						"projectId":session.CURR_PROJ.CURR_PROJ, "location":params.location, "tab":"Todo" ])
		} else {
			flash.message = "Asset not found"
			redirect ( action:'logisticsAssetSearch', params:params)
		}
	}
	
	/**
	 * Generates the user's list of tasks for the current project
	 * params:
	 *		tab - all or todo 
	 */
	def listComment={

		def project = securityService.getUserCurrentProject()
		log.error "PROJECT: ${project}"
		def person = securityService.getUserLoginPerson()
		log.error "PERSON=${person}"
		def allTasks = false

		// Parameters 		
		def tab
		def listComment
		def todo
		def all
		
		// Deal with the user preferences
		def viewMode = params.viewMode
		def search = params.search
		def sort = params.sort
		
		if (viewMode) { 
			session.setAttribute('TASK_VIEW_MODE', viewMode)
		}
		if (search == null) {
			session.removeAttribute("TASK_VIEW_SORT_ON")
			session.removeAttribute("TASK_VIEW_SORT_ORDER")
		}
		if (sort) {
			session.setAttribute("TASK_VIEW_SORT_ON", params.sort)
			session.setAttribute("TASK_VIEW_SORT_ORDER", params.order)
		}

		def sortOn = session.getAttribute("TASK_VIEW_SORT_ON") ? session.getAttribute("TASK_VIEW_SORT_ON") : 'dueDate'
		def sortOrder = session.getAttribute("TASK_VIEW_SORT_ORDER") ? session.getAttribute("TASK_VIEW_SORT_ORDER") : 'ASC , lastUpdated DESC'

		// Use the taskService.getUserTasks service to get all of the tasks [all,todo]
		def tasks = taskService.getUserTasks(person, project, search, sortOn, sortOrder )
		
		// Get the size of the lists
		def todoSize = tasks['todo'].size()
		def allSize = tasks['all'].size()

		// Based on which tab the user is viewing we'll set listComment to the appropriate list to be returned to the user
		if (params.tab=='all') {
			tab = 'all'
			listComment = tasks['all']
			allTasks = true
		} else {
			tab = 'todo'
			listComment = tasks['todo']
		}
		
		// Build the list and associate the proper CSS style
		def issueList = []
		listComment.each{ task ->
			def css = taskService.getCssClassForStatus( task.status )
			issueList << ['item':task,'css':css]
		}

		// Determine the model and view
		// TODO: runbook : WHY ARE WE PASSING THE userId back to the browser?
		def model = [listComment:issueList, tab:tab, todoSize:todoSize, allSize:allSize, search:search, userId:5]
		def view = 'myIssues'
		if ( session.getAttribute('ISSUE_VIEW_MODE') == 'mobile') {
			view = view+'_m'
		} else {
			model << [timers:session.MY_ISSUE_REFRESH?.MY_ISSUE_REFRESH]
		}
		// Send the user on his merry way
		render (view:view, model:model)
		
	}
	/**
	 * @author : Ross Macfarlane
	 * @return : The number of tasks assigned to the current user
	 */
	def getToDoCount={
		def projectId = session.CURR_PROJ.CURR_PROJ
		def projectInstance = Project.get(projectId)
		def userId = session.getAttribute("LOGIN_PERSON").id
		def personInstance = Person.get(userId)
		def todoSize = AssetComment.findAll("From AssetComment a where a.project = :project AND commentType=:type AND assignedTo = :assignedTo AND (status is null OR status in('','Ready' , 'Started')) ",[project:projectInstance,assignedTo:personInstance,type:'issue']).size()
		def map = []
		map << [count:todoSize]
		render map as JSON
	}
	def showIssue={
		def assetComment = AssetComment.get(params.issueId)
		def noteList = assetComment.notes.sort{it.dateCreated}
		def notes = []
		noteList.each{
			def dateCreated = it.dateCreated.format("E, d MMM 'at ' HH:mma")
			notes << [dateCreated , it.createdBy.toString() ,it.note]
		}
		def viewMode = session.getAttribute('ISSUE_VIEW_MODE')
		if(viewMode=='mobile'){
			render (view:'showIssue_m',model:['assetComment':assetComment,notes:notes])
		}else{
			return[assetComment:assetComment,notes:notes]
		}
	}
}
