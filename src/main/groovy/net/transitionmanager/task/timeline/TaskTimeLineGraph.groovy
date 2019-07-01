package net.transitionmanager.task.timeline

import groovy.transform.CompileStatic
import net.transitionmanager.exception.InvalidParamException

@CompileStatic
class TaskTimeLineGraph {

	Map<String, TaskVertex> verticesMap

	Set<TaskVertex> vertices
	List<TaskVertex> starts = []
	List<TaskVertex> sinks = []

	TaskTimeLineGraph(Set<TaskVertex> vertices) {
		this.vertices = vertices
		verticesMap = this.vertices.collectEntries { TaskVertex taskVertex ->
			if (taskVertex.isStart()) {
				starts.add(taskVertex)
			}
			if (taskVertex.isSink()) {
				sinks.add(taskVertex)
			}

			[(taskVertex.taskNumber): taskVertex]
		}
	}

	List<TaskVertex> getStarts() {
		return starts
	}

	List<TaskVertex> getSinks() {
		return sinks
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
		return this.getVertex(taskVertex.taskNumber)
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
		Builder withVertex(Long id, String taskId, String description, Integer duration) {
			if (currentVertex) {
				vertices.add(currentVertex)
			}
			currentVertex = [
				id         : id,
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
		Builder withVertex(Long id, String taskId, Integer duration) {
			withVertex(id, taskId, '', duration)
		}

		Builder addEdgeTo(String taskId) {
			if (!currentVertex) {
				throw new InvalidParamException('Cannot add an edge without a previous vertex')
			}
			(currentVertex.successors as List).add(taskId)
			return this
		}

		Builder addEdgesTo(String... taskIds) {
			if (!currentVertex) {
				throw new InvalidParamException('Cannot add an edge without a previous vertex')
			}
			taskIds.each { (currentVertex.successors as List).add(it) }
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

					TaskVertex taskVertex = new TaskVertex(vertexMap.id as Long, vertexMap.taskId.toString(), vertexMap.duration as Integer)
					if (vertexMap.description) {
						taskVertex.comment = vertexMap.description
					}

					[(vertexMap.taskId): taskVertex]
				}

			vertices.each { Map<String, ?> vertexMap ->
				vertexMap.successors.each { String successorId ->
					TaskVertex successor = taskVertices[successorId]
					if (!successor) {
						throw new InvalidParamException("TaskVertex: ${successorId} was not found")
					}
					taskVertices[vertexMap.taskId.toString()].addSuccessor(successor)
				}
			}
			return new TaskTimeLineGraph(taskVertices.values().toSet())
		}
	}
}
