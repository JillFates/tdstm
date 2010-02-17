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
			if(moveBundleStep.calcMethod != "M"){
				def stepSnapshot
				// If MoveBundleStep.actualStartTime is not NULL and actualCompletionTime is NULL create a record
				if( moveBundleStep.actualStartTime && !moveBundleStep.actualCompletionTime ){
				
					stepSnapshot = new StepSnapshot()

					//If MoveBundleStep.actualStartTime is not NULL and actualCompletionTime is not NULL
				} else if( moveBundleStep.actualStartTime && moveBundleStep.actualCompletionTime ) {
					
					def existingStepSnapshot = StepSnapshot.find( "FROM StepSnapshot ss WHERE ss.moveBundleStep= ${moveBundleStep.id} ORDER BY ss.dateCreated desc" )
					
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
					
					// statements to calculate dialIndicator 
					def projected = moveBundleStep.planDuration + planDelta
					def aheadFactor = 2
					def behindFactor = 3
					def adjust 
					if( planDelta < 0 ) {
						// adjust = 50 - ( projected / moveBundleStep.planDuration )^aheadFactor
						def x=( projected / moveBundleStep.planDuration )
						x = x * x 
						adjust = 50 - x
					} else {
						// adjust = -50 + ( moveBundleStep.planDuration / projected )^behindFactor
						def x=( projected / moveBundleStep.planDuration )
						x = x * x * x 
						adjust = -50 + x
					}
					def dialIndicator = 50 + 50 * adjust
					
					// assign dialIndicator to stepSnapshot.dialIndicator
					stepSnapshot.dialIndicator =  dialIndicator
					stepSnapshot.save(flush:true)
				}
			}
    		moveBundleStep.actualCompletionTime = actualCompletionTime
			moveBundleStep.actualStartTime = actualStartTime
			
			moveBundleStep.save( flush : true )
		}
    }
}
