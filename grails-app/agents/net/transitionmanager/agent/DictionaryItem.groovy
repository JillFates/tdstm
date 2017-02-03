package net.transitionmanager.agent

/**
 * Represents the properties of a method available in the Dictionary of an agent class
 */
class DictionaryItem {
	String name
	String description=''
	String method
	Map params = [:]
	Map results = [:]

	String getMethod() {
		this.method ?: this.name
	}
}