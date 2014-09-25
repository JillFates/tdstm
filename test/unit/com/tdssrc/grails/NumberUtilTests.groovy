package com.tdssrc.grails

import grails.test.*

class NumberUtilTests extends GrailsUnitTestCase {
	
	public void testToLong() {
		int four=4

		assertEquals 1L, NumberUtil.toLong('1')
		assertEquals 2L, NumberUtil.toLong("2")	
		assertEquals 3L, NumberUtil.toLong(3L)
		assertEquals 4L, NumberUtil.toLong(four)
		assertNull  NumberUtil.toLong(12.34)
		assertNull 	NumberUtil.toLong("123.56")
		assertNull 	NumberUtil.toLong(false)
	}
		
}