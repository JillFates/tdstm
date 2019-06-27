package net.transitionmanager.task.timeline

import spock.lang.Specification

class CriticalPathSpec extends Specification {

	void 'test 1'() {
		given:
			//The example dependency graph from
			//http://www.ctl.ua.edu/math103/scheduling/scheduling_algorithms.htm
			HashSet<Task> allTasks = new HashSet<Task>()
			Task end = new Task("End", 0)
			Task F = new Task("F", 2, end)
			Task A = new Task("A", 3, end)
			Task X = new Task("X", 4, F, A)
			Task Q = new Task("Q", 2, A, X)
			Task start = new Task("Start", 0, Q)
			allTasks.add(end)
			allTasks.add(F)
			allTasks.add(A)
			allTasks.add(X)
			allTasks.add(Q)
			allTasks.add(start)
		when:
			Task[] results = CriticalPath.criticalPath(allTasks)
			println("Critical Path: " + Arrays.toString(results));
		then:
			results.size() == 6

	}

	void 'test 2'() {
		given:
			//The example dependency graph from
			//http://www.ctl.ua.edu/math103/scheduling/scheduling_algorithms.htm
			HashSet<Task> allTasks = new HashSet<Task>()
			Task end = new Task("End", 0)
			Task D = new Task("D", 5, end)
			Task C = new Task("C", 2, D)
			Task B = new Task("B", 4, D)
			Task A = new Task("A", 3, B, C)
			Task start = new Task("Start", 0, A)
			allTasks.add(end)
			allTasks.add(D)
			allTasks.add(C)
			allTasks.add(B)
			allTasks.add(A)
			allTasks.add(start)
		when:
			Task[] results = CriticalPath.criticalPath(allTasks)
			println("Critical Path: " + Arrays.toString(results));
		then:
			results.size() == 6

	}
}

class Task {
	//the actual cost of the task
	int cost;
	//the cost of the task along the critical path
	int criticalCost;
	//a name for the task for printing
	String name;
	//the tasks on which this task is dependant
	HashSet<Task> dependencies = new HashSet<Task>();

	Task(String name, int cost, Task... dependencies) {
		this.name = name;
		this.cost = cost;
		for (Task t : dependencies) {
			this.dependencies.add(t);
		}
	}

	@Override
	String toString() {
		return name + ": " + criticalCost;
	}

	boolean isDependent(Task t) {
		//is t a direct dependency?
		if (dependencies.contains(t)) {
			return true;
		}
		//is t an indirect dependency
		for (Task dep : dependencies) {
			if (dep.isDependent(t)) {
				return true;
			}
		}
		return false;
	}
}

class CriticalPath {

	//A wrapper class to hold the tasks during the calculation
	static Task[] criticalPath(Set<Task> tasks) {
		//tasks whose critical cost has been calculated
		HashSet<Task> completed = new HashSet<Task>();
		//tasks whose ciritcal cost needs to be calculated
		HashSet<Task> remaining = new HashSet<Task>(tasks);

		//Backflow algorithm
		//while there are tasks whose critical cost isn't calculated.
		while (!remaining.isEmpty()) {
			boolean progress = false;

			//find a new task to calculate
			for (Iterator<Task> it = remaining.iterator(); it.hasNext();) {
				Task task = it.next();
				if (completed.containsAll(task.dependencies)) {
					//all dependencies calculated, critical cost is max dependency
					//critical cost, plus our cost
					int critical = 0;
					for (Task t : task.dependencies) {
						if (t.criticalCost > critical) {
							critical = t.criticalCost;
						}
					}
					task.criticalCost = critical + task.cost;
					//set task as calculated an remove
					completed.add(task);
					it.remove();
					//note we are making progress
					progress = true;
				}
			}
			//If we haven't made any progress then a cycle must exist in
			//the graph and we wont be able to calculate the critical path
			if (!progress) throw new RuntimeException("Cyclic dependency, algorithm stopped!");
		}

		//get the tasks
		Task[] ret = completed.toArray(new Task[0]);
		//create a priority list
		Arrays.sort(ret, new Comparator<Task>() {

			@Override
			int compare(Task o1, Task o2) {
				//sort by cost
				int i = o2.criticalCost - o1.criticalCost;
				if (i != 0) return i;

				//using dependency as a tie breaker
				//note if a is dependent on b then
				//critical cost a must be >= critical cost of b
				if (o1.isDependent(o2)) return -1;
				if (o2.isDependent(o1)) return 1;
				return 0;
			}
		});

		return ret;
	}
}
