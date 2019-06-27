package net.transitionmanager.task.timeline

import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.task.TaskTimeLineGraphTopologicalSort

class TaskTimeLineCalculator {


	/**
	 * The {@code TaskTimeLineCalculator#walkListForward} method receives the array that stores the vertices
	 * and performs the forward walking inside the activity list calculating
	 * for each activity its earliest start time and earliest end time.
	 * @param directedGraph
	 * @return
	 */
	private static Set<TaskVertex> walkListForward(TaskVertex start, TaskTimeLineGraph directedGraph) {

		Queue<TaskVertex> queue = [] as Queue<TaskVertex>
		queue.add(start)

		while (!queue.isEmpty()) {
			TaskVertex currentVertex = queue.poll()

			for (TaskVertex predecessor : currentVertex.predecessors) {

				if (currentVertex.earliestStartTime < predecessor.earliestEndTime) {
					currentVertex.earliestStartTime = predecessor.earliestEndTime
				}
			}
			currentVertex.earliestEndTime = currentVertex.earliestStartTime + currentVertex.duration

			for (TaskVertex successor : currentVertex.successors) {
				if (!queue.contains(successor)) {
					queue.add(successor)
				}
			}
		}
		return directedGraph.vertices
	}
	/**
	 * 	After the forward walking the {@code TaskTimeLineCalculator#walkListForward}
	 * 	performs the backward walking calculating for each activity its latest start time and latest end time.
	 * @param directedGraph
	 * @return
	 */
	private static Set<TaskVertex> walkListBackward(TaskVertex sink, TaskTimeLineGraph directedGraph) {

		sink.latestEndTime = sink.earliestEndTime
		//sink.latestStartTime = sink.latestEndTime - sink.duration

		Queue<TaskVertex> queue = [] as Queue<TaskVertex>
		queue.add(sink)

		while (!queue.isEmpty()) {
			TaskVertex currentVertex = queue.poll()

			for (TaskVertex successor : currentVertex.successors) {
				if (currentVertex.latestEndTime == 0) {
					currentVertex.latestEndTime = successor.latestStartTime
				} else if (currentVertex.latestEndTime > successor.latestStartTime) {
					currentVertex.latestEndTime = successor.latestStartTime
				}
			}
			currentVertex.latestStartTime = currentVertex.latestEndTime - currentVertex.duration

			for (TaskVertex predecessor : currentVertex.predecessors) {
				if (!queue.contains(predecessor)) {
					queue.add(predecessor)
				}
			}
		}

//		(directedGraph.vertices - sink).reverseEach { TaskVertex activity ->
//			activity.successors.each { TaskVertex successor ->
//
//				if (activity.latestEndTime == 0) {
//					activity.latestEndTime = successor.latestStartTime
//				} else if (activity.latestEndTime > successor.latestStartTime) {
//					activity.latestEndTime = successor.latestStartTime
//				}
//				activity.latestStartTime = activity.latestEndTime - activity.duration
//			}
//		}

		return directedGraph.vertices
	}

	/**
	 *
	 * @param timeLineGraph
	 * @return
	 */
	static Set<TaskVertex> calculate(TaskTimeLineGraph timeLineGraph) {

		TaskTimeLineGraphCycleFinder cycleFinder = new TaskTimeLineGraphCycleFinder(timeLineGraph)
		if (cycleFinder.hasCycle()) {
			throw new InvalidParamException("TaskTimeLineGraph contains cycles: ${cycleFinder.cycles}")
		}

		TaskTimeLineGraphTopologicalSort sort = new TaskTimeLineGraphTopologicalSort(timeLineGraph)
		if (!sort.hasOrder()){
			throw new InvalidParamException("Cannot order TaskTimeLineGrap vertices")
		}

		List<TaskVertex> criticalPath = []
		TaskVertex source = timeLineGraph.getStart()
		TaskVertex sink = timeLineGraph.getSink()

		//directedGraph.vertices = sortVertexList(source, directedGraph)
		timeLineGraph.vertices = walkListForward(source, timeLineGraph)
		timeLineGraph.vertices = walkListBackward(sink, timeLineGraph)

		// TODO: dcorrea refactor this code to return Critical Path
		// in order starting by Starter Vertex
		return timeLineGraph.vertices.findAll { TaskVertex activity ->
			activity.taskId == TaskVertex.BINDER_START_NODE || activity.taskId == TaskVertex.BINDER_SINK_NODE ||
				(activity.earliestEndTime - activity.latestEndTime == 0) && (activity.earliestStartTime - activity.latestStartTime == 0)
		}
	}


