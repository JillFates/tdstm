/*
 *  This Service is responsible for managing the workflow and status of assets in the system.
 */
class WorkflowService {
    boolean transactional = true
    def stateEngineService
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
    	if ( projectAssetMap ) {
    		currentState = projectAssetMap.currentStateId
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
	        		def state = stateEngineService.getStateId( process, toState )
	        		def assetTransition = new AssetTransition( stateFrom:currentState, stateTo:state, comment:comment, assetEntity:assetEntity, moveBundle:moveBundle, projectTeam:projectTeam, userLogin:userLogin )
	        		if ( !assetTransition.validate() || !assetTransition.save() ) {
	    				message = "Unable to create AssetTransition: " + GormUtil.allErrorsString( assetTransition )
	    			} else {
	    				/* Set preexisting AssetTransitions as voided where stateTo >= new state */
	    				setPreExistTransitionsAsVoided( assetEntity, state )
	    				/* Set time elapsed for each transition */
	    				message = setTransitionTimeElapsed(assetTransition)
	    				message = "Transaction created successfully"
	    				if(projectTeam){
							projectTeam.isIdle = flag.contains('busy') ? 1 : 0
							projectTeam.save()
	    				}
	    				projectAssetMap.currentStateId = Integer.parseInt(stateEngineService.getStateId( process, toState ))
	    				projectAssetMap.save()

						success = true
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
		        def assetTransition = new AssetTransition( stateFrom:state, stateTo:state, comment:comment, assetEntity:assetEntity, moveBundle:moveBundle, projectTeam:projectTeam, userLogin:userLogin )
		        if ( !assetTransition.validate() || !assetTransition.save() ) {
		    		message = "Unable to create AssetTransition: " + GormUtil.allErrorsString( assetTransition )
		    	} else {
		    		message = setTransitionTimeElapsed(assetTransition)
		    		def projectAssetMapInstance = new ProjectAssetMap(project:moveBundle.project, asset:assetEntity)
		    		projectAssetMapInstance.currentStateId = Integer.parseInt(stateEngineService.getStateId( process, toState ))
		    		projectAssetMapInstance.save()
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
    			assetTransition.timeElapsed = Integer.parseInt(timeDiff.toString())
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
    def setPreExistTransitionsAsVoided( def assetEntity, def state ) {
    	if(state != "10"){
	    	def preExistTransitions = AssetTransition.findAll("from AssetTransition where assetEntity = $assetEntity.id and stateTo > $state ")
	    	preExistTransitions.each{
	    		it.voided = 1
	    		it.save()
	    	}
    	}
    }
}
