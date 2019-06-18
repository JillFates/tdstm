package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class DirectedCycleSpec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	void 'test can check cycle in an acyclic directed graph with one source and one sink'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()
			)

		then:
			!directedCycle.hasCycle()
	}

	void 'test can check cycle in an acyclic directed graph with two sources and one sink'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoSourcesAndOneSink()
			)

		then:
			!directedCycle.hasCycle()
	}

	void 'test can check cycle in an acyclic directed graph with one source and two sinks'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndTwoSinks()
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
}
