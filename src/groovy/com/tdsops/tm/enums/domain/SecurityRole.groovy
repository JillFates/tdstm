/**
 * The SecurityRole represents the various Security roles that users can be assigned to
 */

package com.tdsops.tm.enums.domain

enum SecurityRole {

	USER ('User'),	// Limited access
	EDITOR ('Editor'),	// Client user with moderate access'),
	SUPERVISOR ('Supervisor'), 
	ADMIN ('Administrator') // All rights

	//
	// Boiler Plate from here down - Just swap out the enum class name
	//

	String value
	private static List keys
	private static List labels

	SecurityRole(String value) {
		this.value = value
	}	

	String toString() { name() }
	String value() { value }

	// Used to convert a string to the enum or null if string doesn't match any of the constants
	static SecurityRole asEnum(key) {
		def obj
		try {
			obj = key as SecurityRole
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
			keys = SecurityRole.values()
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
			labels = SecurityRole.values()*.value
		}
	} 

}