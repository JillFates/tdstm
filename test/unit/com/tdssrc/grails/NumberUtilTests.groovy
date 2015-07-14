package com.tdssrc.grails

import grails.test.*
import spock.lang.Specification

/**
 * Unit test cases for the NumberUtil class
*/
class NumberUtilTests extends Specification {
	
	public void testToLong() {
		int four=4

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

	public void testToPositiveLong() {
		int four=4

		assertEquals 1L, NumberUtil.toPositiveLong('1')
		assertEquals 5L, NumberUtil.toPositiveLong(-3L, 5L)
		assertNull NumberUtil.toPositiveLong('-1')
	}

	public void testToInteger() {
		int four=4

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


	public void testToTinyInt() {
		int four=4

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
	
	public void testIsLong() {
		assertTrue NumberUtil.isLong(50)
		assertTrue NumberUtil.isLong(100L)
		assertTrue NumberUtil.isLong('5')
		assertTrue NumberUtil.isLong('12391023')
		assertTrue NumberUtil.isLong('-1232135')
		assertFalse NumberUtil.isLong('abc')
		assertFalse NumberUtil.isLong(new Date())
		assertFalse NumberUtil.isLong('123.12')
		assertFalse NumberUtil.isLong(null)

	}

	public void testIsPostiveLong() {
		assertTrue NumberUtil.isPositiveLong('5')
		assertFalse NumberUtil.isPositiveLong('-5')
		assertTrue NumberUtil.isPositiveLong(100)
		assertFalse NumberUtil.isPositiveLong(-100L)
	}
}