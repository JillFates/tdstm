package net.transitionmanager.credential

import net.transitionmanager.service.InvalidSyntaxException

/**
 * Contains the follow on methods that used by each of the DSL methods
 */
trait CredentialValidationExpressionContainsMissing {
	CredentialValidationExpression credentialValidationExpression

	Object contains(String value) {
		credentialValidationExpression.evaluation = ExpressionEvaluation.CONTAINS
		credentialValidationExpression.value = value
		return this
	}

	Object missing(String value) {
		credentialValidationExpression.evaluation = ExpressionEvaluation.MISSING
		credentialValidationExpression.value = value
		return this
	}

	def methodMissing(String methodName, args) {
		throw new InvalidSyntaxException( CredentialValidationExpression.INVALID_EXPRESSION_MSG )
	}
}