package net.transitionmanager.task.cpm

import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification

class DirectedGraphSpec extends Specification {

	@Shared
	DirectedGraphTestHelper directedGraphTestHelper = new DirectedGraphTestHelper()

	void 'can calculate vertices for a DirectedGraph'() {

		when: 'a new instance of DirectedGraph is created'
			DirectedGraph graph = directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		then: 'vertices can be calculated'
			graph.vertices == 8
	}

	void 'can calculate source for a DirectedGraph with One Source and One Sink'() {

		when: 'a new instance of DirectedGraph is created'
			DirectedGraph graph = directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		then: 'source can be calculated'
			graph.source.taskId == 'A'
	}

	void 'can calculate sink for a DirectedGraph with One Source and One Sink'() {

		when: 'a new instance of DirectedGraph is created'
			DirectedGraph graph = directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		then: 'source can be calculated'
			graph.sink.taskId == 'H'
	}

	void 'test can check DirectedGraph without cycles'() {

		when: 'a new instance of DirectedGraph is created'
			DirectedGraph graph = directedGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

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
