package net.transitionmanager.integration

/**
 * This represents all of the details that were used build up the API request
 */
class ActionRequest {
	private boolean readonly = false
	ActionRequestParameter param
	ActionRequestHeader headers
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
