package net.transitionmanager.command.task


import net.transitionmanager.command.CommandObject
/**
 * A command for passing the public key, so that either a form post or a json post can be used.
 */
class RecordRemoteActionStartedCommand implements CommandObject {

	/**
	 * The public that will be used for encrypting the credentials that will be passed down to TMD.
	 */
	String publicKey
}
