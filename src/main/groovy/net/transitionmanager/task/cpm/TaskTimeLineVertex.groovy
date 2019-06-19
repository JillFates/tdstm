package net.transitionmanager.task.cpm

import groovy.transform.CompileStatic

/*
	TaskTimeLineVertex Structure definition
	------------------------------

										+-----+----------+-----+    	est: earliest start time
taskId: task Identification				| est |  taskId  | eet |  		lst: latest start time
										+----------------------+
duration: time to complete a task		| lst | duration | let |		est: earliest start time
										+-----+----------+-----+		est: earliest start time
 */

@CompileStatic
class TaskTimeLineVertex {

	static final String HIDDEN_SOURCE_NODE = '_HIDDEN_SOURCE_NODE_'
	static final String HIDDEN_SINK_NODE = '_HIDDEN_SINK_NODE_'

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

	List<TaskTimeLineVertex> successors = []
	List<TaskTimeLineVertex> predecessors = []

	/**
	 *
	 * @param successor
	 */
	void addSuccessor(TaskTimeLineVertex successor) {
		this.successors.add(successor)
		successor.predecessors.add(this)
	}

	void addPredecessor(TaskTimeLineVertex predecessor) {
		this.predecessors.add(predecessor)
		predecessor.successors.add(this)
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false

		TaskTimeLineVertex that = (TaskTimeLineVertex) o

		if (taskId != that.taskId) return false

		return true
	}

	int hashCode() {
		return taskId.hashCode()
	}

	@Override
	String toString() {
		return "TaskTimeLineVertex { " +
			"taskId='" + taskId + '\'' +
			", description='" + (description ?: '') + '\'' +
			", duration=" + duration +
			' }';
	}

	//// ------------------------------------------------////
	//// ----------Factory Methods ----------------------////
	//// ------------------------------------------------////
	static class Factory {
		/**
		 * Factory Method patter to create a new instance of {@code TaskTimeLineVertex}
		 * @param taskId
		 * @param description
		 * @param duration
		 * @return
		 */
		static TaskTimeLineVertex newSimpleVertex(String taskId, String description, int duration) {
			return new TaskTimeLineVertex(taskId: taskId, description: description, duration: duration)
		}

		/**
		 * Factory Method patter to create a new instance of {@code TaskTimeLineVertex}
		 * @param taskId
		 * @param duration
		 * @return
		 */
		static TaskTimeLineVertex newSimpleVertex(String taskId, int duration) {
			return new TaskTimeLineVertex(taskId: taskId, duration: duration)
		}

		/**
		 * Factory Method patter to create a new instance of {@code TaskTimeLineVertex}
		 * @return
		 */
		static TaskTimeLineVertex newHiddenSource() {
			return new TaskTimeLineVertex(taskId: HIDDEN_SOURCE_NODE, duration: 1)
		}

		/**
		 * Factory Method patter to create a new instance of {@code TaskTimeLineVertex}
		 * @return
		 */
		static TaskTimeLineVertex newHiddenSink() {
			return new TaskTimeLineVertex(taskId: HIDDEN_SINK_NODE, duration: 1)
		}
	}
}
