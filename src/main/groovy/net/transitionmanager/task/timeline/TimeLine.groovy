package net.transitionmanager.task.timeline

import com.tdssrc.grails.StopWatch
import com.tdssrc.grails.TimeUtil
import groovy.transform.CompileStatic

/**
 * Critical Path analysis using a Dijkstra-like algorithm, Deep First Search.
 * It can calculate Critical Path routes, Cycles and determines {@code TaskVertex} fields
 * like earliest start and finish or latest start and latest finish.
 *
 */
@CompileStatic
class TimeLine {

	private TaskTimeLineGraph graph
	private TimelineSummary timelineSummary
	private TimelineTable timelineTable

	TimeLine(TaskTimeLineGraph graph) {
		this.graph = graph
	}

	/**
	 * <p>Calculates Critical Path analysis using an instance of {@code TaskTimeLineGraph} set in constructor.</p>
	 * <p>It uses 3 variables for calculate slack, earliest start and finish, remaining and elapsed time,
	 * and latest start and finish. </p>
	 * <p>Window start time and window end time define a window time range to detect if critical path
	 * could be inside that range or not. It also add slack time in critical path calculation.</p>
	 * <p>It returns an instance of {@TimelineSummary} that contains all the results after critical path calculation</p>
	 * <p> Internally it executes critical path doing a Dijkstra-like algorithm, Deep First Search.
	 * It makes it doing first walking forward {@code TimeLine#doDijkstraForEarliestTimes}
	 * from sources to sinks and then backwards {@code TimeLine#doDijkstraForLatestTimes}
	 * from sinks to sources</p>
	 * <p>It also calculates elapsed time and saves it in {@code #TimeLine}</p>
	 *
	 * @param windowStartTime a {@code Date} instance defining window range start time
	 * @param windowEndTime a {@code Date} instance defining window range end time
	 * @param currentTime a {@code Date} instance defining calculation time
	 * @return an instance of {@code TimelineSummary} with results containing Critical Path,
	 * 			Cycles and the table with earliest/latest times for each task.
	 *
	 * @see TimelineSummary
	 */
	TimelineSummary calculate(Date windowStartTime, Date windowEndTime, Date currentTime = TimeUtil.nowGMT()) {

		StopWatch stopWatch = new StopWatch()

		// Initialize object for results.
		timelineSummary = new TimelineSummary(windowStartTime, windowEndTime, currentTime)

		// Initialize the table that will contain the information of the graph traversal.
		timelineTable = new TimelineTable(graph, windowStartTime, windowEndTime, currentTime)

		String tag = UUID.randomUUID()
		stopWatch.begin(tag)
		// Executes the Critical Path Analysis.
		executeCriticalPathAnalysis()
		// Build all the paths results.
		timelineTable.calculateAllPaths(graph, timelineSummary)

		determineFullyCyclicalGraph(graph, timelineSummary)

		timelineSummary.elapsedTime = TimeUtil.ago(stopWatch.lap(tag))
		return timelineSummary
	}

	/**
	 * Executes the Critical Path Analysis on the graph.
	 * First walking forward {@code TimeLine#doDijkstraForEarliestTimes}
	 * from sources to sinks and then backwards {@code TimeLine#doDijkstraForLatestTimes}
	 * from sinks to sources
	 */
	private void executeCriticalPathAnalysis() {

		for (TaskVertex start : graph.getStarts()) {
			GraphPath graphPath = new GraphPath()
			doDijkstraForEarliestTimes(start, graphPath)
		}

		for (TaskVertex sink : graph.getSinks()) {
			GraphPath graphPath = new GraphPath()
			timelineTable.updateSinkLatestTimes(sink)
			doDijkstraForLatestTimes(sink, graphPath)
		}
	}

	/**
	 * Calculate if there are cyclical references for the special edge case where:
	 * - There are no cycles yet present
	 * - There is more than one task
	 * - There are no start vectors or sink vectors present
	 * If this is the case, then add the list of {@code vertices} to the {@code cycles} element on {@code TimelineSummary}
	 * so it will represent a cycle present on the Graph.
	 * @param taskGraph an instance of {@code TaskTimeLineGraph}
	 * @param timelineSummary an instance of {@code timelineSummary}
	 */
	private void determineFullyCyclicalGraph(TaskTimeLineGraph taskGraph, TimelineSummary timelineSummary) {
		if (!timelineSummary.hasCycles() && taskGraph.vertices.size() > 1 && (taskGraph.hasNoStarts() || taskGraph.hasNoSinks())) {
			timelineSummary.cycles.add(taskGraph.vertices as List)
		}
	}

