package net.transitionmanager.task.timeline

import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineCalculatorSpec extends Specification implements TaskTimeLineDataTest {

	/**
	 * Common TaskVertex Ids used in several test cases.
	 */
	static String A = 'A', B = 'B', C = 'C', D = 'D', E = 'E', F = 'F', G = 'G', H = 'H'

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	void 'test can calculate critical path for an empty list of TaskVertex'() {

		given: 'a TaskTimeLineGraph with an empty list of TaskVertex'
			TaskTimeLineGraph emptyGraph = new TaskTimeLineGraph([] as Set)

		when: 'TaskTimeLineCalculator tries to calculate its critical path'
			TaskTimeLineCalculator.calculate(emptyGraph) // It creates START and SINK BINDING NODES

		then:
			emptyGraph.V() == 2
			with(emptyGraph.vertices.first(), TaskVertex) {
				taskId == TaskVertex.BINDER_START_NODE
				duration == 0
				earliestStartTime == 0
				earliestEndTime == 0
				latestStartTime == 0
				latestEndTime == 0
				isCriticalPath()
			}
			with(emptyGraph.vertices.last(), TaskVertex) {
				taskId == TaskVertex.BINDER_SINK_NODE
				duration == 0
				earliestStartTime == 0
				earliestEndTime == 0
				latestStartTime == 0
				latestEndTime == 0
				isCriticalPath()
			}
	}

	void 'test can calculate critical path for a graph with only one TaskVertex'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex('A', 3)
				.build()

		when: 'TaskTimeLineCalculator tries to calculate its critical path'
			TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			taskTimeLineGraph.V() == 1
			with(taskTimeLineGraph.vertices.first(), TaskVertex) {
				taskId == 'A'
				duration == 3
				earliestStartTime == 0
				earliestEndTime == 3
				latestStartTime == 0
				latestEndTime == 3
				isCriticalPath()
			}
	}

	void 'test can calculate critical path for a graph with two TaskVertex'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex('A', 3).addEdgeTo('B')
				.withVertex('B', 4)
				.build()

		when: 'TaskTimeLineCalculator tries to calculate its critical path'
			TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			taskTimeLineGraph.V() == 2
			with(taskTimeLineGraph.vertices.first(), TaskVertex) {
				taskId == 'A'
				duration == 3
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 3
				latestEndTime == 3
				isCriticalPath()
			}
			with(taskTimeLineGraph.vertices.last(), TaskVertex) {
				taskId == 'B'
				duration == 4
				earliestStartTime == 3
				latestStartTime == 3
				earliestEndTime == 7
				latestEndTime == 7
				isCriticalPath()
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one start'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex('A', 3).addEdgesTo('B', 'C')
				.withVertex('B', 4)
				.withVertex('C', 2)
				.build()

		when: 'TaskTimeLineCalculator tries to calculate its critical path'
			TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			taskTimeLineGraph.V() == 4
			with(taskTimeLineGraph.vertices[0], TaskVertex) {
				taskId == 'A'
				duration == 3
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 3
				latestEndTime == 3
				isCriticalPath()
			}
			with(taskTimeLineGraph.vertices[1], TaskVertex) {
				taskId == 'B'
				duration == 4
				earliestStartTime == 3
				latestStartTime == 3
				earliestEndTime == 7
				latestEndTime == 7
				isCriticalPath()
			}
			with(taskTimeLineGraph.vertices[2], TaskVertex) {
				taskId == 'C'
				duration == 2
				earliestStartTime == 3
				latestStartTime == 5
				earliestEndTime == 5
				latestEndTime == 7
				!isCriticalPath()
			}
			with(taskTimeLineGraph.vertices[3], TaskVertex) {
				taskId == TaskVertex.BINDER_SINK_NODE
				duration == 0
				earliestStartTime == 7
				latestStartTime == 7
				earliestEndTime == 7
				latestEndTime == 7
				isCriticalPath()
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex('B', 4).addEdgeTo('D')
				.withVertex('C', 2).addEdgeTo('D')
				.withVertex('D', 5)
				.build()

		when: 'TaskTimeLineCalculator tries to calculate its critical path'
			TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			taskTimeLineGraph.V() == 4
			with(taskTimeLineGraph.vertices[0], TaskVertex) {
				taskId == TaskVertex.BINDER_START_NODE
				duration == 0
				earliestStartTime == 0
				latestStartTime == 2
				earliestEndTime == 0
				latestEndTime == 2
				!isCriticalPath()
			}
			with(taskTimeLineGraph.vertices[1], TaskVertex) {
				taskId == 'B'
				duration == 4
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 4
				latestEndTime == 4
				isCriticalPath()
			}
			with(taskTimeLineGraph.vertices[2], TaskVertex) {
				taskId == 'C'
				duration == 2
				earliestStartTime == 0
				latestStartTime == 2
				earliestEndTime == 2
				latestEndTime == 4
				!isCriticalPath()
			}
			with(taskTimeLineGraph.vertices[3], TaskVertex) {
				taskId == 'D'
				duration == 5
				earliestStartTime == 4
				latestStartTime == 4
				earliestEndTime == 9
				latestEndTime == 9
				isCriticalPath()
			}
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(A, 3).addEdgesTo(B, C, D)
				.withVertex(B, 4).addEdgeTo(D)
				.withVertex(C, 2).addEdgeTo(D)
				.withVertex(D, 5)
				.build()

		when: 'TaskTimeLineCalculator tries to calculate its critical path'
			Set<TaskVertex> results = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 4
			withTaskVertex(taskTimeLineGraph.vertices[0],
				[0, A, 3],
				[0, 3, 3],
			)
			withTaskVertex(taskTimeLineGraph.vertices[1],
				[3, B, 7],
				[3, 4, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[2],
				[3, C, 5],
				[5, 2, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[3],
				[7, D, 12],
				[7, 5, 12],
			)

			withTaskTimeLineGraph(taskTimeLineGraph, """
				Task	dur.	est.	eed.	lst.	led.	CriticalPath?
				A		3		0		3		0		3		true
				B		4		3		7		3		7		true
				C		2		3		5		5		7		false
				D		5		7		12		7		12		true
			""")

		and: 'critical path vertices where returned'
			withCriticalPath(results, [A, B, D])
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink and using a TaskVertex with duration equals zero in critical path'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(A, 3).addEdgesTo(B, C)
				.withVertex(B, 4).addEdgeTo(E)
				.withVertex(E, 0).addEdgeTo(D) // Task with duration zero in critical path
				.withVertex(C, 2).addEdgeTo(D)
				.withVertex(D, 5)
				.build()

		when: 'TaskTimeLineCalculator tries to calculate its critical path'
			Set<TaskVertex> results = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 5
			withTaskVertex(taskTimeLineGraph.vertices[0],
				[0, A, 3],
				[0, 3, 3],
			)
			withTaskVertex(taskTimeLineGraph.vertices[1],
				[3, B, 7],
				[3, 4, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[2],
				[7, E, 7],
				[7, 0, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[3],
				[3, C, 5],
				[5, 2, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[4],
				[7, D, 12],
				[7, 5, 12],
			)

			withTaskTimeLineGraph(taskTimeLineGraph, """
				Task	dur.	est.	eed.	lst.	led.	CriticalPath?
				A		3		0		3		0		3		true
				B		4		3		7		3		7		true
				E		0		7		7		7		7		true
				C		2		3		5		5		7		false
				D		5		7		12		7		12		true
			""")

		and: 'critical path vertices where returned'
			withCriticalPath(results, [A, B, E, D])

	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink and using a TaskVertex with duration equals zero NOT in critical path'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(A, 3).addEdgesTo(B, C)
				.withVertex(B, 4).addEdgeTo(D)
				.withVertex(E, 0).addEdgeTo(D) // Task with duration zero, NOT in critical path
				.withVertex(C, 2).addEdgeTo(E)
				.withVertex(D, 5)
				.build()

		when: 'TaskTimeLineCalculator tries to calculate its critical path'
			Set<TaskVertex> results = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 5
			withTaskVertex(taskTimeLineGraph.vertices[0],
				[0, A, 3],
				[0, 3, 3],
			)
			withTaskVertex(taskTimeLineGraph.vertices[1],
				[3, B, 7],
				[3, 4, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[2],
				[5, E, 5],
				[7, 0, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[3],
				[3, C, 5],
				[5, 2, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[4],
				[7, D, 12],
				[7, 5, 12],
			)

			withTaskTimeLineGraph(taskTimeLineGraph, """
				Task	dur.	est.	eed.	lst.	led.	CriticalPath?
				A		3		0		3		0		3		true
				B		4		3		7		3		7		true
				E		0		5		7		5		7		false
				C		2		3		5		5		7		false
				D		5		7		12		7		12		true
			""")

		and: 'critical path vertices where returned'
			withCriticalPath(results, [A, B, D])

	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink after randomize vertices order'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(B, 4).addEdgeTo(D)
				.withVertex(D, 5)
				.withVertex(C, 2).addEdgeTo(D)
				.withVertex(A, 3).addEdgesTo(B, C)
				.build()

		when: 'TaskTimeLineCalculator tries to calculate its critical path'
			Set<TaskVertex> results = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 4
			withTaskVertex(taskTimeLineGraph.vertices[0],
				[3, B, 7],
				[3, 4, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[1],
				[7, D, 12],
				[7, 5, 12],
			)
			withTaskVertex(taskTimeLineGraph.vertices[2],
				[3, C, 5],
				[5, 2, 7],
			)
			withTaskVertex(taskTimeLineGraph.vertices[3],
				[0, A, 3],
				[0, 3, 3],
			)

			withTaskTimeLineGraph(taskTimeLineGraph, """
				Task	dur.	est.	eed.	lst.	led.	CriticalPath?
				B		4		3		7		3		7		true
				D		5		7		12		7		12		true
				C		2		3		5		5		7		false
				A		3		0		3		0		3		true
			""")

		and: 'critical path vertices where returned'
			withCriticalPath(results, [B, D, A])
	}

	void 'test can calculate critical path method for an acyclic directed graph with one source and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		when:
			Set<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == 'A' }, TaskVertex) {
				duration == 3
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 3
				latestEndTime == 3
				isCriticalPath() == true
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskVertex) {
				duration == 4
				earliestStartTime == 3
				latestStartTime == 3
				earliestEndTime == 7
				latestEndTime == 7
				isCriticalPath() == true
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskVertex) {
				duration == 5
				earliestStartTime == 7
				latestStartTime == 7
				earliestEndTime == 12
				latestEndTime == 12
				isCriticalPath() == true
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskVertex) {
				duration == 4
				earliestStartTime == 12
				latestStartTime == 12
				earliestEndTime == 16
				latestEndTime == 16
				isCriticalPath() == true
			}
			with(criticalPath.find { it.taskId == 'H' }, TaskVertex) {
				duration == 3
				earliestStartTime == 16
				latestStartTime == 16
				earliestEndTime == 19
				latestEndTime == 19
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two starts and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()

		when:
			Set<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == TaskVertex.BINDER_START_NODE }, TaskVertex) {
				duration == 1
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 1
				latestEndTime == 1
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskVertex) {
				duration == 4
				earliestStartTime == 1
				latestStartTime == 1
				earliestEndTime == 5
				latestEndTime == 5
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskVertex) {
				duration == 5
				earliestStartTime == 5
				latestStartTime == 5
				earliestEndTime == 10
				latestEndTime == 10
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskVertex) {
				duration == 4
				earliestStartTime == 10
				latestStartTime == 10
				earliestEndTime == 14
				latestEndTime == 14
			}
			with(criticalPath.find { it.taskId == 'H' }, TaskVertex) {
				duration == 3
				earliestStartTime == 14
				latestStartTime == 14
				earliestEndTime == 17
				latestEndTime == 17
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with one start and two sinks'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()

		when:
			Set<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == 'A' }, TaskVertex) {
				duration == 3
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 3
				latestEndTime == 3
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskVertex) {
				duration == 4
				earliestStartTime == 3
				latestStartTime == 3
				earliestEndTime == 7
				latestEndTime == 7
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskVertex) {
				duration == 5
				earliestStartTime == 7
				latestStartTime == 7
				earliestEndTime == 12
				latestEndTime == 12
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskVertex) {
				duration == 4
				earliestStartTime == 12
				latestStartTime == 12
				earliestEndTime == 16
				latestEndTime == 16
			}
			with(criticalPath.find { it.taskId == TaskVertex.BINDER_SINK_NODE }, TaskVertex) {
				duration == 1
				earliestStartTime == 16
				latestStartTime == 16
				earliestEndTime == 17
				latestEndTime == 17
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two start and two sinks'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndTwoSinks()

		when:
			Set<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			with(criticalPath.find { it.taskId == TaskVertex.BINDER_START_NODE }, TaskVertex) {
				duration == 1
				earliestStartTime == 0
				latestStartTime == 0
				earliestEndTime == 1
				latestEndTime == 1
			}
			with(criticalPath.find { it.taskId == 'B' }, TaskVertex) {
				duration == 4
				earliestStartTime == 1
				latestStartTime == 1
				earliestEndTime == 5
				latestEndTime == 5
			}
			with(criticalPath.find { it.taskId == 'D' }, TaskVertex) {
				duration == 5
				earliestStartTime == 5
				latestStartTime == 5
				earliestEndTime == 10
				latestEndTime == 10
			}
			with(criticalPath.find { it.taskId == 'G' }, TaskVertex) {
				duration == 4
				earliestStartTime == 10
				latestStartTime == 10
				earliestEndTime == 14
				latestEndTime == 14
			}
			with(criticalPath.find { it.taskId == TaskVertex.BINDER_SINK_NODE }, TaskVertex) {
				duration == 1
				earliestStartTime == 14
				latestStartTime == 14
				earliestEndTime == 15
				latestEndTime == 15
			}
	}

	void 'test can calculate critical path method for group a task and dependencies'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createTaskAndDependenciesExample()

		when:
			Set<TaskVertex> criticalPath = TaskTimeLineCalculator.calculate(taskTimeLineGraph)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 6
			criticalPath.collect { it.taskId } == [
				TaskVertex.BINDER_START_NODE,
				'3',
				'11',
				'13',
				'16',
				TaskVertex.BINDER_SINK_NODE
			]
	}
}
