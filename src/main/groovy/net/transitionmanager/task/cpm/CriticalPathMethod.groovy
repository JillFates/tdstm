package net.transitionmanager.task.cpm

class CriticalPathMethod {

	/**
	 * The {@code CriticalPathMethod#walkListAhead} method receives the array that stores the activities
	 * and performs the forward walking inside the activity list calculating
	 * for each activity its earliest start time and earliest end time.
	 * @param directedGraph
	 * @return
	 */
	private static List<Activity> walkListAhead(DirectedGraph directedGraph) {

		Activity sourceActivity = directedGraph.getSource()
		List<Activity> activities = directedGraph.activities

		sourceActivity.eet = sourceActivity.est + sourceActivity.duration

		activities.subList(1, activities.size()).each { Activity activity ->

			activity.predecessors.each { Activity predecessor ->

				if (activity.est < predecessor.eet) {
					activity.est = predecessor.eet
				}
			}
			activity.eet = activity.est + activity.duration
		}

		return activities
	}
	/**
	 * 	After the forward walking the {@code CriticalPathMethod#walkListAhead}
	 * 	performs the backward walking calculating for each activity its latest start time and latest end time.
	 * @param directedGraph
	 * @return
	 */
	private static List<Activity> walkListAback(DirectedGraph directedGraph) {

		List<Activity> activities = directedGraph.activities
		Activity sink = directedGraph.getSink()
		sink.let = sink.eet
		sink.lst = sink.let - sink.duration

		activities.subList(0, activities.size() - 1).reverseEach { Activity activity ->

			activity.successors.each { Activity successor ->

				if (activity.let == 0) {
					activity.let = successor.lst
				} else if (activity.let > successor.lst) {
					activity.let = successor.lst
				}

				activity.lst = activity.let - activity.duration
			}
		}

		return activities;
	}


	static List<Activity> calculate(DirectedGraph directedGraph) {
		List<Activity> criticalPath = []

		directedGraph.activities = walkListAhead(directedGraph)
		directedGraph.activities = walkListAback(directedGraph)

		return directedGraph.activities.findAll { Activity activity ->
			// TODO: dcorrea refactor this code
			activity.taskId == Activity.HIDDEN_SOURCE_NODE ||
			(activity.eet - activity.let == 0) && (activity.est - activity.lst == 0)
		}
	}


}
