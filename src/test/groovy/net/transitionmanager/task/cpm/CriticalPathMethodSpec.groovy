package net.transitionmanager.task.cpm

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

	void 'test can calculate critical path method for a list of activities'() {

		given:
			DirectedGraph directedGraph = directedGraphTestHelper.createAcyclicDirectedGraph()
		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(directedGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			criticalPath.collect { it.taskId } == ['A', 'B', 'D', 'G', 'H']

	}
}
