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
								 +-+
								 |D|
					  +--------+>+-+------------+
					  |          |5|            v
					 +++         +++           +-+
					 |B|                       |G|
		   +-------+>+-+          +----------+>+-+
		   |         |4|          |            |4|
		  +++        +-+         +++           +-+
		  |A|                    |E|
		  +-+         +--------+>+-+
		  |3|         |          |1|
		  +++        +++         +-+            +-+
		   |         |C|                        |H|
		   +-------+>+-+                   + -> +-+
					 |2|                   |    |3|
					 +++         +-+       |    +-+
					  |          |F|       |
					  +--------+>+-+-------+
								 |2|
								 +-+
 	*/

	DirectedGraph createAcyclicDirectedGraphWithOneSourceAndTwoSinks() {
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

	DirectedGraph createTaskAndDependenciesExample() {

		Activity taskA = new Activity(taskId: '1', description: 'A', duration: 5)
		Activity taskB = new Activity(taskId: '2', description: 'B', duration: 2)
		Activity taskC = new Activity(taskId: '3', description: 'C', duration: 8)
		Activity taskD = new Activity(taskId: '4', description: 'D', duration: 6)
		Activity taskE = new Activity(taskId: '5', description: 'E', duration: 10)
		Activity taskF = new Activity(taskId: '6', description: 'F', duration: 7)
		Activity taskG = new Activity(taskId: '7', description: 'G', duration: 1)
		Activity taskH = new Activity(taskId: '8', description: 'H', duration: 3)
		Activity taskI = new Activity(taskId: '9', description: 'I', duration: 4)
		Activity taskJ = new Activity(taskId: '10', description: 'J', duration: 1)
		Activity taskK = new Activity(taskId: '11', description: 'K', duration: 5)
		Activity taskL = new Activity(taskId: '12', description: 'L', duration: 4)
		Activity taskM = new Activity(taskId: '13', description: 'M', duration: 3)
		Activity taskN = new Activity(taskId: '14', description: 'N', duration: 3)
		Activity taskO = new Activity(taskId: '15', description: 'O', duration: 5)
		Activity taskP = new Activity(taskId: '16', description: 'P', duration: 9)
		Activity taskQ = new Activity(taskId: '17', description: 'Q', duration: 1)
		Activity taskR = new Activity(taskId: '18', description: 'R', duration: 5)
		Activity taskS = new Activity(taskId: '19', description: 'S', duration: 6)

		DirectedGraph directedGraph = new DirectedGraph([
			taskA, taskB, taskC, taskD, taskE, taskF, taskG, taskH, taskI, taskJ,
			taskK, taskL, taskM, taskN, taskO, taskP, taskQ, taskR, taskS
		])

		// Edges E
		directedGraph
			.addEdge(taskE, taskO)
			.addEdge(taskE, taskH)

		// Edges A
		directedGraph
			.addEdge(taskA, taskH)

		// Edges B
		directedGraph
			.addEdge(taskB, taskI)

		// Edges F
		directedGraph
			.addEdge(taskF, taskD)
			.addEdge(taskF, taskG)

		// Edges C
		directedGraph
			.addEdge(taskC, taskK)
			.addEdge(taskC, taskS)

		// Edges H
		directedGraph
			.addEdge(taskH, taskL)
			//.addEdge(taskH, taskI) // Cycle (D -> H -> I -> D)

		// Edges I
		directedGraph
			.addEdge(taskI, taskD)

		// Edges G
		directedGraph
			.addEdge(taskG, taskD)
			.addEdge(taskG, taskJ)

		// Edges D
		directedGraph
			.addEdge(taskD, taskH)
			.addEdge(taskD, taskL)
			.addEdge(taskD, taskN)
			.addEdge(taskD, taskK)

		// Edges L
		directedGraph
			.addEdge(taskL, taskO)

		// Edges J
		directedGraph
			.addEdge(taskJ, taskN)

		// Edges K
		directedGraph
			.addEdge(taskK, taskM)

		// Edges M
		directedGraph
			.addEdge(taskM, taskQ)
			.addEdge(taskM, taskP)

//		// Edges P
//		directedGraph
//			.addEdge(taskP, taskk) // Cycle (P -> K -> M -> P)


		return directedGraph
	}
}
