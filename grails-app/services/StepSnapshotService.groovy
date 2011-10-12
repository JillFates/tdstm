import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.runtime.TimeCategory

import com.tds.asset.AssetEntity;
import com.tdssrc.grails.GormUtil

class StepSnapshotService {
	protected static Log log = LogFactory.getLog( StepSnapshotService.class )

	def moveBundleService
	def stateEngineService
	def jdbcTemplate

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
		def dateNow = GormUtil.convertInToGMT( "now", "EDT" )
		def timeNow = dateNow.getTime()
		
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
			log.debug("actualTimes=${actualTimes}")
			
			def earliestStartTime = actualTimes.start
			def latestCompletionTime = actualTimes.completion
			
			// Since we're not penalizing for early starts, the actual time should be used (even though it is earlier than the start)
		
			/*if(earliestStartTime && earliestStartTime < moveBundleStep.moveBundle.startTime)
				earliestStartTime = moveBundleStep.moveBundle.startTime*/
				
			// Get latest StepSnapshot
			/*def latestStepSnapshot = StepSnapshot.find( "from StepSnapshot as s where s.moveBundleStep=? ORDER BY s.dateCreated DESC", [ moveBundleStep ] )
			if(latestStepSnapshot && !earliestStartTime && !latestCompletionTime){
				earliestStartTime = latestStepSnapshot.dateCreated
				latestCompletionTime = latestStepSnapshot.dateCreated
			}*/
			log.debug("Process Step with earliestSTime=${earliestStartTime}, latestCTime=${latestCompletionTime}, MBS: ${moveBundleStep}" )
			
			// If the Step hasn't started and it is not scheduled to start then we don't need to do anything
			if ( ! earliestStartTime && moveBundleStep.planStartTime.getTime() > timeNow )
			   return  // next step

			// If the Step was completed, lets make sure that a more recent transition hasn't occurred (i.e. a Step task was rolled back)
			if ( moveBundleStep.isCompleted() && moveBundleStep.actualCompletionTime == latestCompletionTime )
				return // Don't need to do anything so on to the next step

			def tasksCompleted = moveBundleService.assetCompletionCount( moveBundleId, moveBundleStep.transitionId )
			
			//
			// Create the StepSnapshot
			//
			def stepSnapshot = new StepSnapshot()
			stepSnapshot.moveBundleStep = moveBundleStep
			stepSnapshot.tasksCount = tasksCount
			stepSnapshot.tasksCompleted = tasksCompleted
			stepSnapshot.duration = earliestStartTime ? (( timeNow - earliestStartTime.getTime() ) / 1000).intValue() : 0

			def planDelta = calcProjectedDelta( stepSnapshot, dateNow )
			stepSnapshot.planDelta = planDelta
			stepSnapshot.dialIndicator =  calcStepDialIndicator( stepSnapshot, dateNow )
			
			log.debug("Creating StepSnapshot: ${stepSnapshot}")
			stepSnapshot.save(flush:true)

			//
			// Update the MoveStepBundle
			// 
			if ( stepSnapshot.isCompleted() &&  ( ! moveBundleStep.actualCompletionTime || moveBundleStep.actualCompletionTime != latestCompletionTime ) )
				moveBundleStep.actualCompletionTime = latestCompletionTime
				
			if ( moveBundleStep.actualStartTime != earliestStartTime)
				moveBundleStep.actualStartTime = earliestStartTime

