package com.tdssrc.grails

import grails.test.*

class NumberUtilTests extends GrailsUnitTestCase {
	
	public void testToLong() {
		int four=4

		assertEquals 1L, NumberUtil.toLong('1')
		assertEquals 2L, NumberUtil.toLong("2")	
		assertEquals 3L, NumberUtil.toLong(3L)
		assertEquals 4L, NumberUtil.toLong(four)
		assertEquals 99L, NumberUtil.toLong('', 99L)
		assertNull  NumberUtil.toLong(12.34)
		assertNull 	NumberUtil.toLong("123.56")
		assertNull 	NumberUtil.toLong(false)
		assertNull 	NumberUtil.toLong('')
	}

	public void testToInteger() {
		int four=4

		assertEquals 1, NumberUtil.toInteger('1')
		assertEquals 2, NumberUtil.toInteger("2")	
		assertEquals 3, NumberUtil.toInteger(3L)
		assertEquals 4, NumberUtil.toInteger(four)
		assertEquals 99, NumberUtil.toInteger('', 99)
		assertNull 'real number', NumberUtil.toInteger(12.34)
		assertNull 'real # string', NumberUtil.toInteger("123.56")
		assertNull 'boolean', NumberUtil.toInteger(false)
		assertNull 'blank', NumberUtil.toInteger('')
	}


	public void testToTinyInt() {
		int four=4

		assertEquals 1, NumberUtil.toTinyInt('1')
		assertEquals 2, NumberUtil.toTinyInt("2")	
		assertEquals 3, NumberUtil.toTinyInt(3L)
		assertEquals 4, NumberUtil.toTinyInt(four)
		assertEquals 99, NumberUtil.toTinyInt('', 99)
		assertNull 'real number', NumberUtil.toTinyInt(12.34)
		assertNull 'real # string',	NumberUtil.toTinyInt("123.56")
		assertNull 'boolean', NumberUtil.toTinyInt(false)
		assertNull 'blank', NumberUtil.toTinyInt('')
		assertNull 'to large of an int', NumberUtil.toTinyInt('500')

	}
		
}