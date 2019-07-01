package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.TimeScale
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

	Long id
	String taskNumber
	/**
	 * Time to complete a Task {@code AssetComment}
	 */
	int duration
	TimeScale durationScale

	String comment
	String description

	Date estimatedStart
	Date estimatedFinish
	Date actualStart
	String status

	List<TaskVertex> successors = []
	List<TaskVertex> predecessors = []

	TaskVertex(Long id, String taskNumber, int duration = 0, TimeScale durationScale = TimeScale.M) {
		this.id = id
		this.taskNumber = taskNumber
		this.duration = duration
		this.durationScale = durationScale
	}

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

	boolean isSuccessor(TaskVertex taskVertex) {
		return successors.contains(taskVertex)
	}

	Boolean isPredecessor(TaskVertex taskVertex) {
		return predecessors.contains(taskVertex)
	}

	/**
	 *
	 * @return
	 */
	Boolean isStart() {
		return predecessors.isEmpty()
	}

	/**
	 *
	 * @return
	 */
	Boolean isSink() {
		return successors.isEmpty()
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false

		TaskVertex that = (TaskVertex) o

		if (id != that.id) return false

		return true
	}

	int hashCode() {
		return id.hashCode()
	}

	@Override
	String toString() {
		return "TaskVertex { " +
			"id='" + id + '\'' +
			"taskNumber='" + taskNumber + '\'' +
			", description='" + (description ?: '') + '\'' +
			", duration=" + duration +
			' }';
	}
}
