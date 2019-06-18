package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.DirectedGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class DirectedCycleSpec extends Specification {

	@Shared
	DirectedGraphTestHelper directedGraphTestHelper = new DirectedGraphTestHelper()


	void 'test can check cycle in an acyclic directed graph with one source and one sink'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()
			)

		then:
			!directedCycle.hasCycle()
	}

	void 'test can check cycle in an acyclic directed graph with two sources and one sink'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				directedGraphTestHelper.createAcyclicDirectedGraphWithTwoSourcesAndOneSink()
			)

		then:
			!directedCycle.hasCycle()
	}

	void 'test can check cycle in an acyclic directed graph with one source and two sinks'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndTwoSinks()
			)

		then:
			!directedCycle.hasCycle()
	}

	void 'test can check cycle in a cyclic directed graph'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				directedGraphTestHelper.createCyclicDirectedGraph()
			)

		then:
			directedCycle.hasCycle()
	}

	void 'test can check cycle in a cyclic directed graph with self loop'() {

		when:
			DirectedCycle directedCycle = new DirectedCycle(
				directedGraphTestHelper.createCyclicDirectedGraphWithSelfLoop()
			)

		then:
			directedCycle.hasCycle()
	}
}
