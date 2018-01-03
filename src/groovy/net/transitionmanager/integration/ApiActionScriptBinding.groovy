package net.transitionmanager.integration

import groovy.transform.builder.Builder
import groovy.transform.builder.ExternalStrategy

/**
 * This class is used for binding context in every Api Action script processed.
 */
class ApiActionScriptBinding extends Binding {

    ApiActionScriptBinding (Map vars = [:]) {
        this.variables.putAll([
                SC: ReactionHttpStatus,
//                *: apiActionProcessor.metaClass.methods.collectEntries {
//                    [(it.name): InvokerHelper.getMethodPointer(apiActionProcessor, it.name)]
//                },
                * : vars
        ])
    }

    /**
     * Custom lookup variable. If a variable isn't found it throws an exception
     * @param name
     * @return
     */
    @Override
    Object getVariable (String name) {

        if (variables == null)
            throw new MissingPropertyException('There is not variables bound in this script context')

        Object result = variables.get(name)

        if (result == null && !variables.containsKey(name)) {
            throw new MissingPropertyException('There is no property with name: ' + name + ' bound in this script context')
//            result = name
        }

        return result
    }

    /**
     * Creates a ApiActionScriptBinding base on the ReactionScriptCode params.
     * @param code a instance of ReactionScriptCode
     * @return an instance of ApiActionScriptBinding
     */
    static ApiActionScriptBinding scriptBindingFor (ReactionScriptCode code, ActionRequest request,
                                                    ApiActionResponse response,
                                                    ReactionAssetFacade asset,
                                                    ReactionTaskFacade task,
                                                    ApiActionJob job) {

        ApiActionScriptBinding binding

        switch (code) {
            case ReactionScriptCode.PRE:
                binding = new ApiActionScriptBinding([
                        request: request,
                        asset  : asset,
                        task   : task,
                        job    : job
                ])
                break
            case ReactionScriptCode.EVALUATE:
                binding = new ApiActionScriptBinding([
                        request : request,
                        response: response,
                        *       : ReactionScriptCode.values()
                                .collectEntries {
                            [(it.name()): it]
                        }
                ])
                break
            default:
                binding = new ApiActionScriptBinding([
                        request : request,
                        response: response,
                        asset   : asset,
                        task    : task,
                        job     : job,
                        *       : ReactionScriptCode.values()
                                .collectEntries {
                            [(it.name()): it]
                        }
                ])
                break
        }

        return binding
    }

    /**
     * Fluent API for Builder pattern.
     */
    static class Builder {

        ActionRequest request
        ApiActionResponse response
        ReactionAssetFacade asset
        ReactionTaskFacade task
        ApiActionJob job


        Builder with (ActionRequest request) {
            this.request = request
            this
        }

        Builder with (ApiActionResponse response) {
            this.response = response
            this
        }

        Builder with (ReactionAssetFacade asset) {
            this.asset = asset
            this
        }

        Builder with (ReactionTaskFacade task) {
            this.task = task
            this
        }

        Builder with (ApiActionJob job) {
            this.job
            this
        }

        ApiActionScriptBinding build (ReactionScriptCode code) {

            ApiActionScriptBinding binding

            switch (code) {
                case ReactionScriptCode.PRE:
                    binding = new ApiActionScriptBinding([
                            request: request,
                            asset  : asset,
                            task   : task,
                            job    : job
                    ])
                    break
                case ReactionScriptCode.EVALUATE:
                    binding = new ApiActionScriptBinding([
                            request : request,
                            response: response,
                            *       : ReactionScriptCode.values()
                                    .collectEntries {
                                [(it.name()): it]
                            }
                    ])
                    break
                default:
                    binding = new ApiActionScriptBinding([
                            request : request,
                            response: response,
                            asset   : asset,
                            task    : task,
                            job     : job,
                            *       : ReactionScriptCode.values()
                                    .collectEntries {
                                [(it.name()): it]
                            }
                    ])
                    break
            }

            return binding
        }
    }
}

@Builder(builderStrategy = ExternalStrategy, forClass = ApiActionScriptBinding)
class ApiActionScriptBindingBuilder {

    ApiActionScriptBinding build (ReactionScriptCode code) {

        ApiActionScriptBinding binding

        switch (code) {
            case ReactionScriptCode.PRE:
                binding = new ApiActionScriptBinding([
                        request: request,
                        asset  : asset,
                        task   : task,
                        job    : job
                ])
                break
            case ReactionScriptCode.EVALUATE:
                binding = new ApiActionScriptBinding([
                        request : request,
                        response: response,
                        *       : ReactionScriptCode.values()
                                .collectEntries {
                            [(it.name()): it]
                        }
                ])
                break
            default:
                binding = new ApiActionScriptBinding([
                        request : request,
                        response: response,
                        asset   : asset,
                        task    : task,
                        job     : job,
                        *       : ReactionScriptCode.values()
                                .collectEntries {
                            [(it.name()): it]
                        }
                ])
                break
        }

        return binding
    }
}
