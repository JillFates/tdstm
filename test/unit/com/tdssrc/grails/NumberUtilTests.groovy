package com.tdssrc.grails

import spock.lang.Specification

class NumberUtilTests extends Specification {

	void testToLong() {
		int four = 4

		expect:
		1L == NumberUtil.toLong('1')
		2L == NumberUtil.toLong("2")
		3L == NumberUtil.toLong(3L)
		4L == NumberUtil.toLong(four)
		99L == NumberUtil.toLong('', 99L)
		NumberUtil.toLong(12.34) == null
		NumberUtil.toLong("123.56") == null
		NumberUtil.toLong(false) == null
		NumberUtil.toLong('') == null
	}

	void testLimit() {
		expect:
		10 == NumberUtil.limit(12, 1, 10)
		10 == NumberUtil.limit(1, 10, 50)
		3 == NumberUtil.limit(3, 1, 10)
	}

	void testToPositiveLong() {
		expect:
		1L == NumberUtil.toPositiveLong('1')
		5L == NumberUtil.toPositiveLong(-3L, 5L)
		!NumberUtil.toPositiveLong('-1')
	}

	void testToInteger() {
		given:
		int four = 4

		expect:
		1 == NumberUtil.toInteger('1')
		2 == NumberUtil.toInteger("2")
		3 == NumberUtil.toInteger(3L)
		4 == NumberUtil.toInteger(four)
		99 == NumberUtil.toInteger('', 99)
		// real number
		NumberUtil.toInteger(12.34) == null
		// real # string'
		NumberUtil.toInteger("123.56") == null
		// boolean'
		NumberUtil.toInteger(false) == null
		// blank
		NumberUtil.toInteger('') == null
	}

	void 'Test toPositiveInteger'() {
		expect:
			// String
			1 == NumberUtil.toPositiveInteger('1')
			// GString
			1 == NumberUtil.toPositiveInteger("1")
			// Negative values no default
			null == NumberUtil.toPositiveInteger('-1')
			// Negative value with default
			2 == NumberUtil.toPositiveInteger('-4', 2)
			// non-numeric
			5 == NumberUtil.toPositiveInteger('abc', 5)
	}

	void testToTinyInt() {
		given:
		int four = 4

		expect:
		1 == NumberUtil.toTinyInt('1')
		2 == NumberUtil.toTinyInt("2")
		3 == NumberUtil.toTinyInt(3L)
		4 == NumberUtil.toTinyInt(four)
		99 == NumberUtil.toTinyInt('', 99)
		// real number
		NumberUtil.toTinyInt(12.34) == null
		// real # string'
		NumberUtil.toTinyInt("123.56") == null
		// boolean
		NumberUtil.toTinyInt(false) == null
		// blank
		NumberUtil.toTinyInt('') == null
		// to large of an int
		NumberUtil.toTinyInt('500') == null
	}

	void testIsLong() {
		expect:
		NumberUtil.isLong(50)
		NumberUtil.isLong(100L)
		NumberUtil.isLong('5')
		NumberUtil.isLong('12391023')
		NumberUtil.isLong('-1232135')
		!NumberUtil.isLong('abc')
		!NumberUtil.isLong(new Date())
		!NumberUtil.isLong('123.12')
		!NumberUtil.isLong(null)
	}

	void testIsPostiveLong() {
		expect:
		NumberUtil.isPositiveLong('5')
		!NumberUtil.isPositiveLong('-5')
		NumberUtil.isPositiveLong(100)
		!NumberUtil.isPositiveLong(-100L)
	}

	void testMapToPositiveInteger(){
		def arr = ["1", "2", 3L, 4, "nada", null, "5"]
		expect:
			[1,2,3,4,5] == NumberUtil.mapToPositiveInteger(arr)
			[1,2,3,4,0,0,5] == NumberUtil.mapToPositiveInteger(arr, 0)
			NumberUtil.mapToPositiveInteger(arr).each {
				assert it instanceof Integer
			}
	}

	void testMapToPositiveLong(){
		def arr = ["1", "2", 3L, 4, "nada", null, "5"]
		expect:
			[1L,2L,3L,4L,5L] == NumberUtil.mapToPositiveLong(arr)
			[1L,2L,3L,4L,0L,0L,5L] == NumberUtil.mapToPositiveLong(arr, 0)

			NumberUtil.mapToPositiveLong(arr).each {
				assert it instanceof Long
			}
	}
}
