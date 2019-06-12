package net.transitionmanager.task.cpm

import spock.lang.Specification

class DirectedEdgeSpec extends Specification {

	void 'void test can create an instance of DirectedEdge'(){

		when:
			DirectedEdge directedEdge = new DirectedEdge(0, 1, 12.34d)
		then:
			directedEdge.from() == 0
			directedEdge.to() == 1
			directedEdge.weight() == 12.34d
	}

}
