package net.transitionmanager.task.cpm


import spock.lang.Shared
import spock.lang.Specification

class DirectedGraphSpec extends Specification {

	@Shared
	DirectedGraphTestHelper directedGraphTestHelper = new DirectedGraphTestHelper()

	void 'can create calculate vertices for a DirectedGraph'() {

		when: 'a new instance of DirectedGraph is created'
			DirectedGraph graph = directedGraphTestHelper.createAcyclicDirectedGraph()

		then: 'vertices can be calculated'
			graph.vertices == 8
	}

	void 'test can check DirectedGraph without cycles'() {

		when: 'a new instance of DirectedGraph is created'
			DirectedGraph graph = directedGraphTestHelper.createAcyclicDirectedGraph()

		then: 'vertices can be calculated'
			graph.isCyclic() == false
	}

	void 'test can detect cycles in a DirectedGraph'() {

		when: 'a new instance of DirectedGraph is created with a cycle'
			DirectedGraph graph = directedGraphTestHelper.createCyclicDirectedGraph()

		then: 'vertices can be calculated'
			graph.isCyclic() == true
	}

	void 'test can detect self loop cycle in a DirectedGraph'() {

		when: 'a new instance of DirectedGraph is created with a self loop cycle'
			DirectedGraph graph = directedGraphTestHelper.createCyclicDirectedGraphWithSelfLoop()

		then: 'vertices can be calculated'
			graph.isCyclic() == true
	}
}
