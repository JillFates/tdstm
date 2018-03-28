package net.transitionmanager.integration

/**
 * This represents all of the details that were used build up the API request
 */
class ActionRequest {
	private boolean readonly = false

	// Contains the parameters with the contextual values that are defined in the ApiAction
	// These parameters will end up in the query string and/or json payload of requests
	ActionRequestParameter params

	// Contain properties need by the API logic (used internally by the implemenation code)
	ActionRequestParameter options

	// Contains any header that will be use in the request that can be populated in the Action PRE script
	ActionRequestHeader headers

	// Contains setting used by Camel that can be populated in the Action PRE script
	ActionRequestConfig config

	ActionRequest() {
		this([:])
	}

	ActionRequest(Map<String, Object> parameters) {
		this.param = new ActionRequestParameter(parameters)
		this.headers = [:]
		this.config = [:]
	}

	boolean isReadonly() {
		return readonly
	}

	void setReadonly(boolean value) {
		this.readonly = value
		this.param.setReadonly(this.readonly)
		this.headers.setReadonly(this.readonly)
	}

}
