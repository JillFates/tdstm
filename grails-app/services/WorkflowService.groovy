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
	        		def state = stateEngineService.getState( process, currentState )
	        		def assetTransition = new AssetTransition( stateFrom:state, stateTo:toState, comment:comment, assetEntity:assetEntity, moveBundle:moveBundle, projectTeam:projectTeam, userLogin:userLogin )
	        		if ( !assetTransition.validate() || !assetTransition.save() ) {
	    				message = "Unable to create AssetTransition: " + GormUtil.allErrorsString( assetTransition )
	    			} else {
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
    		message = "Transaction failed"
    	}
		
    	return [success:success, message:message]
    }
}
