
/*
 *  This Service is responsible for managing the workflow and status of assets in the system.
 */
class WorkflowService {

    boolean transactional = true
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
    }
}
