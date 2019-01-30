package net.transitionmanager.integration

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.domain.Person
import net.transitionmanager.i18n.Message
import net.transitionmanager.service.MessageSourceService
import net.transitionmanager.service.TaskService
import net.transitionmanager.task.TaskFacade
import org.springframework.context.i18n.LocaleContextHolder
import spock.lang.Specification
import spock.lang.Unroll

import static ReactionHttpStatus.NOT_FOUND
import static ReactionHttpStatus.OK
import static net.transitionmanager.integration.ReactionScriptCode.ERROR
import static net.transitionmanager.integration.ReactionScriptCode.SUCCESS

@TestMixin(GrailsUnitTestMixin)
class ApiActionScriptSpec extends Specification {

	static doWithSpring = {
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

	@Unroll
	void 'test can create a script binding context based on a ReactionScriptCode.#reactionScriptCode'() {

		setup:
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
					.with(new ActionRequest(['property1': 'value1']))
					.with(new ApiActionResponse().asImmutable())
					.with(new AssetFacade(null, [:], true))
					.with(new TaskFacade())
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
			ReactionScriptCode.STATUS   || true       | true        | false   | false    | false  | true
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

	void 'test can throw an Exception if it tries to create a script binding without the correct context objects'() {

		when: 'Tries to create an instance of ApiActionScriptBinding for a ReactionScriptCode without the correct context objects'
			applicationContext.getBean(ApiActionScriptBindingBuilder)
					.with(new AssetFacade(null, [:], true))
					.with(new TaskFacade())
					.with(new ApiActionJob())
					.build(ReactionScriptCode.PRE)

		then: 'An Exception is thrown'
			ApiActionException e = thrown(ApiActionException)
			e.message == 'Can not build a biding context for PRE without request object'
	}

	void 'test can throw an Exception with i18n message if it tries to create a script binding without the correct context objects'() {

		setup:
			LocaleContextHolder.setLocale(Locale.FRENCH)
			messageSource.addMessage(
					Message.ApiActionInvalidBindingParams,
					Locale.FRENCH,
					'Impossible de créer un contexte de liaison pour {0} sans objet de {1}')

		when: 'Tries to create an instance of ApiActionScriptBinding for a ReactionScriptCode without the correct context objects'
			applicationContext.getBean(ApiActionScriptBindingBuilder)
					.with(new AssetFacade(null, [:], true))
					.with(new TaskFacade())
					.with(new ApiActionJob())
					.build(ReactionScriptCode.PRE)

		then: 'An Exception is thrown'
			Exception e = thrown(ApiActionException)
			e.message == 'Impossible de créer un contexte de liaison pour PRE sans objet de request'

		cleanup:
			LocaleContextHolder.resetLocaleContext()
	}

	void 'test can invoke a simple PRE Script to customize Http4 component with params and headers'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])

			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
					.with(request)
					.with(new ApiActionResponse())
					.with(new AssetFacade(null, [:], true))
					.with(new TaskFacade())
					.with(new ApiActionJob())
					.build(ReactionScriptCode.PRE)

		when: 'The PRE script is evaluated'
			new ApiActionScriptEvaluator(scriptBinding).evaluate("""
				request.params.format = 'json'
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
				
			""".stripIndent())

		then: 'All the correct variables were bound'
			scriptBinding.hasVariable('request')
			!scriptBinding.hasVariable('response')
			scriptBinding.hasVariable('task')
			scriptBinding.hasVariable('asset')
			scriptBinding.hasVariable('job')
			scriptBinding.hasVariable('SC')

