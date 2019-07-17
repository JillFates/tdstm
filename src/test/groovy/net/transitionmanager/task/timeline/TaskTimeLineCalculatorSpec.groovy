package net.transitionmanager.task.timeline

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.project.Project
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.timeline.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineCalculatorSpec extends Specification implements TaskTimeLineDataTest {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	@Shared
	Project project

	/**
	 * Common TaskVertex Ids used in several test cases.
	 */
	static String A = 'A', B = 'B', C = 'C', D = 'D', E = 'E', F = 'F', G = 'G', H = 'H'

	void setup() {
		project = Mock(Project)
		project.getId() >> 125612l
	}

	void 'test can calculate critical path for an empty list of TaskVertex'() {

		given: 'a TaskTimeLineGraph with an empty list of TaskVertex'
			TaskTimeLineGraph emptyGraph = new TaskTimeLineGraph([] as Set)

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(emptyGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 0
			}
	}

	void 'test can calculate critical path for a graph with only one TaskVertex'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, A, 3)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				criticalPath.collect { it.taskComment } == [A]
			}
	}

	void 'test can calculate critical path for a graph with two TaskVertex'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, A, 3).addEdgeTo(B)
				.withVertex(2, B, 4)
				.build()

		and:
			TimeLine timeLine = new TimeLine(taskTimeLineGraph)

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = timeLine.calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 2
				criticalPath.collect { it.taskComment } == [A, B]
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex, one isolated with a bigger duration'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, A, 3).addEdgeTo(B)
				.withVertex(2, B, 4)
				.withVertex(3, C, 15)
				.build()

		and:
			TimeLine timeLine = new TimeLine(taskTimeLineGraph)

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = timeLine.calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 1
				criticalPath.collect { it.taskComment } == [C]
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex, one isolated with a smaller duration'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, A, 3).addEdgeTo(B)
				.withVertex(2, B, 4)
				.withVertex(3, C, 3)
				.build()

		and:
			TimeLine timeLine = new TimeLine(taskTimeLineGraph)

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = timeLine.calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 2
				criticalPath.collect { it.taskComment } == [A, B]
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one start'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date pointInTime = TimeUtil.nowGMT()

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, A, 3).addEdgesTo(B, C)
				.withVertex(2, B, 4)
				.withVertex(3, C, 2)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(pointInTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 2
				criticalPath.collect { it.taskComment } == [A, B]
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one start and one task already started'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'

			AssetComment A = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 3)
			AssetComment B = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 4)
			AssetComment C = new AssetComment(project: project, taskNumber: 3, comment: C, duration: 2)
			TaskDependency A_B = new TaskDependency(id: 101, predecessor: A, assetComment: B, type: 'SS')
			TaskDependency A_C = new TaskDependency(id: 101, predecessor: A, assetComment: C, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(A)
				.withVertex(B)
				.withVertex(C)
				.withEdge(A_B)
				.withEdge(A_C)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 2
				criticalPath.collect { it.taskNumber } == [A.taskNumber, B.taskNumber]
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, B, 4).addEdgeTo(D)
				.withVertex(2, C, 2).addEdgeTo(D)
				.withVertex(3, D, 5)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 2
				criticalPath.collect { it.taskComment } == [B, D]
			}
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, A, 3).addEdgesTo(B, D, C)
				.withVertex(2, B, 4).addEdgeTo(D)
				.withVertex(3, C, 2).addEdgeTo(D)
				.withVertex(4, D, 5)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then: 'graph contains all the final result values'
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 3
				criticalPath.collect { it.taskComment } == [A, B, D]
			}

		and:
			withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	es.		ef.		ls.		lf.
				A		3		0		3		0		3
				B		4		3		7		3		7
				C		2		3		5		5		7
				D		5		7		12		7		12
			""")
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink and using a TaskVertex with duration equals zero in critical path'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, A, 3).addEdgesTo(B, C)
				.withVertex(2, B, 4).addEdgeTo(E)
				.withVertex(3, E, 0).addEdgeTo(D) // Task with duration zero in critical path
				.withVertex(4, C, 2).addEdgeTo(D)
				.withVertex(5, D, 5)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 5

			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 4
				criticalPath.collect { it.taskComment } == [A, B, E, D]
			}

			withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	es.		ef.		ls.		lf.
				A		3		0		3		0		3
				B		4		3		7		3		7
				E		0		7		7		7		7
				C		2		3		5		5		7
				D		5		7		12		7		12
			""")
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink and using a TaskVertex with duration equals zero NOT in critical path'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, A, 3).addEdgesTo(B, C)
				.withVertex(2, B, 4).addEdgeTo(D)
				.withVertex(3, E, 0).addEdgeTo(D) // Task with duration zero, NOT in critical path
				.withVertex(4, C, 2).addEdgeTo(E)
				.withVertex(5, D, 5)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then: 'graph contains all the final result values'
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 3
				criticalPath.collect { it.taskComment } == [A, B, D]
			}

		and:
			withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	es.		ef.		ls.		lf.
				A		3		0		3		0		3
				B		4		3		7		3		7
				E		0		5		5		7		7
				C		2		3		5		5		7
				D		5		7		12		7		12
			""")
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink after randomize vertices order'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.SimpleBuilder()
				.withVertex(1, B, 4).addEdgeTo(D)
				.withVertex(2, D, 5)
				.withVertex(3, C, 2).addEdgeTo(D)
				.withVertex(4, A, 3).addEdgesTo(B, C)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then: 'graph contains all the final result values'
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 3
				criticalPath.collect { it.taskComment } == [A, B, D]
			}

		and:
			withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	es.		ef.		ls.		lf.
				B		4		3		7		3		7
				D		5		7		12		7		12
				C		2		3		5		5		7
				A		3		0		3		0		3
			""")
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
				criticalPath.collect { it.taskComment } == [A, B, D, G, H]
				withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	et.		ef.		ls.		lf.
				A		3		0		3		0		3
				B		4		3		7		3		7
				C		2		3		5		9		11
				D		5		7		12		7		12
				E		1		5		6		11		12
				F		2		5		7		14		16
				G		4		12		16		12		16
				H		3		16		19		16		19""")
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
				cycles[0].collect { it.taskComment } == [B, D, G]
				criticalPath.size() == 5
				criticalPath.collect { it.taskComment } == [A, B, D, G, H]
				withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	es.		ef.		ls.		lf.		CriticalPath?
				A		3		0		3		0		3			true
				B		4		3		7		3		7			true
				C		2		3		5		9		11			true
				D		5		7		12		7		12
				E		1		5		6		11		12
				F		2		5		7		14		16
				G		4		12		16		12		16
				H		3		16		19		16		19
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
				criticalPath.collect { it.taskComment } == [B, D, G, H]
				withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	et.		ef.		ls.		lf.
				B		4		0		4		0		4
				C		2		0		2		6		8
				D		5		4		9		4		9
				E		1		2		3		8		9
				F		2		2		4		11		13
				G		4		9		13		9		13
				H		3		13		16		13		16 
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
				cycles[0].collect { it.taskComment } == [D, G]
				criticalPath.size() == 4
				criticalPath.collect { it.taskComment } == [B, D, G, H]
				withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	et.		ef.		ls.		lf.
				B		4		0		4		0		4
				C		2		0		2		6		8
				D		5		4		9		4		9
				E		1		2		3		8		9
				F		2		2		4		11		13
				G		4		9		13		9		13
				H		3		13		16		13		16 
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
				criticalPath.collect { it.taskComment } == [A, B, D, G]
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

	void 'test can calculate critical path method for an cyclic directed graph with one start and two sinks'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithOneStartAndTwoSinks()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 1
				cycles[0].collect { it.taskComment } == [B, D]
				criticalPath.size() == 4
				criticalPath.collect { it.taskComment } == [A, B, D, G]
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
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPath.size() == 3
				criticalPath.collect { it.taskComment } == [B, D, G]
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
				criticalPath.size() == 6
				criticalPath.collect { it.taskComment } == ['6', '7', '4', '11', '13', '16']
			}
	}
}
