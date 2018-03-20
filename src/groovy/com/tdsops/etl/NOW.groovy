package com.tdsops.etl

import com.tdssrc.grails.TimeUtil

/**
 * Date wrapper to return the String Date format as an ISO8601 Date
 */
class NOW {

	@Delegate Date value = new Date()

	@Override
	String toString() {
		return TimeUtil.formatToISO8601DateTime(value)
	}
}
