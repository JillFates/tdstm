import com.tds.asset.AssetTransition
import com.tds.asset.AssetComment


import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tds.asset.TaskDependency
import com.tdssrc.grails.GormUtil
/*
 *  This Service is responsible for managing the workflow and status of assets in the system.
 */
class WorkflowService {
    boolean transactional = true
    def stateEngineService
	def grailsApplication
    def jdbcTemplate
	// protected TaskService taskService
	/**
	 * This initializer method is invoked in bootstrap in order to inject the taskService into this service. Since 
	 * both services need access each another, we have to do a late IOC otherwise we'd have a circular reference
	 * error since Grails doesn't support lazy IOC.
	def initialize( ) {
		this.taskService = grailsApplication.mainContext.taskService
		log.info "initialize: taskService is ${taskService==null ? 'null' : this.taskService.class.name }"
	}
	 */
	
    /*
     *  Used to create the Asset Transaction 
	 * @param process		String containing the workflow code
	 * @param role			String the indicating the role that the user is playing (e.g. SUPERVISOR)
	 * @param updateTask	boolean indicating if this method should attempt to call TaskService to update the associate task
     */
    def createTransition( def process, def role, def toState, def assetEntity, def moveBundle, def userLogin, def projectTeam, def comment, def updateTask=true ) {
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
		def project = assetEntity.project
		log.info "createTransition: process=$process, role=$role, toState=$toState"
		
    	if ( projectAssetMap ) {
			log.info "createTransition: in projectAssetMap logic"
    		def transitionStates = jdbcTemplate.queryForList("SELECT CAST(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
    														"WHERE t.asset_entity_id = ${assetEntity.id} and voided = 0 order by date_created desc, stateTo desc limit 1 ")
			if(transitionStates.size()){
				currentState = transitionStates[0].stateTo
			}
    		if(stateEngineService.getState( process, currentState ) != "Hold"){
    			currentState = projectAssetMap.currentStateId
    		}
	    	def fromState = stateEngineService.getState( process, currentState )
	    	def roleCheck = stateEngineService.canDoTask( process, role, fromState, toState )
	    	// Check whether role has permission to change the State
			
	    	if ( ! roleCheck ) {
				log.warn "createTransition: role check failed - role:${role}, currentState:${currentState}, fromState:${currentState}, roleCheck:${roleCheck}"
				message = "$role does not have permission to change the State"
			} else {

	    		flag = stateEngineService.getFlags(process, role, fromState, toState)
	    		if ( ! comment && flag && ( flag.contains("comment") || flag.contains("issue") ) ) {
        			verifyFlag = false
        			message = "A comment is required"
	        	}
				if ( verifyFlag ) {
					// TODO : Check to see that all task predecessors have been completed
				}
	    		//	If verification is successful then create AssetTransition and Update ProjectTeam
	        	if ( verifyFlag ) {
	        		AssetTransition.withTransaction { status ->

						// Update the new Task based runbook steps TODO: MOVE ABOVE CREATING THE TRANSITION
						try {
							// Get the state Id for the state we're bumping the asset to
							def state = stateEngineService.getStateId( process, toState )
							
							// The legacy application will call this function without passing the updateTask flag that defaults 
							// to true.  In the event that someone completes a task, we need to mimic creating the workflow transition as well
							// so in the new Task logic we will call this function and don't want it update the task since it has already been
							// taken care of.
							// If the project uses runbooks, then update the tasks
							if ( updateTask && project.runbookOn) {
								completeWorkflowTask(assetEntity, userLogin, process, state)
							}
							
							def lastState
							if(stateType != "boolean"){
								lastState = doPreExistingTransitions(process, currentState.toString(), state, assetEntity, moveBundle, projectTeam, userLogin)
							}
							lastState = lastState ? lastState : currentState.toString()
									
			        		def assetTransition = new AssetTransition( stateFrom:lastState, stateTo:state, comment:comment, assetEntity:assetEntity, moveBundle:moveBundle, projectTeam:projectTeam, userLogin:userLogin, type:stateType )
							
			        		if ( !assetTransition.validate() || !assetTransition.save(flush:true) ) {
			    				message = "Unable to create AssetTransition: " + GormUtil.allErrorsString( assetTransition )
			    			} else {
			    				def holdState = stateEngineService.getStateId( process, 'Hold' )
			    				/* Set preexisting AssetTransitions as voided where stateTo >= new state */
			    				setPreExistTransitionsAsVoided( process, assetEntity, state, assetTransition, stateType, holdState)
			    				/* Set time elapsed for each transition */
			    				message = setTransitionTimeElapsed(assetTransition)
			    				message = "Transaction created successfully"
			    				if(projectTeam){
									projectTeam.isIdle = flag?.contains('busy') ? 1 : 0
									projectTeam.latestAsset = assetEntity
									projectTeam.save(flush:true)
			    				}
								// store the current status into asset 
								if(stateType != "boolean" || toState == "Hold"){
									
									projectAssetMap.currentStateId = Integer.parseInt(stateEngineService.getStateId( process, toState ))
				    				projectAssetMap.save(flush:true)
									
									assetEntity.currentStatus = Integer.parseInt(stateEngineService.getStateId( process, toState ))
									assetEntity.save(flush:true)
			    				}
								success = true
			    			}
							
						} catch (TaskCompletionException e) {
							message = e.toString()
						} catch (Exception e) {
							message = 'Unexpected error occurred during update'
							log.error e.toString()
						}
						
						if(!success){
							// TODO : Disabled the rollbacks because a) using myISAM and b) not properly trapping org.springframework.transaction.UnexpectedRollbackException
							// status.setRollbackOnly()
						}
	        		}
	        	}
	    	}
    	} else { // if ( projectAssetMap )
			// TODO : Runbook - What is the difference with/without projectAssetMap?
			log.info "createTransition: in ! projectAssetMap logic"
		
    		def state = stateEngineService.getStateId( process, toState )
    		if ( toState == "Hold" ) {
	        	if ( ! comment ) {
	        		verifyFlag = false
	        		message = "A comment is required"
	        	}
	        }
    		if ( verifyFlag ) {
				try {
							
					// The legacy application will call this function without passing the updateTask flag that defaults 
					// to true.  In the event that someone completes a task, we need to mimic creating the workflow transition as well
					// so in the new Task logic we will call this function and don't want it update the task since it has already been
					// taken care of.
					// If the project uses runbooks, then update the tasks
					if ( updateTask && project.runbookOn) {
						message = completeWorkflowTask(assetEntity, userLogin, process, state)
					}

			        def assetTransition = new AssetTransition( stateFrom:state, stateTo:state, comment:comment, assetEntity:assetEntity, moveBundle:moveBundle, projectTeam:projectTeam, userLogin:userLogin, type:stateType )
			        if ( !assetTransition.validate() || !assetTransition.save(flush:true) ) {
			    		message = "Unable to create AssetTransition: " + GormUtil.allErrorsString( assetTransition )
			    	} else {
			    		message = setTransitionTimeElapsed(assetTransition)
			    		// store the current status into asset 
						if(stateType != "boolean" || toState == "Hold"){
							
							def projectAssetMapInstance = new ProjectAssetMap(project:moveBundle.project, asset:assetEntity)
				    		projectAssetMapInstance.currentStateId = Integer.parseInt(stateEngineService.getStateId( process, toState ))
				    		projectAssetMapInstance.save(flush:true)
							
							assetEntity.currentStatus = Integer.parseInt(stateEngineService.getStateId( process, toState ))
							assetEntity.save()
	    				}
			    		message = "Transaction created successfully"
						success = true
						
					}
				} catch (TaskCompletionException e) {
					message = e.toString()
				} catch (Exception e) {
					message = 'Unexpected error occurred during update'
					log.error e.toString()
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
        		if ( !assetTransition.validate() || !assetTransition.save(flush:true) ) {
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
    	if(stateType != "boolean") {
    		def workflow = Workflow.findByProcess( process )
	    	def preExistTransitions = AssetTransition.findAll("from AssetTransition where assetEntity = $assetEntity.id and voided = 0 and ( stateTo >= $state or stateTo = $holdState ) "+
	    														"and id != $assetTransition.id and stateTo not in(select w.transId from WorkflowTransition w "+
	    														"where w.workflow = '${workflow.id}' and w.transId != $holdState and w.type != 'process' ) ")
	    	preExistTransitions.each{
	    		it.voided = 1
	    		it.save(flush:true)
	    	}
			// TODO : runbook - rollback tasks? 
    	}
    }

    /**
     * When moving a given asset from one workflow status to another will do pre existing transitions if there are steps in between. 
	 *	If selected transition failed then this transitions will be rolled back at called place. 
	 *	For example, if current status is at 30 and the user wants to go to 33, fill in 31 and 32 with the same time as the 33 time. 
	 *	This assumes 30-33 are all in the workflow.
     * 
     * @author : Lokanath Reddy
     * @param  : process, stateFrom, stateTo,  assetEntity, moveBundle, projectTeam, userLogin
     * @return : last transition stateTo id.
     */
     def doPreExistingTransitions( def process, def stateFrom, def stateTo,  def assetEntity, def moveBundle, def projectTeam, def userLogin ){
	 	
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
						 
						 // Go and complete the task associated with this workflow step. 
		    		 }
				 }
			 }
			
		}
		return currentState
     }
	 
