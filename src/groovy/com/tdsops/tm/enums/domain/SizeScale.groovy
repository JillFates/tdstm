/**
 * The SizeScale represents the size scale or unit of measure
 */

package com.tdsops.tm.enums.domain

enum SizeScale {

	KB ('Kilobyte'),
	MB ('Megabyte'),
	GB ('Gigabyte'),
	TB ('Terabyte'),
	PB ('Petabyte')

	// Used to access the application's default value to use
	static SizeScale getDefault() {
		return MB
	} 

	//
	// Boiler Plate from here down - Just swap out the enum class name
	//

	String value
	private static List keys
	private static List labels

	SizeScale(String value) {
		this.value = value
	}	

	String toString() { name() }
	String value() { value }

	// Used to convert a string to the enum or null if string doesn't match any of the constants
	static SizeScale asEnum(key) {
		def obj
		try {
			obj = key as SizeScale
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
			keys = SizeScale.values()
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
			labels = SizeScale.values()*.value
		}
	} 

}