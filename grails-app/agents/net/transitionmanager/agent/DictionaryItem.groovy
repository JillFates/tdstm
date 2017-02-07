package net.transitionmanager.agent

import groovy.transform.ToString

/**
 * Represents the properties of a method available in the Dictionary of an agent class
 */
@ToString(includes='name,method,description', includeNames=true, includeFields=true)
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