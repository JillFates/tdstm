package net.transitionmanager.task.timeline

import net.transitionmanager.project.Project
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.timeline.helper.TaskTimeLineGraphTestHelper
import net.transitionmanager.task.timeline.test.TaskTimeLineDataTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat

import static com.tdsops.tm.enums.domain.AssetCommentStatus.COMPLETED
import static com.tdsops.tm.enums.domain.AssetCommentStatus.HOLD
import static com.tdsops.tm.enums.domain.AssetCommentStatus.PLANNED
import static com.tdsops.tm.enums.domain.AssetCommentStatus.READY
import static com.tdsops.tm.enums.domain.AssetCommentStatus.STARTED
import static com.tdsops.tm.enums.domain.AssetCommentStatus.TERMINATED

class TimeLineSpec extends Specification implements TaskTimeLineDataTest {

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
		+---------------+---------------+---------------+
	  05:00		      06:00           06:30           07:00
 	*/

	@Unroll
	void 'test can calculate critical path for a graph with only one TaskVertex with event in range [#startTime, #endTime], status=#status, actual start=#actStart and status updated=#statusUpdated'() {

		setup: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartDate = hourInDay(startTime)
			Date windowEndTime = hourInDay(endTime)
			Date currentTime = hourInDay(now)

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay(actStart), status: status, statusUpdated: hourInDay(statusUpdated))

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(taskA)
				.build()

