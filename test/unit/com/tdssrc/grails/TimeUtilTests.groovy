package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import grails.test.*
import spock.lang.Specification

/**
 * Unit test cases for the TimeUtil class
*/
class TimeUtilTests extends Specification {
	
	public void testAgoWithSeconds() {
		expect:
		'25s' == TimeUtil.ago(25)
		'3m 5s' == TimeUtil.ago(185)		
		'1-day 1-hr' == TimeUtil.ago( 24*60*60 + 60*60 + 61, TimeUtil.ABBREVIATED )		
		'2-days 2-hrs' == TimeUtil.ago( 24*60*60*2 + 60*60*2 + 61*2, TimeUtil.ABBREVIATED )
		'3-days 3-hours' == TimeUtil.ago( 24*60*60*3 + 60*60*3 + 61*3, TimeUtil.FULL )
		'3-hours 1-minute' == TimeUtil.ago( 60*60*3 + 60, TimeUtil.FULL )
		'1-minute 12-seconds' == TimeUtil.ago( 72, TimeUtil.FULL )
	}

	public void testAgoWithTwoDates() {
		Date start = new Date()
		Date end
		use( TimeCategory ) {
			when:
		    	end = start + 1.day + 2.hours + 5.minutes + 21.seconds
		    then:
		    	'1d 2h' == TimeUtil.ago(start, end)
		    	'1-day 2-hrs' == TimeUtil.ago(start, end, TimeUtil.ABBREVIATED)
		    	'1-day 2-hours' == TimeUtil.ago(start, end, TimeUtil.FULL)

		    when:
		    	end = start + 3.hours
		    then:
		    	'3h' == TimeUtil.ago(start, end)

		    when:
		    	end = start + 3.hours + 24.minutes
		    then:
		    	'3h 24m' == TimeUtil.ago(start, end)

		    when:
		    	end = start + 3.hours + 11.seconds
		    then:
		    	'3h' == TimeUtil.ago(start, end)
		}

	}

	public void testAgoWithTimeDuration() {
		TimeDuration start = new TimeDuration(0, 30, 5, 0)
		TimeDuration end = new TimeDuration(0, 35, 7, 0)
		TimeDuration diff = end - start
		expect:
			'5m 2s' == TimeUtil.ago(diff)
	}

	public void testAgoWithInvertedTimeDuration() {

		when:
			TimeDuration est = new TimeDuration(0, 30, 0, 0)
			TimeDuration act = new TimeDuration(0, 20, 0, 0)
			TimeDuration delta = act - est
		then:
			'-10m' == TimeUtil.ago(delta, TimeUtil.SHORT)

		when:
			est = new TimeDuration(0, 30, 35, 0)
			act = new TimeDuration(0, 20, 20, 0)
			delta = act - est
		then:
			'-9m 45s' == TimeUtil.ago(delta)

		when:
			est = new TimeDuration(0, 1, 0, 0)
			act = new TimeDuration(0, 0, 12, 0)
			delta = act - est
		then:
			'-48s' == TimeUtil.ago(delta)

	}
		
}