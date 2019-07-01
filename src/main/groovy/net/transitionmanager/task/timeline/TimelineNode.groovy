package net.transitionmanager.task.timeline

import com.tdssrc.grails.TimeUtil
import groovy.time.TimeCategory

class TimelineNode {

	TaskVertex earliestPredecessor
	Integer earliestStart
	Integer earliestFinish

	TaskVertex latestPredecessor
	Integer latestStart
	Integer latestFinish

	/* The following fields are only needed if start/finish times must be presented as dates. */
	Integer slack
	Date earliestStartDate
	Date earliestFinishDate
	Date latestStartDate
	Date latestFinishDate

	TimelineNode(TaskVertex vertex, Date startDate) {
		Integer earliest = Integer.MAX_VALUE
		Integer latest = Integer.MIN_VALUE
		if (vertex.isStart()) {
			// TODO: dcorrea review this logic.
			Integer startTime = TimeUtil.toMinutes(TimeUtil.elapsed(startDate, vertex.actualStart))
			earliestStart = startTime
			latestStart = startTime
		}
		earliestStart = earliest
		latestStart = latest
	}

	/**
	 * This method transform the earliest/latest start/finish times into the
	 * corresponding date values.
	 * It also calculates the slack for the current task as the difference between
	 * the latest start time and the earliest start time.
	 *
	 * @param startDate
	 */
	void calculateDatesAndSlack(Date startDate) {
		slack = latestStart - earliestStart
		use(TimeCategory) {
			earliestStartDate = startDate + earliestStart.minutes
			earliestFinishDate = startDate + earliestFinish.minutes
			latestStartDate = startDate + latestStart.minutes
			latestFinishDate = startDate + latestFinish.minutes
		}
	}

}
