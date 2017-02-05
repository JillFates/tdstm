package net.transitionmanager.agent

/*
 * Used to determine the Callback on an action is:
 *   Called directly
 *   Called by message
 *   Not Applicable
 */
enum CallbackMode {
	NA('NA'),
	DIRECT('DIRECT'),
	MESSAGE('MESSAGE')

	String id
	CallbackMode (String id) {
		this.id = id
	}

	static CallbackMode forId(String id) {
		values().find { it.id == id }
	}
}