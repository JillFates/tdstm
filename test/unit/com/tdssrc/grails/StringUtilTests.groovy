package com.tdssrc.grails

import grails.test.*
import spock.lang.Specification

/**
 * Unit test cases for the StringUtil class
*/
class StringUtilTests extends Specification {
	
	public void testStripOffPrefixChars() {
		
		def match='!<>='
		def criteria = ">=500"

		expect:
			// Test to see that it strips off the leading chars from the string
			'500' == StringUtil.stripOffPrefixChars(match, criteria)
			'5' == StringUtil.stripOffPrefixChars(match, '>=5')
		
			// Test to see that it doesn't modify a string where there are no matches
			'abc' == StringUtil.stripOffPrefixChars('X', 'abc')		
			'a' == StringUtil.stripOffPrefixChars('X', 'a')		
		
	}
	
	public void testEllipsis() {
		def s='abcdefgh'
		expect:
			"abc..." == StringUtil.ellipsis(s, 6)
			s == StringUtil.ellipsis(s, 50)
	}


	def "Test instanceOfString"() {
		expect:
			StringUtil.instanceOfString('a single quoted string')			
			StringUtil.instanceOfString("a double quoted string (a.k.a. GString")
			! StringUtil.instanceOfString(123L)
			! StringUtil.instanceOfString( new Date() )
	}

	def "Test toLongIfString"() {
		expect: 
			(StringUtil.toLongIfString('1') instanceof Long)
			(StringUtil.toLongIfString("1") instanceof Long)
			! (StringUtil.toLongIfString(new Date()) instanceof Long)
	}
	
}