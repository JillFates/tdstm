package net.transitionmanager.task.timeline
/**
 * Summary of results applied to a {@code TimeLine}
 * over an instance of {@code TaskTimeLineGraph}.
 *
 */
class TimelineSummary {

	/**
	 * Window start time range used in critical path calculation
	 */
	Date windowStartTime
	/**
	 * Window end time range used in critical path calculation
	 */
	Date windowEndTime
	/**
	 * Time when the calculation of the critical path is executed
	 */
	Date currentTime
	/**
	 * {@code List} of cycles detected during critical path calculation.
	 * A cycle is represented by a {@code List} of {@code TaskVertex}
	 */
	List<List<TaskVertex>> cycles = []
	/**
	 * {@code List} of {@codeCriticalPathRoute} detected during critical path calculation
	 */
	List<CriticalPathRoute> criticalPathRoutes = []

	TimelineSummary(Date windowStartTime, Date windowEndTime, Date currentTime) {
		this.windowStartTime = windowStartTime
		this.windowEndTime = windowEndTime
		this.currentTime = currentTime
	}

	/**
	 * Add a new Critical Path route detected during critical path calculation
	 * It also marks each {@code TaskVertex} as critical path
	 * @param criticalPathRoute an instance of {@code CriticalPathRoute}
	 */
	void addCriticalPathRoute(CriticalPathRoute criticalPathRoute) {
		criticalPathRoute.vertices.each { it.markAsCriticalPath() }
		criticalPathRoutes.add(criticalPathRoute)
	}

	/**
	 * Replaces an instance of {@code CriticalPathRoute} by a new one with a larger path.
	 * It also marks the new {@code CriticalPathRoute} {@code TaskVertex} list as critical path.
	 * For replaced {@code CriticalPathRoute} instance it unmark all {@code TaskVertex}.
	 *
	 * @param criticalPathRoute a instance of {@code CriticalPathRoute} to be added in
	 * {@code TimelineSummary#criticalPathRoute}
	 * @param newCriticalPathRoute a instance of {@code CriticalPathRoute} to be removed from
	 * {@code TimelineSummary#criticalPathRoute}
	 */
	void replaceCriticalPathRouteBy(CriticalPathRoute criticalPathRoute, CriticalPathRoute newCriticalPathRoute) {
		criticalPathRoute.vertices.each { it.unmarkAsCriticalPath() }
		criticalPathRoutes.remove(criticalPathRoute)
		addCriticalPathRoute(newCriticalPathRoute)
	}
}
/**
 * Route for a critical path results.
 * It contains a {@code List} of {@code TaskVertex}
 * and It also defines the latest finish used comparing {@code CriticalPathRoute} instances.
 */
class CriticalPathRoute {

	/**
	 * {@code List} of {@code TaskVertex} that belongs to a critical path result
	 */
	final List<TaskVertex> vertices
	final Integer latestFinish

	CriticalPathRoute(List<TaskVertex> vertices, Integer latestFinish) {
		this.vertices = vertices
		this.latestFinish = latestFinish
	}

	/**
	 * Checks intersections of both {@code CriticalPathRoute#vertices}.
	 *
	 * @param other an instance of {@code CriticalPathRoute}.
	 * @return true if there is an intersection between both {@code CriticalPathRoute#vertices}.
	 */
	Boolean intersectsPath(CriticalPathRoute other) {
		return !vertices.intersect(other.vertices)?.isEmpty()
	}

	/**
	 * Checks if an instance of {@code CriticalPathRoute} is larger than the current
	 * using  {@code CriticalPathRoute#latestFinish} field
	 *
	 * @param other an instance of {@code CriticalPathRoute}.
	 * @return true if {@code CriticalPathRoute#latestFinish}
	 * 		is bigger than the cu
	 */
	boolean isLargerEqualsThan(CriticalPathRoute other) {
		return latestFinish >= other.latestFinish
	}
}
