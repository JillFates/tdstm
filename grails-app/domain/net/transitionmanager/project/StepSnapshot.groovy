package net.transitionmanager.project

import com.tdssrc.grails.TimeUtil

/**
 * Represents a point in time representation of the status of a Step in a MoveBundle. A group of snapshot
 * records will be created at one time for all MoveBundleStep records associated with a MoveBundle.
 */
class StepSnapshot {

	MoveBundleStep moveBundleStep
	int tasksCount           // The number of tasks/assets to be processed.  For Manual steps, it should be 100
	int tasksCompleted       // The number of tasks/assets completed.  For manual steps, it will be entered
	                         // by the user as a percentage of completion
	int duration             // The number of seconds that the task has been executing
	int planDelta            // The number of seconds that the step is over(+)/under(-) the planned finish time.
	int dialIndicator        // Quantity to display in the dial/gage on dashboard
	Date dateCreated = TimeUtil.nowGMT()

	static constraints = {
		dateCreated nullable: true
	}

	static mapping = {
		version false
		autoTimestamp false
		columns {
			tasksCompleted sqltype: 'smallint unsigned'
			duration sqltype: 'mediumint unsigned'
			planDelta sqltype: 'int'
			dialIndicator sqltype: 'tinyint'
		}
	}

	/**
	 * Computes the pace of the task in seconds. Return zero (0) if there are no tasks.
	 */
	int getPlanTaskPace() {
		int planDuration = moveBundleStep.planDuration
		planDuration && tasksCount ? (planDuration / tasksCount).intValue() : 0
	}

	/**
	 * Computes the pace of the task in seconds. Return zero (0) if no tasks have been completed
	 */
	int getActualTaskPace() {
		tasksCompleted && duration ? (duration / tasksCompleted).intValue() : 0
	}

	/**
	 * calculates the projected time (seconds) remaining based on the number task uncompleted times the planned pace.
	 * @return the number of projected time remaining (seconds)
	 * Case:
	 *    1. step has not started - return the moveBundle.planDuration
	 *    2. step in progress - return (taskCount - taskCompleted) * moveBundleStep.planPace
	 *    3. step is completed - return zero (0)
	 */
	int getProjectedTimeRemaining() {
		tasksCompleted < tasksCount ? ((tasksCount - tasksCompleted) * planTaskPace).intValue()  : 0
	}

	/**
	 * Calculates the projected time (seconds) over(+)/under(-) the planned completion time for the step.
	 * @return  projected time over planned over completion time (seconds)
	 * Case:
	 *    1. step has not started:
	 *       a. planStartTime <= current time: return zero (0)
	 *       b. planStartTime > current time: (current time + projectedTimeRemaining) - moveBundleStep.planCompletionTime
	 *    2. step in progress: (current time + projectedTimeRemaining) - moveBundleStep.planCompletionTime
	 *    3. step is completed:  return zero (0)
	 */
	int getProjectedTimeOver() {
		int timeOver = 0
		long nowTime = System.currentTimeMillis()
		long projectedMillisecondsRemaining = projectedTimeRemaining * 1000
		if (!hasStarted()) {
			if (moveBundleStep.planStartTime.time > nowTime) {
				timeOver = (nowTime + projectedMillisecondsRemaining) - moveBundleStep.planCompletionTime.time
			}
		}
		else if (!isCompleted()) {
			timeOver = (nowTime + projectedMillisecondsRemaining) - moveBundleStep.planCompletionTime.time
		}
		timeOver / 1000
	}

	/**
	 * Calculates the the projected Date that the step will be completed based on the pace.
	 */
	Date getProjectedCompletionTime() {
		int projectedCompletionTimeInseconds = (moveBundleStep.planCompletionTime.time / 1000) + projectedTimeOver
		new Date((long) (projectedCompletionTimeInseconds * 1000))
	}

	/**
	 * Determine if the Step has been completed
	 * @return true if step has been completed
	 */
	boolean isCompleted() {
		tasksCount == tasksCompleted
	}

	/**
	 * Determine if the step has started.  It will return true even after completed
	 * @return true if the step has started
	 */
	boolean hasStarted() {
		moveBundleStep.actualStartTime
	}

	/**
	 * The status bar color (green/red/yellow) indicating the step status to planned completion time.
	 */
	String getStatusColor() {
		// Need to test on current snapshot so that this will work historically

		if (moveBundleStep.showInGreen) {
			'green'
		}
		else if (tasksCompleted == tasksCount) {
			moveBundleStep.actualCompletionTime > moveBundleStep.planCompletionTime ? 'red' : 'green'
		}
		else {
			if (dialIndicator < 25) {
				'red'
			}
			else if (dialIndicator >= 25 && dialIndicator < 50) {
				'yellow'
			}
			else {
				'green'
			}
		}
	}

	String toString() {
		moveBundleStep.label + ' ' + dateCreated
	}

}
