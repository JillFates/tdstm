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
	/**
	 * Lookup for ReactionScriptCodes from a String.
	 * <code>
	 *     assert ReactionScriptCode.lookup('EVALUATE') ==  ReactionScriptCode.EVALUATE
	 *     assert ReactionScriptCode.lookup('FOO') ==  null
	 * </code>
	 * @param value a String value
	 * @return an instance of net.transitionmanager.integration.ReactionScriptCode or null if the value is not a valid code
	 */
	static ReactionScriptCode lookup(String value) {
		return ReactionScriptCode.enumConstantDirectory().get(value)
	}
}
