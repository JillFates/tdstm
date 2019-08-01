package net.transitionmanager.task.timeline

import com.tdssrc.grails.TimeUtil
import groovy.transform.CompileStatic

@CompileStatic
class TimeLine {

	private TaskTimeLineGraph graph
	private TimelineSummary timelineSummary
	private TimelineTable timelineTable

	TimeLine(TaskTimeLineGraph graph) {
		this.graph = graph
	}

	TimelineSummary calculate(Date windowStartTime, Date windowEndTime, Date currentTime = TimeUtil.nowGMT()) {
		// Initialize object for results.
		timelineSummary = new TimelineSummary(windowStartTime, windowEndTime, currentTime)
		// Initialize the table that will contain the information of the graph traversal.
		timelineTable = new TimelineTable(graph, windowStartTime, windowEndTime, currentTime)
		// Executes the Critical Path Analysis.
		executeCriticalPathAnalysis()
		// Transform the earliest/latest starts into dates and calculate the slack for each task.
		//timelineTable.calculateDatesAndSlacks(startDate)
		// Build all the paths.
		timelineTable.calculateAllPaths(graph, timelineSummary)

		timelineSummary.timelineTable = timelineTable
		// Return results containing Critical Path, Cycles and the table with earliest/latest times for each task.
		return timelineSummary
	}


	/**
	 * Executes the Critical Path Analysis on the graph.
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
	 * Executes a Dijkstra-like algorithm, Deep First Search.
	 *
	 * @param vertex
	 * @param graphPath
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
	 *
	 * @param vertex
	 * @param graphPath
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


	TimelineTable getTimelineTable() {
		return timelineTable
	}

	/**
	 * <b>Internal class used to calculate
	 * visited nodes during deep first search.</b>
	 */
	private class GraphPath {

		/**
		 * Stack of {@code TaskVertex} used to calculate
		 * each stack of nodes visited,
		 * during deep first search algorithm.
		 */
		Stack<TaskVertex> vertices = new Stack<TaskVertex>()
		Map<TaskVertex, Boolean> visitedMap = [:]


		void pop() {
			TaskVertex vertex = vertices.pop()
			visitedMap.remove(vertex)
		}

		void push(TaskVertex vertex) {
			vertices.push(vertex)
			visitedMap[vertex] = true
		}

		boolean visited(TaskVertex vertex) {
			return visitedMap[vertex]
		}

		List<TaskVertex> getCyclePath(TaskVertex vertex) {
			List<TaskVertex> cycle = vertices.subList(vertices.indexOf(vertex), vertices.size())
			return cycle.toList()
		}
	}
}
