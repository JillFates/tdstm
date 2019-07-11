package net.transitionmanager.task.timeline

class TimelineTable {

	Map<TaskVertex, TimelineNode> nodesMap

	TimelineTable(TaskTimeLineGraph graph, Date startDate) {
		nodesMap = [:]
		graph.vertices.each { TaskVertex vertex ->
			//todo: dcorrea. Add in constructor
			vertex.actualStart = startDate
			nodesMap[vertex] = new TimelineNode(vertex, startDate)
		}
	}

	/**
	 *
	 * @param successor
	 * @param predecessor
	 * @return true: any of the times were updated, false otherwise.
	 */
	boolean checkAndUpdateEarliestTimes(TaskVertex vertex, TaskVertex successor) {
		boolean timesUpdated = false

		TimelineNode currentNode = nodesMap[vertex]
		TimelineNode successorNode = nodesMap[successor]

		if (successorNode.earliestStart == 0 || (currentNode.earliestFinish > successorNode.earliestStart)) {
			successorNode.earliestStart = currentNode.earliestFinish
			successorNode.earliestFinish = successorNode.earliestStart + successor.duration
			successorNode.earliestPredecessor = vertex

			if (successor.isSink()) {
				successorNode.latestFinish = successorNode.earliestFinish
				successorNode.latestStart = successorNode.latestFinish - successor.duration
			}

			timesUpdated = true
		}

		return timesUpdated
	}

	boolean checkAndUpdateLatestTimes(TaskVertex predecessor, TaskVertex vertex) {

		boolean timesUpdated = false
		TimelineNode predecessorNode = nodesMap[predecessor]
		TimelineNode currentNode = nodesMap[vertex]

		if (predecessorNode.latestFinish > currentNode.latestStart) {
			predecessorNode.latestFinish = currentNode.latestStart
			predecessorNode.latestStart = predecessorNode.latestFinish - predecessor.duration
			predecessorNode.latestPredecessor = predecessor
			timesUpdated = true
		}
		return timesUpdated
	}


	/**
	 * Transform the earliest/latest times into the corresponding duration
	 */
	void calculateDatesAndSlacks(Date startDate) {
		nodesMap.each { TaskVertex vertex, TimelineNode timelineNode ->
			timelineNode.calculateDatesAndSlack(startDate)
		}
	}

	/**
	 * For each sink, calculate the earliest path and the latest path. Additionally, determine which is
	 * the critical path.
	 *
	 * @param taskGraph
	 * @param timelineSummary
	 */
	void calculateAllPaths(TaskTimeLineGraph taskGraph, TimelineSummary timelineSummary) {
		Integer criticalFinish = Integer.MIN_VALUE

		taskGraph.sinks.each { TaskVertex sink ->
			TimelineNode sinkNode = nodesMap[sink]
			List<TaskVertex> earliestPath = getPath(sink, 'earliestPredecessor')
			if (sinkNode.latestFinish > criticalFinish) {
				criticalFinish = sinkNode.latestFinish
				timelineSummary.criticalPath = earliestPath
			}
		}
	}


	/**
	 * Starting from a sink, traverse the timeline table building the path corresponding
	 * to the earliest/latest times.
	 * @param sink
	 * @param predecessorField
	 * @return
	 */
	private List<TaskVertex> getPath(TaskVertex sink, String predecessorField) {
		List<TaskVertex> path = []
		TaskVertex vertex = sink
		while (vertex != null) {
			path.add(vertex)
			vertex = (TaskVertex) nodesMap[vertex][predecessorField]
		}
		return path.reverse()
	}
}