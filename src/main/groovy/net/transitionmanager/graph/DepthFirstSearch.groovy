package net.transitionmanager.graph

class DepthFirstSearch {

    private Graph graph

    private Map<Long, Boolean> visited

    private List<List<Long>> cycles

    DepthFirstSearch(Graph graph) {
        this.graph = graph
    }

    /**
     * Find all the cycles in the given graph.
     *
     * @return a list with the cycles.
     */
    List<List<Long>> findCycles() {
        visited = initializeVisitedMap()
        cycles = []
        graph.vertexIds.each { Long id ->
            if (!visited[id]) {
                Stack<Long> currentPath = []
                findCyclesFor(id, currentPath, initializeVisitedMap())
            }
        }
        return cycles
    }

    /**
     * Trigger a DFS off the given node in search for cycles.
     * @param id
     * @param currentPath
     * @param visitedInPath
     */
    private void findCyclesFor(Long id, Stack<Long> currentPath, Map<Long, Boolean> visitedInPath) {
        visited[id] = true
        // Check if has been previously visited. If so, a new cycle has been found.
        if (visitedInPath[id]) {
            addCycle(id, currentPath)
        } else {
            visitedInPath[id] = true
            currentPath.push(id)
            graph.getAdjacencyList(id).each { Long adjacent ->
                findCyclesFor(adjacent, currentPath, visitedInPath)
            }

            currentPath.pop()
            visitedInPath[id] = false
        }
    }

    /**
     * Construct and add a cycle to the list of cycles.
     *
     * @param startId
     * @param currentPath
     */
    private void addCycle(Long startId, Stack<Long> currentPath) {
        int startIndex = currentPath.indexOf(startId)
        List<Long> currentCycle = currentPath[startIndex..currentPath.size() - 1]
        boolean alreadyFound = cycles.any { List<Long> cycle ->
            cycle.containsAll(currentCycle)
        }
        if (!alreadyFound) {
            cycles.add(currentCycle)
        }
    }


    /**
     * Build a map with the vertices as key and a boolean flag set to false.
     * @return
     */
    private Map<Long, Boolean> initializeVisitedMap() {
        Map<Long, Boolean> visitedMap = [:]
        graph.vertexIds.each { Long id ->
            visitedMap[id] = false
        }
        return visitedMap
    }

}
