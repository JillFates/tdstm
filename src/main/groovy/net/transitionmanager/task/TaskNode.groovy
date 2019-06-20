package net.transitionmanager.task

trait TaskNode {

	/**
	 * Time to complete the activity
	 */
	int duration
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
	Boolean isCriticalPath = false

}