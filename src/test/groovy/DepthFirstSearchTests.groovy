import net.transitionmanager.graph.Graph
import spock.lang.Specification

class DepthFirstSearchTests extends Specification {

    void '01. Test a simple scenario of findCycles'() {
        setup: 'create a graph with a single cycle ([1, 2, 5])'
            Graph graph = new Graph([
                    1l: [2l, 3l],
                    2l: [4l, 5l],
                    3l: [4l],
                    4l: [],
                    5l: [1l],
                    6l: []
            ])
            List<Long> expectedCycle = [1l, 2l, 5l]
        when: 'finding cycles'
            List<List<Long>> cycles = graph.findCycles()
        then: 'only one cycle is found'
            cycles.size() == 1
        and: 'the list has the expected number of elements (3)'
            cycles[0].size() == 3
        and: 'the cycle includes the expected elements'
            cycles[0].containsAll(expectedCycle)
    }

    void '02. Test findCycles under a scenario with multiple cycles for the same node'() {
        setup: 'create a graph where the same node is part of multiple cycles ([1, 2, 5], [1, 3, 4], [1, 2, 4])'
            Graph graph = new Graph([
                    5l: [1l],
                    3l: [4l],
                    2l: [4l, 5l],
                    1l: [2l, 3l],
                    4l: [1],
                    6l: []
            ])
            List<List<Long>> expectedCycles = [ [1l, 2l, 5l], [1l, 2l, 4l], [1l, 3l, 4l] ]
        when: 'finding cycles'
            List<List<Long>> cycles = graph.findCycles()
        then: 'three cycles are found'
            cycles.size() == 3
        and: 'each cycle has 3 elements'
            cycles.each { List<Long> cycle ->
                assert cycle.size() == 3
            }
        and: 'the cycles contain the expected elements'
            expectedCycles.each { List<Long> expectedCycle ->
                assert cycles.any { List<Long> cycle ->
                   cycle.containsAll(expectedCycle)
                }
            }
    }

    void '03. Test findCycles with a graph with no cycles'() {
        setup: 'create a graph with no cycles'
            Graph graph = new Graph([
                    1l: [2l, 3l],
                    2l: [4l, 5l]
            ])
        when: 'finding cycles'
            List<List<Long>> cycles = graph.findCycles()
        then: 'the list of cycles is empty'
            cycles.size() == 0
    }

    void '04. Test findCycles with a more complex scenario where the same cycles could be reported multiple times'() {
        setup: 'create the graph with two cycles ([2, 4, 6], [2, 5, 6])'
        Graph graph = new Graph([
                1l: [2l],
                2l: [3l, 4l, 5l],
                3l: [],
                4l: [6l],
                5l: [6l],
                6l: [2l]
        ])
        List<List<Long>> expectedCycles = [[2l, 4l, 6l], [2l, 5l, 6l]]
        when: 'finding cycles'
        List<List<Long>> cycles = graph.findCycles()
        then: 'both cycles are found'
        cycles.size() == 2
        and: 'the cycles have the expected number of elements (3)'
            cycles.each { List<Long> cycle ->
                assert cycle.size() == 3
            }
        and: 'the cycle includes the expected elements'
            expectedCycles.each { List<Long> expectedCycle ->
                assert cycles.any { List<Long> cycle ->
                    cycle.containsAll(expectedCycle)
                }
            }
    }
}
