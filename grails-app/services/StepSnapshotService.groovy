import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.runtime.TimeCategory

class StepSnapshotService {
	protected static Log log = LogFactory.getLog( StepSnapshotService.class )

	def moveBundleService
	def stateEngineService
	
    boolean transactional = true

	/**
	 * The process method will generate StepSnapshot records for a MoveBundle.  It iterates 
	 * over the list of MoveBundleSteps for the MoveBundle and creates records for any Step that has started or has
	 * lapsed it's planned start time.
	 * @param MoveBundle.id to generate records
	 */
    def process( def moveBundleId ) {

		def moveBundle = MoveBundle.findById( moveBundleId )
		def moveBundleSteps = MoveBundleStep.findAllByMoveBundle(moveBundle)
		def timeNow = new Date().getTime()
		
		def tasksCount = moveBundleService.assetCount( moveBundleId )
	
		log.debug("Processing moveBundle ${moveBundleId}")
		
		//Loop on each MoveBundleStep for the give MoveBundle
		moveBundleSteps.each { moveBundleStep ->
					
			// Don't do anything for MANUAL steps
			if (moveBundleStep.calcMethod == MoveBundleStep.METHOD_MANUAL) 
				return // next step
		
			def actualTimes = getActualTimes( moveBundleStep )
			if ( ! actualTimes ) {
				log.error("Unable to get ActualTimes for ${moveBundleStep}")
				return
			}
			
			def actualStartTime = actualTimes.start
			def actualCompletionTime = actualTimes.completed
			
			// If the Step hasn't started and it is not scheduled to start then we don't need to do anything
			if ( ! actualStartTime && moveBundleStep.startTime?.getTime() > timeNow )
			   return  // next step

			// Get latest StepSnapshot
			def latestStepSnapshot = StepSnapshot.find( "FROM StepSnapshot WHERE moveBundleStep=? ORDER BY dateCreated DESC", [ moveBundleStep.id ] )
			
			// If the Step was completed, lets make sure that a more recent transition hasn't occurred (i.e. a Step task was rolled back)
			if ( moveBundleStep.isCompleted() && moveBundleStep.actualCompletionTime == actualCompletionTime )
				return // Don't need to do anything so on to the next step

			def tasksCompleted = moveBundleService.assetCompletionCount( moveBundleId, moveBundleStep.transitionId )
			
			//
			// Create the StepSnapshot
			//
			def stepSnapshot = new StepSnapshot()
			stepSnapshot.moveBundleStep = moveBundleStep
			stepSnapshot.tasksCount = tasksCount
			stepSnapshot.tasksCompleted = tasksCompleted
			stepSnapshot.duration = actualStartTime ? (( timeNow - actualStartTime.getTime() ) / 1000).intValue() : 0

			def planDelta = calcProjectedDelta( stepSnapshot, timeNow )
			stepSnapshot.dialIndicator =  calcDialIndicator( stepSnapshot.moveBundleStep.planDuration, planDelta )
			
			log.debug("Creating StepSnapshot: ${stepSnapshot}")
			stepSnapshot.save(flush:true)

			// Update the MoveBundleStep if the actual times have changed
			if ( moveBundleStep.actualCompletionTime != actualCompletionTime || moveBundleStep.actualStartTime != actualStartTime) {
    			moveBundleStep.actualCompletionTime = actualCompletionTime
				moveBundleStep.actualStartTime = actualStartTime

				log.debug("Updating MoveBundleStep with new times: ${moveBundleStep}")
				moveBundleStep.save( flush : true )
			}
			
		} // moveBundleSteps.each{ moveBundleStep ->
		
		// Now create the summary
		// TODO - JPM - The MoveEventSnapshot.processSummary should be called somewhere else...
		processSummary( moveBundle.moveEvent.id )
    }

