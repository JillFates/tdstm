package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineGraphCycleFinderSpec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()


	void 'test can check cycle in an acyclic directed graph with one start and one sink'() {

		when:
			TaskTimeLineGraphCycleFinder directedCycleFinder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()
			)

		then:
			!directedCycleFinder.isCyclic()
	}

	void 'test can check cycle in an acyclic directed graph with two starts and one sink'() {

		when:
			TaskTimeLineGraphCycleFinder directedCycleFinder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()
			)

		then:
			!directedCycleFinder.isCyclic()
	}

	void 'test can check cycle in an acyclic directed graph with one start and two sinks'() {

		when:
			TaskTimeLineGraphCycleFinder directedCycleFinder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()
			)

		then:
			!directedCycleFinder.isCyclic()
	}

	void 'test can check cycle in a cyclic directed graph'() {

		when:
			TaskTimeLineGraphCycleFinder directedCycleFinder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraph()
			)

		then:
			directedCycleFinder.isCyclic()
	}

	void 'test can check cycle in a cyclic directed graph with self loop'() {

		when:
			TaskTimeLineGraphCycleFinder directedCycleFinder = new TaskTimeLineGraphCycleFinder(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithSelfLoop()
			)

		then:
			directedCycleFinder.isCyclic()
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
			TaskTimeLineGraphCycleFinder directedCycleFinder = new TaskTimeLineGraphCycleFinder(taskTimeLineGraph)

		then:
			directedCycleFinder.isCyclic()

	}
}