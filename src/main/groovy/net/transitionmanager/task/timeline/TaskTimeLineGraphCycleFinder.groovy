package net.transitionmanager.task.timeline

/**
 * http://www.personal.kent.edu/~rmuhamma/Algorithms/MyAlgorithms/GraphAlgor/depthSearch.htm
 * {@code TaskTimeLineGraphCycleFinder} implements DFS (Depth-First Search).
 * The basic idea of depth-first search is this: It methodically explore every edge.
 * We start over from different vertices as necessary. As soon as we discover a vertex, DFS starts exploring from it.
 *
 */
class TaskTimeLineGraphCycleFinder {

	/**
	 * Like BFS, to keep track of progress depth-first-search colors each vertex. Each vertex of the graph is in one of three states:
	 *
	 * 1. Undiscovered;
	 * 2. Discovered but not finished (not done exploring from it); and
	 * 3. Finished (have found everything reachable from it) i.e. fully explored.
	 */
	enum NodeState {
		/**
		 * node X has not been visited before.
		 */
		UNDISCOVERED,
		/**
		 * node X has been visited but not all of its outgoing edges have been visited.
		 */
		DISCOVERED,
		/**
		 * means that all the edges going out of u have been visited.
		 */
		FINISHED
	}

	private Map<String, NodeState> nodeStateMap = [:]
	private List<List<TaskVertex>> cycles = []


	TaskTimeLineGraphCycleFinder(TaskTimeLineGraph directedGraph) {
		// Initialize custom structure
		directedGraph.vertices.each { TaskVertex activity ->
			nodeStateMap[activity.taskId] = NodeState.UNDISCOVERED
		}

		directedGraph.vertices.each { TaskVertex activity ->
			if (nodeStateMap[activity.taskId] == NodeState.UNDISCOVERED) {
				dfsVisit(activity)
			}
		}
	}

	/**
	 *
	 * @param taskVertex
	 */
	private void dfsVisit(TaskVertex taskVertex) {

		nodeStateMap[taskVertex.taskId] = NodeState.DISCOVERED

		for (TaskVertex successor : taskVertex.successors) {
			if (nodeStateMap[successor.taskId] == NodeState.UNDISCOVERED) {
				dfsVisit(successor)
			} else if (nodeStateMap[successor.taskId] == NodeState.DISCOVERED) {
				// Saves cycles to help in cycles detection
				cycles.add(
					[
						taskVertex,
						successor,
						taskVertex.predecessors.find { successor.successors.contains(it) }
					]
				)
			}
		}
		nodeStateMap[taskVertex.taskId] = NodeState.FINISHED
	}

	/**
	 * Does the {@code TaskTimeLineGraph} have a directed cycle?
	 * @return {@code true} if the digraph has a directed cycle, {@code false} otherwise
	 */
	boolean hasCycle() {
		return !cycles.isEmpty()
	}

	/**
	 * Returned a {@code List} of {@code TaskVertex}
	 * cycles detected.
	 *
	 * @return {@code List} of {@code TaskVertex}
	 */
	List<List<TaskVertex>> getCycles() {
		return cycles
	}
}
