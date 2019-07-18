package net.transitionmanager.task.timeline


import net.transitionmanager.project.Project
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskDependency
import net.transitionmanager.task.timeline.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineGraphSpec extends Specification {

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	@Shared
	Project project

	void setup() {
		project = Mock(Project)
		project.getId() >> 125612l
	}

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
			graph.starts[0].taskComment == 'A'
	}

	void 'can calculate sink for a TaskTimeLineGraph with one start and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'sink can be calculated'
			graph.sinks.size() == 1
			graph.sinks[0].taskComment == 'H'
	}

	void 'can retrieve a for a TaskTimeLineGraph with one start and one sink by taskId'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'a vertex can be retrieved'
			graph.starts.size() == 1
			graph.starts[0].taskComment == 'A'

		and:
			graph.sinks.size() == 1
			graph.sinks[0].taskComment == 'H'
	}

	void 'can retrieve a for a TaskTimeLineGraph with one start and one sink by an instance of TaskTimeLineVertex'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndOneSink()

		then: 'a vertex can be retrieved'
			graph.getVertex(graph.vertices.first()).taskComment == 'A'
	}

	void 'can calculate start and sink for a TaskTimeLineGraph with two starts and one sink'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithTwoStartsAndOneSink()

		then: 'start can be calculated'
			graph.starts.size() == 2
			graph.starts[0].taskComment == 'B'
			graph.starts[1].taskComment == 'C'

		then: 'sink can be calculated'
			graph.sinks.size() == 1
			graph.sinks[0].taskComment == 'H'
	}

	void 'can calculate start and sink for a TaskTimeLineGraph with one start and two sinks'() {

		when: 'a new instance of TaskTimeLineGraph is created'
			TaskTimeLineGraph graph = taskTimeLineGraphTestHelper.createAcyclicDirectedGraphWithOneStartAndTwoSinks()

		then: 'start can be calculated'
			graph.starts.size() == 1
			graph.starts[0].taskComment == 'A'

		and: 'sink can be calculated'
			graph.sinks.size() == 2
			graph.sinks[0].taskComment == 'G'
			graph.sinks[1].taskComment == 'H'
	}

	void 'test can create a TimeLineNodeGraph using AssetComment and TaskDependency'() {

		given:
			List<AssetComment> tasks = [
				new AssetComment(project: project, taskNumber: 1000, duration: 5, comment: 'Start Move'), // Start vertex
				new AssetComment(project: project, taskNumber: 1001, duration: 8, comment: 'SD App Exchange'),
				new AssetComment(project: project, taskNumber: 1002, duration: 10, comment: 'SD App Payroll'),
				new AssetComment(project: project, taskNumber: 1003, duration: 3, comment: 'PD Srv xyzzy'),
				new AssetComment(project: project, taskNumber: 1004, duration: 20, comment: 'PD VM vsmssql01'),
				new AssetComment(project: project, taskNumber: 1005, duration: 15, comment: 'Unrack Srv xyzzy'),
				new AssetComment(project: project, taskNumber: 1006, duration: 9, comment: 'Disable monitoring'), // Start vertex
				new AssetComment(project: project, taskNumber: 1007, duration: 45, comment: 'Post Move Testing'), // Sink vertex
				new AssetComment(project: project, taskNumber: 1008, duration: 2, comment: 'Make Coffee'), // Sink vertex
				new AssetComment(project: project, taskNumber: 1009, duration: 1, comment: 'Done Move') // Sink vertex
			]

		and:
			List<TaskDependency> dependencies = [
				new TaskDependency(id: 100, predecessor: tasks[0], assetComment: tasks[1], type: 'SS'),
				// 1 > [3,4], 3 > 5, 5 > [7,8,9], 4 > 9
				// 8    3,20  3  15, 15  45,2,1   20  1
				// 1 > 3 > 5 > [7,8,9] (72)
				// 1 > 4 > 9 (29)
				new TaskDependency(id: 101, predecessor: tasks[0], assetComment: tasks[2], type: 'SS'),
				new TaskDependency(id: 102, predecessor: tasks[1], assetComment: tasks[3], type: 'SS'),
				new TaskDependency(id: 103, predecessor: tasks[1], assetComment: tasks[4], type: 'SS'),
				new TaskDependency(id: 104, predecessor: tasks[2], assetComment: tasks[3], type: 'SS'),
				new TaskDependency(id: 105, predecessor: tasks[3], assetComment: tasks[5], type: 'SS'),
				// 4 downstream tasks
				new TaskDependency(id: 106, predecessor: tasks[4], assetComment: tasks[9], type: 'SS'),
				new TaskDependency(id: 107, predecessor: tasks[6], assetComment: tasks[1], type: 'SS'),
				new TaskDependency(id: 108, predecessor: tasks[6], assetComment: tasks[2], type: 'SS'),
				new TaskDependency(id: 109, predecessor: tasks[5], assetComment: tasks[7], type: 'SS'),
				new TaskDependency(id: 110, predecessor: tasks[5], assetComment: tasks[8], type: 'SS'),
				new TaskDependency(id: 111, predecessor: tasks[5], assetComment: tasks[9], type: 'SS')
			]


	}
}
