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
}