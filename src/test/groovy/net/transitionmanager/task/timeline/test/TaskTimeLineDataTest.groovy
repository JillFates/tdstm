package net.transitionmanager.task.timeline.test

import net.transitionmanager.task.timeline.TaskVertex
import net.transitionmanager.task.timeline.TimelineTable
import spock.lang.Shared

import java.text.SimpleDateFormat

trait TaskTimeLineDataTest {

	@Shared
	String aDay = '06/22/2018'

	@Shared
	SimpleDateFormat formatter = new SimpleDateFormat('MM/dd/yyyy HH:mm')

	Date hourInDay(String dateTime) {
		return dateTime ? formatter.parse(aDay + ' ' + dateTime) : null
	}

	/*
		TaskVertex Structure definition
		-----------------------------------------------------------------------

											+-----+----------+-----+    	est: earliest start time
	taskId: task Identification				| est |  taskId  | eet |  		eet: earliest end time
											+----------------------+
	duration: time to complete a task		| lst | duration | let |		lst: latest start time
											+-----+----------+-----+		let: latest end time
 */
	/**
	 * Returns a list of list with results.
	 * Used fo assertions
	 * @return a{@code List} of {@code TaskVertex} results
	 * 			using a Matrix format
	 */
	void withTaskVertex(TaskVertex target,
						List<Object> firstRow,
						List<Integer> secondRow) {

		assert firstRow == [
			target.earliestStartTime,
			target.taskNumber,
			target.earliestEndTime
		]
		assert secondRow == [
			target.latestStartTime,
			target.duration,
			target.latestEndTime
		]
	}

	void withCriticalPath(Set<TaskVertex> target, Set<String> criticalPath) {
		assert target.collect { it.taskNumber } == criticalPath
	}

	/**
	 * Asserts several values from a {@code TaskTimeLineGraph#vertices}
	 * from a custom table in a GString defined like te following example:
	 * <pre>
	 * 	Task	dur.	es.		ef.		ls.		lf.		slack	CriticalPath?
	 * 	A		3		0		3		0		3		0		true
	 * 	B		4		3		3		7		7		4		false
	 * 	C		2		3		5		5		7		2		false
	 * 	D		5		7		7		12		12		0		true
	 * </pre>
	 * @param target
	 * @param tableResults
	 */
	Boolean withTimeLineTable(TimelineTable target, String tableResults) {
		List<String> resultList = tableResults.trim().stripIndent().split('\n')
		assert target.vertices.size() == resultList.size() - 1

		resultList.takeRight(resultList.size() - 1).eachWithIndex { String row, int index ->
			List<String> rowValues = row.trim().split('\\s+').toList()
			TaskVertex vertex = target.vertices.find { it.taskComment == rowValues[0] }

			assert vertex
			assert rowValues[1].toInteger() == vertex.duration, "Incorrect duration for row number $index"
			assert rowValues[2].toInteger() == vertex.earliestStart, "Incorrect earliestStart for row number $index"
			assert rowValues[3].toInteger() == vertex.earliestFinish, "Incorrect earliestFinish for row number $index"
			assert rowValues[4].toInteger() == vertex.latestStart, "Incorrect latestStart for row number $index"
			assert rowValues[5].toInteger() == vertex.latestFinish, "Incorrect latestFinish for row number $index"
			assert rowValues[6].toInteger() == vertex.getSlack(), "Incorrect slack for row number $index"
			assert new Boolean(rowValues[7]) == vertex.isCriticalPath(), "Incorrect isCriticalPath() for row number $index"
		}
		return true
	}

}