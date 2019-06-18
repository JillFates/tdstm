package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.DirectedGraphTestHelper
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

}
