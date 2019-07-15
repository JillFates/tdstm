package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.TimeScale
import groovy.transform.CompileStatic

@CompileStatic
class TaskVertex {

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

	Integer remainingDurationInMinutes() {
		//      if (status in [COMPLETED, TERMINATED]){
//		return 0
//	   } else if (vertex.startTime != null) {
//			return this.duration = duration - ((NOW - actualStart)? :)
//		} else {
//			return this.duration
//		}

		return duration
	}

	/**
	 * A <b>source</b> or <b>start</b> is a vertex with not predecessors
	 * or incoming edges
	 * @return
	 */
	Boolean isStart() {
		return predecessors.isEmpty()
	}

	/**
	 * A <b>sink</b> is a vertex without successors
	 * or outgoing edges
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
