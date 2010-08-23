import com.tdssrc.grails.GormUtil
/*
 *  This Service is responsible for managing the workflow and status of assets in the system.
 */
class WorkflowService {
    boolean transactional = true
    def stateEngineService
    def jdbcTemplate
    /*
     *  Used to create the Asset Transaction 
     */
    def createTransition( def process, def role, def toState, def assetEntity, def moveBundle, def userLogin, def projectTeam, def comment ) {
    	/*
    	The method should verify that:
		1. get the current state of the assetEntity from projectAssetMap
		2. verify that the ROLE can change the state from / to (should implement method canDoTask(Process, ROLE, from, to) on the StateEngineService.
		3. get the flags for the from/to state for the role
		4. if there is a comment or issue flag, then the comment string must contain text otherwise error.
    	If verification is successful then do the following
		1. create AssetTransition
		2. Update ProjectAssetMap currentState to the toState.id
			a. Need to update the StateEngine to read new property "id"
			b. Need to change ProjectAssetMap domain currentState to currentStateId (Integer)
		3. Update ProjectTeam
			a. set isIdle based on "busy" flag for the task. isIdle = ! "busy"
		 */
		boolean success = false
    	def currentState
    	def flag
    	def verifyFlag = true
    	def message
    	def projectAssetMap = ProjectAssetMap.findByAsset( assetEntity )
    	def stateType = stateEngineService.getStateType( process, toState )
    	if ( projectAssetMap ) {
    		def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
    														"where t.asset_entity_id = ${assetEntity.id} and voided = 0 order by date_created desc, stateTo desc limit 1 ")
			if(transitionStates.size()){
				currentState = transitionStates[0].stateTo
			}
    		if(stateEngineService.getState( process, currentState ) != "Hold"){
    			currentState = projectAssetMap.currentStateId
    		}
	    	def fromState = stateEngineService.getState( process, currentState )
	    	def roleCheck = stateEngineService.canDoTask( process, role, fromState, toState )
	    	// Check whether role has permission to change the State
	    	if ( roleCheck ) {
	    		flag = stateEngineService.getFlags(process, role, fromState, toState)
	    		if ( flag.contains("comment") || flag.contains("issue") ) {
	        		if ( ! comment ) {
	        			verifyFlag = false
	        			message = "A comment is required"
	        		}
	        	}
	    		//	If verification is successful then create AssetTransition and Update ProjectTeam
	        	if ( verifyFlag ) {
	        		AssetTransition.withTransaction { status ->
		        		def state = stateEngineService.getStateId( process, toState )
						def lastState
						if(stateType != "boolean"){
							lastState = doPreExistTransitions(process, currentState.toString(), state, assetEntity, moveBundle, projectTeam, userLogin)
						}
						lastState = lastState ? lastState : currentState.toString()
								
		        		def assetTransition = new AssetTransition( stateFrom:lastState, stateTo:state, comment:comment, assetEntity:assetEntity, moveBundle:moveBundle, projectTeam:projectTeam, userLogin:userLogin, type:stateType )
						
		        		if ( !assetTransition.validate() || !assetTransition.save() ) {
		    				message = "Unable to create AssetTransition: " + GormUtil.allErrorsString( assetTransition )
		    			} else {
		    				def holdState = stateEngineService.getStateId( process, 'Hold' )
		    				/* Set preexisting AssetTransitions as voided where stateTo >= new state */
		    				setPreExistTransitionsAsVoided( process, assetEntity, state, assetTransition, stateType, holdState)
		    				/* Set time elapsed for each transition */
		    				message = setTransitionTimeElapsed(assetTransition)
		    				message = "Transaction created successfully"
		    				if(projectTeam){
								projectTeam.isIdle = flag.contains('busy') ? 1 : 0
								projectTeam.latestAsset = assetEntity
								projectTeam.save()
		    				}
		    				if(stateType != "boolean"){
		    					projectAssetMap.currentStateId = Integer.parseInt(stateEngineService.getStateId( process, toState ))
		    				}
		    				projectAssetMap.save(flush:true)
							// store the current status into asset 
							if(stateType != "boolean" || toState == "Hold"){
								assetEntity.currentStatus = Integer.parseInt(stateEngineService.getStateId( process, toState ))
								assetEntity.save(flush:true)
		    				}
							success = true
						}
						if(!success){
							status.setRollbackOnly()
						}
	        		}
	        	}
	    	} else {
	    		message = "$role does not have permission to change the State"
	    	}
    	} else {
    		def state = stateEngineService.getStateId( process, toState )
    		if ( toState == "Hold" ) {
	        	if ( ! comment ) {
	        		verifyFlag = false
	        		message = "A comment is required"
	        	}
	        }
    		if ( verifyFlag ) {
		        def assetTransition = new AssetTransition( stateFrom:state, stateTo:state, comment:comment, assetEntity:assetEntity, moveBundle:moveBundle, projectTeam:projectTeam, userLogin:userLogin, type:stateType )
		        if ( !assetTransition.validate() || !assetTransition.save() ) {
		    		message = "Unable to create AssetTransition: " + GormUtil.allErrorsString( assetTransition )
		    	} else {
		    		message = setTransitionTimeElapsed(assetTransition)
		    		if(stateType != "boolean"){
			    		def projectAssetMapInstance = new ProjectAssetMap(project:moveBundle.project, asset:assetEntity)
			    		projectAssetMapInstance.currentStateId = Integer.parseInt(stateEngineService.getStateId( process, toState ))
			    		projectAssetMapInstance.save()
		    		}
		    		// store the current status into asset 
					if(stateType != "boolean" || toState == "Hold"){
						assetEntity.currentStatus = Integer.parseInt(stateEngineService.getStateId( process, toState ))
						assetEntity.save(flush:true)
    				}
		    		message = "Transaction created successfully"
					success = true
				}
    		}
    	}
    	return [success:success, message:message]
    }
    /*----------------------------------------------------
     * @author : Lokanath Reddy
     * @param  : AssetTransition Object
     * @return : Set the Transition TimeElapsed 
     *---------------------------------------------------*/
    def setTransitionTimeElapsed( def assetTransition ){
    	def message
    	def lastTransition =  AssetTransition.executeQuery(" select max(id) from AssetTransition where state_to = :stateTo "+
    														"and assetEntity = :asset ", [stateTo:assetTransition.stateFrom, asset:assetTransition.assetEntity ])[0]
    	if(lastTransition){
    		def previousTransition = AssetTransition.findById(lastTransition)
    		def timeDiff = assetTransition.dateCreated.getTime() - previousTransition.dateCreated.getTime()
    		if(timeDiff != 0){
    			assetTransition.timeElapsed = timeDiff
        		if ( !assetTransition.validate() || !assetTransition.save() ) {
					message = "Unable to create AssetTransition: " + GormUtil.allErrorsString( assetTransition )
    		    } 
    		}
    	}
    	return message
    }
    /*--------------------------------------------------------------------------------
     * @author : Lokanath Reddy
     * @param  : AssetEntity , stateTo
     * @return : Set preexisting AssetTransitions as voided where stateTo >= new state
     * -----------------------------------------------------------------------------*/
    def setPreExistTransitionsAsVoided( def process, def assetEntity, def state, def assetTransition, def stateType, def holdState ) {
    	if(stateType != "boolean"){
	    	def preExistTransitions = AssetTransition.findAll("from AssetTransition where assetEntity = $assetEntity.id and ( stateTo >= $state or stateTo = $holdState ) "+
	    														"and id != $assetTransition.id and stateTo not in(select w.transId from WorkflowTransition w "+
	    														"where w.process = '$process' and w.transId != $holdState and w.type != 'process' ) ")
	    	preExistTransitions.each{
	    		it.voided = 1
	    		it.save()
	    	}
    	}
    }
    /*-------------------------------------------------------------------------------
     * 
     * When moving a given asset from one workflow status to another will do pre existing transitions if there are steps in between. 
	 	If selected transition failed then this transitions will be rolled back at called place. 
	 	For example, if current status is at 30 and the user wants to go to 33, fill in 31 and 32 with the same time as the 33 time. 
		This assumes 30-33 are all in the workflow.
     * 
     * @author : Lokanath Reddy
     * @param  : process, stateFrom, stateTo,  assetEntity, moveBundle, projectTeam, userLogin
     * @return : last transition stateTo id.
     *-----------------------------------------------------------------------------*/
     def doPreExistTransitions( def process, def stateFrom, def stateTo,  def assetEntity, def moveBundle, def projectTeam, def userLogin ){
	 	
	 	// Skip the steps when setting asset to Completed once user set "VM Completed".
		def lastTransition = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
    														"where t.asset_entity_id = ${assetEntity.id} order by date_created desc, stateTo desc limit 1 ")
		if(lastTransition[0] && stateEngineService.getState( process, lastTransition[0].stateTo ) == "VMCompleted" &&
			stateEngineService.getState( process, Integer.parseInt(stateTo) ) == "Completed"){
			return stateFrom
		}
	 	
    	def min = Integer.parseInt(stateFrom) + 1
		def max = Integer.parseInt(stateTo)
		
		def processTransitions = stateEngineService.getTasks( process, "TASK_ID")
		def currentState = stateFrom

		for (int i = min; i < max ; i++){
			 if( processTransitions.contains( i.toString()) ){
				 def stateType = stateEngineService.getStateType( process, stateEngineService.getState( process, i ) )
				 if(stateType != "boolean"){
					 def assetTransition = new AssetTransition( stateFrom:currentState, stateTo:i.toString(), comment:"", assetEntity:assetEntity, moveBundle:moveBundle, projectTeam:projectTeam, userLogin:userLogin, type:"process" )
					 if ( assetTransition.validate() && assetTransition.save(flush:true) ) {
						 currentState = i.toString();
		    		 }
				 }
			 }
			
		}
		return currentState
     }
}
