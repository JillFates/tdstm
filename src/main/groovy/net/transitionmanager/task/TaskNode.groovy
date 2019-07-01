package net.transitionmanager.task

trait TaskNode {


	// the cost of the task along the critical path
	int criticalCost;
	/**
	 * Earliest start time
	 */
	int earliestStartTime
	/**
	 * Latest start time
	 */
	int latestStartTime
	/**
	 * Earliest end time
	 */
	int earliestEndTime
	/**
	 * Latest end time
	 */
	int latestEndTime
	/**
	 * Defines if this activity belongs to the Critical Path
	 */
	Boolean isCriticalPath() {
		return (earliestEndTime - latestEndTime == 0) && (earliestStartTime - latestStartTime == 0)
	}

}