		and: 'the request object was modified correctly'
			request.params.format == 'json'
			request.config.getProperty('httpClient.socketTimeout') == 5000
			request.config.getProperty('httpClient.connectionTimeout') == 5000
			request.config.getProperty('proxyAuthHost') == '123.88.23.42'
			request.config.getProperty('proxyAuthPort') == 8080
	}

	void 'test can throw an ApiActionException if PRE try to use response object'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])

			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
					.with(request)
					.with(new ApiActionResponse().asImmutable())
					.with(new AssetFacade(null, [:], true))
					.with(new TaskFacade())
					.with(new ApiActionJob())
					.build(ReactionScriptCode.PRE)

		when: 'The PRE script is evaluated'
			new ApiActionScriptEvaluator(scriptBinding).evaluate("""
				request.params.format = 'json'
				request.headers.add('header1', 'value1')
				
				if (response.status == SC.OK) {
				   return SUCCESS
				} else {
				   return ERROR
				}
			""".stripIndent())

		then: 'A ApiActionException is thrown'
			ApiActionException e = thrown(ApiActionException)
			e.message == 'There is no property with name response bound in this script context'
	}

	void 'test can throw an ApiActionException with an i18n message if PRE try to use response object'() {

		given:
			LocaleContextHolder.setLocale(Locale.FRENCH)
			messageSource.addMessage(Message.ApiActionNotBoundProperty,
					Locale.FRENCH,
					'Il ny a pas de propriété avec une {0} de nom liée dans ce contexte de script')

		and:
			ActionRequest request = new ActionRequest(['format': 'xml'])

			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
					.with(request)
					.with(new ApiActionResponse().asImmutable())
					.with(new AssetFacade(null, [:], true))
					.with(new TaskFacade())
					.with(new ApiActionJob())
					.build(ReactionScriptCode.PRE)

		when: 'The PRE script is evaluated'
			new ApiActionScriptEvaluator(scriptBinding).evaluate("""
				request.params.format = 'json'
				request.headers.add('header1', 'value1')
				
				if (response.status == SC.OK) {
				   return SUCCESS
				} else {
				   return ERROR
				}
			""".stripIndent())

		then: 'A ApiActionException is thrown'
			ApiActionException e = thrown(ApiActionException)
			e.message == 'Il ny a pas de propriété avec une response de nom liée dans ce contexte de script'

		cleanup:
			LocaleContextHolder.resetLocaleContext()
	}

	void 'test can invoke a simple EVALUATE Script and return a ReactionScriptCode result'() {

		setup:
			ApiActionResponse response = new ApiActionResponse()
			response.data = 'anything'
			response.status = status

			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
					.with(new ActionRequest(['property1': 'value1']))
					.with(response.asImmutable())
					.with(new AssetFacade(null, [:], true))
					.with(new TaskFacade())
					.with(new ApiActionJob())
					.build(ReactionScriptCode.STATUS)

		expect: 'The evaluation of the script returns a ReactionScriptCode'
			new ApiActionScriptEvaluator(scriptBinding).evaluate("""
				if (response.status == SC.OK) {
				   return SUCCESS
				} else {
				   return ERROR
				}
			""".stripIndent()) == reaction

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

	void 'test can invoke a simple SUCCESS Script to check Asset if asset is a Device or an Application and change a task to done'() {

		given:
			AssetEntity assetEntity = new AssetEntity(assetType: 'database')
			AssetComment assetComment = new AssetComment([status: 'Ready'])
			AssetFacade asset = new AssetFacade(assetEntity, [:], true)

			def taskServiceMock = [
				getAutomaticPerson: { -> new Person() },
				setTaskStatus: { AssetComment task, String status, Person whom ->
					assetComment.status = 'Completed'
					assetComment
				}
			] as TaskService

			TaskFacade task = applicationContext.getBean(TaskFacade, assetComment)
			task.taskService = taskServiceMock

			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
					.with(new ActionRequest(['property1': 'value1']))
					.with(new ApiActionResponse().asImmutable())
					.with(asset)
					.with(task)
					.with(new ApiActionJob())
					.build(ReactionScriptCode.SUCCESS)

		when: 'The script is evaluated'
			new ApiActionScriptEvaluator(scriptBinding).evaluate("""
				// Check to see if the asset is a VM
				if ( asset.isaDevice() || asset.isaDatabase() ) {
				   task.done()
				} 
				""".stripIndent())

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

	void 'test can invoke a simple FINALIZE Script to evaluate what has been performed'() {

		given:
			AssetEntity assetEntity = new AssetEntity(assetType: 'database')
			AssetComment assetComment = new AssetComment([status: 'Ready'])
			AssetFacade asset = new AssetFacade(assetEntity, [:], true)

			def taskServiceMock = [
				getAutomaticPerson: { -> new Person() },
				setTaskStatus: { AssetComment task, String status, Person whom ->
					assetComment.status = 'Completed'
					assetComment
				}
			] as TaskService

			TaskFacade task = applicationContext.getBean(TaskFacade, assetComment)
			task.taskService = taskServiceMock

			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
					.with(new ActionRequest(['property1': 'value1']))
					.with(new ApiActionResponse().asImmutable())
					.with(new AssetFacade(null, [:], true))
					.with(task)
					.with(new ApiActionJob())
					.build(ReactionScriptCode.FINAL)

		when: 'The script is evaluated'
			new ApiActionScriptEvaluator(scriptBinding).evaluate("""
				// Complete the task 
				task.done()
			""".stripIndent())

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
}

