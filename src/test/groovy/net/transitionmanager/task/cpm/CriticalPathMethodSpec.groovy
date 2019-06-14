package net.transitionmanager.task.cpm

import spock.lang.Specification

class CriticalPathMethodSpec extends Specification {

	void 'test can calculate critical path method for a list of activities'() {
		setup:
			Activity A = new Activity(taskId: 'A', duration: 3)
			Activity B = new Activity(taskId: 'B', duration: 4)
			Activity C = new Activity(taskId: 'C', duration: 2)
			Activity D = new Activity(taskId: 'D', duration: 5)
			Activity E = new Activity(taskId: 'E', duration: 1)
			Activity F = new Activity(taskId: 'F', duration: 2)
			Activity G = new Activity(taskId: 'G', duration: 4)
			Activity H = new Activity(taskId: 'H', duration: 3)

			B.addPredecessor(A)
			C.addPredecessor(A)
			D.addPredecessor(B)
			E.addPredecessor(C)
			F.addPredecessor(C)
			G.addPredecessor(D)
			G.addPredecessor(E)
			H.addPredecessor(F)
			H.addPredecessor(G)

			List<Activity> activities = [A, B, C, D, E, F, G, H]

		when:
			List<Activity> criticalPath = CriticalPathMethod.calculate(activities)

		then:
			!criticalPath.isEmpty()
			criticalPath.size() == 5
			criticalPath.collect { it.taskId } == ['A', 'B', 'D', 'G', 'H']

	}
}
