package net.transitionmanager.integration

import spock.lang.Specification
import spock.lang.Unroll

import static ReactionHttpStatus.NOT_FOUND
import static ReactionHttpStatus.OK
import static net.transitionmanager.integration.ReactionScriptCode.ERROR
import static net.transitionmanager.integration.ReactionScriptCode.SUCCESS


class ApiActionScriptProcessorSpec extends Specification {

    def setupSpec () {

    }

    def cleanupSpec () {

    }

    def setup () {

    }

    @Unroll
    void 'test can create a script binding context based on a ReactionScriptCode.#reactionScriptCode' () {

        setup:
            ApiActionScriptBinding scriptBinding = new ApiActionScriptBinding.Builder()
                    .with(new ActionRequest(['property1': 'value1']))
                    .with(new ApiActionResponse().asImmutable())
                    .with(new ReactionAssetFacade())
                    .with(new ReactionTaskFacade())
                    .with(new ApiActionJob())
                    .build(reactionScriptCode)

        expect: 'All the bound variables were correctly set within the api action script binding'
            scriptBinding.hasVariable('request') == hasRequest
            scriptBinding.hasVariable('response') == hasResponse
            scriptBinding.hasVariable('task') == hasTask
            scriptBinding.hasVariable('asset') == hasAsset
            scriptBinding.hasVariable('job') == hasJob
            scriptBinding.hasVariable('SC') == hasSC

        where: 'The ReactionScriptCode instance is defined'
            reactionScriptCode          || hasRequest | hasResponse | hasTask | hasAsset | hasJob | hasSC
            ReactionScriptCode.EVALUATE || true       | true        | false   | false    | false  | true
            ReactionScriptCode.SUCCESS  || true       | true        | true    | true     | true   | true
            ReactionScriptCode.ERROR    || true       | true        | true    | true     | true   | true
            ReactionScriptCode.DEFAULT  || true       | true        | true    | true     | true   | true
            ReactionScriptCode.FAILED   || true       | true        | true    | true     | true   | true
            ReactionScriptCode.TIMEDOUT || true       | true        | true    | true     | true   | true
            ReactionScriptCode.LAPSED   || true       | true        | true    | true     | true   | true
            ReactionScriptCode.STALLED  || true       | true        | true    | true     | true   | true
            ReactionScriptCode.PRE      || true       | false       | true    | true     | true   | true
            ReactionScriptCode.FINAL    || true       | true        | true    | true     | true   | true

    }

    void 'test can throw an Exception if you try to create a script binding without the correct context objects'(){

        when: 'Tries to create an instance of ApiActionScriptBinding for a ReactionScriptCode without the correct context objects'
            new ApiActionScriptBinding.Builder()
                    .with(new ReactionAssetFacade())
                    .with(new ReactionTaskFacade())
                    .with(new ApiActionJob())
                    .build(ReactionScriptCode.PRE)

        then: 'An Exception is thrown'
            Exception e = thrown(Exception)
            e.message == 'Can not build a biding context for PRE without request object'
    }

