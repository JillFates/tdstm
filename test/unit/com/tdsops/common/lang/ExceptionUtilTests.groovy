package com.tdsops.common.lang

import spock.lang.Specification

class ExceptionUtilTests extends Specification {

	void testOne() {
		String st
		try {
			def x = 1 / 0
		}
		catch (e) {
			st = ExceptionUtil.stackTraceToString(e, 10)
		}

		// And that the original template is still in tack
		def eType = 'Division by zero'
		def parsed = st?.split(/\n/)
		def numLines = parsed?.size()

		expect:
		// Exception string contains data
		st?.size() > 0
		// Contains $eType
		st?.contains(eType)
		// Number Of Lines
		10 == numLines
	}
}
