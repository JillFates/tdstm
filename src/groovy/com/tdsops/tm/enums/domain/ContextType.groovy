package com.tdsops.tm.enums.domain

import MoveBundle
import MoveEvent

import com.tds.asset.Application

enum ContextType {

    E('Event'),
    B('Bundle'),
    A('Application');

	// Used to access the application's default value to use
	static ContextType getDefault() {
		return ContextType.E
	}

	//
	// Boiler Plate from here down - Just swap out the enum class name
	//

	String value
	private static List keys
	private static List labels

	ContextType(String value) {
		this.value = value
	}	
	
	public Object getObject(contextId) {
		//abstract methods are not supported
		switch (this) {
			case ContextType.A:
				return Application.get(contextId)
				break;
			case ContextType.B:
				return MoveBundle.get(contextId)
				break;
			case ContextType.E:
				return MoveEvent.get(contextId)
				break;
			default:
				throw new IllegalArgumentException('')
				break;
		}
	}

	String toString() { name() }
	String value() { value }

	// Used to convert a string to the enum or null if string doesn't match any of the constants
	static ContextType asEnum(key) {
		def obj
		try {
			obj = key as ContextType
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
			keys = ContextType.values()
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
			labels = ContextType.values()*.value
		}
	} 
}

