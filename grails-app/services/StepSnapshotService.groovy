import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.runtime.TimeCategory

class StepSnapshotService {
	protected static Log log = LogFactory.getLog( StepSnapshotService.class )

	def moveBundleService
	def stateEngineService
	
    boolean transactional = true

    def process( def moveBundleId ) {

		def moveBundle = MoveBundle.findById( moveBundleId )
		def moveBundleSteps = MoveBundleStep.findAllByMoveBundle(moveBundle)
		def timeNow = new Date().getTime()
		
		def tasksCount = moveBundleService.assetCount( moveBundleId )
	
		log.debug("processing moveBundle ${moveBundleId}")
		
		//Loop on each MoveBundleStep for the give MoveBundle
		moveBundleSteps.each { moveBundleStep ->
					
			// Don't do anything for MANUAL steps
			if (moveBundleStep.calcMethod == MoveBundleStep.METHOD_MANUAL) 
				return // next step
		
			// Determining if the moveBundleStep.transitionId has a predecessorId 
			def predecessor = stateEngineService.getPredecessor( moveBundle?.project?.workflowCode, moveBundleStep.transitionId )
		
			def actualStartTime
			def actualCompletionTime
			
			// get Actual Times based on Step having predecessor or not
			if (predecessor){
				// Get Start from Predecessor and Completion from current transition
				actualStartTime = moveBundleService.getActualTimes( moveBundleId, predecessor)?.get("started")
				actualCompletionTime  = moveBundleService.getActualTimes( moveBundleId, moveBundleStep.transitionId)?.get("completed")
			} else { 
				// Get both times from current transition
				def actualTimes = moveBundleService.getActualTimes( moveBundleId, moveBundleStep.transitionId )
				actualStartTime = actualTimes?.get("started")
				actualCompletionTime = actualTimes?.get("completed")
			}
			
			// If the Step hasn't started and it is not scheduled to start then we don't need to do anything
			if ( ! actualStartTime && moveBundlStep.startTime?.getTime() > timeNow )
			   return  // next step

			// Get latest StepSnapshot
			def latestStepSnapshot = StepSnapshot.find( "FROM StepSnapshot WHERE moveBundleStep=? ORDER BY dateCreated DESC", [ moveBundleStep.id ] )
			
			// If the Step was completed, lets make sure that a more recent transition hasn't occurred (i.e. a Step task was rolled back)
			if ( moveBundleStep.isCompleted() && moveBundleStep.actualCompletionTime.getTime() == actualCompletionTime?.getTime() )
				return // Don't need to do anything so on to the next step

			def tasksCompleted = moveBundleService.assetCompletionCount( moveBundleId, moveBundleStep.transitionId )
			
			//
			// Create the StepSnapshot
			//
			def stepSnapshot = new StepSnapshot()
			stepSnapshot.moveBundleStep = moveBundleStep
			stepSnapshot.tasksCount = tasksCount
			stepSnapshot.tasksCompleted = tasksCompleted
			stepSnapshot.duration = actualStartTime ? ( timeNow - actualStartTime.getTime() ) / 1000 : 0

			def planDelta = calcPlanDelta( timeNow, steSnapshot )
			stepSnapshot.dialIndicator =  calcDialIndicator( stepSnapshot.moveBundleStep.planDuration, planDelta )
			
			stepSnapshot.save(flush:true)

			// Update the MoveBundleStep if the actual times have changed
			if ( moveBundleStep.actualCompletionTime != actualCompletionTime || moveBundleStep.actualStartTime != actualStartTime) {
    			moveBundleStep.actualCompletionTime = actualCompletionTime
				moveBundleStep.actualStartTime = actualStartTime

				moveBundleStep.save( flush : true )
			}
			
		} // moveBundleSteps.each{ moveBundleStep ->
    }

