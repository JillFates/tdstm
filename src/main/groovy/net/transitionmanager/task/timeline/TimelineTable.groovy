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
	 * To avoid unnecessary traversing of the graph, the process must quit the
	 * analysis of the current path as soon as it detects that neither times are going to be updated.
	 *
	 * When any of the times needs updating, the corresponding start and finish times are updated along
	 * with the reference to the predecessor task that caused the update.
	 *
	 * @param successor
	 * @param predecessor
	 * @return true: any of the times were updated, false otherwise.
	 */
	boolean checkAndUpdateTimes(TaskVertex successor, TaskVertex predecessor) {
		boolean earliestUpdated = updateEarliestTimes(successor, predecessor)
		boolean latestUpdates = updateLatestTimes(successor, predecessor)
		return earliestUpdated || latestUpdates
	}


	/**
	 * Check and update the earliest times.
	 * @param predecessor
	 * @param taskDuration
	 * @return
	 */
	boolean updateEarliestTimes(TaskVertex successor, TaskVertex predecessor) {
		boolean timesUpdated = false
		TimelineNode predecessorNode = nodesMap[predecessor]
		TimelineNode successorNode = nodesMap[successor]
		if (predecessorNode.latestFinish < successorNode.earliestStart) {
			successorNode.earliestStart = predecessorNode.latestFinish
			successorNode.earliestFinish = successorNode.earliestStart + successor.duration
			successorNode.earliestPredecessor = predecessor
			timesUpdated = true
		}
		return timesUpdated
	}

	/**
	 * Check and update the latest times.
	 * @param predecessor
	 * @param taskDuration
	 * @return
	 */
	boolean updateLatestTimes(TaskVertex successor, TaskVertex predecessor) {
		boolean timesUpdated = false
		TimelineNode predecessorNode = nodesMap[predecessor]
		TimelineNode successorNode = nodesMap[successor]
		if (predecessorNode.latestFinish > successorNode.latestStart) {
			successorNode.latestStart = predecessorNode.latestFinish
			successorNode.latestFinish = predecessorNode.latestStart + successor.duration
			successorNode.latestPredecessor = predecessor
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
		timelineSummary.earliestPaths = []
		timelineSummary.latestPaths = []
		Integer criticalFinish = Integer.MIN_VALUE
		taskGraph.sinks.each { TaskVertex sink ->
			TimelineNode sinkNode = nodesMap[sink]
			List<TaskVertex> earliestPath = getPath(sink, 'earliestPredecessor')
			List<TaskVertex> latestPath = getPath(sink, 'latestPredecessor')
			if (sinkNode.latestFinish > criticalFinish) {
				criticalFinish = sinkNode.latestFinish
				timelineSummary.criticalPath = latestPath
			}
			timelineSummary.latestPaths.add(latestPath)
			timelineSummary.earliestPaths.add(earliestPath)
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