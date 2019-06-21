package net.transitionmanager.task.cpm


import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class DirectedCycleFinderSpec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()


	void 'test can check cycle in an acyclic directed graph with one start and one sink'() {

		when:
			DirectedCycleFinder directedCycleFinder = new DirectedCycleFinder(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()
			)

		then:
			!directedCycleFinder.hasCycle()
	}

	void 'test can check cycle in an acyclic directed graph with two starts and one sink'() {

		when:
			DirectedCycleFinder directedCycleFinder = new DirectedCycleFinder(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()
			)

		then:
			!directedCycleFinder.hasCycle()
	}

	void 'test can check cycle in an acyclic directed graph with one start and two sinks'() {

		when:
			DirectedCycleFinder directedCycleFinder = new DirectedCycleFinder(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()
			)

		then:
			!directedCycleFinder.hasCycle()
	}

	void 'test can check cycle in a cyclic directed graph'() {

		when:
			DirectedCycleFinder directedCycleFinder = new DirectedCycleFinder(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraph()
			)

		then:
			directedCycleFinder.hasCycle()
	}

	void 'test can check cycle in a cyclic directed graph with self loop'() {

		when:
			DirectedCycleFinder directedCycleFinder = new DirectedCycleFinder(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithSelfLoop()
			)

		then:
			directedCycleFinder.hasCycle()
	}

	void 'test can check cycles in a complex graph'() {

		given: 'directed graph'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex('0', 5).addEdgesTo(['1', '2'])
				.withVertex('1', 10).addEdgeTo('2')
				.withVertex('2', 5).addEdgesTo(['1', '3'])
				.withVertex('3', 10)
				.build()

		when:
			DirectedCycleFinder directedCycleFinder = new DirectedCycleFinder(taskTimeLineGraph)

		then:
			directedCycleFinder.hasCycle() == true

	}
}
