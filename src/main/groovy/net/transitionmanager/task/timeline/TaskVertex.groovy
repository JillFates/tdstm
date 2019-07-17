package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.TimeUtil
import groovy.time.TimeCategory
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic
class TaskVertex {

	Integer taskNumber
	String taskComment
	/**
	 * Time to complete a Task {@code AssetComment}
	 * In minutes
	 */
	int duration

	String comment
	String description

	Date estimatedStart
	Date estimatedFinish
	Date actualStart
	String status

	List<TaskVertex> successors = []
	List<TaskVertex> predecessors = []

	TaskVertex(Integer taskNumber,
			   String taskComment,
			   int duration = 0,
			   String status = AssetCommentStatus.PLANNED,
			   Date actualStart = null) {

		this.taskNumber = taskNumber
		this.taskComment = taskComment
		this.duration = duration
		this.status = status
		this.actualStart = actualStart
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
	 * <p>Calculates remaining duration for a {@code TaskVertex}
	 * in  minutes based on 3 scenarios:</p>
	 * 1) COMPLETED/TERMINATED task. <BR/>
	 * 2) Started Task with elapsed time but did not finished. <BR/>
	 * 3) Duration time defined at task creation time. <BR/>
	 *
	 * @return {@code Integer} value with remaining time
	 */
	@CompileStatic(TypeCheckingMode.SKIP)
	Integer remainingDurationInMinutes(Date pointInTime) {

		if (status in [AssetCommentStatus.COMPLETED, AssetCommentStatus.TERMINATED]) {
			return 0
		} else if (actualStart != null) {
			Integer elapsedTime = 0
			use(TimeCategory) {
				elapsedTime = (pointInTime - actualStart).minutes
			}
			return this.duration = this.duration - elapsedTime
		} else {
			return this.duration
		}
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

		if (taskNumber != that.taskNumber) return false

		return true
	}

	int hashCode() {
		return taskNumber.hashCode()
	}

	@Override
	String toString() {
		return "TaskVertex { " +
			"taskNumber='" + taskNumber + '\'' +
			"taskComment='" + taskComment + '\'' +
			", description='" + (description ?: '') + '\'' +
			", duration=" + duration +
			' }';
	}
}
