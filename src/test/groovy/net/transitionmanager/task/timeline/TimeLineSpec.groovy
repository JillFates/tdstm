package net.transitionmanager.task.timeline


import net.transitionmanager.project.Project
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.timeline.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat

import static com.tdsops.tm.enums.domain.AssetCommentStatus.HOLD
import static com.tdsops.tm.enums.domain.AssetCommentStatus.PLANNED
import static com.tdsops.tm.enums.domain.AssetCommentStatus.STARTED
import static com.tdsops.tm.enums.domain.AssetCommentStatus.TERMINATED

class TimeLineSpec extends Specification implements TaskTimeLineDataTest {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	@Shared
	Project project

	@Shared
	SimpleDateFormat formatter = new SimpleDateFormat('MM/dd/yyyy hh:mm')

	@Shared
	String aDay = '06/22/2018'
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
			Date windowStartDate = hourInDay('06:00')
			Date windowEndTime = hourInDay('06:30')
			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph emptyGraph = new TaskTimeLineGraph([] as Set)

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(emptyGraph)
				.calculate(windowStartDate, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 0
				windowEndTime == windowEndTime
			}
	}

	/*
		   +-----+---+-----|
		   |06:00| A |06:30|
		   +-----+---+-----+
		   |06:00| 30|06:30|
		   +-----+---+-----|
		   +---------------+---------------+
		 06:00           06:30           07:00
 	*/

	@Unroll
	void 'test can calculate critical path for a graph with only one TaskVertex with status=#status, actual start=#actStart, status updated=#statusUpdated and window end time=#endTime'() {

		setup: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartDate = hourInDay('06:00')
			Date windowEndTime = hourInDay(endTime)
			Date currentTime = hourInDay(now)

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay(actStart), status: status, statusUpdated: hourInDay(statusUpdated))

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(taskA)
				.build()

		and: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartDate, windowEndTime, currentTime)

		expect:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				duration == dur
				remaining == rem
				elapsed == elap
				slack == sla
				earliestStartDate == hourInDay(es)
				earliestFinishDate == hourInDay(ef)
				latestStartDate == hourInDay(ls)
				latestFinishDate == hourInDay(lf)
			}

		where:
			endTime | now     | actStart | status     | statusUpdated || dur | rem | elap | sla | es      | ef      | ls      | lf
			'06:30' | '06:00' | '06:00'  | PLANNED    | '05:00'       || 30  | 30  | 0    | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'07:00' | '06:00' | '06:00'  | PLANNED    | '05:00'       || 30  | 30  | 0    | 30  | '06:00' | '06:30' | '06:30' | '07:00'
			'06:30' | '06:10' | '06:00'  | STARTED    | '06:00'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:30' | '06:20' | '06:00'  | STARTED    | '06:00'       || 30  | 10  | 20   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:30' | '06:20' | '06:00'  | HOLD       | '06:10'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'07:00' | '06:20' | '06:00'  | HOLD       | '06:10'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'07:00' | '06:10' | '06:00'  | STARTED    | '06:00'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:30' | '06:30' | '06:00'  | TERMINATED | '06:30'       || 30  | 0   | 30   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:70' | '06:30' | '06:00'  | TERMINATED | '06:30'       || 30  | 0   | 30   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
	}

	void 'test can calculate critical path for a graph with only one TaskVertex and a larger window end time'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartDate = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:00')
			Date currentTime = hourInDay('06:00')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30)

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(taskA)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartDate, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 30
				elapsed == 0
				slack == 30
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:30')
				latestFinishDate == hourInDay('07:00')
			}
	}

	void 'test can calculate critical path for a graph with only one started TaskVertex and a larger window end time'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartDate = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:00')
			Date currentTime = hourInDay('06:10')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay('06:00'), status: STARTED)

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartDate, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 20
				elapsed == 10
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('06:30')
			}
	}

	void 'test can calculate critical path for a graph with only one started TaskVertex and current time 10 minutes later'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartDate = hourInDay('06:00')
			Date windowEndTime = hourInDay('06:30')
			Date currentTime = hourInDay('06:10')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay('06:00'), status: STARTED)

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartDate, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 20
				elapsed == 10
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('06:30')
			}
	}
	//TODO: dcorrea review results with John
	void 'test can calculate critical path for a graph with only one started TaskVertex and larger window end time with current time 15 minutes later'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('06:30')
			Date currentTime = hourInDay('06:15')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay('06:10'), status: STARTED)

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 25
				slack == 0
				earliestStartDate == hourInDay('06:10')
				earliestFinishDate == hourInDay('06:40')
				latestStartDate == hourInDay('06:10')
				latestFinishDate == hourInDay('06:40')
			}
	}

	void 'test can calculate critical path for a graph with two TaskVertex'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30)
			AssetComment taskB = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 40)
			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB)
				.withEdge(edgeAB)
				.build()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 30
				elapsed == 0
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				taskNumber == 2
				taskComment == B
				duration == 40
				remaining == 40
				elapsed == 0
				slack == 0
				earliestStartDate == hourInDay('06:30')
				earliestFinishDate == hourInDay('07:10')
				latestStartDate == hourInDay('06:30')
				latestFinishDate == hourInDay('07:10')
			}
	}

	void 'test can calculate critical path for a graph with two TaskVertex and larger window end time'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:20')
			Date currentTime = hourInDay('06:00')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30)
			AssetComment taskB = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 40)
			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB)
				.withEdge(edgeAB)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 30
				elapsed == 0
				slack == 10
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:10')
				latestFinishDate == hourInDay('06:40')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				criticalPath
				taskNumber == 2
				taskComment == B
				duration == 40
				remaining == 40
				elapsed == 0
				slack == 10
				earliestStartDate == hourInDay('06:30')
				earliestFinishDate == hourInDay('07:10')
				latestStartDate == hourInDay('06:40')
				latestFinishDate == hourInDay('07:20')
			}
	}

	void 'test can calculate critical path for a graph with two TaskVertex, one already started'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')

			Date currentTime = hourInDay('06:10')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay('06:00'), status: STARTED)
			AssetComment taskB = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 40)
			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB)
				.withEdge(edgeAB)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 20
				elapsed == 10
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				criticalPath
				taskNumber == 2
				taskComment == B
				duration == 40
				remaining == 40
				elapsed == 0
				slack == 0
				earliestStartDate == hourInDay('06:30')
				earliestFinishDate == hourInDay('07:10')
				latestStartDate == hourInDay('06:30')
				latestFinishDate == hourInDay('07:10')
			}
	}

	void 'test can calculate critical path for a graph with two TaskVertex, one already started and a larger window end time'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:20')

			Date currentTime = hourInDay('06:10')

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay('06:00'), status: STARTED)
			AssetComment taskB = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 40)
			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB)
				.withEdge(edgeAB)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 20
				elapsed == 10
				slack == 0
				//TODO: dcorrea. Review this result with John.
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:10')
				latestFinishDate == hourInDay('06:40')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				criticalPath
				taskNumber == 2
				taskComment == B
				duration == 40
				remaining == 40
				elapsed == 0
				slack == 10
				earliestStartDate == hourInDay('06:30')
				earliestFinishDate == hourInDay('07:10')
				latestStartDate == hourInDay('06:40')
				latestFinishDate == hourInDay('07:20')
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex, one isolated with a bigger duration finding two routes'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('08:00')

			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 30).addEdgeTo(B)
				.withVertex(2, B, 40)
				.withVertex(3, C, 120)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 2
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B]
				criticalPathRoutes[1].vertices.collect { it.taskComment } == [C]
			}
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 30
				elapsed == 0
				slack == 50
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:50')
				latestFinishDate == hourInDay('07:20')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				criticalPath
				taskNumber == 2
				taskComment == B
				duration == 40
				remaining == 40
				elapsed == 0
				slack == 50
				earliestStartDate == hourInDay('06:30')
				earliestFinishDate == hourInDay('07:10')
				latestStartDate == hourInDay('07:20')
				latestFinishDate == hourInDay('08:00')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				criticalPath
				taskNumber == 3
				taskComment == C
				duration == 120
				remaining == 120
				elapsed == 0
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('08:00')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('08:00')
			}
	}

	void 'test can calculate critical path for a graph with three TaskVertex, one isolated with a smaller duration finding two routes'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')

			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 30).addEdgeTo(B)
				.withVertex(2, B, 40)
				.withVertex(3, C, 30)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 2
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B]
				criticalPathRoutes[1].vertices.collect { it.taskComment } == [C]
			}
		and:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 30
				elapsed == 0
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				criticalPath
				taskNumber == 2
				taskComment == B
				duration == 40
				remaining == 40
				elapsed == 0
				slack == 0
				earliestStartDate == hourInDay('06:30')
				earliestFinishDate == hourInDay('07:10')
				latestStartDate == hourInDay('06:30')
				latestFinishDate == hourInDay('07:10')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				criticalPath
				taskNumber == 3
				taskComment == C
				duration == 30
				remaining == 30
				elapsed == 0
				slack == 40
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:40')
				latestFinishDate == hourInDay('07:10')
			}
	}

	//TODO: dcorrea add graph examples
	void 'test can calculate critical path for a graph with three TaskVertex defining one start finding one route'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 30).addEdgesTo(B, C)
				.withVertex(2, B, 40)
				.withVertex(3, C, 20)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B]
			}
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 30
				elapsed == 0
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('06:30')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				criticalPath
				taskNumber == 2
				taskComment == B
				duration == 40
				remaining == 40
				elapsed == 0
				slack == 0
				earliestStartDate == hourInDay('06:30')
				earliestFinishDate == hourInDay('07:10')
				latestStartDate == hourInDay('06:30')
				latestFinishDate == hourInDay('07:10')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				!criticalPath
				taskNumber == 3
				taskComment == C
				duration == 20
				remaining == 20
				elapsed == 0
				slack == 20
				earliestStartDate == hourInDay('06:30')
				earliestFinishDate == hourInDay('06:50')
				latestStartDate == hourInDay('06:50')
				latestFinishDate == hourInDay('07:10')
			}

	}

	/*
                                             +-----+---+-----+
                                             |06:30| B |07:10|
                               +------------>+-----+---+-----+
                               |             |06:30| 40|07:10|
                               |             +-----+---+-----+
                               |
               +-----+---+-----|
               |06:00| A |06:30|
               +-----+---+-----+
               |06:00| 30|06:30|
               +-----+---+-----|
                               |
                               |       +-----+---+-----+
                               |       |06:30| A |06:50|
                               +------>+-----+---+-----+
                                       |06:50| 20|07:10|
                                       +-----+---+-----+
               +-----------------------+---------------+--------+
             06:00                   06:30           06:50    07:10
	 */

	@Unroll
	void 'test can calculate critical path for a graph with three TaskVertex A status=#statusA, B status=#statusB and C status=#statusC, window end time=#endTime and current time=#current'() {

		setup: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay(endTime)
			Date currentTime = hourInDay(current)

			AssetComment taskA = new AssetComment(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay(startA), status: statusA)
			AssetComment taskB = new AssetComment(project: project, taskNumber: 2, comment: B, duration: 40,
				actStart: hourInDay(startB), status: statusB)
			AssetComment taskC = new AssetComment(project: project, taskNumber: 3, comment: C, duration: 20,
				actStart: hourInDay(startC), status: statusC)
			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')
			TaskDependency edgeAC = new TaskDependency(id: 102, predecessor: taskA, assetComment: taskC, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB, taskC)
				.withEdges(edgeAB, edgeAC)
				.build()

		and: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		expect:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [taskA.comment, taskB.comment]
			}
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath == cpA
				slack == slackA
				earliestStartDate == hourInDay(esA)
				earliestFinishDate == hourInDay(efA)
				latestStartDate == hourInDay(lsA)
				latestFinishDate == hourInDay(lfA)
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				criticalPath == cpB
				slack == slackB
				earliestStartDate == hourInDay(esB)
				earliestFinishDate == hourInDay(efB)
				latestStartDate == hourInDay(lsB)
				latestFinishDate == hourInDay(lfB)
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				criticalPath == cpC
				slack == slackC
				earliestStartDate == hourInDay(esC)
				earliestFinishDate == hourInDay(efC)
				latestStartDate == hourInDay(lsC)
				latestFinishDate == hourInDay(lfC)
			}

		where:
			endTime | current | startA  | statusA    | startB  | statusB    | startC  | statusC    || cpA  | slackA | esA     | efA     | lsA     | lfA     | cpB  | slackB | esB     | efB     | lsB     | lfB     | cpC   | slackC | esC     | efC     | lsC     | lfC
			'07:10' | '06:00' | null    | PLANNED    | null    | PLANNED    | null    | PLANNED    || true | 0      | '06:00' | '06:30' | '06:00' | '06:30' | true | 0      | '06:30' | '07:10' | '06:30' | '07:10' | false | 20     | '06:30' | '06:50' | '06:50' | '07:10'
			'07:20' | '06:00' | null    | PLANNED    | null    | PLANNED    | null    | PLANNED    || true | 10     | '06:00' | '06:30' | '06:10' | '06:40' | true | 10     | '06:30' | '07:10' | '06:40' | '07:20' | false | 30     | '06:30' | '06:50' | '07:00' | '07:20'
			'07:10' | '06:00' | '06:00' | STARTED    | null    | PLANNED    | null    | PLANNED    || true | 0      | '06:00' | '06:30' | '06:00' | '06:30' | true | 0      | '06:30' | '07:10' | '06:30' | '07:10' | false | 20     | '06:30' | '06:50' | '06:50' | '07:10'
			'07:10' | '06:10' | '06:00' | STARTED    | null    | PLANNED    | null    | PLANNED    || true | 0      | '06:00' | '06:30' | '06:00' | '06:30' | true | 0      | '06:30' | '07:10' | '06:30' | '07:10' | false | 20     | '06:30' | '06:50' | '06:50' | '07:10'
			'07:20' | '06:00' | '06:00' | STARTED    | null    | PLANNED    | null    | PLANNED    || true | 0      | '06:00' | '06:30' | '06:10' | '06:40' | true | 10     | '06:30' | '07:10' | '06:40' | '07:20' | false | 30     | '06:30' | '06:50' | '07:00' | '07:20'
			'07:10' | '06:40' | '06:00' | TERMINATED | '06:30' | STARTED    | '06:30' | STARTED    || true | 0      | '06:00' | '06:30' | '06:00' | '06:30' | true | 0      | '06:30' | '07:10' | '06:30' | '07:10' | false | 0      | '06:30' | '06:50' | '06:30' | '06:50'
			'07:10' | '07:10' | '06:00' | TERMINATED | '06:30' | TERMINATED | '06:30' | TERMINATED || true | 0      | '06:00' | '06:30' | '06:00' | '06:30' | true | 0      | '06:30' | '07:10' | '06:30' | '07:10' | false | 0      | '06:30' | '06:50' | '06:30' | '06:50'
	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, B, 4).addEdgeTo(D)
				.withVertex(2, C, 2).addEdgeTo(D)
				.withVertex(3, D, 5)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [B, D]
			}
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 3).addEdgesTo(B, D, C)
				.withVertex(2, B, 4).addEdgeTo(D)
				.withVertex(3, C, 2).addEdgeTo(D)
				.withVertex(4, D, 5)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then: 'graph contains all the final result values'
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D]
			}
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink and using a TaskVertex with duration equals zero in critical path'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 3).addEdgesTo(B, C)
				.withVertex(2, B, 4).addEdgeTo(E)
				.withVertex(3, E, 0).addEdgeTo(D) // Task with duration zero in critical path
				.withVertex(4, C, 2).addEdgeTo(D)
				.withVertex(5, D, 5)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then: 'graph contains all the final result values'
			taskTimeLineGraph.V() == 5

			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, E, D]
			}
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink and using a TaskVertex with duration equals zero NOT in critical path'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, A, 3).addEdgesTo(B, C)
				.withVertex(2, B, 4).addEdgeTo(D)
				.withVertex(3, E, 0).addEdgeTo(D) // Task with duration zero, NOT in critical path
				.withVertex(4, C, 2).addEdgeTo(E)
				.withVertex(5, D, 5)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then: 'graph contains all the final result values'
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D]
			}
	}

	void 'test can calculate critical path for a graph with four TaskVertex defining one start and one sink after randomize vertices order'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1, B, 4).addEdgeTo(D)
				.withVertex(2, D, 5)
				.withVertex(3, C, 2).addEdgeTo(D)
				.withVertex(4, A, 3).addEdgesTo(B, C)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then: 'graph contains all the final result values'
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D]
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with one source and one sink'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D, G, H]
			}
	}

	void 'test can calculate critical path method for an cyclic directed graph with one source and one sink'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithOneStartAndOneSink()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 1
				cycles[0].collect { it.taskComment } == [B, D, G]
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D, G, H]
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two starts and one sink'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [B, D, G, H]
			}
	}

	void 'test can calculate critical path method for an cyclic directed graph with two starts and one sink'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithTwoStartsAndOneSink()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 1
				cycles[0].collect { it.taskComment } == [D, G]
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [B, D, G, H]
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with one start and two sinks'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D, G]
			}
	}

	void 'test can calculate critical path method for an cyclic directed graph with one start and two sinks'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createCyclicDirectedGraphWithOneStartAndTwoSinks()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 1
				cycles[0].collect { it.taskComment } == [B, D]
				criticalPathRoutes.size() == 1
				criticalPathRoutes[0].vertices.collect { it.taskComment } == [A, B, D, G]
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two start and two sinks'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndTwoSinks()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 2
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with multiples critical path with same size'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithMultiplesCriticalPaths()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 2
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with three sub-graphs'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithThreeSubGraphs()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 3
			}
	}

	void 'test can calculate critical path method for an acyclic directed graph with two sub-graphs'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoSubGraphs()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 2
			}
	}

	void 'test can calculate critical path method for group a task and dependencies'() {

		given:
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

		and:
			TaskTimeLineGraph taskTimeLineGraph = taskTimeLineGraphTestHelper.createTaskAndDependenciesExample()

		when:
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(timelineSummary, TimelineSummary) {
				cycles.size() == 0
				criticalPathRoutes.size() == 3
				criticalPathRoutes[0].vertices.collect { it.taskComment } == ['6', '7', '4', '11', '13', '16']
			}
	}

	private Date hourInDay(String dateTime) {
		return dateTime ? formatter.parse(aDay + ' ' + dateTime) : null
	}
}