	/**
	 * Executes a Dijkstra-like algorithm, Deep First Search.
	 * It calculates {@code TaskVertex#latestStart} and {@code TaskVertex#latestFinish}
	 *
	 * @param vertex current {@code TaskVertex} to be analyzed.
	 * @param graphPath current {@code graphPath}
	 * 			used to detect and avoid cycles
	 * @see TimelineTable#checkAndUpdateLatestTimes(net.transitionmanager.task.timeline.TaskVertex, net.transitionmanager.task.timeline.TaskVertex)
	 */
	private void doDijkstraForLatestTimes(TaskVertex vertex, GraphPath graphPath) {
		graphPath.push(vertex)
		for (TaskVertex predecessor : vertex.predecessors) {
			if (!graphPath.visited(predecessor)
				&& timelineTable.checkAndUpdateLatestTimes(predecessor, vertex)) {
				doDijkstraForLatestTimes(predecessor, graphPath)
			}
		}
		graphPath.pop()
	}

	/**
	 * Executes a Dijkstra-like algorithm, Deep First Search.
	 * It calculates {@code TaskVertex#earliestStart} and {@code TaskVertex#earliestFinish}.
	 * It also detects cycles adding results in {@code TimelineSummary}
	 *
	 * @param vertex current {@code TaskVertex} to be analyzed.
	 * @param graphPath current {@code graphPath}
	 * 			used to detect and avoid cycles
	 * @see TimelineTable#checkAndUpdateEarliestTimes(net.transitionmanager.task.timeline.TaskVertex, net.transitionmanager.task.timeline.TaskVertex)
	 * @see TimelineSummary#cycles
	 */
	private void doDijkstraForEarliestTimes(TaskVertex vertex, GraphPath graphPath) {
		graphPath.push(vertex)
		for (TaskVertex successor : vertex.successors) {
			if (graphPath.visited(successor)) {
				timelineSummary.cycles.add(graphPath.getCyclePath(successor))
			} else if (timelineTable.checkAndUpdateEarliestTimes(vertex, successor)) {
				doDijkstraForEarliestTimes(successor, graphPath)
			}
		}
		graphPath.pop()
	}

	/**
	 * Returns current instance of {@code TimelineTable}
	 * @return an instance of {@code TimelineTable}
	 */
	TimelineTable getTimelineTable() {
		return timelineTable
	}

	/**
	 * <b>Internal class used to calculate
	 * visited nodes during deep first search.
	 * It can collect visited {@code TaskVertex} visited and determines
	 * if during deep first search, a current node was already used in current path</b>
	 */
	private class GraphPath {

		/**
		 * Stack of {@code TaskVertex} used to calculate
		 * each stack of nodes visited,
		 * during deep first search algorithm.
		 */
		Stack<TaskVertex> vertices = new Stack<TaskVertex>()
		Map<TaskVertex, Boolean> visitedMap = [:]

		/**
		 * Removes latest {@code TaskVertex} from
		 * {@code GraphPath#visitedMap}
		 */
		void pop() {
			TaskVertex vertex = vertices.pop()
			visitedMap.remove(vertex)
		}

		/**
		 * Adds a new instance of {@code TaskVertex} in
		 * {@code GraphPath#visitedMap}
		 */
		void push(TaskVertex vertex) {
			vertices.push(vertex)
			visitedMap[vertex] = true
		}

		/**
		 * Returns true if {@code TaskVertex}
		 * was marked as visited or not
		 * @param vertex an instance of {@code TaskVertex}
		 * @return true if {@code TaskVertex} was visited
		 * 		otherwise returns false
		 */
		boolean visited(TaskVertex vertex) {
			return visitedMap[vertex]
		}

		/**
		 * Calculates the path between a {@code TaskVertex} instance
		 * and the end of the current {@code GraphPath}.
		 * It is used calculating cycles.
		 *
		 * @param vertex an instance of {@code TaskVertex}
		 * @return a list with all {@code TaskVertex}
		 * 		in cycle path
		 */
		List<TaskVertex> getCyclePath(TaskVertex vertex) {
			List<TaskVertex> cycle = vertices.subList(vertices.indexOf(vertex), vertices.size())
			return cycle.toList()
		}
	}
}
