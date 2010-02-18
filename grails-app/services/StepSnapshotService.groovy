class StepSnapshotService {

	def moveBundleService
	def stateEngineService
	
    boolean transactional = true

    def process( def moveBundleId ) {

		def moveBundle = MoveBundle.findById( moveBundleId )
		def moveBundleSteps = MoveBundleStep.findAllByMoveBundle(moveBundle)
		def timeNow = new Date().getTime()
	
		//Loop on each MoveBundleStep for the give MoveBundle
		moveBundleSteps.each{ moveBundleStep ->
    		
			// Determining if the moveBundleStep.transitionId has a predecessorId 
			def predecessor = stateEngineService.getPredecessor( moveBundle?.project?.workflowCode, moveBundleStep.transitionId )
			
			def actualStartTime
			def actualCompletionTime
			// if moveBundleStep.transitionId has a predecessorId
			if(predecessor){
				actualStartTime = moveBundleService.getActualTimes( moveBundleId, predecessor)?.get("started")
				actualCompletionTime  = moveBundleService.getActualTimes( moveBundleId, moveBundleStep.transitionId)?.get("completed")
			} else { // other wise
				def actualTimes = moveBundleService.getActualTimes( moveBundleId, moveBundleStep.transitionId)
				actualStartTime = actualTimes?.get("started")
				actualCompletionTime = actualTimes?.get("completed")
			}

			// If a MoveBundleStep.calcMethod = "M" (manual) additional records will NOT be created by this process
			if(moveBundleStep.calcMethod != "M") {
				def stepSnapshot
				// If MoveBundleStep.actualStartTime is not NULL and actualCompletionTime is NULL create a record
				if( moveBundleStep.actualStartTime && !moveBundleStep.actualCompletionTime ){
				
					stepSnapshot = new StepSnapshot()

					//If MoveBundleStep.actualStartTime is not NULL and actualCompletionTime is not NULL
				} else if( moveBundleStep.actualStartTime && moveBundleStep.actualCompletionTime ) {
					
					def existingStepSnapshot = StepSnapshot.find( "FROM StepSnapshot WHERE moveBundleStep=? ORDER BY dateCreated DESC",
					 	[ moveBundleStep.id ] )
					
					// Checking StepSnapshot record corresponding to the step
					if( !existingStepSnapshot ){
					
						stepSnapshot = new StepSnapshot()
						
						// the last record does not reflect completed (tasksCompleted == tasksCount),
					} else if( existingStepSnapshot.tasksCount == existingStepSnapshot.tasksCompleted ){
					
						stepSnapshot = new StepSnapshot()
						// check to determine if the start/completion times have change
					} else if( moveBundleStep.actualStartTime?.getTime() != actualStartTime.getTime() || 
							moveBundleStep.actualCompletionTime?.getTime() != actualCompletionTime.getTime() ){
							
							stepSnapshot = new StepSnapshot()
						
					}
				}
				if( timeNow >= moveBundleStep.actualStartTime?.getTime() ){
					stepSnapshot = new StepSnapshot()
				}
				
				if( stepSnapshot ) {
					stepSnapshot.moveBundleStep = moveBundleStep
					stepSnapshot.tasksCount = moveBundleService.assetCount( moveBundleId )
					stepSnapshot.tasksCompleted = moveBundleService.assetCompletionCount( moveBundleId, moveBundleStep.transitionId )
					
					//If hasStarted, duration should be populated with the dateCreated - moveBundleStep.actualStartTime otherwise remain zero (0)
					if(stepSnapshot.hasStarted()){
						stepSnapshot.duration = ( timeNow - moveBundleStep.actualStartTime?.getTime() ) / 1000
					} else {
						stepSnapshot.duration = 0
					}
					
					// statements to calculate the planDelta
					def planDelta
					if (stepSnapshot.tasksCompleted > 0) {
						// Need to determine the finish time based on weighted average of actual and planned paces
						def wtFactor = stepSnapshot.duration > moveBundleStep.planDuration ? 1 : ( stepSnapshot.duration / moveBundleStep.planDuration )
							
						def tasksRemaining = stepSnapshot.tasksCount - stepSnapshot.tasksCompleted
						def remainingDuration = tasksRemaining * (1 - wtFactor) * stepSnapshot.planTaskPace
						   + tasksRemaining * wtFactor * stepSnapshot.actualTaskPace
					
						planDelta = ( timeNow + remainingDuration * 1000 ) - moveBundleStep.planCompletionTime.getTime()
					
					} else {
					   // If task hasn't started then base finish time on planned pace
					   def projectedDuration = stepSnapshot.tasksCount * stepSnapshot.planTaskPace
					   planDelta = ( timeNow + projectedDuration * 1000 ) - moveBundleStep.planCompletionTime.getTime()
					}
					
					// assign planDelta to stepSnapshot.planDelta
					if( planDelta ){
						planDelta = planDelta / 1000
					}
					stepSnapshot.planDelta = planDelta
					
					stepSnapshot.dialIndicator =  calcDialIndicator( stepSnapshot.moveBundleStep.planDuration, planDelta )
					
					stepSnapshot.save(flush:true)
				}
			}
    		moveBundleStep.actualCompletionTime = actualCompletionTime
			moveBundleStep.actualStartTime = actualStartTime
			
			moveBundleStep.save( flush : true )
		}
    }

	/**
	 * Used to calculate the Dial Indicator value used in the dashboard display.  The value can range from 0-100 and 50 represents 
	 * that the project is tracking to planned completion time.  A smaller value indicates behind schedule and larger values are ahead
	 * of schedule.
	 * @param planDuration - number representing the total planned duration
	 * @param planDelta - number representing the different (projected or actual) that the task is off planned time
	 * @return number - representing the dial position to display 
	 */ 
	def calcDialIndicator ( def planDuration, def planDelta ) {
		// statements to calculate dialIndicator 
		def projected = planDuration + planDelta
		def aheadFactor = 2
		def behindFactor = 3
		def adjust 
		if( planDelta < 0 ) {
			adjust = 1 - Math.pow( projected / planDuration, aheadFactor )
		} else {
			adjust = -1 + Math.pow( planDuration / projected, behindFactor )
		}
		
		return 50 + 50 * adjust	
	}
	
	/*
	 * 
	 */
	def summaryProcess( def moveEventId ) {
		
		def moveEvent = MoveEvent.get( moveEventId )
		def lastBundleId = null
		def maxOverallDelta = null	// Tracks the max delta for all move events
		def maxDelta = null
		def lastIsCompleted = false
		def hasActive = false
		def isActive = false
		def isCompleted = false
		
		def stepsListQuery = "SELECT mb.move_bundle_id as moveBundleId, mbs.transition_id as transitioId, mbs.label,"+
								" mbs.plan_start_time as planStartTime, mbs.plan_completion_time as planCompletionTime,"+
								" mbs.actual_start_time as actualStartTime, mbs.actual_completion_time as actualCompletionTime,"+
								" ss.date_created as dateCreated, ss.tasks_count as tasksCount, ss.tasks_completed as tasksCompleted,"+
								" ss.dial_indicator as dialIndicator, ss.plan_delta as planDelta"+
								" FROM move_event me "+
								" JOIN move_bundle mb ON mb.move_event_id = me.move_event_id "+
								" LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id"+
								" LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id"+
								" WHERE me.move_event_id = ? "+
								" AND ss.date_created = (SELECT MAX(date_created) FROM step_snapshot ss2 WHERE ss2.move_bundle_step_id = mbs.id) "+
								" UNION "+
								" SELECT mb.move_bundle_id as moveBundleId, mbs.transition_id as transitioId, mbs.label,"+
								" mbs.plan_start_time as planStartTime, mbs.plan_completion_time as planCompletionTime,"+
								" mbs.actual_start_time as actualStartTime, mbs.actual_completion_time as actualCompletionTime,"+
								" ss.date_created as dateCreated, ss.tasks_count as tasksCount, ss.tasks_completed as tasksCompleted,"+
								" ss.dial_indicator as dialIndicator, ss.plan_delta as planDelta"+
								" FROM move_event me "+
								" JOIN move_bundle mb ON mb.move_event_id = me.move_event_id"+
								" LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id"+
								" LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id"+
								" WHERE mb.move_event_id = ? "+
								" AND ss.date_created IS NULL "+
								" ORDER BY moveBundleId, transitioId "
								
		def stepsList = jdbcTemplate.queryForList( stepsListQuery , [ moveEventId, moveEventId ]);
		for(int i = 0 ; i < stepsList.length(); i++){
			if (stepsList[i].moveBundleId != lastBundleId) {
				if (lastBundleId != null) {
					if ( maxOverallDelta == null || maxDelta > maxOverallDelta ) 
			            maxOverallDelta = maxDelta
					}
				lastBundleId = stepsList[i].moveBundleId
				maxDelta = null
				lastIsCompleted = false
				hasActive = false
				isActive = false
			}
			
			// see if task is completed 
			if(stepsList[i].actualCompletionTime){
				isCompleted  = true
			}
				
			// If we have an active Task then we ignore all completed
			if ( hasActive && isCompleted ) continue  // continue not allowed in each loop - should change to a for loop perhaps
			
			// Determine if active (either has start time or planDelta > 0 and is not completed)
			if( stepsList[i].actualStartTime  && stepsList[i].planDelta > 0  && ! isCompleted ){
				isActive =  true
			}
			
			// Track that the bundle has an active step if that's the case
			if( hasActive || isActive ){
				hasActive = true
			}
			
			// If this is a subsequent completed step then we just take the current delta
			if ( isCompleted && lastIsCompleted ) {
				maxDelta = stepsList[i].planDelta
				continue
			}
			
			// If last step was a completed and the current step is active then current step overrules          
			if ( !isCompleted && lastIsCompleted ) {
				maxDelta = stepsList[i].planDelta
				lastIsCompleted = false
				continue
			}
			
			// see if this step is projected further into the future
			if ( stepsList[i].planDelta > maxDelta || maxDelta == null ) {
				maxDelta = stepsList[i].planDelta
				lastIsCompleted = isCompleted
			}
		}
		//  @lok :  I will move this into domain method 
		def planTimes = jdbcTemplate.queryForMap("SELECT MAX(mb.start_time) as startTime ,  MAX(mb.completion_time) as completionTime "+
												"FROM move_bundle WHERE mb.move_event_id = ? ",[moveEventId])
		def planStartTime = planTimes.get("startTime").getTime()
		def planCompletionTime = planTimes.get("completionTime").getTime()
		def planDuration = ( planCompletionTime - planStartTime ) / 1000
		def planDialIndicator = calcDialIndicator( planDuration, maxOverallDelta )
		
		new MoveEventSnapshot(moveEvent : moveEvent, type : "p", planDelta : maxOverallDelta, dialIndicator : dialIndicator ).save(flush:true)
		
		if(moveEvent.revisedCompletionTime){
			def revsedPlanDuration = ( moveEvent.revisedCompletionTime.getTime() - planStartTime ) / 1000
			def revisedDialIndicator = calcDialIndicator( revsedPlanDuration, maxOverallDelta )
			new MoveEventSnapshot( moveEvent : moveEvent, type : "R", planDelta : maxOverallDelta, dialIndicator : revisedDialIndicator ).save(flush:true)
		}
	}
	
	/** 
	  * Creates the MoveEventSnapshot records for a MoveEvent.  If event has revised completion it will create two records.
	  * @...
	  */
	def createSummary( moveEvent, planDelta ) {
		// @lok : I will move this into domain method 
		def planTimes = jdbcTemplate.queryForMap("SELECT MAX(mb.start_time) as startTime ,  MAX(mb.completion_time) as completionTime "+
												"FROM move_bundle WHERE mb.move_event_id = ? ",[moveEvent.id])
		def planStartTime = planTimes.get("startTime").getTime()
		def planCompletionTime = planTimes.get("completionTime").getTime()
		def planDuration = ( planCompletionTime - planStartTime ) / 1000
				
		def dialIndicator = calcDialIndicator( planDuration, planDelta )
		
		// Create MoveEventSnapshot
		new MoveEventSnapshot( moveEvent : moveEvent, type : "p", planDelta : planDelta, dialIndicator : dialIndicator ).save(flush:true)
		// Create the revised snapshot if there is a revised completion tim
	   	if (moveEvent.revisedCompletionTime) {
	   		def revsedPlanDuration = ( moveEvent.revisedCompletionTime.getTime() - planStartTime ) / 1000
			def revisedDialIndicator = calcDialIndicator( revsedPlanDuration, planDelta )
			new MoveEventSnapshot( moveEvent : moveEvent, type : "R", planDelta : planDelta, dialIndicator : revisedDialIndicator ).save(flush:true)
	   	}
	   
	}
	
}