	/**
	 * Used to generate MoveEventSnapshot records for a MoveEvent.  The process looks over all latest StepSnapshot records
	 * of the MoveBundles associated with the MoveEvent and determines the worst case MoveEventStep across all Steps in the 
	 * moveEvent. It uses that value to determine offset of the completion time.
	 * If a revised completion time has been added to the MoveEvent then two records are created to reflect the differences in
	 * the dialIndicator.
	 * <p/>
	 * If no steps have started and the current time is less than the earliest scheduled MoveBundle start then no record should
	 * be created.  Likewise if all the steps are completed and an applicable MoveEventSnapshot record has already been created
	 * then no record will be created.
	 *
	 * @param int moveEvent.id 
	 */
	def processSummary( def moveEventId ) {
		
		def moveEvent = MoveEvent.get( moveEventId )
		if ( !moveEvent ) {
			log.error("Unable to get  moveEvent record with id=${moveEventId}")
			return
		}
		
		def lastBundleId = null
		def maxDelta = 0					// Tracks the max delta for all move events
		def lastIsCompleted = false
		def hasActive = false
		def isActive = false
		def isCompleted = false
		def allCompleted = true
		def noneStarted = true
										
		def stepsList = getLatestStepSnapshots( moveEventId )
		if (! stepsList ) {
			log.error("No StepSnapshot records found for moveEventId=${moveEventId}")
			return
		}
		
		for(int i = 0 ; i < stepsList.length(); i++) {
			// If first or new bundle reset various vars that are bundle dependent
			if (stepsList[i].moveBundleId != lastBundleId) {
				lastBundleId = stepsList[i].moveBundleId
				lastIsCompleted = false
				hasActive = false
				isActive = false
			}
			
			// see if task is completed 
			if (stepsList[i].actualCompletionTime){
				isCompleted  = true
			}
			
			// Toggle if not isCompleted
			allCompleted = ! isCompleted ? false : allCompleted
				
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
		
		def planTimes = moveBundle.getPlanTimes()
		if (! planTimes ) {
			log.error("Unable to get MoveBundle planTimes: ${moveBundle}")
			return
		}
		
		def planStartTime = planTimes.start.getTime()
		def planCompletionTime = planTimes.completion.getTime()
		def planDuration = (( planCompletionTime - planStartTime ) / 1000).intValue()
		def dialIndicator = calcDialIndicator( planDuration, maxOverallDelta )
		
		def mes = new MoveEventSnapshot(moveEvent: moveEvent, type: MoveEventSnapshot.TYPE_PLANNED, planDelta: maxDelta, dialIndicator: dialIndicator )
		if (! mes.save(flush:true) ) {
			log.error("Unable to save Planned MoveEventSnapshot: ${mes}")
			return
		}
		
		if ( moveEvent.revisedCompletionTime ){
			planDuration = (( moveEvent.revisedCompletionTime.getTime() - planStartTime ) / 1000).intValue()
			dialIndicator = calcDialIndicator( planDuration, maxOverallDelta )
			mes = new MoveEventSnapshot( moveEvent: moveEvent, type: TYPE_REVISED, planDelta: maxDelta, dialIndicator: dialIndicator )
			if (! mes.save(flush:true) ) {
				log.error("Unable to save Revised MoveEventSnapshot: ${mes}")
				return
			}
		}
	}	

	/**
	 * Used to access the Actual Start and Completion times of a MoveBundleStep by looking up the AssetTransition records 
	 * for all Assets associated to the move bundle.  When a Step has a predecessor the logic will use Transition Ids ranging 
	 * from the predecessor id up to but not including the step's transition id.
	 * @param MoveBundleStep object
	 * @return map[start, completion] map of actual Date values for the MoveBundleStep
	 */
	def getActualTimes( def moveBundleStep ) {
		def actualStartTime, actualCompletionTime
		
		// Determining if the moveBundleStep.transitionId has a predecessorId 
		def workflow = moveBundleStep?.moveBundle?.project?.workflowCode
		if ( !workflow ) {
			log.error("Unable to reference workflow in: ${moveBundleStep}" )
			return null
		}
		
		def moveBundleId = moveBundleStep?.moveBundle?.id
		if ( !moveBundleId ) {
			log.error("Unable to reference moveBundle.id in: ${moveBundleStep}" )
			return null
		}
		
		def predecessor = stateEngineService.getPredecessor( workflow, moveBundleStep.transitionId )

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
		
		return [start: actualStartTime, completion: actualCompletionTime]
	}


	/**
	 * Calculates the delta time of remaining tasks for a Step.  It calculates the remaining time on a weighted average of
	 * the planned and actual paces.  The more tasks completed the more the formula relies on the actual pace so that the 
	 * result is more inline with real times.  Early on in the Step we don't want a few slower tasks to skew the projections.
	 * @param stepSnapshot the StepSnapshot that the delta is being determined
	 * @param timeAsOf the time that the calculation will use when calculating projection
	 * @return int representing # of seconds that the Step is +/- to the planned completion
	 */
	def calcProjectedDelta( def stepSnapshot, def timeAsOf ) {
		def planDelta
		def planCompletionTime = stepSnapshot.moveBundleStep.planCompletionTime.getTime() / 1000
		def planDuration = stepSnapshot.moveBundleStep.planDuration
		def planTaskPace = stepSnapshot.getPlanTaskPace()
		def actualTaskPace = stepSnapshot.getActualTaskPace()
		
		// if ( ! timeAsOf ) timeAsOf = new Date() 
		timeAsOf = timeAsOf.getTime() / 1000
		
		// print "timeAsOf=${timeAsOf}, planCompletion=${planCompletionTime}, difference=${ planCompletionTime - timeAsOf }\n"
		if (stepSnapshot.tasksCompleted > 0) {
			// Need to determine the finish time based on weighted average of actual and planned paces
			def wtFactor = stepSnapshot.duration > planDuration ? 1 : ( stepSnapshot.duration / planDuration )
			
			def tasksRemaining = stepSnapshot.tasksCount - stepSnapshot.tasksCompleted
			def remainingDuration = (tasksRemaining * (1 - wtFactor) * planTaskPace + tasksRemaining * wtFactor * actualTaskPace).intValue()
	
			planDelta = (timeAsOf + remainingDuration - planCompletionTime).intValue()
			
			// print "wtFactor=${wtFactor}, tasksRemaining=${tasksRemaining}, remainingDuration=${remainingDuration}, " +
			//	"planTaskPace=${planTaskPace}, \nactualTaskPace=${actualTaskPace}, planDelta=${planDelta}\n"
				
		} else {
			// If task hasn't started then base finish time on planned pace
			def projectedDuration = stepSnapshot.tasksCount * planTaskPace
			planDelta = (timeAsOf + projectedDuration - planCompletionTime).intValue()
		}
	
		return planDelta
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
	
	
	/**
	 * Queries the database for the most recent StepSnapshot records for all MoveBundles associated with the 
	 * MoveEvent.
	 * @param int moveEvent.id used to lookup the MoveEvent
	 * @return List of records
	 */
	def getLatestStepSnapshots( def moveEventId ) {
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
	}
}
