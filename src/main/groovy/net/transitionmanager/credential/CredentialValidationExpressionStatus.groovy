package net.transitionmanager.credential

import net.transitionmanager.exception.InvalidSyntaxException

class CredentialValidationExpressionStatus implements CredentialValidationExpressionTraits {

	/**
	 * Process the 'status' command that requires the 'content' argument which is just a place holder for the
	 * command argument pattern.
	 * @param cse - the CredentialValidationExpression that the script will populate the arguments into
	 * @param content - a string with the value 'content'
	 *
	 * Usage:
	 * 		status code contains|missing '200'
	 */
	CredentialValidationExpressionStatus(CredentialValidationExpression cse, String code) {
		credentialValidationExpression = cse

		if (code != 'code') {
			throw new InvalidSyntaxException(CredentialValidationExpression.INVALID_STATUS_EXPRESSION_MSG)
		}
		credentialValidationExpression.attribute = ExpressionAttributeEnum.STATUS
	}
}