	 /**
	  * Called by the workflow logic to complete a task associated to a particular workflow step. It will complete any predecessor tasks
	  * that task is associated with. This is used from the Asset Tracker which is Workflow based and doesn't know about the underlying tasks.
	  * @param assetEntity	The asset that is being completed
	  * @param userLogin	the user account that is completing the task
	  * @param process	the workflow (aka process) that the asset belongs to (via bundle)
	  * @param stateId	the state number that is being completed 
	  * @return
	  */
	 def completeWorkflowTask( assetEntity, userLogin, process, state) {
		 def task = getTaskFromAssetAndWorkflow(assetEntity, state)
		 def message = ''
		 log.info "completeWorkflowTask: asset:${assetEntity.id}, user:${userLogin}, process:${process}, state:${state}, task:${task?.id}"
		 if (task) {
			 // TODO : calling completeTask with false doesn't allow for updating multiple states at once
			 // message = taskService.completeTask(task.id, userLogin.id, false)
			 message = grailsApplication.mainContext.taskService.completeTask(task.id, userLogin.id, false)
		 } else {
		 	message = "Sorry but was unable to locate the task associated with the workflow."
		 	log.info "No task associated with workflow ${process}:${state} for asset id ${assetEntity.id}"
		 }
		 return message
	 }
	 
