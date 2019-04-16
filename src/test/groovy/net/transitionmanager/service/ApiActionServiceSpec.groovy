package net.transitionmanager.service

import net.transitionmanager.action.ApiActionService
import net.transitionmanager.common.MessageSourceService
import net.transitionmanager.task.AssetComment
import net.transitionmanager.asset.AssetEntity
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.person.Person
import net.transitionmanager.i18n.Message
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.exception.ApiActionException
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ApiActionScriptBindingBuilder
import net.transitionmanager.integration.ApiActionScriptCommand
import net.transitionmanager.integration.ReactionHttpStatus
import net.transitionmanager.integration.ReactionScriptCode
import net.transitionmanager.task.TaskFacade
import net.transitionmanager.task.TaskService
import org.springframework.context.i18n.LocaleContextHolder
import spock.lang.Specification

class ApiActionServiceSpec  extends Specification implements ServiceUnitTest<ApiActionService>, DataTest {

	def setup() {
		defineBeans {
			messageSourceService(MessageSourceService) { bean ->
				messageSource = ref('messageSource')
			}

			apiActionScriptBindingBuilder(ApiActionScriptBindingBuilder) { bean ->
				bean.scope = 'prototype'
				messageSourceService = ref('messageSourceService')
			}

			taskFacade(TaskFacade) { bean ->
				bean.scope = 'prototype'
			}
		}
		service.grailsApplication = grailsApplication
	}

	def 'test can validate syntax for a reaction PRE script'() {

		setup: 'Given an instance of ApiActionScriptProcessor'

			ActionRequest actionRequest = new ActionRequest(['property1': 'value1', 'format': 'xml'])
			ApiActionResponse actionResponse = new ApiActionResponse()
			AssetFacade asset = new AssetFacade(null, [:], true)
			TaskFacade task = new TaskFacade()
			ApiActionJob job = new ApiActionJob()

		when: 'A PRE script is evaluated'
			String script = """
				request.params.format = 'json'
				request.headers.add('header1', 'value1')
						
				request.config.setProperty('proxyAuthHost', '123.88.23.42')
				request.config.setProperty('proxyAuthPort', 8080)
			""".stripIndent()

			Map<String, ?> response = service.compileReactionScript(
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
				validSyntax
				!errors
			}
	}

	def 'test can evaluate a reaction PRE script'() {

		setup: 'Given an instance of ApiActionScriptProcessor'

			ActionRequest actionRequest = new ActionRequest(['property1': 'value1', 'format': 'xml'])
			ApiActionResponse actionResponse = new ApiActionResponse()
			AssetFacade asset = new AssetFacade(null, [:], true)
			TaskFacade task = new TaskFacade()
			ApiActionJob job = new ApiActionJob()

		when: 'A PRE script is evaluated'
			String script = """
				request.params.format = 'json'
				request.headers.add('header1', 'value1')
						
				request.config.setProperty('proxyAuthHost', '123.88.23.42')
				request.config.setProperty('proxyAuthPort', 8080)
			""".stripIndent()

			Map<String, ?> response = service.invokeReactionScript(
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

	def 'test can evaluate a reaction STATUS script'() {

		setup: 'Given an instance of ApiActionScriptProcessor'

			ActionRequest actionRequest = new ActionRequest(['property1': 'value1', 'format': 'xml'])
			ApiActionResponse actionResponse = new ApiActionResponse()
			actionResponse.data = 'anything'
			actionResponse.status = ReactionHttpStatus.OK

			AssetFacade asset = new AssetFacade(null, [:], true)
			TaskFacade task = new TaskFacade()
			ApiActionJob job = new ApiActionJob()

		when: 'A STATUS script is evaluated'
			String script = """
				if (response.status == SC.OK) {
					return SUCCESS
				} else {
					return ERROR
				}
			""".stripIndent()

			Map<String, ?> response = service.invokeReactionScript(
					ReactionScriptCode.STATUS,
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

			AssetFacade asset = new AssetFacade(new AssetEntity([assetType: 'Database']), [:], true)
			AssetComment assetComment = new AssetComment([status: 'Ready'])
			//TaskFacade task = new TaskFacade(new AssetComment())
			def taskServiceMock = [
				getAutomaticPerson: { -> new Person() },
				setTaskStatus: { AssetComment task, String status, Person whom ->
					assetComment.status = 'Completed'
					assetComment
				}
			] as TaskService

			TaskFacade task = applicationContext.getBean(TaskFacade, assetComment)
			task.taskService = taskServiceMock


			ApiActionJob job = new ApiActionJob()

		when: 'A STATUS script is evaluated'
			String script = """
				// Check to see if the asset is a VM
				if ( asset.isaDevice() || asset.isaDatabase() ) {
					task.done()
				}
			""".stripIndent()

			Map<String, ?> response = service.invokeReactionScript(
					ReactionScriptCode.SUCCESS,
					script,
					actionRequest,
					actionResponse,
					task,
					asset,
					job
			)

		then: 'Service result has a ReactionScriptCode'
			null == response.result

		and: 'The asset and task object received the correct messages'
			task.isDone()
	}

	void 'test can throw an Exception if a reaction STATUS script does not return a ReactionScriptCode'() {
		setup:
			ActionRequest actionRequest = new ActionRequest(['property1': 'value1', 'format': 'xml'])
			ApiActionResponse actionResponse = new ApiActionResponse()
			actionResponse.data = 'anything'
			actionResponse.status = ReactionHttpStatus.NOT_FOUND

			AssetFacade asset = new AssetFacade(null, [:], true)
			TaskFacade task = new TaskFacade()
			ApiActionJob job = new ApiActionJob()


		when: 'A STATUS script is evaluated that does not return a an instance of ReactionScriptCode'
			String script = """
				 if (response.status == SC.OK) {
					return SUCCESS
				 } 
			""".stripIndent()

			service.invokeReactionScript(
					ReactionScriptCode.STATUS,
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

	void 'test can throw an Exception with i18n message if a reaction STATUS script does not return a ReactionScriptCode'() {
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

			AssetFacade asset = new AssetFacade(null, [:], true)
			TaskFacade task = new TaskFacade()
			ApiActionJob job = new ApiActionJob()


		when: 'A STATUS script is evaluated that does not return a an instance of ReactionScriptCode'
			String script = """
				 if (response.status == SC.OK) {
					return SUCCESS
				 } 
			""".stripIndent()

			service.invokeReactionScript(
					ReactionScriptCode.STATUS,
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

	void 'test can validate syntax for a list fo scripts with error messages'() {

		given:
			List<ApiActionScriptCommand> scripts = [
					new ApiActionScriptCommand(code: ReactionScriptCode.STATUS, script: 'if (response.status == SC.OK) \n   return SUCCESS\n} else {\n   return ERROR\n}'),
					new ApiActionScriptCommand(code: ReactionScriptCode.SUCCESS, script: 'task.done()')
			]

		when: 'a List of scripts is evaluated'
			List<Map<String, ?>> results = service.validateSyntax(scripts)

		then: 'service returns a list of results associated with codes'
			results.size() == 2
			with(results[0]) {
				!validSyntax
				errors.size() == 1
				with(errors[0]) {
					startLine == 3
					endLine == 3
					startColumn == 1
					endColumn == 2
					fatal == true
					message == 'unexpected token: } @ line 3, column 1.'
			}   }

			with(results[1]) {
				validSyntax
				!errors
			}
	}
}
