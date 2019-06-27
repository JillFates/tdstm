package net.transitionmanager.task.timeline

import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineGraphCycleFinder2Spec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	void 'test can check cycle in an acyclic directed graph with one start and one sink'() {

		when:
			TaskTimeLineGraphCycleFinder2 finder = new TaskTimeLineGraphCycleFinder2(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()
			)

		then:
			!finder.isCyclic()
	}

	void 'test can check cycle in an acyclic directed graph with two starts and one sink'() {

		when:
			TaskTimeLineGraphCycleFinder2 finder = new TaskTimeLineGraphCycleFinder2(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()
			)

		then:
			!finder.isCyclic()
	}

	void 'test can check cycle in an acyclic directed graph with one start and two sinks'() {

		when:
			TaskTimeLineGraphCycleFinder2 finder = new TaskTimeLineGraphCycleFinder2(
				taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()
			)

		then:
			!finder.isCyclic()
	}

	void 'test can check cycle in a cyclic directed graph'() {

		when:
			TaskTimeLineGraphCycleFinder2 finder = new TaskTimeLineGraphCycleFinder2(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraph()
			)

		then:
			finder.isCyclic()
	}

	void 'test can check cycle in a cyclic directed graph with self loop'() {

		when:
			TaskTimeLineGraphCycleFinder2 finder = new TaskTimeLineGraphCycleFinder2(
				taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithSelfLoop()
			)

		then:
			finder.isCyclic()
	}

	void 'test can check cycles in a Task with Dependencies graph'() {

		given: 'directed graph'
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createTaskAndDependenciesExample()

		when:
			TaskTimeLineGraphCycleFinder2 finder = new TaskTimeLineGraphCycleFinder2(taskTimeLineGraph)

		then:
			!finder.isCyclic()
	}

	void 'test can check cycles in a Task with Dependencies graph using cycles'() {

		given: 'directed graph'
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createTaskAndDependenciesExampleWithCycles()

		when:
			TaskTimeLineGraphCycleFinder2 finder = new TaskTimeLineGraphCycleFinder2(taskTimeLineGraph)

		then:
			finder.isCyclic()
	}
}