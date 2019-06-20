package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineCalculatorSpec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	void 'test can calculate critical path method for an acyclic directed graph with one source and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		when:
			List<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == 'A' }, TaskVertex) {
				duration == 3
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 3
				latestEndTime == 3
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskVertex) {
				duration == 4
				earliestStartTime == 3
				latestStartTime == 3
				earliestEndTime == 7
				latestEndTime == 7
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskVertex) {
				duration == 5
				earliestStartTime == 7
				latestStartTime == 7
				earliestEndTime == 12
				latestEndTime == 12
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskVertex) {
				duration == 4
				earliestStartTime == 12
				latestStartTime == 12
				earliestEndTime == 16
				latestEndTime == 16
			}
			with(criticalPath.find { it.taskId == 'H' }, TaskVertex) {
				duration == 3
				earliestStartTime == 16
				latestStartTime == 16
				earliestEndTime == 19
				latestEndTime == 19
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two sources and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()

		when:
			List<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == TaskVertex.BINDER_START_NODE }, TaskVertex) {
				duration == 1
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 1
				latestEndTime == 1
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskVertex) {
				duration == 4
				earliestStartTime == 1
				latestStartTime == 1
				earliestEndTime == 5
				latestEndTime == 5
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskVertex) {
				duration == 5
				earliestStartTime == 5
				latestStartTime == 5
				earliestEndTime == 10
				latestEndTime == 10
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskVertex) {
				duration == 4
				earliestStartTime == 10
				latestStartTime == 10
				earliestEndTime == 14
				latestEndTime == 14
			}
			with(criticalPath.find { it.taskId == 'H' }, TaskVertex) {
				duration == 3
				earliestStartTime == 14
				latestStartTime == 14
				earliestEndTime == 17
				latestEndTime == 17
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with one source and two sinks'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()

		when:
			List<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == 'A' }, TaskVertex) {
				duration == 3
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 3
				latestEndTime == 3
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskVertex) {
				duration == 4
				earliestStartTime == 3
				latestStartTime == 3
				earliestEndTime == 7
				latestEndTime == 7
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskVertex) {
				duration == 5
				earliestStartTime == 7
				latestStartTime == 7
				earliestEndTime == 12
				latestEndTime == 12
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskVertex) {
				duration == 4
				earliestStartTime == 12
				latestStartTime == 12
				earliestEndTime == 16
				latestEndTime == 16
			}
			with(criticalPath.find { it.taskId == TaskVertex.BINDER_SINK_NODE }, TaskVertex) {
				duration == 1
				earliestStartTime == 16
				latestStartTime == 16
				earliestEndTime == 17
				latestEndTime == 17
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two sources and two sinks'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndTwoSinks()

		when:
			List<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == TaskVertex.BINDER_START_NODE }, TaskVertex) {
				duration == 1
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 1
				latestEndTime == 1
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskVertex) {
				duration == 4
				earliestStartTime == 1
				latestStartTime == 1
				earliestEndTime == 5
				latestEndTime == 5
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskVertex) {
				duration == 5
				earliestStartTime == 5
				latestStartTime == 5
				earliestEndTime == 10
				latestEndTime == 10
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskVertex) {
				duration == 4
				earliestStartTime == 10
				latestStartTime == 10
				earliestEndTime == 14
				latestEndTime == 14
			}
			with(criticalPath.find { it.taskId == TaskVertex.BINDER_SINK_NODE }, TaskVertex) {
				duration == 1
				earliestStartTime == 14
				latestStartTime == 14
				earliestEndTime == 15
				latestEndTime == 15
			}
	}

	void 'test can calculate critical path method for group a task and dependencies'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createTaskAndDependenciesExample()

		when:
			List<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 6
			criticalPath.collect { it.taskId } == [
				TaskVertex.BINDER_START_NODE,
				'3',
				'11',
				'13',
				'16',
				TaskVertex.BINDER_SINK_NODE
			]
	}
}
