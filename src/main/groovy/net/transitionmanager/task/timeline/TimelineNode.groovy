package net.transitionmanager.task.timeline


import groovy.time.TimeCategory

/*
		TimelineNode Structure definition
		-----------------------------------------------------------------------

											+----+----------+----+    	es: earliest start
	taskId: task Identification				| es |  taskId  | ls |  	ef: earliest finish
											+--------------------+
	duration: time to complete a task		| ls | duration | lf |		ls: latest start
											+----+----------+----+		lf: latest finish
 */

class TimelineNode {

	Integer duration

	Integer earliestStart = 0
	Integer earliestFinish = 0
	TaskVertex earliestPredecessor

	Integer latestStart = 0
	Integer latestFinish = Integer.MAX_VALUE
	TaskVertex latestPredecessor

	/* The following fields are only needed if start/finish times must be presented as dates. */
	Integer slack = 0

	TimelineNode(TaskVertex vertex, Date startDate) {
		Integer earliest = 0
		Integer latest = 0
		earliestStart = earliest
		latestStart = latest

		this.duration = vertex.remainingDurationInMinutes(startDate)

		if (vertex.isStart()) {
			// TODO: dcorrea review this logic.
			//Integer startTime = TimeUtil.toMinutes(TimeUtil.elapsed(startDate, vertex.actualStart))
			earliestStart = 0 //startTime
			//latestStart = startTime
			earliestFinish = earliestStart + vertex.duration
		}
	}

	/**
	 * Defines if this activity belongs to the Critical Path
	 */
	Boolean isCriticalPath() {
		return (earliestStart - latestStart == 0) && (earliestFinish - latestFinish == 0)
	}
	/**
	 * Slack for the current task is the difference between
	 * the latest start time and the earliest start time.
	 */
	Integer getSlack() {
		return latestStart - earliestStart
	}

	/**
	 * This method transform the earliest/latest start/finish times into the
	 * corresponding date values.
	 * It also calculates the slack for the current task as the difference between
	 * the latest start time and the earliest start time.
	 *
	 * @param startDate
	 */
	void calculateDatesAndSlack(Date startDate, TaskVertex vertex) {
		slack = latestStart - earliestStart
		use(TimeCategory) {
			vertex.earliestStart = startDate + earliestStart.minutes
			vertex.earliestFinish = startDate + earliestFinish.minutes
			vertex.latestStart = startDate + latestStart.minutes
			vertex.latestFinish = startDate + latestFinish.minutes
		}
	}

}
