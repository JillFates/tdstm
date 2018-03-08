package net.transitionmanager.credential

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import net.transitionmanager.service.InvalidSyntaxException

/**
 * Used to parse a Credential Validation Expression string and to perform
 * the actual validationExpression evaluation.  This will parse the expression
 * using the Groovy DSL language
 */
class CredentialValidationExpression {

	private ExpressionAttributeEnum attribute = null
	private ExpressionEvaluationEnum evaluation = null
	private String headerName = null
	private String value = null

	static final String INVALID_EXPRESSION_MSG = 'Invalid expression (e.g. contains|missing "some text)"'
	static final String UNRECOGNIZED_EXPRESSION_MSG = 'Unrecognized expression'
	static final String INVALID_BODY_EXPRESSION_MSG = 'Invalid body expression (e.g. body content contains "Welcome")'
	static final String INVALID_HEADER_EXPRESSION_MSG = 'Invalid header expression (e.g. header Location contains "/welcome")'
	static final String INVALID_STATUS_EXPRESSION_MSG = 'Invalid status expression (e.g. status code contains "200")'
	static final String EMPTY_EXPRESSION_MSG = 'An expression is required'

	CredentialValidationExpression(String expression) {
		parse(expression)
	}

	/**
	 * Used by the consumer to evaluate the expression with a HTTP Response
	 * to determine if the expected response was received.
	 * @parameter response - the HTTP response that was received from the authentication call
	 * @return true if the evaluation matched otherwise false
	 */
	// boolean evaluate(HttpResponse response) {
	boolean evaluate() {
		String comparer = 'blah'
		/*
		switch (attribute) {
			case ExpressionAttributeEnum.BODY:

			case ExpressionAttributeEnum.HEADER:
				// get header with headerName
				comparer = response.getHeader(headerName)
				break
			case ExpressionAttributeEnum.STATUS:
		}

		switch (evaluation) {
			case ExpressionEvaluation.EQUAL:
				return comparer == this.value
			case ExpressionEvaluation.CONTAINS:

			ExpressionEvaluation.MISSING:
		}
		*/
	}

	/**
	 * Used to parse the expression by the DSL process. Upon completion the four
	 * class private properties should be populated appropriately.
	 * @param expression - the Credential validationExpression to be parsed
	 */
	private void parse(String expression) {

		if (! expression ) {
			throw new InvalidSyntaxException(EMPTY_EXPRESSION_MSG)
		}

		Binding binding = new CredentialValidationExpressionBinding( )

		SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
		secureASTCustomizer.closuresAllowed = false             // disallow closure creation
		secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
		secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
		secureASTCustomizer.starImportsWhitelist = []

		ImportCustomizer customizer = new ImportCustomizer()

		CompilerConfiguration cc = new CompilerConfiguration()
		cc.addCompilationCustomizers customizer, secureASTCustomizer

		cc.setScriptBaseClass(DelegatingScript.class.getName())
		def cl = this.class.classLoader
		GroovyShell sh = new GroovyShell( cl, binding, cc)

		DelegatingScript script = (DelegatingScript)sh.parse(expression)

		// Using the CredentialValidationExpressionDelegator so that only the
		// DSL method names (body, header & status) are exposed to the scripting language
		script.setDelegate(new CredentialValidationExpressionDelegator(this))

		script.run()

		println this.dump()

		// Blow up if any of the require attributes were not specified
		if (! attribute )  {
			throw new InvalidSyntaxException(UNRECOGNIZED_EXPRESSION_MSG)
		}

		// Blow up if header is missing the headerName (e.g. header '' contains '')
		if (attribute == ExpressionAttributeEnum.HEADER && !headerName) {
			throw new InvalidSyntaxException(INVALID_HEADER_EXPRESSION_MSG)
		}

		// Blow up if the value is null
		if (value == null) {
			String msg
			if (attribute == ExpressionAttributeEnum.BODY) {
				msg = INVALID_BODY_EXPRESSION_MSG
			} else if (attribute == ExpressionAttributeEnum.HEADER) {
				msg = INVALID_HEADER_EXPRESSION_MSG
			} else {
				msg = INVALID_STATUS_EXPRESSION_MSG
			}
			throw new InvalidSyntaxException(msg)
		}

	}

	// Below are the binding methods used by the Binding class. Each of these
	// methods represents one of the method names in the DSL language. The method
	// names consist of: body, header and status

	CredentialValidationExpressionBody body(String content) {
		return new CredentialValidationExpressionBody( this, content )
	}

	CredentialValidationExpressionHeader header(String headerName) {
		return new CredentialValidationExpressionHeader( this, headerName )
	}

	CredentialValidationExpressionStatus status(String code) {
		return new CredentialValidationExpressionStatus( this, code )
	}
}