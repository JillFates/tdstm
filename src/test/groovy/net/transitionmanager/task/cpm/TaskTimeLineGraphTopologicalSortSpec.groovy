package net.transitionmanager.task.cpm

import net.transitionmanager.task.TaskTimeLineGraphTopologicalSort
import net.transitionmanager.task.cpm.helper.TaskTimeLineGraphTestHelper
import spock.lang.Shared
import spock.lang.Specification

class TaskTimeLineGraphTopologicalSortSpec extends Specification {

	/**
	 * Common TaskVertex Ids used in several test cases.
	 */
	static String A = 'A', B = 'B', C = 'C', D = 'D', E = 'E', F = 'F', G = 'G', H = 'H'

	@Shared
	TaskTimeLineGraphTestHelper taskTimeLineGraphTestHelper = new TaskTimeLineGraphTestHelper()

	void 'test can apply topological sort on a simple TaskTimeLineGraph'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex('0', 10)
				.withVertex('1', 11)
				.withVertex('2', 12).addEdgeTo('3')
				.withVertex('3', 13).addEdgeTo('1')
				.withVertex('4', 14).addEdgesTo('0', '1')
				.withVertex('5', 15).addEdgesTo('2', '0')
				.build()

		when: 'applies a topological sort'
			TaskTimeLineGraphTopologicalSort topologicalSort = new TaskTimeLineGraphTopologicalSort(taskTimeLineGraph)

		then: 'topologicalSort has order'
			topologicalSort.hasOrder()

		and: 'can retrieve results ordered by TaskVertex#taskId field'
			topologicalSort.order.collect { it.taskId } == ['5', '4', '2', '3', '1', '0']
	}

	void 'test can apply topological sort on a TaskTimeLineGraph with vertices topologically ordered'() {

		given: 'a TaskTimeLineGraph with a list of TaskVertex'
			TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
				.withVertex(A, 3).addEdgesTo(B, C, D)
				.withVertex(B, 4).addEdgeTo(D)
				.withVertex(C, 2).addEdgeTo(D)
				.withVertex(D, 5)
				.build()

		when: 'topological order is applied'
			TaskTimeLineGraphTopologicalSort sort = new TaskTimeLineGraphTopologicalSort(taskTimeLineGraph)

		then:
			sort.hasOrder()




	}
}
