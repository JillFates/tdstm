package net.transitionmanager.task.timeline

trait TaskTimeLineDataTest {

	/*
		TaskVertex Structure definition, compound with TaskNode
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
	 * 	Task	dur.	est.	eed.	lst.	led.	CriticalPath?
	 * 	A		3		0		3		0		3		true
	 * 	B		4		3		3		7		7		true
	 * 	C		2		3		5		5		7		false
	 * 	D		5		7		7		12		12		true
	 * </pre>
	 * @param target
	 * @param tableResults
	 */
	void withTaskTimeLineGraph(TaskTimeLineGraph target, String tableResults) {
		List<String> resultList = tableResults.trim().stripIndent().split('\n')
		assert target.V() == resultList.size() - 1

		resultList.takeRight(resultList.size() - 1).eachWithIndex { String row, int index ->
			List<String> rowValues = row.trim().split('\t\t').toList()
			assert rowValues[0] == target.vertices[index].taskNumber
			assert rowValues[1].toInteger() == target.vertices[index].duration
			assert rowValues[2].toInteger() == target.vertices[index].earliestStartTime
			assert rowValues[3].toInteger() == target.vertices[index].earliestEndTime
			assert rowValues[4].toInteger() == target.vertices[index].latestStartTime
			assert rowValues[5].toInteger() == target.vertices[index].latestEndTime
			assert new Boolean(rowValues[6]) == target.vertices[index].isCriticalPath()
		}
	}

}