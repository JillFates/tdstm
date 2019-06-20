package net.transitionmanager.task.cpm.helper

import groovy.transform.CompileStatic
import net.transitionmanager.task.cpm.TaskTimeLineGraph

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
			.withVertex('A', 3).addEdgesTo(['B', 'C'])
			.withVertex('B', 4).addEdgeTo('D')
			.withVertex('C', 2).addEdgesTo(['E', 'F'])
			.withVertex('D', 5).addEdgeTo('G')
			.withVertex('E', 1).addEdgeTo('G')
			.withVertex('F', 2).addEdgeTo('H')
			.withVertex('G', 4).addEdgeTo('H')
			.withVertex('H', 3)
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
			.withVertex('B', 4).addEdgeTo('D')
			.withVertex('C', 2).addEdgesTo(['E', 'F'])
			.withVertex('D', 5).addEdgeTo('G')
			.withVertex('E', 1).addEdgeTo('G')
			.withVertex('F', 2).addEdgeTo('H')
			.withVertex('G', 4).addEdgeTo('H')
			.withVertex('H', 3)
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
			.withVertex('A', 3).addEdgesTo(['B', 'C'])
			.withVertex('B', 4).addEdgeTo('D')
			.withVertex('C', 2).addEdgesTo(['E', 'F'])
			.withVertex('D', 5).addEdgeTo('G')
			.withVertex('E', 1).addEdgeTo('G')
			.withVertex('F', 2).addEdgeTo('H')
			.withVertex('G', 4)
			.withVertex('H', 3)
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
			.withVertex('B', 4).addEdgeTo('D')
			.withVertex('C', 2).addEdgesTo(['E', 'F'])
			.withVertex('D', 5).addEdgeTo('G')
			.withVertex('E', 1).addEdgeTo('G')
			.withVertex('F', 2).addEdgeTo('H')
			.withVertex('G', 4)
			.withVertex('H', 3)
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
			.withVertex('0', 3).addEdgesTo(['1', '2'])
			.withVertex('1', 4).addEdgeTo('2')
			.withVertex('2', 2).addEdgesTo(['1', '3'])
			.withVertex('3', 5)
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
			.withVertex('0', 3).addEdgesTo(['1', '2'])
			.withVertex('1', 4).addEdgeTo('2')
			.withVertex('2', 2).addEdgeTo('3')
			.withVertex('3', 5).addEdgeTo('3')
			.build()
	}

	TaskTimeLineGraph createTaskAndDependenciesExample() {

		return new TaskTimeLineGraph.Builder()
			.withVertex('1', 'A', 5).addEdgeTo('H')
			.withVertex('2', 'B', 2).addEdgeTo('I')
			.withVertex('3', 'C', 8).addEdgesTo(['K', 'S'])
			.withVertex('4', 'D', 6).addEdgesTo(['H', 'L', 'N', 'K'])
			.withVertex('5', 'E', 10).addEdgesTo(['O', 'H'])
			.withVertex('6', 'F', 7).addEdgesTo(['D', 'G'])
			.withVertex('7', 'G', 1).addEdgesTo(['D', 'J'])
			.withVertex('8', 'H', 3).addEdgeTo('L')
			.withVertex('9', 'I', 4).addEdgeTo('D')
			.withVertex('10', 'J', 1).addEdgeTo('N')
			.withVertex('11', 'K', 5).addEdgeTo('M')
			.withVertex('12', 'L', 4).addEdgeTo('O')
			.withVertex('13', 'M', 3).addEdgesTo(['Q', 'P'])
			.withVertex('14', 'N', 3)
			.withVertex('15', 'O', 5)
			.withVertex('16', 'P', 9)
			.withVertex('17', 'Q', 1)
			.withVertex('18', 'R', 5)
			.withVertex('19', 'S', 6)
			.build()
	}
}
