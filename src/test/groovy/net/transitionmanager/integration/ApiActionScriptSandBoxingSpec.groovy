package net.transitionmanager.integration

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.service.MessageSourceService
import net.transitionmanager.task.TaskFacade
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import spock.lang.Specification

import static org.codehaus.groovy.syntax.Types.COMPARE_EQUAL
import static org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN
import static org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN_EQUAL
import static org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN
import static org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN_EQUAL
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_EQUAL
import static org.codehaus.groovy.syntax.Types.DIVIDE
import static org.codehaus.groovy.syntax.Types.EQUALS
import static org.codehaus.groovy.syntax.Types.LOGICAL_AND
import static org.codehaus.groovy.syntax.Types.LOGICAL_OR
import static org.codehaus.groovy.syntax.Types.MINUS
import static org.codehaus.groovy.syntax.Types.MINUS_MINUS
import static org.codehaus.groovy.syntax.Types.MOD
import static org.codehaus.groovy.syntax.Types.MULTIPLY
import static org.codehaus.groovy.syntax.Types.NOT
import static org.codehaus.groovy.syntax.Types.PLUS
import static org.codehaus.groovy.syntax.Types.PLUS_EQUAL
import static org.codehaus.groovy.syntax.Types.PLUS_PLUS
import static org.codehaus.groovy.syntax.Types.POWER

@TestMixin(GrailsUnitTestMixin)
class ApiActionScriptSandBoxingSpec extends Specification {

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

	void 'test can evaluate a PRE script using the default compiler configuration'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(new ApiActionResponse())
				.with(new AssetFacade(null, [:], true))
				.with(new TaskFacade())
				.with(new ApiActionJob())
				.build(ReactionScriptCode.PRE)

		and:
			ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(scriptBinding)

		when: 'The PRE script is evaluated'
			evaluator.evaluate("""
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

	void 'test can evaluate a PRE script using a custom compiler configuration'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(new ApiActionResponse())
				.with(new AssetFacade(null, [:], true))
				.with(new TaskFacade())
				.with(new ApiActionJob())
				.build(ReactionScriptCode.PRE)

		and:
			ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(scriptBinding)

			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.with {
				// allow closure creation for the ETL iterate command
				closuresAllowed = false
				// disallow method definitions
				methodDefinitionAllowed = false
				// Empty withe list means forbid imports
				importsWhitelist = []
				starImportsWhitelist = []
				// Language tokens allowed
				tokensWhitelist = [
					DIVIDE, PLUS, MINUS, MULTIPLY, MOD, POWER, PLUS_PLUS, MINUS_MINUS, PLUS_EQUAL, LOGICAL_AND,
					COMPARE_EQUAL, COMPARE_NOT_EQUAL, COMPARE_LESS_THAN, COMPARE_LESS_THAN_EQUAL, LOGICAL_OR, NOT,
					COMPARE_GREATER_THAN, COMPARE_GREATER_THAN_EQUAL, EQUALS, COMPARE_NOT_EQUAL, COMPARE_EQUAL
				].asImmutable()
				// Types allowed to be used (Including primitive types)
				constantTypesClassesWhiteList = [
					Object, Integer, Float, Long, Double, BigDecimal, String,
					Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
				].asImmutable()
				// Classes who are allowed to be receivers of method calls
				receiversClassesWhiteList = [
					Object, // TODO: This is too much generic class.
					Integer, Float, Double, Long, BigDecimal, String
				].asImmutable()
			}

			ImportCustomizer customizer = new ImportCustomizer()
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer

		when: 'The PRE script is evaluated'
			evaluator.evaluate("""
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
			""".stripIndent(),
				configuration)

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

	void 'test can evaluate an API script with comments using the default compiler configuration'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(new ApiActionResponse())
				.with(new AssetFacade(null, [:], true))
				.with(new TaskFacade())
				.with(new ApiActionJob())
				.build(ReactionScriptCode.PRE)

		and:
			ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(scriptBinding)

		when: 'The PRE script is evaluated'
			evaluator.evaluate("""
				// Script supports one line comments
				request.params.format = 'json'
				request.headers.add('header1', 'value1')
				/**
				 *	And multiple Lines comments
				 */
				request.config.setProperty('httpClient.socketTimeout', 5000)
				request.config.setProperty('httpClient.connectionTimeout', 5000)
				
				request.config.setProperty('proxyAuthHost', '123.88.23.42')
				request.config.setProperty('proxyAuthPort', 8080)
				request.config.setProperty('Exchange.CHARSET_NAME', 'ISO-8859-1')
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

	void 'test can throw an Exception if an API Action script has a closure defined using default compiler configuration'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(new ApiActionResponse())
				.with(new AssetFacade(null, [:], true))
				.with(new TaskFacade())
				.with(new ApiActionJob())
				.build(ReactionScriptCode.PRE)

		and:
			ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(scriptBinding)

		when: 'The PRE script is evaluated'
			evaluator.evaluate("""
				request.params.format = 'json'
				def greeting = { String name -> "Hello, \$name!" }
				assert greeting('Diego') == 'Hello, Diego!'
			""".stripIndent())

		then: 'An MissingMethodException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors[0].cause*.message == ['Closures are not allowed']
	}

