package net.transitionmanager.credential

import net.transitionmanager.service.InvalidSyntaxException

class CredentialValidationExpressionHeader implements CredentialValidationExpressionTraits {

	/**
	 * Process the 'header' command that expects a header name as an argument
	 * command argument pattern.
	 * @param cse - the CredentialValidationExpression that the script will populate the arguments into
	 * @param headerName - a string with the name of the header to inspect
	 *
	 * Usage:
	 * 		header SomeHeaderName contains|missing 'value'
	 */
	CredentialValidationExpressionHeader(CredentialValidationExpression cse, String headerName) {
		credentialValidationExpression = cse

		if (! headerName) {
			throw new InvalidSyntaxException(CredentialValidationExpression.INVALID_HEADER_EXPRESSION_MSG)
		}

		credentialValidationExpression.attribute = ExpressionAttributeEnum.HEADER
		credentialValidationExpression.headerName = headerName
	}
}
