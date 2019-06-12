package net.transitionmanager.task.cpm

import groovy.transform.CompileStatic

@CompileStatic
class Activity {

	String taskId;
	String description;
	/**
	 * Time to complete the activity
	 */
	int duration;
	/**
	 * Earliest start time
	 */
	int est;
	/**
	 * Latest start time
	 */
	int lst;
	/**
	 * Earliest end time
	 */
	int eet;
	/**
	 * Latest end time
	 */
	int let;
	/**
	 * Defines if this activity belongs to the Critical Path
	 */
	Boolean isCriticalPath = false

	Set<Activity> successors = []
	Set<Activity> predecessors = []

}
