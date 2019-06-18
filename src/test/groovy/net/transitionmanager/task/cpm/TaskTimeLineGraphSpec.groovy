package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineGraphSpec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	void 'can calculate vertices for a TaskTimeLineGraph'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		then: 'vertices can be calculated'
			graph.V() == 8
	}

	void 'can calculate source for a TaskTimeLineGraph with one source and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		then: 'source can be calculated'
			graph.source.taskId == 'A'
	}

	void 'can calculate sink for a TaskTimeLineGraph with one source and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		then: 'sink can be calculated'
			graph.sink.taskId == 'H'
	}

	void 'can retrieve a for a TaskTimeLineGraph with one source and one sink by taskId'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		then: 'a vertex can be retrieved'
			graph.getVertex('B').taskId == 'B'
	}

	void 'can retrieve a for a TaskTimeLineGraph with one source and one sink by an instance of TaskTimeLineVertex'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndOneSink()

		then: 'a vertex can be retrieved'
			graph.getVertex(graph.vertices.first()).taskId == 'A'
	}

	void 'can calculate source and sink for a TaskTimeLineGraph with two sources and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoSourcesAndOneSink()

		then: 'source can be calculated'
			graph.source.taskId == TaskTimeLineVertex.HIDDEN_SOURCE_NODE

		then: 'sink can be calculated'
			graph.sink.taskId == 'H'
	}

	void 'can calculate source and sink for a TaskTimeLineGraph with one source and two sinks'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneSourceAndTwoSinks()

		then: 'source can be calculated'
			graph.source.taskId == 'A'

		and: 'sink can be calculated'
			graph.sink.taskId == TaskTimeLineVertex.HIDDEN_SINK_NODE
	}
}
