/**
 * The StepSnapshot domain represents a point in time representation of the status of a Step in a MoveBundle. A group of
 * snapshot records will be created at one time for all MoveBundleStep records associated with a MoveBundle.
 */
class StepSnapshot {
	MoveBundleStep moveBundleStep
	int snapGroupId				// used to group a series of steps as a single group
	int tasksCount				// The number of tasks/assets to be processed.  For Manual steps, it should be 100
	int	tasksCompleted			// The number of tasks/assets completed.  For manual steps, it will be entered by the user as a percentage of completion
	int duration				// The number of seconds that the task has been executing
	int planDelta				// The number of seconds that the step is over(+)/under(-) the planned finish time.
	int dialIndicator			// Quantity to display in the dial/gage on dashboard
	Date dateCreated = new Date()

	static constraints = { 
	}

	static mapping  = {
		version true
		columns {
			taskCount sqltype: 'smallint unsigned'
			tasksCompleted sqltype: 'smallint unsigned'
			duration sqltype: 'mediumint unsigned'
			planDelta sqltype: 'int'
			dialIndicator sqltype: 'tinyint'		
		}
	}
	
	/**
	 * computes the pace of the task in seconds
	 */
	def getPlanTaskPace () {
		// return tasksCount > 0 ? int moveBundleStep.planDuration / tasksCount : 0
		def planDuration = moveBundleStep.getPlanDuration()
		if( planDuration ){
			return tasksCount > 0 ? (Integer)(planDuration / tasksCount ) : 0
		} else {
			return 0
		}
				
	}
	
	/**
	 * computes the pace of the task in seconds. Return zero (0) if no tasks have been completed
	 */
	def getActualTaskPace () {
		// return tasksCompleted && duration ? int duration / tasksCompleted : 0
		return tasksCompleted && duration ? (Integer)(duration / tasksCompleted ) : 0
	}	
	
	/**
	 * calculates the projected time (seconds) remaining based on the number task uncompleted times the planned pace.
	 * @return int - number of projected time remaining (seconds)
	 * Case: 
	 *    1. step has not started - return the moveBundle.planDuration
	 *    2. step in progress - return (taskCount - taskCompleted) * moveBundleStep.planPace
	 *    3. step is completed - return zero (0)
	 */
	def getProjectedTimeRemaining() {
		def timeRemaining = 0
		if(!hasStarted()){
			timeRemaining = moveBundle.getPlanDuration()
		} else if( !isCompleted() ){
			timeRemaining = (tasksCount - tasksCompleted) * getActualTaskPace()
		}
		 return timeRemaining 
	}
	
	/**
	 * Calculates the projected time (seconds) over(+)/under(-) the planned completion time for the step.  
	 * @return int - projected time over planed over completion time (seconds)
	 * Case:
	 *    1. step has not started: 
	 *       a. planStartTime <= current time: return zero (0)
	 *       b. planStartTime > current time: (current time + projectedTimeRemaining) - moveBundleStep.planCompletionTime
	 *    2. step in progress: (current time + projectedTimeRemaining) - moveBundleStep.planCompletionTime
	 *    3. step is completed:  return zero (0)
	 */
	def getProjectedTimeOver() {
		def timeOver = 0
		if(!hasStarted()){
			if( moveBundleStep.planStartTime.getTime() > new Date().getTime() ) {
				timeOver = ( new Date().getTime() + getProjectedTimeRemaining() * 1000 ) - moveBundleStep.planCompletionTime.getTime()
			}
		} else if(!isCompleted()){
			timeOver = (new Date().getTime() + getProjectedTimeRemaining() * 1000 ) - moveBundleStep.planCompletionTime.getTime()
		}
		
		if(timeOver){
			timeOver = timeOver / 1000
		}
		 return timeOver 
	}

	/**
	 * calculates the the projected time that the step will be completed based on the pace
	 * @return date - projected completion time
	 */
	def getProjectedCompletionTime() {
		// return moveBundleStep.planCompletionTime + projectedTimeOver
		return ( moveBundleStep.planCompletionTime.getTime() / 1000 ) + projectedTimeOver
	}

	/**
	 * Used to determine if the Step has been completed
	 * @return boolean - true if step has been completed
	 */
	def isCompleted() {
		return tasksCount == tasksCompleted
	}
	
	/**
	 * Used to determine if the step has started.  It will return true even after completed
	 * @return boolean - true if the step has started
	 */
	def hasStarted() {
		 //	return moveBundleStep.actualStartTime not null
		 if(moveBundleStep.actualStartTime){
			 return true;
		 } else {
			 return false;
		 }
	}
	
	/**
	 * Returns the status bar color 
	 * @return string - the color green/red indication the step status to planned completion time
	 */
	def getStatusColor() {
		return planDelta > 0 ? "red" : "green"
	}	
	
    String toString(){
		moveBundleStep.name + " " + dateCreated
	}

}
