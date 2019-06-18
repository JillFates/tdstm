package net.transitionmanager.task.cpm

class DirectedGraph {

	Map<String, Activity> activitiesMap

	List<Activity> activities
	/**
	 * number of vertices in this digraph
	 */
	int vertices

	DirectedGraph(List<Activity> activities) {
		this.activities = activities
		this.vertices = this.activities.size()
		//TODO: dcorrea create activitiesMap

	}

	Activity getSource() {
		// Avoid sources without sucessors too.
		List<Activity> sources = activities.findAll { Activity act ->
			act.predecessors.isEmpty() && !act.successors.isEmpty()
		}
		if (sources.size() == 1) {
			return sources.first()
		} else {
			// If there is more than one source
			// We could add a new Activity
			// pointing to these multiple sources
			Activity hiddenSource = new Activity(taskId: Activity.HIDDEN_SOURCE_NODE, duration: 1)
			activities = [hiddenSource] + activities
			sources.each { addEdge(hiddenSource, it) }
			return hiddenSource
		}
	}

	/**
	 *
	 * @return
	 */
	Activity getSink() {

		List<Activity> sinks = activities.findAll { Activity act ->
			act.successors.isEmpty() && !act.predecessors.isEmpty()
		}

		if (sinks.size() == 1) {
			return sinks.first()
		} else {
			// If there is more than one sink
			// We could add a new Activity
			// pointing to these multiple sinks
			Activity hiddenSink = new Activity(taskId: Activity.HIDDEN_SOURCE_NODE, duration: 1)
			activities = activities + [hiddenSink]
			sinks.each { addEdge(it, hiddenSink) }
			return hiddenSink
		}
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
}
