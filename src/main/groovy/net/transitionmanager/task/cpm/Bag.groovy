package net.transitionmanager.task.cpm

import groovy.transform.CompileStatic

/******************************************************************************
 *  A generic bag or multiset, implemented using a singly linked list.
 *
 *  % tobe.txt
 *  to be or not to - be - - that - - - is
 *
 *  % java Bag < tobe.txt
 *  size of bag = 14
 *  is
 *  -
 *  -
 *  -
 *  that
 *  -
 *  -
 *  be
 *  -
 *  to
 *  not
 *  or
 *  be
 *  to
 *
 ******************************************************************************/
/**
 *  The {@code Bag} class represents a bag (or multiset) of
 *  generic items. It supports insertion and iterating over the
 *  items in arbitrary order.
 *  <p>
 *  This implementation uses a singly linked list with a static nested class Node.
 *  See {@link LinkedBag} for the version from the
 *  textbook that uses a non-static nested class.
 *  See {@link ResizingArrayBag} for a version that uses a resizing array.
 *  The <em>add</em>, <em>isEmpty</em>, and <em>size</em> operations
 *  take constant time. Iteration takes time proportional to the number of items.
 *  <p>
 * @param < T >    the generic type of an item in this bag
 */
@CompileStatic
class Bag<T> implements Iterable<T> {

	private Node<T> first    // beginning of bag
	private int n            // number of elements in bag

	// helper linked list class
	private static class Node<T> {

		private T item
		private Node<T> next
	}

	/**
	 * Initializes an empty bag.
	 */
	Bag() {
		first = null
		n = 0
	}

	/**
	 * Returns true if this bag is empty.
	 * @return {@code true} if this bag is empty;
	 * {@code false} otherwise
	 */
	boolean isEmpty() {
		return first == null
	}

	/**
	 * Returns the number of items in this bag.
	 * @return the number of items in this bag
	 */
	int size() {
		return n
	}

	/**
	 * Adds the item to this bag.
	 * @param item the item to add to this bag
	 */
	void add(T item) {
		Node<T> oldfirst = first
		first = new Node<T>()
		first.item = item
		first.next = oldfirst
		n++;
	}


	/**
	 * Returns an iterator that iterates over the items in this bag in arbitrary order.
	 * @return an iterator that iterates over the items in this bag in arbitrary order
	 */
	Iterator<T> iterator() {
		return new ListIterator(first)
	}

	// an iterator, doesn't implement remove() since it's optional
	class ListIterator implements Iterator<T> {

		private Node<T> current

		ListIterator(Node<T> first) {
			current = first
		}

		boolean hasNext() {
			return current != null
		}

		void remove() {
			throw new UnsupportedOperationException()
		}

		T next() {
			if (!hasNext()) throw new NoSuchElementException()
			T item = current.item
			current = current.next
			return item
		}
	}

}