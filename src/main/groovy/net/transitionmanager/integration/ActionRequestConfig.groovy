package net.transitionmanager.integration

/**
 * This is used encapsulate the configuration settings that can be overridden in the Camel Http4 Component
 * when performing HTTP calls.
 *
 * Those properties can include:
 * - socket and connection timeouts
 * - proxy configuration
 * - charset
 * - content type
 *
 * Between others. @see http://camel.apache.org/http4.html for more details
 */
class ActionRequestConfig extends HashMap<String, Object> {

	Object getProperty(String key) {
		return this.get(key)
	}

	void setProperty(String key, Object value) {
		this.put(key, value)
	}

	boolean hasProperty(String key) {
		return this.containsKey(key)
	}

}
