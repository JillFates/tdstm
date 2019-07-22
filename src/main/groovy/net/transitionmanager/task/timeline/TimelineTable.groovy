package net.transitionmanager.task.timeline

class TimelineTable {

	Map<TaskVertex, TimelineNode> nodesMap

	TimelineTable(TaskTimeLineGraph graph, Date startDate) {
		nodesMap = [:]
		graph.vertices.each { TaskVertex vertex ->
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
			successorNode.earliestFinish = successorNode.earliestStart + successorNode.duration
			successorNode.earliestPredecessor = vertex

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
			predecessorNode.latestStart = predecessorNode.latestFinish - predecessorNode.duration
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

		taskGraph.sinks.each { TaskVertex sink ->
			TimelineNode sinkNode = nodesMap[sink]
			List<TaskVertex> earliestPath = getPath(sink, 'earliestPredecessor')

			CriticalPathRoute newCriticalPathRoute = new CriticalPathRoute(earliestPath, sinkNode.latestFinish)
			CriticalPathRoute criticalPathRoute = timelineSummary.criticalPathRoutes.find { it.intersectsPath(newCriticalPathRoute) }

			if (!criticalPathRoute){
				// new critical path for a different sub-graph
				timelineSummary.addCriticalPathRoute(newCriticalPathRoute)
			} else if (newCriticalPathRoute.isGreatherEqualsThan(criticalPathRoute)){
				timelineSummary.replaceCriticalPathRouteBy(criticalPathRoute, newCriticalPathRoute)
			}
		}
	}


	/**
	 * Starting from a sink, traverse the timeline table building the path corresponding
	 * to the earliest/latest times.
	 *
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

	/**
	 * Updates a sink {@code TaskVertex} latest finish
	 * and latest start before calculate backwards
	 * critical path algorithm.
	 * @param sink an instance of {@code TaskVertex}
	 * @see TimeLine#doDijkstraForLatestTimes(net.transitionmanager.task.timeline.TaskVertex, net.transitionmanager.task.timeline.TimeLine.GraphPath)
	 */
	void updateSinkLatestTimes(TaskVertex sink) {
		TimelineNode sinkNode = nodesMap[sink]
		sinkNode.latestFinish = sinkNode.earliestFinish
		sinkNode.latestStart = sinkNode.latestFinish - sinkNode.duration

	}
}