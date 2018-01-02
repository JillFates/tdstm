package net.transitionmanager.integration

class ApiActionScriptProcessor {

    ActionRequest request
    ApiActionResponse response
    ReactionAssetFacade asset
    ReactionTaskFacade task
    ApiActionJob job

    ApiActionScriptProcessor (ActionRequest request,
                              ApiActionResponse response,
                              ReactionAssetFacade asset,
                              ReactionTaskFacade task,
                              ApiActionJob job) {
        this.request = request
        this.response = response
        this.asset = asset
        this.task = task
        this.job = job
    }

    /**
     * Creates a ApiActionScriptBinding base on the ReactionScriptCode params.
     * @param code a instance of ReactionScriptCode
     * @return an instance of ApiActionScriptBinding
     */
    ApiActionScriptBinding scriptBindingFor (ReactionScriptCode code) {

        ApiActionScriptBinding binding

        switch (code) {
            case ReactionScriptCode.PRE:
                binding = new ApiActionScriptBinding(this, [
                        request: request,
                        asset  : asset,
                        task   : task,
                        job    : job
                ])
                break
            case ReactionScriptCode.EVALUATE:
                binding = new ApiActionScriptBinding(this, [
                        request : request,
                        response: response,
                        *       : ReactionScriptCode.values()
                                .collectEntries {
                            [(it.name()): it]
                        }
                ])
                break
            default:
                binding = new ApiActionScriptBinding(this, [
                        request : request,
                        response: response,
                        asset   : asset,
                        task    : task,
                        job     : job
                ])
                break
        }

        return binding
    }
}

/**
 * Mock classes
 *  TODO: Remove when tickets TM-8694 and TM-8695 were finished
 */
class ReactionAssetFacade {
}

class ReactionTaskFacade {
}

class ApiActionJob {
}