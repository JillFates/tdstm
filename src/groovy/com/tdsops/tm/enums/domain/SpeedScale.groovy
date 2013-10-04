/**
 * The SpeedScale represents the speed scale or unit of measure used to represent the speed of a resource
 */

package com.tdsops.tm.enums.domain

enum SpeedScale {

	Kbps ('Kilobit/sec'),
	KBps ('KiloByte/sec'),
	Mbps ('Megabit/sec'),
	MBps ('MegaByte/sec'),
	Gbps ('Gigabit/sec'),
	GBps ('GigaByte/sec')

	// Used to access the application's default value to use
	static SpeedScale getDefault() {
		return SpeedScale.MBps
	}

	//
	// Boiler Plate from here down ('Just swap out the enum class name
	//

	String value
	private static List keys
	private static List labels

	SpeedScale(String value) {
		this.value = value
	}	

	String toString() { name() }
	String value() { value }

	// Used to convert a string to the enum or null if string doesn't match any of the constants
	static SpeedScale asEnum(key) {
		def obj
		try {
			obj = key as SpeedScale
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
			keys = SpeedScale.values()
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
			labels = SpeedScale.values()*.value
		}
	} 

}