package net.transitionmanager.agent

/*
 * Used to determine the Callback on an action is:
 *   Called directly
 *   Called by message
 *   Not Applicable
 */
enum CallbackMode {
	DIRECT(1),
	MESSAGE(2),
	NA(3)

	int id
	CallbackMode(int id) {
		this.id = id
	}

	static CallbackMode forId(int id) {
		values().find { it.id == id }
	}
}