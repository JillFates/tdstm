package net.transitionmanager.task.cpm

import groovy.transform.CompileStatic
import net.transitionmanager.task.TaskNode

/*
		TaskVertex Structure definition, compound with TaskNode
		-----------------------------------------------------------------------

											+-----+----------+-----+    	est: earliest start time
	taskId: task Identification				| est |  taskId  | eet |  		lst: latest start time
											+----------------------+
	duration: time to complete a task		| lst | duration | let |		est: earliest start time
											+-----+----------+-----+		est: earliest start time
 */

@CompileStatic
class TaskVertex implements TaskNode {

	static final String BINDER_START_NODE = '_BINDER_START_NODE_'
	static final String BINDER_SINK_NODE = '_BINDER_SINK_NODE_'

	String taskId
	String description

	List<TaskVertex> successors = []
	List<TaskVertex> predecessors = []

	/**
	 *
	 * @param successor
	 */
	void addSuccessor(TaskVertex successor) {
		this.successors.add(successor)
		successor.predecessors.add(this)
	}

	void addPredecessor(TaskVertex predecessor) {
		this.predecessors.add(predecessor)
		predecessor.successors.add(this)
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false

		TaskVertex that = (TaskVertex) o

		if (taskId != that.taskId) return false

		return true
	}

	int hashCode() {
		return taskId.hashCode()
	}

	@Override
	String toString() {
		return "TaskVertex { " +
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
		 * Factory Method patter to create a new instance of {@code TaskVertex}
		 * @param taskId
		 * @param description
		 * @param duration
		 * @return
		 */
		static TaskVertex newSimpleVertex(String taskId, String description, int duration) {
			return new TaskVertex(taskId: taskId, description: description, duration: duration)
		}

		/**
		 * Factory Method patter to create a new instance of {@code TaskVertex}
		 * @param taskId
		 * @param duration
		 * @return
		 */
		static TaskVertex newSimpleVertex(String taskId, int duration) {
			return new TaskVertex(taskId: taskId, duration: duration)
		}

		/**
		 * Factory Method patter to create a new instance of {@code TaskVertex}
		 * @return
		 */
		static TaskVertex newBinderStart() {
			return new TaskVertex(taskId: BINDER_START_NODE, duration: 0)
		}

		/**
		 * Factory Method patter to create a new instance of {@code TaskVertex}
		 * @return
		 */
		static TaskVertex newBinderSink() {
			return new TaskVertex(taskId: BINDER_SINK_NODE, duration: 0)
		}
	}
}
