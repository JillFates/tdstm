package net.transitionmanager.asset

import groovy.transform.CompileStatic

@CompileStatic
enum AssetEntityType {

	APPLICATION('A'),
	DATABASE('B'),
	DEVICE('D'),
	NETWORK('N'),
	STORAGE('S')

	final String value

	private AssetEntityType(String label) {
		value = label
	}

	String value() { value }

	static AssetEntityType asEnum(String key) {
		values().find { it.name() == key }
	}

	static final List<AssetEntityType> keys = (values() as List).asImmutable()

	static final List<String> labels = keys.collect { it.value }.asImmutable()

	static List<String> getLabels(String locale = 'en') { labels }
}
