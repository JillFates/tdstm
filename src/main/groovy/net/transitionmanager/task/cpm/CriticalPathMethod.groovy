package net.transitionmanager.task.cpm

class CriticalPathMethod {

	/**
	 * The {@code CriticalPathMethod#walkListAhead} method receives the array that stores the vertices
	 * and performs the forward walking inside the activity list calculating
	 * for each activity its earliest start time and earliest end time.
	 * @param directedGraph
	 * @return
	 */
	private static List<TaskTimeLineVertex> walkListAhead(TaskTimeLineVertex source, TaskTimeLineGraph directedGraph) {

		TaskTimeLineVertex sourceActivity = directedGraph.getSource()

		sourceActivity.eet = sourceActivity.est + sourceActivity.duration

		(directedGraph.vertices - source).each { TaskTimeLineVertex activity ->

			activity.predecessors.each { TaskTimeLineVertex predecessor ->

				if (activity.est < predecessor.eet) {
					activity.est = predecessor.eet
				}
			}
			activity.eet = activity.est + activity.duration
		}

		return directedGraph.vertices
	}
	/**
	 * 	After the forward walking the {@code CriticalPathMethod#walkListAhead}
	 * 	performs the backward walking calculating for each activity its latest start time and latest end time.
	 * @param directedGraph
	 * @return
	 */
	private static List<TaskTimeLineVertex> walkListAback(TaskTimeLineVertex sink, TaskTimeLineGraph directedGraph) {

		sink.let = sink.eet
		sink.lst = sink.let - sink.duration

		(directedGraph.vertices - sink).reverseEach { TaskTimeLineVertex activity ->

			activity.successors.each { TaskTimeLineVertex successor ->

				if (activity.let == 0) {
					activity.let = successor.lst
				} else if (activity.let > successor.lst) {
					activity.let = successor.lst
				}

				activity.lst = activity.let - activity.duration
			}
		}

		return directedGraph.vertices
	}


	static List<TaskTimeLineVertex> calculate(TaskTimeLineGraph directedGraph) {
		List<TaskTimeLineVertex> criticalPath = []

		TaskTimeLineVertex source = directedGraph.getSource()
		TaskTimeLineVertex sink = directedGraph.getSink()

		directedGraph.vertices = walkListAhead(source, directedGraph)
		directedGraph.vertices = walkListAback(sink, directedGraph)

		return directedGraph.vertices.findAll { TaskTimeLineVertex activity ->
			// TODO: dcorrea refactor this code
			activity.taskId == TaskTimeLineVertex.HIDDEN_SOURCE_NODE ||
			(activity.eet - activity.let == 0) && (activity.est - activity.lst == 0)
		}
	}


}