	 /**
	  * Used to retrieve the task associated to an asset and a workflow state (e.g. Powerdown)
	  * @return AssetComment (aka task)
	  */
	 def getTaskFromAssetAndWorkflow(def assetEntity, def state) {
		 def task
		 def moveBundle = assetEntity.moveBundle
		 def workflowCode = moveBundle.workflowCode
		 def wfId = stateEngineService.getTransitionId(workflowCode, state)
		 if (wfId) {
			 def wfTransition = WorkflowTransition.get(wfId)
			 if (wfTransition) {
				 task = AssetComment.findByAssetEntityAndWorkflowTransition(assetEntity, wfTransition)
				 if (! task) {
					 log.info "Unable to find asset (${assetEntity.id}) task for (wf:${workflowCode}/state:${state}/wftId:${wfId})"
				 }
			 } else {
				 error.info "Missing to find workflow Transition for workflow (wf:${workflowCode}/state:${state}/wftId:${wfId})"
			 }
		 } else {
			 error.info "Missing workflowTransition ID from state engine for workflow (${workflowCode}:${state})"
		 }
		 return task
	 }
	 
}

// TODO : refactor this into the src/grails/com/tdsops/tm/exceptions source tree
class TaskCompletionException extends Exception {
	public TaskCompletionException(String message) {
		super(message);
	}
}