	/**
	 * Calculates the delta time of a Step as compared to actual planned time based on the remaining tasks and the planned  
	 * pace of the tasks.
	 * @return int representing # of seconds that the Step is +/- to the planned completion
	 */
	def calcPlanDelta( def timeBasedOn, def stepSnapshot ) {
		def planDelta
		def planCompletionTime = (stepSnapshot.moveBundleStep.planCompletionTime.getTime() / 1000).intValue()
		def planDuration = stepSnapshot.moveBundleStep.planDuration
		
		if (stepSnapshot.tasksCompleted > 0) {
			// Need to determine the finish time based on weighted average of actual and planned paces
			def wtFactor = stepSnapshot.duration > planDuration ? 1 : ( stepSnapshot.duration / planDuration )
			
			def tasksRemaining = stepSnapshot.tasksCount - stepSnapshot.tasksCompleted
			def remainingDuration = tasksRemaining * (1 - wtFactor) * stepSnapshot.planTaskPace
			   + tasksRemaining * wtFactor * stepSnapshot.actualTaskPace
	
			use (TimeCategory) {			 
				planDelta = ( timeBasedOn + (remainingDuration * 1000).intValue() ) - planCompletionTime.seconds
			}
			
			print "wtFactor=${wtFactor}, tasksRemaining=${tasksRemaining}, remainingDuration=${remainingDuration}, " +
				"actualTaskPace=${stepSnapshot.actualTaskPace}, planDelta=${planDelta}"
		} else {
			// If task hasn't started then base finish time on planned pace
			def projectedDuration = stepSnapshot.tasksCount * stepSnapshot.planTaskPace
			use (TimeCategory) {			 
				planDelta = ( timeBasedOn + (projectedDuration * 1000).intValue() ) - planCompletionTime.seconds
			}	
		}
	
		return planDelta ? (planDelta.getTime() / 1000).intValue() : null
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
		def projected = planDuration + planDelta
		def aheadFactor = 2
		def behindFactor = 3
		def adjust 
		
		if ( planDelta < 0 ) {
			adjust = 1 - Math.pow( projected / planDuration, aheadFactor )
		} else {
			adjust = -1 + Math.pow( planDuration / projected, behindFactor )
		}
		def result = (50 + 50 * adjust).intValue()
		
		// print "planDuration=${planDuration}, planDelta=${planDelta}, Adjust = ${adjust}, result=${result} \n"

		return result
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
		
		def stepsListQuery = """
			SELECT mb.move_bundle_id as moveBundleId, mbs.transition_id as transitioId, mbs.label,
			   mbs.plan_start_time as planStartTime, mbs.plan_completion_time as planCompletionTime,
			   mbs.actual_start_time as actualStartTime, mbs.actual_completion_time as actualCompletionTime,
			   ss.date_created as dateCreated, ss.tasks_count as tasksCount, ss.tasks_completed as tasksCompleted,
			   ss.dial_indicator as dialIndicator, ss.plan_delta as planDelta
			   FROM move_event me 
			   JOIN move_bundle mb ON mb.move_event_id = me.move_event_id 
			   LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id
			   LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id
			   WHERE me.move_event_id = ? 
			   AND ss.date_created = (SELECT MAX(date_created) FROM step_snapshot ss2 WHERE ss2.move_bundle_step_id = mbs.id) 

			UNION 

			SELECT mb.move_bundle_id as moveBundleId, mbs.transition_id as transitioId, mbs.label,
			   mbs.plan_start_time as planStartTime, mbs.plan_completion_time as planCompletionTime,
			   mbs.actual_start_time as actualStartTime, mbs.actual_completion_time as actualCompletionTime,
			   ss.date_created as dateCreated, ss.tasks_count as tasksCount, ss.tasks_completed as tasksCompleted,
			   ss.dial_indicator as dialIndicator, ss.plan_delta as planDelta
			   FROM move_event me 
			   JOIN move_bundle mb ON mb.move_event_id = me.move_event_id
			   LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id
			   LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id
			   WHERE mb.move_event_id = ? 
			      AND ss.date_created IS NULL 
			ORDER BY moveBundleId, transitionId 
		"""
								
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
			if ( hasActive && isCompleted ) 
				continue
			
			// Determine if active (either has start time or planDelta > 0 and is not completed)
			if ( stepsList[i].actualStartTime  && stepsList[i].planDelta > 0  && ! isCompleted ) 
				isActive =  true
			
			// Track that the bundle has an active step if that's the case
			if ( hasActive || isActive ) 
				hasActive = true
			
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
		def planTimes = jdbcTemplate.queryForMap(
			"SELECT MIN(start_time) as startTime ,  MAX(completion_time) as completionTime FROM move_bundle WHERE move_event_id = ? ", [moveEventId])
		def planStartTime = planTimes.get("startTime").getTime()
		def planCompletionTime = planTimes.get("completionTime").getTime()
		def planDuration = ( planCompletionTime - planStartTime ) / 1000
		def planDialIndicator = calcDialIndicator( planDuration, maxOverallDelta )
		
		new MoveEventSnapshot(moveEvent : moveEvent, type : "p", planDelta : maxOverallDelta, dialIndicator : dialIndicator ).save(flush:true)
		
		if (moveEvent.revisedCompletionTime){
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
		def planTimes = jdbcTemplate.queryForMap(
			"SELECT MAX(start_time) as startTime, MAX(completion_time) as completionTime "+
			"FROM move_bundle WHERE move_event_id = ? ",[moveEvent.id])
		def planStartTime = planTimes.get("startTime")?.getTime()
		def planCompletionTime = planTimes.get("completionTime")?.getTime()
		def planDuration = ( planCompletionTime - planStartTime ) / 1000
				
		def dialIndicator = calcDialIndicator( planDuration, planDelta )
		
		// Create MoveEventSnapshot
		new MoveEventSnapshot( moveEvent : moveEvent, type : "p", planDelta : planDelta, dialIndicator : dialIndicator ).save(flush:true)
		// Create the revised snapshot if there is a revised completion tim
	   	if ( moveEvent.revisedCompletionTime ) {
	   		def revisedPlanDuration = ( moveEvent.revisedCompletionTime.getTime() - planStartTime ) / 1000
			def revisedDialIndicator = calcDialIndicator( revisedPlanDuration, planDelta )
			
			new MoveEventSnapshot( moveEvent : moveEvent, type : "R", planDelta : planDelta, dialIndicator : revisedDialIndicator ).save(flush:true)
	   	}
	   
	}
	
}
