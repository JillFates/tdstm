package net.transitionmanager.integration

/**
 * This class is used to encapsulate the parameters needed to perform an ApiAction call. The values
 * might belong to an Asset or a Task.
 */
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

	private checkPropertyMissing(String name) {
		if (Objects.nonNull(parameters)) {
			if (!parameters.containsKey(name)) {
				raiseMissingPropertyException(name)
			}
		} else {
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
