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
	def taskService
	
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
		def project = assetEntity.project
		
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
							
							// If the project uses runbooks, then update the tasks
							if (project.runbookOn) {
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
									assetEntity.save()
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
					
					// If the project uses runbooks, then update the tasks
					if (project.runbookOn) {
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
		 log.info "completeWorkflowTask: asset:${assetEntity.id}, user:${userLogin}, process:${process}, state:${state}, task:${task.id}"
		 if (task) {
			 // TODO : calling completeTask with false doesn't allow for updating multiple states at once
			 message = completeTask(task.id, userLogin.id, false)
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

	 /**
	  * Used to set the state of a task (aka AssetComment) to completed and update any dependencies to the ready state appropriately. This will 
	  * complete predecessor tasks if completePredecessors=true.
	  * @param task
	  * @return
	  */
	 // TODO: runbook : completeTask() : move into the TaskService and change so that we pass in the Task instead of the id
	 def completeTask( taskId, userId, completePredecessors=false ) {
		 log.info "completeTask: taskId:${taskId}, userId:${userId}"
		 def task = AssetComment.get(taskId)
		 def userLogin = UserLogin.get(userId)
		 def tasksToComplete = [task]
		 
		 def predecessors = getIncompletePredecessors(task)
		 
		 // If we're not going to automatically complete predecessors (Supervisor role only), the we can't do anything 
		 // if there are any incomplete predecessors.
		 if (! completePredecessors && predecessors.size() > 0) {
			throw new TaskCompletionException("Unable to complete task [${task.taskNumber}] due to incomplete predecessor task # (${predecessors[0].taskNumber})")
		 }

		 // If automatically completing predecessors, check first to see if there are any on hold and stop 		 
		 if (completePredecessors) {
			 // TODO : Runbook - if/when we want to complete all predecessors, perhaps we should do it recursive since this logic only goes one level I believe.
			 if ( predecessors.size() > 0 ) {
				 // Scan the list to see if any of the predecessor tasks are on hold because we don't want to do anything if that is the case
				 def hasHold=false
				 for (int i=0; i < predecessors.size(); i++) {
					 if (predecessors[i].status == "Hold") {
						hasHold = true
						break
					 }
				 }
				 if (hasHold) {
					 throw new TaskCompletionException("Unable to complete task [${task.taskNumber}] due to predecessor task # (${predecessors[0].taskNumber}) being on Hold")
				 }
			 }
		 
			 tasksToComplete += predecessors
		 }

	 	// Complete the task(s)
		tasksToComplete.each() { activeTask ->
			// activeTask.dateResolved = GormUtil.convertInToGMT( "now", "EDT" )
			// activeTask.resolvedBy = userLogin.person
			taskService.setTaskStatus(activeTask, AssetCommentStatus.DONE)
			if ( ! (activeTask.validate() && activeTask.save(flush:true)) ) {
				throw new TaskCompletionException("Unable to complete task # ${activeTask.taskNumber} due to " +
					GormUtil.allErrorsString(activeTask) )
				log.error "Failed Completing task [${activeTask.id} " + GormUtil.allErrorsString(successorTask)
				return "Unable to complete due to " + GormUtil.allErrorsString(successorTask)
			}
		} 
		
		//
		// Now Mark any successors as Ready if all predecessors are Completed
		//
		def succDependencies = TaskDependency.findByPredecessor(task)
		succDependencies?.each() { succDepend ->
			def successorTask = succDepend.assetComment
			
			// If the Task is in the Planned or Pending state, we can check to see if it makes sense to set to READY
			if ([AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING].contains(successorTask.status)) {
				// Find all predecessors for the successor, other than the current task, and make sure they are completed
				def predDependencies = TaskDependency.findByAssetCommentAndPredecessorNot(successorTask, task)
				def makeReady=true
				predDependencies?.each() { predDependency
					def predTask = predDependency.assetEntity
					if (predTask.status != AssetCommentStatus.COMPLETED) makeReady = false
				}
				if (makeReady) {
					successorTask.status = AssetCommentStatus.READY
					if (! (successorTask.validate() && successorTask.save(flush:true)) ) {
					 	throw new TaskCompletionException("Unable to release task # ${successorTask.taskNumber} due to " +
							 GormUtil.allErrorsString(successorTask) )
						log.error "Failed Readying successor task [${successorTask.id} " + GormUtil.allErrorsString(successorTask)
					 }
				}
			}
		}
	 }
	 
	 /**
	  * Provides a list of upstream task predecessors that have not been completed for a given task. Implemented by recursion
	  * @param task
	  * @param taskList list of predecessors collected during the recursive lookup
	  * @return list of predecessor tasks
	  */
	 // TODO: runbook : Refactor getIncompletePredecessors() into the TaskService,
	 def getIncompletePredecessors(task, taskList=[]) {
		 task.taskDependencies?.each() { dependency ->
			 def predecessor = dependency.predecessor

			 // Check to see if we already have the predecessor in the list where there were multiple predecessors that referenced one another
			 def skip=false
			 for (int i=0; i < taskList.size(); i++) {
				 if (taskList[i].id == predecessor.id ) {
				 	skip = true
					break
			 	}
			 }
			 
			 // If it is not Completed, add it to the list and recursively look for more predecessors
			 if (! skip && predecessor.status != 'Completed') {
				 taskList << predecessor
				 getIncompletePredecessors(predecessor, taskList)
			 }
		 }
		 return taskList
	 }
}

// TODO : refactor this into the src/grails/com/tdsops/tm/exceptions source tree
class TaskCompletionException extends Exception {
	public TaskCompletionException(String message) {
		super(message);
	}
}
