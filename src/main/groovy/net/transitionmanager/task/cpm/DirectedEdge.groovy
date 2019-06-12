package net.transitionmanager.task.cpm

import groovy.transform.CompileStatic

/**
 * The {@code DirectedEdge} class represents a weighted edge in an
 * {@link EdgeWeightedDigraph}. Each edge consists of two integers
 * (naming the two vertices) and a real-value weight. The data type
 * provides methods for accessing the two endpoints of the directed edge and
 * the weight.<p>
 */
@CompileStatic
class DirectedEdge {

	private final int v
	private final int w
	private final double weight

	/**
	 * Initializes a directed edge from vertex {@code v} to vertex {@code w} with
	 * the given {@code weight}.
	 * @param v the tail vertex
	 * @param w the head vertex
	 * @param weight the weight of the directed edge
	 * @throws IllegalArgumentException if either {@code v} or {@code w}
	 *    is a negative integer
	 * @throws IllegalArgumentException if {@code weight} is {@code NaN}
	 */
	DirectedEdge(int v, int w, double weight) {
		if (v < 0) throw new IllegalArgumentException("Vertex names must be non-negative integers")
		if (w < 0) throw new IllegalArgumentException("Vertex names must be non-negative integers")
		if (Double.isNaN(weight)) throw new IllegalArgumentException("Weight is NaN")
		this.v = v
		this.w = w
		this.weight = weight
	}
	/**
	 * Returns the tail vertex of the directed edge.
	 * @return the tail vertex of the directed edge
	 */
	int from() {
		return v
	}

	/**
	 * Returns the head vertex of the directed edge.
	 * @return the head vertex of the directed edge
	 */
	int to() {
		return w
	}

	/**
	 * Returns the weight of the directed edge.
	 * @return the weight of the directed edge
	 */
	double weight() {
		return weight
	}

	/**
	 * Returns a string representation of the directed edge.
	 * @return a string representation of the directed edge
	 */
	String toString() {
		return v + "->" + w + " " + String.format("%5.2f", weight)
	}
}
