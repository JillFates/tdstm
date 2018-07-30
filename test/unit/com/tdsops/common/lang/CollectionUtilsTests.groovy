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

	void 'test deepClone of a Map'() {
		setup:
			Map source = [
				a: [1,2,3],
				b: ['Red', 'Orange', 'Yellow'],
				c: [
					a: [true, false, true],
					b: 'xray',
					c: 'zulu'
				]
			]

		when: 'a Map instance is deepCloned'
			Map cloned = CollectionUtils.deepClone(source)

		then: 'the two maps should different objects'
			! cloned.is(source)
		and: 'the nested elements should be different objects'
			! cloned.a.is(source.a)
			! cloned.b.is(source.b)
			! cloned.c.is(source.c)
			! cloned.c.a.is(source.c.a)
			! cloned.c.b.is(source.c.b)
			! cloned.c.c.is(source.c.c)
		and: 'the values of all of the elments are the same'
			cloned.a == source.a
			cloned.b == source.b
			cloned.c == source.c
	}
}
