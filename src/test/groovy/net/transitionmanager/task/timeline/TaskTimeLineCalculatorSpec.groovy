package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.AssetCommentStatus
import net.transitionmanager.project.Project
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.timeline.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

import java.text.SimpleDateFormat

class TaskTimeLineCalculatorSpec extends Specification implements TaskTimeLineDataTest {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	@Shared
	Project project

	@Shared
	SimpleDateFormat formatter = new SimpleDateFormat('MM/dd/yyyy hh:mm')

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
			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			Date startDate = pointInTime('06/01/2019 06:00')

			TaskTimeLineGraph emptyGraph = new TaskTimeLineGraph([] as Set)

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(emptyGraph).calculate(startDate)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 0
			}
	}

	void 'test can calculate critical path for a graph with only one TaskVertex'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			Date startDate = pointInTime('06/01/2019 06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 30)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(startDate)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A]
			}
		and:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				earliestStart == pointInTime('06/01/2019 06:00')
				earliestFinish == pointInTime('06/01/2019 06:30')
				latestStart == pointInTime('06/01/2019 06:00')
				latestFinish == pointInTime('06/01/2019 06:30')
			}
	}

	void 'test can calculate critical path for a graph with two TaskVertex'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			Date startDate = pointInTime('06/01/2019 06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 30).addEdgeTo(B)
				.withVertex(2, B, 40)
				.build()

		and:
			TimeLine timeLine = new TimeLine(taskTimeLineGraph)

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = timeLine.calculate(startDate)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B]
			}
		and:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				earliestStart == pointInTime('06/01/2019 06:00')
				earliestFinish == pointInTime('06/01/2019 06:30')
				latestStart == pointInTime('06/01/2019 06:00')
				latestFinish == pointInTime('06/01/2019 06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				taskNumber == 2
				taskComment == B
				duration == 40
				earliestStart == pointInTime('06/01/2019 06:30')
				earliestFinish == pointInTime('06/01/2019 07:10')
				latestStart == pointInTime('06/01/2019 06:30')
				latestFinish == pointInTime('06/01/2019 07:10')
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex, one isolated with a bigger duration'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			Date startDate = pointInTime('06/01/2019 06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 30).addEdgeTo(B)
				.withVertex(2, B, 40)
				.withVertex(3, C, 120)
				.build()

		and:
			TimeLine timeLine = new TimeLine(taskTimeLineGraph)

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = timeLine.calculate(startDate)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 2
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B]
				criticalPathRoutes[1].vertices.collect { it.taskComment } == [C]
			}
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				earliestStart == pointInTime('06/01/2019 06:00')
				earliestFinish == pointInTime('06/01/2019 06:30')
				latestStart == pointInTime('06/01/2019 06:00')
				latestFinish == pointInTime('06/01/2019 06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				taskNumber == 2
				taskComment == B
				duration == 40
				earliestStart == pointInTime('06/01/2019 06:30')
				earliestFinish == pointInTime('06/01/2019 07:10')
				latestStart == pointInTime('06/01/2019 06:30')
				latestFinish == pointInTime('06/01/2019 07:10')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				taskNumber == 3
				taskComment == C
				duration == 120
				earliestStart == pointInTime('06/01/2019 06:00')
				earliestFinish == pointInTime('06/01/2019 08:00')
				latestStart == pointInTime('06/01/2019 06:00')
				latestFinish == pointInTime('06/01/2019 08:00')
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex, one isolated with a smaller duration'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			Date startDate = pointInTime('06/01/2019 06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 30).addEdgeTo(B)
				.withVertex(2, B, 40)
				.withVertex(3, C, 30)
				.build()

		and:
			TimeLine timeLine = new TimeLine(taskTimeLineGraph)

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = timeLine.calculate(startDate)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 2
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B]
				criticalPathRoutes[1].vertices.collect { it.taskComment } == [C]
			}
		and:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				earliestStart == pointInTime('06/01/2019 06:00')
				earliestFinish == pointInTime('06/01/2019 06:30')
				latestStart == pointInTime('06/01/2019 06:00')
				latestFinish == pointInTime('06/01/2019 06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				taskNumber == 2
				taskComment == B
				duration == 40
				earliestStart == pointInTime('06/01/2019 06:30')
				earliestFinish == pointInTime('06/01/2019 07:10')
				latestStart == pointInTime('06/01/2019 06:30')
				latestFinish == pointInTime('06/01/2019 07:10')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				taskNumber == 3
				taskComment == C
				duration == 30
				earliestStart == pointInTime('06/01/2019 06:00')
				earliestFinish == pointInTime('06/01/2019 06:30')
				latestStart == pointInTime('06/01/2019 06:00')
				latestFinish == pointInTime('06/01/2019 06:30')
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one start'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			Date startDate = pointInTime('06/01/2019 06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 30).addEdgesTo(B, C)
				.withVertex(2, B, 40)
				.withVertex(3, C, 20)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(startDate)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B]
			}
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				earliestStart == pointInTime('06/01/2019 06:00')
				earliestFinish == pointInTime('06/01/2019 06:30')
				latestStart == pointInTime('06/01/2019 06:00')
				latestFinish == pointInTime('06/01/2019 06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				taskNumber == 2
				taskComment == B
				duration == 40
				earliestStart == pointInTime('06/01/2019 06:30')
				earliestFinish == pointInTime('06/01/2019 07:10')
				latestStart == pointInTime('06/01/2019 06:30')
				latestFinish == pointInTime('06/01/2019 07:10')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				taskNumber == 3
				taskComment == C
				duration == 20
				earliestStart == pointInTime('06/01/2019 06:30')
				earliestFinish == pointInTime('06/01/2019 06:50')
				latestStart == pointInTime('06/01/2019 06:30')
				latestFinish == pointInTime('06/01/2019 06:50')
			}

	}

	void 'test can calculate critical path for a graph with three TaskVertex with all task in the future'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'

			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			Date startDate = pointInTime('06/01/2019 06:00')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30)
			AssetComment taskB = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 40)
			AssetComment taskC = new AssetComment(project: project, taskNumber: 3, comment: C, duration: 20)
			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')
			TaskDependency edgeAC = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskC, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB, taskC)
				.withEdges(edgeAB, edgeAC)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(startDate)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [taskA.comment, taskB.comment]
			}
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				earliestStart == pointInTime('06/01/2019 06:00')
				earliestFinish == pointInTime('06/01/2019 06:30')
				latestStart == pointInTime('06/01/2019 06:00')
				latestFinish == pointInTime('06/01/2019 06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				taskNumber == 2
				taskComment == B
				duration == 40
				earliestStart == pointInTime('06/01/2019 06:30')
				earliestFinish == pointInTime('06/01/2019 07:10')
				latestStart == pointInTime('06/01/2019 06:30')
				latestFinish == pointInTime('06/01/2019 07:10')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				taskNumber == 3
				taskComment == C
				duration == 20
				earliestStart == pointInTime('06/01/2019 06:30')
				earliestFinish == pointInTime('06/01/2019 06:50')
				latestStart == pointInTime('06/01/2019 06:30')
				latestFinish == pointInTime('06/01/2019 06:50')
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex with one task already started'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'

			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: pointInTime('06/01/2019 06:00'), status: AssetCommentStatus.STARTED)
			AssetComment taskB = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 40)
			AssetComment taskC = new AssetComment(project: project, taskNumber: 3, comment: C, duration: 20)
			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')
			TaskDependency edgeAC = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskC, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB, taskC)
				.withEdges(edgeAB, edgeAC)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(pointInTime('06/01/2019 06:20'))

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [taskA.comment, taskB.comment]
			}
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				earliestStart == pointInTime('06/01/2019 06:20')
				earliestFinish == pointInTime('06/01/2019 06:30')
				latestStart == pointInTime('06/01/2019 06:20')
				latestFinish == pointInTime('06/01/2019 06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				taskNumber == 2
				taskComment == B
				duration == 40
				earliestStart == pointInTime('06/01/2019 06:30')
				earliestFinish == pointInTime('06/01/2019 07:10')
				latestStart == pointInTime('06/01/2019 06:30')
				latestFinish == pointInTime('06/01/2019 07:10')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				taskNumber == 3
				taskComment == C
				duration == 20
				earliestStart == pointInTime('06/01/2019 06:30')
				earliestFinish == pointInTime('06/01/2019 06:50')
				latestStart == pointInTime('06/01/2019 06:30')
				latestFinish == pointInTime('06/01/2019 06:50')
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex with one finished and two tasks already started'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'

			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: pointInTime('06/01/2019 06:00'), status: AssetCommentStatus.TERMINATED)
			AssetComment taskB = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 40,
				actStart: pointInTime('06/01/2019 06:30'), status: AssetCommentStatus.STARTED)
			AssetComment taskC = new AssetComment(project: project, taskNumber: 3, comment: C, duration: 20,
				actStart: pointInTime('06/01/2019 06:30'), status: AssetCommentStatus.STARTED)

			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')
			TaskDependency edgeAC = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskC, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB, taskC)
				.withEdges(edgeAB, edgeAC)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(pointInTime('06/01/2019 06:40'))

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [taskA.comment, taskB.comment]
			}
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				earliestStart == pointInTime('06/01/2019 06:40')
				earliestFinish == pointInTime('06/01/2019 06:40')
				latestStart == pointInTime('06/01/2019 06:40')
				latestFinish == pointInTime('06/01/2019 06:40')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				taskNumber == 2
				taskComment == B
				duration == 40
				earliestStart == pointInTime('06/01/2019 06:40')
				earliestFinish == pointInTime('06/01/2019 07:10')
				latestStart == pointInTime('06/01/2019 06:40')
				latestFinish == pointInTime('06/01/2019 07:10')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				taskNumber == 3
				taskComment == C
				duration == 20
				earliestStart == pointInTime('06/01/2019 06:40')
				earliestFinish == pointInTime('06/01/2019 06:50')
				latestStart == pointInTime('06/01/2019 06:40')
				latestFinish == pointInTime('06/01/2019 06:50')
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex with all task finished'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'

			Date windowTimeStart = pointInTime('06/01/2019 05:00')
			Date windowTimeEnd = pointInTime('06/01/2019 10:00')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: pointInTime('06/01/2019 06:00'), status: AssetCommentStatus.TERMINATED)
			AssetComment taskB = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 40,
				actStart: pointInTime('06/01/2019 06:30'), status: AssetCommentStatus.TERMINATED)
			AssetComment taskC = new AssetComment(project: project, taskNumber: 3, comment: C, duration: 20,
				actStart: pointInTime('06/01/2019 06:30'), status: AssetCommentStatus.TERMINATED)

			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')
			TaskDependency edgeAC = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskC, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB, taskC)
				.withEdges(edgeAB, edgeAC)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate(pointInTime('06/01/2019 07:30'))

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [taskA.comment, taskC.comment]
			}
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				earliestStart == pointInTime('06/01/2019 07:30')
				earliestFinish == pointInTime('06/01/2019 07:30')
				latestStart == pointInTime('06/01/2019 07:30')
				latestFinish == pointInTime('06/01/2019 07:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				taskNumber == 2
				taskComment == B
				duration == 40
				earliestStart == pointInTime('06/01/2019 07:30')
				earliestFinish == pointInTime('06/01/2019 07:30')
				latestStart == pointInTime('06/01/2019 07:30')
				latestFinish == pointInTime('06/01/2019 07:30')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				taskNumber == 3
				taskComment == C
				duration == 20
				earliestStart == pointInTime('06/01/2019 07:30')
				earliestFinish == pointInTime('06/01/2019 07:30')
				latestStart == pointInTime('06/01/2019 07:30')
				latestFinish == pointInTime('06/01/2019 07:30')
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, B, 4).addEdgeTo(D)
				.withVertex(2, C, 2).addEdgeTo(D)
				.withVertex(3, D, 5)
				.build()

		when: 'TimeLine tries to calculate its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [B, D]
			}
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
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
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D]
				withTimeLineTable(timelineSummary.timelineTable, """
					Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath
					A		3		0		3		0		3		0		true
					B		4		3		7		3		7		0		true
					C		2		3		5		5		7		2		false
					D		5		7		12		7		12		0		true
				""")
			}
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
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 5

			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, E, D]
			}

			withTimeLineTable(timelineSummary.timelineTable, """
				Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath
				A		3		0		3		0		3		0		true
				B		4		3		7		3		7		0		true
				E		0		7		7		7		7		0		true
				C		2		3		5		5		7		2		false
				D		5		7		12		7		12		0		true
			""")
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
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then: 'graph contains all the final result values'
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D]
				withTimeLineTable(timelineTable, """
					Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath
					A		3		0		3		0		3		0		true
					B		4		3		7		3		7		0		true
					E		0		5		5		7		7		2		false
					C		2		3		5		5		7		2		false
					D		5		7		12		7		12		0		true
				""")
			}
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
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then: 'graph contains all the final result values'
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D]
				withTimeLineTable(timelineTable, """
					Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath
					B		4		3		7		3		7		0		true
					D		5		7		12		7		12		0		true
					C		2		3		5		5		7		2		false
					A		3		0		3		0		3		0		true
				""")
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with one source and one sink'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D, G, H]
				withTimeLineTable(timelineTable, """
					Task	dur.	et.		ef.		ls.		lf.		slack	CriticalPath
					A		3		0		3		0		3		0		true
					B		4		3		7		3		7		0		true
					C		2		3		5		9		11		6		false
					D		5		7		12		7		12		0		true
					E		1		5		6		11		12		6		false
					F		2		5		7		14		16		9		false
					G		4		12		16		12		16		0		true
					H		3		16		19		16		19		0		true
				""")
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
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D, G, H]
				withTimeLineTable(timelineTable, """
					Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath?
					A		3		0		3		0		3		0		true
					B		4		3		7		3		7		0		true
					C		2		3		5		9		11		6		false
					D		5		7		12		7		12		0		true
					E		1		5		6		11		12		6		false
					F		2		5		7		14		16		9		false
					G		4		12		16		12		16		0		true
					H		3		16		19		16		19		0		true
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
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [B, D, G, H]
				withTimeLineTable(timelineTable, """
					Task	dur.	et.		ef.		ls.		lf.		slack	CriticalPath
					B		4		0		4		0		4		0		true
					C		2		0		2		6		8		6		false
					D		5		4		9		4		9		0		true
					E		1		2		3		8		9		6		false
					F		2		2		4		11		13		9		false
					G		4		9		13		9		13		0		true
					H		3		13		16		13		16 		0		true
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
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [B, D, G, H]
				withTimeLineTable(timelineTable, """
					Task	dur.	et.		ef.		ls.		lf.		slack	CriticalPath
					B		4		0		4		0		4		0		true
					C		2		0		2		6		8		6		false
					D		5		4		9		4		9		0		true
					E		1		2		3		8		9		6		false
					F		2		2		4		11		13		9		false
					G		4		9		13		9		13		0		true
					H		3		13		16		13		16 		0		true
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
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D, G]
				withTimeLineTable(timelineTable, """
					Task	dur.	et.		ef.		ls.		lf.		slack	CriticalPath
					A		3		0		3		0		3		0		true
					B		4		3		7		3		7		0		true
					C		2		3		5		3		5		0		false
					D		5		7		12		7		12		0		true
					E		1		5		6		11		12		6		false
					F		2		5		7		5		7		0		false
					G		4		12		16		12		16		0		true
					H		3		7		10		7		10		0		false
				""")
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
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D, G]
				withTimeLineTable(timelineTable, """
					Task	dur.	et.		ef.		ls.		lf.		slack	Critical Path
					A		3		0		3		0		3		0		true
					B		4		3		7		3		7		0		true
					C		2		3		5		3		5		0		false
					D		5		7		12		7		12		0		true
					E		1		5		6		11		12		6		false
					F		2		5		7		5		7		0		false
					G		4		12		16		12		16		0		true
					H		3		7		10		7		10		0		false
				""")
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
				criticalPathRoutes.size() == 2
				withTimeLineTable(timelineTable, """
					Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath?
					B		4		0		4		0		4		0		true
					C		2		0		2		0		2		0		true
					D		5		4		9		4		9		0		true
					E		1		2		3		8		9		6		false
					F		2		2		4		2		4		0		true
					G		4		9		13		9		13		0		true
					H		3		4		7		4		7		0		true
				""")
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with multiples critical path with same size'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithMultiplesCriticalPaths()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 2
				withTimeLineTable(timelineTable, """
					Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath?
					B		2		0		2		0		2		0		true
					C		2		0		2		0		2		0		true
					D		1		2		3		2		3		0		true
					E		1		2		3		2		3		0		false
					F		2		2		4		2		4		0		true
					G		4		3		7		3		7		0		true
					H		3		4		7		4		7		0		true
				""")
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with three sub-graphs'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithThreeSubGraphs()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 3
				withTimeLineTable(timelineTable, """
					Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath?
					B		4		0		4		0		4		0		true
					C		2		0		2		0		2		0		true
					D		5		4		9		4		9		0		true
					E		1		2		3		2		3		0		false
					F		2		2		4		2		4		0		true
					G		4		9		13		9		13		0		true
					H		3		4		7		4		7		0		true
					I		8		0		8		0		8		0		true
				""")
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two sub-graphs'() {

		given:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoSubGraphs()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph).calculate()

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 2
				withTimeLineTable(timelineTable, """
					Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath?
					B		4		0		4		0		4		0		true
					C		2		0		2		0		2		0		true
					D		5		4		9		4		9		0		true
					E		1		2		3		2		3		0		false
					F		2		2		4		2		4		0		true
					G		4		9		13		9		13		0		true
					H		3		4		7		4		7		0		true
				""")
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
				criticalPathRoutes.size() == 3
				criticalPathRoutes[0].vertices.collect { it.taskComment } == ['6', '7', '4', '11', '13', '16']
			}
	}

	private Date pointInTime(String dateTime) {
		return formatter.parse(dateTime)
	}
}
