package net.transitionmanager.task.timeline
/**
 * Calculates following values for vertices in critical path calculation:
 * 1) Earliest start
 * 2) Earliest finish
 * 3) Latest start
 * 4) Latest finish
 */
class TimelineTable {

	Set<TaskVertex> vertices

	TimelineTable(TaskTimeLineGraph graph, Date startDate) {
		vertices = graph.vertices
		vertices.each { TaskVertex vertex ->
			vertex.initialize(startDate)
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

		if (successor.earliestStart == 0 || (vertex.earliestFinish > successor.earliestStart)) {
			successor.earliestStart = vertex.earliestFinish
			successor.earliestFinish = successor.earliestStart + successor.remainingDuration
			successor.earliestPredecessor = vertex

			timesUpdated = true
		}

		return timesUpdated
	}

	/**
	 *
	 * @param predecessor
	 * @param vertex
	 * @return
	 */
	boolean checkAndUpdateLatestTimes(TaskVertex predecessor, TaskVertex vertex) {

		boolean timesUpdated = false

		if (predecessor.latestFinish > vertex.latestStart) {
			predecessor.latestFinish = vertex.latestStart
			predecessor.latestStart = predecessor.latestFinish - predecessor.remainingDuration
			predecessor.latestPredecessor = predecessor
			timesUpdated = true
		}
		return timesUpdated
	}


	/**
	 * Transform the earliest/latest times into the corresponding duration
	 */
	void calculateDatesAndSlacks(Date startDate) {
		vertices.each { TaskVertex vertex ->
			vertex.calculateDatesAndSlack(startDate)
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
			List<TaskVertex> earliestPath = getPath(sink, 'earliestPredecessor')

			CriticalPathRoute newCriticalPathRoute = new CriticalPathRoute(earliestPath, sink.latestFinish)
			CriticalPathRoute criticalPathRoute = timelineSummary.criticalPathRoutes.find {
				it.intersectsPath(newCriticalPathRoute)
			}

			if (!criticalPathRoute) {
				// new critical path for a different sub-graph
				timelineSummary.addCriticalPathRoute(newCriticalPathRoute)
			} else if (newCriticalPathRoute.isGreatherEqualsThan(criticalPathRoute)) {
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
			vertex = (TaskVertex) vertex[predecessorField]
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
		sink.latestFinish = sink.earliestFinish
		sink.latestStart = sink.latestFinish - sink.remainingDuration

	}

}