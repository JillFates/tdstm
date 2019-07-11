package net.transitionmanager.task.timeline


import groovy.time.TimeCategory

class TimelineNode {

	Integer earliestStart = 0
	Integer earliestFinish = 0
	TaskVertex earliestPredecessor

	Integer latestStart = 0
	Integer latestFinish = Integer.MAX_VALUE
	TaskVertex latestPredecessor

	/* The following fields are only needed if start/finish times must be presented as dates. */
	Integer slack = 0

	Date earliestStartDate
	Date earliestFinishDate
	Date latestStartDate
	Date latestFinishDate

	TimelineNode(TaskVertex vertex, Date startDate) {
		Integer earliest = 0
		Integer latest = 0
		earliestStart = earliest
		latestStart = latest

//		this.duration = vertex.remainingDuration()
//      if (status in [COMPLETED, TERMINATED]){
//		return 0
//	   } else if (vertex.startTime != null) {
//			return this.duration = duration - ((NOW - actualStart)? :)
//		} else {
//			return this.duration
//		}

		if (vertex.isStart()) {
			// TODO: dcorrea review this logic.
			//Integer startTime = TimeUtil.toMinutes(TimeUtil.elapsed(startDate, vertex.actualStart))
			earliestStart = 0 //startTime
			//latestStart = startTime
			earliestFinish = earliestStart + vertex.duration
		}
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
