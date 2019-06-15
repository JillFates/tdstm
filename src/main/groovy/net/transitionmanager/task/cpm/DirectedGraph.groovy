package net.transitionmanager.task.cpm

class DirectedGraph {

	List<Activity> activities
	/**
	 * number of vertices in this digraph
	 */
	int vertices

	DirectedGraph(List<Activity> activities) {
		this.activities = activities
		this.vertices = this.activities.size()
	}

	int getVertices() {
		return vertices
	}
	/**
	 * Check whether the graph contains a cycle or not
	 * using DFS solution
	 * Backedge: an edge that is from a node to itself (selfloop)
	 * or one of its ancestors
	 *
	 * @return true if this {@code DirectedGraph} contains a cycle
	 * 		and false in all the other cases.
	 */
	Boolean isCyclic() {

		Map<String, Boolean> visitedMap = [:]

		Stack<Activity> stack = new Stack<Activity>()
		// TODO: Starts with the source of thd DirectedGraph
		Activity source = activities.first()
		stack.push(source)
		visitedMap[source.taskId] = true

		while (!stack.isEmpty()) {
			Activity activity = stack.pop()
			for (Activity predecessor in activity.predecessors) {
				if (visitedMap[predecessor.taskId]) {
					return true
				} else {
					stack.push(predecessor)
					visitedMap[predecessor.taskId] = true
				}
			}
		}
		return false
	}
}

class ActivityWrapper {

	final Activity activity
	Boolean visited

	ActivityWrapper(Activity activity) {
		this.activity = activity
	}

}
