package net.transitionmanager.task.cpm

import groovy.transform.CompileStatic

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

	Activity getSource() {
		List<Activity> sources = activities.findAll { it.predecessors.isEmpty() }
		if (sources.size() == 1) {
			return sources.first()
		} else {
			// If there is more than one source
			// We could add a new Activity
			// pointing to these multiple sources
			Activity source = new Activity(taskId: Activity.HIDDEN_SOURCE_NODE, duration: 0)
			activities = [source] + activities
			sources.each { addEdge(source, it) }
			return source
		}
	}

	Activity getSink() {
		return activities.last()
	}

	int getVertices() {
		return vertices
	}

	/**
	 *
	 * @param from
	 * @param to
	 * @return this instance of {@code DirectedGraph}
	 */
	DirectedGraph addEdge(Activity from, Activity to) {
		// TODO: Could we add here validations
		// Like self loops ?
		from.addSuccessor(to)
		return this
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
		// TODO: dcorrea: Starts with the source of thd DirectedGraph
		// Refactor this
		Activity source = getSource()
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