			log.debug("Updating MoveBundleStep with new times: ${moveBundleStep}")
			if ( ! moveBundleStep.save( flush : true ) ) 
				log.error("Unable to save changes to MoveBundleStep: ${moveBundleStep}")
			
		} // moveBundleSteps.each{ moveBundleStep ->
		
		// Now create the summary
		// TODO - JPM - The MoveEventSnapshot.processSummary should be called somewhere else...
		processSummary( moveBundle.moveEvent.id, timeNow )
    }

	/**
	 * Used to create manual snapshots.  When creating it will update the MoveBundleStep as well appropriately.
	 *
	 */
	def createManualSnapshot( def moveBundleId, def moveBundleStepId, def tasksCompleted ) {
		// Check to see if we can find and return appropriate error codes (temporary solution)
		def moveBundleStep = MoveBundleStep.get( moveBundleStepId )
		if (! moveBundleStep ) return 401
		if ( moveBundleStep.moveBundle.id != moveBundleId) return 403
		
		def planDelta
		def now = GormUtil.convertInToGMT( "now","EDT" )
		def nowTime = now.getTime() / 1000
		def planCompletionTime = (moveBundleStep.planCompletionTime.getTime() / 1000) + 59 // 59s added to planCompletion to consider the minuits instead of seconds

		int tasksCount = 100		// Default the total

		//
		// Create the StepSnapshot
		//
		def stepSnapshot = new StepSnapshot()
		stepSnapshot.moveBundleStep = moveBundleStep
		stepSnapshot.tasksCount = tasksCount
		stepSnapshot.tasksCompleted = tasksCompleted

		if (tasksCompleted == tasksCount) {
			
			// stop and return process if step already completed
			if(moveBundleStep.actualCompletionTime)
				return 200
			//	 Completing Manual Process
			moveBundleStep.actualCompletionTime = now
			// Update actual start time if by chance it was never set
			if ( ! moveBundleStep.actualStartTime )
				moveBundleStep.actualStartTime = now

			moveBundleStep.save(force:true)
			
			planDelta = ( nowTime - planCompletionTime ).intValue()
			
		} else {

			// Task in  Progress
			def timeToFinish = (tasksCount - tasksCompleted) * stepSnapshot.getPlanTaskPace()
			planDelta = (nowTime + timeToFinish - planCompletionTime).intValue()

			// Update actual start time if by chance it was never set, or user reset time after completing
			if ( ! moveBundleStep.actualStartTime )	moveBundleStep.actualStartTime = now
			// If user resets tasks to zero then clear out the start
			if ( tasksCompleted == 0 ){ 
				moveBundleStep.actualStartTime = null
			}
			moveBundleStep.actualCompletionTime = null	// Clear out since it is not completed
			moveBundleStep.save(force:true)

		}
		
		def actualStartTime = moveBundleStep.actualStartTime

		//
		// Finish up the StepSnapshot
		//
		stepSnapshot.duration = actualStartTime ? ( nowTime - actualStartTime.getTime() / 1000  ).intValue() : 0

		planDelta = calcProjectedDelta( stepSnapshot, now )
		stepSnapshot.planDelta = planDelta
		stepSnapshot.dialIndicator =  calcStepDialIndicator( stepSnapshot, now )
		
		log.debug("Creating StepSnapshot: ${stepSnapshot}")
		stepSnapshot.save(flush:true)
	
		return 200
	}
	
	/**
	 * Used to create Linear snapshots.  When creating it will update the MoveBundleStep as well appropriately.
	 *
	 */
	def createLinearSnapshot( def moveBundleId, def moveBundleStepId ) {
		// Check to see if we can find and return appropriate error codes (temporary solution)
		def moveBundleStep = MoveBundleStep.get( moveBundleStepId )
		if (! moveBundleStep ) return 401
		if ( moveBundleStep.moveBundle.id != moveBundleId) return 403
		
		def latestStepSnapshot = StepSnapshot.find( "from StepSnapshot as s where s.moveBundleStep=? ORDER BY s.dateCreated DESC", [ moveBundleStep ] )
		
		if( !latestStepSnapshot ){
			// Update actual start time if by chance it was never set, or user reset time after completing
			moveBundleStep.actualStartTime = null
			moveBundleStep.actualCompletionTime = null	// Clear out since it is not completed
			
			moveBundleStep.save(flush:true)
			
			int tasksCount = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = $moveBundleId").size()
			
			//
			// Create the StepSnapshot
			//
			def stepSnapshot = new StepSnapshot()
	
			stepSnapshot.moveBundleStep = moveBundleStep
			stepSnapshot.tasksCount = tasksCount
			stepSnapshot.tasksCompleted = 0
			
			def actualStartTime = moveBundleStep.actualStartTime
			
			stepSnapshot.duration = actualStartTime ? ( nowTime - actualStartTime.getTime() / 1000  ).intValue() : 0
			
			def planDelta = calcProjectedDelta( stepSnapshot, GormUtil.convertInToGMT( "now","EDT" ) )
			stepSnapshot.planDelta = planDelta 
			stepSnapshot.dialIndicator =  calcStepDialIndicator( stepSnapshot, GormUtil.convertInToGMT( "now","EDT" ) )
			
			log.debug("Creating StepSnapshot: ${stepSnapshot}")
			stepSnapshot.save(flush:true)
		}
		return 200
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
	def processSummary( def moveEventId , def timeNow) {
		
		def moveEvent = MoveEvent.get( moveEventId )
		if ( !moveEvent ) {
			log.error(": Unable to get  moveEvent record with id=${moveEventId}")
			return
		}
		if(moveEvent.calcMethod == MoveEvent.METHOD_MANUAL){
			log.error(": MoveEvent '${moveEvent}' process has been changed to MANUAL")
			return
		}
		def lastBundleId = null
		def maxDelta = 0					// Tracks the max delta for all move events
		def lastIsCompleted = false
		def hasActive = false
		def allCompleted = true
		def noneStarted = true
										
		def stepsList = getLatestStepSnapshots( moveEventId )
		if (! stepsList ) {
			log.error("No StepSnapshot records found for moveEventId=${moveEventId}")
			return
		}
		
		stepsList.each { step ->
			
			def isActive = false
			def isCompleted = false
			// If first or new bundle reset various vars that are bundle dependent
			if (step.moveBundleId != lastBundleId) {
				lastBundleId = step.moveBundleId
				lastIsCompleted = false
				hasActive = false
				isActive = false
				// TODO : Need to work in the OverallMaxDelta as it is necessary when dealing with resets within bundle
			}
			
			// see if task is completed 
			if (step.actualCompletionTime){
				isCompleted  = true
			}
			
			// Toggle if not isCompleted
			allCompleted = ! isCompleted ? false : allCompleted
				
			// If we have an active Task then we ignore all completed
			if ( hasActive && isCompleted ) 
				return
			
			// Determine if active (either has start time or planDelta > 0 and is not completed)
			// For our purposes if the task was suppose to of started at this time, we are considering it active
			if ( step.actualStartTime || ( step.planDelta > 0  && ! isCompleted )) 
				isActive = true
			
			// Track that the bundle has an active step if that's the case
			if ( hasActive || isActive ) 
				hasActive = true
			
			// If this is a subsequent completed step then we just take the current delta
			if ( step.planDelta && isCompleted && lastIsCompleted ) {
				// Only set the maxDelta if there are no active tasks before this step
				if ( ! hasActive ) maxDelta = step.planDelta
				return
			}
			
			// If last step was a completed and the current step is active then current step overrules          
			if ( step.planDelta && !isCompleted && lastIsCompleted ) {
				maxDelta = step.planDelta
				lastIsCompleted = false
				return
			}
			
			// see if this step is projected further into the future
			if ( step.planDelta && (step.planDelta > maxDelta || maxDelta == 0 ) ) {
				maxDelta = step.planDelta
				lastIsCompleted = isCompleted
			}
		}
		def planTimes = moveEvent.getPlanTimes()
		if (! planTimes.start ) {
			log.error("Unable to get MoveEvent planTimes: ${moveEvent}")
			return
		}
		
		def planStartTime = planTimes.start.getTime()
		def planCompletionTime = planTimes.completion.getTime() + 59000 // 59000ms added to planCompletion to consider the minuits instead of seconds
		def remainingDuration = (( planCompletionTime - timeNow ) / 1000).intValue()
		def dialIndicator = calcSummaryDialIndicator( moveEvent, remainingDuration, timeNow )
		
		log.debug("******CREATING SUMMARY*******")
		
		log.debug("Creating Summary: planStartTime=${planStartTime}, planCompletionTime=${planCompletionTime}, remainingDuration =${remainingDuration }")
		print "Creating Summary: planStartTime=${planStartTime}, planCompletionTime=${planCompletionTime}, remainingDuration =${remainingDuration }, maxDelta${maxDelta}\n\n"

		def mes = new MoveEventSnapshot(moveEvent: moveEvent, type: MoveEventSnapshot.TYPE_PLANNED, planDelta: maxDelta, dialIndicator: dialIndicator )
		if (! mes.save(flush:true) ) {
			log.error("Unable to save Planned MoveEventSnapshot: ${mes}")
			return
		}
		
		if ( moveEvent.revisedCompletionTime ){
			remainingDuration  = (( moveEvent.revisedCompletionTime.getTime() - planStartTime ) / 1000).intValue()
			dialIndicator = calcSummaryDialIndicator(moveEvent, remainingDuration, timeNow )
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
		// if ( ! timeAsOf ) timeAsOf = new Date() 
		timeAsOf = timeAsOf.getTime() / 1000
		
		// print "timeAsOf=${timeAsOf}, planCompletion=${planCompletionTime}, difference=${ planCompletionTime - timeAsOf }\n"
		if (stepSnapshot.tasksCompleted > 0) {
			
			def planCompletionTime = (stepSnapshot.moveBundleStep.planCompletionTime.getTime() / 1000 ) + 59 	// 59s added to planCompletion to consider the minuits instead of seconds
			def planStartTime = stepSnapshot.moveBundleStep.planStartTime.getTime() / 1000
			def planDuration = stepSnapshot.moveBundleStep.planDuration
			def planTaskPace = stepSnapshot.getPlanTaskPace()
			//def actualTaskPace = stepSnapshot.getActualTaskPace()
			
			def tasksRemaining = stepSnapshot.tasksCount - stepSnapshot.tasksCompleted
			
			def actualStartTime = stepSnapshot.moveBundleStep.actualStartTime?.getTime()
				actualStartTime = actualStartTime ? actualStartTime / 1000 : timeAsOf 
			
			if( actualStartTime < planStartTime){
				// calculate the projectedDuration when plan started before planStartTime
				def projectedDuration =  (timeAsOf - actualStartTime) + (tasksRemaining * planTaskPace)
				planDelta  = projectedDuration - planDuration

			} else {
				//	Need to determine the finish time based on weighted average of actual and planned paces
				/*def wtFactor = stepSnapshot.duration > planDuration ? 1 : ( stepSnapshot.duration / planDuration )
				def remainingDuration = (tasksRemaining * (1 - wtFactor) * planTaskPace + tasksRemaining * wtFactor * actualTaskPace).intValue()*/
				def remainingDuration = tasksRemaining * planTaskPace
				planDelta = (timeAsOf - planCompletionTime + remainingDuration).intValue()
				
			}
			// print "wtFactor=${wtFactor}, tasksRemaining=${tasksRemaining}, remainingDuration=${remainingDuration}, " +
			//	"planTaskPace=${planTaskPace}, \nactualTaskPace=${actualTaskPace}, planDelta=${planDelta}\n"
				
		} else {
			// If task hasn't started then base finish time on planned pace
			//def projectedDuration = stepSnapshot.tasksCount * planTaskPace
			planDelta = 0 //(timeAsOf + projectedDuration - planCompletionTime).intValue()
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
	def calcSummaryDialIndicator ( def moveEvent, def remainingDuration, def timeNow ) {
		
		def timeAsOf = timeNow / 1000
		// calculate B14
		def impactOfLastFinished = 0 // B14
		def lastFinishedStep = MoveBundleStep.findAll("FROM MoveBundleStep m WHERE m.moveBundle.moveEvent = ? AND m.actualCompletionTime is not null ORDER BY m.actualCompletionTime DESC",[moveEvent])
		if(lastFinishedStep[0]){
			impactOfLastFinished = (lastFinishedStep[0].actualCompletionTime?.getTime() - (lastFinishedStep[0].planCompletionTime?.getTime()+ 59000) ) / 1000  	// 59000ms added to planCompletion to consider the minuits instead of seconds
		}
		
		// calculate B15
		def maxImpactOfIp
		def inProgressSteps = MoveBundleStep.findAll("FROM MoveBundleStep m WHERE m.moveBundle.moveEvent = ? AND m.actualCompletionTime is null ",[moveEvent])
		inProgressSteps.each{ iPstep ->
			
			def maxvalue
			def planCompletionTime = (iPstep.planCompletionTime.getTime() / 1000) + 59  	// 59s added to planCompletion to consider the minuits instead of seconds
			def planStartTime = (iPstep.planStartTime.getTime() / 1000)
			if(iPstep.actualCompletionTime){
				maxvalue = 0
			} else if(iPstep.actualStartTime){
				
				def latestStepSnapshot = StepSnapshot.find( "from StepSnapshot as s where s.moveBundleStep=? ORDER BY s.dateCreated DESC", [ iPstep ] )
				def remainingEffort =  (latestStepSnapshot.tasksCount - latestStepSnapshot.tasksCompleted) * latestStepSnapshot.getPlanTaskPace() // D18
				def remainingStepTime = timeAsOf > planCompletionTime ? 0 : planCompletionTime - timeAsOf // D19
						
				maxvalue = remainingEffort - remainingStepTime // D20
			
			} else if(timeAsOf > planStartTime){
				maxvalue = timeAsOf - planStartTime
			} else if(!iPstep.actualStartTime){
				maxvalue =  timeAsOf + (planCompletionTime - planStartTime) // current - planDuration
			} 
			
			if(maxvalue && (maxvalue > maxImpactOfIp || !maxImpactOfIp ) ){
				maxImpactOfIp = maxvalue
			}
		}
		maxImpactOfIp = maxImpactOfIp ? maxImpactOfIp : 0
	    def projected = impactOfLastFinished +  maxImpactOfIp	 // B14 + B15
		
		def adjust
		if( projected > 0 ) {
			adjust = -50*( projected / remainingDuration ) 
		} else {
			adjust = 50*(1-( projected / remainingDuration ) )
		}
		def result = (50 + adjust).intValue()
		
		// to show the dial inbetween 0 to 100
		result = result > 100 ? 100 : result
		result = result < 0 ? 0 : result
				
		return result  
	}
	/**
	 * Used to calculate the Dial Indicator value used in the dashboard display.  The value can range from 0-100 and 50 represents 
	 * that the project is tracking to planned completion time.  A smaller value indicates behind schedule and larger values are ahead
	 * of schedule.
	 * @param planDuration - number representing the total planned duration
	 * @param planDelta - number representing the different (projected or actual) that the task is off planned time
	 * @return number - representing the dial position to display 
	 */ 
	def calcStepDialIndicator ( def stepSnapshot, def timeAsOf ) {
		def resul = 50
		
		timeAsOf = timeAsOf.getTime() / 1000
		def planCompletionTime = (stepSnapshot.moveBundleStep.planCompletionTime.getTime() / 1000 ) + 59  	// 59s added to planCompletion to consider the minuits instead of seconds
		def remainingStepTime = timeAsOf > planCompletionTime ? 0 : planCompletionTime - timeAsOf // D19 
		def planTaskPace = stepSnapshot.getPlanTaskPace()
		def tasksRemaining = stepSnapshot.tasksCount - stepSnapshot.tasksCompleted

		def remainingEffort =  tasksRemaining * planTaskPace // D18
		
		def projectedMinOver = 0 // D20
		if(stepSnapshot.moveBundleStep.actualStartTime ||  stepSnapshot.tasksCompleted > 0){
			projectedMinOver  = remainingEffort - remainingStepTime
		} else {
			projectedMinOver  =  timeAsOf + stepSnapshot.moveBundleStep.getPlanDuration()
		}
		def adjust 
		
		if ( remainingEffort && projectedMinOver > 0) {
			adjust =  -50 * (1-(remainingStepTime / remainingEffort))
		} else {
			adjust =  50 * (1-(remainingEffort / (planCompletionTime - timeAsOf ) ) )
		}
		def result = (50 + adjust).intValue()
		
		// to show the dial inbetween 0 to 100
		result = result > 100 ? 100 : result
		result = result < 0 ? 0 : result
				
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
			SELECT mb.move_bundle_id as moveBundleId, mbs.transition_id as transitionId, mbs.label,
			   mbs.plan_start_time as planStartTime, mbs.plan_completion_time as planCompletionTime,
			   mbs.actual_start_time as actualStartTime, mbs.actual_completion_time as actualCompletionTime,
			   ss.date_created as dateCreated, ss.tasks_count as tasksCount, ss.tasks_completed as tasksCompleted,
			   ss.dial_indicator as dialIndicator, ss.plan_delta as planDelta
			   FROM move_event me 
			   JOIN move_bundle mb ON mb.move_event_id = me.move_event_id 
			   LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id
			   LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id
			   INNER JOIN (SELECT move_bundle_step_id, MAX(date_created) as date_created FROM step_snapshot group by move_bundle_step_id ) ss2
			   ON ss2.move_bundle_step_id = mbs.id AND ss.date_created = ss2.date_created
			   WHERE me.move_event_id = ${moveEventId} 

			UNION 

			SELECT mb.move_bundle_id as moveBundleId, mbs.transition_id as transitionId, mbs.label,
			   mbs.plan_start_time as planStartTime, mbs.plan_completion_time as planCompletionTime,
			   mbs.actual_start_time as actualStartTime, mbs.actual_completion_time as actualCompletionTime,
			   ss.date_created as dateCreated, ss.tasks_count as tasksCount, ss.tasks_completed as tasksCompleted,
			   ss.dial_indicator as dialIndicator, ss.plan_delta as planDelta
			   FROM move_event me 
			   JOIN move_bundle mb ON mb.move_event_id = me.move_event_id
			   LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id
			   LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id
			   WHERE mb.move_event_id = ${moveEventId} 
			      AND ss.date_created IS NULL 
			ORDER BY moveBundleId, transitionId 
		"""
							
		def stepsList = jdbcTemplate.queryForList( stepsListQuery )
		return stepsList
	}
	/*-------------------------------------------------------------------------
	 * Background function to process the snapshots for all the move events and Bundles
	 * This functions runs by quartz job periodically (every 120 seconds) and checks for inProgress flags on Move Events. 
	 * If no flags, do nothing. If true, initiate the snapshot function for the bundles in that move event. 
	 * If the current time is greater than the completion time for all bundles in that event, turn off the inProgress flag.
	 * @author : Lokanada Reddy
	 *------------------------------------------------------------------------*/
	def backgroundSnapshotProcess(){
		def workflowCodes = []
		stateEngineService.getCurrentWorkflowCodes().each{
			workflowCodes << it
		}
		def moveEventsList
		if(workflowCodes.size() > 0)
			moveEventsList = MoveEvent.findAll("from MoveEvent m where m.project.workflowCode in (:workflowCode)", [ workflowCode: workflowCodes] )
		
		def now = GormUtil.convertInToGMT( "now", "EDT" );
		
		moveEventsList.each{ event ->
			def planTimes = event.getEventTimes()
			if(!planTimes.completion)
				return  // next event
			
			// stop the process if current time is greater than event completion time
			if(now.getTime() > planTimes.completion?.getTime()){
				event.inProgress = "false"
				event.save(flush:true)
				return;
			}
			//	start the process if current time in betweeen the event start and completion times.
			if(event.inProgress == "auto" && now.getTime() >= planTimes.start?.getTime() && now.getTime() < planTimes.completion?.getTime() ){
					event.inProgress = "true"
					event.save(flush:true)
			}
			// Run the process if event is active
			if(event.inProgress == "true"){
				def moveBundlesList = MoveBundle.findAllByMoveEvent( event )
				moveBundlesList.each{ bundle ->
					process(bundle.id)
				}
			}
		} // end of event loop
	}// end of function
}
