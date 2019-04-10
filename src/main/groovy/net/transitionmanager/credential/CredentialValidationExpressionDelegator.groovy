package net.transitionmanager.credential

import net.transitionmanager.exception.InvalidSyntaxException

/**
 * This class is used to limit the methods that are available in the DSL
 * scripting language to the methods body, header and status. This also
 * implementes the methodMissing which will catch any lines that don't start
 * with one of the three methods.
 */
class CredentialValidationExpressionDelegator {

	// Only expose the three DSL methods on the CredentialValidationExpression that
	// are allow in the DSL syntax
	@Delegate(includes=['body', 'header' ,'status'])

	CredentialValidationExpression cve

	CredentialValidationExpressionDelegator(CredentialValidationExpression cve) {
		this.cve = cve
	}

	def methodMissing(String name, args) {
		throw new InvalidSyntaxException("No method $name found")
    }
}
