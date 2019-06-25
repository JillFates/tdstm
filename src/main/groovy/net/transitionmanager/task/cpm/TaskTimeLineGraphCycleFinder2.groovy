package net.transitionmanager.task.cpm

/**
 * 	// This function is a variation of DFSUtil() in
 * 	// https://www.geeksforgeeks.org/archives/18212
 */
class TaskTimeLineGraphCycleFinder2 {

	private TaskTimeLineGraph graph
	Map<String, Boolean> recStack
	Map<String, Boolean> visited

	TaskTimeLineGraphCycleFinder2(TaskTimeLineGraph graph) {
		this.graph = graph
	}

	private boolean isCyclicUtil(TaskVertex vertex) {

		// Mark the current node as visited and
		// part of recursion stack
		if (recStack[vertex.taskId]) {
			return true
		}

		if (visited[vertex.taskId]) {
			return false
		}

		visited[vertex.taskId] = true
		recStack[vertex.taskId] = true


		for (TaskVertex successor : vertex.successors) {
			if (isCyclicUtil(successor)) {
				return true
			}
		}

		recStack[vertex.taskId] = false;

		return false;
	}

	// Returns true if the graph contains a
	// cycle, else false.
	// This function is a variation of DFS() in
	// https://www.geeksforgeeks.org/archives/18212
	Boolean isCyclic() {

		// Mark all the vertices as not visited and
		// not part of recursion stack
		visited = [:]
		recStack = [:]

		// Call the recursive helper function to
		// detect cycle in different DFS trees
		for (TaskVertex vertex : graph.vertices) {
			if (isCyclicUtil(vertex)) {
				return true
			}
		}
		return false;

	}
}
