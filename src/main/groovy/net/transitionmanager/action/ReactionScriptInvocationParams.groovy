package net.transitionmanager.action


import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.task.TaskFacade
import org.grails.web.json.JSONObject

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

    String getScript(ReactionScriptCode reactionScriptCode) {
        return this.reactionScriptsMap.get(reactionScriptCode)
    }
}
