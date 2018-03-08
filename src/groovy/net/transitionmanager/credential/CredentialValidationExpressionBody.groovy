package net.transitionmanager.credential

import net.transitionmanager.service.InvalidSyntaxException

class CredentialValidationExpressionBody implements CredentialValidationExpressionTraits {
	/**
	 * Process the body command that requires the 'content' argument, which is just a place holder for the
	 * command argument pattern of: method param
	 * @param cse - the CredentialValidationExpression that the script will populate the arguments into
	 * @param content - a string with the value 'content'
	 *
	 * Usage:
	 * 		body content contains|missing 'value'
	 */
	CredentialValidationExpressionBody(CredentialValidationExpression cse, String content) {
		credentialValidationExpression = cse
		if (content != 'content') {
			throw new InvalidSyntaxException(CredentialValidationExpression.INVALID_BODY_EXPRESSION_MSG)
		}
		credentialValidationExpression.attribute = ExpressionAttributeEnum.BODY
	}
}