    void 'test can invoke a simple PRE Script to customize Http4 component with params and headers' () {

        given:
            ActionRequest request = new ActionRequest(['format': 'xml'])

            ApiActionScriptBinding scriptBinding = new ApiActionScriptBinding.Builder()
                    .with(request)
                    .with(new ApiActionResponse())
                    .with(new ReactionAssetFacade())
                    .with(new ReactionTaskFacade())
                    .with(new ApiActionJob())
                    .build(ReactionScriptCode.PRE)

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
                        
                    """.stripIndent(), ApiActionScriptBinding.class.name)

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

    void 'test can throw an MissingPropertyException if PRE try to use response object' () {

        given:
            ActionRequest request = new ActionRequest(['format': 'xml'])

            ApiActionScriptBinding scriptBinding = new ApiActionScriptBinding.Builder()
                    .with(request)
                    .with(new ApiActionResponse().asImmutable())
                    .with(new ReactionAssetFacade())
                    .with(new ReactionTaskFacade())
                    .with(new ApiActionJob())
                    .build(ReactionScriptCode.PRE)

        when: 'The PRE script is evaluated'
            new GroovyShell(this.class.classLoader, scriptBinding)
                    .evaluate("""
                        request.param.format = 'json'
                        request.headers.add('header1', 'value1')
                        
                        if (response.status == SC.OK) {
                           return SUCCESS
                        } else {
                           return ERROR
                        }
                    """.stripIndent(), ApiActionScriptBinding.class.name)

        then: 'A MissingPropertyException is thrown'
            MissingPropertyException e = thrown(MissingPropertyException)
            e.message == 'No such property: response for class: net_transitionmanager_integration'
    }

    void 'test can invoke a simple EVALUATE Script and return a ReactionScriptCode result' () {

        setup:
            ApiActionResponse response = new ApiActionResponse()
            response.data = 'anything'
            response.status = status

            ApiActionScriptBinding scriptBinding = new ApiActionScriptBinding.Builder()
                    .with(new ActionRequest(['property1': 'value1']))
                    .with(response.asImmutable())
                    .with(new ReactionAssetFacade())
                    .with(new ReactionTaskFacade())
                    .with(new ApiActionJob())
                    .build(ReactionScriptCode.EVALUATE)

        expect: 'The evaluation of the script returns a ReactionScriptCode'
            new GroovyShell(this.class.classLoader, scriptBinding)
                    .evaluate("""
                        if (response.status == SC.OK) {
                           return SUCCESS
                        } else {
                           return ERROR
                        }
                    """.stripIndent(), ApiActionScriptBinding.class.name) == reaction

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
            ReactionAssetFacade asset = new ReactionAssetFacade()
            ReactionTaskFacade task = new ReactionTaskFacade()

            ApiActionScriptBinding scriptBinding = new ApiActionScriptBinding.Builder()
                    .with(new ActionRequest(['property1': 'value1']))
                    .with(new ApiActionResponse().asImmutable())
                    .with(asset)
                    .with(task)
                    .with(new ApiActionJob())
                    .build(ReactionScriptCode.SUCCESS)

        when: 'The script is evaluated'
            new GroovyShell(this.class.classLoader, scriptBinding)
                    .evaluate("""
                    // Check to see if the asset is a VM
                    if ( asset.isaDevice() && asset.isaDatabase() ) {
                       task.done()
                    } 
                    """.stripIndent(), ApiActionScriptBinding.class.name)

        then: 'The asset and task object received the correct messages'
            task.isDone()

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
            ReactionTaskFacade task = new ReactionTaskFacade()

            ApiActionScriptBinding scriptBinding = new ApiActionScriptBinding.Builder()
                    .with(new ActionRequest(['property1': 'value1']))
                    .with(new ApiActionResponse().asImmutable())
                    .with(new ReactionAssetFacade())
                    .with(task)
                    .with(new ApiActionJob())
                    .build(ReactionScriptCode.FINAL)

        when: 'The script is evaluated'
            new GroovyShell(this.class.classLoader, scriptBinding)
                    .evaluate("""
                        // Complete the task 
                        task.done()
                        
                        // Add a note to the task
                        task.addNote('hickory dickery dock, a mouse ran up the clock')
                    """.stripIndent(), ApiActionScriptBinding.class.name)

        then: 'The asset and task object received the correct messages'
            task.isDone()
            task.getNote() == 'hickory dickery dock, a mouse ran up the clock'

        and: 'All the correct variables were bound'
            scriptBinding.hasVariable('request')
            scriptBinding.hasVariable('response')
            scriptBinding.hasVariable('task')
            scriptBinding.hasVariable('asset')
            scriptBinding.hasVariable('job')
            scriptBinding.hasVariable('SC')
    }
}

