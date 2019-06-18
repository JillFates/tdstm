package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.DirectedGraphTestHelper
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification

class CriticalPathMethodSpec extends Specification {

	@Shared
	DirectedGraphTestHelper directedGraphTestHelper = new DirectedGraphTestHelper()
	/*
	+---+---+---+
	| 0 | A | 3 |
	+-----------+
	| 0 | 3 | 3 |
	+---+---+---+
 	*/

	void 'test can calculate critical path method for an Acyclic Directed Graph with one source and one sink'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			criticalPath.collect { it.taskId } == ['A', 'B', 'D', 'G', 'H']
	}


	void 'test can calculate critical path method for an Acyclic Directed Graph with two sources and one sink'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createAcyclicDirectedGraphWithTwoSourcesAndOneSink()

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			criticalPath.collect { it.taskId } == [Activity.HIDDEN_SOURCE_NODE, 'B', 'D', 'G', 'H']
	}

	void 'test can calculate critical path method for an Acyclic Directed Graph with one source and two sinks'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndTwoSinks()

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			criticalPath.collect { it.taskId } == ['A', 'B', 'D', 'G', Activity.HIDDEN_SOURCE_NODE]
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
