package net.transitionmanager.task.timeline

import groovy.transform.CompileStatic
import net.transitionmanager.exception.InvalidParamException

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
     * Returns {@code TaskVertex#vertices} size
     *
     * @return {@code TaskVertex#vertices} size
     */
    int verticesSize() {
        return vertices.size()
    }

    /**
     * Retrieve an instance of {@code TaskVertex} by its {@code TaskVertex#taskNumber}
     *
     * @param taskNumber
     * @return an instance of {@code TaskVertex}
     * 		or null if it is not present in {@code TaskVertex#verticesMap}
     */
    TaskVertex getVertex(Integer taskNumber) {
        return this.verticesMap[taskNumber]
    }

    /**
     * Retrieve an instance of {@code TaskVertex} by its {@code TaskVertex#taskNumber}
     *
     * @param taskVertex an instance of {@code TaskVertex}
     * @return an instance of {@code TaskVertex}
     * 		or null if it is not present in {@code TaskVertex#verticesMap}
     */
    TaskVertex getVertex(TaskVertex taskVertex) {
        return getVertex(taskVertex.taskNumber)
    }

    /**
     * Returns if {@code TaskVertex#starts} is Empty
     *
     * @return true if {@code TaskVertex#starts} is empty
     * 			otherwise return false
     */
    Boolean hasNoStarts() {
        return starts.isEmpty()
    }

    /**
     * Returns if {@code TaskVertex#sinks} is Empty
     *
     * @return true if {@code TaskVertex#sinks} is empty
     * 			otherwise return false
     */
    Boolean hasNoSinks() {
        return sinks.isEmpty()
    }

    /**
     * Returns if {@code TaskVertex#starts} size is bigger than 1
     *
     * @return true if {@code TaskVertex#starts} size is bigger than 1
     * 			otherwise return false
     */
    Boolean hasMultipleStarts() {
        return starts.size() > 1
    }

    /**
     * Returns if {@code TaskVertex#sinks} size is bigger than 1
     *
     * @return true if {@code TaskVertex#sinks} size is bigger than 1
     * 			otherwise return false
     */
    Boolean hasMultipleSinks() {
        return sinks.size() > 1
    }

    /**
     * Returns if {@code TaskVertex#starts} size is equals to 1
     *
     * @return true if {@code TaskVertex#starts#size == 1}
     */
    Boolean hasOneStart() {
        return starts.size() == 1
    }

    /**
     * Returns if {@code TaskVertex#sinks} size is equals to 1
     *
     * @return true if {@code TaskVertex#sinks#size == 1}
     */
    Boolean hasOneSink() {
        return sinks.size() == 1
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
        Builder withVertex(Long taskId, Integer taskNumber, String taskComment, String description, Integer duration) {

            checkAndAddCurrentVertex()
            currentVertex = new TaskVertex(
                    taskId,
                    taskNumber,
                    taskComment,
                    duration
            )
            return this
        }

        /**
         * Adds a new {@code TimelineTask} task in
         * {@code TaskTimeLineGraph} builder creation.
         *
         * @param task a instance of {@code TimelineTask}
         * @return current instance of {@code Builder}
         */
        Builder withVertex(TimelineTask task) {

            checkAndAddCurrentVertex()
            currentVertex = new TaskVertex(
                    task.id,
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
        Builder withVertices(TimelineTask... tasks) {
            tasks.each { TimelineTask task -> withVertex(task) }
            return this
        }

        /**
         * Add a {@code List} of {@code}
         * @param tasks
         * @return current instance of {@code Builder}
         */
        Builder withVertices(List<TimelineTask> tasks) {
            tasks.each { TimelineTask task -> withVertex(task) }
            return this
        }

        /**
         * Adds {@code TimelineDependency} as edge in {@code TaskTimeLineGraph}.
         * It used {@code TimelineDependency#successorTaskNumber} and
         * {@code TimelineDependency#successor} as edge relation
         *
         * @param TimelineDependency an instance of {@code TimelineDependency}
         * @return current instance of {@code Builder}
         */
        Builder withEdge(TimelineDependency taskDependency) {
            edgesByTaskNumber.add(new Tuple2<>(taskDependency.predecessorTaskNumber, taskDependency.successorTaskNumber))
            return this
        }

        /**
         * Adds {@code TimelineDependency} as edge in {@code TaskTimeLineGraph}.
         * It used {@code TimelineDependency#successorTaskNumber} and
         * {@code TimelineDependency#successorTaskNumber} as edge relation
         *
         * @param dependencies a List of of {@code TimelineDependency}
         * @return
         */
        Builder withEdges(TimelineDependency... dependencies) {
            dependencies.each { TimelineDependency taskDependency -> withEdge(taskDependency) }
            return this
        }

        /**
         * Adds {@code TimelineDependency} as edge in {@code TaskTimeLineGraph}.
         * It used {@code TimelineDependency#successorTaskNumber} and
         * {@code TimelineDependency#successorTaskNumber} as edge relation
         *
         * @param taskDependencies a List of of {@code TimelineDependency}
         * @return
         */
        Builder withEdges(List<TimelineDependency> dependencies) {
            dependencies.each { TimelineDependency dependency -> withEdge(dependency) }
            return this
        }

        /**
         * Adds a new vertex with parameters: taskComment and duration
         * @param taskId
         * @param taskComment
         * @param duration
         * @return current instance of {@code TaskTimeLineGraph.Builder}
         */
        Builder withVertex(Long taskId, Integer taskNumber, String taskComment, Integer duration) {
            withVertex(taskId, taskNumber, taskComment, '', duration)
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
                TaskVertex successor = taskVertexMapByTaskNumber[tuple.second]
                predecessor.addSuccessor(successor)
            }

            edgesByTaskComment.each { Tuple2<String, String> tuple ->
                TaskVertex predecessor = taskVertexMapByTaskComment[tuple.first]
                TaskVertex successor = taskVertexMapByTaskComment[tuple.second]
                predecessor.addSuccessor(successor)
            }

            return new TaskTimeLineGraph(taskVertexMapByTaskNumber.values().toSet())
        }
    }
}
