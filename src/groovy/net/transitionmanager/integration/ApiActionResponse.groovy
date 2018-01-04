package net.transitionmanager.integration
/**
 * The API Action Reaction logic is going to need the ability to inspect the response object
 * for information that is returned from the API action invocation.
 * This class will be exposed to the Reaction DSL scripting language
 * to examine the results that came back from the call.
 */
class ApiActionResponse {
	boolean readonly = false
	Object data
	Long elapsed
	String error
	Map<String, String> headers = [:]
	List<Map> files
	String filename
	String originalFilename
	int status
	Boolean successful

	void setReadonly (boolean value) {
		readonly = value
	}

	boolean isReadonly () {
		return readonly
	}

	void setProperty (String name, Object value) {
		if (name != 'readonly') {
			checkReadonly(name)
			this.@"$name" = value
		}
	}

	boolean hasHeader (String key) {
		return headers.containsKey(key)
	}

	String getHeader (String key) {
		if (hasHeader(key)) {
			return headers.get(key)
		}
		return null
	}

	ApiActionResponse asImmutable () {
		ApiActionResponse immutable = new ApiActionResponse()
		immutable.data = this.data
		immutable.elapsed = this.elapsed
		immutable.error = this.error
		immutable.headers = this.headers
		immutable.files = this.files
		immutable.filename = this.filename
		immutable.originalFilename = this.originalFilename
		immutable.status = this.status
		immutable.successful = this.successful

		immutable.setReadonly(true)
		return immutable
	}

	private checkReadonly (String name) {
		if (isReadonly()) {
			throw new ReadOnlyPropertyException(name, ApiActionResponse.class.name)
		}
	}
}
