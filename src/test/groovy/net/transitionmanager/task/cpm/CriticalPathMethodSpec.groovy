package net.transitionmanager.task.cpm

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

	void 'test can calculate critical path method for an Acyclic Directed Graph with 1 source and 1 sink'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			criticalPath.collect { it.taskId } == ['A', 'B', 'D', 'G', 'H']
	}

	void 'test can calculate critical path method for an Acyclic Directed Graph with 2 sources and 1 sink'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createAcyclicDirectedGraphWithTwoSourcesAndOneSink()

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			criticalPath.collect { it.taskId } == [Activity.HIDDEN_SOURCE_NODE, 'B', 'D', 'G', 'H']
	}

}
