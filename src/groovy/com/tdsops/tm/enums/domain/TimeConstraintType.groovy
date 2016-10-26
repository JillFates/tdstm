package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Represents the various contraints that can be applied to a task that while affect its scheduling
 *
 * 	ALAP 	As Late As Possible: Schedules the task as late as it can without delaying subsequent tasks. Use no constraint date.
 * 	ASAP	As Soon As Possible: Schedules the task to start as early as it can. Use no constraint date.
 * 	MSO 	Must Start On: Schedules the task to start on the constraint date. Once selected the task will not be movable on the timescale
 *
 * For more information visit http://support.microsoft.com/kb/74978
 */
@CompileStatic
enum TimeConstraintType {

	ALAP('As Late As Possible'),
	ASAP('As Soon As Possible'),
	FNLT('Finish No Later Than'),
	MSO('Must Start On'),
	SNLT('Start No Later Than')

	final String value

	private TimeConstraintType(String label) {
		value = label
	}

	String value() { value }

	static TimeConstraintType asEnum(String key) {
		values().find { it.name() == key }
	}

	static final List<TimeConstraintType> keys = (values() as List).asImmutable()

	static final List<String> labels = keys.collect { it.value }.asImmutable()

	static List<String> getLabels(String locale = 'en') { labels }
}
