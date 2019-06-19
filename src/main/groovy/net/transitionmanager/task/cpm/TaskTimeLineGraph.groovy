package net.transitionmanager.task.cpm


class TaskTimeLineGraph {

	Map<String, TaskTimeLineVertex> verticesMap

	List<TaskTimeLineVertex> vertices

	TaskTimeLineGraph(List<TaskTimeLineVertex> vertices) {
		this.vertices = vertices
		verticesMap = this.vertices.collectEntries { TaskTimeLineVertex activity ->
			[(activity.taskId): activity]
		}
	}

	TaskTimeLineVertex getSource() {
		// Avoid sources without successors too.
		List<TaskTimeLineVertex> sources = vertices.findAll { TaskTimeLineVertex act ->
			act.predecessors.isEmpty() && !act.successors.isEmpty()
		}

		if (sources.size() == 1) {
			return sources.first()
		} else {
			// If there is more than one source
			// We could add a new TaskTimeLineVertex
			// pointing to these multiple sources
			TaskTimeLineVertex hiddenSource = TaskTimeLineVertex.Factory.newHiddenSource()
			vertices = [hiddenSource] + vertices
			sources.each { addEdge(hiddenSource, it) }
			return hiddenSource
		}
	}

	/**
	 *
	 * @return
	 */
	TaskTimeLineVertex getSink() {

		List<TaskTimeLineVertex> sinks = vertices.findAll { TaskTimeLineVertex act ->
			act.successors.isEmpty() && !act.predecessors.isEmpty()
		}

		if (sinks.size() == 1) {
			return sinks.first()
		} else {
			// If there is more than one sink
			// We could add a new TaskTimeLineVertex
			// pointing to these multiple sinks
			TaskTimeLineVertex hiddenSink = TaskTimeLineVertex.Factory.newHiddenSink()
			vertices = vertices + [hiddenSink]
			sinks.each { addEdge(it, hiddenSink) }
			return hiddenSink
		}
	}

	/**
	 *
	 * @return
	 */
	int V() {
		return vertices.size()
	}

	/**
	 *
	 * @param taskTimeLineVertexId
	 * @return
	 */
	TaskTimeLineVertex getVertex(String taskTimeLineVertexId) {
		return this.verticesMap[taskTimeLineVertexId]
	}

	/**
	 *
	 * @param taskTimeLineVertex
	 * @return
	 */
	TaskTimeLineVertex getVertex(TaskTimeLineVertex taskTimeLineVertex) {
		return this.getVertex(taskTimeLineVertex.taskId)
	}
	/**
	 *
	 * @param from
	 * @param to
	 * @return this instance of {@code TaskTimeLineGraph}
	 */
	TaskTimeLineGraph addEdge(TaskTimeLineVertex from, TaskTimeLineVertex to) {
		// TODO: Could we add here validations
		// Like self loops ?
		from.addSuccessor(to)
		return this
	}
}
