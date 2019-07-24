package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.AssetCommentStatus
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
	Integer duration
	Integer remainingDuration

	String comment
	String description

	Date actualStart
	String status

	Boolean criticalPath = false

	Date earliestStartDate
	Date earliestFinishDate
	Date latestStartDate
	Date latestFinishDate

	List<TaskVertex> successors = []
	List<TaskVertex> predecessors = []


	Integer earliestStart = 0
	Integer earliestFinish = 0
	TaskVertex earliestPredecessor

	Integer latestStart = 0
	Integer latestFinish = Integer.MAX_VALUE
	TaskVertex latestPredecessor

	/* The following fields are only needed if start/finish times must be presented as dates. */
	Integer slack = 0

	TaskVertex(Integer taskNumber,
			   String taskComment,
			   int duration = 0,
			   String status = AssetCommentStatus.PLANNED,
			   Date actualStart = null) {

		this.taskNumber = taskNumber
		this.taskComment = taskComment
		this.status = status
		this.actualStart = actualStart
		this.duration = duration
	}

	void initialize(Date startDate) {
		Integer earliest = 0
		Integer latest = 0
		earliestStart = earliest
		latestStart = latest

		this.remainingDuration = remainingDurationInMinutes(startDate)

		if (isStart()) {
			earliestStart = 0 //startTime
			earliestFinish = earliestStart + this.remainingDuration
		}
	}

	/**
	 * This method transform the earliest/latest start/finish times into the
	 * corresponding date values.
	 * It also calculates the slack for the current task as the difference between
	 * the latest start time and the earliest start time.
	 *
	 * @param startDate
	 */
	@CompileStatic(TypeCheckingMode.SKIP)
	void calculateDatesAndSlack(Date startDate) {
		slack = latestStart - earliestStart
		use(TimeCategory) {
			earliestStartDate = startDate + earliestStart.minutes
			earliestFinishDate = startDate + earliestFinish.minutes
			latestStartDate = startDate + latestStart.minutes
			latestFinishDate = startDate + latestFinish.minutes
		}
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
			return this.duration - elapsedTime
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

	Boolean isCriticalPath() {
		return criticalPath
	}

	/**
	 * Defines this {@code TaskVertex} as a Critical Path Node.
	 */
	void markAsCriticalPath() {
		this.criticalPath = true
	}

	/**
	 * Defines this {@code TaskVertex} as a not Critical Path Node.
	 */
	void unmarkAsCriticalPath() {
		this.criticalPath = false
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
