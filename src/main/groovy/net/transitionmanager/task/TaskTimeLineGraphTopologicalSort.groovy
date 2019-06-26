package net.transitionmanager.task

import net.transitionmanager.task.cpm.TaskTimeLineGraph
import net.transitionmanager.task.cpm.TaskVertex

/**
 *
 */
class TaskTimeLineGraphTopologicalSort {

	private final TaskTimeLineGraph graph

	private List<TaskVertex> order
	private Stack<TaskVertex> stack
	private Map<String, Boolean> visited

	TaskTimeLineGraphTopologicalSort(TaskTimeLineGraph graph) {
		this.graph = graph
		stack = new Stack<TaskVertex>()
		// Mark all the vertices as not visited
		visited = graph.vertices.collectEntries { TaskVertex taskVertex ->
			[(taskVertex.taskId): false]
		}
		order = []
		applyTopologicalSort()
	}

	void topologicalSortVisit(TaskVertex taskVertex) {
		// Mark the current node as visited.
		visited[taskVertex.taskId] = true

		// Recur for all the vertices adjacent to this
		// vertex
		for (TaskVertex successor : taskVertex.successors) {
			if (!visited[successor.taskId]) {
				topologicalSortVisit(successor)
			}
		}
		stack.push(taskVertex)
	}

	// The function to do Topological Sort. It uses
	// recursive topologicalSortUtil()
	void applyTopologicalSort() {

		for (TaskVertex taskVertex : graph.vertices) {
			if (!visited[taskVertex.taskId]) {
				topologicalSortVisit(taskVertex)
			}
		}

		while (!stack.isEmpty()) {
			order.add(stack.pop())
		}

	}

	/**
	 * Returns a topological order if the digraph has a topologial order,
	 * and {@code null} otherwise.
	 * @return a topological order of the vertices (as an List) if the
	 *    digraph has a topological order (or equivalently, if the digraph is a DAG),
	 *    and {@code null} otherwise
	 */
	List<TaskVertex> getOrder() {
		return order
	}

	boolean hasOrder() {
		return !order.isEmpty()
	}
}
