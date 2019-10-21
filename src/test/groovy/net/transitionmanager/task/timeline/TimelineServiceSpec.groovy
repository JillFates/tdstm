package net.transitionmanager.task.timeline

import grails.testing.services.ServiceUnitTest
import net.transitionmanager.project.Project
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.timeline.test.TaskTimeLineDataTest
import spock.lang.Shared
import spock.lang.Specification

import static com.tdsops.tm.enums.domain.AssetCommentStatus.PLANNED

class TimelineServiceSpec extends Specification implements ServiceUnitTest<TimelineService>, TaskTimeLineDataTest {

	@Shared
	Project project

	void setup() {
		project = Mock(Project)
		project.getId() >> 125612l
	}

	void 'test can check and update zero durations in a task timeline graph'() {

		setup: 'a TaskTimeLineGraph with a list of TaskVertex'
			Date windowStartTime = hourInDay('06:00')
			Date windowEndTime = hourInDay('06:30')
			Date currentTime = hourInDay('06:00')

			Task taskA = new Task(project: project, taskNumber: 1, comment: 'Task A', duration: 0, actStart: null, status: PLANNED)
			Task taskB = new Task(project: project, taskNumber: 2, comment: 'Task B', duration: 30, actStart: null, status: PLANNED)
			Task taskC = new Task(project: project, taskNumber: 3, comment: 'Task C', duration: 30, actStart: null, status: PLANNED)
			List<Task> tasks = [taskA, taskB, taskC]

			TaskDependency edgeAB = new TaskDependency(id: 101, predecessor: taskA, assetComment: taskB, type: 'SS')
			TaskDependency edgeAC = new TaskDependency(id: 102, predecessor: taskA, assetComment: taskC, type: 'SS')
			List<TaskDependency> dependencies = [edgeAB, edgeAC]

			TaskTimeLineGraph graph = new TaskTimeLineGraph.Builder()
				.withVertices(tasks)
				.withEdges(dependencies)
				.build()

			TimelineSummary summary = new TimeLine(graph)
				.calculate(windowStartTime, windowEndTime, currentTime)

			CPAResults cpaResults = new CPAResults(graph, summary, tasks, dependencies)

		when: 'TimeLine calculates its critical path'
			cpaResults = service.checkAndUpdateZeroDurations(cpaResults)

		then:
			with(taskTimeLineGraph.getVertex(1), TaskVertex) {
				criticalPath == true
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:00')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('06:00')
			}
			with(taskTimeLineGraph.getVertex(2), TaskVertex) {
				criticalPath == true
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('06:30')
			}
			with(taskTimeLineGraph.getVertex(3), TaskVertex) {
				criticalPath == true
				slack == 0
				earliestStartDate == hourInDay('06:00')
				earliestFinishDate == hourInDay('06:30')
				latestStartDate == hourInDay('06:00')
				latestFinishDate == hourInDay('06:30')
			}

	}
}
