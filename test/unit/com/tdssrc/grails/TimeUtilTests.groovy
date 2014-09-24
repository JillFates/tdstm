package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import grails.test.*

class TimeUtilTests extends GrailsUnitTestCase {
	
	public void testAgoWithSeconds() {
		assertEquals 'Test 1', '25s', TimeUtil.ago(25)
		assertEquals 'Test 2', '3m 5s', TimeUtil.ago(185)		
		assertEquals 'Test 3', '1-day 1-hr', TimeUtil.ago( 24*60*60 + 60*60 + 61, TimeUtil.ABBREVIATED )		
		assertEquals 'Test 4', '2-days 2-hrs', TimeUtil.ago( 24*60*60*2 + 60*60*2 + 61*2, TimeUtil.ABBREVIATED )
		assertEquals 'Test 5', '3-days 3-hours', TimeUtil.ago( 24*60*60*3 + 60*60*3 + 61*3, TimeUtil.FULL )
		assertEquals 'Test 6', '3-hours 1-minute', TimeUtil.ago( 60*60*3 + 60, TimeUtil.FULL )
		assertEquals 'Test 7', '1-minute 12-seconds', TimeUtil.ago( 72, TimeUtil.FULL )

	}

	public void testAgoWithTwoDates() {
		Date start = new Date()
		Date end
		use( TimeCategory ) {
		    end = start + 1.day + 2.hours + 5.minutes + 21.seconds
		    assertEquals 'Test 1', '1d 2h', TimeUtil.ago(start, end)
		    assertEquals 'Test 2', '1-day 2-hrs', TimeUtil.ago(start, end, TimeUtil.ABBREVIATED)
		    assertEquals 'Test 3', '1-day 2-hours', TimeUtil.ago(start, end, TimeUtil.FULL)

		    end = start + 3.hours
		    assertEquals 'Test 4', '3h', TimeUtil.ago(start, end)
		    end = start + 3.hours + 24.minutes
		    assertEquals 'Test 5', '3h 24m', TimeUtil.ago(start, end)
		    end = start + 3.hours + 11.seconds
		    assertEquals 'Test 5', '3h', TimeUtil.ago(start, end)
		}

	}

	public void testAgoWithTimeDuration() {
		TimeDuration start = new TimeDuration(0, 30, 5, 0)
		TimeDuration end = new TimeDuration(0, 35, 7, 0)
		TimeDuration diff = end - start
		assertEquals '5m 2s', TimeUtil.ago(diff)
	}

	public void testAgoWithInvertedTimeDuration() {

		TimeDuration start = new TimeDuration(0, 30, 0, 0)
		TimeDuration end = new TimeDuration(0, 20, 0, 0)
		TimeDuration diff = end - start
		assertEquals '-10m', TimeUtil.ago(diff, TimeUtil.SHORT)

		start = new TimeDuration(0, 30, 35, 0)
		end = new TimeDuration(0, 20, 20, 0)
		diff = end - start
		assertEquals '-9m 45s', TimeUtil.ago(diff)

	}
		
}