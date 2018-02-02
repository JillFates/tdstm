package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Represents the size scale or unit of measure.
 */
@CompileStatic
enum SizeScale {

	KB('Kilobyte'),
	MB('Megabyte'),
	GB('Gigabyte'),
	TB('Terabyte'),
	PB('Petabyte')

	static SizeScale getDefault() { MB }

	final String value

	private SizeScale(String label) {
		value = label
	}

	String value() { value }

	// The keys of the enum keys
	static final List<SizeScale> keys = (values() as List).asImmutable()

	// Returns the labels of the enum labels
	static final List<String> labels = keys.collect { it.value }.asImmutable()

	// Convert a string to the enum or null if string doesn't match any of the constants
	static SizeScale asEnum(String key) {
		values().find { it.name() == key }
	}

	static List<String> getLabels(String locale = 'en') { labels }

	// Convert enum to a representation with key/values
	static List<Object> getAsJsonList() {
		List<Object> stringList = new ArrayList<Object>();
		keys.each {
			stringList.add([ 'value' :(it.name()), 'text': it.getValue()])
		}
		return stringList
	}
}
