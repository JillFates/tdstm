package net.transitionmanager.task.cpm

import spock.lang.Shared
import spock.lang.Specification

class DijkstraShortestPathSpec extends Specification {

	@Shared
	EdgeWeightedDigraph weightedDigraph

	void setup() {
		weightedDigraph = new EdgeWeightedDigraph(8)
		weightedDigraph.addEdge(new DirectedEdge(4, 5, 0.35))
		weightedDigraph.addEdge(new DirectedEdge(5, 4, 0.35))
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
	}

	void 'test can calculate shortest path between vertices'() {

		when:
			DijkstraShortestPath dijkstraShortestPath = new DijkstraShortestPath(weightedDigraph, 0)

		then:
			dijkstraShortestPath.hasPathTo(0)
			dijkstraShortestPath.hasPathTo(1)
			dijkstraShortestPath.hasPathTo(2)
			dijkstraShortestPath.hasPathTo(3)
			dijkstraShortestPath.hasPathTo(4)
			dijkstraShortestPath.hasPathTo(5)
			dijkstraShortestPath.hasPathTo(6)
			dijkstraShortestPath.hasPathTo(7)

		and:
			dijkstraShortestPath.pathTo(1).collect {
				[it.from(), it.to(), it.weight()]
			} == [
				[5, 1, 0.32], [4, 5, 0.35], [0, 4, 0.38]
			]

		and:
			dijkstraShortestPath.pathTo(6).collect {
				[it.from(), it.to(), it.weight()]
			} == [
				[3, 6, 0.52], [7, 3, 0.39], [2, 7, 0.34], [0, 2, 0.26]
			]
	}

}
