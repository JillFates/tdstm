package net.transitionmanager.integration
import groovy.transform.CompileStatic

/**
 * This class is used to encapsulate the parameters needed to perform an ApiAction call. The values
 * might belong to an Asset or a Task.
 */
@CompileStatic
class ActionRequestParameter {
	private boolean readonly = false
	private Map<String, Object> parameters

	ActionRequestParameter(Map<String, Object> parameters) {
		this.parameters = parameters
	}

	void setReadonly(boolean value) {
		readonly = value
	}

	boolean isReadonly() {
		return readonly
	}

	Object getProperty(String name) {
		checkPropertyMissing(name)
		return parameters.get(name)
	}

	void setProperty(String name, Object value) {
		checkPropertyMissing(name)
		checkReadonly(name)
		parameters.put(name, value)
	}

	String toString() {
		return parameters
	}

	/**
	 * Used to determine if a particular property has been set on the object
	 * @param name - the name of the property
	 * @return true if was previously set otherwise false
	 */
	Boolean hasProperty(String name) {
		return (parameters && parameters.containsKey(name))
	}

	/**
	 * Used to retrieve all parameters set on the instance
	 * @return a map with the name/values of all parameters
	 */
	Map<String, Object> getAllProperties() {
		return parameters
	}

	private void checkPropertyMissing(String name) {
		if (! hasProperty(name)) {
			raiseMissingPropertyException(name)
		}
	}

	private void raiseMissingPropertyException(String property) {
		throw new MissingPropertyException("No such property: " + property)
	}

	private checkReadonly(String name) {
		if (isReadonly()) {
			throw new ReadOnlyPropertyException(name, ActionRequestParameter.class.name)
		}
	}
}
