package com.tdsops.common.lang

import spock.lang.Specification

/**
 * TODO: A lot of methods are still missing to test ( We need some code coverage)
 */
class CollectionUtilsTests extends Specification {

	/**
	 * Testing the Sorting using closure builder
	 * @author @tavo_luna
	 */
	void testCaseInsensitiveSorterBuilder() {
		when:
		def list = [[name: "IContact"], [name: "iContact"], [name: "A"], [name: "K"], [name: "k"]]

		def expectedSort = [[name: "A"], [name: "iContact"], [name: "IContact"], [name: "k"], [name: "K"]]

		def sorter = CollectionUtils.caseInsensitiveSorterBuilder({ it.name })
		list.sort(sorter)

		then:
		expectedSort == list
	}
}
