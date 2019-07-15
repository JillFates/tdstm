package net.transitionmanager.task.timeline

import net.transitionmanager.project.MoveEvent

/**
 * Summary of results applied to a {@code TimeLine}
 * over an instance of {@code TaskTimeLineGraph}.
 */
class TimelineSummary {

	Date startDate
	MoveEvent moveEvent

	TimelineTable timelineTable

	List<List<TaskVertex>> cycles = []
	List<TaskVertex> criticalPath = []

	TimelineSummary(Date startDate) {
		this.startDate = startDate
	}
}
