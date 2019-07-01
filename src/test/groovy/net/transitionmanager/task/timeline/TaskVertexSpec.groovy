package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.TimeScale
import spock.lang.Specification

class TaskVertexSpec extends Specification {

	void 'test can create a TaskVertex with default constructor'() {

		setup:
			TaskVertex taskVertex = new TaskVertex(id, taskNumber)

		expect:
			with(taskVertex, TaskVertex) {
				id == id
				taskNumber == taskNumber
				duration == 0
				durationScale == TimeScale.M
			}

		where:
			id       | taskNumber
			1234567l | 'Task III'
			1234997l | ''
	}

	void 'test can create a TaskVertex with duration and time scale'() {

		setup:
			TaskVertex taskVertex = new TaskVertex(id, taskNumber, duration, timeScale)

		expect:
			with(taskVertex, TaskVertex) {
				id == id
				taskNumber == taskNumber
				duration == duration
				timeScale == timeScale
			}

		where:
			id       | taskNumber | duration | timeScale
			1234567l | 'Task III' | 360      | TimeScale.M
			1234599l | ''         | 1500     | TimeScale.H
	}

	void 'test can add predecessor to a TaskVertex'() {

		given: 'a TaskVertex instance'
			TaskVertex taskVertex = new TaskVertex(1234l, 'Task III', 360)

		and: 'another TaskVertex'
			TaskVertex predecessor = new TaskVertex(4321l, 'Task V', 1600)

		when: 'a predecessor is added'
			taskVertex.addPredecessor(predecessor)

		then: 'task vertex has predecessor'
			taskVertex.isPredecessor(predecessor)

		and: 'predecessor has a successor'
			predecessor.isSuccessor(taskVertex)
	}

	void 'test can add successor to a TaskVertex'() {

		given: 'a TaskVertex instance'
			TaskVertex taskVertex = new TaskVertex(1234l, 'Task III', 360)

		and: 'another TaskVertex'
			TaskVertex successor = new TaskVertex(4321l, 'Task V', 1600)

		when: 'a successor is added'
			taskVertex.addSuccessor(successor)

		then: 'task vertex has successor'
			taskVertex.isSuccessor(successor)

		and: 'successor has a predecessor'
			successor.isPredecessor(taskVertex)
	}
}
