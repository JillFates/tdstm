package net.transitionmanager.integration

/**
 * The the ReactionScriptCode enum specifies all of the codes that will exist in the ApiAction.reactionJson hash.
 */
enum ReactionScriptCode {

	EVALUATE,
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
	 * @param code
	 * @return
	 */
	static ReactionScriptCode lookup(String code){
		for (ReactionScriptCode reactionScriptCode: values()){
			if (reactionScriptCode.name().equalsIgnoreCase(code)) {
				return reactionScriptCode
			}
		}
		return null
	}
}