	static List<TaskVertex> sortVertexList(TaskVertex start, TaskTimeLineGraph taskTimeLineGraph) {

		HashSet<TaskVertex> stack = new HashSet<TaskVertex>();
		stack.add(start)

		return taskTimeLineGraph.vertices.sort { TaskVertex a, TaskVertex b ->
			if (a.isSuccessor(b)) {
				return -1
			} else if (b.isSuccessor(a)) {
				return 1
			} else {
				return 0
			}
		}
	}

	static List<TaskVertex> apply(TaskTimeLineGraph directedGraph) {

		//tasks whose critical cost has been calculated
		HashSet<TaskVertex> completed = new HashSet<TaskVertex>()
		//tasks whose ciritcal cost needs to be calculated
		HashSet<TaskVertex> remaining = directedGraph.vertices.toSet()

		//Backflow algorithm
		//while there are tasks whose critical cost isn't calculated.
		while (!remaining.isEmpty()) {
			boolean progress = false

			for (Iterator<TaskVertex> it = remaining.iterator(); it.hasNext();) {
				TaskVertex task = it.next()

				if (completed.containsAll(task.successors)) {
					//all dependencies calculated, critical cost is max dependency
					//critical cost, plus our cost
					int critical = 0
					for (TaskVertex successor : task.successors) {

						if (successor.criticalCost > critical) {
							critical = task.criticalCost
						}
					}

					task.criticalCost = critical + task.duration
					//set task as calculated an remove
					completed.add(task)
					it.remove()
					//note we are making progress
					progress = true
				}
			}

			//If we haven't made any progress then a cycle must exist in
			//the graph and we wont be able to calculate the critical path
			if (!progress) throw new RuntimeException("Cyclic dependency, algorithm stopped!")
		}


		// get the cost
		int maxCost = maxCost(directedGraph.vertices.toSet());
		HashSet<TaskVertex> initialNodes = initials(directedGraph.vertices.toSet());
		calculateEarly(initialNodes);


		// get the tasks
		TaskVertex[] ret = completed.toArray(new TaskVertex[0]);
		// create a priority list
		Arrays.sort(ret, new Comparator<TaskVertex>() {

			@Override
			int compare(TaskVertex o1, TaskVertex o2) {
				return o1.taskId.compareTo(o2.taskId);
			}
		});


		String format = '%1$-10s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s %7$-10s\n'
		System.out.format(format, "Task", "ES", "EF", "LS", "LF", "Slack", "Critical?")
		for (TaskVertex t : ret)
			System.out.format(format, (Object[]) t.toStringArray());

		return ret;
	}

	static void setEarly(TaskVertex initial) {
		int completionTime = initial.earliestStartTime
		for (TaskVertex t : initial.successors) {
			if (completionTime >= t.earliestStartTime) {
				t.earliestStartTime = completionTime
				t.earliestEndTime = completionTime + t.duration
			}
			setEarly(t)
		}
	}

	static void calculateEarly(HashSet<TaskVertex> initials) {
		for (TaskVertex initial : initials) {
			initial.earliestStartTime = 0
			initial.earliestEndTime = initial.duration
			setEarly(initial)
		}
	}

	static HashSet<TaskVertex> initials(Set<TaskVertex> tasks) {
		HashSet<TaskVertex> remaining = new HashSet<TaskVertex>(tasks);
		for (TaskVertex t : tasks) {
			for (TaskVertex td : t.successors) {
				remaining.remove(td)
			}
		}

		print("Initial nodes: ")
		for (TaskVertex t : remaining)
			System.out.print(t.taskId + " ")
		System.out.print("\n\n")
		return remaining
	}

	static int maxCost(Set<TaskVertex> tasks) {
		int max = -1
		for (TaskVertex t : tasks) {
			if (t.criticalCost > max)
				max = t.criticalCost
		}
		println("Critical path length (cost): " + max);
		for (TaskVertex t : tasks) {
			t.setLatest(max)
		}

		return max
	}

}
