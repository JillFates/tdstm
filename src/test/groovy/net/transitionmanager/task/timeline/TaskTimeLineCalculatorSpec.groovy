package net.transitionmanager.task.timeline

import net.transitionmanager.task.timeline.helper.TaskTimeLineGraphTestHelper
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

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(emptyGraph).calculate(new Date())

		then:
			emptyGraph.V() == 0
	}

	void 'test can calculate critical path for a graph with only one TaskVertex'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, 'A', 3)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(new Date())

		then:
			taskTimeLineGraph.V() == 1
			with(taskTimeLineGraph.vertices.first(), TaskVertex) {
				taskNumber == 'A'
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
				.withVertex(1, 'A', 3).addEdgeTo('B')
				.withVertex(2, 'B', 4)
				.build()

		and:
			TimeLine timeLine = new TimeLine(taskTimeLineGraph)

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = timeLine.calculate()

		then:
			taskTimeLineGraph.V() == 2

		and:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				earliestPaths.size() == 1
				latestPaths.size() == 1
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one start'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, 'A', 3).addEdgesTo('B', 'C')
				.withVertex(2, 'B', 4)
				.withVertex(3, 'C', 2)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(new Date())

		then:
			taskTimeLineGraph.V() == 3

			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				earliestPaths.size() == 1
				latestPaths.size() == 1
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, 'B', 4).addEdgeTo('D')
				.withVertex(2, 'C', 2).addEdgeTo('D')
				.withVertex(3, 'D', 5)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(new Date())

		then:
			taskTimeLineGraph.V() == 4
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				earliestPaths.size() == 1
				latestPaths.size() == 1
			}
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 3).addEdgesTo(B, C, D)
				.withVertex(2, B, 4).addEdgeTo(D)
				.withVertex(3, C, 2).addEdgeTo(D)
				.withVertex(4, D, 5)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(new Date())

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 4

			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				earliestPaths.size() == 1
				latestPaths.size() == 1
			}

//			withTaskVertex(taskTimeLineGraph.vertices[0],
//				[0, A, 3],
//				[0, 3, 3],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[1],
//				[3, B, 7],
//				[3, 4, 7],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[2],
//				[3, C, 5],
//				[5, 2, 7],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[3],
//				[7, D, 12],
//				[7, 5, 12],
//			)
//
//			withTaskTimeLineGraph(taskTimeLineGraph, """
//				Task	dur.	est.	eed.	lst.	led.	CriticalPath?
//				A		3		0		3		0		3		true
//				B		4		3		7		3		7		true
//				C		2		3		5		5		7		false
//				D		5		7		12		7		12		true
//			""")
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink and using a TaskVertex with duration equals zero in critical path'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 3).addEdgesTo(B, C)
				.withVertex(2, B, 4).addEdgeTo(E)
				.withVertex(3, E, 0).addEdgeTo(D) // Task with duration zero in critical path
				.withVertex(4, C, 2).addEdgeTo(D)
				.withVertex(5, D, 5)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(new Date())

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 5

			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				earliestPaths.size() == 1
				latestPaths.size() == 1
			}

//			withTaskVertex(taskTimeLineGraph.vertices[0],
//				[0, A, 3],
//				[0, 3, 3],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[1],
//				[3, B, 7],
//				[3, 4, 7],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[2],
//				[7, E, 7],
//				[7, 0, 7],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[3],
//				[3, C, 5],
//				[5, 2, 7],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[4],
//				[7, D, 12],
//				[7, 5, 12],
//			)
//
//			withTaskTimeLineGraph(taskTimeLineGraph, """
//				Task	dur.	est.	eed.	lst.	led.	CriticalPath?
//				A		3		0		3		0		3		true
//				B		4		3		7		3		7		true
//				E		0		7		7		7		7		true
//				C		2		3		5		5		7		false
//				D		5		7		12		7		12		true
//			""")
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink and using a TaskVertex with duration equals zero NOT in critical path'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 3).addEdgesTo(B, C)
				.withVertex(2, B, 4).addEdgeTo(D)
				.withVertex(3, E, 0).addEdgeTo(D) // Task with duration zero, NOT in critical path
				.withVertex(4, C, 2).addEdgeTo(E)
				.withVertex(5, D, 5)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(new Date())

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 5

			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				earliestPaths.size() == 1
				latestPaths.size() == 1
			}

