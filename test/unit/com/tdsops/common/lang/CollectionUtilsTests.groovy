package com.tdsops.common.lang

import com.tdssrc.grails.HtmlUtil
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

	void 'test asList' () {
		when: 'converting a simple value to a list'
			int anInt = 2
			List result = CollectionUtils.asList(anInt)
		then: 'the list has one element and one element only'
			result.size() == 1
		and: 'and the element is the original parameter'
			result[0] == anInt
		when: 'sending a list as argument'
			List aList = [1, 2]
			result = CollectionUtils.asList(aList)
		then: 'the same reference was returned'
			result == aList
		and: 'the values in the list are unchanged'
			result.eachWithIndex{ value, int i ->
				result[i] == aList[i]
			}
		when: 'requesting values to be escaped and the parameter is a string but does not need it'
			String aString = 'foo'
			result = CollectionUtils.asList( aString, true)
		then: 'the parameter remains unchanged'
			result[0] == aString
		when: 'requesting escaping on non-string values'
			result = CollectionUtils.asList(anInt, true)
		then: 'the parameter remains unchanged'
			result[0] == anInt
		when: 'requesting escaping on a string that should be escaped'
			String xssString = "XSS+Exploit%3Csjavascript%3Acript%3Ealert%28%22XSS%22%29%3C%2Fsjavascript%3Acript%3E"
			result = CollectionUtils.asList(xssString, true)
		then: 'the string is correctly escaped'
			result[0] == HtmlUtil.escape(xssString)
		when: 'requesting escaping on a list with a string that should be escaped'
			result = CollectionUtils.asList([xssString], true)
		then: 'the string is correctly escaped'
			result[0] == HtmlUtil.escape(xssString)
		when: 'converting to list a list with mixed types and also requesting escaping'
			List mixedList = [xssString, aString, anInt]
			result = CollectionUtils.asList(mixedList, true)
		then: 'each value was handled accordingly'
			result[0] == HtmlUtil.escape(xssString)
			result[1] == aString
			result[2] == anInt
		when: 'when sending a null value'
			result = CollectionUtils.asList(null, true)
		then: 'the result is null'
			result == null
		when: 'sending an empty list'
			result = CollectionUtils.asList([], true)
		then: 'the result is also an empty list'
			result.size() == 0


	}
}
