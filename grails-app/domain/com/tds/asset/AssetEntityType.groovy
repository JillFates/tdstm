package com.tds.asset

public enum AssetEntityType {
	APPLICATION('A'),
	DATABASE('B'),
	DEVICE('D'),
	NETWORK('N'),
	STORAGE('S')

	//
	// Boiler Plate from here down - Just swap out the enum class name
	//

	String value
	private static List keys
	private static List labels

	AssetEntityType(String value) {
		this.value = value
	}	

	String toString() { name() }
	String value() { return value }

	// Used to convert a string to the enum or null if string doesn't match any of the constants
	static AssetEntityType asEnum(key) {
		def obj
		try {
			obj = key as AssetEntityType
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
		if (keys == null)
			keys = AssetEntityType.values()
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
			labels = AssetEntityType.values()*.value
		}
	} 

}