		and: 'TimeLine calculates its critical path'
			new TimeLine(taskTimeLineGraph)
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
			startTime | endTime | now     | actStart | status     | statusUpdated || dur | rem | elap | sla | es      | ef      | ls      | lf
			'06:00'   | null    | '06:00' | '05:00'  | STARTED    | '05:00'       || 30  | 30  | 0    | 0   | '05:00' | '05:30' | '05:00' | '05:30'
			'07:00'   | null    | '06:00' | '05:00'  | STARTED    | '05:00'       || 30  | 30  | 0    | 0   | '05:00' | '05:30' | '05:00' | '05:30'
			'06:00'   | null    | '07:00' | '05:00'  | STARTED    | '05:00'       || 30  | 30  | 0    | 0   | '05:00' | '05:30' | '05:00' | '05:30'
			'06:00'   | null    | '06:00' | '05:00'  | HOLD       | '05:10'       || 30  | 20  | 10   | 0   | '05:00' | '05:30' | '05:00' | '05:30'
			'07:00'   | null    | '06:00' | '05:00'  | HOLD       | '05:10'       || 30  | 20  | 10   | 0   | '05:00' | '05:30' | '05:00' | '05:30'
			'06:00'   | null    | '07:00' | '05:00'  | HOLD       | '05:10'       || 30  | 20  | 10   | 0   | '05:00' | '05:30' | '05:00' | '05:30'
			'06:00'   | '06:30' | '06:00' | '06:00'  | PLANNED    | '05:00'       || 30  | 30  | 0    | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | null    | '06:00' | '06:00'  | PLANNED    | '05:00'       || 30  | 30  | 0    | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | '07:00' | '06:00' | '06:00'  | PLANNED    | '05:00'       || 30  | 30  | 0    | 30  | '06:00' | '06:30' | '06:30' | '07:00'
			'06:00'   | '06:30' | '06:10' | '06:00'  | STARTED    | '06:00'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | null    | '06:10' | '06:00'  | STARTED    | '06:00'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | '06:30' | '06:20' | '06:00'  | STARTED    | '06:00'       || 30  | 10  | 20   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | '06:30' | '06:20' | '06:00'  | HOLD       | '06:10'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | '07:00' | '06:20' | '06:00'  | HOLD       | '06:10'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | null    | '06:20' | '06:00'  | HOLD       | '06:10'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | '07:00' | '06:10' | '06:00'  | STARTED    | '06:00'       || 30  | 20  | 10   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | '06:30' | '06:30' | '06:00'  | TERMINATED | '06:30'       || 30  | 0   | 30   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | '06:70' | '06:30' | '06:00'  | TERMINATED | '06:30'       || 30  | 0   | 30   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'06:00'   | null    | '06:30' | '06:00'  | TERMINATED | '06:30'       || 30  | 0   | 30   | 0   | '06:00' | '06:30' | '06:00' | '06:30'
			'10:00'   | null    | '06:00' | null     | PLANNED    | null          || 30  | 30  | 0    | 0   | '10:00' | '10:30' | '10:00' | '10:30'
			'10:00'   | '11:00' | '06:00' | null     | PLANNED    | null          || 30  | 30  | 0    | 30  | '10:00' | '10:30' | '10:30' | '11:00'
	}

	void 'test can calculate critical path for a graph with only one TaskVertex and a larger window end time'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartDate = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:00')
			Date currentTime = hourInDay('06:00')

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30)

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(taskA)
				.build()

		when: 'TimeLine calculates its critical path'
			new TimeLine(taskTimeLineGraph).calculate(windowStartDate, windowEndTime, currentTime)

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

	void 'test can calculate critical path for a graph with only one TaskVertex and a larger window end time in the future'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartDate = hourInDay('10:00')
			Date windowEndTime = hourInDay('11:00')
			Date currentTime = hourInDay('06:00')

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30)

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(taskA)
				.build()

		when: 'TimeLine calculates its critical path'
			new TimeLine(taskTimeLineGraph).calculate(windowStartDate, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath
				taskNumber == 1
				taskComment == A
				duration == 30
				remaining == 30
				elapsed == 0
				slack == 30
				earliestStartDate == hourInDay('10:00')
				earliestFinishDate == hourInDay('10:30')
				latestStartDate == hourInDay('10:30')
				latestFinishDate == hourInDay('11:00')
			}
	}

	void 'test can calculate critical path for a graph with only one started TaskVertex and a larger window end time'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartDate = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:00')
			Date currentTime = hourInDay('06:10')

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay('06:00'), status: STARTED)

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA)
				.build()

		when: 'TimeLine calculates its critical path'
			new TimeLine(taskTimeLineGraph).calculate(windowStartDate, windowEndTime, currentTime)

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

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30,
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

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30,
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

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30)
			Task taskB = new Task(project: project, taskNumber: 2, comment: B, duration: 40)
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

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30)
			Task taskB = new Task(project: project, taskNumber: 2, comment: B, duration: 40)
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

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay('06:00'), status: STARTED)
			Task taskB = new Task(project: project, taskNumber: 2, comment: B, duration: 40)
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

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay('06:00'), status: STARTED)
			Task taskB = new Task(project: project, taskNumber: 2, comment: B, duration: 40)
			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertices(taskA, taskB)
				.withEdge(edgeAB)
				.build()

		when: 'TimeLine calculates its critical path'
			new TimeLine(taskTimeLineGraph)
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
				.withVertex(1l, 1, A, 30).addEdgeTo(B)
				.withVertex(2l, 2, B, 40)
				.withVertex(3l, 3, C, 120)
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
				.withVertex(1l, 1, A, 30).addEdgeTo(B)
				.withVertex(2l, 2, B, 40)
				.withVertex(3l, 3, C, 30)
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

	void 'test can calculate critical path for a graph with three TaskVertex defining one start finding one route'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1l, 1, A, 30).addEdgesTo(B, C)
				.withVertex(2l, 2, B, 40)
				.withVertex(4l, 3, C, 20)
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
                                       +------------+-------+-----------+
                                       |    06:30   |   B   |   07:30   |
                               +------>+------------+-------+-----------+
                               |       |    06:30   |   60  |   07:30   |
                               |       +------------+-------+-----------+
                               |
               +-----+---+-----|
               |06:00| A |06:30|
               +-----+---+-----+
               |06:00| 30|06:30|
               +-----+---+-----|
                               |
                               |       +-----+---+-----+
                               |       |06:30| A |07:00|
                               +------>+-----+---+-----+
                                       |07:00| 30|07:30|
                                       +-----+---+-----+
               +-----------------------+---------------+--------------+
             06:00                   06:30           07:00           07:30
	 */

	@Unroll
	void 'test can calculate critical path for a graph with three TaskVertex in event range [#startTime, #endTime], A [status=#statusA, start=#startA ], B [status=#statusB, start=#startB ], C [status=#statusC, start=#startC ] and current time=#current'() {

		setup: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay(startTime)
			Date windowEndTime = hourInDay(endTime)
			Date currentTime = hourInDay(current)

			Task taskA = new Task(project: project, taskNumber: 1, comment: A, duration: 30,
				actStart: hourInDay(startA), status: statusA)
			Task taskB = new Task(project: project, taskNumber: 2, comment: B, duration: 60,
				actStart: hourInDay(startB), status: statusB)
			Task taskC = new Task(project: project, taskNumber: 3, comment: C, duration: 30,
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
			startTime | endTime | current  | startA  | statusA    | startB  | statusB | startC | statusC || cpA  | slackA | esA     | efA     | lsA         | lfA     | cpB  | slackB | esB     | efB     | lsB     | lfB     | cpC   | slackC | esC     | efC     | lsC     | lfC
			/* Initial example. Event inside [06:00, 07:30]. current: 06:00 */
			'06:00'   | '07:30' | '06:00'  | null    | PLANNED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			//'06:00'   | '07:30' | '06:30' | null    | PLANNED   | null   | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00' | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			// First task status and current time variations
			'06:00'   | '07:30' | '06:15'  | '06:00' | STARTED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			'06:00'   | '07:30' | '06:25'  | '06:00' | STARTED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			'06:00'   | '07:30' | '06:30'  | '06:00' | STARTED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			'06:00'   | '07:30' | '06:30'  | '06:00' | TERMINATED | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			'06:00'   | '07:30' | '06:30'  | '06:00' | HOLD       | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			'06:00'   | '07:30' | '06:30'  | '06:00' | COMPLETED  | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			// Example with slacks. Event inside [06:00, 07:30] with task status and current time variations
			'06:00'   | '08:00' | '06:00'  | null    | PLANNED    | null    | PLANNED | null   | PLANNED || true | 60     | '06:00' | '06:30' | '07:00'     | '07:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			'06:00'   | '08:00' | '06:00'  | '06:00' | PLANNED    | null    | PLANNED | null   | PLANNED || true | 60     | '06:00' | '06:30' | '07:00'     | '07:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			'06:00'   | '08:00' | '06:00'  | '06:00' | READY      | null    | PLANNED | null   | PLANNED || true | 60     | '06:00' | '06:30' | '07:00'     | '07:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			'06:00'   | '08:00' | '06:15'  | '06:00' | STARTED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:	00' | '06:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			'06:00'   | '08:00' | '06:15'  | '06:00' | HOLD       | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			'06:00'   | '08:00' | '06:30'  | '06:00' | STARTED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			'06:00'   | '08:00' | '06: 30' | '06:00' | HOLD       | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			'06:00'   | '08:00' | '06:30'  | '06:00' | COMPLETED  | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			'06:00'   | '08:00' | '06:30'  | '06:00' | TERMINATED | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			//When Event does not have start time, it uses current time
			null      | null    | '06:00'  | null    | PLANNED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			null      | '07:30' | '06:00'  | null    | PLANNED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			null      | '08:00' | '06:00'  | null    | PLANNED    | null    | PLANNED | null   | PLANNED || true | 60     | '06:00' | '06:30' | '07:00'     | '07:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			null      | '07:30' | '06:10'  | '06:00' | STARTED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			null      | '07:30' | '06:10'  | '06:00' | HOLD       | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 0      | '06:30' | '07:30' | '06:30' | '07:30' | false | 30     | '06:30' | '07:00' | '07:00' | '07:30'
			null      | '08:00' | '06:10'  | '06:00' | STARTED    | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			null      | '08:00' | '06:30'  | '06:00' | COMPLETED  | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			null      | '08:00' | '06:30'  | '06:00' | TERMINATED | null    | PLANNED | null   | PLANNED || true | 0      | '06:00' | '06:30' | '06:00'     | '06:30' | true | 30     | '06:30' | '07:30' | '07:00' | '08:00' | false | 60     | '06:30' | '07:00' | '07:30' | '08:00'
			/// Examples with Task A started before event start Time
			'06:00'   | '07:30' | '05:40'  | '05:00' | COMPLETED  | null    | PLANNED | null   | PLANNED || true | 0      | '05:00' | '05:30' | '05:00'     | '05:30' | true | 30     | '06:00' | '07:00' | '06:30' | '07:30' | false | 60     | '06:00' | '06:30' | '07:00' | '07:30'
			'06:00'   | '07:30' | '05:45'  | '05:00' | COMPLETED  | '05:45' | STARTED | null   | PLANNED || true | 0      | '05:00' | '05:30' | '05:00'     | '05:30' | true | 0      | '05:45' | '06:45' | '05:45' | '06:45' | false | 60     | '06:00' | '06:30' | '07:00' | '07:30'

	}

	void 'test can calculate critical path for a graph with three TaskVertex defining one sink'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:10')
			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1l, 1, B, 4).addEdgeTo(D)
				.withVertex(2l, 2, C, 2).addEdgeTo(D)
				.withVertex(3l, 3, D, 5)
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
				.withVertex(1l, 1, A, 3).addEdgesTo(B, D, C)
				.withVertex(2l, 2, B, 4).addEdgeTo(D)
				.withVertex(3l, 3, C, 2).addEdgeTo(D)
				.withVertex(4l, 4, D, 5)
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
				.withVertex(1l, 1, A, 3).addEdgesTo(B, C)
				.withVertex(2l, 2, B, 4).addEdgeTo(E)
				.withVertex(3l, 3, E, 0).addEdgeTo(D) // Task with duration zero in critical path
				.withVertex(4l, 4, C, 2).addEdgeTo(D)
				.withVertex(5l, 5, D, 5)
				.build()

		when: 'TimeLine calculates its critical path'
			TimelineSummary timelineSummary = new TimeLine(taskTimeLineGraph)
				.calculate(windowStartTime, windowEndTime, currentTime)

		then: 'graph contains all the final result values'
			taskTimeLineGraph.verticesSize() == 5

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
				.withVertex(1l, 1, A, 3).addEdgesTo(B, C)
				.withVertex(2l, 2, B, 4).addEdgeTo(D)
				.withVertex(3l, 3, E, 0).addEdgeTo(D) // Task with duration zero, NOT in critical path
				.withVertex(4l, 4, C, 2).addEdgeTo(E)
				.withVertex(5l, 5, D, 5)
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
				.withVertex(1l, 1, B, 4).addEdgeTo(D)
				.withVertex(2l, 2, D, 5)
				.withVertex(3l, 3, C, 2).addEdgeTo(D)
				.withVertex(4l, 4, A, 3).addEdgesTo(B, C)
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

	void 'test can calculate critical path for a graph with three TaskVertex in circular cycle'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('07:30')
			Date currentTime = hourInDay('06:00')

			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(1l, 1, A, 30).addEdgeTo(B)
				.withVertex(2l, 2, B, 60).addEdgeTo(C)
				.withVertex(3l, 3, C, 30).addEdgeTo(A) // Creates a circular cycle
				.build()

		when: 'TimeLine calculates its critical path'
			new TimeLine(taskTimeLineGraph).calculate(windowStartTime, windowEndTime, currentTime)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				!criticalPath
				slack == 0
				earliestStartDate == null
				earliestFinishDate == null
				latestStartDate == null
				latestFinishDate == null
			}

			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				!criticalPath
				slack == 0
				earliestStartDate == null
				earliestFinishDate == null
				latestStartDate == null
				latestFinishDate == null
			}

			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				!criticalPath
				slack == 0
				earliestStartDate == null
				earliestFinishDate == null
				latestStartDate == null
				latestFinishDate == null
			}
	}

	private Date hourInDay(String dateTime) {
		return dateTime ? formatter.parse(aDay + ' ' + dateTime) : null
	}
}
