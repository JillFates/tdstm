package net.transitionmanager.task.cpm.helper


import net.transitionmanager.task.cpm.TaskTimeLineGraph
import net.transitionmanager.task.cpm.TaskTimeLineVertex

class TaskTimeLineGraphTestHelper {

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

	TaskTimeLineGraph createAcyclicDirectedGraphWithOneSourceAndOneSink() {
		TaskTimeLineVertex A = TaskTimeLineVertex.Factory.newSimpleVertex('A', 3)
		TaskTimeLineVertex B = TaskTimeLineVertex.Factory.newSimpleVertex('B', 4)
		TaskTimeLineVertex C = TaskTimeLineVertex.Factory.newSimpleVertex('C', 2)
		TaskTimeLineVertex D = TaskTimeLineVertex.Factory.newSimpleVertex('D', 5)
		TaskTimeLineVertex E = TaskTimeLineVertex.Factory.newSimpleVertex('E', 1)
		TaskTimeLineVertex F = TaskTimeLineVertex.Factory.newSimpleVertex('F', 2)
		TaskTimeLineVertex G = TaskTimeLineVertex.Factory.newSimpleVertex('G', 4)
		TaskTimeLineVertex H = TaskTimeLineVertex.Factory.newSimpleVertex('H', 3)

		TaskTimeLineGraph directedGraph = new TaskTimeLineGraph([A, B, C, D, E, F, G, H])
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

	TaskTimeLineGraph createAcyclicDirectedGraphWithTwoSourcesAndOneSink() {
		TaskTimeLineVertex B = TaskTimeLineVertex.Factory.newSimpleVertex('B', 4)
		TaskTimeLineVertex C = TaskTimeLineVertex.Factory.newSimpleVertex('C', 2)
		TaskTimeLineVertex D = TaskTimeLineVertex.Factory.newSimpleVertex('D', 5)
		TaskTimeLineVertex E = TaskTimeLineVertex.Factory.newSimpleVertex('E', 1)
		TaskTimeLineVertex F = TaskTimeLineVertex.Factory.newSimpleVertex('F', 2)
		TaskTimeLineVertex G = TaskTimeLineVertex.Factory.newSimpleVertex('G', 4)
		TaskTimeLineVertex H = TaskTimeLineVertex.Factory.newSimpleVertex('H', 3)

		TaskTimeLineGraph directedGraph = new TaskTimeLineGraph([B, C, D, E, F, G, H])
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

	TaskTimeLineGraph createAcyclicDirectedGraphWithOneSourceAndTwoSinks() {
		TaskTimeLineVertex A = TaskTimeLineVertex.Factory.newSimpleVertex('A', 3)
		TaskTimeLineVertex B = TaskTimeLineVertex.Factory.newSimpleVertex('B', 4)
		TaskTimeLineVertex C = TaskTimeLineVertex.Factory.newSimpleVertex('C', 2)
		TaskTimeLineVertex D = TaskTimeLineVertex.Factory.newSimpleVertex('D', 5)
		TaskTimeLineVertex E = TaskTimeLineVertex.Factory.newSimpleVertex('E', 1)
		TaskTimeLineVertex F = TaskTimeLineVertex.Factory.newSimpleVertex('F', 2)
		TaskTimeLineVertex G = TaskTimeLineVertex.Factory.newSimpleVertex('G', 4)
		TaskTimeLineVertex H = TaskTimeLineVertex.Factory.newSimpleVertex('H', 3)

		TaskTimeLineGraph directedGraph = new TaskTimeLineGraph([A, B, C, D, E, F, G, H])
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
								 +-+
								 |D|
					  +--------+>+-+------------+
					  |          |5|            v
					 +++         +++           +-+
					 |B|                       |G|
		   			 +-+          +----------+>+-+
		             |4|          |            |4|
		  			 +-+         +++           +-+
		                         |E|
		              +--------+>+-+
		              |          |1|       		+-+
		             +++         +-+       	 	|H|
		             |C|                   +--->+-+
		             +-+                   |	|3|
					 |2|                   |	+-+
					 +++         +-+       |
					  |          |F|       |
					  +--------+>+-+-------+
								 |2|
								 +-+
 */

	TaskTimeLineGraph createAcyclicDirectedGraphWithTwoSourcesAndTwoSinks() {
		TaskTimeLineVertex B = TaskTimeLineVertex.Factory.newSimpleVertex('B', 4)
		TaskTimeLineVertex C = TaskTimeLineVertex.Factory.newSimpleVertex('C', 2)
		TaskTimeLineVertex D = TaskTimeLineVertex.Factory.newSimpleVertex('D', 5)
		TaskTimeLineVertex E = TaskTimeLineVertex.Factory.newSimpleVertex('E', 1)
		TaskTimeLineVertex F = TaskTimeLineVertex.Factory.newSimpleVertex('F', 2)
		TaskTimeLineVertex G = TaskTimeLineVertex.Factory.newSimpleVertex('G', 4)
		TaskTimeLineVertex H = TaskTimeLineVertex.Factory.newSimpleVertex('H', 3)

		TaskTimeLineGraph directedGraph = new TaskTimeLineGraph([B, C, D, E, F, G, H])
		directedGraph
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

	TaskTimeLineGraph createCyclicDirectedGraph() {
		TaskTimeLineVertex Activity0 = TaskTimeLineVertex.Factory.newSimpleVertex('0', 3)
		TaskTimeLineVertex Activity1 = TaskTimeLineVertex.Factory.newSimpleVertex('1', 4)
		TaskTimeLineVertex Activity2 = TaskTimeLineVertex.Factory.newSimpleVertex('2', 2)
		TaskTimeLineVertex Activity3 = TaskTimeLineVertex.Factory.newSimpleVertex('3', 5)

		TaskTimeLineGraph directedGraph = new TaskTimeLineGraph([Activity0, Activity1, Activity2, Activity3])

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

	TaskTimeLineGraph createCyclicDirectedGraphWithSelfLoop() {
		TaskTimeLineVertex Activity0 = TaskTimeLineVertex.Factory.newSimpleVertex('0', 3)
		TaskTimeLineVertex Activity1 = TaskTimeLineVertex.Factory.newSimpleVertex('1', 4)
		TaskTimeLineVertex Activity2 = TaskTimeLineVertex.Factory.newSimpleVertex('2', 2)
		TaskTimeLineVertex Activity3 = TaskTimeLineVertex.Factory.newSimpleVertex('3', 5)

		TaskTimeLineGraph directedGraph = new TaskTimeLineGraph([Activity0, Activity1, Activity2, Activity3])

		directedGraph
			.addEdge(Activity0, Activity1)
			.addEdge(Activity0, Activity2)
			.addEdge(Activity1, Activity2)
			.addEdge(Activity2, Activity3)
			.addEdge(Activity3, Activity3)// Cyclic edge

		return directedGraph
	}

	TaskTimeLineGraph createTaskAndDependenciesExample() {

		TaskTimeLineVertex taskA = TaskTimeLineVertex.Factory.newSimpleVertex('1', 'A', 5)
		TaskTimeLineVertex taskB = TaskTimeLineVertex.Factory.newSimpleVertex('2', 'B', 2)
		TaskTimeLineVertex taskC = TaskTimeLineVertex.Factory.newSimpleVertex('3', 'C', 8)
		TaskTimeLineVertex taskD = TaskTimeLineVertex.Factory.newSimpleVertex('4', 'D', 6)
		TaskTimeLineVertex taskE = TaskTimeLineVertex.Factory.newSimpleVertex('5', 'E', 10)
		TaskTimeLineVertex taskF = TaskTimeLineVertex.Factory.newSimpleVertex('6', 'F', 7)
		TaskTimeLineVertex taskG = TaskTimeLineVertex.Factory.newSimpleVertex('7', 'G', 1)
		TaskTimeLineVertex taskH = TaskTimeLineVertex.Factory.newSimpleVertex('8', 'H', 3)
		TaskTimeLineVertex taskI = TaskTimeLineVertex.Factory.newSimpleVertex('9', 'I', 4)
		TaskTimeLineVertex taskJ = TaskTimeLineVertex.Factory.newSimpleVertex('10', 'J', 1)
		TaskTimeLineVertex taskK = TaskTimeLineVertex.Factory.newSimpleVertex('11', 'K', 5)
		TaskTimeLineVertex taskL = TaskTimeLineVertex.Factory.newSimpleVertex('12', 'L', 4)
		TaskTimeLineVertex taskM = TaskTimeLineVertex.Factory.newSimpleVertex('13', 'M', 3)
		TaskTimeLineVertex taskN = TaskTimeLineVertex.Factory.newSimpleVertex('14', 'N', 3)
		TaskTimeLineVertex taskO = TaskTimeLineVertex.Factory.newSimpleVertex('15', 'O', 5)
		TaskTimeLineVertex taskP = TaskTimeLineVertex.Factory.newSimpleVertex('16', 'P', 9)
		TaskTimeLineVertex taskQ = TaskTimeLineVertex.Factory.newSimpleVertex('17', 'Q', 1)
		TaskTimeLineVertex taskR = TaskTimeLineVertex.Factory.newSimpleVertex('18', 'R', 5)
		TaskTimeLineVertex taskS = TaskTimeLineVertex.Factory.newSimpleVertex('19', 'S', 6)

		TaskTimeLineGraph directedGraph = new TaskTimeLineGraph([
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
