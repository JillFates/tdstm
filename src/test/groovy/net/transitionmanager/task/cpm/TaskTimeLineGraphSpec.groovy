package net.transitionmanager.task.cpm

import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineGraphSpec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	void 'can calculate vertices for a TaskTimeLineGraph'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'vertices can be calculated'
			graph.V() == 8
	}

	void 'can calculate source for a TaskTimeLineGraph with one start and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'start can be calculated'
			graph.start.taskId == 'A'
	}

	void 'can calculate sink for a TaskTimeLineGraph with one start and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'sink can be calculated'
			graph.sink.taskId == 'H'
	}

	void 'can retrieve a for a TaskTimeLineGraph with one start and one sink by taskId'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'a vertex can be retrieved'
			graph.getVertex('B').taskId == 'B'
	}

	void 'can retrieve a for a TaskTimeLineGraph with one start and one sink by an instance of TaskTimeLineVertex'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'a vertex can be retrieved'
			graph.getVertex(graph.vertices.first()).taskId == 'A'
	}

	void 'can calculate start and sink for a TaskTimeLineGraph with two starts and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()

		then: 'start can be calculated'
			graph.start.taskId == TaskVertex.BINDER_START_NODE

		then: 'sink can be calculated'
			graph.sink.taskId == 'H'
	}

	void 'can calculate start and sink for a TaskTimeLineGraph with one start and two sinks'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()

		then: 'start can be calculated'
			graph.start.taskId == 'A'

		and: 'sink can be calculated'
			graph.sink.taskId == TaskVertex.BINDER_SINK_NODE
	}
}
