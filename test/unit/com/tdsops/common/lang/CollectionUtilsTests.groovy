package com.tdsops.common.lang

import spock.lang.Specification

/**
 * TODO: A lot of methods are still missing to test ( We need some code coverage)
 */
class CollectionUtilsTests extends Specification {

	void testCaseInsensitiveSorterBuilder() {
		when:
			def list = [[name: "IContact"], [name: "iContact"], [name: "A"], [name: "K"], [name: "k"]]

			def expectedSort = [[name: "A"], [name: "iContact"], [name: "IContact"], [name: "k"], [name: "K"]]

			def sorter = CollectionUtils.caseInsensitiveSorterBuilder({ it.name })
			list.sort(sorter)

		then:
			expectedSort == list
	}

	void 'test flatten map'() {
		expect: 'resulting flatten map contains all values and its keys fatten using dot notation'
			result == CollectionUtils.flattenMap(map)

		where: 'giving tests table'
			map | result
			['a': 1, 'b': 2] 					| ['a': 1, 'b': 2]
			['a': 1, 'b': ['bb': 2]] 			| ['a': 1, 'b.bb': 2]
			['a': 1, 'b': ['bb': ['bbb': 2]]] 	| ['a': 1, 'b.bb.bbb': 2]
			['a': 1, 'b': 2, 'c': false] 		| ['a': 1, 'b': 2, 'c': false]
	}
}
