package net.transitionmanager.task.cpm

class TaskTimeLineCalculator {

	/**
	 * The {@code TaskTimeLineCalculator#walkListForward} method receives the array that stores the vertices
	 * and performs the forward walking inside the activity list calculating
	 * for each activity its earliest start time and earliest end time.
	 * @param directedGraph
	 * @return
	 */
	private static List<TaskVertex> walkListForward(TaskVertex source, TaskTimeLineGraph directedGraph) {

		TaskVertex sourceActivity = directedGraph.getStart()

		sourceActivity.earliestEndTime = sourceActivity.earliestStartTime + sourceActivity.duration

		(directedGraph.vertices - source).each { TaskVertex activity ->

			activity.predecessors.each { TaskVertex predecessor ->

				if (activity.earliestStartTime < predecessor.earliestEndTime) {
					activity.earliestStartTime = predecessor.earliestEndTime
				}
			}
			activity.earliestEndTime = activity.earliestStartTime + activity.duration
		}

		return directedGraph.vertices
	}
	/**
	 * 	After the forward walking the {@code TaskTimeLineCalculator#walkListForward}
	 * 	performs the backward walking calculating for each activity its latest start time and latest end time.
	 * @param directedGraph
	 * @return
	 */
	private static List<TaskVertex> walkListBackwards(TaskVertex sink, TaskTimeLineGraph directedGraph) {

		sink.latestEndTime = sink.earliestEndTime
		sink.latestStartTime = sink.latestEndTime - sink.duration

		(directedGraph.vertices - sink).reverseEach { TaskVertex activity ->

			activity.successors.each { TaskVertex successor ->

				if (activity.latestEndTime == 0) {
					activity.latestEndTime = successor.latestStartTime
				} else if (activity.latestEndTime > successor.latestStartTime) {
					activity.latestEndTime = successor.latestStartTime
				}

				activity.latestStartTime = activity.latestEndTime - activity.duration
			}
		}

		return directedGraph.vertices
	}


	static List<TaskVertex> calculate(TaskTimeLineGraph directedGraph) {
		List<TaskVertex> criticalPath = []

		TaskVertex source = directedGraph.getStart()
		TaskVertex sink = directedGraph.getSink()

		directedGraph.vertices = walkListForward(source, directedGraph)
		directedGraph.vertices = walkListBackwards(sink, directedGraph)

		return directedGraph.vertices.findAll { TaskVertex activity ->
			// TODO: dcorrea refactor this code
			activity.taskId == TaskVertex.BINDER_START_NODE ||
			(activity.earliestEndTime - activity.latestEndTime == 0) && (activity.earliestStartTime - activity.latestStartTime == 0)
		}
	}


}
