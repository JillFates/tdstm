/**
 * Unit test cases for testing all of the Enum classes
 */
package com.tdsops.tm.enums.domain

import grails.test.GrailsUnitTestCase

import com.tdsops.tm.enums.domain.TimeScale

class TimeScaleTests extends GrailsUnitTestCase {

	void testToMinutes() {
		assertEquals "Hours to Minutes", 60, TimeScale.H.toMinutes( 1 )
		assertEquals "Day to Minutes", 60*24, TimeScale.D.toMinutes( 1 )
		assertEquals "Week to Minutes", 60*24*7, TimeScale.W.toMinutes( 1 )
	}

	void testToHours() {
		assertEquals "Minutes to Hours", 2, TimeScale.M.toHours( 120 )
		assertEquals "Minutes to Hours - partial", 1, TimeScale.M.toHours( 105 )	// Test partial hours
		assertEquals "Days to Hours", 48, TimeScale.D.toHours( 2 )
		assertEquals "Weeks to Hours", 24 * 7, TimeScale.W.toHours( 1 )
	}

	void testToDays() {
		assertEquals "Minutes to Days", 2, TimeScale.M.toDays( 60 * 24 * 2 )
		assertEquals "Hours to Days", 2, TimeScale.H.toDays( 48 )
		assertEquals "Weeks to Days", 14, TimeScale.W.toDays( 2 )
	}

	void testToWeeks() {
		assertEquals "Minutes to Weeks", 1, TimeScale.M.toWeeks( 60 * 24 * 7 )
		assertEquals "Hours to Weeks", 1, TimeScale.H.toWeeks( 168 )
		assertEquals "Hours to Weeks - partial", 1, TimeScale.H.toWeeks( 180 )
		assertEquals "Days to Weeks", 2, TimeScale.D.toWeeks( 14 )
	}

}
