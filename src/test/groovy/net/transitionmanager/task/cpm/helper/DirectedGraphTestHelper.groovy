package net.transitionmanager.task.cpm.helper

import net.transitionmanager.task.cpm.Activity
import net.transitionmanager.task.cpm.DirectedGraph

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

	DirectedGraph createAcyclicDirectedGraphWithOneSourceAndOneSink() {
		Activity A = new Activity(taskId: 'A', duration: 3)
		Activity B = new Activity(taskId: 'B', duration: 4)
		Activity C = new Activity(taskId: 'C', duration: 2)
		Activity D = new Activity(taskId: 'D', duration: 5)
		Activity E = new Activity(taskId: 'E', duration: 1)
		Activity F = new Activity(taskId: 'F', duration: 2)
		Activity G = new Activity(taskId: 'G', duration: 4)
		Activity H = new Activity(taskId: 'H', duration: 3)

		DirectedGraph directedGraph = new DirectedGraph([A, B, C, D, E, F, G, H])
		directedGraph
			.addEdge(A, B)
			.addEdge(A, C)
			.addEdge(B, D)
			.addEdge(C, E)
			.addEdge(C, F)
			.addEdge(D, G)
			.addEdge(E, G)
			.addEdge(G, H)
			.addEdge(F, H)

		return directedGraph
	}

	/*
								 +-+
								 |D|
					  +--------+>+-+------------+
					  |          |5|            v
					 +++         +++           +-+
					 |B|                       |G|
		   			 +-+          +----------+>+-+
		             |4|          |            |4|
		             +-+         +++           +-+
		                         |E|            +    +-+
		              +--------+>+-+            +--->|H|
		              |          |1|                 +-+
		             +++         +-+            +--->|3|
		             |C|                        |    +-+
		      		 +-+                        |
					 |2|                        |
					 +++         +-+            |
					  |          |F|            |
					  +--------+>+-+------------+
								 |2|
								 +-+
 	*/

	DirectedGraph createAcyclicDirectedGraphWithTwoSourcesAndOneSink() {
		Activity B = new Activity(taskId: 'B', duration: 4)
		Activity C = new Activity(taskId: 'C', duration: 2)
		Activity D = new Activity(taskId: 'D', duration: 5)
		Activity E = new Activity(taskId: 'E', duration: 1)
		Activity F = new Activity(taskId: 'F', duration: 2)
		Activity G = new Activity(taskId: 'G', duration: 4)
		Activity H = new Activity(taskId: 'H', duration: 3)

		DirectedGraph directedGraph = new DirectedGraph([B, C, D, E, F, G, H])
		directedGraph
			.addEdge(B, D)
			.addEdge(C, E)
			.addEdge(C, F)
			.addEdge(D, G)
			.addEdge(E, G)
			.addEdge(G, H)
			.addEdge(F, H)

		return directedGraph
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

		DirectedGraph directedGraph = new DirectedGraph([Activity0, Activity1, Activity2, Activity3])

		directedGraph
			.addEdge(Activity0, Activity1)
			.addEdge(Activity0, Activity2)
			.addEdge(Activity1, Activity2)
			.addEdge(Activity2, Activity1)// Cyclic edge
			.addEdge(Activity2, Activity3)

		return directedGraph
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

		DirectedGraph directedGraph = new DirectedGraph([Activity0, Activity1, Activity2, Activity3])

		directedGraph
			.addEdge(Activity0, Activity1)
			.addEdge(Activity0, Activity2)
			.addEdge(Activity1, Activity2)
			.addEdge(Activity2, Activity3)
			.addEdge(Activity3, Activity3)// Cyclic edge

		return directedGraph
	}
}
