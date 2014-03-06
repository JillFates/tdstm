/**
 * The TimeConstraintType represents the various contraints that can be applied to a task that while affect its scheduling
 * 
 * 		ALAP 	As Late As Possible: Schedules the task as late as it can without delaying subsequent tasks. Use no constraint date.
 * 		ASAP	As Soon As Possible: Schedules the task to start as early as it can. Use no constraint date.
 * 		MSO 	Must Start On: Schedules the task to start on the constraint date. Once selected the task will not be movable on the timescale
 * 
 * For more information visit http://support.microsoft.com/kb/74978
 */

package com.tdsops.tm.enums.domain

enum TimeConstraintType {

	ALAP ('As Late As Possible'),
	ASAP ('As Soon As Possible'),
	FNLT ('Finish No Later Than'),
	MSO ('Must Start On'),
	SNLT ('Start No Later Than')

	//
	// Boiler Plate from here down - Just swap out the enum class name
	//

	String value
	private static List keys
	private static List labels

	TimeConstraintType(String value) {
		this.value = value
	}	

	String toString() { name() }
	String value() { value }

	// Used to convert a string to the enum or null if string doesn't match any of the constants
	static TimeConstraintType asEnum(key) {
		def obj
		try {
			obj = key as TimeConstraintType
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
			keys = TimeConstraintType.values()
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
			labels = TimeConstraintType.values()*.value
		}
	} 

}