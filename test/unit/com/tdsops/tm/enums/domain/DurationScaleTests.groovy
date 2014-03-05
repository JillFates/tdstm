/**
 * Unit test cases for testing all of the Enum classes
 */
package com.tdsops.tm.enums.domain

import grails.test.GrailsUnitTestCase

import com.tdsops.tm.enums.domain.DurationScale

class DurationScaleTests extends GrailsUnitTestCase {

	void testToMinutes() {
		assertEquals "Hours to Minutes", 60, DurationScale.H.toMinutes( 1 )
		assertEquals "Day to Minutes", 60*24, DurationScale.D.toMinutes( 1 )
		assertEquals "Week to Minutes", 60*24*7, DurationScale.W.toMinutes( 1 )
	}

	void testToHours() {
		assertEquals "Minutes to Hours", 2, DurationScale.M.toHours( 120 )
		assertEquals "Minutes to Hours - partial", 1, DurationScale.M.toHours( 105 )	// Test partial hours
		assertEquals "Days to Hours", 48, DurationScale.D.toHours( 2 )
		assertEquals "Weeks to Hours", 24 * 7, DurationScale.W.toHours( 1 )
	}

	void testToDays() {
		assertEquals "Minutes to Days", 2, DurationScale.M.toDays( 60 * 24 * 2 )
		assertEquals "Hours to Days", 2, DurationScale.H.toDays( 48 )
		assertEquals "Weeks to Days", 14, DurationScale.W.toDays( 2 )
	}

	void testToWeeks() {
		assertEquals "Minutes to Weeks", 1, DurationScale.M.toWeeks( 60 * 24 * 7 )
		assertEquals "Hours to Weeks", 1, DurationScale.H.toWeeks( 168 )
		assertEquals "Hours to Weeks - partial", 1, DurationScale.H.toWeeks( 180 )
		assertEquals "Days to Weeks", 2, DurationScale.D.toWeeks( 14 )
	}

}
