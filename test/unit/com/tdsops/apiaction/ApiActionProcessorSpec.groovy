package com.tdsops.apiaction

import net.transitionmanager.integration.ActionRequest
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
            ActionRequest request = new ActionRequest(['format': 'xml'])

            ApiActionProcessor apiActionProcessor = new ApiActionProcessor(
                    request,
                    GroovyMock(ApiActionResponse),
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )
            ApiActionBinding actionBinding = apiActionProcessor.preScriptBinding()

        when: 'The PRE script is evaluated'
            new GroovyShell(this.class.classLoader, actionBinding)
                    .evaluate("""
                        request.param.format = 'json'
                        request.headers.add('header1', 'value1')
                        
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

        then: 'All the correct varibales were bound'
            actionBinding.hasVariable('request')
            !actionBinding.hasVariable('response')
            actionBinding.hasVariable('task')
            actionBinding.hasVariable('asset')
            actionBinding.hasVariable('job')
            actionBinding.hasVariable('SC')

        and: 'the request object was modified correctly'
            request.param.format == 'json'
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
                    new ActionRequest(['property1': 'value1']),
                    response,
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )

            ApiActionBinding actionBinding = apiActionProcessor.evaluateScriptBinding()

        expect: 'The evaluation of the script returns a ReactionScriptCode'
            new GroovyShell(this.class.classLoader, apiActionProcessor.evaluateScriptBinding())
                    .evaluate("""
                        if (response.status == SC.OK) {
                           return SUCCESS
                        } else {
                           return ERROR
                        }
                    """.stripIndent(), ApiActionProcessor.class.name) == reaction

        and: 'And all the variables were bound correctly'
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
    void 'test can invoke a simple SUCCESS Script to check Asset if asset is a Device or an Application and change a task to done' () {

        given:
            ReactionAssetFacade asset = GroovyMock()
            ReactionTaskFacade task = GroovyMock()

            ApiActionProcessor apiActionProcessor = new ApiActionProcessor(
                    new ActionRequest(['property1': 'value1']),
                    GroovyMock(ApiActionResponse),
                    asset,
                    task,
                    GroovyMock(ApiActionJob)
            )
            ApiActionBinding actionBinding = apiActionProcessor.resultScriptBinding()

        when: 'The script is evaluated'
            new GroovyShell(this.class.classLoader, actionBinding)
                    .evaluate("""
                    // Check to see if the asset is a VM
                    if ( !asset.isaDevice() && !asset.isaDatabase() ) {
                       task.done()
                    } 
                    """.stripIndent(), ApiActionProcessor.class.name)

        then: 'The asset and task object received the correct messages'
            1 * task.done()
            1 * asset.isaDevice()
            1 * asset.isaDatabase()

        and: 'All the correct varibales were bound'
            actionBinding.hasVariable('request')
            actionBinding.hasVariable('response')
            actionBinding.hasVariable('task')
            actionBinding.hasVariable('asset')
            actionBinding.hasVariable('job')
            actionBinding.hasVariable('SC')
    }

    /**
     * FINALIZE Script section.
     */
    void 'test can invoke a simple FINALIZE Script to evaluate what has been performed' () {

        given:

            ReactionTaskFacade task = GroovyMock(ReactionTaskFacade)

            ApiActionProcessor apiActionProcessor = new ApiActionProcessor(
                    new ActionRequest(['property1': 'value1']),
                    GroovyMock(ApiActionResponse),
                    GroovyMock(ReactionAssetFacade),
                    task,
                    GroovyMock(ApiActionJob)
            )
            ApiActionBinding actionBinding = apiActionProcessor.resultScriptBinding()

        when: 'The script is evaluated'
            new GroovyShell(this.class.classLoader, actionBinding)
                    .evaluate("""
                        // Complete the task 
                        task.done()
                        
                        // Add a note to the task
                        task.addNote('hickory dickery dock, a mouse ran up the clock')
                    """.stripIndent(), ApiActionProcessor.class.name)

        then: 'The asset and task object received the correct messages'
            1 * task.done()
            1 * task.addNote('hickory dickery dock, a mouse ran up the clock')

        and: 'All the correct varibales were bound'
            actionBinding.hasVariable('request')
            actionBinding.hasVariable('response')
            actionBinding.hasVariable('task')
            actionBinding.hasVariable('asset')
            actionBinding.hasVariable('job')
            actionBinding.hasVariable('SC')
    }
}

