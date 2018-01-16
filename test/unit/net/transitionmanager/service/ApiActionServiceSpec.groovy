package net.transitionmanager.service

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import net.transitionmanager.i18n.Message
import net.transitionmanager.integration.*
import org.springframework.context.i18n.LocaleContextHolder
import spock.lang.Specification

@TestFor(ApiActionService)
@TestMixin(GrailsUnitTestMixin)
class ApiActionServiceSpec extends Specification {

	static doWithSpring = {

		apiActionScriptBindingBuilder(ApiActionScriptBindingBuilder) { bean ->
			bean.scope = 'prototype'
			messageSource = ref('messageSource')
		}
	}

	def setup() {
		service.grailsApplication = grailsApplication
	}

	def 'test can evaluate a reaction PRE script'() {

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

			Map<String, ?> response = service.evaluateReactionScript(
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

	def 'test can evaluate a reaction EVALUATE script'() {

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

			Map<String, ?> response = service.evaluateReactionScript(
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

	def 'test can evaluate a reaction SUCCESS script'() {

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

			Map<String, ?> response = service.evaluateReactionScript(
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

	void 'test can throw an Exception if a reaction EVALUATE script does not return a ReactionScriptCode'() {
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
			ApiActionException e = thrown(ApiActionException)
			e.message == 'Script must return SUCCESS or ERROR'
	}

	void 'test can throw an Exception with i18n message if a reaction EVALUATE script does not return a ReactionScriptCode'() {
		setup:

			LocaleContextHolder.setLocale(Locale.FRENCH)
			messageSource.addMessage(
					Message.ApiActionMustReturnResults,
					Locale.FRENCH,
					'Le script doit renvoyer SUCCESS ou ERROR')

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
			ApiActionException e = thrown(ApiActionException)
			e.message == 'Le script doit renvoyer SUCCESS ou ERROR'

		cleanup:
			LocaleContextHolder.resetLocaleContext()
	}

	void 'test can validate syntax for a list fo scripts'() {

		given:
			List<ApiActionScriptCommand> scripts = [
					new ApiActionScriptCommand(code: 'EVALUATE', script: 'if (response.status == SC.OK) {\n   return SUCCESS\n} else {\n   return ERROR\n}', reactionScriptCode: ReactionScriptCode.EVALUATE),
					new ApiActionScriptCommand(code: 'SUCCESS', script: 'task.done()', reactionScriptCode: ReactionScriptCode.SUCCESS)
			]

		when: 'a List of scripts is evaluated'
			List<Map<String, ?>> results = service.validateSyntax(scripts)

		then: 'service returns a list of results associated with codes'
			results.size() == 2
			with (results[0]) {
				code == 'EVALUATE'
				result == 'ERROR'
				!error
			}

			with (results[1]) {
				code == 'SUCCESS'
				result == 'true'
				!error
			}
	}
}
