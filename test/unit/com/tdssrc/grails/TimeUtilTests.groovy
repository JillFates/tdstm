package com.tdssrc.grails

import groovy.time.TimeCategory
import grails.test.*

class TimeUtilTests extends GrailsUnitTestCase {
	
	public void testAgoWithSeconds() {
		assertEquals 'Test 1', '25s', TimeUtil.ago(25)
		assertEquals 'Test 2', '3m 5s', TimeUtil.ago(185)		
		assertEquals 'Test 3', '1-day 1-hr 1-min', TimeUtil.ago( 24*60*60 + 60*60 + 61, TimeUtil.ABBREVIATED )		
		assertEquals 'Test 4', '2-days 2-hrs 2-mins', TimeUtil.ago( 24*60*60*2 + 60*60*2 + 61*2, TimeUtil.ABBREVIATED )
		assertEquals 'Test 5', '3-days 3-hours 3-minutes', TimeUtil.ago( 24*60*60*3 + 60*60*3 + 61*3, TimeUtil.FULL )

	}

	public void testAgoWithTwoDates() {
		Date start = new Date()
		Date end
		use( TimeCategory ) {
		    end = start + 1.day + 2.hours + 5.minutes + 21.seconds
		    assertEquals 'Test 1', '1d 2h 5m', TimeUtil.ago(start, end)
		    assertEquals 'Test 2', '1-day 2-hrs 5-mins', TimeUtil.ago(start, end, TimeUtil.ABBREVIATED)
		    assertEquals 'Test 3', '1-day 2-hours 5-minutes', TimeUtil.ago(start, end, TimeUtil.FULL)

		    end = start + 3.hours
		    assertEquals 'Test 4', '3h 0m', TimeUtil.ago(start, end)
		    end = start + 3.hours + 11.seconds
		    assertEquals 'Test 5', '3h 0m 11s', TimeUtil.ago(start, end)
		}

	}
		
}