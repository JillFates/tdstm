package net.transitionmanager.task.cpm

import spock.lang.Specification

class EdgeWeightedDigraphSpec extends Specification {

	void 'test can create an instance of EdgeWeightedDigraph'(){
		given:
			EdgeWeightedDigraph weightedDigraph = new EdgeWeightedDigraph(8)

		when:
			weightedDigraph.addEdge(new DirectedEdge(4, 5, 0.35))
			weightedDigraph.addEdge(new DirectedEdge(5,4 , 0.35))
			weightedDigraph.addEdge(new DirectedEdge(4, 7, 0.37))
			weightedDigraph.addEdge(new DirectedEdge(5, 7, 0.28))
			weightedDigraph.addEdge(new DirectedEdge(7, 5, 0.28))
			weightedDigraph.addEdge(new DirectedEdge(5, 1, 0.32))
			weightedDigraph.addEdge(new DirectedEdge(0, 4, 0.38))
			weightedDigraph.addEdge(new DirectedEdge(0, 2, 0.26))
			weightedDigraph.addEdge(new DirectedEdge(7, 3, 0.39))
			weightedDigraph.addEdge(new DirectedEdge(1, 3, 0.29))
			weightedDigraph.addEdge(new DirectedEdge(2, 7, 0.34))
			weightedDigraph.addEdge(new DirectedEdge(6, 2, 0.40))
			weightedDigraph.addEdge(new DirectedEdge(3, 6, 0.52))
			weightedDigraph.addEdge(new DirectedEdge(6, 0, 0.58))
			weightedDigraph.addEdge(new DirectedEdge(6, 4, 0.93))

		then: 'number of vertices can be calculated'
			weightedDigraph.V() == 8

		and: 'number of edges can be calculated'
			weightedDigraph.E() == 15
	}
}
