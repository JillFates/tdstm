package net.transitionmanager.task.cpm

class DirectedGraphTestHelper {

	/*
									 +-+
									 |D|
						  +--------+>+-+------------+
						  |          |5|            v
						 +++         +++           +-+
						 |B|                       |G|
			   +-------+>+-+          +----------+>+-+
			   |         |4|          |            |4|
			  +++        +-+         +++           +-+
			  |A|                    |E|            +    +-+
			  +-+         +--------+>+-+            +--->|H|
			  |3|         |          |1|                 +-+
			  +++        +++         +-+            +--->|3|
			   |         |C|                        |    +-+
			   +-------+>+-+                        |
						 |2|                        |
						 +++         +-+            |
						  |          |F|            |
						  +--------+>+-+------------+
									 |2|
									 +-+
	 */

	DirectedGraph createAcyclicDirectedGraph() {
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

		return new DirectedGraph([A, B, C, D, E, F, G, H])
	}

	/*
		  +++       +++
		  |0|+----->|1|
		  +-+       +++
		  ^ |        |
		  | |   +----+
		  | v   |
		  +++<--+   +++
		  |2|       |3|
		  +-++----->+-+
	 */

	DirectedGraph createCyclicDirectedGraph() {
		Activity Activity0 = new Activity(taskId: '0', duration: 3)
		Activity Activity1 = new Activity(taskId: '1', duration: 4)
		Activity Activity2 = new Activity(taskId: '2', duration: 2)
		Activity Activity3 = new Activity(taskId: '3', duration: 5)

		Activity0.addPredecessor(Activity1)
		Activity0.addPredecessor(Activity2)
		Activity2.addPredecessor(Activity0) // Cyclic edge
		Activity1.addPredecessor(Activity2)
		Activity2.addPredecessor(Activity3)

		return new DirectedGraph([Activity0, Activity1, Activity2, Activity3])
	}

	/*
			  +++       +++
			  |0|+----->|1|
			  +++       +++
			   |         |
			   |    +----+
			   v    |
			  +++   |   +++
			  |2|<--+   |3|<--+
			  +-+------>+-+   |
						 |    |
						 +----+
	 */
	DirectedGraph createCyclicDirectedGraphWithSelfLoop() {
		Activity Activity0 = new Activity(taskId: '0', duration: 3)
		Activity Activity1 = new Activity(taskId: '1', duration: 4)
		Activity Activity2 = new Activity(taskId: '2', duration: 2)
		Activity Activity3 = new Activity(taskId: '3', duration: 5)

		Activity0.addPredecessor(Activity1)
		Activity0.addPredecessor(Activity2)
		Activity1.addPredecessor(Activity2)
		Activity2.addPredecessor(Activity3)
		Activity3.addPredecessor(Activity3) // Cyclic by self-loop

		return new DirectedGraph([Activity0, Activity1, Activity2, Activity3])
	}
}
