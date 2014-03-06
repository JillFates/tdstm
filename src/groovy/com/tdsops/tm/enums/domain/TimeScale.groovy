/**
 * The TimeScale represents the various values that durations represent with task duration lengths
 */

package com.tdsops.tm.enums.domain

enum TimeScale {

	M ('Minutes'),
	H ('Hours'),
	D ('Days'),
	W ('Weeks')

	// Used to access the application's default value to use
	static TimeScale getDefault() {
		return TimeScale.M
	}

	//
	// Some conversion functions
	//

	/**
	 * Coverts a value from the particular type to minutes
	 * @param value - an integer to convert
	 * @return the value converted to minutes
	 * @example assert TimeScale.D.toMinutes( 5 ) == 5 * 60 * 24
	 */
	Integer toMinutes( Integer value ) {
		def v = value
		switch (this) {
			case M:	break
			case H:	v = value * 60; break
			case D: v = value * 60 * 24; break
			case W:	v = value * 60 * 24 * 7; break
		}
		return v
	}

	/**
	 * Coverts a value from the particular type to hours
	 * @param value - an integer to convert
	 * @return the value converted to hours rounded down
	 * @example assert TimeScale.D.toHours( 2 ) == 2 * 24
	 */	
	Integer toHours( Integer value ) {
		def v = value
		switch (this) {
			case M:	v = (value / 60).toInteger() ; break
			case H:	break
			case D: v = value * 24; break
			case W:	v = value * 24 * 7; break
		}
		return v
	}

	/**
	 * Coverts a value from the particular type to days
	 * @param value - an integer to convert
	 * @return the value converted to days rounded down
	 * @example assert TimeScale.H.toDays( 48 ) == 2
	 */	
	Integer toDays( Integer value ) {
		def v = value
		switch (this) {
			case M:	v = (value / (60 * 24)).toInteger() ; break
			case H:	v = (value / 24).toInteger() ; break
			case D: break
			case W:	v = value * 7; break
		}
		return v
	}

	/**
	 * Coverts a value from the particular type to weeks
	 * @param value - an integer to convert
	 * @return the value converted to weeks rounded down
	 * @example assert TimeScale.D.toWeeks( 14 ) == 2
	 */	
	Integer toWeeks( Integer value ) {
		def v = value
		switch (this) {
			case M:	v = (value / (60 * 24 * 7)).toInteger() ; break
			case H:	v = (value / (24 * 7)).toInteger() ; break
			case D: v = (value / 7).toInteger() ;break
			case W:	break
		}
		return v
	}

	//
	// Boiler Plate from here down - Just swap out the enum class name
	//

	String value
	private static List keys
	private static List labels

	TimeScale(String value) {
		this.value = value
	}	

	String toString() { name() }
	String value() { value }

	// Used to convert a string to the enum or null if string doesn't match any of the constants
	static TimeScale asEnum(key) {
		def obj
		try {
			obj = key as TimeScale
		} catch (e) { }
		return obj
	}

	// Returns the keys of the enum keys
	static List getKeys() { 
		if (keys == null) 
			buildKeys()
		return keys
	}

	// Construct the static keys 
	private static synchronized void buildKeys() { 
		if (keys == null) {
			keys = TimeScale.values()
		}
	} 

	// Returns the labels of the enum labels
	static List getLabels(String locale='en') { 
		if (labels == null) 
			buildLabels()
		return labels
	}

	// Construct the static labels 
	private static synchronized void buildLabels() { 
		if (labels == null) {
			labels = TimeScale.values()*.value
		}
	} 
}