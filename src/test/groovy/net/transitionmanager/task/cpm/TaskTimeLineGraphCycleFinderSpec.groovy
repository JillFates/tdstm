package net.transitionmanager.task.cpm


import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineGraphCycleFinderSpec extends Specification {

	/**
	 * Common TaskVertex Ids used in several test cases.
	 */
	static String A = 'A', B = 'B', C = 'C', D = 'D', E = 'E', F = 'F', G = 'G', H = 'H'

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	void 'test can find cycle in acyclic directed graph with one start and one sink'() {

		when:
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()
			)

		then:
			!finder.hasCycle()
	}

	void 'test can find cycle in acyclic directed graph with two starts and one sink'() {

		when:
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()
			)

		then:
			!finder.hasCycle()
	}

	void 'test can find cycle in acyclic directed graph with one start and two sinks'() {

		when:
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()
			)

		then:
			!finder.hasCycle()
	}

	void 'test can find cycle in cyclic directed graph'() {

		when: 'a cyclic graphs is created'
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraph()
			)

		then: 'TaskTimeLineGraphCycleFinder can find cycles'
			finder.hasCycle()

		and:
			finder.cycles.size() == 1
			finder.cycles[0].collect { it.taskId } == ['2', '0', '1']
	}

	void 'test can find cycle in cyclic directed graph with self loop'() {

		when:
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithSelfLoop()
			)

		then:
			finder.hasCycle()

		and:
			finder.cycles.size() == 1
			finder.cycles[0].collect { it.taskId } == ['3', '3', '3']
	}

	void 'test can find cycles in Task with Dependencies graph'() {

		given: 'directed graph'
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createTaskAndDependenciesExample()

		when:
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(taskTimeLineGraph)

		then:
			!finder.hasCycle()
	}

	void 'test can find cycles in Task with Dependencies graph using cycles'() {

		given: 'directed graph with cycles'
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createTaskAndDependenciesExampleWithCycles()

		when:
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(taskTimeLineGraph)

		then: 'it contains cycles'
			finder.hasCycle()

		and: 'a list of cycles can be returned'
			finder.cycles.size() == 2
			finder.cycles[0].collect { it.description } == ['P', 'K', 'M']
			finder.cycles[1].collect { it.description } == ['D', 'H', 'I']
	}

	void 'test can find cycles in empty list of TaskVertex'() {

		given: 'a TaskTimeLineGraph with an empty list of TaskVertex'
			TaskTimeLineGraph emptyGraph = new TaskTimeLineGraph([] as Set)

		when:
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(emptyGraph)

		then: 'it contains cycles'
			!finder.hasCycle()
	}

	void 'test can find cycles in graph with two TaskVertex'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex('A', 3).addEdgeTo('B')
				.withVertex('B', 4)
				.build()

		when:
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(taskTimeLineGraph)

		then: 'it contains cycles'
			!finder.hasCycle()
	}

	void 'test can find cycles in graph with three TaskVertex defining one start'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex('A', 3).addEdgesTo('B', 'C')
				.withVertex('B', 4)
				.withVertex('C', 2)
				.build()

		when:
			TaskTimeLineGraphCycleFinder finder = new TaskTimeLineGraphCycleFinder(taskTimeLineGraph)

		then: 'it contains cycles'
			!finder.hasCycle()
	}
}
