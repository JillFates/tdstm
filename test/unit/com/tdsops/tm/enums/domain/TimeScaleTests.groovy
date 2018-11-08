package com.tdsops.tm.enums.domain

import spock.lang.Specification

class TimeScaleTests extends Specification {

	void testToMinutes() {
		expect:
		// Hours to Minutes
		60 == TimeScale.H.toMinutes(1)
		// Day to Minutes
		60 * 24 == TimeScale.D.toMinutes(1)
		// Week to Minutes
		60 * 24 * 7 == TimeScale.W.toMinutes(1)
	}

	void testToHours() {
		expect:
		// Minutes to Hours
		2 == TimeScale.M.toHours(120)
		// Minutes to Hours - partial
		1 == TimeScale.M.toHours(105)   // Test partial hours
		// Days to Hours
		48 == TimeScale.D.toHours(2)
		// Weeks to Hours
		24 * 7 == TimeScale.W.toHours(1)
	}

	void testToDays() {
		expect:
		// Minutes to Days
		2 == TimeScale.M.toDays(60 * 24 * 2)
		// Hours to Days
		2 == TimeScale.H.toDays(48)
		// Weeks to Days
		14 == TimeScale.W.toDays(2)
	}

	void testToWeeks() {
		expect:
		// Minutes to Weeks
		1 == TimeScale.M.toWeeks(60 * 24 * 7)
		// Hours to Weeks
		1 == TimeScale.H.toWeeks(168)
		// Hours to Weeks - partial
		1 == TimeScale.H.toWeeks(180)
		// Days to Weeks
		2 == TimeScale.D.toWeeks(14)
	}

	void 'test fromLabel'() {
		expect: 'the correct enum is determined'
			TimeScale.fromLabel(label) == expectedResult
		where:
			label       |   expectedResult
			'Minutes'   |   TimeScale.M
			'MINutES'   |   TimeScale.M
			'M'         |   null
			null        |   null
			''          |   null
			'Hours'     |   TimeScale.H
	}
}