	void 'test can evaluate an API Action script has a closure defined using a custom compiler configuration'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(new ApiActionResponse())
				.with(new AssetFacade(null, [:], true))
				.with(new TaskFacade())
				.with(new ApiActionJob())
				.build(ReactionScriptCode.PRE)

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.with {
				// allow closure creation for the ETL iterate command
				closuresAllowed = true
				// disallow method definitions
				methodDefinitionAllowed = false
				// Empty withe list means forbid imports
				importsWhitelist = []
				starImportsWhitelist = []
				// Language tokens allowed
				tokensWhitelist = [
					DIVIDE, PLUS, MINUS, MULTIPLY, MOD, POWER, PLUS_PLUS, MINUS_MINUS, PLUS_EQUAL, LOGICAL_AND,
					COMPARE_EQUAL, COMPARE_NOT_EQUAL, COMPARE_LESS_THAN, COMPARE_LESS_THAN_EQUAL, LOGICAL_OR, NOT,
					COMPARE_GREATER_THAN, COMPARE_GREATER_THAN_EQUAL, EQUALS, COMPARE_NOT_EQUAL, COMPARE_EQUAL
				].asImmutable()
				// Types allowed to be used (Including primitive types)
				constantTypesClassesWhiteList = [
					Object, Integer, Float, Long, Double, BigDecimal, String,
					Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
				].asImmutable()
				// Classes who are allowed to be receivers of method calls
				receiversClassesWhiteList = [
					Object, // TODO: This is too much generic class.
					Integer, Float, Double, Long, BigDecimal, String
				].asImmutable()
			}

			ImportCustomizer customizer = new ImportCustomizer()

			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer

			ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(scriptBinding)

