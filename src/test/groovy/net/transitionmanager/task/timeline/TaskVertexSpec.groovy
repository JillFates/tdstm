package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.TimeUtil
import spock.lang.Specification
import spock.lang.Unroll

class TaskVertexSpec extends Specification {

	void 'test can create a TaskVertex with default constructor'() {

		setup:
			TaskVertex taskVertex = new TaskVertex(taskId, taskNumber, taskComment)

		expect:
			with(taskVertex, TaskVertex) {
				taskId == taskId
				taskNumber == taskNumber
				taskComment == taskComment
				duration == 0
			}

		where:
			taskId   | taskNumber | taskComment
			1234567l | 1234567    | 'Task III'
			1234997l | 1234997    | ''
	}

	@Unroll
	void 'test can calculate remaining time duration in minutes based on duration = #duration, actual start = #actualStart and status = #status'() {

		setup:
			Date pointInTime = TimeUtil.nowGMT()

		expect:
			new TaskVertex(
				9999999l,
				12345,
				'TASK III',
				duration,
				status,
				actualStart
			).remainingDurationInMinutes(pointInTime) == taskDuration

		where:
			duration | status                        | actualStart || taskDuration
			1        | AssetCommentStatus.PLANNED    | null        || 1
			1        | AssetCommentStatus.COMPLETED  | null        || 0
			1        | AssetCommentStatus.TERMINATED | null        || 0
	}

	void 'test can calculate remaining time for task already started'() {

		given: 'a task with duration 30 minutes and started 10 minutes before'
			Date pointInTime = TimeUtil.nowGMT()
			Date taskActualStart = TimeUtil.dateFromUsingMinutes(-10, pointInTime)

		and:
			TaskVertex taskVertex = new TaskVertex(
				9999999l,
				12345,
				'TASK III',
				30,
				AssetCommentStatus.STARTED,
				taskActualStart
			)

		when: 'remaining duration time is calculated'
			Integer remainingMinutes = taskVertex.remainingDurationInMinutes(pointInTime)

		then: 'remaining duration is less than 20 minutes'
			remainingMinutes == 20
	}

	void 'test can add predecessor to a TaskVertex'() {

		given: 'a TaskVertex instance'
			TaskVertex taskVertex = new TaskVertex(9999999l, 1234, 'Task III', 360)

		and: 'another TaskVertex'
			TaskVertex predecessor = new TaskVertex(9999998l, 4321, 'Task V', 1600)

		when: 'a predecessor is added'
			taskVertex.addPredecessor(predecessor)

		then: 'task vertex has predecessor'
			taskVertex.isPredecessor(predecessor)

		and: 'predecessor has a successor'
			predecessor.isSuccessor(taskVertex)
	}

	void 'test can add successor to a TaskVertex'() {

		given: 'a TaskVertex instance'
			TaskVertex taskVertex = new TaskVertex(9999999l, 1234, 'Task III', 360)

		and: 'another TaskVertex'
			TaskVertex successor = new TaskVertex(9999988l, 4321, 'Task V', 1600)

		when: 'a successor is added'
			taskVertex.addSuccessor(successor)

		then: 'task vertex has successor'
			taskVertex.isSuccessor(successor)

		and: 'successor has a predecessor'
			successor.isPredecessor(taskVertex)
	}
}
