package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Represents the various values that durations represent with task duration lengths
 */
@CompileStatic
enum TimeScale {

	M('Minutes'),
	H('Hours'),
	D('Days'),
	W('Weeks')

	static TimeScale getDefault() { M }

	// Some conversion functions

	/**
	 * Converts a value from the particular type to minutes.
	 * @param value  the amount to convert
	 * @return the value converted to minutes
	 * @example assert TimeScale.D.toMinutes(5) == 5 * 60 * 24
	 */
	int toMinutes(int value) {
		switch (this) {
			case M: return value
			case H: return value * 60
			case D: return value * 60 * 24
			case W: return value * 60 * 24 * 7
		}
	}

	/**
	 * Coverts a value from the particular type to hours.
	 * @param value  the amount to convert
	 * @return the value converted to hours rounded down
	 * @example assert TimeScale.D.toHours(2) == 2 * 24
	 */
	int toHours(int value) {
		switch (this) {
			case M: return (int)(value / 60)
			case H: return value
			case D: return value * 24
			case W: return value * 24 * 7
		}
	}

	/**
	 * Coverts a value from the particular type to days.
	 * @param value  the amount to convert
	 * @return the value converted to days rounded down
	 * @example assert TimeScale.H.toDays(48) == 2
	 */
	int toDays(int value) {
		switch (this) {
			case M: return (int)(value / 60 / 24)
			case H: return (int)(value / 24)
			case D: return value
			case W: return value * 7
		}
	}

	/**
	 * Coverts a value from the particular type to weeks.
	 * @param value  the amount to convert
	 * @return the value converted to weeks rounded down
	 * @example assert TimeScale.D.toWeeks(14) == 2
	 */
	int toWeeks(int value) {
		switch (this) {
			case M: return (int)(value / 60 / 24 / 7)
			case H: return (int)(value / 24 / 7)
			case D: return (int)(value / 7)
			case W: return value
		}
	}

	final String value

	private TimeScale(String label) {
		value = label
	}

	String value() { value }

	static TimeScale asEnum(String key) {
		values().find { it.name() == key }
	}

	static final List<TimeScale> keys = (values() as List).asImmutable()

	static final List<String> labels = keys.collect { it.value }.asImmutable()

	static List<String> getLabels(String locale = 'en') { labels }

	/**
	 * Return the corresponding TimeScale for the given label ('Minutes', 'Hours', etc.)
	 * @param label
	 * @return a TimeScale enum or null if the label is invalid.
	 */
	static TimeScale fromLabel(String label) {
		TimeScale result = null
		if (label) {
			result = values().find { it.value().toLowerCase() == label.toLowerCase()}
		}
		return result
	}
}
