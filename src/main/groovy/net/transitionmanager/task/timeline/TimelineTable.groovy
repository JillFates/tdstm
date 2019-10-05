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
	 * {@code Set} of {@code TaskVertex} used in fields calculations.
	 */
	Set<TaskVertex> vertices
	/**
	 * Window start time range used in critical path calculation
	 */
	Date windowStartTime
	/**
	 * Window end time range used in critical path calculation
	 */
	Date windowEndTime

	/**
	 * Class constructor. It builds new a instance of {@code TimelineTable}.
	 * It initializes {@code TaskTimeLineGraph#vertices} using currentTime parameter.
	 *
	 * @param graph an instance of {@code TaskTimeLineGraph}
	 * @param windowStartTime Window start time range used in critical path calculation
	 * @param windowEndTime Window end time range used in critical path calculation
	 * @param currentTime Time when the calculation of the critical path is executed
	 */
	TimelineTable(TaskTimeLineGraph graph, Date windowStartTime, Date windowEndTime, Date currentTime) {
		this.windowStartTime = windowStartTime
		this.windowEndTime = windowEndTime
		vertices = graph.vertices
		vertices.each { TaskVertex vertex ->
			Date startingPoint = currentTime
			if (windowStartTime > currentTime) {
				startingPoint = windowStartTime
			}
			vertex.initialize(startingPoint)
		}
	}

	/**
	 * During critical path analysis on the graph, walking forward {@code TimeLine#doDijkstraForEarliestTimes}
	 * from sources to sinks, it calculates earliest start time and
	 * earliest start finish using
	 * @param vertex an instance of {@code TaskVertex}
	 * @param successor another instance of {@code TaskVertex}, predecessor of vertex param.
	 * @return true: any of the times were updated, false otherwise.
	 */
	boolean checkAndUpdateEarliestTimes(TaskVertex vertex, TaskVertex successor) {

		if (successor.earliestStart == 0 || (vertex.earliestFinish > successor.earliestStart)) {
			successor.earliestStart = vertex.earliestFinish
			successor.earliestFinish = successor.earliestStart + successor.remaining + successor.elapsed
			successor.criticalPredecessor = vertex

			use(TimeCategory) {
				successor.earliestStartDate = vertex.earliestFinishDate
				if (successor.hasStarted()) {
					successor.earliestStartDate = successor.actualStart ?: vertex.earliestFinishDate
				} else if (successor.earliestStartDate < windowStartTime) { // Validate if earliestStartDate is in range [windowStartTime, windowEndTime]
					successor.earliestStartDate = windowStartTime
				}
				successor.earliestFinishDate = successor.earliestStartDate + successor.remaining.minutes + successor.elapsed.minutes
			}

			return true
		} else {
			return false
		}
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
				if (predecessor.hasStarted()) {
					predecessor.latestFinishDate = predecessor.earliestFinishDate
				}
				predecessor.latestStartDate = predecessor.latestFinishDate - predecessor.remaining.minutes - predecessor.elapsed.minutes
			}

			timesUpdated = true
		}
		return timesUpdated
	}

	/**
	 * For each sink, calculate the earliest path and the latest path. Additionally, determine which is
	 * the correct critical path.
	 * Each critical path calculated in saved in an instance of {@code CriticalPathRoute}.
	 * For more details about that see {@see TimelineTable#getPath}
	 * Then there is 2 new scenarios:
	 * <ul>
	 * <li> 1) It's a new {@code CriticalPathRoute} from a different sub-graph.
	 * 		So it's going to be added immediately in
	 * {@code TimelineSummary#criticalPathRoutes}
	 * <li> 2) It has an intersection with other {@code CriticalPathRoute}
	 * 		previously calculated and it is necessary to determine if
	 * 		this is bigger than the intersected in order to be replaced.
	 * </ul>
	 * @param taskGraph an instance of {@code TaskTimeLineGraph}
	 * @param timelineSummary an instance of {@code timelineSummary}
	 */
	void calculateAllPaths(TaskTimeLineGraph taskGraph, TimelineSummary timelineSummary) {

		taskGraph.sinks.each { TaskVertex sink ->

			CriticalPathRoute newCriticalPathRoute = new CriticalPathRoute(getPath(sink), sink.latestFinish)
			CriticalPathRoute criticalPathRoute = timelineSummary.criticalPathRoutes.find {
				it.intersectsPath(newCriticalPathRoute)
			}

			if (!criticalPathRoute) {
				// new critical path from a different sub-graph
				timelineSummary.addCriticalPathRoute(newCriticalPathRoute)
			} else if (newCriticalPathRoute.isLargerEqualsThan(criticalPathRoute)) {
				//it is larger that an existing one and it need to be replaced
				timelineSummary.replaceCriticalPathRouteBy(criticalPathRoute, newCriticalPathRoute)
			}
		}
	}

	/**
	 * Starting from a sink, traverse the timeline table building the path corresponding
	 * to the earliest/latest times using {@code TaskVertex#criticalPredecessor} field.
	 *
	 * @param sink an instance of {@code TaskVertex}
	 * @return a{@code List} of {@code TaskVertex} instances
	 * 		than belongs to a path from one {@code TaskVertex} source
	 * 		to the sink {@code TaskVertex} parameter
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

		if (!windowEndTime) {
			windowEndTime = sink.latestFinishDate
		}

		Integer windowEndTimeDifference = 0
		if (!sink.hasStarted() && windowEndTime > sink.latestFinishDate) {
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