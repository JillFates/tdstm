package net.transitionmanager.task.cpm


import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class DirectedCycleSpec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()


	void 'test can check cycle in an acyclic directed graph with one start and one sink'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()
			)

		then:
			!directedCycle.hasCycle()
	}

	void 'test can check cycle in an acyclic directed graph with two starts and one sink'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()
			)

		then:
			!directedCycle.hasCycle()
	}

	void 'test can check cycle in an acyclic directed graph with one start and two sinks'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()
			)

		then:
			!directedCycle.hasCycle()
	}

	void 'test can check cycle in a cyclic directed graph'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraph()
			)

		then:
			directedCycle.hasCycle()
	}

	void 'test can check cycle in a cyclic directed graph with self loop'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithSelfLoop()
			)

		then:
			directedCycle.hasCycle()
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
			DirectedCycle directedCycle = new DirectedCycle(taskTimeLineGraph)

		then:
			directedCycle.hasCycle() == true

	}
}
