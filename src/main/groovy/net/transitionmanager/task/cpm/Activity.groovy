package net.transitionmanager.task.cpm

import groovy.transform.CompileStatic

/*
	Activity Structure definition
	------------------------------

										+-----+----------+-----+    	est: earliest start time
taskId: task Identification				| est |  taskId  | eet |  		lst: latest start time
										+----------------------+
duration: time to complete a task		| lst | duration | let |		est: earliest start time
										+-----+----------+-----+		est: earliest start time
 */

@CompileStatic
class Activity {

	static final String HIDDEN_SOURCE_NODE = '_HIDDEN_SOURCE_NODE_'

	String taskId
	String description
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

	List<Activity> successors = []
	List<Activity> predecessors = []

	/**
	 *
	 * @param successor
	 */
	void addSuccessor(Activity successor) {
		this.successors.add(successor)
		successor.predecessors.add(this)
	}

	void addPredecessor(Activity predecessor) {
		this.predecessors.add(predecessor)
		predecessor.successors.add(this)
	}


	@Override
	String toString() {
		return "Activity { " +
			"taskId='" + taskId + '\'' +
			", description='" + (description ?: '') + '\'' +
			", duration=" + duration +
			' }';
	}
}
