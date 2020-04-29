package net.transitionmanager.graph

class Graph {

    Map<Long, List<Long>> vertices = [:]

    Graph() {

    }

    Graph(Map<Long, List<Long>> vertices) {
        this.vertices = vertices
    }


    /**
     * Add a new vertex to the graph. If it already existed, an exception will be thrown.
     * @param id
     * @param adjacencyList
     */
    void addVertex(Long id, List<Long> adjacencyList) {
        if (id in vertices.keySet()) {
            throw new RuntimeException("Tried to add duplicate vertex $id.")
        }
        vertices[id] = adjacencyList?: []
    }

    /**
     * Return the list of vertices for this graph.
     * @return
     */
    Set<Long> getVertexIds() {
        return vertices.keySet()
    }

    /**
     * Return the list of adjacent vertices.
     * @param id
     * @return
     */
    List<Long> getAdjacencyList(Long id) {
        return vertices.get(id)
    }

    /**
     * Trigger the search for cycles.
     * @return
     */
    List<List<Long>> findCycles() {
        return new DepthFirstSearch(this).findCycles()
    }

}
