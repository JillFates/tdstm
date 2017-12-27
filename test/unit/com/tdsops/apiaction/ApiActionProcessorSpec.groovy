package com.tdsops.apiaction

import spock.lang.Specification

class ApiActionProcessorSpec extends Specification {


    def setupSpec () {

    }

    def cleanupSpec () {

    }

    def setup () {

    }

    /**
     * PRE Script section.
     */
    void 'test can invoke a simple PRE Script to customize Http4 component' () {

        given:
            ApiActionProcessor apiActionProcessor = new ApiActionProcessor(
                    GroovyMock(ApiActionRequest),
                    GroovyMock(ApiActionResponse),
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )
            ApiActionBinding actionBinding = apiActionProcessor.preScriptBinding()
        when:
            new GroovyShell(this.class.classLoader, actionBinding)
                    .evaluate("""
                        
                    """.stripIndent(), ApiActionProcessor.class.name)

        then:
            hasAllVariables(apiActionProcessor)
            hasPreScriptBindingVariables(actionBinding)
    }

    /**
     * EVALUATE Script section.
     */
    void 'test can invoke a simple EVALUATE Script to customize Http4 component' () {

        given:
            ApiActionProcessor apiActionProcessor = new ApiActionProcessor(
                    GroovyMock(ApiActionRequest),
                    GroovyMock(ApiActionResponse),
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )

            ApiActionBinding actionBinding = apiActionProcessor.evaluateScriptBinding()

        when:
            new GroovyShell(this.class.classLoader, apiActionProcessor.evaluateScriptBinding())
                    .evaluate("""
                        
                    """.stripIndent(), ApiActionProcessor.class.name)

        then:
            hasAllVariables(apiActionProcessor)
            hasEvaluateScriptBindingVariables(actionBinding)
    }

    /**
     * EVALUATE Script section.
     */
    void 'test can invoke a simple EVALUATE Script to customize SUCCESS or ERROR invocation' () {

        given:
            ApiActionProcessor apiActionProcessor = new ApiActionProcessor(
                    GroovyMock(ApiActionRequest),
                    GroovyMock(ApiActionResponse),
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )
            ApiActionBinding actionBinding = apiActionProcessor.evaluateScriptBinding()

        when:
            new GroovyShell(this.class.classLoader, actionBinding)
                    .evaluate("""
                        
                    """.stripIndent(), ApiActionProcessor.class.name)

        then:
            hasAllVariables(apiActionProcessor)
            hasEvaluateScriptBindingVariables(actionBinding)
    }

    /**
     * FINALIZE Script section.
     */
    void 'test can invoke a simple FINALIZE Script to evaluate what has been performed' () {

        given:
            ApiActionProcessor apiActionProcessor = new ApiActionProcessor(
                    GroovyMock(ApiActionRequest),
                    GroovyMock(ApiActionResponse),
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )
            ApiActionBinding actionBinding = apiActionProcessor.resultScriptBinding()

        when:
            new GroovyShell(this.class.classLoader, actionBinding)
                    .evaluate("""
                        
                    """.stripIndent(), ApiActionProcessor.class.name)

        then:
            hasAllVariables(apiActionProcessor)
            hasResultScriptBindingVariables(actionBinding)
    }

    /**
     * Custom matcher to check if an instance of ApiActionProcessor is complete
     * @param apiActionProcessor
     */
    private void hasAllVariables (final ApiActionProcessor apiActionProcessor) {
        apiActionProcessor.request != null
        apiActionProcessor.response != null
        apiActionProcessor.asset != null
        apiActionProcessor.task != null
        apiActionProcessor.job != null
    }

    private void hasEvaluateScriptBindingVariables (final ApiActionBinding actionBinding) {
        actionBinding.getVariable('request') != null
        actionBinding.getVariable('response') != null
        actionBinding.getVariable('task') == null
        actionBinding.getVariable('asset') == null
        actionBinding.getVariable('job') == null
        actionBinding.getVariable('SC') != null
    }

    private void hasPreScriptBindingVariables (final ApiActionBinding actionBinding) {
        actionBinding.getVariable('request') != null
        actionBinding.getVariable('response') == null
        actionBinding.getVariable('task') != null
        actionBinding.getVariable('asset') != null
        actionBinding.getVariable('job') == null
        actionBinding.getVariable('SC') != null
    }

    private void hasResultScriptBindingVariables (final ApiActionBinding actionBinding) {
        actionBinding.getVariable('request') != null
        actionBinding.getVariable('response') != null
        actionBinding.getVariable('task') != null
        actionBinding.getVariable('asset') != null
        actionBinding.getVariable('job') != null
        actionBinding.getVariable('SC') != null
    }

}
