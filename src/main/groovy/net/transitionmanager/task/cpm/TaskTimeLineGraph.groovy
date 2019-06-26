package net.transitionmanager.task.cpm

import net.transitionmanager.exception.InvalidParamException


class TaskTimeLineGraph {

	Map<String, TaskVertex> verticesMap

	Set<TaskVertex> vertices

	TaskTimeLineGraph(Set<TaskVertex> vertices) {
		this.vertices = vertices
		verticesMap = this.vertices.collectEntries { TaskVertex taskVertex ->
			[(taskVertex.taskId): taskVertex]
		}
	}

	TaskVertex getStart() {
		// Avoid sources without successors too.
		Set<TaskVertex> starters = vertices.findAll { TaskVertex taskVertex ->
			taskVertex.predecessors.isEmpty() && taskVertex.taskId != TaskVertex.BINDER_SINK_NODE
		}

		if (starters.size() == 1) {
			return starters.first()
		} else {
			// If there is more than one source
			// We could add a new TaskVertex
			// pointing to these multiple sources
			TaskVertex binderStart = TaskVertex.Factory.newBinderStart()
			vertices = [binderStart] + vertices
			starters.each { addEdge(binderStart, it) }
			return binderStart
		}
	}

	/**
	 *
	 * @return
	 */
	TaskVertex getSink() {

		Set<TaskVertex> sinks = vertices.findAll { TaskVertex taskVertex ->
			taskVertex.successors.isEmpty() && taskVertex.taskId != TaskVertex.BINDER_START_NODE
		}

		if (sinks.size() == 1) {
			return sinks.first()
		} else {
			// If there is more than one sink
			// We could add a new TaskVertex
			// pointing to these multiple sinks
			TaskVertex hiddenSink = TaskVertex.Factory.newBinderSink()
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
	 * @param taskId
	 * @return
	 */
	TaskVertex getVertex(String taskId) {
		return this.verticesMap[taskId]
	}

	/**
	 *
	 * @param taskVertex
	 * @return
	 */
	TaskVertex getVertex(TaskVertex taskVertex) {
		return this.getVertex(taskVertex.taskId)
	}
	/**
	 * Adds a new edge with successor and predecessor
	 * @param from an instance of {@code TaskVertex}
	 * @param to an instance of {@code TaskVertex}
	 * @return this instance of {@code TaskTimeLineGraph}
	 */
	private TaskTimeLineGraph addEdge(TaskVertex from, TaskVertex to) {
		from.addSuccessor(to)
		return this
	}
	/**
	 * Builder pattern for {@code TaskTimeLineGraph}
	 */
	static class Builder {

		List<Map<String, ?>> vertices = []
		Map<String, ?> currentVertex

		/**
		 * Adds a new vertex with parameters: taskId, description and duration
		 * @param taskId
		 * @param description
		 * @param duration
		 * @return
		 */
		Builder withVertex(String taskId, String description, Integer duration) {
			if (currentVertex) {
				vertices.add(currentVertex)
			}
			currentVertex = [
				taskId     : taskId,
				duration   : duration,
				description: description,
				successors : []
			]
			return this
		}

		/**
		 * Adds a new vertex with parameters: taskId and duration
		 * @param taskId
		 * @param duration
		 * @return same instance of {@code TaskTimeLineGraph.Builder}
		 */
		Builder withVertex(String taskId, Integer duration) {
			withVertex(taskId, '', duration)
		}

		Builder addEdgeTo(String taskId) {
			if (!currentVertex) {
				throw new InvalidParamException('Cannot add an edge without a previous vertex')
			}
			currentVertex.successors.add(taskId)
			return this
		}

		Builder addEdgesTo(String... taskIds) {
			if (!currentVertex) {
				throw new InvalidParamException('Cannot add an edge without a previous vertex')
			}
			taskIds.each { currentVertex.successors.add(it) }
			return this
		}

		/**
		 * Builds a new instance of {@code TaskTimeLineGraph}
		 *
		 * @return an instance of {@code TaskTimeLineGraph}
		 */
		TaskTimeLineGraph build() {

			if (currentVertex) {
				vertices.add(currentVertex)
			}
			Map<String, TaskVertex> taskVertices =
				vertices.collectEntries { Map<String, ?> vertexMap ->
					[
						(vertexMap.taskId): TaskVertex.Factory.newSimpleVertex(
							vertexMap.taskId, vertexMap.description, vertexMap.duration
						)
					]
				}

			vertices.each { Map<String, ?> vertexMap ->
				vertexMap.successors.each { String successorId ->
					TaskVertex successor = taskVertices[successorId]
					if (!successor) {
						throw new InvalidParamException("TaskVertex: ${successorId} was not found")
					}
					taskVertices[vertexMap.taskId].addSuccessor(successor)
				}
			}
			return new TaskTimeLineGraph(taskVertices.values().toSet())
		}
	}
}
