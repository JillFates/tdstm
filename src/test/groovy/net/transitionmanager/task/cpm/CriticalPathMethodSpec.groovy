package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class CriticalPathMethodSpec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	void 'test can calculate critical path method for an acyclic directed graph with one source and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		when:
			List<TaskTimeLineVertex> criticalPath = CriticalPathMethod.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == 'A' }, TaskTimeLineVertex) {
				duration == 3
				est == 0
				lst == 0
				eet == 3
				let == 3
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskTimeLineVertex) {
				duration == 4
				est == 3
				lst == 3
				eet == 7
				let == 7
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskTimeLineVertex) {
				duration == 5
				est == 7
				lst == 7
				eet == 12
				let == 12
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskTimeLineVertex) {
				duration == 4
				est == 12
				lst == 12
				eet == 16
				let == 16
			}
			with(criticalPath.find { it.taskId == 'H' }, TaskTimeLineVertex) {
				duration == 3
				est == 16
				lst == 16
				eet == 19
				let == 19
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two sources and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoSourcesAndOneSink()

		when:
			List<TaskTimeLineVertex> criticalPath = CriticalPathMethod.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == TaskTimeLineVertex.HIDDEN_SOURCE_NODE }, TaskTimeLineVertex) {
				duration == 1
				est == 0
				lst == 0
				eet == 1
				let == 1
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskTimeLineVertex) {
				duration == 4
				est == 1
				lst == 1
				eet == 5
				let == 5
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskTimeLineVertex) {
				duration == 5
				est == 5
				lst == 5
				eet == 10
				let == 10
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskTimeLineVertex) {
				duration == 4
				est == 10
				lst == 10
				eet == 14
				let == 14
			}
			with(criticalPath.find { it.taskId == 'H' }, TaskTimeLineVertex) {
				duration == 3
				est == 14
				lst == 14
				eet == 17
				let == 17
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with one source and two sinks'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndTwoSinks()

		when:
			List<TaskTimeLineVertex> criticalPath = CriticalPathMethod.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == 'A' }, TaskTimeLineVertex) {
				duration == 3
				est == 0
				lst == 0
				eet == 3
				let == 3
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskTimeLineVertex) {
				duration == 4
				est == 3
				lst == 3
				eet == 7
				let == 7
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskTimeLineVertex) {
				duration == 5
				est == 7
				lst == 7
				eet == 12
				let == 12
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskTimeLineVertex) {
				duration == 4
				est == 12
				lst == 12
				eet == 16
				let == 16
			}
			with(criticalPath.find { it.taskId == TaskTimeLineVertex.HIDDEN_SINK_NODE }, TaskTimeLineVertex) {
				duration == 1
				est == 16
				lst == 16
				eet == 17
				let == 17
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two sources and two sinks'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoSourcesAndTwoSinks()

		when:
			List<TaskTimeLineVertex> criticalPath = CriticalPathMethod.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == TaskTimeLineVertex.HIDDEN_SOURCE_NODE }, TaskTimeLineVertex) {
				duration == 1
				est == 0
				lst == 0
				eet == 1
				let == 1
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskTimeLineVertex) {
				duration == 4
				est == 1
				lst == 1
				eet == 5
				let == 5
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskTimeLineVertex) {
				duration == 5
				est == 5
				lst == 5
				eet == 10
				let == 10
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskTimeLineVertex) {
				duration == 4
				est == 10
				lst == 10
				eet == 14
				let == 14
			}
			with(criticalPath.find { it.taskId == TaskTimeLineVertex.HIDDEN_SINK_NODE }, TaskTimeLineVertex) {
				duration == 1
				est == 14
				lst == 14
				eet == 15
				let == 15
			}
	}

	void 'test can calculate critical path method for group a task and dependencies'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createTaskAndDependenciesExample()

		when:
			List<TaskTimeLineVertex> criticalPath = CriticalPathMethod.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 6
			criticalPath.collect { it.taskId } == [
				TaskTimeLineVertex.HIDDEN_SOURCE_NODE,
				'3',
				'11',
				'13',
				'16',
				TaskTimeLineVertex.HIDDEN_SINK_NODE
			]
	}
}
