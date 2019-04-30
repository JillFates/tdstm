package net.transitionmanager.credential

import net.transitionmanager.exception.InvalidSyntaxException

/**
 * Contains the follow on methods that used by each of the DSL methods
 */
trait CredentialValidationExpressionTraits {
	CredentialValidationExpression credentialValidationExpression

	Object contains(String value) {
		credentialValidationExpression.evaluation = ExpressionEvaluationEnum.CONTAINS
		credentialValidationExpression.value = value
		return this
	}

	Object missing(String value) {
		credentialValidationExpression.evaluation = ExpressionEvaluationEnum.MISSING
		credentialValidationExpression.value = value
		return this
	}

	Object equal(String value) {
		credentialValidationExpression.evaluation = ExpressionEvaluationEnum.EQUAL
		credentialValidationExpression.value = value
		return this
	}

	Object equal(Integer value) {
		credentialValidationExpression.evaluation = ExpressionEvaluationEnum.EQUAL
		credentialValidationExpression.value = value.toString()
		return this
	}

	def methodMissing(String methodName, args) {
		throw new InvalidSyntaxException( CredentialValidationExpression.INVALID_EXPRESSION_MSG )
	}
}
