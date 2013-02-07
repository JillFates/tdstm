package com.tdssrc.grails

import grails.test.*

class StringUtilTests extends GrailsUnitTestCase {
	
	public void testStripOffPrefixChars() {
		
		def match='!<>='
		def criteria = ">=500"

		// Test to see that it strips off the leading chars from the string
		assertEquals '500', StringUtil.stripOffPrefixChars(match, criteria)
		
		assertEquals '5', StringUtil.stripOffPrefixChars(match, '>=5')
		
		// Test to see that it doesn't modify a string where there are no matches
		assertEquals 'abc', StringUtil.stripOffPrefixChars('X', 'abc')		
		assertEquals 'a', StringUtil.stripOffPrefixChars('X', 'a')		
		
	}
	
	public void testEllipsis() {
		def s='abcdefgh'
		assertEquals "abc...", StringUtil.ellipsis(s, 6)
		assertEquals s, StringUtil.ellipsis(s, 50)
	}

	
}