package net.transitionmanager.connector

/*
 * Used to determine the Callback on an action is:
 *   Called directly
 *   Called by message
 *   Not Applicable
 */
enum CallbackMode {
	NA,
	DIRECT,
	MESSAGE
}
