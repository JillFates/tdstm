package com.tdsops.apiaction

class ApiActionProcessor {

    ApiActionRequest request
    ApiActionResponse response
    ReactionAssetFacade asset
    ReactionTaskFacade task
    ApiActionJob job

    ApiActionProcessor (ApiActionRequest request,
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
     *
     * @return
     */
    ApiActionBinding preScriptBinding () {

        return new ApiActionBinding(this, [
                request: request,
                asset  : asset,
                task   : task,
                job    : job
        ])
    }

    /**
     *
     * @return
     */
    ApiActionBinding evaluateScriptBinding () {

        return new ApiActionBinding(this, [
                request : request,
                response: response,
                *       : ReactionScriptCode.values()
                        .collectEntries {
                    [(it.name()): it]
                }
        ])
    }

    /**
     *
     * @return
     */
    ApiActionBinding resultScriptBinding () {

        return new ApiActionBinding(this, [
                request : request,
                response: response,
                asset   : asset,
                task    : task,
                job     : job
        ])
    }

}

/**
 * Mock classes
 *
 */
class ApiActionRequest {
}

class ApiActionResponse {
}

class ReactionAssetFacade {
}

class ReactionTaskFacade {
}

class ApiActionJob {
}