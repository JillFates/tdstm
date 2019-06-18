package net.transitionmanager.task.cpm

/**
 * http://www.personal.kent.edu/~rmuhamma/Algorithms/MyAlgorithms/GraphAlgor/depthSearch.htm
 */
class DirectedCycle {

	enum NodeState {
		/**
		 * node X has not been visited before.
		 */
		UNVISITED,
		/**
		 * node X has been visited but not all of its outgoing edges have been visited.
		 */
		OPEN,
		/**
		 * means that all the edges going out of u have been visited.
		 */
		CLOSED
	}

	private Map<String, NodeState> stateMap = [:]
	private Boolean cycle = false

	DirectedCycle(DirectedGraph directedGraph) {
		// Initialize custom structure
		directedGraph.activities.each { Activity activity ->
			stateMap[activity.taskId] = NodeState.UNVISITED
		}
		directedGraph.activities.each { Activity activity ->
			if (stateMap[activity.taskId] == NodeState.UNVISITED) {
				dfsVisit(activity)
			}
		}
	}

	// check that algorithm computes either the topological order or finds a directed cycle
	private void dfsVisit(Activity activity) {

		stateMap[activity.taskId] = NodeState.OPEN
		activity.successors.each { Activity successor ->
			if (stateMap[successor.taskId] == NodeState.UNVISITED) {
				dfsVisit(successor)
			} else if (stateMap[successor.taskId] == NodeState.OPEN) {
				cycle = true
			}
		}
		stateMap[activity.taskId] = NodeState.CLOSED
	}

	/**
	 * Does the {@code DirectedGraph} have a directed cycle?
	 * @return {@code true} if the digraph has a directed cycle, {@code false} otherwise
	 */
	boolean hasCycle() {
		return cycle
	}
}
