package net.transitionmanager.integration

/**
 * This represents all of the details that were used build up the API request
 * TM-8692
 */
class ActionRequest {
	private boolean readonly = false

	// Contains the parameters with the contextual values that are defined in the ApiAction
	// These parameters will end up in the query string and/or json payload of requests
	ActionRequestParameter params

	// Contain properties need by the API logic (used internally by the implementation code)
	ActionRequestParameter options

	// Contains any header that will be use in the request that can be populated in the Action PRE script
	ActionRequestHeader headers

	// Contains setting used by Camel that can be populated in the Action PRE script
	ActionRequestConfig config

	ActionRequest() {
		this([:])
	}

	ActionRequest(Map<String, Object> parameters) {
		this.params = new ActionRequestParameter(parameters)
		this.options = [:]
		this.headers = [:]
		this.config = [:]
	}

	boolean isReadonly() {
		return readonly
	}

	void setReadonly(boolean value) {
		this.readonly = value
		this.params.setReadonly(this.readonly)
		this.headers.setReadonly(this.readonly)
		this.options.setReadonly(this.readonly)
	}

	/**
	 * Return a ActionRequest as map representation
	 * @return
	 */
	Map toMap() {
		Map params
		if (this.params) {
			params = this.params.getAllProperties()
		}

		Map options
		if (this.options) {
			options = this.options.getAllProperties()
		}

		Map headers
		if (this.headers) {
			headers = this.headers.getHeadersAsMap()
		}

		Map config
		if (this.config) {
			config = this.config.asImmutable()
		}

		return [
		        params: params,
				options: options,
				headers: headers,
				config: config
		]
	}

}
