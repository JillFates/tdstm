package net.transitionmanager.task.timeline


import groovy.transform.CompileStatic
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskDependency

@CompileStatic
class TaskTimeLineGraph {

	Map<Integer, TaskVertex> verticesMap

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
	 * @param taskNumber
	 * @return
	 */
	TaskVertex getVertex(Integer taskNumber) {
		return this.verticesMap[taskNumber]
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

		Map<Integer, TaskVertex> taskVertexMapByTaskNumber = [:]
		Map<String, TaskVertex> taskVertexMapByTaskComment = [:]

		List<Tuple2<Integer, Integer>> edgesByTaskNumber = []
		List<Tuple2<String, String>> edgesByTaskComment = []
		/**
		 * Current Task Vertex used in Builder Pattern
		 */
		TaskVertex currentVertex

		/**
		 * <p>Check status of {@code Builder#currentVertex}
		 * It also prepares an internal structure for this builder:</p>
		 * 1) A Map with taskNumber as Key and TaskVertex as Value<BR/>
		 * 2) A Map with taskComment as Key and TaskVertex as Value<BR/>
		 */
		private void checkAndAddCurrentVertex() {
			if (currentVertex) {
				taskVertexMapByTaskNumber[currentVertex.taskNumber] = currentVertex
				taskVertexMapByTaskComment[currentVertex.taskComment] = currentVertex
			}
		}

		/**
		 * Adds a new vertex with parameters: taskComment, description and duration
		 * @param taskComment * @param description
		 * @param duration
		 * @return
		 */
		Builder withVertex(Integer taskNumber, String taskComment, String description, Integer duration) {

			checkAndAddCurrentVertex()
			currentVertex = new TaskVertex(
				taskNumber,
				taskComment,
				duration
			)
			return this
		}

		/**
		 * Adds a new {@code AssetComment} task in
		 * {@code TaskTimeLineGraph} builder creation.
		 *
		 * @param task a instance of {@code AssetComment}
		 * @return current instance of {@code Builder}
		 */
		Builder withVertex(AssetComment task) {

			checkAndAddCurrentVertex()
			currentVertex = new TaskVertex(
				task.taskNumber,
				task.comment,
				task.duration,
				task.status,
				task.actStart,
				task.statusUpdated
			)
			return this
		}

		/**
		 * Add a {@code List} of {@code}
		 * @param tasks
		 * @return current instance of {@code Builder}
		 */
		Builder withVertices(AssetComment... tasks) {
			tasks.each { AssetComment task -> withVertex(task) }
			return this
		}

		/**
		 * Add a {@code List} of {@code}
		 * @param tasks
		 * @return current instance of {@code Builder}
		 */
		Builder withVertices(List<AssetComment> tasks) {
			tasks.each { AssetComment task -> withVertex(task) }
			return this
		}

		/**
		 * Adds {@code TaskDependency} as edge in {@code TaskTimeLineGraph}.
		 * It used {@code TaskDependency#predecessor} and
		 * {@code TaskDependency#successor} as edge relation
		 *
		 * @param taskDependency an instance of {@code TaskDependency}
		 * @return current instance of {@code Builder}
		 */
		Builder withEdge(TaskDependency taskDependency) {
			edgesByTaskNumber.add(new Tuple2<>(taskDependency.predecessor.taskNumber, taskDependency.successor.taskNumber))
			return this
		}

		/**
		 * Adds {@code TaskDependency} as edge in {@code TaskTimeLineGraph}.
		 * It used {@code TaskDependency#predecessor} and
		 * {@code TaskDependency#successor} as edge relation
		 *
		 * @param taskDependencies a List of of {@code TaskDependency}
		 * @return
		 */
		Builder withEdges(TaskDependency... taskDependencies) {
			taskDependencies.each { TaskDependency taskDependency -> withEdge(taskDependency) }
			return this
		}

		/**
		 * Adds {@code TaskDependency} as edge in {@code TaskTimeLineGraph}.
		 * It used {@code TaskDependency#predecessor} and
		 * {@code TaskDependency#successor} as edge relation
		 *
		 * @param taskDependencies a List of of {@code TaskDependency}
		 * @return
		 */
		Builder withEdges(List<TaskDependency> taskDependencies) {
			taskDependencies.each { TaskDependency taskDependency -> withEdge(taskDependency) }
			return this
		}

		/**
		 * Adds a new vertex with parameters: taskComment and duration
		 * @param taskComment
		 * @param duration
		 * @return current instance of {@code TaskTimeLineGraph.Builder}
		 */
		Builder withVertex(Integer taskNumber, String taskComment, Integer duration) {
			withVertex(taskNumber, taskComment, '', duration)
		}

		/**
		 * Add Edges to a {@code TaskTimeLineGraph} using
		 * {@code TaskTimeLineGraph#Builder#currentVertex}
		 * and a task comment.
		 * <pre>
		 * 	TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
		 * 				.withVertex(1, A, 3).addEdgeTo(B)
		 * 				.withVertex(2, B, 4)
		 * 				.build()
		 * <pre>
		 * @param taskNumber {@code String} task comment
		 * @return current instance of {@code TaskTimeLineGraph.Builder}
		 */
		Builder addEdgeTo(String taskComment) {
			if (!currentVertex) {
				throw new InvalidParamException('Cannot add an edge without a previous vertex')
			}
			edgesByTaskComment.add(new Tuple2<>(currentVertex.taskComment, taskComment))
			return this
		}

		/**
		 * Add Edges to a {@code TaskTimeLineGraph} using
		 * {@code TaskTimeLineGraph#Builder#currentVertex}
		 * and a list of task comments.
		 * <pre>
		 * 	TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
		 * 				.withVertex(1, A, 3).addEdgesTo(B, C)
		 * 				.withVertex(2, B, 4)
		 * 	 			.withVertex(3, C, 5)
		 * 				.build()
		 * <pre>
		 * @param taskComments a {@code List} of {@code String} task comments
		 * @return current instance of {@code TaskTimeLineGraph.Builder}
		 */
		Builder addEdgesTo(String... taskComments) {
			if (!currentVertex) {
				throw new InvalidParamException('Cannot add an edge without a previous vertex')
			}
			taskComments.each { String taskComment -> addEdgeTo(taskComment) }
			return this
		}

		/**
		 * Add Edges to a {@code TaskTimeLineGraph} using
		 * {@code TaskTimeLineGraph#Builder#currentVertex}
		 * and a task number.
		 * <pre>
		 * 	TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
		 * 				.withVertex(1, A, 3).addEdgeTo(2)
		 * 				.withVertex(2, B, 4)
		 * 				.build()
		 * <pre>
		 * @param taskNumber {@code Integer} task number
		 * @return current instance of {@code TaskTimeLineGraph.Builder}
		 */
		Builder addEdgeTo(Integer taskNumber) {
			if (!currentVertex) {
				throw new InvalidParamException('Cannot add an edge without a previous vertex')
			}
			edgesByTaskNumber.add(new Tuple2<>(currentVertex.taskNumber, taskNumber))
			return this
		}

		/**
		 * Add Edges to a {@code TaskTimeLineGraph} using
		 * {@code TaskTimeLineGraph#Builder#currentVertex}
		 * and a list of task numbers.
		 * <pre>
		 * 	TaskTimeLineGraph taskTimeLineGraph = new TaskTimeLineGraph.Builder()
		 * 				.withVertex(1, A, 3).addEdgesTo(2, 3)
		 * 				.withVertex(2, B, 4)
		 * 	 			.withVertex(3, C, 5)
		 * 				.build()
		 * <pre>
		 * @param taskNumbers a {@code List} of {@code Integer} task numbers
		 * @return current instance of {@code TaskTimeLineGraph.Builder}
		 */
		Builder addEdgesTo(Integer... taskNumbers) {
			if (!currentVertex) {
				throw new InvalidParamException('Cannot add an edge without a previous vertex')
			}
			taskNumbers.each { Integer taskNumber -> addEdgeTo(taskNumber) }
			return this
		}

		/**
		 * Builds a new instance of {@code TaskTimeLineGraph}
		 *
		 * @return an instance of {@code TaskTimeLineGraph}
		 */
		TaskTimeLineGraph build() {

			checkAndAddCurrentVertex()

			edgesByTaskNumber.each { Tuple2<Integer, Integer> tuple ->
				TaskVertex predecessor = taskVertexMapByTaskNumber[tuple.first]
				TaskVertex sucessor = taskVertexMapByTaskNumber[tuple.second]
				predecessor.addSuccessor(sucessor)
			}

			edgesByTaskComment.each { Tuple2<String, String> tuple ->
				TaskVertex predecessor = taskVertexMapByTaskComment[tuple.first]
				TaskVertex sucessor = taskVertexMapByTaskComment[tuple.second]
				predecessor.addSuccessor(sucessor)
			}

			return new TaskTimeLineGraph(taskVertexMapByTaskNumber.values().toSet())
		}
	}
}
