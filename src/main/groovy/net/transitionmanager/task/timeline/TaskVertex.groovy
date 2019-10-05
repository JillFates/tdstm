package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.TimeUtil
import groovy.time.TimeCategory
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic
class TaskVertex {

	/**
	 * Saves {@code Task#id}
	 */
	Long taskId
	/**
	 * Saves {@code Task#taskNumber}
	 */
	Integer taskNumber
	/**
	 * Task comment name
	 */
	String taskComment
	/**
	 * Duration time, in minutes, to complete current {@code TaskVertex}
	 */
	Integer duration
	/**
	 * Remaining time, in minutes, to complete current {@code TaskVertex}
	 */
	Integer remaining
	/**
	 * Elapsed time, in minutes, for completion of the current {@code TaskVertex}
	 */
	Integer elapsed

	Date actualStart
	/**
	 * @see {@code AssetComment#statusUpdated}
	 */
	// Updated when the status changes so we can compute the elapsed time that a task is in a status
	Date statusUpdated

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
	TaskVertex criticalPredecessor

	Integer latestStart = 0
	Integer latestFinish = Integer.MAX_VALUE

	/* The following fields are only needed if start/finish times must be presented as dates. */
	private Integer slack = 0

	TaskVertex(Long taskId,
			   Integer taskNumber,
			   String taskComment,
			   int duration = 0,
			   String status = AssetCommentStatus.PLANNED,
			   Date actualStart = null,
			   Date statusUpdated = null) {

		this.taskId = taskId
		this.taskNumber = taskNumber
		this.taskComment = taskComment
		this.status = status
		this.statusUpdated = statusUpdated
		this.actualStart = actualStart
		this.duration = duration
	}

	@CompileStatic(TypeCheckingMode.SKIP)
	void initialize(Date currentTime) {
		Integer earliest = 0
		Integer latest = 0
		earliestStart = earliest
		latestStart = latest
		remaining = remainingDurationInMinutes(currentTime)
		elapsed = duration - remaining

		if (isStart()) {
			earliestStart = 0 //startTime
			earliestFinish = earliestStart + remaining + elapsed

			use(TimeCategory) {
				earliestStartDate = actualStart ?: currentTime
				earliestFinishDate = earliestStartDate + remaining.minutes + elapsed.minutes
			}
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
	Integer remainingDurationInMinutes(Date currentTime) {

		if (hasFinished()) {
			return 0
		} else if (isStarted() && actualStart != null) {
			Integer elapsedTime = 0
			use(TimeCategory) {
				elapsedTime = (currentTime - actualStart).minutes
			}
			return this.duration - elapsedTime
		} else if (isOnHold() && statusUpdated != null) {
			// We assume the time that the status was updated to ON_HOLD
			// minus start time it is the amount of work done.
			Integer elapsedTime = 0
			use(TimeCategory) {
				// statusUpdated
				elapsedTime = (actualStart != null) ? (statusUpdated - actualStart).minutes : 0
			}
			return this.duration - elapsedTime
		} else {
			return this.duration
		}
	}

	/**
	 * It calculates if a {@code TaskVertex} has already finished.
	 * It is defined by {@code AssetCommentStatus#COMPLETED} and
	 * {@code AssetCommentStatus#TERMINATED}
	 * @return true if {@code TaskVertex#status} is in
	 * 		[{@code AssetCommentStatus#COMPLETED}, {@code AssetCommentStatus#TERMINATED}]
	 */
	Boolean hasFinished() {
		return status in [AssetCommentStatus.COMPLETED, AssetCommentStatus.TERMINATED]
	}

	/**
	 * It calculates if a {@code TaskVertex} has not started yet.
	 * It is defined by {@code AssetCommentStatus#PLANNED} and
	 * {@code AssetCommentStatus#PENDING}
	 * @return true if {@code TaskVertex#status} is in
	 * 		[{@code AssetCommentStatus#PLANNED}, {@code AssetCommentStatus#PENDING}]
	 */
	Boolean hasNotStarted() {
		return status in [
			AssetCommentStatus.PLANNED,
			AssetCommentStatus.PENDING
		]
	}

	/**
	 * Checks if current {@code TaskVertex} is a start node
	 * for critical analysis.
	 * @return true if {@code TaskVertex#predecessors} is empty.
	 */
	Boolean isStart() {
		return predecessors.isEmpty()
	}

	/**
	 * It calculates if a {@code TaskVertex} has already started.
	 * It is defined by {@code AssetCommentStatus#STARTED} and
	 * {@code AssetCommentStatus#HOLD}
	 * @return true if {@code TaskVertex#status} is in
	 * 		[{@code AssetCommentStatus#STARTED},
	 * 		{@code AssetCommentStatus#HOLD},
	 * 		{@code AssetCommentStatus#COMPLETED},
	 * 		{@code AssetCommentStatus#TERMINATED}]
	 */
	Boolean hasStarted() {
		return status in [
			AssetCommentStatus.STARTED,
			AssetCommentStatus.HOLD,
			AssetCommentStatus.COMPLETED,
			AssetCommentStatus.TERMINATED
		]
	}

	/**
	 * Check if {@code TaskVertex} is in status
	 * {@code AssetCommentStatus#STARTED}
	 * @return
	 */
	Boolean isStarted() {
		return status in [AssetCommentStatus.STARTED]
	}

	/**
	 * Check if {@code TaskVertex} is in status
	 * {@code AssetCommentStatus#TERMINATED}
	 * @return
	 */
	Boolean isTerminated() {
		return status in [AssetCommentStatus.TERMINATED]
	}

	/**
	 * Check if {@code TaskVertex} is in status
	 * {@code AssetCommentStatus#HOLD}
	 * @return
	 */
	Boolean isOnHold() {
		return status in [AssetCommentStatus.HOLD]
	}

	/**
	 * Checks if current {@code TaskVertex} is a sink node
	 * for critical analysis.
	 * @return true if {@code TaskVertex#successors} is empty.
	 */
	Boolean isSink() {
		return successors.isEmpty()
	}

	Boolean isCriticalPath() {
		return criticalPath
	}
	/**
	 * Calculates Slack time based on {@code TaskVertex#status}
	 * or based on the difference between {@code TaskVertex#earliestStartDate}
	 * and {@code TaskVertex#latestStartDate}
	 *
	 * @return slack size in minutes
	 */
	Integer getSlack() {
		Integer slack = 0
		if (!hasStarted()) {
			slack = TimeUtil.minutesElapsed(earliestStartDate, latestStartDate)
		}

		return slack
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
			", duration=" + duration +
			' }';
	}
}
