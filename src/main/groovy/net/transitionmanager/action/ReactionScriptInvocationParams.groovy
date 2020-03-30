package net.transitionmanager.action


import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.task.TaskFacade
import org.grails.web.json.JSONObject

/**
 * Every API ActionReaction invocation needs a List of parameters tomo complete the execution.
 * This class is intent to contain all these parameters.
 * It also manages reaction script using Map with each script associated.
 * <pre>
 *     JSONObject reactionScriptsJSONObject = JsonUtil.parseJson(action.reactionScripts)
 *      ReactionScriptInvocationParams invocationParams =   new ReactionScriptInvocationParams( reactionScriptsJSONObject,...)
 *      ...
 *      invocationParams.getScript(ReactionScriptCode.PRE) == '// PRE script content'
 * </pre>
 * @see
 */
class ReactionScriptInvocationParams {

    Map<ReactionScriptCode, String> reactionScriptsMap = [:]
    ActionRequest actionRequest
    ApiActionResponse apiActionResponse
    TaskFacade taskFacade
    AssetFacade assetFacade
    ApiActionJob apiActionJob

    ReactionScriptInvocationParams(JSONObject reactionScripts, ActionRequest actionRequest, TaskFacade taskFacade, AssetFacade assetFacade) {
        this.reactionScriptsMap = reactionScripts.collectEntries { [(ReactionScriptCode.lookup(it.key)): it.value] }
        this.actionRequest = actionRequest
        this.apiActionResponse = new ApiActionResponse()
        this.taskFacade = taskFacade
        this.assetFacade = assetFacade
        this.apiActionJob = new ApiActionJob()
    }
    /**
     *
     * @param reactionScriptCode a value from {@link ReactionScriptCode} enumeration.
     * @return a String script content from {@link ReactionScriptInvocationParams#reactionScriptsMap}
     */
    String getScript(ReactionScriptCode reactionScriptCode) {
        return this.reactionScriptsMap.get(reactionScriptCode)
    }
}
