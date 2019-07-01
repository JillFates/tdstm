package net.transitionmanager.task.timeline

import groovy.transform.CompileStatic

@CompileStatic
class TimeLine {

	private TaskTimeLineGraph graph
	private TimelineSummary timelineSummary
	private TimelineTable timelineTable

	TimeLine(TaskTimeLineGraph graph) {
		this.graph = graph
	}

	TimelineSummary calculate(Date startDate) {
		// Initialize object for results.
		timelineSummary = new TimelineSummary(startDate)
		// Initialize the table that will contain the information of the graph traversal.
		timelineTable = new TimelineTable(graph, startDate)
		// Executes the Critical Path Analysis.
		executeCriticalPathAnalysis()
		// Transform the earliest/latest starts into dates and calculate the slack for each task.
		timelineTable.calculateDatesAndSlacks(startDate)
		// Build all the paths.
		timelineTable.calculateAllPaths(graph, timelineSummary)
		// Return results containing Critical Path, Cycles and the table with earliest/latest times for each task.
		return timelineSummary
	}


	/**
	 * Executes the Critical Path Analysis on the graph.
	 */
	private void executeCriticalPathAnalysis() {
		for (TaskVertex root : graph.getStarts()) {
			GraphPath graphPath = new GraphPath()
			doDijkstra(root, graphPath)
		}
	}

	/**
	 * Executes a Dijkstra-like algorithm.
	 *
	 * @param vertex
	 */
	private void doDijkstra(TaskVertex vertex, GraphPath graphPath) {
		graphPath.push(vertex)
		for (TaskVertex successor : vertex.successors) {
			if (graphPath.visited(successor)) {
				timelineSummary.cycles.add(graphPath.getCyclePath(vertex))
			} else if (timelineTable.checkAndUpdateTimes(successor, vertex)) {
				doDijkstra(successor, graphPath)
			}
		}
		graphPath.pop()
	}

	private class GraphPath {

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
			return vertices.subList(vertices.indexOf(vertex), vertices.size())
		}
	}
}
