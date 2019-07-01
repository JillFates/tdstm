package net.transitionmanager.task.timeline

import net.transitionmanager.task.timeline.helper.TaskTimeLineGraphTestHelper
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

	void 'can calculate sources for a TaskTimeLineGraph with one start and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'start can be calculated'
			graph.starts.size() == 1
			graph.starts[0].taskNumber == 'A'
	}

	void 'can calculate sink for a TaskTimeLineGraph with one start and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'sink can be calculated'
			graph.sinks.size() == 1
			graph.sinks[0].taskNumber == 'H'
	}

	void 'can retrieve a for a TaskTimeLineGraph with one start and one sink by taskId'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'a vertex can be retrieved'
			graph.starts.size() == 1
			graph.starts[0].taskNumber == 'A'

		and:
			graph.sinks.size() == 1
			graph.sinks[0].taskNumber == 'H'
	}

	void 'can retrieve a for a TaskTimeLineGraph with one start and one sink by an instance of TaskTimeLineVertex'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'a vertex can be retrieved'
			graph.getVertex(graph.vertices.first()).taskNumber == 'A'
	}

	void 'can calculate start and sink for a TaskTimeLineGraph with two starts and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()

		then: 'start can be calculated'
			graph.starts.size() == 2
			graph.starts[0].taskNumber == 'B'
			graph.starts[1].taskNumber == 'C'

		then: 'sink can be calculated'
			graph.sinks.size() == 1
			graph.sinks[0].taskNumber == 'H'
	}

	void 'can calculate start and sink for a TaskTimeLineGraph with one start and two sinks'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()

		then: 'start can be calculated'
			graph.starts.size() == 1
			graph.starts[0].taskNumber == 'A'

		and: 'sink can be calculated'
			graph.sinks.size() == 2
			graph.sinks[0].taskNumber == 'G'
			graph.sinks[1].taskNumber == 'H'
	}
}
