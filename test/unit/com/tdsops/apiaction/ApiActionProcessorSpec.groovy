package com.tdsops.apiaction

import spock.lang.Specification

import static com.tdsops.apiaction.ReactionHttpStatusCodes.NOT_FOUND
import static com.tdsops.apiaction.ReactionHttpStatusCodes.OK
import static com.tdsops.apiaction.ReactionScriptCode.ERROR
import static com.tdsops.apiaction.ReactionScriptCode.SUCCESS


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
    void 'test can invoke a simple PRE Script to customize Http4 component with params and headers' () {

        given:
            ApiActionRequest request = GroovyMock(ApiActionRequest) {
                final Map<String, ?> params = [:]
                final Map<String, ?> config = [:]

                getParams() >> params
                getConfig() >> new Expando()
            }

            def apiActionProcessor = new ApiActionProcessor(
                    request,
                    GroovyMock(ApiActionResponse),
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )
            ApiActionBinding actionBinding = apiActionProcessor.preScriptBinding()

        when:
            new GroovyShell(this.class.classLoader, actionBinding)
                    .evaluate("""
                        request.params.format = 'json'
                        
                        // Set the socket and connect to 5 seconds
                        request.config.setProperty('httpClient.socketTimeout', 5000)
                        request.config.setProperty('httpClient.connectionTimeout', 5000)
                        
                        // Set up a proxy for the call
                        request.config.setProperty('proxyAuthHost', '123.88.23.42')
                        request.config.setProperty('proxyAuthPort', 8080)
                        
                        // Set the charset for the exchange
                        request.config.setProperty('Exchange.CHARSET_NAME', 'ISO-8859-1')
                        
                        // Set the content-type to JSON
                        request.config.setProperty('Exchange.CONTENT_TYPE', 'application/json')
                        
                    """.stripIndent(), ApiActionProcessor.class.name)

        then:
            actionBinding.hasVariable('request')
            !actionBinding.hasVariable('response')
            actionBinding.hasVariable('task')
            actionBinding.hasVariable('asset')
            actionBinding.hasVariable('job')
            actionBinding.hasVariable('SC')

        and:
            request.config.getProperty('httpClient.socketTimeout') == 5000
            request.config.getProperty('httpClient.connectionTimeout') == 5000
            request.config.getProperty('proxyAuthHost') == '123.88.23.42'
            request.config.getProperty('proxyAuthPort') == 8080
    }

    /**
     * EVALUATE Script section.
     */
    void 'test can invoke a simple EVALUATE Script and return a ReactionScriptCode result' () {

        setup:
            ApiActionResponse response = GroovyMock(ApiActionResponse) {
                getStatus() >> status
            }

            ApiActionProcessor apiActionProcessor = new ApiActionProcessor(
                    GroovyMock(ApiActionRequest),
                    response,
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )

            ApiActionBinding actionBinding = apiActionProcessor.evaluateScriptBinding()

        expect:
            new GroovyShell(this.class.classLoader, apiActionProcessor.evaluateScriptBinding())
                    .evaluate("""
                        if (response.status == SC.OK) {
                           return SUCCESS
                        } else {
                           return ERROR
                        }
                    """.stripIndent(), ApiActionProcessor.class.name) == reaction

        and:
            actionBinding.hasVariable('request') == hasRequest
            actionBinding.hasVariable('response') == hasResponse
            actionBinding.hasVariable('task') == hasTask
            actionBinding.hasVariable('asset') == hasAsset
            actionBinding.hasVariable('job') == hasJob
            actionBinding.hasVariable('SC') == hasSC

        where:
            status    || hasRequest | hasResponse | hasTask | hasAsset | hasJob | hasSC | reaction
            OK        || true       | true        | false   | false    | false  | true  | SUCCESS
            NOT_FOUND || true       | true        | false   | false    | false  | true  | ERROR
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
        assert apiActionProcessor.request != null
        assert apiActionProcessor.response != null
        assert apiActionProcessor.asset != null
        assert apiActionProcessor.task != null
        assert apiActionProcessor.job != null
    }
    /**
     * It checks if all the correct variables were bound in an EVALUATE script
     * @param actionBinding
     */
    private void hasEvaluateScriptBindingVariables (final ApiActionBinding actionBinding) {
        assert actionBinding.hasVariable('request')
        assert actionBinding.hasVariable('response')
        assert !actionBinding.hasVariable('task')
        assert !actionBinding.hasVariable('asset')
        assert !actionBinding.hasVariable('job')
        assert actionBinding.hasVariable('SC')
    }

    /**
     * It checks if all the correct variables were bound in an PRE script
     * @param actionBinding
     */
    private void hasPreScriptBindingVariables (final ApiActionBinding actionBinding) {
        assert actionBinding.getVariable('request') != null
        assert actionBinding.getVariable('response') == null
        assert actionBinding.getVariable('task') != null
        assert actionBinding.getVariable('asset') != null
        assert actionBinding.getVariable('job') == null
        assert actionBinding.getVariable('SC') != null
    }

    /**
     * It checks if all the correct variables were bound in an result script
     * @param actionBinding
     */
    private void hasResultScriptBindingVariables (final ApiActionBinding actionBinding) {
        assert actionBinding.getVariable('request') != null
        assert actionBinding.getVariable('response') != null
        assert actionBinding.getVariable('task') != null
        assert actionBinding.getVariable('asset') != null
        assert actionBinding.getVariable('job') != null
        assert actionBinding.getVariable('SC') != null
    }

}

