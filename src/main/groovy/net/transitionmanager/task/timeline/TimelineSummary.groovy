package net.transitionmanager.task.timeline

import net.transitionmanager.project.MoveEvent

/**
 * Summary of results applied to a {@code TimeLine}
 * over an instance of {@code TaskTimeLineGraph}.
 */
class TimelineSummary {

	Date windowStartTime
	Date windowEndTime
	Date currentTime

	MoveEvent moveEvent

	TimelineTable timelineTable

	List<List<TaskVertex>> cycles = []
	List<TaskVertex> longestCriticalPath = []
	List<CriticalPathRoute> criticalPathRoutes = []

	TimelineSummary(Date windowStartTime, Date windowEndTime, Date currentTime) {
		this.windowStartTime = windowStartTime
		this.windowEndTime = windowEndTime
		this.currentTime = currentTime
	}

	void addCriticalPathRoute(CriticalPathRoute criticalPathRoute) {
		criticalPathRoute.vertices.each { it.markAsCriticalPath() }
		criticalPathRoutes.add(criticalPathRoute)
	}

	void replaceCriticalPathRouteBy(CriticalPathRoute criticalPathRoute, CriticalPathRoute newCriticalPathRoute) {
		criticalPathRoute.vertices.each { it.unmarkAsCriticalPath() }
		criticalPathRoutes.remove(criticalPathRoute)
		addCriticalPathRoute(newCriticalPathRoute)
	}
}

class CriticalPathRoute {

	final List<TaskVertex> vertices
	final Integer latestFinish

	CriticalPathRoute(List<TaskVertex> vertices, Integer latestFinish) {
		this.vertices = vertices
		this.latestFinish = latestFinish
	}

	Boolean intersectsPath(CriticalPathRoute other) {
		return !vertices.intersect(other.vertices)?.isEmpty()
	}

	boolean isGreatherEqualsThan(CriticalPathRoute other) {
		return latestFinish >= other.latestFinish
	}
}
