package net.transitionmanager.command

import grails.validation.Validateable


trait CommandObject implements Validateable {

	/**
	 * Used to populate the Command Object with the properties from the domain
	 * @param domain - the domain object that the proporty values will be retrieved from
	 */
	void populateFromDomain(Object domain) {
		for (prop in commandProperties()) {
			// TODO check that the domain has the property
			this[prop.key] = domain[prop.key]
		}
	}

	/**
	 * Used to populate the domain object from the command object with the option to skip properties that are empty/null
	 * @param domain - the domain object to set the properties on
	 * @param skipEmptyProperties - flag (true|false) to indicate if empty properties of the command object should be skipped over (default false)
	 * @param ignoreProperties - an optional list of properties to not copy over
	 */
	void populateDomain(Object domain, Boolean skipEmptyProperties=false, List<String> ignoreProperties=null) {
		if (ignoreProperties == null) {
			ignoreProperties = []
		}
		for (prop in commandProperties()) {
			// If skip empty and the Command Object doesn't have a value for the property we will skip over the it
			if ( skipEmptyProperties && (prop.value == null || ((prop.value instanceof String) && prop.value == '' ))) {
				continue
			}

			// Check to see if the property is in the ignoreProperties list
			if (prop.key in ignoreProperties) {
				continue
			}

			// Only set the value if is different so as to not trigger the dirty flag
			if (prop.value != domain[prop.key]) {
				domain[prop.key] = prop.value
			}
		}
	}

	/**
	 * Used to exact the properties of the command object as a Map
	 * @return a map of all of the properties that make up the Command Object
	 */
	Map toMap() {
		commandProperties()
	}

	/**
	 * Used to return the property/values of the domain object excluding those that are injected by the
	 * @return a map of all of the property/values
	 */
	private Map commandProperties() {
		this.properties.findAll{ !['metaClass','class', 'constraints', 'constraintsMap',  'errors'].contains(it.key)}
	}
}