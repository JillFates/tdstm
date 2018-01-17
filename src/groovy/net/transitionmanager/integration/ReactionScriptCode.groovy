package net.transitionmanager.integration

/**
 * The the ReactionScriptCode enum specifies all of the codes that will exist in the ApiAction.reactionJson hash.
 */
enum ReactionScriptCode {

	STATUS,
	SUCCESS,
	ERROR,
	DEFAULT,
	FAILED,
	TIMEDOUT,
	LAPSED,
	STALLED,
	PRE,
	FINAL
}