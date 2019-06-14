package net.transitionmanager.task.cpm

class DirectedGraph {

	final List<Activity> activities

	DirectedGraph(List<Activity> activities) {
		this.activities = activities
	}

	/**
	 * Check whether the graph contains a cycle or not
	 * using DFS solution
	 * Backedge: an edge that is from a node to itself (selfloop)
	 * or one of its ancestors
	 *
	 * @return true if this {@code DirectedGraph} contains a cycle
	 * 		and false in all the other cases.
	 */
	Boolean isCyclic(){

		List<ActivityWrapper> wrappedActivities = activities.collect {Activity activity ->
			return new ActivityWrapper(activity)
		}

		wrappedActivities.each { ActivityWrapper activityWrapper ->
			if (isCyclic())
		}

		return false
	}
}

class ActivityWrapper {
	final Activity activity
	Boolean visited

	ActivityWrapper(Activity activity) {
		this.activity = activity
	}

}
