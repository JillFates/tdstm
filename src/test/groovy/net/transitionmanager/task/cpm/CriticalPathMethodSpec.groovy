package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.DirectedGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class CriticalPathMethodSpec extends Specification {

	@Shared
	DirectedGraphTestHelper directedGraphTestHelper = new DirectedGraphTestHelper()

	void 'test can calculate critical path method for an Acyclic Directed Graph with one source and one sink'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == 'A' }, Activity) {
				duration == 3
				est == 0
				lst == 0
				eet == 3
				let == 3
			}
			with(criticalPath.find { it.taskId == 'B' }, Activity) {
				duration == 4
				est == 3
				lst == 3
				eet == 7
				let == 7
			}
			with(criticalPath.find { it.taskId == 'D' }, Activity) {
				duration == 5
				est == 7
				lst == 7
				eet == 12
				let == 12
			}
			with(criticalPath.find { it.taskId == 'G' }, Activity) {
				duration == 4
				est == 12
				lst == 12
				eet == 16
				let == 16
			}
			with(criticalPath.find { it.taskId == 'H' }, Activity) {
				duration == 3
				est == 16
				lst == 16
				eet == 19
				let == 19
			}
	}

	void 'test can calculate critical path method for an Acyclic Directed Graph with two sources and one sink'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createAcyclicDirectedGraphWithTwoSourcesAndOneSink()

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == Activity.HIDDEN_SOURCE_NODE }, Activity) {
				duration == 1
				est == 0
				lst == 0
				eet == 1
				let == 1
			}
			with(criticalPath.find { it.taskId == 'B' }, Activity) {
				duration == 4
				est == 1
				lst == 1
				eet == 5
				let == 5
			}
			with(criticalPath.find { it.taskId == 'D' }, Activity) {
				duration == 5
				est == 5
				lst == 5
				eet == 10
				let == 10
			}
			with(criticalPath.find { it.taskId == 'G' }, Activity) {
				duration == 4
				est == 10
				lst == 10
				eet == 14
				let == 14
			}
			with(criticalPath.find { it.taskId == 'H' }, Activity) {
				duration == 3
				est == 14
				lst == 14
				eet == 17
				let == 17
			}
	}

	void 'test can calculate critical path method for an Acyclic Directed Graph with one source and two sinks'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndTwoSinks()

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == 'A' }, Activity) {
				duration == 3
				est == 0
				lst == 0
				eet == 3
				let == 3
			}
			with(criticalPath.find { it.taskId == 'B' }, Activity) {
				duration == 4
				est == 3
				lst == 3
				eet == 7
				let == 7
			}
			with(criticalPath.find { it.taskId == 'D' }, Activity) {
				duration == 5
				est == 7
				lst == 7
				eet == 12
				let == 12
			}
			with(criticalPath.find { it.taskId == 'G' }, Activity) {
				duration == 4
				est == 12
				lst == 12
				eet == 16
				let == 16
			}
			with(criticalPath.find { it.taskId == Activity.HIDDEN_SOURCE_NODE }, Activity) {
				duration == 1
				est == 16
				lst == 16
				eet == 17
				let == 17
			}

	}

	void 'test can calculate critical path method for group a task and dependencies'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createTaskAndDependenciesExample()

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 6
			criticalPath.collect { it.taskId } == [
				Activity.HIDDEN_SOURCE_NODE,
				'3',
				'11',
				'13',
				'16',
				Activity.HIDDEN_SOURCE_NODE
			]
	}
}
