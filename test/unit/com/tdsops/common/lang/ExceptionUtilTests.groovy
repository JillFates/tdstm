package com.tdsops.common.lang

import grails.test.*
import com.tdsops.common.lang.ExceptionUtil

class ExceptionUtilTests  extends GrailsUnitTestCase {
		
	public void testOne() {
		String st
		try {
			def x = 1/0
		} catch (e) {
			st = ExceptionUtil.stackTraceToString(e, 10)
		}

		println "exception is : $st"

		// And that the original template is still in tack
		def eType = 'Division by zero'
		def parsed = st?.split(/\n/)
		def numLines = parsed?.size()

		assertTrue 'Exception string contains data', st?.size() > 0
		assertTrue "Contains $eType", st?.contains(eType)
		assertEquals "Number Of Lines", 10, numLines

	}
	
}