package net.transitionmanager.task.timeline.helper

import groovy.transform.CompileStatic
import net.transitionmanager.task.timeline.TaskTimeLineGraph

@CompileStatic
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

	TaskTimeLineGraph createAcyclicDirectedGraphWithOneStartAndOneSink() {

		return new TaskTimeLineGraph.Builder()
			.withVertex(1, 'A', 3).addEdgesTo('B', 'C')
			.withVertex(2, 'B', 4).addEdgeTo('D')
			.withVertex(3, 'C', 2).addEdgesTo('E', 'F')
			.withVertex(4, 'D', 5).addEdgeTo('G')
			.withVertex(5, 'E', 1).addEdgeTo('G')
			.withVertex(6, 'F', 2).addEdgeTo('H')
			.withVertex(7, 'G', 4).addEdgeTo('H')
			.withVertex(8, 'H', 3)
			.build()
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

	TaskTimeLineGraph createAcyclicDirectedGraphWithTwoStartsAndOneSink() {

		return new TaskTimeLineGraph.Builder()
			.withVertex(1, 'B', 4).addEdgeTo('D')
			.withVertex(2, 'C', 2).addEdgesTo('E', 'F')
			.withVertex(3, 'D', 5).addEdgeTo('G')
			.withVertex(4, 'E', 1).addEdgeTo('G')
			.withVertex(5, 'F', 2).addEdgeTo('H')
			.withVertex(6, 'G', 4).addEdgeTo('H')
			.withVertex(7, 'H', 3)
			.build()
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

	TaskTimeLineGraph createAcyclicDirectedGraphWithOneStartAndTwoSinks() {

		return new TaskTimeLineGraph.Builder()
			.withVertex(1, 'A', 3).addEdgesTo('B', 'C')
			.withVertex(2, 'B', 4).addEdgeTo('D')
			.withVertex(3, 'C', 2).addEdgesTo('E', 'F')
			.withVertex(4, 'D', 5).addEdgeTo('G')
			.withVertex(5, 'E', 1).addEdgeTo('G')
			.withVertex(6, 'F', 2).addEdgeTo('H')
			.withVertex(7, 'G', 4)
			.withVertex(8, 'H', 3)
			.build()
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

	TaskTimeLineGraph createAcyclicDirectedGraphWithTwoStartsAndTwoSinks() {

		return new TaskTimeLineGraph.Builder()
			.withVertex(1, 'B', 4).addEdgeTo('D')
			.withVertex(2, 'C', 2).addEdgesTo('E', 'F')
			.withVertex(3, 'D', 5).addEdgeTo('G')
			.withVertex(4, 'E', 1).addEdgeTo('G')
			.withVertex(5, 'F', 2).addEdgeTo('H')
			.withVertex(6, 'G', 4)
			.withVertex(7, 'H', 3)
			.build()
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

		return new TaskTimeLineGraph.Builder()
			.withVertex(1, '0', 3).addEdgesTo('1', '2')
			.withVertex(2, '1', 4).addEdgeTo('2')
			.withVertex(3, '2', 2).addEdgesTo('0', '3')
			.withVertex(4, '3', 5)
			.build()
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
		return new TaskTimeLineGraph.Builder()
			.withVertex(1, '0', 3).addEdgesTo('1', '2')
			.withVertex(2, '1', 4).addEdgeTo('2')
			.withVertex(3, '2', 2).addEdgeTo('3')
			.withVertex(4, '3', 5).addEdgeTo('3')
			.build()
	}

	TaskTimeLineGraph createTaskAndDependenciesExample() {

		return new TaskTimeLineGraph.Builder()
			.withVertex(1, '1', 'A', 5).addEdgeTo('8')
			.withVertex(2, '2', 'B', 2).addEdgeTo('9')
			.withVertex(3, '3', 'C', 8).addEdgesTo('11', '19')
			.withVertex(4, '4', 'D', 6).addEdgesTo('8', '12', '14', '11')
			.withVertex(5, '5', 'E', 10).addEdgesTo('15', '8')
			.withVertex(6, '6', 'F', 7).addEdgesTo('4', '7')
			.withVertex(7, '7', 'G', 1).addEdgesTo('4', '10')
			.withVertex(8, '8', 'H', 3).addEdgeTo('12')
			.withVertex(9, '9', 'I', 4).addEdgeTo('4')
			.withVertex(10, '10', 'J', 1).addEdgeTo('14')
			.withVertex(11, '11', 'K', 5).addEdgeTo('13')
			.withVertex(12, '12', 'L', 4).addEdgeTo('15')
			.withVertex(13, '13', 'M', 3).addEdgesTo('17', '16')
			.withVertex(14, '14', 'N', 3)
			.withVertex(15, '15', 'O', 5)
			.withVertex(16, '16', 'P', 9)
			.withVertex(17, '17', 'Q', 1)
			.withVertex(18, '18', 'R', 5)
			.withVertex(19, '19', 'S', 6)
			.build()
	}

	TaskTimeLineGraph createTaskAndDependenciesExampleWithCycles() {

		return new TaskTimeLineGraph.Builder()
			.withVertex(1, '1', 'A', 5).addEdgeTo('8')
			.withVertex(2, '2', 'B', 2).addEdgeTo('9')
			.withVertex(3, '3', 'C', 8).addEdgesTo('11', '19')
			.withVertex(4, '4', 'D', 6).addEdgesTo('8', '12', '14', '11')
			.withVertex(5, '5', 'E', 10).addEdgesTo('15', '8')
			.withVertex(6, '6', 'F', 7).addEdgesTo('4', '7')
			.withVertex(7, '7', 'G', 1).addEdgesTo('4', '10')
			.withVertex(8, '8', 'H', 3).addEdgesTo('9', '12') // Add Cycle I -> D -> H
			.withVertex(9, '9', 'I', 4).addEdgeTo('4')
			.withVertex(10, '10', 'J', 1).addEdgeTo('14')
			.withVertex(11, '11', 'K', 5).addEdgeTo('13')
			.withVertex(12, '12', 'L', 4).addEdgeTo('15')
			.withVertex(13, '13', 'M', 3).addEdgesTo('17', '16')
			.withVertex(14, '14', 'N', 3)
			.withVertex(15, '15', 'O', 5)
			.withVertex(16, '16', 'P', 9).addEdgeTo('11') // Add Cycle K -> M -> P
			.withVertex(17, '17', 'Q', 1)
			.withVertex(18, '18', 'R', 5)
			.withVertex(19, '19', 'S', 6)
			.build()
	}

}
