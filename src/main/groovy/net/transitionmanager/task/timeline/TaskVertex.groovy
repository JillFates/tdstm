package net.transitionmanager.task.timeline

import groovy.transform.CompileStatic
import net.transitionmanager.task.TaskNode

/*
		TaskVertex Structure definition, compound with TaskNode
		-----------------------------------------------------------------------

											+-----+----------+-----+    	est: earliest start time
	taskId: task Identification				| est |  taskId  | eet |  		eet: earliest end time
											+----------------------+
	duration: time to complete a task		| lst | duration | let |		lst: latest start time
											+-----+----------+-----+		let: latest end time
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

	void setLatest(int maxCost) {
		latestStartTime = maxCost - criticalCost
		latestEndTime = latestStartTime + duration
	}

	boolean isSuccessor(TaskVertex taskVertex) {
		return successors.contains(taskVertex)
	}

	boolean isDependent(TaskVertex taskVertex) {
		//is t a direct dependency?
		if (successors.contains(taskVertex)) {
			return true;
		}
		//is t an indirect dependency
		for (TaskVertex successor : successors) {
			if (successor.isDependent(taskVertex)) {
				return true;
			}
		}
		return false;
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

	String[] toStringArray() {
		String criticalCond = earliestStartTime == latestStartTime ? "Yes" : "No";
		String[] toString = [taskId, earliestStartTime + "", earliestEndTime + "", latestStartTime + "", latestEndTime + "",
							 latestStartTime - earliestStartTime + "", criticalCond];
		return toString;
	}

	@Override
	String toString() {
		return "TaskVertex { " +
			"taskId='" + taskId + '\'' +
			", description='" + (description ?: '') + '\'' +
			", duration=" + duration +
			' }';
	}

	Boolean hasPredecessor(TaskVertex taskVertex) {
		return predecessors.contains(taskVertex)
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
