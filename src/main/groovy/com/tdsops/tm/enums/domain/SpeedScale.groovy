package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Represents the speed scale or unit of measure used to represent the speed of a resource.
 */
@CompileStatic
enum SpeedScale {

	Kbps('Kilobit/sec'),
	KBps('KiloByte/sec'),
	Mbps('Megabit/sec'),
	MBps('MegaByte/sec'),
	Gbps('Gigabit/sec'),
	GBps('GigaByte/sec')

	static SpeedScale getDefault() { MBps }

	final String value

	private SpeedScale(String label) {
		value = label
	}

	String value() { value }

	String toString() { name() }

	static SpeedScale asEnum(String key) {
		values().find { it.name() == key }
	}

	static final List<SpeedScale> keys = (values() as List).asImmutable()

	static final List<String> labels = keys.collect { it.value }.asImmutable()

	static List<String> getLabels(String locale = 'en') { labels }
}
