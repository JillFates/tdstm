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
	}
	
	/**
	 * computes the pace of the task in seconds. Return zero (0) if no tasks have been completed
	 */
	def getActualTaskPace () {
		// return tasksCompleted && duration ? int duration / tasksCompleted : 0
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
		// return moveBundleStep.actualStartTime not null
	}
	
    String toString(){
		moveBundleStep.name + " " + dateCreated
	}

}