		when: 'The PRE script with a closure is evaluated'
			evaluator.evaluate("""
				def contentType = { String type -> "application/\$type" }
				request.params.format = 'json'
				request.headers.add('header1', 'value1')
				request.config.setProperty('httpClient.socketTimeout', 5000)
				request.config.setProperty('httpClient.connectionTimeout', 5000)
				
				request.config.setProperty('proxyAuthHost', '123.88.23.42')
				request.config.setProperty('proxyAuthPort', 8080)
				
				request.config.setProperty('Exchange.CHARSET_NAME', 'ISO-8859-1')
				request.config.setProperty('Exchange.CONTENT_TYPE', contentType('json'))
			""".stripIndent(),
				configuration)

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
			request.config.getProperty('Exchange.CONTENT_TYPE') == 'application/json'
	}

	void 'test can throw an Exception if an API Action script has a method defined using default compiler configuration'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(new ApiActionResponse())
				.with(new AssetFacade(null, [:], true))
				.with(new TaskFacade())
				.with(new ApiActionJob())
				.build(ReactionScriptCode.PRE)

		and:
			ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(scriptBinding)

		when: 'The PRE script is evaluated'
			evaluator.evaluate("""
				request.params.format = 'json'
				def greeting(String name){ 
					"Hello, \$name" 
				}
				assert greeting('Diego') == 'Hello, Diego!'
			""".stripIndent())

		then: 'An MissingMethodException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors[0].cause*.message == ['Method definitions are not allowed']
	}

	void 'test can evaluate an API Action script has a method defined using a custom compiler configuration'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(new ApiActionResponse())
				.with(new AssetFacade(null, [:], true))
				.with(new TaskFacade())
				.with(new ApiActionJob())
				.build(ReactionScriptCode.PRE)

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.with {
				// allow closure creation for the ETL iterate command
				closuresAllowed = false
				// disallow method definitions
				methodDefinitionAllowed = true
				// Empty withe list means forbid imports
				importsWhitelist = []
				starImportsWhitelist = []
				// Language tokens allowed
				tokensWhitelist = [
					DIVIDE, PLUS, MINUS, MULTIPLY, MOD, POWER, PLUS_PLUS, MINUS_MINUS, PLUS_EQUAL, LOGICAL_AND,
					COMPARE_EQUAL, COMPARE_NOT_EQUAL, COMPARE_LESS_THAN, COMPARE_LESS_THAN_EQUAL, LOGICAL_OR, NOT,
					COMPARE_GREATER_THAN, COMPARE_GREATER_THAN_EQUAL, EQUALS, COMPARE_NOT_EQUAL, COMPARE_EQUAL
				].asImmutable()
				// Types allowed to be used (Including primitive types)
				constantTypesClassesWhiteList = [
					Object, Integer, Float, Long, Double, BigDecimal, String,
					Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
				].asImmutable()
				// Classes who are allowed to be receivers of method calls
				receiversClassesWhiteList = [
					Object, // TODO: This is too much generic class.
					Integer, Float, Double, Long, BigDecimal, String
				].asImmutable()
			}

			ImportCustomizer customizer = new ImportCustomizer()

			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer

			ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(scriptBinding)

		when: 'The PRE script with a closure is evaluated'
			evaluator.evaluate("""
				def contentType(String type){ 
					return "application/\$type" 
				}
				request.params.format = 'json'
				request.headers.add('header1', 'value1')
				request.config.setProperty('httpClient.socketTimeout', 5000)
				request.config.setProperty('httpClient.connectionTimeout', 5000)
				
				request.config.setProperty('proxyAuthHost', '123.88.23.42')
				request.config.setProperty('proxyAuthPort', 8080)
				
				request.config.setProperty('Exchange.CHARSET_NAME', 'ISO-8859-1')
				request.config.setProperty('Exchange.CONTENT_TYPE', contentType('json'))
			""".stripIndent(),
				configuration)

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
			request.config.getProperty('Exchange.CONTENT_TYPE') == 'application/json'
	}

	void 'test can throw an Exception if an API Action script has an import defined using default compiler configuration'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(new ApiActionResponse())
				.with(new AssetFacade(null, [:], true))
				.with(new TaskFacade())
				.with(new ApiActionJob())
				.build(ReactionScriptCode.PRE)

		and:
			ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(scriptBinding)

		when: 'The PRE script is evaluated'
			evaluator.evaluate("""
				import java.lang.Math
				
				request.params.format = 'json'
				Math.max 10, 100
			""".stripIndent())

		then: 'An MissingMethodException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors[0].cause*.message == ['Importing [java.lang.Math] is not allowed']
	}

	void 'test can evaluate an API Action script has an import defined using a custom compiler configuration'() {

		given:
			ActionRequest request = new ActionRequest(['format': 'xml'])
			ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
				.with(request)
				.with(new ApiActionResponse())
				.with(new AssetFacade(null, [:], true))
				.with(new TaskFacade())
				.with(new ApiActionJob())
				.build(ReactionScriptCode.PRE)

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.with {
				// allow closure creation for the ETL iterate command
				closuresAllowed = false
				// disallow method definitions
				methodDefinitionAllowed = false
				// Language tokens allowed
				tokensWhitelist = [
					DIVIDE, PLUS, MINUS, MULTIPLY, MOD, POWER, PLUS_PLUS, MINUS_MINUS, PLUS_EQUAL, LOGICAL_AND,
					COMPARE_EQUAL, COMPARE_NOT_EQUAL, COMPARE_LESS_THAN, COMPARE_LESS_THAN_EQUAL, LOGICAL_OR, NOT,
					COMPARE_GREATER_THAN, COMPARE_GREATER_THAN_EQUAL, EQUALS, COMPARE_NOT_EQUAL, COMPARE_EQUAL
				].asImmutable()
				// Types allowed to be used (Including primitive types)
				constantTypesClassesWhiteList = [
					Object, Integer, Float, Long, Double, BigDecimal, String,
					Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
				].asImmutable()
				// Classes who are allowed to be receivers of method calls
				receiversClassesWhiteList = [
					Math,
					Object, // TODO: This is too much generic class.
					Integer, Float, Double, Long, BigDecimal, String
				].asImmutable()
			}

			ImportCustomizer customizer = new ImportCustomizer()
//			customizer.addStaticStars('java.lang.Math')
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer

			ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(scriptBinding)

		when: 'The PRE script with a closure is evaluated'
			evaluator.evaluate("""
				import java.lang.Math
				
				request.params.format = 'json'
				request.headers.add('header1', 'value1')
				request.config.setProperty('httpClient.socketTimeout', 5000)
				request.config.setProperty('httpClient.connectionTimeout', 5000)
				
				request.config.setProperty('proxyAuthHost', '123.88.23.42')
				request.config.setProperty('proxyAuthPort', Math.max(80, 8080))
				
				request.config.setProperty('Exchange.CHARSET_NAME', 'ISO-8859-1')
				request.config.setProperty('Exchange.CONTENT_TYPE', 'application/json')
			""".stripIndent(),
				configuration)

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
			request.config.getProperty('Exchange.CONTENT_TYPE') == 'application/json'
	}
}

