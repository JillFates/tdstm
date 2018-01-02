package net.transitionmanager.integration

import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ApiActionScriptBinding
import net.transitionmanager.integration.ApiActionScriptProcessor
import net.transitionmanager.integration.ReactionAssetFacade
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.integration.ReactionTaskFacade
import spock.lang.Specification

import static net.transitionmanager.integration.ReactionHttpStatusCodes.NOT_FOUND
import static net.transitionmanager.integration.ReactionHttpStatusCodes.OK
import static net.transitionmanager.integration.ReactionScriptCode.ERROR
import static net.transitionmanager.integration.ReactionScriptCode.SUCCESS


class ApiActionScriptProcessorSpec extends Specification {

    def setupSpec () {

    }

    def cleanupSpec () {

    }

    def setup () {

    }

    void 'test can invoke a simple PRE Script to customize Http4 component with params and headers' () {

        given:
            ActionRequest request = new ActionRequest(['format': 'xml'])

            ApiActionScriptProcessor apiActionProcessor = new ApiActionScriptProcessor(
                    request,
                    new ApiActionResponse(),
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )
            ApiActionScriptBinding scriptBinding = apiActionProcessor.scriptBindingFor(ReactionScriptCode.PRE)

        when: 'The PRE script is evaluated'
            new GroovyShell(this.class.classLoader, scriptBinding)
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
                        
                    """.stripIndent(), ApiActionScriptProcessor.class.name)

        then: 'All the correct variables were bound'
            scriptBinding.hasVariable('request')
            !scriptBinding.hasVariable('response')
            scriptBinding.hasVariable('task')
            scriptBinding.hasVariable('asset')
            scriptBinding.hasVariable('job')
            scriptBinding.hasVariable('SC')

        and: 'the request object was modified correctly'
            request.param.format == 'json'
            request.config.getProperty('httpClient.socketTimeout') == 5000
            request.config.getProperty('httpClient.connectionTimeout') == 5000
            request.config.getProperty('proxyAuthHost') == '123.88.23.42'
            request.config.getProperty('proxyAuthPort') == 8080
    }

    void 'test can invoke a simple EVALUATE Script and return a ReactionScriptCode result' () {

        setup:
            ApiActionResponse response = new ApiActionResponse()
            response.data = 'anything'
            response.status = status

            ApiActionScriptProcessor apiActionProcessor = new ApiActionScriptProcessor(
                    new ActionRequest(['property1': 'value1']),
                    response.asImmutable(),
                    GroovyMock(ReactionAssetFacade),
                    GroovyMock(ReactionTaskFacade),
                    GroovyMock(ApiActionJob)
            )

            ApiActionScriptBinding scriptBinding = apiActionProcessor.scriptBindingFor(ReactionScriptCode.EVALUATE)

        expect: 'The evaluation of the script returns a ReactionScriptCode'
            new GroovyShell(this.class.classLoader, scriptBinding)
                    .evaluate("""
                        if (response.status == SC.OK) {
                           return SUCCESS
                        } else {
                           return ERROR
                        }
                    """.stripIndent(), ApiActionScriptProcessor.class.name) == reaction

        and: 'And all the variables were bound correctly'
            scriptBinding.hasVariable('request') == hasRequest
            scriptBinding.hasVariable('response') == hasResponse
            scriptBinding.hasVariable('task') == hasTask
            scriptBinding.hasVariable('asset') == hasAsset
            scriptBinding.hasVariable('job') == hasJob
            scriptBinding.hasVariable('SC') == hasSC

        where:
            status    || hasRequest | hasResponse | hasTask | hasAsset | hasJob | hasSC | reaction
            OK        || true       | true        | false   | false    | false  | true  | SUCCESS
            NOT_FOUND || true       | true        | false   | false    | false  | true  | ERROR
    }

    void 'test can invoke a simple SUCCESS Script to check Asset if asset is a Device or an Application and change a task to done' () {

        given:
            ReactionAssetFacade asset = GroovyMock()
            ReactionTaskFacade task = GroovyMock()

            ApiActionScriptProcessor apiActionProcessor = new ApiActionScriptProcessor(
                    new ActionRequest(['property1': 'value1']),
                    new ApiActionResponse(),
                    asset,
                    task,
                    GroovyMock(ApiActionJob)
            )
            ApiActionScriptBinding scriptBinding = apiActionProcessor.scriptBindingFor(ReactionScriptCode.SUCCESS)

        when: 'The script is evaluated'
            new GroovyShell(this.class.classLoader, scriptBinding)
                    .evaluate("""
                    // Check to see if the asset is a VM
                    if ( !asset.isaDevice() && !asset.isaDatabase() ) {
                       task.done()
                    } 
                    """.stripIndent(), ApiActionScriptProcessor.class.name)

        then: 'The asset and task object received the correct messages'
            1 * task.done()
            1 * asset.isaDevice()
            1 * asset.isaDatabase()

        and: 'All the correct variables were bound'
            scriptBinding.hasVariable('request')
            scriptBinding.hasVariable('response')
            scriptBinding.hasVariable('task')
            scriptBinding.hasVariable('asset')
            scriptBinding.hasVariable('job')
            scriptBinding.hasVariable('SC')
    }

    void 'test can invoke a simple FINALIZE Script to evaluate what has been performed' () {

        given:

            ReactionTaskFacade task = GroovyMock(ReactionTaskFacade)

            ApiActionScriptProcessor apiActionProcessor = new ApiActionScriptProcessor(
                    new ActionRequest(['property1': 'value1']),
                    new ApiActionResponse(),
                    GroovyMock(ReactionAssetFacade),
                    task,
                    GroovyMock(ApiActionJob)
            )
            ApiActionScriptBinding scriptBinding = apiActionProcessor.scriptBindingFor(ReactionScriptCode.FINAL)

        when: 'The script is evaluated'
            new GroovyShell(this.class.classLoader, scriptBinding)
                    .evaluate("""
                        // Complete the task 
                        task.done()
                        
                        // Add a note to the task
                        task.addNote('hickory dickery dock, a mouse ran up the clock')
                    """.stripIndent(), ApiActionScriptProcessor.class.name)

        then: 'The asset and task object received the correct messages'
            1 * task.done()
            1 * task.addNote('hickory dickery dock, a mouse ran up the clock')

        and: 'All the correct variables were bound'
            scriptBinding.hasVariable('request')
            scriptBinding.hasVariable('response')
            scriptBinding.hasVariable('task')
            scriptBinding.hasVariable('asset')
            scriptBinding.hasVariable('job')
            scriptBinding.hasVariable('SC')
    }
}

