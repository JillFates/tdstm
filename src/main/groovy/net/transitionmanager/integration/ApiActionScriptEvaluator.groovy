package net.transitionmanager.integration

import groovy.transform.TimedInterrupt
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.control.messages.SyntaxErrorMessage

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

/**
 * Evaluate and check API action scripts.
 *
 * <pre>
 *     ApiActionScriptBinding binding = ...
 *     ApiActionScriptEvaluator evaluator = new ApiActionScriptEvaluator(binding)
 *
 *     evaluator.evaluate("""
 *          request.params.format = 'json'
 *          request.headers.add('header1', 'value1')
 *
 *          // Set the socket and connect to 5 seconds
 *          request.config.setProperty('httpClient.socketTimeout', 5000)
 *          request.config.setProperty('httpClient.connectionTimeout', 5000)
 *     """)
 * </pre>
 */
class ApiActionScriptEvaluator {

	private ApiActionScriptBinding binding

	ApiActionScriptEvaluator(ApiActionScriptBinding binding) {
		this.binding = binding
	}

	// ---------------------------------------------
	// ETL DSL evaluation/check syntax methods
	// ---------------------------------------------
	/**
	 * It returns the default compiler configuration used by an instance Api Action script invoker.
	 * It prepares an instance of CompilerConfiguration with an instance of ImportCustomizer
	 * and an instance of SecureASTCustomizer.
	 * @see CompilerConfiguration
	 * @see SecureASTCustomizer
	 * @see ImportCustomizer
	 * @return a default instance of CompilerConfiguration
	 */
	private CompilerConfiguration defaultCompilerConfiguration() {

		SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
		secureASTCustomizer.with {
			// allow closure creation for the ETL iterate command
			closuresAllowed = false
			// disallow method definitions
			methodDefinitionAllowed = false
			// Empty withe list means forbid imports
			importsWhitelist = ['org.springframework.beans.factory.annotation.Autowired']
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
		return configuration
	}

	/**
	 * Using an instance of GroovyShell, it evaluates an ETL script content
	 * using an Api action script invoker.
	 * @see GroovyShell#evaluate(java.lang.String)
	 * @param script an API Action script content
	 * @return
	 */
	@TimedInterrupt(600l)
	Object evaluate(String script) {
		return evaluate(script, defaultCompilerConfiguration())
	}

	/**
	 * Using an instance of GroovyShell, it evaluates an ETL script content
	 * used by an instance Api Action script invoker.
	 * It throws an InterruptedException when checks indicate code ran longer than desired
	 * @see GroovyShell#evaluate(java.lang.String)
	 * @param script an API Action script content
	 * @params configuration
	 * @return the result of evaluate API action script param
	 * @see TimedInterrupt

	 */
	@TimedInterrupt(600l)
	Object evaluate(String script, CompilerConfiguration configuration) {
		return new GroovyShell(this.class.classLoader, this.binding, configuration)
			.evaluate(script, ApiActionScriptEvaluator.class.name)
	}

	/**
	 * Using an instance of GroovyShell, it checks syntax of an API action script content.
	 * @see GroovyShell#evaluate(java.lang.String)
	 * @param script an API action script content
	 * @param configuration an instance of CompilerConfiguration
	 * @return a List of errors
	 */
	List<Map<String, ?>> checkSyntax(String script, CompilerConfiguration configuration) {

		List<Map<String, ?>> errors = []

		try{
			new GroovyShell(
				this.class.classLoader,
				this.binding,
				configuration
			).parse(script?.trim(), ApiActionScriptEvaluator.class.name)

		} catch(MultipleCompilationErrorsException cfe){
			ErrorCollector errorCollector = cfe.getErrorCollector()
			errors = errorCollector.getErrors()
		}

		return errors.collect { error ->

			if(error instanceof SyntaxErrorMessage){
				[
					startLine: error.cause?.startLine,
					endLine: error.cause?.endLine,
					startColumn: error.cause?.startColumn,
					endColumn: error.cause?.endColumn,
					fatal: error.cause?.fatal,
					message: error.cause?.message
				]
			} else{
				[
					startLine: null,
					endLine: null,
					startColumn: null,
					endColumn: null,
					fatal: true,
					message: error.cause?.message
				]

			}
		}
	}

	/**
	 * Using an instance of GroovyShell, it checks syntax of an API Action script content
	 * using this instance of the ApiActionScriptEvaluator instance and its defaultCompilerConfiguration
	 * @see ApiActionScriptEvaluator#defaultCompilerConfiguration()
	 * @see GroovyShell#parse(java.lang.String)
	 * @param script an ETL script content
	 * @param configuration an instance of CompilerConfiguration
	 * @return a List of errors
	 */
	List<Map<String, ?>> checkSyntax(String script) {
		return checkSyntax(script, defaultCompilerConfiguration())
	}


}
