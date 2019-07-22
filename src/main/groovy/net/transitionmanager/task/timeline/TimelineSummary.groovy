package net.transitionmanager.task.timeline

import net.transitionmanager.project.MoveEvent

/**
 * Summary of results applied to a {@code TimeLine}
 * over an instance of {@code TaskTimeLineGraph}.
 */
class TimelineSummary {

	Date startDate
	Date windowStartDate
	Date windowEndDate

	MoveEvent moveEvent

	TimelineTable timelineTable

	List<List<TaskVertex>> cycles = []
	List<TaskVertex> longestCriticalPath = []
	List<CriticalPathRoute> criticalPathRoutes = []

	TimelineSummary(Date startDate) {
		this.startDate = startDate
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
