package net.transitionmanager.task

trait CriticalPathNode {

	/**
	 * Time to complete the activity
	 */
	int duration
	/**
	 * Earliest start time
	 */
	int est
	/**
	 * Latest start time
	 */
	int lst
	/**
	 * Earliest end time
	 */
	int eet
	/**
	 * Latest end time
	 */
	int let
	/**
	 * Defines if this activity belongs to the Critical Path
	 */
	Boolean isCriticalPath = false

}