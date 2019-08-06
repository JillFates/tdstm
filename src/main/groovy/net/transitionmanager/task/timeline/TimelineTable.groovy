package net.transitionmanager.task.timeline


import groovy.time.TimeCategory

/**
 * Calculates following values for vertices in critical path calculation:
 * 1) Earliest start
 * 2) Earliest finish
 * 3) Latest start
 * 4) Latest finish
 */
class TimelineTable {

	/**
	 * {@code Set} of {@code TaskVertex}
	 */
	Set<TaskVertex> vertices
	Date windowEndTime
	Date windowStartTime

	TimelineTable(TaskTimeLineGraph graph, Date windowStartTime, Date windowEndTime, Date currentTime) {
		this.windowStartTime = windowStartTime
		this.windowEndTime = windowEndTime
		vertices = graph.vertices
		vertices.each { TaskVertex vertex ->
			vertex.initialize(currentTime)
		}
	}

	/**
	 *
	 * @param successor
	 * @param predecessor
	 * @return true: any of the times were updated, false otherwise.
	 */
	boolean checkAndUpdateEarliestTimes(TaskVertex currentVertex, TaskVertex successor) {
		boolean timesUpdated = false

		if (successor.earliestStart == 0 || (currentVertex.earliestFinish > successor.earliestStart)) {
			successor.earliestStart = currentVertex.earliestFinish
			successor.earliestFinish = successor.earliestStart + successor.remaining + successor.elapsed
			successor.criticalPredecessor = currentVertex

			use(TimeCategory) {
				successor.earliestStartDate = currentVertex.earliestFinishDate
				successor.earliestFinishDate = successor.earliestStartDate + successor.remaining.minutes + successor.elapsed.minutes
			}

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
			predecessor.latestStart = predecessor.latestFinish - predecessor.remaining - predecessor.elapsed

			use(TimeCategory) {
				predecessor.latestFinishDate = vertex.latestStartDate
				predecessor.latestStartDate = predecessor.latestFinishDate - predecessor.remaining.minutes - predecessor.elapsed.minutes
			}

			timesUpdated = true
		}
		return timesUpdated
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
			List<TaskVertex> earliestPath = getPath(sink)

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
	 * @return
	 */
	private List<TaskVertex> getPath(TaskVertex sink) {
		List<TaskVertex> path = []
		TaskVertex vertex = sink
		while (vertex != null) {
			path.add(vertex)
			vertex = (TaskVertex) vertex.criticalPredecessor
		}
		return path.reverse()
	}

	/**
	 * Updates a sink {@code TaskVertex} latest finish
	 * and latest start before calculate backwards
	 * critical path algorithm.
	 *
	 * @param sink an instance of {@code TaskVertex}
	 * @see TimeLine#doDijkstraForLatestTimes(net.transitionmanager.task.timeline.TaskVertex, net.transitionmanager.task.timeline.TimeLine.GraphPath)
	 */
	void updateSinkLatestTimes(TaskVertex sink) {

		sink.latestFinish = sink.earliestFinish
		sink.latestFinishDate = sink.earliestFinishDate

		Integer windowEndTimeDifference = 0
		if (!sink.hasStarted() && !sink.hasFinished() && windowEndTime > sink.latestFinishDate) {
			use(TimeCategory) {
				windowEndTimeDifference = (windowEndTime - sink.latestFinishDate).minutes
			}
			sink.latestFinishDate = windowEndTime
		}

		sink.latestStart = sink.latestFinish + windowEndTimeDifference - sink.remaining - sink.elapsed
		use(TimeCategory) {
			sink.latestStartDate = sink.latestFinishDate - sink.remaining.minutes - sink.elapsed.minutes
		}

	}

}