//			withTaskVertex(taskTimeLineGraph.vertices[0],
//				[0, A, 3],
//				[0, 3, 3],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[1],
//				[3, B, 7],
//				[3, 4, 7],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[2],
//				[5, E, 5],
//				[7, 0, 7],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[3],
//				[3, C, 5],
//				[5, 2, 7],
//			)
//			withTaskVertex(taskTimeLineGraph.vertices[4],
//				[7, D, 12],
//				[7, 5, 12],
//			)
//
//			withTaskTimeLineGraph(taskTimeLineGraph, """
//				Task	dur.	est.	eed.	lst.	led.	CriticalPath?
//				A		3		0		3		0		3		true
//				B		4		3		7		3		7		true
//				E		0		5		7		5		7		false
//				C		2		3		5		5		7		false
//				D		5		7		12		7		12		true
//			""")
//
//		and: 'critical path vertices where returned'
//			withCriticalPath(results, [A, B, D])
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink after randomize vertices order'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, B, 4).addEdgeTo(D)
				.withVertex(2, D, 5)
				.withVertex(3, C, 2).addEdgeTo(D)
				.withVertex(4, A, 3).addEdgesTo(B, C)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(new Date())

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
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 5
				criticalPath.collect { it.taskNumber } == ['A', 'B', 'D', 'G', 'H']
				withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	et.		ef.		ls.		lf.		CriticalPath?
				A		3		0		3		0		3		true
				B		4		3		7		3		7		true
				C		2		3		5		9		11		false
				D		5		7		12		7		12		true
				E		1		5		6		11		12		false
				F		2		5		7		14		16		false
				G		4		12		16		12		16		true
				H		3		16		19		16		19		true """)
			}
	}


	void 'test can calculate critical path method for an cyclic directed graph with one source and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithOneStartAndOneSink()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 1
				cycles[0].collect { it.taskNumber } == ['B', 'D', 'G']
				criticalPath.size() == 5
				criticalPath.collect { it.taskNumber } == ['A', 'B', 'D', 'G', 'H']
				withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	et.		ef.		ls.		lf.		CriticalPath?
				A		3		0		3		0		3		true
				B		4		3		7		3		7		true
				C		2		3		5		9		11		false
				D		5		7		12		7		12		true
				E		1		5		6		11		12		false
				F		2		5		7		14		16		false
				G		4		12		16		12		16		true
				H		3		16		19		16		19		true
			""")
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two starts and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 4
				criticalPath.collect { it.taskNumber } == ['B', 'D', 'G', 'H']
				withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	et.		ef.		ls.		lf.		CriticalPath?
				B		4		0		4		0		4		true
				C		2		0		2		6		8		false
				D		5		4		9		4		9		true
				E		1		2		3		8		9		false
				F		2		2		4		11		13		false
				G		4		9		13		9		13		true
				H		3		13		16		13		16		true 
				""")
			}
	}

	void 'test can calculate critical path method for an cyclic directed graph with two starts and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithTwoStartsAndOneSink()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 1
				cycles[0].collect { it.taskNumber } == ['D', 'G']
				criticalPath.size() == 4
				criticalPath.collect { it.taskNumber } == ['B', 'D', 'G', 'H']
				withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	et.		ef.		ls.		lf.		CriticalPath?
				B		4		0		4		0		4		true
				C		2		0		2		6		8		false
				D		5		4		9		4		9		true
				E		1		2		3		8		9		false
				F		2		2		4		11		13		false
				G		4		9		13		9		13		true
				H		3		13		16		13		16		true 
				""")
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with one start and two sinks'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 4
				criticalPath.collect { it.taskNumber } == ['A', 'B', 'D', 'G']
				withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	et.		ef.		ls.		lf.
				A		3		0		3		0		3
				B		4		3		7		3		7
				C		2		3		5		3		5
				D		5		7		12		7		12
				E		1		5		6		11		12
				F		2		5		7		5		7
				G		4		12		16		12		16
				H		3		7		10		7		10""")
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two start and two sinks'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndTwoSinks()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(new Date())

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				earliestPaths.size() == 1
				latestPaths.size() == 1
			}
	}

	void 'test can calculate critical path method for group a task and dependencies'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createTaskAndDependenciesExample()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(new Date())

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				earliestPaths.size() == 1
				latestPaths.size() == 1
			}
	}
}
