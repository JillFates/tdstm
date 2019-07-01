package net.transitionmanager.task.timeline

import net.transitionmanager.project.MoveEvent

class TimelineSummary {

	Date startDate
	MoveEvent moveEvent
	List<List<TaskVertex>> cycles = []
	List<TaskVertex> criticalPath
	List<List<TaskVertex>> earliestPaths
	List<List<TaskVertex>> latestPaths

	TimelineSummary(Date startDate) {
		this.startDate = startDate
	}
}
