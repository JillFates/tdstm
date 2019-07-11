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
	 * 	Task	dur.	es.		ef.		ls.		lf.		CriticalPath?
	 * 	A		3		0		3		0		3		true
	 * 	B		4		3		3		7		7		true
	 * 	C		2		3		5		5		7		false
	 * 	D		5		7		7		12		12		true
	 * </pre>
	 * @param target
	 * @param tableResults
	 */
	Boolean withTimeLineTable(TimelineTable target, String tableResults) {
		List<String> resultList = tableResults.trim().stripIndent().split('\n')
		assert target.nodesMap.size() == resultList.size() - 1

		resultList.takeRight(resultList.size() - 1).eachWithIndex { String row, int index ->
			List<String> rowValues = row.trim().split('\t\t').toList()
			Map.Entry<TaskVertex, TimelineNode> entry = target.nodesMap.find { it.key.taskNumber == rowValues[0] }
			TaskVertex vertex = entry.key
			TimelineNode timelineNode = entry.value

			assert timelineNode
			assert rowValues[1].toInteger() == vertex.duration, "Incorrect duration for row number $index"
			assert rowValues[2].toInteger() == timelineNode.earliestStart, "Incorrect earliestStart for row number $index"
			assert rowValues[3].toInteger() == timelineNode.earliestFinish, "Incorrect earliestFinish for row number $index"
			assert rowValues[4].toInteger() == timelineNode.latestStart, "Incorrect latestStart for row number $index"
			assert rowValues[5].toInteger() == timelineNode.latestFinish, "Incorrect latestFinish for row number $index"
		}
		return true
	}

}