package net.transitionmanager.service

import grails.test.mixin.TestFor
import net.transitionmanager.integration.*
import spock.lang.Specification

@TestFor(ApiActionService)
class ApiActionServiceSpec extends Specification {

    def 'test can evaluate a reaction PRE script' () {

        setup: 'Given an instance of ApiActionScriptProcessor'

            ActionRequest actionRequest = new ActionRequest(['property1': 'value1', 'format': 'xml'])
            ApiActionResponse actionResponse = new ApiActionResponse()
            ReactionAssetFacade asset = new ReactionAssetFacade()
            ReactionTaskFacade task = new ReactionTaskFacade()
            ApiActionJob job = new ApiActionJob()

        when: 'A PRE script is evaluated'
            String script = """
                request.param.format = 'json'
                request.headers.add('header1', 'value1')
                        
                request.config.setProperty('proxyAuthHost', '123.88.23.42')
                request.config.setProperty('proxyAuthPort', 8080)
			""".stripIndent()

            def response = service.evaluateReactionScript(
                    ReactionScriptCode.PRE,
                    script,
                    actionRequest,
                    actionResponse,
                    task,
                    asset,
                    job
            )

        then: 'Service result returns the last interaction within the script'
            with(response) {
                !result
            }
    }

    def 'test can evaluate a reaction EVALUATE script' () {

        setup: 'Given an instance of ApiActionScriptProcessor'

            ActionRequest actionRequest = new ActionRequest(['property1': 'value1', 'format': 'xml'])
            ApiActionResponse actionResponse = new ApiActionResponse()
            actionResponse.data = 'anything'
            actionResponse.status = ReactionHttpStatus.OK

            ReactionAssetFacade asset = new ReactionAssetFacade()
            ReactionTaskFacade task = new ReactionTaskFacade()
            ApiActionJob job = new ApiActionJob()

        when: 'A EVALUATE script is evaluated'
            String script = """
                if (response.status == SC.OK) {
                    return SUCCESS
                } else {
                    return ERROR
                }
			""".stripIndent()

            def response = service.evaluateReactionScript(
                    ReactionScriptCode.EVALUATE,
                    script,
                    actionRequest,
                    actionResponse,
                    task,
                    asset,
                    job
            )

        then: 'Service result has a ReactionScriptCode'
            with(response) {
                result == ReactionScriptCode.SUCCESS
            }
    }

    def 'test can evaluate a reaction SUCCESS script' () {

        setup: 'Given an instance of ApiActionScriptProcessor'

            ActionRequest actionRequest = new ActionRequest(['property1': 'value1', 'format': 'xml'])
            ApiActionResponse actionResponse = new ApiActionResponse()
            actionResponse.data = 'anything'
            actionResponse.status = ReactionHttpStatus.OK

            ReactionAssetFacade asset = new ReactionAssetFacade()
            ReactionTaskFacade task = new ReactionTaskFacade()
            ApiActionJob job = new ApiActionJob()

        when: 'A EVALUATE script is evaluated'
            String script = """
                // Check to see if the asset is a VM
                if ( asset.isaDevice() && asset.isaDatabase() ) {
                    task.done()
                }
			""".stripIndent()

            def response = service.evaluateReactionScript(
                    ReactionScriptCode.SUCCESS,
                    script,
                    actionRequest,
                    actionResponse,
                    task,
                    asset,
                    job
            )

        then: 'Service result has a ReactionScriptCode'
            with(response) {
                !!result
            }

        and: 'The asset and task object received the correct messages'
            task.isDone()
    }

    void 'test can throw an Exception if a reaction EVALUATE script does not return a ReactionScriptCode' () {
        setup:
            ActionRequest actionRequest = new ActionRequest(['property1': 'value1', 'format': 'xml'])
            ApiActionResponse actionResponse = new ApiActionResponse()
            actionResponse.data = 'anything'
            actionResponse.status = ReactionHttpStatus.NOT_FOUND

            ReactionAssetFacade asset = new ReactionAssetFacade()
            ReactionTaskFacade task = new ReactionTaskFacade()
            ApiActionJob job = new ApiActionJob()


        when: 'A EVALUATE script is evaluated that does not return a an instance of ReactionScriptCode'
            String script = """
                 if (response.status == SC.OK) {
                    return SUCCESS
                 } 
			""".stripIndent()

            service.evaluateReactionScript(
                    ReactionScriptCode.EVALUATE,
                    script,
                    actionRequest,
                    actionResponse.asImmutable(),
                    task,
                    asset,
                    job
            )

        then: 'An Exception is thrown'
            Exception e = thrown(Exception)
            e.message == 'Script must return SUCCESS or ERROR'
    }
}
