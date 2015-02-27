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
		
}