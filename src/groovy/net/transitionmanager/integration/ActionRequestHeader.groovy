package net.transitionmanager.integration

/**
 * This class is used to encapsulate all headers that we can pass to the Camel when executing routes.
 */
class ActionRequestHeader {
	private boolean readonly = false
	private Map<String, String> headers

	ActionRequestHeader(Map<String, String> headers) {
		this.headers = headers
	}

	void setReadonly(boolean value) {
		readonly = value
	}

	boolean isReadonly() {
		return readonly
	}

	void add(String key, String value) {
		checkReadonly(key)
		headers.put(key, value)
	}

	String get(String header) {
		if (Objects.nonNull(headers)) {
			return headers.get(header)
		} else {
			throw new MissingPropertyException("No such header: " + header)
		}
	}

	Map<String, String> getHeadersAsMap() {
		return headers
	}

	String toString() {
		return headers
	}

	private checkReadonly(String name) {
		if (isReadonly()) {
			throw new ReadOnlyPropertyException(name, ActionRequestHeader.class.name)
		}
	}

}
